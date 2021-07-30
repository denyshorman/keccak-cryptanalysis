package keccak.util

import io.kotest.core.spec.style.FunSpec
import kotlin.test.assertEquals

class LongUtilsTest : FunSpec({
    context("setBit") {
        test("1") {
            var long = 0L
            long = long.setBit(0, true)
            long = long.setBit(63, true)
            long = long.setBit(62, true)
            long = long.setBit(62, false)
            assertEquals("1000000000000000000000000000000000000000000000000000000000000001", long.toBitString())
        }
    }

    context("getByte") {
        test("1") {
            val long = 0xAABBCCDD11223344uL.toLong()
            assertEquals(0xAA.toByte(), long.getByte(0))
            assertEquals(0xBB.toByte(), long.getByte(1))
            assertEquals(0xCC.toByte(), long.getByte(2))
            assertEquals(0xDD.toByte(), long.getByte(3))
            assertEquals(0x11.toByte(), long.getByte(4))
            assertEquals(0x22.toByte(), long.getByte(5))
            assertEquals(0x33.toByte(), long.getByte(6))
            assertEquals(0x44.toByte(), long.getByte(7))
        }
    }

    context("toBitString") {
        test("1") {
            val long = 0xAABBCCDD11223344uL.toLong()
            assertEquals("1010101010111011110011001101110100010001001000100011001101000100", long.toBitString())
        }
    }
})
