package keccak.util

private val NonDigitsRegex = "\\D+".toRegex()

fun String.mapToOnlyDigits(): String {
    return NonDigitsRegex.replace(this, "")
}

fun String.toBoolean(): Boolean {
    return if (this == "0") {
        false
    } else if (this == "1") {
        true
    } else {
        throw RuntimeException("Character $this is not supported. Only 0 or 1 is available")
    }
}
