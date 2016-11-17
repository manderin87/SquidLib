package squidpony.squidmath;

import squidpony.StringKit;
import java.util.Random;
import java.io.Serializable;

/**
 * This makes java.util.Random available in SquidLib.
 * It is relevant mainly as example code
 * or if you want to compare what your results would have been
 * without using a better RNG.
 * @author Ben McLean
 */
public class JavaRNG implements RandomnessSource, StatefulRandomness, Serializable
{
    public Random random;

    /** Creates a new generator seeded using Math.random. */
    public JavaRNG() { this((long) Math.floor(Math.random() * Long.MAX_VALUE)); }

    public JavaRNG( final long seed ) { setSeed(seed); }

    public JavaRNG( final Random random ) { this.random = random; }

    @Override
    public int next( int bits ) { return random.next(bits); }

    @Override
    public long nextLong() { return random.nextLong(); }

    @Override
    public RandomnessSource copy() { return new JavaRNG(random); }

    public int nextInt() { return random.nextInt(); }

    public int nextInt( final int bound ) { return random.nextInt(bound); }
    /**
     * Inclusive lower, exclusive upper.
     * @param lower the lower bound, inclusive, can be positive or negative
     * @param upper the upper bound, exclusive, should be positive, must be greater than lower
     * @return a random int at least equal to lower and less than upper
     */
    public int nextInt( final int lower, final int upper ) {
        if ( upper - lower <= 0 ) throw new IllegalArgumentException("Upper bound must be greater than lower bound");
        return lower + nextInt(upper - lower);
    }

    /**
     * Exclusive on the upper bound. The lower bound is 0.
     * @param bound the upper bound; should be positive
     * @return a random long less than n
     */
    public long nextLong( final long bound ) { return random.nextLong(bound); }

    /**
     * Inclusive lower, exclusive upper.
     * @param lower the lower bound, inclusive, can be positive or negative
     * @param upper the upper bound, exclusive, should be positive, must be greater than lower
     * @return a random long at least equal to lower and less than upper
     */
    public long nextLong( final long lower, final long upper ) {
        if ( upper - lower <= 0 )  throw new IllegalArgumentException("Upper bound must be greater than lower bound");
        return lower + nextLong(upper - lower);
    }
    public double nextDouble() { return random.nextDouble(); }
    public double nextDouble(final double outer) {
        return nextDouble() * outer;
    }

    /**
     * Gets a uniform random float in the range [0.0,1.0)
     * @return a random float at least equal to 0.0 and less than 1.0
     */
    public float nextFloat() { return random.nextFloat(); }
    /**
     * Gets a random value, true or false.
     * @return a random true or false value.
     */
    public boolean nextBoolean() { return ( random.nextBoolean()); }

    /**
     * Given a byte array as a parameter, this will fill the array with random bytes (modifying it
     * in-place).
     */
    public void nextBytes( final byte[] bytes ) { random.nextBytes(bytes); }

    /**
     * Sets the seed of this generator (which is also the current state).
     * @param seed the seed to use for this LightRNG, as if it was constructed with this seed.
     */
    public void setSeed( final long seed ) {
        random = new Random(seed);
    }

    @Override
    public void setState( final long seed ) {
        random.setSeed(seed);
    }

    @Override
    public Random getState() { return random; }

    @Override
    public String toString() {
        return "Java.util.Random in JavaRNG';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JavaRNG javaRNG = (JavaRNG) o;

        return random == javaRNG.getState();
    }
}
