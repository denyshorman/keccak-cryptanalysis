package keccak

import keccak.util.*
import mu.KotlinLogging
import java.util.*
import kotlin.math.max
import kotlin.random.Random

class AndEquationSystem {
    val rows: Int get() = equations.size
    val cols: Int
    val equations: Array<Equation>
    val andOpLeftResults: BitSet
    val andOpRightResults: BitSet
    val rightXorResults: BitSet

    constructor(rows: Int, cols: Int) {
        this.cols = cols
        this.equations = Array(rows) { Equation(BitSet(cols), BitSet(cols), BitSet(cols)) }
        this.andOpLeftResults = BitSet(rows)
        this.andOpRightResults = BitSet(rows)
        this.rightXorResults = BitSet(rows)
    }

    constructor(
        cols: Int,
        equations: Array<Equation>,
        andOpLeftResults: BitSet,
        andOpRightResults: BitSet,
        rightXorResults: BitSet,
    ) {
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

    fun isEmpty(eqIndex: Int): Boolean {
        return equations[eqIndex].andOpLeft.isEmpty &&
                equations[eqIndex].andOpRight.isEmpty &&
                equations[eqIndex].rightXor.isEmpty &&
                !andOpLeftResults[eqIndex] &&
                !andOpRightResults[eqIndex] &&
                !rightXorResults[eqIndex]
    }

    fun hasVariable(eqIndex: Int, varIndex: Int): Boolean {
        return equations[eqIndex].andOpLeft[varIndex] ||
                equations[eqIndex].andOpRight[varIndex] ||
                equations[eqIndex].rightXor[varIndex]
    }

    fun hasSideMatch(eq0: Int, eq1: Int): Boolean {
        val a0 = equations[eq0].andOpLeft
        val a1 = equations[eq0].andOpRight
        val a2 = equations[eq0].rightXor

        val b0 = equations[eq1].andOpLeft
        val b1 = equations[eq1].andOpRight
        val b2 = equations[eq1].rightXor

        return a0 == b0 || a0 == b1 || a0 == b2 ||
                a1 == b0 || a1 == b1 || a1 == b2 ||
                a2 == b0 || a2 == b1 || a2 == b2
    }

    fun countEmptyEquations(): Int {
        var eqIndex = 0
        var counter = 0
        while (eqIndex < rows) {
            if (isEmpty(eqIndex)) {
                counter++
            }
            eqIndex++
        }
        return counter
    }

    fun substitute(varIndex: Int, value: Boolean) {
        var eqIndex = 0

        while (eqIndex < rows) {
            if (equations[eqIndex].andOpLeft[varIndex]) {
                equations[eqIndex].andOpLeft.clear(varIndex)
                andOpLeftResults.xor(eqIndex, value)
            }

            if (equations[eqIndex].andOpRight[varIndex]) {
                equations[eqIndex].andOpRight.clear(varIndex)
                andOpRightResults.xor(eqIndex, value)
            }

            if (equations[eqIndex].rightXor[varIndex]) {
                equations[eqIndex].rightXor.clear(varIndex)
                rightXorResults.xor(eqIndex, value)
            }

            eqIndex++
        }
    }

    fun substitute(values: Iterable<Pair<Int, Boolean>>) {
        for ((index, value) in values) {
            substitute(index, value)
        }
    }

    fun substitute(
        xorSystem: XorEquationSystem,
        mask: BitSet? = null,
        substituteLeft: Boolean = true,
        substituteRight: Boolean = true,
        substituteResult: Boolean = true,
    ) {
        var xorEqIndex = 0

        while (xorEqIndex < xorSystem.rows) {
            if (!xorSystem.equations[xorEqIndex].isEmpty) {
                val exprVarIndex = xorSystem.eqVarMap[xorEqIndex]

                if (exprVarIndex >= 0 && (mask == null || mask[exprVarIndex])) {
                    var andEqIndex = 0

                    while (andEqIndex < rows) {
                        if (substituteLeft && !equations[andEqIndex].andOpLeft.isEmpty && equations[andEqIndex].andOpLeft[exprVarIndex]) {
                            equations[andEqIndex].andOpLeft.xor(xorSystem.equations[xorEqIndex])
                            andOpLeftResults.xor(andEqIndex, xorSystem.results[xorEqIndex])
                        }

                        if (substituteRight && !equations[andEqIndex].andOpRight.isEmpty && equations[andEqIndex].andOpRight[exprVarIndex]) {
                            equations[andEqIndex].andOpRight.xor(xorSystem.equations[xorEqIndex])
                            andOpRightResults.xor(andEqIndex, xorSystem.results[xorEqIndex])
                        }

                        if (substituteResult && !equations[andEqIndex].rightXor.isEmpty && equations[andEqIndex].rightXor[exprVarIndex]) {
                            equations[andEqIndex].rightXor.xor(xorSystem.equations[xorEqIndex])
                            rightXorResults.xor(andEqIndex, xorSystem.results[xorEqIndex])
                        }

                        andEqIndex++
                    }
                }
            }

            xorEqIndex++

            if (modPow2(xorEqIndex, 4096) == 0) {
                logger.info("Processed $xorEqIndex rows")
            }
        }
    }

    fun invertToXorAndSystem(): XorAndEquationSystem {
        val xorSystem = invertToXorSystem()
        val andSystem = invertToAndSystem()
        return XorAndEquationSystem(xorSystem, andSystem)
    }

    fun invertToXorSystem(): XorEquationSystem {
        val newRowsCount = rows * 2
        val newColsCount = cols + newRowsCount
        val xorSystem = XorEquationSystem(newRowsCount, newColsCount)

        var andEqRow = 0
        var newXorEqRow = 0
        var newVarIndex = cols

        while (andEqRow < rows) {
            xorSystem.equations[newXorEqRow] = equations[andEqRow].andOpLeft.clone() as BitSet
            xorSystem.equations[newXorEqRow].set(newVarIndex)
            xorSystem.results.setIfTrue(newXorEqRow, andOpLeftResults[andEqRow])
            xorSystem.eqVarMap[newXorEqRow] = newVarIndex
            xorSystem.varEqMap[newVarIndex] = newXorEqRow

            newXorEqRow++
            newVarIndex++

            xorSystem.equations[newXorEqRow] = equations[andEqRow].andOpRight.clone() as BitSet
            xorSystem.equations[newXorEqRow].set(newVarIndex)
            xorSystem.results.setIfTrue(newXorEqRow, andOpRightResults[andEqRow])
            xorSystem.eqVarMap[newXorEqRow] = newVarIndex
            xorSystem.varEqMap[newVarIndex] = newXorEqRow

            newXorEqRow++
            newVarIndex++
            andEqRow++
        }

        return xorSystem
    }

    fun invertToAndSystem(): AndEquationSystem {
        val andSystem = AndEquationSystem(rows, cols + rows * 2)

        var andEqRow = 0
        var newXorEqRow = 0
        var newVarIndex = cols

        while (andEqRow < rows) {
            andSystem.equations[andEqRow].andOpLeft.set(newVarIndex)

            newXorEqRow++
            newVarIndex++

            andSystem.equations[andEqRow].andOpRight.set(newVarIndex)

            andSystem.equations[andEqRow].rightXor = equations[andEqRow].rightXor.clone() as BitSet
            andSystem.rightXorResults.setIfTrue(andEqRow, rightXorResults[andEqRow])

            newXorEqRow++
            newVarIndex++
            andEqRow++
        }

        return andSystem
    }

    fun normalize(): AndEquationSystem {
        val system = AndEquationSystem(rows = 2 * (rows - countEmptyEquations()), cols)

        var i = 0
        var j = 0
        while (i < rows) {
            if (isEmpty(i)) {
                i++
                continue
            }

            system.equations[j].andOpLeft = equations[i].andOpLeft.clone() as BitSet
            system.andOpLeftResults.setIfTrue(j, andOpLeftResults[i])

            system.equations[j].andOpRight = equations[i].andOpRight.clone() as BitSet
            system.equations[j].andOpRight.xor(equations[i].rightXor)
            system.andOpRightResults.setIfTrue(j, andOpRightResults[i] xor rightXorResults[i])

            j++

            system.equations[j].andOpLeft = equations[i].rightXor.clone() as BitSet
            system.andOpLeftResults.setIfTrue(j, rightXorResults[i])

            system.equations[j].andOpRight = equations[i].andOpLeft.clone() as BitSet
            system.andOpRightResults.setIfTrue(j, !andOpLeftResults[i])

            j++
            i++
        }

        return system
    }

    fun countSolutions(): Int {
        val iterator = CombinationIterator(cols)
        var solutions = 0

        iterator.iterateAll {
            if (isValid(iterator.combination)) {
                // println(iterator.combination.toString(cols))
                solutions++
            }
        }

        return solutions
    }

    fun exchange(i: Int, j: Int) {
        if (i == j) return
        equations.exchange(i, j)
        andOpLeftResults.exchange(i, j)
        andOpRightResults.exchange(i, j)
        rightXorResults.exchange(i, j)
    }

    fun clear(eqIndex: Int) {
        equations[eqIndex].andOpLeft.clear()
        equations[eqIndex].andOpRight.clear()
        equations[eqIndex].rightXor.clear()
        andOpLeftResults.clear(eqIndex)
        andOpRightResults.clear(eqIndex)
        rightXorResults.clear(eqIndex)
    }

    fun clone(): AndEquationSystem {
        return AndEquationSystem(
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

    //#region Public Models
    class BruteForceAlgorithm(val system: AndEquationSystem) {
        fun solve(): Sequence<BitSet> {
            return sequence {
                val ci = CombinationIterator(system.cols)

                ci.iterateAll {
                    if (system.isValid(ci.combination)) {
                        yield(ci.combination.clone() as BitSet)
                    }
                }
            }
        }
    }

    class PivotSolutionAlgorithm {
        private val random: Random
        private val pivotSolution: BitSet
        private val badVarsCounter: BadVarsCounter
        private val varsCount: Int

        val invertedSystem: XorEquationSystem

        constructor(
            system: AndEquationSystem,
            pivotSolution: BitSet,
            random: Random = Random,
        ) {
            system.rotate(pivotSolution, left = false, right = false)

            this.random = random
            this.pivotSolution = pivotSolution
            varsCount = system.cols
            invertedSystem = system.invertToXorSystem()
            badVarsCounter = BadVarsCounter(invertedSystem, varsCount)
        }

        constructor(
            system: XorEquationSystem,
            varsCount: Int,
            pivotSolution: BitSet,
            random: Random = Random,
            solutionFilter: ((Int, XorEquationSystem) -> Boolean)? = null,
        ) {
            this.random = random
            this.pivotSolution = pivotSolution
            this.varsCount = varsCount
            invertedSystem = system
            badVarsCounter = BadVarsCounter(invertedSystem, varsCount, solutionFilter)
        }

        fun solve(
            logProgress: Boolean = false,
            progressStep: Int = 1024,
        ): Set<BitSet> {
            var solutions = extractSolutions()
            if (solutions.isNotEmpty()) return solutions

            var varIndex = 0
            while (varIndex < varsCount) {
                if (invertedSystem.varEqMap[varIndex] != -1) {
                    varIndex++
                    continue
                }

                var eqIndex = 0
                while (eqIndex < invertedSystem.rows) {
                    if (
                        invertedSystem.equations[eqIndex].isEmpty ||
                        invertedSystem.eqVarMap[eqIndex] < varsCount ||
                        !invertedSystem.equations[eqIndex][varIndex]
                    ) {
                        eqIndex++
                        continue
                    }

                    invertedSystem.expressVariable(eqIndex, varIndex)
                    badVarsCounter.recalculate()

                    if (logProgress && modPow2(varIndex, progressStep) == 0) {
                        logger.info("Expressed $varIndex variables")
                    }

                    solutions = extractSolutions()

                    if (solutions.isNotEmpty()) {
                        return solutions
                    } else {
                        break
                    }
                }

                varIndex++
            }

            solutions = extractSolutions()
            if (solutions.isNotEmpty()) return solutions

            val indexCache = IntArray(max(invertedSystem.cols, invertedSystem.rows))
            var indexCachePtr = 0

            if (logProgress) {
                logger.info("Starting random walk")
            }

            var loopCounter = 0L

            while (!Thread.interrupted()) {
                if (logProgress && modPow2(loopCounter++, progressStep.toLong()) == 0L) {
                    logger.info("Random walk #$loopCounter")
                }

                var i = varsCount
                while (i < invertedSystem.cols) {
                    if (invertedSystem.varEqMap[i] == -1) {
                        indexCache[indexCachePtr++] = i
                    }
                    i++
                }

                val randomVarIndex = indexCache[random.nextInt(indexCachePtr)]
                indexCachePtr = 0

                i = 0
                while (i < invertedSystem.rows) {
                    if (invertedSystem.eqVarMap[i] >= varsCount && invertedSystem.equations[i][randomVarIndex]) {
                        indexCache[indexCachePtr++] = i
                    }
                    i++
                }

                val randomEqIndex = indexCache[random.nextInt(indexCachePtr)]
                indexCachePtr = 0

                invertedSystem.expressVariable(randomEqIndex, randomVarIndex)
                badVarsCounter.recalculate()

                solutions = extractSolutions()
                if (solutions.isNotEmpty()) return solutions
            }

            return emptySet()
        }

        private fun extractSolutions(): Set<BitSet> {
            if (badVarsCounter.hasSolution()) {
                val solutions = badVarsCounter.solutions().filter { it != pivotSolution }.toSet()

                if (solutions.isNotEmpty()) {
                    return solutions
                }
            }

            return emptySet()
        }

        companion object {
            private val logger = KotlinLogging.logger {}
        }
    }

    class XorAlgorithm(
        val system: AndEquationSystem,
        val pivotSolution: BitSet,
    ) {
        fun solve(useXor: Boolean): XorEquationSystem {
            system.rotate(pivotSolution, left = true, right = false)
            val xorSystem = XorEquationSystem(system.rows, system.cols)

            var i = 0
            while (i < system.rows) {
                if (useXor) {
                    xorSystem.equations[i].xor(system.equations[i].andOpLeft)
                    xorSystem.equations[i].xor(system.equations[i].andOpRight)
                    xorSystem.results.setIfTrue(i, system.andOpLeftResults[i] xor system.andOpRightResults[i])
                } else {
                    xorSystem.equations[i].xor(system.equations[i].andOpRight)
                    xorSystem.results.setIfTrue(i, system.andOpRightResults[i])
                }

                i++
            }

            xorSystem.solve(
                skipValidation = true,
                logProgress = true,
                sortEquations = true,
            )

            return xorSystem
        }
    }

    class SolutionPairsCounter(
        private val system: XorEquationSystem,
        private val varsCount: Int,
        val pairsCount: Int,
    ) {
        private val varsCountMod2 = isMod2(varsCount)

        val pairs = BitSet(pairsCount)

        init {
            var varIndex = varsCount
            while (varIndex < system.cols) {
                val eq0 = system.varEqMap[varIndex]
                val eq1 = system.varEqMap[varIndex + 1]

                if (eq0 != -1 && eq1 != -1 && system.results[eq0] && system.results[eq1]) {
                    pairs.set(toSolutionPairIndex(varIndex))
                }

                varIndex += 2
            }
        }

        fun update(eqIndex: Int) {
            if (system.eqVarMap[eqIndex] < varsCount) {
                return
            }

            val var0 = system.eqVarMap[eqIndex]

            if (var0 == -1) {
                return
            }

            val var1 = toCompanionVarIndex(var0)

            if (
                system.varEqMap[var1] != -1 &&
                system.results[eqIndex] &&
                system.results[system.varEqMap[var1]]
            ) {
                pairs.set(toSolutionPairIndex(var0))
            } else {
                pairs.clear(toSolutionPairIndex(var0))
            }
        }

        fun clear(eqIndex: Int) {
            val varIndex = system.eqVarMap[eqIndex]

            if (varIndex != -1) {
                pairs.clear(toSolutionPairIndex(varIndex))
            }
        }

        fun hasSolution(): Boolean {
            return pairs.isEmpty
        }

        fun toSolutionPairIndex(varIndex: Int): Int = (varIndex - varsCount) / 2
        fun fromSolutionPairIndex(varIndex: Int): Int = varIndex * 2 + varsCount
        fun toCompanionVarIndex(varIndex: Int): Int = varIndex + if (varsCountMod2 xor isMod2(varIndex)) -1 else 1

        override fun toString(): String {
            return pairs.toString(pairsCount)
        }
    }

    class BadVarsCounter(
        val system: XorEquationSystem,
        val varsCount: Int,
        val solutionFilter: ((Int, XorEquationSystem) -> Boolean)? = null,
    ) {
        private val tmp = BitSet(system.cols)
        private val noSolution = BitSet(system.cols)
        private val missingVars = BitSet(system.cols)
        val badVars = BitSet(system.cols)

        init {
            noSolution.invert(system.cols)
            findMissingVars()
            calculate()
        }

        fun recalculate() {
            badVars.clear()
            calculate()
        }

        fun hasSolution(): Boolean {
            var hasSolutions = badVars != noSolution

            return if (hasSolutions) {
                if (solutionFilter != null) {
                    hasSolutions = false

                    badVars.iterateOverClearBits(fromIndex = 0, toIndex = system.cols) { varIndex ->
                        if (solutionFilter!!(varIndex, system)) {
                            hasSolutions = true
                            false
                        } else {
                            true
                        }
                    }

                    hasSolutions
                } else {
                    true
                }
            } else {
                false
            }
        }

        fun solutions(): Sequence<BitSet> {
            return sequence {
                badVars.iterateOverAllClearBits(0, system.cols) { varIndex ->
                    if (solutionFilter != null) {
                        if (solutionFilter!!(varIndex, system)) {
                            yield(varIndex)
                        }
                    } else {
                        yield(varIndex)
                    }
                }
            }
                .map { varIndex ->
                    val solution = BitSet(varsCount)

                    if (varIndex < varsCount) {
                        solution.set(varIndex)
                    }

                    var i = 0
                    while (i < varsCount) {
                        if (system.varEqMap[i] != -1) {
                            val value = system.results[system.varEqMap[i]] xor
                                    system.equations[system.varEqMap[i]][varIndex]

                            solution.setIfTrue(i, value)
                        }
                        i++
                    }

                    solution
                }
        }

        private fun calculate() {
            badVars.or(missingVars)

            var varIndex = 0
            while (varIndex < varsCount) {
                if (system.varEqMap[varIndex] != -1) {
                    badVars.set(varIndex)
                }
                varIndex++
            }

            varIndex = varsCount
            while (varIndex < system.cols) {
                val varCompIndex = varIndex + 1
                val eq0 = system.varEqMap[varIndex]
                val eq1 = system.varEqMap[varCompIndex]

                if (eq0 != -1 && eq1 != -1) {
                    val r0 = system.results[eq0]
                    val r1 = system.results[eq1]

                    if (!r0 && !r1) {
                        tmp.xor(system.equations[eq0])
                        tmp.and(system.equations[eq1])
                        tmp.or(varIndex, true)
                        tmp.or(varCompIndex, true)
                    } else if (!r0) {
                        tmp.xor(system.equations[eq0])
                        tmp.andNot(system.equations[eq1])
                        tmp.set(varCompIndex)
                    } else if (!r1) {
                        tmp.xor(system.equations[eq1])
                        tmp.andNot(system.equations[eq0])
                        tmp.set(varIndex)
                    } else {
                        tmp.xor(system.equations[eq0])
                        tmp.or(system.equations[eq1])
                        tmp.invert(system.cols)
                        tmp.or(varIndex, true)
                        tmp.or(varCompIndex, true)
                    }

                    badVars.or(tmp)
                    tmp.clear()
                } else if (eq0 != -1) {
                    badVars.or(varIndex, true)

                    if (system.equations[eq0][varCompIndex] || system.results[eq0]) {
                        badVars.or(varCompIndex, true)
                    }
                } else if (eq1 != -1) {
                    badVars.or(varCompIndex, true)

                    if (system.equations[eq1][varIndex] || system.results[eq1]) {
                        badVars.or(varIndex, true)
                    }
                }

                varIndex += 2
            }
        }

        private fun findMissingVars() {
            var eqIndex = 0
            while (eqIndex < system.rows) {
                missingVars.or(system.equations[eqIndex])
                eqIndex++
            }
            missingVars.invert(system.cols)
        }

        override fun toString(): String {
            return badVars.toString(system.cols)
        }
    }
    //#endregion

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
