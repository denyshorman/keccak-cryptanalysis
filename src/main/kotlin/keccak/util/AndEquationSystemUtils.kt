package keccak.util

import keccak.AndEquationSystem
import keccak.XorEquationSystem

private val AndEquationPattern = "^\\(([01]+)\\|([01])\\)\\(([01]+)\\|([01])\\)\\s*=\\s*([01]+)\\|([01])$".toRegex()

fun AndEquationSystem(rows: Int, cols: Int, vararg equations: String): AndEquationSystem {
    val system = AndEquationSystem(rows, cols)

    var i = 0
    while (i < equations.size) {
        val eq = equations[i]

        val matched = AndEquationPattern.matchEntire(eq) ?: throw RuntimeException("Equation is not correct")

        val (l, lv, r, rv, x, xv) = matched.destructured

        var j = 0
        while (j < l.length) {
            system.equations[i].andOpLeft[j] = l[j].toBoolean()
            system.equations[i].andOpRight[j] = r[j].toBoolean()
            system.equations[i].rightXor[j] = x[j].toBoolean()

            j++
        }

        system.andOpLeftResults[i] = lv.toBoolean()
        system.andOpRightResults[i] = rv.toBoolean()
        system.rightXorResults[i] = xv.toBoolean()

        i++
    }

    return system
}

fun AndEquationSystem.toXorEquationSystem(): XorEquationSystem {
    val xorEquations = rows * 3
    val xorEquationsVars = cols + rows * 2
    val xorEquationSystem = XorEquationSystem(xorEquations, xorEquationsVars)

    var andEqIndex = 0
    var xorEqIndex = 0
    var addVarIndex = cols

    while (andEqIndex < rows) {
        xorEquationSystem.equations[xorEqIndex].xor(equations[andEqIndex].andOpLeft)
        xorEquationSystem.equations[xorEqIndex].set(addVarIndex)
        xorEquationSystem.results[xorEqIndex] = xorEquationSystem.results[xorEqIndex] xor andOpLeftResults[andEqIndex]
        xorEqIndex++

        xorEquationSystem.equations[xorEqIndex].xor(equations[andEqIndex].andOpRight)
        xorEquationSystem.equations[xorEqIndex].set(addVarIndex + 1)
        xorEquationSystem.results[xorEqIndex] = xorEquationSystem.results[xorEqIndex] xor andOpRightResults[andEqIndex]
        xorEqIndex++

        xorEquationSystem.equations[xorEqIndex].xor(equations[andEqIndex].rightXor)
        xorEquationSystem.equations[xorEqIndex].set(addVarIndex)
        xorEquationSystem.equations[xorEqIndex].set(addVarIndex + 1)
        xorEquationSystem.results[xorEqIndex] = xorEquationSystem.results[xorEqIndex] xor rightXorResults[andEqIndex] xor true

        addVarIndex += 2
        xorEqIndex++
        andEqIndex++
    }

    return xorEquationSystem
}
