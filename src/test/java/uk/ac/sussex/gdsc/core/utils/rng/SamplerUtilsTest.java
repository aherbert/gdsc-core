package uk.ac.sussex.gdsc.core.utils.rng;

import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.core.source64.SplitMix64;
import org.apache.commons.rng.sampling.distribution.ContinuousSampler;
import org.apache.commons.rng.sampling.distribution.DiscreteSampler;
import org.apache.commons.rng.sampling.distribution.SharedStateContinuousSampler;
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

  @Test
  public void testCreateGaussianSampler() {
    final UniformRandomProvider rng = new SplitMix64(0L);
    final double mean = 1.23;
    final double standardDeviation = 4.56;
    final SharedStateContinuousSampler sampler =
        SamplerUtils.createGaussianSampler(rng, mean, standardDeviation);
    Assertions.assertNotNull(sampler);
  }

  @Test
  public void testCreateGammaSampler() {
    final UniformRandomProvider rng = new SplitMix64(0L);
    final double shape = 1.23;
    final double scale = 4.56;
    final ContinuousSampler sampler = SamplerUtils.createGammaSampler(rng, shape, scale);
    Assertions.assertNotNull(sampler);
  }

  @Test
  public void testCreateBinomialSampler() {
    final UniformRandomProvider rng = new SplitMix64(0L);
    final int trials = 14;
    final double probabilityOfSuccess = 0.789;
    final DiscreteSampler sampler =
        SamplerUtils.createBinomialSampler(rng, trials, probabilityOfSuccess);
    Assertions.assertNotNull(sampler);
  }
}
