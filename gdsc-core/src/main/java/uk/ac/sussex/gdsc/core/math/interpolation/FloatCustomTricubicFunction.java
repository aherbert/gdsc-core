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

package uk.ac.sussex.gdsc.core.math.interpolation;

/**
 * 3D-spline function using single precision float values to store the coefficients. This reduces
 * the memory required to store the function.
 *
 * <p>Note: Computation still uses double precision. The result is a large number of
 * {@code float * double} computations which evaluate slower than {@code double * double}. This
 * class should only be used when storage space is limited.
 *
 * <p>This class is immutable.
 */
public class FloatCustomTricubicFunction extends CustomTricubicFunction {
  private static final long serialVersionUID = 20190326L;

  /** The 64 coefficients (coeff) for the tri-cubic function. */
  private final FloatCubicSplineData coeff;

  @Override
  public void getCoefficients(double[] coefficients) {
    final float[] tmp = new float[64];
    coeff.toArray(tmp);
    for (int i = 0; i < 64; i++) {
      coefficients[i] = tmp[i];
    }
  }

  @Override
  public void getCoefficients(float[] coefficients) {
    coeff.toArray(coefficients);
  }

  /**
   * Create a new instance.
   *
   * @param coefficients Spline coefficients.
   */
  FloatCustomTricubicFunction(FloatCubicSplineData coefficients) {
    this.coeff = coefficients;
  }

  @Override
  public boolean isSinglePrecision() {
    return true;
  }

  @Override
  public CustomTricubicFunction toSinglePrecision() {
    return this;
  }

  @Override
  public CustomTricubicFunction toDoublePrecision() {
    final double[] tmp = new double[64];
    getCoefficients(tmp);
    return new DoubleCustomTricubicFunction(new DoubleCubicSplineData(tmp));
  }

  @Override
  public CustomTricubicFunction copy() {
    // The class is immutable
    return this;
  }

  @Override
  public CustomTricubicFunction scale(double scale) {
    return new FloatCustomTricubicFunction(coeff.scale(scale));
  }

  // XXX - Copy from DoubleCustomTricubicFunction after here

  @Override
  public double value000() {
    return coeff.x0y0z0;
  }

  @Override
  public double value000(double[] derivative1) {
    derivative1[0] = coeff.x1y0z0;
    derivative1[1] = coeff.x0y1z0;
    derivative1[2] = coeff.x0y0z1;
    return coeff.x0y0z0;
  }

  @Override
  public double value000(double[] derivative1, double[] derivative2) {
    derivative1[0] = coeff.x1y0z0;
    derivative1[1] = coeff.x0y1z0;
    derivative1[2] = coeff.x0y0z1;
    derivative2[0] = 2 * coeff.x2y0z0;
    derivative2[1] = 2 * coeff.x0y2z0;
    derivative2[2] = 2 * coeff.x0y0z2;
    return coeff.x0y0z0;
  }

  @Override
  protected double value0(final CubicSplinePosition x, final CubicSplinePosition y,
      final CubicSplinePosition z) {
    //@formatter:off
    return               (coeff.x0y0z0 + x.x1 * coeff.x1y0z0 + x.x2 * coeff.x2y0z0 + x.x3 * coeff.x3y0z0)
                + y.x1 * (coeff.x0y1z0 + x.x1 * coeff.x1y1z0 + x.x2 * coeff.x2y1z0 + x.x3 * coeff.x3y1z0)
                + y.x2 * (coeff.x0y2z0 + x.x1 * coeff.x1y2z0 + x.x2 * coeff.x2y2z0 + x.x3 * coeff.x3y2z0)
                + y.x3 * (coeff.x0y3z0 + x.x1 * coeff.x1y3z0 + x.x2 * coeff.x2y3z0 + x.x3 * coeff.x3y3z0)
        + z.x1 * (       (coeff.x0y0z1 + x.x1 * coeff.x1y0z1 + x.x2 * coeff.x2y0z1 + x.x3 * coeff.x3y0z1)
                + y.x1 * (coeff.x0y1z1 + x.x1 * coeff.x1y1z1 + x.x2 * coeff.x2y1z1 + x.x3 * coeff.x3y1z1)
                + y.x2 * (coeff.x0y2z1 + x.x1 * coeff.x1y2z1 + x.x2 * coeff.x2y2z1 + x.x3 * coeff.x3y2z1)
                + y.x3 * (coeff.x0y3z1 + x.x1 * coeff.x1y3z1 + x.x2 * coeff.x2y3z1 + x.x3 * coeff.x3y3z1))
        + z.x2 * (       (coeff.x0y0z2 + x.x1 * coeff.x1y0z2 + x.x2 * coeff.x2y0z2 + x.x3 * coeff.x3y0z2)
                + y.x1 * (coeff.x0y1z2 + x.x1 * coeff.x1y1z2 + x.x2 * coeff.x2y1z2 + x.x3 * coeff.x3y1z2)
                + y.x2 * (coeff.x0y2z2 + x.x1 * coeff.x1y2z2 + x.x2 * coeff.x2y2z2 + x.x3 * coeff.x3y2z2)
                + y.x3 * (coeff.x0y3z2 + x.x1 * coeff.x1y3z2 + x.x2 * coeff.x2y3z2 + x.x3 * coeff.x3y3z2))
        + z.x3 * (       (coeff.x0y0z3 + x.x1 * coeff.x1y0z3 + x.x2 * coeff.x2y0z3 + x.x3 * coeff.x3y0z3)
                + y.x1 * (coeff.x0y1z3 + x.x1 * coeff.x1y1z3 + x.x2 * coeff.x2y1z3 + x.x3 * coeff.x3y1z3)
                + y.x2 * (coeff.x0y2z3 + x.x1 * coeff.x1y2z3 + x.x2 * coeff.x2y2z3 + x.x3 * coeff.x3y2z3)
                + y.x3 * (coeff.x0y3z3 + x.x1 * coeff.x1y3z3 + x.x2 * coeff.x2y3z3 + x.x3 * coeff.x3y3z3));
    //@formatter:on
  }

