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
 * Cached Bicubic Interpolator using the Catmull-Rom spline.
 *
 * <p>Taken from http://www.paulinternet.nl/?page=bicubic.
 */
public class CachedBicubicInterpolator {
  private double a00;
  private double a01;
  private double a02;
  private double a03;
  private double a10;
  private double a11;
  private double a12;
  private double a13;
  private double a20;
  private double a21;
  private double a22;
  private double a23;
  private double a30;
  private double a31;
  private double a32;
  private double a33;

  /**
   * Update coefficients.
   *
   * <p>Note that if x=-1 and x=2 are not available then they can be replaced with x=1 and x=0. This
   * is because the cubic interpolation uses the points to construct the gradient at x=0 as
   * ((x=1)-(x=-1)) / 2. Setting x=-1 to x=1 will just zero the gradient at x=0. Likewise for the
   * gradient at x=1 = ((x=2)-(x=0))/2. Similar arguments apply to y.
   *
   * @param values the value of the function at x=-1 to x=2 and y=-1 to y=2
   */
  public void updateCoefficients(double[][] values) {
    //@formatter:off
    a00 = values[1][1];
    a01 = -.5*values[1][0] + .5*values[1][2];
    a02 = values[1][0] - 2.5*values[1][1] + 2*values[1][2] - .5*values[1][3];
    a03 = -.5*values[1][0] + 1.5*values[1][1] - 1.5*values[1][2] + .5*values[1][3];
    a10 = -.5*values[0][1] + .5*values[2][1];
    a11 = .25*values[0][0] - .25*values[0][2] - .25*values[2][0] + .25*values[2][2];
    a12 = -.5*values[0][0] + 1.25*values[0][1] - values[0][2] + .25*values[0][3] + .5*values[2][0] - 1.25*values[2][1] + values[2][2] - .25*values[2][3];
    a13 = .25*values[0][0] - .75*values[0][1] + .75*values[0][2] - .25*values[0][3] - .25*values[2][0] + .75*values[2][1] - .75*values[2][2] + .25*values[2][3];
    a20 = values[0][1] - 2.5*values[1][1] + 2*values[2][1] - .5*values[3][1];
    a21 = -.5*values[0][0] + .5*values[0][2] + 1.25*values[1][0] - 1.25*values[1][2] - values[2][0] + values[2][2] + .25*values[3][0] - .25*values[3][2];
    a22 = values[0][0] - 2.5*values[0][1] + 2*values[0][2] - .5*values[0][3] - 2.5*values[1][0] + 6.25*values[1][1] - 5*values[1][2] + 1.25*values[1][3] + 2*values[2][0] - 5*values[2][1] + 4*values[2][2] - values[2][3] - .5*values[3][0] + 1.25*values[3][1] - values[3][2] + .25*values[3][3];
    a23 = -.5*values[0][0] + 1.5*values[0][1] - 1.5*values[0][2] + .5*values[0][3] + 1.25*values[1][0] - 3.75*values[1][1] + 3.75*values[1][2] - 1.25*values[1][3] - values[2][0] + 3*values[2][1] - 3*values[2][2] + values[2][3] + .25*values[3][0] - .75*values[3][1] + .75*values[3][2] - .25*values[3][3];
    a30 = -.5*values[0][1] + 1.5*values[1][1] - 1.5*values[2][1] + .5*values[3][1];
    a31 = .25*values[0][0] - .25*values[0][2] - .75*values[1][0] + .75*values[1][2] + .75*values[2][0] - .75*values[2][2] - .25*values[3][0] + .25*values[3][2];
    a32 = -.5*values[0][0] + 1.25*values[0][1] - values[0][2] + .25*values[0][3] + 1.5*values[1][0] - 3.75*values[1][1] + 3*values[1][2] - .75*values[1][3] - 1.5*values[2][0] + 3.75*values[2][1] - 3*values[2][2] + .75*values[2][3] + .5*values[3][0] - 1.25*values[3][1] + values[3][2] - .25*values[3][3];
    a33 = .25*values[0][0] - .75*values[0][1] + .75*values[0][2] - .25*values[0][3] - .75*values[1][0] + 2.25*values[1][1] - 2.25*values[1][2] + .75*values[1][3] + .75*values[2][0] - 2.25*values[2][1] + 2.25*values[2][2] - .75*values[2][3] - .25*values[3][0] + .75*values[3][1] - .75*values[3][2] + .25*values[3][3];
    //@formatter:on
  }

