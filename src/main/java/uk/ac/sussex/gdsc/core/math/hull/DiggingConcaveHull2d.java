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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.ToIntFunction;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.euclidean.twod.hull.ConvexHull2D;
import org.apache.commons.math3.geometry.euclidean.twod.hull.MonotoneChain;
import uk.ac.sussex.gdsc.core.math.GeometryUtils;
import uk.ac.sussex.gdsc.core.trees.DoubleDistanceFunction;
import uk.ac.sussex.gdsc.core.trees.DoubleDistanceFunctions;
import uk.ac.sussex.gdsc.core.trees.IntDoubleKdTree;
import uk.ac.sussex.gdsc.core.trees.KdTrees;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

/**
 * Build a set of paired coordinates representing the concave hull of a set of points.
 *
 * <p>The algorithm uses the digging algorithm method to construct the hull by iteratively digging
 * into the initial concave hull based on the ratio of the distance between two hull points (edge
 * distance, eh) and the distance of the hull points to the closest internal point (decision
 * distance, dd).
 *
 * <pre>
 * {@code
 * eh / dd > N
 * }
 * </pre>
 *
 * <p>Where {@code N} is the algorithm parameter. If the ratio exceeds the threshold {@code N} then
 * the edge is removed and replaced by two edges from the previous edge vertices to the internal
 * point.
 *
 * <blockquote>Park and Oh (2012) <br>A New Concave Hull Algorithm and Concaveness Measure for
 * n-dimensional Datasets. <br>Journal of Information Science and Engineering
 * 28:587-600.</blockquote>
 *
 * @since 2.0
 */
public final class DiggingConcaveHull2d {

  /**
   * A builder to create a 2D concave hull.
   *
   * @since 2.0
   */
  public static final class Builder implements Hull.Builder {
    /** Default value for decision threshold. */
    private static final double DEFAULT_THRESHOLD = 2.0;
    /** Default value for tolerance for constructing the initial convex hull. */
    private static final double DEFAULT_TOLERANCE = 0.0;
    /** The distance function. */
    private static final DoubleDistanceFunction DISTANCE_FUNCTION =
        DoubleDistanceFunctions.SQUARED_EUCLIDEAN_2D;

    /** The coordinates stored in a KD-tree. */
    private IntDoubleKdTree tree;

    /** The edge distance to decision distance threshold for digging. */
    private double threshold = DEFAULT_THRESHOLD;

    /**
     * Private constructor.
     */
    private Builder() {
      clear();
    }

    /**
     * Gets edge distance to decision distance threshold for digging.
     *
     * @return the threshold
     */
    public double getThreshold() {
      return threshold;
    }

    /**
     * Sets edge distance to decision distance threshold for digging.
     *
     * @param threshold the threshold
     * @return a reference to this builder
     * @throws IllegalArgumentException if {@code value} is not {@code >0}
     */
    public DiggingConcaveHull2d.Builder setThreshold(double threshold) {
      // Make sure threshold is positive
      ValidationUtils.checkStrictlyPositive(threshold, "threshold");
      this.threshold = threshold;
      return this;
    }

    /**
     * Compute the Euclidean distance between the points.
     *
     * @param point1 the first point
     * @param point2 the second point
     * @return the Euclidean distance
     */
    private static double distance(double[] point1, double[] point2) {
      return Math.sqrt(DISTANCE_FUNCTION.distance(point1, point2));
    }

    /**
     * Compute the Euclidean distance squared between the points.
     *
     * @param point1 the first point
     * @param point2 the second point
     * @return the Euclidean distance
     */
    private static double distanceSquared(double[] point1, double[] point2) {
      return DISTANCE_FUNCTION.distance(point1, point2);
    }

    /**
     * {@inheritDoc}.
     *
     * <p>This method uses only the first 2 indexes in the input point. Higher dimensions are
     * ignored.
     */
    @Override
    public DiggingConcaveHull2d.Builder add(double... point) {
      // Ensure the point is unique
      tree.addIfAbsent(new double[] {point[0], point[1]}, tree.size());
      return this;
    }

    @Override
    public DiggingConcaveHull2d.Builder clear() {
      tree = KdTrees.newIntDoubleKdTree(2);
      return this;
    }

