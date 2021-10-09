package keccak

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import keccak.util.toString

class CombinationIteratorTest : FunSpec({
    context("algorithm") {
        context("increasing") {
            test("1") {
                val iter = CombinationIterator(
                    varsCount = 4,
                    algorithm = CombinationIterator.Algorithm.Increasing,
                )

                val combinations = arrayOf(
                    "0000",
                    "0001",
                    "0010",
                    "0100",
                    "1000",
                    "0011",
                    "0101",
                    "1001",
                    "0110",
                    "1010",
                    "1100",
                    "0111",
                    "1011",
                    "1101",
                    "1110",
                    "1111",
                )

                combinations.forEach { combination ->
                    iter.combination.toString(iter.varsCount).shouldBe(combination)
                    iter.next()
                }

                iter.combination.toString(iter.varsCount).shouldBe(combinations[0])
            }
        }
    }
})
