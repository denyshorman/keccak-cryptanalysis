package keccak

import keccak.util.invert
import keccak.util.setBitsCount
import java.util.*

class CombinationIterator(
    val varsCount: Int,
    val mask: BitSet = defaultMask(varsCount),
) {
    val combination = BitSet(varsCount)
    val solutionsCount = 1 shl mask.setBitsCount()
    var solutionIndex = -1L
        private set

    fun hasNext(): Boolean {
        return solutionIndex + 1 < solutionsCount
    }

    fun next() {
        var i = mask.previousSetBit(varsCount - 1)

        while (i >= 0) {
            if (combination[i]) {
                combination.clear(i)
                i = mask.previousSetBit(i - 1)
            } else {
                combination.set(i)
                break
            }
        }

        solutionIndex++
    }

    companion object {
        private fun defaultMask(size: Int): BitSet {
            val bitSet = BitSet(size)
            bitSet.invert(size)
            return bitSet
        }
    }
}
