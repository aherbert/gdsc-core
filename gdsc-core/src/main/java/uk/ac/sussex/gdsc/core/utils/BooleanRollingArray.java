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

import java.util.Arrays;

/**
 * Provide a rolling array of booleans.
 */
public class BooleanRollingArray {
  private final byte[] data;
  private final int capacity;
  private int index;
  private int sum;
  private boolean wrapped;

  /**
   * Create a rolling array.
   *
   * @param capacity the capacity
   */
  public BooleanRollingArray(int capacity) {
    this.capacity = capacity;
    this.data = new byte[capacity];
  }

  /**
   * Remove all the numbers from the array.
   */
  public void clear() {
    sum = 0;
    index = 0;
    wrapped = false;
    Arrays.fill(data, (byte) 0);
  }

  /**
   * Add a number to the array.
   *
   * @param value the value
   */
  public void add(boolean value) {
    // Subtract the item to be replaced.
    // If not full then this will be zero.
    sum -= data[index];

    // Add to the true count
    sum += value ? 1 : 0;
    // Replace the item
    data[index++] = (byte) (value ? 1 : 0);
    // Wrap the index
    if (index == capacity) {
      index = 0;
      wrapped = true;
    }
  }

  /**
   * Add a number to the array n times.
   *
   * @param value the value
   * @param n the number of times
   */
  public void add(boolean value, int n) {
    if (n >= capacity) {
      // Saturate
      Arrays.fill(data, (byte) (value ? 1 : 0));
      sum = value ? capacity : 0;
      index = 0;
      wrapped = true;
    } else {
      for (int i = n; i-- > 0;) {
        add(value);
      }
    }
  }

  /**
   * Gets the count of items stored in the array.
   *
   * @return The count of items stored in the array.
   */
  public int getCount() {
    return wrapped ? capacity : index;
  }

  /**
   * Gets the capacity of the array.
   *
   * @return The capacity of the array.
   */
  public int getCapacity() {
    return capacity;
  }

  /**
   * Gets the number of true items stored in the array.
   *
   * @return The number of true items stored in the array.
   */
  public int getTrueCount() {
    return sum;
  }

  /**
   * Gets the number of false items stored in the array.
   *
   * @return The number of false items stored in the array.
   */
  public int getFalseCount() {
    return getCount() - sum;
  }

  /**
   * Checks if is full.
   *
   * @return True if full.
   */
  public boolean isFull() {
    return wrapped;
  }

  /**
   * Convert to an array. The data is returned oldest first.
   *
   * @return the array
   */
  public boolean[] toArray() {
    final boolean[] result = new boolean[getCount()];
    for (int i = result.length, j = index - 1; i-- != 0;) {
      j = j < 0 ? j + capacity : j;
      result[i] = data[j--] == 1;
    }
    return result;
  }
}
