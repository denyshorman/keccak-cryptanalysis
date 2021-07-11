package keccak

import java.util.*

object GeneralEquationSolver {
    fun solve(equations: Array<Equation>) {
        TODO()
    }
}

class Equation(val left: Node, val right: Node) {
    fun extract(variable: Variable): Equation {
        val node = Xor(left, right).simplify()

        if (!node.contains(variable)) {
            return Equation(node, Bit())
        }

        return when (val extracted = node.extract(variable)) {
            is Bit -> Equation(extracted, extracted)
            is Variable, is And -> Equation(extracted, Bit())
            is Xor -> {
                var l: Node = Nop
                val r = LinkedList<Node>()

                extracted.nodes.forEach { extractedNode ->
                    if (extractedNode.contains(variable)) {
                        if (l is Nop) {
                            l = extractedNode
                        } else {
                            throw IllegalStateException()
                        }
                    } else {
                        r.add(extractedNode)
                    }
                }

                Equation(l, Xor(r).simplify())
            }
            is Nop -> Equation(Bit(), Bit())
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Equation

        return left == other.left && right == other.right || left == other.right && right == other.left
    }

    override fun hashCode(): Int {
        return left.hashCode() + right.hashCode()
    }

    override fun toString(): String {
        return "$left = $right"
    }

    private fun Node.extract(variable: Variable): Node {
        return when (this) {
            is Variable -> {
                if (variable == this) {
                    this
                } else {
                    throw IllegalStateException()
                }
            }
            is Xor -> {
                val (varNodes, notVarNodes) = nodes.partition(variable)

                val extractedNodes = varNodes.asSequence().map { it.extract(variable) }.map { node ->
                    when (node) {
                        is Variable -> Bit(true)
                        is Xor -> {
                            var variableNode: Node = Nop

                            node.nodes.forEach { node0 ->
                                if (node0.contains(variable)) {
                                    if (variableNode == Nop) {
                                        variableNode = when (node0) {
                                            is Variable -> Bit(true)
                                            is And -> And(node0.nodes.asSequence().filter { it != variable }).simplify()
                                            else -> throw IllegalStateException()
                                        }
                                    } else {
                                        throw IllegalStateException()
                                    }
                                } else {
                                    notVarNodes.add(node0)
                                }
                            }

                            variableNode
                        }
                        is And -> And(node.nodes.asSequence().filter { it != variable }).simplify()
                        is Nop -> Nop
                        is Bit -> throw IllegalStateException()
                    }
                }

                val processedNode = And(variable, Xor(extractedNodes).simplify()).simplify()

                notVarNodes.add(processedNode)

                Xor(notVarNodes).simplify()
            }
            is And -> {
                val (varNodes, notVarNodes) = nodes.partition(variable)

                val varNodesAnd = And(varNodes.asSequence().map { it.extract(variable) })

                if (varNodesAnd.nodes.any { it is Xor }) {
                    val xorNode = varNodesAnd.nodes.find { it is Xor } as Xor

                    var extractedVar: Node = Nop
                    val remainingNodes = LinkedList<Node>()

                    xorNode.nodes.forEach { node ->
                        if (node.contains(variable)) {
                            if (extractedVar is Nop) {
                                extractedVar = node
                            } else {
                                throw IllegalStateException()
                            }
                        } else {
                            remainingNodes.add(node)
                        }
                    }

                    val processedExceptCurrent = And(varNodesAnd.nodes.asSequence().filter { it != xorNode }).simplify()

                    Xor(
                        And(sequenceOf(extractedVar) + processedExceptCurrent + notVarNodes).simplify(),
                        And(sequenceOf(Xor(remainingNodes.asSequence()).simplify()) + processedExceptCurrent + notVarNodes).simplify(),
                    )
                        .simplify()
                        .extract(variable)
                } else {
                    And(varNodesAnd.nodes.asSequence() + notVarNodes.asSequence()).simplify()
                }
            }
            is Bit, is Nop -> this
        }
    }

    private fun Iterable<Node>.partition(variable: Variable): Pair<LinkedList<Node>, LinkedList<Node>> {
        val varNodes = LinkedList<Node>()
        val notVarNodes = LinkedList<Node>()

        forEach { node ->
            if (node.contains(variable)) {
                when (node) {
                    is Bit -> notVarNodes.add(node)
                    is Variable, is And, is Xor -> {
                        if (node.contains(variable)) {
                            varNodes.add(node)
                        } else {
                            notVarNodes.add(node)
                        }
                    }
                    is Nop -> run {}
                }
            } else {
                notVarNodes.add(node)
            }
        }

        return Pair(varNodes, notVarNodes)
    }
}
