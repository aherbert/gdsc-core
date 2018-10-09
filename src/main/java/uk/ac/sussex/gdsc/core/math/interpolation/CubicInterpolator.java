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

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;


/**
 * Cubic Interpolator using the Catmull-Rom spline. <p> Taken from
 * http://www.paulinternet.nl/?page=bicubic.
 */
public class CubicInterpolator {
  /**
   * Gets the interpolated value.
   *
   * @param p the value of the function at x=-1, x=0, x=1, and x=2
   * @param x the x (between 0 and 1)
   * @return the value
   */
  public static double getValue(double[] p, double x) {
    return p[1] + 0.5 * x * (p[2] - p[0] + x
        * (2.0 * p[0] - 5.0 * p[1] + 4.0 * p[2] - p[3] + x * (3.0 * (p[1] - p[2]) + p[3] - p[0])));
  }

  /**
   * Gets the interpolated value.
   *
   * @param p the value of the function at x=-1, x=0, x=1, and x=2
   * @param i the offset in the value
   * @param x the x (between 0 and 1)
   * @return the value
   */
  public static double getValue(double[] p, int i, double x) {
    return p[i + 1] + 0.5 * x * (p[i + 2] - p[i] + x * (2.0 * p[i] - 5.0 * p[i + 1] + 4.0 * p[i + 2]
        - p[i + 3] + x * (3.0 * (p[i + 1] - p[i + 2]) + p[i + 3] - p[i])));
  }
}
