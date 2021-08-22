package keccak.util

import keccak.*
import java.util.*

fun Long.getBit(bitIndex: Int): Boolean {
    return ((this shr (Long.SIZE_BITS - bitIndex - 1)) and 1) > 0
}

fun Long.setBit(bitIndex: Int, value: Boolean): Long {
    return if (value) {
        this or 1L.shl(Long.SIZE_BITS - bitIndex - 1)
    } else {
        this and 1L.shl(Long.SIZE_BITS - bitIndex - 1).inv()
    }
}

fun Long.getByte(byteIndex: Int): Byte {
    return (this.shr((Long.SIZE_BYTES - byteIndex - 1) * Byte.SIZE_BITS) and UByte.MAX_VALUE.toLong()).toByte()
}

fun Long.toLittleEndianBytes(): ByteArray {
    val value = this
    val bytes = ByteArray(Long.SIZE_BYTES) { 0 }

    var i = 0
    while (i < Long.SIZE_BYTES) {
        bytes[i] = (value.shr(i * Byte.SIZE_BITS) and UByte.MAX_VALUE.toLong()).toByte()
        i++
    }

    return bytes
}

fun Long.toBitString(): String {
    return String(CharArray(Long.SIZE_BITS) { getBit(it).toNumChar() })
}

fun Long.toBitGroup(): BitGroup {
    val long = this
    val bits = BitGroup(Long.SIZE_BITS)

    var i = 0
    while (i < Long.SIZE_BITS) {
        bits[i] = long.getBit(i)
        i++
    }

    return bits
}

fun Long.toEquationSystem(varsCount: Int): XorEquationSystem {
    val long = this
    val equationSystem = XorEquationSystem(Long.SIZE_BITS, varsCount)

    var i = 0
    while (i < Long.SIZE_BITS) {
        equationSystem.results[i] = long.getBit(i)
        i++
    }

    return equationSystem
}

fun Long.toNodeGroup(): NodeGroup {
    val long = this

    val bits = Array<Node>(Long.SIZE_BITS) { bitIndex ->
        Bit(long.getBit(bitIndex))
    }

    return NodeGroup(bits)
}

fun LongArray.toBitSet(): BitSet {
    val longArray = map { java.lang.Long.reverse(it) }.toLongArray()
    return BitSet.valueOf(longArray)
}
