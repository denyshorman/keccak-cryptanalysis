package keccak.util

import java.util.*

fun BitSet.exchange(i: Int, j: Int) {
    val tmp = this[i]
    this[i] = this[j]
    this[j] = tmp
}

fun BitSet.setBitsCount(): Int {
    return cardinality()
}

fun BitSet.xor(bitIndex: Int, value: Boolean) {
    this[bitIndex] = this[bitIndex] xor value
}

fun BitSet.invert(size: Int) {
    flip(0, size)
}

fun bitSet(vararg values: Boolean): BitSet {
    val set = BitSet(values.size)
    values.forEachIndexed { index, value ->
        set[index] = value
    }
    return set
}

fun bitSet(vararg values: Int): BitSet {
    val set = BitSet(values.size)
    values.forEachIndexed { index, value ->
        set[index] = value == 1
    }
    return set
}