  /**
   * Update coefficients.
   *
   * <p>Note that if x=-1 and x=2 are not available then they can be replaced with x=1 and x=0.
   * This is because the cubic interpolation uses the points to construct the gradient at x=0 as
   * ((x=1)-(x=-1)) / 2. Setting x=-1 to x=1 will just zero the gradient at x=0. Likewise for the
   * gradient at x=1 = ((x=2)-(x=0))/2. Similar arguments apply to y.
   *
   * @param values the value of the function at x=-1 to x=2 and y=-1 to y=2
   */
  public void updateCoefficients(float[][] values) {
    //@formatter:off
    a00 = values[1][1];
    a01 = -.5*values[1][0] + .5*values[1][2];
    a02 = values[1][0] - 2.5*values[1][1] + 2*values[1][2] - .5*values[1][3];
    a03 = -.5*values[1][0] + 1.5*values[1][1] - 1.5*values[1][2] + .5*values[1][3];
    a10 = -.5*values[0][1] + .5*values[2][1];
    a11 = .25*values[0][0] - .25*values[0][2] - .25*values[2][0] + .25*values[2][2];
    a12 = -.5*values[0][0] + 1.25*values[0][1] - values[0][2] + .25*values[0][3] + .5*values[2][0] - 1.25*values[2][1] + values[2][2] - .25*values[2][3];
    a13 = .25*values[0][0] - .75*values[0][1] + .75*values[0][2] - .25*values[0][3] - .25*values[2][0] + .75*values[2][1] - .75*values[2][2] + .25*values[2][3];
    a20 = values[0][1] - 2.5*values[1][1] + 2*values[2][1] - .5*values[3][1];
    a21 = -.5*values[0][0] + .5*values[0][2] + 1.25*values[1][0] - 1.25*values[1][2] - values[2][0] + values[2][2] + .25*values[3][0] - .25*values[3][2];
    a22 = values[0][0] - 2.5*values[0][1] + 2*values[0][2] - .5*values[0][3] - 2.5*values[1][0] + 6.25*values[1][1] - 5*values[1][2] + 1.25*values[1][3] + 2*values[2][0] - 5*values[2][1] + 4*values[2][2] - values[2][3] - .5*values[3][0] + 1.25*values[3][1] - values[3][2] + .25*values[3][3];
    a23 = -.5*values[0][0] + 1.5*values[0][1] - 1.5*values[0][2] + .5*values[0][3] + 1.25*values[1][0] - 3.75*values[1][1] + 3.75*values[1][2] - 1.25*values[1][3] - values[2][0] + 3*values[2][1] - 3*values[2][2] + values[2][3] + .25*values[3][0] - .75*values[3][1] + .75*values[3][2] - .25*values[3][3];
    a30 = -.5*values[0][1] + 1.5*values[1][1] - 1.5*values[2][1] + .5*values[3][1];
    a31 = .25*values[0][0] - .25*values[0][2] - .75*values[1][0] + .75*values[1][2] + .75*values[2][0] - .75*values[2][2] - .25*values[3][0] + .25*values[3][2];
    a32 = -.5*values[0][0] + 1.25*values[0][1] - values[0][2] + .25*values[0][3] + 1.5*values[1][0] - 3.75*values[1][1] + 3*values[1][2] - .75*values[1][3] - 1.5*values[2][0] + 3.75*values[2][1] - 3*values[2][2] + .75*values[2][3] + .5*values[3][0] - 1.25*values[3][1] + values[3][2] - .25*values[3][3];
    a33 = .25*values[0][0] - .75*values[0][1] + .75*values[0][2] - .25*values[0][3] - .75*values[1][0] + 2.25*values[1][1] - 2.25*values[1][2] + .75*values[1][3] + .75*values[2][0] - 2.25*values[2][1] + 2.25*values[2][2] - .75*values[2][3] - .25*values[3][0] + .75*values[3][1] - .75*values[3][2] + .25*values[3][3];
    //@formatter:on
  }

