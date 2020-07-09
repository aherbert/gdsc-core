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

package uk.ac.sussex.gdsc.core.math.hull;

import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.euclidean.twod.hull.ConvexHull2D;
import org.apache.commons.math3.geometry.euclidean.twod.hull.MonotoneChain;
import uk.ac.sussex.gdsc.core.math.GeometryUtils;
import uk.ac.sussex.gdsc.core.utils.LocalList;

/**
 * Contains a set of paired coordinates representing the convex hull of a set of points.
 */
public final class ConvexHull2d implements Hull {

  /**
   * A builder to create a 2D convex hull.
   */
  public static final class Builder implements Hull.Builder {
    /** Default value for tolerance. */
    private static final double DEFAULT_TOLERANCE = 1e-10;

    private final LocalList<Vector2D> points = new LocalList<>();

    /**
     * Private constructor.
     */
    private Builder() {
      // Do nothing
    }

    @Override
    public ConvexHull2d.Builder add(double... point) {
      points.add(new Vector2D(point[0], point[1]));
      return this;
    }

    @Override
    public ConvexHull2d.Builder clear() {
      points.clear();
      return this;
    }

    @Override
    public ConvexHull2d build() {
      // Use Apache Math to do this
      final MonotoneChain chain = new MonotoneChain(false, DEFAULT_TOLERANCE);
      ConvexHull2D hull = null;
      try {
        hull = chain.generate(points);
      } catch (final ConvergenceException ex) {
        // Ignore
      }

      if (hull == null) {
        return null;
      }

      final Vector2D[] v = hull.getVertices();
      final int size = v.length;
      if (size == 0) {
        return null;
      }

      final double[] xx = new double[size];
      final double[] yy = new double[size];
      for (int i = 0; i < size; i++) {
        xx[i] = v[i].getX();
        yy[i] = v[i].getY();
      }
      return new ConvexHull2d(xx, yy);
    }
  }

  /** The x coordinates. */
  private final double[] x;

  /** The y coordinates. */
  private final double[] y;

  /**
   * Instantiates a new convex hull.
   *
   * @param x the x coordinates
   * @param y the y coordinates
   */
  private ConvexHull2d(double[] x, double[] y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Create a new builder.
   *
   * @return the builder
   */
  public static ConvexHull2d.Builder newBuilder() {
    return new ConvexHull2d.Builder();
  }

  /**
   * Create a new convex hull from the given coordinates.
   *
   * <p>The hull may be null if it cannot be created (e.g. not enough non-colinear points).
   *
   * @param x the x coordinates
   * @param y the y coordinates
   * @return the convex hull
   * @throws NullPointerException if the inputs are null
   * @throws ArrayIndexOutOfBoundsException if the y are smaller than the x
   */
  public static ConvexHull2d create(double[] x, double[] y) {
    return create(x, y, x.length);
  }

  /**
   * Create a new convex hull from the given coordinates.
   *
   * <p>The hull may be null if it cannot be created (e.g. not enough non-colinear points).
   *
   * @param x the x coordinates
   * @param y the y coordinates
   * @param n the number of coordinates
   * @return the convex hull
   * @throws NullPointerException if the inputs are null
   * @throws ArrayIndexOutOfBoundsException if the arrays are smaller than n
   */
  public static ConvexHull2d create(double[] x, double[] y, int n) {
    final Builder builder = newBuilder();
    for (int i = 0; i < n; i++) {
      builder.add(x[i], y[i]);
    }
    return builder.build();
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
   * {@inheritDoc}.
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
   * @param point the point
   * @return true if the hull contains the point
   */
  public boolean contains(double[] point) {
    // This is a Java version of the remarkably small C program by W. Randolph Franklin at
    // http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html#The%20C%20Code
    final double xp = point[0];
    final double yp = point[1];
    final double[] xpoints = this.x;
    final double[] ypoints = this.y;
    boolean inside = false;
    for (int i = xpoints.length, j = 0; i-- > 0; j = i) {
      if (((ypoints[i] > yp) != (ypoints[j] > yp))
          && (xp < (xpoints[j] - xpoints[i]) * (yp - ypoints[i]) / (ypoints[j] - ypoints[i])
              + xpoints[i])) {
        inside = !inside;
      }
    }
    return inside;
  }

  /**
   * Gets the length.
   *
   * @return the length
   */
  public double getLength() {
    if (getNumberOfVertices() < 2) {
      return 0;
    }
    double length = 0;
    for (int i = x.length, j = 0; i-- > 0; j = i) {
      length += distance(x[i], y[i], x[j], y[j]);
    }
    return length;
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

  /**
   * Gets the area.
   *
   * @return the area
   */
  public double getArea() {
    return GeometryUtils.getArea(x, y);
  }
}
