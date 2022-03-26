package keccak.util

import keccak.*
import java.util.*

fun AndEquationSystem.sortBySolvableGroups(): AndEquationSystem {
    val offsets = buildGroupOffsets()
    val batchCount = 1600
    val andSystem = AndEquationSystem(rows, cols)

    var eqIndex = 0
    while (eqIndex < rows) {
        val offset = eqIndex % batchCount
        val eqIndex2 = offsets[offset / 5][offset % 5] + (eqIndex / batchCount) * batchCount

        andSystem.equations[eqIndex].andOpLeft = equations[eqIndex2].andOpLeft
        andSystem.equations[eqIndex].andOpRight = equations[eqIndex2].andOpRight
        andSystem.equations[eqIndex].rightXor = equations[eqIndex2].rightXor
        andSystem.andOpLeftResults.setIfTrue(eqIndex, andOpLeftResults[eqIndex2])
        andSystem.andOpRightResults.setIfTrue(eqIndex, andOpRightResults[eqIndex2])
        andSystem.rightXorResults.setIfTrue(eqIndex, rightXorResults[eqIndex2])

        eqIndex++
    }

    return andSystem
}

fun AndEquationSystem.buildGroupOffsets(): Array<IntArray> {
    val batchCount = 1600
    val varOffset = -1088

    val eqs = Array(batchCount) { i ->
        val offset = i + batchCount
        val lastSetBit = equations[offset].rightXor.previousSetBit(cols - 1)
        equations[offset].rightXor.clear(lastSetBit)

        val l = equations[offset].andOpLeft.toXorString(varOffset = varOffset)
        val r = equations[offset].andOpRight.toXorString(varOffset = varOffset)
        val x = equations[offset].rightXor.toXorString(varOffset = varOffset)

        equations[offset].rightXor.set(lastSetBit)

        i to arrayOf(l, r, x)
    }

    var eqIndex = 0
    while (eqIndex < batchCount) {
        var eqIndex2 = eqIndex + 1
        while (eqIndex2 < batchCount) {
            if (eqs[eqIndex2].second[1] == eqs[eqIndex].second[2]) {
                eqs.exchange(eqIndex + 1, eqIndex2)
                break
            }
            eqIndex2++
        }

        eqIndex++
    }

    val groupSize = 5
    var groupIndex = 0
    var groupItemIndex = 0
    val groups = Array(batchCount/groupSize) { IntArray(groupSize) { -1 } }

    eqIndex = 0
    while (eqIndex < batchCount) {
        groups[groupIndex][groupItemIndex] = eqs[eqIndex].first

        if (groupItemIndex == 4) {
            groupItemIndex = 0
            groupIndex++
        } else {
            groupItemIndex++
        }

        eqIndex++
    }

    return groups
}

fun AndEquationSystem.invertGroup(groupIndex: Int): NodeEquationSystem {
    val batchCount = 1600
    val groupsCount = rows / batchCount

    val nodeEqSystem = NodeEquationSystem(batchCount)

    if (groupIndex + 1 == groupsCount) {

    } else {
        val groupSize = 5
        val t = Bit(true)
        val f = Bit(false)
        val x = Array(groupSize) { BitSet(cols) }
        val y = Array(groupSize) { BitSet(cols) }
        val xr = BooleanArray(groupSize)
        val yr = BooleanArray(groupSize)
        val xx = Array<Node>(groupSize) { f }
        val yy = Array<Node>(groupSize) { f }

        var eqIndex = groupIndex * batchCount
        val endIndex = eqIndex + batchCount
        while (eqIndex < endIndex) {
            var i = 0
            while (i < groupSize) {
                x[i].xor(equations[eqIndex + (i + 2) % groupSize].andOpLeft)
                xr[i] = andOpLeftResults[eqIndex + (i + 2) % groupSize]
                xx[i] = x[i].toNode(xr[i]).simplify()

                y[i].xor(equations[eqIndex + i].rightXor)
                y[i].xor(x[i])
                yr[i] = rightXorResults[eqIndex + i] xor xr[i]
                yy[i] = y[i].toNode(yr[i]).simplify()

                i++
            }

            i = 0
            while (i < groupSize) {
                val r = xx[i] + yy[i] + (yy[(i + 4) % groupSize] + t)*(yy[(i + 1) % groupSize]*(yy[(i + 2) % groupSize] + t) + yy[(i + 3) % groupSize])
                nodeEqSystem.equations.add(r)
                i++
            }

            i = 0
            while (i < groupSize) {
                x[i].clear()
                y[i].clear()
                xr[i] = false
                yr[i] = false
                xx[i] = f
                yy[i] = f
                i++
            }

            eqIndex += groupSize
        }
    }

    return nodeEqSystem
}