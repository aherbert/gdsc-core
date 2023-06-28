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

package uk.ac.sussex.gdsc.core.utils;

import java.util.function.IntPredicate;

/**
 * Contains a list of fixed capacity. This is a simple wrapper around an array providing get/set
 * index methods and dynamic addition of data to the end of the array up to the capacity.
 */
public class IntFixedList {
  /** The data. */
  private final int[] data;

  /** The size. */
  private int size;

  /**
   * Instantiates a new fixed int list.
   *
   * @param capacity the capacity
   */
  public IntFixedList(int capacity) {
    this.data = new int[capacity];
  }

  /**
   * Get the capacity.
   *
   * @return the capacity
   */
  public int capacity() {
    return data.length;
  }

  /**
   * Get the size.
   *
   * @return the size
   */
  public int size() {
    return size;
  }

  /**
   * Adds the value. No bounds checks are made against capacity.
   *
   * @param value the value
   */
  public void add(int value) {
    data[size++] = value;
  }

  /**
   * Adds the values. No bounds checks are made against capacity or for a valid input array.
   *
   * @param values the values
   */
  public void add(int[] values) {
    final int length = values.length;
    System.arraycopy(values, 0, data, size, length);
    size += length;
  }

  /**
   * Adds the values. No bounds checks are made against capacity or for a valid input array.
   *
   * @param values the values
   */
  public void add(IntFixedList values) {
    final int length = values.size;
    System.arraycopy(values.data, 0, data, size, length);
    size += length;
  }

  /**
   * Adds the values. No bounds checks are made against capacity or for a valid input array.
   *
   * @param values the values
   */
  public void addValues(int... values) {
    add(values);
  }

  /**
   * Gets the value at the given index. No bounds checks are made against the size.
   *
   * @param index the index
   * @return the value
   */
  public int get(int index) {
    return data[index];
  }

  /**
   * Sets the value at the given index. No bounds checks are made against the size and the size is
   * not increased.
   *
   * @param index the index
   * @param value the value
   */
  public void set(int index, int value) {
    data[index] = value;
  }

  /**
   * Removes the value at the given index.
   *
   * @param index the index
   * @return the value
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  public int remove(int index) {
    if (index >= size) {
      throw new IndexOutOfBoundsException("Index " + index + " not valid for size " + size);
    }
    // Implicit IndexOutOfBounds for index < 0
    final int previous = data[index];
    // Shift left all elements after the remove index
    size--;
    System.arraycopy(data, index + 1, data, index, size - index);
    return previous;
  }

  /**
   * Convert the current values to an array.
   *
   * @return the int[] array
   */
  public int[] toArray() {
    final int[] copy = new int[size];
    System.arraycopy(data, 0, copy, 0, size);
    return copy;
  }

  /**
   * Clear the list.
   */
  public void clear() {
    size = 0;
  }

  /**
   * Copy the data into the provided array at the given position.
   *
   * @param dest the destination
   * @param destPos the destination position
   */
  public void copy(int[] dest, int destPos) {
    System.arraycopy(data, 0, dest, destPos, size);
  }

  /**
   * Remove any items from the list that are identified by the filter. Functions as if testing all
   * items:
   *
   * <pre>
   * int newSize = 0;
   * for (int i = 0; i &lt; size; i++) {
   *   if (filter.test((E) data[i])) {
   *     // remove
   *     continue;
   *   }
   *   data[newSize++] = data[i];
   * }
   * size = newSize;
   * </pre>
   *
   * @param filter the filter
   * @return true if the size was modified by the filter
   */
  public boolean removeIf(IntPredicate filter) {
    int index = 0;
    final int[] elements = data;
    final int length = size;

    // Find first item to filter.
    for (; index < length; index++) {
      if (filter.test(elements[index])) {
        break;
      }
    }
    if (index == length) {
      // Nothing to remove
      return false;
    }

    // The list has changed.
    // Do a single sweep across the remaining data copying elements if they are not filtered.
    // Note: The filter may throw and leave the list without a new size.
    // (E.g. is the filter is a collection that does not allow null).
    // So we set the new size in a finally block.
    int newSize = index;
    try {
      // We know the current index is identified by the filter so advance 1
      index++;

      // Scan the rest
      for (; index < length; index++) {
        final int e = elements[index];
        if (filter.test(e)) {
          continue;
        }
        elements[newSize++] = e;
      }
    } finally {
      // Ensure the length is correct
      if (index != length) {
        // Did not get to the end of the list (e.g. the filter may throw) so copy it verbatim.
        final int len = length - index;
        System.arraycopy(data, index, data, newSize, len);
        newSize += len;
      }
      size = newSize;
    }
    // The list was modified
    return true;
  }
}
