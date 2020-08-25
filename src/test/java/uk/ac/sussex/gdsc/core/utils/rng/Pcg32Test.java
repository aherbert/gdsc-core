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
 * Copyright (C) 2011 - 2020 Alex Herbert
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

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.commons.rng.RandomProviderState;
import org.apache.commons.rng.core.util.NumberFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

@SuppressWarnings("javadoc")
class Pcg32Test {

  static class ReferenceSequenceParams implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      final long x = 0x012de1babb3c4104L;
      final long y = 0xc8161b4202294965L;
      // Tested with respect to C++ implementation.
      // See: http://www.pcg-random.org/download.html#cpp-implementation
      return Stream.of(
          // pcg_engines::setseq_xsh_rs_64_32(x, y)
          Arguments.of(new int[] {0xba4138b8, 0xd329a393, 0x75d68d3f, 0xbb7572ca, 0x7a48d2f2,
              0xcb3c1e37, 0xc1374a97, 0x7c2c5bfa, 0x8a1c8695, 0x30db4fea, 0x95f9a901, 0x72ebfa48,
              0x6a284dbf, 0x0ef11286, 0x37330e11, 0xfeb53893, 0x77e3adda, 0x64dc86bd, 0xc8d762d7,
              0xbf3fb80c, 0x732dfd12, 0x6088e86d, 0xbc4e79e5, 0x56ece5b1, 0xe706ac72, 0xee798018,
              0xef73de74, 0x3de1f966, 0x7a36db53, 0x1e921eb2, 0x55e35484, 0x2577c6f2, 0x0a006e21,
              0x8cb811b7, 0x5f26c916, 0x3990837f, 0x15f2983d, 0x546ccb4a, 0x4eda8716, 0xb8666a25,},
              Pcg32.xshrs(x, y)),
          // pcg_engines::setseq_xsh_rs_64_32(x)
          Arguments.of(new int[] {0x5ab2ddd9, 0x215c476c, 0x83c34b11, 0xe2c5e213, 0x37979624,
              0x303cf5b5, 0xbf2a146e, 0xb0692351, 0x49b00de3, 0xd9ded67c, 0x298e2bb9, 0xa20d2287,
              0xa067cd33, 0x5c10d395, 0x1f8d8bd5, 0x4306b6bc, 0x97a3e50b, 0x992e0604, 0x8a982b33,
              0x4baa6604, 0xefd995eb, 0x0f341c29, 0x080bce32, 0xb22b3de2, 0x5fbf47ff, 0x7fc928bf,
              0x075a5871, 0x174a0c48, 0x72458b67, 0xa869a8c1, 0x64857577, 0xed28377c, 0x3ce86b48,
              0xa855af8b, 0x6a051d88, 0x23b06c33, 0xb3e4afc1, 0xa848c3e4, 0x79f969a6, 0x670e2acb,},
              Pcg32.xshrs(x)),
          // pcg_engines::setseq_xsh_rr_64_32(x, y)
          Arguments.of(new int[] {0xe860dd24, 0x15d339c0, 0xd9f75c46, 0x00efabb7, 0xa625e97f,
              0xcdeae599, 0x6304e667, 0xbc81be11, 0x2b8ea285, 0x8e186699, 0xac552be9, 0xd1ae72e5,
              0x5b953ad4, 0xa061dc1b, 0x526006e7, 0xf5a6c623, 0xfcefea93, 0x3a1964d2, 0xd6f03237,
              0xf3e493f7, 0x0c733750, 0x34a73582, 0xc4f8807b, 0x92b741ca, 0x0d38bf9c, 0xc39ee6ad,
              0xdc24857b, 0x7ba8f7d8, 0x377a2618, 0x92d83d3f, 0xd22a957a, 0xb6724af4, 0xe116141a,
              0xf465fe45, 0xa95f35bb, 0xf0398d4d, 0xe880af3e, 0xc2951dfd, 0x984ec575, 0x8679addb,},
              Pcg32.xshrr(x, y)),
          // pcg_engines::setseq_xsh_rr_64_32(x)
          Arguments.of(new int[] {0x0d2d5291, 0x45df90aa, 0xc60f3fb7, 0x06694f16, 0x29563e6f,
              0x42f46063, 0xf2be5583, 0x30360e91, 0x36385531, 0xddd36cd9, 0x5f4a6535, 0x644d10c0,
              0xaca075d7, 0x33781706, 0x4e1f9f34, 0x0676e286, 0xaca5eeb2, 0x7315cc93, 0xa6dfefe2,
              0xd480e065, 0xda9da26f, 0xda0f27b7, 0x045c0844, 0x22acfa0f, 0xcd7ecd75, 0xb97fd692,
              0xac96dd03, 0xf59c7174, 0x488947fe, 0x64a3d543, 0x90963884, 0x4adee0bb, 0x993cf7c0,
              0x8545b3f2, 0x409b542d, 0x6bf0a247, 0xfd59f9b4, 0x8f50b06e, 0x1bbcf6f5, 0xe1fdd29c,},
              Pcg32.xshrr(x)));
    }
  }

  static class PcgFactory {
    LongFunction<Pcg32> constructor;
    String name;

    PcgFactory(LongFunction<Pcg32> constructor, String name) {
      this.constructor = constructor;
      this.name = name;
    }

    Pcg32 create(long seed) {
      return constructor.apply(seed);
    }

    @Override
    public String toString() {
      return name;
    }
  }

  static class PcgFactoryParams implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(Arguments.of(new PcgFactory(Pcg32::xshrs, "xshrs")),
          Arguments.of(new PcgFactory(Pcg32::xshrr, "xshrr")));
    }
  }

  @ParameterizedTest(name = "{index}: {1}")
  @ArgumentsSource(ReferenceSequenceParams.class)
  void testReferenceSequence(int[] expectedSequence, Pcg32 rng) {
    for (int i = 0; i < expectedSequence.length; i++) {
      Assertions.assertEquals(expectedSequence[i], rng.nextInt());
    }
  }

  /**
   * Check the long is two int values joined together.
   */
  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(PcgFactoryParams.class)
  void testNextLong(PcgFactory constructor) {
    final long seed = ThreadLocalRandom.current().nextLong();
    final Pcg32 rng1 = constructor.create(seed);
    final Pcg32 rng2 = constructor.create(seed);
    for (int i = 0; i < 200; i++) {
      final long expected = NumberFactory.makeLong(rng1.nextInt(), rng1.nextInt());
      Assertions.assertEquals(expected, rng2.nextLong());
    }
  }

  /**
   * Check the boolean is a sign test on the int value.
   */
  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(PcgFactoryParams.class)
  void testNextBooleanIsSignTest(PcgFactory constructor) {
    final long seed = ThreadLocalRandom.current().nextLong();
    final Pcg32 rng1 = constructor.create(seed);
    final Pcg32 rng2 = constructor.create(seed);
    for (int i = 0; i < 200; i++) {
      Assertions.assertEquals(rng1.nextInt() < 0, rng2.nextBoolean());
    }
  }

  /**
   * Check the float is the upper 24-bits from the int value multiplied by a constant.
   */
  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(PcgFactoryParams.class)
  void testNextFloatIs24BitProduct(PcgFactory constructor) {
    final long seed = ThreadLocalRandom.current().nextLong();
    final Pcg32 rng1 = constructor.create(seed);
    final Pcg32 rng2 = constructor.create(seed);
    for (int i = 0; i < 200; i++) {
      Assertions.assertEquals((rng1.nextInt() >>> 8) * 0x1.0p-24f, rng2.nextFloat());
    }
  }

  /**
   * Check the double is the upper 53-bits from the long value multiplied by a constant.
   */
  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(PcgFactoryParams.class)
  void testNextDoubleIs53BitProduct(PcgFactory constructor) {
    final long seed = ThreadLocalRandom.current().nextLong();
    final Pcg32 rng1 = constructor.create(seed);
    final Pcg32 rng2 = constructor.create(seed);
    for (int i = 0; i < 200; i++) {
      Assertions.assertEquals(NumberUtils.makeDouble(rng1.nextInt(), rng1.nextInt()),
          rng2.nextDouble());
    }
  }

  // All basic RNG methods based on the monobit test.
  // A fixed seed is used to avoid flaky tests.

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(PcgFactoryParams.class)
  void testNextInt(PcgFactory constructor) {
    final long seed = 2378923479523479L;
    final Pcg32 rng = constructor.create(seed);
    int bitCount = 0;
    final int n = 100;
    for (int i = 0; i < n; i++) {
      bitCount += Integer.bitCount(rng.nextInt());
    }
    final int numberOfBits = n * Integer.SIZE;
    assertMonobit(bitCount, numberOfBits);
  }

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(PcgFactoryParams.class)
  void testNextDouble(PcgFactory constructor) {
    final long seed = 789432646432165L;
    final Pcg32 rng = constructor.create(seed);
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

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(PcgFactoryParams.class)
  void testNextBoolean(PcgFactory constructor) {
    final long seed = 456129879875161546L;
    final Pcg32 rng = constructor.create(seed);
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

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(PcgFactoryParams.class)
  void testNextFloat(PcgFactory constructor) {
    final long seed = 123489743213246946L;
    final Pcg32 rng = constructor.create(seed);
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

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(PcgFactoryParams.class)
  void testNextBytes(PcgFactory constructor) {
    final long seed = -1514989856145479866L;
    final Pcg32 rng = constructor.create(seed);
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

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(PcgFactoryParams.class)
  void testNextIntInRange(PcgFactory constructor) {
    final long seed = -7891452149463L;
    final Pcg32 rng = constructor.create(seed);
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
  private static void assertNextIntInRange(Pcg32 rng, int limit, int bins) {
    Assertions.assertEquals(0, limit % bins, "Invalid test: limit/bins must be a whole number");

    final long[] observed = new long[bins];
    final int divisor = limit / bins;
    final int samples = 10000;
    for (int i = 0; i < 10000; i++) {
      observed[rng.nextInt(limit) / divisor]++;
    }
    final double[] expected = new double[bins];
    Arrays.fill(expected, (double) samples / bins);
    final ChiSquareTest test = new ChiSquareTest();
    final double pvalue = test.chiSquareTest(expected, observed);
    Assertions.assertFalse(pvalue < 0.01, "P-value = " + pvalue);
  }

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(PcgFactoryParams.class)
  void testNextLongInRange(PcgFactory constructor) {
    final long seed = 1451657946515648L;
    final Pcg32 rng = constructor.create(seed);
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
  private static void assertNextLongInRange(Pcg32 rng, long limit, int bins) {
    Assertions.assertEquals(0, limit % bins, "Invalid test: limit/bins must be a whole number");

    final long[] observed = new long[bins];
    final long divisor = limit / bins;
    final int samples = 10000;
    for (int i = 0; i < 10000; i++) {
      observed[(int) (rng.nextLong(limit) / divisor)]++;
    }
    final double[] expected = new double[bins];
    Arrays.fill(expected, (double) samples / bins);
    final ChiSquareTest test = new ChiSquareTest();
    final double pvalue = test.chiSquareTest(expected, observed);
    Assertions.assertFalse(pvalue < 0.01, "P-value = " + pvalue);
  }

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(PcgFactoryParams.class)
  void testAdvance(PcgFactory constructor) {
    final long seed = ThreadLocalRandom.current().nextLong();
    final Pcg32 rng1 = constructor.create(seed);
    final Pcg32 rng2 = constructor.create(seed);
    for (final int range : new int[] {1, 10, 32}) {
      for (int i = 0; i < range; i++) {
        rng1.nextInt();
      }
      rng2.advance(range);
      for (int i = 0; i < 10; i++) {
        Assertions.assertEquals(rng1.nextInt(), rng2.nextInt());
      }
    }
  }

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(PcgFactoryParams.class)
  void testAdvanceBackwards(PcgFactory constructor) {
    final long seed = ThreadLocalRandom.current().nextLong();
    final Pcg32 rng1 = constructor.create(seed);
    for (final int range : new int[] {1, 10, 32}) {
      final int[] seq1 = new int[range];
      fill(rng1, seq1);
      rng1.advance(-range);
      final int[] seq2 = new int[range];
      fill(rng1, seq2);
      Assertions.assertArrayEquals(seq1, seq2);
    }
  }

  private static void fill(Pcg32 rng, int[] sequence) {
    for (int i = 0; i < sequence.length; i++) {
      sequence[i] = rng.nextInt();
    }
  }

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(PcgFactoryParams.class)
  void testCopyAndJump(PcgFactory constructor) {
    final long seed = ThreadLocalRandom.current().nextLong();
    final Pcg32 rng1 = constructor.create(seed);
    final Pcg32 rng2 = rng1.copyAndJump();
    Assertions.assertNotSame(rng1, rng2);
    for (int i = 0; i < 10; i++) {
      Assertions.assertNotEquals(rng1.nextInt(), rng2.nextInt());
    }
  }

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(PcgFactoryParams.class)
  void testNextIntUsingZeroThrows(PcgFactory constructor) {
    final Pcg32 rng = constructor.create(0);
    Assertions.assertThrows(IllegalArgumentException.class, () -> rng.nextInt(0));
  }

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(PcgFactoryParams.class)
  void testNextLongUsingZeroThrows(PcgFactory constructor) {
    final Pcg32 rng = constructor.create(0);
    Assertions.assertThrows(IllegalArgumentException.class, () -> rng.nextLong(0));
  }

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(PcgFactoryParams.class)
  void testSaveAndRestoreState(PcgFactory constructor) {
    final long seed = ThreadLocalRandom.current().nextLong();
    final Pcg32 rng = constructor.create(seed);
    final RandomProviderState state = rng.saveState();
    final int[] seq1 = new int[10];
    fill(rng, seq1);
    rng.restoreState(state);
    final int[] seq2 = new int[seq1.length];
    fill(rng, seq2);
    Assertions.assertArrayEquals(seq1, seq2);
  }

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(PcgFactoryParams.class)
  void testRestoreUsingBadStateThrows(PcgFactory constructor) {
    final Pcg32 rng = constructor.create(0);
    final RandomProviderState state = null;
    Assertions.assertThrows(IllegalArgumentException.class, () -> rng.restoreState(state));
  }

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(PcgFactoryParams.class)
  void testCopy(PcgFactory constructor) {
    final long seed = ThreadLocalRandom.current().nextLong();
    assertDuplicate(constructor.create(seed), Pcg32::copy, true);
  }

  @ParameterizedTest(name = "{index}: {0}")
  @ArgumentsSource(PcgFactoryParams.class)
  void testSplit(PcgFactory constructor) {
    final long seed = ThreadLocalRandom.current().nextLong();
    assertDuplicate(constructor.create(seed), Pcg32::split, false);
  }

  private static void assertDuplicate(Pcg32 rng1, UnaryOperator<Pcg32> duplicate,
      boolean expectedEqual) {
    final Pcg32 rng2 = duplicate.apply(rng1);
    Assertions.assertNotSame(rng1, rng2);
    boolean equal = true;
    for (int i = 0; i < 10; i++) {
      if (rng1.nextInt() != rng2.nextInt()) {
        equal = false;
        break;
      }
    }
    Assertions.assertEquals(expectedEqual, equal);
  }
}
