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

package uk.ac.sussex.gdsc.core.utils;

import org.apache.commons.rng.sampling.distribution.SharedStateContinuousSampler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.NoiseEstimator.Method;
import uk.ac.sussex.gdsc.core.utils.rng.SamplerUtils;
import uk.ac.sussex.gdsc.test.rng.RngFactory;

@SuppressWarnings({"javadoc"})
class NoiseEstimatorTest {
  @Test
  void canGetName() {
    for (final Method m : Method.values()) {
      Assertions.assertEquals(m.toString(), m.getName());
      Assertions.assertNotEquals(m.name(), m.getName());
    }
  }

  @Test
  void testBadDataThrows() {
    final int width = 5;
    final int height = 6;
    final float[] data = new float[width * height];
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> NoiseEstimator.wrap(data, 0, height));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> NoiseEstimator.wrap(data, width, 0));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> NoiseEstimator.wrap(null, width, height));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> NoiseEstimator.wrap(new float[1], width, height));
    // Allowed
    NoiseEstimator.wrap(new float[width * height + 10], width, height);
  }

  @Test
  void testProperties() {
    final int width = 5;
    final int height = 6;
    final float[] data = new float[width * height];
    final NoiseEstimator ne = NoiseEstimator.wrap(data, width, height);
    Assertions.assertNotEquals(0, ne.getRange());
    ne.setRange(0);
    Assertions.assertEquals(1, ne.getRange());
    ne.setRange(4);
    Assertions.assertEquals(4, ne.getRange());
    for (final boolean value : new boolean[] {true, false}) {
      ne.setPreserveResiduals(value);
      Assertions.assertEquals(value, ne.isPreserveResiduals());
    }
  }

  @Test
  void canEstimateNoise() {
    final int width = 50;
    final int height = 60;
    final float[] data = new float[width * height];
    final double mean = 4.56;
    final double standardDeviation = 3.45;
    final SharedStateContinuousSampler sampler =
        SamplerUtils.createGaussianSampler(RngFactory.create(12345L), mean, standardDeviation);
    for (int i = 0; i < data.length; i++) {
      data[i] = (float) sampler.sample();
    }
    final NoiseEstimator ie1 = NoiseEstimator.wrap(data, width, height);
    final NoiseEstimator ie2 = NoiseEstimator.wrap(data, width, height);
    ie1.setRange(10);
    ie1.setPreserveResiduals(false);
    ie2.setRange(10);
    ie2.setPreserveResiduals(true);
    for (final Method m : Method.values()) {
      final double actual1 = ie1.getNoise(m);
      Assertions.assertEquals(standardDeviation, actual1, 1e-1 * standardDeviation, m::toString);
      Assertions.assertEquals(actual1, ie2.getNoise(m), m::toString);
      Assertions.assertEquals(actual1, ie2.getNoise(m), m::toString);
    }
  }

  @Test
  void canEstimateZeroNoise() {
    final int big = 3;
    final float[] data = new float[big * big];
    final NoiseEstimator ie = NoiseEstimator.wrap(data, big, big);
    for (final Method m : Method.values()) {
      Assertions.assertEquals(0, ie.getNoise(m), () -> m.toString());
    }
  }

  /**
   * This test hits edge cases where the noise defaults to zero when it cannot be estimated.
   */
  @Test
  void testNoiseIsZeroWhenCannotComputeResiduals() {
    final int big = 3;
    final int small = 1;
    // Data should have some noise (different values)
    final float[] data = SimpleArrayUtils.newArray(big * big, 0f, 1f);
    final NoiseEstimator ie1 = NoiseEstimator.wrap(data, big, small);
    final NoiseEstimator ie2 = NoiseEstimator.wrap(data, small, big);
    final NoiseEstimator ie3 = NoiseEstimator.wrap(data, small, small);
    for (final Method m : Method.values()) {
      // Only applies to residuals methods
      if (!m.name().contains("RESIDUALS")) {
        continue;
      }
      Assertions.assertEquals(0, ie1.getNoise(m), () -> m.toString());
      Assertions.assertEquals(0, ie2.getNoise(m), () -> m.toString());
      Assertions.assertEquals(0, ie3.getNoise(m), () -> m.toString());
    }
  }
}
