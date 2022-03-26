package keccak.util

import keccak.*
import java.io.OutputStream

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

fun Node.printAllSolutions(
    varPrefix: String = "x",
    varsCount: Int = maxVariables(varPrefix),
    varOffset: Int = 0,
    condition: (NodeContext, Boolean) -> Boolean = { _, _ -> true },
) {
    printAllSolutions(arrayOf(this), varPrefix, varsCount, varOffset, condition)
}

fun printAllSolutions(
    functions: Array<out Node>,
    varPrefix: String = "x",
    varsCount: Int,
    varOffset: Int = 0,
    condition: (NodeContext, Boolean) -> Boolean = { _, _ -> true },
) {
    val iter = CombinationIteratorSimple(varsCount)
    val ctx = NodeContext()
    var i: Int

    while (true) {
        ctx.variables.clear()
        i = -1

        while (++i < iter.varsCount) {
            ctx.variables["$varPrefix${i + varOffset}"] = Bit(iter.combination[i])
        }

        val funcRes = functions.asSequence()
            .map { it.evaluate(ctx) }
            .filter { condition(ctx, it.value) }
            .joinToString(" ") { it.toString() }

        if (funcRes.isNotBlank()) {
            println("$iter = $funcRes")
        }

        if (iter.hasNext()) iter.next() else break
    }
}

fun countTrueValues(
    functions: Array<out Node>,
    varPrefix: String = "x",
    varsCount: Int,
    varOffset: Int = 0,
) {
    val iter = CombinationIteratorSimple(varsCount)
    val ctx = NodeContext()
    var i: Int
    val trueValues = Array(functions.size) { 0 }

    while (true) {
        ctx.variables.clear()
        i = -1
        while (++i < iter.varsCount) {
            ctx.variables["$varPrefix${i + varOffset}"] = Bit(iter.combination[i])
        }

        functions.forEachIndexed { idx, v ->
            val res = v.evaluate(ctx)
            if (res.value) trueValues[idx]++
        }

        if (iter.hasNext()) iter.next() else break
    }

    trueValues.forEachIndexed { index, count ->
        println("F$index: ${count}T ${pow2(varsCount) - count}F | ${functions[index]}")
    }
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

fun areCompatible(
    interpolation: Node,
    expected: Node,
    varPrefix: String = "x",
    varsCount: Int = (interpolation * expected).countVariables(varPrefix)
): Boolean {
    val xIter = CombinationIteratorSimple(varsCount)
    var compatible = true

    xIter.iterate {
        val a = interpolation.evaluate(xIter.combination, varPrefix)
        val b = expected.evaluate(xIter.combination, varPrefix)
        if (!a && b) {
            compatible = false
        }
        compatible
    }

    return compatible
}

fun compatibilityDifference(
    interpolation: Node,
    expected: Node,
    varPrefix: String = "x",
    varsCount: Int = (interpolation * expected).countVariables(varPrefix)
): Int {
    val xIter = CombinationIteratorSimple(varsCount)
    var diff = 0

    xIter.iterateAll {
        val a = interpolation.evaluate(xIter.combination, varPrefix)
        val b = expected.evaluate(xIter.combination, varPrefix)
        if (!a && b) {
            diff++
        }
    }

    return diff
}

fun Node.printAllFuncSplitAnd(
    splitCount: Int,
    varPrefix: String = "x",
    factor: Boolean = true,
    printCondition: ((Array<Node>) -> Boolean)? = null,
    outputStream: OutputStream = System.out,
) {
    val base = this
    val xIter = CombinationIteratorSimple(base.countVariables(varPrefix))
    var zeroCount = 0

    xIter.iterateAll {
        if (!base.evaluate(xIter.combination)) {
            zeroCount++
        }
    }

    val zeroIter = Array(splitCount - 1) { CombinationIteratorSimple(zeroCount) }

    fun modifier(zeroIterCombination: BooleanArray): Node {
        var mod: Node = Bit(false)
        var zeroCounter = 0

        xIter.iterateAll {
            if (!base.evaluate(xIter.combination)) {
                if (zeroIterCombination[zeroCounter]) {
                    val terms = xIter.combination.asSequence()
                        .mapIndexed { i, v -> Variable("$varPrefix$i") + Bit(v) + t }
                    mod += And(terms)
                }
                zeroCounter++
            }
        }

        return mod
    }

    fun Array<Node>.print() {
        if (printCondition != null && !printCondition(this)) {
            return
        }

        forEach { term ->
            var preparedTerm = term.expand()
            if (factor) {
                preparedTerm = preparedTerm.factor()
            }
            outputStream.write((preparedTerm.toString() + "\n").toByteArray())
        }

        outputStream.write("\n".toByteArray())
    }

    fun iterate(idx: Int) {
        if (idx >= zeroIter.size) {
            val terms = Array(splitCount) { i ->
                if (i == splitCount - 1) {
                    Bit(false)
                } else {
                    modifier(zeroIter[i].combination)
                }
            }

            var d: Node = Bit(true)
            var i = 0
            while (i < splitCount - 1) {
                d *= terms[i]
                terms[i] += base
                i++
            }
            terms[i] = d + t

            terms.print()
        } else {
            zeroIter[idx].iterateAll {
                iterate(idx + 1)
            }
        }
    }

    iterate(0)
}

fun Node.printAllFuncSplitXor(
    splitCount: Int,
    varPrefix: String = "x",
    factor: Boolean = true,
    printCondition: ((Array<Node>) -> Boolean)? = null,
) {
    val base = this
    val xIter = CombinationIteratorSimple(base.countVariables(varPrefix))
    val funcIter = Array(splitCount - 1) { CombinationIteratorSimple(pow2(xIter.varsCount).toInt()) }

    fun modifier(idx: Int): Node {
        var mod: Node = Bit(false)

        xIter.iterateAll {
            if (funcIter[idx].combination[xIter.combination.toDecimal().toInt()]) {
                val terms = xIter.combination.asSequence()
                    .mapIndexed { i, v -> Variable("$varPrefix$i") + Bit(v) + t }
                mod += And(terms)
            }
        }

        return mod
    }

    fun Array<Node>.print() {
        if (printCondition != null && !printCondition(this)) {
            return
        }

        forEach { term ->
            var preparedTerm = term.expand()
            if (factor) {
                preparedTerm = preparedTerm.factor()
            }
            println(preparedTerm)
        }

        println()
    }

    fun iterate(idx: Int) {
        if (idx >= funcIter.size) {
            val terms = Array(splitCount) { i ->
                if (i == splitCount - 1) {
                    base
                } else {
                    modifier(i)
                }
            }

            var i = 0
            val j = terms.size - 1
            while (i < j) {
                terms[j] += terms[i++]
            }

            terms.print()
        } else {
            funcIter[idx].iterateAll {
                iterate(idx + 1)
            }
        }
    }

    iterate(0)
}