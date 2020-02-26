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
 * Class to calculate the correlation between two datasets, storing the data for the correlation
 * calculation.
 */
public class Correlator {
  private int[] x;
  private int[] y;
  private int count;

  private long sumx;
  private long sumy;

  /**
   * Constructor.
   *
   * @param capacity The initial capacity
   */
  public Correlator(int capacity) {
    final int size = Math.max(0, capacity);
    x = new int[size];
    y = new int[size];
  }

  /**
   * Constructor.
   */
  public Correlator() {
    this(100);
  }

  /**
   * Add a pair of data points.
   *
   * @param v1 the first value
   * @param v2 the second value
   */
  public void add(final int v1, final int v2) {
    checkCapacity(1);
    addData(v1, v2);
  }

  /**
   * Add a set of paired data points.
   *
   * @param v1 the first values
   * @param v2 the second values
   */
  public void add(final int[] v1, final int[] v2) {
    if (v1 == null || v2 == null) {
      return;
    }
    final int length = Math.min(v1.length, v2.length);
    checkCapacity(length);
    for (int i = 0; i < length; i++) {
      addData(v1[i], v2[i]);
    }
  }

  /**
   * Add a set of paired data points.
   *
   * @param v1 the first values
   * @param v2 the second values
   * @param length the length of the data set
   */
  public void add(final int[] v1, final int[] v2, int length) {
    if (v1 == null || v2 == null) {
      return;
    }
    final int size = Math.min(Math.min(v1.length, v2.length), length);
    checkCapacity(size);
    for (int i = 0; i < size; i++) {
      addData(v1[i], v2[i]);
    }
  }

  /**
   * Ensure that the specified number of elements can be added to the arrays.
   *
   * @param length the length
   */
  private void checkCapacity(int length) {
    final int minCapacity = count + length;
    if (minCapacity > x.length) {
      int newCapacity = (x.length * 3) / 2 + 1;
      if (newCapacity < minCapacity) {
        newCapacity = minCapacity;
      }
      int[] newValues = new int[newCapacity];
      System.arraycopy(x, 0, newValues, 0, count);
      x = newValues;
      newValues = new int[newCapacity];
      System.arraycopy(y, 0, newValues, 0, count);
      y = newValues;
    }
  }

  /**
   * Add a pair of data points to the sums.
   *
   * @param v1 the first value
   * @param v2 the second value
   */
  private void addData(final int v1, final int v2) {
    sumx += v1;
    sumy += v2;
    x[count] = v1;
    y[count] = v2;
    count++;
  }

  /**
   * Gets the correlation.
   *
   * @return The correlation.
   */
  public double getCorrelation() {
    if (count == 0) {
      return Double.NaN;
    }
    final double ux = sumx / (double) count;
    final double uy = sumy / (double) count;
    return doCorrelation(x, y, count, ux, uy);
  }

  /**
   * Gets the correlation using a fast sum.
   *
   * @return The correlation calculated using a fast sum.
   */
  public double getFastCorrelation() {
    if (count == 0) {
      return Double.NaN;
    }

    long sumxy = 0;
    long sumxx = 0;
    long sumyy = 0;

    for (int i = count; i-- > 0;) {
      sumxy += (x[i] * y[i]);
      sumxx += (x[i] * x[i]);
      sumyy += (y[i] * y[i]);
    }

    return FastCorrelator.calculateCorrelation(sumx, sumxy, sumxx, sumyy, sumy, count);
  }

  /**
   * Gets the x-data.
   *
   * @return The X-data.
   */
  public int[] getX() {
    return Arrays.copyOf(x, count);
  }

  /**
   * Gets the y-data.
   *
   * @return The Y-data.
   */
  public int[] getY() {
    return Arrays.copyOf(y, count);
  }

  /**
   * Gets the sum of the X data.
   *
   * @return The sum of the X data.
   */
  public long getSumX() {
    return sumx;
  }

  /**
   * Gets the sum of the Y data.
   *
   * @return The sum of the Y data.
   */
  public long getSumY() {
    return sumy;
  }

  /**
   * Gets the number of data points.
   *
   * @return The number of data points.
   */
  public int getN() {
    return count;
  }

  /**
   * Calculate the correlation.
   *
   * @param x The X data
   * @param y the Y data
   * @return The correlation
   */
  public static double correlation(int[] x, int[] y) {
    if (x == null || y == null) {
      return Double.NaN;
    }
    final int n = Math.min(x.length, y.length);
    return correlation(x, y, n);
  }

  /**
   * Calculate the correlation.
   *
   * @param x The X data
   * @param y the Y data
   * @param n The number of data points
   * @return The correlation
   */
  public static double correlation(int[] x, int[] y, int n) {
    if (x == null || y == null) {
      return Double.NaN;
    }
    final int count = Math.min(Math.min(x.length, y.length), n);
    return doCorrelation(x, y, count);
  }

  /**
   * Calculate the correlation.
   *
   * @param x The X data
   * @param y the Y data
   * @param n The number of data points
   * @return The correlation
   */
  private static double doCorrelation(final int[] x, final int[] y, final int n) {
    if (n <= 0) {
      return Double.NaN;
    }

    long sx = 0;
    long sy = 0;

    // Get means
    for (int i = n; i-- > 0;) {
      sx += x[i];
      sy += y[i];
    }

    final double ux = sx / (double) n;
    final double uy = sy / (double) n;

    return doCorrelation(x, y, n, ux, uy);
  }

  /**
   * Calculate the correlation.
   *
   * @param x The X data
   * @param y the Y data
   * @param n The number of data points
   * @param ux The mean of the X data
   * @param uy The mean of the Y data
   * @return The correlation
   */
  private static double doCorrelation(final int[] x, final int[] y, final int n, final double ux,
      final double uy) {
    // TODO - Add a check to ensure that the sum will not lose precision as the total aggregates.
    // This could be done by keeping a BigDecimal to store the overall sums. When the rolling total
    // reaches the specified precision limit for a double then it should be added to the
    // BigDecimal and reset. The precision limit could be set using the value of the mean,
    // e.g. 1e10 times bigger than the mean.

    // Calculate variances
    double p1 = 0;
    double p2 = 0;
    double p3 = 0;
    for (int i = n; i-- > 0;) {
      final double d1 = x[i] - ux;
      final double d2 = y[i] - uy;
      p1 += d1 * d1;
      p2 += d2 * d2;
      p3 += d1 * d2;
    }

    final double p1ByP2 = p1 * p2;
    return (p1ByP2 == 0) ? 0 : p3 / Math.sqrt(p1ByP2);
  }

  /**
   * Clear all stored values.
   */
  public void clear() {
    sumx = sumy = 0;
    count = 0;
  }
}
