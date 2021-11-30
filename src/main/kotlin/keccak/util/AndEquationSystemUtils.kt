package keccak.util

import keccak.*
import keccak.AndEquationSystem
import keccak.XorEquationSystem
import java.io.File
import java.util.*
import kotlin.math.min
import kotlin.random.Random

private val AndEquationBinaryPattern = "^\\(([01]+)\\|([01])\\)\\(([01]+)\\|([01])\\)\\s*=\\s*([01]+)\\|([01])$".toRegex()
private val AndEquationHumanPattern = "^\\s*\\(?(.*?)\\)?\\s*\\*\\s*\\(?(.*?)\\)?\\s*=\\s*(.*?)$".toRegex()

fun AndEquationSystem(rows: Int, cols: Int, vararg equations: String): AndEquationSystem {
    val system = AndEquationSystem(rows, cols)

    var i = 0
    while (i < equations.size) {
        system.set(i, equations[i], false)
        i++
    }

    return system
}

fun AndEquationSystem.set(eqIndex: Int, equation: String, humanReadable: Boolean) {
    if (humanReadable) {
        val matched = AndEquationHumanPattern.matchEntire(equation)
            ?: throw IllegalArgumentException("Equation is not correct")
        val (left, right, result) = matched.destructured

        left.split("+").asSequence().map { it.trim() }.forEach { term ->
            if (term[0] == '1') {
                andOpLeftResults[eqIndex] = !andOpLeftResults[eqIndex]
            } else if (term[0] == '0') {
                // ignore
            } else {
                equations[eqIndex].andOpLeft.set(term.mapToOnlyDigits().toInt())
            }
        }

        right.split("+").asSequence().map { it.trim() }.forEach { term ->
            if (term[0] == '1') {
                andOpRightResults[eqIndex] = !andOpRightResults[eqIndex]
            } else if (term[0] == '0') {
                // ignore
            } else {
                equations[eqIndex].andOpRight.set(term.mapToOnlyDigits().toInt())
            }
        }

        result.split("+").asSequence().map { it.trim() }.forEach { term ->
            if (term[0] == '1') {
                rightXorResults[eqIndex] = !rightXorResults[eqIndex]
            } else if (term[0] == '0') {
                // ignore
            } else {
                equations[eqIndex].rightXor.set(term.mapToOnlyDigits().toInt())
            }
        }
    } else {
        val matched = AndEquationBinaryPattern.matchEntire(equation)
            ?: throw IllegalArgumentException("Equation is not correct")
        val (l, lv, r, rv, x, xv) = matched.destructured

        var j = 0
        while (j < l.length) {
            equations[eqIndex].andOpLeft[j] = l[j].toBoolean()
            equations[eqIndex].andOpRight[j] = r[j].toBoolean()
            equations[eqIndex].rightXor[j] = x[j].toBoolean()

            j++
        }

        andOpLeftResults[eqIndex] = lv.toBoolean()
        andOpRightResults[eqIndex] = rv.toBoolean()
        rightXorResults[eqIndex] = xv.toBoolean()
    }
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

fun AndEquationSystem.toNodeEquationSystem(varPrefix: String = "x", varOffset: Int = 1): NodeEquationSystem {
    val variables = Array(cols) { Variable("$varPrefix${it + varOffset}") }

    fun convert(vars: BitSet, value: Boolean): List<Node> {
        val left = LinkedList<Node>()

        var bitIndex = vars.nextSetBit(0)
        while (bitIndex >= 0) {
            left.add(Variable("$varPrefix${bitIndex + varOffset}"))
            bitIndex = vars.nextSetBit(bitIndex + 1)
        }

        if (value) {
            left.add(Bit(true))
        }

        return left
    }

    val equations = Array(rows) { eqIndex ->
        val left = convert(equations[eqIndex].andOpLeft, andOpLeftResults[eqIndex])
        val right = convert(equations[eqIndex].andOpRight, andOpRightResults[eqIndex])
        val center = convert(equations[eqIndex].rightXor, rightXorResults[eqIndex])

        NodeEquation(And(Xor(left), Xor(right)), Xor(center))
    }

    return NodeEquationSystem(equations, variables)
}

fun AndEquationSystem.toCharacteristicEquation(
    characteristicVarPrefix: String = "a",
    varPrefix: String = "x",
    useZeroPosition: Boolean = true,
): Node {
    val lSystem = XorEquationSystem(rows, cols, Array(equations.size) { equations[it].andOpLeft }, andOpLeftResults)
    val rSystem = XorEquationSystem(rows, cols, Array(equations.size) { equations[it].andOpRight }, andOpRightResults)
    val xSystem = XorEquationSystem(rows, cols, Array(equations.size) { equations[it].rightXor }, rightXorResults)

    val lChar = lSystem.toCharacteristicEquation(characteristicVarPrefix, varPrefix, useZeroPosition)
    val rChar = rSystem.toCharacteristicEquation(characteristicVarPrefix, varPrefix, useZeroPosition)
    val xChar = xSystem.toCharacteristicEquation(characteristicVarPrefix, varPrefix, useZeroPosition)

    return lChar*rChar + xChar
}

fun AndEquationSystem.toFile(
    file: File,
    eqStartIndex: Int = 0,
    eqCount: Int = rows,
    humanReadable: Boolean = false,
) {
    file.outputStream().writer(Charsets.US_ASCII).use { writer ->
        var eqIndex = eqStartIndex
        val limit = min(eqIndex + eqCount, rows)

        if (humanReadable) {
            while (eqIndex < limit) {
                val left = equations[eqIndex].andOpLeft.toXorString(andOpLeftResults[eqIndex])
                val right = equations[eqIndex].andOpRight.toXorString(andOpRightResults[eqIndex])
                val result = equations[eqIndex].rightXor.toXorString(rightXorResults[eqIndex])

                if (!(left == "0" && right == "0" && result == "0")) {
                    if (left.contains('+')) {
                        writer.append('(')
                        writer.append(left)
                        writer.append(')')
                    } else {
                        writer.append(left)
                    }

                    writer.append(" * ")

                    if (right.contains('+')) {
                        writer.append('(')
                        writer.append(right)
                        writer.append(')')
                    } else {
                        writer.append(right)
                    }

                    writer.append(" = ")
                    writer.appendLine(result)
                }

                eqIndex++
            }
        } else {
            while (eqIndex < limit) {
                writer.append('(')
                writer.append(equations[eqIndex].andOpLeft.toString(cols))
                if (andOpLeftResults[eqIndex]) {
                    writer.append("|1")
                } else {
                    writer.append("|0")
                }
                writer.append(")(")

                writer.append(equations[eqIndex].andOpRight.toString(cols))
                if (andOpRightResults[eqIndex]) {
                    writer.append("|1")
                } else {
                    writer.append("|0")
                }
                writer.append(") = ")

                writer.append(equations[eqIndex].rightXor.toString(cols))

                if (rightXorResults[eqIndex]) {
                    writer.append("|1")
                } else {
                    writer.append("|0")
                }

                eqIndex++

                if (eqIndex != limit) {
                    writer.append('\n')
                }
            }
        }
    }
}

fun randomAndEquationSystem(
    rows: Int,
    cols: Int,
    allowIncompatibleSystem: Boolean = false,
    equalToZero: Boolean = false,
    solutionsCount: Int? = null,
    random: Random = Random,
): Pair<BitSet, AndEquationSystem> {
    val system = AndEquationSystem(rows, cols)
    val solution = randomBitSet(cols, random)

    while (true) {
        var i = 0
        while (i < rows) {
            while (true) {
                system.equations[i].andOpLeft.randomize(cols, random)
                system.equations[i].andOpRight.randomize(cols, random)
                system.andOpLeftResults[i] = random.nextBoolean()
                system.andOpRightResults[i] = random.nextBoolean()

                if (!equalToZero) {
                    system.equations[i].rightXor.clear()
                    system.equations[i].rightXor.set(i)
                    system.rightXorResults[i] = false
                }

                if (system.equations[i].andOpLeft.isEmpty || system.equations[i].andOpRight.isEmpty || (system.equations[i].rightXor.isEmpty xor equalToZero)) {
                    continue
                }

                val x0 = system.equations[i].andOpLeft.evaluate(solution) xor system.andOpLeftResults[i]
                val x1 = system.equations[i].andOpRight.evaluate(solution) xor system.andOpRightResults[i]
                val x2 = system.equations[i].rightXor.evaluate(solution) xor system.rightXorResults[i]

                if (allowIncompatibleSystem || (x0 && x1) == x2) {
                    break
                }
            }

            i++
        }

        if (solutionsCount == null || system.countSolutions() == solutionsCount) {
            break
        }
    }

    return Pair(solution, system)
}
