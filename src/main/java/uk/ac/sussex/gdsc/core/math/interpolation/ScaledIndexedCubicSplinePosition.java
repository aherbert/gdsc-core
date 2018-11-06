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
 * Copyright (C) 2011 - 2018 Alex Herbert
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
 */
public class ScaledIndexedCubicSplinePosition extends IndexedCubicSplinePosition {
  /** The scale factor used to compress the original value to the range 0-1. */
  private final double scaleFactor;

  /**
   * Instantiates a new indexed cubic spline position. Only used when x is known to be in the range
   * 0-1 and the index is positive.
   *
   * @param index the index
   * @param x the x
   * @param scaleFactor the scale factor used to compress the original value to the range 0-1
   * @param dummy the dummy flag
   */
  ScaledIndexedCubicSplinePosition(int index, double x, double scaleFactor, boolean dummy) {
    super(index, x, dummy);
    this.scaleFactor = scaleFactor;
  }

  /**
   * Instantiates a new spline position.
   *
   * @param index the index
   * @param x the distance along the spline to the next node (range 0 to 1)
   * @param scale the scale used to compress the original value to the range 0-1
   * @throws IllegalArgumentException If the index is negative or the scale is negative or zero
   * @throws OutOfRangeException If x is not in the range 0 to 1
   */
  public ScaledIndexedCubicSplinePosition(int index, double x, double scale) {
    super(index, x);
    if (!Double.isFinite(scale) || scale <= 0) {
      throw new IllegalArgumentException("Scale must be strictly positive");
    }
    this.scaleFactor = scale;
  }

  @Override
  public double scale(double x) {
    return x * getScaleFactor();
  }

  @Override
  public double scaleGradient(double derivative1) {
    return derivative1 / getScaleFactor();
  }

  @Override
  public double scaleGradient2(double derivative2) {
    return derivative2 / getScaleFactor() / getScaleFactor();
  }

  /**
   * Gets the scale factor used to compress the original value to the range 0-1.
   *
   * @return the scale factor
   */
  public double getScaleFactor() {
    return scaleFactor;
  }
}
