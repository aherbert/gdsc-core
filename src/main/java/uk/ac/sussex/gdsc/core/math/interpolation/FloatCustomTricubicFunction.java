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
 * Copyright (C) 2011 - 2019 Alex Herbert
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
 * <p>Not all computations use exclusively float precision. The computations using the power table
 * use float computation and should show the largest speed benefit over the double precision counter
 * part.
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

  // Allow the working variables for the power computation
  // to be declared at the top of the method
  // CHECKSTYLE.OFF: VariableDeclarationUsageDistance
  // CHECKSTYLE.OFF: LocalVariableName

  @Override
  protected double value0(final CubicSplinePosition x, final CubicSplinePosition y,
      final CubicSplinePosition z) {
    double zCyB;
    double result = 0;

    result += coeff.x0y0z0;
    result += x.x1 * coeff.x1y0z0;
    result += x.x2 * coeff.x2y0z0;
    result += x.x3 * coeff.x3y0z0;
    result += y.x1 * coeff.x0y1z0;
    result += y.x1 * x.x1 * coeff.x1y1z0;
    result += y.x1 * x.x2 * coeff.x2y1z0;
    result += y.x1 * x.x3 * coeff.x3y1z0;
    result += y.x2 * coeff.x0y2z0;
    result += y.x2 * x.x1 * coeff.x1y2z0;
    result += y.x2 * x.x2 * coeff.x2y2z0;
    result += y.x2 * x.x3 * coeff.x3y2z0;
    result += y.x3 * coeff.x0y3z0;
    result += y.x3 * x.x1 * coeff.x1y3z0;
    result += y.x3 * x.x2 * coeff.x2y3z0;
    result += y.x3 * x.x3 * coeff.x3y3z0;
    result += z.x1 * coeff.x0y0z1;
    result += z.x1 * x.x1 * coeff.x1y0z1;
    result += z.x1 * x.x2 * coeff.x2y0z1;
    result += z.x1 * x.x3 * coeff.x3y0z1;
    zCyB = z.x1 * y.x1;
    result += zCyB * coeff.x0y1z1;
    result += zCyB * x.x1 * coeff.x1y1z1;
    result += zCyB * x.x2 * coeff.x2y1z1;
    result += zCyB * x.x3 * coeff.x3y1z1;
    zCyB = z.x1 * y.x2;
    result += zCyB * coeff.x0y2z1;
    result += zCyB * x.x1 * coeff.x1y2z1;
    result += zCyB * x.x2 * coeff.x2y2z1;
    result += zCyB * x.x3 * coeff.x3y2z1;
    zCyB = z.x1 * y.x3;
    result += zCyB * coeff.x0y3z1;
    result += zCyB * x.x1 * coeff.x1y3z1;
    result += zCyB * x.x2 * coeff.x2y3z1;
    result += zCyB * x.x3 * coeff.x3y3z1;
    result += z.x2 * coeff.x0y0z2;
    result += z.x2 * x.x1 * coeff.x1y0z2;
    result += z.x2 * x.x2 * coeff.x2y0z2;
    result += z.x2 * x.x3 * coeff.x3y0z2;
    zCyB = z.x2 * y.x1;
    result += zCyB * coeff.x0y1z2;
    result += zCyB * x.x1 * coeff.x1y1z2;
    result += zCyB * x.x2 * coeff.x2y1z2;
    result += zCyB * x.x3 * coeff.x3y1z2;
    zCyB = z.x2 * y.x2;
    result += zCyB * coeff.x0y2z2;
    result += zCyB * x.x1 * coeff.x1y2z2;
    result += zCyB * x.x2 * coeff.x2y2z2;
    result += zCyB * x.x3 * coeff.x3y2z2;
    zCyB = z.x2 * y.x3;
    result += zCyB * coeff.x0y3z2;
    result += zCyB * x.x1 * coeff.x1y3z2;
    result += zCyB * x.x2 * coeff.x2y3z2;
    result += zCyB * x.x3 * coeff.x3y3z2;
    result += z.x3 * coeff.x0y0z3;
    result += z.x3 * x.x1 * coeff.x1y0z3;
    result += z.x3 * x.x2 * coeff.x2y0z3;
    result += z.x3 * x.x3 * coeff.x3y0z3;
    zCyB = z.x3 * y.x1;
    result += zCyB * coeff.x0y1z3;
    result += zCyB * x.x1 * coeff.x1y1z3;
    result += zCyB * x.x2 * coeff.x2y1z3;
    result += zCyB * x.x3 * coeff.x3y1z3;
    zCyB = z.x3 * y.x2;
    result += zCyB * coeff.x0y2z3;
    result += zCyB * x.x1 * coeff.x1y2z3;
    result += zCyB * x.x2 * coeff.x2y2z3;
    result += zCyB * x.x3 * coeff.x3y2z3;
    zCyB = z.x3 * y.x3;
    result += zCyB * coeff.x0y3z3;
    result += zCyB * x.x1 * coeff.x1y3z3;
    result += zCyB * x.x2 * coeff.x2y3z3;
    result += zCyB * x.x3 * coeff.x3y3z3;

    return result;
  }

  @Override
  protected double value1(final CubicSplinePosition x, final CubicSplinePosition y,
      final CubicSplinePosition z, final double[] derivative1) {
    double zCyB;
    double zCyBxA;
    double result = 0;
    derivative1[0] = 0;
    derivative1[1] = 0;
    derivative1[2] = 0;

    result += coeff.x0y0z0;
    derivative1[0] += coeff.x1y0z0;
    derivative1[1] += coeff.x0y1z0;
    derivative1[2] += coeff.x0y0z1;
    result += x.x1 * coeff.x1y0z0;
    derivative1[0] += 2 * x.x1 * coeff.x2y0z0;
    derivative1[1] += x.x1 * coeff.x1y1z0;
    derivative1[2] += x.x1 * coeff.x1y0z1;
    result += x.x2 * coeff.x2y0z0;
    derivative1[0] += 3 * x.x2 * coeff.x3y0z0;
    derivative1[1] += x.x2 * coeff.x2y1z0;
    derivative1[2] += x.x2 * coeff.x2y0z1;
    result += x.x3 * coeff.x3y0z0;
    derivative1[1] += x.x3 * coeff.x3y1z0;
    derivative1[2] += x.x3 * coeff.x3y0z1;
    result += y.x1 * coeff.x0y1z0;
    derivative1[0] += y.x1 * coeff.x1y1z0;
    derivative1[1] += 2 * y.x1 * coeff.x0y2z0;
    derivative1[2] += y.x1 * coeff.x0y1z1;
    zCyBxA = y.x1 * x.x1;
    result += zCyBxA * coeff.x1y1z0;
    derivative1[0] += 2 * zCyBxA * coeff.x2y1z0;
    derivative1[1] += 2 * zCyBxA * coeff.x1y2z0;
    derivative1[2] += zCyBxA * coeff.x1y1z1;
    zCyBxA = y.x1 * x.x2;
    result += zCyBxA * coeff.x2y1z0;
    derivative1[0] += 3 * zCyBxA * coeff.x3y1z0;
    derivative1[1] += 2 * zCyBxA * coeff.x2y2z0;
    derivative1[2] += zCyBxA * coeff.x2y1z1;
    zCyBxA = y.x1 * x.x3;
    result += zCyBxA * coeff.x3y1z0;
    derivative1[1] += 2 * zCyBxA * coeff.x3y2z0;
    derivative1[2] += zCyBxA * coeff.x3y1z1;
    result += y.x2 * coeff.x0y2z0;
    derivative1[0] += y.x2 * coeff.x1y2z0;
    derivative1[1] += 3 * y.x2 * coeff.x0y3z0;
    derivative1[2] += y.x2 * coeff.x0y2z1;
    zCyBxA = y.x2 * x.x1;
    result += zCyBxA * coeff.x1y2z0;
    derivative1[0] += 2 * zCyBxA * coeff.x2y2z0;
    derivative1[1] += 3 * zCyBxA * coeff.x1y3z0;
    derivative1[2] += zCyBxA * coeff.x1y2z1;
    zCyBxA = y.x2 * x.x2;
    result += zCyBxA * coeff.x2y2z0;
    derivative1[0] += 3 * zCyBxA * coeff.x3y2z0;
    derivative1[1] += 3 * zCyBxA * coeff.x2y3z0;
    derivative1[2] += zCyBxA * coeff.x2y2z1;
    zCyBxA = y.x2 * x.x3;
    result += zCyBxA * coeff.x3y2z0;
    derivative1[1] += 3 * zCyBxA * coeff.x3y3z0;
    derivative1[2] += zCyBxA * coeff.x3y2z1;
    result += y.x3 * coeff.x0y3z0;
    derivative1[0] += y.x3 * coeff.x1y3z0;
    derivative1[2] += y.x3 * coeff.x0y3z1;
    zCyBxA = y.x3 * x.x1;
    result += zCyBxA * coeff.x1y3z0;
    derivative1[0] += 2 * zCyBxA * coeff.x2y3z0;
    derivative1[2] += zCyBxA * coeff.x1y3z1;
    zCyBxA = y.x3 * x.x2;
    result += zCyBxA * coeff.x2y3z0;
    derivative1[0] += 3 * zCyBxA * coeff.x3y3z0;
    derivative1[2] += zCyBxA * coeff.x2y3z1;
    zCyBxA = y.x3 * x.x3;
    result += zCyBxA * coeff.x3y3z0;
    derivative1[2] += zCyBxA * coeff.x3y3z1;
    result += z.x1 * coeff.x0y0z1;
    derivative1[0] += z.x1 * coeff.x1y0z1;
    derivative1[1] += z.x1 * coeff.x0y1z1;
    derivative1[2] += 2 * z.x1 * coeff.x0y0z2;
    zCyBxA = z.x1 * x.x1;
    result += zCyBxA * coeff.x1y0z1;
    derivative1[0] += 2 * zCyBxA * coeff.x2y0z1;
    derivative1[1] += zCyBxA * coeff.x1y1z1;
    derivative1[2] += 2 * zCyBxA * coeff.x1y0z2;
    zCyBxA = z.x1 * x.x2;
    result += zCyBxA * coeff.x2y0z1;
    derivative1[0] += 3 * zCyBxA * coeff.x3y0z1;
    derivative1[1] += zCyBxA * coeff.x2y1z1;
    derivative1[2] += 2 * zCyBxA * coeff.x2y0z2;
    zCyBxA = z.x1 * x.x3;
    result += zCyBxA * coeff.x3y0z1;
    derivative1[1] += zCyBxA * coeff.x3y1z1;
    derivative1[2] += 2 * zCyBxA * coeff.x3y0z2;
    zCyB = z.x1 * y.x1;
    result += zCyB * coeff.x0y1z1;
    derivative1[0] += zCyB * coeff.x1y1z1;
    derivative1[1] += 2 * zCyB * coeff.x0y2z1;
    derivative1[2] += 2 * zCyB * coeff.x0y1z2;
    zCyBxA = zCyB * x.x1;
    result += zCyBxA * coeff.x1y1z1;
    derivative1[0] += 2 * zCyBxA * coeff.x2y1z1;
    derivative1[1] += 2 * zCyBxA * coeff.x1y2z1;
    derivative1[2] += 2 * zCyBxA * coeff.x1y1z2;
    zCyBxA = zCyB * x.x2;
    result += zCyBxA * coeff.x2y1z1;
    derivative1[0] += 3 * zCyBxA * coeff.x3y1z1;
    derivative1[1] += 2 * zCyBxA * coeff.x2y2z1;
    derivative1[2] += 2 * zCyBxA * coeff.x2y1z2;
    zCyBxA = zCyB * x.x3;
    result += zCyBxA * coeff.x3y1z1;
    derivative1[1] += 2 * zCyBxA * coeff.x3y2z1;
    derivative1[2] += 2 * zCyBxA * coeff.x3y1z2;
    zCyB = z.x1 * y.x2;
    result += zCyB * coeff.x0y2z1;
    derivative1[0] += zCyB * coeff.x1y2z1;
    derivative1[1] += 3 * zCyB * coeff.x0y3z1;
    derivative1[2] += 2 * zCyB * coeff.x0y2z2;
    zCyBxA = zCyB * x.x1;
    result += zCyBxA * coeff.x1y2z1;
    derivative1[0] += 2 * zCyBxA * coeff.x2y2z1;
    derivative1[1] += 3 * zCyBxA * coeff.x1y3z1;
    derivative1[2] += 2 * zCyBxA * coeff.x1y2z2;
    zCyBxA = zCyB * x.x2;
    result += zCyBxA * coeff.x2y2z1;
    derivative1[0] += 3 * zCyBxA * coeff.x3y2z1;
    derivative1[1] += 3 * zCyBxA * coeff.x2y3z1;
    derivative1[2] += 2 * zCyBxA * coeff.x2y2z2;
    zCyBxA = zCyB * x.x3;
    result += zCyBxA * coeff.x3y2z1;
    derivative1[1] += 3 * zCyBxA * coeff.x3y3z1;
    derivative1[2] += 2 * zCyBxA * coeff.x3y2z2;
    zCyB = z.x1 * y.x3;
    result += zCyB * coeff.x0y3z1;
    derivative1[0] += zCyB * coeff.x1y3z1;
    derivative1[2] += 2 * zCyB * coeff.x0y3z2;
    zCyBxA = zCyB * x.x1;
    result += zCyBxA * coeff.x1y3z1;
    derivative1[0] += 2 * zCyBxA * coeff.x2y3z1;
    derivative1[2] += 2 * zCyBxA * coeff.x1y3z2;
    zCyBxA = zCyB * x.x2;
    result += zCyBxA * coeff.x2y3z1;
    derivative1[0] += 3 * zCyBxA * coeff.x3y3z1;
    derivative1[2] += 2 * zCyBxA * coeff.x2y3z2;
    zCyBxA = zCyB * x.x3;
    result += zCyBxA * coeff.x3y3z1;
    derivative1[2] += 2 * zCyBxA * coeff.x3y3z2;
    result += z.x2 * coeff.x0y0z2;
    derivative1[0] += z.x2 * coeff.x1y0z2;
    derivative1[1] += z.x2 * coeff.x0y1z2;
    derivative1[2] += 3 * z.x2 * coeff.x0y0z3;
    zCyBxA = z.x2 * x.x1;
    result += zCyBxA * coeff.x1y0z2;
    derivative1[0] += 2 * zCyBxA * coeff.x2y0z2;
    derivative1[1] += zCyBxA * coeff.x1y1z2;
    derivative1[2] += 3 * zCyBxA * coeff.x1y0z3;
    zCyBxA = z.x2 * x.x2;
    result += zCyBxA * coeff.x2y0z2;
    derivative1[0] += 3 * zCyBxA * coeff.x3y0z2;
    derivative1[1] += zCyBxA * coeff.x2y1z2;
    derivative1[2] += 3 * zCyBxA * coeff.x2y0z3;
    zCyBxA = z.x2 * x.x3;
    result += zCyBxA * coeff.x3y0z2;
    derivative1[1] += zCyBxA * coeff.x3y1z2;
    derivative1[2] += 3 * zCyBxA * coeff.x3y0z3;
    zCyB = z.x2 * y.x1;
    result += zCyB * coeff.x0y1z2;
    derivative1[0] += zCyB * coeff.x1y1z2;
    derivative1[1] += 2 * zCyB * coeff.x0y2z2;
    derivative1[2] += 3 * zCyB * coeff.x0y1z3;
    zCyBxA = zCyB * x.x1;
    result += zCyBxA * coeff.x1y1z2;
    derivative1[0] += 2 * zCyBxA * coeff.x2y1z2;
    derivative1[1] += 2 * zCyBxA * coeff.x1y2z2;
    derivative1[2] += 3 * zCyBxA * coeff.x1y1z3;
    zCyBxA = zCyB * x.x2;
    result += zCyBxA * coeff.x2y1z2;
    derivative1[0] += 3 * zCyBxA * coeff.x3y1z2;
    derivative1[1] += 2 * zCyBxA * coeff.x2y2z2;
    derivative1[2] += 3 * zCyBxA * coeff.x2y1z3;
    zCyBxA = zCyB * x.x3;
    result += zCyBxA * coeff.x3y1z2;
    derivative1[1] += 2 * zCyBxA * coeff.x3y2z2;
    derivative1[2] += 3 * zCyBxA * coeff.x3y1z3;
    zCyB = z.x2 * y.x2;
    result += zCyB * coeff.x0y2z2;
    derivative1[0] += zCyB * coeff.x1y2z2;
    derivative1[1] += 3 * zCyB * coeff.x0y3z2;
    derivative1[2] += 3 * zCyB * coeff.x0y2z3;
    zCyBxA = zCyB * x.x1;
    result += zCyBxA * coeff.x1y2z2;
    derivative1[0] += 2 * zCyBxA * coeff.x2y2z2;
    derivative1[1] += 3 * zCyBxA * coeff.x1y3z2;
    derivative1[2] += 3 * zCyBxA * coeff.x1y2z3;
    zCyBxA = zCyB * x.x2;
    result += zCyBxA * coeff.x2y2z2;
    derivative1[0] += 3 * zCyBxA * coeff.x3y2z2;
    derivative1[1] += 3 * zCyBxA * coeff.x2y3z2;
    derivative1[2] += 3 * zCyBxA * coeff.x2y2z3;
    zCyBxA = zCyB * x.x3;
    result += zCyBxA * coeff.x3y2z2;
    derivative1[1] += 3 * zCyBxA * coeff.x3y3z2;
    derivative1[2] += 3 * zCyBxA * coeff.x3y2z3;
    zCyB = z.x2 * y.x3;
    result += zCyB * coeff.x0y3z2;
    derivative1[0] += zCyB * coeff.x1y3z2;
    derivative1[2] += 3 * zCyB * coeff.x0y3z3;
    zCyBxA = zCyB * x.x1;
    result += zCyBxA * coeff.x1y3z2;
    derivative1[0] += 2 * zCyBxA * coeff.x2y3z2;
    derivative1[2] += 3 * zCyBxA * coeff.x1y3z3;
    zCyBxA = zCyB * x.x2;
    result += zCyBxA * coeff.x2y3z2;
    derivative1[0] += 3 * zCyBxA * coeff.x3y3z2;
    derivative1[2] += 3 * zCyBxA * coeff.x2y3z3;
    zCyBxA = zCyB * x.x3;
    result += zCyBxA * coeff.x3y3z2;
    derivative1[2] += 3 * zCyBxA * coeff.x3y3z3;
    result += z.x3 * coeff.x0y0z3;
    derivative1[0] += z.x3 * coeff.x1y0z3;
    derivative1[1] += z.x3 * coeff.x0y1z3;
    zCyBxA = z.x3 * x.x1;
    result += zCyBxA * coeff.x1y0z3;
    derivative1[0] += 2 * zCyBxA * coeff.x2y0z3;
    derivative1[1] += zCyBxA * coeff.x1y1z3;
    zCyBxA = z.x3 * x.x2;
    result += zCyBxA * coeff.x2y0z3;
    derivative1[0] += 3 * zCyBxA * coeff.x3y0z3;
    derivative1[1] += zCyBxA * coeff.x2y1z3;
    zCyBxA = z.x3 * x.x3;
    result += zCyBxA * coeff.x3y0z3;
    derivative1[1] += zCyBxA * coeff.x3y1z3;
    zCyB = z.x3 * y.x1;
    result += zCyB * coeff.x0y1z3;
    derivative1[0] += zCyB * coeff.x1y1z3;
    derivative1[1] += 2 * zCyB * coeff.x0y2z3;
    zCyBxA = zCyB * x.x1;
    result += zCyBxA * coeff.x1y1z3;
    derivative1[0] += 2 * zCyBxA * coeff.x2y1z3;
    derivative1[1] += 2 * zCyBxA * coeff.x1y2z3;
    zCyBxA = zCyB * x.x2;
    result += zCyBxA * coeff.x2y1z3;
    derivative1[0] += 3 * zCyBxA * coeff.x3y1z3;
    derivative1[1] += 2 * zCyBxA * coeff.x2y2z3;
    zCyBxA = zCyB * x.x3;
    result += zCyBxA * coeff.x3y1z3;
    derivative1[1] += 2 * zCyBxA * coeff.x3y2z3;
    zCyB = z.x3 * y.x2;
    result += zCyB * coeff.x0y2z3;
    derivative1[0] += zCyB * coeff.x1y2z3;
    derivative1[1] += 3 * zCyB * coeff.x0y3z3;
    zCyBxA = zCyB * x.x1;
    result += zCyBxA * coeff.x1y2z3;
    derivative1[0] += 2 * zCyBxA * coeff.x2y2z3;
    derivative1[1] += 3 * zCyBxA * coeff.x1y3z3;
    zCyBxA = zCyB * x.x2;
    result += zCyBxA * coeff.x2y2z3;
    derivative1[0] += 3 * zCyBxA * coeff.x3y2z3;
    derivative1[1] += 3 * zCyBxA * coeff.x2y3z3;
    zCyBxA = zCyB * x.x3;
    result += zCyBxA * coeff.x3y2z3;
    derivative1[1] += 3 * zCyBxA * coeff.x3y3z3;
    zCyB = z.x3 * y.x3;
    result += zCyB * coeff.x0y3z3;
    derivative1[0] += zCyB * coeff.x1y3z3;
    zCyBxA = zCyB * x.x1;
    result += zCyBxA * coeff.x1y3z3;
    derivative1[0] += 2 * zCyBxA * coeff.x2y3z3;
    zCyBxA = zCyB * x.x2;
    result += zCyBxA * coeff.x2y3z3;
    derivative1[0] += 3 * zCyBxA * coeff.x3y3z3;
    zCyBxA = zCyB * x.x3;
    result += zCyBxA * coeff.x3y3z3;

    return result;
  }

  @Override
  protected double value2(final CubicSplinePosition x, final CubicSplinePosition y,
      final CubicSplinePosition z, final double[] derivative1, double[] derivative2) {
    double zCyB;
    double zCyBxA;
    double result = 0;
    derivative1[0] = 0;
    derivative1[1] = 0;
    derivative1[2] = 0;
    derivative2[0] = 0;
    derivative2[1] = 0;
    derivative2[2] = 0;
    
    result += coeff.x0y0z0;
    derivative1[0] += coeff.x1y0z0;
    derivative2[0] += 2 * coeff.x2y0z0;
    derivative1[1] += coeff.x0y1z0;
    derivative2[1] += 2 * coeff.x0y2z0;
    derivative1[2] += coeff.x0y0z1;
    derivative2[2] += 2 * coeff.x0y0z2;
    result += x.x1 * coeff.x1y0z0;
    derivative1[0] += 2 * x.x1 * coeff.x2y0z0;
    derivative2[0] += 6 * x.x1 * coeff.x3y0z0;
    derivative1[1] += x.x1 * coeff.x1y1z0;
    derivative2[1] += 2 * x.x1 * coeff.x1y2z0;
    derivative1[2] += x.x1 * coeff.x1y0z1;
    derivative2[2] += 2 * x.x1 * coeff.x1y0z2;
    result += x.x2 * coeff.x2y0z0;
    derivative1[0] += 3 * x.x2 * coeff.x3y0z0;
    derivative1[1] += x.x2 * coeff.x2y1z0;
    derivative2[1] += 2 * x.x2 * coeff.x2y2z0;
    derivative1[2] += x.x2 * coeff.x2y0z1;
    derivative2[2] += 2 * x.x2 * coeff.x2y0z2;
    result += x.x3 * coeff.x3y0z0;
    derivative1[1] += x.x3 * coeff.x3y1z0;
    derivative2[1] += 2 * x.x3 * coeff.x3y2z0;
    derivative1[2] += x.x3 * coeff.x3y0z1;
    derivative2[2] += 2 * x.x3 * coeff.x3y0z2;
    result += y.x1 * coeff.x0y1z0;
    derivative1[0] += y.x1 * coeff.x1y1z0;
    derivative2[0] += 2 * y.x1 * coeff.x2y1z0;
    derivative1[1] += 2 * y.x1 * coeff.x0y2z0;
    derivative2[1] += 6 * y.x1 * coeff.x0y3z0;
    derivative1[2] += y.x1 * coeff.x0y1z1;
    derivative2[2] += 2 * y.x1 * coeff.x0y1z2;
    zCyBxA = y.x1 * x.x1;
    result += zCyBxA * coeff.x1y1z0;
    derivative1[0] += 2 * zCyBxA * coeff.x2y1z0;
    derivative2[0] += 6 * zCyBxA * coeff.x3y1z0;
    derivative1[1] += 2 * zCyBxA * coeff.x1y2z0;
    derivative2[1] += 6 * zCyBxA * coeff.x1y3z0;
    derivative1[2] += zCyBxA * coeff.x1y1z1;
    derivative2[2] += 2 * zCyBxA * coeff.x1y1z2;
    zCyBxA = y.x1 * x.x2;
    result += zCyBxA * coeff.x2y1z0;
    derivative1[0] += 3 * zCyBxA * coeff.x3y1z0;
    derivative1[1] += 2 * zCyBxA * coeff.x2y2z0;
    derivative2[1] += 6 * zCyBxA * coeff.x2y3z0;
    derivative1[2] += zCyBxA * coeff.x2y1z1;
    derivative2[2] += 2 * zCyBxA * coeff.x2y1z2;
    zCyBxA = y.x1 * x.x3;
    result += zCyBxA * coeff.x3y1z0;
    derivative1[1] += 2 * zCyBxA * coeff.x3y2z0;
    derivative2[1] += 6 * zCyBxA * coeff.x3y3z0;
    derivative1[2] += zCyBxA * coeff.x3y1z1;
    derivative2[2] += 2 * zCyBxA * coeff.x3y1z2;
    result += y.x2 * coeff.x0y2z0;
    derivative1[0] += y.x2 * coeff.x1y2z0;
    derivative2[0] += 2 * y.x2 * coeff.x2y2z0;
    derivative1[1] += 3 * y.x2 * coeff.x0y3z0;
    derivative1[2] += y.x2 * coeff.x0y2z1;
    derivative2[2] += 2 * y.x2 * coeff.x0y2z2;
    zCyBxA = y.x2 * x.x1;
    result += zCyBxA * coeff.x1y2z0;
    derivative1[0] += 2 * zCyBxA * coeff.x2y2z0;
    derivative2[0] += 6 * zCyBxA * coeff.x3y2z0;
    derivative1[1] += 3 * zCyBxA * coeff.x1y3z0;
    derivative1[2] += zCyBxA * coeff.x1y2z1;
    derivative2[2] += 2 * zCyBxA * coeff.x1y2z2;
    zCyBxA = y.x2 * x.x2;
    result += zCyBxA * coeff.x2y2z0;
    derivative1[0] += 3 * zCyBxA * coeff.x3y2z0;
    derivative1[1] += 3 * zCyBxA * coeff.x2y3z0;
    derivative1[2] += zCyBxA * coeff.x2y2z1;
    derivative2[2] += 2 * zCyBxA * coeff.x2y2z2;
    zCyBxA = y.x2 * x.x3;
    result += zCyBxA * coeff.x3y2z0;
    derivative1[1] += 3 * zCyBxA * coeff.x3y3z0;
    derivative1[2] += zCyBxA * coeff.x3y2z1;
    derivative2[2] += 2 * zCyBxA * coeff.x3y2z2;
    result += y.x3 * coeff.x0y3z0;
    derivative1[0] += y.x3 * coeff.x1y3z0;
    derivative2[0] += 2 * y.x3 * coeff.x2y3z0;
    derivative1[2] += y.x3 * coeff.x0y3z1;
    derivative2[2] += 2 * y.x3 * coeff.x0y3z2;
    zCyBxA = y.x3 * x.x1;
    result += zCyBxA * coeff.x1y3z0;
    derivative1[0] += 2 * zCyBxA * coeff.x2y3z0;
    derivative2[0] += 6 * zCyBxA * coeff.x3y3z0;
    derivative1[2] += zCyBxA * coeff.x1y3z1;
    derivative2[2] += 2 * zCyBxA * coeff.x1y3z2;
    zCyBxA = y.x3 * x.x2;
    result += zCyBxA * coeff.x2y3z0;
    derivative1[0] += 3 * zCyBxA * coeff.x3y3z0;
    derivative1[2] += zCyBxA * coeff.x2y3z1;
    derivative2[2] += 2 * zCyBxA * coeff.x2y3z2;
    zCyBxA = y.x3 * x.x3;
    result += zCyBxA * coeff.x3y3z0;
    derivative1[2] += zCyBxA * coeff.x3y3z1;
    derivative2[2] += 2 * zCyBxA * coeff.x3y3z2;
    result += z.x1 * coeff.x0y0z1;
    derivative1[0] += z.x1 * coeff.x1y0z1;
    derivative2[0] += 2 * z.x1 * coeff.x2y0z1;
    derivative1[1] += z.x1 * coeff.x0y1z1;
    derivative2[1] += 2 * z.x1 * coeff.x0y2z1;
    derivative1[2] += 2 * z.x1 * coeff.x0y0z2;
    derivative2[2] += 6 * z.x1 * coeff.x0y0z3;
    zCyBxA = z.x1 * x.x1;
    result += zCyBxA * coeff.x1y0z1;
    derivative1[0] += 2 * zCyBxA * coeff.x2y0z1;
    derivative2[0] += 6 * zCyBxA * coeff.x3y0z1;
    derivative1[1] += zCyBxA * coeff.x1y1z1;
    derivative2[1] += 2 * zCyBxA * coeff.x1y2z1;
    derivative1[2] += 2 * zCyBxA * coeff.x1y0z2;
    derivative2[2] += 6 * zCyBxA * coeff.x1y0z3;
    zCyBxA = z.x1 * x.x2;
    result += zCyBxA * coeff.x2y0z1;
    derivative1[0] += 3 * zCyBxA * coeff.x3y0z1;
    derivative1[1] += zCyBxA * coeff.x2y1z1;
    derivative2[1] += 2 * zCyBxA * coeff.x2y2z1;
    derivative1[2] += 2 * zCyBxA * coeff.x2y0z2;
    derivative2[2] += 6 * zCyBxA * coeff.x2y0z3;
    zCyBxA = z.x1 * x.x3;
    result += zCyBxA * coeff.x3y0z1;
    derivative1[1] += zCyBxA * coeff.x3y1z1;
    derivative2[1] += 2 * zCyBxA * coeff.x3y2z1;
    derivative1[2] += 2 * zCyBxA * coeff.x3y0z2;
    derivative2[2] += 6 * zCyBxA * coeff.x3y0z3;
    zCyB = z.x1 * y.x1;
    result += zCyB * coeff.x0y1z1;
    derivative1[0] += zCyB * coeff.x1y1z1;
    derivative2[0] += 2 * zCyB * coeff.x2y1z1;
    derivative1[1] += 2 * zCyB * coeff.x0y2z1;
    derivative2[1] += 6 * zCyB * coeff.x0y3z1;
    derivative1[2] += 2 * zCyB * coeff.x0y1z2;
    derivative2[2] += 6 * zCyB * coeff.x0y1z3;
    zCyBxA = zCyB * x.x1;
    result += zCyBxA * coeff.x1y1z1;
    derivative1[0] += 2 * zCyBxA * coeff.x2y1z1;
    derivative2[0] += 6 * zCyBxA * coeff.x3y1z1;
    derivative1[1] += 2 * zCyBxA * coeff.x1y2z1;
    derivative2[1] += 6 * zCyBxA * coeff.x1y3z1;
    derivative1[2] += 2 * zCyBxA * coeff.x1y1z2;
    derivative2[2] += 6 * zCyBxA * coeff.x1y1z3;
    zCyBxA = zCyB * x.x2;
    result += zCyBxA * coeff.x2y1z1;
    derivative1[0] += 3 * zCyBxA * coeff.x3y1z1;
    derivative1[1] += 2 * zCyBxA * coeff.x2y2z1;
    derivative2[1] += 6 * zCyBxA * coeff.x2y3z1;
    derivative1[2] += 2 * zCyBxA * coeff.x2y1z2;
    derivative2[2] += 6 * zCyBxA * coeff.x2y1z3;
    zCyBxA = zCyB * x.x3;
    result += zCyBxA * coeff.x3y1z1;
    derivative1[1] += 2 * zCyBxA * coeff.x3y2z1;
    derivative2[1] += 6 * zCyBxA * coeff.x3y3z1;
    derivative1[2] += 2 * zCyBxA * coeff.x3y1z2;
    derivative2[2] += 6 * zCyBxA * coeff.x3y1z3;
    zCyB = z.x1 * y.x2;
    result += zCyB * coeff.x0y2z1;
    derivative1[0] += zCyB * coeff.x1y2z1;
    derivative2[0] += 2 * zCyB * coeff.x2y2z1;
    derivative1[1] += 3 * zCyB * coeff.x0y3z1;
    derivative1[2] += 2 * zCyB * coeff.x0y2z2;
    derivative2[2] += 6 * zCyB * coeff.x0y2z3;
    zCyBxA = zCyB * x.x1;
    result += zCyBxA * coeff.x1y2z1;
    derivative1[0] += 2 * zCyBxA * coeff.x2y2z1;
    derivative2[0] += 6 * zCyBxA * coeff.x3y2z1;
    derivative1[1] += 3 * zCyBxA * coeff.x1y3z1;
    derivative1[2] += 2 * zCyBxA * coeff.x1y2z2;
    derivative2[2] += 6 * zCyBxA * coeff.x1y2z3;
    zCyBxA = zCyB * x.x2;
    result += zCyBxA * coeff.x2y2z1;
    derivative1[0] += 3 * zCyBxA * coeff.x3y2z1;
    derivative1[1] += 3 * zCyBxA * coeff.x2y3z1;
    derivative1[2] += 2 * zCyBxA * coeff.x2y2z2;
    derivative2[2] += 6 * zCyBxA * coeff.x2y2z3;
    zCyBxA = zCyB * x.x3;
    result += zCyBxA * coeff.x3y2z1;
    derivative1[1] += 3 * zCyBxA * coeff.x3y3z1;
    derivative1[2] += 2 * zCyBxA * coeff.x3y2z2;
    derivative2[2] += 6 * zCyBxA * coeff.x3y2z3;
    zCyB = z.x1 * y.x3;
    result += zCyB * coeff.x0y3z1;
    derivative1[0] += zCyB * coeff.x1y3z1;
    derivative2[0] += 2 * zCyB * coeff.x2y3z1;
    derivative1[2] += 2 * zCyB * coeff.x0y3z2;
    derivative2[2] += 6 * zCyB * coeff.x0y3z3;
    zCyBxA = zCyB * x.x1;
    result += zCyBxA * coeff.x1y3z1;
    derivative1[0] += 2 * zCyBxA * coeff.x2y3z1;
    derivative2[0] += 6 * zCyBxA * coeff.x3y3z1;
    derivative1[2] += 2 * zCyBxA * coeff.x1y3z2;
    derivative2[2] += 6 * zCyBxA * coeff.x1y3z3;
    zCyBxA = zCyB * x.x2;
    result += zCyBxA * coeff.x2y3z1;
    derivative1[0] += 3 * zCyBxA * coeff.x3y3z1;
    derivative1[2] += 2 * zCyBxA * coeff.x2y3z2;
    derivative2[2] += 6 * zCyBxA * coeff.x2y3z3;
    zCyBxA = zCyB * x.x3;
    result += zCyBxA * coeff.x3y3z1;
    derivative1[2] += 2 * zCyBxA * coeff.x3y3z2;
    derivative2[2] += 6 * zCyBxA * coeff.x3y3z3;
    result += z.x2 * coeff.x0y0z2;
    derivative1[0] += z.x2 * coeff.x1y0z2;
    derivative2[0] += 2 * z.x2 * coeff.x2y0z2;
    derivative1[1] += z.x2 * coeff.x0y1z2;
    derivative2[1] += 2 * z.x2 * coeff.x0y2z2;
    derivative1[2] += 3 * z.x2 * coeff.x0y0z3;
    zCyBxA = z.x2 * x.x1;
    result += zCyBxA * coeff.x1y0z2;
    derivative1[0] += 2 * zCyBxA * coeff.x2y0z2;
    derivative2[0] += 6 * zCyBxA * coeff.x3y0z2;
    derivative1[1] += zCyBxA * coeff.x1y1z2;
    derivative2[1] += 2 * zCyBxA * coeff.x1y2z2;
    derivative1[2] += 3 * zCyBxA * coeff.x1y0z3;
    zCyBxA = z.x2 * x.x2;
    result += zCyBxA * coeff.x2y0z2;
    derivative1[0] += 3 * zCyBxA * coeff.x3y0z2;
    derivative1[1] += zCyBxA * coeff.x2y1z2;
    derivative2[1] += 2 * zCyBxA * coeff.x2y2z2;
    derivative1[2] += 3 * zCyBxA * coeff.x2y0z3;
    zCyBxA = z.x2 * x.x3;
    result += zCyBxA * coeff.x3y0z2;
    derivative1[1] += zCyBxA * coeff.x3y1z2;
    derivative2[1] += 2 * zCyBxA * coeff.x3y2z2;
    derivative1[2] += 3 * zCyBxA * coeff.x3y0z3;
    zCyB = z.x2 * y.x1;
    result += zCyB * coeff.x0y1z2;
    derivative1[0] += zCyB * coeff.x1y1z2;
    derivative2[0] += 2 * zCyB * coeff.x2y1z2;
    derivative1[1] += 2 * zCyB * coeff.x0y2z2;
    derivative2[1] += 6 * zCyB * coeff.x0y3z2;
    derivative1[2] += 3 * zCyB * coeff.x0y1z3;
    zCyBxA = zCyB * x.x1;
    result += zCyBxA * coeff.x1y1z2;
    derivative1[0] += 2 * zCyBxA * coeff.x2y1z2;
    derivative2[0] += 6 * zCyBxA * coeff.x3y1z2;
    derivative1[1] += 2 * zCyBxA * coeff.x1y2z2;
    derivative2[1] += 6 * zCyBxA * coeff.x1y3z2;
    derivative1[2] += 3 * zCyBxA * coeff.x1y1z3;
    zCyBxA = zCyB * x.x2;
    result += zCyBxA * coeff.x2y1z2;
    derivative1[0] += 3 * zCyBxA * coeff.x3y1z2;
    derivative1[1] += 2 * zCyBxA * coeff.x2y2z2;
    derivative2[1] += 6 * zCyBxA * coeff.x2y3z2;
    derivative1[2] += 3 * zCyBxA * coeff.x2y1z3;
    zCyBxA = zCyB * x.x3;
    result += zCyBxA * coeff.x3y1z2;
    derivative1[1] += 2 * zCyBxA * coeff.x3y2z2;
    derivative2[1] += 6 * zCyBxA * coeff.x3y3z2;
    derivative1[2] += 3 * zCyBxA * coeff.x3y1z3;
    zCyB = z.x2 * y.x2;
    result += zCyB * coeff.x0y2z2;
    derivative1[0] += zCyB * coeff.x1y2z2;
    derivative2[0] += 2 * zCyB * coeff.x2y2z2;
    derivative1[1] += 3 * zCyB * coeff.x0y3z2;
    derivative1[2] += 3 * zCyB * coeff.x0y2z3;
    zCyBxA = zCyB * x.x1;
    result += zCyBxA * coeff.x1y2z2;
    derivative1[0] += 2 * zCyBxA * coeff.x2y2z2;
    derivative2[0] += 6 * zCyBxA * coeff.x3y2z2;
    derivative1[1] += 3 * zCyBxA * coeff.x1y3z2;
    derivative1[2] += 3 * zCyBxA * coeff.x1y2z3;
    zCyBxA = zCyB * x.x2;
    result += zCyBxA * coeff.x2y2z2;
    derivative1[0] += 3 * zCyBxA * coeff.x3y2z2;
    derivative1[1] += 3 * zCyBxA * coeff.x2y3z2;
    derivative1[2] += 3 * zCyBxA * coeff.x2y2z3;
    zCyBxA = zCyB * x.x3;
    result += zCyBxA * coeff.x3y2z2;
    derivative1[1] += 3 * zCyBxA * coeff.x3y3z2;
    derivative1[2] += 3 * zCyBxA * coeff.x3y2z3;
    zCyB = z.x2 * y.x3;
    result += zCyB * coeff.x0y3z2;
    derivative1[0] += zCyB * coeff.x1y3z2;
    derivative2[0] += 2 * zCyB * coeff.x2y3z2;
    derivative1[2] += 3 * zCyB * coeff.x0y3z3;
    zCyBxA = zCyB * x.x1;
    result += zCyBxA * coeff.x1y3z2;
    derivative1[0] += 2 * zCyBxA * coeff.x2y3z2;
    derivative2[0] += 6 * zCyBxA * coeff.x3y3z2;
    derivative1[2] += 3 * zCyBxA * coeff.x1y3z3;
    zCyBxA = zCyB * x.x2;
    result += zCyBxA * coeff.x2y3z2;
    derivative1[0] += 3 * zCyBxA * coeff.x3y3z2;
    derivative1[2] += 3 * zCyBxA * coeff.x2y3z3;
    zCyBxA = zCyB * x.x3;
    result += zCyBxA * coeff.x3y3z2;
    derivative1[2] += 3 * zCyBxA * coeff.x3y3z3;
    result += z.x3 * coeff.x0y0z3;
    derivative1[0] += z.x3 * coeff.x1y0z3;
    derivative2[0] += 2 * z.x3 * coeff.x2y0z3;
    derivative1[1] += z.x3 * coeff.x0y1z3;
    derivative2[1] += 2 * z.x3 * coeff.x0y2z3;
    zCyBxA = z.x3 * x.x1;
    result += zCyBxA * coeff.x1y0z3;
    derivative1[0] += 2 * zCyBxA * coeff.x2y0z3;
    derivative2[0] += 6 * zCyBxA * coeff.x3y0z3;
    derivative1[1] += zCyBxA * coeff.x1y1z3;
    derivative2[1] += 2 * zCyBxA * coeff.x1y2z3;
    zCyBxA = z.x3 * x.x2;
    result += zCyBxA * coeff.x2y0z3;
    derivative1[0] += 3 * zCyBxA * coeff.x3y0z3;
    derivative1[1] += zCyBxA * coeff.x2y1z3;
    derivative2[1] += 2 * zCyBxA * coeff.x2y2z3;
    zCyBxA = z.x3 * x.x3;
    result += zCyBxA * coeff.x3y0z3;
    derivative1[1] += zCyBxA * coeff.x3y1z3;
    derivative2[1] += 2 * zCyBxA * coeff.x3y2z3;
    zCyB = z.x3 * y.x1;
    result += zCyB * coeff.x0y1z3;
    derivative1[0] += zCyB * coeff.x1y1z3;
    derivative2[0] += 2 * zCyB * coeff.x2y1z3;
    derivative1[1] += 2 * zCyB * coeff.x0y2z3;
    derivative2[1] += 6 * zCyB * coeff.x0y3z3;
    zCyBxA = zCyB * x.x1;
    result += zCyBxA * coeff.x1y1z3;
    derivative1[0] += 2 * zCyBxA * coeff.x2y1z3;
    derivative2[0] += 6 * zCyBxA * coeff.x3y1z3;
    derivative1[1] += 2 * zCyBxA * coeff.x1y2z3;
    derivative2[1] += 6 * zCyBxA * coeff.x1y3z3;
    zCyBxA = zCyB * x.x2;
    result += zCyBxA * coeff.x2y1z3;
    derivative1[0] += 3 * zCyBxA * coeff.x3y1z3;
    derivative1[1] += 2 * zCyBxA * coeff.x2y2z3;
    derivative2[1] += 6 * zCyBxA * coeff.x2y3z3;
    zCyBxA = zCyB * x.x3;
    result += zCyBxA * coeff.x3y1z3;
    derivative1[1] += 2 * zCyBxA * coeff.x3y2z3;
    derivative2[1] += 6 * zCyBxA * coeff.x3y3z3;
    zCyB = z.x3 * y.x2;
    result += zCyB * coeff.x0y2z3;
    derivative1[0] += zCyB * coeff.x1y2z3;
    derivative2[0] += 2 * zCyB * coeff.x2y2z3;
    derivative1[1] += 3 * zCyB * coeff.x0y3z3;
    zCyBxA = zCyB * x.x1;
    result += zCyBxA * coeff.x1y2z3;
    derivative1[0] += 2 * zCyBxA * coeff.x2y2z3;
    derivative2[0] += 6 * zCyBxA * coeff.x3y2z3;
    derivative1[1] += 3 * zCyBxA * coeff.x1y3z3;
    zCyBxA = zCyB * x.x2;
    result += zCyBxA * coeff.x2y2z3;
    derivative1[0] += 3 * zCyBxA * coeff.x3y2z3;
    derivative1[1] += 3 * zCyBxA * coeff.x2y3z3;
    zCyBxA = zCyB * x.x3;
    result += zCyBxA * coeff.x3y2z3;
    derivative1[1] += 3 * zCyBxA * coeff.x3y3z3;
    zCyB = z.x3 * y.x3;
    result += zCyB * coeff.x0y3z3;
    derivative1[0] += zCyB * coeff.x1y3z3;
    derivative2[0] += 2 * zCyB * coeff.x2y3z3;
    zCyBxA = zCyB * x.x1;
    result += zCyBxA * coeff.x1y3z3;
    derivative1[0] += 2 * zCyBxA * coeff.x2y3z3;
    derivative2[0] += 6 * zCyBxA * coeff.x3y3z3;
    zCyBxA = zCyB * x.x2;
    result += zCyBxA * coeff.x2y3z3;
    derivative1[0] += 3 * zCyBxA * coeff.x3y3z3;
    zCyBxA = zCyB * x.x3;
    result += zCyBxA * coeff.x3y3z3;

    return result;
  }

  // CHECKSTYLE.ON: VariableDeclarationUsageDistance

  @Override
  public double value(DoubleCubicSplineData table) {
    return table.x0y0z0 * coeff.x0y0z0 + table.x1y0z0 * coeff.x1y0z0 + table.x2y0z0 * coeff.x2y0z0
        + table.x3y0z0 * coeff.x3y0z0 + table.x0y1z0 * coeff.x0y1z0 + table.x1y1z0 * coeff.x1y1z0
        + table.x2y1z0 * coeff.x2y1z0 + table.x3y1z0 * coeff.x3y1z0 + table.x0y2z0 * coeff.x0y2z0
        + table.x1y2z0 * coeff.x1y2z0 + table.x2y2z0 * coeff.x2y2z0 + table.x3y2z0 * coeff.x3y2z0
        + table.x0y3z0 * coeff.x0y3z0 + table.x1y3z0 * coeff.x1y3z0 + table.x2y3z0 * coeff.x2y3z0
        + table.x3y3z0 * coeff.x3y3z0 + table.x0y0z1 * coeff.x0y0z1 + table.x1y0z1 * coeff.x1y0z1
        + table.x2y0z1 * coeff.x2y0z1 + table.x3y0z1 * coeff.x3y0z1 + table.x0y1z1 * coeff.x0y1z1
        + table.x1y1z1 * coeff.x1y1z1 + table.x2y1z1 * coeff.x2y1z1 + table.x3y1z1 * coeff.x3y1z1
        + table.x0y2z1 * coeff.x0y2z1 + table.x1y2z1 * coeff.x1y2z1 + table.x2y2z1 * coeff.x2y2z1
        + table.x3y2z1 * coeff.x3y2z1 + table.x0y3z1 * coeff.x0y3z1 + table.x1y3z1 * coeff.x1y3z1
        + table.x2y3z1 * coeff.x2y3z1 + table.x3y3z1 * coeff.x3y3z1 + table.x0y0z2 * coeff.x0y0z2
        + table.x1y0z2 * coeff.x1y0z2 + table.x2y0z2 * coeff.x2y0z2 + table.x3y0z2 * coeff.x3y0z2
        + table.x0y1z2 * coeff.x0y1z2 + table.x1y1z2 * coeff.x1y1z2 + table.x2y1z2 * coeff.x2y1z2
        + table.x3y1z2 * coeff.x3y1z2 + table.x0y2z2 * coeff.x0y2z2 + table.x1y2z2 * coeff.x1y2z2
        + table.x2y2z2 * coeff.x2y2z2 + table.x3y2z2 * coeff.x3y2z2 + table.x0y3z2 * coeff.x0y3z2
        + table.x1y3z2 * coeff.x1y3z2 + table.x2y3z2 * coeff.x2y3z2 + table.x3y3z2 * coeff.x3y3z2
        + table.x0y0z3 * coeff.x0y0z3 + table.x1y0z3 * coeff.x1y0z3 + table.x2y0z3 * coeff.x2y0z3
        + table.x3y0z3 * coeff.x3y0z3 + table.x0y1z3 * coeff.x0y1z3 + table.x1y1z3 * coeff.x1y1z3
        + table.x2y1z3 * coeff.x2y1z3 + table.x3y1z3 * coeff.x3y1z3 + table.x0y2z3 * coeff.x0y2z3
        + table.x1y2z3 * coeff.x1y2z3 + table.x2y2z3 * coeff.x2y2z3 + table.x3y2z3 * coeff.x3y2z3
        + table.x0y3z3 * coeff.x0y3z3 + table.x1y3z3 * coeff.x1y3z3 + table.x2y3z3 * coeff.x2y3z3
        + table.x3y3z3 * coeff.x3y3z3;
  }

  @Override
  public double value(FloatCubicSplineData table) {
    return table.x0y0z0 * coeff.x0y0z0 + table.x1y0z0 * coeff.x1y0z0 + table.x2y0z0 * coeff.x2y0z0
        + table.x3y0z0 * coeff.x3y0z0 + table.x0y1z0 * coeff.x0y1z0 + table.x1y1z0 * coeff.x1y1z0
        + table.x2y1z0 * coeff.x2y1z0 + table.x3y1z0 * coeff.x3y1z0 + table.x0y2z0 * coeff.x0y2z0
        + table.x1y2z0 * coeff.x1y2z0 + table.x2y2z0 * coeff.x2y2z0 + table.x3y2z0 * coeff.x3y2z0
        + table.x0y3z0 * coeff.x0y3z0 + table.x1y3z0 * coeff.x1y3z0 + table.x2y3z0 * coeff.x2y3z0
        + table.x3y3z0 * coeff.x3y3z0 + table.x0y0z1 * coeff.x0y0z1 + table.x1y0z1 * coeff.x1y0z1
        + table.x2y0z1 * coeff.x2y0z1 + table.x3y0z1 * coeff.x3y0z1 + table.x0y1z1 * coeff.x0y1z1
        + table.x1y1z1 * coeff.x1y1z1 + table.x2y1z1 * coeff.x2y1z1 + table.x3y1z1 * coeff.x3y1z1
        + table.x0y2z1 * coeff.x0y2z1 + table.x1y2z1 * coeff.x1y2z1 + table.x2y2z1 * coeff.x2y2z1
        + table.x3y2z1 * coeff.x3y2z1 + table.x0y3z1 * coeff.x0y3z1 + table.x1y3z1 * coeff.x1y3z1
        + table.x2y3z1 * coeff.x2y3z1 + table.x3y3z1 * coeff.x3y3z1 + table.x0y0z2 * coeff.x0y0z2
        + table.x1y0z2 * coeff.x1y0z2 + table.x2y0z2 * coeff.x2y0z2 + table.x3y0z2 * coeff.x3y0z2
        + table.x0y1z2 * coeff.x0y1z2 + table.x1y1z2 * coeff.x1y1z2 + table.x2y1z2 * coeff.x2y1z2
        + table.x3y1z2 * coeff.x3y1z2 + table.x0y2z2 * coeff.x0y2z2 + table.x1y2z2 * coeff.x1y2z2
        + table.x2y2z2 * coeff.x2y2z2 + table.x3y2z2 * coeff.x3y2z2 + table.x0y3z2 * coeff.x0y3z2
        + table.x1y3z2 * coeff.x1y3z2 + table.x2y3z2 * coeff.x2y3z2 + table.x3y3z2 * coeff.x3y3z2
        + table.x0y0z3 * coeff.x0y0z3 + table.x1y0z3 * coeff.x1y0z3 + table.x2y0z3 * coeff.x2y0z3
        + table.x3y0z3 * coeff.x3y0z3 + table.x0y1z3 * coeff.x0y1z3 + table.x1y1z3 * coeff.x1y1z3
        + table.x2y1z3 * coeff.x2y1z3 + table.x3y1z3 * coeff.x3y1z3 + table.x0y2z3 * coeff.x0y2z3
        + table.x1y2z3 * coeff.x1y2z3 + table.x2y2z3 * coeff.x2y2z3 + table.x3y2z3 * coeff.x3y2z3
        + table.x0y3z3 * coeff.x0y3z3 + table.x1y3z3 * coeff.x1y3z3 + table.x2y3z3 * coeff.x2y3z3
        + table.x3y3z3 * coeff.x3y3z3;
  }

  @Override
  public double value(DoubleCubicSplineData table, double[] derivative1) {
    derivative1[0] = table.x0y0z0 * coeff.x1y0z0 + 2 * table.x1y0z0 * coeff.x2y0z0
        + 3 * table.x2y0z0 * coeff.x3y0z0 + table.x0y1z0 * coeff.x1y1z0
        + 2 * table.x1y1z0 * coeff.x2y1z0 + 3 * table.x2y1z0 * coeff.x3y1z0
        + table.x0y2z0 * coeff.x1y2z0 + 2 * table.x1y2z0 * coeff.x2y2z0
        + 3 * table.x2y2z0 * coeff.x3y2z0 + table.x0y3z0 * coeff.x1y3z0
        + 2 * table.x1y3z0 * coeff.x2y3z0 + 3 * table.x2y3z0 * coeff.x3y3z0
        + table.x0y0z1 * coeff.x1y0z1 + 2 * table.x1y0z1 * coeff.x2y0z1
        + 3 * table.x2y0z1 * coeff.x3y0z1 + table.x0y1z1 * coeff.x1y1z1
        + 2 * table.x1y1z1 * coeff.x2y1z1 + 3 * table.x2y1z1 * coeff.x3y1z1
        + table.x0y2z1 * coeff.x1y2z1 + 2 * table.x1y2z1 * coeff.x2y2z1
        + 3 * table.x2y2z1 * coeff.x3y2z1 + table.x0y3z1 * coeff.x1y3z1
        + 2 * table.x1y3z1 * coeff.x2y3z1 + 3 * table.x2y3z1 * coeff.x3y3z1
        + table.x0y0z2 * coeff.x1y0z2 + 2 * table.x1y0z2 * coeff.x2y0z2
        + 3 * table.x2y0z2 * coeff.x3y0z2 + table.x0y1z2 * coeff.x1y1z2
        + 2 * table.x1y1z2 * coeff.x2y1z2 + 3 * table.x2y1z2 * coeff.x3y1z2
        + table.x0y2z2 * coeff.x1y2z2 + 2 * table.x1y2z2 * coeff.x2y2z2
        + 3 * table.x2y2z2 * coeff.x3y2z2 + table.x0y3z2 * coeff.x1y3z2
        + 2 * table.x1y3z2 * coeff.x2y3z2 + 3 * table.x2y3z2 * coeff.x3y3z2
        + table.x0y0z3 * coeff.x1y0z3 + 2 * table.x1y0z3 * coeff.x2y0z3
        + 3 * table.x2y0z3 * coeff.x3y0z3 + table.x0y1z3 * coeff.x1y1z3
        + 2 * table.x1y1z3 * coeff.x2y1z3 + 3 * table.x2y1z3 * coeff.x3y1z3
        + table.x0y2z3 * coeff.x1y2z3 + 2 * table.x1y2z3 * coeff.x2y2z3
        + 3 * table.x2y2z3 * coeff.x3y2z3 + table.x0y3z3 * coeff.x1y3z3
        + 2 * table.x1y3z3 * coeff.x2y3z3 + 3 * table.x2y3z3 * coeff.x3y3z3;
    derivative1[1] = table.x0y0z0 * coeff.x0y1z0 + table.x1y0z0 * coeff.x1y1z0
        + table.x2y0z0 * coeff.x2y1z0 + table.x3y0z0 * coeff.x3y1z0
        + 2 * table.x0y1z0 * coeff.x0y2z0 + 2 * table.x1y1z0 * coeff.x1y2z0
        + 2 * table.x2y1z0 * coeff.x2y2z0 + 2 * table.x3y1z0 * coeff.x3y2z0
        + 3 * table.x0y2z0 * coeff.x0y3z0 + 3 * table.x1y2z0 * coeff.x1y3z0
        + 3 * table.x2y2z0 * coeff.x2y3z0 + 3 * table.x3y2z0 * coeff.x3y3z0
        + table.x0y0z1 * coeff.x0y1z1 + table.x1y0z1 * coeff.x1y1z1 + table.x2y0z1 * coeff.x2y1z1
        + table.x3y0z1 * coeff.x3y1z1 + 2 * table.x0y1z1 * coeff.x0y2z1
        + 2 * table.x1y1z1 * coeff.x1y2z1 + 2 * table.x2y1z1 * coeff.x2y2z1
        + 2 * table.x3y1z1 * coeff.x3y2z1 + 3 * table.x0y2z1 * coeff.x0y3z1
        + 3 * table.x1y2z1 * coeff.x1y3z1 + 3 * table.x2y2z1 * coeff.x2y3z1
        + 3 * table.x3y2z1 * coeff.x3y3z1 + table.x0y0z2 * coeff.x0y1z2
        + table.x1y0z2 * coeff.x1y1z2 + table.x2y0z2 * coeff.x2y1z2 + table.x3y0z2 * coeff.x3y1z2
        + 2 * table.x0y1z2 * coeff.x0y2z2 + 2 * table.x1y1z2 * coeff.x1y2z2
        + 2 * table.x2y1z2 * coeff.x2y2z2 + 2 * table.x3y1z2 * coeff.x3y2z2
        + 3 * table.x0y2z2 * coeff.x0y3z2 + 3 * table.x1y2z2 * coeff.x1y3z2
        + 3 * table.x2y2z2 * coeff.x2y3z2 + 3 * table.x3y2z2 * coeff.x3y3z2
        + table.x0y0z3 * coeff.x0y1z3 + table.x1y0z3 * coeff.x1y1z3 + table.x2y0z3 * coeff.x2y1z3
        + table.x3y0z3 * coeff.x3y1z3 + 2 * table.x0y1z3 * coeff.x0y2z3
        + 2 * table.x1y1z3 * coeff.x1y2z3 + 2 * table.x2y1z3 * coeff.x2y2z3
        + 2 * table.x3y1z3 * coeff.x3y2z3 + 3 * table.x0y2z3 * coeff.x0y3z3
        + 3 * table.x1y2z3 * coeff.x1y3z3 + 3 * table.x2y2z3 * coeff.x2y3z3
        + 3 * table.x3y2z3 * coeff.x3y3z3;
    derivative1[2] = table.x0y0z0 * coeff.x0y0z1 + table.x1y0z0 * coeff.x1y0z1
        + table.x2y0z0 * coeff.x2y0z1 + table.x3y0z0 * coeff.x3y0z1 + table.x0y1z0 * coeff.x0y1z1
        + table.x1y1z0 * coeff.x1y1z1 + table.x2y1z0 * coeff.x2y1z1 + table.x3y1z0 * coeff.x3y1z1
        + table.x0y2z0 * coeff.x0y2z1 + table.x1y2z0 * coeff.x1y2z1 + table.x2y2z0 * coeff.x2y2z1
        + table.x3y2z0 * coeff.x3y2z1 + table.x0y3z0 * coeff.x0y3z1 + table.x1y3z0 * coeff.x1y3z1
        + table.x2y3z0 * coeff.x2y3z1 + table.x3y3z0 * coeff.x3y3z1
        + 2 * table.x0y0z1 * coeff.x0y0z2 + 2 * table.x1y0z1 * coeff.x1y0z2
        + 2 * table.x2y0z1 * coeff.x2y0z2 + 2 * table.x3y0z1 * coeff.x3y0z2
        + 2 * table.x0y1z1 * coeff.x0y1z2 + 2 * table.x1y1z1 * coeff.x1y1z2
        + 2 * table.x2y1z1 * coeff.x2y1z2 + 2 * table.x3y1z1 * coeff.x3y1z2
        + 2 * table.x0y2z1 * coeff.x0y2z2 + 2 * table.x1y2z1 * coeff.x1y2z2
        + 2 * table.x2y2z1 * coeff.x2y2z2 + 2 * table.x3y2z1 * coeff.x3y2z2
        + 2 * table.x0y3z1 * coeff.x0y3z2 + 2 * table.x1y3z1 * coeff.x1y3z2
        + 2 * table.x2y3z1 * coeff.x2y3z2 + 2 * table.x3y3z1 * coeff.x3y3z2
        + 3 * table.x0y0z2 * coeff.x0y0z3 + 3 * table.x1y0z2 * coeff.x1y0z3
        + 3 * table.x2y0z2 * coeff.x2y0z3 + 3 * table.x3y0z2 * coeff.x3y0z3
        + 3 * table.x0y1z2 * coeff.x0y1z3 + 3 * table.x1y1z2 * coeff.x1y1z3
        + 3 * table.x2y1z2 * coeff.x2y1z3 + 3 * table.x3y1z2 * coeff.x3y1z3
        + 3 * table.x0y2z2 * coeff.x0y2z3 + 3 * table.x1y2z2 * coeff.x1y2z3
        + 3 * table.x2y2z2 * coeff.x2y2z3 + 3 * table.x3y2z2 * coeff.x3y2z3
        + 3 * table.x0y3z2 * coeff.x0y3z3 + 3 * table.x1y3z2 * coeff.x1y3z3
        + 3 * table.x2y3z2 * coeff.x2y3z3 + 3 * table.x3y3z2 * coeff.x3y3z3;
    return table.x0y0z0 * coeff.x0y0z0 + table.x1y0z0 * coeff.x1y0z0 + table.x2y0z0 * coeff.x2y0z0
        + table.x3y0z0 * coeff.x3y0z0 + table.x0y1z0 * coeff.x0y1z0 + table.x1y1z0 * coeff.x1y1z0
        + table.x2y1z0 * coeff.x2y1z0 + table.x3y1z0 * coeff.x3y1z0 + table.x0y2z0 * coeff.x0y2z0
        + table.x1y2z0 * coeff.x1y2z0 + table.x2y2z0 * coeff.x2y2z0 + table.x3y2z0 * coeff.x3y2z0
        + table.x0y3z0 * coeff.x0y3z0 + table.x1y3z0 * coeff.x1y3z0 + table.x2y3z0 * coeff.x2y3z0
        + table.x3y3z0 * coeff.x3y3z0 + table.x0y0z1 * coeff.x0y0z1 + table.x1y0z1 * coeff.x1y0z1
        + table.x2y0z1 * coeff.x2y0z1 + table.x3y0z1 * coeff.x3y0z1 + table.x0y1z1 * coeff.x0y1z1
        + table.x1y1z1 * coeff.x1y1z1 + table.x2y1z1 * coeff.x2y1z1 + table.x3y1z1 * coeff.x3y1z1
        + table.x0y2z1 * coeff.x0y2z1 + table.x1y2z1 * coeff.x1y2z1 + table.x2y2z1 * coeff.x2y2z1
        + table.x3y2z1 * coeff.x3y2z1 + table.x0y3z1 * coeff.x0y3z1 + table.x1y3z1 * coeff.x1y3z1
        + table.x2y3z1 * coeff.x2y3z1 + table.x3y3z1 * coeff.x3y3z1 + table.x0y0z2 * coeff.x0y0z2
        + table.x1y0z2 * coeff.x1y0z2 + table.x2y0z2 * coeff.x2y0z2 + table.x3y0z2 * coeff.x3y0z2
        + table.x0y1z2 * coeff.x0y1z2 + table.x1y1z2 * coeff.x1y1z2 + table.x2y1z2 * coeff.x2y1z2
        + table.x3y1z2 * coeff.x3y1z2 + table.x0y2z2 * coeff.x0y2z2 + table.x1y2z2 * coeff.x1y2z2
        + table.x2y2z2 * coeff.x2y2z2 + table.x3y2z2 * coeff.x3y2z2 + table.x0y3z2 * coeff.x0y3z2
        + table.x1y3z2 * coeff.x1y3z2 + table.x2y3z2 * coeff.x2y3z2 + table.x3y3z2 * coeff.x3y3z2
        + table.x0y0z3 * coeff.x0y0z3 + table.x1y0z3 * coeff.x1y0z3 + table.x2y0z3 * coeff.x2y0z3
        + table.x3y0z3 * coeff.x3y0z3 + table.x0y1z3 * coeff.x0y1z3 + table.x1y1z3 * coeff.x1y1z3
        + table.x2y1z3 * coeff.x2y1z3 + table.x3y1z3 * coeff.x3y1z3 + table.x0y2z3 * coeff.x0y2z3
        + table.x1y2z3 * coeff.x1y2z3 + table.x2y2z3 * coeff.x2y2z3 + table.x3y2z3 * coeff.x3y2z3
        + table.x0y3z3 * coeff.x0y3z3 + table.x1y3z3 * coeff.x1y3z3 + table.x2y3z3 * coeff.x2y3z3
        + table.x3y3z3 * coeff.x3y3z3;
  }

  @Override
  public double value(FloatCubicSplineData table, double[] derivative1) {
    derivative1[0] = table.x0y0z0 * coeff.x1y0z0 + 2 * table.x1y0z0 * coeff.x2y0z0
        + 3 * table.x2y0z0 * coeff.x3y0z0 + table.x0y1z0 * coeff.x1y1z0
        + 2 * table.x1y1z0 * coeff.x2y1z0 + 3 * table.x2y1z0 * coeff.x3y1z0
        + table.x0y2z0 * coeff.x1y2z0 + 2 * table.x1y2z0 * coeff.x2y2z0
        + 3 * table.x2y2z0 * coeff.x3y2z0 + table.x0y3z0 * coeff.x1y3z0
        + 2 * table.x1y3z0 * coeff.x2y3z0 + 3 * table.x2y3z0 * coeff.x3y3z0
        + table.x0y0z1 * coeff.x1y0z1 + 2 * table.x1y0z1 * coeff.x2y0z1
        + 3 * table.x2y0z1 * coeff.x3y0z1 + table.x0y1z1 * coeff.x1y1z1
        + 2 * table.x1y1z1 * coeff.x2y1z1 + 3 * table.x2y1z1 * coeff.x3y1z1
        + table.x0y2z1 * coeff.x1y2z1 + 2 * table.x1y2z1 * coeff.x2y2z1
        + 3 * table.x2y2z1 * coeff.x3y2z1 + table.x0y3z1 * coeff.x1y3z1
        + 2 * table.x1y3z1 * coeff.x2y3z1 + 3 * table.x2y3z1 * coeff.x3y3z1
        + table.x0y0z2 * coeff.x1y0z2 + 2 * table.x1y0z2 * coeff.x2y0z2
        + 3 * table.x2y0z2 * coeff.x3y0z2 + table.x0y1z2 * coeff.x1y1z2
        + 2 * table.x1y1z2 * coeff.x2y1z2 + 3 * table.x2y1z2 * coeff.x3y1z2
        + table.x0y2z2 * coeff.x1y2z2 + 2 * table.x1y2z2 * coeff.x2y2z2
        + 3 * table.x2y2z2 * coeff.x3y2z2 + table.x0y3z2 * coeff.x1y3z2
        + 2 * table.x1y3z2 * coeff.x2y3z2 + 3 * table.x2y3z2 * coeff.x3y3z2
        + table.x0y0z3 * coeff.x1y0z3 + 2 * table.x1y0z3 * coeff.x2y0z3
        + 3 * table.x2y0z3 * coeff.x3y0z3 + table.x0y1z3 * coeff.x1y1z3
        + 2 * table.x1y1z3 * coeff.x2y1z3 + 3 * table.x2y1z3 * coeff.x3y1z3
        + table.x0y2z3 * coeff.x1y2z3 + 2 * table.x1y2z3 * coeff.x2y2z3
        + 3 * table.x2y2z3 * coeff.x3y2z3 + table.x0y3z3 * coeff.x1y3z3
        + 2 * table.x1y3z3 * coeff.x2y3z3 + 3 * table.x2y3z3 * coeff.x3y3z3;
    derivative1[1] = table.x0y0z0 * coeff.x0y1z0 + table.x1y0z0 * coeff.x1y1z0
        + table.x2y0z0 * coeff.x2y1z0 + table.x3y0z0 * coeff.x3y1z0
        + 2 * table.x0y1z0 * coeff.x0y2z0 + 2 * table.x1y1z0 * coeff.x1y2z0
        + 2 * table.x2y1z0 * coeff.x2y2z0 + 2 * table.x3y1z0 * coeff.x3y2z0
        + 3 * table.x0y2z0 * coeff.x0y3z0 + 3 * table.x1y2z0 * coeff.x1y3z0
        + 3 * table.x2y2z0 * coeff.x2y3z0 + 3 * table.x3y2z0 * coeff.x3y3z0
        + table.x0y0z1 * coeff.x0y1z1 + table.x1y0z1 * coeff.x1y1z1 + table.x2y0z1 * coeff.x2y1z1
        + table.x3y0z1 * coeff.x3y1z1 + 2 * table.x0y1z1 * coeff.x0y2z1
        + 2 * table.x1y1z1 * coeff.x1y2z1 + 2 * table.x2y1z1 * coeff.x2y2z1
        + 2 * table.x3y1z1 * coeff.x3y2z1 + 3 * table.x0y2z1 * coeff.x0y3z1
        + 3 * table.x1y2z1 * coeff.x1y3z1 + 3 * table.x2y2z1 * coeff.x2y3z1
        + 3 * table.x3y2z1 * coeff.x3y3z1 + table.x0y0z2 * coeff.x0y1z2
        + table.x1y0z2 * coeff.x1y1z2 + table.x2y0z2 * coeff.x2y1z2 + table.x3y0z2 * coeff.x3y1z2
        + 2 * table.x0y1z2 * coeff.x0y2z2 + 2 * table.x1y1z2 * coeff.x1y2z2
        + 2 * table.x2y1z2 * coeff.x2y2z2 + 2 * table.x3y1z2 * coeff.x3y2z2
        + 3 * table.x0y2z2 * coeff.x0y3z2 + 3 * table.x1y2z2 * coeff.x1y3z2
        + 3 * table.x2y2z2 * coeff.x2y3z2 + 3 * table.x3y2z2 * coeff.x3y3z2
        + table.x0y0z3 * coeff.x0y1z3 + table.x1y0z3 * coeff.x1y1z3 + table.x2y0z3 * coeff.x2y1z3
        + table.x3y0z3 * coeff.x3y1z3 + 2 * table.x0y1z3 * coeff.x0y2z3
        + 2 * table.x1y1z3 * coeff.x1y2z3 + 2 * table.x2y1z3 * coeff.x2y2z3
        + 2 * table.x3y1z3 * coeff.x3y2z3 + 3 * table.x0y2z3 * coeff.x0y3z3
        + 3 * table.x1y2z3 * coeff.x1y3z3 + 3 * table.x2y2z3 * coeff.x2y3z3
        + 3 * table.x3y2z3 * coeff.x3y3z3;
    derivative1[2] = table.x0y0z0 * coeff.x0y0z1 + table.x1y0z0 * coeff.x1y0z1
        + table.x2y0z0 * coeff.x2y0z1 + table.x3y0z0 * coeff.x3y0z1 + table.x0y1z0 * coeff.x0y1z1
        + table.x1y1z0 * coeff.x1y1z1 + table.x2y1z0 * coeff.x2y1z1 + table.x3y1z0 * coeff.x3y1z1
        + table.x0y2z0 * coeff.x0y2z1 + table.x1y2z0 * coeff.x1y2z1 + table.x2y2z0 * coeff.x2y2z1
        + table.x3y2z0 * coeff.x3y2z1 + table.x0y3z0 * coeff.x0y3z1 + table.x1y3z0 * coeff.x1y3z1
        + table.x2y3z0 * coeff.x2y3z1 + table.x3y3z0 * coeff.x3y3z1
        + 2 * table.x0y0z1 * coeff.x0y0z2 + 2 * table.x1y0z1 * coeff.x1y0z2
        + 2 * table.x2y0z1 * coeff.x2y0z2 + 2 * table.x3y0z1 * coeff.x3y0z2
        + 2 * table.x0y1z1 * coeff.x0y1z2 + 2 * table.x1y1z1 * coeff.x1y1z2
        + 2 * table.x2y1z1 * coeff.x2y1z2 + 2 * table.x3y1z1 * coeff.x3y1z2
        + 2 * table.x0y2z1 * coeff.x0y2z2 + 2 * table.x1y2z1 * coeff.x1y2z2
        + 2 * table.x2y2z1 * coeff.x2y2z2 + 2 * table.x3y2z1 * coeff.x3y2z2
        + 2 * table.x0y3z1 * coeff.x0y3z2 + 2 * table.x1y3z1 * coeff.x1y3z2
        + 2 * table.x2y3z1 * coeff.x2y3z2 + 2 * table.x3y3z1 * coeff.x3y3z2
        + 3 * table.x0y0z2 * coeff.x0y0z3 + 3 * table.x1y0z2 * coeff.x1y0z3
        + 3 * table.x2y0z2 * coeff.x2y0z3 + 3 * table.x3y0z2 * coeff.x3y0z3
        + 3 * table.x0y1z2 * coeff.x0y1z3 + 3 * table.x1y1z2 * coeff.x1y1z3
        + 3 * table.x2y1z2 * coeff.x2y1z3 + 3 * table.x3y1z2 * coeff.x3y1z3
        + 3 * table.x0y2z2 * coeff.x0y2z3 + 3 * table.x1y2z2 * coeff.x1y2z3
        + 3 * table.x2y2z2 * coeff.x2y2z3 + 3 * table.x3y2z2 * coeff.x3y2z3
        + 3 * table.x0y3z2 * coeff.x0y3z3 + 3 * table.x1y3z2 * coeff.x1y3z3
        + 3 * table.x2y3z2 * coeff.x2y3z3 + 3 * table.x3y3z2 * coeff.x3y3z3;
    return table.x0y0z0 * coeff.x0y0z0 + table.x1y0z0 * coeff.x1y0z0 + table.x2y0z0 * coeff.x2y0z0
        + table.x3y0z0 * coeff.x3y0z0 + table.x0y1z0 * coeff.x0y1z0 + table.x1y1z0 * coeff.x1y1z0
        + table.x2y1z0 * coeff.x2y1z0 + table.x3y1z0 * coeff.x3y1z0 + table.x0y2z0 * coeff.x0y2z0
        + table.x1y2z0 * coeff.x1y2z0 + table.x2y2z0 * coeff.x2y2z0 + table.x3y2z0 * coeff.x3y2z0
        + table.x0y3z0 * coeff.x0y3z0 + table.x1y3z0 * coeff.x1y3z0 + table.x2y3z0 * coeff.x2y3z0
        + table.x3y3z0 * coeff.x3y3z0 + table.x0y0z1 * coeff.x0y0z1 + table.x1y0z1 * coeff.x1y0z1
        + table.x2y0z1 * coeff.x2y0z1 + table.x3y0z1 * coeff.x3y0z1 + table.x0y1z1 * coeff.x0y1z1
        + table.x1y1z1 * coeff.x1y1z1 + table.x2y1z1 * coeff.x2y1z1 + table.x3y1z1 * coeff.x3y1z1
        + table.x0y2z1 * coeff.x0y2z1 + table.x1y2z1 * coeff.x1y2z1 + table.x2y2z1 * coeff.x2y2z1
        + table.x3y2z1 * coeff.x3y2z1 + table.x0y3z1 * coeff.x0y3z1 + table.x1y3z1 * coeff.x1y3z1
        + table.x2y3z1 * coeff.x2y3z1 + table.x3y3z1 * coeff.x3y3z1 + table.x0y0z2 * coeff.x0y0z2
        + table.x1y0z2 * coeff.x1y0z2 + table.x2y0z2 * coeff.x2y0z2 + table.x3y0z2 * coeff.x3y0z2
        + table.x0y1z2 * coeff.x0y1z2 + table.x1y1z2 * coeff.x1y1z2 + table.x2y1z2 * coeff.x2y1z2
        + table.x3y1z2 * coeff.x3y1z2 + table.x0y2z2 * coeff.x0y2z2 + table.x1y2z2 * coeff.x1y2z2
        + table.x2y2z2 * coeff.x2y2z2 + table.x3y2z2 * coeff.x3y2z2 + table.x0y3z2 * coeff.x0y3z2
        + table.x1y3z2 * coeff.x1y3z2 + table.x2y3z2 * coeff.x2y3z2 + table.x3y3z2 * coeff.x3y3z2
        + table.x0y0z3 * coeff.x0y0z3 + table.x1y0z3 * coeff.x1y0z3 + table.x2y0z3 * coeff.x2y0z3
        + table.x3y0z3 * coeff.x3y0z3 + table.x0y1z3 * coeff.x0y1z3 + table.x1y1z3 * coeff.x1y1z3
        + table.x2y1z3 * coeff.x2y1z3 + table.x3y1z3 * coeff.x3y1z3 + table.x0y2z3 * coeff.x0y2z3
        + table.x1y2z3 * coeff.x1y2z3 + table.x2y2z3 * coeff.x2y2z3 + table.x3y2z3 * coeff.x3y2z3
        + table.x0y3z3 * coeff.x0y3z3 + table.x1y3z3 * coeff.x1y3z3 + table.x2y3z3 * coeff.x2y3z3
        + table.x3y3z3 * coeff.x3y3z3;
  }

  @Override
  public double value(DoubleCubicSplineData table, DoubleCubicSplineData table2,
      DoubleCubicSplineData table3, double[] derivative1) {
    derivative1[0] = table.x0y0z0 * coeff.x1y0z0 + table2.x1y0z0 * coeff.x2y0z0
        + table3.x2y0z0 * coeff.x3y0z0 + table.x0y1z0 * coeff.x1y1z0 + table2.x1y1z0 * coeff.x2y1z0
        + table3.x2y1z0 * coeff.x3y1z0 + table.x0y2z0 * coeff.x1y2z0 + table2.x1y2z0 * coeff.x2y2z0
        + table3.x2y2z0 * coeff.x3y2z0 + table.x0y3z0 * coeff.x1y3z0 + table2.x1y3z0 * coeff.x2y3z0
        + table3.x2y3z0 * coeff.x3y3z0 + table.x0y0z1 * coeff.x1y0z1 + table2.x1y0z1 * coeff.x2y0z1
        + table3.x2y0z1 * coeff.x3y0z1 + table.x0y1z1 * coeff.x1y1z1 + table2.x1y1z1 * coeff.x2y1z1
        + table3.x2y1z1 * coeff.x3y1z1 + table.x0y2z1 * coeff.x1y2z1 + table2.x1y2z1 * coeff.x2y2z1
        + table3.x2y2z1 * coeff.x3y2z1 + table.x0y3z1 * coeff.x1y3z1 + table2.x1y3z1 * coeff.x2y3z1
        + table3.x2y3z1 * coeff.x3y3z1 + table.x0y0z2 * coeff.x1y0z2 + table2.x1y0z2 * coeff.x2y0z2
        + table3.x2y0z2 * coeff.x3y0z2 + table.x0y1z2 * coeff.x1y1z2 + table2.x1y1z2 * coeff.x2y1z2
        + table3.x2y1z2 * coeff.x3y1z2 + table.x0y2z2 * coeff.x1y2z2 + table2.x1y2z2 * coeff.x2y2z2
        + table3.x2y2z2 * coeff.x3y2z2 + table.x0y3z2 * coeff.x1y3z2 + table2.x1y3z2 * coeff.x2y3z2
        + table3.x2y3z2 * coeff.x3y3z2 + table.x0y0z3 * coeff.x1y0z3 + table2.x1y0z3 * coeff.x2y0z3
        + table3.x2y0z3 * coeff.x3y0z3 + table.x0y1z3 * coeff.x1y1z3 + table2.x1y1z3 * coeff.x2y1z3
        + table3.x2y1z3 * coeff.x3y1z3 + table.x0y2z3 * coeff.x1y2z3 + table2.x1y2z3 * coeff.x2y2z3
        + table3.x2y2z3 * coeff.x3y2z3 + table.x0y3z3 * coeff.x1y3z3 + table2.x1y3z3 * coeff.x2y3z3
        + table3.x2y3z3 * coeff.x3y3z3;
    derivative1[1] = table.x0y0z0 * coeff.x0y1z0 + table.x1y0z0 * coeff.x1y1z0
        + table.x2y0z0 * coeff.x2y1z0 + table.x3y0z0 * coeff.x3y1z0 + table2.x0y1z0 * coeff.x0y2z0
        + table2.x1y1z0 * coeff.x1y2z0 + table2.x2y1z0 * coeff.x2y2z0 + table2.x3y1z0 * coeff.x3y2z0
        + table3.x0y2z0 * coeff.x0y3z0 + table3.x1y2z0 * coeff.x1y3z0 + table3.x2y2z0 * coeff.x2y3z0
        + table3.x3y2z0 * coeff.x3y3z0 + table.x0y0z1 * coeff.x0y1z1 + table.x1y0z1 * coeff.x1y1z1
        + table.x2y0z1 * coeff.x2y1z1 + table.x3y0z1 * coeff.x3y1z1 + table2.x0y1z1 * coeff.x0y2z1
        + table2.x1y1z1 * coeff.x1y2z1 + table2.x2y1z1 * coeff.x2y2z1 + table2.x3y1z1 * coeff.x3y2z1
        + table3.x0y2z1 * coeff.x0y3z1 + table3.x1y2z1 * coeff.x1y3z1 + table3.x2y2z1 * coeff.x2y3z1
        + table3.x3y2z1 * coeff.x3y3z1 + table.x0y0z2 * coeff.x0y1z2 + table.x1y0z2 * coeff.x1y1z2
        + table.x2y0z2 * coeff.x2y1z2 + table.x3y0z2 * coeff.x3y1z2 + table2.x0y1z2 * coeff.x0y2z2
        + table2.x1y1z2 * coeff.x1y2z2 + table2.x2y1z2 * coeff.x2y2z2 + table2.x3y1z2 * coeff.x3y2z2
        + table3.x0y2z2 * coeff.x0y3z2 + table3.x1y2z2 * coeff.x1y3z2 + table3.x2y2z2 * coeff.x2y3z2
        + table3.x3y2z2 * coeff.x3y3z2 + table.x0y0z3 * coeff.x0y1z3 + table.x1y0z3 * coeff.x1y1z3
        + table.x2y0z3 * coeff.x2y1z3 + table.x3y0z3 * coeff.x3y1z3 + table2.x0y1z3 * coeff.x0y2z3
        + table2.x1y1z3 * coeff.x1y2z3 + table2.x2y1z3 * coeff.x2y2z3 + table2.x3y1z3 * coeff.x3y2z3
        + table3.x0y2z3 * coeff.x0y3z3 + table3.x1y2z3 * coeff.x1y3z3 + table3.x2y2z3 * coeff.x2y3z3
        + table3.x3y2z3 * coeff.x3y3z3;
    derivative1[2] = table.x0y0z0 * coeff.x0y0z1 + table.x1y0z0 * coeff.x1y0z1
        + table.x2y0z0 * coeff.x2y0z1 + table.x3y0z0 * coeff.x3y0z1 + table.x0y1z0 * coeff.x0y1z1
        + table.x1y1z0 * coeff.x1y1z1 + table.x2y1z0 * coeff.x2y1z1 + table.x3y1z0 * coeff.x3y1z1
        + table.x0y2z0 * coeff.x0y2z1 + table.x1y2z0 * coeff.x1y2z1 + table.x2y2z0 * coeff.x2y2z1
        + table.x3y2z0 * coeff.x3y2z1 + table.x0y3z0 * coeff.x0y3z1 + table.x1y3z0 * coeff.x1y3z1
        + table.x2y3z0 * coeff.x2y3z1 + table.x3y3z0 * coeff.x3y3z1 + table2.x0y0z1 * coeff.x0y0z2
        + table2.x1y0z1 * coeff.x1y0z2 + table2.x2y0z1 * coeff.x2y0z2 + table2.x3y0z1 * coeff.x3y0z2
        + table2.x0y1z1 * coeff.x0y1z2 + table2.x1y1z1 * coeff.x1y1z2 + table2.x2y1z1 * coeff.x2y1z2
        + table2.x3y1z1 * coeff.x3y1z2 + table2.x0y2z1 * coeff.x0y2z2 + table2.x1y2z1 * coeff.x1y2z2
        + table2.x2y2z1 * coeff.x2y2z2 + table2.x3y2z1 * coeff.x3y2z2 + table2.x0y3z1 * coeff.x0y3z2
        + table2.x1y3z1 * coeff.x1y3z2 + table2.x2y3z1 * coeff.x2y3z2 + table2.x3y3z1 * coeff.x3y3z2
        + table3.x0y0z2 * coeff.x0y0z3 + table3.x1y0z2 * coeff.x1y0z3 + table3.x2y0z2 * coeff.x2y0z3
        + table3.x3y0z2 * coeff.x3y0z3 + table3.x0y1z2 * coeff.x0y1z3 + table3.x1y1z2 * coeff.x1y1z3
        + table3.x2y1z2 * coeff.x2y1z3 + table3.x3y1z2 * coeff.x3y1z3 + table3.x0y2z2 * coeff.x0y2z3
        + table3.x1y2z2 * coeff.x1y2z3 + table3.x2y2z2 * coeff.x2y2z3 + table3.x3y2z2 * coeff.x3y2z3
        + table3.x0y3z2 * coeff.x0y3z3 + table3.x1y3z2 * coeff.x1y3z3 + table3.x2y3z2 * coeff.x2y3z3
        + table3.x3y3z2 * coeff.x3y3z3;
    return table.x0y0z0 * coeff.x0y0z0 + table.x1y0z0 * coeff.x1y0z0 + table.x2y0z0 * coeff.x2y0z0
        + table.x3y0z0 * coeff.x3y0z0 + table.x0y1z0 * coeff.x0y1z0 + table.x1y1z0 * coeff.x1y1z0
        + table.x2y1z0 * coeff.x2y1z0 + table.x3y1z0 * coeff.x3y1z0 + table.x0y2z0 * coeff.x0y2z0
        + table.x1y2z0 * coeff.x1y2z0 + table.x2y2z0 * coeff.x2y2z0 + table.x3y2z0 * coeff.x3y2z0
        + table.x0y3z0 * coeff.x0y3z0 + table.x1y3z0 * coeff.x1y3z0 + table.x2y3z0 * coeff.x2y3z0
        + table.x3y3z0 * coeff.x3y3z0 + table.x0y0z1 * coeff.x0y0z1 + table.x1y0z1 * coeff.x1y0z1
        + table.x2y0z1 * coeff.x2y0z1 + table.x3y0z1 * coeff.x3y0z1 + table.x0y1z1 * coeff.x0y1z1
        + table.x1y1z1 * coeff.x1y1z1 + table.x2y1z1 * coeff.x2y1z1 + table.x3y1z1 * coeff.x3y1z1
        + table.x0y2z1 * coeff.x0y2z1 + table.x1y2z1 * coeff.x1y2z1 + table.x2y2z1 * coeff.x2y2z1
        + table.x3y2z1 * coeff.x3y2z1 + table.x0y3z1 * coeff.x0y3z1 + table.x1y3z1 * coeff.x1y3z1
        + table.x2y3z1 * coeff.x2y3z1 + table.x3y3z1 * coeff.x3y3z1 + table.x0y0z2 * coeff.x0y0z2
        + table.x1y0z2 * coeff.x1y0z2 + table.x2y0z2 * coeff.x2y0z2 + table.x3y0z2 * coeff.x3y0z2
        + table.x0y1z2 * coeff.x0y1z2 + table.x1y1z2 * coeff.x1y1z2 + table.x2y1z2 * coeff.x2y1z2
        + table.x3y1z2 * coeff.x3y1z2 + table.x0y2z2 * coeff.x0y2z2 + table.x1y2z2 * coeff.x1y2z2
        + table.x2y2z2 * coeff.x2y2z2 + table.x3y2z2 * coeff.x3y2z2 + table.x0y3z2 * coeff.x0y3z2
        + table.x1y3z2 * coeff.x1y3z2 + table.x2y3z2 * coeff.x2y3z2 + table.x3y3z2 * coeff.x3y3z2
        + table.x0y0z3 * coeff.x0y0z3 + table.x1y0z3 * coeff.x1y0z3 + table.x2y0z3 * coeff.x2y0z3
        + table.x3y0z3 * coeff.x3y0z3 + table.x0y1z3 * coeff.x0y1z3 + table.x1y1z3 * coeff.x1y1z3
        + table.x2y1z3 * coeff.x2y1z3 + table.x3y1z3 * coeff.x3y1z3 + table.x0y2z3 * coeff.x0y2z3
        + table.x1y2z3 * coeff.x1y2z3 + table.x2y2z3 * coeff.x2y2z3 + table.x3y2z3 * coeff.x3y2z3
        + table.x0y3z3 * coeff.x0y3z3 + table.x1y3z3 * coeff.x1y3z3 + table.x2y3z3 * coeff.x2y3z3
        + table.x3y3z3 * coeff.x3y3z3;
  }

  @Override
  public double value(FloatCubicSplineData table, FloatCubicSplineData table2,
      FloatCubicSplineData table3, double[] derivative1) {
    derivative1[0] = table.x0y0z0 * coeff.x1y0z0 + table2.x1y0z0 * coeff.x2y0z0
        + table3.x2y0z0 * coeff.x3y0z0 + table.x0y1z0 * coeff.x1y1z0 + table2.x1y1z0 * coeff.x2y1z0
        + table3.x2y1z0 * coeff.x3y1z0 + table.x0y2z0 * coeff.x1y2z0 + table2.x1y2z0 * coeff.x2y2z0
        + table3.x2y2z0 * coeff.x3y2z0 + table.x0y3z0 * coeff.x1y3z0 + table2.x1y3z0 * coeff.x2y3z0
        + table3.x2y3z0 * coeff.x3y3z0 + table.x0y0z1 * coeff.x1y0z1 + table2.x1y0z1 * coeff.x2y0z1
        + table3.x2y0z1 * coeff.x3y0z1 + table.x0y1z1 * coeff.x1y1z1 + table2.x1y1z1 * coeff.x2y1z1
        + table3.x2y1z1 * coeff.x3y1z1 + table.x0y2z1 * coeff.x1y2z1 + table2.x1y2z1 * coeff.x2y2z1
        + table3.x2y2z1 * coeff.x3y2z1 + table.x0y3z1 * coeff.x1y3z1 + table2.x1y3z1 * coeff.x2y3z1
        + table3.x2y3z1 * coeff.x3y3z1 + table.x0y0z2 * coeff.x1y0z2 + table2.x1y0z2 * coeff.x2y0z2
        + table3.x2y0z2 * coeff.x3y0z2 + table.x0y1z2 * coeff.x1y1z2 + table2.x1y1z2 * coeff.x2y1z2
        + table3.x2y1z2 * coeff.x3y1z2 + table.x0y2z2 * coeff.x1y2z2 + table2.x1y2z2 * coeff.x2y2z2
        + table3.x2y2z2 * coeff.x3y2z2 + table.x0y3z2 * coeff.x1y3z2 + table2.x1y3z2 * coeff.x2y3z2
        + table3.x2y3z2 * coeff.x3y3z2 + table.x0y0z3 * coeff.x1y0z3 + table2.x1y0z3 * coeff.x2y0z3
        + table3.x2y0z3 * coeff.x3y0z3 + table.x0y1z3 * coeff.x1y1z3 + table2.x1y1z3 * coeff.x2y1z3
        + table3.x2y1z3 * coeff.x3y1z3 + table.x0y2z3 * coeff.x1y2z3 + table2.x1y2z3 * coeff.x2y2z3
        + table3.x2y2z3 * coeff.x3y2z3 + table.x0y3z3 * coeff.x1y3z3 + table2.x1y3z3 * coeff.x2y3z3
        + table3.x2y3z3 * coeff.x3y3z3;
    derivative1[1] = table.x0y0z0 * coeff.x0y1z0 + table.x1y0z0 * coeff.x1y1z0
        + table.x2y0z0 * coeff.x2y1z0 + table.x3y0z0 * coeff.x3y1z0 + table2.x0y1z0 * coeff.x0y2z0
        + table2.x1y1z0 * coeff.x1y2z0 + table2.x2y1z0 * coeff.x2y2z0 + table2.x3y1z0 * coeff.x3y2z0
        + table3.x0y2z0 * coeff.x0y3z0 + table3.x1y2z0 * coeff.x1y3z0 + table3.x2y2z0 * coeff.x2y3z0
        + table3.x3y2z0 * coeff.x3y3z0 + table.x0y0z1 * coeff.x0y1z1 + table.x1y0z1 * coeff.x1y1z1
        + table.x2y0z1 * coeff.x2y1z1 + table.x3y0z1 * coeff.x3y1z1 + table2.x0y1z1 * coeff.x0y2z1
        + table2.x1y1z1 * coeff.x1y2z1 + table2.x2y1z1 * coeff.x2y2z1 + table2.x3y1z1 * coeff.x3y2z1
        + table3.x0y2z1 * coeff.x0y3z1 + table3.x1y2z1 * coeff.x1y3z1 + table3.x2y2z1 * coeff.x2y3z1
        + table3.x3y2z1 * coeff.x3y3z1 + table.x0y0z2 * coeff.x0y1z2 + table.x1y0z2 * coeff.x1y1z2
        + table.x2y0z2 * coeff.x2y1z2 + table.x3y0z2 * coeff.x3y1z2 + table2.x0y1z2 * coeff.x0y2z2
        + table2.x1y1z2 * coeff.x1y2z2 + table2.x2y1z2 * coeff.x2y2z2 + table2.x3y1z2 * coeff.x3y2z2
        + table3.x0y2z2 * coeff.x0y3z2 + table3.x1y2z2 * coeff.x1y3z2 + table3.x2y2z2 * coeff.x2y3z2
        + table3.x3y2z2 * coeff.x3y3z2 + table.x0y0z3 * coeff.x0y1z3 + table.x1y0z3 * coeff.x1y1z3
        + table.x2y0z3 * coeff.x2y1z3 + table.x3y0z3 * coeff.x3y1z3 + table2.x0y1z3 * coeff.x0y2z3
        + table2.x1y1z3 * coeff.x1y2z3 + table2.x2y1z3 * coeff.x2y2z3 + table2.x3y1z3 * coeff.x3y2z3
        + table3.x0y2z3 * coeff.x0y3z3 + table3.x1y2z3 * coeff.x1y3z3 + table3.x2y2z3 * coeff.x2y3z3
        + table3.x3y2z3 * coeff.x3y3z3;
    derivative1[2] = table.x0y0z0 * coeff.x0y0z1 + table.x1y0z0 * coeff.x1y0z1
        + table.x2y0z0 * coeff.x2y0z1 + table.x3y0z0 * coeff.x3y0z1 + table.x0y1z0 * coeff.x0y1z1
        + table.x1y1z0 * coeff.x1y1z1 + table.x2y1z0 * coeff.x2y1z1 + table.x3y1z0 * coeff.x3y1z1
        + table.x0y2z0 * coeff.x0y2z1 + table.x1y2z0 * coeff.x1y2z1 + table.x2y2z0 * coeff.x2y2z1
        + table.x3y2z0 * coeff.x3y2z1 + table.x0y3z0 * coeff.x0y3z1 + table.x1y3z0 * coeff.x1y3z1
        + table.x2y3z0 * coeff.x2y3z1 + table.x3y3z0 * coeff.x3y3z1 + table2.x0y0z1 * coeff.x0y0z2
        + table2.x1y0z1 * coeff.x1y0z2 + table2.x2y0z1 * coeff.x2y0z2 + table2.x3y0z1 * coeff.x3y0z2
        + table2.x0y1z1 * coeff.x0y1z2 + table2.x1y1z1 * coeff.x1y1z2 + table2.x2y1z1 * coeff.x2y1z2
        + table2.x3y1z1 * coeff.x3y1z2 + table2.x0y2z1 * coeff.x0y2z2 + table2.x1y2z1 * coeff.x1y2z2
        + table2.x2y2z1 * coeff.x2y2z2 + table2.x3y2z1 * coeff.x3y2z2 + table2.x0y3z1 * coeff.x0y3z2
        + table2.x1y3z1 * coeff.x1y3z2 + table2.x2y3z1 * coeff.x2y3z2 + table2.x3y3z1 * coeff.x3y3z2
        + table3.x0y0z2 * coeff.x0y0z3 + table3.x1y0z2 * coeff.x1y0z3 + table3.x2y0z2 * coeff.x2y0z3
        + table3.x3y0z2 * coeff.x3y0z3 + table3.x0y1z2 * coeff.x0y1z3 + table3.x1y1z2 * coeff.x1y1z3
        + table3.x2y1z2 * coeff.x2y1z3 + table3.x3y1z2 * coeff.x3y1z3 + table3.x0y2z2 * coeff.x0y2z3
        + table3.x1y2z2 * coeff.x1y2z3 + table3.x2y2z2 * coeff.x2y2z3 + table3.x3y2z2 * coeff.x3y2z3
        + table3.x0y3z2 * coeff.x0y3z3 + table3.x1y3z2 * coeff.x1y3z3 + table3.x2y3z2 * coeff.x2y3z3
        + table3.x3y3z2 * coeff.x3y3z3;
    return table.x0y0z0 * coeff.x0y0z0 + table.x1y0z0 * coeff.x1y0z0 + table.x2y0z0 * coeff.x2y0z0
        + table.x3y0z0 * coeff.x3y0z0 + table.x0y1z0 * coeff.x0y1z0 + table.x1y1z0 * coeff.x1y1z0
        + table.x2y1z0 * coeff.x2y1z0 + table.x3y1z0 * coeff.x3y1z0 + table.x0y2z0 * coeff.x0y2z0
        + table.x1y2z0 * coeff.x1y2z0 + table.x2y2z0 * coeff.x2y2z0 + table.x3y2z0 * coeff.x3y2z0
        + table.x0y3z0 * coeff.x0y3z0 + table.x1y3z0 * coeff.x1y3z0 + table.x2y3z0 * coeff.x2y3z0
        + table.x3y3z0 * coeff.x3y3z0 + table.x0y0z1 * coeff.x0y0z1 + table.x1y0z1 * coeff.x1y0z1
        + table.x2y0z1 * coeff.x2y0z1 + table.x3y0z1 * coeff.x3y0z1 + table.x0y1z1 * coeff.x0y1z1
        + table.x1y1z1 * coeff.x1y1z1 + table.x2y1z1 * coeff.x2y1z1 + table.x3y1z1 * coeff.x3y1z1
        + table.x0y2z1 * coeff.x0y2z1 + table.x1y2z1 * coeff.x1y2z1 + table.x2y2z1 * coeff.x2y2z1
        + table.x3y2z1 * coeff.x3y2z1 + table.x0y3z1 * coeff.x0y3z1 + table.x1y3z1 * coeff.x1y3z1
        + table.x2y3z1 * coeff.x2y3z1 + table.x3y3z1 * coeff.x3y3z1 + table.x0y0z2 * coeff.x0y0z2
        + table.x1y0z2 * coeff.x1y0z2 + table.x2y0z2 * coeff.x2y0z2 + table.x3y0z2 * coeff.x3y0z2
        + table.x0y1z2 * coeff.x0y1z2 + table.x1y1z2 * coeff.x1y1z2 + table.x2y1z2 * coeff.x2y1z2
        + table.x3y1z2 * coeff.x3y1z2 + table.x0y2z2 * coeff.x0y2z2 + table.x1y2z2 * coeff.x1y2z2
        + table.x2y2z2 * coeff.x2y2z2 + table.x3y2z2 * coeff.x3y2z2 + table.x0y3z2 * coeff.x0y3z2
        + table.x1y3z2 * coeff.x1y3z2 + table.x2y3z2 * coeff.x2y3z2 + table.x3y3z2 * coeff.x3y3z2
        + table.x0y0z3 * coeff.x0y0z3 + table.x1y0z3 * coeff.x1y0z3 + table.x2y0z3 * coeff.x2y0z3
        + table.x3y0z3 * coeff.x3y0z3 + table.x0y1z3 * coeff.x0y1z3 + table.x1y1z3 * coeff.x1y1z3
        + table.x2y1z3 * coeff.x2y1z3 + table.x3y1z3 * coeff.x3y1z3 + table.x0y2z3 * coeff.x0y2z3
        + table.x1y2z3 * coeff.x1y2z3 + table.x2y2z3 * coeff.x2y2z3 + table.x3y2z3 * coeff.x3y2z3
        + table.x0y3z3 * coeff.x0y3z3 + table.x1y3z3 * coeff.x1y3z3 + table.x2y3z3 * coeff.x2y3z3
        + table.x3y3z3 * coeff.x3y3z3;
  }

  @Override
  public double value(DoubleCubicSplineData table, double[] derivative1, double[] derivative2) {
    derivative1[0] = table.x0y0z0 * coeff.x1y0z0 + 2 * table.x1y0z0 * coeff.x2y0z0
        + 3 * table.x2y0z0 * coeff.x3y0z0 + table.x0y1z0 * coeff.x1y1z0
        + 2 * table.x1y1z0 * coeff.x2y1z0 + 3 * table.x2y1z0 * coeff.x3y1z0
        + table.x0y2z0 * coeff.x1y2z0 + 2 * table.x1y2z0 * coeff.x2y2z0
        + 3 * table.x2y2z0 * coeff.x3y2z0 + table.x0y3z0 * coeff.x1y3z0
        + 2 * table.x1y3z0 * coeff.x2y3z0 + 3 * table.x2y3z0 * coeff.x3y3z0
        + table.x0y0z1 * coeff.x1y0z1 + 2 * table.x1y0z1 * coeff.x2y0z1
        + 3 * table.x2y0z1 * coeff.x3y0z1 + table.x0y1z1 * coeff.x1y1z1
        + 2 * table.x1y1z1 * coeff.x2y1z1 + 3 * table.x2y1z1 * coeff.x3y1z1
        + table.x0y2z1 * coeff.x1y2z1 + 2 * table.x1y2z1 * coeff.x2y2z1
        + 3 * table.x2y2z1 * coeff.x3y2z1 + table.x0y3z1 * coeff.x1y3z1
        + 2 * table.x1y3z1 * coeff.x2y3z1 + 3 * table.x2y3z1 * coeff.x3y3z1
        + table.x0y0z2 * coeff.x1y0z2 + 2 * table.x1y0z2 * coeff.x2y0z2
        + 3 * table.x2y0z2 * coeff.x3y0z2 + table.x0y1z2 * coeff.x1y1z2
        + 2 * table.x1y1z2 * coeff.x2y1z2 + 3 * table.x2y1z2 * coeff.x3y1z2
        + table.x0y2z2 * coeff.x1y2z2 + 2 * table.x1y2z2 * coeff.x2y2z2
        + 3 * table.x2y2z2 * coeff.x3y2z2 + table.x0y3z2 * coeff.x1y3z2
        + 2 * table.x1y3z2 * coeff.x2y3z2 + 3 * table.x2y3z2 * coeff.x3y3z2
        + table.x0y0z3 * coeff.x1y0z3 + 2 * table.x1y0z3 * coeff.x2y0z3
        + 3 * table.x2y0z3 * coeff.x3y0z3 + table.x0y1z3 * coeff.x1y1z3
        + 2 * table.x1y1z3 * coeff.x2y1z3 + 3 * table.x2y1z3 * coeff.x3y1z3
        + table.x0y2z3 * coeff.x1y2z3 + 2 * table.x1y2z3 * coeff.x2y2z3
        + 3 * table.x2y2z3 * coeff.x3y2z3 + table.x0y3z3 * coeff.x1y3z3
        + 2 * table.x1y3z3 * coeff.x2y3z3 + 3 * table.x2y3z3 * coeff.x3y3z3;
    derivative1[1] = table.x0y0z0 * coeff.x0y1z0 + table.x1y0z0 * coeff.x1y1z0
        + table.x2y0z0 * coeff.x2y1z0 + table.x3y0z0 * coeff.x3y1z0
        + 2 * table.x0y1z0 * coeff.x0y2z0 + 2 * table.x1y1z0 * coeff.x1y2z0
        + 2 * table.x2y1z0 * coeff.x2y2z0 + 2 * table.x3y1z0 * coeff.x3y2z0
        + 3 * table.x0y2z0 * coeff.x0y3z0 + 3 * table.x1y2z0 * coeff.x1y3z0
        + 3 * table.x2y2z0 * coeff.x2y3z0 + 3 * table.x3y2z0 * coeff.x3y3z0
        + table.x0y0z1 * coeff.x0y1z1 + table.x1y0z1 * coeff.x1y1z1 + table.x2y0z1 * coeff.x2y1z1
        + table.x3y0z1 * coeff.x3y1z1 + 2 * table.x0y1z1 * coeff.x0y2z1
        + 2 * table.x1y1z1 * coeff.x1y2z1 + 2 * table.x2y1z1 * coeff.x2y2z1
        + 2 * table.x3y1z1 * coeff.x3y2z1 + 3 * table.x0y2z1 * coeff.x0y3z1
        + 3 * table.x1y2z1 * coeff.x1y3z1 + 3 * table.x2y2z1 * coeff.x2y3z1
        + 3 * table.x3y2z1 * coeff.x3y3z1 + table.x0y0z2 * coeff.x0y1z2
        + table.x1y0z2 * coeff.x1y1z2 + table.x2y0z2 * coeff.x2y1z2 + table.x3y0z2 * coeff.x3y1z2
        + 2 * table.x0y1z2 * coeff.x0y2z2 + 2 * table.x1y1z2 * coeff.x1y2z2
        + 2 * table.x2y1z2 * coeff.x2y2z2 + 2 * table.x3y1z2 * coeff.x3y2z2
        + 3 * table.x0y2z2 * coeff.x0y3z2 + 3 * table.x1y2z2 * coeff.x1y3z2
        + 3 * table.x2y2z2 * coeff.x2y3z2 + 3 * table.x3y2z2 * coeff.x3y3z2
        + table.x0y0z3 * coeff.x0y1z3 + table.x1y0z3 * coeff.x1y1z3 + table.x2y0z3 * coeff.x2y1z3
        + table.x3y0z3 * coeff.x3y1z3 + 2 * table.x0y1z3 * coeff.x0y2z3
        + 2 * table.x1y1z3 * coeff.x1y2z3 + 2 * table.x2y1z3 * coeff.x2y2z3
        + 2 * table.x3y1z3 * coeff.x3y2z3 + 3 * table.x0y2z3 * coeff.x0y3z3
        + 3 * table.x1y2z3 * coeff.x1y3z3 + 3 * table.x2y2z3 * coeff.x2y3z3
        + 3 * table.x3y2z3 * coeff.x3y3z3;
    derivative1[2] = table.x0y0z0 * coeff.x0y0z1 + table.x1y0z0 * coeff.x1y0z1
        + table.x2y0z0 * coeff.x2y0z1 + table.x3y0z0 * coeff.x3y0z1 + table.x0y1z0 * coeff.x0y1z1
        + table.x1y1z0 * coeff.x1y1z1 + table.x2y1z0 * coeff.x2y1z1 + table.x3y1z0 * coeff.x3y1z1
        + table.x0y2z0 * coeff.x0y2z1 + table.x1y2z0 * coeff.x1y2z1 + table.x2y2z0 * coeff.x2y2z1
        + table.x3y2z0 * coeff.x3y2z1 + table.x0y3z0 * coeff.x0y3z1 + table.x1y3z0 * coeff.x1y3z1
        + table.x2y3z0 * coeff.x2y3z1 + table.x3y3z0 * coeff.x3y3z1
        + 2 * table.x0y0z1 * coeff.x0y0z2 + 2 * table.x1y0z1 * coeff.x1y0z2
        + 2 * table.x2y0z1 * coeff.x2y0z2 + 2 * table.x3y0z1 * coeff.x3y0z2
        + 2 * table.x0y1z1 * coeff.x0y1z2 + 2 * table.x1y1z1 * coeff.x1y1z2
        + 2 * table.x2y1z1 * coeff.x2y1z2 + 2 * table.x3y1z1 * coeff.x3y1z2
        + 2 * table.x0y2z1 * coeff.x0y2z2 + 2 * table.x1y2z1 * coeff.x1y2z2
        + 2 * table.x2y2z1 * coeff.x2y2z2 + 2 * table.x3y2z1 * coeff.x3y2z2
        + 2 * table.x0y3z1 * coeff.x0y3z2 + 2 * table.x1y3z1 * coeff.x1y3z2
        + 2 * table.x2y3z1 * coeff.x2y3z2 + 2 * table.x3y3z1 * coeff.x3y3z2
        + 3 * table.x0y0z2 * coeff.x0y0z3 + 3 * table.x1y0z2 * coeff.x1y0z3
        + 3 * table.x2y0z2 * coeff.x2y0z3 + 3 * table.x3y0z2 * coeff.x3y0z3
        + 3 * table.x0y1z2 * coeff.x0y1z3 + 3 * table.x1y1z2 * coeff.x1y1z3
        + 3 * table.x2y1z2 * coeff.x2y1z3 + 3 * table.x3y1z2 * coeff.x3y1z3
        + 3 * table.x0y2z2 * coeff.x0y2z3 + 3 * table.x1y2z2 * coeff.x1y2z3
        + 3 * table.x2y2z2 * coeff.x2y2z3 + 3 * table.x3y2z2 * coeff.x3y2z3
        + 3 * table.x0y3z2 * coeff.x0y3z3 + 3 * table.x1y3z2 * coeff.x1y3z3
        + 3 * table.x2y3z2 * coeff.x2y3z3 + 3 * table.x3y3z2 * coeff.x3y3z3;
    derivative2[0] = 2 * table.x0y0z0 * coeff.x2y0z0 + 6 * table.x1y0z0 * coeff.x3y0z0
        + 2 * table.x0y1z0 * coeff.x2y1z0 + 6 * table.x1y1z0 * coeff.x3y1z0
        + 2 * table.x0y2z0 * coeff.x2y2z0 + 6 * table.x1y2z0 * coeff.x3y2z0
        + 2 * table.x0y3z0 * coeff.x2y3z0 + 6 * table.x1y3z0 * coeff.x3y3z0
        + 2 * table.x0y0z1 * coeff.x2y0z1 + 6 * table.x1y0z1 * coeff.x3y0z1
        + 2 * table.x0y1z1 * coeff.x2y1z1 + 6 * table.x1y1z1 * coeff.x3y1z1
        + 2 * table.x0y2z1 * coeff.x2y2z1 + 6 * table.x1y2z1 * coeff.x3y2z1
        + 2 * table.x0y3z1 * coeff.x2y3z1 + 6 * table.x1y3z1 * coeff.x3y3z1
        + 2 * table.x0y0z2 * coeff.x2y0z2 + 6 * table.x1y0z2 * coeff.x3y0z2
        + 2 * table.x0y1z2 * coeff.x2y1z2 + 6 * table.x1y1z2 * coeff.x3y1z2
        + 2 * table.x0y2z2 * coeff.x2y2z2 + 6 * table.x1y2z2 * coeff.x3y2z2
        + 2 * table.x0y3z2 * coeff.x2y3z2 + 6 * table.x1y3z2 * coeff.x3y3z2
        + 2 * table.x0y0z3 * coeff.x2y0z3 + 6 * table.x1y0z3 * coeff.x3y0z3
        + 2 * table.x0y1z3 * coeff.x2y1z3 + 6 * table.x1y1z3 * coeff.x3y1z3
        + 2 * table.x0y2z3 * coeff.x2y2z3 + 6 * table.x1y2z3 * coeff.x3y2z3
        + 2 * table.x0y3z3 * coeff.x2y3z3 + 6 * table.x1y3z3 * coeff.x3y3z3;
    derivative2[1] = 2 * table.x0y0z0 * coeff.x0y2z0 + 2 * table.x1y0z0 * coeff.x1y2z0
        + 2 * table.x2y0z0 * coeff.x2y2z0 + 2 * table.x3y0z0 * coeff.x3y2z0
        + 6 * table.x0y1z0 * coeff.x0y3z0 + 6 * table.x1y1z0 * coeff.x1y3z0
        + 6 * table.x2y1z0 * coeff.x2y3z0 + 6 * table.x3y1z0 * coeff.x3y3z0
        + 2 * table.x0y0z1 * coeff.x0y2z1 + 2 * table.x1y0z1 * coeff.x1y2z1
        + 2 * table.x2y0z1 * coeff.x2y2z1 + 2 * table.x3y0z1 * coeff.x3y2z1
        + 6 * table.x0y1z1 * coeff.x0y3z1 + 6 * table.x1y1z1 * coeff.x1y3z1
        + 6 * table.x2y1z1 * coeff.x2y3z1 + 6 * table.x3y1z1 * coeff.x3y3z1
        + 2 * table.x0y0z2 * coeff.x0y2z2 + 2 * table.x1y0z2 * coeff.x1y2z2
        + 2 * table.x2y0z2 * coeff.x2y2z2 + 2 * table.x3y0z2 * coeff.x3y2z2
        + 6 * table.x0y1z2 * coeff.x0y3z2 + 6 * table.x1y1z2 * coeff.x1y3z2
        + 6 * table.x2y1z2 * coeff.x2y3z2 + 6 * table.x3y1z2 * coeff.x3y3z2
        + 2 * table.x0y0z3 * coeff.x0y2z3 + 2 * table.x1y0z3 * coeff.x1y2z3
        + 2 * table.x2y0z3 * coeff.x2y2z3 + 2 * table.x3y0z3 * coeff.x3y2z3
        + 6 * table.x0y1z3 * coeff.x0y3z3 + 6 * table.x1y1z3 * coeff.x1y3z3
        + 6 * table.x2y1z3 * coeff.x2y3z3 + 6 * table.x3y1z3 * coeff.x3y3z3;
    derivative2[2] = 2 * table.x0y0z0 * coeff.x0y0z2 + 2 * table.x1y0z0 * coeff.x1y0z2
        + 2 * table.x2y0z0 * coeff.x2y0z2 + 2 * table.x3y0z0 * coeff.x3y0z2
        + 2 * table.x0y1z0 * coeff.x0y1z2 + 2 * table.x1y1z0 * coeff.x1y1z2
        + 2 * table.x2y1z0 * coeff.x2y1z2 + 2 * table.x3y1z0 * coeff.x3y1z2
        + 2 * table.x0y2z0 * coeff.x0y2z2 + 2 * table.x1y2z0 * coeff.x1y2z2
        + 2 * table.x2y2z0 * coeff.x2y2z2 + 2 * table.x3y2z0 * coeff.x3y2z2
        + 2 * table.x0y3z0 * coeff.x0y3z2 + 2 * table.x1y3z0 * coeff.x1y3z2
        + 2 * table.x2y3z0 * coeff.x2y3z2 + 2 * table.x3y3z0 * coeff.x3y3z2
        + 6 * table.x0y0z1 * coeff.x0y0z3 + 6 * table.x1y0z1 * coeff.x1y0z3
        + 6 * table.x2y0z1 * coeff.x2y0z3 + 6 * table.x3y0z1 * coeff.x3y0z3
        + 6 * table.x0y1z1 * coeff.x0y1z3 + 6 * table.x1y1z1 * coeff.x1y1z3
        + 6 * table.x2y1z1 * coeff.x2y1z3 + 6 * table.x3y1z1 * coeff.x3y1z3
        + 6 * table.x0y2z1 * coeff.x0y2z3 + 6 * table.x1y2z1 * coeff.x1y2z3
        + 6 * table.x2y2z1 * coeff.x2y2z3 + 6 * table.x3y2z1 * coeff.x3y2z3
        + 6 * table.x0y3z1 * coeff.x0y3z3 + 6 * table.x1y3z1 * coeff.x1y3z3
        + 6 * table.x2y3z1 * coeff.x2y3z3 + 6 * table.x3y3z1 * coeff.x3y3z3;
    return table.x0y0z0 * coeff.x0y0z0 + table.x1y0z0 * coeff.x1y0z0 + table.x2y0z0 * coeff.x2y0z0
        + table.x3y0z0 * coeff.x3y0z0 + table.x0y1z0 * coeff.x0y1z0 + table.x1y1z0 * coeff.x1y1z0
        + table.x2y1z0 * coeff.x2y1z0 + table.x3y1z0 * coeff.x3y1z0 + table.x0y2z0 * coeff.x0y2z0
        + table.x1y2z0 * coeff.x1y2z0 + table.x2y2z0 * coeff.x2y2z0 + table.x3y2z0 * coeff.x3y2z0
        + table.x0y3z0 * coeff.x0y3z0 + table.x1y3z0 * coeff.x1y3z0 + table.x2y3z0 * coeff.x2y3z0
        + table.x3y3z0 * coeff.x3y3z0 + table.x0y0z1 * coeff.x0y0z1 + table.x1y0z1 * coeff.x1y0z1
        + table.x2y0z1 * coeff.x2y0z1 + table.x3y0z1 * coeff.x3y0z1 + table.x0y1z1 * coeff.x0y1z1
        + table.x1y1z1 * coeff.x1y1z1 + table.x2y1z1 * coeff.x2y1z1 + table.x3y1z1 * coeff.x3y1z1
        + table.x0y2z1 * coeff.x0y2z1 + table.x1y2z1 * coeff.x1y2z1 + table.x2y2z1 * coeff.x2y2z1
        + table.x3y2z1 * coeff.x3y2z1 + table.x0y3z1 * coeff.x0y3z1 + table.x1y3z1 * coeff.x1y3z1
        + table.x2y3z1 * coeff.x2y3z1 + table.x3y3z1 * coeff.x3y3z1 + table.x0y0z2 * coeff.x0y0z2
        + table.x1y0z2 * coeff.x1y0z2 + table.x2y0z2 * coeff.x2y0z2 + table.x3y0z2 * coeff.x3y0z2
        + table.x0y1z2 * coeff.x0y1z2 + table.x1y1z2 * coeff.x1y1z2 + table.x2y1z2 * coeff.x2y1z2
        + table.x3y1z2 * coeff.x3y1z2 + table.x0y2z2 * coeff.x0y2z2 + table.x1y2z2 * coeff.x1y2z2
        + table.x2y2z2 * coeff.x2y2z2 + table.x3y2z2 * coeff.x3y2z2 + table.x0y3z2 * coeff.x0y3z2
        + table.x1y3z2 * coeff.x1y3z2 + table.x2y3z2 * coeff.x2y3z2 + table.x3y3z2 * coeff.x3y3z2
        + table.x0y0z3 * coeff.x0y0z3 + table.x1y0z3 * coeff.x1y0z3 + table.x2y0z3 * coeff.x2y0z3
        + table.x3y0z3 * coeff.x3y0z3 + table.x0y1z3 * coeff.x0y1z3 + table.x1y1z3 * coeff.x1y1z3
        + table.x2y1z3 * coeff.x2y1z3 + table.x3y1z3 * coeff.x3y1z3 + table.x0y2z3 * coeff.x0y2z3
        + table.x1y2z3 * coeff.x1y2z3 + table.x2y2z3 * coeff.x2y2z3 + table.x3y2z3 * coeff.x3y2z3
        + table.x0y3z3 * coeff.x0y3z3 + table.x1y3z3 * coeff.x1y3z3 + table.x2y3z3 * coeff.x2y3z3
        + table.x3y3z3 * coeff.x3y3z3;
  }

  @Override
  public double value(FloatCubicSplineData table, double[] derivative1, double[] derivative2) {
    derivative1[0] = table.x0y0z0 * coeff.x1y0z0 + 2 * table.x1y0z0 * coeff.x2y0z0
        + 3 * table.x2y0z0 * coeff.x3y0z0 + table.x0y1z0 * coeff.x1y1z0
        + 2 * table.x1y1z0 * coeff.x2y1z0 + 3 * table.x2y1z0 * coeff.x3y1z0
        + table.x0y2z0 * coeff.x1y2z0 + 2 * table.x1y2z0 * coeff.x2y2z0
        + 3 * table.x2y2z0 * coeff.x3y2z0 + table.x0y3z0 * coeff.x1y3z0
        + 2 * table.x1y3z0 * coeff.x2y3z0 + 3 * table.x2y3z0 * coeff.x3y3z0
        + table.x0y0z1 * coeff.x1y0z1 + 2 * table.x1y0z1 * coeff.x2y0z1
        + 3 * table.x2y0z1 * coeff.x3y0z1 + table.x0y1z1 * coeff.x1y1z1
        + 2 * table.x1y1z1 * coeff.x2y1z1 + 3 * table.x2y1z1 * coeff.x3y1z1
        + table.x0y2z1 * coeff.x1y2z1 + 2 * table.x1y2z1 * coeff.x2y2z1
        + 3 * table.x2y2z1 * coeff.x3y2z1 + table.x0y3z1 * coeff.x1y3z1
        + 2 * table.x1y3z1 * coeff.x2y3z1 + 3 * table.x2y3z1 * coeff.x3y3z1
        + table.x0y0z2 * coeff.x1y0z2 + 2 * table.x1y0z2 * coeff.x2y0z2
        + 3 * table.x2y0z2 * coeff.x3y0z2 + table.x0y1z2 * coeff.x1y1z2
        + 2 * table.x1y1z2 * coeff.x2y1z2 + 3 * table.x2y1z2 * coeff.x3y1z2
        + table.x0y2z2 * coeff.x1y2z2 + 2 * table.x1y2z2 * coeff.x2y2z2
        + 3 * table.x2y2z2 * coeff.x3y2z2 + table.x0y3z2 * coeff.x1y3z2
        + 2 * table.x1y3z2 * coeff.x2y3z2 + 3 * table.x2y3z2 * coeff.x3y3z2
        + table.x0y0z3 * coeff.x1y0z3 + 2 * table.x1y0z3 * coeff.x2y0z3
        + 3 * table.x2y0z3 * coeff.x3y0z3 + table.x0y1z3 * coeff.x1y1z3
        + 2 * table.x1y1z3 * coeff.x2y1z3 + 3 * table.x2y1z3 * coeff.x3y1z3
        + table.x0y2z3 * coeff.x1y2z3 + 2 * table.x1y2z3 * coeff.x2y2z3
        + 3 * table.x2y2z3 * coeff.x3y2z3 + table.x0y3z3 * coeff.x1y3z3
        + 2 * table.x1y3z3 * coeff.x2y3z3 + 3 * table.x2y3z3 * coeff.x3y3z3;
    derivative1[1] = table.x0y0z0 * coeff.x0y1z0 + table.x1y0z0 * coeff.x1y1z0
        + table.x2y0z0 * coeff.x2y1z0 + table.x3y0z0 * coeff.x3y1z0
        + 2 * table.x0y1z0 * coeff.x0y2z0 + 2 * table.x1y1z0 * coeff.x1y2z0
        + 2 * table.x2y1z0 * coeff.x2y2z0 + 2 * table.x3y1z0 * coeff.x3y2z0
        + 3 * table.x0y2z0 * coeff.x0y3z0 + 3 * table.x1y2z0 * coeff.x1y3z0
        + 3 * table.x2y2z0 * coeff.x2y3z0 + 3 * table.x3y2z0 * coeff.x3y3z0
        + table.x0y0z1 * coeff.x0y1z1 + table.x1y0z1 * coeff.x1y1z1 + table.x2y0z1 * coeff.x2y1z1
        + table.x3y0z1 * coeff.x3y1z1 + 2 * table.x0y1z1 * coeff.x0y2z1
        + 2 * table.x1y1z1 * coeff.x1y2z1 + 2 * table.x2y1z1 * coeff.x2y2z1
        + 2 * table.x3y1z1 * coeff.x3y2z1 + 3 * table.x0y2z1 * coeff.x0y3z1
        + 3 * table.x1y2z1 * coeff.x1y3z1 + 3 * table.x2y2z1 * coeff.x2y3z1
        + 3 * table.x3y2z1 * coeff.x3y3z1 + table.x0y0z2 * coeff.x0y1z2
        + table.x1y0z2 * coeff.x1y1z2 + table.x2y0z2 * coeff.x2y1z2 + table.x3y0z2 * coeff.x3y1z2
        + 2 * table.x0y1z2 * coeff.x0y2z2 + 2 * table.x1y1z2 * coeff.x1y2z2
        + 2 * table.x2y1z2 * coeff.x2y2z2 + 2 * table.x3y1z2 * coeff.x3y2z2
        + 3 * table.x0y2z2 * coeff.x0y3z2 + 3 * table.x1y2z2 * coeff.x1y3z2
        + 3 * table.x2y2z2 * coeff.x2y3z2 + 3 * table.x3y2z2 * coeff.x3y3z2
        + table.x0y0z3 * coeff.x0y1z3 + table.x1y0z3 * coeff.x1y1z3 + table.x2y0z3 * coeff.x2y1z3
        + table.x3y0z3 * coeff.x3y1z3 + 2 * table.x0y1z3 * coeff.x0y2z3
        + 2 * table.x1y1z3 * coeff.x1y2z3 + 2 * table.x2y1z3 * coeff.x2y2z3
        + 2 * table.x3y1z3 * coeff.x3y2z3 + 3 * table.x0y2z3 * coeff.x0y3z3
        + 3 * table.x1y2z3 * coeff.x1y3z3 + 3 * table.x2y2z3 * coeff.x2y3z3
        + 3 * table.x3y2z3 * coeff.x3y3z3;
    derivative1[2] = table.x0y0z0 * coeff.x0y0z1 + table.x1y0z0 * coeff.x1y0z1
        + table.x2y0z0 * coeff.x2y0z1 + table.x3y0z0 * coeff.x3y0z1 + table.x0y1z0 * coeff.x0y1z1
        + table.x1y1z0 * coeff.x1y1z1 + table.x2y1z0 * coeff.x2y1z1 + table.x3y1z0 * coeff.x3y1z1
        + table.x0y2z0 * coeff.x0y2z1 + table.x1y2z0 * coeff.x1y2z1 + table.x2y2z0 * coeff.x2y2z1
        + table.x3y2z0 * coeff.x3y2z1 + table.x0y3z0 * coeff.x0y3z1 + table.x1y3z0 * coeff.x1y3z1
        + table.x2y3z0 * coeff.x2y3z1 + table.x3y3z0 * coeff.x3y3z1
        + 2 * table.x0y0z1 * coeff.x0y0z2 + 2 * table.x1y0z1 * coeff.x1y0z2
        + 2 * table.x2y0z1 * coeff.x2y0z2 + 2 * table.x3y0z1 * coeff.x3y0z2
        + 2 * table.x0y1z1 * coeff.x0y1z2 + 2 * table.x1y1z1 * coeff.x1y1z2
        + 2 * table.x2y1z1 * coeff.x2y1z2 + 2 * table.x3y1z1 * coeff.x3y1z2
        + 2 * table.x0y2z1 * coeff.x0y2z2 + 2 * table.x1y2z1 * coeff.x1y2z2
        + 2 * table.x2y2z1 * coeff.x2y2z2 + 2 * table.x3y2z1 * coeff.x3y2z2
        + 2 * table.x0y3z1 * coeff.x0y3z2 + 2 * table.x1y3z1 * coeff.x1y3z2
        + 2 * table.x2y3z1 * coeff.x2y3z2 + 2 * table.x3y3z1 * coeff.x3y3z2
        + 3 * table.x0y0z2 * coeff.x0y0z3 + 3 * table.x1y0z2 * coeff.x1y0z3
        + 3 * table.x2y0z2 * coeff.x2y0z3 + 3 * table.x3y0z2 * coeff.x3y0z3
        + 3 * table.x0y1z2 * coeff.x0y1z3 + 3 * table.x1y1z2 * coeff.x1y1z3
        + 3 * table.x2y1z2 * coeff.x2y1z3 + 3 * table.x3y1z2 * coeff.x3y1z3
        + 3 * table.x0y2z2 * coeff.x0y2z3 + 3 * table.x1y2z2 * coeff.x1y2z3
        + 3 * table.x2y2z2 * coeff.x2y2z3 + 3 * table.x3y2z2 * coeff.x3y2z3
        + 3 * table.x0y3z2 * coeff.x0y3z3 + 3 * table.x1y3z2 * coeff.x1y3z3
        + 3 * table.x2y3z2 * coeff.x2y3z3 + 3 * table.x3y3z2 * coeff.x3y3z3;
    derivative2[0] = 2 * table.x0y0z0 * coeff.x2y0z0 + 6 * table.x1y0z0 * coeff.x3y0z0
        + 2 * table.x0y1z0 * coeff.x2y1z0 + 6 * table.x1y1z0 * coeff.x3y1z0
        + 2 * table.x0y2z0 * coeff.x2y2z0 + 6 * table.x1y2z0 * coeff.x3y2z0
        + 2 * table.x0y3z0 * coeff.x2y3z0 + 6 * table.x1y3z0 * coeff.x3y3z0
        + 2 * table.x0y0z1 * coeff.x2y0z1 + 6 * table.x1y0z1 * coeff.x3y0z1
        + 2 * table.x0y1z1 * coeff.x2y1z1 + 6 * table.x1y1z1 * coeff.x3y1z1
        + 2 * table.x0y2z1 * coeff.x2y2z1 + 6 * table.x1y2z1 * coeff.x3y2z1
        + 2 * table.x0y3z1 * coeff.x2y3z1 + 6 * table.x1y3z1 * coeff.x3y3z1
        + 2 * table.x0y0z2 * coeff.x2y0z2 + 6 * table.x1y0z2 * coeff.x3y0z2
        + 2 * table.x0y1z2 * coeff.x2y1z2 + 6 * table.x1y1z2 * coeff.x3y1z2
        + 2 * table.x0y2z2 * coeff.x2y2z2 + 6 * table.x1y2z2 * coeff.x3y2z2
        + 2 * table.x0y3z2 * coeff.x2y3z2 + 6 * table.x1y3z2 * coeff.x3y3z2
        + 2 * table.x0y0z3 * coeff.x2y0z3 + 6 * table.x1y0z3 * coeff.x3y0z3
        + 2 * table.x0y1z3 * coeff.x2y1z3 + 6 * table.x1y1z3 * coeff.x3y1z3
        + 2 * table.x0y2z3 * coeff.x2y2z3 + 6 * table.x1y2z3 * coeff.x3y2z3
        + 2 * table.x0y3z3 * coeff.x2y3z3 + 6 * table.x1y3z3 * coeff.x3y3z3;
    derivative2[1] = 2 * table.x0y0z0 * coeff.x0y2z0 + 2 * table.x1y0z0 * coeff.x1y2z0
        + 2 * table.x2y0z0 * coeff.x2y2z0 + 2 * table.x3y0z0 * coeff.x3y2z0
        + 6 * table.x0y1z0 * coeff.x0y3z0 + 6 * table.x1y1z0 * coeff.x1y3z0
        + 6 * table.x2y1z0 * coeff.x2y3z0 + 6 * table.x3y1z0 * coeff.x3y3z0
        + 2 * table.x0y0z1 * coeff.x0y2z1 + 2 * table.x1y0z1 * coeff.x1y2z1
        + 2 * table.x2y0z1 * coeff.x2y2z1 + 2 * table.x3y0z1 * coeff.x3y2z1
        + 6 * table.x0y1z1 * coeff.x0y3z1 + 6 * table.x1y1z1 * coeff.x1y3z1
        + 6 * table.x2y1z1 * coeff.x2y3z1 + 6 * table.x3y1z1 * coeff.x3y3z1
        + 2 * table.x0y0z2 * coeff.x0y2z2 + 2 * table.x1y0z2 * coeff.x1y2z2
        + 2 * table.x2y0z2 * coeff.x2y2z2 + 2 * table.x3y0z2 * coeff.x3y2z2
        + 6 * table.x0y1z2 * coeff.x0y3z2 + 6 * table.x1y1z2 * coeff.x1y3z2
        + 6 * table.x2y1z2 * coeff.x2y3z2 + 6 * table.x3y1z2 * coeff.x3y3z2
        + 2 * table.x0y0z3 * coeff.x0y2z3 + 2 * table.x1y0z3 * coeff.x1y2z3
        + 2 * table.x2y0z3 * coeff.x2y2z3 + 2 * table.x3y0z3 * coeff.x3y2z3
        + 6 * table.x0y1z3 * coeff.x0y3z3 + 6 * table.x1y1z3 * coeff.x1y3z3
        + 6 * table.x2y1z3 * coeff.x2y3z3 + 6 * table.x3y1z3 * coeff.x3y3z3;
    derivative2[2] = 2 * table.x0y0z0 * coeff.x0y0z2 + 2 * table.x1y0z0 * coeff.x1y0z2
        + 2 * table.x2y0z0 * coeff.x2y0z2 + 2 * table.x3y0z0 * coeff.x3y0z2
        + 2 * table.x0y1z0 * coeff.x0y1z2 + 2 * table.x1y1z0 * coeff.x1y1z2
        + 2 * table.x2y1z0 * coeff.x2y1z2 + 2 * table.x3y1z0 * coeff.x3y1z2
        + 2 * table.x0y2z0 * coeff.x0y2z2 + 2 * table.x1y2z0 * coeff.x1y2z2
        + 2 * table.x2y2z0 * coeff.x2y2z2 + 2 * table.x3y2z0 * coeff.x3y2z2
        + 2 * table.x0y3z0 * coeff.x0y3z2 + 2 * table.x1y3z0 * coeff.x1y3z2
        + 2 * table.x2y3z0 * coeff.x2y3z2 + 2 * table.x3y3z0 * coeff.x3y3z2
        + 6 * table.x0y0z1 * coeff.x0y0z3 + 6 * table.x1y0z1 * coeff.x1y0z3
        + 6 * table.x2y0z1 * coeff.x2y0z3 + 6 * table.x3y0z1 * coeff.x3y0z3
        + 6 * table.x0y1z1 * coeff.x0y1z3 + 6 * table.x1y1z1 * coeff.x1y1z3
        + 6 * table.x2y1z1 * coeff.x2y1z3 + 6 * table.x3y1z1 * coeff.x3y1z3
        + 6 * table.x0y2z1 * coeff.x0y2z3 + 6 * table.x1y2z1 * coeff.x1y2z3
        + 6 * table.x2y2z1 * coeff.x2y2z3 + 6 * table.x3y2z1 * coeff.x3y2z3
        + 6 * table.x0y3z1 * coeff.x0y3z3 + 6 * table.x1y3z1 * coeff.x1y3z3
        + 6 * table.x2y3z1 * coeff.x2y3z3 + 6 * table.x3y3z1 * coeff.x3y3z3;
    return table.x0y0z0 * coeff.x0y0z0 + table.x1y0z0 * coeff.x1y0z0 + table.x2y0z0 * coeff.x2y0z0
        + table.x3y0z0 * coeff.x3y0z0 + table.x0y1z0 * coeff.x0y1z0 + table.x1y1z0 * coeff.x1y1z0
        + table.x2y1z0 * coeff.x2y1z0 + table.x3y1z0 * coeff.x3y1z0 + table.x0y2z0 * coeff.x0y2z0
        + table.x1y2z0 * coeff.x1y2z0 + table.x2y2z0 * coeff.x2y2z0 + table.x3y2z0 * coeff.x3y2z0
        + table.x0y3z0 * coeff.x0y3z0 + table.x1y3z0 * coeff.x1y3z0 + table.x2y3z0 * coeff.x2y3z0
        + table.x3y3z0 * coeff.x3y3z0 + table.x0y0z1 * coeff.x0y0z1 + table.x1y0z1 * coeff.x1y0z1
        + table.x2y0z1 * coeff.x2y0z1 + table.x3y0z1 * coeff.x3y0z1 + table.x0y1z1 * coeff.x0y1z1
        + table.x1y1z1 * coeff.x1y1z1 + table.x2y1z1 * coeff.x2y1z1 + table.x3y1z1 * coeff.x3y1z1
        + table.x0y2z1 * coeff.x0y2z1 + table.x1y2z1 * coeff.x1y2z1 + table.x2y2z1 * coeff.x2y2z1
        + table.x3y2z1 * coeff.x3y2z1 + table.x0y3z1 * coeff.x0y3z1 + table.x1y3z1 * coeff.x1y3z1
        + table.x2y3z1 * coeff.x2y3z1 + table.x3y3z1 * coeff.x3y3z1 + table.x0y0z2 * coeff.x0y0z2
        + table.x1y0z2 * coeff.x1y0z2 + table.x2y0z2 * coeff.x2y0z2 + table.x3y0z2 * coeff.x3y0z2
        + table.x0y1z2 * coeff.x0y1z2 + table.x1y1z2 * coeff.x1y1z2 + table.x2y1z2 * coeff.x2y1z2
        + table.x3y1z2 * coeff.x3y1z2 + table.x0y2z2 * coeff.x0y2z2 + table.x1y2z2 * coeff.x1y2z2
        + table.x2y2z2 * coeff.x2y2z2 + table.x3y2z2 * coeff.x3y2z2 + table.x0y3z2 * coeff.x0y3z2
        + table.x1y3z2 * coeff.x1y3z2 + table.x2y3z2 * coeff.x2y3z2 + table.x3y3z2 * coeff.x3y3z2
        + table.x0y0z3 * coeff.x0y0z3 + table.x1y0z3 * coeff.x1y0z3 + table.x2y0z3 * coeff.x2y0z3
        + table.x3y0z3 * coeff.x3y0z3 + table.x0y1z3 * coeff.x0y1z3 + table.x1y1z3 * coeff.x1y1z3
        + table.x2y1z3 * coeff.x2y1z3 + table.x3y1z3 * coeff.x3y1z3 + table.x0y2z3 * coeff.x0y2z3
        + table.x1y2z3 * coeff.x1y2z3 + table.x2y2z3 * coeff.x2y2z3 + table.x3y2z3 * coeff.x3y2z3
        + table.x0y3z3 * coeff.x0y3z3 + table.x1y3z3 * coeff.x1y3z3 + table.x2y3z3 * coeff.x2y3z3
        + table.x3y3z3 * coeff.x3y3z3;
  }

  @Override
  public double value(DoubleCubicSplineData table, DoubleCubicSplineData table2,
      DoubleCubicSplineData table3, DoubleCubicSplineData table6, double[] derivative1,
      double[] derivative2) {
    derivative1[0] = table.x0y0z0 * coeff.x1y0z0 + table2.x1y0z0 * coeff.x2y0z0
        + table3.x2y0z0 * coeff.x3y0z0 + table.x0y1z0 * coeff.x1y1z0 + table2.x1y1z0 * coeff.x2y1z0
        + table3.x2y1z0 * coeff.x3y1z0 + table.x0y2z0 * coeff.x1y2z0 + table2.x1y2z0 * coeff.x2y2z0
        + table3.x2y2z0 * coeff.x3y2z0 + table.x0y3z0 * coeff.x1y3z0 + table2.x1y3z0 * coeff.x2y3z0
        + table3.x2y3z0 * coeff.x3y3z0 + table.x0y0z1 * coeff.x1y0z1 + table2.x1y0z1 * coeff.x2y0z1
        + table3.x2y0z1 * coeff.x3y0z1 + table.x0y1z1 * coeff.x1y1z1 + table2.x1y1z1 * coeff.x2y1z1
        + table3.x2y1z1 * coeff.x3y1z1 + table.x0y2z1 * coeff.x1y2z1 + table2.x1y2z1 * coeff.x2y2z1
        + table3.x2y2z1 * coeff.x3y2z1 + table.x0y3z1 * coeff.x1y3z1 + table2.x1y3z1 * coeff.x2y3z1
        + table3.x2y3z1 * coeff.x3y3z1 + table.x0y0z2 * coeff.x1y0z2 + table2.x1y0z2 * coeff.x2y0z2
        + table3.x2y0z2 * coeff.x3y0z2 + table.x0y1z2 * coeff.x1y1z2 + table2.x1y1z2 * coeff.x2y1z2
        + table3.x2y1z2 * coeff.x3y1z2 + table.x0y2z2 * coeff.x1y2z2 + table2.x1y2z2 * coeff.x2y2z2
        + table3.x2y2z2 * coeff.x3y2z2 + table.x0y3z2 * coeff.x1y3z2 + table2.x1y3z2 * coeff.x2y3z2
        + table3.x2y3z2 * coeff.x3y3z2 + table.x0y0z3 * coeff.x1y0z3 + table2.x1y0z3 * coeff.x2y0z3
        + table3.x2y0z3 * coeff.x3y0z3 + table.x0y1z3 * coeff.x1y1z3 + table2.x1y1z3 * coeff.x2y1z3
        + table3.x2y1z3 * coeff.x3y1z3 + table.x0y2z3 * coeff.x1y2z3 + table2.x1y2z3 * coeff.x2y2z3
        + table3.x2y2z3 * coeff.x3y2z3 + table.x0y3z3 * coeff.x1y3z3 + table2.x1y3z3 * coeff.x2y3z3
        + table3.x2y3z3 * coeff.x3y3z3;
    derivative1[1] = table.x0y0z0 * coeff.x0y1z0 + table.x1y0z0 * coeff.x1y1z0
        + table.x2y0z0 * coeff.x2y1z0 + table.x3y0z0 * coeff.x3y1z0 + table2.x0y1z0 * coeff.x0y2z0
        + table2.x1y1z0 * coeff.x1y2z0 + table2.x2y1z0 * coeff.x2y2z0 + table2.x3y1z0 * coeff.x3y2z0
        + table3.x0y2z0 * coeff.x0y3z0 + table3.x1y2z0 * coeff.x1y3z0 + table3.x2y2z0 * coeff.x2y3z0
        + table3.x3y2z0 * coeff.x3y3z0 + table.x0y0z1 * coeff.x0y1z1 + table.x1y0z1 * coeff.x1y1z1
        + table.x2y0z1 * coeff.x2y1z1 + table.x3y0z1 * coeff.x3y1z1 + table2.x0y1z1 * coeff.x0y2z1
        + table2.x1y1z1 * coeff.x1y2z1 + table2.x2y1z1 * coeff.x2y2z1 + table2.x3y1z1 * coeff.x3y2z1
        + table3.x0y2z1 * coeff.x0y3z1 + table3.x1y2z1 * coeff.x1y3z1 + table3.x2y2z1 * coeff.x2y3z1
        + table3.x3y2z1 * coeff.x3y3z1 + table.x0y0z2 * coeff.x0y1z2 + table.x1y0z2 * coeff.x1y1z2
        + table.x2y0z2 * coeff.x2y1z2 + table.x3y0z2 * coeff.x3y1z2 + table2.x0y1z2 * coeff.x0y2z2
        + table2.x1y1z2 * coeff.x1y2z2 + table2.x2y1z2 * coeff.x2y2z2 + table2.x3y1z2 * coeff.x3y2z2
        + table3.x0y2z2 * coeff.x0y3z2 + table3.x1y2z2 * coeff.x1y3z2 + table3.x2y2z2 * coeff.x2y3z2
        + table3.x3y2z2 * coeff.x3y3z2 + table.x0y0z3 * coeff.x0y1z3 + table.x1y0z3 * coeff.x1y1z3
        + table.x2y0z3 * coeff.x2y1z3 + table.x3y0z3 * coeff.x3y1z3 + table2.x0y1z3 * coeff.x0y2z3
        + table2.x1y1z3 * coeff.x1y2z3 + table2.x2y1z3 * coeff.x2y2z3 + table2.x3y1z3 * coeff.x3y2z3
        + table3.x0y2z3 * coeff.x0y3z3 + table3.x1y2z3 * coeff.x1y3z3 + table3.x2y2z3 * coeff.x2y3z3
        + table3.x3y2z3 * coeff.x3y3z3;
    derivative1[2] = table.x0y0z0 * coeff.x0y0z1 + table.x1y0z0 * coeff.x1y0z1
        + table.x2y0z0 * coeff.x2y0z1 + table.x3y0z0 * coeff.x3y0z1 + table.x0y1z0 * coeff.x0y1z1
        + table.x1y1z0 * coeff.x1y1z1 + table.x2y1z0 * coeff.x2y1z1 + table.x3y1z0 * coeff.x3y1z1
        + table.x0y2z0 * coeff.x0y2z1 + table.x1y2z0 * coeff.x1y2z1 + table.x2y2z0 * coeff.x2y2z1
        + table.x3y2z0 * coeff.x3y2z1 + table.x0y3z0 * coeff.x0y3z1 + table.x1y3z0 * coeff.x1y3z1
        + table.x2y3z0 * coeff.x2y3z1 + table.x3y3z0 * coeff.x3y3z1 + table2.x0y0z1 * coeff.x0y0z2
        + table2.x1y0z1 * coeff.x1y0z2 + table2.x2y0z1 * coeff.x2y0z2 + table2.x3y0z1 * coeff.x3y0z2
        + table2.x0y1z1 * coeff.x0y1z2 + table2.x1y1z1 * coeff.x1y1z2 + table2.x2y1z1 * coeff.x2y1z2
        + table2.x3y1z1 * coeff.x3y1z2 + table2.x0y2z1 * coeff.x0y2z2 + table2.x1y2z1 * coeff.x1y2z2
        + table2.x2y2z1 * coeff.x2y2z2 + table2.x3y2z1 * coeff.x3y2z2 + table2.x0y3z1 * coeff.x0y3z2
        + table2.x1y3z1 * coeff.x1y3z2 + table2.x2y3z1 * coeff.x2y3z2 + table2.x3y3z1 * coeff.x3y3z2
        + table3.x0y0z2 * coeff.x0y0z3 + table3.x1y0z2 * coeff.x1y0z3 + table3.x2y0z2 * coeff.x2y0z3
        + table3.x3y0z2 * coeff.x3y0z3 + table3.x0y1z2 * coeff.x0y1z3 + table3.x1y1z2 * coeff.x1y1z3
        + table3.x2y1z2 * coeff.x2y1z3 + table3.x3y1z2 * coeff.x3y1z3 + table3.x0y2z2 * coeff.x0y2z3
        + table3.x1y2z2 * coeff.x1y2z3 + table3.x2y2z2 * coeff.x2y2z3 + table3.x3y2z2 * coeff.x3y2z3
        + table3.x0y3z2 * coeff.x0y3z3 + table3.x1y3z2 * coeff.x1y3z3 + table3.x2y3z2 * coeff.x2y3z3
        + table3.x3y3z2 * coeff.x3y3z3;
    derivative2[0] = table2.x0y0z0 * coeff.x2y0z0 + table6.x1y0z0 * coeff.x3y0z0
        + table2.x0y1z0 * coeff.x2y1z0 + table6.x1y1z0 * coeff.x3y1z0 + table2.x0y2z0 * coeff.x2y2z0
        + table6.x1y2z0 * coeff.x3y2z0 + table2.x0y3z0 * coeff.x2y3z0 + table6.x1y3z0 * coeff.x3y3z0
        + table2.x0y0z1 * coeff.x2y0z1 + table6.x1y0z1 * coeff.x3y0z1 + table2.x0y1z1 * coeff.x2y1z1
        + table6.x1y1z1 * coeff.x3y1z1 + table2.x0y2z1 * coeff.x2y2z1 + table6.x1y2z1 * coeff.x3y2z1
        + table2.x0y3z1 * coeff.x2y3z1 + table6.x1y3z1 * coeff.x3y3z1 + table2.x0y0z2 * coeff.x2y0z2
        + table6.x1y0z2 * coeff.x3y0z2 + table2.x0y1z2 * coeff.x2y1z2 + table6.x1y1z2 * coeff.x3y1z2
        + table2.x0y2z2 * coeff.x2y2z2 + table6.x1y2z2 * coeff.x3y2z2 + table2.x0y3z2 * coeff.x2y3z2
        + table6.x1y3z2 * coeff.x3y3z2 + table2.x0y0z3 * coeff.x2y0z3 + table6.x1y0z3 * coeff.x3y0z3
        + table2.x0y1z3 * coeff.x2y1z3 + table6.x1y1z3 * coeff.x3y1z3 + table2.x0y2z3 * coeff.x2y2z3
        + table6.x1y2z3 * coeff.x3y2z3 + table2.x0y3z3 * coeff.x2y3z3
        + table6.x1y3z3 * coeff.x3y3z3;
    derivative2[1] = table2.x0y0z0 * coeff.x0y2z0 + table2.x1y0z0 * coeff.x1y2z0
        + table2.x2y0z0 * coeff.x2y2z0 + table2.x3y0z0 * coeff.x3y2z0 + table6.x0y1z0 * coeff.x0y3z0
        + table6.x1y1z0 * coeff.x1y3z0 + table6.x2y1z0 * coeff.x2y3z0 + table6.x3y1z0 * coeff.x3y3z0
        + table2.x0y0z1 * coeff.x0y2z1 + table2.x1y0z1 * coeff.x1y2z1 + table2.x2y0z1 * coeff.x2y2z1
        + table2.x3y0z1 * coeff.x3y2z1 + table6.x0y1z1 * coeff.x0y3z1 + table6.x1y1z1 * coeff.x1y3z1
        + table6.x2y1z1 * coeff.x2y3z1 + table6.x3y1z1 * coeff.x3y3z1 + table2.x0y0z2 * coeff.x0y2z2
        + table2.x1y0z2 * coeff.x1y2z2 + table2.x2y0z2 * coeff.x2y2z2 + table2.x3y0z2 * coeff.x3y2z2
        + table6.x0y1z2 * coeff.x0y3z2 + table6.x1y1z2 * coeff.x1y3z2 + table6.x2y1z2 * coeff.x2y3z2
        + table6.x3y1z2 * coeff.x3y3z2 + table2.x0y0z3 * coeff.x0y2z3 + table2.x1y0z3 * coeff.x1y2z3
        + table2.x2y0z3 * coeff.x2y2z3 + table2.x3y0z3 * coeff.x3y2z3 + table6.x0y1z3 * coeff.x0y3z3
        + table6.x1y1z3 * coeff.x1y3z3 + table6.x2y1z3 * coeff.x2y3z3
        + table6.x3y1z3 * coeff.x3y3z3;
    derivative2[2] = table2.x0y0z0 * coeff.x0y0z2 + table2.x1y0z0 * coeff.x1y0z2
        + table2.x2y0z0 * coeff.x2y0z2 + table2.x3y0z0 * coeff.x3y0z2 + table2.x0y1z0 * coeff.x0y1z2
        + table2.x1y1z0 * coeff.x1y1z2 + table2.x2y1z0 * coeff.x2y1z2 + table2.x3y1z0 * coeff.x3y1z2
        + table2.x0y2z0 * coeff.x0y2z2 + table2.x1y2z0 * coeff.x1y2z2 + table2.x2y2z0 * coeff.x2y2z2
        + table2.x3y2z0 * coeff.x3y2z2 + table2.x0y3z0 * coeff.x0y3z2 + table2.x1y3z0 * coeff.x1y3z2
        + table2.x2y3z0 * coeff.x2y3z2 + table2.x3y3z0 * coeff.x3y3z2 + table6.x0y0z1 * coeff.x0y0z3
        + table6.x1y0z1 * coeff.x1y0z3 + table6.x2y0z1 * coeff.x2y0z3 + table6.x3y0z1 * coeff.x3y0z3
        + table6.x0y1z1 * coeff.x0y1z3 + table6.x1y1z1 * coeff.x1y1z3 + table6.x2y1z1 * coeff.x2y1z3
        + table6.x3y1z1 * coeff.x3y1z3 + table6.x0y2z1 * coeff.x0y2z3 + table6.x1y2z1 * coeff.x1y2z3
        + table6.x2y2z1 * coeff.x2y2z3 + table6.x3y2z1 * coeff.x3y2z3 + table6.x0y3z1 * coeff.x0y3z3
        + table6.x1y3z1 * coeff.x1y3z3 + table6.x2y3z1 * coeff.x2y3z3
        + table6.x3y3z1 * coeff.x3y3z3;
    return table.x0y0z0 * coeff.x0y0z0 + table.x1y0z0 * coeff.x1y0z0 + table.x2y0z0 * coeff.x2y0z0
        + table.x3y0z0 * coeff.x3y0z0 + table.x0y1z0 * coeff.x0y1z0 + table.x1y1z0 * coeff.x1y1z0
        + table.x2y1z0 * coeff.x2y1z0 + table.x3y1z0 * coeff.x3y1z0 + table.x0y2z0 * coeff.x0y2z0
        + table.x1y2z0 * coeff.x1y2z0 + table.x2y2z0 * coeff.x2y2z0 + table.x3y2z0 * coeff.x3y2z0
        + table.x0y3z0 * coeff.x0y3z0 + table.x1y3z0 * coeff.x1y3z0 + table.x2y3z0 * coeff.x2y3z0
        + table.x3y3z0 * coeff.x3y3z0 + table.x0y0z1 * coeff.x0y0z1 + table.x1y0z1 * coeff.x1y0z1
        + table.x2y0z1 * coeff.x2y0z1 + table.x3y0z1 * coeff.x3y0z1 + table.x0y1z1 * coeff.x0y1z1
        + table.x1y1z1 * coeff.x1y1z1 + table.x2y1z1 * coeff.x2y1z1 + table.x3y1z1 * coeff.x3y1z1
        + table.x0y2z1 * coeff.x0y2z1 + table.x1y2z1 * coeff.x1y2z1 + table.x2y2z1 * coeff.x2y2z1
        + table.x3y2z1 * coeff.x3y2z1 + table.x0y3z1 * coeff.x0y3z1 + table.x1y3z1 * coeff.x1y3z1
        + table.x2y3z1 * coeff.x2y3z1 + table.x3y3z1 * coeff.x3y3z1 + table.x0y0z2 * coeff.x0y0z2
        + table.x1y0z2 * coeff.x1y0z2 + table.x2y0z2 * coeff.x2y0z2 + table.x3y0z2 * coeff.x3y0z2
        + table.x0y1z2 * coeff.x0y1z2 + table.x1y1z2 * coeff.x1y1z2 + table.x2y1z2 * coeff.x2y1z2
        + table.x3y1z2 * coeff.x3y1z2 + table.x0y2z2 * coeff.x0y2z2 + table.x1y2z2 * coeff.x1y2z2
        + table.x2y2z2 * coeff.x2y2z2 + table.x3y2z2 * coeff.x3y2z2 + table.x0y3z2 * coeff.x0y3z2
        + table.x1y3z2 * coeff.x1y3z2 + table.x2y3z2 * coeff.x2y3z2 + table.x3y3z2 * coeff.x3y3z2
        + table.x0y0z3 * coeff.x0y0z3 + table.x1y0z3 * coeff.x1y0z3 + table.x2y0z3 * coeff.x2y0z3
        + table.x3y0z3 * coeff.x3y0z3 + table.x0y1z3 * coeff.x0y1z3 + table.x1y1z3 * coeff.x1y1z3
        + table.x2y1z3 * coeff.x2y1z3 + table.x3y1z3 * coeff.x3y1z3 + table.x0y2z3 * coeff.x0y2z3
        + table.x1y2z3 * coeff.x1y2z3 + table.x2y2z3 * coeff.x2y2z3 + table.x3y2z3 * coeff.x3y2z3
        + table.x0y3z3 * coeff.x0y3z3 + table.x1y3z3 * coeff.x1y3z3 + table.x2y3z3 * coeff.x2y3z3
        + table.x3y3z3 * coeff.x3y3z3;
  }

  @Override
  public double value(FloatCubicSplineData table, FloatCubicSplineData table2,
      FloatCubicSplineData table3, FloatCubicSplineData table6, double[] derivative1,
      double[] derivative2) {
    derivative1[0] = table.x0y0z0 * coeff.x1y0z0 + table2.x1y0z0 * coeff.x2y0z0
        + table3.x2y0z0 * coeff.x3y0z0 + table.x0y1z0 * coeff.x1y1z0 + table2.x1y1z0 * coeff.x2y1z0
        + table3.x2y1z0 * coeff.x3y1z0 + table.x0y2z0 * coeff.x1y2z0 + table2.x1y2z0 * coeff.x2y2z0
        + table3.x2y2z0 * coeff.x3y2z0 + table.x0y3z0 * coeff.x1y3z0 + table2.x1y3z0 * coeff.x2y3z0
        + table3.x2y3z0 * coeff.x3y3z0 + table.x0y0z1 * coeff.x1y0z1 + table2.x1y0z1 * coeff.x2y0z1
        + table3.x2y0z1 * coeff.x3y0z1 + table.x0y1z1 * coeff.x1y1z1 + table2.x1y1z1 * coeff.x2y1z1
        + table3.x2y1z1 * coeff.x3y1z1 + table.x0y2z1 * coeff.x1y2z1 + table2.x1y2z1 * coeff.x2y2z1
        + table3.x2y2z1 * coeff.x3y2z1 + table.x0y3z1 * coeff.x1y3z1 + table2.x1y3z1 * coeff.x2y3z1
        + table3.x2y3z1 * coeff.x3y3z1 + table.x0y0z2 * coeff.x1y0z2 + table2.x1y0z2 * coeff.x2y0z2
        + table3.x2y0z2 * coeff.x3y0z2 + table.x0y1z2 * coeff.x1y1z2 + table2.x1y1z2 * coeff.x2y1z2
        + table3.x2y1z2 * coeff.x3y1z2 + table.x0y2z2 * coeff.x1y2z2 + table2.x1y2z2 * coeff.x2y2z2
        + table3.x2y2z2 * coeff.x3y2z2 + table.x0y3z2 * coeff.x1y3z2 + table2.x1y3z2 * coeff.x2y3z2
        + table3.x2y3z2 * coeff.x3y3z2 + table.x0y0z3 * coeff.x1y0z3 + table2.x1y0z3 * coeff.x2y0z3
        + table3.x2y0z3 * coeff.x3y0z3 + table.x0y1z3 * coeff.x1y1z3 + table2.x1y1z3 * coeff.x2y1z3
        + table3.x2y1z3 * coeff.x3y1z3 + table.x0y2z3 * coeff.x1y2z3 + table2.x1y2z3 * coeff.x2y2z3
        + table3.x2y2z3 * coeff.x3y2z3 + table.x0y3z3 * coeff.x1y3z3 + table2.x1y3z3 * coeff.x2y3z3
        + table3.x2y3z3 * coeff.x3y3z3;
    derivative1[1] = table.x0y0z0 * coeff.x0y1z0 + table.x1y0z0 * coeff.x1y1z0
        + table.x2y0z0 * coeff.x2y1z0 + table.x3y0z0 * coeff.x3y1z0 + table2.x0y1z0 * coeff.x0y2z0
        + table2.x1y1z0 * coeff.x1y2z0 + table2.x2y1z0 * coeff.x2y2z0 + table2.x3y1z0 * coeff.x3y2z0
        + table3.x0y2z0 * coeff.x0y3z0 + table3.x1y2z0 * coeff.x1y3z0 + table3.x2y2z0 * coeff.x2y3z0
        + table3.x3y2z0 * coeff.x3y3z0 + table.x0y0z1 * coeff.x0y1z1 + table.x1y0z1 * coeff.x1y1z1
        + table.x2y0z1 * coeff.x2y1z1 + table.x3y0z1 * coeff.x3y1z1 + table2.x0y1z1 * coeff.x0y2z1
        + table2.x1y1z1 * coeff.x1y2z1 + table2.x2y1z1 * coeff.x2y2z1 + table2.x3y1z1 * coeff.x3y2z1
        + table3.x0y2z1 * coeff.x0y3z1 + table3.x1y2z1 * coeff.x1y3z1 + table3.x2y2z1 * coeff.x2y3z1
        + table3.x3y2z1 * coeff.x3y3z1 + table.x0y0z2 * coeff.x0y1z2 + table.x1y0z2 * coeff.x1y1z2
        + table.x2y0z2 * coeff.x2y1z2 + table.x3y0z2 * coeff.x3y1z2 + table2.x0y1z2 * coeff.x0y2z2
        + table2.x1y1z2 * coeff.x1y2z2 + table2.x2y1z2 * coeff.x2y2z2 + table2.x3y1z2 * coeff.x3y2z2
        + table3.x0y2z2 * coeff.x0y3z2 + table3.x1y2z2 * coeff.x1y3z2 + table3.x2y2z2 * coeff.x2y3z2
        + table3.x3y2z2 * coeff.x3y3z2 + table.x0y0z3 * coeff.x0y1z3 + table.x1y0z3 * coeff.x1y1z3
        + table.x2y0z3 * coeff.x2y1z3 + table.x3y0z3 * coeff.x3y1z3 + table2.x0y1z3 * coeff.x0y2z3
        + table2.x1y1z3 * coeff.x1y2z3 + table2.x2y1z3 * coeff.x2y2z3 + table2.x3y1z3 * coeff.x3y2z3
        + table3.x0y2z3 * coeff.x0y3z3 + table3.x1y2z3 * coeff.x1y3z3 + table3.x2y2z3 * coeff.x2y3z3
        + table3.x3y2z3 * coeff.x3y3z3;
    derivative1[2] = table.x0y0z0 * coeff.x0y0z1 + table.x1y0z0 * coeff.x1y0z1
        + table.x2y0z0 * coeff.x2y0z1 + table.x3y0z0 * coeff.x3y0z1 + table.x0y1z0 * coeff.x0y1z1
        + table.x1y1z0 * coeff.x1y1z1 + table.x2y1z0 * coeff.x2y1z1 + table.x3y1z0 * coeff.x3y1z1
        + table.x0y2z0 * coeff.x0y2z1 + table.x1y2z0 * coeff.x1y2z1 + table.x2y2z0 * coeff.x2y2z1
        + table.x3y2z0 * coeff.x3y2z1 + table.x0y3z0 * coeff.x0y3z1 + table.x1y3z0 * coeff.x1y3z1
        + table.x2y3z0 * coeff.x2y3z1 + table.x3y3z0 * coeff.x3y3z1 + table2.x0y0z1 * coeff.x0y0z2
        + table2.x1y0z1 * coeff.x1y0z2 + table2.x2y0z1 * coeff.x2y0z2 + table2.x3y0z1 * coeff.x3y0z2
        + table2.x0y1z1 * coeff.x0y1z2 + table2.x1y1z1 * coeff.x1y1z2 + table2.x2y1z1 * coeff.x2y1z2
        + table2.x3y1z1 * coeff.x3y1z2 + table2.x0y2z1 * coeff.x0y2z2 + table2.x1y2z1 * coeff.x1y2z2
        + table2.x2y2z1 * coeff.x2y2z2 + table2.x3y2z1 * coeff.x3y2z2 + table2.x0y3z1 * coeff.x0y3z2
        + table2.x1y3z1 * coeff.x1y3z2 + table2.x2y3z1 * coeff.x2y3z2 + table2.x3y3z1 * coeff.x3y3z2
        + table3.x0y0z2 * coeff.x0y0z3 + table3.x1y0z2 * coeff.x1y0z3 + table3.x2y0z2 * coeff.x2y0z3
        + table3.x3y0z2 * coeff.x3y0z3 + table3.x0y1z2 * coeff.x0y1z3 + table3.x1y1z2 * coeff.x1y1z3
        + table3.x2y1z2 * coeff.x2y1z3 + table3.x3y1z2 * coeff.x3y1z3 + table3.x0y2z2 * coeff.x0y2z3
        + table3.x1y2z2 * coeff.x1y2z3 + table3.x2y2z2 * coeff.x2y2z3 + table3.x3y2z2 * coeff.x3y2z3
        + table3.x0y3z2 * coeff.x0y3z3 + table3.x1y3z2 * coeff.x1y3z3 + table3.x2y3z2 * coeff.x2y3z3
        + table3.x3y3z2 * coeff.x3y3z3;
    derivative2[0] = table2.x0y0z0 * coeff.x2y0z0 + table6.x1y0z0 * coeff.x3y0z0
        + table2.x0y1z0 * coeff.x2y1z0 + table6.x1y1z0 * coeff.x3y1z0 + table2.x0y2z0 * coeff.x2y2z0
        + table6.x1y2z0 * coeff.x3y2z0 + table2.x0y3z0 * coeff.x2y3z0 + table6.x1y3z0 * coeff.x3y3z0
        + table2.x0y0z1 * coeff.x2y0z1 + table6.x1y0z1 * coeff.x3y0z1 + table2.x0y1z1 * coeff.x2y1z1
        + table6.x1y1z1 * coeff.x3y1z1 + table2.x0y2z1 * coeff.x2y2z1 + table6.x1y2z1 * coeff.x3y2z1
        + table2.x0y3z1 * coeff.x2y3z1 + table6.x1y3z1 * coeff.x3y3z1 + table2.x0y0z2 * coeff.x2y0z2
        + table6.x1y0z2 * coeff.x3y0z2 + table2.x0y1z2 * coeff.x2y1z2 + table6.x1y1z2 * coeff.x3y1z2
        + table2.x0y2z2 * coeff.x2y2z2 + table6.x1y2z2 * coeff.x3y2z2 + table2.x0y3z2 * coeff.x2y3z2
        + table6.x1y3z2 * coeff.x3y3z2 + table2.x0y0z3 * coeff.x2y0z3 + table6.x1y0z3 * coeff.x3y0z3
        + table2.x0y1z3 * coeff.x2y1z3 + table6.x1y1z3 * coeff.x3y1z3 + table2.x0y2z3 * coeff.x2y2z3
        + table6.x1y2z3 * coeff.x3y2z3 + table2.x0y3z3 * coeff.x2y3z3
        + table6.x1y3z3 * coeff.x3y3z3;
    derivative2[1] = table2.x0y0z0 * coeff.x0y2z0 + table2.x1y0z0 * coeff.x1y2z0
        + table2.x2y0z0 * coeff.x2y2z0 + table2.x3y0z0 * coeff.x3y2z0 + table6.x0y1z0 * coeff.x0y3z0
        + table6.x1y1z0 * coeff.x1y3z0 + table6.x2y1z0 * coeff.x2y3z0 + table6.x3y1z0 * coeff.x3y3z0
        + table2.x0y0z1 * coeff.x0y2z1 + table2.x1y0z1 * coeff.x1y2z1 + table2.x2y0z1 * coeff.x2y2z1
        + table2.x3y0z1 * coeff.x3y2z1 + table6.x0y1z1 * coeff.x0y3z1 + table6.x1y1z1 * coeff.x1y3z1
        + table6.x2y1z1 * coeff.x2y3z1 + table6.x3y1z1 * coeff.x3y3z1 + table2.x0y0z2 * coeff.x0y2z2
        + table2.x1y0z2 * coeff.x1y2z2 + table2.x2y0z2 * coeff.x2y2z2 + table2.x3y0z2 * coeff.x3y2z2
        + table6.x0y1z2 * coeff.x0y3z2 + table6.x1y1z2 * coeff.x1y3z2 + table6.x2y1z2 * coeff.x2y3z2
        + table6.x3y1z2 * coeff.x3y3z2 + table2.x0y0z3 * coeff.x0y2z3 + table2.x1y0z3 * coeff.x1y2z3
        + table2.x2y0z3 * coeff.x2y2z3 + table2.x3y0z3 * coeff.x3y2z3 + table6.x0y1z3 * coeff.x0y3z3
        + table6.x1y1z3 * coeff.x1y3z3 + table6.x2y1z3 * coeff.x2y3z3
        + table6.x3y1z3 * coeff.x3y3z3;
    derivative2[2] = table2.x0y0z0 * coeff.x0y0z2 + table2.x1y0z0 * coeff.x1y0z2
        + table2.x2y0z0 * coeff.x2y0z2 + table2.x3y0z0 * coeff.x3y0z2 + table2.x0y1z0 * coeff.x0y1z2
        + table2.x1y1z0 * coeff.x1y1z2 + table2.x2y1z0 * coeff.x2y1z2 + table2.x3y1z0 * coeff.x3y1z2
        + table2.x0y2z0 * coeff.x0y2z2 + table2.x1y2z0 * coeff.x1y2z2 + table2.x2y2z0 * coeff.x2y2z2
        + table2.x3y2z0 * coeff.x3y2z2 + table2.x0y3z0 * coeff.x0y3z2 + table2.x1y3z0 * coeff.x1y3z2
        + table2.x2y3z0 * coeff.x2y3z2 + table2.x3y3z0 * coeff.x3y3z2 + table6.x0y0z1 * coeff.x0y0z3
        + table6.x1y0z1 * coeff.x1y0z3 + table6.x2y0z1 * coeff.x2y0z3 + table6.x3y0z1 * coeff.x3y0z3
        + table6.x0y1z1 * coeff.x0y1z3 + table6.x1y1z1 * coeff.x1y1z3 + table6.x2y1z1 * coeff.x2y1z3
        + table6.x3y1z1 * coeff.x3y1z3 + table6.x0y2z1 * coeff.x0y2z3 + table6.x1y2z1 * coeff.x1y2z3
        + table6.x2y2z1 * coeff.x2y2z3 + table6.x3y2z1 * coeff.x3y2z3 + table6.x0y3z1 * coeff.x0y3z3
        + table6.x1y3z1 * coeff.x1y3z3 + table6.x2y3z1 * coeff.x2y3z3
        + table6.x3y3z1 * coeff.x3y3z3;
    return table.x0y0z0 * coeff.x0y0z0 + table.x1y0z0 * coeff.x1y0z0 + table.x2y0z0 * coeff.x2y0z0
        + table.x3y0z0 * coeff.x3y0z0 + table.x0y1z0 * coeff.x0y1z0 + table.x1y1z0 * coeff.x1y1z0
        + table.x2y1z0 * coeff.x2y1z0 + table.x3y1z0 * coeff.x3y1z0 + table.x0y2z0 * coeff.x0y2z0
        + table.x1y2z0 * coeff.x1y2z0 + table.x2y2z0 * coeff.x2y2z0 + table.x3y2z0 * coeff.x3y2z0
        + table.x0y3z0 * coeff.x0y3z0 + table.x1y3z0 * coeff.x1y3z0 + table.x2y3z0 * coeff.x2y3z0
        + table.x3y3z0 * coeff.x3y3z0 + table.x0y0z1 * coeff.x0y0z1 + table.x1y0z1 * coeff.x1y0z1
        + table.x2y0z1 * coeff.x2y0z1 + table.x3y0z1 * coeff.x3y0z1 + table.x0y1z1 * coeff.x0y1z1
        + table.x1y1z1 * coeff.x1y1z1 + table.x2y1z1 * coeff.x2y1z1 + table.x3y1z1 * coeff.x3y1z1
        + table.x0y2z1 * coeff.x0y2z1 + table.x1y2z1 * coeff.x1y2z1 + table.x2y2z1 * coeff.x2y2z1
        + table.x3y2z1 * coeff.x3y2z1 + table.x0y3z1 * coeff.x0y3z1 + table.x1y3z1 * coeff.x1y3z1
        + table.x2y3z1 * coeff.x2y3z1 + table.x3y3z1 * coeff.x3y3z1 + table.x0y0z2 * coeff.x0y0z2
        + table.x1y0z2 * coeff.x1y0z2 + table.x2y0z2 * coeff.x2y0z2 + table.x3y0z2 * coeff.x3y0z2
        + table.x0y1z2 * coeff.x0y1z2 + table.x1y1z2 * coeff.x1y1z2 + table.x2y1z2 * coeff.x2y1z2
        + table.x3y1z2 * coeff.x3y1z2 + table.x0y2z2 * coeff.x0y2z2 + table.x1y2z2 * coeff.x1y2z2
        + table.x2y2z2 * coeff.x2y2z2 + table.x3y2z2 * coeff.x3y2z2 + table.x0y3z2 * coeff.x0y3z2
        + table.x1y3z2 * coeff.x1y3z2 + table.x2y3z2 * coeff.x2y3z2 + table.x3y3z2 * coeff.x3y3z2
        + table.x0y0z3 * coeff.x0y0z3 + table.x1y0z3 * coeff.x1y0z3 + table.x2y0z3 * coeff.x2y0z3
        + table.x3y0z3 * coeff.x3y0z3 + table.x0y1z3 * coeff.x0y1z3 + table.x1y1z3 * coeff.x1y1z3
        + table.x2y1z3 * coeff.x2y1z3 + table.x3y1z3 * coeff.x3y1z3 + table.x0y2z3 * coeff.x0y2z3
        + table.x1y2z3 * coeff.x1y2z3 + table.x2y2z3 * coeff.x2y2z3 + table.x3y2z3 * coeff.x3y2z3
        + table.x0y3z3 * coeff.x0y3z3 + table.x1y3z3 * coeff.x1y3z3 + table.x2y3z3 * coeff.x2y3z3
        + table.x3y3z3 * coeff.x3y3z3;
  }

  @Override
  public void gradient(DoubleCubicSplineData table, double[] derivative1) {
    derivative1[0] = table.x0y0z0 * coeff.x1y0z0 + 2 * table.x1y0z0 * coeff.x2y0z0
        + 3 * table.x2y0z0 * coeff.x3y0z0 + table.x0y1z0 * coeff.x1y1z0
        + 2 * table.x1y1z0 * coeff.x2y1z0 + 3 * table.x2y1z0 * coeff.x3y1z0
        + table.x0y2z0 * coeff.x1y2z0 + 2 * table.x1y2z0 * coeff.x2y2z0
        + 3 * table.x2y2z0 * coeff.x3y2z0 + table.x0y3z0 * coeff.x1y3z0
        + 2 * table.x1y3z0 * coeff.x2y3z0 + 3 * table.x2y3z0 * coeff.x3y3z0
        + table.x0y0z1 * coeff.x1y0z1 + 2 * table.x1y0z1 * coeff.x2y0z1
        + 3 * table.x2y0z1 * coeff.x3y0z1 + table.x0y1z1 * coeff.x1y1z1
        + 2 * table.x1y1z1 * coeff.x2y1z1 + 3 * table.x2y1z1 * coeff.x3y1z1
        + table.x0y2z1 * coeff.x1y2z1 + 2 * table.x1y2z1 * coeff.x2y2z1
        + 3 * table.x2y2z1 * coeff.x3y2z1 + table.x0y3z1 * coeff.x1y3z1
        + 2 * table.x1y3z1 * coeff.x2y3z1 + 3 * table.x2y3z1 * coeff.x3y3z1
        + table.x0y0z2 * coeff.x1y0z2 + 2 * table.x1y0z2 * coeff.x2y0z2
        + 3 * table.x2y0z2 * coeff.x3y0z2 + table.x0y1z2 * coeff.x1y1z2
        + 2 * table.x1y1z2 * coeff.x2y1z2 + 3 * table.x2y1z2 * coeff.x3y1z2
        + table.x0y2z2 * coeff.x1y2z2 + 2 * table.x1y2z2 * coeff.x2y2z2
        + 3 * table.x2y2z2 * coeff.x3y2z2 + table.x0y3z2 * coeff.x1y3z2
        + 2 * table.x1y3z2 * coeff.x2y3z2 + 3 * table.x2y3z2 * coeff.x3y3z2
        + table.x0y0z3 * coeff.x1y0z3 + 2 * table.x1y0z3 * coeff.x2y0z3
        + 3 * table.x2y0z3 * coeff.x3y0z3 + table.x0y1z3 * coeff.x1y1z3
        + 2 * table.x1y1z3 * coeff.x2y1z3 + 3 * table.x2y1z3 * coeff.x3y1z3
        + table.x0y2z3 * coeff.x1y2z3 + 2 * table.x1y2z3 * coeff.x2y2z3
        + 3 * table.x2y2z3 * coeff.x3y2z3 + table.x0y3z3 * coeff.x1y3z3
        + 2 * table.x1y3z3 * coeff.x2y3z3 + 3 * table.x2y3z3 * coeff.x3y3z3;
    derivative1[1] = table.x0y0z0 * coeff.x0y1z0 + table.x1y0z0 * coeff.x1y1z0
        + table.x2y0z0 * coeff.x2y1z0 + table.x3y0z0 * coeff.x3y1z0
        + 2 * table.x0y1z0 * coeff.x0y2z0 + 2 * table.x1y1z0 * coeff.x1y2z0
        + 2 * table.x2y1z0 * coeff.x2y2z0 + 2 * table.x3y1z0 * coeff.x3y2z0
        + 3 * table.x0y2z0 * coeff.x0y3z0 + 3 * table.x1y2z0 * coeff.x1y3z0
        + 3 * table.x2y2z0 * coeff.x2y3z0 + 3 * table.x3y2z0 * coeff.x3y3z0
        + table.x0y0z1 * coeff.x0y1z1 + table.x1y0z1 * coeff.x1y1z1 + table.x2y0z1 * coeff.x2y1z1
        + table.x3y0z1 * coeff.x3y1z1 + 2 * table.x0y1z1 * coeff.x0y2z1
        + 2 * table.x1y1z1 * coeff.x1y2z1 + 2 * table.x2y1z1 * coeff.x2y2z1
        + 2 * table.x3y1z1 * coeff.x3y2z1 + 3 * table.x0y2z1 * coeff.x0y3z1
        + 3 * table.x1y2z1 * coeff.x1y3z1 + 3 * table.x2y2z1 * coeff.x2y3z1
        + 3 * table.x3y2z1 * coeff.x3y3z1 + table.x0y0z2 * coeff.x0y1z2
        + table.x1y0z2 * coeff.x1y1z2 + table.x2y0z2 * coeff.x2y1z2 + table.x3y0z2 * coeff.x3y1z2
        + 2 * table.x0y1z2 * coeff.x0y2z2 + 2 * table.x1y1z2 * coeff.x1y2z2
        + 2 * table.x2y1z2 * coeff.x2y2z2 + 2 * table.x3y1z2 * coeff.x3y2z2
        + 3 * table.x0y2z2 * coeff.x0y3z2 + 3 * table.x1y2z2 * coeff.x1y3z2
        + 3 * table.x2y2z2 * coeff.x2y3z2 + 3 * table.x3y2z2 * coeff.x3y3z2
        + table.x0y0z3 * coeff.x0y1z3 + table.x1y0z3 * coeff.x1y1z3 + table.x2y0z3 * coeff.x2y1z3
        + table.x3y0z3 * coeff.x3y1z3 + 2 * table.x0y1z3 * coeff.x0y2z3
        + 2 * table.x1y1z3 * coeff.x1y2z3 + 2 * table.x2y1z3 * coeff.x2y2z3
        + 2 * table.x3y1z3 * coeff.x3y2z3 + 3 * table.x0y2z3 * coeff.x0y3z3
        + 3 * table.x1y2z3 * coeff.x1y3z3 + 3 * table.x2y2z3 * coeff.x2y3z3
        + 3 * table.x3y2z3 * coeff.x3y3z3;
    derivative1[2] = table.x0y0z0 * coeff.x0y0z1 + table.x1y0z0 * coeff.x1y0z1
        + table.x2y0z0 * coeff.x2y0z1 + table.x3y0z0 * coeff.x3y0z1 + table.x0y1z0 * coeff.x0y1z1
        + table.x1y1z0 * coeff.x1y1z1 + table.x2y1z0 * coeff.x2y1z1 + table.x3y1z0 * coeff.x3y1z1
        + table.x0y2z0 * coeff.x0y2z1 + table.x1y2z0 * coeff.x1y2z1 + table.x2y2z0 * coeff.x2y2z1
        + table.x3y2z0 * coeff.x3y2z1 + table.x0y3z0 * coeff.x0y3z1 + table.x1y3z0 * coeff.x1y3z1
        + table.x2y3z0 * coeff.x2y3z1 + table.x3y3z0 * coeff.x3y3z1
        + 2 * table.x0y0z1 * coeff.x0y0z2 + 2 * table.x1y0z1 * coeff.x1y0z2
        + 2 * table.x2y0z1 * coeff.x2y0z2 + 2 * table.x3y0z1 * coeff.x3y0z2
        + 2 * table.x0y1z1 * coeff.x0y1z2 + 2 * table.x1y1z1 * coeff.x1y1z2
        + 2 * table.x2y1z1 * coeff.x2y1z2 + 2 * table.x3y1z1 * coeff.x3y1z2
        + 2 * table.x0y2z1 * coeff.x0y2z2 + 2 * table.x1y2z1 * coeff.x1y2z2
        + 2 * table.x2y2z1 * coeff.x2y2z2 + 2 * table.x3y2z1 * coeff.x3y2z2
        + 2 * table.x0y3z1 * coeff.x0y3z2 + 2 * table.x1y3z1 * coeff.x1y3z2
        + 2 * table.x2y3z1 * coeff.x2y3z2 + 2 * table.x3y3z1 * coeff.x3y3z2
        + 3 * table.x0y0z2 * coeff.x0y0z3 + 3 * table.x1y0z2 * coeff.x1y0z3
        + 3 * table.x2y0z2 * coeff.x2y0z3 + 3 * table.x3y0z2 * coeff.x3y0z3
        + 3 * table.x0y1z2 * coeff.x0y1z3 + 3 * table.x1y1z2 * coeff.x1y1z3
        + 3 * table.x2y1z2 * coeff.x2y1z3 + 3 * table.x3y1z2 * coeff.x3y1z3
        + 3 * table.x0y2z2 * coeff.x0y2z3 + 3 * table.x1y2z2 * coeff.x1y2z3
        + 3 * table.x2y2z2 * coeff.x2y2z3 + 3 * table.x3y2z2 * coeff.x3y2z3
        + 3 * table.x0y3z2 * coeff.x0y3z3 + 3 * table.x1y3z2 * coeff.x1y3z3
        + 3 * table.x2y3z2 * coeff.x2y3z3 + 3 * table.x3y3z2 * coeff.x3y3z3;
  }

  @Override
  public void gradient(FloatCubicSplineData table, double[] derivative1) {
    derivative1[0] = table.x0y0z0 * coeff.x1y0z0 + 2 * table.x1y0z0 * coeff.x2y0z0
        + 3 * table.x2y0z0 * coeff.x3y0z0 + table.x0y1z0 * coeff.x1y1z0
        + 2 * table.x1y1z0 * coeff.x2y1z0 + 3 * table.x2y1z0 * coeff.x3y1z0
        + table.x0y2z0 * coeff.x1y2z0 + 2 * table.x1y2z0 * coeff.x2y2z0
        + 3 * table.x2y2z0 * coeff.x3y2z0 + table.x0y3z0 * coeff.x1y3z0
        + 2 * table.x1y3z0 * coeff.x2y3z0 + 3 * table.x2y3z0 * coeff.x3y3z0
        + table.x0y0z1 * coeff.x1y0z1 + 2 * table.x1y0z1 * coeff.x2y0z1
        + 3 * table.x2y0z1 * coeff.x3y0z1 + table.x0y1z1 * coeff.x1y1z1
        + 2 * table.x1y1z1 * coeff.x2y1z1 + 3 * table.x2y1z1 * coeff.x3y1z1
        + table.x0y2z1 * coeff.x1y2z1 + 2 * table.x1y2z1 * coeff.x2y2z1
        + 3 * table.x2y2z1 * coeff.x3y2z1 + table.x0y3z1 * coeff.x1y3z1
        + 2 * table.x1y3z1 * coeff.x2y3z1 + 3 * table.x2y3z1 * coeff.x3y3z1
        + table.x0y0z2 * coeff.x1y0z2 + 2 * table.x1y0z2 * coeff.x2y0z2
        + 3 * table.x2y0z2 * coeff.x3y0z2 + table.x0y1z2 * coeff.x1y1z2
        + 2 * table.x1y1z2 * coeff.x2y1z2 + 3 * table.x2y1z2 * coeff.x3y1z2
        + table.x0y2z2 * coeff.x1y2z2 + 2 * table.x1y2z2 * coeff.x2y2z2
        + 3 * table.x2y2z2 * coeff.x3y2z2 + table.x0y3z2 * coeff.x1y3z2
        + 2 * table.x1y3z2 * coeff.x2y3z2 + 3 * table.x2y3z2 * coeff.x3y3z2
        + table.x0y0z3 * coeff.x1y0z3 + 2 * table.x1y0z3 * coeff.x2y0z3
        + 3 * table.x2y0z3 * coeff.x3y0z3 + table.x0y1z3 * coeff.x1y1z3
        + 2 * table.x1y1z3 * coeff.x2y1z3 + 3 * table.x2y1z3 * coeff.x3y1z3
        + table.x0y2z3 * coeff.x1y2z3 + 2 * table.x1y2z3 * coeff.x2y2z3
        + 3 * table.x2y2z3 * coeff.x3y2z3 + table.x0y3z3 * coeff.x1y3z3
        + 2 * table.x1y3z3 * coeff.x2y3z3 + 3 * table.x2y3z3 * coeff.x3y3z3;
    derivative1[1] = table.x0y0z0 * coeff.x0y1z0 + table.x1y0z0 * coeff.x1y1z0
        + table.x2y0z0 * coeff.x2y1z0 + table.x3y0z0 * coeff.x3y1z0
        + 2 * table.x0y1z0 * coeff.x0y2z0 + 2 * table.x1y1z0 * coeff.x1y2z0
        + 2 * table.x2y1z0 * coeff.x2y2z0 + 2 * table.x3y1z0 * coeff.x3y2z0
        + 3 * table.x0y2z0 * coeff.x0y3z0 + 3 * table.x1y2z0 * coeff.x1y3z0
        + 3 * table.x2y2z0 * coeff.x2y3z0 + 3 * table.x3y2z0 * coeff.x3y3z0
        + table.x0y0z1 * coeff.x0y1z1 + table.x1y0z1 * coeff.x1y1z1 + table.x2y0z1 * coeff.x2y1z1
        + table.x3y0z1 * coeff.x3y1z1 + 2 * table.x0y1z1 * coeff.x0y2z1
        + 2 * table.x1y1z1 * coeff.x1y2z1 + 2 * table.x2y1z1 * coeff.x2y2z1
        + 2 * table.x3y1z1 * coeff.x3y2z1 + 3 * table.x0y2z1 * coeff.x0y3z1
        + 3 * table.x1y2z1 * coeff.x1y3z1 + 3 * table.x2y2z1 * coeff.x2y3z1
        + 3 * table.x3y2z1 * coeff.x3y3z1 + table.x0y0z2 * coeff.x0y1z2
        + table.x1y0z2 * coeff.x1y1z2 + table.x2y0z2 * coeff.x2y1z2 + table.x3y0z2 * coeff.x3y1z2
        + 2 * table.x0y1z2 * coeff.x0y2z2 + 2 * table.x1y1z2 * coeff.x1y2z2
        + 2 * table.x2y1z2 * coeff.x2y2z2 + 2 * table.x3y1z2 * coeff.x3y2z2
        + 3 * table.x0y2z2 * coeff.x0y3z2 + 3 * table.x1y2z2 * coeff.x1y3z2
        + 3 * table.x2y2z2 * coeff.x2y3z2 + 3 * table.x3y2z2 * coeff.x3y3z2
        + table.x0y0z3 * coeff.x0y1z3 + table.x1y0z3 * coeff.x1y1z3 + table.x2y0z3 * coeff.x2y1z3
        + table.x3y0z3 * coeff.x3y1z3 + 2 * table.x0y1z3 * coeff.x0y2z3
        + 2 * table.x1y1z3 * coeff.x1y2z3 + 2 * table.x2y1z3 * coeff.x2y2z3
        + 2 * table.x3y1z3 * coeff.x3y2z3 + 3 * table.x0y2z3 * coeff.x0y3z3
        + 3 * table.x1y2z3 * coeff.x1y3z3 + 3 * table.x2y2z3 * coeff.x2y3z3
        + 3 * table.x3y2z3 * coeff.x3y3z3;
    derivative1[2] = table.x0y0z0 * coeff.x0y0z1 + table.x1y0z0 * coeff.x1y0z1
        + table.x2y0z0 * coeff.x2y0z1 + table.x3y0z0 * coeff.x3y0z1 + table.x0y1z0 * coeff.x0y1z1
        + table.x1y1z0 * coeff.x1y1z1 + table.x2y1z0 * coeff.x2y1z1 + table.x3y1z0 * coeff.x3y1z1
        + table.x0y2z0 * coeff.x0y2z1 + table.x1y2z0 * coeff.x1y2z1 + table.x2y2z0 * coeff.x2y2z1
        + table.x3y2z0 * coeff.x3y2z1 + table.x0y3z0 * coeff.x0y3z1 + table.x1y3z0 * coeff.x1y3z1
        + table.x2y3z0 * coeff.x2y3z1 + table.x3y3z0 * coeff.x3y3z1
        + 2 * table.x0y0z1 * coeff.x0y0z2 + 2 * table.x1y0z1 * coeff.x1y0z2
        + 2 * table.x2y0z1 * coeff.x2y0z2 + 2 * table.x3y0z1 * coeff.x3y0z2
        + 2 * table.x0y1z1 * coeff.x0y1z2 + 2 * table.x1y1z1 * coeff.x1y1z2
        + 2 * table.x2y1z1 * coeff.x2y1z2 + 2 * table.x3y1z1 * coeff.x3y1z2
        + 2 * table.x0y2z1 * coeff.x0y2z2 + 2 * table.x1y2z1 * coeff.x1y2z2
        + 2 * table.x2y2z1 * coeff.x2y2z2 + 2 * table.x3y2z1 * coeff.x3y2z2
        + 2 * table.x0y3z1 * coeff.x0y3z2 + 2 * table.x1y3z1 * coeff.x1y3z2
        + 2 * table.x2y3z1 * coeff.x2y3z2 + 2 * table.x3y3z1 * coeff.x3y3z2
        + 3 * table.x0y0z2 * coeff.x0y0z3 + 3 * table.x1y0z2 * coeff.x1y0z3
        + 3 * table.x2y0z2 * coeff.x2y0z3 + 3 * table.x3y0z2 * coeff.x3y0z3
        + 3 * table.x0y1z2 * coeff.x0y1z3 + 3 * table.x1y1z2 * coeff.x1y1z3
        + 3 * table.x2y1z2 * coeff.x2y1z3 + 3 * table.x3y1z2 * coeff.x3y1z3
        + 3 * table.x0y2z2 * coeff.x0y2z3 + 3 * table.x1y2z2 * coeff.x1y2z3
        + 3 * table.x2y2z2 * coeff.x2y2z3 + 3 * table.x3y2z2 * coeff.x3y2z3
        + 3 * table.x0y3z2 * coeff.x0y3z3 + 3 * table.x1y3z2 * coeff.x1y3z3
        + 3 * table.x2y3z2 * coeff.x2y3z3 + 3 * table.x3y3z2 * coeff.x3y3z3;
  }
}
