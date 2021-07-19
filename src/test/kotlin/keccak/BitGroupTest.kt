package keccak

import io.kotest.core.spec.style.FunSpec
import org.junit.jupiter.api.Assertions.assertEquals

class BitGroupTest : FunSpec({
    test("littleEndianBytesToLong") {
        val longValue = 0xAABBCCDDL

        val longValueAfterManipulations = longValue
            .toBitGroup()
            .toLittleEndianBytes()
            .toList()
            .toTypedArray()
            .littleEndianBytesToLong()
            .toLong(NodeContext())

        assertEquals(longValue, longValueAfterManipulations)
    }
})
