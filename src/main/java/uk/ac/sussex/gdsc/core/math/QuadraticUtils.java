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
package uk.ac.sussex.gdsc.core.math;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;


import uk.ac.sussex.gdsc.core.data.DataException;

/**
 * Class for working with quadratics
 */
public class QuadraticUtils {
  /**
   * Find the absolute max of the data.
   *
   * @param data the data
   * @return the absolute max
   */
  public static double absoluteMax(double[] data) {
    return absoluteMax(data, data.length);
  }

  /**
   * Find the absolute max of the data.
   *
   * @param data the data
   * @param n the number of points in the data
   * @return the absolute max
   */
  public static double absoluteMax(double[] data, int n) {
    double max = Math.abs(data[0]);
    for (int i = 1; i < n; i++) {
      max = max(max, Math.abs(data[i]));
    }
    return max;
  }

  /**
   * Find the max.
   *
   * @param a the a
   * @param b the b
   * @return the max
   */
  private static double max(double a, double b) {
    return (a > b) ? a : b;
  }

  /**
   * Gets the determinant of a 3x3 matrix.
   *
   * @param data the data (3x3 matrix)
   * @return the determinant
   */
  public static double getDeterminant3x3(double[] data) {
    final double a11 = data[0];
    final double a12 = data[1];
    final double a13 = data[2];
    final double a21 = data[3];
    final double a22 = data[4];
    final double a23 = data[5];
    final double a31 = data[6];
    final double a32 = data[7];
    final double a33 = data[8];

    final double m11 = a22 * a33 - a23 * a32;
    final double m12 = -(a21 * a33 - a23 * a31);
    final double m13 = a21 * a32 - a22 * a31;

    return (a11 * m11 + a12 * m12 + a13 * m13);
  }

  /**
   * Gets the determinant of a 3x3 matrix.
   *
   * @param data the data (3x3 matrix)
   * @param scale the scale (this can be set using 1 / [absolute maximum of the data])
   * @return the determinant
   */
  public static double getDeterminant3x3(double[] data, double scale) {
    // scale = 1;

    final double a11 = data[0] * scale;
    final double a12 = data[1] * scale;
    final double a13 = data[2] * scale;
    final double a21 = data[3] * scale;
    final double a22 = data[4] * scale;
    final double a23 = data[5] * scale;
    final double a31 = data[6] * scale;
    final double a32 = data[7] * scale;
    final double a33 = data[8] * scale;

    final double m11 = a22 * a33 - a23 * a32;
    final double m12 = -(a21 * a33 - a23 * a31);
    final double m13 = a21 * a32 - a22 * a31;

    return (a11 * m11 + a12 * m12 + a13 * m13) / (scale * scale * scale);
  }

  /**
   * Solve the quadratic ax^2 + bx + c that passes through the points 1,2,3. <p> Solved using
   * Cramer's rule: <a href=
   * "https://en.wikipedia.org/wiki/Cramer%27s_rule#Explicit_formulas_for_small_systems">Explicit_formulas_for_small_systems</a>.
   *
   * @param x1 the x-coordinate of point 1
   * @param y1 the y-coordinate of point 1
   * @param x2 the x-coordinate of point 2
   * @param y2 the y-coordinate of point 2
   * @param x3 the x-coordinate of point 3
   * @param y3 the y-coordinate of point 3
   * @return [a,b,c] or null if no solution exists (e.g. points are colocated/colinear)
   */
  public static double[] solve(double x1, double y1, double x2, double y2, double x3, double y3) {
    // Formulate linear problem:
    // a1*x + b1*y + c1*z = d1
    // a2*x + b2*y + c2*z = d2
    // a3*x + b3*y + c3*z = d3
    double a1 = x1 * x1;
    double a2 = x2 * x2;
    double a3 = x3 * x3;
    double b1 = x1;
    double b2 = x2;
    double b3 = x3;
    // double c1 = 1;
    // double c2 = 1;
    // double c3 = 1;
    double d1 = y1;
    double d2 = y2;
    double d3 = y3;

    // Scale data for stability
    final double c_ = 1. / absoluteMax(new double[] {a1, a2, a3, b1, b2, b3, d1, d2, d3});
    a1 *= c_;
    a2 *= c_;
    a3 *= c_;
    b1 *= c_;
    b2 *= c_;
    b3 *= c_;
    // c1 = scale;
    // c2 = scale;
    // c3 = scale;
    d1 *= c_;
    d2 *= c_;
    d3 *= c_;

    final double D_ = getDeterminant3x3(new double[] {a1, b1, c_, a2, b2, c_, a3, b3, c_});
    if (D_ == 0) {
      return null;
    }
    final double Dx = getDeterminant3x3(new double[] {d1, b1, c_, d2, b2, c_, d3, b3, c_});
    final double Dy = getDeterminant3x3(new double[] {a1, d1, c_, a2, d2, c_, a3, d3, c_});
    final double Dz = getDeterminant3x3(new double[] {a1, b1, d1, a2, b2, d2, a3, b3, d3});

    return new double[] {
        // x (quadratic constant a)
        Dx / D_,
        // y (quadratic constant b)
        Dy / D_,
        // z (quadratic constant c)
        Dz / D_};
  }

  /**
   * Find the max/min of the quadratic ax^2 + bx + c that passes through the points 1,2,3. <p> This
   * is the solution to 0 = 2ax + b, i.e. the root of the quadratic gradient.
   *
   * @param x1 the x-coordinate of point 1
   * @param y1 the y-coordinate of point 1
   * @param x2 the x-coordinate of point 2
   * @param y2 the y-coordinate of point 2
   * @param x3 the x-coordinate of point 3
   * @param y3 the y-coordinate of point 3
   * @return [a,b,c] or null if no solution exists
   * @throws DataException If there is no quadratic solution (e.g. points are colocated/colinear)
   */
  public static double findMinMax(double x1, double y1, double x2, double y2, double x3, double y3)
      throws DataException {
    // Formulate linear problem:
    // a1*x + b1*y + c1*z = d1
    // a2*x + b2*y + c2*z = d2
    // a3*x + b3*y + c3*z = d3
    double a1 = x1 * x1;
    double a2 = x2 * x2;
    double a3 = x3 * x3;
    double b1 = x1;
    double b2 = x2;
    double b3 = x3;
    // double c1 = 1;
    // double c2 = 1;
    // double c3 = 1;
    double d1 = y1;
    double d2 = y2;
    double d3 = y3;

    // Scale data for stability
    final double c_ = 1. / absoluteMax(new double[] {a1, a2, a3, b1, b2, b3, d1, d2, d3});
    a1 *= c_;
    a2 *= c_;
    a3 *= c_;
    b1 *= c_;
    b2 *= c_;
    b3 *= c_;
    // c1 = scale;
    // c2 = scale;
    // c3 = scale;
    d1 *= c_;
    d2 *= c_;
    d3 *= c_;

    final double D_ = getDeterminant3x3(new double[] {a1, b1, c_, a2, b2, c_, a3, b3, c_});
    if (D_ == 0) {
      throw new DataException("No quadratic solution");
    }
    final double Dx = getDeterminant3x3(new double[] {d1, b1, c_, d2, b2, c_, d3, b3, c_});
    if (Dx == 0) {
      throw new DataException("No min/max solution, points are colinear");
    }

    // x (quadratic constant a)
    final double a = Dx / D_;

    final double Dy = getDeterminant3x3(new double[] {a1, d1, c_, a2, d2, c_, a3, d3, c_});
    final double b = Dy / D_;

    // 0 = 2ax + b
    // x = -b/2a
    return -b / (2 * a);
  }
}
