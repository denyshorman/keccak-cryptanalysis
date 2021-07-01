package keccak

import org.web3j.utils.Numeric

fun main() {
    val hash = Numeric.toHexString(Keccak.KECCAK_256.hash("hello".toByteArray()))
    println(hash)
}
