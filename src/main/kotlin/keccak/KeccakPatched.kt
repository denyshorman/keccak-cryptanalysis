package keccak

import java.util.*

//#region General Utils
fun Array<KeccakPatched.CustomByte>.toByteArray(): ByteArray {
    return map { it.byte }.toByteArray()
}
//#endregion

class KeccakPatched private constructor() {
    //#region Public API
    fun hash(
        message: ByteArray,
        replaceRules: List<BitReplacementSubsystem.ReplaceRule> = emptyList(),
        replaceRulesInverse: Boolean = true,
        replacePadding: Boolean = true,
    ): Result {
        val paddedMessage = message.pad()
        val longBlocks = paddedMessage.blocks().longBlocks()

        val varsCount = BLOCK_SIZE_BITS + longBlocks.size * 38400
        val constraintIndex = longBlocks.size * BLOCK_SIZE_BITS

        val eqSystemBlocks = BitReplacementSubsystem2.getBlocks(
            message,
            replaceRules,
            replaceRulesInverse,
            replacePadding,
            BLOCK_SIZE_BYTES,
            varsCount,
        )

        val blocks = longBlocks.zip(eqSystemBlocks) { a, b -> Block(a, b) }

        val state = State(
            state1 = Array(STATE_SIZE) { EquationSystem(Long.SIZE_BITS, varsCount) },
            constraintIndex = constraintIndex,
            allVarsCount = varsCount,
        )

        val bytes = state.run {
            absorb(blocks)
            squeeze()
        }

        return Result(bytes, state.constraints)
    }
    //#endregion

    //#region Implementation
    private fun ByteArray.pad(): ByteArray {
        val message = this
        val paddingSize = BLOCK_SIZE_BYTES - message.size % BLOCK_SIZE_BYTES
        val padding = mutableListOf<Byte>()

        if (paddingSize == 1) {
            padding.add(0x81.toByte())
        } else if (paddingSize >= 2) {
            padding.add(0x01)

            repeat(paddingSize - 2) {
                padding.add(0x00)
            }

            padding.add(0x80.toByte())
        }

        return message + padding
    }

    private fun ByteArray.blocks(): List<ByteArray> {
        return asSequence()
            .chunked(BLOCK_SIZE_BYTES)
            .map { it.toByteArray() }
            .toList()
    }

    private fun List<ByteArray>.longBlocks(): List<LongArray> {
        return asSequence()
            .map { block ->
                block.asSequence()
                    .chunked(Long.SIZE_BYTES)
                    .map { it.toByteArray() }
                    .map { it.littleEndianToLong() }
                    .toList()
                    .toLongArray()
            }
            .toList()
    }

    private fun State.absorb(blocks: List<Block>) {
        blocks.forEach { block ->
            absorb(block)
        }
    }

    private fun State.absorb(block: Block) {
        ABSORB_CONSTANTS.forEach { x ->
            state0[x[0]] = state0[x[0]] xor block.block0[x[1]]
            state1[x[0]].xor(block.block1[x[1]])
        }

        permutation()
    }

    private fun State.permutation() {
        repeat(ROUND_COUNT) { round ->
            permutation(round)
        }
    }

