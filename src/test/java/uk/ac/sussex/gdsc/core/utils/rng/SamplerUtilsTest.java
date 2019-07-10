package uk.ac.sussex.gdsc.core.utils.rng;

import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("javadoc")
public class SamplerUtilsTest {
  @Test
  public void testCreateDiscreteSamples() {
    final int size = 10;
    final int start = 5;
    final int increment = 2;
    final int[] expected = SimpleArrayUtils.newArray(size, start, increment);
    final AtomicInteger count = new AtomicInteger(start);
    final int[] actual = SamplerUtils.createSamples(size, () -> count.getAndAdd(increment));
    Assertions.assertArrayEquals(expected, actual);
  }

  @Test
  public void testCreateContinuousSamples() {
    final int size = 10;
    final double start = 5.43;
    final double increment = 2.11;
    final double[] expected = SimpleArrayUtils.newArray(size, start, increment);
    final AtomicInteger count = new AtomicInteger();
    final double[] actual =
        SamplerUtils.createSamples(size, () -> start + count.getAndIncrement() * increment);
    Assertions.assertArrayEquals(expected, actual);
  }
}
