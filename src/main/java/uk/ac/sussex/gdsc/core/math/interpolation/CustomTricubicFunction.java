/*-
 * %%Ignore-License
 *
 * GDSC Software
 *
 * This is an extension of the
 * org.apache.commons.math3.analysis.interpolation.TricubicFunction
 *
 * Modifications have been made to allow computation of gradients and computation
 * with pre-computed x,y,z powers using single/floating precision.
 *
 * The code is released under the original Apache licence:
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.sussex.gdsc.core.math.interpolation;

import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;

import org.apache.commons.math3.analysis.TrivariateFunction;
import org.apache.commons.math3.exception.OutOfRangeException;

/**
 * 3D-spline function.
 */
public abstract class CustomTricubicFunction implements TrivariateFunction {
  /**
   * Get a copy of the 64 coefficients for the tricubic function.
   *
   * @return the coefficients
   */
  public abstract double[] getCoefficients();

  /**
   * Get coefficient at the specified index for the tricubic function.
   *
   * @param index the index
   * @return the coefficient
   */
  public abstract double get(int index);

  /**
   * Get the float-valued coefficient at the specified index for the tricubic function.
   *
   * @param index the index
   * @return the float-valued coefficient
   */
  public abstract float getf(int index);

  /**
   * Checks if is single precision.
   *
   * @return true, if is single precision
   */
  public abstract boolean isSinglePrecision();

  /**
   * Convert this instance to single precision.
   *
   * @return the custom tricubic function
   */
  public abstract CustomTricubicFunction toSinglePrecision();

  /**
   * Convert this instance to double precision.
   *
   * @return the custom tricubic function
   */
  public abstract CustomTricubicFunction toDoublePrecision();

  /**
   * Copy the function.
   *
   * @return the custom tricubic function
   */
  public abstract CustomTricubicFunction copy();

  /**
   * Gets the index in the table of 64 coefficients for the specified power of each dimension.
   *
   * @param powerX the x power
   * @param powerY the y power
   * @param powerZ the z power
   * @return the index
   */
  static int getIndex(int powerX, int powerY, int powerZ) {
    return powerX + 4 * (powerY + 4 * powerZ);
  }

  /**
   * Compute the value with no interpolation (i.e. x=0,y=0,z=0).
   *
   * @return the interpolated value.
   */
  public abstract double value000();

  /**
   * Compute the value and partial first-order derivatives with no interpolation (i.e. x=0,y=0,z=0).
   *
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @return the interpolated value.
   */
  public abstract double value000(double[] derivative1);

  /**
   * Compute the value and partial first-order and second-order derivatives with no interpolation
   * (i.e. x=0,y=0,z=0).
   *
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @param derivative2 the partial second order derivatives with respect to x,y,z
   * @return the interpolated value.
   */
  public abstract double value000(double[] derivative1, double[] derivative2);

  /**
   * Get the interpolated value.
   *
   * @param powerX x-coordinate powers of the interpolation point.
   * @param powerY y-coordinate powers of the interpolation point.
   * @param powerZ z-coordinate powers of the interpolation point.
   * @return the interpolated value.
   */
  protected abstract double value0(final double[] powerX, final double[] powerY,
      final double[] powerZ);

  /**
   * Compute the value and partial first-order derivatives.
   *
   * @param powerX x-coordinate powers of the interpolation point.
   * @param powerY y-coordinate powers of the interpolation point.
   * @param powerZ z-coordinate powers of the interpolation point.
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @return the interpolated value.
   */
  protected abstract double value1(final double[] powerX, final double[] powerY,
      final double[] powerZ, final double[] derivative1);

  /**
   * Compute the value and partial first-order and second-order derivatives.
   *
   * @param powerX x-coordinate powers of the interpolation point.
   * @param powerY y-coordinate powers of the interpolation point.
   * @param powerZ z-coordinate powers of the interpolation point.
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @param derivative2 the partial second order derivatives with respect to x,y,z
   * @return the interpolated value.
   */
  protected abstract double value2(final double[] powerX, final double[] powerY,
      final double[] powerZ, final double[] derivative1, double[] derivative2);

