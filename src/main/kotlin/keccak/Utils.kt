package keccak

import java.util.*
import kotlin.math.min

private val NonDigitsRegex = "\\D+".toRegex()

fun String.mapToOnlyDigits(): String {
    return NonDigitsRegex.replace(this, "")
}

fun Boolean.toNumChar(): Char = if (this) '1' else '0'

fun pow(a: Int, b: Int): Long {
    var count = 1L
    var i = 0
    while (i < b) {
        count *= a
        i++
    }
    return count
}

fun ByteArray.littleEndianToLong(): Long {
    val bytes = this
    var value = 0L

    var i = 0
    while (i < bytes.size) {
        value = value or bytes[i].toUByte().toLong().shl(i * Byte.SIZE_BITS)
        i++
    }

    return value
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

fun LongArray.toBitSet(): BitSet {
    val longArray = map { java.lang.Long.reverse(it) }.toLongArray()
    return BitSet.valueOf(longArray)
}

fun List<KeccakPatched.AndEqInfo>.toBitEquations(varsCount: Int): List<Pair<BitEquation, BitEquation>> {
    return map {eq ->
        val l = eq.leftNode.toBitEquation(varsCount)
        val r = eq.rightNode.toBitEquation(varsCount)
        Pair(l, r)
    }
}

fun Xor.toBitEquation(varsCount: Int): BitEquation {
    val eq = BitEquation(varsCount)

    this.nodes.forEach { node ->
        when (node) {
            is Variable -> {
                val varPos = node.name.mapToOnlyDigits().toInt()
                eq.setVariable(varPos)
            }
            is Bit -> {
                eq.setBit(node.value)
            }
            else -> throw IllegalStateException()
        }
    }

    return eq
}

fun List<Pair<BitEquation, BitEquation>>.substitute(eqSystem: EquationSystem) {
    forEach {
        it.first.substitute(eqSystem)
        it.second.substitute(eqSystem)
    }
}

fun BitEquation.substitute(eqSystem: EquationSystem) {
    var i = 0
    val size = min(eqSystem.rows, eqSystem.cols)

    while (i < size) {
        if (eqSystem.equations[i][i] && bitSet[i]) {
            bitSet.xor(eqSystem.equations[i])
            result = result xor eqSystem.results[i]
        }

        i++
    }
}

fun List<Pair<BitEquation, BitEquation>>.allSetBits(varsCount: Int): FixedBitSet {
    val mask = FixedBitSet(varsCount)

    forEach {
        mask.or(it.first.bitSet)
        mask.or(it.second.bitSet)
    }

    return mask
}

fun List<Pair<BitEquation, BitEquation>>.varCountAndOffset(varsCount: Int): Pair<Int, Int> {
    val mask = allSetBits(varsCount)
    val newVarsCount = mask.setBitsCount()
    val offset = mask.nextSetBit(0)
    return Pair(newVarsCount, offset)
}

fun List<Pair<BitEquation, BitEquation>>.additionalEqToBitSystem(varsCount: Int, offset: Int): EquationSystem {
    val varCombinationsCount = (varsCount * (varsCount + 1)) / 2
    val system = EquationSystem(size, varCombinationsCount)

    forEachIndexed { eqIndex, eq ->
        var leftSetBitIndex = eq.first.bitSet.nextSetBit(0)
        while (leftSetBitIndex >= 0) {
            if (leftSetBitIndex == Integer.MAX_VALUE) break
            var rightSetBitIndex = eq.second.bitSet.nextSetBit(0)
            while (rightSetBitIndex >= 0) {
                if (rightSetBitIndex == Integer.MAX_VALUE) break
                val varCombinationIndex = calcCombinationIndex(leftSetBitIndex - offset, rightSetBitIndex - offset, varsCount)
                system.equations[eqIndex].xor(varCombinationIndex, true)
                rightSetBitIndex = eq.second.bitSet.nextSetBit(rightSetBitIndex + 1)
            }
            leftSetBitIndex = eq.first.bitSet.nextSetBit(leftSetBitIndex + 1)
        }

        if (eq.first.result) {
            var bitIndex = eq.second.bitSet.nextSetBit(0)
            while (bitIndex >= 0) {
                if (bitIndex == Integer.MAX_VALUE) break
                val varCombinationIndex = calcCombinationIndex(bitIndex - offset, varsCount)
                system.equations[eqIndex].xor(varCombinationIndex, true)
                bitIndex = eq.second.bitSet.nextSetBit(bitIndex + 1)
            }
        }

        if (eq.second.result) {
            var bitIndex = eq.first.bitSet.nextSetBit(0)
            while (bitIndex >= 0) {
                if (bitIndex == Integer.MAX_VALUE) break
                val varCombinationIndex = calcCombinationIndex(bitIndex - offset, varsCount)
                system.equations[eqIndex].xor(varCombinationIndex, true)
                bitIndex = eq.first.bitSet.nextSetBit(bitIndex + 1)
            }
        }

        if (eq.first.result && eq.second.result) {
            system.results.xor(eqIndex, true)
        }

        var bitIndex = eq.second.bitSet.nextSetBit(0)
        while (bitIndex >= 0) {
            if (bitIndex == Integer.MAX_VALUE) break
            val varCombinationIndex = calcCombinationIndex(bitIndex - offset, varsCount)
            system.equations[eqIndex].xor(varCombinationIndex, true)
            bitIndex = eq.second.bitSet.nextSetBit(bitIndex + 1)
        }

        if (eq.second.result) {
            system.results.xor(eqIndex, true)
        }
    }

    return system
}

fun calcCombinationIndex(i: Int, j: Int, varsCount: Int): Int {
    val left: Int
    val right: Int

    if (i >= j) {
        left = j
        right = i
    } else {
        left = i
        right = j
    }

    return right + (left * (2 * varsCount - left - 1)) / 2
}

fun calcCombinationIndex(i: Int, varsCount: Int): Int {
    return (i * (2 * varsCount - i + 1)) / 2
}
