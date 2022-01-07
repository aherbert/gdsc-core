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
 * Copyright (C) 2011 - 2022 Alex Herbert
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

import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.core.source64.SplitMix64;
import org.apache.commons.rng.sampling.distribution.ContinuousSampler;
import org.apache.commons.rng.sampling.distribution.DiscreteSampler;
import org.apache.commons.rng.sampling.distribution.SharedStateContinuousSampler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;

@SuppressWarnings("javadoc")
class SamplerUtilsTest {
  @Test
  void testCreateDiscreteSamples() {
    final int size = 10;
    final int start = 5;
    final int increment = 2;
    final int[] expected = SimpleArrayUtils.newArray(size, start, increment);
    final AtomicInteger count = new AtomicInteger(start);
    final int[] actual = SamplerUtils.createSamples(size, () -> count.getAndAdd(increment));
    Assertions.assertArrayEquals(expected, actual);
  }

  @Test
  void testCreateContinuousSamples() {
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
  void testCreateGaussianSampler() {
    final UniformRandomProvider rng = new SplitMix64(0L);
    final double mean = 1.23;
    final double standardDeviation = 4.56;
    final SharedStateContinuousSampler sampler =
        SamplerUtils.createGaussianSampler(rng, mean, standardDeviation);
    Assertions.assertNotNull(sampler);
  }

  @Test
  void testCreateGammaSampler() {
    final UniformRandomProvider rng = new SplitMix64(0L);
    final double shape = 1.23;
    final double scale = 4.56;
    final ContinuousSampler sampler = SamplerUtils.createGammaSampler(rng, shape, scale);
    Assertions.assertNotNull(sampler);
  }

  @Test
  void testCreateBinomialSampler() {
    final UniformRandomProvider rng = new SplitMix64(0L);
    final int trials = 14;
    final double probabilityOfSuccess = 0.789;
    final DiscreteSampler sampler =
        SamplerUtils.createBinomialSampler(rng, trials, probabilityOfSuccess);
    Assertions.assertNotNull(sampler);
  }

  @Test
  void testCreateGeometricSampler() {
    final UniformRandomProvider rng = new SplitMix64(0L);
    final double probabilityOfSuccess = 0.789;
    final DiscreteSampler sampler = SamplerUtils.createGeometricSampler(rng, probabilityOfSuccess);
    Assertions.assertNotNull(sampler);
  }

  @Test
  void testCreateGeometricSamplerFromMean() {
    final UniformRandomProvider rng = new SplitMix64(0L);
    final double mean = 456.322;
    final DiscreteSampler sampler = SamplerUtils.createGeometricSamplerFromMean(rng, mean);
    Assertions.assertNotNull(sampler);
  }

  @Test
  void testCreateExponentialSampler() {
    final UniformRandomProvider rng = new SplitMix64(0L);
    final ContinuousSampler sampler = SamplerUtils.createExponentialSampler(rng);
    Assertions.assertNotNull(sampler);
  }

  @Test
  void testCreateExponentialSamplerWithMean() {
    final UniformRandomProvider rng = new SplitMix64(0L);
    final double mean = 3.45;
    final ContinuousSampler sampler = SamplerUtils.createExponentialSampler(rng, mean);
    Assertions.assertNotNull(sampler);
  }
}
