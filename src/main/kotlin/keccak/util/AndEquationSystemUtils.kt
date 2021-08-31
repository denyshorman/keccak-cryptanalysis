package keccak.util

import keccak.AndEquationSystem
import keccak.XorEquationSystem
import java.util.*
import kotlin.random.Random

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
    val xorEquationSystem = XorEquationSystem(rows, cols)

    var i = 0
    while (i < rows) {
        xorEquationSystem.equations[i].xor(equations[i].andOpLeft)
        xorEquationSystem.equations[i].xor(equations[i].andOpRight)
        xorEquationSystem.equations[i].xor(equations[i].rightXor)

        xorEquationSystem.results[i] = xorEquationSystem.results[i]
            .xor(andOpLeftResults[i])
            .xor(andOpRightResults[i])
            .xor(rightXorResults[i])
            .xor(true)

        i++
    }

    return xorEquationSystem
}

fun randomAndEquationSystem(
    rows: Int,
    cols: Int,
    random: Random = Random,
): Pair<BitSet, AndEquationSystem> {
    val system = AndEquationSystem(rows, cols)

    val solution = randomBitSet(cols, random)

    var i = 0
    while (i < rows) {
        while (true) {
            system.equations[i].andOpLeft.randomize(cols, random)
            system.equations[i].andOpRight.randomize(cols, random)
            system.equations[i].rightXor.clear()
            system.equations[i].rightXor.set(i)

            system.andOpLeftResults[i] = random.nextBoolean()
            system.andOpRightResults[i] = random.nextBoolean()
            system.rightXorResults[i] = false

            if (system.equations[i].andOpLeft.isEmpty || system.equations[i].andOpRight.isEmpty || system.equations[i].rightXor.isEmpty) {
                continue
            }

            val x0 = system.equations[i].andOpLeft.evaluate(solution) xor system.andOpLeftResults[i]
            val x1 = system.equations[i].andOpRight.evaluate(solution) xor system.andOpRightResults[i]
            val x2 = system.equations[i].rightXor.evaluate(solution) xor system.rightXorResults[i]

            if ((x0 && x1) == x2) {
                break
            }
        }

        i++
    }

    return Pair(solution, system)
}
