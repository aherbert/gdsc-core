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

import org.apache.commons.math3.exception.OutOfRangeException;

/**
 * Contains the cubic spline position for a value within the interpolation range. Used to
 * pre-compute values to evaluate the spline value.
 *
 * <p>This class is immutable.
 */
public class CubicSplinePosition {
  /** The value x^1. */
  final double x1;
  /** The value x^2. */
  final double x2;
  /** The value x^3. */
  final double x3;

  /**
   * Create a new instance. Only used when x is known to be in the range {@code [0,1]}.
   *
   * @param x the x
   * @param dummy the dummy flag (this is unused)
   */
  CubicSplinePosition(double x, boolean dummy) {
    x1 = x;
    x2 = x * x;
    x3 = x2 * x;
  }

  /**
   * Create a new instance.
   *
   * @param x the distance along the spline to the next node (range {@code [0,1]})
   * @throws IllegalArgumentException If the index is negative
   * @throws OutOfRangeException If x is not in the range {@code [0,1]}
   */
  public CubicSplinePosition(double x) {
    // Use negation to catch NaN
    if (!(x >= 0 && x <= 1)) {
      throw new OutOfRangeException(x, 0, 1);
    }
    x1 = x;
    x2 = x * x;
    x3 = x2 * x;
  }

  /**
   * Gets x.
   *
   * @return x
   */
  public double getX() {
    return x1;
  }

  /**
   * Gets x^2.
   *
   * @return x^2
   */
  public double getX2() {
    return x2;
  }

  /**
   * Gets x^3.
   *
   * @return x^3
   */
  public double getX3() {
    return x3;
  }
}
