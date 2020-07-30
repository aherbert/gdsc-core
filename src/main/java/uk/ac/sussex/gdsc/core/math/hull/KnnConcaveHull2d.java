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

import gnu.trove.list.array.TIntArrayList;
import java.util.Arrays;
import uk.ac.sussex.gdsc.core.data.VisibleForTesting;
import uk.ac.sussex.gdsc.core.math.GeometryUtils;
import uk.ac.sussex.gdsc.core.trees.DoubleDistanceFunctions;
import uk.ac.sussex.gdsc.core.trees.IntDoubleKdTree;
import uk.ac.sussex.gdsc.core.trees.KdTrees;

/**
 * Build a set of paired coordinates representing the concave hull of a set of points.
 *
 * <p>The algorithm uses the K-nearest neighbours method to construct the hull by selecting the
 * neighbour with the largest right hand turn from the current hull edge that does not intersect the
 * existing hull.
 *
 * <blockquote>Moreira and Santos (2007) <br>Concave hull: A k-nearest neighbours approach for the
 * computation of the region occupied by a set of points. <br>Conference: GRAPP 2007, Proceedings
 * of the Second International Conference on Computer Graphics Theory and Applications, Barcelona,
 * Spain.</blockquote>
 *
 * @since 2.0
 */
public final class KnnConcaveHull2d {

  /**
   * A builder to create a 2D concave hull.
   *
   * @since 2.0
   */
  public static final class Builder implements Hull.Builder {

    /** The coordinates stored in a KD-tree. */
    private IntDoubleKdTree tree;

    /** The number of nearest neighbours (k). */
    private int numberOfNeighbours = 3;

    /**
     * Private constructor.
     */
    private Builder() {
      clear();
    }

    /**
     * Gets the number of nearest neighbours (k) to scan when extending the hull.
     *
     * @return the number of neighbours
     */
    public int getK() {
      return numberOfNeighbours;
    }

    /**
     * Sets the number of nearest neighbours (k) to scan when extending the hull.
     *
     * @param k the number of neighbours
     * @return a reference to this builder
     */
    public KnnConcaveHull2d.Builder setK(int k) {
      // Make sure k >= 3
      this.numberOfNeighbours = Math.max(3, k);
      return this;
    }

    /**
     * {@inheritDoc}.
     *
     * <p>This method uses only the first 2 indexes in the input point. Higher dimensions are
     * ignored.
     */
    @Override
    public KnnConcaveHull2d.Builder add(double... point) {
      // Ensure the point is unique
      tree.addIfAbsent(new double[] {point[0], point[1]}, tree.size());
      return this;
    }

    @Override
    public KnnConcaveHull2d.Builder clear() {
      tree = KdTrees.newIntDoubleKdTree(2);
      return this;
    }

    @Override
    public Hull2d build() {
      if (tree.size() == 0) {
        return null;
      }
      // Get the coordinates
      final double[][] points = new double[tree.size()][];
      tree.forEach((p, t) -> points[t] = p);
      if (points.length <= 3) {
        return Hull2d.create(points);
      }
      final int first = findMinYPoint(points);
      // Ensure that k neighbours can be found
      final int kk = Math.min(numberOfNeighbours, points.length - 1);
      final ActiveList active = new ActiveList(points.length);
      final TIntArrayList hull = new TIntArrayList();
      return concaveHull(tree, points, active, kk, first, hull);
    }

    /**
     * Find the minimum point on the y-axis. Ties in y are resolved using the smallest x.
     *
     * @param points the points
     * @return the index of the minimum point
     */
    private static int findMinYPoint(double[][] points) {
      int min = 0;
      double x = points[0][0];
      double y = points[0][1];
      for (int i = 1; i < points.length; i++) {
        final double yy = points[i][1];
        if (yy > y) {
          continue;
        }
        // Equal or smaller. Resolve ties with x.
        if (yy < y || points[i][0] < x) {
          min = i;
          x = points[i][0];
          y = yy;
        }
      }
      return min;
    }

