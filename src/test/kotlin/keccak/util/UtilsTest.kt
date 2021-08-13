package keccak.util

import io.kotest.core.spec.style.FunSpec
import keccak.XorEquation
import kotlin.test.assertEquals

class UtilsTest : FunSpec({
    context("calcCombinationIndex") {
        test("1") {
            assertEquals(0, calcCombinationIndex(0, 0, 4))
            assertEquals(1, calcCombinationIndex(0, 1, 4))
            assertEquals(2, calcCombinationIndex(0, 2, 4))
            assertEquals(3, calcCombinationIndex(0, 3, 4))
            assertEquals(4, calcCombinationIndex(1, 1, 4))
            assertEquals(5, calcCombinationIndex(1, 2, 4))
            assertEquals(6, calcCombinationIndex(1, 3, 4))
            assertEquals(7, calcCombinationIndex(2, 2, 4))
            assertEquals(8, calcCombinationIndex(2, 3, 4))
            assertEquals(9, calcCombinationIndex(3, 3, 4))
        }

        test("2") {
            assertEquals(0, calcCombinationIndex(0, 4))
            assertEquals(4, calcCombinationIndex(1, 4))
            assertEquals(7, calcCombinationIndex(2, 4))
            assertEquals(9, calcCombinationIndex(3, 4))
        }
    }

    context("additionalEqToBitSystem") {
        test("1") {
            val varsCount = 3

            val eq0 = XorEquation(varsCount)
            val eq1 = XorEquation(varsCount)

            eq0.setVariable(0)
            eq0.setVariable(1)
            eq0.setBit(true)

            eq1.setVariable(1)
            eq1.setVariable(2)
            eq1.setBit(true)

            val eqSystem = listOf(Pair(eq0, eq1))

            val newSystem = eqSystem.additionalEqToBitSystem(varsCount, 0)

            assertEquals("111010|0", newSystem.toString())
        }

        test("2") {
            val varsCount = 3

            val eq0 = XorEquation(varsCount)
            val eq1 = XorEquation(varsCount)

            eq0.setVariable(0)
            eq0.setVariable(1)
            eq0.setBit(false)

            eq1.setVariable(0)
            eq1.setVariable(1)
            eq1.setBit(true)

            val eqSystem = listOf(Pair(eq0, eq1))

            val newSystem = eqSystem.additionalEqToBitSystem(varsCount, 0)
            assertEquals("100100|1", newSystem.toString())
        }
    }
})
