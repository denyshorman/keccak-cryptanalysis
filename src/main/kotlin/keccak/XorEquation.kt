package keccak

import keccak.util.toNumChar

class XorEquation {
    val bitGroup: BitGroup
    var result: Boolean

    constructor(cols: Int) {
        this.bitGroup = BitGroup(cols)
        this.result = false
    }

    constructor(bitGroup: BitGroup, result: Boolean = false) {
        this.bitGroup = bitGroup
        this.result = result
    }

    val varCount: Int get() = bitGroup.size

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

    fun xor(eq: XorEquation) {
        bitGroup.xor(eq.bitGroup)
        result = result xor eq.result
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

        other as XorEquation

        if (result != other.result) return false
        if (bitGroup != other.bitGroup) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bitGroup.hashCode()
        result = 31 * result + result.hashCode()
        return result
    }

    override fun toString(): String {
        return "$bitGroup = ${result.toNumChar()}"
    }
}
