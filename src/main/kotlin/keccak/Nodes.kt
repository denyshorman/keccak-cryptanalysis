package keccak

class NodeContext {
    val variables = mutableMapOf<String, Bit>()

    companion object {
        val EmptyContext = NodeContext()
    }
}

sealed interface Node {
    fun evaluate(context: NodeContext): Bit
}

@JvmInline
value class Bit(val value: Boolean = false) : Node {
    override fun evaluate(context: NodeContext): Bit {
        return this
    }

    override fun toString(): String {
        return if (value) "1" else "0"
    }
}

@JvmInline
value class Variable(val name: String) : Node {
    override fun evaluate(context: NodeContext): Bit {
        return context.variables[name] ?: throw IllegalStateException("Variable $name not found")
    }

    override fun toString(): String {
        return name
    }
}

class Xor(vararg initNodes: Node) : Node {
    val nodes: Set<Node>

    init {
        val nodeSet = HashSet<Node>()

        initNodes.forEach { node ->
            when (node) {
                is Xor -> {
                    node.nodes.forEach { anotherNode ->
                        nodeSet.addNode(anotherNode)
                    }
                }
                else -> nodeSet.addNode(node)
            }
        }

        nodes = nodeSet
    }

    override fun evaluate(context: NodeContext): Bit {
        return nodes.fold(Bit()) { bit1, node ->
            val bit2 = node.evaluate(context)
            Bit(bit1 != bit2)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Xor

        if (nodes != other.nodes) return false

        return true
    }

    override fun hashCode(): Int {
        return nodes.hashCode()
    }

    override fun toString(): String {
        return nodes.asSequence().map {
            when (it) {
                is Bit, is Variable, is Not -> it.toString()
                else -> "($it)"
            }
        }.joinToString(" ^ ")
    }

    private fun MutableSet<Node>.addNode(node: Node) {
        if (contains(node)) {
            remove(node)
        } else if (node !is Bit || node.value) {
            add(node)
        }
    }
}

class And(vararg initNodes: Node) : Node {
    val nodes: Set<Node>

    init {
        val nodeSet = HashSet<Node>()

        initNodes.forEach { node ->
            when (node) {
                is And -> nodeSet.addAll(node.nodes)
                else -> nodeSet.add(node)
            }
        }

        nodes = nodeSet
    }

    override fun evaluate(context: NodeContext): Bit {
        return Bit(nodes.all { it.evaluate(context).value })
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as And

        if (nodes != other.nodes) return false

        return true
    }

    override fun hashCode(): Int {
        return nodes.hashCode()
    }

    override fun toString(): String {
        return nodes.asSequence().map {
            when (it) {
                is Bit, is Variable, is Not -> it.toString()
                else -> "($it)"
            }
        }.joinToString(" & ")
    }
}

class Not(val node: Node) : Node {
    override fun evaluate(context: NodeContext): Bit {
        val bit = node.evaluate(context)
        return Bit(!bit.value)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Not

        if (node != other.node) return false

        return true
    }

    override fun hashCode(): Int {
        return node.hashCode()
    }

    override fun toString(): String {
        return when (node) {
            is Bit, is Variable -> "!$node"
            else -> "!($node)"
        }
    }
}
