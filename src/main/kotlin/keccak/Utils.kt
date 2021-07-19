package keccak

private val NonDigitsRegex = "\\D+".toRegex()

fun String.mapToOnlyDigits(): String {
    return NonDigitsRegex.replace(this, "")
}

fun ByteArray.littleEndianToLong(): Long {
    val bytes = this
    var value = 0L

    var i = 0
    while (i < bytes.size) {
        value = value or bytes[i].toLong().shl(i * Byte.SIZE_BITS)
        i++
    }

    return value
}

fun Long.toLittleEndianBytes(): ByteArray {
    val value = this
    val bytes = ByteArray(Long.SIZE_BYTES) { 0 }

    var i = 0
    while (i < Long.SIZE_BYTES) {
        bytes[i] = (value.shr(i * Byte.SIZE_BITS) and UByte.MAX_VALUE.toLong()).toByte()
        i++
    }

    return bytes
}
