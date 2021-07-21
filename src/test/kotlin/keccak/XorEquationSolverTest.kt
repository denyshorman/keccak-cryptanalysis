package keccak

import io.kotest.core.spec.style.FunSpec
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.random.Random

class XorEquationSolverTest : FunSpec({
    test("EquationSolver3") {
        val variables = bitSet(0, 1, 0, 0)

        val equations = arrayOf(
            bitSet(1, 1, 1, 0),
            bitSet(1, 1, 0, 1),
            bitSet(0, 1, 1, 1),
            bitSet(1, 0, 1, 1),
        )

        XorEquationSolver.solve(equations, variables, equations.size)

        println(equationsToString(equations, variables, equations.size))
    }

    test("EquationSolver4") {
        val variables = bitSet(0, 1, 0, 1)

        val equations = arrayOf(
            bitSet(1, 1, 0, 0),
            bitSet(0, 1, 1, 0),
            bitSet(0, 0, 1, 1),
            bitSet(1, 0, 0, 1),
        )

        XorEquationSolver.solve(equations, variables, equations.size)

        println(equationsToString(equations, variables, equations.size))
    }

    test("EquationSolver5") {
        val variables = bitSet(1, 0, 1)

        val equations = arrayOf(
            bitSet(1, 1, 0),
            bitSet(1, 0, 1),
            bitSet(0, 1, 1),
        )

        XorEquationSolver.solve(equations, variables, equations.size)

        println(equationsToString(equations, variables, equations.size))
    }

    test("EquationSolver6") {
        val variables = bitSet(0, 1, 0, 0)

        val equations = arrayOf(
            bitSet(1, 1, 1, 0),
            bitSet(0, 0, 1, 1),
            bitSet(0, 0, 0, 0),
            bitSet(0, 0, 0, 0),
        )

        XorEquationSolver.solve(equations, variables, equations.size)

        println(equationsToString(equations, variables, equations.size))
    }

    xtest("RandomEquationSolver7") {
        while(true) {
            val variables = bitSet(Random.nextInt(2), Random.nextInt(2), Random.nextInt(2), Random.nextInt(2))

            val equations = arrayOf(
                bitSet(Random.nextInt(2), Random.nextInt(2), Random.nextInt(2), Random.nextInt(2)),
                bitSet(Random.nextInt(2), Random.nextInt(2), Random.nextInt(2), Random.nextInt(2)),
                bitSet(Random.nextInt(2), Random.nextInt(2), Random.nextInt(2), Random.nextInt(2)),
                bitSet(Random.nextInt(2), Random.nextInt(2), Random.nextInt(2), Random.nextInt(2)),
            )

            println("-----------------------------")
            try {
                println(matrixToString(equations, variables, equations.size))
                XorEquationSolver.solve(equations, variables, equations.size)
            } catch (e: XorEquationSolver.NoSolution) {
                println("no solution")
            } finally {
                println(matrixToString(equations, variables, equations.size))
            }

            Thread.sleep(800)
        }
    }

    xtest("RandomEquationSolver8") {
        while(true) {
            val varCount = Random.nextInt(1, 10)
            val eqCount = Random.nextInt(1, 15)

            val variables = BitSet(varCount)

            repeat(varCount) {
                variables.set(it, Random.nextInt(2) == 1)
            }

            val equations = Array(eqCount) {
                val xxx = BitSet(varCount)
                repeat(varCount) {
                    xxx.set(it, Random.nextInt(2) == 1)
                }
                xxx
            }

            println("-----------------------------")
            try {
                println(matrixToString(equations, variables, varCount))
                XorEquationSolver.solve(equations, variables, varCount)
            } catch (e: XorEquationSolver.NoSolution) {
                println("no solution")
            } finally {
                println(matrixToString(equations, variables, varCount))
            }

            Thread.sleep(800)
        }
    }

    test("EquationSolver8") {
        val variables = bitSet(0, 1, 0, 0)

        val equations = arrayOf(
            bitSet(1, 1, 0, 0),
            bitSet(0, 1, 0, 1),
            bitSet(0, 1, 1, 0),
            bitSet(0, 0, 1, 1),
        )

        println(matrixToString(equations, variables, equations.size))

        assertThrows<XorEquationSolver.NoSolution> {
            XorEquationSolver.solve(equations, variables, equations.size)
        }

        println("no solution")
        println(matrixToString(equations, variables, equations.size))
    }

    test("EquationSolver9") {
        val variables = bitSet(0, 0, 1, 0, 1, 1, 0)
        val varCount = 7

        val equations = arrayOf(
            bitSet(1, 0, 1, 1, 0, 0, 1),
            bitSet(0, 0, 0, 1, 1, 1, 0),
            bitSet(0, 0, 0, 1, 1, 0, 0),
            bitSet(0, 1, 1, 0, 1, 1, 1),
            bitSet(1, 0, 0, 1, 1, 1, 1),
            bitSet(1, 0, 0, 1, 1, 0, 0),
        )

        println(matrixToString(equations, variables, varCount))
        XorEquationSolver.solve(equations, variables, varCount)
        println(matrixToString(equations, variables, varCount))
    }
})
