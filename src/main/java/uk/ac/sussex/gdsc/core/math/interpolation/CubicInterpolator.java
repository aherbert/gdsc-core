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

/**
 * Cubic Interpolator using the Catmull-Rom spline.
 *
 * <p>Taken from <a href="http://www.paulinternet.nl/?page=bicubic">Cubic interpolation</a>.
 */
public class CubicInterpolator {

  /**
   * Instantiates a new cubic interpolator.
   */
  protected CubicInterpolator() {
    // Do nothing
  }

  /**
   * Gets the interpolated value.
   *
   * @param values the values of the function at x=-1, x=0, x=1, and x=2
   * @param x the x (between 0 and 1)
   * @return the interpolated value
   */
  public static double getValue(double[] values, double x) {
    return values[1] + 0.5 * x
        * (values[2] - values[0] + x * (2.0 * values[0] - 5.0 * values[1] + 4.0 * values[2]
            - values[3] + x * (3.0 * (values[1] - values[2]) + values[3] - values[0])));
  }

  /**
   * Gets the interpolated value.
   *
   * <p>This can be used when the values array length is larger than 4.
   *
   * @param values the values of the function at x=-1, x=0, x=1, and x=2, starting at offset
   * @param offset the offset to the first values (x=-1) in the values array
   * @param x the x (between 0 and 1)
   * @return the interpolated value
   */
  public static double getValue(double[] values, int offset, double x) {
    return values[offset + 1] + 0.5 * x
        * (values[offset + 2] - values[offset]
            + x * (2.0 * values[offset] - 5.0 * values[offset + 1] + 4.0 * values[offset + 2]
                - values[offset + 3] + x * (3.0 * (values[offset + 1] - values[offset + 2])
                    + values[offset + 3] - values[offset])));
  }
}
