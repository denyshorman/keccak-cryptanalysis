package keccak

import java.util.*

//#region General Utils
fun generateState(): Array<BitGroup> {
    return (0 until 25 * Long.SIZE_BITS).asSequence()
        .chunked(Long.SIZE_BITS)
        .map { bitIndices ->
            val bits = Array<Node>(Long.SIZE_BITS) { i -> Variable("${bitIndices[i]}") }
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
            context.variables["$i"] = bit.evaluate(context)
            i++
        }
    }
}

fun Array<BitGroup>.toArrayBitSet(): Array<BitSet> {
    val bitGroups = this

    return bitGroups.flatMap { bitGroup ->
        bitGroup.bits.map { xor ->
            require(xor is Xor)

            val bitSet = BitSet(bitGroups.size * Long.SIZE_BITS)

            xor.nodes.forEach { variable ->
                require(variable is Variable)

                val pos = variable.name.toInt()
                bitSet[pos] = true
            }

            bitSet
        }
    }.toTypedArray()
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

fun LongArray.toBitSet(): BitSet {
    val longArray = map { java.lang.Long.reverse(it) }.toLongArray()
    return BitSet.valueOf(longArray)
}

fun Array<BitSet>.hasCollision(): Boolean {
    var i = 0
    while (i < size) {
        if (!this[i][i]) {
            return true
        }

        i++
    }
    return false
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
        val state = State()
        val longBlocks = message.pad().blocks().longBlocks()
        val bitGroupBlocks = longBlocks.seedBlocks()
        val blocks = bitGroupBlocks.zip(longBlocks) { a, b -> Block(a, b) }

        val output = state.run {
            absorb(blocks)
            squeeze()
        }

        val context = longBlocks.seedContext()

        output.forEach { x ->
            val computedByte = x.bitGroup.toByte(context)
            require(x.byte == computedByte) { "Computed byte $computedByte does not equal to real byte ${x.byte}" }
        }

        return output.map { it.byte }.toByteArray()
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

    private fun List<LongArray>.bitGroupBlocks(): List<Array<BitGroup>> {
        return map { block -> block.map { it.toBitGroup() }.toTypedArray() }
    }

    private fun List<LongArray>.seedBlocks(): List<Array<BitGroup>> {
        return mapIndexed { blockIndex, block ->
            block.mapIndexed { longIndex, _ ->
                BitGroup(Array(Long.SIZE_BITS) { eqIndex ->
                    val varIndex = BLOCK_SIZE_BITS * blockIndex + Long.SIZE_BITS * longIndex + eqIndex
                    val varName = "a$varIndex"
                    Variable(varName)
                })
            }.toTypedArray()
        }
    }

    private fun List<LongArray>.seedContext(): NodeContext {
        val context = NodeContext()

        forEachIndexed { blockIndex, block ->
            block.forEachIndexed { longIndex, long ->
                val bitGroup = long.toBitGroup()
                bitGroup.bits.forEachIndexed { bitIndex, bit ->
                    require(bit is Bit)
                    val varIndex = BLOCK_SIZE_BITS * blockIndex + Long.SIZE_BITS * longIndex + bitIndex
                    val varName = "a$varIndex"
                    context.variables[varName] = bit
                }
            }
        }

        return context
    }

    private fun State.absorb(blocks: List<Block>) {
        blocks.forEach { block ->
            absorb(block)
        }
    }

    private fun State.absorb(block: Block) {
        //#region Init
        val state0 = this.state0
        val state1 = this.state1

        val block0 = block.block0
        val block1 = block.block1
        //#endregion

        //#region Append0
        state0[0] = state0[0] xor block0[0]
        state0[1] = state0[1] xor block0[5]
        state0[2] = state0[2] xor block0[10]
        state0[3] = state0[3] xor block0[15]
        state0[5] = state0[5] xor block0[1]
        state0[6] = state0[6] xor block0[6]
        state0[7] = state0[7] xor block0[11]
        state0[8] = state0[8] xor block0[16]
        state0[10] = state0[10] xor block0[2]
        state0[11] = state0[11] xor block0[7]
        state0[12] = state0[12] xor block0[12]
        state0[15] = state0[15] xor block0[3]
        state0[16] = state0[16] xor block0[8]
        state0[17] = state0[17] xor block0[13]
        state0[20] = state0[20] xor block0[4]
        state0[21] = state0[21] xor block0[9]
        state0[22] = state0[22] xor block0[14]
        //#endregion

        //#region Append1
        state1[0] = state1[0] xor block1[0]
        state1[1] = state1[1] xor block1[5]
        state1[2] = state1[2] xor block1[10]
        state1[3] = state1[3] xor block1[15]
        state1[5] = state1[5] xor block1[1]
        state1[6] = state1[6] xor block1[6]
        state1[7] = state1[7] xor block1[11]
        state1[8] = state1[8] xor block1[16]
        state1[10] = state1[10] xor block1[2]
        state1[11] = state1[11] xor block1[7]
        state1[12] = state1[12] xor block1[12]
        state1[15] = state1[15] xor block1[3]
        state1[16] = state1[16] xor block1[8]
        state1[17] = state1[17] xor block1[13]
        state1[20] = state1[20] xor block1[4]
        state1[21] = state1[21] xor block1[9]
        state1[22] = state1[22] xor block1[14]
        //#endregion

        permutation()
    }

    private fun State.permutation() {
        repeat(24) { round ->
            permutation(round)
        }
    }

    private fun State.permutation(round: Int) {
        //#region Variables
        val state0 = this.state0
        val state1 = this.state1
        //#endregion

        //#region θ step
        val c1 = LongArray(LANE_SIZE) { 0 }
        val d1 = LongArray(LANE_SIZE) { 0 }

        c1[0] = state1[0] xor state1[1] xor state1[2] xor state1[3] xor state1[4]
        c1[1] = state1[5] xor state1[6] xor state1[7] xor state1[8] xor state1[9]
        c1[2] = state1[10] xor state1[11] xor state1[12] xor state1[13] xor state1[14]
        c1[3] = state1[15] xor state1[16] xor state1[17] xor state1[18] xor state1[19]
        c1[4] = state1[20] xor state1[21] xor state1[22] xor state1[23] xor state1[24]


        d1[0] = c1[4] xor c1[1].rotateLeft(1)
        d1[1] = c1[0] xor c1[2].rotateLeft(1)
        d1[2] = c1[1] xor c1[3].rotateLeft(1)
        d1[3] = c1[2] xor c1[4].rotateLeft(1)
        d1[4] = c1[3] xor c1[0].rotateLeft(1)


        state1[0] = state1[0] xor d1[0]
        state1[1] = state1[1] xor d1[0]
        state1[2] = state1[2] xor d1[0]
        state1[3] = state1[3] xor d1[0]
        state1[4] = state1[4] xor d1[0]

        state1[5] = state1[5] xor d1[1]
        state1[6] = state1[6] xor d1[1]
        state1[7] = state1[7] xor d1[1]
        state1[8] = state1[8] xor d1[1]
        state1[9] = state1[9] xor d1[1]

        state1[10] = state1[10] xor d1[2]
        state1[11] = state1[11] xor d1[2]
        state1[12] = state1[12] xor d1[2]
        state1[13] = state1[13] xor d1[2]
        state1[14] = state1[14] xor d1[2]

        state1[15] = state1[15] xor d1[3]
        state1[16] = state1[16] xor d1[3]
        state1[17] = state1[17] xor d1[3]
        state1[18] = state1[18] xor d1[3]
        state1[19] = state1[19] xor d1[3]

        state1[20] = state1[20] xor d1[4]
        state1[21] = state1[21] xor d1[4]
        state1[22] = state1[22] xor d1[4]
        state1[23] = state1[23] xor d1[4]
        state1[24] = state1[24] xor d1[4]
        //#endregion

        //#region Alternative θ step
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

        //#region ρ and π steps
        val b1 = LongArray(STATE_SIZE) { 0 }

        b1[0] = state1[0].rotateLeft(0)
        b1[1] = state1[15].rotateLeft(28)
        b1[2] = state1[5].rotateLeft(1)
        b1[3] = state1[20].rotateLeft(27)
        b1[4] = state1[10].rotateLeft(62)
        b1[5] = state1[6].rotateLeft(44)
        b1[6] = state1[21].rotateLeft(20)
        b1[7] = state1[11].rotateLeft(6)
        b1[8] = state1[1].rotateLeft(36)
        b1[9] = state1[16].rotateLeft(55)
        b1[10] = state1[12].rotateLeft(43)
        b1[11] = state1[2].rotateLeft(3)
        b1[12] = state1[17].rotateLeft(25)
        b1[13] = state1[7].rotateLeft(10)
        b1[14] = state1[22].rotateLeft(39)
        b1[15] = state1[18].rotateLeft(21)
        b1[16] = state1[8].rotateLeft(45)
        b1[17] = state1[23].rotateLeft(8)
        b1[18] = state1[13].rotateLeft(15)
        b1[19] = state1[3].rotateLeft(41)
        b1[20] = state1[24].rotateLeft(14)
        b1[21] = state1[14].rotateLeft(61)
        b1[22] = state1[4].rotateLeft(18)
        b1[23] = state1[19].rotateLeft(56)
        b1[24] = state1[9].rotateLeft(2)
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

        //#region χ step
        state1[0] = b1[0] xor b1[10] xor (b1[5] and b1[10])
        state1[1] = b1[1] xor b1[11] xor (b1[6] and b1[11])
        state1[2] = b1[2] xor b1[12] xor (b1[7] and b1[12])
        state1[3] = b1[3] xor b1[13] xor (b1[8] and b1[13])
        state1[4] = b1[4] xor b1[14] xor (b1[9] and b1[14])
        state1[5] = b1[5] xor b1[15] xor (b1[10] and b1[15])
        state1[6] = b1[6] xor b1[16] xor (b1[11] and b1[16])
        state1[7] = b1[7] xor b1[17] xor (b1[12] and b1[17])
        state1[8] = b1[8] xor b1[18] xor (b1[13] and b1[18])
        state1[9] = b1[9] xor b1[19] xor (b1[14] and b1[19])
        state1[10] = b1[10] xor b1[20] xor (b1[15] and b1[20])
        state1[11] = b1[11] xor b1[21] xor (b1[16] and b1[21])
        state1[12] = b1[12] xor b1[22] xor (b1[17] and b1[22])
        state1[13] = b1[13] xor b1[23] xor (b1[18] and b1[23])
        state1[14] = b1[14] xor b1[24] xor (b1[19] and b1[24])
        state1[15] = b1[15] xor b1[0] xor (b1[20] and b1[0])
        state1[16] = b1[16] xor b1[1] xor (b1[21] and b1[1])
        state1[17] = b1[17] xor b1[2] xor (b1[22] and b1[2])
        state1[18] = b1[18] xor b1[3] xor (b1[23] and b1[3])
        state1[19] = b1[19] xor b1[4] xor (b1[24] and b1[4])
        state1[20] = b1[20] xor b1[5] xor (b1[0] and b1[5])
        state1[21] = b1[21] xor b1[6] xor (b1[1] and b1[6])
        state1[22] = b1[22] xor b1[7] xor (b1[2] and b1[7])
        state1[23] = b1[23] xor b1[8] xor (b1[3] and b1[8])
        state1[24] = b1[24] xor b1[9] xor (b1[4] and b1[9])
        //#endregion

        //#region Alternative χ step
        state0[0] = b0[0] xor b0[10] xor (b1[5] and b1[10]).toBitGroup()
        state0[1] = b0[1] xor b0[11] xor (b1[6] and b1[11]).toBitGroup()
        state0[2] = b0[2] xor b0[12] xor (b1[7] and b1[12]).toBitGroup()
        state0[3] = b0[3] xor b0[13] xor (b1[8] and b1[13]).toBitGroup()
        state0[4] = b0[4] xor b0[14] xor (b1[9] and b1[14]).toBitGroup()
        state0[5] = b0[5] xor b0[15] xor (b1[10] and b1[15]).toBitGroup()
        state0[6] = b0[6] xor b0[16] xor (b1[11] and b1[16]).toBitGroup()
        state0[7] = b0[7] xor b0[17] xor (b1[12] and b1[17]).toBitGroup()
        state0[8] = b0[8] xor b0[18] xor (b1[13] and b1[18]).toBitGroup()
        state0[9] = b0[9] xor b0[19] xor (b1[14] and b1[19]).toBitGroup()
        state0[10] = b0[10] xor b0[20] xor (b1[15] and b1[20]).toBitGroup()
        state0[11] = b0[11] xor b0[21] xor (b1[16] and b1[21]).toBitGroup()
        state0[12] = b0[12] xor b0[22] xor (b1[17] and b1[22]).toBitGroup()
        state0[13] = b0[13] xor b0[23] xor (b1[18] and b1[23]).toBitGroup()
        state0[14] = b0[14] xor b0[24] xor (b1[19] and b1[24]).toBitGroup()
        state0[15] = b0[15] xor b0[0] xor (b1[20] and b1[0]).toBitGroup()
        state0[16] = b0[16] xor b0[1] xor (b1[21] and b1[1]).toBitGroup()
        state0[17] = b0[17] xor b0[2] xor (b1[22] and b1[2]).toBitGroup()
        state0[18] = b0[18] xor b0[3] xor (b1[23] and b1[3]).toBitGroup()
        state0[19] = b0[19] xor b0[4] xor (b1[24] and b1[4]).toBitGroup()
        state0[20] = b0[20] xor b0[5] xor (b1[0] and b1[5]).toBitGroup()
        state0[21] = b0[21] xor b0[6] xor (b1[1] and b1[6]).toBitGroup()
        state0[22] = b0[22] xor b0[7] xor (b1[2] and b1[7]).toBitGroup()
        state0[23] = b0[23] xor b0[8] xor (b1[3] and b1[8]).toBitGroup()
        state0[24] = b0[24] xor b0[9] xor (b1[4] and b1[9]).toBitGroup()
        //#endregion

        //#region ι step
        state1[0] = state1[0] xor ROUND_CONSTANTS[round]
        //#endregion

        //#region Alternative ι step
        state0[0] = state0[0] xor ROUND_CONSTANTS[round].toBitGroup()
        //#endregion
    }

    private fun State.squeeze(): List<Output> {
        val state0 = this.state0
        val state1 = this.state1

        val outputBytesStream = sequence {
            while (true) {
                state1[0].toLittleEndianBytes().zip(state0[0].toLittleEndianBytes()).forEach { (a, b) -> yield(Output(a, b)) }
                state1[5].toLittleEndianBytes().zip(state0[5].toLittleEndianBytes()).forEach { (a, b) -> yield(Output(a, b)) }
                state1[10].toLittleEndianBytes().zip(state0[10].toLittleEndianBytes()).forEach { (a, b) -> yield(Output(a, b)) }
                state1[15].toLittleEndianBytes().zip(state0[15].toLittleEndianBytes()).forEach { (a, b) -> yield(Output(a, b)) }

                permutation()
            }
        }

        return outputBytesStream.take(OUTPUT_SIZE_BYTES).toList()
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

    private fun BitGroup.toLittleEndianBytes(): Array<BitGroup> {
        return bits.asSequence()
            .chunked(Byte.SIZE_BITS)
            .map { BitGroup(it.toTypedArray()) }
            .toList()
            .reversed()
            .toTypedArray()
    }
    //#endregion

    //#region Private Models
    private class State(
        val state0: Array<BitGroup> = Array(STATE_SIZE) { BitGroup(Array(Long.SIZE_BITS) { Bit() }) },
        val state1: LongArray = LongArray(STATE_SIZE) { 0 },
    )

    private class Block(
        val block0: Array<BitGroup>,
        val block1: LongArray,
    )

    private class Output(
        val byte: Byte,
        val bitGroup: BitGroup,
    )
    //#endregion

    companion object {
        //#region Private Constants
        private const val LANE_SIZE = 5
        private const val STATE_SIZE = LANE_SIZE * LANE_SIZE
        private const val BLOCK_SIZE_BYTES = 136
        private const val BLOCK_SIZE_BITS = BLOCK_SIZE_BYTES * Byte.SIZE_BITS
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
