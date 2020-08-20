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

import org.apache.commons.math3.distribution.TDistribution;

/**
 * Simple class to calculate the mean and standard deviation of data.
 */
public class Statistics {
  /** The size of the data. */
  protected int size;

  /** The sum of the data. */
  protected double sum;

  /** The sum-of-squares of the data. */
  protected double sumSq;

  /**
   * Instantiates a new statistics.
   *
   * @param data the data
   * @return the statistics
   */
  public static Statistics create(float[] data) {
    final Statistics stats = new Statistics();
    stats.add(data);
    return stats;
  }

  /**
   * Instantiates a new statistics.
   *
   * @param data the data
   * @return the statistics
   */
  public static Statistics create(double[] data) {
    final Statistics stats = new Statistics();
    stats.add(data);
    return stats;
  }

  /**
   * Instantiates a new statistics.
   *
   * @param data the data
   * @return the statistics
   */
  public static Statistics create(int[] data) {
    final Statistics stats = new Statistics();
    stats.add(data);
    return stats;
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
    addInternal(data, 0, data.length);
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
    addInternal(data, 0, data.length);
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
    addInternal(data, 0, data.length);
  }

  /**
   * Add the data.
   *
   * @param data the data
   * @param from the from index (inclusive)
   * @param to the to index (exclusive)
   */
  public void add(float[] data, int from, int to) {
    if (data == null) {
      return;
    }
    rangeCheck(data.length, from, to);
    addInternal(data, from, to);
  }

  /**
   * Add the data.
   *
   * @param data the data
   * @param from the from index (inclusive)
   * @param to the to index (exclusive)
   */
  public void add(double[] data, int from, int to) {
    if (data == null) {
      return;
    }
    rangeCheck(data.length, from, to);
    addInternal(data, from, to);
  }

  /**
   * Add the data.
   *
   * @param data the data
   * @param from the from index (inclusive)
   * @param to the to index (exclusive)
   */
  public void add(int[] data, int from, int to) {
    if (data == null) {
      return;
    }
    rangeCheck(data.length, from, to);
    addInternal(data, from, to);
  }

  /**
   * Add the value.
   *
   * @param value the value
   */
  public void add(final double value) {
    addInternal(value);
  }

  /**
   * Add the value n times.
   *
   * @param n The number of times
   * @param value The value
   * @throws IllegalArgumentException if the number of times is not strictly positive
   */
  public void add(int n, double value) {
    ValidationUtils.checkStrictlyPositive(n, "number of times");
    addInternal(n, value);
  }

  /**
   * Add the statistics to the data.
   *
   * @param statistics the statistics
   */
  public void add(Statistics statistics) {
    size += statistics.size;
    sum += statistics.sum;
    sumSq += statistics.sumSq;
  }

  /**
   * Checks that {@code fromIndex} and {@code toIndex} are in the range and throws an exception if
   * they aren't.
   */
  private static void rangeCheck(int arrayLength, int fromIndex, int toIndex) {
    if (fromIndex > toIndex) {
      throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
    }
    if (fromIndex < 0) {
      throw new ArrayIndexOutOfBoundsException(fromIndex);
    }
    if (toIndex > arrayLength) {
      throw new ArrayIndexOutOfBoundsException(toIndex);
    }
  }

  /**
   * Add the data.
   *
   * @param data the data
   * @param from the from index (inclusive)
   * @param to the to index (exclusive)
   */
  protected void addInternal(float[] data, int from, int to) {
    for (int i = from; i < to; i++) {
      final double value = data[i];
      sum += value;
      sumSq += value * value;
    }
    size += (to - from);
  }

  /**
   * Add the data.
   *
   * @param data the data
   * @param from the from index (inclusive)
   * @param to the to index (exclusive)
   */
  protected void addInternal(double[] data, int from, int to) {
    for (int i = from; i < to; i++) {
      final double value = data[i];
      sum += value;
      sumSq += value * value;
    }
    size += (to - from);
  }

  /**
   * Add the data.
   *
   * @param data the data
   * @param from the from index (inclusive)
   * @param to the to index (exclusive)
   */
  protected void addInternal(int[] data, int from, int to) {
    for (int i = from; i < to; i++) {
      final double value = data[i];
      sum += value;
      sumSq += value * value;
    }
    size += (to - from);
  }

  /**
   * Add the value.
   *
   * @param value the value
   */
  protected void addInternal(final double value) {
    size++;
    sum += value;
    sumSq += value * value;
  }

  /**
   * Add the value n times.
   *
   * @param n The number of times
   * @param value The value
   */
  protected void addInternal(int n, double value) {
    this.size += n;
    sum += n * value;
    sumSq += n * value * value;
  }

  /**
   * Add the data. Synchronized for thread safety.
   *
   * @param data the data
   */
  public void safeAdd(float[] data) {
    synchronized (this) {
      add(data);
    }
  }

