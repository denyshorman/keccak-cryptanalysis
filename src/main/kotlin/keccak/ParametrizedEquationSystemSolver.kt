package keccak

import kotlin.math.min

fun solveParametrizedEquationSystem(system: ParametrizedEquationSystem) {
    var row = 0
    var col = 0

    while (row < system.rows && col < system.cols) {
        var i = row
        var found = false

        while (i < system.rows) {
            if (system.equations[i][col]) {
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
                if (system.equations[i][col]) {
                    system.xor(i, row)
                }

                i++
            }
        }

        row++
        col++
    }

    row = min(system.rows, system.cols) - 1
    col = row

    while (row >= 0 && col >= 0) {
        var i = row - 1

        while (i >= 0) {
            if (system.equations[i][col]) {
                system.xor(i, row)
            }

            i--
        }

        row--
        col--
    }
}
