package keccak

import keccak.util.toString
import java.util.*

class AndEquationSystem {
    val rows: Int
    val cols: Int
    val equations: Array<Equation>
    val andOpLeftResults: BitSet
    val andOpRightResults: BitSet
    val rightXorResults: BitSet

    constructor(rows: Int, cols: Int) {
        this.rows = rows
        this.cols = cols
        this.equations = Array(rows) { Equation(BitSet(cols), BitSet(cols), BitSet(cols)) }
        this.andOpLeftResults = BitSet(rows)
        this.andOpRightResults = BitSet(rows)
        this.rightXorResults = BitSet(rows)
    }

    data class Equation(
        val andOpLeft: BitSet,
        val andOpRight: BitSet,
        val rightXor: BitSet,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AndEquationSystem

        if (rows != other.rows) return false
        if (cols != other.cols) return false
        if (andOpLeftResults != other.andOpLeftResults) return false
        if (andOpRightResults != other.andOpRightResults) return false
        if (rightXorResults != other.rightXorResults) return false
        if (!equations.contentEquals(other.equations)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rows
        result = 31 * result + cols
        result = 31 * result + equations.contentHashCode()
        result = 31 * result + andOpLeftResults.hashCode()
        result = 31 * result + andOpRightResults.hashCode()
        result = 31 * result + rightXorResults.hashCode()
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder()
        var i = 0
        while (i < rows) {
            val left = equations[i].andOpLeft.toString(cols)
            val right = equations[i].andOpRight.toString(cols)
            val res = equations[i].rightXor.toString(cols)

            sb.append('(')
            sb.append(left)
            if (andOpLeftResults[i]) {
                sb.append("|1")
            } else {
                sb.append("|0")
            }
            sb.append(')')

            sb.append('(')
            sb.append(right)
            if (andOpRightResults[i]) {
                sb.append("|1")
            } else {
                sb.append("|0")
            }
            sb.append(')')

            sb.append(" = ")

            sb.append(res)

            if (rightXorResults[i]) {
                sb.append("|1")
            } else {
                sb.append("|0")
            }

            i++

            if (i != rows) {
                sb.append('\n')
            }
        }
        return sb.toString()
    }
}
