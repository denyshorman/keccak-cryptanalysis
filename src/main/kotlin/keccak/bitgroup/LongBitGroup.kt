package keccak.bitgroup

class LongBitGroup {
    private val bitsArray: LongArray

    //#region Constructors
    constructor(bitsCount: Int) {
        bitsArray = LongArray(bitsCount.toLongArraySize())
    }

    constructor(bitsArray: LongArray) {
        this.bitsArray = bitsArray
    }
    //#endregion

    //#region Logical operations
    fun xor(other: LongBitGroup) {
        var i = 0
        while (i < bitsArray.size) {
            bitsArray[i] = bitsArray[i] xor other.bitsArray[i]
            i++
        }
    }

    fun xor(bitIndex: Int, value: Boolean) {
        if (!value) return
        val wordIndex = bitIndex.toWordIndex()
        val bitIndexOffset = bitIndex.toBitIndexOffset()
        bitsArray[wordIndex] = bitsArray[wordIndex] xor 1L.shl(bitIndexOffset)
    }

    fun or(other: LongBitGroup) {
        var i = 0
        while (i < bitsArray.size) {
            bitsArray[i] = bitsArray[i] or other.bitsArray[i]
            i++
        }
    }

    fun or(bitIndex: Int, value: Boolean) {
        if (!value) return
        val wordIndex = bitIndex.toWordIndex()
        val bitIndexOffset = bitIndex.toBitIndexOffset()
        bitsArray[wordIndex] = bitsArray[wordIndex] or 1L.shl(bitIndexOffset)
    }

    fun and(other: LongBitGroup) {
        var i = 0
        while (i < bitsArray.size) {
            bitsArray[i] = bitsArray[i] and other.bitsArray[i]
            i++
        }
    }

    fun and(bitIndex: Int, value: Boolean) {
        if (value) return
        val wordIndex = bitIndex.toWordIndex()
        val bitIndexOffset = bitIndex.toBitIndexOffset()
        bitsArray[wordIndex] = bitsArray[wordIndex] and 1L.shl(bitIndexOffset)
    }

    fun invert() {
        var i = 0
        while (i < bitsArray.size) {
            bitsArray[i] = bitsArray[i].inv()
            i++
        }
        //TODO: remove remaining
    }

    fun invert(bitIndex: Int) {
        xor(bitIndex, true)
    }
    //#endregion

    fun noBitsSet(): Boolean {
        var i = 0
        while (i < bitsArray.size) {
            if (bitsArray[i] != 0L) {
                return false
            }
            i++
        }
        return true
    }

    fun onlyOneBitSet(): Boolean {
        var only1BitSet = false
        var i = 0
        while (i < bitsArray.size) {
            if (bitsArray[i] != 0L && bitsArray[i].and(bitsArray[i] - 1L) == 0L) {
                if (only1BitSet) {
                    return false
                } else {
                    only1BitSet = true
                }
            }
            i++
        }
        return only1BitSet
    }

    operator fun get(bitIndex: Int): Boolean {
        val wordIndex = bitIndex.toWordIndex()
        val bitIndexOffset = bitIndex.toBitIndexOffset()
        return bitsArray[wordIndex].shr(bitIndexOffset).and(1L) > 0L
    }

    operator fun set(bitIndex: Int, value: Boolean) {
        val wordIndex = bitIndex.toWordIndex()
        val bitIndexOffset = bitIndex.toBitIndexOffset()

        if (value) {
            bitsArray[wordIndex] = bitsArray[wordIndex] or 1L.shl(bitIndexOffset)
        } else {
            bitsArray[wordIndex] = bitsArray[wordIndex] and 1L.shl(bitIndexOffset)
        }
    }

    //#region Utils
    private fun Int.toLongArraySize(): Int {
        return this.shr(LONG_SIZE_BITS_POWER) + 1
    }

    private fun Int.toWordIndex(): Int {
        return this.shr(LONG_SIZE_BITS_POWER)
    }

    private fun Int.toBitIndexOffset(): Int {
        return this.and(Long.SIZE_BITS - 1)
    }
    //#endregion

    fun clone(): LongBitGroup {
        return LongBitGroup(bitsArray.clone())
    }

    fun toString(size: Int): String {
        val chars = CharArray(size)
        var charIndex = 0
        var wordIndex = 0
        while (wordIndex < bitsArray.size) {
            var bitIndex = 0
            while (bitIndex < Long.SIZE_BITS) {
                chars[charIndex++] = if ((bitsArray[wordIndex].shr(bitIndex) and 1L) > 0L) '1' else '0'
                bitIndex++
            }
            wordIndex++
        }
        return String(chars)
    }

    override fun toString(): String {
        return toString(bitsArray.size)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LongBitGroup

        if (!bitsArray.contentEquals(other.bitsArray)) return false

        return true
    }

    override fun hashCode(): Int {
        return bitsArray.contentHashCode()
    }

    companion object {
        private const val LONG_SIZE_BITS_POWER = 6
    }
}