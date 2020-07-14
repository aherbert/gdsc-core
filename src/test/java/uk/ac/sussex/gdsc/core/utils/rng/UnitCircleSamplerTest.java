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
 * Copyright (C) 2011 - 2020 Alex Herbert
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("javadoc")
public class UnitCircleSamplerTest {
  @Test
  public void testSamples() {
    final UniformRandomProvider rng = SplitMix.new64(0);
    final UnitCircleSampler s = UnitCircleSampler.of(rng);
    final UnitCircleSampler s2 = s.withUniformRandomProvider(rng);
    // No statistical test. Just verify within a circle.
    double[] p;
    for (int i = 0; i < 100; i++) {
      p = s.sample();
      Assertions.assertTrue(Math.hypot(p[0], p[1]) <= 1.0);
      p = s2.sample();
      Assertions.assertTrue(Math.hypot(p[0], p[1]) <= 1.0);
    }
  }
}
