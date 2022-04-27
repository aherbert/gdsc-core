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
 * Copyright (C) 2011 - 2022 Alex Herbert
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

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;
import java.util.function.IntFunction;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import uk.ac.sussex.gdsc.core.data.VisibleForTesting;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

/**
 * Calculates the closest pair of 2D coordinates.
 *
 * <p>Uses the divide-and-conquer algorithm which recursively divides the points in half based only
 * on the x-coordinate. The closest pair from the ultimate sets are found. The sub-results are
 * merged by considering only those points on the boundary between sets.
 *
 * <p>Complexity is O(n*log(n)).
 */
public final class ClosestPairCalculator {
  /** The size point to switch from the all-vs-all to the partitioned algorithm. */
  public static final int ALGORITHM_SWITCH = 512;

  /** No public construction. */
  private ClosestPairCalculator() {}

  /**
   * Calculates the closest pair of points.
   *
   * <p>The algorithm is chosen based on the number of points to minimise run-time.
   *
   * <pre>
   * {@code
   * Point2D[] data = ...;
   *
   * Pair<Point2D, Point2D> = ClosestPairCalculator.closestPair(data);
   * }
   * </pre>
   *
   * @param list the list of points
   * @return the closest pair
   * @throws IllegalArgumentException if the number of points is less than 2
   */
  public static Pair<Point2D, Point2D> closestPair(Point2D[] list) {
    return ArrayUtils.getLength(list) < ALGORITHM_SWITCH ? closestPairAllVsAll(list)
        : closestPairPartitioned(list);
  }

  /**
   * Calculates the closest pair of points.
   *
   * <p>The algorithm is chosen based on the number of points to minimise run-time.
   *
   * <pre>
   * {@code
   * Point2D[] data = ...;
   *
   * Pair<Point2D, Point2D> = ClosestPairCalculator.closestPair(Arrays.asList(data),
   *   Point2D::getX, Point2D::getY);
   * }
   * </pre>
   *
   * @param <T> the generic type
   * @param points the points
   * @param getX the function to get the X coordinate
   * @param getY the function to get the Y coordinate
   * @return the closest pair
   * @throws IllegalArgumentException if the number of points is less than 2
   */
  public static <T> Pair<T, T> closestPair(Collection<T> points, ToDoubleFunction<T> getX,
      ToDoubleFunction<T> getY) {
    return getSize(points) < ALGORITHM_SWITCH ? closestPairAllVsAll(points, getX, getY)
        : closestPairPartitioned(points, getX, getY);
  }

  /**
   * Calculates the closest pair of points using an all-vs-all distance calculation.
   *
   * <p>Complexity is O(n*n)).
   *
   * <pre>
   * {@code
   * Point2D[] data = ...;
   *
   * Pair<Point2D, Point2D> = ClosestPairCalculator.closestPairAllVsAll(data);
   * }
   * </pre>
   *
   * @param list the list of points
   * @return the closest pair
   * @throws IllegalArgumentException if the number of points is less than 2
   */
  public static Pair<Point2D, Point2D> closestPairAllVsAll(Point2D[] list) {
    final int size = ArrayUtils.getLength(list);
    checkSize(size);

    double min = Double.POSITIVE_INFINITY;
    int ii = 0;
    int jj = 1;
    for (int i = 0; i < list.length; i++) {
      for (int j = i + 1; j < list.length; j++) {
        final double distance = list[i].distanceSq(list[j]);
        if (distance < min) {
          min = distance;
          ii = i;
          jj = j;
        }
      }
    }
    return Pair.of(list[ii], list[jj]);
  }

