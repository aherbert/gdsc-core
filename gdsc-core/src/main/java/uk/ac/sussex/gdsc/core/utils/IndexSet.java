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

package uk.ac.sussex.gdsc.core.utils;

import java.util.Spliterator;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

/**
 * A set for storing positive-valued integers.
 *
 * <p>Usage includes filtering array indices to unique values, and conditionally processing the
 * first occurrence of a positive ID.
 */
public interface IndexSet {
  /**
   * Add an index into the set.
   *
   * <p>Note: If the caller does not require the result then use {@link #put(int)}.
   *
   * @param index the index
   * @return true if the set was modified by the operation
   * @throws IndexOutOfBoundsException if the index is negative
   * @see #put(int)
   */
  boolean add(int index);

  /**
   * Put an index into the set.
   *
   * <p>The default implementation calls {@link #add(int)}. Implementations can override this method
   * if computing the return value is expensive.
   *
   * @param index the index
   * @throws IndexOutOfBoundsException if the index is negative
   * @see #add(int)
   */
  default void put(int index) {
    add(index);
  }

  /**
   * Searches the set an index.
   *
   * @param index the index
   * @return true if the set contains the index
   * @throws IndexOutOfBoundsException if the index is negative
   */
  boolean contains(int index);

  /**
   * Returns the number of distinct indices in the set.
   *
   * @return the size
   */
  int size();

  /**
   * Removes all indices from the set. The set will be empty after the operation.
   */
  void clear();

  /**
   * Return a stream of indices.
   *
   * <p>If the set makes any guarantees about the order returned from the forEach action, this
   * method must return the indices in the same order.
   *
   * <p>It is expected the size of the stream is equal to the size of the set. The index set must
   * remain constant during execution of the terminal stream operation, otherwise the result of the
   * stream operation is undefined.
   *
   * @return the stream of indices
   */
  IntStream intStream();

  /**
   * Return a spliterator for the indices in the set.
   *
   * <p>The spliterator must report at least {@link Spliterator#DISTINCT}.
   *
   * @return the spliterator
   */
  Spliterator.OfInt spliterator();

  /**
   * Perform the action on each index in the set. Unless explicitly noted by implementations the
   * order is undefined.
   *
   * @param action the action
   */
  void forEach(IntConsumer action);

  /**
   * Returns an array containing the indices in this set.
   *
   * <p>If the set makes any guarantees about the order returned from the forEach action, this
   * method must return the indices in the same order.
   *
   * <p>If the indices fit in the destination array then it will be filled from index 0. If the
   * array is longer than the current size the remaining array is unchanged. If the array is null or
   * smaller than the current size a new array allocation is performed; the destination is
   * unchanged.
   *
   * @param destination the destination array
   * @return the array
   */
  default int[] toArray(int[] destination) {
    final int size = size();
    final int[] a = destination == null || destination.length < size ? new int[size] : destination;
    final int[] index = {0};
    forEach(i -> a[index[0]++] = i);
    return a;
  }
}
