package keccak

import keccak.util.toNumChar

class BitEquation {
    val cols: Int
    val bitGroup: BitGroup
    var result: Boolean

    constructor(cols: Int) {
        this.cols = cols
        this.bitGroup = BitGroup(cols)
        this.result = false
    }

    constructor(bitGroup: BitGroup, result: Boolean = false) {
        this.cols = bitGroup.size
        this.bitGroup = bitGroup.clone()
        this.result = result
    }

    fun setVariable(varIndex: Int) {
        bitGroup[varIndex] = bitGroup[varIndex] xor true
    }

    fun setBit(value: Boolean) {
        result = result xor value
    }

    fun copy(bitGroup: BitGroup, result: Boolean) {
        clear()
        xor(bitGroup, result)
    }

    fun xor(bitGroup: BitGroup, result: Boolean) {
        this.bitGroup.xor(bitGroup)
        this.result = this.result xor result
    }

    fun clear() {
        bitGroup.clear()
        result = false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BitEquation

        if (cols != other.cols) return false
        if (result != other.result) return false
        if (bitGroup != other.bitGroup) return false

        return true
    }

    override fun hashCode(): Int {
        var hash = cols
        hash = 31 * hash + bitGroup.hashCode()
        hash = 31 * hash + result.hashCode()
        return hash
    }

    override fun toString(): String {
        return "$bitGroup = ${result.toNumChar()}"
    }
}
