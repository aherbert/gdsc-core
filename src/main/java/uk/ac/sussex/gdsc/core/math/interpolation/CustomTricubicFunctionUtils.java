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

package uk.ac.sussex.gdsc.core.math.interpolation;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Utilities for a 3D-spline function.
 */
public final class CustomTricubicFunctionUtils {
  /**
   * No public construction.
   */
  private CustomTricubicFunctionUtils() {}

  /**
   * Creates the CustomTricubicFunction from the 64 coefficients.
   *
   * <p>Coefficients must be computed as if iterating: z^a * y^b * x^c with a,b,c in [0, 3].
   *
   * @param coefficients the coefficients
   * @return the custom tricubic function
   * @throws IllegalArgumentException if the input array length {@code < 64}
   */
  public static CustomTricubicFunction create(double[] coefficients) {
    checkLength(coefficients);
    return new DoubleCustomTricubicFunction(new DoubleCubicSplineData(coefficients));
  }

  /**
   * Creates the CustomTricubicFunction from the 64 coefficients.
   *
   * <p>Coefficients must be computed as if iterating: z^a * y^b * x^c with a,b,c in [0, 3].
   *
   * @param coefficients the coefficients
   * @return the custom tricubic function
   * @throws IllegalArgumentException if the input array length {@code < 64}
   */
  public static CustomTricubicFunction create(float[] coefficients) {
    checkLength(coefficients);
    return new FloatCustomTricubicFunction(new FloatCubicSplineData(coefficients));
  }

  private static void checkLength(Object coefficients) {
    if (ArrayUtils.getLength(coefficients) < 64) {
      throw new IllegalArgumentException("Require an array of 64 coefficients");
    }
  }
}
