package squidpony.squidmath;

import squidpony.annotation.GwtIncompatible;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

/**
 * RandomnessSource using Mersenne Twister algorithm (not recommended).
 * <br>
 * Uses the Mersenne Twister algorithm to provide results with a longer period.
 * Mersenne Twister has known statistical vulnerabilities, however, and this
 * implementation is incredibly slow, which is why it is deprecated. You should
 * use {@link LongPeriodRNG} for most of the cases that MersenneTwister would be
 * good at in theory, or {@link IsaacRNG} for cases that need an extremely large
 * period and cryptographic-like properties.
 * <br>
 *
 * @author Daniel Dyer (Java Port)
 * @author Makoto Matsumoto and Takuji Nishimura (original C version)
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Lewis Potter
 * @deprecated
 */
@GwtIncompatible /* Because of SecureRandom */
@Deprecated /* This code is really, really slow due to threading behavior, and should be avoided. */
public class MersenneTwister implements RandomnessSource {

	// The actual seed size isn't that important, but it should be a multiple of 4.
    private static final int SEED_SIZE_BYTES = 16;
    // Magic numbers from original C version.
    private static final int N = 624;
    private static final int M = 397;
    private static final int[] MAG01 = {0, 0x9908b0df};
    private static final int UPPER_MASK = 0x80000000;
    private static final int LOWER_MASK = 0x7fffffff;
    private static final int BOOTSTRAP_SEED = 19650218;
    private static final int BOOTSTRAP_FACTOR = 1812433253;
    private static final int SEED_FACTOR1 = 1664525;
    private static final int SEED_FACTOR2 = 1566083941;
    private static final int GENERATE_MASK1 = 0x9d2c5680;
    private static final int GENERATE_MASK2 = 0xefc60000;
    private final byte[] seed;
    // Lock to prevent concurrent modification of the RNG's internal state.
    private final ReentrantLock lock = new ReentrantLock();
    private final int[] mt = new int[N]; // State vector.
    private int mtIndex = 0; // Index into state vector.    
    private static final int BITWISE_BYTE_TO_INT = 0x000000FF;

	private static final long serialVersionUID = 217351968847857679L;

    /**
     * Creates a new RNG and seeds it using the default seeding strategy.
     */
    public MersenneTwister() {
        this(new SecureRandom().generateSeed(SEED_SIZE_BYTES));
    }

    /**
     * Creates an RNG and seeds it with the specified seed data.
     *
     * @param seed The seed data used to initialize the RNG.
     */
    public MersenneTwister(byte[] seed) {
        if (seed == null || seed.length != SEED_SIZE_BYTES) {
            throw new IllegalArgumentException("Mersenne Twister RNG requires a 128-bit (16-byte) seed.");
        }
        this.seed = Arrays.copyOf(seed, seed.length);

        int[] seedInts = convertBytesToInts(this.seed);

        // This section is translated from the init_genrand code in the C version.
        mt[0] = BOOTSTRAP_SEED;
        for (mtIndex = 1; mtIndex < N; mtIndex++) {
            mt[mtIndex] = BOOTSTRAP_FACTOR
                    * (mt[mtIndex - 1] ^ (mt[mtIndex - 1] >>> 30))
                    + mtIndex;
        }

        // This section is translated from the init_by_array code in the C version.
        int i = 1;
        int j = 0;
        for (int k = Math.max(N, seedInts.length); k > 0; k--) {
            mt[i] = (mt[i] ^ ((mt[i - 1] ^ (mt[i - 1] >>> 30)) * SEED_FACTOR1)) + seedInts[j] + j;
            i++;
            j++;
            if (i >= N) {
                mt[0] = mt[N - 1];
                i = 1;
            }
            if (j >= seedInts.length) {
                j = 0;
            }
        }
        for (int k = N - 1; k > 0; k--) {
            mt[i] = (mt[i] ^ ((mt[i - 1] ^ (mt[i - 1] >>> 30)) * SEED_FACTOR2)) - i;
            i++;
            if (i >= N) {
                mt[0] = mt[N - 1];
                i = 1;
            }
        }
        mt[0] = UPPER_MASK; // Most significant bit is 1 - guarantees non-zero initial array.
    }

    /**
     * Take four bytes from the specified position in the specified block and
     * convert them into a 32-bit int, using the big-endian convention.
     *
     * @param bytes The data to read from.
     * @param offset The position to start reading the 4-byte int from.
     * @return The 32-bit integer represented by the four bytes.
     */
    public static int convertBytesToInt(byte[] bytes, int offset) {
        return (BITWISE_BYTE_TO_INT & bytes[offset + 3])
                | ((BITWISE_BYTE_TO_INT & bytes[offset + 2]) << 8)
                | ((BITWISE_BYTE_TO_INT & bytes[offset + 1]) << 16)
                | ((BITWISE_BYTE_TO_INT & bytes[offset]) << 24);
    }

    /**
     * Convert an array of bytes into an array of ints. 4 bytes from the input
     * data map to a single int in the output data.
     *
     * @param bytes The data to read from.
     * @return An array of 32-bit integers constructed from the data.
     * @since 1.1
     */
    public static int[] convertBytesToInts(byte[] bytes) {
        if (bytes.length % 4 != 0) {
            throw new IllegalArgumentException("Number of input bytes must be a multiple of 4.");
        }
        int[] ints = new int[bytes.length / 4];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = convertBytesToInt(bytes, i * 4);
        }
        return ints;
    }

    public byte[] getSeed() {
        return Arrays.copyOf(seed, seed.length);
    }

    @Override
    public final int next(int bits) {
        int y;
        try {
            lock.lock();
            if (mtIndex >= N) // Generate N ints at a time.
            {
                int kk;
                for (kk = 0; kk < N - M; kk++) {
                    y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                    mt[kk] = mt[kk + M] ^ (y >>> 1) ^ MAG01[y & 0x1];
                }
                for (; kk < N - 1; kk++) {
                    y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                    mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ MAG01[y & 0x1];
                }
                y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
                mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ MAG01[y & 0x1];

                mtIndex = 0;
            }

            y = mt[mtIndex++];
        } finally {
            lock.unlock();
        }
        // Tempering
        y ^= y >>> 11;
        y ^= (y << 7) & GENERATE_MASK1;
        y ^= (y << 15) & GENERATE_MASK2;
        y ^= y >>> 18;

        return y >>> (32 - bits);
    }
    @Override
    public final long nextLong() {
        return ((next(32) & 0xffffffffL) << 32) | (next(32) & 0xffffffffL);
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public RandomnessSource copy() {
        MersenneTwister next = new MersenneTwister(seed);
        System.arraycopy(mt, 0, next.mt, 0, mt.length);
        next.mtIndex = mtIndex;
        return next;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MersenneTwister that = (MersenneTwister) o;

        if (mtIndex != that.mtIndex) return false;
        if (!Arrays.equals(seed, that.seed)) return false;
        return Arrays.equals(mt, that.mt);
    }

    @Override
    public int hashCode() {
        int result = CrossHash.Lightning.hash(seed);
        result = 31 * result + CrossHash.Lightning.hash(mt);
        result = 31 * result + mtIndex;
        return result;
    }

    @Override
    public String toString()
    {
        return "MersenneTwister with hidden state (id is " + System.identityHashCode(this)  + ')';
    }
}
