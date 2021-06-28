package keccak;

import org.bouncycastle.util.Pack;

public class Keccak256Digest {
    //#region Constants
    private static final long[] KECCAK_ROUND_CONSTANTS = new long[]{
            0x0000000000000001L,
            0x0000000000008082L,
            0x800000000000808aL,
            0x8000000080008000L,
            0x000000000000808bL,
            0x0000000080000001L,
            0x8000000080008081L,
            0x8000000000008009L,
            0x000000000000008aL,
            0x0000000000000088L,
            0x0000000080008009L,
            0x000000008000000aL,
            0x000000008000808bL,
            0x800000000000008bL,
            0x8000000000008089L,
            0x8000000000008003L,
            0x8000000000008002L,
            0x8000000000000080L,
            0x000000000000800aL,
            0x800000008000000aL,
            0x8000000080008081L,
            0x8000000000008080L,
            0x0000000080000001L,
            0x8000000080008008L
    };

    private static final int FIXED_OUTPUT_LENGTH = 256;
    private static final int RATE = 1088;
    private static final int BLOCK_SIZE = 136;
    //#endregion

    //#region Fields
    private final long[] state = new long[25];
    private final byte[] dataQueue = new byte[192];
    private int bitsInQueue = 0;
    //#endregion

    //#region Public Methods
    public byte[] digest(byte[] in) {
        var bytes = new byte[32];
        absorb(in);
        squeeze(bytes);
        return bytes;
    }
    //#endregion

    //#region Private Methods
    private void absorb(byte[] msg) {
        var bytesInQueue = 0;

        var blockOffset = 0;
        var penultimateBlockOffset = msg.length - BLOCK_SIZE;

        while (blockOffset < msg.length) {
            if (bytesInQueue == 0 && blockOffset <= penultimateBlockOffset) {
                do {
                    keccakAbsorb(msg, blockOffset);
                    blockOffset += BLOCK_SIZE;
                } while (blockOffset <= penultimateBlockOffset);
            } else {
                int partialBlock = Math.min(BLOCK_SIZE - bytesInQueue, msg.length - blockOffset);
                System.arraycopy(msg, blockOffset, dataQueue, bytesInQueue, partialBlock);

                bytesInQueue += partialBlock;
                blockOffset += partialBlock;

                if (bytesInQueue == BLOCK_SIZE) {
                    keccakAbsorb(dataQueue, 0);
                    bytesInQueue = 0;
                }
            }
        }

        bitsInQueue = bytesInQueue << 3;
    }

    private void squeeze(byte[] output) {
        dataQueue[bitsInQueue >> 3] |= (byte) (1L << (bitsInQueue & 7));

        if (++bitsInQueue == RATE) {
            keccakAbsorb(dataQueue, 0);
            bitsInQueue = 0;
        }

        int full = bitsInQueue >> 6;
        int partial = bitsInQueue & 63;
        var off = 0;

        for (var i = 0; i < full; ++i) {
            state[i] ^= Pack.littleEndianToLong(dataQueue, off);
            off += 8;
        }

        if (partial > 0) {
            long mask = (1L << partial) - 1L;
            state[full] ^= Pack.littleEndianToLong(dataQueue, off) & mask;
        }

        state[16] ^= 0x8000000000000000L;

        keccakPermutation();
        keccakExtract();

        bitsInQueue = RATE;

        long i = 0;
        while (i < FIXED_OUTPUT_LENGTH) {
            if (bitsInQueue == 0) {
                keccakPermutation();
                keccakExtract();
                bitsInQueue = RATE;
            }

            int partialBlock = (int) Math.min(bitsInQueue, FIXED_OUTPUT_LENGTH - i);
            System.arraycopy(dataQueue, (RATE - bitsInQueue) / 8, output, (int) (i / 8), partialBlock / 8);

            bitsInQueue -= partialBlock;
            i += partialBlock;
        }
    }

    private void keccakAbsorb(byte[] data, int off) {
        for (var i = 0; i < 17; i++) {
            state[i] ^= Pack.littleEndianToLong(data, off);
            off += 8;
        }

        keccakPermutation();
    }

    private void keccakExtract() {
        Pack.longToLittleEndian(state, 0, 17, dataQueue, 0);
    }