  @Override
  protected double value1(final CubicSplinePosition x, final CubicSplinePosition y,
      final CubicSplinePosition z, final double[] derivative1) {
    //@formatter:off
    derivative1[0] =         (coeff.x1y0z0 + y.x1 * coeff.x1y1z0 + y.x2 * coeff.x1y2z0 + y.x3 * coeff.x1y3z0)
                    + z.x1 * (coeff.x1y0z1 + y.x1 * coeff.x1y1z1 + y.x2 * coeff.x1y2z1 + y.x3 * coeff.x1y3z1)
                    + z.x2 * (coeff.x1y0z2 + y.x1 * coeff.x1y1z2 + y.x2 * coeff.x1y2z2 + y.x3 * coeff.x1y3z2)
                    + z.x3 * (coeff.x1y0z3 + y.x1 * coeff.x1y1z3 + y.x2 * coeff.x1y2z3 + y.x3 * coeff.x1y3z3)
        + 2 * x.x1 * (       (coeff.x2y0z0 + y.x1 * coeff.x2y1z0 + y.x2 * coeff.x2y2z0 + y.x3 * coeff.x2y3z0)
                    + z.x1 * (coeff.x2y0z1 + y.x1 * coeff.x2y1z1 + y.x2 * coeff.x2y2z1 + y.x3 * coeff.x2y3z1)
                    + z.x2 * (coeff.x2y0z2 + y.x1 * coeff.x2y1z2 + y.x2 * coeff.x2y2z2 + y.x3 * coeff.x2y3z2)
                    + z.x3 * (coeff.x2y0z3 + y.x1 * coeff.x2y1z3 + y.x2 * coeff.x2y2z3 + y.x3 * coeff.x2y3z3))
        + 3 * x.x2 * (       (coeff.x3y0z0 + y.x1 * coeff.x3y1z0 + y.x2 * coeff.x3y2z0 + y.x3 * coeff.x3y3z0)
                    + z.x1 * (coeff.x3y0z1 + y.x1 * coeff.x3y1z1 + y.x2 * coeff.x3y2z1 + y.x3 * coeff.x3y3z1)
                    + z.x2 * (coeff.x3y0z2 + y.x1 * coeff.x3y1z2 + y.x2 * coeff.x3y2z2 + y.x3 * coeff.x3y3z2)
                    + z.x3 * (coeff.x3y0z3 + y.x1 * coeff.x3y1z3 + y.x2 * coeff.x3y2z3 + y.x3 * coeff.x3y3z3));

    derivative1[1] =         (coeff.x0y1z0 + x.x1 * coeff.x1y1z0 + x.x2 * coeff.x2y1z0 + x.x3 * coeff.x3y1z0)
                    + z.x1 * (coeff.x0y1z1 + x.x1 * coeff.x1y1z1 + x.x2 * coeff.x2y1z1 + x.x3 * coeff.x3y1z1)
                    + z.x2 * (coeff.x0y1z2 + x.x1 * coeff.x1y1z2 + x.x2 * coeff.x2y1z2 + x.x3 * coeff.x3y1z2)
                    + z.x3 * (coeff.x0y1z3 + x.x1 * coeff.x1y1z3 + x.x2 * coeff.x2y1z3 + x.x3 * coeff.x3y1z3)
        + 2 * y.x1 * (       (coeff.x0y2z0 + x.x1 * coeff.x1y2z0 + x.x2 * coeff.x2y2z0 + x.x3 * coeff.x3y2z0)
                    + z.x1 * (coeff.x0y2z1 + x.x1 * coeff.x1y2z1 + x.x2 * coeff.x2y2z1 + x.x3 * coeff.x3y2z1)
                    + z.x2 * (coeff.x0y2z2 + x.x1 * coeff.x1y2z2 + x.x2 * coeff.x2y2z2 + x.x3 * coeff.x3y2z2)
                    + z.x3 * (coeff.x0y2z3 + x.x1 * coeff.x1y2z3 + x.x2 * coeff.x2y2z3 + x.x3 * coeff.x3y2z3))
        + 3 * y.x2 * (       (coeff.x0y3z0 + x.x1 * coeff.x1y3z0 + x.x2 * coeff.x2y3z0 + x.x3 * coeff.x3y3z0)
                    + z.x1 * (coeff.x0y3z1 + x.x1 * coeff.x1y3z1 + x.x2 * coeff.x2y3z1 + x.x3 * coeff.x3y3z1)
                    + z.x2 * (coeff.x0y3z2 + x.x1 * coeff.x1y3z2 + x.x2 * coeff.x2y3z2 + x.x3 * coeff.x3y3z2)
                    + z.x3 * (coeff.x0y3z3 + x.x1 * coeff.x1y3z3 + x.x2 * coeff.x2y3z3 + x.x3 * coeff.x3y3z3));

    // Note: the computation for value0 is arranged using zyx so precompute the factors for z
    final double factorZ1 =
                 (coeff.x0y0z1 + x.x1 * coeff.x1y0z1 + x.x2 * coeff.x2y0z1 + x.x3 * coeff.x3y0z1)
        + y.x1 * (coeff.x0y1z1 + x.x1 * coeff.x1y1z1 + x.x2 * coeff.x2y1z1 + x.x3 * coeff.x3y1z1)
        + y.x2 * (coeff.x0y2z1 + x.x1 * coeff.x1y2z1 + x.x2 * coeff.x2y2z1 + x.x3 * coeff.x3y2z1)
        + y.x3 * (coeff.x0y3z1 + x.x1 * coeff.x1y3z1 + x.x2 * coeff.x2y3z1 + x.x3 * coeff.x3y3z1);
    final double factorZ2 =
                 (coeff.x0y0z2 + x.x1 * coeff.x1y0z2 + x.x2 * coeff.x2y0z2 + x.x3 * coeff.x3y0z2)
        + y.x1 * (coeff.x0y1z2 + x.x1 * coeff.x1y1z2 + x.x2 * coeff.x2y1z2 + x.x3 * coeff.x3y1z2)
        + y.x2 * (coeff.x0y2z2 + x.x1 * coeff.x1y2z2 + x.x2 * coeff.x2y2z2 + x.x3 * coeff.x3y2z2)
        + y.x3 * (coeff.x0y3z2 + x.x1 * coeff.x1y3z2 + x.x2 * coeff.x2y3z2 + x.x3 * coeff.x3y3z2);
    final double factorZ3 =
                 (coeff.x0y0z3 + x.x1 * coeff.x1y0z3 + x.x2 * coeff.x2y0z3 + x.x3 * coeff.x3y0z3)
        + y.x1 * (coeff.x0y1z3 + x.x1 * coeff.x1y1z3 + x.x2 * coeff.x2y1z3 + x.x3 * coeff.x3y1z3)
        + y.x2 * (coeff.x0y2z3 + x.x1 * coeff.x1y2z3 + x.x2 * coeff.x2y2z3 + x.x3 * coeff.x3y2z3)
        + y.x3 * (coeff.x0y3z3 + x.x1 * coeff.x1y3z3 + x.x2 * coeff.x2y3z3 + x.x3 * coeff.x3y3z3);
    derivative1[2] = factorZ1
        + 2 * z.x1 * factorZ2
        + 3 * z.x2 * factorZ3;

    return               (coeff.x0y0z0 + x.x1 * coeff.x1y0z0 + x.x2 * coeff.x2y0z0 + x.x3 * coeff.x3y0z0)
                + y.x1 * (coeff.x0y1z0 + x.x1 * coeff.x1y1z0 + x.x2 * coeff.x2y1z0 + x.x3 * coeff.x3y1z0)
                + y.x2 * (coeff.x0y2z0 + x.x1 * coeff.x1y2z0 + x.x2 * coeff.x2y2z0 + x.x3 * coeff.x3y2z0)
                + y.x3 * (coeff.x0y3z0 + x.x1 * coeff.x1y3z0 + x.x2 * coeff.x2y3z0 + x.x3 * coeff.x3y3z0)
        + z.x1 * factorZ1
        + z.x2 * factorZ2
        + z.x3 * factorZ3;
    //@formatter:on
  }

