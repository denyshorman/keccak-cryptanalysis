package keccak

import io.kotest.core.spec.style.FunSpec
import kotlin.test.assertEquals

class NodeEquationTest : FunSpec({
    context("extract") {
        test("1") {
            val equation = NodeEquation(
                "a" xor "b" xor "d",
                "c".toVar(),
            )
            val extracted = equation.extract("a".toVar())
            println(extracted)
            assertEquals(NodeEquation("a".toVar(), "b" xor "c" xor "d"), extracted)
        }

        test("2") {
            val equation = NodeEquation(
                ("a" and "x") xor "b" xor "d",
                "c".toVar(),
            )
            val extracted = equation.extract("a".toVar())
            println(extracted)
            assertEquals(NodeEquation("a" and "x", "b" xor "c" xor "d"), extracted)
        }

        test("3") {
            val equation = NodeEquation(
                ("a" and "x") xor ("a" and "x") xor "d",
                Bit(),
            )
            val extracted = equation.extract("a".toVar())
            println(extracted)
            assertEquals(NodeEquation("d".toVar(), Bit()), extracted)
        }

        test("4") {
            val equation = NodeEquation(
                ("a" and "x") xor ("a" and "y") xor "d",
                Bit(),
            )
            val extracted = equation.extract("a".toVar())
            println(extracted)
            assertEquals(NodeEquation("a" and ("x" xor "y"), "d".toVar()), extracted)
        }

        test("5") {
            val equation = NodeEquation(
                ("a" and "x") xor ("a" and ("a" xor "y")) xor "d",
                Bit(),
            )
            val extracted = equation.extract("a".toVar())
            println(extracted)
            assertEquals(NodeEquation("a" and (Bit(true) xor "x" xor "y"), "d".toVar()), extracted)
        }

        test("6") {
            val equation = NodeEquation(
                "b" xor (("a" xor "b") and "x"),
                Bit(),
            )
            val extracted = equation.extract("a".toVar())
            println(extracted)
            assertEquals(NodeEquation("a" and "x", "b" xor ("b" and "x")), extracted)
        }

        test("7") {
            val equation = NodeEquation(
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

        test("8") {
            val equation = NodeEquation(
                (("a" xor "b" xor "c" xor "d") and "x") xor "b" xor "c" xor "x" xor "y" xor "b" xor ("a" and "x" and "y") xor ("a" and "b") xor Bit(
                    true
                ),
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
    }

    context("substitute") {
        test("1") {
            val equation = NodeEquation(
                (("a" xor "b" xor "c" xor "d") and "x") xor "b" xor "c" xor "x" xor "y" xor "b" xor ("a" and "x" and "y") xor ("a" and "b") xor Bit(
                    true
                ),
                Bit(),
            )

            val subEq = NodeEquation("a" and "b", "x".toVar())

            println(equation)

            val result = equation.substitute(subEq, "a".toVar())
            println(result)
        }
    }

    context("multiply") {
        test("1") {
            val eq = NodeEquation("a" xor "b" xor ("c" and "d") xor ("x" and ("a" xor "b")), "x" xor "y")
            val mul = "a" and "b"

            val multiplied = eq.multiply(mul)
            val expected = NodeEquation(
                ("a" and "b" and "c" and "d") xor ("a" and "b" and ("a" xor "b") and "x"),
                ("a" and "b" and "x") xor ("a" and "b" and "y")
            )

            println(eq)
            println(mul)
            println(multiplied)

            assertEquals(expected, multiplied)
        }
    }

    context("replace") {
        test("1") {
            val eq = NodeEquation("a" xor "b" xor ("c" and "d") xor ("x" and ("a" xor "b")), "x" xor "y")
            val what = "a".toVar()
            val with = ("b" xor "c") and "x"

            val actual = eq.replace(what, with)
            val expected = NodeEquation(with xor "b" xor ("c" and "d") xor ("x" and (with xor "b")), "x" xor "y")

            println("Initial eq: $eq")
            println("Replace what: $what")
            println("Replace with: $with")
            println("Result: $actual")

            assertEquals(expected, actual)
        }

        test("2") {
            val eq =
                NodeEquation(("a" and "b") xor "b" xor ("c" and "d") xor ("x" and ("a" xor ("b" and "a"))), "x" xor "y")
            val what = "a" and "b"
            val with = ("b" xor "c") and "x"

            val actual = eq.replace(what, with)
            val expected = NodeEquation(with xor "b" xor ("c" and "d") xor ("x" and ("a" xor with)), "x" xor "y")

            println("Initial eq: $eq")
            println("Replace what: $what")
            println("Replace with: $with")
            println("Result: $actual")

            assertEquals(expected, actual)
        }

        test("3") {
            val eq = NodeEquation(("a" and "b" and "c") xor ("a" and "b" and "x"), "x" xor "y")
            val what = "a" and "b"
            val with = ("b" xor "c")

            val actual = eq.replace(what, with)
            val expected = NodeEquation((with and "c") xor (with and "x"), "x" xor "y")

            println("Initial eq: $eq")
            println("Replace what: $what")
            println("Replace with: $with")
            println("Result: $actual")

            assertEquals(expected, actual)
        }

        test("4") {
            val eq = NodeEquation(
                ("a" and "b" and ("a" xor ("b" xor ("a" and "b" and "x")))) xor ("a" and "b" and "x"),
                "x" xor "y"
            )
            val what = "a" and "b"
            val with = ("b" xor "c")

            val actual = eq.replace(what, with)
            val expected = NodeEquation((with and ("a" xor ("b" xor (with and "x")))) xor (with and "x"), "x" xor "y")

            println("Initial eq: $eq")
            println("Replace what: $what")
            println("Replace with: $with")
            println("Result: $actual")

            assertEquals(expected, actual)
        }

        test("5") {
            val eq = NodeEquation("a" xor "b" xor ("c" and ("a" xor "b")), "a" xor "b")
            val what = "a" xor "b"
            val with = "b" xor "c"

            val actual = eq.replace(what, with)
            val expected = NodeEquation(with xor ("c" and (with)), with)

            println("Initial eq: $eq")
            println("Replace what: $what")
            println("Replace with: $with")
            println("Result: $actual")

            assertEquals(expected, actual)
        }
    }
})
