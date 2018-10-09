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
package uk.ac.sussex.gdsc.core.utils;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;


import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.euclidean.twod.hull.ConvexHull2D;
import org.apache.commons.math3.geometry.euclidean.twod.hull.MonotoneChain;

import uk.ac.sussex.gdsc.core.math.Geometry;

/**
 * Contains a set of paired coordinates representing the convex hull of a set of points. <p>
 * Functionality of this has been taken from ij.process.FloatPolygon.
 */
public class ConvexHull {
  /** The x coordinates. */
  public final float[] x;

  /** The y coordinates. */
  public final float[] y;

  /**
   * Instantiates a new convex hull.
   *
   * @param x the x
   * @param y the y
   */
  private ConvexHull(float xbase, float ybase, float[] x, float[] y) {
    this.x = x;
    this.y = y;
    if (xbase != 0 || ybase != 0) {
      for (int i = x.length; i-- > 0;) {
        x[i] += xbase;
        y[i] += ybase;
      }
    }
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
   * @param xCoordinates the x coordinates
   * @param yCoordinates the y coordinates
   * @return the convex hull
   * @throws NullPointerException if the inputs are null
   * @throws ArrayIndexOutOfBoundsException if the yCoordinates are smaller than the xCoordinates
   */
  public static ConvexHull create(float[] xCoordinates, float[] yCoordinates) {
    return create(0, 0, xCoordinates, yCoordinates, xCoordinates.length);
  }

  /**
   * Create a new convex hull from the given coordinates.
   *
   * @param xCoordinates the x coordinates
   * @param yCoordinates the y coordinates
   * @param n the number of coordinates
   * @return the convex hull
   * @throws NullPointerException if the inputs are null
   * @throws ArrayIndexOutOfBoundsException if the yCoordinates are smaller than the xCoordinates
   */
  public static ConvexHull create(float[] xCoordinates, float[] yCoordinates, int n) {
    return create(0, 0, xCoordinates, yCoordinates, n);
  }

  /** Default value for tolerance. */
  private static final double DEFAULT_TOLERANCE = 1e-10;

  /**
   * Create a new convex hull from the given coordinates.
   *
   * @param xbase the x base coordinate (origin)
   * @param ybase the y base coordinate (origin)
   * @param xCoordinates the x coordinates
   * @param yCoordinates the y coordinates
   * @param n the number of coordinates
   * @return the convex hull
   * @throws NullPointerException if the inputs are null
   * @throws ArrayIndexOutOfBoundsException if the yCoordinates are smaller than the xCoordinates
   */
  public static ConvexHull create(float xbase, float ybase, float[] xCoordinates,
      float[] yCoordinates, int n) {
    // Use Apache Math to do this
    final MonotoneChain chain = new MonotoneChain(false, DEFAULT_TOLERANCE);
    final TurboList<Vector2D> points = new TurboList<>(n);
    for (int i = 0; i < n; i++) {
      points.add(new Vector2D(xbase + xCoordinates[i], ybase + yCoordinates[i]));
    }
    ConvexHull2D hull = null;
    try {
      hull = chain.generate(points);
    } catch (final ConvergenceException e) { // Ignore
    }

    if (hull == null) {
      return null;
    }

    final Vector2D[] v = hull.getVertices();
    if (v.length == 0) {
      return null;
    }

    final int size = v.length;
    final float[] xx = new float[size];
    final float[] yy = new float[size];
    int n2 = 0;
    for (int i = 0; i < size; i++) {
      xx[n2] = (float) v[i].getX();
      yy[n2] = (float) v[i].getY();
      n2++;
    }
    return new ConvexHull(0, 0, xx, yy);
  }

  // Below is functionality taken from ij.process.FloatPolygon
  private Rectangle bounds;
  private float minX, minY, maxX, maxY;

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
    final int npoints = size();
    if (npoints == 0) {
      return new Rectangle();
    }
    if (bounds == null) {
      calculateBounds(x, y, npoints);
    }
    return bounds.getBounds();
  }

  /**
   * Gets the float bounds.
   *
   * @return the float bounds
   */
  public Rectangle2D.Double getFloatBounds() {
    final int npoints = size();
    final float[] xpoints = this.x;
    final float[] ypoints = this.y;
    if (npoints == 0) {
      return new Rectangle2D.Double();
    }
    if (bounds == null) {
      calculateBounds(xpoints, ypoints, npoints);
    }
    return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
  }

  private void calculateBounds(float[] xpoints, float[] ypoints, int npoints) {
    minX = xpoints[0];
    minY = ypoints[0];
    maxX = minX;
    maxY = minY;
    for (int i = 1; i < npoints; i++) {
      final float x = xpoints[i];
      if (maxX < x) {
        maxX = x;
      } else if (minX > x) {
        minX = x;
      }
      final float y = ypoints[i];
      if (maxY < y) {
        maxY = y;
      } else if (minY > y) {
        minY = y;
      }
    }
    final int iMinX = (int) Math.floor(minX);
    final int iMinY = (int) Math.floor(minY);
    bounds = new Rectangle(iMinX, iMinY, (int) (maxX - iMinX + 0.5), (int) (maxY - iMinY + 0.5));
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
    return Geometry.getArea(x, y);
  }
}
