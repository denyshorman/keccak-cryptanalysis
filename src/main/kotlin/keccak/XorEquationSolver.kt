package keccak

import keccak.util.pow
import keccak.util.setBitsCount
import java.util.*
import kotlin.math.min

data class NoSolution(val eqIndex: Int) : Throwable(
    "No solution for the system. Failed at equation $eqIndex",
    null,
    false,
    false
)

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
