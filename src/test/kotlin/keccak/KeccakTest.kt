package keccak

import io.kotest.core.spec.style.FunSpec
import org.web3j.utils.Numeric
import kotlin.test.assertEquals

class KeccakTest : FunSpec({
    test("test keccak224") {
        val msg = "hello".toByteArray()

        val keccak224Hash = Numeric.toHexString(Keccak.KECCAK_224.hash(msg))
        assertEquals("0x45524ec454bcc7d4b8f74350c4a4e62809fcb49bc29df62e61b69fa4", keccak224Hash)
    }

    test("test keccak256") {
        val msg = "hello".toByteArray()

        val keccak256Hash = Numeric.toHexString(Keccak.KECCAK_256.hash(msg))
        assertEquals("0x1c8aff950685c2ed4bc3174f3472287b56d9517b9c948127319a09a7a36deac8", keccak256Hash)
    }

    test("test keccak384") {
        val msg = "hello".toByteArray()

        val keccak384Hash = Numeric.toHexString(Keccak.KECCAK_384.hash(msg))
        assertEquals("0xdcef6fb7908fd52ba26aaba75121526abbf1217f1c0a31024652d134d3e32fb4cd8e9c703b8f43e7277b59a5cd402175", keccak384Hash)
    }

    test("test keccak512") {
        val msg = "hello".toByteArray()

        val keccak512Hash = Numeric.toHexString(Keccak.KECCAK_512.hash(msg))
        assertEquals("0x52fa80662e64c128f8389c9ea6c73d4c02368004bf4463491900d11aaadca39d47de1b01361f207c512cfa79f0f92c3395c67ff7928e3f5ce3e3c852b392f976", keccak512Hash)
    }
})
