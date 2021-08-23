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

fun BitSet.evaluate(varsCount: Int, values: BitSet): Boolean {
    var result = false
    var i = 0
    while (i < varsCount) {
        if (this[i] && values[i]) {
            result = result xor true
        }
        i++
    }
    return result
}

fun BitSet.toString(size: Int): String {
    return String(CharArray(size) { this[it].toNumChar() })
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

fun randomBitSet(size: Int, random: Random = Random): BitSet {
    val bitSet = BitSet(size)

    var i = 0
    while (i < size) {
        if (random.nextBoolean()) {
            bitSet.set(i)
        }
        i++
    }

    return bitSet
}
