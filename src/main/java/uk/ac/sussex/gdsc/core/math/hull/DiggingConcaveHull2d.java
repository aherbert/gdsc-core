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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.euclidean.twod.hull.ConvexHull2D;
import org.apache.commons.math3.geometry.euclidean.twod.hull.MonotoneChain;
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
 * <blockquote>Park and Oh (2012) <br/> A New Concave Hull Algorithm and Concaveness Measure for
 * n-dimensional Datasets. <br/>Journal of Information Science and Engineering
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
      final List<IntVector2D> hull = createConvexHull(Arrays.asList(points));
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
     * @return the hull points
     */
    @SuppressWarnings("unchecked")
    private static List<IntVector2D> createConvexHull(List<? extends Vector2D> points) {
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
      final LinkedList<IntVector2D> list = new LinkedList<>();
      for (int i = 0; i < size; i++) {
        list.add((IntVector2D) v[i]);
      }
      return list;
    }


    /**
     * Compute the concave hull using the digging algorithm. Must be called with a valid convex
     * hull.
     *
     * @param tree the tree
     * @param points the points
     * @param hull the initial convex hull
     * @param threshold the edge distance to decision distance threshold for digging
     * @return the hull
     */
    private static Hull2d concaveHull(IntDoubleKdTree tree, IntVector2D[] points,
        List<IntVector2D> hull, double threshold) {
      // Process each edge in the hull. Check if the edge can be replaced by two edges
      // to an internal point (digging into the hull). Replacing an edge (e1,e2) with
      // two more edges (e1, p) and (p, e2) involves inserting a point after the first
      // edge point. The existing edge is thus processed and the new edges are added to
      // the end of the list ot process. Thus newly added edges are processed after existing
      // hull edges.
      // The hull is a list of points. The list is optimised for insertion, e.g. LinkedList.
      // The edges to test are put into a FIFO queue. Each item references the points of
      // the edge by ID. If replaced the ID of the first edge point is found in the hull list
      // and the point inserted after.
      final Queue<Edge> queue = createEdgeQueue(hull);
      final ActiveList active = createActiveList(points.length, hull);

      // Cache the nearest neighbour of the second edge point.
      // In most cases where no digging is done the nearest neighbour becomes the
      // neighbour of the first point.
      final Neighbour n1 = new Neighbour();
      final Neighbour n2 = new Neighbour();
      int n1EdgeIndex = -1;

      // Process while we have internal points and remaining edges
      while (active.size() != 0 && queue.size() != 0) {
        // The edge to test
        final Edge edge = queue.remove();
        final double[] e1 = getPoint(points, edge.start);
        final double[] e2 = getPoint(points, edge.end);
        // Reuse cache if possible
        if (n1EdgeIndex != edge.start) {
          tree.nearestNeighbour(e1, DISTANCE_FUNCTION, active::isEnabled, n1::set);
        }
        tree.nearestNeighbour(e2, DISTANCE_FUNCTION, active::isEnabled, n2::set);

        // Decision distance of closest neighbour
        final Neighbour k = n1.distance < n2.distance ? n1 : n2;
        final double dd = Math.sqrt(k.distance);
        if (edge.distance / dd > threshold) {
          // Insert point into hull and remove from internal points
          insertAfter(hull, edge.start, points[k.index]);
          active.disable(k.index);
          // Add new edges to the queue
          final double[] p = getPoint(points, k.index);
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

      return createHull(hull);
    }

    /**
     * Creates the queue of hull edges to check.
     *
     * @param hull the hull points
     * @return the queue
     */
    private static Queue<Edge> createEdgeQueue(List<IntVector2D> hull) {
      final Queue<Edge> queue = new LinkedList<>();
      final Iterator<IntVector2D> itr = hull.iterator();
      // Assume at least one point in the hull
      IntVector2D prev = itr.next();
      while (itr.hasNext()) {
        final IntVector2D next = itr.next();
        queue.add(new Edge(prev.id, next.id, distance(prev.toArray(), next.toArray())));
        prev = next;
      }
      // Circular wrap
      final IntVector2D next = hull.get(0);
      queue.add(new Edge(prev.id, next.id, distance(prev.toArray(), next.toArray())));
      return queue;
    }

    /**
     * Creates the list of active (internal) points by excluding current hull points.
     *
     * @param size the size
     * @param hull the hull
     * @return the active list
     */
    private static ActiveList createActiveList(int size, List<IntVector2D> hull) {
      final ActiveList active = new ActiveList(size);
      active.enableAll();
      hull.forEach(v -> active.disable(v.id));
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
     * Insert the point into the hull after the point with the specified ID.
     *
     * @param hull the hull
     * @param id the id
     * @param point the point to insert
     */
    private static void insertAfter(List<IntVector2D> hull, int id, IntVector2D point) {
      final ListIterator<IntVector2D> itr = hull.listIterator();
      // This assumes the hull will contain the specified id
      while (itr.next().id != id) {
        // Advancing to the correct point
      }
      itr.add(point);
    }

    /**
     * Creates the hull from the point indexes. This ignores the final point if it is the same as
     * the first point.
     *
     * @param points the points
     * @return the hull
     */
    private static Hull2d createHull(List<IntVector2D> points) {
      final double[] x = new double[points.size()];
      final double[] y = new double[points.size()];
      int i = 0;
      for (IntVector2D p : points) {
        x[i] = p.getX();
        y[i] = p.getY();
        i++;
      }
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
