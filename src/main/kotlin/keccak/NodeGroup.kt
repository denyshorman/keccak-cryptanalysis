package keccak

class NodeGroup(val bits: Array<Node>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NodeGroup

        if (!bits.contentEquals(other.bits)) return false

        return true
    }

    override fun hashCode(): Int {
        return bits.contentHashCode()
    }

    override fun toString(): String {
        return bits.asSequence().map { "[$it]" }.joinToString("")
    }
}
