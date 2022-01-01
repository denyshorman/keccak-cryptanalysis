package keccak.util

import keccak.*
import java.io.File
import java.util.*
import kotlin.math.min
import kotlin.random.Random

private val AndEquationBinaryPattern = "^\\(([01]+)\\|([01])\\)\\(([01]+)\\|([01])\\)\\s*=\\s*([01]+)\\|([01])$".toRegex()
private val AndEquationHumanPattern = "^\\s*\\(?(.*?)\\)?\\s*\\*\\s*\\(?(.*?)\\)?\\s*=\\s*(.*?)$".toRegex()

fun AndEquationSystem(rows: Int, cols: Int, humanReadable: Boolean, vararg equations: String): AndEquationSystem {
    val system = AndEquationSystem(rows, cols)

    var i = 0
    while (i < equations.size) {
        system.set(i, equations[i], humanReadable)
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

    return NodeEquationSystem(equations)
}

fun AndEquationSystem.toCharacteristicEquation(
    characteristicVarPrefix: String = "a",
    varPrefix: String = "x",
    useZeroPosition: Boolean = true,
): Node {
    val lSystem = XorEquationSystem(cols, Array(equations.size) { equations[it].andOpLeft }, andOpLeftResults)
    val rSystem = XorEquationSystem(cols, Array(equations.size) { equations[it].andOpRight }, andOpRightResults)
    val xSystem = XorEquationSystem(cols, Array(equations.size) { equations[it].rightXor }, rightXorResults)

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

fun AndEquationSystem.toHumanString(
    varPrefix: String = "x",
    varOffset: Int = 0,
): String {
    val sb = StringBuilder()
    var i = 0
    while (i < rows) {
        val l = equations[i].andOpLeft.toXorString(andOpLeftResults[i], varPrefix, varOffset)
        val r = equations[i].andOpRight.toXorString(andOpRightResults[i], varPrefix, varOffset)
        val x = equations[i].rightXor.toXorString(rightXorResults[i], varPrefix, varOffset)
        sb.appendLine("($l)*($r) = $x")
        i++
    }
    return sb.toString()
}

fun AndEquationSystem.rotate(solution: BitSet, left: Boolean, right: Boolean) {
    var i = 0
    while (i < rows) {
        val leftFreeBit = andOpLeftResults[i]
        val rightFreeBit = andOpRightResults[i]

        val leftActual = equations[i].andOpLeft.evaluate(solution) xor leftFreeBit
        val rightActual = equations[i].andOpRight.evaluate(solution) xor rightFreeBit

        if (!left && !right) {
            if (!leftActual && rightActual) {
                equations[i].andOpRight.xor(equations[i].andOpLeft)
                andOpRightResults.set(i, !rightFreeBit xor leftFreeBit)
            } else if (leftActual && !rightActual) {
                equations[i].andOpLeft.xor(equations[i].andOpRight)
                andOpLeftResults.set(i, !leftFreeBit xor rightFreeBit)
            }
        } else if (!left) {
            if (!leftActual && !rightActual) {
                equations[i].andOpRight.xor(equations[i].andOpLeft)
                andOpRightResults.set(i, !rightFreeBit xor leftFreeBit)
            } else if (!rightActual) {
                val tmp = equations[i].andOpRight
                equations[i].andOpRight = equations[i].andOpLeft
                equations[i].andOpLeft = tmp

                andOpLeftResults.set(i, rightFreeBit)
                andOpRightResults.set(i, leftFreeBit)
            }
        } else if (!right) {
            if (!leftActual && !rightActual) {
                equations[i].andOpLeft.xor(equations[i].andOpRight)
                andOpLeftResults.set(i, !leftFreeBit xor rightFreeBit)
            } else if (!leftActual) {
                val tmp = equations[i].andOpRight
                equations[i].andOpRight = equations[i].andOpLeft
                equations[i].andOpLeft = tmp

                andOpLeftResults.set(i, rightFreeBit)
                andOpRightResults.set(i, leftFreeBit)
            }
        }

        i++
    }
}

fun AndEquationSystem.solveExperimental(): XorEquationSystem {
    val invertedXorSystem = invertToXorSystem()
    invertedXorSystem.solve(skipValidation = true)

    //region debug
    val invertedXorSystemNode = invertedXorSystem.toNodeEquationSystem(varOffset = 0)
    println(invertedXorSystemNode)
    println()
    //endregion

    var startIndex = 0
    var i = 0
    while (i < invertedXorSystem.rows) {
        val firstSetBitIndex = invertedXorSystem.equations[i].nextSetBit(0)
        if (firstSetBitIndex == cols) {
            startIndex = i
            break
        }
        i++
    }

    val invertedVariableRows = invertedXorSystem.rows - cols
    val invertedVariableCols = rows * 2

    val system2 = XorEquationSystem(
        rows = (invertedVariableRows*invertedVariableRows + 1)*(rows + 1) - 1,
        cols = combinationsWithRepetition(invertedVariableCols.toLong(), 2).toInt(),
    )

    val invertedAndSystem = invertToAndSystem()

    invertedAndSystem.substitute(
        xorSystem = invertedXorSystem,
        substituteLeft = false,
        substituteRight = false,
    )

    //region debug
    val invertedAndSystemNode = invertedAndSystem.toNodeEquationSystem(varOffset = 0)
    println(invertedAndSystemNode)
    println()
    //endregion

    var k = 0
    i = startIndex

    while (i < invertedXorSystem.rows) {
        var j = startIndex
        while (j < invertedXorSystem.rows) {
            invertedXorSystem.equations[i].iterateOverAllSetBits { i0 ->
                invertedXorSystem.equations[j].iterateOverAllSetBits { j0 ->
                    val newIndex = calcCombinationIndex(i0 - startIndex, j0 - startIndex, invertedVariableCols)
                    system2.equations[k].flip(newIndex)
                }
            }

            val a = !invertedXorSystem.results[i]
            val b = !invertedXorSystem.results[j]

            if (a) {
                invertedXorSystem.equations[j].iterateOverAllSetBits { j0 ->
                    val newIndex = calcCombinationIndex(j0 - startIndex, invertedVariableCols)
                    system2.equations[k].flip(newIndex)
                }
            }

            if (b) {
                invertedXorSystem.equations[i].iterateOverAllSetBits { i0 ->
                    val newIndex = calcCombinationIndex(i0 - startIndex, invertedVariableCols)
                    system2.equations[k].flip(newIndex)
                }
            }

            if (!(a && b)) {
                system2.results.set(k)
            }

            k++
            j++
        }
        i++
    }

    i = 0
    while (i < invertedAndSystem.rows) {
        invertedAndSystem.equations[i].andOpLeft.iterateOverAllSetBits { i0 ->
            invertedAndSystem.equations[i].andOpRight.iterateOverAllSetBits { j0 ->
                val newIndex = calcCombinationIndex(i0 - startIndex, j0 - startIndex, invertedVariableCols)
                system2.equations[k].flip(newIndex)
            }
        }

        val a = invertedAndSystem.andOpLeftResults[i]
        val b = invertedAndSystem.andOpRightResults[i]

        if (a) {
            invertedAndSystem.equations[i].andOpRight.iterateOverAllSetBits { j0 ->
                val newIndex = calcCombinationIndex(j0 - startIndex, invertedVariableCols)
                system2.equations[k].flip(newIndex)
            }
        }

        if (b) {
            invertedAndSystem.equations[i].andOpLeft.iterateOverAllSetBits { i0 ->
                val newIndex = calcCombinationIndex(i0 - startIndex, invertedVariableCols)
                system2.equations[k].flip(newIndex)
            }
        }

        if (a && b) {
            system2.results.set(k)
        }

        invertedAndSystem.equations[i].rightXor.iterateOverAllSetBits { i0 ->
            val newIndex = calcCombinationIndex(i0 - startIndex, invertedVariableCols)
            system2.equations[k].flip(newIndex)
        }

        system2.results.xor(k, invertedAndSystem.rightXorResults[i])

        k++
        i++
    }

    val left = BitSet(invertedXorSystem.cols)
    val right = BitSet(invertedXorSystem.cols)
    var leftBit: Boolean
    var rightBit: Boolean

    i = 0
    while (i < invertedAndSystem.rows) {
        var x = startIndex
        while (x < invertedXorSystem.rows) {
            var y = startIndex
            while (y < invertedXorSystem.rows) {
                left.clear()
                right.clear()

                left.xor(invertedAndSystem.equations[i].andOpLeft)
                right.xor(invertedAndSystem.equations[i].andOpRight)

                left.xor(invertedXorSystem.equations[x])
                right.xor(invertedXorSystem.equations[y])

                leftBit = invertedAndSystem.andOpLeftResults[i] xor invertedXorSystem.results[x]
                rightBit = invertedAndSystem.andOpRightResults[i] xor invertedXorSystem.results[y]

                left.iterateOverAllSetBits { i0 ->
                    right.iterateOverAllSetBits { j0 ->
                        val newIndex = calcCombinationIndex(i0 - startIndex, j0 - startIndex, invertedVariableCols)
                        system2.equations[k].flip(newIndex)
                    }
                }

                if (leftBit) {
                    right.iterateOverAllSetBits { j0 ->
                        val newIndex = calcCombinationIndex(j0 - startIndex, invertedVariableCols)
                        system2.equations[k].flip(newIndex)
                    }
                }

                if (rightBit) {
                    left.iterateOverAllSetBits { i0 ->
                        val newIndex = calcCombinationIndex(i0 - startIndex, invertedVariableCols)
                        system2.equations[k].flip(newIndex)
                    }
                }

                if (leftBit && rightBit) {
                    system2.results.set(k)
                }

                invertedAndSystem.equations[i].rightXor.iterateOverAllSetBits { i0 ->
                    val newIndex = calcCombinationIndex(i0 - startIndex, invertedVariableCols)
                    system2.equations[k].flip(newIndex)
                }

                system2.results.xor(k, invertedAndSystem.rightXorResults[i])

                k++
                y++
            }
            x++
        }

        i++
    }

    //region debug
    val solutionSystemNode = system2.toSecondOrderNodeEquationSystem(varOffset = startIndex)
    println(solutionSystemNode)
    println()
    //endregion

    return system2
}