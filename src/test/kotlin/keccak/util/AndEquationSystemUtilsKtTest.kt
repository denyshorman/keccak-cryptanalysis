package keccak.util

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AndEquationSystemUtilsKtTest : FunSpec({
    context("rotate") {
        test("1") {
            val solution = BitSet("110")

            val system = AndEquationSystem(
                rows = 1, cols = 3, humanReadable = true,
                "(x0 + x1 + x2 + 1)*(x0 + 1) = 0"
            )

            system.rotate(solution, left = false, right = false)

            val expectedSystem = AndEquationSystem(
                rows = 1, cols = 3, humanReadable = true,
                "(x1 + x2 + 1)*(x0 + 1) = 0"
            )

            system.shouldBe(expectedSystem)
        }

        test("2") {
            val solution = BitSet("110")

            val system = AndEquationSystem(
                rows = 1, cols = 3, humanReadable = true,
                "(x0 + x1 + x2 + 1)*(x0 + 1) = 0"
            )

            system.rotate(solution, left = true, right = false)

            val expectedSystem = AndEquationSystem(
                rows = 1, cols = 3, humanReadable = true,
                "(x0 + x1 + x2 + 1)*(x0 + 1) = 0"
            )

            system.shouldBe(expectedSystem)
        }

        test("3") {
            val solution = BitSet("110")

            val system = AndEquationSystem(
                rows = 1, cols = 3, humanReadable = true,
                "(x0 + x1 + x2 + 1)*(x0 + 1) = 0"
            )

            system.rotate(solution, left = false, right = true)

            val expectedSystem = AndEquationSystem(
                rows = 1, cols = 3, humanReadable = true,
                "(x0 + 1)*(x0 + x1 + x2 + 1) = 0"
            )

            system.shouldBe(expectedSystem)
        }

        test("4") {
            val solution = BitSet("100")

            val system = AndEquationSystem(
                rows = 1, cols = 3, humanReadable = true,
                "(x0 + x1 + x2 + 1)*(x0 + 1) = 0"
            )

            system.rotate(solution, left = false, right = false)

            val expectedSystem = AndEquationSystem(
                rows = 1, cols = 3, humanReadable = true,
                "(x0 + x1 + x2 + 1)*(x0 + 1) = 0"
            )

            system.shouldBe(expectedSystem)
        }

        test("5") {
            val solution = BitSet("100")

            val system = AndEquationSystem(
                rows = 1, cols = 3, humanReadable = true,
                "(x0 + x1 + x2 + 1)*(x0 + 1) = 0"
            )

            system.rotate(solution, left = false, right = true)

            val expectedSystem = AndEquationSystem(
                rows = 1, cols = 3, humanReadable = true,
                "(x0 + x1 + x2 + 1)*(x1 + x2 + 1) = 0"
            )

            system.shouldBe(expectedSystem)
        }

        test("6") {
            val solution = BitSet("100")

            val system = AndEquationSystem(
                rows = 1, cols = 3, humanReadable = true,
                "(x0 + x1 + x2 + 1)*(x0 + 1) = 0"
            )

            system.rotate(solution, left = true, right = false)

            val expectedSystem = AndEquationSystem(
                rows = 1, cols = 3, humanReadable = true,
                "(x1 + x2 + 1)*(x0 + 1) = 0"
            )

            system.shouldBe(expectedSystem)
        }

        test("7") {
            val solution = BitSet("100")

            val system = AndEquationSystem(
                rows = 1, cols = 3, humanReadable = true,
                "(x0 + x1 + x2)*(x0 + 1) = 0"
            )

            system.rotate(solution, left = false, right = true)

            val expectedSystem = AndEquationSystem(
                rows = 1, cols = 3, humanReadable = true,
                "(x0 + 1)*(x0 + x1 + x2) = 0"
            )

            system.shouldBe(expectedSystem)
        }

        test("8") {
            val solution = BitSet("100")

            val system = AndEquationSystem(
                rows = 1, cols = 3, humanReadable = true,
                "(x0 + x1 + x2)*(x0 + 1) = 0"
            )

            system.rotate(solution, left = false, right = false)

            val expectedSystem = AndEquationSystem(
                rows = 1, cols = 3, humanReadable = true,
                "(x1 + x2)*(x0 + 1) = 0"
            )

            system.shouldBe(expectedSystem)
        }
    }
})
