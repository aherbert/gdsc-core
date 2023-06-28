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
 * Provide a rolling array of integers.
 */
public class IntRollingArray {
  private final int[] data;
  private final int capacity;
  private int index;
  private long sum;
  private boolean wrapped;

  /**
   * Create a rolling array.
   *
   * @param capacity the capacity
   */
  public IntRollingArray(int capacity) {
    this.capacity = capacity;
    this.data = new int[capacity];
  }

  /**
   * Remove all the numbers from the array.
   */
  public void clear() {
    sum = 0;
    index = 0;
    wrapped = false;
    Arrays.fill(data, 0);
  }

  /**
   * Add a number to the array.
   *
   * @param number the number
   */
  public void add(int number) {
    // Subtract the item to be replaced.
    // If not full then this will be zero.
    sum -= data[index];

    // Add to the total
    sum += number;
    // Replace the item
    data[index++] = number;
    // Wrap the index
    if (index == capacity) {
      index = 0;
      wrapped = true;
    }
  }

  /**
   * Add a number to the array n times.
   *
   * @param number the number
   * @param repeats the repeats (n)
   */
  public void add(int number, int repeats) {
    if (repeats >= capacity) {
      // Saturate
      Arrays.fill(data, number);
      sum = (long) capacity * number;
      index = 0;
      wrapped = true;
    } else {
      for (int i = repeats; i-- > 0;) {
        add(number);
      }
    }
  }

  /**
   * Gets the count of numbers stored in the array.
   *
   * @return The count
   */
  public int getCount() {
    return wrapped ? capacity : index;
  }

  /**
   * Gets the capacity of the array.
   *
   * @return The capacity
   */
  public int getCapacity() {
    return capacity;
  }

  /**
   * Gets the sum using the rolling sum of the numbers (may accumulate errors).
   *
   * @return The sum
   */
  public long getSum() {
    return sum;
  }

  /**
   * Gets the recomputed sum using the current set of numbers.
   *
   * @return The recomputed sum using the current set of numbers.
   */
  public long computeAndGetSum() {
    long newSum = 0;
    // If full 'count' will be the length of the data array
    for (int i = getCount(); i-- != 0; ) {
      newSum += data[i];
    }

    // Reset the sum
    sum = newSum;
    return newSum;
  }

  /**
   * Gets average using the rolling sum of the numbers.
   *
   * @return The average using the rolling sum of the numbers.
   */
  public double getAverage() {
    return (double) sum / getCount();
  }

  /**
   * Gets the average using a recomputed sum of the current numbers.
   *
   * @return The average using a recomputed sum of the current numbers.
   */
  public double computeAndGetAverage() {
    return (double) computeAndGetSum() / getCount();
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
  public int[] toArray() {
    final int count = getCount();
    if (isFull()) {
      final int[] result = new int[count];
      final int size = data.length - index;
      System.arraycopy(data, index, result, 0, size);
      System.arraycopy(data, 0, result, size, data.length - size);
      return result;
    }
    return Arrays.copyOf(data, count);
  }
}
