package keccak

import io.kotest.core.spec.style.FunSpec
import keccak.util.toBigGroup
import keccak.util.toByte
import org.web3j.crypto.Hash
import org.web3j.utils.Numeric
import java.nio.file.Files
import java.nio.file.Paths
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

    test("getRidOfVariables2") {
        val msgBytes = byteArrayOf(43, -41, 18, -104, -29, 71, -26, -52, -77, 125, -82, 85, -96, 0, 108, -45, 118, -98, 110, 47, -53, -85, 0, -18, 13, 98, 26, 69, -121, -84, -121, -45)

        val hashResult = KeccakPatched.KECCAK_256.hash(msgBytes, replaceRulesInverse = true, replacePadding = false)

        val eqSystem = hashResult.bytes.toEquationSystem()

        getRidOfVariables2(eqSystem, 1088)

        println("done")
    }

    test("extendHashEquations") {
        val msgBytes = byteArrayOf(43, -41, 18, -104, -29, 71, -26, -52, -77, 125, -82, 85, -96, 0, 108, -45, 118, -98, 110, 47, -53, -85, 0, -18, 13, 98, 26, 69, -121, -84, -121, -45, 100, 99, 98)

        val hashResult = KeccakPatched.KECCAK_256.hash(msgBytes, replaceRulesInverse = true, replacePadding = false)

        val eqSystem = hashResult.bytes.toEquationSystem()

        println("getting updated system")
        val updatedSystem = extendHashEquations(eqSystem, hashResult.constraints)
        println("solving system")
        solveXorEquations(updatedSystem)
        // solveSquareMatrixXorEquations(updatedSystem)
        println("solved")
        Files.writeString(Paths.get("./build/matrix.txt"), updatedSystem.toString())
    }

    test("extendHashEquations2") {
        val msgBytes = byteArrayOf(43, -41, 18, -104, -29, 71, -26, -52, -77, 125, -82, 85, -96, 0, 108, -45, 118, -98, 110, 47, -53, -85, 0, -18, 13, 98, 26, 69, -121, -84, -121, -45, 100, 99, 98)

        val hashResult = KeccakPatched.KECCAK_256.hash(msgBytes, replaceRulesInverse = true, replacePadding = false)

        val eqSystem = hashResult.bytes.toEquationSystem()

        println("solve 256 equations")
        solveXorEquations(eqSystem)

        val cols = hashResult.constraints[0].leftSystem.cols

        val parametrizedEquationSystem = ParametrizedEquationSystem(cols, cols)
        var generalConstraintIndex = 0

        println("patch constrains")
        hashResult.constraints.forEach { constraint ->
            var eqIndex = 0
            while (eqIndex < eqSystem.equations.size) {
                var constraintIndex = 0
                while (constraintIndex < constraint.leftSystem.equations.size) {
                    if (constraint.leftSystem.equations[constraintIndex][eqIndex]) {
                        constraint.leftSystem.equations[constraintIndex].xor(eqSystem.equations[eqIndex])
                        constraint.leftSystem.results[constraintIndex] = constraint.leftSystem.results[constraintIndex] xor eqSystem.results[eqIndex]
                    }
                    if (constraint.rightSystem.equations[constraintIndex][eqIndex]) {
                        constraint.rightSystem.equations[constraintIndex].xor(eqSystem.equations[eqIndex])
                        constraint.rightSystem.results[constraintIndex] = constraint.rightSystem.results[constraintIndex] xor eqSystem.results[eqIndex]
                    }
                    constraintIndex++
                }

                constraintIndex = 0
                while (constraintIndex < constraint.leftSystem.equations.size) {
                    constraint.leftSystem.equations[constraintIndex].clear(0, 1088)
                    constraint.rightSystem.equations[constraintIndex].clear(0, 1088)
                    constraint.leftSystem.equations[constraintIndex].xor(constraint.rightSystem.equations[constraintIndex])
                    val varIndex = constraint.varSystem.equations[constraintIndex].nextSetBit(0)
                    if (constraint.leftSystem.equations[constraintIndex][varIndex]) {
                        constraint.leftSystem.equations[constraintIndex].clear(varIndex)
                    } else {
                        constraint.leftSystem.equations[constraintIndex].set(varIndex)
                    }
                    constraintIndex++
                }

                eqIndex++
            }

            var constraintIndex = 0
            while (constraintIndex < constraint.leftSystem.equations.size) {
                parametrizedEquationSystem.equations[generalConstraintIndex] = constraint.leftSystem.equations[constraintIndex]
                generalConstraintIndex++
                constraintIndex++
            }
        }

        println("solveParametrizedEquationSystem")
        solveParametrizedEquationSystem(parametrizedEquationSystem)
        println("solved")

        val emptyEquations = parametrizedEquationSystem.equations.asSequence().filter { it.isEmpty() }.count() - 1088
        println(emptyEquations)

        println("dump0")
        var s = EquationSystem(parametrizedEquationSystem.equations, BitGroup(parametrizedEquationSystem.rows))
        Files.writeString(Paths.get("./build/matrix0.txt"), s.toString())

        println("dump1")
        s = EquationSystem(parametrizedEquationSystem.results, BitGroup(parametrizedEquationSystem.rows))
        Files.writeString(Paths.get("./build/matrix1.txt"), s.toString())
    }

    test("keccak256 some bytes2") {
        val msg0 = byteArrayOf(43, -41, 18, -104, -29, 71, -26, -52, -77, 125, -82, 85, -96, 0, 108, -45, 118, -98, 110, 47, -53, -85, 0, -18, 13, 98, 26, 69, -121, -84, -121, -45, 100, 99, 98)
        val msg1 = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

        val a = Numeric.toHexString(Hash.sha3(msg0))
        val b = Numeric.toHexString(Hash.sha3(msg1))

        println(a)
        println(b)
    }

    context("mapToEquation") {
        test("1") {
            val rows = 2
            val cols = 4

            val hash = EquationSystem(rows, cols)
            hash.equations[0][0] = true
            hash.equations[0][1] = false
            hash.equations[0][2] = true
            hash.equations[0][3] = true
            hash.results[0] = false

            hash.equations[1][0] = true
            hash.equations[1][1] = true
            hash.equations[1][2] = true
            hash.equations[1][3] = true
            hash.results[1] = false

            val group = BitGroup(cols)
            group[0] = true
            group[1] = false
            group[2] = false
            group[3] = true

            val actual = mapToEquation(hash, group)
            val expected = XorEquation(cols)
            expected.bitGroup[0] = true
            expected.bitGroup[1] = false
            expected.bitGroup[2] = true
            expected.bitGroup[3] = true
            expected.result = false

            assertEquals(expected, actual)
        }

        test("2") {
            val rows = 2
            val cols = 4

            val hash = EquationSystem(rows, cols)
            hash.equations[0][0] = true
            hash.equations[0][1] = false
            hash.equations[0][2] = true
            hash.equations[0][3] = false

            hash.equations[1][0] = false
            hash.equations[1][1] = true
            hash.equations[1][2] = false
            hash.equations[1][3] = true

            val group = BitGroup(cols)
            group[0] = true
            group[1] = false
            group[2] = false
            group[3] = true

            val actual = mapToEquation(hash, group)
            val expected = XorEquation(cols)
            expected.bitGroup[0] = true
            expected.bitGroup[1] = true
            expected.bitGroup[2] = true
            expected.bitGroup[3] = true
            expected.result = false

            assertEquals(expected, actual)
        }

        test("3") {
            val rows = 2
            val cols = 4

            val hash = EquationSystem(rows, cols)
            hash.equations[0][0] = false
            hash.equations[0][1] = true
            hash.equations[0][2] = true
            hash.equations[0][3] = false

            hash.equations[1][0] = true
            hash.equations[1][1] = true
            hash.equations[1][2] = false
            hash.equations[1][3] = false

            val group = BitGroup(cols)
            group[0] = true
            group[1] = false
            group[2] = false
            group[3] = true

            val actual = mapToEquation(hash, group)
            val expected = XorEquation(cols)
            expected.bitGroup[0] = true
            expected.bitGroup[1] = true
            expected.bitGroup[2] = false
            expected.bitGroup[3] = false
            expected.result = false

            assertEquals(expected, actual)
        }

        test("4") {
            val rows = 2
            val cols = 4

            val hash = EquationSystem(rows, cols)
            hash.equations[0][0] = false
            hash.equations[0][1] = true
            hash.equations[0][2] = true
            hash.equations[0][3] = false
            hash.results[0] = true

            hash.equations[1][0] = true
            hash.equations[1][1] = true
            hash.equations[1][2] = false
            hash.equations[1][3] = false
            hash.results[1] = true

            val group = BitGroup(cols)
            group[0] = true
            group[1] = false
            group[2] = false
            group[3] = true

            val actual = mapToEquation(hash, group)
            val expected = XorEquation(cols)
            expected.bitGroup[0] = true
            expected.bitGroup[1] = true
            expected.bitGroup[2] = false
            expected.bitGroup[3] = false
            expected.result = true

            assertEquals(expected, actual)
        }

        test("5") {
            val rows = 2
            val cols = 4

            val hash = EquationSystem(rows, cols)
            hash.equations[0][0] = true
            hash.equations[0][1] = true
            hash.equations[0][2] = false
            hash.equations[0][3] = false
            hash.results[0] = true

            hash.equations[1][0] = false
            hash.equations[1][1] = false
            hash.equations[1][2] = true
            hash.equations[1][3] = true
            hash.results[1] = true

            val group = BitGroup(cols)
            group[0] = true
            group[1] = false
            group[2] = false
            group[3] = true

            val actual = mapToEquation(hash, group)
            val expected = XorEquation(cols)
            expected.bitGroup[0] = true
            expected.bitGroup[1] = true
            expected.bitGroup[2] = true
            expected.bitGroup[3] = true
            expected.result = false

            assertEquals(expected, actual)
        }

        test("6") {
            val rows = 2
            val cols = 4

            val hash = EquationSystem(rows, cols)
            hash.equations[0][0] = true
            hash.equations[0][1] = true
            hash.equations[0][2] = false
            hash.equations[0][3] = false
            hash.results[0] = false

            hash.equations[1][0] = false
            hash.equations[1][1] = false
            hash.equations[1][2] = true
            hash.equations[1][3] = true
            hash.results[1] = true

            val group = BitGroup(cols)
            group[0] = true
            group[1] = false
            group[2] = false
            group[3] = true

            val actual = mapToEquation(hash, group)
            val expected = XorEquation(cols)
            expected.bitGroup[0] = true
            expected.bitGroup[1] = true
            expected.bitGroup[2] = true
            expected.bitGroup[3] = true
            expected.result = true

            assertEquals(expected, actual)
        }

        test("7") {
            val rows = 2
            val cols = 4

            val hash = EquationSystem(rows, cols)
            hash.equations[0][0] = false
            hash.equations[0][1] = true
            hash.equations[0][2] = true
            hash.equations[0][3] = false
            hash.results[0] = false

            hash.equations[1][0] = false
            hash.equations[1][1] = true
            hash.equations[1][2] = true
            hash.equations[1][3] = false
            hash.results[1] = true

            val group = BitGroup(cols)
            group[0] = true
            group[1] = false
            group[2] = false
            group[3] = true

            val actual = mapToEquation(hash, group)
            val expected = XorEquation(cols)
            expected.bitGroup[0] = false
            expected.bitGroup[1] = false
            expected.bitGroup[2] = false
            expected.bitGroup[3] = false
            expected.result = false

            assertEquals(expected, actual)
        }
    }
})
