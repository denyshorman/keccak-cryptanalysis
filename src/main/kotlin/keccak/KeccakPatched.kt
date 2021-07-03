package keccak

class KeccakPatched private constructor() {
    //#region Public API
    fun hash(message: ByteArray): ByteArray {
        val state = LongArray(STATE_SIZE) { 0 }
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

    private fun LongArray.absorb(blocks: List<LongArray>) {
        blocks.forEach { block ->
            absorb(block)
        }
    }

    private fun LongArray.absorb(block: LongArray) {
        val state = this

        state[0] = state[0] xor block[0]
        state[1] = state[1] xor block[5]
        state[2] = state[2] xor block[10]
        state[3] = state[3] xor block[15]
        state[5] = state[5] xor block[1]
        state[6] = state[6] xor block[6]
        state[7] = state[7] xor block[11]
        state[8] = state[8] xor block[16]
        state[10] = state[10] xor block[2]
        state[11] = state[11] xor block[7]
        state[12] = state[12] xor block[12]
        state[15] = state[15] xor block[3]
        state[16] = state[16] xor block[8]
        state[17] = state[17] xor block[13]
        state[20] = state[20] xor block[4]
        state[21] = state[21] xor block[9]
        state[22] = state[22] xor block[14]

        permutation()
    }

    private fun LongArray.permutation() {
        repeat(24) { round ->
            permutation(round)
        }
    }

    private fun LongArray.permutation(round: Int) {
        //#region Variables
        val a = this
        val b = LongArray(STATE_SIZE) { 0 }
        val c = LongArray(LANE_SIZE) { 0 }
        val d = LongArray(LANE_SIZE) { 0 }
        //#endregion

        //#region θ step
        c[0] = a[0] xor a[1] xor a[2] xor a[3] xor a[4]
        c[1] = a[5] xor a[6] xor a[7] xor a[8] xor a[9]
        c[2] = a[10] xor a[11] xor a[12] xor a[13] xor a[14]
        c[3] = a[15] xor a[16] xor a[17] xor a[18] xor a[19]
        c[4] = a[20] xor a[21] xor a[22] xor a[23] xor a[24]

        d[0] = c[4] xor c[1].rotateLeft(1)
        d[1] = c[0] xor c[2].rotateLeft(1)
        d[2] = c[1] xor c[3].rotateLeft(1)
        d[3] = c[2] xor c[4].rotateLeft(1)
        d[4] = c[3] xor c[0].rotateLeft(1)

        a[0] = a[0] xor d[0]
        a[1] = a[1] xor d[0]
        a[2] = a[2] xor d[0]
        a[3] = a[3] xor d[0]
        a[4] = a[4] xor d[0]
        a[5] = a[5] xor d[1]
        a[6] = a[6] xor d[1]
        a[7] = a[7] xor d[1]
        a[8] = a[8] xor d[1]
        a[9] = a[9] xor d[1]
        a[10] = a[10] xor d[2]
        a[11] = a[11] xor d[2]
        a[12] = a[12] xor d[2]
        a[13] = a[13] xor d[2]
        a[14] = a[14] xor d[2]
        a[15] = a[15] xor d[3]
        a[16] = a[16] xor d[3]
        a[17] = a[17] xor d[3]
        a[18] = a[18] xor d[3]
        a[19] = a[19] xor d[3]
        a[20] = a[20] xor d[4]
        a[21] = a[21] xor d[4]
        a[22] = a[22] xor d[4]
        a[23] = a[23] xor d[4]
        a[24] = a[24] xor d[4]
        //#endregion

        //#region ρ and π steps
        b[0] = a[0].rotateLeft(0)
        b[1] = a[15].rotateLeft(28)
        b[2] = a[5].rotateLeft(1)
        b[3] = a[20].rotateLeft(27)
        b[4] = a[10].rotateLeft(62)
        b[5] = a[6].rotateLeft(44)
        b[6] = a[21].rotateLeft(20)
        b[7] = a[11].rotateLeft(6)
        b[8] = a[1].rotateLeft(36)
        b[9] = a[16].rotateLeft(55)
        b[10] = a[12].rotateLeft(43)
        b[11] = a[2].rotateLeft(3)
        b[12] = a[17].rotateLeft(25)
        b[13] = a[7].rotateLeft(10)
        b[14] = a[22].rotateLeft(39)
        b[15] = a[18].rotateLeft(21)
        b[16] = a[8].rotateLeft(45)
        b[17] = a[23].rotateLeft(8)
        b[18] = a[13].rotateLeft(15)
        b[19] = a[3].rotateLeft(41)
        b[20] = a[24].rotateLeft(14)
        b[21] = a[14].rotateLeft(61)
        b[22] = a[4].rotateLeft(18)
        b[23] = a[19].rotateLeft(56)
        b[24] = a[9].rotateLeft(2)
        //#endregion

        //#region χ step
        a[0] = b[0] xor (b[5].inv() and b[10])
        a[1] = b[1] xor (b[6].inv() and b[11])
        a[2] = b[2] xor (b[7].inv() and b[12])
        a[3] = b[3] xor (b[8].inv() and b[13])
        a[4] = b[4] xor (b[9].inv() and b[14])
        a[5] = b[5] xor (b[10].inv() and b[15])
        a[6] = b[6] xor (b[11].inv() and b[16])
        a[7] = b[7] xor (b[12].inv() and b[17])
        a[8] = b[8] xor (b[13].inv() and b[18])
        a[9] = b[9] xor (b[14].inv() and b[19])
        a[10] = b[10] xor (b[15].inv() and b[20])
        a[11] = b[11] xor (b[16].inv() and b[21])
        a[12] = b[12] xor (b[17].inv() and b[22])
        a[13] = b[13] xor (b[18].inv() and b[23])
        a[14] = b[14] xor (b[19].inv() and b[24])
        a[15] = b[15] xor (b[20].inv() and b[0])
        a[16] = b[16] xor (b[21].inv() and b[1])
        a[17] = b[17] xor (b[22].inv() and b[2])
        a[18] = b[18] xor (b[23].inv() and b[3])
        a[19] = b[19] xor (b[24].inv() and b[4])
        a[20] = b[20] xor (b[0].inv() and b[5])
        a[21] = b[21] xor (b[1].inv() and b[6])
        a[22] = b[22] xor (b[2].inv() and b[7])
        a[23] = b[23] xor (b[3].inv() and b[8])
        a[24] = b[24] xor (b[4].inv() and b[9])
        //#endregion

        //#region ι step
        a[0] = a[0] xor ROUND_CONSTANTS[round]
        //#endregion
    }

    private fun LongArray.squeeze(): ByteArray {
        val state = this

        val outputBytesStream = sequence {
            while (true) {
                state[0].toLittleEndianBytes().forEach { yield(it) }
                state[5].toLittleEndianBytes().forEach { yield(it) }
                state[10].toLittleEndianBytes().forEach { yield(it) }
                state[15].toLittleEndianBytes().forEach { yield(it) }

                permutation()
            }
        }

        return outputBytesStream.take(OUTPUT_SIZE_BYTES).toList().toByteArray()
    }

    private fun ByteArray.littleEndianToLong(): Long {
        val bytes = this
        var value = 0L

        var i = 0
        while (i < bytes.size) {
            value = value or bytes[i].toLong().shl(i * Byte.SIZE_BITS)
            i++
        }

        return value
    }

    private fun Long.toLittleEndianBytes(): ByteArray {
        val value = this
        val bytes = ByteArray(Long.SIZE_BYTES) { 0 }

        var i = 0
        while (i < Long.SIZE_BYTES) {
            bytes[i] = (value.shr(i * Byte.SIZE_BITS) and UByte.MAX_VALUE.toLong()).toByte()
            i++
        }

        return bytes
    }
    //#endregion

    companion object {
        //#region Private Constants
        private const val LANE_SIZE = 5
        private const val STATE_SIZE = LANE_SIZE * LANE_SIZE
        private const val BLOCK_SIZE_BYTES = 136
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
        //#endregion

        //#region Keccak Implementations
        val KECCAK_256 = KeccakPatched()
        //#endregion
    }
}
