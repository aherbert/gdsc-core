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

package uk.ac.sussex.gdsc.core.math.interpolation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link IndexedCubicSplinePosition}.
 */
@SuppressWarnings({"javadoc"})
class IndexedCubicSplinePositionTest {
  // Note: Avoids testing the super-class methods again. Only those new to this
  // class.

  @Test
  void testConstructor() {
    final int index = 0;
    final double x = 0.5;
    Assertions.assertNotNull(new IndexedCubicSplinePosition(index, x));
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      @SuppressWarnings("unused")
      final IndexedCubicSplinePosition p = new IndexedCubicSplinePosition(-1, x);
    });
  }

  @Test
  void testProperties() {
    final double x = 0.5;
    for (int i = 0; i <= 5; i++) {
      final IndexedCubicSplinePosition p = new IndexedCubicSplinePosition(i, x);
      Assertions.assertNotNull(p);
      Assertions.assertEquals(i, p.index);
    }
  }
}
