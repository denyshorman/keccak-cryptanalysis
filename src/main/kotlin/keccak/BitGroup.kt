package keccak

import kotlin.experimental.or

class BitGroup(val bits: Array<Node>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BitGroup

        if (!bits.contentEquals(other.bits)) return false

        return true
    }

    override fun hashCode(): Int {
        return bits.contentHashCode()
    }

    override fun toString(): String {
        return bits.asSequence().map { "[$it]" }.joinToString("")
    }
}

//#region Extensions
infix fun BitGroup.xor(other: BitGroup): BitGroup {
    require(bits.size == other.bits.size)
    val bits = bits.zip(other.bits) { l, r -> Xor(l, r) }
    return BitGroup(bits.toTypedArray())
}

infix fun BitGroup.and(other: BitGroup): BitGroup {
    require(bits.size == other.bits.size)
    val bits = bits.zip(other.bits) { l, r -> And(l, r) }
    return BitGroup(bits.toTypedArray())
}

fun BitGroup.rotateLeft(bitCount: Int): BitGroup {
    return BitGroup((bits.drop(bitCount) + bits.take(bitCount)).toTypedArray())
}

fun BitGroup.toLong(context: NodeContext): Long {
    require(bits.size == Long.SIZE_BITS)

    var value = 0L

    (Long.SIZE_BITS - 1 downTo 0).forEach { i ->
        if (bits[i].evaluate(context).value) {
            value = value or (1L shl Long.SIZE_BITS - i - 1)
        }
    }

    return value
}

fun BitGroup.toByte(context: NodeContext): Byte {
    require(bits.size == Byte.SIZE_BITS)

    var value: Byte = 0

    (Byte.SIZE_BITS - 1 downTo 0).forEach { i ->
        if (bits[i].evaluate(context).value) {
            value = value or (1.shl(Byte.SIZE_BITS - i - 1)).toByte()
        }
    }

    return value
}

fun Long.toBitGroup(): BitGroup {
    val long = this

    val bits = Array<Node>(Long.SIZE_BITS) { bitIndex ->
        val bit = (long shr (Long.SIZE_BITS - bitIndex - 1)) and 1
        Bit(bit > 0)
    }

    return BitGroup(bits)
}

fun Byte.toBitGroup(): BitGroup {
    val byte = this

    val bits = Array<Node>(Byte.SIZE_BITS) { bitIndex ->
        val bit = (byte.toInt() shr (Byte.SIZE_BITS - bitIndex - 1)) and 1
        Bit(bit > 0)
    }

    return BitGroup(bits)
}

fun ByteArray.toBitGroup(): Array<BitGroup> {
    return Array(size) { get(it).toBitGroup() }
}

fun BitGroup.toLittleEndianBytes(): Array<BitGroup> {
    return bits.asSequence()
        .chunked(Byte.SIZE_BITS)
        .map { BitGroup(it.toTypedArray()) }
        .toList()
        .reversed()
        .toTypedArray()
}

fun Array<BitGroup>.littleEndianBytesToLong(): BitGroup {
    val bytes = this

    val arr = Array(Long.SIZE_BITS) { bitIndex ->
        val byteIndex = Long.SIZE_BYTES - bitIndex / Byte.SIZE_BITS - 1
        bytes[byteIndex].bits[bitIndex % Byte.SIZE_BITS]
    }

    return BitGroup(arr)
}
//#endregion
