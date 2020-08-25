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

package uk.ac.sussex.gdsc.core.clustering.optics;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

@SuppressWarnings({"javadoc"})
class MinMax2dTest {
  @Test
  void testEmpty() {
    Assertions.assertNull(new MinMax2d().getBounds());
  }

  @SeededTest
  void testBounds(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final MinMax2d mm = new MinMax2d();
    for (final int n : new int[] {1, 5, 10}) {
      final float[] x = new float[n];
      final float[] y = new float[n];
      mm.clear();
      for (int i = 0; i < n; i++) {
        final float xx = rng.nextFloat();
        final float yy = rng.nextFloat();
        mm.add(xx, yy);
        x[i] = xx;
        y[i] = yy;
      }
      final float[] bounds = mm.getBounds();
      Assertions.assertEquals(MathUtils.min(x), bounds[0]);
      Assertions.assertEquals(MathUtils.max(x), bounds[1]);
      Assertions.assertEquals(MathUtils.min(y), bounds[2]);
      Assertions.assertEquals(MathUtils.max(y), bounds[3]);
    }
  }
}