  /**
   * Calculates the closest pair of points using an all-vs-all distance calculation.
   *
   * <p>Complexity is O(n*n)).
   *
   * <pre>
   * {@code
   * Point2D[] data = ...;
   *
   * Pair<Point2D, Point2D> = ClosestPairCalculator.closestPairAllVsAll(Arrays.asList(data),
   *   Point2D::getX, Point2D::getY);
   * }
   * </pre>
   *
   * @param <T> the generic type
   * @param points the points
   * @param getX the function to get the X coordinate
   * @param getY the function to get the Y coordinate
   * @return the closest pair
   * @throws IllegalArgumentException if the number of points is less than 2
   */
  public static <T> Pair<T, T> closestPairAllVsAll(Collection<T> points, ToDoubleFunction<T> getX,
      ToDoubleFunction<T> getY) {
    ValidationUtils.checkNotNull(points, "Points must not be null");
    ValidationUtils.checkNotNull(getX, "Function to get X coordinate must not be null");
    ValidationUtils.checkNotNull(getY, "Function to get Y coordinate must not be null");

    final int size = points.size();
    checkSize(size);

    // Ensure random access to the points
    final IntFunction<T> list = toRandomAccess(points);

    double min = Double.POSITIVE_INFINITY;
    int ii = 0;
    int jj = 1;
    for (int i = 0; i < size; i++) {
      final T p1 = list.apply(i);
      final double x1 = getX.applyAsDouble(p1);
      final double y1 = getY.applyAsDouble(p1);
      for (int j = i + 1; j < size; j++) {
        final T p2 = list.apply(j);
        final double distance = squaredDistance(x1, getX.applyAsDouble(p2))
            + squaredDistance(y1, getY.applyAsDouble(p2));
        if (distance < min) {
          min = distance;
          ii = i;
          jj = j;
        }
      }
    }
    return Pair.of(list.apply(ii), list.apply(jj));
  }

  /**
   * Calculates the closest pair of points using a partition algorithm.
   *
   * <p>The algorithm recursively divides the points in half based only on the x-coordinate. The
   * closest pair from the ultimate sets are found. The sub-results are merged by considering only
   * those points on the boundary between sets.
   *
   * <p>Complexity is O(n*log(n)).
   *
   * <pre>
   * {@code
   * Point2D[] data = ...;
   *
   * Pair<Point2D, Point2D> = ClosestPairCalculator.closestPairPartitioned(data);
   * }
   * </pre>
   *
   * @param list the list of points
   * @return the closest pair
   * @throws IllegalArgumentException if the number of points is less than 2
   */
  public static Pair<Point2D, Point2D> closestPairPartitioned(Point2D[] list) {
    final int size = ArrayUtils.getLength(list);
    checkSize(size);

    // Ordered indices of the input list
    final Integer[] indices = IntStream.range(0, size).boxed().toArray(Integer[]::new);

    // Order by x
    final double[] values = new double[size];
    Arrays.setAll(values, i -> list[i].getX());
    Arrays.sort(indices, (i, j) -> Double.compare(values[i], values[j]));
    final int[] indicesX = Arrays.stream(indices).mapToInt(Integer::intValue).toArray();

    // Order by y
    Arrays.setAll(values, i -> list[i].getY());
    Arrays.sort(indices, (i, j) -> Double.compare(values[i], values[j]));
    final int[] indicesY = Arrays.stream(indices).mapToInt(Integer::intValue).toArray();

    final int[] leftSet = new int[size];
    final Assignment pair = findClosestPair(list, indicesX, 0, size, indicesY, leftSet, 1);
    return Pair.of(list[pair.getTargetId()], list[pair.getPredictedId()]);
  }

