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
            "b" xor (("a" xor "b") and "x"),
            Bit(),
        )
        val extracted = equation.extract("a".toVar())
        println(extracted)
        assertEquals(Equation("a" and "x", "b" xor ("b" and "x")), extracted)
    }

    test("GeneralEquationSolver7") {
        val equation = Equation(
            ("a" xor "b" xor "c") and ("a" xor "c" xor "d") and "x" and "y" and ("b" xor ("x" and ("a" xor "b"))),
            Bit(),
        )
        val extracted = equation.extract("a".toVar())

        println(equation)
        println(extracted)

        val eqXor = extracted.left xor extracted.right

        (0..1).forEach { a ->
            (0..1).forEach { b ->
                (0..1).forEach { c ->
                    (0..1).forEach { d ->
                        (0..1).forEach { x ->
                            (0..1).forEach { y ->
                                val ctx = NodeContext()
                                ctx.variables["a"] = Bit(a)
                                ctx.variables["b"] = Bit(b)
                                ctx.variables["c"] = Bit(c)
                                ctx.variables["d"] = Bit(d)
                                ctx.variables["x"] = Bit(x)
                                ctx.variables["y"] = Bit(y)

                                assertEquals(equation.left.evaluate(ctx), eqXor.evaluate(ctx))
                            }
                        }
                    }
                }
            }
        }
    }

    test("GeneralEquationSolver8") {
        val equation = Equation(
            (("a" xor "b" xor "c" xor "d") and "x") xor "b" xor "c" xor "x" xor "y" xor "b" xor ("a" and "x" and "y") xor ("a" and "b") xor Bit(true),
            Bit(),
        )
        val extracted = equation.extract("a".toVar())

        println(equation)
        println(extracted)

        val eqXor = extracted.left xor extracted.right

        (0..1).forEach { a ->
            (0..1).forEach { b ->
                (0..1).forEach { c ->
                    (0..1).forEach { d ->
                        (0..1).forEach { x ->
                            (0..1).forEach { y ->
                                val ctx = NodeContext()
                                ctx.variables["a"] = Bit(a)
                                ctx.variables["b"] = Bit(b)
                                ctx.variables["c"] = Bit(c)
                                ctx.variables["d"] = Bit(d)
                                ctx.variables["x"] = Bit(x)
                                ctx.variables["y"] = Bit(y)

                                assertEquals(equation.left.evaluate(ctx), eqXor.evaluate(ctx))
                                // println("${equation.left.evaluate(ctx)} = ${eqXor.evaluate(ctx)}")
                            }
                        }
                    }
                }
            }
        }
    }
})