  @Override
  protected double value2(final CubicSplinePosition x, final CubicSplinePosition y,
      final CubicSplinePosition z, final double[] derivative1, double[] derivative2) {
    //@formatter:off
    // Pre-compute the factors for x
    final double factorX1 =
                 (coeff.x1y0z0 + y.x1 * coeff.x1y1z0 + y.x2 * coeff.x1y2z0 + y.x3 * coeff.x1y3z0)
        + z.x1 * (coeff.x1y0z1 + y.x1 * coeff.x1y1z1 + y.x2 * coeff.x1y2z1 + y.x3 * coeff.x1y3z1)
        + z.x2 * (coeff.x1y0z2 + y.x1 * coeff.x1y1z2 + y.x2 * coeff.x1y2z2 + y.x3 * coeff.x1y3z2)
        + z.x3 * (coeff.x1y0z3 + y.x1 * coeff.x1y1z3 + y.x2 * coeff.x1y2z3 + y.x3 * coeff.x1y3z3);
    final double factorX2 =
                 (coeff.x2y0z0 + y.x1 * coeff.x2y1z0 + y.x2 * coeff.x2y2z0 + y.x3 * coeff.x2y3z0)
        + z.x1 * (coeff.x2y0z1 + y.x1 * coeff.x2y1z1 + y.x2 * coeff.x2y2z1 + y.x3 * coeff.x2y3z1)
        + z.x2 * (coeff.x2y0z2 + y.x1 * coeff.x2y1z2 + y.x2 * coeff.x2y2z2 + y.x3 * coeff.x2y3z2)
        + z.x3 * (coeff.x2y0z3 + y.x1 * coeff.x2y1z3 + y.x2 * coeff.x2y2z3 + y.x3 * coeff.x2y3z3);
    final double factorX3 =
                 (coeff.x3y0z0 + y.x1 * coeff.x3y1z0 + y.x2 * coeff.x3y2z0 + y.x3 * coeff.x3y3z0)
        + z.x1 * (coeff.x3y0z1 + y.x1 * coeff.x3y1z1 + y.x2 * coeff.x3y2z1 + y.x3 * coeff.x3y3z1)
        + z.x2 * (coeff.x3y0z2 + y.x1 * coeff.x3y1z2 + y.x2 * coeff.x3y2z2 + y.x3 * coeff.x3y3z2)
        + z.x3 * (coeff.x3y0z3 + y.x1 * coeff.x3y1z3 + y.x2 * coeff.x3y2z3 + y.x3 * coeff.x3y3z3);
    derivative1[0] = factorX1
        + 2 * x.x1 * factorX2
        + 3 * x.x2 * factorX3;
    derivative2[0] =
          2 *        factorX2
        + 6 * x.x1 * factorX3;

    // Pre-compute the factors for y
    final double factorY1 =
                 (coeff.x0y1z0 + x.x1 * coeff.x1y1z0 + x.x2 * coeff.x2y1z0 + x.x3 * coeff.x3y1z0)
        + z.x1 * (coeff.x0y1z1 + x.x1 * coeff.x1y1z1 + x.x2 * coeff.x2y1z1 + x.x3 * coeff.x3y1z1)
        + z.x2 * (coeff.x0y1z2 + x.x1 * coeff.x1y1z2 + x.x2 * coeff.x2y1z2 + x.x3 * coeff.x3y1z2)
        + z.x3 * (coeff.x0y1z3 + x.x1 * coeff.x1y1z3 + x.x2 * coeff.x2y1z3 + x.x3 * coeff.x3y1z3);
    final double factorY2 =
                 (coeff.x0y2z0 + x.x1 * coeff.x1y2z0 + x.x2 * coeff.x2y2z0 + x.x3 * coeff.x3y2z0)
        + z.x1 * (coeff.x0y2z1 + x.x1 * coeff.x1y2z1 + x.x2 * coeff.x2y2z1 + x.x3 * coeff.x3y2z1)
        + z.x2 * (coeff.x0y2z2 + x.x1 * coeff.x1y2z2 + x.x2 * coeff.x2y2z2 + x.x3 * coeff.x3y2z2)
        + z.x3 * (coeff.x0y2z3 + x.x1 * coeff.x1y2z3 + x.x2 * coeff.x2y2z3 + x.x3 * coeff.x3y2z3);
    final double factorY3 =
                 (coeff.x0y3z0 + x.x1 * coeff.x1y3z0 + x.x2 * coeff.x2y3z0 + x.x3 * coeff.x3y3z0)
        + z.x1 * (coeff.x0y3z1 + x.x1 * coeff.x1y3z1 + x.x2 * coeff.x2y3z1 + x.x3 * coeff.x3y3z1)
        + z.x2 * (coeff.x0y3z2 + x.x1 * coeff.x1y3z2 + x.x2 * coeff.x2y3z2 + x.x3 * coeff.x3y3z2)
        + z.x3 * (coeff.x0y3z3 + x.x1 * coeff.x1y3z3 + x.x2 * coeff.x2y3z3 + x.x3 * coeff.x3y3z3);
    derivative1[1] = factorY1
        + 2 * y.x1 * factorY2
        + 3 * y.x2 * factorY3;
    derivative2[1] =
          2 *        factorY2
        + 6 * y.x1 * factorY3;

    // Pre-compute the factors for z
    final double factorZ1 =          (coeff.x0y0z1 + x.x1 * coeff.x1y0z1 + x.x2 * coeff.x2y0z1 + x.x3 * coeff.x3y0z1)
                            + y.x1 * (coeff.x0y1z1 + x.x1 * coeff.x1y1z1 + x.x2 * coeff.x2y1z1 + x.x3 * coeff.x3y1z1)
                            + y.x2 * (coeff.x0y2z1 + x.x1 * coeff.x1y2z1 + x.x2 * coeff.x2y2z1 + x.x3 * coeff.x3y2z1)
                            + y.x3 * (coeff.x0y3z1 + x.x1 * coeff.x1y3z1 + x.x2 * coeff.x2y3z1 + x.x3 * coeff.x3y3z1);
    final double factorZ2 =          (coeff.x0y0z2 + x.x1 * coeff.x1y0z2 + x.x2 * coeff.x2y0z2 + x.x3 * coeff.x3y0z2)
                            + y.x1 * (coeff.x0y1z2 + x.x1 * coeff.x1y1z2 + x.x2 * coeff.x2y1z2 + x.x3 * coeff.x3y1z2)
                            + y.x2 * (coeff.x0y2z2 + x.x1 * coeff.x1y2z2 + x.x2 * coeff.x2y2z2 + x.x3 * coeff.x3y2z2)
                            + y.x3 * (coeff.x0y3z2 + x.x1 * coeff.x1y3z2 + x.x2 * coeff.x2y3z2 + x.x3 * coeff.x3y3z2);
    final double factorZ3 =          (coeff.x0y0z3 + x.x1 * coeff.x1y0z3 + x.x2 * coeff.x2y0z3 + x.x3 * coeff.x3y0z3)
                            + y.x1 * (coeff.x0y1z3 + x.x1 * coeff.x1y1z3 + x.x2 * coeff.x2y1z3 + x.x3 * coeff.x3y1z3)
                            + y.x2 * (coeff.x0y2z3 + x.x1 * coeff.x1y2z3 + x.x2 * coeff.x2y2z3 + x.x3 * coeff.x3y2z3)
                            + y.x3 * (coeff.x0y3z3 + x.x1 * coeff.x1y3z3 + x.x2 * coeff.x2y3z3 + x.x3 * coeff.x3y3z3);
    derivative1[2] = factorZ1
        + 2 * z.x1 * factorZ2
        + 3 * z.x2 * factorZ3;
    derivative2[2] =
          2 *        factorZ2
        + 6 * z.x1 * factorZ3;

    // Note: The computation for value0 is arranged using zyx so reuse the factors for z
    // and compute the remaining polynomial.
    return               (coeff.x0y0z0 + x.x1 * coeff.x1y0z0 + x.x2 * coeff.x2y0z0 + x.x3 * coeff.x3y0z0)
                + y.x1 * (coeff.x0y1z0 + x.x1 * coeff.x1y1z0 + x.x2 * coeff.x2y1z0 + x.x3 * coeff.x3y1z0)
                + y.x2 * (coeff.x0y2z0 + x.x1 * coeff.x1y2z0 + x.x2 * coeff.x2y2z0 + x.x3 * coeff.x3y2z0)
                + y.x3 * (coeff.x0y3z0 + x.x1 * coeff.x1y3z0 + x.x2 * coeff.x2y3z0 + x.x3 * coeff.x3y3z0)
        + z.x1 * factorZ1
        + z.x2 * factorZ2
        + z.x3 * factorZ3;
    //@formatter:on
  }
}
