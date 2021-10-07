package keccak

import keccak.util.modPow2
import mu.KotlinLogging

class XorAndEquationSystem(
    val xorSystem: XorEquationSystem,
    val andSystem: AndEquationSystem,
) {
    fun substituteAndSystem() {
        var xorEqIndex = 0
        while (xorEqIndex < xorSystem.rows) {
            if (!xorSystem.equations[xorEqIndex].isEmpty) {
                val firstBitIndex = xorSystem.equations[xorEqIndex].nextSetBit(0)

                var andEqIndex = 0
                while (andEqIndex < andSystem.rows) {
                    if (andSystem.equations[andEqIndex].andOpLeft[firstBitIndex]) {
                        andSystem.equations[andEqIndex].andOpLeft.xor(xorSystem.equations[xorEqIndex])
                        andSystem.andOpLeftResults[andEqIndex] = andSystem.andOpLeftResults[andEqIndex] xor xorSystem.results[xorEqIndex]
                    }

                    if (andSystem.equations[andEqIndex].andOpRight[firstBitIndex]) {
                        andSystem.equations[andEqIndex].andOpRight.xor(xorSystem.equations[xorEqIndex])
                        andSystem.andOpRightResults[andEqIndex] = andSystem.andOpRightResults[andEqIndex] xor xorSystem.results[xorEqIndex]
                    }

                    if (andSystem.equations[andEqIndex].rightXor[firstBitIndex]) {
                        andSystem.equations[andEqIndex].rightXor.xor(xorSystem.equations[xorEqIndex])
                        andSystem.rightXorResults[andEqIndex] = andSystem.rightXorResults[andEqIndex] xor xorSystem.results[xorEqIndex]
                    }

                    andEqIndex++
                }
            }

            xorEqIndex++

            if (modPow2(xorEqIndex, 4096) == 0) {
                logger.info("Processed $xorEqIndex rows")
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
