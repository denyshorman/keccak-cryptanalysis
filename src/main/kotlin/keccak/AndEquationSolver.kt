package keccak

import keccak.util.toXorEquationSystem

fun solveAndEquationSystem(system: AndEquationSystem): XorEquationSystem {
    val xorEqSystem = system.toXorEquationSystem()
    solveXorEquationSystem(xorEqSystem)
    return xorEqSystem
}
