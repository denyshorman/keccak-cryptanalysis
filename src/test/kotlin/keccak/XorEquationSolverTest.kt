package keccak

import io.kotest.core.spec.style.FunSpec
import org.junit.jupiter.api.assertThrows
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

        XorEquationSolver.solve(equations, variables)

        println(equationsToString(equations, variables))
    }

    test("EquationSolver4") {
        val variables = bitSet(0, 1, 0, 1)

        val equations = arrayOf(
            bitSet(1, 1, 0, 0),
            bitSet(0, 1, 1, 0),
            bitSet(0, 0, 1, 1),
            bitSet(1, 0, 0, 1),
        )

        XorEquationSolver.solve(equations, variables)

        println(equationsToString(equations, variables))
    }

    test("EquationSolver5") {
        val variables = bitSet(1, 0, 1)

        val equations = arrayOf(
            bitSet(1, 1, 0),
            bitSet(1, 0, 1),
            bitSet(0, 1, 1),
        )

        XorEquationSolver.solve(equations, variables)

        println(equationsToString(equations, variables))
    }

    test("EquationSolver6") {
        val variables = bitSet(0, 1, 0, 0)

        val equations = arrayOf(
            bitSet(1, 1, 1, 0),
            bitSet(0, 0, 1, 1),
            bitSet(0, 0, 0, 0),
            bitSet(0, 0, 0, 0),
        )

        XorEquationSolver.solve(equations, variables)

        println(equationsToString(equations, variables))
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
                println(matrixToString(equations, variables))
                XorEquationSolver.solve(equations, variables)
            } catch (e: XorEquationSolver.NoSolution) {
                println("no solution")
            } finally {
                println(matrixToString(equations, variables))
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

        println(matrixToString(equations, variables))

        assertThrows<XorEquationSolver.NoSolution> {
            XorEquationSolver.solve(equations, variables)
        }

        println("no solution")
        println(matrixToString(equations, variables))
    }
})
