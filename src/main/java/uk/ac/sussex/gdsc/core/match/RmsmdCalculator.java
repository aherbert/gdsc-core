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

package uk.ac.sussex.gdsc.core.match;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

/**
 * Calculates the root mean square minimum distance between two sets of points.
 *
 * <blockquote> Y. Sun, "Root mean square minimum distance as a quality metric for stochastic
 * optical localization nanoscopy images," Sci. Reports, vol. 8, no. 1, pp. 17211, Nov. 2018.
 * </blockquote>
 */
public final class RmsmdCalculator {
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
    return rmsmd(a, b, RmsmdCalculator::distanceSquared);
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

    return rmsmd(cA, cB, RmsmdCalculator::distanceSquared);
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
    return rmsmd(a, b, toCoordinatesA, toCoordinatesB, RmsmdCalculator::distanceSquared);
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
   * Compute the sum of squared distances in each dimension for the square distance. This is the
   * square of the Euclidean distance between the two points.
   *
   * @param d1 the first point
   * @param d2 the second point
   * @return the squared Euclidean distance
   */
  static double distanceSquared(double[] d1, double[] d2) {
    double sum = 0;
    for (int i = 0; i < d1.length; i++) {
      final double d = d1[i] - d2[i];
      sum += d * d;
    }
    return sum;
  }

  /**
   * Sum the minimum distances between each point in set 1 to any point in set 2.
   *
   * @param points1 the first set of points
   * @param points2 the second set of points
   * @param distanceFunction the distance function
   * @return the sum
   */
  private static double sumMinimumDistances(double[][] points1, double[][] points2,
      ToDoubleBiFunction<double[], double[]> distanceFunction) {
    // TODO
    // All-vs-all will not scale well.
    // If the collection of points 2 is large then put into a KD-tree for efficient
    // nearest neighbour search for each point 1.

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
}
