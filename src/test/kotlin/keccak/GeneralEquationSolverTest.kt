package keccak

import io.kotest.core.spec.style.FunSpec
import kotlin.test.assertEquals

class GeneralEquationSolverTest : FunSpec({
    test("GeneralEquationSolver") {
        val equation = Equation(
            "a" xor "b" xor "d",
            "c".toVar(),
        )
        val extracted = equation.extract("a".toVar())
        println(extracted)
        assertEquals(Equation("a".toVar(), "b" xor "c" xor "d"), extracted)
    }

    test("GeneralEquationSolver2") {
        val equation = Equation(
            ("a" and "x") xor "b" xor "d",
            "c".toVar(),
        )
        val extracted = equation.extract("a".toVar())
        println(extracted)
        assertEquals(Equation("a" and "x", "b" xor "c" xor "d"), extracted)
    }

    test("GeneralEquationSolver3") {
        val equation = Equation(
            ("a" and "x") xor ("a" and "x") xor "d",
            Bit(),
        )
        val extracted = equation.extract("a".toVar())
        println(extracted)
        assertEquals(Equation("d".toVar(), Bit()), extracted)
    }

    test("GeneralEquationSolver4") {
        val equation = Equation(
            ("a" and "x") xor ("a" and "y") xor "d",
            Bit(),
        )
        val extracted = equation.extract("a".toVar())
        println(extracted)
        assertEquals(Equation("a" and ("x" xor "y"), "d".toVar()), extracted)
    }

    test("GeneralEquationSolver5") {
        val equation = Equation(
            ("a" and "x") xor ("a" and ("a" xor "y")) xor "d",
            Bit(),
        )
        val extracted = equation.extract("a".toVar())
        println(extracted)
        assertEquals(Equation("a" and (Bit(true) xor "x" xor "y"), "d".toVar()), extracted)
    }

    test("GeneralEquationSolver6") {
        val equation = Equation(
            ("a" xor "b" xor "c") and ("a" xor "c" xor "d") and "x" and "y" and ("b" xor ("x" and ("a" xor "b"))),
            Bit(),
        )
        val extracted = equation.extract("a".toVar())
        println(extracted)
        // assertEquals(Equation("a" and (Bit(true) xor "x" xor "y"), "d".toVar()), extracted)
    }
})
