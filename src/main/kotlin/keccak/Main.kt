package keccak

import org.web3j.utils.Numeric

fun main() {
    val hash = Numeric.toHexString(Keccak256Digest().digest("t".toByteArray()))
    println(hash)
}
