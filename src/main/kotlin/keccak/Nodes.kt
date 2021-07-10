package keccak

class NodeContext {
    val variables = mutableMapOf<String, Bit>()
}

sealed interface Node {
    fun evaluate(context: NodeContext): Bit
    fun contains(node: Node): Boolean
}

@JvmInline
value class Bit(val value: Boolean = false) : Node {
    constructor(v: Int) : this(v == 1)

    override fun evaluate(context: NodeContext): Bit {
        return this
    }

    override fun contains(node: Node): Boolean {
        return this == node
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

    override fun contains(node: Node): Boolean {
        return this == node
    }

    override fun toString(): String {
        return name
    }
}

class Xor : Node {
    val nodes: Set<Node>

    constructor(vararg initNodes: Node) : this(initNodes.asIterable())
    constructor(initNodes: Sequence<Node>) : this(initNodes.asIterable())

    constructor(initNodes: Iterable<Node>) {
        val nodeSet = HashSet<Node>()
        nodeSet.addNodes(initNodes)
        nodes = nodeSet
    }

    override fun evaluate(context: NodeContext): Bit {
        return nodes.fold(Bit()) { bit1, node ->
            val bit2 = node.evaluate(context)
            Bit(bit1 != bit2)
        }
    }

    override fun contains(node: Node): Boolean {
        return nodes.contains(node) || nodes.any { it.contains(node) }
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
                is Bit, is Variable, is And -> it.toString()
                else -> "($it)"
            }
        }.joinToString(" + ")
    }

    private fun HashSet<Node>.addNodes(nodes: Iterable<Node>) {
        nodes.forEach { node ->
            when (node) {
                is Xor -> addNodes(node.nodes)
                else -> addNode(node)
            }
        }
    }

    private fun HashSet<Node>.addNode(node: Node) {
        if (contains(node)) {
            remove(node)
        } else if (!(node is Bit && !node.value || node is Nop)) {
            add(node)
        }
    }
}

class And : Node {
    val nodes: Set<Node>

    constructor(vararg initNodes: Node) : this(initNodes.asIterable())
    constructor(initNodes: Sequence<Node>) : this(initNodes.asIterable())

    constructor(initNodes: Iterable<Node>) {
        nodes = try {
            val nodeSet = HashSet<Node>()
            nodeSet.addNodes(initNodes.asIterable())
            nodeSet
        } catch (_: Zero) {
            emptySet()
        }
    }

    override fun evaluate(context: NodeContext): Bit {
        return Bit(nodes.all { it.evaluate(context).value })
    }

    override fun contains(node: Node): Boolean {
        return nodes.contains(node) || nodes.any { it.contains(node) }
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
                is Bit, is Variable -> it.toString()
                else -> "($it)"
            }
        }.joinToString("*")
    }

    private fun HashSet<Node>.addNodes(nodes: Iterable<Node>) {
        nodes.forEach { node ->
            when (node) {
                is Bit -> if (!node.value) throw Zero
                is And -> addNodes(node.nodes)
                is Nop -> run {}
                else -> add(node)
            }
        }
    }

    companion object {
        private object Zero : Throwable(null, null, false, false)
    }
}

object Nop : Node {
    override fun evaluate(context: NodeContext): Bit {
        throw IllegalStateException("Can't evaluate nop")
    }

    override fun contains(node: Node): Boolean {
        return false
    }

    override fun toString(): String {
        return "nop"
    }
}

//#region Extensions

infix fun Node.xor(other: Node) = Xor(this, other)
infix fun Node.and(other: Node) = And(this, other)
infix fun String.xor(other: Node) = Xor(Variable(this), other)
infix fun String.and(other: Node) = And(Variable(this), other)
infix fun Node.xor(other: String) = Xor(this, Variable(other))
infix fun Node.and(other: String) = And(this, Variable(other))
infix fun String.xor(other: String) = Xor(Variable(this), Variable(other))
infix fun String.and(other: String) = And(Variable(this), Variable(other))
fun String.toVar() = Variable(this)

fun Node.simplify(): Node {
    return when (this) {
        is Xor -> when {
            nodes.isEmpty() -> Nop
            nodes.size == 1 -> nodes.first()
            else -> this
        }
        is And -> when {
            nodes.isEmpty() -> Nop
            nodes.size == 1 -> nodes.first()
            else -> this
        }
        else -> this
    }
}
//#endregion
