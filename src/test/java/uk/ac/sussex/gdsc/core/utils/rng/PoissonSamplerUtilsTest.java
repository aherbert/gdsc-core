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

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.DiscreteSampler;
import org.apache.commons.rng.sampling.distribution.PoissonSampler;
import org.apache.commons.rng.sampling.distribution.SharedStateDiscreteSampler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("javadoc")
class PoissonSamplerUtilsTest {
  @Test
  void testCreatePoissonSamplerWithMeanZero() {
    final UniformRandomProvider rng = SplitMix.new64(0);
    final SharedStateDiscreteSampler sampler = PoissonSamplerUtils.createPoissonSampler(rng, 0);
    for (int i = 0; i < 10; i++) {
      Assertions.assertEquals(0, sampler.sample());
    }
    Assertions.assertSame(sampler, sampler.withUniformRandomProvider(SplitMix.new64(99)));
  }

  @Test
  void testNextPoissonSampleWithMeanZero() {
    final UniformRandomProvider rng = SplitMix.new64(0);
    Assertions.assertEquals(0, PoissonSamplerUtils.nextPoissonSample(rng, 0));
  }

  @Test
  void testCreatePoissonSampler() {
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
  void testNextPoissonSample() {
    final UniformRandomProvider rng1 = SplitMix.new64(0);
    final UniformRandomProvider rng2 = SplitMix.new64(0);
    final double mean = 3.456;
    final DiscreteSampler sampler1 = new PoissonSampler(rng1, mean);
    for (int i = 0; i < 10; i++) {
      Assertions.assertEquals(sampler1.sample(), PoissonSamplerUtils.nextPoissonSample(rng2, mean));
    }
  }
}
