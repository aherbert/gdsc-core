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

package uk.ac.sussex.gdsc.core.utils;

import java.util.function.IntConsumer;
import java.util.stream.IntStream;

/**
 * A set for storing positive-valued integers.
 */
public interface IndexSet {
  /**
   * Add an index into the set.
   *
   * @param index the index
   * @return true if the set was modified by the operation
   * @throws IllegalArgumentException if the index is negative
   */
  boolean add(int index);

  /**
   * Searches the set an index.
   *
   * @param index the index
   * @return true if the set contains the index
   * @throws IllegalArgumentException if the index is negative
   */
  boolean contains(int index);

  /**
   * Returns the number of distinct indices in the set.
   *
   * @return the size
   */
  int size();

  /**
   * Removes all elements from the set. The set will be empty after the operation.
   */
  void clear();

  /**
   * Return a stream of indices. Unless explicitly noted by implementations the ordered is
   * undefined.
   *
   * <p>It is expected the size of the stream is equal to the size of the set. The index set must
   * remain constant during execution of the terminal stream operation, otherwise the result of the
   * stream operation is undefined.
   *
   * @return the stream of indices
   */
  IntStream stream();

  /**
   * Perform the action on each index in the set. Unless explicitly noted by implementations the
   * ordered is undefined.
   *
   * @param action the action
   */
  default void forEach(IntConsumer action) {
    stream().forEach(action);
  }
}
