package keccak

import io.kotest.core.spec.style.FunSpec
import keccak.util.AndEquationSystem
import keccak.util.toXorEquationSystem
import kotlin.test.assertEquals

class AndEquationSystemTest : FunSpec({
    context("solveEquationSystem") {
        test("1") {
            val andEqSystem = AndEquationSystem(
                rows = 4, cols = 4,
                "(1010|0)(0011|0) = 1000|0",
                "(1110|0)(0101|1) = 0100|0",
                "(0111|0)(0110|0) = 0010|0",
                "(1111|1)(1010|1) = 0001|0",
            )

            println()
            println("Equation system")
            println(andEqSystem)

            val xorEqSystem = andEqSystem.toXorEquationSystem()

            println()
            println("Equation system converted to xor")
            println(xorEqSystem)

            solveXorEquationSystem(xorEqSystem)

            println()
            println("Equation system solution")
            println(xorEqSystem)

            assertEquals(false, xorEqSystem.results[0])
            assertEquals(true, xorEqSystem.results[1])
            assertEquals(false, xorEqSystem.results[2])
            assertEquals(true, xorEqSystem.results[3])
        }

        test("2") {
            val andEqSystem = AndEquationSystem(
                rows = 3, cols = 3,
                "(111|1)(111|0) = 100|0",
                "(011|0)(110|0) = 010|0",
                "(011|1)(100|1) = 001|0",
            )

            println()
            println("Equation system")
            println(andEqSystem)

            val xorEqSystem = andEqSystem.toXorEquationSystem()

            println()
            println("Equation system converted to xor")
            println(xorEqSystem)

            solveXorEquationSystem(xorEqSystem)

            println()
            println("Equation system solution")
            println(xorEqSystem)

            assertEquals(false, xorEqSystem.results[0])
            assertEquals(true, xorEqSystem.results[1])
            assertEquals(false, xorEqSystem.results[2])
        }
    }
})
