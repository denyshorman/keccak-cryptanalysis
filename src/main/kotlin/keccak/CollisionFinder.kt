package keccak

import java.util.*

fun List<KeccakPatched.AndEqInfo>.filterTrueEquations(): Array<Node> {
    return asSequence()
        .filter { it.evaluatedValue.value }
        .flatMap { sequenceOf(it.leftNode, it.rightNode) }
        .toList()
        .toTypedArray()
}

fun Array<Node>.trueEquationsToXorEquations(): Pair<Array<BitSet>, BitSet> {
    val size = this.size
    val equations = Array(size) { BitSet(size) }
    val results = BitSet(size)
    results.set(0, size)

    this.forEachIndexed { i, xorNode ->
        xorNode as Xor

        xorNode.nodes.forEach { node ->
            when (node) {
                is Variable -> {
                    val varPos = node.name.mapToOnlyDigits().toInt()
                    equations[i].set(varPos)
                }
                is Bit -> {
                    results[i] = results[i] xor node.value
                }
                else -> throw IllegalStateException()
            }
        }
    }

    return Pair(equations, results)
}

fun Array<KeccakPatched.CustomByte>.toXorEquations(varCount: Int): Pair<Array<BitSet>, BitSet> {
    val equations = Array(varCount) { BitSet(varCount) }
    val results = BitSet(varCount)

    forEachIndexed { byteIndex, output ->
        val byteBitSet = output.byte.toBitSet()

        output.bitGroup.bits.forEachIndexed { eqIndex, eq ->
            val generalEqIndex = byteIndex * Byte.SIZE_BITS + eqIndex

            require(eq is Xor)

            eq.nodes.forEach { xorNode ->
                when (xorNode) {
                    is Variable -> {
                        val varPos = xorNode.name.mapToOnlyDigits().toInt()
                        equations[generalEqIndex].set(varPos)
                    }
                    is Bit -> {
                        results[generalEqIndex] = results[generalEqIndex] xor xorNode.value
                    }
                    else -> throw IllegalStateException()
                }
            }

            results[generalEqIndex] = results[generalEqIndex] xor byteBitSet[eqIndex]
        }
    }

    return Pair(equations, results)
}