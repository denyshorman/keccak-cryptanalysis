package keccak.util

import keccak.Bit
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

operator fun Boolean.plus(other: Boolean) = this xor other

operator fun Boolean.times(other: Boolean) = this && other
