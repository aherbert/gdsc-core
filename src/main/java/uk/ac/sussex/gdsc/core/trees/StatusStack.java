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
 * Copyright (C) 2011 - 2021 Alex Herbert
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
 *
 * <p>This is a specialised structure used for searching a KD-tree. It has no bounds checking.
 *
 * @since 2.0
 */
class StatusStack {
  /**
   * The values.
   *
   * <p>Warning: this is indexed starting at 1.
   */
  private final byte[] values;

  /** The size. */
  private int size;

  /**
   * Create a new instance.
   *
   * @param capacity the capacity
   */
  StatusStack(int capacity) {
    this.values = new byte[capacity + 1];
  }

  /**
   * Pushes the element onto the top of the stack.
   *
   * <p>No capacity checking is performed.
   *
   * @param value the value
   */
  void push(byte value) {
    final int s = size + 1;
    values[s] = value;
    size = s;
  }

  /**
   * Pops an element from the top of the stack.
   *
   * <p>No capacity checking is performed. This will return 0 on the first call when empty,
   * otherwise will throw an exception.
   *
   * @return the element (or 0 on the first call when empty)
   * @throws IndexOutOfBoundsException If empty
   */
  byte pop() {
    // Note: This deliberately decrements after. It means when the size is 0 it will return 0
    // for the first call when empty.
    return values[size--];
  }
}
