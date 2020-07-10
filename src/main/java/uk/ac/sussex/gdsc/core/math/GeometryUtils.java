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
 * Copyright (C) 2011 - 2020 Alex Herbert
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

/**
 * Contains methods for standard geometry computations.
 */
public final class GeometryUtils {

  /** The minimum vertices for an area. */
  private static final int MIN_VERTICES = 3;

  /** No public construction. */
  private GeometryUtils() {}

  /**
   * Gets the area of the triangle from its vertices.
   *
   * @param x1 the first vertex x
   * @param y1 the first vertex y
   * @param x2 the second vertex x
   * @param y2 the second vertex y
   * @param x3 the third vertex x
   * @param y3 the third vertex y
   * @return the area
   */
  public static double getArea(double x1, double y1, double x2, double y2, double x3, double y3) {
    return Math.abs((x1 - x3) * (y2 - y1) - (x1 - x2) * (y3 - y1)) / 2;
  }

  /**
   * Gets the area of the triangle from its vertices assuming the third vertex is 0,0.
   *
   * @param x1 the first vertex x
   * @param y1 the first vertex y
   * @param x2 the second vertex x
   * @param y2 the second vertex y
   * @return the area
   */
  public static double getArea(double x1, double y1, double x2, double y2) {
    return Math.abs(x1 * y2 - y1 * x2) / 2;
  }

  /**
   * Gets the area of a polygon using the Shoelace formula
   * (https://en.wikipedia.org/wiki/Shoelace_formula).
   *
   * <p>The area formula is valid for any non-self-intersecting (simple) polygon, which can be
   * convex or concave.
   *
   * <p>Note: The float values are cast up to double precision for the computation.
   *
   * @param x the x
   * @param y the y
   * @return the area
   * @throws IllegalArgumentException If the arrays are not the same length
   */
  public static double getArea(float[] x, float[] y) {
    if (x.length < MIN_VERTICES) {
      return 0;
    }
    if (x.length != y.length) {
      throw new IllegalArgumentException("Input arrays must be the same length");
    }
    double sum1 = 0;
    double sum2 = 0;
    for (int i = x.length, j = 0; i-- > 0; j = i) {
      sum1 += (double) x[i] * (double) y[j];
      sum2 += (double) x[j] * (double) y[i];
    }
    return (sum1 - sum2) / 2;
  }

  /**
   * Gets the area of a polygon using the Shoelace formula
   * (https://en.wikipedia.org/wiki/Shoelace_formula).
   *
   * <p>The area formula is valid for any non-self-intersecting (simple) polygon, which can be
   * convex or concave.
   *
   * @param x the x
   * @param y the y
   * @return the area
   * @throws IllegalArgumentException If the arrays are not the same length
   */
  public static double getArea(double[] x, double[] y) {
    if (x.length < MIN_VERTICES) {
      return 0;
    }
    if (x.length != y.length) {
      throw new IllegalArgumentException("Input arrays must be the same length");
    }
    double sum1 = 0;
    double sum2 = 0;
    for (int i = x.length, j = 0; i-- > 0; j = i) {
      sum1 += x[i] * y[j];
      sum2 += x[j] * y[i];
    }
    return (sum1 - sum2) / 2;
  }

  /**
   * Gets the intersection between the line segments x1,y1 to x2,y2 and x3,y3 to x4,y4.
   *
   * <p>If the line segments have zero length the results are undefined.
   *
   * <p>If the line segments are coincident (overlap and are parallel) this will return the end
   * point of one of the line segments that is inside the other line segment.
   *
   * @param x1 the first vertex x
   * @param y1 the first vertex y
   * @param x2 the second vertex x
   * @param y2 the second vertex y
   * @param x3 the third vertex x
   * @param y3 the third vertex y
   * @param x4 the forth vertex x
   * @param y4 the forth vertex y
   * @param intersection the intersection
   * @return true if an intersection was found
   * @see <a href="http://en.wikipedia.org/wiki/Line-line_intersection">Intersection</a>
   */
  public static boolean getIntersection(double x1, double y1, double x2, double y2, double x3,
      double y3, double x4, double y4, double[] intersection) {
    // Solved using the equations of Paul Bourke, 1989
    // http://paulbourke.net/geometry/pointlineplane/
    //@formatter:off
    //
    //       P3            P2
    //         **        ++
    //           **    ++
    //             **++ P(x,y)
    //            ++ **
    //          ++     **
    //        ++         **
    //      P1            **
    //                       P4
    //@formatter:on
    //
    // Pa = P1 + ua (P2 - P1)
    // Pb = P3 + ub (P4 - P3)
    //
    // Express for Pa = Pb:
    //
    // x1 + ua (x2-x1) = x3 + ub (x4-x3)
    // y1 + ua (y2-y1) = y3 + ub (y4-y3)
    //
    // Solve for ua and ub

    final double denom = ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));
    final double numA = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3));
    final double numB = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3));

    if (denom == 0) {
      // If the denominator for the equations for ua and ub is 0 then the two lines are parallel.
      if (numA == 0 && numB == 0) {
        // If the denominator and numerator for the equations for ua and ub are 0 then the
        // two lines are coincident.
        // Find a point of coincidence.
        // A point is on a line if the distance from both ends is equal to the line length.
        // d13 + d23 >= length
        // d14 + d24 >= length
        // Pick the smallest length
        final double d13 = Math.hypot(x3 - x1, y3 - y1);
        final double d23 = Math.hypot(x3 - x2, y3 - y2);
        final double d14 = Math.hypot(x4 - x1, y4 - y1);
        final double d24 = Math.hypot(x4 - x2, y4 - y2);
        if (d13 + d23 < d14 + d24) {
          intersection[0] = x3;
          intersection[1] = y3;
        } else {
          intersection[0] = x4;
          intersection[1] = y4;
        }
        return true;
      }
      return false;
    }

    final double uA = numA / denom;
    final double uB = numB / denom;

    // if the intersection of line segments is required then it is only necessary to test if
    // ua and ub lie between 0 and 1. If both lie within the range of 0 to 1 then the
    // intersection point is within both line segments.
    if (uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1) {
      intersection[0] = x1 + (uA * (x2 - x1));
      intersection[1] = y1 + (uA * (y2 - y1));
      return true;
    }

    return false;
  }

  /**
   * Returns true if the line segments x1,y1 to x2,y2 and x3,y3 to x4,y4 intersect.
   *
   * <p>If the line segments have zero length the results are undefined.
   *
   * <p>If the line segments are coincident this will return true.
   *
   * @param x1 the first vertex x
   * @param y1 the first vertex y
   * @param x2 the second vertex x
   * @param y2 the second vertex y
   * @param x3 the third vertex x
   * @param y3 the third vertex y
   * @param x4 the forth vertex x
   * @param y4 the forth vertex y
   * @return true if an intersection was found
   * @see <a href="http://en.wikipedia.org/wiki/Line-line_intersection">Intersection</a>
   */
  public static boolean testIntersect(double x1, double y1, double x2, double y2, double x3,
      double y3, double x4, double y4) {
    // Logic as per getIntersection(...)
    final double denom = ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));
    final double numA = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3));
    final double numB = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3));

    if (denom == 0) {
      // Parallel. Check if coincident.
      return (numA == 0 && numB == 0);
    }

    // Check if intersection is within the line segments.
    final double uA = numA / denom;
    final double uB = numB / denom;
    return (uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1);
  }
}
