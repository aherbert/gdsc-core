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

package uk.ac.sussex.gdsc.core.utils;

import java.util.Arrays;

/**
 * Provide a rolling array of doubles.
 */
public class DoubleRollingArray {
  private final double[] data;
  private final int capacity;
  private int index;
  private int count;
  private double sum;

  /**
   * Create a rolling array.
   *
   * @param capacity the capacity
   */
  public DoubleRollingArray(int capacity) {
    this.capacity = capacity;
    this.data = new double[capacity];
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
   * @param number the number
   */
  public void add(double number) {
    // If at capacity
    if (isFull()) {
      // Subtract the item to be replaced
      sum -= data[index];
    } else {
      // Otherwise increase the count
      count++;
    }
    // Add to the total
    sum += number;
    // Replace the item
    data[index++] = number;
    // Wrap the index
    if (index == capacity) {
      index = 0;
    }
  }

  /**
   * Add a number to the array n times.
   *
   * @param number the number
   * @param repeats the repeats (n)
   */
  public void add(double number, int repeats) {
    if (repeats >= capacity) {
      // Saturate
      Arrays.fill(data, number);
      sum = capacity * number;
      index = 0;
      count = capacity;
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
    return count;
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
  public double getSum() {
    return sum;
  }

  /**
   * Gets the recomputed sum using the current set of numbers.
   *
   * @return The recomputed sum using the current set of numbers.
   */
  public double computeAndGetSum() {
    double newSum = 0;
    // If full 'count' will be the length of the data array
    for (int i = 0; i < count; i++) {
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
    return sum / count;
  }

  /**
   * Gets the average using a recomputed sum of the current numbers.
   *
   * @return The average using a recomputed sum of the current numbers.
   */
  public double computeAndGetAverage() {
    return computeAndGetSum() / count;
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
  public double[] toArray() {
    if (isFull()) {
      final double[] result = new double[count];
      final int size = data.length - index;
      System.arraycopy(data, index, result, 0, size);
      System.arraycopy(data, 0, result, size, data.length - size);
      return result;
    }
    return Arrays.copyOf(data, count);
  }
}
