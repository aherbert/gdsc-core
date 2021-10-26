/*-
 * #%L
 * Genome Damage and Stability Centre ImageJ Core Package
 *
 * Contains code used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2021 Alex Herbert
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

import org.apache.commons.rng.sampling.distribution.ContinuousUniformSampler;
import org.apache.commons.rng.sampling.distribution.DiscreteUniformSampler;
import org.apache.commons.rng.sampling.distribution.SharedStateContinuousSampler;
import org.apache.commons.rng.sampling.distribution.SharedStateDiscreteSampler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("javadoc")
class SplittablesTest {
  @Test
  void testOfInt() {
    final long seed = 12345;
    final int lower = 43;
    final int upper = 678;
    final SplittableUniformRandomProvider rng1 = UniformRandomProviders.createSplittable(seed);
    final SplittableUniformRandomProvider rng2 = UniformRandomProviders.createSplittable(seed);
    final SplittableUniformRandomProvider rng3 = UniformRandomProviders.createSplittable(seed);
    SharedStateDiscreteSampler sampler1 = DiscreteUniformSampler.of(rng1, lower, upper);
    SplittableIntSupplier sampler2 =
        Splittables.ofInt(rng2, DiscreteUniformSampler.of(rng2, lower, upper));
    SplittableIntSupplier sampler3 =
        Splittables.ofInt(rng3, (rng) -> DiscreteUniformSampler.of(rng, lower, upper));

    // Test the same
    for (int i = 0; i < 10; i++) {
      final int expected = sampler1.sample();
      Assertions.assertEquals(expected, sampler2.getAsInt());
      Assertions.assertEquals(expected, sampler3.getAsInt());
    }

    // Split
    sampler1 = sampler1.withUniformRandomProvider(rng1.split());
    sampler2 = sampler2.split();
    sampler3 = sampler3.split();
    for (int i = 0; i < 10; i++) {
      final int expected = sampler1.sample();
      Assertions.assertEquals(expected, sampler2.getAsInt());
      Assertions.assertEquals(expected, sampler3.getAsInt());
    }
  }

  @Test
  void testOfDouble() {
    final long seed = 12345;
    final double lower = 43;
    final double upper = 678;
    final SplittableUniformRandomProvider rng1 = UniformRandomProviders.createSplittable(seed);
    final SplittableUniformRandomProvider rng2 = UniformRandomProviders.createSplittable(seed);
    final SplittableUniformRandomProvider rng3 = UniformRandomProviders.createSplittable(seed);
    SharedStateContinuousSampler sampler1 = ContinuousUniformSampler.of(rng1, lower, upper);
    SplittableDoubleSupplier sampler2 =
        Splittables.ofDouble(rng2, ContinuousUniformSampler.of(rng2, lower, upper));
    SplittableDoubleSupplier sampler3 =
        Splittables.ofDouble(rng3, (rng) -> ContinuousUniformSampler.of(rng, lower, upper));

    // Test the same
    for (int i = 0; i < 10; i++) {
      final double expected = sampler1.sample();
      Assertions.assertEquals(expected, sampler2.getAsDouble());
      Assertions.assertEquals(expected, sampler3.getAsDouble());
    }

    // Split
    sampler1 = sampler1.withUniformRandomProvider(rng1.split());
    sampler2 = sampler2.split();
    sampler3 = sampler3.split();
    for (int i = 0; i < 10; i++) {
      final double expected = sampler1.sample();
      Assertions.assertEquals(expected, sampler2.getAsDouble());
      Assertions.assertEquals(expected, sampler3.getAsDouble());
    }
  }
}
