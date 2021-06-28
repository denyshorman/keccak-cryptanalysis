package keccak

import org.web3j.utils.Numeric

fun main() {
    val hash = ByteArray(32)
    val keccakDigest = KeccakDigest(256)
    keccakDigest.update("t".toByteArray(), 0, 1)
    keccakDigest.doFinal(hash, 0)
    val hex = Numeric.toHexString(hash)
    println(hex)
}
