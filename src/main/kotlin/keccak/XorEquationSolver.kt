package keccak

import keccak.util.pow
import keccak.util.setBitsCount
import java.util.*

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
