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


/**
 * Contains methods for standard geometry computations
 *
 * @author Alex Herbert
 */
public class Geometry {
  /**
   * Gets the area of the triangle from its vertices.
   *
   * @param xA the first vertex x
   * @param yA the first vertex y
   * @param xB the second vertex x
   * @param yB the second vertex y
   * @param xC the third vertex x
   * @param yC the third vertex y
   * @return the area
   */
  public static double getArea(double xA, double yA, double xB, double yB, double xC, double yC) {
    return Math.abs((xA - xC) * (yB - yA) - (xA - xB) * (yC - yA)) / 2;
  }

  /**
   * Gets the area of the triangle from its vertices assuming the third vertex is 0,0.
   *
   * @param xA the first vertex x
   * @param yA the first vertex y
   * @param xB the second vertex x
   * @param yB the second vertex y
   * @return the area
   */
  public static double getArea(double xA, double yA, double xB, double yB) {
    return Math.abs(xA * yB - yA * xB) / 2;
  }

  /**
   * Gets the area of a polygon using the Shoelace formula
   * (https://en.wikipedia.org/wiki/Shoelace_formula) <p> The area formula is valid for any
   * non-self-intersecting (simple) polygon, which can be convex or concave. <p> Note: The float
   * values are cast up to double precision for the computation.
   *
   * @param x the x
   * @param y the y
   * @return the area
   * @throws IllegalArgumentException If the arrays are not the same length
   */
  public static double getArea(float[] x, float[] y) throws IllegalArgumentException {
    if (x.length < 3) {
      return 0;
    }
    if (x.length != y.length) {
      throw new IllegalArgumentException("Input arrays must be the same length");
    }
    double sum1 = 0, sum2 = 0;
    for (int i = x.length, j = 0; i-- > 0; j = i) {
      sum1 += (double) x[i] * (double) y[j];
      sum2 += (double) x[j] * (double) y[i];
    }
    return (sum1 - sum2) / 2;
  }

  /**
   * Gets the area of a polygon using the Shoelace formula
   * (https://en.wikipedia.org/wiki/Shoelace_formula) <p> The area formula is valid for any
   * non-self-intersecting (simple) polygon, which can be convex or concave.
   *
   * @param x the x
   * @param y the y
   * @return the area
   * @throws IllegalArgumentException If the arrays are not the same length
   */
  public static double getArea(double[] x, double[] y) throws IllegalArgumentException {
    if (x.length < 3) {
      return 0;
    }
    if (x.length != y.length) {
      throw new IllegalArgumentException("Input arrays must be the same length");
    }
    double sum1 = 0, sum2 = 0;
    for (int i = x.length, j = 0; i-- > 0; j = i) {
      sum1 += x[i] * y[j];
      sum2 += x[j] * y[i];
    }
    return (sum1 - sum2) / 2;
  }

  /**
   * Gets the intersection between the line x1,y1 to x2,y2 and x3,y3 to x4,y4. <p>
   * http://en.wikipedia.org/wiki/Line-line_intersection
   *
   * @param x1 the x 1
   * @param y1 the y 1
   * @param x2 the x 2
   * @param y2 the y 2
   * @param x3 the x 3
   * @param y3 the y 3
   * @param x4 the x 4
   * @param y4 the y 4
   * @param intersection the intersection
   * @return true if an intersection was found
   */
  public static boolean getIntersection(double x1, double y1, double x2, double y2, double x3,
      double y3, double x4, double y4, double[] intersection) {
    // http://en.wikipedia.org/wiki/Line-line_intersection
    //
    // x1,y1 x4,y4
    // ** ++
    // ** ++
    // **++ P(x,y)
    // ++ **
    // ++ **
    // ++ **
    // x3,y3 **
    // x2,y2

    final double x1_m_x2 = x1 - x2;
    final double x3_m_x4 = x3 - x4;
    final double y1_m_y2 = y1 - y2;
    final double y3_m_y4 = y3 - y4;

    // Check if lines are parallel
    final double d = x1_m_x2 * y3_m_y4 - y1_m_y2 * x3_m_x4;
    if (d == 0) {
      if (y1 == y3) {
        // The lines are the same
        intersection[0] = x1;
        intersection[1] = y1;
        return true;
      }
    } else {
      // Find intersection
      final double x1_by_y2_m_y1_by_x2 = x1 * y2 - y1 * x2;
      final double x3_by_y4_m_y3_by_x4 = x3 * y4 - y3 * x4;
      final double px = (x1_by_y2_m_y1_by_x2 * x3_m_x4 - x1_m_x2 * x3_by_y4_m_y3_by_x4) / d;
      final double py = (x1_by_y2_m_y1_by_x2 * y3_m_y4 - y1_m_y2 * x3_by_y4_m_y3_by_x4) / d;
      intersection[0] = px;
      intersection[1] = py;
      return true;
    }

    return false;
  }
}