  /**
   * Calculates the closest pair of points using a partition algorithm.
   *
   * <p>The algorithm recursively divides the points in half based only on the x-coordinate. The
   * closest pair from the ultimate sets are found. The sub-results are merged by considering only
   * those points on the boundary between sets.
   *
   * <p>Complexity is O(n*log(n)).
   *
   * <pre>
   * {@code
   * Point2D[] data = ...;
   *
   * Pair<Point2D, Point2D> = ClosestPairCalculator.closestPairPartitioned(Arrays.asList(data),
   *   Point2D::getX, Point2D::getY);
   * }
   * </pre>
   *
   * @param <T> the generic type
   * @param points the points
   * @param getX the function to get the X coordinate
   * @param getY the function to get the Y coordinate
   * @return the closest pair
   * @throws IllegalArgumentException if the number of points is less than 2
   */
  public static <T> Pair<T, T> closestPairPartitioned(Collection<T> points,
      ToDoubleFunction<T> getX, ToDoubleFunction<T> getY) {
    ValidationUtils.checkNotNull(points, "Points must not be null");
    ValidationUtils.checkNotNull(getX, "Function to get X coordinate must not be null");
    ValidationUtils.checkNotNull(getY, "Function to get Y coordinate must not be null");

    final int size = points.size();
    checkSize(size);

    // Ensure random access to the points
    final IntFunction<T> list = toRandomAccess(points);

    // Ordered indices of the input list
    final Integer[] indices = IntStream.range(0, size).boxed().toArray(Integer[]::new);

    // Order by x
    final double[] values = new double[size];
    Arrays.setAll(values, i -> getX.applyAsDouble(list.apply(i)));
    Arrays.sort(indices, (i, j) -> Double.compare(values[i], values[j]));
    final int[] indicesX = Arrays.stream(indices).mapToInt(Integer::intValue).toArray();

    // Order by y
    Arrays.setAll(values, i -> getY.applyAsDouble(list.apply(i)));
    Arrays.sort(indices, (i, j) -> Double.compare(values[i], values[j]));
    final int[] indicesY = Arrays.stream(indices).mapToInt(Integer::intValue).toArray();

    final int[] leftSet = new int[size];
    final Assignment pair =
        findClosestPair(list, getX, getY, indicesX, 0, size, indicesY, leftSet, 1);
    return Pair.of(list.apply(pair.getTargetId()), list.apply(pair.getPredictedId()));
  }

  /**
   * Find the closest pair using a divide and conquer algorithm.
   *
   * @param list the list
   * @param indicesX the indices sorted by the X coordinate
   * @param low the low point to use in the X-indices array (inclusive)
   * @param high the high point to use in the X-indices array (exclusive)
   * @param indicesY the same indices between low and high from the X-indices array sorted by the Y
   *        coordinate
   * @param leftSet working space to store members of the left set
   * @param setId the working set Id
   * @return the assignment
   */
  private static Assignment findClosestPair(Point2D[] list, int[] indicesX, int low, int high,
      int[] indicesY, int[] leftSet, int setId) {
    final int size = high - low;

    if (size == 3) {
      return closestPairOf3(list, indicesX[low], indicesX[low + 1], indicesX[low + 2]);
    }

    if (size == 2) {
      return createPair(list, indicesX[low], indicesX[low + 1]);
    }

    // Divide into the new sets left and right based on X coordinate
    final int mid = (low + high) >>> 1;
    for (int i = low; i < mid; i++) {
      leftSet[indicesX[i]] = setId;
    }

    // Build the new sets sorted by Y coordinate
    final int[] leftIndicesY = new int[mid - low];
    final int[] rightIndicesY = new int[high - mid];
    int left = 0;
    int right = 0;
    for (final int id : indicesY) {
      if (leftSet[id] == setId) {
        leftIndicesY[left++] = id;
      } else {
        rightIndicesY[right++] = id;
      }
    }

    final Assignment leftResult =
        findClosestPair(list, indicesX, low, mid, leftIndicesY, leftSet, 2 * setId);
    final Assignment rightResult =
        findClosestPair(list, indicesX, mid, high, rightIndicesY, leftSet, 2 * setId + 1);
    final Assignment result =
        leftResult.getDistance() < rightResult.getDistance() ? leftResult : rightResult;

    // Find points in the left and right sets within the closest distance of the X boundary.
    // We can recycle indicesY for this purpose.
    left = 0;
    final double midX = list[indicesX[mid]].getX();
    for (final int id : indicesY) {
      if (squaredDistance(list[id].getX(), midX) < result.getDistance()) {
        indicesY[left++] = id;
      }
    }

    return closestPairAtBoundary(list, indicesY, left, result);
  }

