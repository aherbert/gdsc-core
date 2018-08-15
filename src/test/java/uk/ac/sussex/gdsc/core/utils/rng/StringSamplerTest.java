package uk.ac.sussex.gdsc.core.utils.rng;

import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.rng.RandomProviderState;
import org.apache.commons.rng.RestorableUniformRandomProvider;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("javadoc")
public class StringSamplerTest {

    @Test
    public void testConstructor() {
        final UniformRandomProvider rng = null;
        final int length = 1;
        for (int radix : new int[] { 2, 8, 16 }) {
            final StringSampler s = new StringSampler(rng, length, radix);
            Assertions.assertNotNull(s);
        }
    }

    @SuppressWarnings("unused")
    @Test
    public void testConstructorThrows() {
        final UniformRandomProvider rng = null;
        final int length = 1;
        final int radix = 16;
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new StringSampler(rng, 0, radix);
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new StringSampler(rng, length, 0);
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new StringSampler(rng, length, 4);
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new StringSampler(rng, length, 17);
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new StringSampler(rng, length, 32);
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new StringSampler(rng, length, 64);
        });
    }

    @Test
    public void testHexSamples() {
        testSamples(16);
    }

    @Test
    public void testOctalSamples() {
        testSamples(8);
    }

    @Test
    public void testBinarySamples() {
        testSamples(2);
    }

    private final int lower1 = '0';
    private final int upper1 = '9';
    private final int lower2 = 'a';
    private final int upper2 = 'f';
    private final int offset2 = lower2 - 10;

    private int map(char c) {
        if (c >= lower1 && c <= upper1)
            return c - lower1;
        if (c >= lower2 && c <= upper2)
            return c - offset2;
        Assertions.fail("Unsupported character: " + c);
        // For the java compiler
        return 0;
    }

    private void testSamples(int radix) {
        final RestorableUniformRandomProvider rng1 = RandomSource.create(RandomSource.MWC_256);
        final RestorableUniformRandomProvider rng2 = RandomSource.create(RandomSource.MWC_256);
        rng2.restoreState(rng1.saveState());
        // Test long enough strings that the algorithm edge cases are hit
        final int[] lengths = new int[] { 1, 2, 3, 4, 5, 10 };
        for (final int length : lengths) {
            final StringSampler s = new StringSampler(rng1, length, radix);
            for (int i = 0; i < 10; i++) {
                final String string = s.sample();
                Assertions.assertNotNull(string);
                // System.out.println(hex);
                Assertions.assertEquals(length, string.length());
                for (int j = 0; j < length; j++) {
                    final char c = string.charAt(j);
                    Assertions.assertTrue(map(c) <= radix);
                }

                // Check the static method does the same
                final String string2;
                switch (radix) {
                case 16:
                    string2 = StringSampler.nextHexString(rng2, length);
                    break;
                case 8:
                    string2 = StringSampler.nextOctalString(rng2, length);
                    break;
                case 2:
                    string2 = StringSampler.nextBinaryString(rng2, length);
                    break;
                default:
                    Assertions.fail("Unsupported radix: " + radix);
                    string2 = null; // For the java compiler
                }
                Assertions.assertEquals(string, string2);
            }
        }
    }

    @Test
    public void testHexSamplesVersesCommonsMath() {

        final RestorableUniformRandomProvider rng1 = RandomSource.create(RandomSource.MWC_256);
        final RestorableUniformRandomProvider rng2 = RandomSource.create(RandomSource.MWC_256);
        rng2.restoreState(rng1.saveState());

        final int[] lengths = new int[] { 1, 5, 10 };
        for (final int length : lengths) {
            final StringSampler s = new StringSampler(rng1, length, 16);
            for (int i = 0; i < 10; i++) {
                final String string1 = s.sample();
                final String string2 = nextHexString(rng2, length);
                Assertions.assertEquals(string2, string1);
            }
        }
    }

    /**
     * Adapted from RandomDataGenerator to match the implementation of the
     * HexStringSampler. Original code is left commented out.
     *
     * @param ran a random number generator
     * @param len the desired string length.
     * @return the random string.
     * @throws NotStrictlyPositiveException if {@code len <= 0}.
     */
    public String nextHexString(UniformRandomProvider ran, int len) {

        // Initialize output buffer
        StringBuilder outBuffer = new StringBuilder();

        // Get int(len/2)+1 random bytes
        // byte[] randomBytes = new byte[(len/2) + 1]; // ORIGINAL
        byte[] randomBytes = new byte[(len + 1) / 2];
        ran.nextBytes(randomBytes);

        // Convert each byte to 2 hex digits
        for (int i = 0; i < randomBytes.length; i++) {

            /*
             * Add 128 to byte value to make interval 0-255 before doing hex conversion.
             * This guarantees <= 2 hex digits from toHexString() toHexString would
             * otherwise add 2^32 to negative arguments.
             */
            // ORIGINAL
            // Integer c = Integer.valueOf(randomBytes[i]);
            // String hex = Integer.toHexString(c.intValue() + 128);

            String hex = Integer.toHexString(randomBytes[i] & 0xff);

            // Make sure we add 2 hex digits for each byte
            if (hex.length() == 1) {
                outBuffer.append('0');
            }
            outBuffer.append(hex);
        }
        return outBuffer.toString().substring(0, len);
    }

    /**
     * Adapted from RandomDataGenerator to match the implementation of the
     * HexStringSampler. Original code is left commented out.
     *
     * @param ran a random number generator
     * @param len the desired string length.
     * @return the random string.
     * @throws NotStrictlyPositiveException if {@code len <= 0}.
     */
    public String nextHexStringOriginal(UniformRandomProvider ran, int len) {

        // Initialize output buffer
        StringBuilder outBuffer = new StringBuilder();

        // Get int(len/2)+1 random bytes
        byte[] randomBytes = new byte[(len / 2) + 1];
        ran.nextBytes(randomBytes);

        // Convert each byte to 2 hex digits
        for (int i = 0; i < randomBytes.length; i++) {
            Integer c = Integer.valueOf(randomBytes[i]);

            /*
             * Add 128 to byte value to make interval 0-255 before doing hex conversion.
             * This guarantees <= 2 hex digits from toHexString() toHexString would
             * otherwise add 2^32 to negative arguments.
             */
            String hex = Integer.toHexString(c.intValue() + 128);

            // Make sure we add 2 hex digits for each byte
            if (hex.length() == 1) {
                hex = "0" + hex;
            }
            outBuffer.append(hex);
        }
        return outBuffer.toString().substring(0, len);
    }

    @Test
    public void testSamplerIsFasterThanCommonsMath() {
        final RestorableUniformRandomProvider rng1 = RandomSource.create(RandomSource.MWC_256);
        final int length = 100;
        long time1 = time(rng1, (rng) -> {
            final StringSampler s = new StringSampler(rng, length, 16);
            return () -> s.sample();
        });
        long time2 = time(rng1, (rng) -> {
            return () -> nextHexString(rng, length);
        });
        long time3 = time(rng1, (rng) -> {
            return () -> nextHexStringOriginal(rng, length);
        });
        long max = Math.max(time1, time2);
        max = Math.max(max, time3);
        
        System.out.printf("StringSampler         = %10d   %.3f\n", time1, (double)time1/max);
        System.out.printf("nextHexString         = %10d   %.3f\n", time2, (double)time2/max);
        System.out.printf("nextHexStringOriginal = %10d   %.3f\n", time3, (double)time3/max);

    }

    private static long time(RestorableUniformRandomProvider rng1, Function<UniformRandomProvider, Supplier<String>> create) {
        RandomProviderState state = rng1.saveState();
        long min = Long.MAX_VALUE;
        for (int i = 0; i < 5; i++) {
            rng1.restoreState(state);
            Supplier<String> f = create.apply(rng1);
            long start = System.nanoTime();
            for (int j = 0; j < 1000; j++) {
                // Do something with the string
                if (f.get() == null)
                    Assertions.fail("No string generated");
            }
            min = Math.min(min, System.nanoTime() - start);
        }
        return min;
    }

    @Test
    public void testHexSamplesAreUniform() {
        testSamplesAreUniform(16);
    }

    @Test
    public void testOctalSamplesAreUniform() {
        testSamplesAreUniform(8);
    }

    @Test
    public void testBinarySamplesAreUniform() {
        testSamplesAreUniform(2);
    }

    private void testSamplesAreUniform(int radix) {
        final int[] h = new int[radix];

        final UniformRandomProvider rng = RandomSource.create(RandomSource.MWC_256);
        int length = 1000;
        int repeats = 100;
        final StringSampler s = new StringSampler(rng, length, radix);
        for (int i = 0; i < repeats; i++) {
            final String hex = s.sample();
            for (int j = 0; j < length; j++) {
                h[map(hex.charAt(j))]++;
            }
        }

        // TODO - Statistical test: Kolmogorov Smirnov
        // https://math.stackexchange.com/questions/2435/is-there-a-simple-test-for-uniform-distributions
        double mean = (double) length * repeats / radix;
        for (int i = 0; i < h.length; i++) {
            System.out.printf("%2d = %d  (%.2f)\n", i, h[i], h[i] / mean);
        }
    }

    // TODO - Test the static method returns the same strings
}
