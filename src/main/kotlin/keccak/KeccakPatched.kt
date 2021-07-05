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
        } else {
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
        val b = LongArray(STATE_SIZE) { 0 }
        val c = LongArray(LANE_SIZE) { 0 }
        val d = LongArray(LANE_SIZE) { 0 }
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

        //#region θ step
        c[0] = state[0] xor state[1] xor state[2] xor state[3] xor state[4]
        c[1] = state[5] xor state[6] xor state[7] xor state[8] xor state[9]
        c[2] = state[10] xor state[11] xor state[12] xor state[13] xor state[14]
        c[3] = state[15] xor state[16] xor state[17] xor state[18] xor state[19]
        c[4] = state[20] xor state[21] xor state[22] xor state[23] xor state[24]


        d[0] = c[4] xor c[1].rotateLeft(1)
        d[1] = c[0] xor c[2].rotateLeft(1)
        d[2] = c[1] xor c[3].rotateLeft(1)
        d[3] = c[2] xor c[4].rotateLeft(1)
        d[4] = c[3] xor c[0].rotateLeft(1)


        state[0] = state[0] xor d[0]
        state[1] = state[1] xor d[0]
        state[2] = state[2] xor d[0]
        state[3] = state[3] xor d[0]
        state[4] = state[4] xor d[0]

        state[5] = state[5] xor d[1]
        state[6] = state[6] xor d[1]
        state[7] = state[7] xor d[1]
        state[8] = state[8] xor d[1]
        state[9] = state[9] xor d[1]

        state[10] = state[10] xor d[2]
        state[11] = state[11] xor d[2]
        state[12] = state[12] xor d[2]
        state[13] = state[13] xor d[2]
        state[14] = state[14] xor d[2]

        state[15] = state[15] xor d[3]
        state[16] = state[16] xor d[3]
        state[17] = state[17] xor d[3]
        state[18] = state[18] xor d[3]
        state[19] = state[19] xor d[3]

        state[20] = state[20] xor d[4]
        state[21] = state[21] xor d[4]
        state[22] = state[22] xor d[4]
        state[23] = state[23] xor d[4]
        state[24] = state[24] xor d[4]
        //#endregion

        var state1 = state0.map { it.toLong(context) }.toLongArray()
        require(state1.contentEquals(state))

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

        //#region ρ and π steps
        b[0] = state[0].rotateLeft(0)
        b[1] = state[15].rotateLeft(28)
        b[2] = state[5].rotateLeft(1)
        b[3] = state[20].rotateLeft(27)
        b[4] = state[10].rotateLeft(62)
        b[5] = state[6].rotateLeft(44)
        b[6] = state[21].rotateLeft(20)
        b[7] = state[11].rotateLeft(6)
        b[8] = state[1].rotateLeft(36)
        b[9] = state[16].rotateLeft(55)
        b[10] = state[12].rotateLeft(43)
        b[11] = state[2].rotateLeft(3)
        b[12] = state[17].rotateLeft(25)
        b[13] = state[7].rotateLeft(10)
        b[14] = state[22].rotateLeft(39)
        b[15] = state[18].rotateLeft(21)
        b[16] = state[8].rotateLeft(45)
        b[17] = state[23].rotateLeft(8)
        b[18] = state[13].rotateLeft(15)
        b[19] = state[3].rotateLeft(41)
        b[20] = state[24].rotateLeft(14)
        b[21] = state[14].rotateLeft(61)
        b[22] = state[4].rotateLeft(18)
        b[23] = state[19].rotateLeft(56)
        b[24] = state[9].rotateLeft(2)
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

        //#region χ step
        state[0] = b[0] xor b[10] xor (b[5] and b[10])
        state[1] = b[1] xor b[11] xor (b[6] and b[11])
        state[2] = b[2] xor b[12] xor (b[7] and b[12])
        state[3] = b[3] xor b[13] xor (b[8] and b[13])
        state[4] = b[4] xor b[14] xor (b[9] and b[14])
        state[5] = b[5] xor b[15] xor (b[10] and b[15])
        state[6] = b[6] xor b[16] xor (b[11] and b[16])
        state[7] = b[7] xor b[17] xor (b[12] and b[17])
        state[8] = b[8] xor b[18] xor (b[13] and b[18])
        state[9] = b[9] xor b[19] xor (b[14] and b[19])
        state[10] = b[10] xor b[20] xor (b[15] and b[20])
        state[11] = b[11] xor b[21] xor (b[16] and b[21])
        state[12] = b[12] xor b[22] xor (b[17] and b[22])
        state[13] = b[13] xor b[23] xor (b[18] and b[23])
        state[14] = b[14] xor b[24] xor (b[19] and b[24])
        state[15] = b[15] xor b[0] xor (b[20] and b[0])
        state[16] = b[16] xor b[1] xor (b[21] and b[1])
        state[17] = b[17] xor b[2] xor (b[22] and b[2])
        state[18] = b[18] xor b[3] xor (b[23] and b[3])
        state[19] = b[19] xor b[4] xor (b[24] and b[4])
        state[20] = b[20] xor b[5] xor (b[0] and b[5])
        state[21] = b[21] xor b[6] xor (b[1] and b[6])
        state[22] = b[22] xor b[7] xor (b[2] and b[7])
        state[23] = b[23] xor b[8] xor (b[3] and b[8])
        state[24] = b[24] xor b[9] xor (b[4] and b[9])
        //#endregion

        state1 = state0.map { it.toLong(context) }.toLongArray()
        require(state1.contentEquals(state))

        //#region Alternative ι step
        state0[0] = state0[0] xor ROUND_CONSTANTS[round].toBitGroup()
        //#endregion

        //#region ι step
        state[0] = state[0] xor ROUND_CONSTANTS[round]
        //#endregion

        state1 = state0.map { it.toLong(context) }.toLongArray()
        require(state1.contentEquals(state))
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