  /**
   * Find the closest pair using a divide and conquer algorithm.
   *
   * @param <T> the generic type
   * @param list the list
   * @param getX the function to get the X coordinate
   * @param getY the function to get the Y coordinate
   * @param indicesX the indices sorted by the X coordinate
   * @param low the low point to use in the X-indices array (inclusive)
   * @param high the high point to use in the X-indices array (exclusive)
   * @param indicesY the same indices between low and high from the X-indices array sorted by the Y
   *        coordinate
   * @param leftSet working space to store members of the left set
   * @param setId the working set Id
   * @return the assignment
   */
  private static <T> Assignment findClosestPair(IntFunction<T> list, ToDoubleFunction<T> getX,
      ToDoubleFunction<T> getY, int[] indicesX, int low, int high, int[] indicesY, int[] leftSet,
      int setId) {
    final int size = high - low;

    if (size == 3) {
      return closestPairOf3(list, getX, getY, indicesX[low], indicesX[low + 1], indicesX[low + 2]);
    }

    if (size == 2) {
      return createPair(list, getX, getY, indicesX[low], indicesX[low + 1]);
    }

    // Divide into the new sets left and right based on X coordinate
    final int mid = (low + high) >>> 1;
    for (int i = low; i < mid; i++) {
      leftSet[indicesX[i]] = setId;
    }

    // Build the new sets sorted by Y coordinate
    final int[] leftIndicesY = new int[mid - low];
    final int[] rightIndicesY = new int[high - mid];
    int left = 0;
    int right = 0;
    for (final int id : indicesY) {
      if (leftSet[id] == setId) {
        leftIndicesY[left++] = id;
      } else {
        rightIndicesY[right++] = id;
      }
    }

    final Assignment leftResult =
        findClosestPair(list, getX, getY, indicesX, low, mid, leftIndicesY, leftSet, 2 * setId);
    final Assignment rightResult = findClosestPair(list, getX, getY, indicesX, mid, high,
        rightIndicesY, leftSet, 2 * setId + 1);
    final Assignment result =
        leftResult.getDistance() < rightResult.getDistance() ? leftResult : rightResult;

    // Find points in the left and right sets within the closest distance of the X boundary.
    // We can recycle indicesY for this purpose.
    left = 0;
    final double midX = getX.applyAsDouble(list.apply(indicesX[mid]));
    for (int i = 0; i < indicesY.length; i++) {
      final int id = indicesY[i];
      if (squaredDistance(getX.applyAsDouble(list.apply(id)), midX) < result.getDistance()) {
        indicesY[left++] = id;
      }
    }

    return closestPairAtBoundary(list, getX, getY, indicesY, left, result);
  }

  /**
   * Find the closest pair of points among points 1, 2 and 3.
   *
   * @param list the list of points
   * @param index1 the first index
   * @param index2 the second index
   * @param index3 the third index
   * @return the assignment
   */
  @VisibleForTesting
  static Assignment closestPairOf3(Point2D[] list, int index1, int index2, int index3) {
    final Point2D p1 = list[index1];
    final Point2D p2 = list[index2];
    final Point2D p3 = list[index3];
    final double d12 = squaredDistance(p1, p2);
    final double d23 = squaredDistance(p2, p3);
    final double d13 = squaredDistance(p1, p3);
    if (d12 < d23) {
      if (d12 < d13) {
        return new ImmutableAssignment(index1, index2, d12);
      }
      return new ImmutableAssignment(index1, index3, d13);
    }
    if (d23 < d13) {
      return new ImmutableAssignment(index2, index3, d23);
    }
    return new ImmutableAssignment(index1, index3, d13);
  }

