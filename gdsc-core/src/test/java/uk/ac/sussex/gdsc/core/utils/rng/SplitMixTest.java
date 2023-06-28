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

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.SplittableRandom;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.commons.rng.RandomProviderState;
import org.apache.commons.rng.core.source64.SplitMix64;
import org.apache.commons.rng.core.util.NumberFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

@SuppressWarnings("javadoc")
class SplitMixTest {
  @SeededTest
  void testNextInt(RandomSeed randomSeed) {
    final long seed = randomSeed.getAsLong();
    final SplittableRandom sr = new SplittableRandom(seed);
    final SplitMix sm = SplitMix.new64(seed);
    for (int i = 0; i < 10; i++) {
      Assertions.assertEquals(sr.nextInt(), sm.nextInt());
    }
  }

  @SeededTest
  void testNextLong(RandomSeed randomSeed) {
    final long seed = randomSeed.getAsLong();
    final SplittableRandom sr = new SplittableRandom(seed);
    final SplitMix sm = SplitMix.new64(seed);
    for (int i = 0; i < 10; i++) {
      Assertions.assertEquals(sr.nextLong(), sm.nextLong());
    }
  }

  @SeededTest
  void testNextDouble(RandomSeed randomSeed) {
    final long seed = randomSeed.getAsLong();
    final SplittableRandom sr = new SplittableRandom(seed);
    final SplitMix sm = SplitMix.new64(seed);
    for (int i = 0; i < 10; i++) {
      Assertions.assertEquals(sr.nextDouble(), sm.nextDouble());
    }
  }

  @SeededTest
  void testNextBoolean(RandomSeed randomSeed) {
    final long seed = randomSeed.getAsLong();
    final SplittableRandom sr = new SplittableRandom(seed);
    final SplitMix sm = SplitMix.new64(seed);
    for (int i = 0; i < 10; i++) {
      Assertions.assertEquals(sr.nextBoolean(), sm.nextBoolean());
    }
  }

  @Test
  void testNextIntInRange() {
    // This test uses a fixed seed to avoid flaky tests. The nextInt method is different from
    // JDK SplittableRandom so do a statistical test.
    final long seed = -143514987121378964L;
    final SplitMix sm = SplitMix.new64(seed);
    // A power of 2 and the worst case scenario for the rejection algorithm.
    // Rejection should occur almost 50% of the time so the test should hit all paths.
    assertNextIntInRange(sm, 16 * 16, 16);
    assertNextIntInRange(sm, 17 * 17, 17);
    assertNextIntInRange(sm, (1 << 30) + 16, 16);
  }

