package keccak

import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.math.min

object XorEquationSolver {
    //#region Public API
    fun solve(equations: Array<BitSet>, result: BitSet, varCount: Int) {
        var row = 0
        var col = 0

        while (row < equations.size && col < varCount) {
            var i = row
            var found = false

            while (i < equations.size) {
                if (equations[i].isEmpty && result[i]) {
                    throw NoSolution(i)
                }

                if (equations[i][col]) {
                    found = true
                    break
                }

                i++
            }

            if (found) {
                if (row != i) {
                    equations.exchange(row, i)
                    result.exchange(row, i)
                }

                i = row + 1

                while (i < equations.size) {
                    if (equations[i][col]) {
                        equations[i].xor(equations[row])
                        result[i] = result[i] xor result[row]

                        if (equations[i].isEmpty && result[i]) {
                            throw NoSolution(i)
                        }
                    }

                    i++
                }
            }

            row++
            col++
        }

        row = min(equations.size, varCount) - 1
        col = row

        while (row >= 0 && col >= 0) {
            if (equations[row].isEmpty) {
                if (result[row]) {
                    throw NoSolution(row)
                }
            } else {
                var i = row - 1

                while (i >= 0) {
                    if (equations[i][col]) {
                        equations[i].xor(equations[row])
                        result[i] = result[i] xor result[row]

                        if (equations[i].isEmpty && result[i]) {
                            throw NoSolution(i)
                        }
                    }

                    i--
                }
            }

            row--
            col--
        }
    }
    //#endregion

    //#region Exceptions
    data class NoSolution(val eqIndex: Int) : Throwable(null, null, false, false)
    //#endregion
}

fun <T> Array<T>.exchange(i: Int, j: Int) {
    val tmp = this[i]
    this[i] = this[j]
    this[j] = tmp
}

fun BitSet.exchange(i: Int, j: Int) {
    val tmp = this[i]
    this[i] = this[j]
    this[j] = tmp
}

fun bitSet(vararg values: Boolean): BitSet {
    val set = BitSet(values.size)
    values.forEachIndexed { index, value ->
        set[index] = value
    }
    return set
}

fun bitSet(vararg values: Int): BitSet {
    val set = BitSet(values.size)
    values.forEachIndexed { index, value ->
        set[index] = value == 1
    }
    return set
}

fun equationsToString(equations: Array<BitSet>, result: BitSet, varCount: Int): String {
    val sb = StringBuilder()

    equations.forEachIndexed { eqIndex, eq ->
        val eqStr = (0 until varCount)
            .filter { eq[it] }
            .ifEmpty { listOf(eqIndex) }
            .joinToString(" ^ ") { "a${it + 1}" }

        val res = if (!eq.isEmpty) {
            if (result[eqIndex]) {
                "1"
            } else {
                "0"
            }
        } else {
            "x"
        }

        sb.appendLine("$eqStr = $res")
    }

    return sb.toString()
}

fun matrixToString(equations: Array<BitSet>, result: BitSet, varCount: Int): String {
    val sb = StringBuilder()

    equations.forEachIndexed { eqIndex, eq ->
        val eqStr = (0 until varCount).joinToString("") { if (eq[it]) "1" else "0" }
        val res = if (result[eqIndex]) "1" else "0"
        sb.appendLine("$eqStr|$res")
    }

    return sb.toString()
}

fun equationsToFile(equations: Array<BitSet>, result: BitSet, varCount: Int, filePath: String) {
    val equationString = equationsToString(equations, result, varCount)
    Files.writeString(Paths.get(filePath), equationString)
}

fun matrixToFile(equations: Array<BitSet>, result: BitSet, varCount: Int, filePath: String) {
    val matrixString = matrixToString(equations, result, varCount)
    Files.writeString(Paths.get(filePath), matrixString)
}

fun Byte.toBitSet(): BitSet {
    val byte = this
    val bitSet = BitSet(Byte.SIZE_BITS)

    var i = 0
    while (i < Byte.SIZE_BITS) {
        if (byte.toInt() and (1 shl Byte.SIZE_BITS - i - 1) > 0) {
            bitSet.set(i)
        }
        i++
    }

    return bitSet
}
