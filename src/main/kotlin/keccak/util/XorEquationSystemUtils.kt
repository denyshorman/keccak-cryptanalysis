package keccak.util

import keccak.*
import keccak.XorEquationSystem
import java.io.File
import java.util.*
import kotlin.math.min
import kotlin.random.Random

private val XorEquationPattern = "^([01]+)\\|([01])$".toRegex()

fun XorEquationSystem(rows: Int, cols: Int, vararg equations: String): XorEquationSystem {
    val system = XorEquationSystem(rows, cols)

    var i = 0
    while (i < equations.size) {
        val eq = equations[i]

        val matched = XorEquationPattern.matchEntire(eq) ?: throw RuntimeException("Equation is not correct")

        val (l, r) = matched.destructured

        var j = 0
        while (j < l.length) {
            system.equations[i][j] = l[j].toBoolean()
            system.results[i] = r.toBoolean()
            j++
        }

        i++
    }

    return system
}

fun XorEquationSystem.set(eqIndex: Int, equation: String, humanReadable: Boolean) {
    if (humanReadable) {
        TODO()
    } else {
        var bitIndex = 0
        while (bitIndex < cols) {
            if (equation[bitIndex] == '1') {
                equations[eqIndex].set(bitIndex)
            }
            bitIndex++
        }

        if (equation[equation.length - 1] == '1') {
            results.set(eqIndex)
        }
    }
}

fun XorEquationSystem.toLittleEndianBytes(): Array<XorEquationSystem> {
    return Array(Long.SIZE_BYTES) { byteIndex ->
        val system = XorEquationSystem(Byte.SIZE_BITS, cols)

        var i = Long.SIZE_BITS - (byteIndex + 1) * Byte.SIZE_BITS
        val limit = i + Byte.SIZE_BITS
        var j = 0
        while (i < limit) {
            system.equations[j].xor(equations[i])
            system.results[j] = results[i]
            i++
            j++
        }

        system
    }
}

fun XorEquationSystem.toByte(): Byte {
    val system = this
    var byte: Byte = 0

    var bitIndex = 0
    while (bitIndex < Byte.SIZE_BITS) {
        byte = byte.setBit(bitIndex, system.results[bitIndex])
        bitIndex++
    }

    return byte
}

fun XorEquationSystem.toLong(): Long {
    val system = this
    var long = 0L

    var bitIndex = 0
    while (bitIndex < Long.SIZE_BITS) {
        long = long.setBit(bitIndex, system.results[bitIndex])
        bitIndex++
    }

    return long
}

fun XorEquationSystem.setVariables() {
    var i = 0
    while (i < rows && i < cols) {
        equations[i][i] = true
        i++
    }
}

fun XorEquationSystem.toBitEquation(eqIndex: Int): XorEquation {
    val eq = equations[eqIndex].clone() as BitSet
    val res = results[eqIndex]
    return XorEquation(cols, eq, res)
}

fun XorEquationSystem.toNodeEquationSystem(): NodeEquationSystem {
    val variables = Array(cols) { Variable("x${it + 1}") }

    val equations = Array(rows) { eqIndex ->
        val left = LinkedList<Node>()

        var bitIndex = equations[eqIndex].nextSetBit(0)
        while (bitIndex >= 0) {
            left.add(Variable("x${bitIndex + 1}"))
            bitIndex = equations[eqIndex].nextSetBit(bitIndex + 1)
        }

        NodeEquation(Xor(left), Bit(results[eqIndex]))
    }

    return NodeEquationSystem(equations, variables)
}

fun XorEquationSystem.toFile(
    file: File,
    eqStartIndex: Int = 0,
    eqCount: Int = rows,
    humanReadable: Boolean = false,
) {
    file.outputStream().writer(Charsets.US_ASCII).use { writer ->
        var eqIndex = eqStartIndex
        val limit = min(eqIndex + eqCount, rows)

        if (humanReadable) {
            while (eqIndex < limit) {
                var bitIndex = 0
                val variables = LinkedList<String>()

                while (bitIndex < cols) {
                    if (equations[eqIndex][bitIndex]) {
                        variables.add("x$bitIndex")
                    }
                    bitIndex++
                }

                val res = results[eqIndex]

                if (variables.isNotEmpty()) {
                    val equation = variables.joinToString(" + ", postfix = " = ${res.toNumChar()}")
                    writer.appendLine(equation)
                } else {
                    if (res) {
                        writer.appendLine("0 = 1")
                    }
                }

                eqIndex++
            }
        } else {
            while (eqIndex < limit) {
                var bitIndex = 0
                while (bitIndex < cols) {
                    writer.append(equations[eqIndex][bitIndex].toNumChar())
                    bitIndex++
                }
                writer.append('|')
                writer.append(results[eqIndex].toNumChar())

                eqIndex++

                if (eqIndex != limit) {
                    writer.append('\n')
                }
            }
        }
    }
}

fun Array<XorEquationSystem>.toLong(): Long {
    val bytes = this
    var value = 0L

    var byteIndex = 0
    var bitIndex = 0
    while (byteIndex < size) {
        var bitIndexInByte = 0
        while (bitIndexInByte < bytes[byteIndex].cols) {
            value = value.setBit(bitIndex, bytes[byteIndex].results[bitIndexInByte])
            bitIndex++
            bitIndexInByte++
        }
        byteIndex++
    }

    return value
}

fun Array<XorEquationSystem>.littleEndianBytesToLong(cols: Int): XorEquationSystem {
    val bytes = this
    val system = XorEquationSystem(Long.SIZE_BITS, cols)

    var bitIndex = 0
    while (bitIndex < Long.SIZE_BITS) {
        val byteIndex = Long.SIZE_BYTES - bitIndex / Byte.SIZE_BITS - 1
        val newBitIndex = bitIndex % Byte.SIZE_BITS
        system.equations[bitIndex] = bytes[byteIndex].equations[newBitIndex]
        system.results[bitIndex] = bytes[byteIndex].results[newBitIndex]
        bitIndex++
    }

    return system
}

fun Array<XorEquationSystem>.merge(): XorEquationSystem {
    val rows = sumOf { it.rows }
    val cols = maxOf { it.cols }

    val mergeSystem = XorEquationSystem(rows, cols)

    var mergedEqIndex = 0
    var systemIndex = 0

    while (systemIndex < size) {
        var systemEqIndex = 0
        while (systemEqIndex < this[systemIndex].rows) {
            mergeSystem.equations[mergedEqIndex] = this[systemIndex].equations[systemEqIndex].clone() as BitSet
            mergeSystem.results[mergedEqIndex] = this[systemIndex].results[systemEqIndex]
            systemEqIndex++
            mergedEqIndex++
        }
        systemIndex++
    }

    return mergeSystem
}

fun randomXorEquationSystem(rows: Int, cols: Int, random: Random = Random): XorEquationSystem {
    val system = XorEquationSystem(rows, cols)
    var row = 0
    while (row < rows) {
        var col = 0
        while (col < cols) {
            system.equations[row][col] = random.nextBoolean()
            col++
        }
        system.results[row] = random.nextBoolean()
        row++
    }
    return system
}
