/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2023 Alex Herbert
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

package uk.ac.sussex.gdsc.core.match;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import uk.ac.sussex.gdsc.core.data.VisibleForTesting;
import uk.ac.sussex.gdsc.core.trees.DoubleDistanceFunction;
import uk.ac.sussex.gdsc.core.trees.DoubleDistanceFunctions;
import uk.ac.sussex.gdsc.core.trees.DoubleKdTree;
import uk.ac.sussex.gdsc.core.trees.KdTrees;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

/**
 * Calculates the root mean square minimum distance between two sets of points.
 *
 * <blockquote> Y. Sun, "Root mean square minimum distance as a quality metric for stochastic
 * optical localization nanoscopy images," Sci. Reports, vol. 8, no. 1, pp. 17211, Nov. 2018.
 * </blockquote>
 */
public final class RmsmdCalculator {
  /**
   * The threshold to switch to using a KD-tree. This is set to a size where construction time of
   * the tree is small relative to the all-vs-all comparison time allowing the tree to have an
   * advantage.
   */
  private static final int SIZE_THRESHOLD_KD_TREE = 512;
  /**
   * The threshold to switch to using a KD-tree for a search set of a given size. This is set
   * heuristically. It is only worth constructing the tree if there are a several points in the
   * search.
   *
   * <p>This has been set based on testing on uniform 2D data.
   */
  private static final int SIZE_THRESHOLD_KD_TREE_SEARCH = 64;

  /**
   * The default square Euclidean distance function.
   *
   * <p>Create an instance to allow detection of the default.
   */
  private static final ToDoubleBiFunction<double[], double[]> DEFAULT_DISTANCE_FUNCTION =
      DoubleDistanceFunctions.SQUARED_EUCLIDEAN_ND::distance;

  /** No public construction. */
  private RmsmdCalculator() {}

  /**
   * Calculate root mean square minimum distance (RMSMD) between two sets of points.
   *
   * <p>Uses the sum of squared distances in each dimension for the square distance. This is the
   * square of the Euclidean distance between the two points.
   *
   * @param a the set A
   * @param b the set B
   * @return RMSMD
   * @throws IllegalArgumentException if either set is empty
   * @throws IndexOutOfBoundsException if points have mismatched dimensions
   */
  public static double rmsmd(Collection<double[]> a, Collection<double[]> b) {
    return rmsmd(a, b, DEFAULT_DISTANCE_FUNCTION);
  }

  /**
   * Calculate root mean square minimum distance (RMSMD) between two sets of points.
   *
   * <p>Uses the sum of squared distances in each dimension for the square distance. This is the
   * square of the Euclidean distance between the two points.
   *
   * @param a the set A
   * @param b the set B
   * @param distanceFunction the distance function to compute the square distance
   * @return RMSMD
   * @throws IllegalArgumentException if either set is empty
   * @throws IndexOutOfBoundsException if points have mismatched dimensions
   */
  public static double rmsmd(Collection<double[]> a, Collection<double[]> b,
      ToDoubleBiFunction<double[], double[]> distanceFunction) {
    // Convert to coordinates.
    final double[][] cA = a.toArray(new double[0][]);
    final double[][] cB = b.toArray(new double[0][]);

    return rmsmd(cA, cB, DEFAULT_DISTANCE_FUNCTION);
  }

  /**
   * Calculate root mean square minimum distance (RMSMD) between two sets of points.
   *
   * <p>Uses the sum of squared distances in each dimension for the square distance. This is the
   * square of the Euclidean distance between the two points.
   *
   * @param <U> the type of the first set
   * @param a the set A
   * @param b the set B
   * @param toCoordinates the function to extract the coordinates
   * @return RMSMD
   * @throws IllegalArgumentException if either set is empty
   * @throws IndexOutOfBoundsException if points have mismatched dimensions
   */
  public static <U> double rmsmd(Collection<U> a, Collection<U> b,
      Function<U, double[]> toCoordinates) {
    return rmsmd(a, b, toCoordinates, toCoordinates);
  }

  /**
   * Calculate root mean square minimum distance (RMSMD) between two sets of points.
   *
   * <p>Uses the sum of squared distances in each dimension for the square distance. This is the
   * square of the Euclidean distance between the two points.
   *
   * @param <U> the type of the first set
   * @param <V> the type of the second set
   * @param a the set A
   * @param b the set B
   * @param toCoordinatesA the function to extract the coordinates for A
   * @param toCoordinatesB the function to extract the coordinates for B
   * @return RMSMD
   * @throws IllegalArgumentException if either set is empty
   * @throws IndexOutOfBoundsException if points have mismatched dimensions
   */
  public static <U, V> double rmsmd(Collection<U> a, Collection<V> b,
      Function<U, double[]> toCoordinatesA, Function<V, double[]> toCoordinatesB) {
    return rmsmd(a, b, toCoordinatesA, toCoordinatesB, DEFAULT_DISTANCE_FUNCTION);
  }

  /**
   * Calculate root mean square minimum distance (RMSMD) between two sets of points.
   *
   * @param <U> the type of the first set
   * @param <V> the type of the second set
   * @param a the set A
   * @param b the set B
   * @param toCoordinatesA the function to extract the coordinates for A
   * @param toCoordinatesB the function to extract the coordinates for B
   * @param distanceFunction the distance function to compute the square distance
   * @return RMSMD
   * @throws IllegalArgumentException if either set is empty
   * @throws IndexOutOfBoundsException if points have mismatched dimensions
   */
  public static <U, V> double rmsmd(Collection<U> a, Collection<V> b,
      Function<U, double[]> toCoordinatesA, Function<V, double[]> toCoordinatesB,
      ToDoubleBiFunction<double[], double[]> distanceFunction) {
    // Convert to coordinates.
    final double[][] cA = a.stream().map(toCoordinatesA).toArray(double[][]::new);
    final double[][] cB = b.stream().map(toCoordinatesB).toArray(double[][]::new);

    return rmsmd(cA, cB, distanceFunction);
  }

