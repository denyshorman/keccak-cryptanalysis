package keccak

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import keccak.util.BitSet
import keccak.util.XorEquationSystem
import keccak.util.randomXorEquationSystem
import keccak.util.toString
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.*

class XorEquationSystemTest : FunSpec({
    context("evaluate") {
        test("1") {
            val rows = 2
            val cols = 2
            val eqSystem = XorEquationSystem(rows, cols)
            val vars = BitSet(cols)

            vars[0] = true
            vars[1] = true

            eqSystem.equations[0][0] = true
            eqSystem.equations[0][1] = false
            eqSystem.results[0] = true

            eqSystem.equations[1][0] = false
            eqSystem.equations[1][1] = true
            eqSystem.results[1] = true

            val expectedEqSystem = XorEquationSystem(rows, cols)
            expectedEqSystem.equations[0][0] = false
            expectedEqSystem.equations[0][1] = false
            expectedEqSystem.results[0] = false

            expectedEqSystem.equations[1][0] = false
            expectedEqSystem.equations[1][1] = false
            expectedEqSystem.results[1] = false

            eqSystem.evaluate(vars)

            assertEquals(expectedEqSystem, eqSystem)
        }

        test("2") {
            val rows = 2
            val cols = 2
            val eqSystem = XorEquationSystem(rows, cols)
            val vars = BitSet(cols)

            vars[0] = true
            vars[1] = true

            eqSystem.equations[0][0] = false
            eqSystem.equations[0][1] = false
            eqSystem.results[0] = true

            eqSystem.equations[1][0] = true
            eqSystem.equations[1][1] = true
            eqSystem.results[1] = false

            val expectedEqSystem = XorEquationSystem(rows, cols)
            expectedEqSystem.equations[0][0] = false
            expectedEqSystem.equations[0][1] = false
            expectedEqSystem.results[0] = true

            expectedEqSystem.equations[1][0] = false
            expectedEqSystem.equations[1][1] = false
            expectedEqSystem.results[1] = false

            eqSystem.evaluate(vars)

            assertEquals(expectedEqSystem, eqSystem)
        }
    }

    context("partiallyEvaluate") {
        test("1") {
            val rows = 2
            val cols = 2

            val varValues = BitGroup(cols)

            varValues[0] = true
            varValues[1] = true

            val availableVars = BitGroup(cols)

            availableVars[0] = true
            availableVars[1] = true

            val eqSystem = XorEquationSystem(rows, cols)

            eqSystem.equations[0][0] = true
            eqSystem.equations[0][1] = false
            eqSystem.results[0] = true

            eqSystem.equations[1][0] = false
            eqSystem.equations[1][1] = true
            eqSystem.results[1] = true

            val expectedEqSystem = XorEquationSystem(rows, cols)

            expectedEqSystem.equations[0][0] = false
            expectedEqSystem.equations[0][1] = false
            expectedEqSystem.results[0] = false

            expectedEqSystem.equations[1][0] = false
            expectedEqSystem.equations[1][1] = false
            expectedEqSystem.results[1] = false

            eqSystem.partiallyEvaluate(varValues.bitSet, availableVars.bitSet)

            assertEquals(expectedEqSystem, eqSystem)
        }

        test("2") {
            val rows = 2
            val cols = 2

            val varValues = BitGroup(cols)

            varValues[0] = true
            varValues[1] = true

            val availableVars = BitGroup(cols)

            availableVars[0] = false
            availableVars[1] = false

            val eqSystem = XorEquationSystem(rows, cols)

            eqSystem.equations[0][0] = true
            eqSystem.equations[0][1] = false
            eqSystem.results[0] = true

            eqSystem.equations[1][0] = false
            eqSystem.equations[1][1] = true
            eqSystem.results[1] = true

            val expectedEqSystem = XorEquationSystem(rows, cols)

            expectedEqSystem.equations[0][0] = true
            expectedEqSystem.equations[0][1] = false
            expectedEqSystem.results[0] = true

            expectedEqSystem.equations[1][0] = false
            expectedEqSystem.equations[1][1] = true
            expectedEqSystem.results[1] = true

            eqSystem.partiallyEvaluate(varValues.bitSet, availableVars.bitSet)

            assertEquals(expectedEqSystem, eqSystem)
        }

        test("3") {
            val rows = 2
            val cols = 2

            val varValues = BitGroup(cols)

            varValues[0] = true
            varValues[1] = true

            val availableVars = BitGroup(cols)

            availableVars[0] = false
            availableVars[1] = true

            val eqSystem = XorEquationSystem(rows, cols)

            eqSystem.equations[0][0] = true
            eqSystem.equations[0][1] = false
            eqSystem.results[0] = true

            eqSystem.equations[1][0] = false
            eqSystem.equations[1][1] = true
            eqSystem.results[1] = true

            val expectedEqSystem = XorEquationSystem(rows, cols)

            expectedEqSystem.equations[0][0] = true
            expectedEqSystem.equations[0][1] = false
            expectedEqSystem.results[0] = true

            expectedEqSystem.equations[1][0] = false
            expectedEqSystem.equations[1][1] = false
            expectedEqSystem.results[1] = false

            eqSystem.partiallyEvaluate(varValues.bitSet, availableVars.bitSet)

            assertEquals(expectedEqSystem, eqSystem)
        }
    }

    context("solutionIterator") {
        test("one solution") {
            val system = XorEquationSystem(rows = 4, cols = 4,
            "1000|1",
            "0100|0",
            "0010|1",
            "0001|0",
            )

            val solIter = system.solutionIterator()

            solIter.solution.toString(system.cols).shouldBe("0000")
            solIter.iterator.mask.toString(system.cols).shouldBe("0000")
            solIter.iterator.combination.toString(system.cols).shouldBe("0000")
            solIter.iterator.solutionsCount.shouldBe(1)
            solIter.iterator.solutionIndex.shouldBe(-1)

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.cols).shouldBe("1010")
            solIter.iterator.mask.toString(system.cols).shouldBe("0000")
            solIter.iterator.combination.toString(system.cols).shouldBe("0000")
            solIter.iterator.solutionsCount.shouldBe(1)
            solIter.iterator.solutionIndex.shouldBe(0)
            system.isValid(solIter.solution).shouldBeTrue()

            solIter.hasNext().shouldBeFalse()
        }

        test("two solutions") {
            val system = XorEquationSystem(rows = 4, cols = 4,
                "1001|1",
                "0101|0",
                "0010|1",
                "0000|0",
            )

            val solIter = system.solutionIterator()

            solIter.solution.toString(system.cols).shouldBe("0000")
            solIter.iterator.mask.toString(system.cols).shouldBe("0001")
            solIter.iterator.combination.toString(system.cols).shouldBe("0000")
            solIter.iterator.solutionsCount.shouldBe(2)
            solIter.iterator.solutionIndex.shouldBe(-1)

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.cols).shouldBe("1010")
            solIter.iterator.combination.toString(system.cols).shouldBe("0001")
            solIter.iterator.solutionIndex.shouldBe(0)
            system.isValid(solIter.solution).shouldBeTrue()

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.cols).shouldBe("0111")
            solIter.iterator.combination.toString(system.cols).shouldBe("0000")
            solIter.iterator.solutionIndex.shouldBe(1)
            system.isValid(solIter.solution).shouldBeTrue()

            solIter.hasNext().shouldBeFalse()
        }

        test("10x12") {
            val system = XorEquationSystem(rows = 10, cols = 12,
                "100000000011|1",
                "010010010001|0",
                "001000000010|1",
                "000110010010|1",
                "000000000000|0",
                "000001010001|1",
                "000000100001|1",
                "000000000000|0",
                "000000001011|0",
                "000000000101|1",
            )

            val solIter = system.solutionIterator()

            solIter.solution.toString(system.cols).shouldBe("000000000000")
            solIter.iterator.mask.toString(system.cols).shouldBe("000010010011")
            solIter.iterator.combination.toString(system.cols).shouldBe("000000000000")
            solIter.iterator.solutionsCount.shouldBe(16)
            solIter.iterator.solutionIndex.shouldBe(-1)

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.cols).shouldBe("101101100100")
            solIter.iterator.combination.toString(system.cols).shouldBe("000000000001")
            solIter.iterator.solutionIndex.shouldBe(0)
            system.isValid(solIter.solution).shouldBeTrue()

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.cols).shouldBe("011100001001")
            solIter.iterator.combination.toString(system.cols).shouldBe("000000000010")
            solIter.iterator.solutionIndex.shouldBe(1)
            system.isValid(solIter.solution).shouldBeTrue()

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.cols).shouldBe("000001101110")
            solIter.iterator.combination.toString(system.cols).shouldBe("000000000011")
            solIter.iterator.solutionIndex.shouldBe(2)
            system.isValid(solIter.solution).shouldBeTrue()

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.cols).shouldBe("110000000011")
            solIter.iterator.combination.toString(system.cols).shouldBe("000000010000")
            solIter.iterator.solutionIndex.shouldBe(3)
            system.isValid(solIter.solution).shouldBeTrue()

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.cols).shouldBe("111000110100")
            solIter.iterator.combination.toString(system.cols).shouldBe("000000010001")
            solIter.iterator.solutionIndex.shouldBe(4)
            system.isValid(solIter.solution).shouldBeTrue()

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.cols).shouldBe("001001011001")
            solIter.iterator.combination.toString(system.cols).shouldBe("000000010010")
            solIter.iterator.solutionIndex.shouldBe(5)
            system.isValid(solIter.solution).shouldBeTrue()

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.cols).shouldBe("010100111110")
            solIter.iterator.combination.toString(system.cols).shouldBe("000000010011")
            solIter.iterator.solutionIndex.shouldBe(6)
            system.isValid(solIter.solution).shouldBeTrue()

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.cols).shouldBe("100101010011")
            solIter.iterator.combination.toString(system.cols).shouldBe("000010000000")
            solIter.iterator.solutionIndex.shouldBe(7)
            system.isValid(solIter.solution).shouldBeTrue()

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.cols).shouldBe("111011100100")
            solIter.iterator.combination.toString(system.cols).shouldBe("000010000001")
            solIter.iterator.solutionIndex.shouldBe(8)
            system.isValid(solIter.solution).shouldBeTrue()

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.cols).shouldBe("001010001001")
            solIter.iterator.combination.toString(system.cols).shouldBe("000010000010")
            solIter.iterator.solutionIndex.shouldBe(9)
            system.isValid(solIter.solution).shouldBeTrue()

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.cols).shouldBe("010111101110")
            solIter.iterator.combination.toString(system.cols).shouldBe("000010000011")
            solIter.iterator.solutionIndex.shouldBe(10)
            system.isValid(solIter.solution).shouldBeTrue()

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.cols).shouldBe("100110000011")
            solIter.iterator.combination.toString(system.cols).shouldBe("000010010000")
            solIter.iterator.solutionIndex.shouldBe(11)
            system.isValid(solIter.solution).shouldBeTrue()

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.cols).shouldBe("101110110100")
            solIter.iterator.combination.toString(system.cols).shouldBe("000010010001")
            solIter.iterator.solutionIndex.shouldBe(12)
            system.isValid(solIter.solution).shouldBeTrue()

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.cols).shouldBe("011111011001")
            solIter.iterator.combination.toString(system.cols).shouldBe("000010010010")
            solIter.iterator.solutionIndex.shouldBe(13)
            system.isValid(solIter.solution).shouldBeTrue()

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.cols).shouldBe("000010111110")
            solIter.iterator.combination.toString(system.cols).shouldBe("000010010011")
            solIter.iterator.solutionIndex.shouldBe(14)
            system.isValid(solIter.solution).shouldBeTrue()

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.cols).shouldBe("110011010011")
            solIter.iterator.combination.toString(system.cols).shouldBe("000000000000")
            solIter.iterator.solutionIndex.shouldBe(15)
            system.isValid(solIter.solution).shouldBeTrue()

            solIter.hasNext().shouldBeFalse()
        }
    }

    context("hasSolution") {
        test("1") {
            val system = XorEquationSystem(
                rows = 4, cols = 4,
                "1000|1",
                "0100|0",
                "0010|1",
                "0001|0",
            )

            system.hasSolution().shouldBeTrue()
        }

        test("2") {
            val system = XorEquationSystem(
                rows = 4, cols = 4,
                "1000|1",
                "1000|0",
                "0010|1",
                "0001|0",
            )

            system.hasSolution().shouldBeFalse()
        }

        test("3") {
            val system = randomXorEquationSystem(5, 5)
            val clonedSystem = system.clone()

            println("-----------")
            println(system.toString())
            println("-----------")

            val solved = system.solve()
            val hasSolution = clonedSystem.hasSolution()

            println()
            println("-----------")
            println(system.toString())
            println("-----------")
            println("-----------")
            println(clonedSystem.toString())
            println("-----------")

            solved.shouldBe(hasSolution)

            println()
            println("----")
            if (solved) {
                println("has solution")
            } else {
                println("no solution")
            }
            println("----")
        }

        test("4") {
            val system = XorEquationSystem(
                5, 5,
                "01000|1",
                "01100|0",
                "01001|1",
                "10000|0",
                "00001|1",
            )
            val clonedSystem = system.clone()

            println("-----------")
            println(system.toString())
            println("-----------")

            val solved = system.solve()
            val hasSolution = clonedSystem.hasSolution()

            println()
            println("-----------")
            println(system.toString())
            println("-----------")
            println("-----------")
            println(clonedSystem.toString())
            println("-----------")

            solved.shouldBe(hasSolution)

            println()
            println("----")
            if (solved) {
                println("has solution")
            } else {
                println("no solution")
            }
            println("----")
        }
    }

    context("solve") {
        test("1") {
            val system = XorEquationSystem(
                rows = 10, cols = 15,
                "110100011101011|1",
                "011011110101100|1",
                "011100000010000|0",
                "101011111000111|1",
                "101011000010000|1",
                "101100011011101|0",
                "100101011100100|1",
                "000011111001100|0",
                "011100101011101|0",
                "101011111000011|1",
            )

            val solved = system.solve()
            solved.shouldBeTrue()

            val expected = """
                100001000001010|0
                010001000001011|0
                001001000000001|1
                000100000011010|1
                000011000011011|0
                000000100011011|0
                000000010011010|0
                000000001010010|0
                000000000111000|0
                000000000000100|0
            """.trimIndent()

            system.toString().shouldBe(expected)
        }

        test("2") {
            val system = randomXorEquationSystem(6, 6)

            println(system)
            println()

            val solved = system.solve()

            println(system)
            println()

            println("Solved: $solved")
        }

        test("3") {
            val system = XorEquationSystem(5, 5,
                "01111|1",
                "10010|0",
                "00100|1",
                "10011|0",
                "00111|1",
            )
            val system2 = system.clone()

            system.solve(rowsMask = BitSet("11111"), colsMask = BitSet("11111"))
            system2.solve()

            system.shouldBe(system2)
        }

        test("4") {
            val system = randomXorEquationSystem(6, 6)
            val system2 = system.clone()

            println(system)
            println()

            val solved = system.solve(rowsMask = BitSet("110011"), colsMask = BitSet("110011"))
            val solved2 = system2.solve()

            println(system)
            println()

            println(system2)
            println()

            println("Solved: $solved")
            println("Solved2: $solved2")
        }
    }
})
