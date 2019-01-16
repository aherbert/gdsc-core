package uk.ac.sussex.gdsc.core.utils.rng;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.core.source64.SplitMix64;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("javadoc")
public class RngUtilsTest {

  private static class DummyDoubleRng extends SplitMix64 {
    final double next;

    public DummyDoubleRng(double next) {
      super(0L);
      this.next = next;
    }

    @Override
    public double nextDouble() {
      return next;
    }
  }

  private static class DummyIntRng extends SplitMix64 {
    final int next;

    public DummyIntRng(int next) {
      super(0L);
      this.next = next;
    }

    @Override
    public int nextInt(int n) {
      return next;
    }
  }

  @Test
  public void testNextDouble() {
    final double min = 5.12;
    final double max = 7.34;

    final UniformRandomProvider low = new DummyDoubleRng(0);
    final UniformRandomProvider high = new DummyDoubleRng(1);

    Assertions.assertEquals(min, RngUtils.nextDouble(low, min, max));
    Assertions.assertEquals(max, RngUtils.nextDouble(high, min, max));

    Assertions.assertEquals(max, RngUtils.nextDouble(low, max, min));
    Assertions.assertEquals(min, RngUtils.nextDouble(high, max, min));
  }

  @Test
  public void testNextInt() {
    final int min = 5;
    final int max = 7;

    final UniformRandomProvider low = new DummyIntRng(0);
    final UniformRandomProvider high = new DummyIntRng(max - min - 1);

    Assertions.assertEquals(min, RngUtils.nextInt(low, min, max));
    Assertions.assertEquals(max - 1, RngUtils.nextInt(high, min, max));

    final UniformRandomProvider rng = new SplitMix64(1L);
    Assertions.assertThrows(IllegalArgumentException.class, () -> RngUtils.nextInt(rng, max, min),
        "Expected max < min to be an error");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> RngUtils.nextInt(rng, Integer.MIN_VALUE, 0), "Expected overflow to be an error");
  }
}
