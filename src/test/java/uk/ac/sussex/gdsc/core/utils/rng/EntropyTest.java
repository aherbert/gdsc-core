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
    final int[] counts = new int[256];
    final long seed = 7238949279L;
    UniformRandomProvider rng = SplitMix.new64(seed);
    final int total = 1024;
    for (int i = 0; i < total; i++) {
      final long value = rng.nextLong();
      for (int j = 0; j < 8; j++) {
        final int bi = ((int) (value >>> j * 8)) & 0xff;
        counts[bi]++;
      }
    }

    final double entropy = Entropy.bits(counts);
    Assertions.assertEquals(8, entropy, 0.05, "Entropy should be close to 8 bits");
    Assertions.assertFalse(entropy > 8, "Entropy should not exceed 8 bits");

    Assertions.assertEquals(entropy, Entropy.bits(Arrays.stream(counts).asLongStream().toArray()),
        "Should be the same when using long[] counts");
    Assertions.assertEquals(entropy,
        Entropy.bits(Arrays.stream(counts).asDoubleStream().map(p -> p / (total * 8.0)).toArray()),
        1e-6, "Should be close when using double[] counts");

    final byte[] bytes = new byte[total * 8];
    SplitMix.new64(seed).nextBytes(bytes);
    final EntropyDigest be = Entropy.createDigest(false);
    for (final byte bi : bytes) {
      be.add(bi);
    }
    Assertions.assertEquals(entropy, be.bits(), "Should be the same when using stream of bytes");

    be.reset();
    rng = SplitMix.new64(seed);
    for (int i = 0; i < total; i++) {
      be.add(rng.nextLong());
    }
    Assertions.assertEquals(entropy, be.bits(), "Should be the same when using stream of longs");

    be.reset();
    rng = SplitMix.new64(seed);
    for (int i = 0; i < total; i++) {
      final long value = rng.nextLong();
      be.add(NumberFactory.extractHi(value));
      be.add(NumberFactory.extractLo(value));
    }
    Assertions.assertEquals(entropy, be.bits(), "Should be the same when using stream of ints");
  }

  @Test
  public void testRandomBits() {
    final int[] counts = new int[2];
    final long seed = 789743214L;
    UniformRandomProvider rng = SplitMix.new64(seed);
    final int total = 1024;
    for (int i = 0; i < total; i++) {
      final int count1 = Long.bitCount(rng.nextLong());
      counts[0] += Long.SIZE - count1;
      counts[1] += count1;
    }

    final double entropy = Entropy.bits(counts);
    Assertions.assertEquals(1, entropy, 0.05, "Entropy should be close to 1 bit");
    Assertions.assertFalse(entropy > 1, "Entropy should not exceed 1 bit");

    Assertions.assertEquals(entropy, Entropy.bits(Arrays.stream(counts).asLongStream().toArray()),
        "Should be the same when using long[] counts");
    Assertions.assertEquals(entropy,
        Entropy.bits(Arrays.stream(counts).asDoubleStream().map(p -> p / (total * 64.0)).toArray()),
        1e-6, "Should be close when using double[] counts");

    final byte[] bytes = new byte[total * 8];
    SplitMix.new64(seed).nextBytes(bytes);
    final EntropyDigest be = Entropy.createDigest(true);
    for (final byte bi : bytes) {
      be.add(bi);
    }
    Assertions.assertEquals(entropy, be.bits(), "Should be the same when using stream of bytes");

    be.reset();
    rng = SplitMix.new64(seed);
    for (int i = 0; i < total; i++) {
      be.add(rng.nextLong());
    }
    Assertions.assertEquals(entropy, be.bits(), "Should be the same when using stream of longs");

    be.reset();
    rng = SplitMix.new64(seed);
    for (int i = 0; i < total; i++) {
      final long value = rng.nextLong();
      be.add(NumberFactory.extractHi(value));
      be.add(NumberFactory.extractLo(value));
    }
    Assertions.assertEquals(entropy, be.bits(), "Should be the same when using stream of ints");
  }
}
