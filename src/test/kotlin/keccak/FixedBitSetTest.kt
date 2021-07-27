package keccak

import io.kotest.core.spec.style.FunSpec
import kotlin.test.assertEquals

class FixedBitSetTest : FunSpec({
    test("toString()") {
        val bitSet = FixedBitSet(8)
        bitSet[0] = true
        bitSet[6] = true
        assertEquals("10000010", bitSet.toString())
    }
})