  /**
   * Find the closest pair of points among points 1, 2 and 3.
   *
   * @param <T> the generic type
   * @param list the list of points
   * @param getX the function to get the X coordinate
   * @param getY the function to get the X coordinate
   * @param index1 the first index
   * @param index2 the second index
   * @param index3 the third index
   * @return the assignment
   */
  @VisibleForTesting
  static <T> Assignment closestPairOf3(IntFunction<T> list, ToDoubleFunction<T> getX,
      ToDoubleFunction<T> getY, int index1, int index2, int index3) {
    final T p1 = list.apply(index1);
    final T p2 = list.apply(index2);
    final T p3 = list.apply(index3);
    final double d12 = squaredDistance(p1, p2, getX, getY);
    final double d23 = squaredDistance(p2, p3, getX, getY);
    final double d13 = squaredDistance(p1, p3, getX, getY);
    if (d12 < d23) {
      if (d12 < d13) {
        return new ImmutableAssignment(index1, index2, d12);
      }
      return new ImmutableAssignment(index1, index3, d13);
    }
    if (d23 < d13) {
      return new ImmutableAssignment(index2, index3, d23);
    }
    return new ImmutableAssignment(index1, index3, d13);
  }

  /**
   * Create the closest pair of points 1 and 2.
   *
   * @param list the list of points
   * @param index1 the first index
   * @param index2 the second index
   * @return the assignment
   */
  private static Assignment createPair(Point2D[] list, int index1, int index2) {
    final Point2D p1 = list[index1];
    final Point2D p2 = list[index2];
    final double d12 = squaredDistance(p1, p2);
    return new ImmutableAssignment(index1, index2, d12);
  }

  /**
   * Create the closest pair of points 1 and 2.
   *
   * @param <T> the generic type
   * @param list the list of points
   * @param getX the function to get the X coordinate
   * @param getY the function to get the X coordinate
   * @param index1 the first index
   * @param index2 the second index
   * @return the assignment
   */
  private static <T> Assignment createPair(IntFunction<T> list, ToDoubleFunction<T> getX,
      ToDoubleFunction<T> getY, int index1, int index2) {
    final T p1 = list.apply(index1);
    final T p2 = list.apply(index2);
    final double d12 = squaredDistance(p1, p2, getX, getY);
    return new ImmutableAssignment(index1, index2, d12);
  }

  /**
   * Find the closest pair among the boundary points.
   *
   * @param list the list
   * @param indicesY the indices sorted by the Y coordinate
   * @param count the number of indices
   * @param result the current closest pair
   * @return the closest pair
   */
  private static Assignment closestPairAtBoundary(Point2D[] list, int[] indicesY, int count,
      Assignment result) {
    // This may double up testing right-vs-right and left-vs-left but the minimum distance
    // is already the min distance from those sets so these comparisons are few.
    for (int i = 0; i < count; i++) {
      final Point2D p1 = list[indicesY[i]];
      final double x1 = p1.getX();
      final double y1 = p1.getY();
      for (int j = i + 1; j < count; j++) {
        final Point2D p2 = list[indicesY[j]];
        final double dy2 = squaredDistance(y1, p2.getY());
        // Test the point is closer in Y
        if (dy2 >= result.getDistance()) {
          // Since these are ordered by Y nothing else will be closer
          break;
        }
        final double dx2 = squaredDistance(x1, p2.getX());
        // Test the distance
        final double distance = dx2 + dy2;
        if (distance < result.getDistance()) {
          result = new ImmutableAssignment(indicesY[i], indicesY[j], distance);
        }
      }
    }
    return result;
  }

