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
 * Copyright (C) 2011 - 2025 Alex Herbert
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

package uk.ac.sussex.gdsc.core.trees;

import java.util.function.IntPredicate;
import java.util.function.ObjIntConsumer;
import uk.ac.sussex.gdsc.core.utils.function.IntDoubleConsumer;

/**
 * A KD-tree that stores an {@code int} item with each {@code float}-valued location.
 *
 * <p>This is the primitive type specialization of {@link ObjFloatKdTree} for {@code int}.
 */
public interface IntFloatKdTree {
  /**
   * Get the number of dimensions in the tree.
   *
   * @return the dimensions
   */
  int dimensions();

  /**
   * Get the number of items in the tree.
   *
   * @return the size
   */
  int size();

  /**
   * Add a point and associated value to the tree.
   *
   * @param location the location
   * @param value the value
   */
  void add(float[] location, int value);

  /**
   * Add a point and associated value to the tree if the specified point is not already present.
   *
   * @param location the location
   * @param value the value
   * @return true if the point was added
   */
  boolean addIfAbsent(float[] location, int value);

  /**
   * Calculates the nearest {@code count} points to {@code location} and puts the items and the
   * distances in the results. It is assumed that the caller can recreate the item location from the
   * item.
   *
   * <p>The result consumer will be called with the minimum of {@code count} or the current number
   * of items in the tree. It is recommended that the first result passed to the consumer should be
   * the neighbour with the highest distance.
   *
   * @param location the location
   * @param count the count
   * @param sorted if true the results will be sorted (largest distance first)
   * @param distanceFunction the distance function
   * @param results the results
   * @return true if neighbours were found
   */
  boolean nearestNeighbours(double[] location, int count, boolean sorted,
      FloatDistanceFunction distanceFunction, IntDoubleConsumer results);

  /**
   * Calculates the nearest {@code count} points to {@code location} that pass the provided filter
   * and puts the items and the distances in the results. It is assumed that the caller can recreate
   * the item location from the item.
   *
   * <p>The result consumer will be called with the minimum of {@code count} or the current number
   * of items in the tree. It is recommended that the first result passed to the consumer should be
   * the neighbour with the highest distance.
   *
   * @param location the location
   * @param count the count
   * @param sorted if true the results will be sorted (largest distance first)
   * @param distanceFunction the distance function
   * @param filter the filter used to select items
   * @param results the results
   * @return true if neighbours were found
   */
  boolean nearestNeighbours(double[] location, int count, boolean sorted,
      FloatDistanceFunction distanceFunction, IntPredicate filter, IntDoubleConsumer results);

  /**
   * Calculates the neighbour points within {@code range} to {@code location} and puts the items and
   * the distances in the results. It is assumed that the caller can recreate the item location from
   * the item.
   *
   * @param location the location
   * @param range the range
   * @param distanceFunction the distance function
   * @param results the results
   * @return true if neighbours were found
   */
  boolean findNeighbours(double[] location, double range, FloatDistanceFunction distanceFunction,
      IntDoubleConsumer results);

  /**
   * Calculates the nearest point to {@code location} and puts the item and the distance in the
   * result. It is assumed that the caller can recreate the item location from the item. The minimum
   * distance is returned.
   *
   * <p>Special cases:
   *
   * <ul>
   *
   * <li>If the tree is empty the distance is zero.
   *
   * <li>If the distance to all points is NaN then the distance is NaN and the result consumer will
   * not be called.
   *
   * </ul>
   *
   * @param location the location
   * @param distanceFunction the distance function
   * @param result the result (can be null)
   * @return the distance
   */
  double nearestNeighbour(double[] location, FloatDistanceFunction distanceFunction,
      IntDoubleConsumer result);

  /**
   * Calculates the nearest point to {@code location} that passes the provided filter and puts the
   * item and the distance it in the result. It is assumed that the caller can recreate the item
   * location from the item. The minimum distance is returned.
   *
   * <p>Special cases:
   *
   * <ul>
   *
   * <li>If the tree is empty the distance is zero.
   *
   * <li>If the distance to all points is NaN then the distance is NaN and the result consumer will
   * not be called.
   *
   * </ul>
   *
   * @param location the location
   * @param distanceFunction the distance function
   * @param filter the filter used to select items
   * @param result the result (can be null)
   * @return the distance
   */
  double nearestNeighbour(double[] location, FloatDistanceFunction distanceFunction,
      IntPredicate filter, IntDoubleConsumer result);

  /**
   * Performs the given action for each item in the tree until all elements have been processed or
   * the action throws an exception. The iteration order is unspecified. Exceptions thrown by the
   * action are relayed to the caller.
   *
   * @param action the action to be performed for each element
   */
  void forEach(ObjIntConsumer<float[]> action);
}
