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

package uk.ac.sussex.gdsc.core.math.interpolation;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link CubicSplinePosition}.
 */
@SuppressWarnings({"javadoc"})
class CubicSplinePositionTest {
  @Test
  void testConstructor() {
    final double x = 0.5;
    Assertions.assertNotNull(new CubicSplinePosition(x));

    Assertions.assertThrows(OutOfRangeException.class, () -> {
      @SuppressWarnings("unused")
      final CubicSplinePosition p = new CubicSplinePosition(-1e-6);
    });
    Assertions.assertThrows(OutOfRangeException.class, () -> {
      @SuppressWarnings("unused")
      final CubicSplinePosition p = new CubicSplinePosition(1.000001);
    });
    Assertions.assertThrows(OutOfRangeException.class, () -> {
      @SuppressWarnings("unused")
      final CubicSplinePosition p = new CubicSplinePosition(Double.NaN);
    });
  }

  @Test
  void testProperties() {
    for (int i = 0; i <= 5; i++) {
      final double x = (double) i / 5;
      final CubicSplinePosition p = new CubicSplinePosition(x);
      Assertions.assertNotNull(p);
      final double x2 = x * x;
      final double x3 = x * x2;
      Assertions.assertEquals(x, p.getX());
      Assertions.assertEquals(x2, p.getX2());
      Assertions.assertEquals(x3, p.getX3());
    }
  }
}
