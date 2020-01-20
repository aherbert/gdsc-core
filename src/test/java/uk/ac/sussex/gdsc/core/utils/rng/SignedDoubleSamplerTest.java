package uk.ac.sussex.gdsc.core.utils.rng;

import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.SharedStateContinuousSampler;
import org.junit.jupiter.api.Assertions;

@SuppressWarnings("javadoc")
public class SignedDoubleSamplerTest {
  @SeededTest
  public void testSampler(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final UniformRandomProvider rng1 = RngUtils.create(seed.getSeed());
    final UniformRandomProvider rng2 = RngUtils.create(seed.getSeed());
    final SharedStateContinuousSampler sampler1 = new SignedDoubleSampler(rng1);
    final SharedStateContinuousSampler sampler2 = sampler1.withUniformRandomProvider(rng2);
    Assertions.assertNotSame(sampler1, sampler2);
    for (int i = 0; i < 10; i++) {
      final double expected = NumberUtils.makeSignedDouble(rng.nextLong());
      Assertions.assertEquals(expected, sampler1.sample());
      Assertions.assertEquals(expected, sampler2.sample());
    }
  }
}