  /**
   * Compute the power table.
   *
   * @param x x-coordinate of the interpolation point.
   * @param y y-coordinate of the interpolation point.
   * @param z z-coordinate of the interpolation point.
   * @return the power table. @ if {@code x}, {@code y} or {@code z} are not in the interval
   *         {@code [0, 1]}.
   */
  public static double[] computePowerTable(double x, double y, double z) {
    if (x < 0 || x > 1) {
      throw new OutOfRangeException(x, 0, 1);
    }
    if (y < 0 || y > 1) {
      throw new OutOfRangeException(y, 0, 1);
    }
    if (z < 0 || z > 1) {
      throw new OutOfRangeException(z, 0, 1);
    }

    final double x2 = x * x;
    final double x3 = x2 * x;
    final double[] powerX = { /* 1, optimised out */ x, x2, x3};

    final double y2 = y * y;
    final double y3 = y2 * y;
    final double[] powerY = { /* 1, optimised out */ y, y2, y3};

    final double z2 = z * z;
    final double z3 = z2 * z;
    final double[] powerZ = { /* 1, optimised out */ z, z2, z3};

    return computePowerTable(powerX, powerY, powerZ);
  }

  /**
   * Compute the power table.
   *
   * @param x x-coordinate of the interpolation point.
   * @param y y-coordinate of the interpolation point.
   * @param z z-coordinate of the interpolation point.
   * @return the power table.
   */
  public static double[] computePowerTable(CubicSplinePosition x, CubicSplinePosition y,
      CubicSplinePosition z) {
    return computePowerTable(x.power, y.power, z.power);
  }

  /**
   * Compute the power table.
   *
   * @param powerX x-coordinate powers of the interpolation point.
   * @param powerY y-coordinate powers of the interpolation point.
   * @param powerZ z-coordinate powers of the interpolation point.
   * @return the power table.
   */
  private static double[] computePowerTable(final double[] powerX, final double[] powerY,
      final double[] powerZ) {
    final double[] table = new double[64];

    table[0] = 1;
    table[1] = powerX[0];
    table[2] = powerX[1];
    table[3] = powerX[2];
    table[4] = powerY[0];
    table[5] = powerY[0] * powerX[0];
    table[6] = powerY[0] * powerX[1];
    table[7] = powerY[0] * powerX[2];
    table[8] = powerY[1];
    table[9] = powerY[1] * powerX[0];
    table[10] = powerY[1] * powerX[1];
    table[11] = powerY[1] * powerX[2];
    table[12] = powerY[2];
    table[13] = powerY[2] * powerX[0];
    table[14] = powerY[2] * powerX[1];
    table[15] = powerY[2] * powerX[2];
    table[16] = powerZ[0];
    table[17] = powerZ[0] * powerX[0];
    table[18] = powerZ[0] * powerX[1];
    table[19] = powerZ[0] * powerX[2];
    table[20] = powerZ[0] * powerY[0];
    table[21] = table[20] * powerX[0];
    table[22] = table[20] * powerX[1];
    table[23] = table[20] * powerX[2];
    table[24] = powerZ[0] * powerY[1];
    table[25] = table[24] * powerX[0];
    table[26] = table[24] * powerX[1];
    table[27] = table[24] * powerX[2];
    table[28] = powerZ[0] * powerY[2];
    table[29] = table[28] * powerX[0];
    table[30] = table[28] * powerX[1];
    table[31] = table[28] * powerX[2];
    table[32] = powerZ[1];
    table[33] = powerZ[1] * powerX[0];
    table[34] = powerZ[1] * powerX[1];
    table[35] = powerZ[1] * powerX[2];
    table[36] = powerZ[1] * powerY[0];
    table[37] = table[36] * powerX[0];
    table[38] = table[36] * powerX[1];
    table[39] = table[36] * powerX[2];
    table[40] = powerZ[1] * powerY[1];
    table[41] = table[40] * powerX[0];
    table[42] = table[40] * powerX[1];
    table[43] = table[40] * powerX[2];
    table[44] = powerZ[1] * powerY[2];
    table[45] = table[44] * powerX[0];
    table[46] = table[44] * powerX[1];
    table[47] = table[44] * powerX[2];
    table[48] = powerZ[2];
    table[49] = powerZ[2] * powerX[0];
    table[50] = powerZ[2] * powerX[1];
    table[51] = powerZ[2] * powerX[2];
    table[52] = powerZ[2] * powerY[0];
    table[53] = table[52] * powerX[0];
    table[54] = table[52] * powerX[1];
    table[55] = table[52] * powerX[2];
    table[56] = powerZ[2] * powerY[1];
    table[57] = table[56] * powerX[0];
    table[58] = table[56] * powerX[1];
    table[59] = table[56] * powerX[2];
    table[60] = powerZ[2] * powerY[2];
    table[61] = table[60] * powerX[0];
    table[62] = table[60] * powerX[1];
    table[63] = table[60] * powerX[2];

    return table;
  }