    private fun State.permutation(round: Int) {
        val c0 = LongArray(LANE_SIZE) { 0 }
        val d0 = LongArray(LANE_SIZE) { 0 }
        val b0 = LongArray(STATE_SIZE) { 0 }

        val c1 = Array(LANE_SIZE) { EquationSystem(Long.SIZE_BITS, allVarsCount) }
        val d1 = Array(LANE_SIZE) { EquationSystem(Long.SIZE_BITS, allVarsCount) }
        val b1 = Array(STATE_SIZE) { EquationSystem(Long.SIZE_BITS, allVarsCount) }

        ROUND_OFFSETS[0].forEach { x ->
            c0[x[0]] = state0[x[1]] xor state0[x[2]] xor state0[x[3]] xor state0[x[4]] xor state0[x[5]]
            c1[x[0]].xor(state1[x[1]], state1[x[2]], state1[x[3]], state1[x[4]], state1[x[5]])
        }

        ROUND_OFFSETS[1].forEach { x ->
            d0[x[0]] = c0[x[1]] xor c0[x[2]].rotateLeft(1)

            d1[x[0]].xor(c1[x[2]])
            d1[x[0]].rotateEquationsLeft(1)
            d1[x[0]].xor(c1[x[1]])
        }

        ROUND_OFFSETS[2].forEach { x ->
            state0[x[0]] = state0[x[0]] xor d0[x[1]]
            state1[x[0]].xor(d1[x[1]])
        }

        ROUND_OFFSETS[3].forEach { x ->
            b0[x[0]] = state0[x[1]].rotateLeft(x[2])

            b1[x[0]].xor(state1[x[1]])
            b1[x[0]].rotateEquationsLeft(x[2])
        }

        ROUND_OFFSETS[4].forEach { x ->
            state0[x[0]] = b0[x[0]] xor b0[x[1]] xor (b0[x[1]] and b0[x[2]])

            val eqSystem = EquationSystem(Long.SIZE_BITS, allVarsCount)
            eqSystem.xor(
                b1[x[0]],
                b1[x[1]],
                recordConstraint(b1[x[1]].clone(), b1[x[2]].clone(), (b0[x[1]] and b0[x[2]]).toFixedBitSet())
            )
            state1[x[0]] = eqSystem
        }

        state0[0] = state0[0] xor ROUND_CONSTANTS[round]
        state1[0].results.xor(ROUND_CONSTANTS_FIXED_BIT_SET[round])
    }

    private fun State.squeeze(): Array<CustomByte> {
        val hashEqSystem = arrayOf(
            *state1[0].toLittleEndianBytes(),
            *state1[5].toLittleEndianBytes(),
            *state1[10].toLittleEndianBytes(),
            *state1[15].toLittleEndianBytes(),
        )

        val hashBytes = byteArrayOf(
            *state0[0].toLittleEndianBytes(),
            *state0[5].toLittleEndianBytes(),
            *state0[10].toLittleEndianBytes(),
            *state0[15].toLittleEndianBytes(),
        )

        return Array(OUTPUT_SIZE_BYTES) { i ->
            CustomByte(hashBytes[i], hashEqSystem[i])
        }
    }

    private fun State.recordConstraint(
        leftSystem: EquationSystem,
        rightSystem: EquationSystem,
        result: FixedBitSet,
    ): EquationSystem {
        val system = EquationSystem(Long.SIZE_BITS, allVarsCount)

        var i = 0
        while (i < Long.SIZE_BITS) {
            system.equations[i].set(constraintIndex)
            constraintIndex++
            i++
        }

        constraints.add(Constraint(leftSystem, rightSystem, system, result))

        return system
    }
    //#endregion

    //#region Private Models
    private class State(
        val state0: LongArray = LongArray(STATE_SIZE) { 0 },
        val state1: Array<EquationSystem>,
        val constraints: LinkedList<Constraint> = LinkedList<Constraint>(),
        var constraintIndex: Int,
        val allVarsCount: Int,
    )

    private class Block(
        val block0: LongArray,
        val block1: Array<EquationSystem>,
    )
    //#endregion

    //#region Public Models
    data class CustomByte(
        val byte: Byte,
        val eqSystem: EquationSystem,
    ) {
        override fun toString(): String {
            return "$byte = $eqSystem"
        }
    }

    data class Result(
        val bytes: Array<CustomByte>,
        val constraints: List<Constraint>,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Result

            if (!bytes.contentEquals(other.bytes)) return false
            if (constraints != other.constraints) return false

            return true
        }

        override fun hashCode(): Int {
            var result = bytes.contentHashCode()
            result = 31 * result + constraints.hashCode()
            return result
        }

