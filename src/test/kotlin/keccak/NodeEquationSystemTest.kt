package keccak

import io.kotest.core.spec.style.FunSpec
import kotlin.test.assertEquals

class NodeEquationSystemTest : FunSpec({
    context("solve") {
        test("1") {
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
                NodeEquation((na and c and d) xor (na and c and nd) xor (b and nc and d) xor (b and c and nd) xor (na and b and c and nd) xor (a and nb and nc and nd), "x".toVar()),
                NodeEquation( (nb and nc and nd) xor (a and b and nc and d) xor (na and c and d) xor (a and nb and c), "y".toVar()),
                NodeEquation((a and nb and nc) xor (a and c and nd) xor (nb and nc and d) xor (nb and c and nd) xor (a and nb and nc and d) xor (a and nb and c and nd) xor (na and b and c and d), "z".toVar()),
                NodeEquation((na and nc and nd) xor (a and nb and nc) xor (a and c and d) xor (na and c and nd), "k".toVar()),
            )

            fun verify(eq: Array<NodeEquation>) {
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

            fun verifyEquation(eq: Array<NodeEquation>) {
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

            val eqSystem = NodeEquationSystem(equations, variables)

            eqSystem.solve()

            verifyEquation(equations)
        }

        test("2") {
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
                NodeEquation((na and c and d) xor (na and c and nd) xor (b and nc and d) xor (b and c and nd) xor (na and b and c and nd) xor (a and nb and nc and nd), Bit(0)),
                NodeEquation( (nb and nc and nd) xor (a and b and nc and d) xor (na and c and d) xor (a and nb and c), Bit(0)),
                NodeEquation((a and nb and nc) xor (a and c and nd) xor (nb and nc and d) xor (nb and c and nd) xor (a and nb and nc and d) xor (a and nb and c and nd) xor (na and b and c and d), Bit(1)),
                NodeEquation((na and nc and nd) xor (a and nb and nc) xor (a and c and d) xor (na and c and nd), Bit(0)),
            )

            val variables = arrayOf("a".toVar(), "b".toVar(), "c".toVar(), "d".toVar())

            val eqSystem = NodeEquationSystem(equations, variables)

            eqSystem.solve()

            equations.forEachIndexed { index, _ ->
                equations[index] = NodeEquation(equations[index].left.flatten(), equations[index].right.flatten())
            }

            equations.forEach { eq ->
                println(eq)
            }
        }
    }
})
