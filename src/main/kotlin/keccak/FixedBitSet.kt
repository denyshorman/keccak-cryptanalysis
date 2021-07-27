package keccak

import java.util.*

class FixedBitSet(val size: Int) {
    private val bitSet = BitSet(size)

    operator fun get(bitIndex: Int): Boolean {
        return bitSet.get(bitIndex)
    }

    operator fun set(bitIndex: Int, value: Boolean) {
        return bitSet.set(bitIndex, value)
    }

    fun set(bitIndex: Int) {
        return bitSet.set(bitIndex, true)
    }

    fun clear() {
        bitSet.clear()
    }

    fun clear(bitIndex: Int) {
        bitSet.clear(bitIndex)
    }

    fun xor(set: FixedBitSet) {
        bitSet.xor(set.bitSet)
    }

    fun xor(set: BitSet) {
        bitSet.xor(set)
    }

    fun xor(bitIndex: Int, value: Boolean) {
        bitSet[bitIndex] = bitSet[bitIndex] xor value
    }

    fun or(set: FixedBitSet) {
        bitSet.or(set.bitSet)
    }

    fun and(set: FixedBitSet) {
        bitSet.and(set.bitSet)
    }

    fun nextSetBit(fromIndex: Int): Int {
        return bitSet.nextSetBit(fromIndex)
    }

    fun previousSetBit(fromIndex: Int): Int {
        return bitSet.previousSetBit(fromIndex)
    }

    fun isEmpty(): Boolean {
        return bitSet.isEmpty
    }

    fun setBitsCount(): Int {
        return bitSet.cardinality()
    }

    fun exchange(i: Int, j: Int) {
        val tmp = bitSet[i]
        bitSet[i] = bitSet[j]
        bitSet[j] = tmp
    }

    fun clone(): FixedBitSet {
        return bitSet.clone() as FixedBitSet
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FixedBitSet

        if (size != other.size) return false
        if (bitSet != other.bitSet) return false

        return true
    }

    override fun hashCode(): Int {
        return 31 * size + bitSet.hashCode()
    }

    override fun toString(): String {
        return String(CharArray(size) { bitSet[it].toNumChar() })
    }
}

fun Long.toFixedBitSet(): FixedBitSet {
    val long = this
    val bits = FixedBitSet(Long.SIZE_BITS)

    var i = 0
    while (i < Long.SIZE_BITS) {
        val bit = (long shr (Long.SIZE_BITS - i - 1)) and 1
        bits[i] = bit > 0
        i++
    }

    return bits
}