  /**
   * Assert the nextInt(int) method is uniform. The bins must exactly divide into the limit.
   *
   * @param rng the rng
   * @param limit the limit
   * @param bins the bins
   */
  private static void assertNextIntInRange(SplitMix rng, int limit, int bins) {
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

  @SeededTest
  void testNextLongInRange(RandomSeed randomSeed) {
    final long seed = randomSeed.getAsLong();
    final SplittableRandom sr = new SplittableRandom(seed);
    final SplitMix sm = SplitMix.new64(seed);
    // A power of 2 and the worst case scenario for the rejection algorithm.
    // Rejection should occur almost 50% of the time so the test should hit all paths.
    for (final long range : new long[] {256, (1L << 62) + 1}) {
      for (int i = 0; i < 10; i++) {
        Assertions.assertEquals(sr.nextLong(range), sm.nextLong(range));
      }
    }
  }

  @SeededTest
  void testNextFloat(RandomSeed randomSeed) {
    final long seed = randomSeed.getAsLong();
    final SplitMix sm1 = SplitMix.new64(seed);
    final SplitMix sm2 = SplitMix.new64(seed);
    for (int i = 0; i < 10; i++) {
      // NumberFactory will be updated in v1.3
      // final float expected = NumberFactory.makeFloat(sm1.nextInt());
      final float expected = (sm1.nextInt() >>> 8) * 0x1.0p-24f;
      Assertions.assertEquals(expected, sm2.nextFloat());
    }
  }

  @SeededTest
  void testNextBytes(RandomSeed randomSeed) {
    final long seed = randomSeed.getAsLong();
    // Test verses Commons RNG implementation
    final SplitMix64 sm1 = new SplitMix64(seed);
    final SplitMix sm2 = SplitMix.new64(seed);
    for (final int range : new int[] {16, 18}) {
      final byte[] b1 = new byte[range];
      final byte[] b2 = new byte[range];
      for (int i = 0; i < 10; i++) {
        sm1.nextBytes(b1);
        sm2.nextBytes(b2);
        Assertions.assertArrayEquals(b1, b2);
      }
    }
  }

  @SeededTest
  void testAdvance(RandomSeed randomSeed) {
    final long seed = randomSeed.getAsLong();
    final SplitMix sm1 = SplitMix.new64(seed);
    final SplitMix sm2 = SplitMix.new64(seed);
    for (final int range : new int[] {1, 10, 32}) {
      for (int i = 0; i < range; i++) {
        sm1.nextLong();
      }
      sm2.advance(range);
      for (int i = 0; i < 10; i++) {
        Assertions.assertEquals(sm1.nextLong(), sm2.nextLong());
      }
    }
  }

  @SeededTest
  void testAdvanceBackwards(RandomSeed randomSeed) {
    final long seed = randomSeed.getAsLong();
    final SplitMix sm1 = SplitMix.new64(seed);
    for (final int range : new int[] {1, 10, 32}) {
      final long[] seq1 = new long[range];
      fill(sm1, seq1);
      sm1.advance(-range);
      final long[] seq2 = new long[range];
      fill(sm1, seq2);
      Assertions.assertArrayEquals(seq1, seq2);
    }
  }

  private static void fill(SplitMix sm, long[] sequence) {
    for (int i = 0; i < sequence.length; i++) {
      sequence[i] = sm.nextLong();
    }
  }

  @SeededTest
  void testCopy(RandomSeed randomSeed) {
    final long seed = randomSeed.getAsLong();
    final SplitMix sm1 = SplitMix.new64(seed);
    final SplitMix sm2 = sm1.copy();
    Assertions.assertNotSame(sm1, sm2);
    for (int i = 0; i < 10; i++) {
      Assertions.assertEquals(sm1.nextLong(), sm2.nextLong());
    }
  }

  @SeededTest
  void testCopyAndJump(RandomSeed randomSeed) {
    final long seed = randomSeed.getAsLong();
    final SplitMix sm1 = SplitMix.new64(seed);
    final SplitMix sm2 = sm1.copyAndJump();
    Assertions.assertNotSame(sm1, sm2);
    for (int i = 0; i < 10; i++) {
      Assertions.assertNotEquals(sm1.nextLong(), sm2.nextLong());
    }
  }

  @Test
  void testNextIntUsingZeroThrows() {
    final SplitMix sm = SplitMix.new64(0);
    Assertions.assertThrows(IllegalArgumentException.class, () -> sm.nextInt(0));
  }

  @Test
  void testNextLongUsingZeroThrows() {
    final SplitMix sm = SplitMix.new64(0);
    Assertions.assertThrows(IllegalArgumentException.class, () -> sm.nextLong(0));
  }

  @SeededTest
  void testGetState(RandomSeed randomSeed) {
    final long seed = randomSeed.getAsLong();
    final SplitMix sm = SplitMix.new64(seed);
    Assertions.assertEquals(seed, sm.getState());
  }

  @SeededTest
  void testSaveAndRestoreState(RandomSeed randomSeed) {
    final long seed = randomSeed.getAsLong();
    final SplitMix sm = SplitMix.new64(seed);
    final RandomProviderState state = sm.saveState();
    final long[] seq1 = new long[10];
    fill(sm, seq1);
    sm.restoreState(state);
    final long[] seq2 = new long[seq1.length];
    fill(sm, seq2);
    Assertions.assertArrayEquals(seq1, seq2);
  }

  @Test
  void testRestoreUsingBadStateThrows() {
    final SplitMix sm = SplitMix.new64(0);
    final RandomProviderState state = null;
    Assertions.assertThrows(IllegalArgumentException.class, () -> sm.restoreState(state));
  }

  //////////////////////////
  // Test 32-bit variation
  //////////////////////////

  @SeededTest
  void testNextInt32(RandomSeed randomSeed) {
    final long seed = randomSeed.getAsLong();
    final SplittableRandom sr = new SplittableRandom(seed);
    final SplitMix sm = SplitMix.new32(seed);
    for (int i = 0; i < 10; i++) {
      Assertions.assertEquals(sr.nextInt(), sm.nextInt());
    }
  }

  @SeededTest
  void testNextLong32(RandomSeed randomSeed) {
    final long seed = randomSeed.getAsLong();
    final SplittableRandom sr = new SplittableRandom(seed);
    final SplitMix sm = SplitMix.new32(seed);
    for (int i = 0; i < 10; i++) {
      final long expected = NumberFactory.makeLong(sr.nextInt(), sr.nextInt());
      Assertions.assertEquals(expected, sm.nextLong());
    }
  }

  @SeededTest
  void testCopy32(RandomSeed randomSeed) {
    final long seed = randomSeed.getAsLong();
    final SplitMix sm1 = SplitMix.new32(seed);
    final SplitMix sm2 = sm1.copy();
    Assertions.assertNotSame(sm1, sm2);
    for (int i = 0; i < 10; i++) {
      Assertions.assertEquals(sm1.nextLong(), sm2.nextLong());
    }
  }

  @SeededTest
  void testNextBytes32(RandomSeed randomSeed) {
    final long seed = randomSeed.getAsLong();
    final SplittableRandom sr = new SplittableRandom(seed);
    final SplitMix sm = SplitMix.new32(seed);
    for (final int range : new int[] {16, 18}) {
      final int samples = (range + 3) / 4;
      final byte[] b2 = new byte[range];
      for (int i = 0; i < 10; i++) {
        final ByteBuffer bb = ByteBuffer.allocate(samples * 4);
        for (int j = 0; j < samples; j++) {
          // The RNGs write bytes least significant first so reverse bytes
          bb.putInt(Integer.reverseBytes(sr.nextInt()));
        }
        bb.flip();
        final byte[] arr = new byte[bb.remaining()];
        bb.get(arr);

        // Truncate to expected size
        final byte[] b1 = Arrays.copyOf(arr, b2.length);

        sm.nextBytes(b2);
        Assertions.assertArrayEquals(b1, b2);
      }
    }
  }
}
