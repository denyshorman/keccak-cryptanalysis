package keccak

import keccak.util.*
import mu.KotlinLogging
import java.util.*

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

    fun substitute(index: Int, value: Boolean) {
        var i = 0

        while (i < rows) {
            if (equations[i].andOpLeft[index]) {
                equations[i].andOpLeft.clear(index)
                andOpLeftResults.xor(index, value)
            }

            if (equations[i].andOpRight[index]) {
                equations[i].andOpRight.clear(index)
                andOpRightResults.xor(index, value)
            }

            if (equations[i].rightXor[index]) {
                equations[i].rightXor.clear(index)
                rightXorResults.xor(index, value)
            }

            i++
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
                val firstBitIndex = xorSystem.equations[xorEqIndex].nextSetBit(0)

                if (firstBitIndex >= 0 && (mask == null || mask[firstBitIndex])) {
                    var andEqIndex = 0

                    while (andEqIndex < rows) {
                        if (substituteLeft && !equations[andEqIndex].andOpLeft.isEmpty && equations[andEqIndex].andOpLeft[firstBitIndex]) {
                            equations[andEqIndex].andOpLeft.xor(xorSystem.equations[xorEqIndex])
                            andOpLeftResults.xor(andEqIndex, xorSystem.results[xorEqIndex])
                        }

                        if (substituteRight && !equations[andEqIndex].andOpRight.isEmpty && equations[andEqIndex].andOpRight[firstBitIndex]) {
                            equations[andEqIndex].andOpRight.xor(xorSystem.equations[xorEqIndex])
                            andOpRightResults.xor(andEqIndex, xorSystem.results[xorEqIndex])
                        }

                        if (substituteResult && !equations[andEqIndex].rightXor.isEmpty && equations[andEqIndex].rightXor[firstBitIndex]) {
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

    fun simplify(): AndEquationSystem {
        val system = AndEquationSystem(2 * rows, cols)

        var i = 0
        var j = 0
        while (i < rows) {
            system.equations[j].andOpLeft = equations[i].andOpLeft.clone() as BitSet
            system.andOpLeftResults.setIfTrue(j, andOpLeftResults[i])

            system.equations[j].andOpRight = equations[i].andOpRight.clone() as BitSet
            system.equations[j].andOpRight.xor(equations[i].rightXor)
            system.andOpRightResults.setIfTrue(j, andOpRightResults[i] xor rightXorResults[i])

            j++

            system.equations[j].andOpLeft = equations[i].rightXor.clone() as BitSet
            system.andOpLeftResults.setIfTrue(j, rightXorResults[i])

            system.equations[j].andOpRight = equations[i].andOpLeft.clone() as BitSet
            system.andOpRightResults.setIfTrue(j, andOpLeftResults[i] xor true)

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
    class PivotSolutionAlgorithm {
        private val pivotSolution: BitSet
        private val solutionPairs: SolutionPairsCounter
        private val badVarsCounter: BadVarsCounter
        private val badVarsSimpleCounter: BadVarsSimpleCounter
        private val varsCount: Int

        val invertedSystem: XorEquationSystem

        constructor(
            system: AndEquationSystem,
            pivotSolution: BitSet,
        ) {
            system.rotate(pivotSolution, left = false, right = false)

            this.pivotSolution = pivotSolution
            varsCount = system.cols
            invertedSystem = system.invertToXorSystem()
            solutionPairs = SolutionPairsCounter(invertedSystem, varsCount, system.rows)
            badVarsCounter = BadVarsCounter(invertedSystem, solutionPairs, varsCount)
            badVarsSimpleCounter = BadVarsSimpleCounter(invertedSystem, varsCount)
        }

        fun solve(): Set<BitSet> {
            var varIndex = 0

            if (solutionPairs.hasSolution()) {
                varIndex = varsCount
            } else if (badVarsSimpleCounter.hasSolution()) {
                val solutions = badVarsSimpleCounter.solutions().filter { it != pivotSolution }.toSet()

                if (solutions.isNotEmpty()) {
                    return solutions
                }
            }

            initVarsLoop@ while (varIndex < varsCount) {
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

                    solutionPairs.clear(eqIndex)

                    invertedSystem.expressVariable(
                        eqIndex,
                        varIndex,
                        varSubstituted = ::varSubstitutedHandler,
                    )

                    badVarsSimpleCounter.recalculate()

                    if (solutionPairs.hasSolution()) {
                        break@initVarsLoop
                    } else if (badVarsSimpleCounter.hasSolution()) {
                        val solutions = badVarsSimpleCounter.solutions().filter { it != pivotSolution }.toSet()

                        if (solutions.isNotEmpty()) {
                            return solutions
                        } else {
                            break
                        }
                    } else {
                        break
                    }
                }

                varIndex++
            }

            if (solutionPairs.hasSolution()) {
                val solution = BitSet(varsCount)
                var allVarsExtracted = true

                varIndex = 0
                while (varIndex < varsCount) {
                    if (invertedSystem.varEqMap[varIndex] != -1) {
                        solution.setIfTrue(varIndex, invertedSystem.results[invertedSystem.varEqMap[varIndex]])
                    } else if (allVarsExtracted) {
                        allVarsExtracted = false
                    }
                    varIndex++
                }

                if (!allVarsExtracted && pivotSolution != solution) {
                    return setOf(solution)
                }
            } else if (badVarsSimpleCounter.hasSolution()) {
                val solutions = badVarsSimpleCounter.solutions().filter { it != pivotSolution }.toSet()

                if (solutions.isNotEmpty()) {
                    return solutions
                }
            }

            return emptySet()
        }

        private fun varSubstitutedHandler(eqIndex: Int): Boolean {
            solutionPairs.update(eqIndex)
            return true
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
                val l = system.results[system.varEqMap[varIndex]]
                val r = system.results[system.varEqMap[varIndex + 1]]
                if (l && r) {
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
            val var1 = toCompanionVarIndex(var0)

            if (
                system.varEqMap[var0] != -1 &&
                system.varEqMap[var1] != -1 &&
                system.results[system.varEqMap[var0]] &&
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
        val solutionPairs: SolutionPairsCounter,
        val varsCount: Int,
    ) {
        val badVars = IntArray(system.cols)

        init {
            calculate()
        }

        fun recalculate() {
            badVars.fill(0)
            calculate()
        }

        fun recalculate(var0: Int, var1: Int) {
            val var0Comp: Int
            val var1Comp: Int

            val eq0: Int
            val eq1: Int
            val eq0Comp: Int
            val eq1Comp: Int

            if (var0 == -1) {
                var0Comp = -1
                eq0 = -1
                eq0Comp = -1
            } else {
                var0Comp = solutionPairs.toCompanionVarIndex(var0)
                eq0 = system.varEqMap[var0]
                eq0Comp = system.varEqMap[var0Comp]
            }

            if (var1 == -1) {
                var1Comp = -1
                eq1 = -1
                eq1Comp = -1
            } else {
                var1Comp = solutionPairs.toCompanionVarIndex(var1)
                eq1 = system.varEqMap[var1]
                eq1Comp = system.varEqMap[var1Comp]
            }

            update(var0, var0Comp, eq0, eq0Comp, delta = -1)
            update(var1, var1Comp, eq1, eq1Comp, delta = -1)

            val eq0Delta = -1
            val eq1Delta: Int
            val eq1CompDelta: Int

            if (eq1 == -1) {
                eq1Delta = eq0
                eq1CompDelta = eq1Comp
            } else {
                eq1Delta = eq1
                eq1CompDelta = eq0
            }

            update(var0, var0Comp, eq0Delta, eq0Comp, delta = 1)
            update(var1, var1Comp, eq1Delta, eq1CompDelta, delta = 1)

            var eqIndex = 0
            while (eqIndex < system.rows) {
                if (eqIndex != eq1 && system.equations[eqIndex][var1]) {
                    val var2 = system.eqVarMap[eqIndex]
                    val var2Comp: Int
                    val eq2Comp: Int

                    if (var2 != -1) {
                        var2Comp = solutionPairs.toCompanionVarIndex(var2)
                        eq2Comp = system.varEqMap[var2Comp]
                    } else {
                        var2Comp = -1
                        eq2Comp = -1
                    }

                    update(var2, var2Comp, eqIndex, eq2Comp, delta = -1)
                }

                eqIndex++
            }
        }

        fun isValidWithFreeBits(validateFreeBits: Boolean = true): Boolean {
            var i = 0
            while (i < badVars.size) {
                if (isValid(i, validateFreeBits)) {
                    return true
                }

                i++
            }
            return false
        }

        fun solutions(validateFreeBits: Boolean = true): Sequence<BitSet> {
            return sequence {
                var i = 0
                while (i < badVars.size) {
                    if (isValid(i, validateFreeBits)) {
                        yield(i)
                    }
                    i++
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
            var newVarIndex = varsCount

            while (newVarIndex < system.cols) {
                val newVarCompIndex = newVarIndex + 1
                val eq0 = system.varEqMap[newVarIndex]
                val eq1 = system.varEqMap[newVarCompIndex]

                update(newVarIndex, newVarCompIndex, eq0, eq1, delta = 1)

                newVarIndex += 2
            }
        }

        private fun update(var0: Int, var1: Int, eq0: Int, eq1: Int, delta: Int) {
            if (eq0 != -1 && eq1 != -1) {
                system.equations[eq0].iterateOverAllSetBits { varIndex ->
                    if (system.equations[eq1][varIndex]) {
                        badVars[varIndex] += delta
                    }
                }
            } else if (eq0 != -1) {
                if (system.equations[eq0][var1]) {
                    badVars[var1] += delta
                }
            } else if (eq1 != -1) {
                if (system.equations[eq1][var0]) {
                    badVars[var0] += delta
                }
            }
        }

        private fun isValid(varIndex: Int, validateFreeBits: Boolean): Boolean {
            return badVars[varIndex] == 0 &&
                    system.varEqMap[varIndex] == -1 &&
                    (!validateFreeBits || isValidWithFreeBits(varIndex))
        }

        private fun isValidWithFreeBits(varIndex: Int): Boolean {
            var i = varsCount
            while (i < system.cols) {
                val lEq = system.varEqMap[i]
                val rEq = system.varEqMap[i + 1]

                val l: Boolean
                val r: Boolean

                if (lEq != -1 && rEq != -1) {
                    l = system.results[lEq] xor system.equations[lEq][varIndex]
                    r = system.results[rEq] xor system.equations[rEq][varIndex]
                } else if (lEq != -1) {
                    l = system.results[lEq] xor system.equations[lEq][varIndex]
                    r = (i + 1) == varIndex
                } else if (rEq != -1) {
                    l = (i + 1) == varIndex
                    r = system.results[rEq] xor system.equations[rEq][varIndex]
                } else {
                    l = false
                    r = false
                }

                if (l && r) return false

                i += 2
            }
            return true
        }

        override fun toString(): String {
            return badVars.asSequence().map { }.joinToString(" ")
        }
    }

    class BadVarsSimpleCounter(
        val system: XorEquationSystem,
        val varsCount: Int,
    ) {
        private val tmp = BitSet(system.cols)
        val badVars = BitSet(system.cols)

        init {
            calculate()
        }

        fun recalculate() {
            badVars.clear()
            calculate()
        }

        fun hasSolution(): Boolean {
            var solutionExists = false
            badVars.iterateOverClearBits(0, system.cols) {
                solutionExists = true
                false
            }
            return solutionExists
        }

        fun solutions(): Sequence<BitSet> {
            return sequence {
                badVars.iterateOverAllClearBits(0, system.cols) {
                    yield(it)
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
            var newVarIndex = varsCount

            while (newVarIndex < system.cols) {
                val newVarCompIndex = newVarIndex + 1
                val eq0 = system.varEqMap[newVarIndex]
                val eq1 = system.varEqMap[newVarCompIndex]

                if (eq0 != -1 && eq1 != -1) {
                    val r0 = system.results[eq0]
                    val r1 = system.results[eq1]

                    if (!r0 && !r1) {
                        tmp.xor(system.equations[eq0])
                        tmp.and(system.equations[eq1])
                        tmp.or(newVarIndex, true)
                        tmp.or(newVarCompIndex, true)
                    } else if (!r0) {
                        tmp.xor(system.equations[eq0])
                        tmp.andNot(system.equations[eq1])
                        tmp.set(newVarCompIndex)
                    } else if (!r1) {
                        tmp.xor(system.equations[eq1])
                        tmp.andNot(system.equations[eq0])
                        tmp.set(newVarIndex)
                    } else {
                        tmp.xor(system.equations[eq0])
                        tmp.and(system.equations[eq1])
                        tmp.invert(system.cols)
                    }

                    badVars.or(tmp)
                    tmp.clear()
                } else if (eq0 != -1) {
                    badVars.or(newVarIndex, true)

                    if (system.equations[eq0][newVarCompIndex] || system.results[eq0]) {
                        badVars.or(newVarCompIndex, true)
                    }
                } else if (eq1 != -1) {
                    badVars.or(newVarCompIndex, true)

                    if (system.equations[eq1][newVarIndex] || system.results[eq1]) {
                        badVars.or(newVarIndex, true)
                    }
                }

                newVarIndex += 2
            }
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