    @Override
    public Hull2d build() {
      final int size = tree.size();
      if (size == 0) {
        return null;
      }

      if (size <= 3) {
        // Simple hull
        final double[] x = new double[size];
        final double[] y = new double[size];
        tree.forEach((p, t) -> {
          x[t] = p[0];
          y[t] = p[1];
        });
        return ConvexHull2d.create(x, y);
      }

      // Get the coordinates
      final IntVector2D[] points = new IntVector2D[size];
      tree.forEach((p, t) -> points[t] = new IntVector2D(p, t));
      // Create initial convex hull. This includes colinear points.
      final CircularList hull = createConvexHull(Arrays.asList(points));
      if (hull == null) {
        // Error creating the hull
        return null;
      }
      return concaveHull(tree, points, hull, threshold);
    }

    /**
     * Creates the convex hull.
     *
     * @param points the points
     * @return the indices of the hull points
     */
    @SuppressWarnings("unchecked")
    private static CircularList createConvexHull(List<? extends Vector2D> points) {
      // Include colinear points in the hull
      final MonotoneChain chain = new MonotoneChain(true, DEFAULT_TOLERANCE);
      ConvexHull2D hull = null;
      try {
        hull = chain.generate((Collection<Vector2D>) points);
      } catch (final ConvergenceException ex) {
        // Ignore
      }

      if (hull == null) {
        return null;
      }

      final Vector2D[] v = hull.getVertices();
      final int size = v.length;
      // No size check as we only call this method with a non-empty point list.

      // Assumes the MonotoneChain uses the points by reference.
      // Thus we can identify the original input point using the stored ID.
      final CircularList list = new CircularList(((IntVector2D) v[0]).id);
      for (int i = 1; i < size; i++) {
        list.insertAfter(((IntVector2D) v[i]).id);
      }
      // Ensure current position is the first point in the hull
      list.next();
      return list;
    }

