package keccak

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import keccak.util.XorEquationSystem
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

            solIter.solution.toString(system.rows).shouldBe("0000")
            solIter.mask.toString(system.cols).shouldBe("0000")
            solIter.iterator.toString(system.cols).shouldBe("0000")
            solIter.solutionsCount.shouldBe(1)
            solIter.solutionIndex.shouldBe(-1)

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.rows).shouldBe("1010")
            solIter.mask.toString(system.cols).shouldBe("0000")
            solIter.iterator.toString(system.cols).shouldBe("0000")
            solIter.solutionsCount.shouldBe(1)
            solIter.solutionIndex.shouldBe(0)

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

            solIter.solution.toString(system.rows).shouldBe("0000")
            solIter.mask.toString(system.cols).shouldBe("0001")
            solIter.iterator.toString(system.cols).shouldBe("0000")
            solIter.solutionsCount.shouldBe(2)
            solIter.solutionIndex.shouldBe(-1)

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.rows).shouldBe("1010")
            solIter.iterator.toString(system.cols).shouldBe("0001")
            solIter.solutionIndex.shouldBe(0)

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.rows).shouldBe("0111")
            solIter.iterator.toString(system.cols).shouldBe("0000")
            solIter.solutionIndex.shouldBe(1)

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

            solIter.solution.toString(system.rows).shouldBe("0000000000")
            solIter.mask.toString(system.cols).shouldBe("000010010011")
            solIter.iterator.toString(system.cols).shouldBe("000000000000")
            solIter.solutionsCount.shouldBe(16)
            solIter.solutionIndex.shouldBe(-1)

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.rows).shouldBe("1011011001")
            solIter.iterator.toString(system.cols).shouldBe("000000000001")
            solIter.solutionIndex.shouldBe(0)

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.rows).shouldBe("0111000010")
            solIter.iterator.toString(system.cols).shouldBe("000000000010")
            solIter.solutionIndex.shouldBe(1)

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.rows).shouldBe("0000011011")
            solIter.iterator.toString(system.cols).shouldBe("000000000011")
            solIter.solutionIndex.shouldBe(2)

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.rows).shouldBe("1100000000")
            solIter.iterator.toString(system.cols).shouldBe("000000010000")
            solIter.solutionIndex.shouldBe(3)

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.rows).shouldBe("1110001101")
            solIter.iterator.toString(system.cols).shouldBe("000000010001")
            solIter.solutionIndex.shouldBe(4)

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.rows).shouldBe("0010010110")
            solIter.iterator.toString(system.cols).shouldBe("000000010010")
            solIter.solutionIndex.shouldBe(5)

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.rows).shouldBe("0101001111")
            solIter.iterator.toString(system.cols).shouldBe("000000010011")
            solIter.solutionIndex.shouldBe(6)

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.rows).shouldBe("1001010100")
            solIter.iterator.toString(system.cols).shouldBe("000010000000")
            solIter.solutionIndex.shouldBe(7)

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.rows).shouldBe("1110111001")
            solIter.iterator.toString(system.cols).shouldBe("000010000001")
            solIter.solutionIndex.shouldBe(8)

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.rows).shouldBe("0010100010")
            solIter.iterator.toString(system.cols).shouldBe("000010000010")
            solIter.solutionIndex.shouldBe(9)

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.rows).shouldBe("0101111011")
            solIter.iterator.toString(system.cols).shouldBe("000010000011")
            solIter.solutionIndex.shouldBe(10)

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.rows).shouldBe("1001100000")
            solIter.iterator.toString(system.cols).shouldBe("000010010000")
            solIter.solutionIndex.shouldBe(11)

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.rows).shouldBe("1011101101")
            solIter.iterator.toString(system.cols).shouldBe("000010010001")
            solIter.solutionIndex.shouldBe(12)

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.rows).shouldBe("0111110110")
            solIter.iterator.toString(system.cols).shouldBe("000010010010")
            solIter.solutionIndex.shouldBe(13)

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.rows).shouldBe("0000101111")
            solIter.iterator.toString(system.cols).shouldBe("000010010011")
            solIter.solutionIndex.shouldBe(14)

            solIter.hasNext().shouldBeTrue()
            solIter.next()

            solIter.solution.toString(system.rows).shouldBe("1100110100")
            solIter.iterator.toString(system.cols).shouldBe("000000000000")
            solIter.solutionIndex.shouldBe(15)

            solIter.hasNext().shouldBeFalse()
        }
    }
})
