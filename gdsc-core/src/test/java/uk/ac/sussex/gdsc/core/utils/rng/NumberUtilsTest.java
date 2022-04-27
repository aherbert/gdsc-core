/*-
 * #%L
 * Genome Damage and Stability Centre ImageJ Core Package
 *
 * Contains code used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2022 Alex Herbert
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package uk.ac.sussex.gdsc.core.utils.rng;

import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

@SuppressWarnings("javadoc")
class NumberUtilsTest {

  @Test
  void testMakeIntInRangeWithRangeZeroThrows() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> NumberUtils.makeIntInRange(0, 0));
  }

  @Test
  void testMakeIntInRangeWithNegativeRangeThrows() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> NumberUtils.makeIntInRange(0, -1));
  }

  @Test
  void testMakeIntInRange() {
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
  void testMakeIntInRangeIsUniform() {
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
  void testMakeLongInRangeWithRangeZeroThrows() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> NumberUtils.makeLongInRange(0L, 0L));
  }

  @Test
  void testMakeLongInRangeWithNegativeRangeThrows() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> NumberUtils.makeLongInRange(0L, -1L));
  }

  @Test
  void testMakeLongInRange() {
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
  void testMakeLongInRangeIsUniform() {
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
  // @Test
  void testBias() {
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

  /**
   * This is a test of the fence computation compared with that published by Daniel Lemire.
   *
   * @see <a href="https://arxiv.org/abs/1805.10941">Fast Random Integer Generation in an
   *      Interval</a>
   */
  @Test
  void testFenceComputation() {
    final long base = 1L << 32;
    for (int power = 1; power <= 31; power++) {
      final long upper = 1L << power;
      final long lower = upper >> 1;
      for (int i = 0; i < 10; i++) {
        final int n = (int) (lower + i);
        if (n >= upper) {
          break;
        }
        final long fence = base % n;
        // Lemire's method is based on having unsigned 32-bit integers where:
        // 2^32 % n == (2^32 - n) % n == -n % n;
        // This method using Java support for unsigned arithmetic
        // converts each integer to a long and uses long modulus. So actually it is
        // slower than having a pre-computed base stored as a long.
        final int threshold = Integer.remainderUnsigned(-n, n);
        // System.out.printf("power=%d, n=%d, fence=%d, %d%n", power, n, fence, threshold);
        Assertions.assertEquals(fence, threshold);
      }
    }
  }

  // @Test
  void outputBiasTable() {
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
      // CHECKSTYLE.OFF: LocalVariableName
      long n = upperN;
      // CHECKSTYLE.ON: LocalVariableName
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
  void outputRejectionTable() {
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

  @Test
  void testMultiply() {
    final long[] values = {0L, 0xffL, 0xffffL, 0xffffffffL, 0xffff00000000L, 0xffffffff00000000L,
        0xffffffffffffffffL};

    for (final long v1 : values) {
      for (final long v2 : values) {
        assertMultiply(v1, v2);
      }
    }
  }

  @SeededTest
  void testMultiply(RandomSeed seed) {
    final long[] values = new long[100];
    final UniformRandomProvider rng = RngFactory.create(seed.get());
    for (int i = 0; i < values.length; i++) {
      values[i] = rng.nextLong();
    }

    for (final long v1 : values) {
      for (final long v2 : values) {
        assertMultiply(v1, v2);
      }
    }
  }

  private static void assertMultiply(long v1, long v2) {
    final String un1 = Long.toUnsignedString(v1);
    final String un2 = Long.toUnsignedString(v2);
    final BigInteger bi1 = new BigInteger(un1);
    final BigInteger bi2 = new BigInteger(un2);
    final BigInteger expected = bi1.multiply(bi2);
    Assertions.assertTrue(expected.bitLength() <= 128);
    final long[] result = new long[2];
    NumberUtils.multiply(v1, v2, result);
    final BigInteger r1 = new BigInteger(Long.toUnsignedString(result[0]));
    final BigInteger r2 = new BigInteger(Long.toUnsignedString(result[1]));
    final BigInteger actual = r1.add(r2.shiftLeft(64));
    Assertions.assertEquals(expected, actual, () -> String.format("%s * %s", un1, un2));
  }

  @Test
  void testComputeLcgAdvance() {
    final long m = 6364136223846793005L;
    final long c = 1442695040888963407L;

    // Nothing
    assertComputeLcgAdvance(6711382, m, c, 0);

    // Small powers of 2
    for (int i = 0; i < 8; i++) {
      assertComputeLcgAdvance(676788, m, c, 1L << i);
    }

    // Odd
    assertComputeLcgAdvance(8997, m, c, 3);
    assertComputeLcgAdvance(99785, m, c, 5);
    assertComputeLcgAdvance(1278, m, c, 7);
    assertComputeLcgAdvance(6711345482L, m, c, 13);
    assertComputeLcgAdvance(9657, m, c, 15);

    // Backwards
    assertComputeLcgAdvance(8997, m, c, -3);
    assertComputeLcgAdvance(99785, m, c, -5);
    assertComputeLcgAdvance(1278, m, c, -7);
    assertComputeLcgAdvance(6711345482L, m, c, -13);
    assertComputeLcgAdvance(9657, m, c, -15);
  }

  /**
   * Compute the state of the Linear Congruential Generator Assert compute lcg advance.
   *
   * @param x the x
   * @param mult the LCG multiplier
   * @param add the LCG addition
   * @param steps the steps
   */
  private static void assertComputeLcgAdvance(final long x, final long mult, final long add,
      long steps) {
    // Run LCG
    long state = x;
    for (long i = Math.abs(steps); i-- > 0;) {
      state = state * mult + add;
    }

    final long[] advance = NumberUtils.computeLcgAdvance(mult, add, steps);

    // Test forward and backward
    long actual;
    long expected;
    if (steps >= 0) {
      // Forward
      actual = x * advance[0] + advance[1];
      expected = state;
    } else {
      // Backward
      actual = state * advance[0] + advance[1];
      expected = x;
    }

    Assertions.assertEquals(expected, actual, () -> "Failed to advance: " + steps);
  }

  @Test
  void testComputeLcgAdvancePow2() {
    final long m = 6364136223846793005L;
    final long c = 1442695040888963407L;

    // Powers of 2
    for (int i = 0; i <= 64; i++) {
      final int power = i;
      final long steps = (power < 64) ? 1L << power : 0;
      final long[] expected = NumberUtils.computeLcgAdvance(m, c, steps);
      final long[] actual = NumberUtils.computeLcgAdvancePow2(m, c, i);
      Assertions.assertArrayEquals(expected, actual, () -> "Failed to advance power: " + power);
    }
  }

  @Test
  void testLcgAdvance() {
    final long m = 6364136223846793005L;
    final long c = 1442695040888963407L;

    // Nothing
    assertLcgAdvance(6711382, m, c, 0);

    // Small powers of 2
    for (int i = 0; i < 8; i++) {
      assertLcgAdvance(676788, m, c, 1L << i);
    }

    // Odd
    assertLcgAdvance(8997, m, c, 3);
    assertLcgAdvance(99785, m, c, 5);
    assertLcgAdvance(1278, m, c, 7);
    assertLcgAdvance(6711345482L, m, c, 13);
    assertLcgAdvance(9657, m, c, 15);

    // Backwards
    assertLcgAdvance(8997, m, c, -3);
    assertLcgAdvance(99785, m, c, -5);
    assertLcgAdvance(1278, m, c, -7);
    assertLcgAdvance(6711345482L, m, c, -13);
    assertLcgAdvance(9657, m, c, -15);
  }

  /**
   * the state of the Linear Congruential Generator Assert compute lcg advance.
   *
   * @param x the x
   * @param mult the LCG multiplier
   * @param add the LCG addition
   * @param steps the steps
   */
  private static void assertLcgAdvance(final long x, final long mult, final long add, long steps) {
    // Run LCG
    long state = x;
    for (long i = Math.abs(steps); i-- > 0;) {
      state = state * mult + add;
    }

    // Test forward and backward
    long actual;
    long expected;
    if (steps >= 0) {
      // Forward
      actual = NumberUtils.lcgAdvance(x, mult, add, steps);
      expected = state;
    } else {
      // Backward
      actual = NumberUtils.lcgAdvance(state, mult, add, steps);
      expected = x;
    }

    Assertions.assertEquals(expected, actual, () -> "Failed to advance: " + steps);
  }

  @Test
  void testLcgAdvancePow2() {
    final long m = 6364136223846793005L;
    final long c = 1442695040888963407L;

    final long state = 2738942865345L;

    // Powers of 2
    for (int i = 0; i <= 64; i++) {
      final int power = i;
      final long steps = (power < 64) ? 1L << power : 0;
      final long expected = NumberUtils.lcgAdvance(state, m, c, steps);
      final long actual = NumberUtils.lcgAdvancePow2(state, m, c, i);
      Assertions.assertEquals(expected, actual, () -> "Failed to advance power: " + power);
    }
  }

  @SeededTest
  void testComputeInverseLong(RandomSeed seed) {
    Assertions.assertThrows(IllegalArgumentException.class, () -> NumberUtils.computeInverse(0L));

    // Known constants taken from the Mixers class
    Assertions.assertEquals(0x2ab9c720d1024adL, NumberUtils.computeInverse(0x9fb21c651e98df25L));

    final UniformRandomProvider rng = RngFactory.create(seed.get());
    for (int i = 0; i < 20; i++) {
      final long x = rng.nextLong() | 1L;
      final long y = NumberUtils.computeInverse(x);
      Assertions.assertEquals(1L, x * y, "x * y");
      Assertions.assertEquals(1L, y * x, "y * x");
    }
  }

  @SeededTest
  void testComputeInverseInt(RandomSeed seed) {
    Assertions.assertThrows(IllegalArgumentException.class, () -> NumberUtils.computeInverse(0));

    final UniformRandomProvider rng = RngFactory.create(seed.get());
    for (int i = 0; i < 20; i++) {
      final int x = rng.nextInt() | 1;
      final int y = NumberUtils.computeInverse(x);
      Assertions.assertEquals(1, x * y, "x * y");
      Assertions.assertEquals(1, y * x, "y * x");
    }
  }

  @Test
  void testMakeSignedDouble() {
    Assertions.assertEquals(0.0, NumberUtils.makeSignedDouble(0L));
    Assertions.assertEquals(Math.nextDown(1.0), NumberUtils.makeSignedDouble(Long.MAX_VALUE));
    Assertions.assertEquals(-1.0, NumberUtils.makeSignedDouble(Long.MIN_VALUE));
    Assertions.assertEquals(-1.0 + 0x1.0p-53,
        NumberUtils.makeSignedDouble(Long.MIN_VALUE + (1L << 10)));
    Assertions.assertEquals(-0x1.0p-53, NumberUtils.makeSignedDouble(-1L));
    Assertions.assertEquals(-0x2.0p-53, NumberUtils.makeSignedDouble(-2L << 10));
    Assertions.assertEquals(-0x3.0p-53, NumberUtils.makeSignedDouble(-3L << 10));
    Assertions.assertEquals(-0x4.0p-53, NumberUtils.makeSignedDouble(-4L << 10));
  }

  @Test
  void testMakeNormalDouble() {
    // Assume bottom 12-bits are discarded
    Assertions.assertEquals(1.0, NumberUtils.makeNormalDouble(0L));
    Assertions.assertEquals(Math.nextDown(2.0), NumberUtils.makeNormalDouble(0xfffffffffffff000L));
    Assertions.assertEquals(Math.nextUp(1.0), NumberUtils.makeNormalDouble(0x0000000000001000L));
    Assertions.assertEquals(1.5, NumberUtils.makeNormalDouble(0x8000000000000000L));
  }
}
