package keccak

import keccak.util.*
import mu.KotlinLogging
import java.util.*
import kotlin.math.min

class XorEquationSystem {
    val rows: Int get() = equations.size
    var cols: Int
    var equations: Array<BitSet>
    var results: BitSet

    constructor(
        rows: Int,
        cols: Int,
    ) {
        this.cols = cols
        this.equations = Array(rows) { BitSet(cols) }
        this.results = BitSet(rows)
    }

    constructor(rows: Int, cols: Int, equations: Array<BitSet>, results: BitSet) {
        this.cols = cols
        this.equations = equations
        this.results = results
    }

    fun isValid(solution: BitSet): Boolean {
        var i = 0
        while (i < rows) {
            if (equations[i].evaluate(solution) != results[i]) {
                return false
            }
            i++
        }
        return true
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
        return equations[eqIndex].isEmpty && results[eqIndex]
    }

    fun isPartiallyEmpty(): Boolean {
        var i = 0
        while (i < rows) {
            if (equations[i++].isEmpty) return true
        }
        return false
    }

    fun exchange(i: Int, j: Int) {
        equations.exchange(i, j)
        results.exchange(i, j)
    }

    fun xor(i: Int, j: Int) {
        equations[i].xor(equations[j])
        results[i] = results[i] xor results[j]
    }

