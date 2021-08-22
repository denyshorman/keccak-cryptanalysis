package keccak

import io.kotest.core.spec.style.FunSpec
import kotlin.random.Random

class ParametrizedEquationSystemSolverTest : FunSpec({
    test("random solveParametrizedEquationSystem") {
        while (true) {
            val system = ParametrizedEquationSystem(4, 4)

            var i = 0
            while (i < system.rows) {
                var j = 0
                while (j < system.cols) {
                    system.equations[i][j] = Random.nextBoolean()
                    j++
                }
                i++
            }

            println("-----------------------------")
            println("==given==")
            println(system)
            solveParametrizedEquationSystem(system)
            println("==solved==")
            println(system)

            Thread.sleep(800)
        }
    }
})
