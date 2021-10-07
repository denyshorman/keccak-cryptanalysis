package keccak

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import keccak.util.toAndEquationSystem
import keccak.util.toFile
import keccak.util.toXorAndEquationSystem
import keccak.util.toXorEquationSystem
import org.web3j.crypto.Hash
import org.web3j.utils.Numeric
import java.io.File
import kotlin.random.Random
import kotlin.random.Random.Default.nextBytes
import kotlin.time.Duration

class Keccak256EqSystemGeneratorTest : FunSpec({
    test("keccak256 hash empty string") {
        val msg = ""
        val msgBytes = msg.toByteArray()

        val expected = Numeric.toHexString(Hash.sha3(msgBytes))
        val actual = Numeric.toHexString(Keccak256EqSystemGenerator.INSTANCE.hash(msgBytes).hash)

        expected.shouldBe(actual)
    }

    test("keccak256 hash test string") {
        val msg = "test"
        val msgBytes = msg.toByteArray()

        val expected = Numeric.toHexString(Hash.sha3(msgBytes))
        val actual = Numeric.toHexString(Keccak256EqSystemGenerator.INSTANCE.hash(msgBytes).hash)

        expected.shouldBe(actual)
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
        val actual = Numeric.toHexString(Keccak256EqSystemGenerator.INSTANCE.hash(msgBytes).hash)

        expected.shouldBe(actual)
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
        val actual = Numeric.toHexString(Keccak256EqSystemGenerator.INSTANCE.hash(msgBytes).hash)

        expected.shouldBe(actual)
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
        val actual = Numeric.toHexString(Keccak256EqSystemGenerator.INSTANCE.hash(msgBytes).hash)

        expected.shouldBe(actual)
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
        val hashResult = Keccak256EqSystemGenerator.INSTANCE.hash(msgBytes, replaceRulesInverse = true, replacePadding = false)
        val actual = Numeric.toHexString(hashResult.hash)

        expected.shouldBe(actual)
        hashResult.equationSystem.isValid(hashResult.varValues).shouldBeTrue()
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
        val hashResult = Keccak256EqSystemGenerator.INSTANCE.hash(msgBytes, replaceRulesInverse = true, replacePadding = false)
        val actual = Numeric.toHexString(hashResult.hash)

        expected.shouldBe(actual)
        hashResult.equationSystem.isValid(hashResult.varValues).shouldBeTrue()
    }

    test("keccak256 hash random bytes") {
        val msgBytes = nextBytes(134)
        val expected = Numeric.toHexString(Hash.sha3(msgBytes))
        val actual = Numeric.toHexString(Keccak256EqSystemGenerator.INSTANCE.hash(msgBytes).hash)
        expected.shouldBe(actual)
    }

    test("keccak256 randomBytes") {
        while (true) {
            val msgBytes = nextBytes(134)

            val expected = Numeric.toHexString(Hash.sha3(msgBytes))
            val hashResult = Keccak256EqSystemGenerator.INSTANCE.hash(msgBytes, replaceRulesInverse = true, replacePadding = false)
            val actual = Numeric.toHexString(hashResult.hash)

            expected.shouldBe(actual)
            hashResult.equationSystem.isValid(hashResult.varValues).shouldBeTrue()
        }
    }

    test("keccak256 some bytes") {
        val msgBytes = byteArrayOf(43, -41, 18, -104, -29, 71, -26, -52, -77, 125, -82, 85, -96, 0, 108, -45, 118, -98, 110, 47, -53, -85, 0, -18, 13, 98, 26, 69, -121, -84, -121, -45)

        val expected = Numeric.toHexString(Hash.sha3(msgBytes))
        val hashResult = Keccak256EqSystemGenerator.INSTANCE.hash(msgBytes, replaceRulesInverse = true, replacePadding = false)
        val actual = Numeric.toHexString(hashResult.hash)

        expected.shouldBe(actual)
        hashResult.equationSystem.isValid(hashResult.varValues).shouldBeTrue()
    }

    test("xorAndEqSystemProcessing").config(timeout = Duration.days(10)) {
        val xorAndEqSystem = (File("D:\\test\\leftRight.txt") to File("D:\\test\\andEquations.txt"))
            .toXorAndEquationSystem(
                xorEqRows = 76777,
                andEqRows = 37057,
                varsCount = 116288,
                xorHumanReadable = false,
                andHumanReadable = true,
            )

        xorAndEqSystem.substituteAndSystem()

        xorAndEqSystem.andSystem.toFile(File("D:\\test\\andEquationsProcessed.txt"), humanReadable = true)
    }

    test("convertAndEquationsProcessedHumanToBinary").config(timeout = Duration.days(10)) {
        val andSystem = File("D:\\test\\andEquationsProcessed.txt").toAndEquationSystem(37057, 116288, true)

        andSystem.toFile(File("D:\\test\\andEquationsProcessedBinary.txt"), humanReadable = false)
    }

    test("zero all eqs and try to find a solution").config(timeout = Duration.days(10)) {
        val system = File("D:\\test\\leftRight.txt").toXorEquationSystem(76777, 116288, humanReadable = false)

        var i = 0
        while (i < system.rows) {
            if (!system.equations[i].isEmpty) {
                val firstBitIndex = system.equations[i].nextSetBit(0)

                if (firstBitIndex < 39488) {
                    system.equations[i].clear(firstBitIndex)
                    system.results[i] = system.results[i] xor Random.nextBoolean()
                } else {
                    system.equations[i].clear()
                }
            }
            i++
        }

        val solved = system.solve(logProgress = true, progressStep = 4096)

        println("solved: $solved")

        system.toFile(File("D:\\test\\leftRightSolved.txt"), humanReadable = true)
    }
    
    test("keccak256 some bytes2") {
        val msg0 = byteArrayOf(43, -41, 18, -104, -29, 71, -26, -52, -77, 125, -82, 85, -96, 0, 108, -45, 118, -98, 110, 47, -53, -85, 0, -18, 13, 98, 26, 69, -121, -84, -121, -45, 100, 99, 98)
        val msg1 = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

        val a = Numeric.toHexString(Hash.sha3(msg0))
        val b = Numeric.toHexString(Hash.sha3(msg1))

        println(a)
        println(b)
    }
})
