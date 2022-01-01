package keccak

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import keccak.util.*

class AndEquationSystemTest : FunSpec({
    context("set") {
        test("1") {
            val system = AndEquationSystem(1, 5)
            system.set(0, "(v0)*(v1 + 1) = 1 + v2", true)
            system.toString().shouldBe("(10000|0)(01000|1) = 00100|1")
        }

        test("2") {
            val system = AndEquationSystem(1, 5)
            system.set(0, "(v0 + v3 + 1)*(v1 + 1 + 1) = 1 + v2", true)
            system.toString().shouldBe("(10010|1)(01000|0) = 00100|1")
        }

        test("3") {
            val system = AndEquationSystem(1, 5)
            system.set(0, "(10010|1)(01000|0) = 00100|1", false)
            system.toString().shouldBe("(10010|1)(01000|0) = 00100|1")
        }
    }

    context("solve") {
        context("one solution") {
            test("1") {
                val andSystem = AndEquationSystem(
                    rows = 6, cols = 6,
                    humanReadable = false,
                    "(111101|0)(111101|0) = 000000|0",
                    "(110111|0)(001110|0) = 000000|0",
                    "(111011|1)(010110|0) = 000000|0",
                    "(101111|1)(111001|0) = 000000|0",
                    "(111110|1)(111011|0) = 000000|0",
                    "(111101|1)(010110|1) = 000000|0",
                )

                val solutionSystem = andSystem.solve()

                solutionSystem.shouldNotBeNull()
                solutionSystem.results.toString(andSystem.cols).shouldBe("101010")

                println("Solution")
                println(solutionSystem)
            }
        }

        test("2") {
            val andSystem = AndEquationSystem(
                rows = 6, cols = 6,
                humanReadable = false,
                "(011100|0)(111100|1) = 000000|0",
                "(110110|1)(000101|1) = 000000|0",
                "(101100|1)(010101|1) = 000000|0",
                "(000100|0)(101001|1) = 000000|0",
                "(011100|1)(011100|1) = 000000|0",
                "(110001|0)(101101|1) = 000000|0",
            )

            val solutionSystem = andSystem.solve()

            solutionSystem.shouldNotBeNull()
            solutionSystem.results.toString(andSystem.cols).shouldBe("001010")

            println("Solution")
            println(solutionSystem)
        }

        test("3") {
            while (true) {
                val (solution, andSystem) = randomAndEquationSystem(
                    rows = 6,
                    cols = 6,
                    allowIncompatibleSystem = false,
                    solutionsCount = 1,
                    equalToZero = true,
                )

                println(andSystem)
                println()

                val solutionSystem = andSystem.solve()

                solutionSystem.shouldNotBeNull()
                solutionSystem.results.shouldBe(solution)

                println("Solution")
                println(solutionSystem)
                println()
            }
        }

        test("4") {
            while (true) {
                val (solution, andSystem) = randomAndEquationSystem(
                    rows = 6,
                    cols = 6,
                    allowIncompatibleSystem = false,
                    solutionsCount = 1,
                    equalToZero = false,
                )

                println(andSystem)
                println()

                println(andSystem.toNodeEquationSystem(varOffset = 0))
                println()

                val normalizedAndSystem = andSystem.simplify()

                println(normalizedAndSystem)
                println()

                val invertedXorSystem = normalizedAndSystem.invertToXorSystem()
                invertedXorSystem.solve()

                println(invertedXorSystem.toNodeEquationSystem(varOffset = 0))
                println()

                val solutionSystem = normalizedAndSystem.solve()

                solutionSystem.shouldNotBeNull()
                solutionSystem.results.shouldBe(solution)

                println("Solution")
                println(solutionSystem)
                println()

                break
            }
        }

        test("5") {
            val andSystem = AndEquationSystem(
                rows = 12,
                cols = 6,
                humanReadable = false,
                "(011110|1)(001000|1) = 000000|0",
                "(100000|0)(011110|0) = 000000|0",
                "(101101|0)(101111|1) = 000000|0",
                "(010000|0)(101101|1) = 000000|0",
                "(011001|1)(010000|1) = 000000|0",
                "(001000|0)(011001|0) = 000000|0",
                "(101011|1)(110001|1) = 000000|0",
                "(000100|0)(101011|0) = 000000|0",
                "(110100|1)(110001|0) = 000000|0",
                "(000010|0)(110100|0) = 000000|0",
                "(101111|0)(010001|1) = 000000|0",
                "(000001|0)(101111|1) = 000000|0",
            )

            println("Solutions: ${andSystem.countSolutions()}\n")
            println(andSystem.invertToXorSystem().toNodeEquationSystem(varOffset = 0))
            println()

            val solutionSystem = andSystem.solve(logProgress = true)

            solutionSystem.shouldNotBeNull()
            //solutionSystem.results.toString(andSystem.cols).shouldBe("001010")

            println("Solution")
            println(solutionSystem)
        }

        test("6") {
            // solved manually
            val andSystem = AndEquationSystem(
                rows = 12,
                cols = 6,
                humanReadable = false,
                "(100000|1)(010000|0) = 000000|0",
                "(100000|0)(100000|0) = 000000|0",
                "(010110|0)(110011|0) = 000000|0",
                "(010000|0)(010110|1) = 000000|0",
                "(000001|0)(001010|1) = 000000|0",
                "(001000|0)(000001|1) = 000000|0",
                "(001000|1)(001011|1) = 000000|0",
                "(000100|0)(001000|0) = 000000|0",
                "(011011|1)(111010|1) = 000000|0",
                "(000010|0)(011011|0) = 000000|0",
                "(011000|0)(001100|1) = 000000|0",
                "(000001|0)(011000|1) = 000000|0",
            )

            println("Solutions: ${andSystem.countSolutions()}\n")
            println(andSystem.invertToXorSystem().toNodeEquationSystem(varOffset = 0))
            println()

            val solutionSystem = andSystem.solve(logProgress = true)

            solutionSystem.shouldNotBeNull()
            //solutionSystem.results.toString(andSystem.cols).shouldBe("001010")

            println("Solution")
            println(solutionSystem)
        }

        test("7") {
            val andSystem = AndEquationSystem(
                rows = 12,
                cols = 6,
                humanReadable = false,
                "(000100|0)(011100|0) = 000000|0",
                "(100000|0)(000100|1) = 000000|0",
                "(111101|0)(111101|1) = 000000|0",
                "(010000|0)(111101|1) = 000000|0",
                "(010110|1)(101101|1) = 000000|0",
                "(001000|0)(010110|0) = 000000|0",
                "(011001|1)(100001|1) = 000000|0",
                "(000100|0)(011001|0) = 000000|0",
                "(111011|0)(011100|0) = 000000|0",
                "(000010|0)(111011|1) = 000000|0",
                "(001011|1)(101001|1) = 000000|0",
                "(000001|0)(001011|0) = 000000|0",
            )
            val andNodeSystem = andSystem.toNodeEquationSystem(varOffset = 0)
            println(andNodeSystem)
            println()

            println("Solutions: ${andSystem.countSolutions()}\n")
            val invertedSystem = andSystem.invertToXorSystem()
            invertedSystem.solve()
            println(invertedSystem.toNodeEquationSystem(varOffset = 0))
            println()
            andNodeSystem.equations.forEach { eq ->
                println("${eq.left.expand()} = ${eq.right.expand()}")
            }
            println()

            val solutionSystem = andSystem.solve(logProgress = true)

            solutionSystem.shouldNotBeNull()
            //solutionSystem.results.toString(andSystem.cols).shouldBe("001010")

            println("Solution")
            println(solutionSystem)
        }
    }
})
