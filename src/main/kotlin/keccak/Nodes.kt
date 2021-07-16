package keccak

import java.util.*

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
        return when (node) {
            is Bit, is Variable, is And -> nodes.contains(node) || nodes.any { it.contains(node) }
            is Xor -> nodes.containsAll(node.nodes) || nodes.any { it.contains(node) }
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
        } else if (node !is Bit || node.value) {
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
        return when (node) {
            is Bit, is Variable, is Xor -> nodes.contains(node) || nodes.any { it.contains(node) }
            is And -> nodes.containsAll(node.nodes) || nodes.any { it.contains(node) }
        }
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
                else -> add(node)
            }
        }
    }

    companion object {
        private object Zero : Throwable(null, null, false, false)
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
            nodes.isEmpty() -> Bit()
            nodes.size == 1 -> nodes.first()
            else -> this
        }
        is And -> when {
            nodes.isEmpty() -> Bit()
            nodes.size == 1 -> nodes.first()
            else -> this
        }
        else -> this
    }
}

fun Node.flatten(): Node {
    return when (this) {
        is Bit, is Variable -> this
        is And -> {
            val variables = LinkedList<Variable>()
            val xors = LinkedList<Xor>()

            this.nodes.forEach { node ->
                when (node) {
                    is Variable -> variables.add(node)
                    is Xor -> {
                        when (val flattenedNode = node.flatten().simplify()) {
                            is Bit -> {
                                if (!flattenedNode.value) {
                                    return Bit()
                                }
                            }
                            is Variable -> {
                                variables.add(flattenedNode)
                            }
                            is And -> {
                                flattenedNode.nodes.forEach { variable ->
                                    require(variable is Variable)
                                    variables.add(variable)
                                }
                            }
                            is Xor -> {
                                xors.add(flattenedNode)
                            }
                        }
                    }
                    else -> throw IllegalStateException()
                }
            }

            val initXor = if (variables.size == 0) {
                Xor()
            } else {
                Xor(And(variables))
            }

            val flattenXor = xors.fold(initXor) { a, b ->
                if (a.nodes.isEmpty() && b.nodes.isEmpty() || b.nodes.isEmpty()) {
                    a
                } else if (a.nodes.isEmpty()) {
                    b
                } else {
                    val nodes = LinkedList<Node>()

                    a.nodes.forEach { l ->
                        b.nodes.forEach { r ->
                            nodes.add(And(l, r).simplify())
                        }
                    }

                    Xor(nodes)
                }
            }

            flattenXor.simplify()
        }
        is Xor -> {
            val flattenNodes = this.nodes.asSequence().map { it.flatten() }
            Xor(flattenNodes).simplify()
        }
    }
}
//#endregion
