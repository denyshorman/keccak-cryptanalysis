package keccak

import io.kotest.core.spec.style.FunSpec
import kotlin.test.assertEquals

class FixedBitSetTest : FunSpec({
    context("flip") {
        test("1") {
            val actual = FixedBitSet(4)
            actual[0] = true
            actual[1] = true
            actual[2] = true
            actual[3] = true

            actual.invert()

            val expected = FixedBitSet(4)
            expected[0] = false
            expected[1] = false
            expected[2] = false
            expected[3] = false

            assertEquals(expected, actual)
        }

        test("2") {
            val actual = FixedBitSet(4)
            actual[0] = true
            actual[1] = false
            actual[2] = true
            actual[3] = false

            actual.invert()

            val expected = FixedBitSet(4)
            expected[0] = false
            expected[1] = true
            expected[2] = false
            expected[3] = true

            assertEquals(expected, actual)
        }
    }

    test("toString()") {
        val bitSet = FixedBitSet(8)
        bitSet[0] = true
        bitSet[6] = true
        assertEquals("10000010", bitSet.toString())
    }
})
