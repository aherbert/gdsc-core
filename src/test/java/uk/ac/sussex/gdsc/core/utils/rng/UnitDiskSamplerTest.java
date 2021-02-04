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

import java.util.Arrays;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("javadoc")
class UnitDiskSamplerTest {
  @Test
  void testSamples() {
    final UniformRandomProvider rng = SplitMix.new64(0);
    final UnitDiskSampler s = UnitDiskSampler.of(rng);
    final UnitDiskSampler s2 = s.withUniformRandomProvider(rng);
    // No statistical test. Just verify within a circle.
    for (int i = 0; i < 100; i++) {
      assertSample(s.sample());
      assertSample(s2.sample());
    }
  }

  private static void assertSample(double[] p) {
    Assertions.assertTrue(Math.sqrt(p[0] * p[0] + p[1] * p[1]) <= 1.0, () -> Arrays.toString(p));
  }
}