  /**
   * Checks if the power table is at the boundary of the interpolation range for the given
   * dimension, i.e. 0 or 1
   *
   * @param dimension the dimension [x=0,y=1,z=2]
   * @param table the table
   * @return true, if at the boundary
   */
  public static boolean isBoundary(int dimension, double[] table) {
    switch (dimension) {
      case 0:
        return isBoundary(table[1]);
      case 1:
        return isBoundary(table[4]);
      case 2:
        return isBoundary(table[16]);
      default:
        return false;
    }
  }

  /**
   * Checks if the power table is at the boundary of the interpolation range for the given
   * dimension, i.e. 0 or 1
   *
   * @param dimension the dimension [x=0,y=1,z=2]
   * @param table the table
   * @return true, if at the boundary
   */
  public static boolean isBoundary(int dimension, float[] table) {
    switch (dimension) {
      case 0:
        return isBoundary(table[1]);
      case 1:
        return isBoundary(table[4]);
      case 2:
        return isBoundary(table[16]);
      default:
        return false;
    }
  }

  /**
   * Checks if the value is 0 or 1.
   *
   * @param value the value
   * @return true, if 0 or 1
   */
  private static boolean isBoundary(double value) {
    return value == 0 || value == 1;
  }

  /**
   * Compute the power table.
   *
   * @param x x-coordinate of the interpolation point.
   * @param y y-coordinate of the interpolation point.
   * @param z z-coordinate of the interpolation point.
   * @return the power table. @ if {@code x}, {@code y} or {@code z} are not in the interval
   *         {@code [0, 1]}.
   */
  public static float[] computeFloatPowerTable(double x, double y, double z) {
    // Compute as a double for precision
    return toFloat(computePowerTable(x, y, z));
  }

  /**
   * Compute the power table.
   *
   * @param x x-coordinate of the interpolation point.
   * @param y y-coordinate of the interpolation point.
   * @param z z-coordinate of the interpolation point.
   * @return the power table.
   */
  public static float[] computeFloatPowerTable(CubicSplinePosition x, CubicSplinePosition y,
      CubicSplinePosition z) {
    // Compute as a double for precision
    return toFloat(computePowerTable(x, y, z));
  }

  /**
   * Convert a length 64 array to a float.
   *
   * @param values the values
   * @return the float array
   */
  protected static float[] toFloat(double[] values) {
    final float[] f = new float[64];
    for (int i = 0; i < 64; i++) {
      f[i] = (float) values[i];
    }
    return f;
  }

  /**
   * Convert a length 64 array to a double.
   *
   * @param values the values
   * @return the double array
   */
  protected static double[] toDouble(float[] values) {
    final double[] d = new double[64];
    for (int i = 0; i < 64; i++) {
      d[i] = values[i];
    }
    return d;
  }

