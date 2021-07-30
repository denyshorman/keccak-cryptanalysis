package keccak.util

import io.kotest.core.spec.style.FunSpec
import kotlin.test.assertEquals

class ByteUtilsTest : FunSpec({
    context("setBit") {
        test("1") {
            var byte: Byte = 0
            byte = byte.setBit(0, true)
            byte = byte.setBit(6, true)
            assertEquals("10000010", byte.toBitString())
        }

        test("2") {
            var byte = 0xFF.toByte()
            byte = byte.setBit(0, false)
            byte = byte.setBit(6, false)
            assertEquals("01111101", byte.toBitString())
        }
    }

    context("toBitString") {
        test("1") {
            val byte = 0x77.toByte()
            assertEquals("01110111", byte.toBitString())
        }
    }
})
