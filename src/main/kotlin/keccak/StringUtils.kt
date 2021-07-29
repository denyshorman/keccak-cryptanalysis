package keccak

private val NonDigitsRegex = "\\D+".toRegex()

fun String.mapToOnlyDigits(): String {
    return NonDigitsRegex.replace(this, "")
}
