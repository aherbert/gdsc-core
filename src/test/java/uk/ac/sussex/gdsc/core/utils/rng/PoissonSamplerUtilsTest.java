package uk.ac.sussex.gdsc.core.utils.rng;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.DiscreteSampler;
import org.apache.commons.rng.sampling.distribution.PoissonSampler;
import org.apache.commons.rng.sampling.distribution.SharedStateDiscreteSampler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("javadoc")
public class PoissonSamplerUtilsTest {
  @Test
  public void testCreatePoissonSamplerWithMeanZero() {
    final UniformRandomProvider rng = SplitMix.new64(0);
    final SharedStateDiscreteSampler sampler = PoissonSamplerUtils.createPoissonSampler(rng, 0);
    for (int i = 0; i < 10; i++) {
      Assertions.assertEquals(0, sampler.sample());
    }
    Assertions.assertSame(sampler, sampler.withUniformRandomProvider(SplitMix.new64(99)));
  }

  @Test
  public void testNextPoissonSampleWithMeanZero() {
    final UniformRandomProvider rng = SplitMix.new64(0);
    Assertions.assertEquals(0, PoissonSamplerUtils.nextPoissonSample(rng, 0));
  }

  @Test
  public void testCreatePoissonSampler() {
    final UniformRandomProvider rng1 = SplitMix.new64(0);
    final UniformRandomProvider rng2 = SplitMix.new64(0);
    final double mean = 3.456;
    final DiscreteSampler sampler1 = new PoissonSampler(rng1, mean);
    final DiscreteSampler sampler2 = PoissonSamplerUtils.createPoissonSampler(rng2, mean);
    for (int i = 0; i < 10; i++) {
      Assertions.assertEquals(sampler1.sample(), sampler2.sample());
    }
  }

  @Test
  public void testNextPoissonSample() {
    final UniformRandomProvider rng1 = SplitMix.new64(0);
    final UniformRandomProvider rng2 = SplitMix.new64(0);
    final double mean = 3.456;
    final DiscreteSampler sampler1 = new PoissonSampler(rng1, mean);
    for (int i = 0; i < 10; i++) {
      Assertions.assertEquals(sampler1.sample(), PoissonSamplerUtils.nextPoissonSample(rng2, mean));
    }
  }
}
