package keccak

class NodeEquationSystem(
    val equations: Array<NodeEquation>,
) {
    fun expand() {
        var i = 0
        while (i < equations.size) {
            equations[i] = equations[i].expand()
            i++
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