    /**
     * Compute the concave hull using the digging algorithm. Must be called with a valid convex
     * hull.
     *
     * @param tree the tree
     * @param points the points
     * @param hull the indices of the convex hull
     * @param threshold the edge distance to decision distance threshold for digging
     * @return the hull
     */
    private static Hull2d concaveHull(IntDoubleKdTree tree, IntVector2D[] points, CircularList hull,
        double threshold) {
      // Process each edge in the hull. Check if the edge can be replaced by two edges
      // to an internal point (digging into the hull). Replacing an edge (e1,e2) with
      // two more edges (e1, p) and (p, e2) involves inserting a point after the first
      // edge point. The existing edge is thus processed and the new edges are added to
      // the end of the list ot process. Thus newly added edges are processed after existing
      // hull edges.
      // The hull is a list of points. The list is optimised for insertion and is circular to allow
      // finding edges before and after the current edge, e.g. a circular linked list.
      // The edges to test are put into a FIFO queue. Each item references the points of
      // the edge by ID. If replaced the ID of the first edge point is found in the hull list
      // and the point inserted after.
      final Queue<Edge> queue = createEdgeQueue(points, hull);
      final ActiveList active = createActiveList(points.length, hull);

      // Cache the nearest neighbour of the second edge point.
      // In most cases where no digging is done the nearest neighbour becomes the
      // neighbour of the first point.
      final Neighbour n1 = new Neighbour();
      final Neighbour n2 = new Neighbour();
      final Neighbour k = new Neighbour();
      int n1EdgeIndex = -1;
      final int hullStartIndex = hull.current();
      final double[] com = new double[2];
      final double[] angle = new double[1];

      // Process while we have internal points and remaining edges
      while (active.size() != 0 && queue.size() != 0) {
        // The edge to test
        final Edge edge = queue.remove();
        final double[] e1 = getPoint(points, edge.start);
        final double[] e2 = getPoint(points, edge.end);

        // Using of the closest point as the candidate may create a triangle where
        // another point is inside:
        //
        // @formatter:off
        //  e1 ---------------------- e2
        //   \         p2
        //    \
        //     p
        // @formatter:on
        //
        // Here p2 is not as close. Constructing the edges e1,p and p,e2 will result in
        // p2 being outside the hull.
        //
        // Modify the method to find the candidate with the largest angle between e1-p-e2.
        // This search is not suited to the KD-tree. Thus we find the closest neighbours
        // then search for points within the triangle created to find the point with the largest
        // angle to become the new candidate. This is a compromise for speed as computing the
        // angle to all possible candidates is too expensive.
        //
        // Note:
        // The neighbour should be on the correct side of the line so that:
        // p-e1 or p-e2 does not intersect the existing hull edges e0-e1 or e2-e3
        // Turn angle e0-e1-p > e0-e1-e2 - 0.5
        // i.e. it does not go 180 degrees back on itself from existing direction e1-e2.
        // This is handled at the end when testing for intersection with itself.

        // Reuse cache if possible
        if (n1EdgeIndex != edge.start) {
          tree.nearestNeighbour(e1, DISTANCE_FUNCTION, active::isEnabled, n1::set);
        }
        tree.nearestNeighbour(e2, DISTANCE_FUNCTION, active::isEnabled, n2::set);

        // Closest neighbour:
        // Copy as this may be adjusted.
        if (n1.distance < n2.distance) {
          k.copy(n1);
        } else {
          k.copy(n2);
        }

        // Decision distance
        double dd = Math.sqrt(k.distance);
        if (edge.distance / dd > threshold) {
          hull.advanceTo(edge.start);
          final double[] p = getPoint(points, k.index);

          // Additional checks:
          // * k should not be closer to neighbour edges of (e1, e2) than (e1, e2)
          // This check is not used as it conflicts with the use of the widest angle.
          // A neighbour edge may be further away but have a wider angle:
          // @formatter:off
          //  e0 ----- e1
          //            \
          //             \
          //              e2 - p
          //              |
          //              |
          //              e3
          // @formatter:on
          // It is also unclear what distance to use to measure closeness.
          // Choosing the widest angle makes the most visually pleasing concave hull
          // as it minimises concavity.

          // Note: If the hull is size 3 then e0 == e3 with no effect.
          // final double[] e0 = getPoint(points, hull.peek(-1));
          // final double[] e3 = getPoint(points, hull.peek(2));
          // if (k.distance > distanceSquared(p, e0) || k.distance > distanceSquared(p, e3)) {
          // continue;
          // }

          // * Ensure no other points are within the triangle e1-p-e2.
          // Find the search centre using the centre-of-mass.
          com[0] = (p[0] + e1[0] + e2[0]) / 3;
          com[1] = (p[1] + e1[1] + e2[1]) / 3;

          // Find the search radius using the maximum distance to the vertices.
          final double search =
              Math.max(Math.max(distanceSquared(com, p), distanceSquared(com, e1)),
                  distanceSquared(com, e2));

          // Find any other candidate with a larger angle.
          angle[0] = cosAngle(e1, p, e2);
          tree.findNeighbours(com, search, DISTANCE_FUNCTION, (t, d) -> {
            if (active.isEnabled(t)) {
              // Compute angle. For cosine(angle) lower is wider.
              final double[] p2 = points[t].toArray();
              final double cosAng = cosAngle(e1, p2, e2);
              if (cosAng < angle[0]) {
                k.set(t, d);
                angle[0] = cosAng;
                // Update the point
                p[0] = p2[0];
                p[1] = p2[1];
              }
            }
          });

          // Check the decision distance again
          dd = Math.sqrt(k.distance);
          if (edge.distance / dd <= threshold) {
            continue;
          }

          // Additional checks not deemed useful:
          // - k should not be closer to any other edges of the hull

          // * Angle subtended to neighbour edges must not be bigger.
          // This ensures no digging when a neighbour could have a better digging
          // point from a neighbour edge.
          // Note: This may eliminate a point from digging due to a neighbour edge angle
          // that was vice versa eliminated for the neighbour edge due to distance.
          // @formatter:off
          //
          // e0             e3
          //   \     p     /
          //    \  /   \  /
          //     e1-----e2
          //
          // @formatter:on
          final double[] e0 = getPoint(points, hull.peek(-1));
          final double[] e3 = getPoint(points, hull.peek(2));
          if (angle[0] > Math.min(cosAngle(e0, p, e1), cosAngle(e2, p, e3))) {
            continue;
          }

          // * New edges must not intersect existing hull.
          // This is important when the number of remaining points is low. The point may
          // be far away and joining it will intersect the current hull.
          hull.mark();
          // Start at e1. Move previous until at e2. Test all edges verses (p,e2)
          if (intersects(p, e2, points, hull, edge.end, CircularList::previous)) {
            continue;
          }
          hull.reset();
          // Start at e2. Move next until at e1. Test all edges verses (e1,p)
          hull.next();
          if (intersects(e1, p, points, hull, edge.start, CircularList::next)) {
            continue;
          }
          hull.reset();

          // Insert point into hull and remove from internal points
          hull.insertAfter(k.index);
          active.disable(k.index);
          // Add new edges to the queue
          queue.add(new Edge(edge.start, k.index, distance(e1, p)));
          queue.add(new Edge(k.index, edge.end, distance(p, e2)));
          // Clear cache
          n1EdgeIndex = -1;
        } else {
          // Cache neighbour
          n1.copy(n2);
          n1EdgeIndex = edge.end;
        }
      }

      // Reset hull start point
      hull.advanceTo(hullStartIndex);
      return createHull(points, hull);
    }

