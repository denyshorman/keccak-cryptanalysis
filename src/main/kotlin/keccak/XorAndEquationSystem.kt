package keccak

class XorAndEquationSystem {
    val rows: Int
    val cols: Int
    val equations: Array<XorAndEquation>
    val results: BitGroup

    constructor(rows: Int, cols: Int) {
        this.rows = rows
        this.cols = cols
        this.equations = Array(rows) { XorAndEquation() }
        this.results = BitGroup(rows)
    }

    private constructor(equations: Array<XorAndEquation>, results: BitGroup) {
        this.rows = equations.size
        this.cols = equations.getOrNull(0)?.varCount ?: 0
        this.equations = equations
        this.results = results
    }

    fun exchange(i: Int, j: Int) {
        equations.exchange(i, j)
        results.exchange(i, j)
    }

    fun xor(i: Int, j: Int) {
        equations[i].xor(equations[j])
        results[i] = results[i] xor results[j]
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as XorAndEquationSystem

        if (rows != other.rows) return false
        if (cols != other.cols) return false
        if (results != other.results) return false
        if (!equations.contentEquals(other.equations)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rows
        result = 31 * result + cols
        result = 31 * result + equations.contentHashCode()
        result = 31 * result + results.hashCode()
        return result
    }
}
