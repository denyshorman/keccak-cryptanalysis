package keccak.util

import io.kotest.core.spec.style.FunSpec
import kotlin.test.assertEquals

class LongUtilsTest : FunSpec({
    context("toBitString") {
        test("1") {
            val long = 0xAABBCCDD11223344uL.toLong()
            assertEquals("1010101010111011110011001101110100010001001000100011001101000100", long.toBitString())
        }
    }
})
