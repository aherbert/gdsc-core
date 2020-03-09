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

package uk.ac.sussex.gdsc.core.utils;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.euclidean.twod.hull.ConvexHull2D;
import org.apache.commons.math3.geometry.euclidean.twod.hull.MonotoneChain;
import uk.ac.sussex.gdsc.core.math.GeometryUtils;

/**
 * Contains a set of paired coordinates representing the convex hull of a set of points.
 *
 * <p>Functionality of this has been taken from {@link ij.process.FloatPolygon}.
 */
public final class ConvexHull {

  /** Default value for tolerance. */
  private static final double DEFAULT_TOLERANCE = 1e-10;

  /** The x coordinates. */
  public final float[] x;

  /** The y coordinates. */
  public final float[] y;

  private Rectangle bounds;
  private float minX;
  private float minY;
  private float maxX;
  private float maxY;

  /**
   * Instantiates a new convex hull.
   *
   * @param x the x coordinates
   * @param y the y coordinates
   */
  private ConvexHull(float[] x, float[] y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Get the size.
   *
   * @return the size
   */
  public int size() {
    return x.length;
  }

  /**
   * Create a new convex hull from the given coordinates.
   *
   * @param x the x coordinates
   * @param y the y coordinates
   * @return the convex hull
   * @throws NullPointerException if the inputs are null
   * @throws ArrayIndexOutOfBoundsException if the y are smaller than the x
   */
  public static ConvexHull create(float[] x, float[] y) {
    return create(x, y, x.length);
  }

  /**
   * Create a new convex hull from the given coordinates.
   *
   * @param x the x coordinates
   * @param y the y coordinates
   * @param n the number of coordinates
   * @return the convex hull
   * @throws NullPointerException if the inputs are null
   * @throws ArrayIndexOutOfBoundsException if the y are smaller than the x
   */
  public static ConvexHull create(float[] x, float[] y, int n) {
    return create(0, 0, x, y, n);
  }

  /**
   * Create a new convex hull from the given coordinates.
   *
   * <p>Returns null if the convex hull cannot be computed by the algorithm.
   *
   * @param xbase the x base coordinate (origin)
   * @param ybase the y base coordinate (origin)
   * @param x the x coordinates
   * @param y the y coordinates
   * @param n the number of coordinates
   * @return the convex hull (or null)
   * @throws NullPointerException if the inputs are null
   * @throws ArrayIndexOutOfBoundsException if the y are smaller than the x
   */
  public static ConvexHull create(float xbase, float ybase, float[] x, float[] y, int n) {
    // Use Apache Math to do this
    final MonotoneChain chain = new MonotoneChain(false, DEFAULT_TOLERANCE);
    final LocalList<Vector2D> points = new LocalList<>(n);
    for (int i = 0; i < n; i++) {
      points.add(new Vector2D(xbase + x[i], ybase + y[i]));
    }
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

    final float[] xx = new float[size];
    final float[] yy = new float[size];
    for (int i = 0; i < size; i++) {
      xx[i] = (float) v[i].getX();
      yy[i] = (float) v[i].getY();
    }
    return new ConvexHull(xx, yy);
  }

  // Below is functionality taken from ij.process.FloatPolygon

  /**
   * Returns 'true' if the point (x,y) is inside this polygon. This is a Java version of the
   * remarkably small C program by W. Randolph Franklin at
   * http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html#The%20C%20Code
   *
   * @param x the x
   * @param y the y
   * @return true, if successful
   */
  public boolean contains(float x, float y) {
    final float[] xpoints = this.x;
    final float[] ypoints = this.y;
    boolean inside = false;
    for (int i = xpoints.length, j = 0; i-- > 0; j = i) {
      if (((ypoints[i] > y) != (ypoints[j] > y))
          && (x < (xpoints[j] - xpoints[i]) * (y - ypoints[i]) / (ypoints[j] - ypoints[i])
              + xpoints[i])) {
        inside = !inside;
      }
    }
    return inside;
  }

  /**
   * Gets the bounds.
   *
   * @return the bounds
   */
  public Rectangle getBounds() {
    // Size is never zero so there are always some bounds
    if (bounds == null) {
      calculateBounds(x, y, size());
    }
    return bounds.getBounds();
  }

  /**
   * Gets the float bounds.
   *
   * @return the float bounds
   */
  public Rectangle2D.Double getFloatBounds() {
    // Size is never zero so there are always some bounds
    if (bounds == null) {
      calculateBounds(x, y, size());
    }
    return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
  }

  private void calculateBounds(float[] xpoints, float[] ypoints, int npoints) {
    minX = xpoints[0];
    minY = ypoints[0];
    maxX = minX;
    maxY = minY;
    for (int i = 1; i < npoints; i++) {
      final float xp = xpoints[i];
      if (maxX < xp) {
        maxX = xp;
      } else if (minX > xp) {
        // Currently this is not hit in test coverage as the hull
        // is computed with the first vertex using the min X value.
        minX = xp;
      }
      final float yp = ypoints[i];
      if (maxY < yp) {
        maxY = yp;
      } else if (minY > yp) {
        minY = yp;
      }
    }
    final int iMinX = (int) Math.floor(minX);
    final int iMinY = (int) Math.floor(minY);
    bounds = new Rectangle(iMinX, iMinY, (int) Math.ceil((double) maxX - iMinX),
        (int) Math.ceil((double) maxY - iMinY));
  }

  /**
   * Gets the length.
   *
   * @return the length
   */
  public double getLength() {
    if (size() < 2) {
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
