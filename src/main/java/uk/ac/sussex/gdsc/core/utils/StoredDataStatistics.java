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
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Calculate the mean and standard deviation of data. Stores the data for later retrieval.
 */
public class StoredDataStatistics extends Statistics implements Iterable<Double>, DoubleData {

  /** The values. */
  private double[] values = ArrayUtils.EMPTY_DOUBLE_ARRAY;

  /** The cached statistics. */
  private DescriptiveStatistics stats;

  /**
   * Instantiates a new stored data statistics.
   */
  public StoredDataStatistics() {
    // Do nothing
  }

  /**
   * Instantiates a new stored data statistics.
   *
   * @param capacity the capacity
   */
  public StoredDataStatistics(int capacity) {
    values = new double[capacity];
  }

  /**
   * Instantiates a new stored data statistics.
   *
   * @param data the data
   * @return the stored data statistics
   */
  public static StoredDataStatistics create(float[] data) {
    final StoredDataStatistics stats = new StoredDataStatistics();
    stats.add(data);
    return stats;
  }

  /**
   * Instantiates a new stored data statistics.
   *
   * @param data the data
   * @return the stored data statistics
   */
  public static StoredDataStatistics create(double[] data) {
    final StoredDataStatistics stats = new StoredDataStatistics();
    stats.add(data);
    return stats;
  }

  /**
   * Instantiates a new stored data statistics.
   *
   * @param data the data
   * @return the stored data statistics
   */
  public static StoredDataStatistics create(int[] data) {
    final StoredDataStatistics stats = new StoredDataStatistics();
    stats.add(data);
    return stats;
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
    stats = null;
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

  @Override
  protected void addInternal(float[] data, int from, int to) {
    // Assume:
    // - data is not null
    // - from <= to
    checkCapacity(to - from);
    for (int i = from; i < to; i++) {
      final double value = data[i];
      values[size++] = value;
      sum += value;
      sumSq += value * value;
    }
  }

  @Override
  protected void addInternal(double[] data, int from, int to) {
    // Assume:
    // - data is not null
    // - from <= to
    checkCapacity(to - from);
    for (int i = from; i < to; i++) {
      final double value = data[i];
      values[size++] = value;
      sum += value;
      sumSq += value * value;
    }
  }

  @Override
  protected void addInternal(int[] data, int from, int to) {
    // Assume:
    // - data is not null
    // - from <= to
    checkCapacity(to - from);
    for (int i = from; i < to; i++) {
      final double value = data[i];
      values[size++] = value;
      sum += value;
      sumSq += value * value;
    }
  }

  @Override
  protected void addInternal(final double value) {
    if (size == values.length) {
      checkCapacity(1);
    }
    stats = null;
    values[size++] = value;
    sum += value;
    sumSq += value * value;
  }

  @Override
  protected void addInternal(int n, double value) {
    checkCapacity(n);
    for (int i = 0; i < n; i++) {
      values[this.size++] = value;
    }
    sum += n * value;
    sumSq += n * value * value;
  }

  @Override
  public void add(Statistics statistics) {
    if (statistics instanceof StoredDataStatistics) {
      final StoredDataStatistics extra = (StoredDataStatistics) statistics;
      if (extra.size > 0) {
        checkCapacity(extra.size);
        System.arraycopy(extra.values, 0, values, size, extra.size);
        size += statistics.size;
        sum += statistics.sum;
        sumSq += statistics.sumSq;
      }
      return;
    }
    throw new IllegalArgumentException("Not a StoredDataStatistics instance");
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

  /**
   * Gets the statistics.
   *
   * @return object used to compute descriptive statistics. The object is cached
   * @see org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
   */
  public DescriptiveStatistics getStatistics() {
    if (stats == null) {
      stats = new DescriptiveStatistics(values());
    }
    return stats;
  }

  /**
   * Gets the median.
   *
   * @return The median
   */
  public double getMedian() {
    if (size == 0) {
      return Double.NaN;
    }
    if (size == 1) {
      return values[0];
    }
    return getStatistics().getPercentile(50);
  }

  /**
   * Returns a list iterator over the elements in this list (in proper sequence).
   *
   * @return a list iterator over the elements in this list (in proper sequence)
   */
  @Override
  public Iterator<Double> iterator() {
    return new Itr();
  }

  /**
   * Copied from ArrayList and removed unrequired code.
   */
  private class Itr implements Iterator<Double> {
    int cursor; // index of next element to return

    @Override
    public boolean hasNext() {
      return cursor != size;
    }

    @Override
    public Double next() {
      // Copied from ArrayList and removed unrequired code
      final int index = cursor;
      if (index >= size) {
        throw new NoSuchElementException();
      }
      final double[] elementData = StoredDataStatistics.this.values;
      cursor = index + 1;
      return elementData[index];
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public double[] values() {
    return getValues();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Note: This does not reset the allocated storage.
   *
   * @see uk.ac.sussex.gdsc.core.utils.Statistics#reset()
   */
  @Override
  public void reset() {
    super.reset();
    stats = null;
  }
}