    /**
     * Compute the cosine of the angle between the points. A lower cosine corresponds to a higher
     * angle:
     *
     * <pre>
     * v1•v2 = |v1||v2| cos(angle)
     * cos(angle) = v1•v2 / |v1||v2|
     * </pre>
     *
     * @param p1 the point 1
     * @param p2 the point 2
     * @param p3 the point 3
     * @return the absolute of the cosine angle
     */
    private static double cosAngle(double[] p1, double[] p2, double[] p3) {
      // v1•v2 = |v1||v2| cos(angle)
      // Normalised vectors
      final double[] v1 = unitVector(p1, p2);
      final double[] v2 = unitVector(p3, p2);
      // return cos(angle)
      return v1[0] * v2[0] + v1[1] * v2[1];
    }

    /**
     * Create a normalised vector from point 1 to 2. Assumes the points are not identical.
     *
     * @param p1 the point 1
     * @param p2 the point 2
     * @return the unit vector
     */
    private static double[] unitVector(double[] p1, double[] p2) {
      final double dx = p1[0] - p2[0];
      final double dy = p1[1] - p2[1];
      final double length = Math.sqrt(dx * dx + dy * dy);
      return new double[] {dx / length, dy / length};
    }

    /**
     * Advance along the hull from the current position until the next point is the specified end
     * position. For each hull edge test if it intersects with the line between the given points.
     *
     * @param p1 the start of the line
     * @param p2 the end of the line
     * @param points the points
     * @param hull the indices of the convex hull
     * @param end the end hull point
     * @param advance the method to advance to the next hull point
     * @return true if the line intersects
     */
    private static boolean intersects(double[] p1, double[] p2, IntVector2D[] points,
        CircularList hull, int end, ToIntFunction<CircularList> advance) {
      final double x1 = p1[0];
      final double y1 = p1[1];
      final double x2 = p2[0];
      final double y2 = p2[1];
      int e1 = hull.current();
      int e2 = advance.applyAsInt(hull);
      double x4 = points[e1].getX();
      double y4 = points[e1].getY();
      while (e2 != end) {
        final double x3 = x4;
        final double y3 = y4;
        x4 = points[e2].getX();
        y4 = points[e2].getY();
        if (GeometryUtils.testIntersect(x1, y1, x2, y2, x3, y3, x4, y4)) {
          return true;
        }
        // Advance
        e1 = e2;
        e2 = advance.applyAsInt(hull);
      }
      return false;
    }

    /**
     * Creates the queue of hull edges to check.
     *
     * @param points the points
     * @param hull the indices of the convex hull
     * @return the queue
     */
    private static Queue<Edge> createEdgeQueue(IntVector2D[] points, CircularList hull) {
      final Queue<Edge> queue = new LinkedList<>();
      // Assume at least two points in the hull
      final int head = hull.current();
      int prev = head;
      do {
        final int next = hull.next();
        queue.add(new Edge(prev, next, distance(points[prev].toArray(), points[next].toArray())));
        prev = next;
      } while (prev != head);
      return queue;
    }

