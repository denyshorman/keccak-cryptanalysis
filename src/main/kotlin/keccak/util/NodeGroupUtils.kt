package keccak.util

import keccak.*
import java.util.*
import kotlin.experimental.or

infix fun NodeGroup.xor(other: NodeGroup): NodeGroup {
    require(bits.size == other.bits.size)
    val bits = bits.zip(other.bits) { l, r -> Xor(l, r) }
    return NodeGroup(bits.toTypedArray())
}

infix fun NodeGroup.and(other: NodeGroup): NodeGroup {
    require(bits.size == other.bits.size)
    val bits = bits.zip(other.bits) { l, r -> And(l, r) }
    return NodeGroup(bits.toTypedArray())
}

fun NodeGroup.rotateLeft(bitCount: Int): NodeGroup {
    return NodeGroup((bits.drop(bitCount) + bits.take(bitCount)).toTypedArray())
}

fun NodeGroup.toLong(context: NodeContext): Long {
    require(bits.size == Long.SIZE_BITS)

    var value = 0L

    (Long.SIZE_BITS - 1 downTo 0).forEach { i ->
        if (bits[i].evaluate(context).value) {
            value = value or (1L shl Long.SIZE_BITS - i - 1)
        }
    }

    return value
}

fun NodeGroup.toByte(context: NodeContext): Byte {
    require(bits.size == Byte.SIZE_BITS)

    var value: Byte = 0

    (Byte.SIZE_BITS - 1 downTo 0).forEach { i ->
        if (bits[i].evaluate(context).value) {
            value = value or (1.shl(Byte.SIZE_BITS - i - 1)).toByte()
        }
    }

    return value
}

fun NodeGroup.toLittleEndianBytes(): Array<NodeGroup> {
    return bits.asSequence()
        .chunked(Byte.SIZE_BITS)
        .map { NodeGroup(it.toTypedArray()) }
        .toList()
        .reversed()
        .toTypedArray()
}

fun Array<NodeGroup>.littleEndianBytesToLong(): NodeGroup {
    val bytes = this

    val arr = Array(Long.SIZE_BITS) { bitIndex ->
        val byteIndex = Long.SIZE_BYTES - bitIndex / Byte.SIZE_BITS - 1
        bytes[byteIndex].bits[bitIndex % Byte.SIZE_BITS]
    }

    return NodeGroup(arr)
}

fun Array<NodeGroup>.toArrayBitSet(): Array<BitSet> {
    val bitGroups = this

    return bitGroups.flatMap { bitGroup ->
        bitGroup.bits.map { xor ->
            require(xor is Xor)

            val bitSet = BitSet(bitGroups.size * Long.SIZE_BITS)

            xor.nodes.forEach { variable ->
                require(variable is Variable)

                val pos = variable.name.toInt()
                bitSet[pos] = true
            }

            bitSet
        }
    }.toTypedArray()
}
