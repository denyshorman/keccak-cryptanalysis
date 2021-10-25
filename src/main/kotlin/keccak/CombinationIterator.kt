package keccak

import keccak.util.invert
import keccak.util.toString
import java.util.*

class CombinationIterator(
    val varsCount: Int,
    val mask: BitSet = defaultMask(varsCount),
    val algorithm: Algorithm = Algorithm.Lexicographical,
) {
    val combination = BitSet(varsCount)

    val next = when (algorithm) {
        Algorithm.Lexicographical -> ::lexicographicalNext
        Algorithm.Increasing -> ::increasingNext
    }

    fun hasNext(): Boolean {
        var i = mask.nextSetBit(0)

        while (i >= 0) {
            if (!combination[i]) {
                return true
            }

            i = mask.nextSetBit(i + 1)
        }

        return false
    }

    fun reset() {
        combination.clear()
    }

    inline fun iterateAll(callback: () -> Unit) {
        while (true) {
            callback()
            if (hasNext()) next() else break
        }
    }

    inline fun iterate(callback: () -> Boolean) {
        while (callback() && hasNext()) {
            next()
        }
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
