package uk.ac.sussex.gdsc.core.utils.rng;

import uk.ac.sussex.gdsc.core.utils.rng.Entropy.EntropyDigest;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.core.util.NumberFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

@SuppressWarnings("javadoc")
public class EntropyTest {
  @Test
  public void testPerfectEntropy() {
    final long[] counts = {10, 10};
    Assertions.assertEquals(1, Entropy.bits(counts), 1e-6, "Failed using long[]");
    final int[] counts2 = {10, 10};
    Assertions.assertEquals(1, Entropy.bits(counts2), 1e-6, "Failed using int[]");
    final double[] probabilities = {0.5, 0.5};
    Assertions.assertEquals(1, Entropy.bits(probabilities), 1e-6, "Failed using double[]");
  }

  @Test
  public void testZeroEntropy() {
    final long[] counts = {0, 10};
    Assertions.assertEquals(0, Entropy.bits(counts), "Failed using long[]");
    final int[] counts2 = {0, 10};
    Assertions.assertEquals(0, Entropy.bits(counts2), "Failed using int[]");
    final double[] probabilities = {0, 1};
    Assertions.assertEquals(0, Entropy.bits(probabilities), "Failed using double[]");
  }

  @Test
  public void testRandomBytes() {
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
  public void testRandomBits() {
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
}
