package keccak

import java.util.*

//#region Bit Operation Nodes
interface Node {
    fun evaluate(context: NodeContext): Bit
}

class NodeContext {
    val variables = mutableMapOf<String, Bit>()

    companion object {
        val EmptyContext = NodeContext()
    }
}

class Bit(val value: Boolean = false) : Node {
    override fun evaluate(context: NodeContext): Bit {
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Bit

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return if (value) "1" else "0"
    }
}

class Variable(val name: String) : Node {
    override fun evaluate(context: NodeContext): Bit {
        return context.variables[name] ?: throw IllegalStateException("Variable $name not found")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Variable

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return name
    }
}

class BitGroup(val bits: Array<Node>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BitGroup

        if (!bits.contentEquals(other.bits)) return false

        return true
    }

    override fun hashCode(): Int {
        return bits.contentHashCode()
    }

    override fun toString(): String {
        return bits.asSequence().map { "[$it]" }.joinToString("")
    }
}

//#region BitGroup extensions
infix fun BitGroup.xor(other: BitGroup): BitGroup {
    require(bits.size == other.bits.size)
    val bits = bits.zip(other.bits) { l, r -> Xor(l, r) }
    return BitGroup(bits.toTypedArray())
}

infix fun BitGroup.and(other: BitGroup): BitGroup {
    require(bits.size == other.bits.size)
    val bits = bits.zip(other.bits) { l, r -> And(l, r) }
    return BitGroup(bits.toTypedArray())
}

infix fun BitGroup.andOptimized(other: BitGroup): BitGroup {
    require(bits.size == other.bits.size)

    val bits = bits.zip(other.bits) { l, r ->
        if (l is Xor && r is Xor) {
            val list = LinkedList<Node>()

            l.nodes.forEach { l0 ->
                r.nodes.forEach { r0 ->
                    list.add(And(l0, r0))
                }
            }

            Xor(*list.toTypedArray())
        } else {
            And(l, r)
        }
    }

    return BitGroup(bits.toTypedArray())
}

fun BitGroup.inv(): BitGroup {
    val invertedBits = bits.map { Not(it) }
    return BitGroup(invertedBits.toTypedArray())
}

fun BitGroup.rotateLeft(bitCount: Int): BitGroup {
    return BitGroup((bits.drop(bitCount) + bits.take(bitCount)).toTypedArray())
}

fun BitGroup.toLong(context: NodeContext): Long {
    require(bits.size == Long.SIZE_BITS)

    var value = 0L

    (Long.SIZE_BITS - 1 downTo 0).forEach { i ->
        if (bits[i].evaluate(context).value) {
            value = value or (1L shl Long.SIZE_BITS - i - 1)
        }
    }

    return value
}

fun Long.toBitGroup(): BitGroup {
    val bits = Array<Node>(Long.SIZE_BITS) { Bit() }
    (0 until Long.SIZE_BITS).forEach { i ->
        val bit = (this shr i) and 1
        bits[Long.SIZE_BITS - i - 1] = Bit(bit != 0L)
    }
    return BitGroup(bits)
}
//#endregion

class Xor(vararg initNodes: Node) : Node {
    private val nodeSet: MutableSet<Node> = HashSet()

    init {
        initNodes.forEach { node ->
            when (node) {
                is Xor -> {
                    node.nodes.forEach { anotherNode ->
                        addNode(anotherNode)
                    }
                }
                else -> addNode(node)
            }
        }
    }

    val nodes: Set<Node> = nodeSet

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

        if (nodeSet != other.nodeSet) return false

        return true
    }

    override fun hashCode(): Int {
        return nodeSet.hashCode()
    }

    override fun toString(): String {
        return nodes.asSequence().map {
            when (it) {
                is Bit, is Variable, is Not -> it.toString()
                else -> "($it)"
            }
        }.joinToString(" ^ ")
    }

    private fun addNode(node: Node) {
        if (nodeSet.contains(node)) {
            nodeSet.remove(node)
        } else if (node !is Bit || node.value) {
            nodeSet.add(node)
        }
    }
}

class Not(private val node: Node) : Node {
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

class And(vararg initNodes: Node) : Node {
    private val nodeSet: MutableSet<Node> = HashSet()

    init {
        initNodes.forEach { node ->
            when (node) {
                is And -> nodeSet.addAll(node.nodes)
                else -> nodeSet.add(node)
            }
        }
    }

