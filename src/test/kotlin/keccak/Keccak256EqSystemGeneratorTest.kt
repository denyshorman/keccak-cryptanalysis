package keccak

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import keccak.util.*
import org.web3j.crypto.Hash
import org.web3j.utils.Numeric
import java.io.File
import java.util.*
import kotlin.concurrent.thread
import kotlin.random.Random
import kotlin.random.Random.Default.nextBytes
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

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

        xorAndEqSystem.substituteAndWithXor()

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

    test("keccak256 equations invert to xor-and system") {
        val msg = byteArrayOf(43, -41, 18, -104, -29, 71, -26, -52, -77, 125, -82, 85, -96, 0, 108, -45, 118, -98, 110, 47, -53, -85, 0, -18, 13, 98, 26, 69, -121, -84, -121, -45, 100)

        val hashResult = Keccak256EqSystemGenerator.INSTANCE.hash(msg, replaceRulesInverse = true, replacePadding = false)

        val invertedSystem = hashResult.equationSystem.invertToXorAndSystem()
        invertedSystem.xorSystem.solve(skipValidation = true, logProgress = true, progressStep = 4096)
        invertedSystem.substituteAndWithXor()

        invertedSystem.toFile(
            File("D:\\test\\xor1.txt"),
            File("D:\\test\\and1.txt"),
            xorHumanReadable = false,
            andHumanReadable = true,
        )
    }

    test("keccak256 equations invert to xor system") {
        val msg = byteArrayOf(43, -41, 18, -104, -29, 71, -26, -52, -77, 125, -82, 85, -96, 0, 108, -45, 118, -98, 110, 47, -53, -85, 0, -18, 13, 98, 26, 69, -121, -84, -121, -45, 100)

        val hashResult = Keccak256EqSystemGenerator.INSTANCE.hash(msg, replaceRulesInverse = true, replacePadding = false)

        val normalizedSystem = hashResult.equationSystem.simplify()
        val invertedSystem = normalizedSystem.invertToXorSystem()
        invertedSystem.solve(skipValidation = true, logProgress = true, progressStep = 1024)

        invertedSystem.toFile(
            file = File("D:\\test\\updated_normalized_inverted_xor.txt"),
            humanReadable = true,
        )
    }

    test("keccak256 validate inverted system") {
        val msg = byteArrayOf(43, -41, 18, -104, -29, 71, -26, -52, -77, 125, -82, 85, -96, 0, 108, -45, 118, -98, 110, 47, -53, -85, 0, -18, 13, 98, 26, 69, -121, -84, -121, -45, 100)

        val hashResult = Keccak256EqSystemGenerator.INSTANCE.hash(msg, replaceRulesInverse = true, replacePadding = false)

        val system = (File("D:\\test\\xor1.txt") to File("D:\\test\\and1.txt")).toXorAndEquationSystem(
            xorEqRows = 77056,
            andEqRows = hashResult.equationSystem.rows,
            varsCount = 116288,
            xorHumanReadable = false,
            andHumanReadable = true,
        )

        var i = hashResult.equationSystem.cols
        var j = 0
        while (j < hashResult.equationSystem.rows) {
            var value = hashResult.equationSystem.equations[j].andOpLeft.evaluate(hashResult.varValues) xor hashResult.equationSystem.andOpLeftResults[j]
            hashResult.varValues.setIfTrue(i++, value)

            value = hashResult.equationSystem.equations[j].andOpRight.evaluate(hashResult.varValues) xor hashResult.equationSystem.andOpRightResults[j]
            hashResult.varValues.setIfTrue(i++, value)

            j++
        }

        val valid = system.isValid(hashResult.varValues)

        println(valid)
    }

    test("keccak256 load xor and convert to human") {
        val xorSystem = File("D:\\test\\xor1.txt").toXorEquationSystem(77056, 116288, false)
        xorSystem.toFile(File("D:\\test\\xor1_binary.txt"), 0, 264, false)
    }

    test("keccak256 load xor and convert to initial") {
        val msg = byteArrayOf(43, -41, 18, -104, -29, 71, -26, -52, -77, 125, -82, 85, -96, 0, 108, -45, 118, -98, 110, 47, -53, -85, 0, -18, 13, 98, 26, 69, -121, -84, -121, -45, 100)

        val hashResult = Keccak256EqSystemGenerator.INSTANCE.hash(msg, replaceRulesInverse = true, replacePadding = false)

        val loadedXorSystem = File("D:\\test\\xor1_binary.txt").toXorEquationSystem(264, 116288, false)

        var i = 0
        while (i < 264) {
            var bitIndex = 1088

            while (true) {
                bitIndex = loadedXorSystem.equations[i].nextSetBit(bitIndex)
                if (bitIndex == -1) break

                val andEqIndex = (bitIndex - hashResult.equationSystem.cols) / 2
                val side = bitIndex % 2

                if (side == 0) {
                    loadedXorSystem.equations[i].xor(hashResult.equationSystem.equations[andEqIndex].andOpLeft)
                    loadedXorSystem.results[i].xor(hashResult.equationSystem.andOpLeftResults[andEqIndex])
                } else {
                    loadedXorSystem.equations[i].xor(hashResult.equationSystem.equations[andEqIndex].andOpRight)
                    loadedXorSystem.results[i].xor(hashResult.equationSystem.andOpRightResults[andEqIndex])
                }

                loadedXorSystem.equations[i].clear(bitIndex)
            }

            i++
        }

        loadedXorSystem.toFile(File("D:\\test\\xor2_human.txt"), 0, 264, true)
    }

    test("keccak256 load xor and calc variables") {
        val xorSystem = File("D:\\test\\xor1.txt").toXorEquationSystem(77056, 116288, false)

        var i = 0
        val mask = BitSet(xorSystem.cols)
        while (i < 264) {
            mask.or(xorSystem.equations[i])
            mask.clear(i)
            i++
        }

        println(mask.setBitsCount())
        println(mask.nextSetBit(0))
        println(mask.previousSetBit(xorSystem.cols - 1))
    }

    test("keccak256 240 bit message") {
        val msg = nextBytes(32)

        val hashResult = Keccak256EqSystemGenerator.INSTANCE.hash(msg, replaceRulesInverse = true, replacePadding = false)

        val colsMask = BitSet(hashResult.equationSystem.cols)
        colsMask.set(0, hashResult.equationSystem.cols)

        var replaced = true
        var count = 0

        while (replaced) {
            replaced = false

            hashResult.equationSystem.equations.forEachIndexed { index, equation ->
                if ((equation.andOpLeft.isEmpty || equation.andOpRight.isEmpty) && !equation.rightXor.isEmpty) {
                    val varIndex = equation.rightXor.nextSetBit(1088)

                    if (equation.andOpLeft.isEmpty && equation.andOpRight.isEmpty) {
                        val value = hashResult.equationSystem.andOpLeftResults[index] &&
                                hashResult.equationSystem.andOpRightResults[index]

                        hashResult.equationSystem.substitute(varIndex, value)
                        replaced = true
                        count++
                    } else if (equation.andOpLeft.isEmpty) {
                        if (!hashResult.equationSystem.andOpLeftResults[index]) {
                            hashResult.equationSystem.substitute(varIndex, false)
                            replaced = true
                            count++
                        }
                    } else if (equation.andOpRight.isEmpty) {
                        if (!hashResult.equationSystem.andOpRightResults[index]) {
                            hashResult.equationSystem.substitute(varIndex, false)
                            replaced = true
                            count++
                        }
                    }
                }
            }
        }

        println(count)

        hashResult.equationSystem.toFile(File("D:\\test\\and1.txt"), humanReadable = false)
    }

    test("keccak256 count all bad lines") {
        val msg = byteArrayOf(43, -41, 18, -104, -29, 71, -26, -52, -77, 125, -82, 85, -96, 0, 108, -45, 118, -98, 110, 47, -53, -85, 0, -18, 13, 98, 26, 69, -121, -84, -121, -45, 100)

        val hashResult = Keccak256EqSystemGenerator.INSTANCE.hash(msg, replaceRulesInverse = true, replacePadding = false)

        val normalizedSystem = hashResult.equationSystem.simplify()
        val invertedSystem = normalizedSystem.invertToXorSystem()

        val normSysCols = normalizedSystem.cols
        val invSystemRows = invertedSystem.rows
        val invSystemCols = invertedSystem.cols

        System.gc()

        val xorSystem = File("D:\\test\\updated_normalized_inverted_xor.txt").toXorEquationSystem(invSystemRows, invSystemCols, true)

        var i = 0
        var counter = 0
        while (i < xorSystem.rows) {
            val index0 = xorSystem.equations[i].nextSetBit(0)

            if (index0 >= normSysCols) {
                if (i + 1 < xorSystem.rows) {
                    val index1 = xorSystem.equations[i + 1].nextSetBit(0)

                    if (index0 + 1 == index1) {
                        xorSystem.equations[i].clear(index0)
                        xorSystem.equations[i + 1].clear(index1)

                        if (xorSystem.equations[i] == xorSystem.equations[i + 1]) {
                            counter++
                        }

                        i += 2
                    } else {
                        i++
                    }
                } else {
                    i++
                }
            } else {
                i++
            }
        }

        println("Eqs: $counter")
    }

    test("find first 3200 nodes") {
        val msg = byteArrayOf(43, -41, 18, -104, -29, 71, -26, -52, -77, 125, -82, 85, -96, 0, 108, -45, 118, -98, 110, 47, -53, -85, 0, -18, 13, 98, 26, 69, -121, -84, -121, -45, 100)

        val hashResult = Keccak256EqSystemGenerator.INSTANCE.hash(msg, replaceRulesInverse = true, replacePadding = false)

        val normalizedSystem = hashResult.equationSystem.simplify()

        var i = 0
        val nodes = LinkedList<Node>()
        while (i < 3200) {
            val lNode = normalizedSystem.equations[i].andOpLeft.toNode(normalizedSystem.andOpLeftResults[i])
            val rNode = normalizedSystem.equations[i].andOpRight.toNode(normalizedSystem.andOpRightResults[i])
            val and = And(lNode, rNode, Variable("a$i"))
            //val and = And(lNode, rNode, Bit(Random.nextBoolean()))
            nodes.add(and)
            //println(and)
            i++
        }

        val node = Xor(nodes)
        println("Init node")
        //println(node.expand())
        println()

        val expandedNode = Xor((node.expand() as Xor).nodes.asSequence().filter { it is And && it.nodes.size >= 3 }).groupBy("x")

        val nodeGroups = expandedNode.groups("x")

        /*for (nodeGroup in nodeGroups) {
            println(nodeGroup)
        }*/

        val system = XorEquationSystem(rows = nodeGroups.size, cols = node.countVariables("a"))

        nodeGroups.values.forEachIndexed { index, eq ->
            system.set(index, "$eq = 0", true)
        }

        val solved = system.solve()
        println("Solved: $solved")
        //println(system)
        println()
        i = 0
        while (i < system.rows) {
            println(system.equations[i].toXorString(system.results[i]))
            i++
        }
        println()
    }

    test("find collision").config(timeout = 10.days) {
        val msg = byteArrayOf(43, -41, 18, -104, -29, 71, -26, -52, -77, 125, -82, 85, -96, 0, 108, -45, 118, -98, 110, 47, -53, -85, 0, -18, 13, 98, 26, 69, -121, -84, -121, -45, 100)

        val hashResult = Keccak256EqSystemGenerator.INSTANCE.hash(msg, replaceRulesInverse = true, replacePadding = true)
        val normalizedSystem = hashResult.equationSystem.simplify()

        val algorithm = AndEquationSystem.PivotSolutionAlgorithm(normalizedSystem, hashResult.varValues)

        System.gc()

        val job = thread {
            val solutions = algorithm.solve(logProgress = true, progressStep = 64)

            if (solutions.isNotEmpty()) {
                for (solution in solutions) {
                    println(solution.toString(normalizedSystem.cols))
                }
            }
        }

        Runtime.getRuntime().addShutdownHook(Thread {
            job.interrupt()
            job.join()

            algorithm.invertedSystem.toFile(
                file = File("D:\\test\\solution.txt"),
                humanReadable = true,
            )
        })

        job.join()
    }
})
