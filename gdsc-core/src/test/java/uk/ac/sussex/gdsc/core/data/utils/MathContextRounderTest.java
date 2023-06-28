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
 * Copyright (C) 2011 - 2023 Alex Herbert
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

package uk.ac.sussex.gdsc.core.data.utils;

import java.math.MathContext;
import java.math.RoundingMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class MathContextRounderTest {

  @Test
  void testMathContextRounderWithPrecision() {
    final Rounder r = new MathContextRounder(3);
    Assertions.assertEquals(1.23, r.round(1.2345));
    Assertions.assertEquals(String.valueOf("1.23"), r.toString(1.2345));
    Assertions.assertEquals(4.57f, r.round(4.5678f));
    Assertions.assertEquals(String.valueOf("4.57"), r.toString(4.5678f));
  }

  @Test
  void testMathContextRounderWithMathContext() {
    Assertions.assertThrows(NullPointerException.class, () -> new MathContextRounder(null));
    final Rounder r = new MathContextRounder(new MathContext(2, RoundingMode.CEILING));
    Assertions.assertEquals(1.3, r.round(1.2345));
    Assertions.assertEquals(String.valueOf("1.3"), r.toString(1.2345));
    Assertions.assertEquals(4.4f, r.round(4.32f));
    Assertions.assertEquals(String.valueOf("4.4"), r.toString(4.32f));
  }

  @Test
  void testMathContextRounderWithNaN() {
    final Rounder r = new MathContextRounder(3);
    Assertions.assertEquals(Double.NaN, r.round(Double.NaN));
    Assertions.assertEquals(String.valueOf("NaN"), r.toString(Double.NaN));
    Assertions.assertEquals(Float.NaN, r.round(Float.NaN));
    Assertions.assertEquals(String.valueOf("NaN"), r.toString(Float.NaN));
  }

  @Test
  void testMathContextRounderWithInfinite() {
    final Rounder r = new MathContextRounder(3);
    Assertions.assertEquals(Double.POSITIVE_INFINITY, r.round(Double.POSITIVE_INFINITY));
    Assertions.assertEquals(String.valueOf("Infinity"), r.toString(Double.POSITIVE_INFINITY));
    Assertions.assertEquals(Float.POSITIVE_INFINITY, r.round(Float.POSITIVE_INFINITY));
    Assertions.assertEquals(String.valueOf("Infinity"), r.toString(Float.POSITIVE_INFINITY));
    Assertions.assertEquals(Double.NEGATIVE_INFINITY, r.round(Double.NEGATIVE_INFINITY));
    Assertions.assertEquals(String.valueOf("-Infinity"), r.toString(Double.NEGATIVE_INFINITY));
    Assertions.assertEquals(Float.NEGATIVE_INFINITY, r.round(Float.NEGATIVE_INFINITY));
    Assertions.assertEquals(String.valueOf("-Infinity"), r.toString(Float.NEGATIVE_INFINITY));
  }
}
