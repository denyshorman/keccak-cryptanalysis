package keccak

import io.kotest.core.spec.style.FunSpec

class EquationSolverTest : FunSpec({
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
})
