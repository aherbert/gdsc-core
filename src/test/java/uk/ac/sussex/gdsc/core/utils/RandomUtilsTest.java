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
    for (final int n : set) {
      for (final int k : set) {
        canComputeSample(rng, k, n);
      }
    }
  }

  @SeededTest
  public void canComputeSampleFromBigData(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeedAsLong());
    final int n = 100;
    for (final int k : new int[] {0, 1, 2, n / 2, n - 2, n - 1, n}) {
      canComputeSample(rng, k, n);
    }
  }

  private static void canComputeSample(UniformRandomProvider rng, int k, int n) {
    final int[] sample = RandomUtils.sample(k, n, rng);
    // TestLog.debug(logger,"%d from %d = %s", k, n, java.util.Arrays.toString(sample));
    Assertions.assertEquals(Math.min(k, n), sample.length);
    for (int i = 0; i < sample.length; i++) {
      for (int j = i + 1; j < sample.length; j++) {
        Assertions.assertNotEquals(sample[i], sample[j]);
      }
    }
  }
}
