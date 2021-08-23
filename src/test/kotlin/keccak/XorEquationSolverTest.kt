package keccak

import io.kotest.core.spec.style.FunSpec
import keccak.util.bitSet
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.random.Random

class XorEquationSolverTest : FunSpec({
    test("EquationSolver3") {
        val variables = bitSet(0, 1, 0, 0)

        val equations = arrayOf(
            bitSet(1, 1, 1, 0),
            bitSet(1, 1, 0, 1),
            bitSet(0, 1, 1, 1),
            bitSet(1, 0, 1, 1),
        )

        val system = XorEquationSystem(equations.size, equations.size, equations, variables)

        solveXorEquationSystem(system)

        println(system)
    }

    test("EquationSolver4") {
        val variables = bitSet(0, 1, 0, 1)

        val equations = arrayOf(
            bitSet(1, 1, 0, 0),
            bitSet(0, 1, 1, 0),
            bitSet(0, 0, 1, 1),
            bitSet(1, 0, 0, 1),
        )

        val system = XorEquationSystem(equations.size, equations.size, equations, variables)

        solveXorEquationSystem(system)

        println(system)
    }

    test("EquationSolver5") {
        val variables = bitSet(1, 0, 1)

        val equations = arrayOf(
            bitSet(1, 1, 0),
            bitSet(1, 0, 1),
            bitSet(0, 1, 1),
        )

        val system = XorEquationSystem(equations.size, equations.size, equations, variables)

        solveXorEquationSystem(system)

        println(system)
    }

    test("EquationSolver6") {
        val variables = bitSet(0, 1, 0, 0)

        val equations = arrayOf(
            bitSet(1, 1, 1, 0),
            bitSet(0, 0, 1, 1),
            bitSet(0, 0, 0, 0),
            bitSet(0, 0, 0, 0),
        )

        val system = XorEquationSystem(equations.size, equations.size, equations, variables)

        solveXorEquationSystem(system)

        println(system)
    }

    test("RandomEquationSolver7") {
        while(true) {
            val variables = bitSet(Random.nextInt(2), Random.nextInt(2), Random.nextInt(2), Random.nextInt(2))

            val equations = arrayOf(
                bitSet(Random.nextInt(2), Random.nextInt(2), Random.nextInt(2), Random.nextInt(2)),
                bitSet(Random.nextInt(2), Random.nextInt(2), Random.nextInt(2), Random.nextInt(2)),
                bitSet(Random.nextInt(2), Random.nextInt(2), Random.nextInt(2), Random.nextInt(2)),
                bitSet(Random.nextInt(2), Random.nextInt(2), Random.nextInt(2), Random.nextInt(2)),
            )

            val system = XorEquationSystem(equations.size, equations.size, equations, variables)

            println("-----------------------------")
            try {
                println(system)
                solveXorEquationSystem(system)
            } catch (e: NoSolution) {
                println("no solution")
            } finally {
                println(system)
            }

            Thread.sleep(800)
        }
    }

    xtest("RandomEquationSolver8") {
        while(true) {
            val varCount = Random.nextInt(1, 10)
            val eqCount = Random.nextInt(1, 15)

            val variables = BitSet(varCount)

            repeat(varCount) {
                variables.set(it, Random.nextInt(2) == 1)
            }

            val equations = Array(eqCount) {
                val xxx = BitSet(varCount)
                repeat(varCount) {
                    xxx.set(it, Random.nextInt(2) == 1)
                }
                xxx
            }

            val system = XorEquationSystem(eqCount, varCount, equations, variables)

            println("-----------------------------")
            try {
                println(system)
                solveXorEquationSystem(system)
            } catch (e: NoSolution) {
                println("no solution")
            } finally {
                println(system)
            }

            Thread.sleep(800)
        }
    }

    test("EquationSolver8") {
        val variables = bitSet(0, 1, 0, 0)

        val equations = arrayOf(
            bitSet(1, 1, 0, 0),
            bitSet(0, 1, 0, 1),
            bitSet(0, 1, 1, 0),
            bitSet(0, 0, 1, 1),
        )

        val system = XorEquationSystem(equations.size, equations.size, equations, variables)

        println(system)

        assertThrows<NoSolution> {
            solveXorEquationSystem(system)
        }

        println("no solution")
        println(system)
    }

    test("EquationSolver9") {
        val variables = bitSet(0, 0, 1, 0, 1, 1, 0)
        val varCount = 7

        val equations = arrayOf(
            bitSet(1, 0, 1, 1, 0, 0, 1),
            bitSet(0, 0, 0, 1, 1, 1, 0),
            bitSet(0, 0, 0, 1, 1, 0, 0),
            bitSet(0, 1, 1, 0, 1, 1, 1),
            bitSet(1, 0, 0, 1, 1, 1, 1),
            bitSet(1, 0, 0, 1, 1, 0, 0),
        )
        val system = XorEquationSystem(equations.size, varCount, equations, variables)

        println(system)
        solveXorEquationSystem(system)
        println(system)
    }

    test("test equation vars extension hypothesis - not confirmed") {
        val rows = 8
        val cols = 4

        while (true) {
            val system = XorEquationSystem(rows, cols)

            //#region Init system
            var i = 0
            var j: Int
            while (i < system.rows) {
                j = 0
                while (j < system.cols) {
                    system.equations[i][j] = Random.nextBoolean()
                    j++
                }
                i++
            }
            //#endregion

            if (system.isPartiallyEmpty()) continue

            val clonedSystem = system.clone()

            solveXorEquationSystem(clonedSystem)

            if (!clonedSystem.isPartiallyEmpty()) continue

            val extendedSystem = XorEquationSystem(rows, rows)

            val generatedVars = Array(cols) {
                val group = BitGroup(rows)
                i = 0
                while (i < group.size) {
                    group[i] = Random.nextBoolean()
                    i++
                }
                group
            }

            //#region Init Extended System
            i = 0
            while (i < extendedSystem.rows) {
                var k = system.equations[i].nextSetBit(0)
                while (k >= 0) {
                    extendedSystem.equations[i].xor(generatedVars[k].bitSet)
                    k = system.equations[i].nextSetBit(k + 1)
                }
                i++
            }
            //#endregion

            solveXorEquationSystem(extendedSystem)

            if (extendedSystem.isPartiallyEmpty()) continue

            println(system)
            println(extendedSystem)
            println(generatedVars.contentDeepToString())
        }
    }
})
