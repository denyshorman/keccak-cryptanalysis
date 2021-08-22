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

        solveXorEquations(system)

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

        solveXorEquations(system)

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

        solveXorEquations(system)

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

        solveXorEquations(system)

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
                solveXorEquations(system)
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
                solveXorEquations(system)
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
            solveXorEquations(system)
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
        solveXorEquations(system)
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

            solveXorEquations(clonedSystem)

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

            solveXorEquations(extendedSystem)

            if (extendedSystem.isPartiallyEmpty()) continue

            println(system)
            println(extendedSystem)
            println(generatedVars.contentDeepToString())
        }
    }

    test("test some xor eq") {
        val eqSystem = XorEquationSystem(9, 9)
        eqSystem.equations[0][0] = true
        eqSystem.equations[0][1] = true
        eqSystem.equations[0][2] = true
        eqSystem.equations[0][3] = true
        eqSystem.equations[0][4] = false
        eqSystem.equations[0][5] = false
        eqSystem.equations[0][6] = false
        eqSystem.equations[0][7] = false
        eqSystem.equations[0][8] = false
        eqSystem.results[0] = true

        eqSystem.equations[1][0] = true
        eqSystem.equations[1][1] = true
        eqSystem.equations[1][2] = false
        eqSystem.equations[1][3] = false
        eqSystem.equations[1][4] = true
        eqSystem.equations[1][5] = false
        eqSystem.equations[1][6] = false
        eqSystem.equations[1][7] = false
        eqSystem.equations[1][8] = false
        eqSystem.results[1] = false

        eqSystem.equations[2][0] = true
        eqSystem.equations[2][1] = false
        eqSystem.equations[2][2] = false
        eqSystem.equations[2][3] = true
        eqSystem.equations[2][4] = true
        eqSystem.equations[2][5] = false
        eqSystem.equations[2][6] = false
        eqSystem.equations[2][7] = false
        eqSystem.equations[2][8] = false
        eqSystem.results[2] = true

        eqSystem.equations[3][0] = false
        eqSystem.equations[3][1] = true
        eqSystem.equations[3][2] = true
        eqSystem.equations[3][3] = false
        eqSystem.equations[3][4] = false
        eqSystem.equations[3][5] = true
        eqSystem.equations[3][6] = false
        eqSystem.equations[3][7] = false
        eqSystem.equations[3][8] = false
        eqSystem.results[3] = false

        eqSystem.equations[4][0] = true
        eqSystem.equations[4][1] = true
        eqSystem.equations[4][2] = false
        eqSystem.equations[4][3] = false
        eqSystem.equations[4][4] = false
        eqSystem.equations[4][5] = false
        eqSystem.equations[4][6] = true
        eqSystem.equations[4][7] = false
        eqSystem.equations[4][8] = false
        eqSystem.results[4] = false

        eqSystem.equations[5][0] = false
        eqSystem.equations[5][1] = true
        eqSystem.equations[5][2] = false
        eqSystem.equations[5][3] = false
        eqSystem.equations[5][4] = false
        eqSystem.equations[5][5] = true
        eqSystem.equations[5][6] = true
        eqSystem.equations[5][7] = false
        eqSystem.equations[5][8] = false
        eqSystem.results[5] = true

        eqSystem.equations[6][0] = false
        eqSystem.equations[6][1] = true
        eqSystem.equations[6][2] = true
        eqSystem.equations[6][3] = false
        eqSystem.equations[6][4] = false
        eqSystem.equations[6][5] = false
        eqSystem.equations[6][6] = false
        eqSystem.equations[6][7] = true
        eqSystem.equations[6][8] = false
        eqSystem.results[6] = true

        eqSystem.equations[7][0] = true
        eqSystem.equations[7][1] = false
        eqSystem.equations[7][2] = false
        eqSystem.equations[7][3] = false
        eqSystem.equations[7][4] = false
        eqSystem.equations[7][5] = false
        eqSystem.equations[7][6] = false
        eqSystem.equations[7][7] = false
        eqSystem.equations[7][8] = true
        eqSystem.results[7] = true

        eqSystem.equations[8][0] = false
        eqSystem.equations[8][1] = false
        eqSystem.equations[8][2] = true
        eqSystem.equations[8][3] = false
        eqSystem.equations[8][4] = false
        eqSystem.equations[8][5] = false
        eqSystem.equations[8][6] = false
        eqSystem.equations[8][7] = true
        eqSystem.equations[8][8] = true
        eqSystem.results[8] = true

        println(eqSystem)

        solveXorEquations(eqSystem)

        println("-------")
        println(eqSystem)
    }

    test("test some xor eq2") {
        val eqSystem = XorEquationSystem(12, 12)
        
        //#region assign
        eqSystem.equations[0][0] = true
        eqSystem.equations[0][1] = false
        eqSystem.equations[0][2] = true
        eqSystem.equations[0][3] = false
        eqSystem.equations[0][4] = true
        eqSystem.equations[0][5] = false
        eqSystem.equations[0][6] = false
        eqSystem.equations[0][7] = false
        eqSystem.equations[0][8] = false
        eqSystem.equations[0][9] = false
        eqSystem.equations[0][10] = false
        eqSystem.equations[0][11] = false
        eqSystem.results[0] = false
        //#endregion

        //#region assign
        eqSystem.equations[1][0] = false
        eqSystem.equations[1][1] = false
        eqSystem.equations[1][2] = true
        eqSystem.equations[1][3] = true
        eqSystem.equations[1][4] = false
        eqSystem.equations[1][5] = true
        eqSystem.equations[1][6] = false
        eqSystem.equations[1][7] = false
        eqSystem.equations[1][8] = false
        eqSystem.equations[1][9] = false
        eqSystem.equations[1][10] = false
        eqSystem.equations[1][11] = false
        eqSystem.results[1] = false
        //#endregion

        //#region assign
        eqSystem.equations[2][0] = true
        eqSystem.equations[2][1] = false
        eqSystem.equations[2][2] = false
        eqSystem.equations[2][3] = false
        eqSystem.equations[2][4] = true
        eqSystem.equations[2][5] = true
        eqSystem.equations[2][6] = false
        eqSystem.equations[2][7] = false
        eqSystem.equations[2][8] = false
        eqSystem.equations[2][9] = false
        eqSystem.equations[2][10] = false
        eqSystem.equations[2][11] = false
        eqSystem.results[2] = true
        //#endregion

        //#region assign
        eqSystem.equations[3][0] = true
        eqSystem.equations[3][1] = true
        eqSystem.equations[3][2] = true
        eqSystem.equations[3][3] = false
        eqSystem.equations[3][4] = false
        eqSystem.equations[3][5] = false
        eqSystem.equations[3][6] = true
        eqSystem.equations[3][7] = false
        eqSystem.equations[3][8] = false
        eqSystem.equations[3][9] = false
        eqSystem.equations[3][10] = false
        eqSystem.equations[3][11] = false
        eqSystem.results[3] = false
        //#endregion

        //#region assign
        eqSystem.equations[4][0] = false
        eqSystem.equations[4][1] = true
        eqSystem.equations[4][2] = false
        eqSystem.equations[4][3] = true
        eqSystem.equations[4][4] = false
        eqSystem.equations[4][5] = false
        eqSystem.equations[4][6] = false
        eqSystem.equations[4][7] = true
        eqSystem.equations[4][8] = false
        eqSystem.equations[4][9] = false
        eqSystem.equations[4][10] = false
        eqSystem.equations[4][11] = false
        eqSystem.results[4] = true
        //#endregion

        //#region assign
        eqSystem.equations[5][0] = false
        eqSystem.equations[5][1] = true
        eqSystem.equations[5][2] = false
        eqSystem.equations[5][3] = false
        eqSystem.equations[5][4] = false
        eqSystem.equations[5][5] = false
        eqSystem.equations[5][6] = true
        eqSystem.equations[5][7] = true
        eqSystem.equations[5][8] = false
        eqSystem.equations[5][9] = false
        eqSystem.equations[5][10] = false
        eqSystem.equations[5][11] = false
        eqSystem.results[5] = true
        //#endregion

        //#region assign
        eqSystem.equations[6][0] = false
        eqSystem.equations[6][1] = true
        eqSystem.equations[6][2] = true
        eqSystem.equations[6][3] = true
        eqSystem.equations[6][4] = false
        eqSystem.equations[6][5] = false
        eqSystem.equations[6][6] = false
        eqSystem.equations[6][7] = false
        eqSystem.equations[6][8] = true
        eqSystem.equations[6][9] = false
        eqSystem.equations[6][10] = false
        eqSystem.equations[6][11] = false
        eqSystem.results[6] = false
        //#endregion

        //#region assign
        eqSystem.equations[7][0] = false
        eqSystem.equations[7][1] = true
        eqSystem.equations[7][2] = true
        eqSystem.equations[7][3] = false
        eqSystem.equations[7][4] = false
        eqSystem.equations[7][5] = false
        eqSystem.equations[7][6] = false
        eqSystem.equations[7][7] = false
        eqSystem.equations[7][8] = false
        eqSystem.equations[7][9] = true
        eqSystem.equations[7][10] = false
        eqSystem.equations[7][11] = false
        eqSystem.results[7] = false
        //#endregion

        //#region assign
        eqSystem.equations[8][0] = false
        eqSystem.equations[8][1] = false
        eqSystem.equations[8][2] = true
        eqSystem.equations[8][3] = false
        eqSystem.equations[8][4] = false
        eqSystem.equations[8][5] = false
        eqSystem.equations[8][6] = false
        eqSystem.equations[8][7] = false
        eqSystem.equations[8][8] = true
        eqSystem.equations[8][9] = true
        eqSystem.equations[8][10] = false
        eqSystem.equations[8][11] = false
        eqSystem.results[8] = true
        //#endregion

        //#region assign
        eqSystem.equations[9][0] = true
        eqSystem.equations[9][1] = true
        eqSystem.equations[9][2] = true
        eqSystem.equations[9][3] = true
        eqSystem.equations[9][4] = false
        eqSystem.equations[9][5] = false
        eqSystem.equations[9][6] = false
        eqSystem.equations[9][7] = false
        eqSystem.equations[9][8] = false
        eqSystem.equations[9][9] = false
        eqSystem.equations[9][10] = true
        eqSystem.equations[9][11] = false
        eqSystem.results[9] = true
        //#endregion

        //#region assign
        eqSystem.equations[10][0] = true
        eqSystem.equations[10][1] = false
        eqSystem.equations[10][2] = true
        eqSystem.equations[10][3] = false
        eqSystem.equations[10][4] = false
        eqSystem.equations[10][5] = false
        eqSystem.equations[10][6] = false
        eqSystem.equations[10][7] = false
        eqSystem.equations[10][8] = false
        eqSystem.equations[10][9] = false
        eqSystem.equations[10][10] = false
        eqSystem.equations[10][11] = true
        eqSystem.results[10] = true
        //#endregion

        //#region assign
        eqSystem.equations[11][0] = false
        eqSystem.equations[11][1] = false
        eqSystem.equations[11][2] = false
        eqSystem.equations[11][3] = true
        eqSystem.equations[11][4] = false
        eqSystem.equations[11][5] = false
        eqSystem.equations[11][6] = false
        eqSystem.equations[11][7] = false
        eqSystem.equations[11][8] = false
        eqSystem.equations[11][9] = false
        eqSystem.equations[11][10] = true
        eqSystem.equations[11][11] = true
        eqSystem.results[11] = true
        //#endregion


        println(eqSystem)

        solveXorEquations(eqSystem)

        println("-------")
        println(eqSystem)
    }
})
