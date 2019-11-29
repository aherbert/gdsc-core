package uk.ac.sussex.gdsc.core.utils.rng;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongFunction;

@SuppressWarnings("javadoc")
public class UniformRandomProvidersTest {
  private static final long SEED = ThreadLocalRandom.current().nextLong();

  @Test
  public void testCreateUniformRandomProvider() {
    Assertions.assertNotNull(UniformRandomProviders.create());
    assertSameOutput(UniformRandomProviders::create);
  }

  @Test
  public void testCreateSplittableUniformRandomProvider() {
    Assertions.assertNotNull(UniformRandomProviders.createSplittable());
    assertSameOutput(UniformRandomProviders::createSplittable);
  }

  @Test
  public void testCreateJumpableUniformRandomProvider() {
    Assertions.assertNotNull(UniformRandomProviders.createJumpable());
    assertSameOutput(UniformRandomProviders::createJumpable);
  }

  @Test
  public void testCreateLongJumpableUniformRandomProvider() {
    Assertions.assertNotNull(UniformRandomProviders.createLongJumpable());
    assertSameOutput(UniformRandomProviders::createLongJumpable);
  }

  private static void assertSameOutput(LongFunction<UniformRandomProvider> factory) {
    final UniformRandomProvider rng1 = factory.apply(SEED);
    final UniformRandomProvider rng2 = factory.apply(SEED);
    for (int i = 0; i < 10; i++) {
      Assertions.assertEquals(rng1.nextLong(), rng2.nextLong(),
          "Two instances with same seed do not produce equal output");
    }
  }
}
