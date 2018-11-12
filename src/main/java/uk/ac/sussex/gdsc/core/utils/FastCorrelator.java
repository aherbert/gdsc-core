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
 * Copyright (C) 2011 - 2018 Alex Herbert
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

import java.math.BigInteger;

/**
 * Class to calculate the correlation between two datasets using rolling sums.
 */
public class FastCorrelator {
  private long sumx;
  private long sumxy;
  private long sumxx;
  private long sumyy;
  private long sumy;
  private int count;

  /**
   * Add a pair of data points.
   *
   * @param v1 the first value
   * @param v2 the second value
   */
  public void add(final int v1, final int v2) {
    addData(v1, v2);
  }

  /**
   * Add a pair of data points.
   *
   * @param v1 the first value
   * @param v2 the second value
   */
  public void add(final long v1, final long v2) {
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
    length = Math.min(Math.min(v1.length, v2.length), length);
    for (int i = 0; i < length; i++) {
      addData(v1[i], v2[i]);
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
    // Use long multiplication
    sumxy += ((long) v1 * v2);
    sumxx += ((long) v1 * v1);
    sumyy += ((long) v2 * v2);
    sumy += v2;
    count++;
  }

  /**
   * Add a pair of data points to the sums.
   *
   * @param v1 the first value
   * @param v2 the second value
   */
  private void addData(final long v1, final long v2) {
    sumx += v1;
    sumxy += (v1 * v2);
    sumxx += (v1 * v1);
    sumyy += (v2 * v2);
    sumy += v2;
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
    return calculateCorrelation(sumx, sumxy, sumxx, sumyy, sumy, count);
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
   * Gets the sum X^2.
   *
   * @return The sum of the X data squared.
   */
  public long getSumSquaredX() {
    return sumxx;
  }

  /**
   * Gets the sum Y^2.
   *
   * @return The sum of the Y data squared.
   */
  public long getSumSquaredY() {
    return sumyy;
  }

  /**
   * Gets the sum X*Y.
   *
   * @return The sum of each X data point multiplied by the paired Y data point.
   */
  public long getSumXbyY() {
    return sumxy;
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
    return doCorrelation(x, y, n);
  }

  /**
   * Calculate the correlation using a fast sum.
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
    n = Math.min(Math.min(x.length, y.length), n);
    return doCorrelation(x, y, n);
  }

  /**
   * Calculate the correlation using a fast sum.
   *
   * @param x The X data
   * @param y the Y data
   * @param n the n
   * @return The correlation
   */
  private static double doCorrelation(int[] x, int[] y, int count) {
    if (count <= 0) {
      return Double.NaN;
    }

    long sumx = 0;
    long sumxy = 0;
    long sumxx = 0;
    long sumyy = 0;
    long sumy = 0;

    for (int i = count; i-- > 0;) {
      sumx += x[i];
      sumxy += (x[i] * y[i]);
      sumxx += (x[i] * x[i]);
      sumyy += (y[i] * y[i]);
      sumy += y[i];
    }

    return calculateCorrelation(sumx, sumxy, sumxx, sumyy, sumy, count);
  }

  /**
   * Calculate the correlation using BigInteger to avoid precision error.
   *
   * @param sumx The sum of the X values
   * @param sumxy The sum of the X*Y values
   * @param sumxx The sum of the X^2 values
   * @param sumyy The sum of the Y^2 values
   * @param sumy The sum of the Y values
   * @param count The number of values
   * @return The correlation
   * @see <a
   *      href="https://en.wikipedia.org/wiki/Pearson_correlation_coefficient#For_a_population">Pearson
   *      correlation coefficient</a>
   */
  public static double calculateCorrelation(long sumx, long sumxy, long sumxx, long sumyy,
      long sumy, long count) {

    // Compute:
    // E[XY] - n E[X]E[Y]
    // -------------------------------------------------
    // sqrt(E[X^2] - [E[X]]^2) * sqrt(E[Y^2] - [E[Y]]^2)

    final BigInteger countB = BigInteger.valueOf(count);
    final BigInteger nsumxy = countB.multiply(BigInteger.valueOf(sumxy));
    final BigInteger nsumxx = countB.multiply(BigInteger.valueOf(sumxx));
    final BigInteger nsumyy = countB.multiply(BigInteger.valueOf(sumyy));

    final BigInteger sumxB = BigInteger.valueOf(sumx);
    final BigInteger sumyB = BigInteger.valueOf(sumy);
    final BigInteger coVariance = nsumxy.subtract(sumxB.multiply(sumyB));
    final BigInteger varianceX = nsumxx.subtract(sumxB.multiply(sumxB));
    final BigInteger varianceY = nsumyy.subtract(sumyB.multiply(sumyB));

    return coVariance.doubleValue() / Math.sqrt(varianceX.multiply(varianceY).doubleValue());
  }

  /**
   * Clear all stored values.
   */
  public void clear() {
    sumx = sumxy = sumxx = sumyy = sumy = 0;
    count = 0;
  }
}
