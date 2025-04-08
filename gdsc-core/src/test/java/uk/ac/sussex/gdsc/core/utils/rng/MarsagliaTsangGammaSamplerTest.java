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
 * Copyright (C) 2011 - 2025 Alex Herbert
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

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.AhrensDieterMarsagliaTsangGammaSampler;
import org.apache.commons.rng.sampling.distribution.ContinuousSampler;
import org.junit.jupiter.api.Assertions;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

@SuppressWarnings("javadoc")
class MarsagliaTsangGammaSamplerTest {
  @SeededTest
  void testGammaSampler(RandomSeed seed) {
    final UniformRandomProvider rng1 = RngFactory.create(seed.get());
    final UniformRandomProvider rng2 = RngFactory.create(seed.get());
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
  void testGammaSamplerAtShapeLimit(RandomSeed seed) {
    final UniformRandomProvider rng1 = RngFactory.create(seed.get());
    final UniformRandomProvider rng2 = RngFactory.create(seed.get());
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
  void testGammaSamplerUseProperties(RandomSeed seed) {
    final UniformRandomProvider rng1 = RngFactory.create(seed.get());
    final UniformRandomProvider rng2 = RngFactory.create(seed.get());
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
