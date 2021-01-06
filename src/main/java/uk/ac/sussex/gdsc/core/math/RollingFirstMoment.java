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
 * <p>Note: If the user desires to maintain a second moment then it advised to use the Apache class
 * org.apache.commons.math3.stat.descriptive.moment.SecondMoment.
 *
 * <p>For each additional value, update the first moment using:
 *
 * <pre>
 * m = m + (new value - m) / (number of observations)
 * </pre>
 */
public final class RollingFirstMoment {
  /** The number of values that have been added. */
  private long size;

  /** First moment of values that have been added. */
  private double m1;

  /**
   * Create an instance.
   */
  public RollingFirstMoment() {
    // Do nothing
  }

  /**
   * Create an instance.
   *
   * @param size the number of values that have been added
   * @param m1 the first moment
   * @throws IllegalArgumentException if the size is negative
   */
  public RollingFirstMoment(long size, double m1) {
    ValidationUtils.checkPositive(size, "size");
    this.size = size;
    this.m1 = m1;
  }

  /**
   * Adds the value.
   *
   * @param value the value
   */
  public void add(double value) {
    final long n = size + 1;
    final double m = m1;
    m1 = m + (value - m) / n;
    size = n;
  }

  /**
   * Adds the moment.
   *
   * @param moment the moment
   */
  public void add(RollingFirstMoment moment) {
    final long nb = moment.size;
    final double mb = moment.m1;
    // Weighted addition of the mean
    final long n = nb + size;
    final double m = m1;
    final double f1 = nb / (double) n;
    m1 = m + (mb - m) * f1;
    size = n;
  }

  /**
   * Gets the first moment. Return NaN if no values have been added.
   *
   * @return the first moment
   */
  public double getFirstMoment() {
    return (size == 0) ? Double.NaN : m1;
  }

  /**
   * Gets the number of values that have been added.
   *
   * @return the number of values that have been added
   */
  public long getN() {
    return size;
  }
}
