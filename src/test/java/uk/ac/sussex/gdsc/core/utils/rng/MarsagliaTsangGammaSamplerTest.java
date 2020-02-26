package uk.ac.sussex.gdsc.core.utils.rng;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.AhrensDieterMarsagliaTsangGammaSampler;
import org.apache.commons.rng.sampling.distribution.ContinuousSampler;
import org.junit.jupiter.api.Assertions;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

@SuppressWarnings("javadoc")
public class MarsagliaTsangGammaSamplerTest {
  @SeededTest
  public void testGammaSampler(RandomSeed seed) {
    final UniformRandomProvider rng1 = RngUtils.create(seed.getSeed());
    final UniformRandomProvider rng2 = RngUtils.create(seed.getSeed());
    final double shape = 6.11;
    final double scale = 4.23;
    // Test against the source implementation.
    final ContinuousSampler sampler1 =
        AhrensDieterMarsagliaTsangGammaSampler.of(rng1, shape, scale);
    final MarsagliaTsangGammaSampler sampler2 = new MarsagliaTsangGammaSampler(rng2, shape, scale);
    for (int i = 0; i < 10; i++) {
      Assertions.assertEquals(sampler1.sample(), sampler2.sample());
    }
  }

  @SeededTest
  public void testGammaSamplerAtShapeLimit(RandomSeed seed) {
    final UniformRandomProvider rng1 = RngUtils.create(seed.getSeed());
    final UniformRandomProvider rng2 = RngUtils.create(seed.getSeed());
    final double shape = 1.0;
    final double scale = 4.23;
    // Test against the source implementation.
    final ContinuousSampler sampler1 =
        AhrensDieterMarsagliaTsangGammaSampler.of(rng1, shape, scale);
    final MarsagliaTsangGammaSampler sampler2 = new MarsagliaTsangGammaSampler(rng2, shape, scale);
    // Run for a long time it may hit the edge case for (v <= 0) in the sampler method.
    for (int i = 0; i < 100; i++) {
      Assertions.assertEquals(sampler1.sample(), sampler2.sample());
    }
  }

  @SeededTest
  public void testGammaSamplerUseProperties(RandomSeed seed) {
    final UniformRandomProvider rng1 = RngUtils.create(seed.getSeed());
    final UniformRandomProvider rng2 = RngUtils.create(seed.getSeed());
    final double shape = 6.11;
    final double scale = 4.23;
    // Test against the source implementation.
    // In v1.2 the parameters were in the incorrect order. This should be updated for v1.3.
    final MarsagliaTsangGammaSampler sampler1 = new MarsagliaTsangGammaSampler(rng1, shape, scale);
    final MarsagliaTsangGammaSampler sampler2 = new MarsagliaTsangGammaSampler(rng2, 0, 0);
    sampler2.setAlpha(shape);
    sampler2.setTheta(scale);
    for (int i = 0; i < 10; i++) {
      Assertions.assertEquals(sampler1.sample(), sampler2.sample());
    }
  }
}
