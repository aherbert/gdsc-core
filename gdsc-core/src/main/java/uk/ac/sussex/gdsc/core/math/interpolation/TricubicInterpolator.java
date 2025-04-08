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
 * Copyright (C) 2011 - 2025 Alex Herbert
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

/**
 * Tricubic Interpolator using the Catmull-Rom spline.
 *
 * <p>Taken from <a href="http://www.paulinternet.nl/?page=bicubic">Cubic interpolation</a>.
 */
public class TricubicInterpolator extends BicubicInterpolator {

  /**
   * Instantiates a new tricubic interpolator.
   */
  protected TricubicInterpolator() {
    super();
  }

  /**
   * Gets the interpolated value.
   *
   * @param values the values of the function at x=-1 to x=2; y=-1 to y=2; and z=-1 to z=2
   * @param x the x (between 0 and 1)
   * @param y the y (between 0 and 1)
   * @param z the z (between 0 and 1)
   * @return the interpolated value
   */
  public double getValue(double[][][] values, double x, double y, double z) {
    final double[] arr = new double[4];
    arr[0] = getValue(values[0], y, z);
    arr[1] = getValue(values[1], y, z);
    arr[2] = getValue(values[2], y, z);
    arr[3] = getValue(values[3], y, z);
    return getValue(arr, x);
  }
}
