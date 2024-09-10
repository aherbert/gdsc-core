/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2023 Alex Herbert
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

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Arrays;
import java.util.stream.Stream;
import org.apache.commons.rng.RandomProviderState;
import org.apache.commons.rng.RestorableUniformRandomProvider;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.core.util.NumberFactory;
import org.apache.commons.statistics.inference.ChiSquareTest;
import org.apache.commons.statistics.inference.SignificanceResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

@SuppressWarnings("javadoc")
class MiddleSquareWeylSequenceTest {

  static class ReferenceSequenceParams implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      final long x = 0x012de1babb3c4104L;
      final long w = 0xc8161b4202294965L;
      final long s = 0xb5ad4eceda1ce2a9L;

      // The data was generated using the author's C code:
      // See: https://mswsrng.wixsite.com/rand
      return Stream.of(
          Arguments.of(new int[] {0xe7f4010b, 0x37bdb1e7, 0x05d8934f, 0x22970c75, 0xe7432a9f,
              0xd157c60f, 0x26e9b5ae, 0x3dd91250, 0x8dbf85f1, 0x99e3aa17, 0xcb90322b, 0x29a007e2,
              0x25a431fb, 0xcc612768, 0x510db5cd, 0xeb0aec2f, 0x05f88c18, 0xcdb79066, 0x5222c513,
              0x9075045c, 0xf11a0e0e, 0x0106ab1d, 0xe2546700, 0xdf0a7656, 0x170e7908, 0x17a7b775,
              0x98d69720, 0x74da3b78, 0x410ea18e, 0x4f708277, 0x471853e8, 0xa2cd2587, 0x16238d96,
              0x57653154, 0x7ecbf9c8, 0xc5dd75bf, 0x32ed82a2, 0x4700e664, 0xb0ad77c9, 0xfb87df7b},
              new MiddleSquareWeylSequence(x, w, s)),
          Arguments.of(new int[] {0x2a9e6357, 0xb397cb89, 0xe6f67161, 0xdd34df97, 0x0661808e,
              0x5dd51f98, 0x7518f41b, 0x594b15a0, 0xe9570f16, 0x5606452f, 0x5eab5422, 0xd1c4e0ca,
              0x0f3c7e59, 0x71e0bf07, 0x37e4856b, 0xb22dd1ff, 0xf0adf1dc, 0x6e14a0c2, 0x7cb7ac48,
              0x6ffcff58, 0xbbb034df, 0x8b3d1c0b, 0x4a17d2a9, 0xc0a679d1, 0x4ceef4cd, 0xacee1cdf,
              0x94f5cbe8, 0xe1178af6, 0x352b888b, 0xce9483e4, 0xfd10db1a, 0x94fb573a, 0x8d61ec1f,
              0x3deb6625, 0x67a48a9c, 0x2644861c, 0xd73ee1c7, 0x2aa14400, 0x703dccab, 0xf65b53ac},
              new MiddleSquareWeylSequence(s, w, s)),
          Arguments.of(new int[] {0xd58b3498, 0x7d389203, 0xbe8d5fb4, 0x4b49fb74, 0x376125d8,
              0x1a9ce8e8, 0xce4bfa22, 0x2e874280, 0x02d0c071, 0xc5a6cc12, 0x9f96d583, 0xaa30adb8,
              0xcd7bbe69, 0x524bfa3f, 0xcc0eedda, 0xfd64d6a2, 0xe5d8be33, 0xeaae39c6, 0x6edd1ec9,
              0x692cfa9a, 0x4584d098, 0xcf6433cf, 0x86fb75e7, 0x5eea777f, 0x434ac39e, 0x9c7b671f,
              0x0dd49cdb, 0x2424764f, 0x4cad5879, 0x876da162, 0x0ac025a3, 0x91a0e8ab, 0xdac8053a,
              0x876236f2, 0x90c253d4, 0xeec506f7, 0x33bc0581, 0x3c15319a, 0xc1895053, 0xd17cbdad},
              new MiddleSquareWeylSequence(x, s, s)));
    }
  }

  @ParameterizedTest(name = "{index}: {1}")
  @ArgumentsSource(ReferenceSequenceParams.class)
  void testReferenceSequence(int[] expectedSequence, MiddleSquareWeylSequence rng) {
    for (int i = 0; i < expectedSequence.length; i++) {
      Assertions.assertEquals(expectedSequence[i], rng.nextInt());
    }
  }

  @Test
  void testIncrementsAreUnique() {
    final int[] increments = MiddleSquareWeylSequence.getIncrements();
    Assertions.assertEquals(1024, increments.length);
    // Check the permutations are unique.
    final IntOpenHashSet set = new IntOpenHashSet(increments.length * 2);
    for (final int inc : increments) {
      if (!set.add(inc)) {
        Assertions
            .fail("Duplicate increment " + Integer.toHexString(inc) + " at position " + set.size());
      }
    }
    // Check the permutations are unique when byte reversed.
    for (final int inc : increments) {
      if (!set.add(Integer.reverseBytes(inc))) {
        Assertions.fail("Duplicate byte reversed increment " + Integer.toHexString(inc)
            + " at position " + (set.size() - increments.length));
      }
    }
  }

  /**
   * Check the long is two int values joined together.
   */
  @SeededTest
  void testNextLong(RandomSeed randomSeed) {
    final long seed = randomSeed.getAsLong();
    final UniformRandomProvider rng1 = MiddleSquareWeylSequence.newInstance(seed);
    final UniformRandomProvider rng2 = MiddleSquareWeylSequence.newInstance(seed);
    for (int i = 0; i < 200; i++) {
      final long expected = NumberFactory.makeLong(rng1.nextInt(), rng1.nextInt());
      Assertions.assertEquals(expected, rng2.nextLong());
    }
  }

  /**
   * Check the boolean is a sign test on the int value.
   */
  @SeededTest
  void testNextBooleanIsSignTest(RandomSeed randomSeed) {
    final long seed = randomSeed.getAsLong();
    final UniformRandomProvider rng1 = MiddleSquareWeylSequence.newInstance(seed);
    final UniformRandomProvider rng2 = MiddleSquareWeylSequence.newInstance(seed);
    for (int i = 0; i < 200; i++) {
      Assertions.assertEquals(rng1.nextInt() < 0, rng2.nextBoolean());
    }
  }

  /**
   * Check the float is the upper 24-bits from the int value multiplied by a constant.
   */
  @SeededTest
  void testNextFloatIs24BitProduct(RandomSeed randomSeed) {
    final long seed = randomSeed.getAsLong();
    final UniformRandomProvider rng1 = MiddleSquareWeylSequence.newInstance(seed);
    final UniformRandomProvider rng2 = MiddleSquareWeylSequence.newInstance(seed);
    for (int i = 0; i < 200; i++) {
      Assertions.assertEquals((rng1.nextInt() >>> 8) * 0x1.0p-24f, rng2.nextFloat());
    }
  }

  /**
   * Check the double is the upper 53-bits from the long value multiplied by a constant.
   */
  @SeededTest
  void testNextDoubleIs53BitProduct(RandomSeed randomSeed) {
    final long seed = randomSeed.getAsLong();
    final UniformRandomProvider rng1 = MiddleSquareWeylSequence.newInstance(seed);
    final UniformRandomProvider rng2 = MiddleSquareWeylSequence.newInstance(seed);
    for (int i = 0; i < 200; i++) {
      Assertions.assertEquals((rng1.nextLong() >>> 11) * 0x1.0p-53, rng2.nextDouble());
    }
  }

  // All basic RNG methods based on the monobit test.
  // A fixed seed is used to avoid flaky tests.

  @Test
  void testNextInt() {
    // Test some simple seeds here
    for (final long seed : new long[] {-1L, 0L, 1L, 789314346434L}) {
      final UniformRandomProvider rng = MiddleSquareWeylSequence.newInstance(seed);
      int bitCount = 0;
      final int n = 100;
      for (int i = 0; i < n; i++) {
        bitCount += Integer.bitCount(rng.nextInt());
      }
      final int numberOfBits = n * Integer.SIZE;
      assertMonobit(bitCount, numberOfBits);
    }
  }

  @Test
  void testNextDouble() {
    final long seed = 45678912654654L;
    final UniformRandomProvider rng = MiddleSquareWeylSequence.newInstance(seed);
    int bitCount = 0;
    final int n = 100;
    for (int i = 0; i < n; i++) {
      final double value = rng.nextDouble();
      Assertions.assertTrue(value >= 0 && value < 1, () -> value + " not in range [0, 1)");
      bitCount += Long.bitCount((long) (value * (1L << 53)));
    }
    final int numberOfBits = n * 53;
    assertMonobit(bitCount, numberOfBits);
  }

  @Test
  void testNextBoolean() {
    final long seed = 76832468234L;
    final UniformRandomProvider rng = MiddleSquareWeylSequence.newInstance(seed);
    int bitCount = 0;
    final int n = 1000;
    for (int i = 0; i < n; i++) {
      if (rng.nextBoolean()) {
        bitCount++;
      }
    }
    final int numberOfBits = n;
    assertMonobit(bitCount, numberOfBits);
  }

  @Test
  void testNextFloat() {
    final long seed = -4154967231346L;
    final UniformRandomProvider rng = MiddleSquareWeylSequence.newInstance(seed);
    int bitCount = 0;
    final int n = 100;
    for (int i = 0; i < n; i++) {
      final float value = rng.nextFloat();
      Assertions.assertTrue(value >= 0 && value < 1, () -> value + " not in range [0, 1)");
      bitCount += Integer.bitCount((int) (value * (1 << 24)));
    }
    final int numberOfBits = n * 24;
    assertMonobit(bitCount, numberOfBits);
  }

  @Test
  void testNextBytes() {
    final long seed = -789451658841221355L;
    final UniformRandomProvider rng = MiddleSquareWeylSequence.newInstance(seed);
    for (final int range : new int[] {16, 18}) {
      final byte[] bytes = new byte[range];
      int bitCount = 0;
      final int n = 100;
      for (int i = 0; i < n; i++) {
        rng.nextBytes(bytes);
        for (final byte b1 : bytes) {
          bitCount += Integer.bitCount(b1 & 0xff);
        }
      }
      final int numberOfBits = n * Byte.SIZE * range;
      assertMonobit(bitCount, numberOfBits);
    }
  }

  /**
   * Assert that the number of 1 bits is approximately 50%. This is based upon a fixed-step "random
   * walk" of +1/-1 from zero.
   *
   * <p>The test is equivalent to the NIST Monobit test with a fixed p-value of 0.01. The number of
   * bits is recommended to be above 100.</p>
   *
   * @see <A href="https://csrc.nist.gov/publications/detail/sp/800-22/rev-1a/final">Bassham, et al
   *      (2010) NIST SP 800-22: A Statistical Test Suite for Random and Pseudorandom Number
   *      Generators for Cryptographic Applications. Section 2.1.</a>
   *
   * @param bitCount The bit count.
   * @param numberOfBits Number of bits.
   */
  private static void assertMonobit(int bitCount, int numberOfBits) {
    // Convert the bit count into a number of +1/-1 steps.
    final double sum = 2.0 * bitCount - numberOfBits;
    // The reference distribution is Normal with a standard deviation of sqrt(n).
    // Check the absolute position is not too far from the mean of 0 with a fixed
    // p-value of 0.01 taken from a 2-tailed Normal distribution. Computation of
    // the p-value requires the complimentary error function.
    final double absSum = Math.abs(sum);
    final double max = Math.sqrt(numberOfBits) * 2.576;
    Assertions.assertTrue(absSum <= max, () -> "Walked too far astray: " + absSum + " > " + max
        + " (test will fail randomly about 1 in 100 times)");
  }

  // Range methods uniformity tested using Chi-squared

  @Test
  void testNextIntInRange() {
    final long seed = 7891211456668865L;
    final UniformRandomProvider rng = MiddleSquareWeylSequence.newInstance(seed);
    // A power of 2 and the worst case scenario for the rejection algorithm.
    // Rejection should occur almost 50% of the time so the test should hit all paths.
    assertNextIntInRange(rng, 16 * 16, 16);
    assertNextIntInRange(rng, 17 * 17, 17);
    assertNextIntInRange(rng, (1 << 30) + 16, 16);
  }

  /**
   * Assert the nextInt(int) method is uniform. The bins must exactly divide into the limit.
   *
   * @param rng the rng
   * @param limit the limit
   * @param bins the bins
   */
  private static void assertNextIntInRange(UniformRandomProvider rng, int limit, int bins) {
    Assertions.assertEquals(0, limit % bins, "Invalid test: limit/bins must be a whole number");

    final long[] observed = new long[bins];
    final int divisor = limit / bins;
    final int samples = 10000;
    for (int i = 0; i < 10000; i++) {
      observed[rng.nextInt(limit) / divisor]++;
    }
    final double[] expected = new double[bins];
    Arrays.fill(expected, (double) samples / bins);
    final SignificanceResult r = ChiSquareTest.withDefaults().test(expected, observed);
    Assertions.assertFalse(r.reject(0.01), "P-value = " + r.getPValue());
  }

  @Test
  void testNextLongInRange() {
    final long seed = -487564654766323L;
    final UniformRandomProvider rng = MiddleSquareWeylSequence.newInstance(seed);
    // A power of 2 and the worst case scenario for the rejection algorithm.
    // Rejection should occur almost 50% of the time so the test should hit all paths.
    assertNextLongInRange(rng, 16 * 16, 16);
    assertNextLongInRange(rng, 17 * 17, 17);
    assertNextLongInRange(rng, (1L << 62) + 16, 16);
  }

  /**
   * Assert the nextLong(long) method is uniform. The bins must exactly divide into the limit.
   *
   * @param rng the rng
   * @param limit the limit
   * @param bins the bins
   */
  private static void assertNextLongInRange(UniformRandomProvider rng, long limit, int bins) {
    Assertions.assertEquals(0, limit % bins, "Invalid test: limit/bins must be a whole number");

    final long[] observed = new long[bins];
    final long divisor = limit / bins;
    final int samples = 10000;
    for (int i = 0; i < 10000; i++) {
      observed[(int) (rng.nextLong(limit) / divisor)]++;
    }
    final double[] expected = new double[bins];
    Arrays.fill(expected, (double) samples / bins);
    final SignificanceResult r = ChiSquareTest.withDefaults().test(expected, observed);
    Assertions.assertFalse(r.reject(0.01), "P-value = " + r.getPValue());
  }

  private static void fill(UniformRandomProvider rng, int[] sequence) {
    for (int i = 0; i < sequence.length; i++) {
      sequence[i] = rng.nextInt();
    }
  }

  @Test
  void testNextIntUsingZeroThrows() {
    final UniformRandomProvider rng = MiddleSquareWeylSequence.newInstance(0);
    Assertions.assertThrows(IllegalArgumentException.class, () -> rng.nextInt(0));
  }

  @Test
  void testNextLongUsingZeroThrows() {
    final UniformRandomProvider rng = MiddleSquareWeylSequence.newInstance(0);
    Assertions.assertThrows(IllegalArgumentException.class, () -> rng.nextLong(0));
  }

  @SeededTest
  void testSaveAndRestoreState(RandomSeed randomSeed) {
    final long seed = randomSeed.getAsLong();
    final RestorableUniformRandomProvider rng = MiddleSquareWeylSequence.newInstance(seed);
    final RandomProviderState state = rng.saveState();
    final int[] seq1 = new int[10];
    fill(rng, seq1);
    rng.restoreState(state);
    final int[] seq2 = new int[seq1.length];
    fill(rng, seq2);
    Assertions.assertArrayEquals(seq1, seq2);
  }

  @Test
  void testRestoreUsingBadStateThrows() {
    final RestorableUniformRandomProvider rng = MiddleSquareWeylSequence.newInstance(0);
    final RandomProviderState state = null;
    Assertions.assertThrows(IllegalArgumentException.class, () -> rng.restoreState(state));
  }

  @SeededTest
  void testSplit(RandomSeed randomSeed) {
    final long seed = randomSeed.getAsLong();
    final SplittableUniformRandomProvider rng1 = MiddleSquareWeylSequence.newInstance(seed);
    final UniformRandomProvider rng2 = rng1.split();
    Assertions.assertNotSame(rng1, rng2);
    for (int i = 0; i < 10; i++) {
      Assertions.assertNotEquals(rng1.nextInt(), rng2.nextInt());
    }
  }
}
