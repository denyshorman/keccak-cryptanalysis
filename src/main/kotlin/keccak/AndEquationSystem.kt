package keccak

import keccak.util.evaluate
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

    constructor(
        rows: Int,
        cols: Int,
        equations: Array<Equation>,
        andOpLeftResults: BitSet,
        andOpRightResults: BitSet,
        rightXorResults: BitSet,
    ) {
        this.rows = rows
        this.cols = cols
        this.equations = equations
        this.andOpLeftResults = andOpLeftResults
        this.andOpRightResults = andOpRightResults
        this.rightXorResults = rightXorResults
    }

    data class Equation(
        var andOpLeft: BitSet,
        var andOpRight: BitSet,
        var rightXor: BitSet,
    ) {
        fun clone(): Equation {
            return Equation(
                andOpLeft.clone() as BitSet,
                andOpRight.clone() as BitSet,
                rightXor.clone() as BitSet,
            )
        }
    }

    fun isValid(solution: BitSet): Boolean {
        var i = 0
        while (i < rows) {
            val l = equations[i].andOpLeft.evaluate(solution) xor andOpLeftResults[i]
            val r = equations[i].andOpRight.evaluate(solution) xor andOpRightResults[i]
            val lr = equations[i].rightXor.evaluate(solution) xor rightXorResults[i]
            if ((l && r) != lr) return false

            i++
        }
        return true
    }

    fun solve(): Boolean {
        TODO("Not implemented")
    }

    fun clone(): AndEquationSystem {
        return AndEquationSystem(
            rows,
            cols,
            Array(rows) { equations[it].clone() },
            andOpLeftResults.clone() as BitSet,
            andOpRightResults.clone() as BitSet,
            rightXorResults.clone() as BitSet,
        )
    }

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
