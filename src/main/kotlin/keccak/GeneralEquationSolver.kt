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
                var l: Node = Bit()
                val r = LinkedList<Node>()

                extracted.nodes.forEach { extractedNode ->
                    if (extractedNode.contains(variable)) {
                        if (l is Bit) {
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

                val extractedNodes = varNodes.asSequence().map { node ->
                    when (node) {
                        is Variable -> Bit(true)
                        is Xor -> node.extract(variable)
                        is And -> And(node.nodes.asSequence().filter { it != variable }).simplify()
                        is Bit, is Nop -> throw IllegalStateException()
                    }
                }

                val processedNode = And(variable, Xor(extractedNodes).simplify()).simplify()

                notVarNodes.add(processedNode)

                Xor(notVarNodes).simplify()
            }
            is And -> {
                val (varNodes, notVarNodes) = nodes.partition(variable)

                val varNodesAnd = And(varNodes)

                if (varNodesAnd.nodes.any { it is Xor }) {
                    val node = varNodesAnd.nodes.find { it is Xor } as Xor

                    var extractedVariable: Node = Bit()
                    val remainingNodes = LinkedList<Node>()

                    node.nodes.forEach { y ->
                        if (y.contains(variable)) {
                            if (extractedVariable is Bit) {
                                extractedVariable = y
                            } else {
                                throw IllegalStateException()
                            }
                        } else {
                            remainingNodes.add(y)
                        }
                    }

                    val processedExceptCurrent = Xor(varNodesAnd.nodes.asSequence().filter { it != node }).simplify()

                    Xor(
                        And(sequenceOf(extractedVariable) + processedExceptCurrent + notVarNodes).simplify(),
                        And(remainingNodes.asSequence() + processedExceptCurrent + notVarNodes).simplify(),
                    )
                        .simplify()
                        .extract(variable)
                } else {
                    And(varNodes.asSequence() + notVarNodes.asSequence()).simplify()
                }
            }
            is Bit, is Nop -> throw IllegalStateException()
        }
    }

    private fun Iterable<Node>.partition(variable: Variable): Pair<LinkedList<Node>, LinkedList<Node>> {
        val varNodes = LinkedList<Node>()
        val notVarNodes = LinkedList<Node>()

        forEach { node ->
            if (node.contains(variable)) {
                when (val processed = node.extract(variable)) {
                    is Bit -> notVarNodes.add(processed)
                    is Variable, is And -> {
                        if (node.contains(variable)) {
                            varNodes.add(processed)
                        } else {
                            notVarNodes.add(processed)
                        }
                    }
                    is Xor -> {
                        if (node.contains(variable)) {
                            varNodes.add(processed)
                        } else {
                            notVarNodes.add(processed)
                        }
                        /*val newVarNodes = LinkedList<Node>()
                        val newNotVarNodes = LinkedList<Node>()

                        processed.nodes.forEach { xorNode ->
                            if (xorNode.contains(variable)) {
                                newVarNodes.add(xorNode)
                            } else {
                                newNotVarNodes.add(xorNode)
                            }
                        }

                        varNodes.addAll(newVarNodes)
                        notVarNodes.addAll(newNotVarNodes)*/
                    }
                }
            } else {
                notVarNodes.add(node)
            }
        }

        return Pair(varNodes, notVarNodes)
    }
}
