package keccak

import keccak.util.toNumChar

class EquationSystem {
    val rows: Int
    val cols: Int
    val equations: Array<BitGroup>
    val results: BitGroup

    constructor(
        rows: Int,
        cols: Int,
    ) {
        this.rows = rows
        this.cols = cols
        this.equations = Array(rows) { BitGroup(cols) }
        this.results = BitGroup(rows)
    }

    private constructor(equations: Array<BitGroup>, results: BitGroup) {
        this.rows = equations.size
        this.cols = equations.getOrNull(0)?.size ?: 0
        this.equations = equations
        this.results = results
    }

    fun isValid(): Boolean {
        var i = 0
        while (i < rows) {
            if (isInvalid(i)) return false
            i++
        }
        return true
    }

    fun isValid(eqIndex: Int): Boolean {
        return !isInvalid(eqIndex)
    }

    fun isInvalid(eqIndex: Int): Boolean {
        return equations[eqIndex].isEmpty() && results[eqIndex]
    }

    fun exchange(i: Int, j: Int) {
        equations.exchange(i, j)
        results.exchange(i, j)
    }

    fun xor(i: Int, j: Int) {
        equations[i].xor(equations[j])
        results[i] = results[i] xor results[j]
    }

    fun xor(vararg systems: EquationSystem) {
        var i = 0
        var j: Int
        while (i < systems.size) {
            j = 0
            while (j < systems[i].rows) {
                equations[j].xor(systems[i].equations[j])
                j++
            }
            results.xor(systems[i].results)
            i++
        }
    }

    fun xor(eqIndex: Int, vararg bitEquations: BitEquation) {
        var bitEqIndex = 0
        while (bitEqIndex < bitEquations.size) {
            equations[eqIndex].xor(bitEquations[bitEqIndex].bitGroup)
            results.xor(eqIndex, bitEquations[bitEqIndex].result)
            bitEqIndex++
        }
    }

    fun rotateEquationsLeft(count: Int) {
        var roundIndex = 0
        var eqIndex: Int
        var firstEquation: BitGroup
        var firstResultBit: Boolean
        val lastBitIndex = rows - 1

        while (roundIndex < count) {
            eqIndex = 0
            firstEquation = equations[0]
            firstResultBit = results[0]
            while (eqIndex < lastBitIndex) {
                equations[eqIndex] = equations[eqIndex + 1]
                results[eqIndex] = results[eqIndex + 1]
                eqIndex++
            }
            equations[lastBitIndex] = firstEquation
            results[lastBitIndex] = firstResultBit
            roundIndex++
        }
    }

    fun evaluate(vars: BitGroup) {
        var i = 0
        while (i < rows) {
            equations[i].and(vars)
            if (equations[i].setBitsCount() % 2 != 0) {
                results.xor(i, true)
            }
            equations[i].clear()
            i++
        }
    }

    fun partiallyEvaluate(varValues: BitGroup, availableVars: BitGroup) {
        val availableVarsInverted = availableVars.clone()
        availableVarsInverted.invert()

        var i = 0
        while (i < rows) {
            val res = equations[i].clone()
            res.and(varValues)
            res.and(availableVars)

            equations[i].and(availableVarsInverted)

            if (res.setBitsCount() % 2 != 0) {
                results.xor(i, true)
            }

            i++
        }
    }

    fun clear() {
        var i = 0
        while (i < equations.size) {
            equations[i].clear()
            i++
        }
        results.clear()
    }

    fun clone(): EquationSystem {
        val newEquations = Array(rows) { equations[it].clone() }
        val newResults = results.clone()
        return EquationSystem(newEquations, newResults)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EquationSystem

        if (rows != other.rows) return false
        if (cols != other.cols) return false
        if (results != other.results) return false
        if (!equations.contentEquals(other.equations)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rows
        result = 31 * result + cols
        result = 31 * result + equations.contentHashCode()
        result = 31 * result + results.hashCode()
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder(rows * (cols + 3) - 1)
        var eqIndex = 0

        while (eqIndex < rows) {
            var bitIndex = 0
            while (bitIndex < cols) {
                sb.append(equations[eqIndex][bitIndex].toNumChar())
                bitIndex++
            }
            sb.append('|')
            sb.append(results[eqIndex].toNumChar())

            eqIndex++

            if (eqIndex != rows) {
                sb.append('\n')
            }
        }

        return sb.toString()
    }
}
