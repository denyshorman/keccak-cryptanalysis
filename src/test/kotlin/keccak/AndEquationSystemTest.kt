package keccak

import io.kotest.core.spec.style.FunSpec
import keccak.util.AndEquationSystem
import keccak.util.randomAndEquationSystem
import keccak.util.toString
import keccak.util.toXorEquationSystem
import kotlin.test.assertEquals
import kotlin.test.fail

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

            val solved = xorEqSystem.solve()

            if (!solved) {
                println()
                println("Partially solved system")
                println(xorEqSystem)
                fail("No solution")
            }

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

            xorEqSystem.solve()

            println()
            println("Equation system solution")
            println(xorEqSystem)

            assertEquals(false, xorEqSystem.results[0])
            assertEquals(true, xorEqSystem.results[1])
            assertEquals(false, xorEqSystem.results[2])
        }

        test("3_two_solutions") {
            val andEqSystem = AndEquationSystem(
                rows = 4, cols = 4,
                "(0010|1)(0111|0) = 1000|0",
                "(0101|0)(0110|0) = 0100|0",
                "(0011|1)(0000|0) = 0010|0",
                "(1000|0)(0010|0) = 0001|0",
            )

            println()
            println("Equation system")
            println(andEqSystem)

            val xorEqSystem = andEqSystem.toXorEquationSystem()

            println()
            println("Equation system converted to xor")
            println(xorEqSystem)

            xorEqSystem.solve()

            println()
            println("Equation system solution")
            println(xorEqSystem)

            assertEquals(true, xorEqSystem.results[0])
            assertEquals(true, xorEqSystem.results[1])
            assertEquals(false, xorEqSystem.results[2])
            assertEquals(false, xorEqSystem.results[3])
        }

        test("4") {
            val andEqSystem = AndEquationSystem(
                rows = 4, cols = 4,
                "(1011|1)(0110|0) = 1000|0",
                "(0001|0)(0100|0) = 0100|0",
                "(1011|1)(1001|0) = 0010|0",
                "(1101|0)(1100|1) = 0001|0",
            )

            println()
            println("Equation system")
            println(andEqSystem)

            val xorEqSystem = andEqSystem.toXorEquationSystem()

            println()
            println("Equation system converted to xor")
            println(xorEqSystem)

            xorEqSystem.solve()

            println()
            println("Equation system solution")
            println(xorEqSystem)

            assertEquals(true, xorEqSystem.results[0])
            assertEquals(true, xorEqSystem.results[1])
            assertEquals(false, xorEqSystem.results[2])
            assertEquals(true, xorEqSystem.results[3])
        }

        test("5_two_solutions") {
            val andEqSystem = AndEquationSystem(
                rows = 4, cols = 4,
                "(1001|0)(0111|1) = 1000|0",
                "(0010|1)(1011|1) = 0100|0",
                "(0010|1)(0100|0) = 0010|0",
                "(1001|0)(0001|0) = 0001|0",
            )

            println()
            println("Equation system")
            println(andEqSystem)

            val xorEqSystem = andEqSystem.toXorEquationSystem()

            println()
            println("Equation system converted to xor")
            println(xorEqSystem)

            xorEqSystem.solve()

            println()
            println("Equation system solution")
            println(xorEqSystem)

            assertEquals(true, xorEqSystem.results[0])
            assertEquals(false, xorEqSystem.results[1])
            assertEquals(false, xorEqSystem.results[2])
            assertEquals(false, xorEqSystem.results[3])
        }

        test("random") {
            val rows = 20
            val cols = 20
            val (solution, andEqSystem) = randomAndEquationSystem(rows, cols)

            println()
            println("Equation system")
            println(andEqSystem)

            println()
            println("Solution")
            println(solution.toString(cols))

            val xorEqSystem = andEqSystem.toXorEquationSystem()

            println()
            println("Equation system converted to xor")
            println(xorEqSystem)

            xorEqSystem.solve()

            println()
            println("Equation system solution")
            println(xorEqSystem)

            var i = 0
            while (i < rows) {
                assertEquals(solution[i], xorEqSystem.results[i])
                i++
            }
        }

        xtest("random zero eqs verification") {
            while (true) {
                val rows = 10
                val cols = 10
                val (solution, andEqSystem) = randomAndEquationSystem(rows, cols)

                val xorEqSystem = andEqSystem.toXorEquationSystem()
                val xorEqSystemInitial = xorEqSystem.clone()

                xorEqSystem.solve()

                var j = cols
                var found = false
                while (j < rows * 2) {
                    if (xorEqSystem.equations[j].isEmpty && xorEqSystem.equations[j + 1].isEmpty) {
                        found = true
                        break
                    }
                    j += 2
                }

                if (!found) {
                    continue
                }

                println()
                println("Equation system")
                println(andEqSystem)

                println()
                println("Solution")
                println(solution.toString(cols))

                println()
                println("Equation system converted to xor")
                println(xorEqSystemInitial)

                println()
                println("Equation system solution")
                println(xorEqSystem)

                break
            }
        }

        xtest("random zero eqs count") {
            while (true) {
                val rows = 20
                val cols = 20
                val (_, andEqSystem) = randomAndEquationSystem(rows, cols)

                val xorEqSystem = andEqSystem.toXorEquationSystem()
                // val xorEqSystemInitial = xorEqSystem.clone()

                xorEqSystem.solve()

                var j = cols
                var zeroCounter = 0
                while (j < rows * 2) {
                    if (xorEqSystem.equations[j].isEmpty) {
                        zeroCounter++
                    }
                    j++
                }

                if (zeroCounter > 1) println(zeroCounter)

                /*println()
                println("Equation system")
                println(andEqSystem)

                println()
                println("Solution")
                println(solution.toString(cols))

                println()
                println("Equation system converted to xor")
                println(xorEqSystemInitial)

                println()
                println("Equation system solution")
                println(xorEqSystem)

                break*/
            }
        }
    }
})
