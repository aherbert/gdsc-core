/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2023 Alex Herbert
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package uk.ac.sussex.gdsc.core.utils.rng;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongFunction;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("javadoc")
class UniformRandomProvidersTest {
  private static final long SEED = ThreadLocalRandom.current().nextLong();

  @Test
  void testCreateUniformRandomProvider() {
    Assertions.assertNotNull(UniformRandomProviders.create());
    assertSameOutput(UniformRandomProviders::create);
  }

  @Test
  void testCreateSplittableUniformRandomProvider() {
    Assertions.assertNotNull(UniformRandomProviders.createSplittable());
    assertSameOutput(UniformRandomProviders::createSplittable);
  }

  @Test
  void testCreateJumpableUniformRandomProvider() {
    Assertions.assertNotNull(UniformRandomProviders.createJumpable());
    assertSameOutput(UniformRandomProviders::createJumpable);
  }

  @Test
  void testCreateLongJumpableUniformRandomProvider() {
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
