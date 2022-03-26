package keccak

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import keccak.util.*

class NodeEquationSystemTest : FunSpec({
    context("isValid") {
        test("1") {
            val eqs = arrayListOf(
                f,
                x0,
                x0 + x1,
                x0 + x1 + t,
                x0*x1,
            )
            val system = NodeEquationSystem(eqs)

            eqs.indices.forEach {
                system.isValid(it).shouldBeTrue()
            }
        }

        test("2") {
            val eqs = arrayListOf(
                t,
                x0 + x0 + t,
                t*t*t + t + t,
            )
            val system = NodeEquationSystem(eqs)

            eqs.indices.forEach {
                system.isValid(it).shouldBeFalse()
            }
        }
    }

    context("expressNode") {
        test("1") {
            val eqs = arrayListOf<Node>(
                x0 + x1 + x2,
                x0 + x2 + x4,
                x1 + x3 + t,
            )

            val system = NodeEquationSystem(eqs)

            val expressed = system.expressNode(0, x0)
            expressed.shouldBeTrue()

            system.equations[0].shouldBe(x0 + x1 + x2)
            system.equations[1].shouldBe(x1 + x4)
            system.equations[2].shouldBe(x1 + x3 + t)
        }
    }

    context("solve") {
        test("1") {
            //val xorSystem = randomXorEquationSystem(rows = 6, cols = 6)
            val xorSystem = XorEquationSystem(
                rows = 6,
                cols = 6,
                humanReadable = true,
                "x0 + x1 + x3 + x4 = 1",
                "x0 + x1 + x3 + x4 + x5 = 0",
                "x0 + x2 + x3 + x4 = 0",
                "x0 + x1 + x3 = 0",
                "x0 + x2 = 1",
                "x0 + x1 + x2 + x4 + x5 = 0",
            )
            val xorNodeSystem = xorSystem.toNodeEquationSystem()

            println(xorSystem.toHumanString())
            println()
            println(xorNodeSystem.toString())
            println()

            xorSystem.solve(varPriority = BitSet("111110"))
            xorNodeSystem.solve(priorityNodes = listOf(x0,x1,x2,x3,x4))

            println(xorSystem.toHumanString())
            println()
            println(xorNodeSystem.toString())
            println()
        }
    }
})
