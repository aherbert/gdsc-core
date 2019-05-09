package uk.ac.sussex.gdsc.core.utils.rng;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("javadoc")
public class NumberUtilsTest {

  @Test
  public void testMakeIntInRangeWithRangeZeroThrows() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> NumberUtils.makeIntInRange(0, 0));
  }

  @Test
  public void testMakeIntInRangeWithNegativeRangeThrows() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> NumberUtils.makeIntInRange(0, -1));
  }

  @Test
  public void testMakeIntInRange() {
    final int allBits = 0xffffffff;
    final int noBits = 0;
    for (int i = 0; i < 31; i++) {
      final int n = 1 << i;
      Assertions.assertEquals(0, NumberUtils.makeIntInRange(noBits, n));
      assertMakeIntInRange(allBits, n);
    }
    assertMakeIntInRange(allBits, Integer.MAX_VALUE);
    for (int i = 1; i <= 31; i++) {
      assertMakeIntInRange(allBits << i, Integer.MAX_VALUE);
    }

    // Check some random values
    final ThreadLocalRandom rng = ThreadLocalRandom.current();
    for (int i = 0; i < 31; i++) {
      final int n = 1 << i;
      assertMakeIntInRange(rng.nextInt(), n);
    }
    for (int i = 0; i < 100; i++) {
      assertMakeIntInRange(rng.nextInt(), rng.nextInt(Integer.MAX_VALUE));
    }
  }

  @Test
  public void testMakeIntInRangeIsUniform() {
    final int bins = 37; // prime
    final int[] h = new int[bins];

    final int binWidth = Integer.MAX_VALUE / bins;
    final int n = binWidth * bins;

    // Weyl sequence using George Marsaglia’s increment from:
    // Marsaglia, G (July 2003). "Xorshift RNGs". Journal of Statistical Software. 8 (14).
    // https://en.wikipedia.org/wiki/Weyl_sequence
    final int increment = 362437;
    final int start = Integer.MIN_VALUE - increment;
    int bits = start;
    // Loop until the first wrap. The entire sequence will be uniform.
    // Note this is not the full period of the sequence.
    // Expect (1L << 32) / increment numbers = 11850
    while ((bits += increment) < start) {
      h[NumberUtils.makeIntInRange(bits, n) / binWidth]++;
    }

    // The bins should all be the same within a value of 1 (i.e. uniform)
    int min = h[0];
    int max = h[0];
    for (final int value : h) {
      min = Math.min(min, value);
      max = Math.max(max, value);
    }
    Assertions.assertTrue(max - min <= 1, "Not uniform, max = " + max + ", min=" + min);
  }

  @Test
  public void testMakeLongInRangeWithRangeZeroThrows() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> NumberUtils.makeLongInRange(0L, 0L));
  }

  @Test
  public void testMakeLongInRangeWithNegativeRangeThrows() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> NumberUtils.makeLongInRange(0L, -1L));
  }

  @Test
  public void testMakeLongInRange() {
    final long allBits = 0xffffffffffffffffL;
    final long noBits = 0;
    for (int i = 0; i < 63; i++) {
      final long n = 1L << i;
      Assertions.assertEquals(0, NumberUtils.makeLongInRange(noBits, n));
      assertMakeLongInRange(allBits, n);
    }
    assertMakeLongInRange(allBits, Long.MAX_VALUE);
    for (int i = 1; i <= 63; i++) {
      assertMakeLongInRange(allBits << 1, Long.MAX_VALUE);
    }

    // Check some random values
    final ThreadLocalRandom rng = ThreadLocalRandom.current();
    for (int i = 0; i < 63; i++) {
      final long n = 1L << i;
      assertMakeLongInRange(rng.nextLong(), n);
    }
    for (int i = 0; i < 100; i++) {
      assertMakeLongInRange(rng.nextLong(), Long.MAX_VALUE);
    }
  }

  @Test
  public void testMakeLongInRangeIsUniform() {
    final long bins = 37; // prime
    final int[] h = new int[(int) bins];

    final long binWidth = Long.MAX_VALUE / bins;
    final long n = binWidth * bins;

    Assertions.assertNotEquals(0, (int) n,
        "Require upper limit to have bits set in the lower 32-bits");

    // Weyl sequence using an increment to approximate the same number of samples
    // as the integer test for uniformity.
    final long increment =
        BigInteger.ONE.shiftLeft(64).divide(BigInteger.valueOf(11850)).longValue();
    final long start = Long.MIN_VALUE - increment;
    long bits = start;
    // Loop until the first wrap. The entire sequence will be uniform.
    // Note this is not the full period of the sequence.
    while ((bits += increment) < start) {
      h[(int) (NumberUtils.makeLongInRange(bits, n) / binWidth)]++;
    }

    // The bins should all be the same within a value of 1 (i.e. uniform)
    long min = h[0];
    long max = h[0];
    for (final long value : h) {
      min = Math.min(min, value);
      max = Math.max(max, value);
    }
    Assertions.assertTrue(max - min <= 1, "Not uniform, max = " + max + ", min=" + min);
  }

  /**
   * Assert that the {@link NumberUtils#makeIntInRange(int, int)} method matches the arithmetic of
   * {@link BigInteger}.
   *
   * <p>This test is included to match the corresponding {@link #assertMakeLongInRange(long, long)}.
   * It should demonstrate that the use of BigInteger is unnecessary and the unsigned integer
   * arithmetic using {@code long} in the {@link NumberUtils} is correct.</p>
   *
   * @param value Value to use as a source of randomness.
   * @param n Bound on the random number to be returned. Must be positive.
   */
  private static void assertMakeIntInRange(int value, int n) {
    final long unsignedValue = value & 0xffffffffL;
    // Use long to ensure the int can fit unsigned
    final long expected = BigInteger.valueOf(n).multiply(BigInteger.valueOf(unsignedValue))
        .shiftRight(32).longValue();
    final long actual = NumberUtils.makeIntInRange(value, n);
    if (expected != actual) {
      Assertions.assertEquals(expected, actual, () -> "v=" + unsignedValue + ",n=" + n);
    }
  }

  /**
   * Assert that the {@link NumberUtils#makeLongInRange(long, long)} method matches the arithmetic
   * of {@link BigInteger}.
   *
   * @param value Value to use as a source of randomness.
   * @param n Bound on the random number to be returned. Must be positive.
   */
  private static void assertMakeLongInRange(long value, long n) {
    // Compute using BigInteger.
    // Construct big-endian byte representation from the long.
    final byte[] bytes = new byte[8];
    for (int i = 0; i < 8; i++) {
      bytes[7 - i] = (byte) ((value >>> (i * 8)) & 0xff);
    }
    final BigInteger unsignedValue = new BigInteger(1, bytes);
    final long expected =
        BigInteger.valueOf(n).multiply(unsignedValue).shiftRight(64).longValueExact();
    final long actual = NumberUtils.makeLongInRange(value, n);
    if (expected != actual) {
      Assertions.assertEquals(expected, actual, () -> "v=" + unsignedValue + ",n=" + n);
    }
  }

  // The following are not tests. They investigate the rejection criteria of the
  // multiplication algorithm.

  /**
   * Test the bias. This enumerates all possible {@code n} for a k-bit base and outputs the result
   * of the {@code n * [0,2^k) / 2^k} and {@code n * [0,2^k) % 2^k} for all numbers in the range
   * [0,2^k).
   */
  //@Test
  public void testBias() {
    final int range = 16; // 2^4
    for (int n = 1; n <= range; n++) {
      // Output number of samples expected for each value in range [0,n)
      final int numberOfSamples = range / n;
      // Output number of extra samples. These must be rejected.
      final int extra = range % n;

      // Note:
      // frequency * numberOfSamples + frequency1 * (numberOfSamples + 1) = range

      // Frequency each number is seen (number of samples) times
      final int frequency = n - extra;
      // Frequency each number is seen (number of samples + 1) times
      final int frequency1 = extra;


      // Output rejection rate.
      final double rejectionProbability = (double) extra / range;
      // Output bias (mean and variance of number of samples) if not rejected.
      final double mean = (double) range / n;
      double var = 0;
      if (extra != 0) {
        double dx = mean - numberOfSamples;
        double sum = frequency * dx * dx;
        dx = 1 - dx;
        sum += frequency1 * dx * dx;
        var = sum / n;
      }

      System.out.printf("[n=%d/%d] %d*%d/%d, %d*%d/%d (%.3f +/- %.3f) %f%n", n, range, frequency,
          numberOfSamples, range, frequency1, numberOfSamples + 1, range, mean, var,
          rejectionProbability);

      // This is the fence method to use:
      final int modFence = (range / n) * n;
      final int mulFence = extra;

      System.out.printf(
          "[n=%d/%d] samples %d (%.3f), extra %d / %d, modulus bits fence >= %d, "
              + "multiply remainder fence < %d%n",
          n, range, numberOfSamples, (double) range / n,
          // extra method to use: range is a power of 2.
          // can be supported using long but for long requires big integer.
          extra, n, modFence, mulFence);

      // Histogram of samples
      final int[] h = new int[n];
      for (int i = 0; i < range; i++) {
        final int sample = (n * i) / range;
        final int remainder = (n * i) & (range - 1);
        h[sample]++;
        final int mod = i % n;
        System.out.printf("%2d %2d %% %2d%s  or %2d%s%n", i, sample, remainder,
            (remainder < mulFence) ? '*' : ' ', mod, (i >= modFence) ? '*' : ' ');
      }
      int min = h[0];
      int max = min;
      for (int i = 0; i < n; i++) {
        min = Math.min(min, h[i]);
        max = Math.max(max, h[i]);
        System.out.printf("%2d %2d%n", i, h[i]);
      }

      // Histogram of the number of observations of a sample.
      // This should be either m or m+1 where m = range / n.
      final int[] h2 = new int[2];
      for (int i = 0; i < n; i++) {
        h2[h[i] - min]++;
      }
      System.out.printf("m : %2d=%2d %2d=%2d%n", min, h2[0], max, h2[1]);
    }
  }

  //@Test
  public void outputBiasTable() {
    outputBiasTable(31, 31);
  }

  /**
   * Output the bias table. This performs a binary search within brackets bounded by powers of 2 for
   * the value {@code n} that maximises the bias in the sample. This is the value {@code n} that
   * maximises the variance of the number of samples of each discrete output where the average
   * number of samples is {@code 2^k / n} but each output may have either floor(2^k / n) or ceil(2^k
   * / n) samples (when the entire range of bits is fed to the sampling algorithm).
   *
   * <p>This does a binary search with long modulus arithmetic so is slow when using high powers.
   *
   * @param power the power
   * @param maxPower the max power to tabulate
   */
  private static void outputBiasTable(int power, int maxPower) {
    final long range = 1L << power;
    System.out.printf("||Upper||n||mean (u)||sd||Frequency(floor(u))||p(floor(u)))||"
        + "Frequency(ceil(u))||p(ceil(u))||%n");

    // skip powers 1,2
    for (int p = 3; p <= maxPower; p++) {

      // long n = (1L << p) - 1;

      // Best case scenario
      // 2^power / 2^p = x samples = 2^(power-p)
      final long x = 1L << (power - p);

      // Worst case scenario output is half the numbers are over-sampled:
      // 2^power / n = x.5
      // 2^(power+1) / n = 2x + 1
      // n = 2^(power+1) / (2x + 1)
      // long upperN = (long) Math.floor(range / (0.5 + x));
      long upperN = 2 * range / (2 * x + 1);

      // Output number of extra samples. These must be rejected.
      long extra = range % upperN;

      // Search down until extra is close to half of n
      final long lowerN = upperN >>> 1;
      //@CHECKSTYLE.OFF: LocalVariableName
      long n = upperN;
      //@CHECKSTYLE.ON: LocalVariableName
      long gap = Math.abs(upperN - 2 * extra);
      while (upperN > lowerN && gap > 1) {
        final long ex = range % (--upperN);
        final long newGap = Math.abs(upperN - 2 * ex);
        // if (newRatio < ratio) {
        if (newGap < gap) {
          n = upperN;
          extra = ex;
          gap = newGap;
        }
      }

      // Output number of samples expected for each value in range [0,n)
      final long numberOfSamples = range / n;

      // frequency * numberOfSamples + frequency1 * (numberOfSamples + 1) = range

      // Frequency each number is seen (number of samples) times
      final long frequency = n - extra;
      // Frequency each number is seen (number of samples + 1) times
      final long frequency1 = extra;


      // Output bias (mean and variance of number of samples) if not rejected.
      final double mean = (double) range / n;
      double var = 0;
      if (extra != 0) {
        double dx = mean - numberOfSamples;
        double sum = frequency * dx * dx;
        dx = 1 - dx;
        sum += frequency1 * dx * dx;
        var = sum / n;
      }

      System.out.printf("|2^%d|%d|%.3f|%.3f|%d|%d|%d|%d|%n", p, n, mean, var, frequency,
          numberOfSamples, frequency1, numberOfSamples + 1);
    }
  }

  /**
   * Output the rejection table for a 31 or 32 bit unsigned integer. This shows the mean and maximum
   * rejection rate within brackets bounded by powers of 2 for the value {@code n}. The rejection
   * rate is defined by the number of samples that must be thrown away to make the output unbiased.
   * It is {@code (2^k % n) / 2^k}, i.e. the extra bits remaining when {@code n} is divided into the
   * number {@code 2^k}.
   *
   * <p>This loops over all possible integers doing long modulus arithmetic so is slow.
   */
  // @Test
  public void outputRejectionTable() {
    System.out.printf("|| || ||2^31|| ||2^32|| ||%n");
    System.out.printf("||Lower||Upper||mean||max||mean||max||%n");
    final long range31 = 1L << 31;
    final long range32 = 1L << 32;

    // skip powers 1,2
    for (int p = 3; p <= 31; p++) {
      final long upper = (1L << p);
      final long lower = upper >>> 1;
      final long count = lower;
      long sum31 = 0;
      long sum32 = 0;
      long max31 = 0;
      long max32 = 0;
      // Do a loop to compute every modulus in the range.
      // Then report the mean and max rejection probability.
      for (long i = lower; i < upper; i++) {
        final long mod31 = range31 % i;
        sum31 += mod31;
        max31 = Math.max(max31, mod31);
        final long mod32 = range32 % i;
        sum32 += mod32;
        max32 = Math.max(max32, mod32);
      }
      System.out.printf("|2^%d|2^%d|%.5g|%.5g|%.5g|%.5g|%n", p - 1, p,
          (double) sum31 / count / range31, (double) max31 / range31,
          (double) sum32 / count / range32, (double) max32 / range32);
    }
  }
}