  /**
   * Add the data. Synchronized for thread safety.
   *
   * @param data the data
   */
  public void safeAdd(double[] data) {
    synchronized (this) {
      add(data);
    }
  }

  /**
   * Add the data. Synchronized for thread safety.
   *
   * @param data the data
   */
  public void safeAdd(int[] data) {
    synchronized (this) {
      add(data);
    }
  }

  /**
   * Add the value. Synchronized for thread safety.
   *
   * @param value the value
   */
  public void safeAdd(final double value) {
    synchronized (this) {
      addInternal(value);
    }
  }

  /**
   * Add the value n times. Synchronized for thread safety.
   *
   * @param n the n
   * @param value the value
   */
  public void safeAdd(int n, final double value) {
    synchronized (this) {
      addInternal(n, value);
    }
  }

  /**
   * Add the statistics to the data. Synchronized for thread safety.
   *
   * @param statistics the statistics
   */
  public void safeAdd(Statistics statistics) {
    synchronized (this) {
      add(statistics);
    }
  }

  /**
   * Gets the number of data points.
   *
   * @return The number of data points
   */
  public int getN() {
    return size;
  }

  /**
   * Gets the sum of the data points.
   *
   * <p>Note: This returns zero when no data has been added. Test the statistics are valid using
   * {@link #getN()}.
   *
   * @return The sum of the data points
   */
  public double getSum() {
    return sum;
  }

  /**
   * Gets the sum of squares of the data points.
   *
   * <p>Note: This returns zero when no data has been added. Test the statistics are valid using
   * {@link #getN()}.
   *
   * @return The sum of squares of the data points
   */
  public double getSumOfSquares() {
    return sumSq;
  }

  /**
   * Gets the mean of the data points.
   *
   * <p>Note: This returns NaN when no data has been added. Test the statistics are valid using
   * {@link #getN()}.
   *
   * @return The mean of the data points
   */
  public double getMean() {
    return sum / size;
  }

  /**
   * Gets the standard deviation of the data points.
   *
   * <p>Note: This returns NaN when no data has been added. Test the statistics are valid using
   * {@link #getN()}.
   *
   * @return The unbiased standard deviation of the data points
   */
  public double getStandardDeviation() {
    if (size == 0) {
      return Double.NaN;
    }
    double stdDev = sumSq - (sum * sum) / size;
    if (stdDev > 0) {
      stdDev = Math.sqrt(stdDev / (size - 1));
    } else {
      stdDev = 0.0;
    }
    return stdDev;
  }

  /**
   * Gets the variance of the data points.
   *
   * <p>Note: This returns NaN when no data has been added. Test the statistics are valid using
   * {@link #getN()}.
   *
   * @return The unbiased variance of the data points
   */
  public double getVariance() {
    if (size == 0) {
      return Double.NaN;
    }
    double variance = sumSq - (sum * sum) / size;
    if (variance > 0) {
      variance = variance / (size - 1);
    } else {
      variance = 0.0;
    }
    return variance;
  }

  /**
   * The standard error is the standard deviation of the sample-mean's estimate of a population
   * mean.
   *
   * <p>Uses the unbiased standard deviation divided by the square root of the sample size.
   *
   * <p>Note: This returns NaN when no data has been added. Test the statistics are valid using
   * {@link #getN()}.
   *
   * @return The standard error
   */
  public double getStandardError() {
    if (size > 0) {
      return getStandardDeviation() / Math.sqrt(size);
    }
    return Double.NaN;
  }

  /**
   * Gets the confidence interval around the mean using the given confidence level. This is computed
   * using the critical value from the two-sided T-distribution multiplied by the standard error.
   *
   * <p>If the number of samples is less than 2 then the result is positive infinity. If the
   * confidence level is one then the result is positive infinity. If the confidence level is zero
   * then the result is 0.
   *
   * @param confidenceLevel the confidence level of the test (in the range 0-1)
   * @return the confidence interval
   * @throws IllegalArgumentException if the confidence level is not in the range 0-1
   * @see "https://en.wikipedia.org/wiki/Confidence_interval#Basic_steps"
   */
  public double getConfidenceInterval(double confidenceLevel) {
    if (size <= 1) {
      return Double.POSITIVE_INFINITY;
    }
    if (confidenceLevel < 0 || confidenceLevel > 1) {
      throw new IllegalArgumentException("Confidence level must be in the range 0-1");
    }
    final double se = getStandardError();
    final double alpha = 1 - (1 - confidenceLevel) * 0.5; // Two-sided, e.g. 0.95 -> 0.975
    final int degreesOfFreedom = size - 1;
    final TDistribution t = new TDistribution(degreesOfFreedom);
    return t.inverseCumulativeProbability(alpha) * se;
  }

  /**
   * Reset the statistics.
   */
  public void reset() {
    size = 0;
    sum = 0;
    sumSq = 0;
  }
}
