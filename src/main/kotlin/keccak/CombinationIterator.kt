package keccak

import keccak.util.invert
import keccak.util.setBitsCount
import keccak.util.toString
import java.util.*

class CombinationIterator(
    val varsCount: Int,
    val mask: BitSet = defaultMask(varsCount),
    val algorithm: Algorithm = Algorithm.Lexicographical,
) {
    val combination = BitSet(varsCount)
    val solutionsCount = 1L shl mask.setBitsCount()
    var solutionIndex = -1L
        private set

    fun hasNext(): Boolean {
        return solutionIndex + 1 < solutionsCount
    }

    fun next() {
        when (algorithm) {
            Algorithm.Lexicographical -> lexicographicalNext()
            Algorithm.Increasing -> increasingNext()
        }

        solutionIndex++
    }

    fun reset() {
        solutionIndex = -1L
        combination.clear()
    }

    override fun toString(): String {
        return combination.toString(varsCount)
    }

    private fun lexicographicalNext() {
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
    }

    private fun increasingNext() {
        var i = 0

        while (i < varsCount) {
            if (combination[i]) {
                if (i == 0) {
                    i++

                    while (i < varsCount) {
                        if (!combination[i]) {
                            break
                        }
                        i++
                    }

                    i++

                    while (i < varsCount) {
                        if (combination[i]) {
                            combination.clear(i)
                            combination.set(i - 1)

                            i--
                            var j = i

                            while (i >= 0) {
                                if (combination[i]) {
                                    combination.clear(i)
                                    combination.set(j)
                                    j--
                                }

                                i--
                            }

                            return
                        }

                        i++
                    }
                } else {
                    combination.clear(i)
                    combination.set(i - 1)
                    return
                }
            }

            i++
        }

        if (combination[varsCount - 1]) {
            combination.clear()
        } else {
            combination.set(varsCount - 1)

            i = varsCount - 2
            var j = i
            while (i >= 0) {
                if (combination[i]) {
                    combination.clear(i)
                    combination.set(j)
                    j--
                }
                i--
            }
        }
    }

    enum class Algorithm {
        Lexicographical,
        Increasing,
    }

    companion object {
        private fun defaultMask(size: Int): BitSet {
            val bitSet = BitSet(size)
            bitSet.invert(size)
            return bitSet
        }
    }
}
