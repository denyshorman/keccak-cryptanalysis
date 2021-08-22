package keccak

class AndEquation {
    val left: XorEquation
    val right: XorEquation

    constructor(cols: Int) {
        this.left = XorEquation(cols)
        this.right = XorEquation(cols)
    }

    constructor(left: XorEquation, right: XorEquation) {
        this.left = left
        this.right = right
    }

    val size: Int get() = left.varCount

    fun variableExists(i: Int, j: Int): Boolean {
        return (left.bitGroup[i] && right.bitGroup[j]) xor if (i == j) {
            (left.bitGroup[i] && right.result) xor (left.result && right.bitGroup[j])
        } else {
            (left.bitGroup[j] && right.bitGroup[i])
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AndEquation

        if (left != other.left) return false
        if (right != other.right) return false

        return true
    }

    override fun hashCode(): Int {
        var result = left.hashCode()
        result = 31 * result + right.hashCode()
        return result
    }

    override fun toString(): String {
        return "($left)($right)"
    }
}
