package keccak.util

import keccak.*
import kotlin.math.min

fun Xor.toBitEquation(varsCount: Int): XorEquation {
    val eq = XorEquation(varsCount)

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

fun List<Pair<XorEquation, XorEquation>>.substitute(eqSystem: EquationSystem) {
    forEach {
        it.first.substitute(eqSystem)
        it.second.substitute(eqSystem)
    }
}

fun XorEquation.substitute(eqSystem: EquationSystem) {
    var i = 0
    val size = min(eqSystem.rows, eqSystem.cols)

    while (i < size) {
        if (eqSystem.equations[i][i] && bitGroup[i]) {
            bitGroup.xor(eqSystem.equations[i])
            result = result xor eqSystem.results[i]
        }

        i++
    }
}

fun List<Pair<XorEquation, XorEquation>>.allSetBits(varsCount: Int): BitGroup {
    val mask = BitGroup(varsCount)

    forEach {
        mask.or(it.first.bitGroup)
        mask.or(it.second.bitGroup)
    }

    return mask
}

fun List<Pair<XorEquation, XorEquation>>.varCountAndOffset(varsCount: Int): Pair<Int, Int> {
    val mask = allSetBits(varsCount)
    val newVarsCount = mask.setBitsCount()
    val offset = mask.nextSetBit(0)
    return Pair(newVarsCount, offset)
}

fun List<Pair<XorEquation, XorEquation>>.additionalEqToBitSystem(varsCount: Int, offset: Int): EquationSystem {
    val varCombinationsCount = (varsCount * (varsCount + 1)) / 2
    val system = EquationSystem(size, varCombinationsCount)

    forEachIndexed { eqIndex, eq ->
        var leftSetBitIndex = eq.first.bitGroup.nextSetBit(0)
        while (leftSetBitIndex >= 0) {
            if (leftSetBitIndex == Integer.MAX_VALUE) break
            var rightSetBitIndex = eq.second.bitGroup.nextSetBit(0)
            while (rightSetBitIndex >= 0) {
                if (rightSetBitIndex == Integer.MAX_VALUE) break
                val varCombinationIndex = calcCombinationIndex(leftSetBitIndex - offset, rightSetBitIndex - offset, varsCount)
                system.equations[eqIndex].xor(varCombinationIndex, true)
                rightSetBitIndex = eq.second.bitGroup.nextSetBit(rightSetBitIndex + 1)
            }
            leftSetBitIndex = eq.first.bitGroup.nextSetBit(leftSetBitIndex + 1)
        }

        if (eq.first.result) {
            var bitIndex = eq.second.bitGroup.nextSetBit(0)
            while (bitIndex >= 0) {
                if (bitIndex == Integer.MAX_VALUE) break
                val varCombinationIndex = calcCombinationIndex(bitIndex - offset, varsCount)
                system.equations[eqIndex].xor(varCombinationIndex, true)
                bitIndex = eq.second.bitGroup.nextSetBit(bitIndex + 1)
            }
        }

        if (eq.second.result) {
            var bitIndex = eq.first.bitGroup.nextSetBit(0)
            while (bitIndex >= 0) {
                if (bitIndex == Integer.MAX_VALUE) break
                val varCombinationIndex = calcCombinationIndex(bitIndex - offset, varsCount)
                system.equations[eqIndex].xor(varCombinationIndex, true)
                bitIndex = eq.first.bitGroup.nextSetBit(bitIndex + 1)
            }
        }

        if (eq.first.result && eq.second.result) {
            system.results.xor(eqIndex, true)
        }

        var bitIndex = eq.second.bitGroup.nextSetBit(0)
        while (bitIndex >= 0) {
            if (bitIndex == Integer.MAX_VALUE) break
            val varCombinationIndex = calcCombinationIndex(bitIndex - offset, varsCount)
            system.equations[eqIndex].xor(varCombinationIndex, true)
            bitIndex = eq.second.bitGroup.nextSetBit(bitIndex + 1)
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

fun toBigGroup(bytes: ByteArray, constraints: List<KeccakPatched.Constraint>): BitGroup {
    val varsCount = constraints[0].leftSystem.cols
    val bitGroup = BitGroup(varsCount)

    var bitGroupVarIndex = 0
    var byteIndex = 0
    while (byteIndex < bytes.size) {
        var bitIndex = 0
        while (bitIndex < Byte.SIZE_BITS) {
            bitGroup[bitGroupVarIndex] = bytes[byteIndex].getBit(bitIndex)
            bitGroupVarIndex++
            bitIndex++
        }
        byteIndex++
    }

    constraints.forEach { constraint ->
        var i = 0
        while (i < constraint.varSystem.rows) {
            val varIndex = constraint.varSystem.equations[i].nextSetBit(0)
            bitGroup[varIndex] = constraint.result.getBit(i)
            i++
        }
    }

    return bitGroup
}
