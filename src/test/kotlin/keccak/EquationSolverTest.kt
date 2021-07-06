package keccak

import io.kotest.core.spec.style.FunSpec

class EquationSolverTest : FunSpec({
    test("EquationSolver") {
        val variables = arrayOf(
            Variable("a1"),
            Variable("a2"),
            Variable("a3"),
            Variable("a4"),
        )

        val equations = arrayOf(
            XorEquation(
                left = listOf(Variable("a1"), Variable("a3"), Variable("a4")),
                right = listOf(Bit(false)),
            ),
            XorEquation(
                left = listOf(Variable("a1"), Variable("a2"), Variable("a3")),
                right = listOf(Bit(false)),
            ),
            XorEquation(
                left = listOf(Variable("a1"), Variable("a2"), Variable("a4")),
                right = listOf(Bit(true)),
            ),
            XorEquation(
                left = listOf(Variable("a2"), Variable("a3"), Variable("a4")),
                right = listOf(Bit(false)),
            ),
        )

        val context = NodeContext()

        EquationSolver.solve(variables, equations, context)
    }

    test("EquationSolver2") {
        val variables = arrayOf(
            Variable("a1"),
            Variable("a2"),
            Variable("a3"),
        )

        val equations = arrayOf(
            XorEquation(left = listOf(Variable("a1"), Variable("a2")), right = listOf(Bit(true))),
            XorEquation(left = listOf(Variable("a1"), Variable("a3")), right = listOf(Bit(false))),
            XorEquation(left = listOf(Variable("a2"), Variable("a3")), right = listOf(Bit(true))),
        )

        val context = NodeContext()

        EquationSolver.solve(variables, equations, context)
        println("good")
    }
})
