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

package uk.ac.sussex.gdsc.core.math;

import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

/**
 * Calculate the mean and sum of squared deviations (ss) from the mean.
 *
 * <pre>
 * mean = sum(x) / n
 * ss = sum(x - mean)^2
 * </pre>
 *
 * <p>The values are computed using a rolling algorithm.
 *
 * <p>The sum of squared deviations can be used to compute the variance and standard deviation, for
 * example for the unbiased estimates:
 *
 * <pre>
 * variance = ss / (n - 1)
 * standard deviation = sqrt( ss / (n - 1) )
 * </pre>
 */
public final class SumOfSquaredDeviations {
  /** The number of values that have been added. */
  private long size;

  /** The mean of values that have been added. */
  private double mean;

  /** The sum of squared deviations from the mean. */
  private double ss;

  /**
   * Create an instance.
   */
  public SumOfSquaredDeviations() {
    // Do nothing
  }

  /**
   * Create an instance.
   *
   * @param size the number of values that have been added
   * @param mean the mean (ignored when the size is zero)
   * @param ss the sum of squared deviations from the mean (ignored when the size is zero)
   * @throws IllegalArgumentException if the size or the sum of squared deviations are negative
   */
  public SumOfSquaredDeviations(long size, double mean, double ss) {
    ValidationUtils.checkPositive(size, "size");
    this.size = size;
    if (size != 0) {
      ValidationUtils.checkPositive(ss, "ss");
      this.mean = mean;
      this.ss = ss;
    }
  }

  /**
   * Create a copy instance.
   *
   * @param source the source to copy
   */
  private SumOfSquaredDeviations(SumOfSquaredDeviations source) {
    this.size = source.size;
    this.mean = source.mean;
    this.ss = source.ss;
  }

  /**
   * Adds the value.
   *
   * @param value the value
   */
  public void add(double value) {
    final long nm1 = size;
    final double mean1 = mean;
    final long n = nm1 + 1;
    final double delta = value - mean1;
    final double deltaOverN = delta / n;
    mean = mean1 + deltaOverN;
    ss = ss + nm1 * delta * deltaOverN;
    size = n;
  }

  /**
   * Adds the other instance.
   *
   * @param other the other instance
   */
  public void add(SumOfSquaredDeviations other) {
    final long n1 = size;
    final double mean1 = mean;
    final double ss1 = ss;
    final long n2 = other.size;
    final double mean2 = other.mean;
    final double ss2 = other.ss;
    // Adapted from
    // org.apache.commons.math3.stat.regression.SimpleRegression.append(SimpleRegression)
    final long n = n1 + n2;
    final double f = n2 / (double) n;
    final double delta = mean2 - mean1;
    mean = mean1 + delta * f;
    ss = ss1 + ss2 + delta * delta * f * n1;
    size = n;
  }

  /**
   * Gets the arithmetic mean. Return NaN if no values have been added.
   *
   * @return the arithmetic mean
   */
  public double getMean() {
    return size == 0 ? Double.NaN : mean;
  }

  /**
   * Gets the sum of squared deviations from the mean. Return NaN if no values have been added.
   *
   * @return the sum of squared deviations from the mean
   */
  public double getSumOfSquaredDeviations() {
    return size == 0 ? Double.NaN : ss;
  }

  /**
   * Gets the number of values that have been added.
   *
   * @return the number of values that have been added
   */
  public long getN() {
    return size;
  }

  /**
   * Gets the bias corrected variance. This is the sum of squared deviations from the mean divided
   * by the size - 1.
   *
   * <p>Returns NaN if no values have been added, and zero if one value has been added.
   *
   * @return the variance
   */
  public double getVariance() {
    if (size == 0) {
      return Double.NaN;
    }
    if (size == 1) {
      return 0;
    }
    return ss / (size - 1);
  }

  /**
   * Gets the bias corrected standard deviation. This is the square root of the sum of squared
   * deviations from the mean divided by the size - 1.
   *
   * <p>Returns NaN if no values have been added, and zero if one value has been added.
   *
   * @return the standard deviation
   */
  public double getStandardDeviation() {
    if (size == 0) {
      return Double.NaN;
    }
    if (size == 1) {
      return 0;
    }
    return Math.sqrt(ss / (size - 1));
  }

  /**
   * Create a copy.
   *
   * @return a copy
   */
  public SumOfSquaredDeviations copy() {
    return new SumOfSquaredDeviations(this);
  }
}
