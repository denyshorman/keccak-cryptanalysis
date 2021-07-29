package keccak.util

import keccak.FixedBitSet

fun Long.bit(bitIndex: Int): Boolean {
    return ((this shr (Long.SIZE_BITS - bitIndex - 1)) and 1) > 0
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
    return String(CharArray(Long.SIZE_BITS) { bit(it).toNumChar() })
}

fun Long.toFixedBitSet(): FixedBitSet {
    val long = this
    val bits = FixedBitSet(Long.SIZE_BITS)

    var i = 0
    while (i < Long.SIZE_BITS) {
        bits[i] = long.bit(i)
        i++
    }

    return bits
}