  /**
   * Scale the power table. The scaled table can be used for fast computation of the gradients.
   *
   * @param table the table
   * @param scale the scale
   * @return the scaled table
   */
  public static double[] scalePowerTable(double[] table, int scale) {
    final double[] tableN = new double[64];
    for (int i = 0; i < 64; i++) {
      tableN[i] = scale * table[i];
    }
    return tableN;
  }

  /**
   * Scale the power table. The scaled table can be used for fast computation of the gradients.
   *
   * @param table the table
   * @param scale the scale
   * @return the scaled table
   */
  public static float[] scalePowerTable(float[] table, int scale) {
    final float[] tableN = new float[64];
    for (int i = 0; i < 64; i++) {
      tableN[i] = scale * table[i];
    }
    return tableN;
  }

  /**
   * Get the interpolated value.
   *
   * @param x x-coordinate of the interpolation point.
   * @param y y-coordinate of the interpolation point.
   * @param z z-coordinate of the interpolation point.
   * @return the interpolated value. @ if {@code x}, {@code y} or {@code z} are not in the interval
   *         {@code [0, 1]}.
   */
  @Override
  public double value(double x, double y, double z) {
    if (x < 0 || x > 1) {
      throw new OutOfRangeException(x, 0, 1);
    }
    if (y < 0 || y > 1) {
      throw new OutOfRangeException(y, 0, 1);
    }
    if (z < 0 || z > 1) {
      throw new OutOfRangeException(z, 0, 1);
    }

    final double x2 = x * x;
    final double x3 = x2 * x;
    final double[] powerX = { /* 1, optimised out */ x, x2, x3};

    final double y2 = y * y;
    final double y3 = y2 * y;
    final double[] powerY = { /* 1, optimised out */ y, y2, y3};

    final double z2 = z * z;
    final double z3 = z2 * z;
    final double[] powerZ = { /* 1, optimised out */ z, z2, z3};

    return value0(powerX, powerY, powerZ);
  }

  /**
   * Get the interpolated value.
   *
   * @param x x-coordinate of the interpolation point.
   * @param y y-coordinate of the interpolation point.
   * @param z z-coordinate of the interpolation point.
   * @return the interpolated value.
   */
  public double value(CubicSplinePosition x, CubicSplinePosition y, CubicSplinePosition z) {
    return value0(x.power, y.power, z.power);
  }

  /**
   * Get the value using a pre-computed power table.
   *
   * @param table the power table
   * @return the interpolated value.
   */
  public abstract double value(double[] table);

  /**
   * Get the value using a pre-computed power table.
   *
   * @param table the power table
   * @return the interpolated value.
   */
  public abstract double value(float[] table);

  /**
   * Compute the value and partial first-order derivatives.
   *
   * <p>WARNING: The gradients will be unscaled.
   *
   * @param x x-coordinate of the interpolation point.
   * @param y y-coordinate of the interpolation point.
   * @param z z-coordinate of the interpolation point.
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @return the interpolated value. @ if {@code x}, {@code y} or {@code z} are not in the interval
   *         {@code [0, 1]}.
   */
  public double value(double x, double y, double z, double[] derivative1) {
    if (x < 0 || x > 1) {
      throw new OutOfRangeException(x, 0, 1);
    }
    if (y < 0 || y > 1) {
      throw new OutOfRangeException(y, 0, 1);
    }
    if (z < 0 || z > 1) {
      throw new OutOfRangeException(z, 0, 1);
    }

    final double x2 = x * x;
    final double x3 = x2 * x;
    final double[] powerX = { /* 1, optimised out */ x, x2, x3};

    final double y2 = y * y;
    final double y3 = y2 * y;
    final double[] powerY = { /* 1, optimised out */ y, y2, y3};

    final double z2 = z * z;
    final double z3 = z2 * z;
    final double[] powerZ = { /* 1, optimised out */ z, z2, z3};

    return value1(powerX, powerY, powerZ, derivative1);
  }

