package keccak

import io.kotest.core.spec.style.FunSpec
import kotlin.test.assertEquals

class AndEquationTest : FunSpec({
    context("variableExists") {
        test("1") {
            val varsCount = 3
            val eq = AndEquation(varsCount)
            eq.left.bitGroup[0] = true
            eq.left.bitGroup[1] = true
            eq.left.bitGroup[2] = true
            eq.left.result = true

            eq.right.bitGroup[0] = true
            eq.right.bitGroup[1] = true
            eq.right.bitGroup[2] = true
            eq.right.result = true

            assertEquals(true, eq.variableExists(0, 0))
            assertEquals(false, eq.variableExists(0, 1))
            assertEquals(false, eq.variableExists(0, 2))
            assertEquals(false, eq.variableExists(1, 0))
            assertEquals(true, eq.variableExists(1, 1))
            assertEquals(false, eq.variableExists(1, 2))
            assertEquals(false, eq.variableExists(2, 0))
            assertEquals(false, eq.variableExists(2, 1))
            assertEquals(true, eq.variableExists(2, 2))
        }

        test("2") {
            val varsCount = 3
            val eq = AndEquation(varsCount)
            eq.left.bitGroup[0] = true
            eq.left.bitGroup[1] = true
            eq.left.bitGroup[2] = true
            eq.left.result = true

            eq.right.bitGroup[0] = false
            eq.right.bitGroup[1] = true
            eq.right.bitGroup[2] = false
            eq.right.result = false

            assertEquals(false, eq.variableExists(0, 0))
            assertEquals(true, eq.variableExists(0, 1))
            assertEquals(false, eq.variableExists(0, 2))
            assertEquals(true, eq.variableExists(1, 0))
            assertEquals(false, eq.variableExists(1, 1))
            assertEquals(true, eq.variableExists(1, 2))
            assertEquals(false, eq.variableExists(2, 0))
            assertEquals(true, eq.variableExists(2, 1))
            assertEquals(false, eq.variableExists(2, 2))
        }
    }
})
