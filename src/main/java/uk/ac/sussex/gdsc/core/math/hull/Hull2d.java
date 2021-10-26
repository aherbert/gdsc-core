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
 * Copyright (C) 2011 - 2021 Alex Herbert
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

package uk.ac.sussex.gdsc.core.math.hull;

import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

/**
 * Contains a set of paired coordinates representing the hull of a set of points. This should be a
 * non-self-intersecting (simple) polygon, which can be convex or concave.
 *
 * @since 2.0
 */
public final class Hull2d implements Hull {

  /** The x coordinates. */
  private final double[] x;

  /** The y coordinates. */
  private final double[] y;

  /** The length. */
  private double length;
  /** The centroid. */
  private double[] centroid;
  /** The area. */
  private double area;

  /**
   * Instantiates a new hull.
   *
   * @param x the x coordinates
   * @param y the y coordinates
   */
  Hull2d(double[] x, double[] y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Create a new hull from the given coordinates.
   *
   * @param x the x coordinates
   * @param y the y coordinates
   * @return the convex hull
   * @throws NullPointerException if the inputs are null
   * @throws IllegalArgumentException if the array lengths are different or zero
   */
  public static Hull2d create(double[] x, double[] y) {
    ValidationUtils.checkArgument(x.length == y.length, "Lengths do not match: %d != %d", x.length,
        y.length);
    ValidationUtils.checkStrictlyPositive(x.length, "coordinates length");
    return new Hull2d(x, y);
  }

  /**
   * Create a new hull from the given coordinates. Only the first two values from each coordinate
   * are used.
   *
   * @param coords the coordinates
   * @return the convex hull
   * @throws NullPointerException if the inputs are null
   * @throws IllegalArgumentException if the array length is zero
   * @throws IndexOutOfBoundsException if the coordinates lengths are not at least 2
   */
  public static Hull2d create(double[][] coords) {
    ValidationUtils.checkStrictlyPositive(coords.length, "coordinates length");
    final double[] x = new double[coords.length];
    final double[] y = new double[coords.length];
    for (int i = 0; i < x.length; i++) {
      x[i] = coords[i][0];
      y[i] = coords[i][1];
    }
    return new Hull2d(x, y);
  }

  @Override
  public int dimensions() {
    return 2;
  }

  @Override
  public int getNumberOfVertices() {
    return x.length;
  }

  /**
   * {@inheritDoc}
   *
   * <p>The returned result is a copy of the vertices.
   */
  @Override
  public double[][] getVertices() {
    final double[][] v = new double[getNumberOfVertices()][];
    for (int i = 0; i < v.length; i++) {
      v[i] = new double[] {x[i], y[i]};
    }
    return v;
  }

  // Additional 2D methods

  /**
   * Returns true if the point is within the hull.
   *
   * <p>Note: Hull points may not be inside the hull. The definition of inside requires that:
   *
   * <ul>
   *
   * <li>it lies completely inside the boundary or;
   *
   * <li>it lies exactly on the boundary and the space immediately adjacent to the point in the
   * increasing X direction is entirely inside the boundary.
   *
   * <li>it lies exactly on a horizontal boundary segment and the space immediately adjacent to the
   * point in the increasing Y direction is inside the boundary.
   *
   * <li>it lies exactly on a horizontal boundary segment and the space immediately adjacent to the
   * point in the increasing Y direction is inside the boundary.
   *
   * </ul>
   *
   * @param point the point
   * @return true if the hull contains the point
   */
  public boolean contains(double[] point) {
    // This is a Java version of the winding number algorithm wn_PnPoly:
    // http://geomalgorithms.com/a03-_inclusion.html
    final double xp = point[0];
    final double yp = point[1];
    final double[] xpoints = this.x;
    final double[] ypoints = this.y;
    int wn = 0;
    // All edges of polygon, each edge is from i to j
    for (int j = xpoints.length, i = 0; j-- > 0; i = j) {
      if (ypoints[i] <= yp) {
        // start y <= yp
        if (ypoints[j] > yp) {
          // an upward crossing
          if (isLeft(xpoints[i], ypoints[i], xpoints[j], ypoints[j], xp, yp) > 0) {
            // P left of edge
            // have a valid up intersect
            ++wn;
          }
        }
      } else {
        // start y > yp (no test needed)
        if (ypoints[j] <= yp) {
          // a downward crossing
          if (isLeft(xpoints[i], ypoints[i], xpoints[j], ypoints[j], xp, yp) < 0) {
            // P right of edge
            // have a valid down intersect
            --wn;
          }
        }
      }
    }
    return wn != 0;
  }

  /**
   * Tests if a point is Left|On|Right of an infinite line.
   *
   * @param x1 the line start x
   * @param y1 the line start y
   * @param x2 the line end x
   * @param y2 the line end y
   * @param x the point x
   * @param y the point y
   * @return >0 for point left of the line through the start to end, =0 for on the line, otherwise
   *         <0
   */
  private static double isLeft(double x1, double y1, double x2, double y2, double x, double y) {
    return ((x2 - x1) * (y - y1) - (x - x1) * (y2 - y1));
  }

  /**
   * Gets the centroid.
   *
   * @return the centroid
   */
  public double[] getCentroid() {
    double[] c = centroid;
    if (centroid == null) {
      computeProperties();
      c = centroid;
    }
    return c.clone();
  }

  /**
   * Gets the length.
   *
   * @return the length
   */
  public double getLength() {
    double l = length;
    if (l == 0) {
      computeProperties();
      l = length;
    }
    return l;
  }

  /**
   * Gets the area. The returned area is unsigned.
   *
   * @return the area
   */
  public double getArea() {
    double a = area;
    if (a == 0) {
      computeProperties();
      a = area;
    }
    return a;
  }

  /**
   * Compute the hull properties (centroid, length, area).
   */
  private synchronized void computeProperties() {
    if (centroid != null) {
      return;
    }
    // Edge cases
    if (x.length == 1) {
      length = area = 0;
      centroid = new double[] {x[0], y[0]};
      return;
    }
    if (x.length == 2) {
      // The hull wraps around the entire line so double the length
      length = 2 * distance(x[0], y[0], x[1], y[1]);
      area = 0;
      centroid = new double[] {(x[0] + x[1]) / 2, (y[0] + y[1]) / 2};
      return;
    }

    // http://paulbourke.net/geometry/polygonmesh
    // Compute area as per the method in GeometryUtils.
    double l = 0;
    double cx = 0;
    double cy = 0;
    double sum1 = 0;
    double sum2 = 0;
    for (int i = x.length, i1 = 0; i-- > 0; i1 = i) {
      final double xi = x[i];
      final double xi1 = x[i1];
      final double yi = y[i];
      final double yi1 = y[i1];
      l += distance(xi, yi, xi1, yi1);
      final double xiyi1 = xi * yi1;
      final double xi1yi = xi1 * yi;
      sum1 += xiyi1;
      sum2 += xi1yi;
      final double d = xiyi1 - xi1yi;
      cx += (xi + xi1) * d;
      cy += (yi + yi1) * d;
    }
    length = l;
    double a = Math.abs((sum1 - sum2) / 2);
    area = a;
    a *= 6;
    centroid = new double[] {cx / a, cy / a};
  }

  /**
   * Compute the euclidian distance between two 2D points.
   *
   * @param x1 the x 1
   * @param y1 the y 1
   * @param x2 the x 2
   * @param y2 the y 2
   * @return the distance
   */
  private static double distance(double x1, double y1, double x2, double y2) {
    // Note: This casts up to double for increased precision
    final double dx = x1 - x2;
    final double dy = y1 - y2;
    return Math.sqrt(dx * dx + dy * dy);
  }
}