  /**
   * Compute the value and partial first-order derivatives.
   *
   * <p>The gradients are scaled
   *
   * @param x x-coordinate of the interpolation point.
   * @param y y-coordinate of the interpolation point.
   * @param z z-coordinate of the interpolation point.
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @return the interpolated value.
   */
  public double value(CubicSplinePosition x, CubicSplinePosition y, CubicSplinePosition z,
      double[] derivative1) {
    final double value = value1(x.power, y.power, z.power, derivative1);
    derivative1[0] = x.scaleGradient(derivative1[0]);
    derivative1[1] = y.scaleGradient(derivative1[1]);
    derivative1[2] = z.scaleGradient(derivative1[2]);
    return value;
  }

  /**
   * Compute the value and partial first-order derivatives using pre-computed power table.
   *
   * @param table the power table
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @return the interpolated value.
   */
  public abstract double value(double[] table, double[] derivative1);

  /**
   * Compute the value and partial first-order derivatives using pre-computed power table.
   *
   * @param table the power table
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @return the interpolated value.
   */
  public abstract double value(float[] table, double[] derivative1);

  /**
   * Compute the value and partial first-order derivatives using pre-computed power table.
   *
   * @param table the power table
   * @param table2 the power table multiplied by 2
   * @param table3 the power table multiplied by 3
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @return the interpolated value.
   */
  public abstract double value(double[] table, double[] table2, double[] table3,
      double[] derivative1);

  /**
   * Compute the value and partial first-order derivatives using pre-computed power table.
   *
   * @param table the power table
   * @param table2 the power table multiplied by 2
   * @param table3 the power table multiplied by 3
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @return the interpolated value.
   */
  public abstract double value(float[] table, float[] table2, float[] table3, double[] derivative1);

  /**
   * Compute the value and partial first-order and second-order derivatives.
   *
   * <p>WARNING: The gradients will be unscaled.
   *
   * @param x x-coordinate of the interpolation point.
   * @param y y-coordinate of the interpolation point.
   * @param z z-coordinate of the interpolation point.
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @param derivative2 the partial second order derivatives with respect to x,y,z
   * @return the interpolated value. @ if {@code x}, {@code y} or {@code z} are not in the interval
   *         {@code [0, 1]}.
   */
  public double value(double x, double y, double z, double[] derivative1, double[] derivative2) {
    if (x < 0 || x > 1) {
      throw new OutOfRangeException(x, 0, 1);
    }
    if (y < 0 || y > 1) {
      throw new OutOfRangeException(y, 0, 1);
    }
    if (z < 0 || z > 1) {
      throw new OutOfRangeException(z, 0, 1);
    }

    final double x2 = x * x;
    final double x3 = x2 * x;
    final double[] powerX = { /* 1, optimised out */ x, x2, x3};

    final double y2 = y * y;
    final double y3 = y2 * y;
    final double[] powerY = { /* 1, optimised out */ y, y2, y3};

    final double z2 = z * z;
    final double z3 = z2 * z;
    final double[] powerZ = { /* 1, optimised out */ z, z2, z3};

    return value2(powerX, powerY, powerZ, derivative1, derivative2);
  }

  /**
   * Compute the value and partial first-order and second-order derivatives.
   *
   * <p>The gradients are scaled.
   *
   * @param x x-coordinate of the interpolation point.
   * @param y y-coordinate of the interpolation point.
   * @param z z-coordinate of the interpolation point.
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @param derivative2 the partial second order derivatives with respect to x,y,z
   * @return the interpolated value.
   */
  public double value(CubicSplinePosition x, CubicSplinePosition y, CubicSplinePosition z,
      double[] derivative1, double[] derivative2) {
    final double value = value2(x.power, y.power, z.power, derivative1, derivative2);
    derivative1[0] = x.scaleGradient(derivative1[0]);
    derivative1[1] = y.scaleGradient(derivative1[1]);
    derivative1[2] = z.scaleGradient(derivative1[2]);
    derivative2[0] = x.scaleGradient2(derivative2[0]);
    derivative2[1] = y.scaleGradient2(derivative2[1]);
    derivative2[2] = z.scaleGradient2(derivative2[2]);
    return value;
  }

