package uk.ac.sussex.gdsc.core.utils.rng;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.core.source64.SplitMix64;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("javadoc")
public class RngUtilsTest {

  private static class DummyRng extends SplitMix64 {
    final double next;

    public DummyRng(double next) {
      super(0L);
      this.next = next;
    }

    @Override
    public double nextDouble() {
      return next;
    }
  }

  @Test
  public void testNextDouble() {
    final double min = 5.12;
    final double max = 7.34;

    final UniformRandomProvider low = new DummyRng(0);
    final UniformRandomProvider high = new DummyRng(1);

    Assertions.assertEquals(min, RngUtils.nextDouble(low, min, max));
    Assertions.assertEquals(max, RngUtils.nextDouble(high, min, max));

    Assertions.assertEquals(max, RngUtils.nextDouble(low, max, min));
    Assertions.assertEquals(min, RngUtils.nextDouble(high, max, min));
  }
}
