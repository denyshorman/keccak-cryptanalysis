package keccak.util

import keccak.CombinationIterator
import keccak.XorAndEquationSystem
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger {}

fun XorAndEquationSystem.solveKeccak256(): BitSet {
    //#region Init
    val solution = BitSet(cols)
    val solutionMask = BitSet(cols)
    val usedVarsMask = BitSet(cols)
    val messageMask = BitSet(cols)
    val setVarsQueue = LinkedList<Pair<Int, Boolean>>()
    val constraintsStartIndex = andSystem.equations[0].rightXor.nextSetBit(0)
    val combinationIterator = CombinationIterator(varsCount = xorSystem.rows, algorithm = CombinationIterator.Algorithm.Increasing)
    val combination = BitSet(cols)
    var rowIndex = 0
    messageMask.set(0, constraintsStartIndex)
    //#endregion

    //#region Util functions
    fun extractValueFromAutoSolvedEquation() {
        if (andSystem.equations[rowIndex].rightXor.isEmpty) {
            return
        }

        val leftEmpty = andSystem.equations[rowIndex].andOpLeft.isEmpty
        val rightEmpty = andSystem.equations[rowIndex].andOpRight.isEmpty

        if (!leftEmpty && !rightEmpty) {
            return
        }

        val firstSetBitIndex = andSystem.equations[rowIndex].rightXor.nextSetBit(0)

        if (firstSetBitIndex == -1) {
            throw IllegalStateException()
        }

        if (leftEmpty && rightEmpty) {
            val res = andSystem.andOpLeftResults[rowIndex] && andSystem.andOpRightResults[rowIndex]

            setVarsQueue.add(firstSetBitIndex to res)

            andSystem.equations[rowIndex].rightXor.clear()
            andSystem.andOpLeftResults.clear(rowIndex)
            andSystem.andOpRightResults.clear(rowIndex)
        } else if (leftEmpty) {
            if (andSystem.andOpLeftResults[rowIndex]) {
                TODO("Add to xor equations list")
            } else {
                andSystem.equations[rowIndex].andOpRight.clear()
                andSystem.equations[rowIndex].rightXor.clear()
                andSystem.andOpLeftResults.clear(rowIndex)
                andSystem.andOpRightResults.clear(rowIndex)

                setVarsQueue.add(firstSetBitIndex to false)
            }
        } else if (rightEmpty) {
            if (andSystem.andOpRightResults[rowIndex]) {
                TODO("Add to xor equations list")
            } else {
                andSystem.equations[rowIndex].andOpLeft.clear()
                andSystem.equations[rowIndex].rightXor.clear()
                andSystem.andOpLeftResults.clear(rowIndex)
                andSystem.andOpRightResults.clear(rowIndex)

                setVarsQueue.add(firstSetBitIndex to false)
            }
        }
    }

    fun prove(vars: BitSet, value: Boolean): Boolean {
        var res: Boolean

        combinationIterator.reset()

        while (combinationIterator.solutionIndex < 5527041) {
            combination.clear()
            res = true

            combination.xor(vars)
            res = res xor value

            var i = combinationIterator.combination.nextSetBit(0)
            while (i >= 0) {
                combination.xor(xorSystem.equations[i])
                res = res xor xorSystem.results[i]
                i = combinationIterator.combination.nextSetBit(i + 1)
            }

            if (combination.isEmpty) {
                logger.warn("Combination was simplified during prove process")
                TODO("Combination simplified")
            }

            if (((combination.setBitsCount() and 1) == 0) xor res) {
                return true
            }

            if (modPow2(combinationIterator.solutionIndex, 8192) == 0L) {
                logger.info("Tried to prove $rowIndex rows")
            }

            if (combinationIterator.hasNext()) {
                combinationIterator.next()
            } else {
                return false
            }
        }

        return false
    }
    //#endregion

    //#region Calculate used variables mask
    while (rowIndex < xorSystem.rows) {
        usedVarsMask.or(xorSystem.equations[rowIndex])
        rowIndex++
    }
    //#endregion

    //#region Remove redundant And equations
    logger.info("Removing redundant equations")
    rowIndex = 0
    var redundantEquations = 0
    while (rowIndex < andSystem.rows) {
        val constraintVarIndex = andSystem.equations[rowIndex].rightXor.nextSetBit(0)

        if (!usedVarsMask[constraintVarIndex]) {
            andSystem.equations[rowIndex].andOpLeft.clear()
            andSystem.equations[rowIndex].andOpRight.clear()
            andSystem.equations[rowIndex].rightXor.clear()
            andSystem.andOpLeftResults.clear(rowIndex)
            andSystem.andOpRightResults.clear(rowIndex)
            andSystem.rightXorResults.clear(rowIndex)
            redundantEquations++
        }
        rowIndex++
    }
    logger.info("Removed $redundantEquations redundant equations")
    //#endregion

    //#region Solve xor equation system
    logger.info("Solving xor equation system")
    xorSystem.solve(skipValidation = true)
    logger.info("Solved xor equation system")
    //#endregion

    //#region Substitute And equation system with Xor
    logger.info("Start And equation system substitution with Xor")
    substituteAndWithXor(messageMask)
    logger.info("And equation system substitution has been completed")
    //#endregion

    //#region Find auto-solved equations and substitute variables
    while (true) {
        //#region Find auto-solved equations
        logger.info("Searching for auto-solved equations")
        rowIndex = 0
        while (rowIndex < andSystem.rows) {
            extractValueFromAutoSolvedEquation()
            rowIndex++

            if (modPow2(rowIndex, 4096) == 0) {
                logger.info("Processed $rowIndex rows")
            }
        }
        logger.info("")
        //#endregion

        if (setVarsQueue.isEmpty()) {
            logger.info("Auto-solved variables not found. Exit.")
            break
        } else {
            logger.info("Found ${setVarsQueue.size} auto-solved variables.")
        }

        //#region Substitute variables
        logger.info("Start variable substitution process")

        for ((index, value) in setVarsQueue) {
            solutionMask.set(index)
            solution.setIfTrue(index, value)
        }

        substitute(setVarsQueue)

        setVarsQueue.clear()

        logger.info("Variable substitution process has been completed")
        //#endregion
    }
    //#endregion

    //#region Normalize xor equation system
    logger.info("Normalizing xor equation system")
    rowIndex = 0
    while (rowIndex < xorSystem.rows) {
        val firstSetBitIndex = xorSystem.equations[rowIndex].nextSetBit(0, constraintsStartIndex)

        if (firstSetBitIndex >= 0) {
            val setBitsCount = xorSystem.equations[rowIndex].setBitsCount()
            val value = ((setBitsCount and 1) == 0) xor xorSystem.results[rowIndex]
            solutionMask.set(firstSetBitIndex)
            solution.setIfTrue(firstSetBitIndex, value)
            xorSystem.substitute(firstSetBitIndex, value)
        } else {
            logger.warn("Can't normalize equation $rowIndex")
        }

        rowIndex++
    }
    logger.info("Normalized xor equation system")
    //#endregion

    //#region Prove that normalization is valid
    logger.info("Start normalization prove")
    rowIndex = 0
    while (rowIndex < andSystem.rows) {
        if (!andSystem.equations[rowIndex].rightXor.isEmpty) {
            var proved = prove(andSystem.equations[rowIndex].andOpLeft, andSystem.andOpLeftResults[rowIndex])

            if (!proved) {
                logger.warn("Can't prove $rowIndex left")
                TODO("Not proved")
            }

            proved = prove(andSystem.equations[rowIndex].andOpRight, andSystem.andOpRightResults[rowIndex])

            if (!proved) {
                logger.warn("Can't prove $rowIndex right")
                TODO("Not proved")
            }
        }

        rowIndex++

        if (modPow2(rowIndex, 4096) == 0) {
            logger.info("Proved $rowIndex rows")
        }
    }
    logger.info("End normalization prove")
    //#endregion

    //#region Build final solution

    //#endregion

    return solution
}
