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
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.core.util.NumberFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.rng.Entropy.EntropyDigest;

@SuppressWarnings("javadoc")
class EntropyTest {
  @Test
  void testPerfectEntropy() {
    final long[] counts = {10, 10};
    Assertions.assertEquals(1, Entropy.bits(counts), 1e-6, "Failed using long[]");
    final int[] counts2 = {10, 10};
    Assertions.assertEquals(1, Entropy.bits(counts2), 1e-6, "Failed using int[]");
    final double[] probabilities = {0.5, 0.5};
    Assertions.assertEquals(1, Entropy.bits(probabilities), 1e-6, "Failed using double[]");
  }

  @Test
  void testZeroEntropy() {
    final long[] counts = {0, 10};
    Assertions.assertEquals(0, Entropy.bits(counts), "Failed using long[]");
    final int[] counts2 = {0, 10};
    Assertions.assertEquals(0, Entropy.bits(counts2), "Failed using int[]");
    final double[] probabilities = {0, 1};
    Assertions.assertEquals(0, Entropy.bits(probabilities), "Failed using double[]");
  }

  @Test
  void testIgnoresNegativeCounts() {
    final double e = Entropy.bits(9, 5, 3, 2, 13, 5);
    final double o1 = Entropy.bits(9, 5, -4, 3, 2, 13, 5, -3);
    Assertions.assertEquals(e, o1, "Failed using int...");
    final double o2 = Entropy.bits(9L, 5L, -4L, 3L, 2L, 13L, 5L, -3L);
    Assertions.assertEquals(e, o2, "Failed using long...");
  }

  @Test
  void testRandomBytes() {
    final long seed = 7238949279L;
    final int samples = 1024;

    final int[] counts = new int[256];
    final UniformRandomProvider rng = SplitMix.new64(seed);
    for (int i = 0; i < samples; i++) {
      final long value = rng.nextLong();
      for (int j = 0; j < 8; j++) {
        final int bi = ((int) (value >>> j * 8)) & 0xff;
        counts[bi]++;
      }
    }

    assertRandom(seed, samples, counts, 8);
  }

  @Test
  void testRandomBits() {
    final long seed = 789743214L;
    final int samples = 256;

    final int[] counts = new int[2];
    final UniformRandomProvider rng = SplitMix.new64(seed);
    for (int i = 0; i < samples; i++) {
      final int count1 = Long.bitCount(rng.nextLong());
      counts[0] += Long.SIZE - count1;
      counts[1] += count1;
    }

    assertRandom(seed, samples, counts, 1);
  }

  private static void assertRandom(long seed, int samples, int[] counts, int maxEntropy) {
    final double entropy = Entropy.bits(counts);
    Assertions.assertEquals(maxEntropy, entropy, 0.05, "Entropy should be close to max");
    Assertions.assertFalse(entropy > maxEntropy, "Entropy should not exceed max");

    Assertions.assertEquals(entropy, Entropy.bits(Arrays.stream(counts).asLongStream().toArray()),
        "Should be the same when using long[] counts");
    final long totalCount = Arrays.stream(counts).sum();
    Assertions.assertEquals(entropy,
        Entropy.bits(Arrays.stream(counts).asDoubleStream().map(p -> p / totalCount).toArray()),
        1e-6, "Should be close when using double[] counts");

    final EntropyDigest be = Entropy.createDigest(maxEntropy == 1);
    Assertions.assertEquals(0, be.bits(), "Should be no entropy");

    final byte[] bytes = new byte[samples * 8];
    SplitMix.new64(seed).nextBytes(bytes);
    for (final byte bi : bytes) {
      be.add(bi);
    }
    Assertions.assertEquals(entropy, be.bits(), "Should be the same when using stream of bytes");

    be.reset();
    Assertions.assertEquals(0, be.bits(), "Should be no entropy");
    UniformRandomProvider rng = SplitMix.new64(seed);
    for (int i = 0; i < samples; i++) {
      be.add(rng.nextLong());
    }
    Assertions.assertEquals(entropy, be.bits(), "Should be the same when using stream of longs");

    be.reset();
    Assertions.assertEquals(0, be.bits(), "Should be no entropy");
    rng = SplitMix.new64(seed);
    for (int i = 0; i < samples; i++) {
      final long value = rng.nextLong();
      be.add(NumberFactory.extractHi(value));
      be.add(NumberFactory.extractLo(value));
    }
    Assertions.assertEquals(entropy, be.bits(), "Should be the same when using stream of ints");
  }

  @Test
  @Disabled("This has no assertions")
  void testRngEntropy() {
    final long seed = ThreadLocalRandom.current().nextLong();
    for (final UniformRandomProvider rng : new UniformRandomProvider[] {SplitMix.new64(seed),
        SplitMix.new32(seed), Pcg32.xshrr(seed), Pcg32.xshrs(seed),
        MiddleSquareWeylSequence.newInstance(seed),}) {
      System.out.println(rng);
      final EntropyDigest ent1 = Entropy.createDigest(false);
      final EntropyDigest ent2 = Entropy.createDigest(true);
      for (int size = 32; size < 1024; size <<= 1) {
        for (int i = 0; i < 1; i++) {
          ent1.reset();
          ent2.reset();
          for (int j = 0; j < size; j++) {
            final long value = rng.nextLong();
            ent1.add(value);
            ent2.add(value);
          }
          System.out.printf("size=%4d [%d]  %-18s  %-18s%n", size, i, ent1.bits(), ent2.bits());
        }
      }
    }
  }
}
