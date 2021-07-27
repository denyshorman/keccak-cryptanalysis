package keccak

class EquationSystem {
    val rows: Int
    val cols: Int
    val equations: Array<FixedBitSet>
    val results: FixedBitSet

    constructor(
        rows: Int,
        cols: Int,
    ) {
        this.rows = rows
        this.cols = cols
        this.equations = Array(rows) { FixedBitSet(cols) }
        this.results = FixedBitSet(rows)
    }

    private constructor(equations: Array<FixedBitSet>, results: FixedBitSet) {
        this.rows = equations.size
        this.cols = equations.getOrNull(0)?.size ?: 0
        this.equations = equations
        this.results = results
    }

    fun isInvalid(eqIndex: Int): Boolean {
        return equations[eqIndex].isEmpty() && results[eqIndex]
    }

    fun exchange(i: Int, j: Int) {
        equations.exchange(i, j)
        results.exchange(i, j)
    }

    fun xor(i: Int, j: Int) {
        equations[i].xor(equations[j])
        results[i] = results[i] xor results[j]
    }

    fun xor(vararg systems: EquationSystem) {
        var i = 0
        var j: Int
        while (i < systems.size) {
            j = 0
            while (j < systems[i].rows) {
                equations[j].xor(systems[i].equations[j])
                j++
            }
            results.xor(systems[i].results)
            i++
        }
    }

    fun rotateEquationsLeft(count: Int) {
        var roundIndex = 0
        var eqIndex: Int
        var firstEquation: FixedBitSet
        var firstResultBit: Boolean
        val lastBitIndex = rows - 1

        while (roundIndex < count) {
            eqIndex = 0
            firstEquation = equations[0]
            firstResultBit = results[0]
            while (eqIndex < lastBitIndex) {
                equations[eqIndex] = equations[eqIndex + 1]
                results[eqIndex] = results[eqIndex + 1]
                eqIndex++
            }
            equations[lastBitIndex] = firstEquation
            results[lastBitIndex] = firstResultBit
            roundIndex++
        }
    }

    fun clear() {
        var i = 0
        while (i < equations.size) {
            equations[i].clear()
            i++
        }
        results.clear()
    }

    fun clone(): EquationSystem {
        val newEquations = Array(rows) { equations[it].clone() }
        val newResults = results.clone()
        return EquationSystem(newEquations, newResults)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EquationSystem

        if (rows != other.rows) return false
        if (cols != other.cols) return false
        if (!equations.contentEquals(other.equations)) return false
        if (results != other.results) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rows
        result = 31 * result + cols
        result = 31 * result + equations.contentHashCode()
        result = 31 * result + results.hashCode()
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder(rows * (cols + 3) - 1)
        var eqIndex = 0

        while (eqIndex < rows) {
            var bitIndex = 0
            while (bitIndex < cols) {
                sb.append(equations[eqIndex][bitIndex].toNumChar())
                bitIndex++
            }
            sb.append('|')
            sb.append(results[eqIndex].toNumChar())

            eqIndex++

            if (eqIndex != rows) {
                sb.append('\n')
            }
        }

        return sb.toString()
    }
}

fun EquationSystem.toLittleEndianBytes(): Array<EquationSystem> {
    return Array(Long.SIZE_BYTES) { byteIndex ->
        val system = EquationSystem(Byte.SIZE_BITS, cols)

        var i = Long.SIZE_BITS - (byteIndex + 1) * Byte.SIZE_BITS
        val limit = i + Byte.SIZE_BITS
        var j = 0
        while (i < limit) {
            system.equations[j] = equations[i]
            system.results[j] = results[i]
            i++
            j++
        }

        system
    }
}

fun Array<EquationSystem>.littleEndianBytesToLong(cols: Int): EquationSystem {
    val bytes = this
    val system = EquationSystem(Long.SIZE_BITS, cols)

    var bitIndex = 0
    while (bitIndex < Long.SIZE_BITS) {
        val byteIndex = Long.SIZE_BYTES - bitIndex / Byte.SIZE_BITS - 1
        val newBitIndex = bitIndex % Byte.SIZE_BITS
        system.equations[bitIndex] = bytes[byteIndex].equations[newBitIndex]
        system.results[bitIndex] = bytes[byteIndex].results[newBitIndex]
        bitIndex++
    }

    return system
}

fun Byte.toEquationSystem(cols: Int): EquationSystem {
    val byte = this
    val system = EquationSystem(Byte.SIZE_BITS, cols)

    var bitIndex = 0
    while (bitIndex < Byte.SIZE_BITS) {
        val bit = (byte.toInt() shr (Byte.SIZE_BITS - bitIndex - 1)) and 1
        system.results[bitIndex] = bit > 0
        bitIndex++
    }

    return system
}
