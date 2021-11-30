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
        rows: Int,
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

    fun solve(
        logProgress: Boolean = false,
    ): XorEquationSystem? {
        var solved: Boolean
        val solutionSystem = XorEquationSystem(cols, cols)

        if (logProgress) {
            println(this)
            println()
            println(solutionSystem)
            println()
        }

        while (true) {
            //region Populate solution system
            var andEqIndex = 0
            var appendIndex = 0
            var hasNotEmptyEq = false

            while (andEqIndex < this.rows) {
                val lRes = this.andOpLeftResults[andEqIndex]
                val rRes = this.andOpRightResults[andEqIndex]
                val lEmpty = this.equations[andEqIndex].andOpLeft.isEmpty
                val rEmpty = this.equations[andEqIndex].andOpRight.isEmpty
                val lEq = this.equations[andEqIndex].andOpLeft
                val rEq = this.equations[andEqIndex].andOpRight

                when {
                    // y1 = 1
                    // y2 = 1
                    lEmpty && rEmpty && lRes && rRes -> {
                        return null
                    }
                    // y1 + a + b + c = 0/1
                    // y2 + a + b + c = 0/1
                    !lEmpty && !rEmpty && lRes == rRes && lEq == rEq -> {
                        appendIndex = solutionSystem.append(lEq, lRes, appendIndex)
                        appendIndex++

                        lEq.clear()
                        rEq.clear()

                        this.andOpLeftResults.clear(andEqIndex)
                        this.andOpRightResults.clear(andEqIndex)
                    }
                    // y1 = 1
                    // y2 + a + b = 0/1
                    lEmpty && lRes && !rEmpty -> {
                        appendIndex = solutionSystem.append(rEq, rRes, appendIndex)
                        appendIndex++

                        rEq.clear()
                        this.andOpRightResults.clear(andEqIndex)
                    }
                    // y1 + a + b = 0/1
                    // y2 = 1
                    !lEmpty && rEmpty && rRes -> {
                        appendIndex = solutionSystem.append(lEq, lRes, appendIndex)
                        appendIndex++

                        lEq.clear()
                        this.andOpLeftResults.clear(andEqIndex)
                    }
                    // y1 + a + b + c = 0/1
                    // y2 + a + d = 0/1
                    !hasNotEmptyEq && !lEmpty && !rEmpty && lEq != rEq -> {
                        hasNotEmptyEq = true
                    }
                    /*// y1 + a = 0
                    // y2 + b + c + d = 0/1
                    lEq.setBitsCount() == 1 && !lRes && !rEq.isEmpty -> {
                        var i = 0
                        var good = true

                        while (i < this.rows) {
                            if (
                                i != andEqIndex &&
                                (lEq == this.equations[i].andOpLeft && this.andOpLeftResults[i] ||
                                lEq == this.equations[i].andOpRight && this.andOpRightResults[i])
                            ) {
                                good = false
                                break
                            }

                            i++
                        }

                        if (good) {
                            *//*appendIndex = solutionSystem.append(lEq, false, appendIndex)
                            appendIndex++*//*

                            lEq.clear()
                        } else {
                            appendIndex = solutionSystem.append(lEq, true, appendIndex)
                            appendIndex++
                        }
                    }
                    // y1 + b + c + d = 0/1
                    // y2 + a = 0
                    rEq.setBitsCount() == 1 && !rRes && !lEq.isEmpty -> {
                        var i = 0
                        var good = true

                        while (i < this.rows) {
                            if (
                                i != andEqIndex &&
                                (rEq == this.equations[i].andOpLeft && this.andOpLeftResults[i] ||
                                rEq == this.equations[i].andOpRight && this.andOpRightResults[i])
                            ) {
                                good = false
                                break
                            }

                            i++
                        }

                        if (good) {
                            *//*appendIndex = solutionSystem.append(rEq, false, appendIndex)
                            appendIndex++*//*

                            rEq.clear()
                        } else {
                            appendIndex = solutionSystem.append(rEq, true, appendIndex)
                            appendIndex++
                        }
                    }*/
                }

                andEqIndex++
            }
            //endregion

            if (logProgress) {
                println(this)
                println()
            }

            if (appendIndex == 0) {
                if (hasNotEmptyEq) {
                    println(this.invertToXorSystem().toNodeEquationSystem(varOffset = 0))
                    TODO("Has more than one solution")
                } else {
                    return solutionSystem
                }
            }

            solved = solutionSystem.solve()

            if (logProgress) {
                println(solutionSystem)
                println()
            }

            if (!solved) {
                return null
            }

            this.substitute(solutionSystem)
        }
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

    fun substitute(xorSystem: XorEquationSystem, mask: BitSet? = null) {
        var xorEqIndex = 0

        while (xorEqIndex < xorSystem.rows) {
            if (!xorSystem.equations[xorEqIndex].isEmpty) {
                val firstBitIndex = xorSystem.equations[xorEqIndex].nextSetBit(0)

                if (firstBitIndex >= 0 && (mask == null || mask[firstBitIndex])) {
                    var andEqIndex = 0

                    while (andEqIndex < rows) {
                        if (!equations[andEqIndex].andOpLeft.isEmpty && equations[andEqIndex].andOpLeft[firstBitIndex]) {
                            equations[andEqIndex].andOpLeft.xor(xorSystem.equations[xorEqIndex])
                            andOpLeftResults.xor(andEqIndex, xorSystem.results[xorEqIndex])
                        }

                        if (!equations[andEqIndex].andOpRight.isEmpty && equations[andEqIndex].andOpRight[firstBitIndex]) {
                            equations[andEqIndex].andOpRight.xor(xorSystem.equations[xorEqIndex])
                            andOpRightResults.xor(andEqIndex, xorSystem.results[xorEqIndex])
                        }

                        if (!equations[andEqIndex].rightXor.isEmpty && equations[andEqIndex].rightXor[firstBitIndex]) {
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

            newXorEqRow++
            newVarIndex++

            xorSystem.equations[newXorEqRow] = equations[andEqRow].andOpRight.clone() as BitSet
            xorSystem.equations[newXorEqRow].set(newVarIndex)
            xorSystem.results.setIfTrue(newXorEqRow, andOpRightResults[andEqRow])

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

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