  /**
   * Calculate root mean square minimum distance (RMSMD) between two sets of points.
   *
   * @param a the set A
   * @param b the set B
   * @param distanceFunction the distance function to compute the square distance
   * @return RMSMD
   * @throws IllegalArgumentException if either set is empty
   * @throws IndexOutOfBoundsException if points have mismatched dimensions
   */
  private static double rmsmd(double[][] a, double[][] b,
      ToDoubleBiFunction<double[], double[]> distanceFunction) {
    final int sizeA = a.length;
    ValidationUtils.checkStrictlyPositive(sizeA, "a size");
    final int sizeB = b.length;
    ValidationUtils.checkStrictlyPositive(sizeB, "b size");

    // Get the sum of the minimum distances
    final double sumA = sumMinimumDistances(a, b, distanceFunction);
    final double sumB = sumMinimumDistances(b, a, distanceFunction);

    return Math.sqrt((sumA + sumB) / (sizeA + sizeB));
  }

  /**
   * Sum the minimum distances between each point in set 1 to any point in set 2.
   *
   * @param points1 the first set of points
   * @param points2 the second set of points
   * @param distanceFunction the distance function
   * @return the sum
   */
  @VisibleForTesting
  static double sumMinimumDistances(double[][] points1, double[][] points2,
      ToDoubleBiFunction<double[], double[]> distanceFunction) {
    // All-vs-all will not scale well.
    // If the collection of points 2 is large then put into a KD-tree for efficient
    // nearest neighbour search for each point 1.
    if (useKdTree(points1.length, points2.length)) {
      final DoubleDistanceFunction df =
          createDoubleDistanceFunction(distanceFunction, points2[0].length);
      return sumMinimumDistancesKdTree(points1, points2, df);
    }
    return sumMinimumDistancesAllVsAll(points1, points2, distanceFunction);
  }

  /**
   * Return true if the KD tree is recommended.
   *
   * @param searchSize the search size
   * @param treeSize the tree size
   * @return true if a KD-tree is recommended
   */
  private static boolean useKdTree(int searchSize, int treeSize) {
    return treeSize >= SIZE_THRESHOLD_KD_TREE && searchSize >= SIZE_THRESHOLD_KD_TREE_SEARCH;
  }

  /**
   * Sum the minimum distances between each point in set 1 to any point in set 2 using an all-vs-all
   * comparison.
   *
   * @param points1 the first set of points
   * @param points2 the second set of points
   * @param distanceFunction the distance function
   * @return the sum
   */
  @VisibleForTesting
  static double sumMinimumDistancesAllVsAll(double[][] points1, double[][] points2,
      ToDoubleBiFunction<double[], double[]> distanceFunction) {
    double sum = 0;
    for (final double[] p1 : points1) {
      double min = distanceFunction.applyAsDouble(p1, points2[0]);
      for (int i = 1; i < points2.length; i++) {
        final double d = distanceFunction.applyAsDouble(p1, points2[i]);
        if (d < min) {
          min = d;
        }
      }
      sum += min;
    }
    return sum;
  }

  /**
   * Sum the minimum distances between each point in set 1 to any point in set 2 using a KD-Tree to
   * store all points from the second set and compute the minimum distance to each in the first set.
   *
   * @param points1 the first set of points
   * @param points2 the second set of points
   * @param distanceFunction the distance function
   * @return the sum
   */
  @VisibleForTesting
  static double sumMinimumDistancesKdTree(double[][] points1, double[][] points2,
      DoubleDistanceFunction distanceFunction) {
    // Put all points2 in a KD-Tree
    final DoubleKdTree tree = KdTrees.newDoubleKdTree(points2[0].length);
    for (final double[] p : points2) {
      tree.add(p);
    }

    double sum = 0;
    for (final double[] p1 : points1) {
      sum += tree.nearestNeighbour(p1, distanceFunction, null);
    }
    return sum;
  }

  /**
   * Creates the KD-tree distance function from the standard distance function.
   *
   * @param distanceFunction the distance function
   * @param dimensions the dimensions
   * @return the double distance function
   */
  private static DoubleDistanceFunction createDoubleDistanceFunction(
      final ToDoubleBiFunction<double[], double[]> distanceFunction, int dimensions) {
    // Check if the function is the default
    if (distanceFunction == DEFAULT_DISTANCE_FUNCTION) {
      return DoubleDistanceFunctions.squaredEuclidean(dimensions);
    }

    // This relies on the KD-tree search not using the distance function concurrently
    return new DoubleDistanceFunction() {
      private final double[] tmp = new double[dimensions];

      @Override
      public double distanceToRectangle(double[] point, double[] min, double[] max) {
        // Create a fake point at the boundary of the region.
        // Thus the distance between the point and the region is the distance to the boundary.
        for (int i = 0; i < dimensions; i++) {
          if (point[i] > max[i]) {
            tmp[i] = max[i];
          } else {
            tmp[i] = point[i] < min[i] ? min[i] : point[i];
          }
        }
        return distanceFunction.applyAsDouble(point, tmp);
      }

      @Override
      public double distance(double[] p1, double[] p2) {
        return distanceFunction.applyAsDouble(p1, p2);
      }
    };
  }
}
