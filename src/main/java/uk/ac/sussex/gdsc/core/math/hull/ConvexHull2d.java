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

import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.euclidean.twod.hull.ConvexHull2D;
import org.apache.commons.math3.geometry.euclidean.twod.hull.MonotoneChain;
import uk.ac.sussex.gdsc.core.utils.LocalList;

/**
 * Build a set of paired coordinates representing the convex hull of a set of points.
 *
 * @since 2.0
 */
public final class ConvexHull2d {

  /**
   * A builder to create a 2D convex hull.
   *
   * @since 2.0
   */
  public static final class Builder implements Hull.Builder {
    /** Default value for tolerance. */
    private static final double DEFAULT_TOLERANCE = 1e-10;

    private final LocalList<Vector2D> points = new LocalList<>();

    /**
     * Private constructor.
     */
    Builder() {
      // Do nothing
    }

    /**
     * {@inheritDoc}.
     *
     * <p>This method uses only the first 2 indexes in the input point. Higher dimensions are
     * ignored.
     */
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
    public Hull2d build() {
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
      return new Hull2d(xx, yy);
    }
  }

  /**
   * No instances.
   */
  private ConvexHull2d() {}

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
  public static Hull2d create(double[] x, double[] y) {
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
  public static Hull2d create(double[] x, double[] y, int n) {
    final Builder builder = newBuilder();
    for (int i = 0; i < n; i++) {
      builder.add(x[i], y[i]);
    }
    return builder.build();
  }
}
