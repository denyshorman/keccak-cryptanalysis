package keccak

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.test.assertEquals

class NodesTest : FunSpec({
    context("operators") {
        context("flatten") {
            test("1") {
                val input = ("a" xor "b") and ("x" xor "y")
                val flattenedInput = input.expand()
                val expected = ("a" and "x") xor ("a" and "y") xor ("b" and "x") xor ("b" and "y")
                println(input)
                println(flattenedInput)
                assertEquals(expected, flattenedInput)
            }

            test("2") {
                val input = "z" and ("a" xor "b") and ("x" xor "y")
                val flattenedInput = input.expand()
                val expected =
                    ("z" and "a" and "x") xor ("z" and "a" and "y") xor ("z" and "b" and "x") xor ("z" and "b" and "y")
                println(input)
                println(flattenedInput)
                assertEquals(expected, flattenedInput)
            }

            test("3") {
                val input = "z" and ("a" xor ("x" and ("a" xor "y"))) and ("x" xor "y")
                val flattenedInput = input.expand()
                val expected = ("z" and "a" and "y") xor ("a" and "x" and "y" and "z")
                println(input)
                println(flattenedInput)
                assertEquals(expected, flattenedInput)
            }

            test("4") {
                val input = "a" xor ("b" and "c") xor ("z" and ("x" xor "y") and ("b" xor "c" xor ("a" and "z")))
                val flattenedInput = input.expand()
                val expected = "a" xor ("a" and "x" and "z") xor ("a" and "y" and "z") xor ("b" and "x" and "z") xor ("b" and "c") xor ("b" and "y" and "z") xor ("c" and "x" and "z") xor ("c" and "y" and "z")
                println(input)
                println(flattenedInput)
                assertEquals(expected, flattenedInput)
            }

            test("5") {
                val input = Bit(1) xor ((Bit(1) xor "d") and (Bit(1) xor "d" xor ("d" and (Bit(1) xor "d")))) xor "d" xor ("d" and (Bit(1) xor "d"))
                val flattenedInput = input.expand()
                val expected = Bit()
                println(input)
                println(flattenedInput)
                assertEquals(expected, flattenedInput)
            }
        }

        context("groupBy") {
            test("1") {
                val node = Bit(true)
                val grouped = node.groupBy("x")
                grouped.shouldBe(node)
            }

            test("2") {
                val node = Bit(true) xor "a"
                val grouped = node.groupBy("x")
                grouped.shouldBe(node)
            }

            test("3") {
                val node = Bit(true) xor "a" xor "x1"
                val grouped = node.groupBy("x")
                grouped.shouldBe(node)
            }

            test("4") {
                val node = Bit(true) xor "a" xor "x1" xor "x2"
                val grouped = node.groupBy("x")
                grouped.shouldBe(node)
            }

            test("5") {
                val node = Bit(true) xor "a" xor ("a" and "x1") xor ("b" and "x1")
                val grouped = node.groupBy("x")
                grouped.shouldBe(Bit(true) xor "a" xor ("x1" and ("a" xor "b")))
            }

            test("6") {
                val node = ("a" and "x1") xor ("b" and "x1") xor ("a" and "c" and "x1" and "x2") xor ("d" and "x1" and "x2")
                val grouped = node.groupBy("x")
                grouped.shouldBe(("x1" and ("a" xor "b")) xor ("x1" and "x2" and (("a" and "c") xor "d")))
            }
        }

        context("groups") {
            test("1") {
                val node = ("x1" and ("a" xor "b")) xor ("x1" and "x2" and (("a" and "c") xor "d"))
                val groups = node.groups("x")

                groups.shouldBe(mapOf(
                    Pair("x1".toVar(), ("a" xor "b")),
                    Pair("x1" and "x2", (("a" and "c") xor "d")),
                ))
            }
        }
    }
})