    fun xor(vararg systems: XorEquationSystem) {
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

    fun xor(eqIndex: Int, vararg xorEquations: XorEquation) {
        var bitEqIndex = 0
        while (bitEqIndex < xorEquations.size) {
            equations[eqIndex].xor(xorEquations[bitEqIndex].bitGroup.bitSet)
            results.xor(eqIndex, xorEquations[bitEqIndex].result)
            bitEqIndex++
        }
    }

    fun rotateEquationsLeft(count: Int) {
        var roundIndex = 0
        var eqIndex: Int
        var firstEquation: BitSet
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

    fun solve(
        skipValidation: Boolean = false,
        logProgress: Boolean = false,
        progressStep: Int = 1024,
        rowsMask: BitSet? = null,
        colsMask: BitSet? = null,
    ): Boolean {
        if (logProgress) {
            if (!isPow2(progressStep)) {
                throw IllegalArgumentException("progressStep must be a power of 2")
            }

            logger.info("Starting forward processing")
        }

        var row = rowsMask?.nextSetBitDefault(0, rows) ?: 0
        var col = colsMask?.nextSetBitDefault(0, cols) ?: 0

        while (row < rows && col < cols) {
            var i = row
            var found = false

            while (i < rows) {
                if (equations[i].isEmpty) {
                    if (!skipValidation && results[i]) {
                        return false
                    }
                } else {
                    if (equations[i][col]) {
                        found = true
                        break
                    }
                }

                i = rowsMask?.nextSetBitDefault(i + 1, rows) ?: (i + 1)
            }

            if (found) {
                if (row != i) {
                    exchange(row, i)
                }

                i = rowsMask?.nextSetBitDefault(row + 1, rows) ?: (row + 1)

                while (i < rows) {
                    if (!equations[i].isEmpty && equations[i][col]) {
                        xor(i, row)

                        if (!skipValidation && isInvalid(i)) {
                            return false
                        }
                    }

                    i = rowsMask?.nextSetBitDefault(i + 1, rows) ?: (i + 1)
                }

                row = rowsMask?.nextSetBitDefault(row + 1, rows) ?: (row + 1)

                if (logProgress && modPow2(row, progressStep) == 0) {
                    logger.info("Processed $row rows")
                }
            }

            col = colsMask?.nextSetBitDefault(col + 1, cols) ?: (col + 1)
        }

        if (logProgress) {
            logger.info("Forward processing has been completed. Starting backward processing")
        }

        row = rowsMask?.previousSetBit(rows - 1) ?: (rows - 1)

        while (row >= 0) {
            if (equations[row].isEmpty) {
                if (!skipValidation && results[row]) {
                    return false
                }
            } else {
                var i = rowsMask?.previousSetBit(row - 1) ?: (row - 1)

                if (i >= 0) {
                    col = if (colsMask == null) {
                        equations[row].nextSetBit(0)
                    } else {
                        equations[row].nextSetBit(colsMask)
                    }

                    if (col >= 0) {
                        while (i >= 0) {
                            if (equations[i][col]) {
                                xor(i, row)

                                if (!skipValidation && isInvalid(i)) {
                                    return false
                                }
                            }

                            i = rowsMask?.previousSetBit(i - 1) ?: (i - 1)
                        }
                    }
                }
            }

            row = rowsMask?.previousSetBit(row - 1) ?: (row - 1)

            if (logProgress && modPow2(row, progressStep) == 0) {
                logger.info("Processed ${rows - row} rows")
            }
        }

        if (logProgress) {
            logger.info("Backward processing has been completed.")
        }

        return true
    }

    fun hasSolution(): Boolean {
        //#region Find equation that is equal to one and put it to top
        var row = 0

        while (row < rows) {
            if (results[row]) {
                if (row != 0) {
                    results.exchange(0, row)
                }
                break
            }
            row++
        }
        //#endregion

        //#region If the system does not have equations equal to 1, then the system has solutions
        if (row == rows) {
            return true
        }
        //#endregion

        //#region Eliminate other equations equal to 1
        row = 1

        while (row < rows) {
            if (results[row]) {
                equations[row].xor(equations[0])
                results[row] = false
            }
            row++
        }
        //#endregion

        //#region Simplify equations equal to 0
        row = 1
        var col = 0

        while (row < rows && col < cols) {
            var i = row
            var found = false

            while (i < rows) {
                if (equations[i][col]) {
                    found = true
                    break
                }

                i++
            }

            if (found) {
                if (row != i) {
                    exchange(row, i)
                }

                i = row + 1

                while (i < rows) {
                    if (equations[i][col]) {
                        xor(i, row)
                    }

                    i++
                }
            }

            row++
            col++
        }

        row = min(rows, cols) - 1
        col = row

        while (row >= 0 && col >= 0) {
            var i = row - 1

            while (i > 0) {
                if (equations[i][col]) {
                    xor(i, row)
                }

                i--
            }

            row--
            col--
        }
        //#endregion

        //#region Substitute main equation
        row = 1
        while (row < rows) {
            if (equations[0][row - 1] && equations[row][row - 1]) {
                equations[0].xor(equations[row])
            }
            row++
        }
        //#endregion

        return !equations[0].isEmpty
    }

    fun substitute(values: BitSet) {
        var i = 0
        while (i < rows) {
            equations[i].and(values)
            if (equations[i].setBitsCount() and 1 != 0) {
                results.invertValue(i)
            }
            equations[i].clear()
            i++
        }
    }

    fun substitute(values: BitSet, mask: BitSet) {
        val maskInverted = mask.clone() as BitSet
        maskInverted.invert(cols)

        var i = 0
        while (i < rows) {
            val res = equations[i].clone() as BitSet
            res.and(values)
            res.and(mask)

            equations[i].and(maskInverted)

            if (res.setBitsCount() and 1 != 0) {
                results.invertValue(i)
            }

            i++
        }
    }

    fun substitute(index: Int, value: Boolean) {
        var i = 0
        while (i < rows) {
            if (equations[i][index]) {
                equations[i].clear(index)
                results.xor(i, value)
            }
            i++
        }
    }

    fun substitute(values: Iterable<Pair<Int, Boolean>>) {
        for ((index, value) in values) {
            substitute(index, value)
        }
    }

    fun substitute(other: XorEquationSystem) {
        var i = 0
        while (i < other.rows) {
            if (!other.equations[i].isEmpty) {
                val firstVarIndex = other.equations[i].nextSetBit(0)
                var j = 0
                while (j < this.rows) {
                    if (!this.equations[j].isEmpty && this.equations[j][firstVarIndex]) {
                        this.equations[j].xor(other.equations[i])
                        this.results.xor(j, other.results[i])
                    }
                    j++
                }
            }
            i++
        }
    }

    fun extend(size: Int) {
        equations = Array(equations.size + size) { i ->
            if (i < equations.size) {
                equations[i]
            } else {
                BitSet(cols)
            }
        }

        results = run {
            val bits = BitSet(equations.size)
            bits.xor(results)
            bits
        }
    }

    fun append(
        vars: BitSet,
        res: Boolean,
        appendFromIndex: Int = 0,
        extendSize: Int = 128,
    ): Int {
        var i = appendFromIndex

        while (i < equations.size) {
            if (equations[i].isEmpty && !results[i]) {
                equations[i].xor(vars)
                results.setIfTrue(i, res)
                return i
            }

            i++
        }

        extend(extendSize)

        equations[i].xor(vars)
        results.setIfTrue(i, res)

        return i
    }

    fun clear() {
        var i = 0
        while (i < equations.size) {
            equations[i].clear()
            i++
        }
        results.clear()
    }

    fun clone(): XorEquationSystem {
        val newEquations = Array(rows) { equations[it].clone() as BitSet }
        val newResults = results.clone() as BitSet
        return XorEquationSystem(rows, cols, newEquations, newResults)
    }

    fun solutionIterator(): SolutionIterator {
        return SolutionIterator()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as XorEquationSystem

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

    //#region Inner classes
    inner class SolutionIterator {
        val solution = BitSet(this@XorEquationSystem.cols)
        val iterator: CombinationIterator

        init {
            iterator = CombinationIterator(this@XorEquationSystem.cols, calcMask())
        }

        fun hasNext(): Boolean {
            return iterator.hasNext()
        }

        fun next() {
            solution.clear()
            solution.xor(this@XorEquationSystem.results)

            var eqIndex = 0
            while (eqIndex < this@XorEquationSystem.rows) {
                if (this@XorEquationSystem.equations[eqIndex].isEmpty) {
                    if (iterator.combination[eqIndex]) {
                        solution[eqIndex] = true
                    }
                } else {
                    var result = this@XorEquationSystem.results[eqIndex]
                    var bitIndex = iterator.combination.nextSetBit(eqIndex + 1)
                    while (bitIndex >= 0) {
                        if (this@XorEquationSystem.equations[eqIndex][bitIndex]) {
                            result = !result
                        }
                        bitIndex = iterator.combination.nextSetBit(bitIndex + 1)
                    }
                    solution[eqIndex] = result
                }
                eqIndex++
            }

            if (cols > rows) {
                var bitIndex = iterator.combination.nextSetBit(rows)
                while (bitIndex >= 0) {
                    solution[bitIndex] = true
                    bitIndex = iterator.combination.nextSetBit(bitIndex + 1)
                }
            }

            iterator.next()
        }

        private fun calcMask(): BitSet {
            val mask = BitSet(this@XorEquationSystem.cols)

            var i = this@XorEquationSystem.rows - 1
            while (i >= 0) {
                mask.or(this@XorEquationSystem.equations[i])
                mask.clear(i)
                i--
            }

            return mask
        }
    }
    //#endregion

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
