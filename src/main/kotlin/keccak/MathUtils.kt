package keccak

fun pow(a: Int, b: Int): Long {
    var count = 1L
    var i = 0
    while (i < b) {
        count *= a
        i++
    }
    return count
}
