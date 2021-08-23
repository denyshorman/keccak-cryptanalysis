package keccak

import keccak.util.pow
import keccak.util.setBitsCount
import java.util.*
import kotlin.math.min

data class NoSolution(val eqIndex: Int) : Throwable(null, null, false, false)

class SolutionsFinder(val equationSystem: XorEquationSystem) {
    val mask = BitSet(equationSystem.cols)
    val iterator = BitSet(equationSystem.cols)
    var solution = BitSet(equationSystem.rows)
    var solutionIndex = 0
    val solutionsCount: Long

    init {
        initMask()
        solutionsCount = pow(2, mask.setBitsCount())
    }

    fun hasNext(): Boolean {
        return solutionIndex + 1 < solutionsCount
    }

    fun next() {
        solution = equationSystem.results.clone() as BitSet

        var i = 0
        while (i < equationSystem.rows) {
            val setBits = iterator.clone() as BitSet
            setBits.and(equationSystem.equations[i])
            if (setBits.setBitsCount() % 2 != 0) {
                solution[i] = solution[i] xor true
            }
            i++
        }

        solutionIndex++
        iteratorIncrement()
    }

    private fun initMask() {
        var i = 0
        while (i < equationSystem.rows) {
            mask.or(equationSystem.equations[i])
            mask.clear(i)
            i++
        }
    }

    private fun iteratorIncrement() {
        var i = mask.previousSetBit(equationSystem.cols - 1)

        while (i >= 0) {
            if (iterator[i]) {
                iterator.clear(i)
                i = mask.previousSetBit(i - 1)
            } else {
                iterator.set(i)
                break
            }
        }
    }
}

fun solveXorEquationSystem(system: XorEquationSystem) {
    var row = 0
    var col = 0

    while (row < system.rows && col < system.cols) {
        var i = row
        var found = false

        while (i < system.rows) {
            if (system.isInvalid(i)) {
                throw NoSolution(i)
            }

            if (system.equations[i][col]) {
                found = true
                break
            }

            i++
        }

        if (found) {
            if (row != i) {
                system.exchange(row, i)
            }

            i = row + 1

            while (i < system.rows) {
                if (system.equations[i][col]) {
                    system.xor(i, row)

                    if (system.isInvalid(i)) {
                        throw NoSolution(i)
                    }
                }

                i++
            }
        }

        row++
        col++
    }

    row = min(system.rows, system.cols) - 1
    col = row

    while (row >= 0 && col >= 0) {
        if (system.equations[row].isEmpty) {
            if (system.results[row]) {
                throw NoSolution(row)
            }
        } else {
            var i = row - 1

            while (i >= 0) {
                if (system.equations[i][col]) {
                    system.xor(i, row)

                    if (system.isInvalid(i)) {
                        throw NoSolution(i)
                    }
                }

                i--
            }
        }

        row--
        col--
    }
}

fun solveSquareMatrixXorEquations(system: XorEquationSystem) {
    var row = 0
    var col = system.cols - 1

    while (row < system.rows && col >= 0) {
        var i = row
        var found = false

        while (i < system.rows) {
            if (system.isInvalid(i)) {
                throw NoSolution(i)
            }

            if (system.equations[i][col]) {
                found = true
                break
            }

            i++
        }

        if (found) {
            if (row != i) {
                system.exchange(row, i)
            }

            i = 0

            while (i < system.rows) {
                if (i != row && system.equations[i][col]) {
                    system.xor(i, row)

                    if (system.isInvalid(i)) {
                        throw NoSolution(i)
                    }
                }

                i++
            }
        }

        row++
        col--
    }
}

fun getRidOfVariables2(system: XorEquationSystem, startIndex: Int) {
    val random = Random(0x1122334455667788L)
    var eqIndex: Int
    var foundEqIndex: Int
    var varIndex = startIndex
    val tmpEq = XorEquation(system.cols)

    while (varIndex < system.cols) {
        eqIndex = 0
        foundEqIndex = -1

        while (eqIndex < system.rows) {
            if (system.equations[eqIndex][varIndex]) {
                foundEqIndex = eqIndex++
                break
            }

            eqIndex++
        }

        if (foundEqIndex != -1) {
            while (eqIndex < system.rows) {
                if (system.equations[eqIndex][varIndex]) {
                    system.xor(eqIndex, foundEqIndex)
                }

                eqIndex++
            }

            eqIndex = 0
            tmpEq.clear()

            while (eqIndex < system.rows) {
                if (eqIndex == foundEqIndex || random.nextBoolean()) {
                    tmpEq.xor(system.equations[eqIndex], system.results[eqIndex])
                }

                eqIndex++
            }

            system.xor(foundEqIndex, tmpEq)
        }

        varIndex++
    }
}

fun getRidOfVariables(system: XorEquationSystem, startIndex: Int) {
    val residualVars = XorEquation(system.cols)
    val tmpEq = XorEquation(system.cols)
    var eqIndex: Int
    var rEqIndex: Int
    var varIndex = startIndex

    while (varIndex < system.cols) {
        eqIndex = 0
        rEqIndex = -1

        while (eqIndex < system.rows) {
            if (system.equations[eqIndex][varIndex]) {
                rEqIndex = if (rEqIndex == -1) {
                    eqIndex
                } else {
                    tmpEq.copy(system.equations[rEqIndex], system.results[rEqIndex])

                    system.xor(rEqIndex, eqIndex)
                    system.xor(eqIndex, tmpEq)

                    -1
                }
            }

            eqIndex++
        }

        if (rEqIndex != -1) {
            residualVars.xor(system.equations[rEqIndex], system.results[rEqIndex])
        }

        varIndex++
    }

    eqIndex = 0

    while (eqIndex < system.rows) {
        varIndex = system.equations[eqIndex].nextSetBit(startIndex)

        if (varIndex != -1) {
            system.xor(varIndex, residualVars)
        }

        eqIndex++
    }
}

