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

package uk.ac.sussex.gdsc.core.trees;

import java.util.function.DoubleConsumer;

/**
 * A KD-tree of {@code float}-valued locations.
 */
public interface FloatKdTree {
  /**
   * Get the number of dimensions in the tree.
   *
   * @return the dimensions
   */
  public int dimensions();

  /**
   * Get the number of items in the tree.
   *
   * @return the size
   */
  public int size();

  /**
   * Add a point and associated value to the tree.
   *
   * @param location the location
   */
  public void addPoint(float[] location);

  /**
   * Calculates the nearest {@code count} points to {@code location} and puts the distances in the
   * results.
   *
   * <p>The result consumer will be called with the minimum of {@code count} or the current number
   * of items in the tree.
   *
   * @param location the location
   * @param count the count
   * @param sorted if true the results will be sorted (largest distance first)
   * @param distanceFunction the distance function
   * @param results the results
   * @return true if neighbours were found
   */
  public boolean nearestNeighbours(double[] location, int count, boolean sorted,
      FloatDistanceFunction distanceFunction, DoubleConsumer results);

  /**
   * Calculates the neighbour points within {@code range} to {@code location} and puts the distances
   * in the results.
   *
   * @param location the location
   * @param range the range
   * @param distanceFunction the distance function
   * @param results the results
   * @return true if neighbours were found
   */
  public boolean findNeighbours(double[] location, double range,
      FloatDistanceFunction distanceFunction, DoubleConsumer results);

  /**
   * Calculates the nearest point to {@code location} and puts the distance in the result. The
   * minimum distance is returned.
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
  public double nearestNeighbour(double[] location, FloatDistanceFunction distanceFunction,
      DoubleConsumer result);
}
