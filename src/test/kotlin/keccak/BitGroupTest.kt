package keccak

import io.kotest.core.spec.style.FunSpec
import keccak.util.toBitGroup
import kotlin.test.assertEquals

class BitGroupTest : FunSpec({
    context("flip") {
        test("1") {
            val actual = BitGroup(4)
            actual[0] = true
            actual[1] = true
            actual[2] = true
            actual[3] = true

            actual.invert()

            val expected = BitGroup(4)
            expected[0] = false
            expected[1] = false
            expected[2] = false
            expected[3] = false

            assertEquals(expected, actual)
        }

        test("2") {
            val actual = BitGroup(4)
            actual[0] = true
            actual[1] = false
            actual[2] = true
            actual[3] = false

            actual.invert()

            val expected = BitGroup(4)
            expected[0] = false
            expected[1] = true
            expected[2] = false
            expected[3] = true

            assertEquals(expected, actual)
        }
    }

    context("extensions") {
        context("Byte.toFixedBitSet") {
            test("1") {
                val byte = 0xFA.toByte()
                val bitSet = byte.toBitGroup()

                assertEquals(8, bitSet.size)
                assertEquals("11111010", bitSet.toString())
            }
        }

        context("ByteArray.toFixedBitSet()") {
            test("1") {
                val bytes = byteArrayOf(0x1A.toByte(), 0xF1.toByte())
                val bitSet = bytes.toBitGroup()

                assertEquals(16, bitSet.size)
                assertEquals("0001101011110001", bitSet.toString())
            }
        }
    }

    test("toString()") {
        val bitSet = BitGroup(8)
        bitSet[0] = true
        bitSet[6] = true
        assertEquals("10000010", bitSet.toString())
    }
})
