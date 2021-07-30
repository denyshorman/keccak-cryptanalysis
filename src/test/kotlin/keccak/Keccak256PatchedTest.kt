package keccak

import io.kotest.core.spec.style.FunSpec
import keccak.util.toBigGroup
import keccak.util.toByte
import org.web3j.crypto.Hash
import org.web3j.utils.Numeric
import kotlin.random.Random.Default.nextBytes
import kotlin.test.assertEquals

class Keccak256PatchedTest : FunSpec({
    test("keccak256 hash empty string") {
        val msg = ""
        val msgBytes = msg.toByteArray()

        val expected = Numeric.toHexString(Hash.sha3(msgBytes))
        val actual = Numeric.toHexString(KeccakPatched.KECCAK_256.hash(msgBytes).bytes.toByteArray())

        assertEquals(expected, actual)
    }

    test("keccak256 hash test string") {
        val msg = "test"
        val msgBytes = msg.toByteArray()

        val expected = Numeric.toHexString(Hash.sha3(msgBytes))
        val actual = Numeric.toHexString(KeccakPatched.KECCAK_256.hash(msgBytes).bytes.toByteArray())

        assertEquals(expected, actual)
    }

    test("keccak256 hash 133 len string") {
        val msg = ("1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "111"
                )
        val msgBytes = msg.toByteArray()

        val expected = Numeric.toHexString(Hash.sha3(msgBytes))
        val actual = Numeric.toHexString(KeccakPatched.KECCAK_256.hash(msgBytes).bytes.toByteArray())

        assertEquals(expected, actual)
    }

    test("keccak256 hash 134 len string") {
        val msg = ("1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111"
                )
        val msgBytes = msg.toByteArray()

        val expected = Numeric.toHexString(Hash.sha3(msgBytes))
        val actual = Numeric.toHexString(KeccakPatched.KECCAK_256.hash(msgBytes).bytes.toByteArray())

        assertEquals(expected, actual)
    }

    test("keccak256 hash 135 len string") {
        val msg = ("1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "11111"
                )
        val msgBytes = msg.toByteArray()

        val expected = Numeric.toHexString(Hash.sha3(msgBytes))
        val actual = Numeric.toHexString(KeccakPatched.KECCAK_256.hash(msgBytes).bytes.toByteArray())

        assertEquals(expected, actual)
    }

    test("keccak256 hash 136 len string") {
        val msg = ("1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "111111"
                )
        val msgBytes = msg.toByteArray()

        val expected = Numeric.toHexString(Hash.sha3(msgBytes))
        val actual = Numeric.toHexString(KeccakPatched.KECCAK_256.hash(msgBytes).bytes.toByteArray())

        assertEquals(expected, actual)
    }

    test("keccak256 hash 137 len string") {
        val msg = ("1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111"
                )
        val msgBytes = msg.toByteArray()

        val expected = Numeric.toHexString(Hash.sha3(msgBytes))
        val actual = Numeric.toHexString(KeccakPatched.KECCAK_256.hash(msgBytes).bytes.toByteArray())

        assertEquals(expected, actual)
    }

    test("keccak256 hash random bytes") {
        val msgBytes = nextBytes(134)
        val expected = Numeric.toHexString(Hash.sha3(msgBytes))
        val actual = Numeric.toHexString(KeccakPatched.KECCAK_256.hash(msgBytes).bytes.toByteArray())
        assertEquals(expected, actual)
    }

    test("keccak256 randomBytes") {
        while (true) {
            val msgBytes = nextBytes(134)

            val expected = Numeric.toHexString(Hash.sha3(msgBytes))
            val actual = Numeric.toHexString(KeccakPatched.KECCAK_256.hash(msgBytes).bytes.toByteArray())

            assertEquals(expected, actual)
        }
    }

    test("keccak256 some bytes") {
        val msgBytes = byteArrayOf(43, -41, 18, -104, -29, 71, -26, -52, -77, 125, -82, 85, -96, 0, 108, -45, 118, -98, 110, 47, -53, -85, 0, -18, 13, 98, 26, 69, -121, -84, -121, -45)

        val expected = Numeric.toHexString(Hash.sha3(msgBytes))
        val hashResult = KeccakPatched.KECCAK_256.hash(msgBytes, replaceRulesInverse = true, replacePadding = false)
        val actual = Numeric.toHexString(hashResult.bytes.toByteArray())

        val resultBitGroup = toBigGroup(msgBytes, hashResult.constraints)

        hashResult.bytes.forEach { byte ->
            byte.eqSystem.evaluate(resultBitGroup)
            val eqSystemByte = byte.eqSystem.toByte()
            assertEquals(byte.byte, eqSystemByte)
        }

        assertEquals(expected, actual)
    }
})
