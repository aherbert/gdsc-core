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

package uk.ac.sussex.gdsc.core.trees;

/**
 * A Last-In-First-Out (LIFO) stack of node status used to descend the branches of a binary tree.
 *
 * <p>This is a specialised structure used for searching a KD-tree. It has no bounds checking.
 */
abstract class StatusStack {
  /**
   * Contains the status stack as an array.
   */
  private static class ArrayStatusStack extends StatusStack {
    /**
     * The values.
     *
     * <p>Warning: this is indexed starting at 1.
     */
    private final byte[] values;

    /** The size. */
    private int size;

    /**
     * Create an instance.
     *
     * @param capacity the capacity
     */
    ArrayStatusStack(int capacity) {
      this.values = new byte[capacity + 1];
    }

    @Override
    void push(byte value) {
      final int s = size + 1;
      values[s] = value;
      size = s;
    }

    @Override
    byte pop() {
      // Note: This deliberately decrements after. It means when the size is 0 it will return 0
      // for the first call when empty.
      return values[size--];
    }
  }

  /**
   * Contains the status stack as long.
   *
   * <p>This assumes the stack entries are 2-bits of the stored byte.
   * It has a maximum capacity of 32 2-bit values.
   */
  private static class LongStatusStack extends StatusStack {
    /**
     * The values.
     */
    private long values;

    @Override
    void push(byte value) {
      values <<= 2;
      values |= (value & 0x3);
    }

    @Override
    byte pop() {
      final byte s = (byte) (values & 0x3);
      values >>>= 2;
      return s;
    }
  }

  /**
   * Pushes the element onto the top of the stack.
   *
   * <p>Note: The method may throw an exception if the stack is at maximum capacity.
   *
   * @param value the value
   */
  abstract void push(byte value);

  /**
   * Pops an element from the top of the stack.
   *
   * <p>Note: This must return 0 on the first call when empty,
   * otherwise the method may throw an exception.
   *
   * @return the element (or 0 on the first call when empty)
   */
  abstract byte pop();

  /**
   * Create a new instance.
   *
   * <p>The stack is suitable for storing only the first 2 bits of the status byte.
   *
   * @param capacity the capacity
   * @return the status stack
   */
  static StatusStack create(int capacity) {
    if (capacity <= 32) {
      return new LongStatusStack();
    }
    return new ArrayStatusStack(capacity);
  }
}
