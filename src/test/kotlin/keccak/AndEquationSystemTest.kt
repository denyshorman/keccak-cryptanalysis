package keccak

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import keccak.util.AndEquationSystem
import keccak.util.randomAndEquationSystem
import keccak.util.set
import keccak.util.toString
import keccak.util.toXorEquationSystem
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

            var validSolutions = 0
            val solIt = xorEqSystem.solutionIterator()
            while (solIt.hasNext()) {
                solIt.next()
                if (andEqSystem.isValid(solIt.solution)) {
                    validSolutions++
                }
            }

            println()
            println("Valid solutions: $validSolutions")

            validSolutions.shouldBeGreaterThan(0)
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

            var validSolutions = 0
            val solIt = xorEqSystem.solutionIterator()
            while (solIt.hasNext()) {
                solIt.next()
                if (andEqSystem.isValid(solIt.solution)) {
                    validSolutions++
                }
            }

            println()
            println("Valid solutions: $validSolutions")

            validSolutions.shouldBeGreaterThan(0)
        }

        test("3") {
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

            var validSolutions = 0
            val solIt = xorEqSystem.solutionIterator()
            while (solIt.hasNext()) {
                solIt.next()
                if (andEqSystem.isValid(solIt.solution)) {
                    validSolutions++
                }
            }

            println()
            println("Valid solutions: $validSolutions")

            validSolutions.shouldBeGreaterThan(0)
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

            var validSolutions = 0
            val solIt = xorEqSystem.solutionIterator()
            while (solIt.hasNext()) {
                solIt.next()
                if (andEqSystem.isValid(solIt.solution)) {
                    validSolutions++
                }
            }

            println()
            println("Valid solutions: $validSolutions")

            validSolutions.shouldBeGreaterThan(0)
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

            var validSolutions = 0
            val solIt = xorEqSystem.solutionIterator()
            while (solIt.hasNext()) {
                solIt.next()
                if (andEqSystem.isValid(solIt.solution)) {
                    validSolutions++
                }
            }

            println()
            println("Valid solutions: $validSolutions")

            validSolutions.shouldBeGreaterThan(0)
        }

        test("random") {
            val rows = 200
            val cols = 200
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

            val solutionsIter = xorEqSystem.solutionIterator()

            var validSolutions = 0
            while (solutionsIter.hasNext()) {
                solutionsIter.next()
                val validSolution = andEqSystem.isValid(solutionsIter.solution)

                if (validSolution) {
                    validSolutions++
                }
            }

            println()
            println("Valid solutions: $validSolutions")
        }

        test("random while") {
            while (true) {
                val rows = 200
                val cols = 200
                val (solution, andEqSystem) = randomAndEquationSystem(rows, cols)

                if (!andEqSystem.isValid(solution)) {
                    println("real solution is not valid")
                    break
                }

                val xorEqSystem = andEqSystem.toXorEquationSystem()

                xorEqSystem.solve()

                val solutionsIter = xorEqSystem.solutionIterator()

                var validSolutions = 0
                while (solutionsIter.hasNext()) {
                    solutionsIter.next()
                    val validSolution = andEqSystem.isValid(solutionsIter.solution)


                    if (validSolution) {
                        validSolutions++
                    }
                }

                if (validSolutions == 0) continue else {
                    println()
                    println("Valid solutions: $validSolutions")
                    break
                }
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

        test("random zero eqs count") {
            val map = HashMap<Int, Long>()
            val solutions = HashMap<Int, Long>()
            var counter = 0L

            while (true) {
                val rows = 200
                val cols = 200
                val (solution, andEqSystem) = randomAndEquationSystem(rows, cols)

                val xorEqSystem = andEqSystem.toXorEquationSystem()
                //val xorEqSystemInitial = xorEqSystem.clone()

                val solved = xorEqSystem.solve()
                if (!solved) {
                    map[-1] = map.getOrDefault(-1, 0) + 1
                    continue
                }

                var j = 0
                var zeroCounter = 0
                while (j < rows) {
                    if (xorEqSystem.equations[j].isEmpty) {
                        zeroCounter++
                    }
                    j++
                }

                val solIt = xorEqSystem.solutionIterator()
                var solutionCounter = 0
                while (solIt.hasNext()) {
                    solIt.next()
                    val valid = andEqSystem.isValid(solIt.solution)
                    if (valid) {
                        solutionCounter++
                    }
                }
                solutions[zeroCounter] = solutions.getOrDefault(zeroCounter, 0) + solutionCounter

                /*if (zeroCounter == 0) {
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
                }*/

                map[zeroCounter] = map.getOrDefault(zeroCounter, 0) + 1

                if (counter % 1000 == 0L) {
                    map.forEach { (t, u) ->
                        println("$t = $u")
                    }
                    println("---------")
                    solutions.forEach { (t, u) ->
                        println("$t = $u")
                    }
                    println("---------")
                }

                counter++

                continue
            }
        }
    }

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
})
