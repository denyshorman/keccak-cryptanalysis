package keccak.util

import java.util.*
import kotlin.random.Random

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

fun BitSet.randomize(size: Int, random: Random = Random) {
    clear()
    var i = 0
    while (i < size) {
        if (random.nextBoolean()) set(i)
        i++
    }
}

fun BitSet.evaluate(vars: BitSet): Boolean {
    var result = false
    var i = vars.nextSetBit(0)
    while (i >= 0) {
        if (this[i]) result = !result
        i = vars.nextSetBit(i + 1)
    }
    return result
}

fun BitSet.toString(size: Int): String {
    return String(CharArray(size) { this[it].toNumChar() })
}

fun bitSet(vararg bits: Boolean): BitSet {
    val bitSet = BitSet(bits.size)
    var i = 0
    while (i < bits.size) {
        if (bits[i]) bitSet.set(i)
        i++
    }
    return bitSet
}

fun bitSet(vararg bits: Int): BitSet {
    val bitSet = BitSet(bits.size)
    var i = 0
    while (i < bits.size) {
        if (bits[i] == 1) bitSet.set(i)
        i++
    }
    return bitSet
}

fun bitSet(bits: String): BitSet {
    val bitSet = BitSet(bits.length)
    var i = 0
    while (i < bits.length) {
        if (bits[i].toBoolean()) bitSet.set(i)
        i++
    }
    return bitSet
}

fun randomBitSet(size: Int, random: Random = Random): BitSet {
    val bitSet = BitSet(size)
    var i = 0
    while (i < size) {
        if (random.nextBoolean()) bitSet.set(i)
        i++
    }
    return bitSet
}
