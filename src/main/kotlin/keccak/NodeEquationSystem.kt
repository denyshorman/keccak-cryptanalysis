package keccak

import keccak.util.isPow2
import keccak.util.iterateOverAllSetBits
import keccak.util.modPow2
import mu.KotlinLogging
import java.util.*
import kotlin.collections.set

class NodeEquationSystem {
    val equations: ArrayList<Node>
    var eqNodeMap: MutableMap<Int, Node>
    var nodeEqMap: MutableMap<Node, Int>

    constructor(eqs: ArrayList<Node>) {
        equations = eqs
        eqNodeMap = HashMap(equations.size, 1f)
        nodeEqMap = HashMap(equations.size, 1f)
    }

    constructor(eqs: Array<Node>) : this(arrayListOf(*eqs))
    constructor(eqs: Array<NodeEquation>) : this(arrayListOf(*(Array<Node>(eqs.size) { eqs[it].left + eqs[it].right })))
    constructor(size: Int): this(ArrayList(size))

    val rows get() = equations.size

    fun expand() {
        var i = 0
        while (i < equations.size) {
            equations[i] = equations[i].expand()
            i++
        }
    }

    fun isInvalid(eqIndex: Int): Boolean {
        return equations[eqIndex].isInvalid()
    }

    fun isValid(eqIndex: Int): Boolean {
        return equations[eqIndex].isValid()
    }

    private fun Node.isInvalid(): Boolean {
        return when (this) {
            is Bit -> this.value
            is Variable -> false
            is Xor -> !this.nodes.any { it.isValid() }
            is And -> !this.nodes.any { it.isValid() }
        }
    }

    private fun Node.isValid(): Boolean {
        return !isInvalid()
    }

    private fun Node.firstNode(): Node? {
        return when (this) {
            is Bit, is Variable, is And -> this
            is Xor -> nodes.firstOrNull { it !is Bit }
        }
    }

    private fun Node.firstNode(other: List<Node>): Node? {
        return when (this) {
            is Bit, is Variable, is And -> if (other.contains(this)) this else null
            is Xor -> nodes.firstOrNull { other.contains(it) }
        }
    }

    fun expressNode(
        fromEqIndex: Int,
        node: Node,
        activeRows: BitSet? = null,
        varSubstituted: ((Int) -> Boolean)? = null,
    ): Boolean {
        if (!equations[fromEqIndex].contains(node)) {
            return false
        }

        val oldNode = eqNodeMap[fromEqIndex]
        eqNodeMap[fromEqIndex] = node
        nodeEqMap[node] = fromEqIndex

        if (oldNode != null) {
            nodeEqMap.remove(oldNode)
        }

        var eqIndex = 0
        while (eqIndex < rows) {
            if (
                (activeRows == null || activeRows[eqIndex]) &&
                eqIndex != fromEqIndex && equations[eqIndex].contains(node)
            ) {
                equations[eqIndex] = equations[eqIndex] xor equations[fromEqIndex]

                if (varSubstituted != null && !varSubstituted(eqIndex)) {
                    return false
                }
            }

            eqIndex++
        }

        return true
    }

    fun solve(
        skipValidation: Boolean = false,
        logProgress: Boolean = false,
        sortEquations: Boolean = false,
        progressStep: Int = 1024,
        activeRows: BitSet? = null,
        priorityNodes: List<Node>,
    ): Boolean {
        if (logProgress) {
            if (!isPow2(progressStep)) {
                throw IllegalArgumentException("progressStep must be a power of 2")
            }

            if (priorityNodes.isEmpty()) {
                logger.info("Start variables expression")
            } else {
                logger.info("Expressing priority variables")
            }
        }

        val validateEquation: ((Int) -> Boolean)? = if (skipValidation) null else ::isValid

        activeRows.iterateOverAllSetBits(fromIndex = 0, rows) { eqIndex ->
            if (equations[eqIndex] == Bit(false) || eqNodeMap[eqIndex] != null) {
                return@iterateOverAllSetBits
            }

            val node = if (priorityNodes.isEmpty()) {
                equations[eqIndex].firstNode()
            } else {
                equations[eqIndex].firstNode(priorityNodes)
            }

            if (node != null) {
                val expressed = expressNode(eqIndex, node, activeRows, validateEquation)
                if (!expressed) return false
            }

            if (logProgress && modPow2(eqIndex, progressStep) == 0) {
                logger.info("Processed $eqIndex rows")
            }
        }

        if (logProgress) {
            if (priorityNodes.isEmpty()) {
                logger.info("All variables have been expressed")
            } else {
                logger.info("All priority variables have been expressed")
            }
        }

        if (priorityNodes.isNotEmpty()) {
            if (logProgress) {
                logger.info("Expressing non-priority variables")
            }

            activeRows.iterateOverAllSetBits(fromIndex = 0, rows) { eqIndex ->
                if (logProgress && modPow2(eqIndex, progressStep) == 0) {
                    logger.info("Processed $eqIndex rows")
                }

                if (equations[eqIndex] == Bit(false) || eqNodeMap[eqIndex] != null) {
                    return@iterateOverAllSetBits
                }

                val node = equations[eqIndex].firstNode()!!
                val expressed = expressNode(eqIndex, node, activeRows, validateEquation)
                if (!expressed) return false
            }

            if (logProgress) {
                logger.info("All non-priority variables have been expressed")
            }
        }

        if (sortEquations) {
            if (logProgress) {
                logger.info("Sorting equations")
            }

            sortEquations()

            if (logProgress) {
                logger.info("Equations have been sorted")
            }
        }

        return true
    }

    fun sortEquations() {
        TODO("Implement sorting")
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

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
