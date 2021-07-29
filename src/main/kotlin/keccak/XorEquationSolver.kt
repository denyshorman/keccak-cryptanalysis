package keccak

import keccak.util.pow
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

class SolutionsFinder(val equationSystem: EquationSystem) {
    val mask = FixedBitSet(equationSystem.cols)
    val iterator = FixedBitSet(equationSystem.cols)
    var solution = FixedBitSet(equationSystem.rows)
    var solutionIndex = 0
    val solutionsCount: Long

    init {
        initMask()
        solutionsCount = pow(2, mask.setBitsCount())
    }

    fun hasNext(): Boolean {
        return solutionIndex + 1 < solutionsCount
    }

    fun next() {
        solution = equationSystem.results.clone()

        var i = 0
        while (i < equationSystem.rows) {
            val setBits = iterator.clone()
            setBits.and(equationSystem.equations[i])
            if (setBits.setBitsCount() % 2 != 0) {
                solution[i] = solution[i] xor true
            }
            i++
        }

        solutionIndex++
        iteratorIncrement()
    }

    private fun initMask() {
        var i = 0
        while (i < equationSystem.rows) {
            mask.or(equationSystem.equations[i])
            mask.clear(i)
            i++
        }
    }

    private fun iteratorIncrement() {
        var i = mask.previousSetBit(equationSystem.cols - 1)

        while (i >= 0) {
            if (iterator[i]) {
                iterator.clear(i)
                i = mask.previousSetBit(i - 1)
            } else {
                iterator.set(i)
                break
            }
        }
    }
}

fun solveXorEquations(system: EquationSystem) {
    var row = 0
    var col = 0

    while (row < system.rows && col < system.cols) {
        var i = row
        var found = false

        while (i < system.rows) {
            if (system.isInvalid(i)) {
                throw XorEquationSolver.NoSolution(i)
            }

            if (system.equations[i][col]) {
                found = true
                break
            }

            i++
        }

        if (found) {
            if (row != i) {
                system.exchange(row, i)
            }

            i = row + 1

            while (i < system.rows) {
                if (system.equations[i][col]) {
                    system.xor(i, row)

                    if (system.isInvalid(i)) {
                        throw XorEquationSolver.NoSolution(i)
                    }
                }

                i++
            }
        }

        row++
        col++
    }

    row = min(system.rows, system.cols) - 1
    col = row

    while (row >= 0 && col >= 0) {
        if (system.equations[row].isEmpty()) {
            if (system.results[row]) {
                throw XorEquationSolver.NoSolution(row)
            }
        } else {
            var i = row - 1

            while (i >= 0) {
                if (system.equations[i][col]) {
                    system.xor(i, row)

                    if (system.isInvalid(i)) {
                        throw XorEquationSolver.NoSolution(i)
                    }
                }

                i--
            }
        }

        row--
        col--
    }
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

fun fixedBitSet(vararg values: Int): FixedBitSet {
    val set = FixedBitSet(values.size)
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
