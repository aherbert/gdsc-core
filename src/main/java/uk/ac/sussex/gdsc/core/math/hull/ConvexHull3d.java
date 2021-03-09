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

import com.github.quickhull3d.Point3d;
import com.github.quickhull3d.QuickHull3D;
import uk.ac.sussex.gdsc.core.utils.LocalList;

/**
 * Build a set of paired coordinates representing the 3D convex hull of a set of points.
 *
 * @since 2.0
 */
public final class ConvexHull3d {

  /**
   * A builder to create a 3D convex hull.
   *
   * @since 2.0
   */
  public static final class Builder implements Hull.Builder {
    private final LocalList<Point3d> points = new LocalList<>();
    private boolean triangulate;

    /**
     * Private constructor.
     */
    private Builder() {
      // Do nothing
    }

    /**
     * Checks if the hull faces will be triangulated.
     *
     * @return true if the hull faces are split to triangles
     */
    public boolean isTriangulate() {
      return triangulate;
    }

    /**
     * Sets if the hull faces will be triangulated.
     *
     * <p>Note: In some cases, due to precision issues, the resulting triangles may be very thin or
     * small, and hence appear to be non-convex.
     *
     * @param triangulate true if the hull faces are split to triangles
     */
    public void setTriangulate(boolean triangulate) {
      this.triangulate = triangulate;
    }

    /**
     * {@inheritDoc}.
     *
     * <p>This method uses only the first 3 indexes in the input point. Higher dimensions are
     * ignored.
     */
    @Override
    public ConvexHull3d.Builder add(double... point) {
      points.add(new Point3d(point[0], point[1], point[2]));
      return this;
    }

    @Override
    public ConvexHull3d.Builder clear() {
      points.clear();
      return this;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This will return null if the number of points is less than 4.
     */
    @Override
    public Hull3d build() {
      // Not possible with less than 4 points
      if (points.size() < 4) {
        return null;
      }

      // Use quickhull3d to do this
      final QuickHull3D hull = new QuickHull3D();
      try {
        hull.build(points.toArray(new Point3d[0]));
      } catch (final IllegalArgumentException ex) {
        // Not possible to create a hull
        return null;
      }

      final Point3d[] vertices = hull.getVertices();
      final int size = vertices.length;
      final double[][] v = new double[size][];
      for (int i = 0; i < size; i++) {
        final Point3d p = vertices[i];
        v[i] = new double[] {p.x, p.y, p.z};
      }
      if (isTriangulate()) {
        hull.triangulate();
      }
      return new Hull3d(v, hull.getFaces());
    }
  }

  /**
   * No instances.
   */
  private ConvexHull3d() {}

  /**
   * Create a new builder.
   *
   * @return the builder
   */
  public static ConvexHull3d.Builder newBuilder() {
    return new ConvexHull3d.Builder();
  }

  /**
   * Create a new convex hull from the given coordinates.
   *
   * <p>The hull may be null if it cannot be created (e.g. the points are degenerate so that they
   * are either coincident, colinear, or colplanar, and thus the convex hull has a zero volume).
   *
   * @param x the x coordinates
   * @param y the y coordinates
   * @param z the z coordinates
   * @return the convex hull
   * @throws NullPointerException if the inputs are null
   * @throws ArrayIndexOutOfBoundsException if the y or z are smaller than the x
   */
  public static Hull3d create(double[] x, double[] y, double[] z) {
    return create(x, y, z, x.length);
  }

  /**
   * Create a new convex hull from the given coordinates.
   *
   * <p>The hull may be null if it cannot be created (e.g. the points are degenerate so that they
   * are either coincident, colinear, or colplanar, and thus the convex hull has a zero volume).
   *
   * @param x the x coordinates
   * @param y the y coordinates
   * @param z the z coordinates
   * @param n the number of coordinates
   * @return the convex hull
   * @throws NullPointerException if the inputs are null
   * @throws ArrayIndexOutOfBoundsException if the arrays are smaller than n
   */
  public static Hull3d create(double[] x, double[] y, double[] z, int n) {
    final Builder builder = newBuilder();
    for (int i = 0; i < n; i++) {
      builder.add(x[i], y[i], z[i]);
    }
    return builder.build();
  }
}
