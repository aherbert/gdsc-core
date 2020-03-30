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

/**
 * A Last-In-First-Out (LIFO) stack of node status used to descend the branches of a binary tree.
 */
class StatusStack {
  /** The values. */
  private Status[] values;

  /** The size. */
  private int size;

  /**
   * Create a new instance.
   *
   * @param capacity the capacity
   */
  StatusStack(int capacity) {
    this.values = new Status[capacity];
  }

  /**
   * Pushes the element onto the top of the stack.
   *
   * @param value the value
   */
  void push(Status value) {
    final int s = size;
    values[s] = value;
    size = s + 1;
  }

  /**
   * Pops an element from the top of the stack.
   *
   * @return the element
   * @throws IndexOutOfBoundsException If the current size is zero
   */
  Status pop() {
    // Do not set the value at the index to null. Since it is an enum singleton then setting
    // to null would not effect garbage collection.
    return values[--size];
  }
}
