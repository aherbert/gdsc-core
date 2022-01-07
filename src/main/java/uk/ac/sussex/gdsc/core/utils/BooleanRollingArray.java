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

import java.util.Arrays;

/**
 * Provide a rolling array of booleans.
 */
public class BooleanRollingArray {
  private final boolean[] data;
  private final int capacity;
  private int index;
  private int count;
  private int sum;

  /**
   * Create a rolling array.
   *
   * @param capacity the capacity
   */
  public BooleanRollingArray(int capacity) {
    this.capacity = capacity;
    this.data = new boolean[capacity];
  }

  /**
   * Remove all the numbers from the array.
   */
  public void clear() {
    sum = 0;
    index = 0;
    count = 0;
  }

  /**
   * Add a number to the array.
   *
   * @param value the value
   */
  public void add(boolean value) {
    // If at capacity
    if (isFull()) {
      // Subtract the item to be replaced
      if (data[index]) {
        sum--;
      }
    } else {
      // Otherwise increase the count
      count++;
    }
    // Add to the true count
    if (value) {
      sum++;
    }
    // Replace the item
    data[index++] = value;
    // Wrap the index
    if (index == capacity) {
      index = 0;
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
      Arrays.fill(data, value);
      sum = (value) ? capacity : 0;
      index = 0;
      count = capacity;
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
    return count;
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
    return count - sum;
  }

  /**
   * Checks if is full.
   *
   * @return True if full.
   */
  public boolean isFull() {
    return count == capacity;
  }

  /**
   * Convert to an array. The data is returned oldest first.
   *
   * @return the array
   */
  public boolean[] toArray() {
    if (isFull()) {
      final boolean[] result = new boolean[count];
      final int size = data.length - index;
      System.arraycopy(data, index, result, 0, size);
      System.arraycopy(data, 0, result, size, data.length - size);
      return result;
    }
    return Arrays.copyOf(data, count);
  }
}
