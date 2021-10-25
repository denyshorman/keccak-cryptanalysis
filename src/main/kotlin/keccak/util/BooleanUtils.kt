package keccak.util

import keccak.Bit

fun Boolean.toNumChar(): Char = if (this) '1' else '0'

fun Boolean.toBit(): Bit = Bit(this)

operator fun Boolean.plus(other: Boolean) = this xor other

operator fun Boolean.times(other: Boolean) = this && other