    /**
     * Compute the concave hull using the KNN algorithm. Must be called with at least 4 points and k
     * above 3.
     *
     * @param tree the tree
     * @param points the points
     * @param active the working list of active points not in the hull
     * @param k the number of neighbours (k)
     * @param first the first point
     * @param hull the working list of hull points
     * @return the hull
     */
    private static Hull2d concaveHull(IntDoubleKdTree tree, double[][] points, ActiveList active,
        int k, int first, TIntArrayList hull) {
      if (k >= points.length) {
        // Occurs when increasing k has not found a solution
        return null;
      }
      // Initialise the hull with the first point
      active.enableAll();
      hull.resetQuick();
      hull.add(first);
      active.disable(first);
      // Initialise the search
      int current = first;
      double previousAngle = AngleList.angle(0, 0, -1, 0);
      int step = 2;
      final AngleList knn = new AngleList(k);
      while ((current != first || step == 2) && active.size() > 0) {
        if (step == 5) {
          // On step 5 there are 4 points in the hull. Allow it to close on the first point.
          // Note: All hulls of <= 3 points have previously been covered.
          active.enable(first);
        }
        // Find the nearest neighbours
        knn.clear();
        tree.nearestNeighbours(points[current], k, false,
            DoubleDistanceFunctions.SQUARED_EUCLIDEAN_2D, active::isEnabled, knn::add);
        knn.sortByAngle(points, current, previousAngle);

        final int index = select(points, first, current, knn, hull);
        if (index == -1) {
          // Since all candidates intersect at least one edge, try again with a higher
          // number of neighbours
          return concaveHull(tree, points, active, k + 1, first, hull);
        }

        // Modification from the Moreira and Santos method.
        // Ordering by angle may not handle colinear points with the same angle. So we
        // sort using the distance (descending) for the same angle and then exclude any
        // remaining candidates with the same angle.
        current = knn.getIndex(index);
        final double angle = knn.getAngle(index);
        for (int i = index + 1; i < knn.size(); i++) {
          if (angle == knn.getAngle(i)) {
            active.disable(knn.getIndex(i));
          } else {
            break;
          }
        }

        // Set the angle for the next step
        previousAngle = AngleList.angle(points[current], points[hull.getQuick(hull.size() - 1)]);
        // Extend the hull
        hull.add(current);
        active.disable(current);
        step++;
      }

      final Hull2d hull2d = createHull(points, hull);
      // Check if all the points are inside the polygon
      for (int i = 0; i < points.length; i++) {
        // Ignore current hull points
        if (active.isEnabled(i) && !hull2d.contains(points[i])) {
          // At least one point is outside the polygon.
          // Note: points on the hull boundary may be classed as outside.
          // This will occur if points are colinear and on the right-hand boundary edge.
          // Fix by adding a test if the point intersects the boundary of the hull.
          return concaveHull(tree, points, active, k + 1, first, hull);
        }
      }
      return hull2d;
    }

    /**
     * Select the first candidate that does not intersects any of the polygon edges.
     *
     * @param points the points
     * @param first the first
     * @param current the current
     * @param knn the nearest neighbour candidates
     * @param hull the list of hull point indexes
     * @return the index of the candidate to extend the hull (or -1)
     */
    private static int select(double[][] points, int first, int current, AngleList knn,
        TIntArrayList hull) {
      final double x1 = points[current][0];
      final double y1 = points[current][1];
      // Store the start point for testing lines in the current hull
      final int n = hull.size() - 2;
      NEXT_CANDIDATE: for (int i = 0; i < knn.size(); i++) {
        final int candidate = knn.getIndex(i);
        final double x2 = points[candidate][0];
        final double y2 = points[candidate][1];
        // Store the end point for testing lines in the current hull
        int last;
        if (candidate == first) {
          last = 1;
        } else {
          last = 0;
        }
        // Count down to test lines most recently added to the hull first.
        // Note: No need to test the most recent edge added to the hull for an intersect.
        for (int j = n; j > last; j--) {
          final int p1 = hull.get(j - 1);
          final int p2 = hull.get(j);
          final double x3 = points[p1][0];
          final double y3 = points[p1][1];
          final double x4 = points[p2][0];
          final double y4 = points[p2][1];
          if (GeometryUtils.testIntersect(x1, y1, x2, y2, x3, y3, x4, y4)) {
            continue NEXT_CANDIDATE;
          }
        }
        // A valid candidate was found
        return i;
      }
      return -1;
    }

