package keccak

import java.math.BigInteger

class EllipticCurveSecp256k1 {
    fun sqrt(n: BigInteger): BigInteger {
        return n.modPow((prime + BigInteger.ONE)/BigIntegerFour, prime)
    }

    fun isOnCurve(point: Point): Boolean {
        return (point.y.pow(2) - point.x.pow(3) - BigIntegerSeven).mod(prime) == BigInteger.ZERO
    }

    fun calcY(x: BigInteger): BigInteger {
        return sqrt((x.pow(3) + BigIntegerSeven).mod(prime))
    }

    fun publicKey(privateKey: BigInteger): Point {
        return basePoint * privateKey
    }

    fun sign(privateKey: BigInteger, randomKey: BigInteger, hash: BigInteger): Signature {
        val p = basePoint * randomKey
        val r = p.x.mod(order)
        val s = r.multiply(privateKey).plus(hash).multiply(randomKey.modInverse(order)).mod(order)
        return Signature(r, s)
    }

    fun verify(publicKey: Point, hash: BigInteger, signature: Signature): Boolean {
        val w = signature.s.modInverse(order)
        val a = basePoint * hash.multiply(w).mod(order)
        val b = publicKey * signature.r.multiply(w).mod(order)
        val c = a + b
        return signature.r == c.x.mod(order)
    }

    fun recoverPublicKey(signature: Signature, hash: BigInteger): Point {
        // TODO: Fix
        val x1 = signature.r + order
        val k1 = Point(x1, calcY(x1))

        val ri = signature.r.modInverse(order)
        val u0 = hash.multiply(ri).negate().mod(order)
        val u1 = signature.s.multiply(ri).mod(order)
        return basePoint * u0 + k1 * u1
    }

    fun recoverPrivateKey(signature0: Signature, signature1: Signature, hash0: BigInteger, hash1: BigInteger): BigInteger {
        val k = (hash0 - hash1).multiply((signature0.s - signature1.s).modInverse(order)).mod(order)
        return signature0.r.modInverse(order).multiply(signature0.s.multiply(k).minus(hash0)).mod(order)
    }

    data class Signature(val r: BigInteger, val s: BigInteger)

    class Point(val x: BigInteger, val y: BigInteger) {
        operator fun plus(other: Point): Point {
            val m = if (this == other) {
                BigIntegerThree * this.x.pow(2) * (BigInteger.TWO * this.y).modInverse(prime)
            } else {
                (this.y - other.y) * (this.x - other.x).modInverse(prime)
            }

            val x3 = m.pow(2) - this.x - other.x
            val y3 = m * (this.x - x3) - this.y

            return Point(
                x = x3.mod(prime),
                y = y3.mod(prime),
            )
        }

        operator fun times(value: BigInteger): Point {
            var n = value
            var addend = this
            var res: Point? = null

            while (n > BigInteger.ZERO) {
                if (n and BigInteger.ONE > BigInteger.ZERO) {
                    res = if (res == null) addend else res + addend
                }
                addend += addend
                n = n shr 1
            }

            return res!!
        }

        override fun toString(): String {
            return "x = ${x.toString(16)}\ny = ${y.toString(16)}"
        }

        override fun equals(other: Any?): Boolean {
            return if (this === other) true else if (other is Point) x == other.x && y == other.y else false
        }

        override fun hashCode(): Int {
            return 31*x.hashCode() + y.hashCode()
        }

        operator fun component1(): BigInteger = x
        operator fun component2(): BigInteger = y
    }

    companion object {
        private val BigIntegerThree = BigInteger("3")
        private val BigIntegerFour = BigInteger("4")
        private val BigIntegerSeven = BigInteger("7")

        val prime = BigInteger("fffffffffffffffffffffffffffffffffffffffffffffffffffffffefffffc2f", 16)

        val basePoint = Point(
            x = BigInteger("79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798", 16),
            y = BigInteger("483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8", 16),
        )

        val order = BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141", 16)
    }
}
