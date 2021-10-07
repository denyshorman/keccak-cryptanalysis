package keccak

import keccak.util.exchange

class NodeEquationSystem(
    val equations: Array<NodeEquation>,
    val variables: Array<Variable>,
) {
    fun solve() {
        bottom(equations, variables)
        top(equations, variables)
    }

    fun flatten() {
        var i = 0
        while (i < equations.size) {
            equations[i] = equations[i].flatten()
            i++
        }
    }

    private fun bottom(equations: Array<NodeEquation>, variables: Array<Variable>) {
        var rowId = 0
        var varId = 0

        while (rowId < equations.size && varId < variables.size) {
            var i = rowId
            var found = false

            while (i < equations.size) {
                if (equations[i].contains(variables[varId])) {
                    found = true
                    break
                }

                i++
            }

            if (found) {
                if (rowId != i) {
                    equations.exchange(rowId, i)
                }

                equations[rowId] = equations[rowId].extract(variables[varId])

                if (equations[rowId].contains(variables[varId])) {
                    i = rowId + 1

                    while (i < equations.size) {
                        if (equations[i].contains(variables[varId])) {
                            equations[i] = equations[i].substitute(equations[rowId], variables[varId])
                        }

                        i++
                    }
                }
            }

            rowId++
            varId++
        }
    }

    private fun top(equations: Array<NodeEquation>, variables: Array<Variable>) {
        var rowId = equations.size - 1
        var varId = equations.size - 1

        while (rowId >= 0 && varId >= 0) {
            if (equations[rowId].contains(variables[varId])) {
                var i = rowId - 1
                var j = varId - 1

                while (i >= 0) {
                    if (equations[i].contains(variables[varId])) {
                        equations[i] = equations[i].substitute(equations[rowId], variables[varId]).extract(variables[j])
                    }

                    i--
                    j--
                }
            }

            rowId--
            varId--
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        var i = 0
        while (i < equations.size) {
            sb.append(equations[i])
            i++
            if (i != equations.size) sb.append('\n')
        }
        return sb.toString()
    }
}
