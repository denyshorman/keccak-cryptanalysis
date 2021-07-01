package keccak

import org.web3j.utils.Numeric

fun main() {
    val msg = "hello".toByteArray()

    val keccak224Hash = Numeric.toHexString(Keccak.KECCAK_224.hash(msg))
    println("keccak224: $keccak224Hash")

    val keccak256Hash = Numeric.toHexString(Keccak.KECCAK_256.hash(msg))
    println("keccak256: $keccak256Hash")

    val keccak384Hash = Numeric.toHexString(Keccak.KECCAK_384.hash(msg))
    println("keccak384: $keccak384Hash")

    val keccak512Hash = Numeric.toHexString(Keccak.KECCAK_512.hash(msg))
    println("keccak512: $keccak512Hash")
}
