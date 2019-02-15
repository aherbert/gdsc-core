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
 * Copyright (C) 2011 - 2019 Alex Herbert
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

import uk.ac.sussex.gdsc.core.data.NotImplementedException;

/**
 * Simple class to calculate the mean and standard deviation of data using a rolling algorithm. This
 * should be used when the numbers are large, e.g. 10^9 + 4, 10^9 + 7, 10^9 + 13, 10^9 + 16.
 *
 * <p>Based on org.apache.commons.math3.stat.descriptive.moment.SecondMoment.
 */
public class RollingStatistics extends Statistics {

  /**
   * Instantiates a new rolling statistics.
   *
   * @param data the data
   * @return the rolling statistics
   */
  public static RollingStatistics create(float[] data) {
    final RollingStatistics stats = new RollingStatistics();
    stats.add(data);
    return stats;
  }

  /**
   * Instantiates a new rolling statistics.
   *
   * @param data the data
   * @return the rolling statistics
   */
  public static RollingStatistics create(double[] data) {
    final RollingStatistics stats = new RollingStatistics();
    stats.add(data);
    return stats;
  }

  /**
   * Instantiates a new rolling statistics.
   *
   * @param data the data
   * @return the rolling statistics
   */
  public static RollingStatistics create(int[] data) {
    final RollingStatistics stats = new RollingStatistics();
    stats.add(data);
    return stats;
  }

  @Override
  protected void addInternal(float[] data, int from, int to) {
    for (int i = from; i < to; i++) {
      addInternal(data[i]);
    }
  }

  @Override
  protected void addInternal(double[] data, int from, int to) {
    for (int i = from; i < to; i++) {
      addInternal(data[i]);
    }
  }

  @Override
  protected void addInternal(int[] data, int from, int to) {
    for (int i = from; i < to; i++) {
      addInternal(data[i]);
    }
  }

  @Override
  protected void addInternal(final double value) {
    // This has changed the meaning of the inherited values sum and sumSq
    // sum -> mean
    // sum-squares -> sum (x-mean)^2
    // See https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online_algorithm
    // This has been adapted from org.apache.commons.math3.stat.descriptive.moment.SecondMoment
    final double delta = value - sum;
    final double nB = size;
    size++;
    final double deltaOverSize = delta / size;
    sum += deltaOverSize;
    sumSq += nB * delta * deltaOverSize;
  }

  @Override
  protected void addInternal(int n, double value) {
    // Note: for the input mean value the
    // deviation from mean is 0 (ss=0)
    final double delta = value - sum;
    final int nB = size;
    size += n;
    sum = (n * value + nB * sum) / size;
    sumSq += delta * delta * n * nB / size;
  }

  @Override
  public double getSum() {
    return sum * size;
  }

  @Override
  public double getSumOfSquares() {
    throw new NotImplementedException("Sum-of-squares not computed");
  }

  @Override
  public double getMean() {
    return sum;
  }

  @Override
  public double getStandardDeviation() {
    if (size > 1) {
      return Math.sqrt(sumSq / (size - 1));
    }
    return (size == 0) ? Double.NaN : 0;
  }

  @Override
  public double getVariance() {
    if (size > 1) {
      return sumSq / (size - 1);
    }
    return (size == 0) ? Double.NaN : 0;
  }

  @Override
  public void add(Statistics statistics) {
    if (statistics instanceof RollingStatistics) {
      final RollingStatistics extra = (RollingStatistics) statistics;
      if (extra.size > 0) {
        // https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Parallel_algorithm
        final double delta = extra.sum - sum;
        final int nA = extra.size;
        final int nB = size;
        size += nA;
        sum = (nA * extra.sum + nB * sum) / size;
        sumSq += extra.sumSq + delta * delta * nA * nB / size;
      }
      return;
    }
    throw new NotImplementedException("Not a RollingStatistics instance");
  }
}
