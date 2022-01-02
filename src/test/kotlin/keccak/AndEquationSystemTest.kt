package keccak

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import keccak.util.AndEquationSystem
import keccak.util.BitSet
import keccak.util.set

class AndEquationSystemTest : FunSpec({
    context("set") {
        test("1") {
            val system = AndEquationSystem(1, 5)
            system.set(0, "(v0)*(v1 + 1) = 1 + v2", true)
            system.toString().shouldBe("(10000|0)(01000|1) = 00100|1")
        }

        test("2") {
            val system = AndEquationSystem(1, 5)
            system.set(0, "(v0 + v3 + 1)*(v1 + 1 + 1) = 1 + v2", true)
            system.toString().shouldBe("(10010|1)(01000|0) = 00100|1")
        }

        test("3") {
            val system = AndEquationSystem(1, 5)
            system.set(0, "(10010|1)(01000|0) = 00100|1", false)
            system.toString().shouldBe("(10010|1)(01000|0) = 00100|1")
        }
    }

    context("solve") {
        test("1") {
            val solution = BitSet("01101")

            val system = AndEquationSystem(
                rows = 5,
                cols = 5,
                humanReadable = true,
                "(x1 + x2 + x3)*(x0 + x1 + x2 + x4) = x0",
                "(x0 + x1 + x3 + 1)*(x1 + x2 + 1) = x1",
                "(x2 + x3 + x4 + 1)*(x0 + x1 + x2 + x3 + 1) = x2",
                "(x3)*(x1 + x2 + x4) = x3",
                "(x0 + x1 + x2 + x3 + x4)*(x2 + x3 + x4 + 1) = x4",
            )

            val normalized = system.simplify()

            val solution2 = AndEquationSystem.PivotSolutionAlgorithm(normalized, solution).solve()

            solution2.shouldNotBeNull()
            solution2.shouldNotBe(solution)
            system.isValid(solution2).shouldBeTrue()
        }
    }
})
