package keccak

import io.kotest.core.spec.style.FunSpec

class ParametrizedEquationSystemTest : FunSpec({
    test("toString") {
        val system = ParametrizedEquationSystem(2, 2)
        system.equations[0][0] = true
        system.equations[1][1] = true

        system.results[0][0] = true
        system.results[0][1] = true
        system.results[1][1] = true

        println(system)
    }
})