  /**
   * Update coefficients.
   *
   * <p>Note that if x=-1 and x=2 are not available then they can be replaced with x=1 and x=0.
   * This is because the cubic interpolation uses the points to construct the gradient at x=0 as
   * ((x=1)-(x=-1)) / 2. Setting x=-1 to x=1 will just zero the gradient at x=0. Likewise for the
   * gradient at x=1 = ((x=2)-(x=0))/2. Similar arguments apply to y.
   *
   * @param values the value of the function at x=-1 to x=2 and y=-1 to y=2, packed in yx order.
   */
  public void updateCoefficients(double[] values) {
    //@formatter:off
    a00 = values[5];
    a01 = -.5*values[1] + .5*values[9];
    a02 = values[1] - 2.5*values[5] + 2*values[9] - .5*values[13];
    a03 = -.5*values[1] + 1.5*values[5] - 1.5*values[9] + .5*values[13];
    a10 = -.5*values[4] + .5*values[6];
    a11 = .25*values[0] - .25*values[8] - .25*values[2] + .25*values[10];
    a12 = -.5*values[0] + 1.25*values[4] - values[8] + .25*values[12] + .5*values[2] - 1.25*values[6] + values[10] - .25*values[14];
    a13 = .25*values[0] - .75*values[4] + .75*values[8] - .25*values[12] - .25*values[2] + .75*values[6] - .75*values[10] + .25*values[14];
    a20 = values[4] - 2.5*values[5] + 2*values[6] - .5*values[7];
    a21 = -.5*values[0] + .5*values[8] + 1.25*values[1] - 1.25*values[9] - values[2] + values[10] + .25*values[3] - .25*values[11];
    a22 = values[0] - 2.5*values[4] + 2*values[8] - .5*values[12] - 2.5*values[1] + 6.25*values[5] - 5*values[9] + 1.25*values[13] + 2*values[2] - 5*values[6] + 4*values[10] - values[14] - .5*values[3] + 1.25*values[7] - values[11] + .25*values[15];
    a23 = -.5*values[0] + 1.5*values[4] - 1.5*values[8] + .5*values[12] + 1.25*values[1] - 3.75*values[5] + 3.75*values[9] - 1.25*values[13] - values[2] + 3*values[6] - 3*values[10] + values[14] + .25*values[3] - .75*values[7] + .75*values[11] - .25*values[15];
    a30 = -.5*values[4] + 1.5*values[5] - 1.5*values[6] + .5*values[7];
    a31 = .25*values[0] - .25*values[8] - .75*values[1] + .75*values[9] + .75*values[2] - .75*values[10] - .25*values[3] + .25*values[11];
    a32 = -.5*values[0] + 1.25*values[4] - values[8] + .25*values[12] + 1.5*values[1] - 3.75*values[5] + 3*values[9] - .75*values[13] - 1.5*values[2] + 3.75*values[6] - 3*values[10] + .75*values[14] + .5*values[3] - 1.25*values[7] + values[11] - .25*values[15];
    a33 = .25*values[0] - .75*values[4] + .75*values[8] - .25*values[12] - .75*values[1] + 2.25*values[5] - 2.25*values[9] + .75*values[13] + .75*values[2] - 2.25*values[6] + 2.25*values[10] - .75*values[14] - .25*values[3] + .75*values[7] - .75*values[11] + .25*values[15];
    //@formatter:on
  }

