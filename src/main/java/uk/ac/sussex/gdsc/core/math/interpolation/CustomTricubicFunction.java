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

import java.io.Serializable;

/**
 * 3D-spline function.
 */
public abstract class CustomTricubicFunction implements TrivariateFunction, Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * Gets the 64 coefficients to a provided array.
   *
   * @param coefficients the coefficients (must be allocated to {@code length >= 64})
   */
  public abstract void getCoefficients(double[] coefficients);

  /**
   * Gets the 64 coefficients to a provided array.
   *
   * @param coefficients the coefficients (must be allocated to {@code length >= 64})
   */
  public abstract void getCoefficients(float[] coefficients);

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
   * @return the copy
   */
  public abstract CustomTricubicFunction copy();

  /**
   * Scale the coefficients by the given value and return a new function.
   *
   * @param scale the scale
   * @return the scaled function
   */
  public abstract CustomTricubicFunction scale(double scale);

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
   * @param x x-coordinate powers of the interpolation point.
   * @param y y-coordinate powers of the interpolation point.
   * @param z z-coordinate powers of the interpolation point.
   * @return the interpolated value.
   */
  protected abstract double value0(final CubicSplinePosition x, CubicSplinePosition y,
      CubicSplinePosition z);

  /**
   * Compute the value and partial first-order derivatives.
   *
   * @param x x-coordinate powers of the interpolation point.
   * @param y y-coordinate powers of the interpolation point.
   * @param z z-coordinate powers of the interpolation point.
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @return the interpolated value.
   */
  protected abstract double value1(final CubicSplinePosition x, CubicSplinePosition y,
      CubicSplinePosition z, final double[] derivative1);

  /**
   * Compute the value and partial first-order and second-order derivatives.
   *
   * @param x x-coordinate powers of the interpolation point.
   * @param y y-coordinate powers of the interpolation point.
   * @param z z-coordinate powers of the interpolation point.
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @param derivative2 the partial second order derivatives with respect to x,y,z
   * @return the interpolated value.
   */
  protected abstract double value2(final CubicSplinePosition x, CubicSplinePosition y,
      CubicSplinePosition z, final double[] derivative1, double[] derivative2);

  /**
   * Checks if the power table is at the boundary of the interpolation range for the given
   * dimension, i.e. 0 or 1
   *
   * @param dimension the dimension [x=0,y=1,z=2]
   * @param table the table
   * @return true, if at the boundary
   */
  public static boolean isBoundary(int dimension, DoubleCubicSplineData table) {
    switch (dimension) {
      case 0:
        return isBoundary(table.x1y0z0);
      case 1:
        return isBoundary(table.x0y1z0);
      case 2:
        return isBoundary(table.x0y0z1);
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
  public static boolean isBoundary(int dimension, FloatCubicSplineData table) {
    switch (dimension) {
      case 0:
        return isBoundary(table.x1y0z0);
      case 1:
        return isBoundary(table.x0y1z0);
      case 2:
        return isBoundary(table.x0y0z1);
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
   * Checks if the value is 0 or 1.
   *
   * @param value the value
   * @return true, if 0 or 1
   */
  private static boolean isBoundary(float value) {
    return value == 0 || value == 1;
  }

  /**
   * Get the interpolated value.
   *
   * @param x x-coordinate of the interpolation point.
   * @param y y-coordinate of the interpolation point.
   * @param z z-coordinate of the interpolation point.
   * @return the interpolated value.
   * @throws OutOfRangeException if {@code x}, {@code y} or {@code z} are not in the interval
   *         {@code [0, 1]}.
   */
  @Override
  public double value(double x, double y, double z) {
    return value0(new CubicSplinePosition(x), new CubicSplinePosition(y),
        new CubicSplinePosition(z));
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
    return value0(x, y, z);
  }

  /**
   * Compute the value and partial first-order derivatives.
   *
   * <p>WARNING: The gradients will be unscaled.
   *
   * @param x x-coordinate of the interpolation point.
   * @param y y-coordinate of the interpolation point.
   * @param z z-coordinate of the interpolation point.
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @return the interpolated value.
   * @throws OutOfRangeException if {@code x}, {@code y} or {@code z} are not in the interval
   *         {@code [0, 1]}.
   */
  public double value(double x, double y, double z, double[] derivative1) {
    return value1(new CubicSplinePosition(x), new CubicSplinePosition(y),
        new CubicSplinePosition(z), derivative1);
  }

  /**
   * Compute the value and partial first-order derivatives.
   *
   * <p>The gradients are scaled.
   *
   * @param x x-coordinate of the interpolation point.
   * @param y y-coordinate of the interpolation point.
   * @param z z-coordinate of the interpolation point.
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @return the interpolated value.
   */
  public double value(CubicSplinePosition x, CubicSplinePosition y, CubicSplinePosition z,
      double[] derivative1) {
    final double value = value1(x, y, z, derivative1);
    derivative1[0] = x.scaleGradient(derivative1[0]);
    derivative1[1] = y.scaleGradient(derivative1[1]);
    derivative1[2] = z.scaleGradient(derivative1[2]);
    return value;
  }

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
   * @return the interpolated value.
   * @throws OutOfRangeException if {@code x}, {@code y} or {@code z} are not in the interval
   *         {@code [0, 1]}.
   */
  public double value(double x, double y, double z, double[] derivative1, double[] derivative2) {

    return value2(new CubicSplinePosition(x), new CubicSplinePosition(y),
        new CubicSplinePosition(z), derivative1, derivative2);
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
    final double value = value2(x, y, z, derivative1, derivative2);
    derivative1[0] = x.scaleGradient(derivative1[0]);
    derivative1[1] = y.scaleGradient(derivative1[1]);
    derivative1[2] = z.scaleGradient(derivative1[2]);
    derivative2[0] = x.scaleGradient2(derivative2[0]);
    derivative2[1] = y.scaleGradient2(derivative2[1]);
    derivative2[2] = z.scaleGradient2(derivative2[2]);
    return value;
  }

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
    int refinementIteration = Math.max(1, refinements);

    final boolean checkValue = relativeError > 0 || absoluteError > 0;

    final CubicSplinePosition[] sx =
        new CubicSplinePosition[] {new CubicSplinePosition(0), new CubicSplinePosition(1)};
    final CubicSplinePosition[] sy = sx.clone();
    final CubicSplinePosition[] sz = sx.clone();
    // 8 cube vertices packed as z*4 + y*2 + x
    final double[] values = new double[8];
    // We can initialise the default node value
    int currentIndex = 0;
    double currentValue = value000();
    for (;;) {
      // Evaluate the 7 flanking positions of the current value
      updateSplineValues(sx, sy, sz, values, currentIndex, currentValue);

      final int newIndex =
          (maximum) ? SimpleArrayUtils.findMaxIndex(values) : SimpleArrayUtils.findMinIndex(values);
      final int z = newIndex / 4;
      final int j = newIndex % 4;
      final int y = j / 2;
      final int x = j % 2;

      final double newValue = values[newIndex];

      boolean converged = (--refinementIteration == 0);
      if (!converged && checkValue && currentIndex != newIndex) {
        // Check convergence on value if the cube vertex has changed.
        // If it hasn't changed then the value will be the same and we continue
        // reducing the cube size.
        converged = areEqual(currentValue, newValue, absoluteError, relativeError);
      }

      if (converged) {
        // Terminate
        return new double[] {sx[x].getX(), sy[y].getX(), sz[z].getX(), newValue};
      }

      currentIndex = newIndex;
      currentValue = newValue;

      // Update bounds
      updateSplineBounds(sx, x);
      updateSplineBounds(sy, y);
      updateSplineBounds(sz, z);
    }
  }

  /**
   * Update the 7 spline values surrounding the current index in the 2x2x2 cube.
   *
   * @param sx the pair of spline positions defining the x bounds
   * @param sy the pair of spline positions defining the y bounds
   * @param sz the pair of spline positions defining the z bounds
   * @param values the values
   * @param currentIndex the current index
   * @param currentValue the current value
   */
  private void updateSplineValues(final CubicSplinePosition[] sx, final CubicSplinePosition[] sy,
      final CubicSplinePosition[] sz, final double[] values, int currentIndex,
      double currentValue) {
    int index = 0;
    for (int z = 0; z < 2; z++) {
      for (int y = 0; y < 2; y++) {
        for (int x = 0; x < 2; x++) {
          // We can skip the value we know
          values[index] = (index == currentIndex) ? currentValue : value(sx[x], sy[y], sz[z]);
          index++;
        }
      }
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
   * @param splinePosition the pair of spline positions defining the bounds
   * @param index the index of the optimum
   */
  private static void updateSplineBounds(CubicSplinePosition[] splinePosition, int index) {
    final double mid = (splinePosition[0].getX() + splinePosition[1].getX()) / 2;
    // Move opposite bound
    splinePosition[(index + 1) % 2] = new CubicSplinePosition(mid);
  }
}
