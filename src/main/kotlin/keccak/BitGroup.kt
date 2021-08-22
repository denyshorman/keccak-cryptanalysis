package keccak

import keccak.util.toNumChar
import java.util.*

class BitGroup {
    val size: Int
    val bitSet: BitSet

    constructor(size: Int) {
        this.size = size
        this.bitSet = BitSet(size)
    }

    constructor(size: Int, bitSet: BitSet) {
        this.size = size
        this.bitSet = bitSet
    }

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

    fun clear(fromIndex: Int, toIndex: Int) {
        bitSet.clear(fromIndex, toIndex)
    }

    fun xor(set: BitGroup) {
        bitSet.xor(set.bitSet)
    }

    fun xor(set: BitSet) {
        bitSet.xor(set)
    }

    fun xor(bitIndex: Int, value: Boolean) {
        bitSet[bitIndex] = bitSet[bitIndex] xor value
    }

    fun or(set: BitGroup) {
        bitSet.or(set.bitSet)
    }

    fun and(set: BitGroup) {
        bitSet.and(set.bitSet)
    }

    fun invert() {
        bitSet.flip(0, size)
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

    fun clone(): BitGroup {
        return BitGroup(size, bitSet.clone() as BitSet)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BitGroup

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
