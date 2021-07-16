package keccak

import io.kotest.core.spec.style.FunSpec
import kotlin.test.assertEquals

class NodesTest : FunSpec({
    context("operators") {
        context("flatten") {
            test("1") {
                val input = ("a" xor "b") and ("x" xor "y")
                val flattenedInput = input.flatten()
                val expected = ("a" and "x") xor ("a" and "y") xor ("b" and "x") xor ("b" and "y")
                println(input)
                println(flattenedInput)
                assertEquals(expected, flattenedInput)
            }

            test("2") {
                val input = "z" and ("a" xor "b") and ("x" xor "y")
                val flattenedInput = input.flatten()
                val expected =
                    ("z" and "a" and "x") xor ("z" and "a" and "y") xor ("z" and "b" and "x") xor ("z" and "b" and "y")
                println(input)
                println(flattenedInput)
                assertEquals(expected, flattenedInput)
            }

            test("3") {
                val input = "z" and ("a" xor ("x" and ("a" xor "y"))) and ("x" xor "y")
                val flattenedInput = input.flatten()
                val expected = ("z" and "a" and "y") xor ("a" and "x" and "y" and "z")
                println(input)
                println(flattenedInput)
                assertEquals(expected, flattenedInput)
            }

            test("4") {
                val input = "a" xor ("b" and "c") xor ("z" and ("x" xor "y") and ("b" xor "c" xor ("a" and "z")))
                val flattenedInput = input.flatten()
                val expected = "a" xor ("a" and "x" and "z") xor ("a" and "y" and "z") xor ("b" and "x" and "z") xor ("b" and "c") xor ("b" and "y" and "z") xor ("c" and "x" and "z") xor ("c" and "y" and "z")
                println(input)
                println(flattenedInput)
                assertEquals(expected, flattenedInput)
            }
        }
    }
})
