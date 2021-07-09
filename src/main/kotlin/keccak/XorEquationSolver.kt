package keccak

import java.util.*

object XorEquationSolver {
    fun solve(equations: Array<BitSet>, result: BitSet) {
        bottom(equations, result)
        top(equations, result)
    }

    private fun bottom(equations: Array<BitSet>, result: BitSet) {
        var row = 0
        var col = 0

        while (row < equations.size && col < equations.size) {
            var i = row
            var found = false

            while (i < equations.size) {
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
                    }

                    i++
                }
            }

            row++
            col++
        }
    }

    private fun top(equations: Array<BitSet>, result: BitSet) {
        var row = equations.size - 1
        var col = equations.size - 1

        while (row >= 0 && col >= 0) {
            if (!equations[row].isEmpty) {
                var i = row - 1

                while (i >= 0) {
                    if (equations[i][col]) {
                        equations[i].xor(equations[row])
                        result[i] = result[i] xor result[row]
                    }

                    i--
                }
            }

            row--
            col--
        }
    }
}

private fun <T> Array<T>.exchange(i: Int, j: Int) {
    val tmp = this[i]
    this[i] = this[j]
    this[j] = tmp
}

private fun BitSet.exchange(i: Int, j: Int) {
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

fun equationsToString(equations: Array<BitSet>, result: BitSet): String {
    val sb = StringBuilder()

    equations.forEachIndexed { index, eq ->
        val eqStr = equations.indices
            .filter { eq[it] }
            .ifEmpty { listOf(index) }
            .joinToString(" ^ ") { "a${it + 1}" }

        val res = if (!eq.isEmpty) {
            if (result[index]) {
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

fun matrixToString(equations: Array<BitSet>, result: BitSet): String {
    val sb = StringBuilder()

    equations.forEachIndexed { index, eq ->
        val eqStr = equations.indices.joinToString("") { if (eq[it]) "1" else "0" }
        val res = if (result[index]) "1" else "0"
        sb.appendLine("$eqStr|$res")
    }

    return sb.toString()
}
