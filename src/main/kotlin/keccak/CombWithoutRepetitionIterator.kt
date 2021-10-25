package keccak

class CombWithoutRepetitionIterator(val n: Int, val r: Int) {
    val combination = IntArray(r)
    val count = calcCombinationsCount()
    var index = -1L
        private set

    init {
        for (i in 0 until r) {
            combination[i] = i
        }
    }

    fun hasNext(): Boolean {
        return combination[r - 1] < n
    }

    fun next() {
        var t = r - 1

        while (t != 0 && combination[t] == n - r + t) {
            t--
        }

        combination[t]++

        for (i in t + 1 until r) {
            combination[i] = combination[i - 1] + 1
        }

        index++
    }

    inline fun iterate(callback: () -> Unit) {
        while (true) {
            callback()
            if (hasNext()) next() else break
        }
    }

    private fun calcCombinationsCount(): Long {
        var count = n.toLong()
        var x = 1L

        while (x <= r - 1) {
            count = count * (n - x) / x
            x++
        }

        return count / r
    }
}
