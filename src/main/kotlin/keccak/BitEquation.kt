package keccak

class BitEquation(val cols: Int) {
    val bitSet = FixedBitSet(cols)
    var result = false

    fun setVariable(varIndex: Int) {
        bitSet[varIndex] = bitSet[varIndex] xor true
    }

    fun setBit(value: Boolean) {
        result = result xor value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BitEquation

        if (cols != other.cols) return false
        if (bitSet != other.bitSet) return false
        if (result != other.result) return false

        return true
    }

    override fun hashCode(): Int {
        var hash = cols
        hash = 31 * hash + bitSet.hashCode()
        hash = 31 * hash + result.hashCode()
        return hash
    }

    override fun toString(): String {
        return "$bitSet = ${result.toNumChar()}"
    }
}
