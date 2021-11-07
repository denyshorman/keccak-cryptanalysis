package keccak.util

fun pow(a: Int, b: Int): Long {
    var count = 1L
    var i = 0
    while (i < b) {
        count *= a
        i++
    }
    return count
}

fun pow2(n: Int): Long {
    return 1L shl n
}

fun pow2(n: Long): Long {
    return 1L shl n.toInt()
}

fun isPow2(n: Int): Boolean = ((n - 1) and n) == 0

fun modPow2(n: Int, p2: Int): Int = n and (p2 - 1)

fun modPow2(n: Long, p2: Long): Long = n and (p2 - 1)

fun modFast(n: Int, b: Int): Int = if (isPow2(b)) modPow2(n, b) else n % b