  /**
   * Compute the value and partial first-order and second-order derivatives using pre-computed power
   * table.
   *
   * @param table the power table
   * @param derivative1 the partial second order derivatives with respect to x,y,z
   * @param derivative2 the partial second order derivatives with respect to x,y,z
   * @return the interpolated value.
   */
  public abstract double value(double[] table, double[] derivative1, double[] derivative2);

  /**
   * Compute the value and partial first-order and second-order derivatives using pre-computed power
   * table.
   *
   * @param table the power table
   * @param derivative1 the partial second order derivatives with respect to x,y,z
   * @param derivative2 the partial second order derivatives with respect to x,y,z
   * @return the interpolated value.
   */
  public abstract double value(float[] table, double[] derivative1, double[] derivative2);

  /**
   * Compute the value and partial first-order and second-order derivatives using pre-computed power
   * table.
   *
   * @param table the power table
   * @param table2 the power table multiplied by 2
   * @param table3 the power table multiplied by 3
   * @param table6 the power table multiplied by 6
   * @param derivative1 the partial second order derivatives with respect to x,y,z
   * @param derivative2 the partial second order derivatives with respect to x,y,z
   * @return the interpolated value.
   */
  public abstract double value(double[] table, double[] table2, double[] table3, double[] table6,
      double[] derivative1, double[] derivative2);

  /**
   * Compute the value and partial first-order and second-order derivatives using pre-computed power
   * table.
   *
   * @param table the power table
   * @param table2 the power table multiplied by 2
   * @param table3 the power table multiplied by 3
   * @param table6 the power table multiplied by 6
   * @param derivative1 the partial second order derivatives with respect to x,y,z
   * @param derivative2 the partial second order derivatives with respect to x,y,z
   * @return the interpolated value.
   */
  public abstract double value(float[] table, float[] table2, float[] table3, float[] table6,
      double[] derivative1, double[] derivative2);

  /**
   * Compute the partial first-order derivatives using pre-computed power table. Provides
   * separability between computing the value and the derivative.
   *
   * @param table the power table
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   */
  public abstract void gradient(double[] table, double[] derivative1);

  /**
   * Compute the partial first-order derivatives using pre-computed power table. Provides
   * separability between computing the value and the derivative.
   *
   * @param table the power table
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   */
  public abstract void gradient(float[] table, double[] derivative1);

  /**
   * Creates the tricubic function.
   *
   * @param coefficients the coefficients
   * @return the custom tricubic function
   */
  public static CustomTricubicFunction create(double[] coefficients) {
    if (coefficients == null || coefficients.length != 64) {
      throw new IllegalArgumentException("Require 64 coefficients");
    }
    return new DoubleCustomTricubicFunction(coefficients);
  }

  /**
   * Creates the tricubic function. The function will store single precision coefficients.
   *
   * @param coefficients the coefficients
   * @return the custom tricubic function
   */
  public static CustomTricubicFunction create(float[] coefficients) {
    if (coefficients == null || coefficients.length != 64) {
      throw new IllegalArgumentException("Require 64 coefficients");
    }
    return new FloatCustomTricubicFunction(coefficients);
  }

  /**
   * Scale the coefficients by the given value.
   *
   * @param scale the scale
   */
  public abstract void scale(double scale);

