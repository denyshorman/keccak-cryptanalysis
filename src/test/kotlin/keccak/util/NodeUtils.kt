package keccak.util

import keccak.*

fun Node.printNodes() {
    if (this !is Xor) return

    val detNodes = nodes.asSequence()
        .map {
            when (it) {
                is Variable -> listOf(it.name.drop(1).toInt())
                is And -> {
                    it.nodes.asSequence()
                        .map { n ->
                            n as Variable
                            n.name.drop(1).toInt()
                        }
                        .sortedBy { x -> x }.toList()
                }
                is Bit -> listOf()
                else -> throw IllegalStateException()
            }
        }
        .sortedWith { a, b ->
            if (a.size == b.size) {
                a.joinToString("") { String.format("%02d", it) }.compareTo(b.joinToString("") { String.format("%02d", it) })
            } else {
                a.size.compareTo(b.size)
            }
        }
        .toList()

    for (detNode in detNodes) {
        val nodeString = detNode.asSequence()
            .map { String.format("%02d", it)}
            .joinToString(" ")

        if (nodeString.isBlank()) {
            println("1")
        } else {
            println(nodeString)
        }
    }
}

fun Node.printNodesCount() {
    if (this !is Xor) return

    val map = HashMap<Int, Int>()

    for (node0 in nodes) {
        when (node0) {
            is Variable -> map.merge(1, 1) { a, b -> a + b }
            is And -> map.merge(node0.nodes.size, 1) { a, b -> a + b }
            else -> {}
        }
    }

    map.forEach { (k, v) ->
        println("$k = $v")
    }
}

fun Node.printAllSolutions(varsCount: Int, varPrefix: String = "x", varAdd: Int = 1) {
    val iter = CombinationIterator(varsCount)
    val ctx = NodeContext()
    var i: Int
    var trueFuncCount = 0L
    while (true) {
        ctx.variables.clear()
        i = -1
        while (++i < iter.varsCount) {
            ctx.variables["$varPrefix${i + varAdd}"] = Bit(iter.combination[i])
        }
        val funcRes = evaluate(ctx)
        if (funcRes.value) trueFuncCount++
        println("$iter = $funcRes")
        if (iter.hasNext()) iter.next() else break
    }
    println("True results: $trueFuncCount")
}

fun Node.printGroups() {
    fun Node.findMax(): Int {
        return when (this) {
            is Bit -> 0
            is Variable -> 1
            is And -> nodes.size
            is Xor -> nodes.maxOf { it.findMax() }
        }
    }

    val maxValue = findMax()

    var i = -1
    while (++i <= maxValue) {
        val node = keep(i)
        println(node)
    }
}
