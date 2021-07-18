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
    val bits = Array<Node>(Long.SIZE_BITS) { Bit() }
    (0 until Long.SIZE_BITS).forEach { i ->
        val bit = (this shr i) and 1
        bits[Long.SIZE_BITS - i - 1] = Bit(bit != 0L)
    }
    return BitGroup(bits)
}
//#endregion