    /**
     * Creates the hull from the point indexes. This ignores the final point if it is the same as
     * the first point.
     *
     * @param points the points
     * @param hull the list of hull point indexes
     * @return the hull
     */
    private static Hull2d createHull(double[][] points, TIntArrayList hull) {
      // Ensure we create a self-closing polygon (no duplicate first and last point)
      if (hull.getQuick(0) == hull.getQuick(hull.size() - 1)) {
        hull.remove(hull.size() - 1, 1);
      }
      final double[] x = new double[hull.size()];
      final double[] y = new double[hull.size()];
      for (int i = 0; i < hull.size(); i++) {
        final double[] p = points[hull.getQuick(i)];
        x[i] = p[0];
        y[i] = p[1];
      }
      return Hull2d.create(x, y);
    }
  }

  /**
   * Contains a list of fixed capacity to store a point index and the angle of the point with
   * respect to a previous vector and origin.
   */
  @VisibleForTesting
  static final class AngleList {
    /** Used to convert an angle in radians to turns. */
    private static final double FROM_RADIANS = 1.0 / (2 * Math.PI);

    /**
     * Store the index, and distance and angle relative to the current hull point.
     */
    static final class IndexAngle {
      /** The index. */
      int index;
      /** The distance. */
      double distance;
      /** The angle. */
      double angle;

      /**
       * Compare the two angles. Ties are resolved using the largest distance.
       *
       * @param o1 the first object
       * @param o2 the second object
       * @return -1 if the first object has a higher angle (or same angle and higher distance), 1
       *         if lower (or same angle and lower distance), else 0 if the same
       */
      static int compare(IndexAngle o1, IndexAngle o2) {
        // highest first
        final int result = Double.compare(o2.angle, o1.angle);
        // Resolve ties with the distance
        return result == 0 ? Double.compare(o2.distance, o1.distance) : result;
      }
    }

    /** The data. */
    private final IndexAngle[] data;

    /** The size. */
    private int size;

    /**
     * Create an instance.
     *
     * @param capacity the capacity
     */
    AngleList(int capacity) {
      this.data = new IndexAngle[capacity];
      for (int i = 0; i < capacity; i++) {
        data[i] = new IndexAngle();
      }
    }

    /**
     * Get the size.
     *
     * @return the size
     */
    int size() {
      return size;
    }

    /**
     * Adds the index and distance. No bounds checks are made against capacity.
     *
     * @param index the index
     * @param distance the distance
     */
    void add(int index, double distance) {
      data[size].index = index;
      data[size].distance = distance;
      size++;
    }

    /**
     * Gets the index at the given offset. No bounds checks are made against the size.
     *
     * @param offset the offset
     * @return the index
     */
    int getIndex(int offset) {
      return data[offset].index;
    }

    /**
     * Gets the index at the given offset. No bounds checks are made against the size.
     *
     * @param offset the offset
     * @return the index
     */
    double getAngle(int offset) {
      return data[offset].angle;
    }

    /**
     * Clear the list.
     */
    void clear() {
      size = 0;
    }

    /**
     * Sort the current candidates (neighbours) in descending order of right-hand turn. Ties are
     * handled using the largest distance. Thus in the case of colinear points the furthest point is
     * chosen.
     *
     * @param points the points
     * @param current the current point
     * @param previousAngle the previous angle
     */
    void sortByAngle(double[][] points, int current, double previousAngle) {
      // Compute angles
      final double ox = points[current][0];
      final double oy = points[current][1];
      for (int i = 0; i < size; i++) {
        data[i].angle = clockwiseTurns(previousAngle, angle(ox, oy, points[data[i].index]));
      }
      // Descending sort
      Arrays.sort(data, 0, size, IndexAngle::compare);
    }

