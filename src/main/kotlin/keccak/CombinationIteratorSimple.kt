package keccak

import keccak.util.clear
import keccak.util.set
import keccak.util.toBitString

class CombinationIteratorSimple(
    val varsCount: Int,
    val algorithm: Algorithm = Algorithm.Lexicographical,
) {
    val combination = BooleanArray(varsCount) { false }

    val next = when (algorithm) {
        Algorithm.Lexicographical -> ::lexicographicalNext
        Algorithm.Increasing -> ::increasingNext
    }

    fun hasNext(): Boolean {
        var i = 0

        while (i < varsCount) {
            if (!combination[i]) {
                return true
            }

            i++
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
        reset()
    }

    inline fun iterate(callback: () -> Boolean) {
        while (callback() && hasNext()) {
            next()
        }
        reset()
    }

    override fun toString(): String {
        return combination.toBitString()
    }

    private fun lexicographicalNext() {
        var i = varsCount - 1

        while (i >= 0) {
            if (combination[i]) {
                combination.clear(i)
                i--
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
}