    val nodes: Set<Node> = nodeSet

    override fun evaluate(context: NodeContext): Bit {
        return Bit(nodes.all { it.evaluate(context).value })
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as And

        if (nodeSet != other.nodeSet) return false

        return true
    }

    override fun hashCode(): Int {
        return nodeSet.hashCode()
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
//#endregion

//#region Equation Solver
class XorEquation(left: List<Node>, right: List<Node>) {
    var leftSide = HashSet<Node>()
    var rightSide = HashSet<Node>()

    init {
        left.forEach { l ->
            leftSide.addNode(l)
        }

        right.forEach { r ->
            rightSide.addNode(r)
        }
    }

    fun contains(variable: Variable): Boolean {
        return leftSide.contains(variable) || rightSide.contains(variable)
    }

    fun noVariables(): Boolean {
        return (leftSide.isEmpty() || leftSide.all { it is Bit }) && (rightSide.isEmpty() || rightSide.all { it is Bit })
    }

    fun prepare(variable: Variable) {
        if (rightSide.contains(variable)) {
            val tmp = leftSide
            leftSide = rightSide
            rightSide = tmp
        }

        require(leftSide.contains(variable))

        leftSide.remove(variable)

        leftSide.forEach { node ->
            rightSide.addNode(node)
        }

        leftSide.clear()
        leftSide.add(variable)
    }

    fun substitute(equation: XorEquation) {
        val variable = equation.leftSide.first()

        if (leftSide.contains(variable)) {
            leftSide.remove(variable)
            equation.rightSide.forEach { node ->
                leftSide.addNode(node)
            }
        }

        if (rightSide.contains(variable)) {
            rightSide.remove(variable)
            equation.rightSide.forEach { node ->
                rightSide.addNode(node)
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

    override fun toString(): String {
        val left = leftSide.asSequence().map { it.toString() }.ifEmpty { sequenceOf("0") }.joinToString(" ^ ")
        val right = rightSide.asSequence().map { it.toString() }.ifEmpty { sequenceOf("0") }.joinToString(" ^ ")
        return "$left = $right"
    }
}

object EquationSolver {
    private fun prepare(allVariables: Array<Variable>, equations: Array<XorEquation>) {
        var i = 0
        var v = 0

        while (i < equations.size) {
            var j = i
            val variable = allVariables[v]
            var equationFound = false

            while (j < equations.size) {
                if (equations[j].contains(variable)) {
                    equations[j].prepare(variable)
                    equationFound = true
                    break
                }

                j++
            }

            if (equationFound) {
                if (i < j) {
                    val tmp = equations[i]
                    equations[i] = equations[j]
                    equations[j] = tmp
                }

                j = i + 1

                while (j < equations.size) {
                    equations[j].substitute(equations[i])
                    j++
                }

                i++
            } else if (equations[i].noVariables()) {
                i++
            }

            v++
        }
    }

    private fun evaluate(allVariables: Array<Variable>, equations: Array<XorEquation>, context: NodeContext) {
        var i = equations.size

        while (i >= 0) {
            if (equations[i].noVariables()) {
                i--
                continue
            }


        }
    }

    fun solve(allVariables: Array<Variable>, equations: Array<XorEquation>, context: NodeContext) {
        prepare(allVariables, equations)
        // evaluate(allVariables, equations, context)
    }
}
//#endregion

//#region General Utils
fun generateState(): Array<BitGroup> {
    return (0 until 25 * Long.SIZE_BITS).asSequence()
        .chunked(Long.SIZE_BITS)
        .map { bitIndices ->
            val bits = Array<Node>(Long.SIZE_BITS) { i -> Variable("a${bitIndices[i]}") }
            BitGroup(bits)
        }
        .toList()
        .toTypedArray()
}

fun setVariables(state: LongArray, context: NodeContext) {
    var i = 0

    state.forEach { longValue ->
        val bitGroup = longValue.toBitGroup()

        bitGroup.bits.forEach { bit ->
            context.variables["a$i"] = bit.evaluate(context)
            i++
        }
    }
}

fun Array<BitGroup>.findFunctionsVariableIncludes(): Map<String, Set<String>> {
    val map = LinkedHashMap<String, Set<String>>()

    (0 until 1600).forEach { i ->
        val varName = "a$i"
        val l = LinkedHashSet<String>()

        forEachIndexed { groupIndex, bitGroup -> // 25 groups
            bitGroup.bits.forEachIndexed { bitIndex, node -> // 64 bits
                require(node is Xor)

                node.nodes.forEach { xorNode ->
                    require(xorNode is Variable)

                    if (varName == xorNode.name) {
                        val f = "f${64 * groupIndex + bitIndex}"
                        l.add(f)
                    }
                }
            }
        }

        map[varName] = l
    }

    return map
}

fun findTwoVariablesThatHaveSameFunctions(state: Array<BitGroup>, stateVars: Map<String, Set<String>>) = sequence {
    state.forEach { bitGroup ->
        val set = HashSet<Set<String>>()

        bitGroup.bits.forEach { xor ->
            require(xor is Xor)

            xor.nodes.forEach { variable0 ->
                require(variable0 is Variable)

                xor.nodes.forEach { variable1 ->
                    require(variable1 is Variable)

                    if (variable0.name != variable1.name) {
                        set.add(setOf(variable0.name, variable1.name))
                    }
                }
            }
        }

        set.forEach { pair ->
            val (q, z) = pair.toList()
            val f1 = stateVars[q]?.toSortedSet()
            val f2 = stateVars[z]?.toSortedSet()
            if (f1 == f2) {
                yield("($q, $z; $f1, $f2)")
            }
        }
    }
}
//#endregion

class KeccakPatched private constructor() {
    //#region Public API
    fun hash(message: ByteArray): ByteArray {
        val state = LongArray(STATE_SIZE) { 0 }
        val blocks = message.pad().blocks().longBlocks()

        return state.run {
            absorb(blocks)
            squeeze()
        }
    }
    //#endregion

    //#region Implementation
    private fun ByteArray.pad(): ByteArray {
        val message = this
        val paddingSize = BLOCK_SIZE_BYTES - message.size % BLOCK_SIZE_BYTES
        val padding = mutableListOf<Byte>()

        if (paddingSize == 1) {
            padding.add(0x81.toByte())
        } else if (paddingSize >= 2) {
            padding.add(0x01)

            repeat(paddingSize - 2) {
                padding.add(0x00)
            }

            padding.add(0x80.toByte())
        }

        return message + padding
    }

    private fun ByteArray.blocks(): List<ByteArray> {
        return asSequence()
            .chunked(BLOCK_SIZE_BYTES)
            .map { it.toByteArray() }
            .toList()
    }

    private fun List<ByteArray>.longBlocks(): List<LongArray> {
        return asSequence()
            .map { block ->
                block.asSequence()
                    .chunked(Long.SIZE_BYTES)
                    .map { it.toByteArray() }
                    .map { it.littleEndianToLong() }
                    .toList()
                    .toLongArray()
            }
            .toList()
    }

    private fun LongArray.absorb(blocks: List<LongArray>) {
        blocks.forEach { block ->
            absorb(block)
        }
    }

    private fun LongArray.absorb(block: LongArray) {
        val state = this

        state[0] = state[0] xor block[0]
        state[1] = state[1] xor block[5]
        state[2] = state[2] xor block[10]
        state[3] = state[3] xor block[15]
        state[5] = state[5] xor block[1]
        state[6] = state[6] xor block[6]
        state[7] = state[7] xor block[11]
        state[8] = state[8] xor block[16]
        state[10] = state[10] xor block[2]
        state[11] = state[11] xor block[7]
        state[12] = state[12] xor block[12]
        state[15] = state[15] xor block[3]
        state[16] = state[16] xor block[8]
        state[17] = state[17] xor block[13]
        state[20] = state[20] xor block[4]
        state[21] = state[21] xor block[9]
        state[22] = state[22] xor block[14]

        permutation()
    }

    private fun LongArray.permutation() {
        repeat(24) { round ->
            permutation(round)
        }
    }

    private fun LongArray.permutation(round: Int) {
        //#region Variables
        val state = this
        //#endregion

        //#region alternative θ step
        //val state0 = state.map { it.toBitGroup() }.toTypedArray()
        val state0 = generateState()
        val context = NodeContext()
        setVariables(state, context)

        val c0 = Array(LANE_SIZE) { BitGroup(Array(Long.SIZE_BITS) { Bit() }) }
        val d0 = Array(LANE_SIZE) { BitGroup(Array(Long.SIZE_BITS) { Bit() }) }

        c0[0] = state0[0] xor state0[1] xor state0[2] xor state0[3] xor state0[4]
        c0[1] = state0[5] xor state0[6] xor state0[7] xor state0[8] xor state0[9]
        c0[2] = state0[10] xor state0[11] xor state0[12] xor state0[13] xor state0[14]
        c0[3] = state0[15] xor state0[16] xor state0[17] xor state0[18] xor state0[19]
        c0[4] = state0[20] xor state0[21] xor state0[22] xor state0[23] xor state0[24]


        d0[0] = c0[4] xor c0[1].rotateLeft(1)
        d0[1] = c0[0] xor c0[2].rotateLeft(1)
        d0[2] = c0[1] xor c0[3].rotateLeft(1)
        d0[3] = c0[2] xor c0[4].rotateLeft(1)
        d0[4] = c0[3] xor c0[0].rotateLeft(1)


        state0[0] = state0[0] xor d0[0]
        state0[1] = state0[1] xor d0[0]
        state0[2] = state0[2] xor d0[0]
        state0[3] = state0[3] xor d0[0]
        state0[4] = state0[4] xor d0[0]

        state0[5] = state0[5] xor d0[1]
        state0[6] = state0[6] xor d0[1]
        state0[7] = state0[7] xor d0[1]
        state0[8] = state0[8] xor d0[1]
        state0[9] = state0[9] xor d0[1]

        state0[10] = state0[10] xor d0[2]
        state0[11] = state0[11] xor d0[2]
        state0[12] = state0[12] xor d0[2]
        state0[13] = state0[13] xor d0[2]
        state0[14] = state0[14] xor d0[2]

        state0[15] = state0[15] xor d0[3]
        state0[16] = state0[16] xor d0[3]
        state0[17] = state0[17] xor d0[3]
        state0[18] = state0[18] xor d0[3]
        state0[19] = state0[19] xor d0[3]

        state0[20] = state0[20] xor d0[4]
        state0[21] = state0[21] xor d0[4]
        state0[22] = state0[22] xor d0[4]
        state0[23] = state0[23] xor d0[4]
        state0[24] = state0[24] xor d0[4]
        //#endregion

        //#region Alternative ρ and π steps
        val b0 = Array(STATE_SIZE) { BitGroup(emptyArray()) }

        b0[0] = state0[0].rotateLeft(0)
        b0[1] = state0[15].rotateLeft(28)
        b0[2] = state0[5].rotateLeft(1)
        b0[3] = state0[20].rotateLeft(27)
        b0[4] = state0[10].rotateLeft(62)
        b0[5] = state0[6].rotateLeft(44)
        b0[6] = state0[21].rotateLeft(20)
        b0[7] = state0[11].rotateLeft(6)
        b0[8] = state0[1].rotateLeft(36)
        b0[9] = state0[16].rotateLeft(55)
        b0[10] = state0[12].rotateLeft(43)
        b0[11] = state0[2].rotateLeft(3)
        b0[12] = state0[17].rotateLeft(25)
        b0[13] = state0[7].rotateLeft(10)
        b0[14] = state0[22].rotateLeft(39)
        b0[15] = state0[18].rotateLeft(21)
        b0[16] = state0[8].rotateLeft(45)
        b0[17] = state0[23].rotateLeft(8)
        b0[18] = state0[13].rotateLeft(15)
        b0[19] = state0[3].rotateLeft(41)
        b0[20] = state0[24].rotateLeft(14)
        b0[21] = state0[14].rotateLeft(61)
        b0[22] = state0[4].rotateLeft(18)
        b0[23] = state0[19].rotateLeft(56)
        b0[24] = state0[9].rotateLeft(2)
        //#endregion

        //#region Alternative χ step
        state0[0] = b0[0] xor b0[10] xor (b0[5] and b0[10])
        state0[1] = b0[1] xor b0[11] xor (b0[6] and b0[11])
        state0[2] = b0[2] xor b0[12] xor (b0[7] and b0[12])
        state0[3] = b0[3] xor b0[13] xor (b0[8] and b0[13])
        state0[4] = b0[4] xor b0[14] xor (b0[9] and b0[14])
        state0[5] = b0[5] xor b0[15] xor (b0[10] and b0[15])
        state0[6] = b0[6] xor b0[16] xor (b0[11] and b0[16])
        state0[7] = b0[7] xor b0[17] xor (b0[12] and b0[17])
        state0[8] = b0[8] xor b0[18] xor (b0[13] and b0[18])
        state0[9] = b0[9] xor b0[19] xor (b0[14] and b0[19])
        state0[10] = b0[10] xor b0[20] xor (b0[15] and b0[20])
        state0[11] = b0[11] xor b0[21] xor (b0[16] and b0[21])
        state0[12] = b0[12] xor b0[22] xor (b0[17] and b0[22])
        state0[13] = b0[13] xor b0[23] xor (b0[18] and b0[23])
        state0[14] = b0[14] xor b0[24] xor (b0[19] and b0[24])
        state0[15] = b0[15] xor b0[0] xor (b0[20] and b0[0])
        state0[16] = b0[16] xor b0[1] xor (b0[21] and b0[1])
        state0[17] = b0[17] xor b0[2] xor (b0[22] and b0[2])
        state0[18] = b0[18] xor b0[3] xor (b0[23] and b0[3])
        state0[19] = b0[19] xor b0[4] xor (b0[24] and b0[4])
        state0[20] = b0[20] xor b0[5] xor (b0[0] and b0[5])
        state0[21] = b0[21] xor b0[6] xor (b0[1] and b0[6])
        state0[22] = b0[22] xor b0[7] xor (b0[2] and b0[7])
        state0[23] = b0[23] xor b0[8] xor (b0[3] and b0[8])
        state0[24] = b0[24] xor b0[9] xor (b0[4] and b0[9])
        //#endregion

        //#region Alternative ι step
        state0[0] = state0[0] xor ROUND_CONSTANTS[round].toBitGroup()
        //#endregion

        state0.forEachIndexed { index, bitGroup ->
            state[index] = bitGroup.toLong(context)
        }
    }

    private fun LongArray.squeeze(): ByteArray {
        val state = this

        val outputBytesStream = sequence {
            while (true) {
                state[0].toLittleEndianBytes().forEach { yield(it) }
                state[5].toLittleEndianBytes().forEach { yield(it) }
                state[10].toLittleEndianBytes().forEach { yield(it) }
                state[15].toLittleEndianBytes().forEach { yield(it) }

                permutation()
            }
        }

        return outputBytesStream.take(OUTPUT_SIZE_BYTES).toList().toByteArray()
    }

    private fun ByteArray.littleEndianToLong(): Long {
        val bytes = this
        var value = 0L

        var i = 0
        while (i < bytes.size) {
            value = value or bytes[i].toLong().shl(i * Byte.SIZE_BITS)
            i++
        }

        return value
    }

    private fun Long.toLittleEndianBytes(): ByteArray {
        val value = this
        val bytes = ByteArray(Long.SIZE_BYTES) { 0 }

        var i = 0
        while (i < Long.SIZE_BYTES) {
            bytes[i] = (value.shr(i * Byte.SIZE_BITS) and UByte.MAX_VALUE.toLong()).toByte()
            i++
        }

        return bytes
    }
    //#endregion

    companion object {
        //#region Private Constants
        private const val LANE_SIZE = 5
        private const val STATE_SIZE = LANE_SIZE * LANE_SIZE
        private const val BLOCK_SIZE_BYTES = 136
        private const val OUTPUT_SIZE_BYTES = 32

        private val ROUND_CONSTANTS = longArrayOf(
            0x0000000000000001uL.toLong(),
            0x0000000000008082uL.toLong(),
            0x800000000000808auL.toLong(),
            0x8000000080008000uL.toLong(),
            0x000000000000808buL.toLong(),
            0x0000000080000001uL.toLong(),
            0x8000000080008081uL.toLong(),
            0x8000000000008009uL.toLong(),
            0x000000000000008auL.toLong(),
            0x0000000000000088uL.toLong(),
            0x0000000080008009uL.toLong(),
            0x000000008000000auL.toLong(),
            0x000000008000808buL.toLong(),
            0x800000000000008buL.toLong(),
            0x8000000000008089uL.toLong(),
            0x8000000000008003uL.toLong(),
            0x8000000000008002uL.toLong(),
            0x8000000000000080uL.toLong(),
            0x000000000000800auL.toLong(),
            0x800000008000000auL.toLong(),
            0x8000000080008081uL.toLong(),
            0x8000000000008080uL.toLong(),
            0x0000000080000001uL.toLong(),
            0x8000000080008008uL.toLong(),
        )
        //#endregion

        //#region Keccak Implementations
        val KECCAK_256 = KeccakPatched()
        //#endregion
    }
}
