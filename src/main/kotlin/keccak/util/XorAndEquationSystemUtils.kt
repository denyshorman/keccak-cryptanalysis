package keccak.util

import keccak.XorAndEquationSystem
import java.io.File

fun XorAndEquationSystem.toFile(
    xorSystemFile: File,
    andSystemFile: File,
    xorHumanReadable: Boolean,
    andHumanReadable: Boolean,
) {
    xorSystem.toFile(xorSystemFile, humanReadable = xorHumanReadable)
    andSystem.toFile(andSystemFile, humanReadable = andHumanReadable)
}
