package uk.ac.sussex.gdsc.core.utils;

import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;

@SuppressWarnings({"javadoc"})
public class RandomUtilsTest {
  @SeededTest
  public void canComputeSample(RandomSeed seed) {
    final int[] set = new int[] {0, 1, 2, 5, 8, 9, 10};
    final UniformRandomProvider rng = RngUtils.create(seed.getSeedAsLong());
    for (final int total : set) {
      for (final int size : set) {
        canComputeSample(rng, size, total);
      }
    }
  }

  private static void canComputeSample(UniformRandomProvider rng, int size, int total) {
    final int[] sample = RandomUtils.sample(size, total, rng);
    // TestLog.debug(logger,"%d from %d = %s", k, n, java.util.Arrays.toString(sample));
    Assertions.assertEquals(Math.min(size, total), sample.length);
    for (int i = 0; i < sample.length; i++) {
      for (int j = i + 1; j < sample.length; j++) {
        Assertions.assertNotEquals(sample[i], sample[j]);
      }
    }
  }

  @SeededTest
  public void canComputeSampleFromBigData(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeedAsLong());
    final int total = 100;
    for (final int size : new int[] {0, 1, 2, total / 2, total - 2, total - 1, total}) {
      canComputeSample(rng, size, total);
    }
  }

  @SeededTest
  public void canGenerateDoubles(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeedAsLong());
    final int n = 13;
    final double[] data = RandomUtils.generate(n, rng);
    Assertions.assertEquals(n, data.length);
    for (int i = 0; i < n; i++) {
      Assertions.assertTrue(data[i] >= 0 && data[i] <= 1.0);
    }
  }
}
