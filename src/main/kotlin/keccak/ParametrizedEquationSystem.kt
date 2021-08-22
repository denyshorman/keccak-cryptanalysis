package keccak

import keccak.util.exchange
import keccak.util.toNumChar
import java.util.*

class ParametrizedEquationSystem {
    val rows: Int
    val cols: Int
    val equations: Array<BitSet>
    val results: Array<BitSet>

    constructor(
        rows: Int,
        cols: Int,
    ) {
        this.rows = rows
        this.cols = cols
        this.equations = Array(rows) { BitSet(cols) }
        this.results = Array(rows) { i ->
            val group = BitSet(rows)
            group[i] = true
            group
        }
    }

    private constructor(rows: Int, cols: Int, equations: Array<BitSet>, results: Array<BitSet>) {
        this.rows = rows
        this.cols = cols
        this.equations = equations
        this.results = results
    }

    fun exchange(i: Int, j: Int) {
        equations.exchange(i, j)
        results.exchange(i, j)
    }

    fun xor(i: Int, j: Int) {
        equations[i].xor(equations[j])
        results[i].xor(results[j])
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParametrizedEquationSystem

        if (rows != other.rows) return false
        if (cols != other.cols) return false
        if (!equations.contentEquals(other.equations)) return false
        if (!results.contentEquals(other.results)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rows
        result = 31 * result + cols
        result = 31 * result + equations.contentHashCode()
        result = 31 * result + results.contentHashCode()
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder(rows * (cols + rows + 2) - 1)
        var eqIndex = 0

        while (eqIndex < rows) {
            var bitIndex = 0
            while (bitIndex < cols) {
                sb.append(equations[eqIndex][bitIndex].toNumChar())
                bitIndex++
            }
            sb.append('=')
            bitIndex = 0
            while (bitIndex < cols) {
                sb.append(results[eqIndex][bitIndex].toNumChar())
                bitIndex++
            }

            eqIndex++

            if (eqIndex != rows) {
                sb.append('\n')
            }
        }

        return sb.toString()
    }
}
