package keccak.util

import io.kotest.core.spec.style.FunSpec
import keccak.XorEquationSystem
import kotlin.test.assertEquals

class XorEquationSystemUtilsTest : FunSpec({
    context("toLittleEndianBytes") {
        test("1") {
            val long = 0xAABBCCDD11223344uL.toLong().toEquationSystem(Long.SIZE_BITS)
            long.setVariables()
            val bytes = long.toLittleEndianBytes()
            assertEquals(0x44.toByte(), bytes[0].toByte())
            assertEquals(0x33.toByte(), bytes[1].toByte())
            assertEquals(0x22.toByte(), bytes[2].toByte())
            assertEquals(0x11.toByte(), bytes[3].toByte())
            assertEquals(0xDD.toByte(), bytes[4].toByte())
            assertEquals(0xCC.toByte(), bytes[5].toByte())
            assertEquals(0xBB.toByte(), bytes[6].toByte())
            assertEquals(0xAA.toByte(), bytes[7].toByte())
        }

        test("1") {
            val long = 0xAABBCCDD11223344uL.toLong()
            val longBitGroup = long.toBitGroup()
            val system = XorEquationSystem(1, Long.SIZE_BITS)
            system.setVariables()
            val bytes = system.toLittleEndianBytes()
            var i = 0
            while (i < bytes.size) {
                bytes[i].evaluate(longBitGroup.bitSet)
                assertEquals(bytes[i].toByte(), long.getByte(i))
                i++
            }
        }
    }

    test("littleEndianBytesToLong") {
        val expected = 0xAABBCCDD11223344uL.toLong()
        val actual = expected
            .toEquationSystem(Long.SIZE_BITS)
            .toLittleEndianBytes()
            .littleEndianBytesToLong(Long.SIZE_BITS)
            .toLong()

        assertEquals(expected, actual)
    }

    test("setVariables") {
        val eq = XorEquationSystem(3, 3)
        eq.setVariables()
        assertEquals("100", eq.equations[0].toString())
        assertEquals("010", eq.equations[1].toString())
        assertEquals("001", eq.equations[2].toString())
    }
})
