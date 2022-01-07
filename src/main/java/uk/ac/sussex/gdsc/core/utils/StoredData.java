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
import java.util.function.DoubleConsumer;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Expandable store for data backed by a double array.
 */
public class StoredData implements DoubleData {

  /** The values. */
  private double[] values = ArrayUtils.EMPTY_DOUBLE_ARRAY;

  /** The size. */
  private int size;

  /**
   * Instantiates a new stored data.
   */
  public StoredData() {
    // Do nothing
  }

  /**
   * Instantiates a new stored data.
   *
   * @param capacity the capacity
   */
  public StoredData(int capacity) {
    values = new double[capacity];
  }

  /**
   * Instantiates a new stored data.
   *
   * @param data the data
   * @return the stored data
   */
  public static StoredData create(float[] data) {
    final StoredData object = new StoredData();
    object.add(data);
    return object;
  }

  /**
   * Instantiates a new stored data.
   *
   * @param data the data
   * @return the stored data
   */
  public static StoredData create(double[] data) {
    final StoredData object = new StoredData();
    object.add(data);
    return object;
  }

  /**
   * Instantiates a new stored data.
   *
   * @param data the data
   * @return the stored data
   */
  public static StoredData create(int[] data) {
    final StoredData object = new StoredData();
    object.add(data);
    return object;
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
    // Overflow safe
    if (minCapacity - oldCapacity > 0) {
      int newCapacity = (oldCapacity * 3) / 2 + 1;
      if (newCapacity - minCapacity < 0) {
        newCapacity = minCapacity;
      }
      final double[] newValues = new double[newCapacity];
      System.arraycopy(values, 0, newValues, 0, size);
      values = newValues;
    }
  }

  /**
   * Add the data.
   *
   * @param data the data
   */
  public void add(float[] data) {
    if (data == null) {
      return;
    }
    checkCapacity(data.length);
    for (final float value : data) {
      values[size++] = value;
    }
  }

  /**
   * Add the data.
   *
   * @param data the data
   */
  public void add(double[] data) {
    if (data == null) {
      return;
    }
    checkCapacity(data.length);
    System.arraycopy(data, 0, values, size, data.length);
    size += data.length;
  }

  /**
   * Add the data.
   *
   * @param data the data
   */
  public void add(int[] data) {
    if (data == null) {
      return;
    }
    checkCapacity(data.length);
    for (final int value : data) {
      values[size++] = value;
    }
  }

  /**
   * Add the value.
   *
   * @param value the value
   */
  public void add(final double value) {
    if (size == values.length) {
      checkCapacity(1);
    }
    values[size++] = value;
  }

  /**
   * Add the value n times.
   *
   * @param n The number of times
   * @param value The value
   * @throws IllegalArgumentException if the number of times is not positive
   */
  public void add(int n, double value) {
    ValidationUtils.checkPositive(n, "number of times");
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
  public void add(StoredData data) {
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
  public void safeAdd(float[] data) {
    synchronized (this) {
      add(data);
    }
  }

  /**
   * Add the data. Synchronized for thread safety. (Multiple threads must all use the same safeAdd
   * method to ensure thread safety.)
   *
   * @param data the data
   */
  public void safeAdd(double[] data) {
    synchronized (this) {
      add(data);
    }
  }

  /**
   * Add the data. Synchronized for thread safety. (Multiple threads must all use the same safeAdd
   * method to ensure thread safety.)
   *
   * @param data the data
   */
  public void safeAdd(int[] data) {
    synchronized (this) {
      add(data);
    }
  }

  /**
   * Add the value. Synchronized for thread safety. (Multiple threads must all use the same safeAdd
   * method to ensure thread safety.)
   *
   * @param value the value
   */
  public void safeAdd(final double value) {
    synchronized (this) {
      add(value);
    }
  }

  /**
   * Add the value n times. Synchronized for thread safety. (Multiple threads must all use the same
   * safeAdd method to ensure thread safety.)
   *
   * @param n the n
   * @param value the value
   */
  public void safeAdd(int n, final double value) {
    synchronized (this) {
      add(n, value);
    }
  }

  /**
   * Adds the data to this store. Synchronized for thread safety. (Multiple threads must all use the
   * same safeAdd method to ensure thread safety.)
   *
   * @param data the data
   */
  public void safeAdd(StoredData data) {
    synchronized (this) {
      add(data);
    }
  }

  /**
   * Gets the values.
   *
   * @return A copy of the values added
   */
  public double[] getValues() {
    return Arrays.copyOf(values, size);
  }

  /**
   * Gets the value.
   *
   * @param index the index
   * @return the value
   */
  public double getValue(int index) {
    return values[index];
  }

  /**
   * Gets the float values.
   *
   * @return A copy of the values added
   */
  public float[] getFloatValues() {
    final float[] data = new float[size];
    for (int i = 0; i < size; i++) {
      data[i] = (float) values[i];
    }
    return data;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public double[] values() {
    return getValues();
  }

  @Override
  public void forEach(DoubleConsumer action) {
    final int n = size;
    final double[] local = values;
    for (int i = 0; i < n; i++) {
      action.accept(local[i]);
    }
  }

  /**
   * Clear the store.
   */
  public void clear() {
    clear(0);
  }

  /**
   * Clear the store and resize to the given capacity.
   *
   * @param capacity the capacity
   */
  public void clear(int capacity) {
    size = 0;
    values = new double[capacity];
  }

  /**
   * Reset the store (but keep the capacity).
   */
  public void reset() {
    size = 0;
  }

  /**
   * Get the capacity of the store.
   *
   * @return the capacity
   */
  public int capacity() {
    return values.length;
  }
}