    private void keccakPermutation() {
        long[] a = state;

        long a00 = a[0];
        long a01 = a[1];
        long a02 = a[2];
        long a03 = a[3];
        long a04 = a[4];
        long a05 = a[5];
        long a06 = a[6];
        long a07 = a[7];
        long a08 = a[8];
        long a09 = a[9];
        long a10 = a[10];
        long a11 = a[11];
        long a12 = a[12];
        long a13 = a[13];
        long a14 = a[14];
        long a15 = a[15];
        long a16 = a[16];
        long a17 = a[17];
        long a18 = a[18];
        long a19 = a[19];
        long a20 = a[20];
        long a21 = a[21];
        long a22 = a[22];
        long a23 = a[23];
        long a24 = a[24];

        for (var i = 0; i < 24; i++) {
            // theta
            long c0 = a00 ^ a05 ^ a10 ^ a15 ^ a20;
            long c1 = a01 ^ a06 ^ a11 ^ a16 ^ a21;
            long c2 = a02 ^ a07 ^ a12 ^ a17 ^ a22;
            long c3 = a03 ^ a08 ^ a13 ^ a18 ^ a23;
            long c4 = a04 ^ a09 ^ a14 ^ a19 ^ a24;

            long d1 = (c1 << 1 | c1 >>> 63) ^ c4;
            long d2 = (c2 << 1 | c2 >>> 63) ^ c0;
            long d3 = (c3 << 1 | c3 >>> 63) ^ c1;
            long d4 = (c4 << 1 | c4 >>> 63) ^ c2;
            long d0 = (c0 << 1 | c0 >>> 63) ^ c3;

            a00 ^= d1;
            a05 ^= d1;
            a10 ^= d1;
            a15 ^= d1;
            a20 ^= d1;
            a01 ^= d2;
            a06 ^= d2;
            a11 ^= d2;
            a16 ^= d2;
            a21 ^= d2;
            a02 ^= d3;
            a07 ^= d3;
            a12 ^= d3;
            a17 ^= d3;
            a22 ^= d3;
            a03 ^= d4;
            a08 ^= d4;
            a13 ^= d4;
            a18 ^= d4;
            a23 ^= d4;
            a04 ^= d0;
            a09 ^= d0;
            a14 ^= d0;
            a19 ^= d0;
            a24 ^= d0;

            // rho/pi
            c1 = a01 << 1 | a01 >>> 63;
            a01 = a06 << 44 | a06 >>> 20;
            a06 = a09 << 20 | a09 >>> 44;
            a09 = a22 << 61 | a22 >>> 3;
            a22 = a14 << 39 | a14 >>> 25;
            a14 = a20 << 18 | a20 >>> 46;
            a20 = a02 << 62 | a02 >>> 2;
            a02 = a12 << 43 | a12 >>> 21;
            a12 = a13 << 25 | a13 >>> 39;
            a13 = a19 << 8 | a19 >>> 56;
            a19 = a23 << 56 | a23 >>> 8;
            a23 = a15 << 41 | a15 >>> 23;
            a15 = a04 << 27 | a04 >>> 37;
            a04 = a24 << 14 | a24 >>> 50;
            a24 = a21 << 2 | a21 >>> 62;
            a21 = a08 << 55 | a08 >>> 9;
            a08 = a16 << 45 | a16 >>> 19;
            a16 = a05 << 36 | a05 >>> 28;
            a05 = a03 << 28 | a03 >>> 36;
            a03 = a18 << 21 | a18 >>> 43;
            a18 = a17 << 15 | a17 >>> 49;
            a17 = a11 << 10 | a11 >>> 54;
            a11 = a07 << 6 | a07 >>> 58;
            a07 = a10 << 3 | a10 >>> 61;
            a10 = c1;

            // chi
            c0 = a00 ^ (~a01 & a02);
            c1 = a01 ^ (~a02 & a03);
            a02 ^= ~a03 & a04;
            a03 ^= ~a04 & a00;
            a04 ^= ~a00 & a01;
            a00 = c0;
            a01 = c1;

            c0 = a05 ^ (~a06 & a07);
            c1 = a06 ^ (~a07 & a08);
            a07 ^= ~a08 & a09;
            a08 ^= ~a09 & a05;
            a09 ^= ~a05 & a06;
            a05 = c0;
            a06 = c1;

            c0 = a10 ^ (~a11 & a12);
            c1 = a11 ^ (~a12 & a13);
            a12 ^= ~a13 & a14;
            a13 ^= ~a14 & a10;
            a14 ^= ~a10 & a11;
            a10 = c0;
            a11 = c1;

            c0 = a15 ^ (~a16 & a17);
            c1 = a16 ^ (~a17 & a18);
            a17 ^= ~a18 & a19;
            a18 ^= ~a19 & a15;
            a19 ^= ~a15 & a16;
            a15 = c0;
            a16 = c1;

            c0 = a20 ^ (~a21 & a22);
            c1 = a21 ^ (~a22 & a23);
            a22 ^= ~a23 & a24;
            a23 ^= ~a24 & a20;
            a24 ^= ~a20 & a21;
            a20 = c0;
            a21 = c1;

            // iota
            a00 ^= KECCAK_ROUND_CONSTANTS[i];
        }

        a[0] = a00;
        a[1] = a01;
        a[2] = a02;
        a[3] = a03;
        a[4] = a04;
        a[5] = a05;
        a[6] = a06;
        a[7] = a07;
        a[8] = a08;
        a[9] = a09;
        a[10] = a10;
        a[11] = a11;
        a[12] = a12;
        a[13] = a13;
        a[14] = a14;
        a[15] = a15;
        a[16] = a16;
        a[17] = a17;
        a[18] = a18;
        a[19] = a19;
        a[20] = a20;
        a[21] = a21;
        a[22] = a22;
        a[23] = a23;
        a[24] = a24;
    }
    //#endregion
}
