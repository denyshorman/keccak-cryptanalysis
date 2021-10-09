package keccak

import mu.KotlinLogging
import java.util.*

class XorAndEquationSystem(
    val xorSystem: XorEquationSystem,
    val andSystem: AndEquationSystem,
) {
    val cols: Int get() = xorSystem.cols

    fun substituteAndWithXor(mask: BitSet? = null) {
        andSystem.substitute(xorSystem, mask)
    }

    fun substitute(index: Int, value: Boolean) {
        xorSystem.substitute(index, value)
        andSystem.substitute(index, value)
    }

    fun substitute(values: Iterable<Pair<Int, Boolean>>) {
        xorSystem.substitute(values)
        andSystem.substitute(values)
    }

    fun isValid(solution: BitSet): Boolean {
        return xorSystem.isValid(solution) && andSystem.isValid(solution)
    }

    /*fun invert(
        hash: XorEquationSystem,
        constraints: List<Keccak256EqSystemGenerator.Constraint>,
        constantVarsStartIndex: Int,
    ): XorAndEquationSystem {
        //#region Prerequisite: hash equations must be prepared
        //hash.solve()
        //#endregion

        val constraintVarsCount = hash.cols - constantVarsStartIndex
        val rows = constraintVarsCount * 2
        val cols = constraintVarsCount * 3 + constantVarsStartIndex

        val xorSystem = XorEquationSystem(rows, cols)
        val andSystem = AndEquationSystem(constraintVarsCount, cols)

        var xorEqIndex = 0
        var andEqIndex = 0

        for (constraint in constraints) {
            var i = 0
            while (i < constraint.leftSystem.rows) {
                var j = 0
                var varExists = false
                val constraintVarIndex = constraint.varSystem.equations[i].nextSetBit(constantVarsStartIndex)

                while (j < hash.rows) {
                    if (!varExists && hash.equations[j][constraintVarIndex]) {
                        varExists = true
                    }

                    val idx = hash.equations[j].nextSetBit(0)

                    if (idx != -1 && idx < constantVarsStartIndex) {
                        if (constraint.leftSystem.equations[i][idx]) {
                            constraint.leftSystem.equations[i].xor(hash.equations[j])
                            constraint.leftSystem.results[i] = constraint.leftSystem.results[i] xor hash.results[j]
                        }

                        if (constraint.rightSystem.equations[i][idx]) {
                            constraint.rightSystem.equations[i].xor(hash.equations[j])
                            constraint.rightSystem.results[i] = constraint.rightSystem.results[i] xor hash.results[j]
                        }
                    }

                    j++
                }

                if (varExists) {
                    val newVarIndexLeft = constantVarsStartIndex + constraintVarsCount + xorEqIndex
                    andSystem.equations[andEqIndex].andOpLeft.set(newVarIndexLeft)
                    xorSystem.equations[xorEqIndex].xor(constraint.leftSystem.equations[i])
                    xorSystem.equations[xorEqIndex].clear(0, constantVarsStartIndex)
                    xorSystem.equations[xorEqIndex].set(newVarIndexLeft)
                    xorSystem.results[xorEqIndex] = constraint.leftSystem.results[i]

                    xorEqIndex++

                    val newVarIndexRight = constantVarsStartIndex + constraintVarsCount + xorEqIndex
                    andSystem.equations[andEqIndex].andOpRight.set(newVarIndexRight)
                    xorSystem.equations[xorEqIndex].xor(constraint.rightSystem.equations[i])
                    xorSystem.equations[xorEqIndex].clear(0, constantVarsStartIndex)
                    xorSystem.equations[xorEqIndex].set(newVarIndexRight)
                    xorSystem.results[xorEqIndex] = constraint.rightSystem.results[i]

                    andSystem.equations[andEqIndex].rightXor.set(constraintVarIndex)

                    xorEqIndex++
                    andEqIndex++
                }

                i++
            }
        }

        return XorAndEquationSystem(xorSystem, andSystem)
    }*/

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