    /**
     * Creates the list of active (internal) points by excluding current hull points.
     *
     * @param size the size
     * @param hull the hull
     * @return the active list
     */
    private static ActiveList createActiveList(int size, CircularList hull) {
      final ActiveList active = new ActiveList(size);
      active.enableAll();
      hull.forEach(active::disable);
      return active;
    }

    /**
     * Gets the point as a 2D array.
     *
     * @param points the points
     * @param index the index
     * @return the point array
     */
    private static double[] getPoint(IntVector2D[] points, int index) {
      return points[index].toArray();
    }

    /**
     * Creates the hull from the point indexes. This ignores the final point if it is the same as
     * the first point.
     *
     * @param points the points
     * @param hull the indices of the convex hull
     * @return the hull
     */
    private static Hull2d createHull(IntVector2D[] points, CircularList hull) {
      final double[] x = new double[hull.size()];
      final double[] y = new double[hull.size()];
      final int[] i = {0};
      hull.forEach(index -> {
        final IntVector2D p = points[index];
        x[i[0]] = p.getX();
        y[i[0]] = p.getY();
        i[0]++;
      });
      return Hull2d.create(x, y);
    }
  }

  /**
   * Extend the Vector2D class with a associated identifier.
   */
  private static class IntVector2D extends Vector2D {
    private static final long serialVersionUID = 1L;

    /** The id. */
    final int id;

    /**
     * Create an instance.
     *
     * @param point the point
     * @param id the id
     */
    public IntVector2D(double[] point, int id) {
      super(point[0], point[1]);
      this.id = id;
    }
  }

  /**
   * Represent an edge from a start point to an end point.
   */
  private static class Edge {
    /** The start. */
    final int start;
    /** The end. */
    final int end;
    /** The distance. */
    final double distance;

    /**
     * Create an instance.
     *
     * @param start the start
     * @param end the end
     * @param distance the distance
     */
    Edge(int start, int end, double distance) {
      this.start = start;
      this.end = end;
      this.distance = distance;
    }
  }

  /**
   * Represent a neighbour index.
   */
  private static class Neighbour {
    int index;
    double distance;

    /**
     * Sets the index and distance.
     *
     * @param index the index
     * @param distance the distance
     */
    void set(int index, double distance) {
      this.index = index;
      this.distance = distance;
    }

    /**
     * Sets the index and distance.
     *
     * @param source the source
     */
    void copy(Neighbour source) {
      this.index = source.index;
      this.distance = source.distance;
    }
  }

  /**
   * No instances.
   */
  private DiggingConcaveHull2d() {}

  /**
   * Create a new builder.
   *
   * @return the builder
   */
  public static DiggingConcaveHull2d.Builder newBuilder() {
    return new DiggingConcaveHull2d.Builder();
  }

  /**
   * Create a new concave hull from the given coordinates using the given edge distance to decision
   * distance threshold for digging.
   *
   * <p>The hull may be null if it cannot be created (e.g. not enough non-colinear points).
   *
   * @param threshold the edge distance to decision distance threshold for digging.
   * @param x the x coordinates
   * @param y the y coordinates
   * @return the concave hull
   * @throws NullPointerException if the inputs are null
   * @throws ArrayIndexOutOfBoundsException if the arrays are smaller than n
   */
  public static Hull2d create(double threshold, double[] x, double[] y) {
    return create(threshold, x, y, x.length);
  }

  /**
   * Create a new concave hull from the given coordinates using the given edge distance to decision
   * distance threshold for digging.
   *
   * <p>The hull may be null if it cannot be created (e.g. not enough non-colinear points).
   *
   * @param threshold the edge distance to decision distance threshold for digging.
   * @param x the x coordinates
   * @param y the y coordinates
   * @param n the number of coordinates
   * @return the concave hull
   * @throws NullPointerException if the inputs are null
   * @throws ArrayIndexOutOfBoundsException if the arrays are smaller than n
   */
  public static Hull2d create(double threshold, double[] x, double[] y, int n) {
    final Builder builder = newBuilder().setThreshold(threshold);
    for (int i = 0; i < n; i++) {
      builder.add(x[i], y[i]);
    }
    return builder.build();
  }
}
