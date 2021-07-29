package keccak

import keccak.util.littleEndianBytesToLong
import keccak.util.toLittleEndianBytes

class Keccak private constructor(
    private val rate: Int,
    capacity: Int,
    outputSize: Int,
) {
    //#region Private Fields
    private val laneLength = (rate + capacity) / (LANE_SIZE * LANE_SIZE)
    private val permutationRoundCount = 12 + 2 * kotlin.math.log2(laneLength.toDouble()).toInt()
    private val blockSizeBytes = rate / Byte.SIZE_BITS
    private val outputSizeBytes = outputSize / Byte.SIZE_BITS
    //#endregion

    //#region Public API
    fun hash(message: ByteArray): ByteArray {
        val state = Array(LANE_SIZE) { LongArray(LANE_SIZE) { 0 } }
        val blocks = message.pad().blocks().longBlocks()

        return state.run {
            absorb(blocks)
            squeeze()
        }
    }
    //#endregion

    //#region Implementation
    private fun ByteArray.pad(): ByteArray {
        val message = this
        val paddingSize = blockSizeBytes - message.size % blockSizeBytes
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
            .chunked(blockSizeBytes)
            .map { it.toByteArray() }
            .toList()
    }

    private fun List<ByteArray>.longBlocks(): List<LongArray> {
        return asSequence()
            .map { block ->
                block.asSequence()
                    .chunked(Long.SIZE_BYTES)
                    .map { it.toByteArray() }
                    .map { it.littleEndianBytesToLong() }
                    .toList()
                    .toLongArray()
            }
            .toList()
    }

    private fun Array<LongArray>.absorb(blocks: List<LongArray>) {
        blocks.forEach { block ->
            absorb(block)
        }
    }

    private fun Array<LongArray>.absorb(block: LongArray) {
        val state = this

        (0 until LANE_SIZE).forEach { x ->
            (0 until LANE_SIZE).forEach { y ->
                if (x + LANE_SIZE * y < rate / laneLength) {
                    state[x][y] = state[x][y] xor block[x + LANE_SIZE * y]
                }
            }
        }

        permutation()
    }

    private fun Array<LongArray>.permutation() {
        (0 until permutationRoundCount).forEach { round ->
            permutation(round)
        }
    }

    private fun Array<LongArray>.permutation(round: Int) {
        //#region Variables
        val a = this
        val b = Array(LANE_SIZE) { LongArray(LANE_SIZE) { 0 } }
        val c = LongArray(LANE_SIZE) { 0 }
        val d = LongArray(LANE_SIZE) { 0 }
        //#endregion

        //#region θ step
        (0 until LANE_SIZE).forEach { x ->
            c[x] = a[x][0] xor a[x][1] xor a[x][2] xor a[x][3] xor a[x][4]
        }

        (0 until LANE_SIZE).forEach { x ->
            d[x] = c[(x + 4) % LANE_SIZE] xor c[(x + 1) % LANE_SIZE].rotateLeft(1)
        }

        (0 until LANE_SIZE).forEach { x ->
            (0 until LANE_SIZE).forEach { y ->
                a[x][y] = a[x][y] xor d[x]
            }
        }
        //#endregion

        //#region ρ and π steps
        (0 until LANE_SIZE).forEach { x ->
            (0 until LANE_SIZE).forEach { y ->
                b[y][(2 * x + 3 * y) % LANE_SIZE] = a[x][y].rotateLeft(ROTATION_OFFSETS[x][y])
            }
        }
        //#endregion

        //#region χ step
        (0 until LANE_SIZE).forEach { x ->
            (0 until LANE_SIZE).forEach { y ->
                a[x][y] = b[x][y] xor (b[(x + 1) % LANE_SIZE][y].inv() and b[(x + 2) % LANE_SIZE][y])
            }
        }
        //#endregion

        //#region ι step
        a[0][0] = a[0][0] xor ROUND_CONSTANTS[round]
        //#endregion
    }

    private fun Array<LongArray>.squeeze(): ByteArray {
        val state = this

        val outputBytesStream = sequence {
            while (true) {
                (0 until LANE_SIZE).forEach { x ->
                    (0 until LANE_SIZE).forEach { y ->
                        if (LANE_SIZE * x + y < rate / laneLength) {
                            state[y][x].toLittleEndianBytes().forEach { yield(it) }
                        }
                    }
                }

                permutation()
            }
        }

        return outputBytesStream.take(outputSizeBytes).toList().toByteArray()
    }
    //#endregion

    companion object {
        //#region Private Constants
        private const val LANE_SIZE = 5
        
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

        private val ROTATION_OFFSETS = arrayOf(
            intArrayOf(0, 36, 3, 41, 18),
            intArrayOf(1, 44, 10, 45, 2),
            intArrayOf(62, 6, 43, 15, 61),
            intArrayOf(28, 55, 25, 21, 56),
            intArrayOf(27, 20, 39, 8, 14),
        )
        //#endregion

        //#region Keccak Implementations
        val KECCAK_224 = Keccak(rate = 1152, capacity = 448, outputSize = 224)
        val KECCAK_256 = Keccak(rate = 1088, capacity = 512, outputSize = 256)
        val KECCAK_384 = Keccak(rate = 832, capacity = 768, outputSize = 384)
        val KECCAK_512 = Keccak(rate = 576, capacity = 1024, outputSize = 512)
        //#endregion
    }
}
