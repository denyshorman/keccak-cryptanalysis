package keccak

import io.kotest.core.spec.style.FunSpec
import kotlin.test.assertEquals

class ByteUtilsTest : FunSpec({
    context("toBitString") {
        test("1") {
            val byte = 0x77.toByte()
            assertEquals("01110111", byte.toBitString())
        }
    }
})
