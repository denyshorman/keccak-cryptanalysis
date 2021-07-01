package keccak

import kotlin.math.log2

class Keccak private constructor(
    private val rate: Int,
    capacity: Int,
    outputSize: Int,
) {
    //#region Private Fields
    private val permutationWidth = rate + capacity
    private val laneLength = permutationWidth / 25
    private val permutationRoundCount = 12 + 2 * log2(laneLength.toDouble()).toInt()
    private val blockSizeBytes = rate / Byte.SIZE_BITS
    private val outputSizeBytes = outputSize / Byte.SIZE_BITS
    //#endregion

    //#region Public API
    fun hash(message: ByteArray): ByteArray {
        val state = Array(size = 5) { ULongArray(size = 5) { 0uL } }
        val blocks = message.pad().blocks().uLongBlocks()
        absorb(state, blocks)
        return squeeze(state)
    }
    //#endregion

    //#region Utils
    private fun ByteArray.pad(): ByteArray {
        val message = this
        val paddingSize = blockSizeBytes - message.size % blockSizeBytes
        val padding = mutableListOf<Byte>()

        if (paddingSize == 1) {
            padding.add(0x81.toByte())
        } else if (paddingSize >= 2) {
            padding.add(0x01)

            for (i in 0 until (paddingSize - 2)) {
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

    private fun List<ByteArray>.uLongBlocks(): List<ULongArray> {
        return asSequence()
            .map { block ->
                block.asSequence()
                    .chunked(ULong.SIZE_BYTES)
                    .map { it.toByteArray() }
                    .map { it.littleEndianToULong() }
                    .toList()
                    .toULongArray()
            }
            .toList()
    }

    private fun absorb(state: Array<ULongArray>, blocks: List<ULongArray>) {
        blocks.forEach { block ->
            (0..4).forEach { x ->
                (0..4).forEach { y ->
                    if (x + 5 * y < rate / laneLength) {
                        state[x][y] = state[x][y] xor block[x + 5 * y]
                    }
                }
            }

            permutation(state)
        }
    }

    private fun permutation(state: Array<ULongArray>) {
        (0 until permutationRoundCount).forEach { round ->
            permutation(state, ROUND_CONSTANTS[round])
        }
    }

    private fun permutation(state: Array<ULongArray>, roundConstant: ULong) {
        //#region Variables
        val c = ULongArray(5) { 0uL }
        val d = ULongArray(5) { 0uL }
        val b = Array(size = 5) { ULongArray(size = 5) { 0uL } }
        //#endregion

        //#region θ step
        (0..4).forEach { x ->
            c[x] = state[x][0] xor state[x][1] xor state[x][2] xor state[x][3] xor state[x][4]
        }

        (0..4).forEach { x ->
            d[x] = c[(x + 4) % 5] xor c[(x + 1) % 5].rotateLeft(1)
        }

        (0..4).forEach { x ->
            (0..4).forEach { y ->
                state[x][y] = state[x][y] xor d[x]
            }
        }
        //#endregion

        //#region ρ and π steps
        (0..4).forEach { x ->
            (0..4).forEach { y ->
                b[y][(2 * x + 3 * y) % 5] = state[x][y].rotateLeft(ROTATION_OFFSETS[x][y])
            }
        }
        //#endregion

        //#region χ step
        (0..4).forEach { x ->
            (0..4).forEach { y ->
                state[x][y] = b[x][y] xor (b[(x + 1) % 5][y].inv() and b[(x + 2) % 5][y])
            }
        }
        //#endregion

        //#region ι step
        state[0][0] = state[0][0] xor roundConstant
        //#endregion
    }

    private fun squeeze(state: Array<ULongArray>): ByteArray {
        val outputBytesStream = sequence {
            while (true) {
                (0..4).forEach { x ->
                    (0..4).forEach { y ->
                        if (5 * x + y < rate / laneLength) {
                            state[y][x].toLittleEndianBytes().forEach { yield(it) }
                        }
                    }
                }

                permutation(state)
            }
        }

        return outputBytesStream.take(outputSizeBytes).toList().toByteArray()
    }

    private fun ByteArray.littleEndianToULong(): ULong {
        val bytes = this
        var value = 0uL

        var i = 0
        while (i < bytes.size) {
            value = value or bytes[i].toULong().shl(i * UByte.SIZE_BITS)
            i++
        }

        return value
    }

    private fun ULong.toLittleEndianBytes(): ByteArray {
        val value = this
        val bytes = ByteArray(ULong.SIZE_BYTES) { 0 }

        var i = 0
        while (i < ULong.SIZE_BYTES) {
            bytes[i] = (value.shr(i * UByte.SIZE_BITS) and UByte.MAX_VALUE.toULong()).toByte()
            i++
        }

        return bytes
    }
    //#endregion

    companion object {
        //#region Private Constants
        private val ROUND_CONSTANTS = ulongArrayOf(
            0x0000000000000001uL,
            0x0000000000008082uL,
            0x800000000000808auL,
            0x8000000080008000uL,
            0x000000000000808buL,
            0x0000000080000001uL,
            0x8000000080008081uL,
            0x8000000000008009uL,
            0x000000000000008auL,
            0x0000000000000088uL,
            0x0000000080008009uL,
            0x000000008000000auL,
            0x000000008000808buL,
            0x800000000000008buL,
            0x8000000000008089uL,
            0x8000000000008003uL,
            0x8000000000008002uL,
            0x8000000000000080uL,
            0x000000000000800auL,
            0x800000008000000auL,
            0x8000000080008081uL,
            0x8000000000008080uL,
            0x0000000080000001uL,
            0x8000000080008008uL,
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
