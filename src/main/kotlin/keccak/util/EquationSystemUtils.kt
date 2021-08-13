package keccak.util

import keccak.EquationSystem
import keccak.XorEquation

fun EquationSystem.toLittleEndianBytes(): Array<EquationSystem> {
    return Array(Long.SIZE_BYTES) { byteIndex ->
        val system = EquationSystem(Byte.SIZE_BITS, cols)

        var i = Long.SIZE_BITS - (byteIndex + 1) * Byte.SIZE_BITS
        val limit = i + Byte.SIZE_BITS
        var j = 0
        while (i < limit) {
            system.equations[j].xor(equations[i])
            system.results[j] = results[i]
            i++
            j++
        }

        system
    }
}

fun EquationSystem.toByte(): Byte {
    val system = this
    var byte: Byte = 0

    var bitIndex = 0
    while (bitIndex < Byte.SIZE_BITS) {
        byte = byte.setBit(bitIndex, system.results[bitIndex])
        bitIndex++
    }

    return byte
}

fun EquationSystem.toLong(): Long {
    val system = this
    var long = 0L

    var bitIndex = 0
    while (bitIndex < Long.SIZE_BITS) {
        long = long.setBit(bitIndex, system.results[bitIndex])
        bitIndex++
    }

    return long
}

fun EquationSystem.setVariables() {
    var i = 0
    while (i < rows && i < cols) {
        equations[i][i] = true
        i++
    }
}

fun EquationSystem.toBitEquation(eqIndex: Int): XorEquation {
    val eq = equations[eqIndex].clone()
    val res = results[eqIndex]
    return XorEquation(eq, res)
}

fun Array<EquationSystem>.toLong(): Long {
    val bytes = this
    var value = 0L

    var byteIndex = 0
    var bitIndex = 0
    while (byteIndex < size) {
        var bitIndexInByte = 0
        while (bitIndexInByte < bytes[byteIndex].cols) {
            value = value.setBit(bitIndex, bytes[byteIndex].results[bitIndexInByte])
            bitIndex++
            bitIndexInByte++
        }
        byteIndex++
    }

    return value
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
