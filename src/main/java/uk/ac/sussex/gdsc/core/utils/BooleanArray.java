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

package uk.ac.sussex.gdsc.core.utils;

import java.util.Arrays;

/**
 * Expandable store for boolean data backed by an array.
 */
public class BooleanArray {

  private boolean[] values;
  private int size;

  /**
   * Instantiates a new boolean array.
   */
  public BooleanArray() {
    values = new boolean[10];
  }

  /**
   * Instantiates a new boolean array.
   *
   * @param capacity the capacity
   */
  public BooleanArray(int capacity) {
    values = new boolean[capacity];
  }

  /**
   * Instantiates a new boolean array.
   *
   * @param data the data
   * @param clone the clone
   */
  public BooleanArray(boolean[] data, boolean clone) {
    if (data != null) {
      values = (clone) ? data.clone() : data;
      size = data.length;
    }
  }

  /**
   * Ensure that the specified number of elements can be added to the array.
   *
   * <p>This is not synchronized. However any class using the safeAdd() methods in different threads
   * should be using the same synchronized method to add data thus this method will be within
   * synchronized code.
   *
   * @param length the length
   */
  private void checkCapacity(int length) {
    final int minCapacity = size + length;
    final int oldCapacity = values.length;
    if (minCapacity > oldCapacity) {
      int newCapacity = (oldCapacity * 3) / 2 + 1;
      if (newCapacity < minCapacity) {
        newCapacity = minCapacity;
      }
      final boolean[] newValues = new boolean[newCapacity];
      System.arraycopy(values, 0, newValues, 0, size);
      values = newValues;
    }
  }

  /**
   * Add the data.
   *
   * @param data the data
   */
  public void add(boolean[] data) {
    if (data == null) {
      return;
    }
    checkCapacity(data.length);
    for (final boolean value : data) {
      values[size++] = value;
    }
  }

  /**
   * Add the value.
   *
   * @param value the value
   */
  public void add(final boolean value) {
    checkCapacity(1);
    values[size++] = value;
  }

  /**
   * Add the value n times.
   *
   * @param n The number of times
   * @param value The value
   */
  public void add(int n, boolean value) {
    checkCapacity(n);
    for (int i = 0; i < n; i++) {
      values[this.size++] = value;
    }
  }

  /**
   * Adds the data to this store.
   *
   * @param data the data
   */
  public void add(BooleanArray data) {
    if (data.size > 0) {
      checkCapacity(data.size);
      System.arraycopy(data.values, 0, values, size, data.size);
      this.size += data.size;
    }
  }

  /**
   * Add the data. Synchronized for thread safety. (Multiple threads must all use the same safeAdd
   * method to ensure thread safety.)
   *
   * @param data the data
   */
  public synchronized void safeAdd(boolean[] data) {
    add(data);
  }

  /**
   * Add the value. Synchronized for thread safety. (Multiple threads must all use the same safeAdd
   * method to ensure thread safety.)
   *
   * @param value the value
   */
  public synchronized void safeAdd(final boolean value) {
    add(value);
  }

  /**
   * Adds the data to this store. Synchronized for thread safety. (Multiple threads must all use the
   * same safeAdd method to ensure thread safety.)
   *
   * @param data the data
   */
  public synchronized void safeAdd(BooleanArray data) {
    this.add(data);
  }

  /**
   * Convert to an array.
   *
   * @return A copy of the values added.
   */
  public boolean[] toArray() {
    return Arrays.copyOf(values, size);
  }

  /**
   * Gets the value.
   *
   * @param index the index
   * @return the value
   */
  public boolean get(int index) {
    if (index >= size) {
      throw new ArrayIndexOutOfBoundsException(index);
    }
    return values[index];
  }

  /**
   * Gets the value without bounds checking.
   *
   * @param index the index
   * @return the value
   */
  public boolean getf(int index) {
    return values[index];
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
   * Clear the array. Does not release capacity. Use {@link #compact()}.
   */
  public void clear() {
    size = 0;
  }

  /**
   * Compact the array to the current size.
   */
  public void compact() {
    values = toArray();
  }
}