  /**
   * Update coefficients.
   *
   * <p>Note that if x=-1 and x=2 are not available then they can be replaced with x=1 and x=0.
   * This is because the cubic interpolation uses the points to construct the gradient at x=0 as
   * ((x=1)-(x=-1)) / 2. Setting x=-1 to x=1 will just zero the gradient at x=0. Likewise for the
   * gradient at x=1 = ((x=2)-(x=0))/2. Similar arguments apply to y.
   *
   * @param values the value of the function at x=-1 to x=2 and y=-1 to y=2, packed in yx order.
   */
  public void updateCoefficients(float[] values) {
    //@formatter:off
    a00 = values[5];
    a01 = -.5*values[1] + .5*values[9];
    a02 = values[1] - 2.5*values[5] + 2*values[9] - .5*values[13];
    a03 = -.5*values[1] + 1.5*values[5] - 1.5*values[9] + .5*values[13];
    a10 = -.5*values[4] + .5*values[6];
    a11 = .25*values[0] - .25*values[8] - .25*values[2] + .25*values[10];
    a12 = -.5*values[0] + 1.25*values[4] - values[8] + .25*values[12] + .5*values[2] - 1.25*values[6] + values[10] - .25*values[14];
    a13 = .25*values[0] - .75*values[4] + .75*values[8] - .25*values[12] - .25*values[2] + .75*values[6] - .75*values[10] + .25*values[14];
    a20 = values[4] - 2.5*values[5] + 2*values[6] - .5*values[7];
    a21 = -.5*values[0] + .5*values[8] + 1.25*values[1] - 1.25*values[9] - values[2] + values[10] + .25*values[3] - .25*values[11];
    a22 = values[0] - 2.5*values[4] + 2*values[8] - .5*values[12] - 2.5*values[1] + 6.25*values[5] - 5*values[9] + 1.25*values[13] + 2*values[2] - 5*values[6] + 4*values[10] - values[14] - .5*values[3] + 1.25*values[7] - values[11] + .25*values[15];
    a23 = -.5*values[0] + 1.5*values[4] - 1.5*values[8] + .5*values[12] + 1.25*values[1] - 3.75*values[5] + 3.75*values[9] - 1.25*values[13] - values[2] + 3*values[6] - 3*values[10] + values[14] + .25*values[3] - .75*values[7] + .75*values[11] - .25*values[15];
    a30 = -.5*values[4] + 1.5*values[5] - 1.5*values[6] + .5*values[7];
    a31 = .25*values[0] - .25*values[8] - .75*values[1] + .75*values[9] + .75*values[2] - .75*values[10] - .25*values[3] + .25*values[11];
    a32 = -.5*values[0] + 1.25*values[4] - values[8] + .25*values[12] + 1.5*values[1] - 3.75*values[5] + 3*values[9] - .75*values[13] - 1.5*values[2] + 3.75*values[6] - 3*values[10] + .75*values[14] + .5*values[3] - 1.25*values[7] + values[11] - .25*values[15];
    a33 = .25*values[0] - .75*values[4] + .75*values[8] - .25*values[12] - .75*values[1] + 2.25*values[5] - 2.25*values[9] + .75*values[13] + .75*values[2] - 2.25*values[6] + 2.25*values[10] - .75*values[14] - .25*values[3] + .75*values[7] - .75*values[11] + .25*values[15];
    //@formatter:on
  }

  /**
   * Gets the interpolated value.
   *
   * @param x the x (between 0 and 1)
   * @param y the y (between 0 and 1)
   * @return the value
   */
  public double getValue(double x, double y) {
    final double x2 = x * x;
    final double x3 = x2 * x;
    final double y2 = y * y;
    final double y3 = y2 * y;

    //@formatter:off
    return (a00 + a01 * y + a02 * y2 + a03 * y3) +
           (a10 + a11 * y + a12 * y2 + a13 * y3) * x +
           (a20 + a21 * y + a22 * y2 + a23 * y3) * x2 +
           (a30 + a31 * y + a32 * y2 + a33 * y3) * x3;
    //@formatter:on
  }

  /**
   * Gets the interpolated value. The power must be computed correctly. This function allows you to
   * pre-compute the powers for efficient sub-sampling.
   *
   * @param x the x (between 0 and 1)
   * @param x2 x^2
   * @param x3 x^3
   * @param y the y (between 0 and 1)
   * @param y2 y^2
   * @param y3 y^3
   * @return the value
   */
  public double getValue(double x, double x2, double x3, double y, double y2, double y3) {
    //@formatter:off
    return (a00 + a01 * y + a02 * y2 + a03 * y3) +
           (a10 + a11 * y + a12 * y2 + a13 * y3) * x +
           (a20 + a21 * y + a22 * y2 + a23 * y3) * x2 +
           (a30 + a31 * y + a32 * y2 + a33 * y3) * x3;
    //@formatter:on
  }
}
