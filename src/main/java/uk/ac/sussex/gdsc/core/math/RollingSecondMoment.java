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

package uk.ac.sussex.gdsc.core.math;

import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

/**
 * Simple class to calculate the mean of data using a rolling algorithm.
 *
 * <p>For each additional value, update the first moment using:
 *
 * <pre>
 * m = m + (new value - m) / (number of observations)
 * </pre>
 *
 * <p>The following recursive updating formula is used for the second moment:
 *
 * <p>Let
 * 
 * <ul>
 * 
 * <li>dev = (current obs - previous mean)</li>
 * 
 * <li>n = number of observations (including current obs)</li>
 * 
 * </ul>
 *
 * <p>Then:
 *
 * <pre>
 * new value = old value + dev^2 * (n - 1) / n.
 * </pre>
 */
public final class RollingSecondMoment {
  /** The number of values that have been added. */
  private long size;

  /** First moment of values that have been added. */
  private double m1;

  /** Second moment of values that have been added. */
  private double m2;

  /**
   * Create an instance.
   */
  public RollingSecondMoment() {
    // Do nothing
  }

  /**
   * Create an instance.
   *
   * @param size the number of values that have been added
   * @param m1 the first moment (ignored when the size is zero)
   * @param m2 the second moment (ignored when the size is zero)
   * @throws IllegalArgumentException if the size is negative
   */
  public RollingSecondMoment(long size, double m1, double m2) {
    ValidationUtils.checkPositive(size, "size");
    this.size = size;
    if (size != 0) {
      this.m1 = m1;
      this.m2 = m2;
    }
  }

  /**
   * Create a copy instance.
   *
   * @param source the source to copy
   */
  private RollingSecondMoment(RollingSecondMoment source) {
    this.size = source.size;
    this.m1 = source.m1;
    this.m2 = source.m2;
  }

  /**
   * Adds the value.
   *
   * @param value the value
   */
  public void add(double value) {
    final long nm1 = size;
    final double m1a = m1;
    final long n = nm1 + 1;
    final double delta = value - m1a;
    final double deltaOverN = delta / n;
    m1 = m1a + deltaOverN;
    m2 = m2 + nm1 * delta * deltaOverN;
    size = n;
  }

  /**
   * Adds the moment.
   *
   * @param moment the moment
   */
  public void add(RollingSecondMoment moment) {
    final long na = size;
    final double m1a = m1;
    final double m2a = m2;
    final long nb = moment.size;
    final double m1b = moment.m1;
    final double m2b = moment.m2;
    // Adapted from
    // org.apache.commons.math3.stat.regression.SimpleRegression.append(SimpleRegression)
    final long n = na + nb;
    final double f1 = nb / (double) n;
    final double delta = m1b - m1a;
    m1 = m1a + delta * f1;
    m2 = m2a + m2b + delta * delta * f1 * na;
    size = n;
  }

  /**
   * Gets the first moment. Return NaN if no values have been added.
   *
   * @return the first moment
   */
  public double getFirstMoment() {
    return size == 0 ? Double.NaN : m1;
  }

  /**
   * Gets the second moment. Return NaN if no values have been added.
   *
   * @return the second moment
   */
  public double getSecondMoment() {
    return size == 0 ? Double.NaN : m2;
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
   * Gets the bias corrected variance. This is the second moment divided by the size - 1. Returns
   * NaN if no values have been added, and zero if one value has been added.
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
    return m2 / (size - 1);
  }

  /**
   * Gets the bias corrected standard deviation. This is the square root of the second moment
   * divided by the size - 1. Returns NaN if no values have been added, and zero if one value has
   * been added.
   *
   * @return the variance
   */
  public double getStandardDeviation() {
    if (size == 0) {
      return Double.NaN;
    }
    if (size == 1) {
      return 0;
    }
    return Math.sqrt(m2 / (size - 1));
  }

  /**
   * Create a copy.
   *
   * @return a copy
   */
  public RollingSecondMoment copy() {
    return new RollingSecondMoment(this);
  }
}
