package keccak.util

import keccak.AndEquationSystem
import keccak.XorAndEquationSystem
import keccak.XorEquationSystem
import mu.KotlinLogging
import java.io.File
import java.util.*

private val logger = KotlinLogging.logger {}

fun File.toXorEquationSystem(rows: Int, cols: Int, humanReadable: Boolean): XorEquationSystem {
    val system = XorEquationSystem(rows, cols)

    inputStream().bufferedReader(Charsets.US_ASCII).use { reader ->
        reader.lineSequence().forEachIndexed { eqIndex, line ->
            system.set(eqIndex, line, humanReadable)

            if (modPow2(eqIndex, 4096) == 0) {
                logger.info("Loaded $eqIndex lines")
            }
        }
    }

    return system
}

fun File.toAndEquationSystem(rows: Int, cols: Int, humanReadable: Boolean): AndEquationSystem {
    val system = AndEquationSystem(rows, cols)

    inputStream().bufferedReader(Charsets.US_ASCII).use { reader ->
        reader.lineSequence().forEachIndexed { eqIndex, line ->
            system.set(eqIndex, line, humanReadable)

            if (modPow2(eqIndex, 4096) == 0) {
                logger.info("Loaded $eqIndex lines")
            }
        }
    }

    return system
}

fun Pair<File, File>.toXorAndEquationSystem(
    xorEqRows: Int,
    andEqRows: Int,
    varsCount: Int,
    xorHumanReadable: Boolean,
    andHumanReadable: Boolean,
): XorAndEquationSystem {
    val xorSystem = first.toXorEquationSystem(xorEqRows, varsCount, xorHumanReadable)
    val andSystem = second.toAndEquationSystem(andEqRows, varsCount, andHumanReadable)
    return XorAndEquationSystem(xorSystem, andSystem)
}

fun File.fromBinaryXorEquationSystemToHumanReadable(saveToFile: File) {
    inputStream().bufferedReader(Charsets.US_ASCII).useLines { lines ->
        saveToFile.outputStream().bufferedWriter(Charsets.US_ASCII).use { writer ->
            lines.forEachIndexed { lineIndex, line ->
                var bitIndex = 0
                val variables = LinkedList<String>()

                while (bitIndex < line.length - 2) {
                    if (line[bitIndex] == '1') {
                        variables.add("x$bitIndex")
                    }
                    bitIndex++
                }

                val res = line[line.length - 1]

                if (variables.isNotEmpty()) {
                    val equation = variables.joinToString(" + ", postfix = " = $res")
                    writer.appendLine(equation)
                } else {
                    if (res == '1') {
                        writer.appendLine("0 = 1")
                    }
                }

                if (modPow2(lineIndex, 4096) == 0) {
                    logger.info("Processed $lineIndex lines")
                }
            }
        }
    }
}
