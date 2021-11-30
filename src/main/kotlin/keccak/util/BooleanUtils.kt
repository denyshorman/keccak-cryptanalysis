package keccak.util

import keccak.*
import java.util.*
import kotlin.random.Random

fun Boolean.toNumChar(): Char = if (this) '1' else '0'

fun Boolean.toBit(): Bit = Bit(this)

fun randomBooleanArray(size: Int, random: Random = Random): BooleanArray {
    return BooleanArray(size) { random.nextBoolean() }
}

fun BooleanArray(bits: String): BooleanArray {
    return BooleanArray(bits.length) { bits[it].toBoolean() }
}

fun BooleanArray.clear() {
    Arrays.fill(this, false)
}

fun BooleanArray.toBitString(): String {
    return String(CharArray(size) { this[it].toNumChar() })
}

fun BooleanArray.toBitArray(): Array<Bit> {
    return Array(size) { Bit(this[it]) }
}

fun BooleanArray.toDecimal(): Long {
    var sum = 0L
    var i = 0
    while (i < size) {
        if (this[i]) sum += pow2(size - i - 1)
        i++
    }
    return sum
}

fun BooleanArray.nextSetBit(bitIndex: Int): Int {
    var j = bitIndex

    while (j < size) {
        if (this[j]) {
            return j
        }

        j++
    }

    return -1
}

fun BooleanArray.previousSetBit(bitIndex: Int): Int {
    var j = bitIndex

    while (j >= 0) {
        if (this[j]) {
            return j
        }

        j--
    }

    return -1
}

fun BooleanArray.clear(bitIndex: Int) {
    this[bitIndex] = false
}

fun BooleanArray.set(bitIndex: Int) {
    this[bitIndex] = true
}

fun BooleanArray.or(bitSet: BitSet) {
    var i = 0

    while (i < size) {
        this[i] = this[i] or bitSet[i]
        i++
    }
}

fun BooleanArray.toNode(varPrefix: String = "x", varIndexOffset: Int = 0): Node {
    val terms = asSequence()
        .mapIndexed { i, v -> Variable("$varPrefix${i + varIndexOffset}") + Bit(!v) }
        .map { it.simplify() }

    return And(terms).simplify()
}

operator fun Boolean.plus(other: Boolean) = this xor other

operator fun Boolean.times(other: Boolean) = this && other