  /**
   * Perform n refinements of a binary search to find the optimum value. 8 vertices of a cube are
   * evaluated per refinement and the optimum value selected. The bounds of the cube are then
   * reduced by 2.
   *
   * <p>The search starts with the bounds at 0,1 for each dimension. This search works because the
   * function is a cubic polynomial and so the peak at the optimum is closest-in-distance to the
   * closest-in-value bounding point.
   *
   * <p>The optimum will be found within error +/- 1/(2^refinements), e.g. 5 refinements will have
   * an error of +/- 1/32.
   *
   * <p>An optional tolerance for improvement can be specified. This is applied only if the optimum
   * vertex has changed, otherwise the value would be the same. If it has changed then the maximum
   * error will be greater than if the maximum refinements was achieved.
   *
   * @param maximum Set to true to find the maximum
   * @param refinements the refinements (this is set to 1 if below 1)
   * @param relativeError relative tolerance threshold (set to negative to ignore)
   * @param absoluteError absolute tolerance threshold (set to negative to ignore)
   * @return [x, y, z, value]
   */
  public double[] search(boolean maximum, int refinements, double relativeError,
      double absoluteError) {
    if (refinements < 1) {
      refinements = 1;
    }

    final boolean checkValue = relativeError > 0 || absoluteError > 0;

    final CubicSplinePosition[] sx =
        new CubicSplinePosition[] {new CubicSplinePosition(0), new CubicSplinePosition(1)};
    final CubicSplinePosition[] sy = sx.clone();
    final CubicSplinePosition[] sz = sx.clone();
    // 8 cube vertices packed as z*4 + y*2 + x
    final double[] values = new double[8];
    // We can initialise the default node value
    int lastI = 0;
    double lastValue = value000();
    for (;;) {
      // Evaluate the 8 flanking positions
      for (int z = 0, i = 0; z < 2; z++) {
        for (int y = 0; y < 2; y++) {
          for (int x = 0; x < 2; x++, i++) {
            // We can skip the value we know
            values[i] = (i == lastI) ? lastValue : value(sx[x], sy[y], sz[z]);
          }
        }
      }

      final int i =
          (maximum) ? SimpleArrayUtils.findMaxIndex(values) : SimpleArrayUtils.findMinIndex(values);
      final int z = i / 4;
      final int j = i % 4;
      final int y = j / 2;
      final int x = j % 2;

      final double value = values[i];

      boolean converged = (--refinements == 0);
      if (!converged && checkValue && lastI != i) {
        // Check convergence on value if the cube vertex has changed.
        // If it hasn't changed then the value will be the same and we continue
        // reducing the cube size.
        converged = areEqual(lastValue, value, absoluteError, relativeError);
      }

      if (converged) {
        // Terminate
        return new double[] {sx[x].getX(), sy[y].getX(), sz[z].getX(), value};
      }

      lastI = i;
      lastValue = value;

      // Update bounds
      update(sx, x);
      update(sy, y);
      update(sz, z);
    }
  }

  /**
   * Check if the pair of values are equal.
   *
   * @param previous the previous
   * @param current the current
   * @param relativeError relative tolerance threshold (set to negative to ignore)
   * @param absoluteError absolute tolerance threshold (set to negative to ignore)
   * @return True if equal
   */
  public static boolean areEqual(final double previous, final double current, double relativeError,
      double absoluteError) {
    final double difference = Math.abs(previous - current);
    if (difference <= absoluteError) {
      return true;
    }
    final double size = max(Math.abs(previous), Math.abs(current));
    return (difference <= size * relativeError);
  }

  private static double max(final double value1, final double value2) {
    // Ignore NaN
    return (value1 > value2) ? value1 : value2;
  }

  /**
   * Update the bounds by fixing the last spline position that was optimum and moving the other
   * position to the midpoint.
   *
   * @param splicePosition the pair of spline positions defining the bounds
   * @param indcex the index of the optimum
   */
  private static void update(CubicSplinePosition[] splinePosition, int index) {
    final double mid = (splinePosition[0].getX() + splinePosition[1].getX()) / 2;
    // Move opposite bound
    splinePosition[(index + 1) % 2] = new CubicSplinePosition(mid);
  }
}
