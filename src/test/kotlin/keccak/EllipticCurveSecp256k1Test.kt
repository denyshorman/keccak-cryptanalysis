package keccak

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import java.math.BigInteger
import java.util.*

class EllipticCurveSecp256k1Test : FunSpec({
    test("Verify public key") {
        val ec = EllipticCurveSecp256k1()
        val privateKey = BigInteger("09e910621c2e988e9f7f6ffcd7024f54ec1461fa6e86a4b545e9e1fe21c28866", 16)
        val publicKey = ec.publicKey(privateKey)
        publicKey.x.toString(16).shouldBe("8e66b3e549818ea2cb354fb70749f6c8de8fa484f7530fc447d5fe80a1c424e4")
        publicKey.y.toString(16).shouldBe("f5ae648d648c980ae7095d1efad87161d83886ca4b6c498ac22a93da5099014a")
    }

    test("Verify public key and signature") {
        val ec = EllipticCurveSecp256k1()
        val privateKey = BigInteger("09e910621c2e988e9f7f6ffcd7024f54ec1461fa6e86a4b545e9e1fe21c28866", 16)
        val randomKey = BigInteger("19e910621c2e988e1f7f6ffcd7024f54ec1461fa6e86a4b545e911fe21c28866", 16)
        val hash = BigInteger("19e910621c2e988e9f7f6ffcd7024f54ec1461f16e86a4b54519e1fe21c28866", 16)
        val publicKey = ec.publicKey(privateKey)
        val sign = ec.sign(privateKey, randomKey, hash)
        publicKey.x.toString(16).shouldBe("8e66b3e549818ea2cb354fb70749f6c8de8fa484f7530fc447d5fe80a1c424e4")
        publicKey.y.toString(16).shouldBe("f5ae648d648c980ae7095d1efad87161d83886ca4b6c498ac22a93da5099014a")
        ec.verify(publicKey, hash, sign).shouldBeTrue()
    }

    test("Verify public key and signature 2") {
        val ec = EllipticCurveSecp256k1()
        val privateKey = BigInteger("112490522435328242673339121206164787450285336172694124859112358751338866301424", 10)
        val randomKey = BigInteger("95906879368288640582072739574560659693561507073703292704016799087674568951283", 10)
        val hash = BigInteger("11024336812845202542736754815889718862783203771635063178616734621641926515049", 10)
        val publicKey = ec.publicKey(privateKey)
        val sign = ec.sign(privateKey, randomKey, hash)
        publicKey.x.toString(10).shouldBe("70937857407851471065477544196886294329421656823743490179390967822015685262721")
        publicKey.y.toString(10).shouldBe("50473909367412121717125716363151441401585539142589467753358830969923091369310")
        sign.r.toString(10).shouldBe("57055356010155740609067801558880770162476398608813527363397633595475095880915")
        sign.s.toString(10).shouldBe("65283695154631870005864730722610860626041011187385979443183224066635176478920")
        ec.verify(publicKey, hash, sign).shouldBeTrue()
    }

    test("Recover private key") {
        val ec = EllipticCurveSecp256k1()
        val privateKey = BigInteger("112490522435328242673339121206164787450285336172694124859112358751338866301424", 10)
        val randomKey = BigInteger("95906879368288640582072739574560659693561507073703292704016799087674568951283", 10)
        val hash0 = BigInteger("11024336812845202542736754815889718862783203771635063178616734621641926515049", 10)
        val hash1 = BigInteger("11024336812845202542736754815889718862783203771635063178616734621641926515149", 10)
        val publicKey = ec.publicKey(privateKey)
        val sign0 = ec.sign(privateKey, randomKey, hash0)
        val sign1 = ec.sign(privateKey, randomKey, hash1)
        val recoveredPrivateKey = ec.recoverPrivateKey(sign0, sign1, hash0, hash1)
        publicKey.x.toString(10).shouldBe("70937857407851471065477544196886294329421656823743490179390967822015685262721")
        publicKey.y.toString(10).shouldBe("50473909367412121717125716363151441401585539142589467753358830969923091369310")
        ec.verify(publicKey, hash0, sign0).shouldBeTrue()
        ec.verify(publicKey, hash1, sign1).shouldBeTrue()
        recoveredPrivateKey.shouldBe(privateKey)
    }

    test("Recover private key loop") {
        val jRandom = java.util.Random(1L)
        while (true) {
            val ec = EllipticCurveSecp256k1()
            val privateKey = BigInteger(256, jRandom).mod(EllipticCurveSecp256k1.order)
            val randomKey = BigInteger(256, jRandom).mod(EllipticCurveSecp256k1.order)
            val hash0 = BigInteger(256, jRandom)
            val hash1 = BigInteger(256, jRandom)
            val publicKey = ec.publicKey(privateKey)
            val sign0 = ec.sign(privateKey, randomKey, hash0)
            val sign1 = ec.sign(privateKey, randomKey, hash1)
            val recoveredPublicKey = ec.recoverPublicKey(sign0, hash0)
            val recoveredPrivateKey = ec.recoverPrivateKey(sign0, sign1, hash0, hash1)
            ec.isOnCurve(publicKey).shouldBeTrue()
            ec.verify(publicKey, hash0, sign0).shouldBeTrue()
            ec.verify(publicKey, hash1, sign1).shouldBeTrue()
            recoveredPublicKey.shouldBe(publicKey)
            recoveredPrivateKey.shouldBe(privateKey)
        }
    }

    test("mod inverse") {
        val jRandom = java.util.Random(1L)
        val p = BigInteger("1151")
        val m = BigInteger("7")
        while (true) {
            try {
                val v = BigInteger(10, jRandom).mod(p)
                val a = (m*v*v).modInverse(p).mod(p)
                val b = (m*(v*v).modInverse(p)).mod(p)
                a.shouldBe(b)
            } catch (e: Exception) {
                continue
            }
        }
    }

    test("remainders") {
        val jRandom = java.util.Random()

        var pp = BigInteger("22")
        var pk = BigInteger("6")
        var k = BigInteger("16")
        var h = BigInteger("4")

        fun BigInteger.isPrime(): Boolean {
            if (this % BigInteger.TWO == BigInteger.ZERO) return false

            var i = BigInteger("3")
            val sqr = this.sqrt() + BigInteger.ONE
            while (i < sqr) {
                if (this % i == BigInteger.ZERO) return false
                i += BigInteger.TWO
            }
            return true
        }

        while(true) {
            while (true) {
                val rr = BigInteger("61")
                pp = BigInteger(16, jRandom).mod(rr) + BigInteger("9")
                pk = BigInteger(16, jRandom).mod(rr)
                k = BigInteger(16, jRandom).mod(rr)
                h = BigInteger(16, jRandom).mod(rr)

                if (
                    pp > BigInteger.ZERO &&
                    pk > BigInteger.ZERO && pk < pp &&
                    k > BigInteger.ZERO && k < pp &&
                    pp.isPrime() &&
                    pp.mod(BigInteger.TWO) == BigInteger.ONE &&
                    pk.mod(BigInteger.TWO) == BigInteger.ONE &&
                    k.mod(BigInteger.TWO) == BigInteger.ONE &&
                    h.mod(BigInteger.TWO) == BigInteger.ONE
                ) {
                    break
                }
            }

            println("pp = $pp")
            println("pk = $pk")
            println("rnd = $k")
            println("hash = $h")
            println()

            fun sign(privateKey: BigInteger, randomKey: BigInteger, hash: BigInteger): EllipticCurveSecp256k1.Signature {
                val p = EllipticCurveSecp256k1.basePoint * randomKey
                val r = p.x.mod(pp)
                val s = r.multiply(privateKey).plus(hash).multiply(randomKey.modInverse(pp)).mod(pp)
                return EllipticCurveSecp256k1.Signature(r, s)
                // s = (r*pk + h)*k'
                // s*k - h = r*pk
                // (8*k - 33)^50 = 1 mod 51

            }

            val (r, s) = try {
                sign(pk, k, h)
            } catch (e: Exception) {
                continue
            }

            if (pp.gcd(s*k - h) != BigInteger.ONE) {
                continue
            }

            val ll = LinkedList<BigInteger>()

            println("r = $r")
            println("s = $s")
            println()

            var i = BigInteger.ZERO
            while (i < pp) {
                val left = ((k*s-h)*i).mod(pp)
                val right = (i*r*pk).mod(pp)
                println("$left = $right")
                ll.add(left)
                i = i.inc()
            }

            val xx = ll.distinct().size.toBigInteger()
            println()
            println("Actual: $xx")
            println("Expected: $pp")
            println("diff: ${pp/xx}")
            break
        }
    }

    test("remainders 2") {
        val p = 29.toBigInteger()
        val h = 49.toBigInteger()
        val s = 27.toBigInteger()

        var k = BigInteger("1")
        while (k < BigInteger("100")) {
            val v = (s*k - h).modPow(p - BigInteger.ONE, p)
            if (v == BigInteger.ONE) {
                println(k)
            }
            k = k.inc()
        }
    }
})
