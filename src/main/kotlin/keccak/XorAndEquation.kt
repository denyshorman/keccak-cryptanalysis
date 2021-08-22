package keccak

import java.util.*

class XorAndEquation {
    val nodes: LinkedList<AndEquation>

    constructor() {
        this.nodes = LinkedList()
    }

    constructor(vararg nodes: AndEquation) {
        this.nodes = LinkedList()
        this.nodes.addAll(nodes)
    }

    val varCount: Int get() = nodes.getOrNull(0)?.size ?: 0

    fun xor(vararg xorAndEquation: XorAndEquation) {
        var i = 0
        while (i < xorAndEquation.size) {
            nodes.addAll(xorAndEquation[i].nodes)
            i++
        }
    }

    fun variableExists(i: Int, j: Int): Boolean {
        var exists = false
        for (node in nodes) {
            exists = exists xor node.variableExists(i, j)
        }
        return exists
    }
}
