package keccak

import io.kotest.core.spec.style.FunSpec
import org.web3j.crypto.Hash
import org.web3j.utils.Numeric
import kotlin.random.Random.Default.nextBytes
import kotlin.system.exitProcess
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
            try {
                val msgBytes = nextBytes(134)

                val expected = Numeric.toHexString(Hash.sha3(msgBytes))
                val actual = Numeric.toHexString(KeccakPatched.KECCAK_256.hash(msgBytes).bytes.toByteArray())

                assertEquals(expected, actual)
            } catch (e: Exception) {
                if (e.message == "has collision") {
                    println("great")
                    exitProcess(0)
                }
            }
        }
    }

    test("keccak256 hash test string and additional tests") {
        val msg = "test"
        val msgBytes = msg.toByteArray()
        val result = KeccakPatched.KECCAK_256.hash(msgBytes)
        val countTrueValues = result.additionalEquations.asSequence().filter {it.evaluatedValue.value}.count()
        println("true values $countTrueValues")
    }

    test("keccak256 hash test string and find collision for true equations") {
        val msgBytes = nextBytes(134)

        val output = KeccakPatched.KECCAK_256.hash(msgBytes)

        val (equations, results) = output.additionalEquations.filterTrueEquations().trueEquationsToXorEquations()

        equationsToFile(equations, results, "eq.txt")
        matrixToFile(equations, results, "matrix.txt")

        XorEquationSolver.solve(equations, results)

        equationsToFile(equations, results, "eq_x.txt")
        matrixToFile(equations, results, "matrix_x.txt")
    }

    test("keccak256 hash test string and find collision") {
        val msg = "test"
        val msgBytes = msg.toByteArray()

        val expected = Numeric.toHexString(Hash.sha3(msgBytes))
        val output = KeccakPatched.KECCAK_256.hash(msgBytes)
        val variablesCount = 1600

        val (equations, results) = output.bytes.toXorEquations(variablesCount)

        equationsToFile(equations, results, "eq.txt")
        matrixToFile(equations, results, "matrix.txt")

        XorEquationSolver.solve(equations, results)

        equationsToFile(equations, results, "eq_x.txt")
        matrixToFile(equations, results, "matrix_x.txt")
    }
})
