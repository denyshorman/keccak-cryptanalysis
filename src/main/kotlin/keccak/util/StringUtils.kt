package keccak.util

private val NonDigitsRegex = "\\D+".toRegex()

fun String.mapToOnlyDigits(): String {
    return NonDigitsRegex.replace(this, "")
}