    /**
     * Compute the number of turns <em>clockwise</em> to move from angle 1 to 2. The angles should
     * be in radians in the range -pi to pi with increasing angle for the <em>counter-clockwise</em>
     * direction (as computed by {@link Math#atan2(double, double)}).
     *
     * <p>The returned number of turns is in the range {@code [0, 1]}.
     *
     * @param angle1 the first angle
     * @param angle2 the second angle
     * @return the number of turns clockwise
     */
    static double clockwiseTurns(double angle1, double angle2) {
      // Adapted from org.apache.commons.numbers.angle.PlaneAngle.normalize(...)
      // Convert radians to turns.
      // Expected range is [-0.5, 0.5] turns for each angle.
      // Result is in the range [-1, 1].
      final double turns = (angle1 - angle2) * FROM_RADIANS;
      // Normalise into the range [0, 1]
      return turns - Math.floor(turns);
    }

    /**
     * Compute the angle of the vector between the point and the origin.
     *
     * @param origin the origin
     * @param point the point
     * @return the angle
     * @see #angle(double, double, double, double)
     */
    static double angle(double[] origin, double[] point) {
      return angle(origin[0], origin[1], point[0], point[1]);
    }

    /**
     * Compute the angle of the vector between the point and the origin.
     *
     * @param ox the origin x
     * @param oy the origin y
     * @param point the point
     * @return the angle
     * @see #angle(double, double, double, double)
     */
    static double angle(double ox, double oy, double[] point) {
      return angle(ox, oy, point[0], point[1]);
    }

    /**
     * Compute the angle of the vector between the point and the origin.
     *
     * <p>Uses {@link Math#atan2(double, double)} thus increasing angle is for the
     * <em>counter-clockwise</em> direction.
     *
     * @param ox the origin x
     * @param oy the origin y
     * @param px the point x
     * @param py the point y
     * @return the angle
     */
    static double angle(double ox, double oy, double px, double py) {
      final double dx = px - ox;
      final double dy = py - oy;
      return Math.atan2(dy, dx);
    }
  }

  /**
   * No instances.
   */
  private KnnConcaveHull2d() {}

  /**
   * Create a new builder.
   *
   * @return the builder
   */
  public static KnnConcaveHull2d.Builder newBuilder() {
    return new KnnConcaveHull2d.Builder();
  }

  /**
   * Create a new concave hull from the given coordinates.
   *
   * <p>The hull may be null if it cannot be created (e.g. not enough non-colinear points).
   *
   * @param x the x coordinates
   * @param y the y coordinates
   * @return the concave hull
   * @throws NullPointerException if the inputs are null
   * @throws ArrayIndexOutOfBoundsException if the y are smaller than the x
   */
  public static Hull2d create(double[] x, double[] y) {
    return create(x, y, x.length);
  }

  /**
   * Create a new concave hull from the given coordinates.
   *
   * <p>The hull may be null if it cannot be created (e.g. not enough non-colinear points).
   *
   * @param x the x coordinates
   * @param y the y coordinates
   * @param n the number of coordinates
   * @return the concave hull
   * @throws NullPointerException if the inputs are null
   * @throws ArrayIndexOutOfBoundsException if the arrays are smaller than n
   */
  public static Hull2d create(double[] x, double[] y, int n) {
    return create(0, x, y, n);
  }

  /**
   * Create a new concave hull from the given coordinates using the given number of neighbours (k)
   * to scan when extending the hull.
   *
   * <p>The hull may be null if it cannot be created (e.g. not enough non-colinear points).
   *
   * @param k the number of neighbours
   * @param x the x coordinates
   * @param y the y coordinates
   * @param n the number of coordinates
   * @return the concave hull
   * @throws NullPointerException if the inputs are null
   * @throws ArrayIndexOutOfBoundsException if the arrays are smaller than n
   */
  public static Hull2d create(int k, double[] x, double[] y, int n) {
    final Builder builder = newBuilder().setK(k);
    for (int i = 0; i < n; i++) {
      builder.add(x[i], y[i]);
    }
    return builder.build();
  }
}