        override fun toString(): String {
            return "Result(bytes=${bytes.contentToString()}, constraints=$constraints)"
        }
    }

    data class Constraint(
        val leftSystem: EquationSystem,
        val rightSystem: EquationSystem,
        val resultSystem: EquationSystem,
        val result: FixedBitSet,
    )
    //#endregion

    companion object {
        //#region Private Constants
        private const val ROUND_COUNT = 24
        private const val LANE_SIZE = 5
        private const val STATE_SIZE = LANE_SIZE * LANE_SIZE
        private const val BLOCK_SIZE_BYTES = 136
        private const val BLOCK_SIZE_BITS = BLOCK_SIZE_BYTES * Byte.SIZE_BITS
        private const val OUTPUT_SIZE_BYTES = 32

        private val ROUND_CONSTANTS = longArrayOf(
            0x0000000000000001uL.toLong(),
            0x0000000000008082uL.toLong(),
            0x800000000000808auL.toLong(),
            0x8000000080008000uL.toLong(),
            0x000000000000808buL.toLong(),
            0x0000000080000001uL.toLong(),
            0x8000000080008081uL.toLong(),
            0x8000000000008009uL.toLong(),
            0x000000000000008auL.toLong(),
            0x0000000000000088uL.toLong(),
            0x0000000080008009uL.toLong(),
            0x000000008000000auL.toLong(),
            0x000000008000808buL.toLong(),
            0x800000000000008buL.toLong(),
            0x8000000000008089uL.toLong(),
            0x8000000000008003uL.toLong(),
            0x8000000000008002uL.toLong(),
            0x8000000000000080uL.toLong(),
            0x000000000000800auL.toLong(),
            0x800000008000000auL.toLong(),
            0x8000000080008081uL.toLong(),
            0x8000000000008080uL.toLong(),
            0x0000000080000001uL.toLong(),
            0x8000000080008008uL.toLong(),
        )

        private val ROUND_CONSTANTS_FIXED_BIT_SET = arrayOf(
            0x0000000000000001uL.toLong().toFixedBitSet(),
            0x0000000000008082uL.toLong().toFixedBitSet(),
            0x800000000000808auL.toLong().toFixedBitSet(),
            0x8000000080008000uL.toLong().toFixedBitSet(),
            0x000000000000808buL.toLong().toFixedBitSet(),
            0x0000000080000001uL.toLong().toFixedBitSet(),
            0x8000000080008081uL.toLong().toFixedBitSet(),
            0x8000000000008009uL.toLong().toFixedBitSet(),
            0x000000000000008auL.toLong().toFixedBitSet(),
            0x0000000000000088uL.toLong().toFixedBitSet(),
            0x0000000080008009uL.toLong().toFixedBitSet(),
            0x000000008000000auL.toLong().toFixedBitSet(),
            0x000000008000808buL.toLong().toFixedBitSet(),
            0x800000000000008buL.toLong().toFixedBitSet(),
            0x8000000000008089uL.toLong().toFixedBitSet(),
            0x8000000000008003uL.toLong().toFixedBitSet(),
            0x8000000000008002uL.toLong().toFixedBitSet(),
            0x8000000000000080uL.toLong().toFixedBitSet(),
            0x000000000000800auL.toLong().toFixedBitSet(),
            0x800000008000000auL.toLong().toFixedBitSet(),
            0x8000000080008081uL.toLong().toFixedBitSet(),
            0x8000000000008080uL.toLong().toFixedBitSet(),
            0x0000000080000001uL.toLong().toFixedBitSet(),
            0x8000000080008008uL.toLong().toFixedBitSet(),
        )

        private val ABSORB_CONSTANTS = arrayOf(
            intArrayOf(0, 0),
            intArrayOf(1, 5),
            intArrayOf(2, 10),
            intArrayOf(3, 15),
            intArrayOf(5, 1),
            intArrayOf(6, 6),
            intArrayOf(7, 11),
            intArrayOf(8, 16),
            intArrayOf(10, 2),
            intArrayOf(11, 7),
            intArrayOf(12, 12),
            intArrayOf(15, 3),
            intArrayOf(16, 8),
            intArrayOf(17, 13),
            intArrayOf(20, 4),
            intArrayOf(21, 9),
            intArrayOf(22, 14),
        )

        private val ROUND_OFFSETS = arrayOf(
            arrayOf(
                intArrayOf(0, 0, 1, 2, 3, 4),
                intArrayOf(1, 5, 6, 7, 8, 9),
                intArrayOf(2, 10, 11, 12, 13, 14),
                intArrayOf(3, 15, 16, 17, 18, 19),
                intArrayOf(4, 20, 21, 22, 23, 24),
            ),
            arrayOf(
                intArrayOf(0, 4, 1),
                intArrayOf(1, 0, 2),
                intArrayOf(2, 1, 3),
                intArrayOf(3, 2, 4),
                intArrayOf(4, 3, 0),
            ),
            arrayOf(
                intArrayOf(0, 0),
                intArrayOf(1, 0),
                intArrayOf(2, 0),
                intArrayOf(3, 0),
                intArrayOf(4, 0),
                intArrayOf(5, 1),
                intArrayOf(6, 1),
                intArrayOf(7, 1),
                intArrayOf(8, 1),
                intArrayOf(9, 1),
                intArrayOf(10, 2),
                intArrayOf(11, 2),
                intArrayOf(12, 2),
                intArrayOf(13, 2),
                intArrayOf(14, 2),
                intArrayOf(15, 3),
                intArrayOf(16, 3),
                intArrayOf(17, 3),
                intArrayOf(18, 3),
                intArrayOf(19, 3),
                intArrayOf(20, 4),
                intArrayOf(21, 4),
                intArrayOf(22, 4),
                intArrayOf(23, 4),
                intArrayOf(24, 4),
            ),
            arrayOf(
                intArrayOf(0, 0, 0),
                intArrayOf(1, 15, 28),
                intArrayOf(2, 5, 1),
                intArrayOf(3, 20, 27),
                intArrayOf(4, 10, 62),
                intArrayOf(5, 6, 44),
                intArrayOf(6, 21, 20),
                intArrayOf(7, 11, 6),
                intArrayOf(8, 1, 36),
                intArrayOf(9, 16, 55),
                intArrayOf(10, 12, 43),
                intArrayOf(11, 2, 3),
                intArrayOf(12, 17, 25),
                intArrayOf(13, 7, 10),
                intArrayOf(14, 22, 39),
                intArrayOf(15, 18, 21),
                intArrayOf(16, 8, 45),
                intArrayOf(17, 23, 8),
                intArrayOf(18, 13, 15),
                intArrayOf(19, 3, 41),
                intArrayOf(20, 24, 14),
                intArrayOf(21, 14, 61),
                intArrayOf(22, 4, 18),
                intArrayOf(23, 19, 56),
                intArrayOf(24, 9, 2),
            ),
            arrayOf(
                intArrayOf(0, 10, 5),
                intArrayOf(1, 11, 6),
                intArrayOf(2, 12, 7),
                intArrayOf(3, 13, 8),
                intArrayOf(4, 14, 9),
                intArrayOf(5, 15, 10),
                intArrayOf(6, 16, 11),
                intArrayOf(7, 17, 12),
                intArrayOf(8, 18, 13),
                intArrayOf(9, 19, 14),
                intArrayOf(10, 20, 15),
                intArrayOf(11, 21, 16),
                intArrayOf(12, 22, 17),
                intArrayOf(13, 23, 18),
                intArrayOf(14, 24, 19),
                intArrayOf(15, 0, 20),
                intArrayOf(16, 1, 21),
                intArrayOf(17, 2, 22),
                intArrayOf(18, 3, 23),
                intArrayOf(19, 4, 24),
                intArrayOf(20, 5, 0),
                intArrayOf(21, 6, 1),
                intArrayOf(22, 7, 2),
                intArrayOf(23, 8, 3),
                intArrayOf(24, 9, 4),
            ),
        )
        //#endregion

        //#region Keccak Implementations
        val KECCAK_256 = KeccakPatched()
        //#endregion
    }
}
