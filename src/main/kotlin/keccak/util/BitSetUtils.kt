package keccak.util

import keccak.Bit
import keccak.Node
import keccak.Variable
import keccak.Xor
import java.util.*
import kotlin.random.Random

fun BitSet(bits: String): BitSet {
    val bitSet = BitSet(bits.length)
    var i = 0
    while (i < bits.length) {
        if (bits[i].toBoolean()) {
            bitSet.set(i)
        }
        i++
    }
    return bitSet
}

fun BitSet.exchange(i: Int, j: Int) {
    val tmp = this[i]
    this[i] = this[j]
    this[j] = tmp
}

fun BitSet.setBitsCount(): Int {
    return cardinality()
}

fun BitSet.setIfTrue(bitIndex: Int, value: Boolean) {
    if (value) {
        set(bitIndex)
    }
}

fun BitSet.setIfFalse(bitIndex: Int, value: Boolean) {
    if (!value) {
        set(bitIndex)
    }
}

fun BitSet.clearIfTrue(bitIndex: Int, value: Boolean) {
    if (value) {
        clear(bitIndex)
    }
}

fun BitSet.clearIfFalse(bitIndex: Int, value: Boolean) {
    if (!value) {
        clear(bitIndex)
    }
}

fun BitSet.nextSetBit(fromIndex: Int, toIndex: Int): Int {
    val bitIndex = nextSetBit(fromIndex)

    @Suppress("ConvertTwoComparisonsToRangeCheck")
    return if (bitIndex >= 0 && bitIndex < toIndex) {
        bitIndex
    } else {
        -1
    }
}

fun BitSet.nextSetBit(mask: BitSet): Int {
    var i = 0

    do {
        i = mask.nextSetBit(i)
        if (i == -1) return -1
        i = nextSetBit(i)
    } while (i >= 0 && !mask[i])

    return i
}

fun BitSet.nextSetBitDefault(fromIndex: Int, defaultIfNotFound: Int): Int {
    val bitIndex = nextSetBit(fromIndex)

    return if (bitIndex == -1) {
        defaultIfNotFound
    } else {
        bitIndex
    }
}

fun BitSet.xor(bitIndex: Int, value: Boolean) {
    this[bitIndex] = this[bitIndex] xor value
}

fun BitSet.invertValue(bitIndex: Int) {
    this[bitIndex] = !this[bitIndex]
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

inline fun BitSet.iterateOverAllSetBits(callback: (Int) -> Unit) {
    var i = nextSetBit(0)

    while (i >= 0) {
        callback(i)
        if (i == Int.MAX_VALUE) break
        i = nextSetBit(i + 1)
    }
}

inline fun BitSet.iterateOverSetBits(callback: (Int) -> Boolean) {
    var i = nextSetBit(0)

    while (i >= 0) {
        if (!callback(i) || i == Int.MAX_VALUE) break
        i = nextSetBit(i + 1)
    }
}

fun BitSet.isSecondOrderEq(firstOrderSystemVarsCount: Int): Boolean {
    var secondOrderEq = false

    iterateOverSetBits { i ->
        val (l, r) = calcCombinationPartialIndex(i, firstOrderSystemVarsCount)
        if (l != r) secondOrderEq = true
        !secondOrderEq
    }

    return secondOrderEq
}

fun BitSet.toString(size: Int): String {
    return String(CharArray(size) { this[it].toNumChar() })
}

fun BitSet.toXorString(
    freeBit: Boolean = false,
    varPrefix: String = "x",
    varOffset: Int = 0,
    defaultIfEmpty: String = "0",
): String {
    if (isEmpty) {
        return if (freeBit) freeBit.toNumChar().toString() else defaultIfEmpty
    }

    var bitIndex = nextSetBit(0)
    val vars = LinkedList<String>()

    while (bitIndex >= 0) {
        vars.add("$varPrefix${bitIndex + varOffset}")
        bitIndex = nextSetBit(bitIndex + 1)
    }

    if (freeBit) {
        vars.add(freeBit.toNumChar().toString())
    }

    return vars.joinToString(separator = " + ")
}

fun BitSet.toNode(
    freeBit: Boolean = false,
    varPrefix: String = "x",
    varOffset: Int = 0,
): Node {
    var i = nextSetBit(0)
    val nodes = LinkedList<Node>()
    nodes.add(Bit(freeBit))
    while (i >= 0) {
        nodes.add(Variable("$varPrefix${i + varOffset}"))
        i = nextSetBit(i + 1)
    }
    return Xor(nodes)
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