  /**
   * Find the closest pair among the boundary points.
   *
   * @param <T> the generic type
   * @param list the list of points
   * @param getX the function to get the X coordinate
   * @param getY the function to get the Y coordinate
   * @param indicesY the indices sorted by the Y coordinate
   * @param count the number of indices
   * @param result the current closest pair
   * @return the closest pair
   */
  private static <T> Assignment closestPairAtBoundary(IntFunction<T> list, ToDoubleFunction<T> getX,
      ToDoubleFunction<T> getY, int[] indicesY, int count, Assignment result) {
    for (int i = 0; i < count; i++) {
      final T p1 = list.apply(indicesY[i]);
      final double x1 = getX.applyAsDouble(p1);
      final double y1 = getY.applyAsDouble(p1);
      for (int j = i + 1; j < count; j++) {
        final T p2 = list.apply(indicesY[j]);
        final double dy2 = squaredDistance(y1, getY.applyAsDouble(p2));
        // Test the point is closer in Y
        if (dy2 >= result.getDistance()) {
          // Since these are ordered by Y nothing else will be closer
          break;
        }
        final double dx2 = squaredDistance(x1, getX.applyAsDouble(p2));
        // Test the distance
        final double distance = dx2 + dy2;
        if (distance < result.getDistance()) {
          result = new ImmutableAssignment(indicesY[i], indicesY[j], distance);
        }
      }
    }
    return result;
  }

  /**
   * Computes the squared distance between 2 points in two dimensions.
   *
   * @param p1 the p 1
   * @param p2 the p 2
   * @return the squared distance
   */
  private static double squaredDistance(Point2D p1, Point2D p2) {
    return squaredDistance(p1.getX(), p2.getX()) + squaredDistance(p1.getY(), p2.getY());
  }

  /**
   * Computes the squared distance between 2 points in two dimensions.
   *
   * @param <T> the generic type
   * @param p1 the p 1
   * @param p2 the p 2
   * @param getX the function to get the X coordinate
   * @param getY the function to get the Y coordinate
   * @return the squared distance
   */
  private static <T> double squaredDistance(T p1, T p2, ToDoubleFunction<T> getX,
      ToDoubleFunction<T> getY) {
    return squaredDistance(p1, p2, getX) + squaredDistance(p1, p2, getY);
  }

  /**
   * Computes the squared distance between 2 points in a single dimension.
   *
   * @param <T> the generic type
   * @param p1 the p 1
   * @param p2 the p 2
   * @param getX the function to get the X coordinate
   * @return the squared distance
   */
  private static <T> double squaredDistance(T p1, T p2, ToDoubleFunction<T> getX) {
    return squaredDistance(getX.applyAsDouble(p1), getX.applyAsDouble(p2));
  }

  /**
   * Computes the squared distance between 2 values.
   *
   * @param value1 the first value
   * @param value2 the second value
   * @return the squared distance
   */
  private static double squaredDistance(double value1, double value2) {
    return pow2(value1 - value2);
  }

  /**
   * Compute value<sup>2</sup>.
   *
   * @param value the value
   * @return the value squared
   */
  private static double pow2(double value) {
    return value * value;
  }

  /**
   * Gets the size of the collection.
   *
   * @param collection the collection
   * @return the size
   */
  @VisibleForTesting
  static int getSize(Collection<?> collection) {
    return collection == null ? 0 : collection.size();
  }

  /**
   * Convert to a random access list.
   *
   * @param <T> the generic type
   * @param collection the collection
   * @return the random access list
   */
  @VisibleForTesting
  @SuppressWarnings("unchecked")
  static <T> IntFunction<T> toRandomAccess(Collection<T> collection) {
    if (collection instanceof List<?> && collection instanceof RandomAccess) {
      final List<T> list = (List<T>) collection;
      return list::get;
    }
    final Object[] array = collection.toArray();
    return i -> (T) array[i];
  }

  /**
   * Check the size for at least 2 points.
   *
   * @param size the size
   */
  private static void checkSize(int size) {
    ValidationUtils.checkArgument(size > 1, "Require at least 2 points");
  }
}
