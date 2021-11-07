package keccak.util

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MathUtilsTest : FunSpec({
    test("pow2") {
        pow2(0).shouldBe(1)
        pow2(1).shouldBe(2)
        pow2(2).shouldBe(4)
        pow2(3).shouldBe(8)
        pow2(4).shouldBe(16)
        pow2(5).shouldBe(32)
    }
})
