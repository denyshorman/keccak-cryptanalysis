package keccak.util

import keccak.*
import keccak.XorEquationSystem
import java.io.File
import java.util.*
import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

private val XorEquationPattern = "^([01]+)\\|([01])$".toRegex()
private val XorHumanEquationPattern = """(\d+)""".toRegex()

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

fun XorEquationSystem.toHumanString(
    varPrefix: String = "x",
    varOffset: Int = 0,
): String {
    val sb = StringBuilder()
    var i = 0
    while (i < rows) {
        val eq = equations[i].toXorString(freeBit = false, varPrefix, varOffset)
        val res = results[i].toNumChar()
        sb.appendLine("$eq = $res")
        i++
    }
    return sb.toString()
}

fun XorEquationSystem.set(eqIndex: Int, equation: String, humanReadable: Boolean) {
    if (humanReadable) {
        val setBitIndices = XorHumanEquationPattern.findAll(equation).map { it.value.toInt() }.toList()

        setBitIndices.forEachIndexed { index, bitIndex ->
            if (index < setBitIndices.size - 1) {
                equations[eqIndex].set(bitIndex)
            } else {
                results.setIfTrue(eqIndex, bitIndex == 1)
            }
        }
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

fun XorEquationSystem.toNodeEquationSystem(varPrefix: String = "x", varOffset: Int = 1): NodeEquationSystem {
    val equations = Array(rows) { eqIndex ->
        val left = LinkedList<Node>()

        var bitIndex = equations[eqIndex].nextSetBit(0)
        while (bitIndex >= 0) {
            left.add(Variable("$varPrefix${bitIndex + varOffset}"))
            bitIndex = equations[eqIndex].nextSetBit(bitIndex + 1)
        }

        NodeEquation(Xor(left), Bit(results[eqIndex]))
    }

    return NodeEquationSystem(equations)
}

fun XorEquationSystem.toSecondOrderNodeEquationSystem(varPrefix: String = "x", varOffset: Int = 0): NodeEquationSystem {
    val variablesCount = secondOrderSystemVarsCount(cols)

    val equations = Array(rows) { eqIndex ->
        val left = LinkedList<Node>()
        var bitIndex = equations[eqIndex].nextSetBit(0)

        while (bitIndex >= 0) {
            val (leftIndex, rightIndex) = calcCombinationPartialIndex(bitIndex, variablesCount)

            if (leftIndex == rightIndex) {
                left.add(Variable("$varPrefix${leftIndex + varOffset}"))
            } else {
                left.add(Variable("$varPrefix${leftIndex + varOffset}")*Variable("$varPrefix${rightIndex + varOffset}"))
            }

            bitIndex = equations[eqIndex].nextSetBit(bitIndex + 1)
        }

        NodeEquation(Xor(left), Bit(results[eqIndex]))
    }

    return NodeEquationSystem(equations)
}

fun XorEquationSystem.extractFirstOrderEquationsFromSecondOrderSystem(): XorEquationSystem {
    val system = XorEquationSystem(rows = 128, cols = secondOrderSystemVarsCount(cols))

    var i = 0
    while (i < rows) {
        if (!equations[i].isSecondOrderEq(system.cols)) {
            val bitSet = BitSet(system.cols)
            equations[i].iterateOverAllSetBits { x ->
                val (y, _) = calcCombinationPartialIndex(x, system.cols)
                bitSet.set(y)
            }
            system.append(bitSet, results[i])
        }
        i++
    }

    return system
}

fun XorEquationSystem.toCharacteristicEquation(
    characteristicVarPrefix: String = "a",
    varPrefix: String = "x",
    useZeroPosition: Boolean = true,
): Node {
    val rowsInUse = rows + if (useZeroPosition) 0 else 1
    val characteristicVarsCount = ceil(log2(rowsInUse.toDouble())).toLong()
    val characteristicFunctions = Array<Node>(cols) { Bit(false) }
    var characteristicFunctionOfFreeBit: Node = Bit(false)
    var characteristicFunction: Node = Bit(false)
    val characteristicVarsIterator = CombinationIteratorSimple(characteristicVarsCount.toInt())

    if (!useZeroPosition) {
        characteristicVarsIterator.next()
    }

    var i = 0
    while (i < rows) {
        var j = 0
        val resultFunc = characteristicVarsIterator.combination.toNode(characteristicVarPrefix)

        while (j < cols) {
            if (equations[i][j]) {
                characteristicFunctions[j] += resultFunc
            }
            j++
        }

        if (results[i]) {
            characteristicFunctionOfFreeBit += resultFunc
        }

        characteristicVarsIterator.next()
        i++
    }

    i = 0
    while (i < cols) {
        val term = characteristicFunctions[i]*Variable("$varPrefix$i")
        characteristicFunction += term.simplify()
        i++
    }
    characteristicFunction += characteristicFunctionOfFreeBit

    return characteristicFunction
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

fun secondOrderSystemVarsCount(secondOrderVarsCount: Int): Int {
    return (sqrt(0.25 + 2*secondOrderVarsCount) - 0.5).toInt()
}