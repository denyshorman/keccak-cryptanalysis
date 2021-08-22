package keccak

import keccak.util.calcCombinationIndex
import keccak.util.calcCombinationPartialIndex
import kotlin.math.min

fun solveXorAndEquationSystem(system: XorAndEquationSystem) {
    var row = 0
    var colX = 0
    var colY = 0

    while (row < system.rows && colX < system.cols && colY < system.cols) {
        var i = row
        var found = false

        while (i < system.rows) {
            if (system.equations[i].variableExists(colX, colY)) {
                found = true
                break
            }

            i++
        }

        if (found) {
            if (row != i) {
                system.exchange(row, i)
            }

            i = row + 1

            while (i < system.rows) {
                if (system.equations[i].variableExists(colX, colY)) {
                    system.xor(i, row)
                }

                i++
            }
        }

        row++
        colY++

        if (colY == system.cols) {
            colX++
            colY = colX
        }
    }

    val varsCount = calcCombinationIndex(system.cols - 1, system.cols)
    row = min(system.rows, varsCount) - 1
    val partIndex = calcCombinationPartialIndex(row, varsCount)
    colX = partIndex.first
    colY = partIndex.second

    while (row >= 0 && colX >= 0 && colY >= 0) {
        var i = row - 1

        while (i >= 0) {
            if (system.equations[i].variableExists(colX, colY)) {
                system.xor(i, row)
            }

            i--
        }

        row--
        colY--

        if (colY < 0) {
            colY = system.cols - 1
            colX--
        }
    }
}
