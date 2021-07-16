package keccak

import io.kotest.core.spec.style.FunSpec
import kotlin.test.assertEquals

class GeneralEquationSolverTest : FunSpec({
    context("Equation") {
        context("extract") {
            test("1") {
                val equation = Equation(
                    "a" xor "b" xor "d",
                    "c".toVar(),
                )
                val extracted = equation.extract("a".toVar())
                println(extracted)
                assertEquals(Equation("a".toVar(), "b" xor "c" xor "d"), extracted)
            }

            test("2") {
                val equation = Equation(
                    ("a" and "x") xor "b" xor "d",
                    "c".toVar(),
                )
                val extracted = equation.extract("a".toVar())
                println(extracted)
                assertEquals(Equation("a" and "x", "b" xor "c" xor "d"), extracted)
            }

            test("3") {
                val equation = Equation(
                    ("a" and "x") xor ("a" and "x") xor "d",
                    Bit(),
                )
                val extracted = equation.extract("a".toVar())
                println(extracted)
                assertEquals(Equation("d".toVar(), Bit()), extracted)
            }

            test("4") {
                val equation = Equation(
                    ("a" and "x") xor ("a" and "y") xor "d",
                    Bit(),
                )
                val extracted = equation.extract("a".toVar())
                println(extracted)
                assertEquals(Equation("a" and ("x" xor "y"), "d".toVar()), extracted)
            }

            test("5") {
                val equation = Equation(
                    ("a" and "x") xor ("a" and ("a" xor "y")) xor "d",
                    Bit(),
                )
                val extracted = equation.extract("a".toVar())
                println(extracted)
                assertEquals(Equation("a" and (Bit(true) xor "x" xor "y"), "d".toVar()), extracted)
            }

            test("6") {
                val equation = Equation(
                    "b" xor (("a" xor "b") and "x"),
                    Bit(),
                )
                val extracted = equation.extract("a".toVar())
                println(extracted)
                assertEquals(Equation("a" and "x", "b" xor ("b" and "x")), extracted)
            }

            test("7") {
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

            test("8") {
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
        }

        context("substitute") {
            test("1") {
                val equation = Equation(
                    (("a" xor "b" xor "c" xor "d") and "x") xor "b" xor "c" xor "x" xor "y" xor "b" xor ("a" and "x" and "y") xor ("a" and "b") xor Bit(true),
                    Bit(),
                )

                val subEq = Equation("a" and "b", "x".toVar())

                println(equation)

                val result = equation.substitute(subEq, "a".toVar())
                println(result)
            }
        }

        context("multiply") {
            test("1") {
                val eq = Equation("a" xor "b" xor ("c" and "d") xor ("x" and ("a" xor "b")), "x" xor "y")
                val mul = "a" and "b"

                val multiplied = eq.multiply(mul)
                val expected = Equation(("a" and "b" and "c" and "d") xor ("a" and "b" and ("a" xor "b") and "x"), ("a" and "b" and "x") xor ("a" and "b" and "y"))

                println(eq)
                println(mul)
                println(multiplied)

                assertEquals(expected, multiplied)
            }
        }

        context("replace") {
            test("1") {
                val eq = Equation("a" xor "b" xor ("c" and "d") xor ("x" and ("a" xor "b")), "x" xor "y")
                val what = "a".toVar()
                val with = ("b" xor "c") and "x"

                val actual = eq.replace(what, with)
                val expected = Equation(with xor "b" xor ("c" and "d") xor ("x" and (with xor "b")), "x" xor "y")

                println("Initial eq: $eq")
                println("Replace what: $what")
                println("Replace with: $with")
                println("Result: $actual")

                assertEquals(expected, actual)
            }

            test("2") {
                val eq = Equation(("a" and "b") xor "b" xor ("c" and "d") xor ("x" and ("a" xor ("b" and "a"))), "x" xor "y")
                val what = "a" and "b"
                val with = ("b" xor "c") and "x"

                val actual = eq.replace(what, with)
                val expected = Equation(with xor "b" xor ("c" and "d") xor ("x" and ("a" xor with)), "x" xor "y")

                println("Initial eq: $eq")
                println("Replace what: $what")
                println("Replace with: $with")
                println("Result: $actual")

                assertEquals(expected, actual)
            }

            test("3") {
                val eq = Equation(("a" and "b" and "c") xor ("a" and "b" and "x"), "x" xor "y")
                val what = "a" and "b"
                val with = ("b" xor "c")

                val actual = eq.replace(what, with)
                val expected = Equation((with and "c") xor (with and "x"), "x" xor "y")

                println("Initial eq: $eq")
                println("Replace what: $what")
                println("Replace with: $with")
                println("Result: $actual")

                assertEquals(expected, actual)
            }

            test("4") {
                val eq = Equation(("a" and "b" and ("a" xor ("b" xor ("a" and "b" and "x")))) xor ("a" and "b" and "x"), "x" xor "y")
                val what = "a" and "b"
                val with = ("b" xor "c")

                val actual = eq.replace(what, with)
                val expected = Equation((with and ("a" xor ("b" xor (with and "x")))) xor (with and "x"), "x" xor "y")

                println("Initial eq: $eq")
                println("Replace what: $what")
                println("Replace with: $with")
                println("Result: $actual")

                assertEquals(expected, actual)
            }

            test("5") {
                val eq = Equation("a" xor "b" xor ("c" and ("a" xor "b")), "a" xor "b")
                val what = "a" xor "b"
                val with = "b" xor "c"

                val actual = eq.replace(what, with)
                val expected = Equation(with xor ("c" and (with)), with)

                println("Initial eq: $eq")
                println("Replace what: $what")
                println("Replace with: $with")
                println("Result: $actual")

                assertEquals(expected, actual)
            }
        }
    }

    context("GeneralEquationSolver") {
        test("solve1") {
            val x = Bit(1)

            val a = "a"
            val b = "b"
            val c = "c"
            val d = "d"

            val na = x xor a
            val nb = x xor b
            val nc = x xor c
            val nd = x xor d

            val f = arrayOf(
                arrayOf(0, 1, 0, 1),
                arrayOf(0, 0, 1, 0),
                arrayOf(1, 0, 1, 1),
                arrayOf(1, 1, 0, 0),
                arrayOf(0, 0, 0, 1),
                arrayOf(1, 0, 0, 0),
                arrayOf(1, 0, 0, 1),
                arrayOf(1, 1, 1, 0),
                arrayOf(1, 1, 1, 1),
                arrayOf(0, 0, 1, 1),
                arrayOf(0, 1, 1, 0),
                arrayOf(0, 1, 0, 1),
                arrayOf(0, 0, 0, 0),
                arrayOf(1, 1, 0, 0),
                arrayOf(1, 0, 1, 0),
                arrayOf(0, 0, 0, 1),
            )

            val equations = arrayOf(
                Equation((na and c and d) xor (na and c and nd) xor (b and nc and d) xor (b and c and nd) xor (na and b and c and nd) xor (a and nb and nc and nd), "x".toVar()),
                Equation( (nb and nc and nd) xor (a and b and nc and d) xor (na and c and d) xor (a and nb and c), "y".toVar()),
                Equation((a and nb and nc) xor (a and c and nd) xor (nb and nc and d) xor (nb and c and nd) xor (a and nb and nc and d) xor (a and nb and c and nd) xor (na and b and c and d), "z".toVar()),
                Equation((na and nc and nd) xor (a and nb and nc) xor (a and c and d) xor (na and c and nd), "k".toVar()),
            )

            fun verify(eq: Array<Equation>) {
                var cc = 0

                (0..1).forEach { ax ->
                    (0..1).forEach { bx ->
                        (0..1).forEach { cx ->
                            (0..1).forEach { dx ->
                                val context = NodeContext()
                                context.variables["a"] = Bit(ax)
                                context.variables["b"] = Bit(bx)
                                context.variables["c"] = Bit(cx)
                                context.variables["d"] = Bit(dx)
                                context.variables["x"] = Bit(f[cc][0])
                                context.variables["y"] = Bit(f[cc][1])
                                context.variables["z"] = Bit(f[cc][2])
                                context.variables["k"] = Bit(f[cc][3])

                                assertEquals(eq[0].right.evaluate(context), eq[0].left.evaluate(context), "f1: ${cc + 1}")
                                assertEquals(eq[1].right.evaluate(context), eq[1].left.evaluate(context), "f2: ${cc + 1}")
                                assertEquals(eq[2].right.evaluate(context), eq[2].left.evaluate(context), "f3: ${cc + 1}")
                                assertEquals(eq[3].right.evaluate(context), eq[3].left.evaluate(context), "f4: ${cc + 1}")

                                cc++
                            }
                        }
                    }
                }
            }

            fun verifyEquation(eq: Array<Equation>) {
                var cc = 0

                (0..1).forEach { ax ->
                    (0..1).forEach { bx ->
                        (0..1).forEach { cx ->
                            (0..1).forEach { dx ->
                                val context = NodeContext()
                                context.variables["a"] = Bit(ax)
                                context.variables["b"] = Bit(bx)
                                context.variables["c"] = Bit(cx)
                                context.variables["d"] = Bit(dx)
                                context.variables["x"] = Bit(f[cc][0])
                                context.variables["y"] = Bit(f[cc][1])
                                context.variables["z"] = Bit(f[cc][2])
                                context.variables["k"] = Bit(f[cc][3])

                                assertEquals(eq[0].left.evaluate(context), eq[0].right.evaluate(context), "f1: ${cc + 1}")
                                assertEquals(eq[1].left.evaluate(context), eq[1].right.evaluate(context), "f2: ${cc + 1}")
                                assertEquals(eq[2].left.evaluate(context), eq[2].right.evaluate(context), "f3: ${cc + 1}")
                                assertEquals(eq[3].left.evaluate(context), eq[3].right.evaluate(context), "f4: ${cc + 1}")

                                println(cc)
                                println("${eq[0].left.evaluate(context)} = ${eq[0].right.evaluate(context)}")
                                println("${eq[1].left.evaluate(context)} = ${eq[1].right.evaluate(context)}")
                                println("${eq[2].left.evaluate(context)} = ${eq[2].right.evaluate(context)}")
                                println("${eq[3].left.evaluate(context)} = ${eq[3].right.evaluate(context)}")
                                println()

                                cc++
                            }
                        }
                    }
                }
            }

            verify(equations)

            val variables = arrayOf("a".toVar(), "b".toVar(), "c".toVar(), "d".toVar())

            GeneralEquationSolver.solve(equations, variables)

            verifyEquation(equations)
        }

        test("solve2") {
            val x = Bit(1)

            val a = "a"
            val b = "b"
            val c = "c"
            val d = "d"

            val na = x xor a
            val nb = x xor b
            val nc = x xor c
            val nd = x xor d

            val equations = arrayOf(
                Equation((na and c and d) xor (na and c and nd) xor (b and nc and d) xor (b and c and nd) xor (na and b and c and nd) xor (a and nb and nc and nd), Bit(0)),
                Equation( (nb and nc and nd) xor (a and b and nc and d) xor (na and c and d) xor (a and nb and c), Bit(0)),
                Equation((a and nb and nc) xor (a and c and nd) xor (nb and nc and d) xor (nb and c and nd) xor (a and nb and nc and d) xor (a and nb and c and nd) xor (na and b and c and d), Bit(1)),
                Equation((na and nc and nd) xor (a and nb and nc) xor (a and c and d) xor (na and c and nd), Bit(0)),
            )

            val variables = arrayOf("a".toVar(), "b".toVar(), "c".toVar(), "d".toVar())

            GeneralEquationSolver.solve(equations, variables)

            equations.forEachIndexed { index, _ ->
                equations[index] = Equation(equations[index].left.flatten(), equations[index].right.flatten())
            }

            equations.forEach { eq ->
                println(eq)
            }
        }
    }
})
