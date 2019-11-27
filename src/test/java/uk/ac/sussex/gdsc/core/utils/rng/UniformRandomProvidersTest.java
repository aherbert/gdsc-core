package uk.ac.sussex.gdsc.core.utils.rng;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("javadoc")
public class UniformRandomProvidersTest {
  @Test
  public void testCreateUniformRandomProvider() {
    Assertions.assertNotNull(UniformRandomProviders.create(0));
  }

  @Test
  public void testCreateSplittableUniformRandomProvider() {
    Assertions.assertNotNull(UniformRandomProviders.createSplittable(0));
  }

  @Test
  public void testCreateJumpableUniformRandomProvider() {
    Assertions.assertNotNull(UniformRandomProviders.createJumpable(0));
  }

  @Test
  public void testCreateLongJumpableUniformRandomProvider() {
    Assertions.assertNotNull(UniformRandomProviders.createLongJumpable(0));
  }
}
