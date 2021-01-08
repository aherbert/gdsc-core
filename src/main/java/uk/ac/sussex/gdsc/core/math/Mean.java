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
 * Calculate the mean of data.
 *
 * <pre>
 * mean = sum(x) / n
 * </pre>
 *
 * <p>The value is computed using a rolling algorithm.
 */
public final class Mean {
  /** The number of values that have been added. */
  private long size;

  /** Mean of values that have been added. */
  private double mean;

  /**
   * Create an instance.
   */
  public Mean() {
    // Do nothing
  }

  /**
   * Create an instance.
   *
   * @param size the number of values that have been added
   * @param mean the mean (ignored when the size is zero)
   * @throws IllegalArgumentException if the size is negative
   */
  public Mean(long size, double mean) {
    ValidationUtils.checkPositive(size, "size");
    this.size = size;
    this.mean = size == 0 ? 0 : mean;
  }

  /**
   * Create a copy instance.
   *
   * @param source the source to copy
   */
  private Mean(Mean source) {
    this.size = source.size;
    this.mean = source.mean;
  }

  /**
   * Adds the value.
   *
   * @param value the value
   */
  public void add(double value) {
    final long n = size + 1;
    final double mean1 = mean;
    mean = mean1 + (value - mean1) / n;
    size = n;
  }

  /**
   * Adds the other instance.
   *
   * @param other the other instance
   */
  public void add(Mean other) {
    final long n1 = size;
    final double mean1 = mean;
    final long n2 = other.size;
    final double mean2 = other.mean;
    // Adapted from
    // org.apache.commons.math3.stat.regression.SimpleRegression.append(SimpleRegression)
    final long n = n1 + n2;
    final double f = n2 / (double) n;
    final double delta = mean2 - mean1;
    mean = mean1 + delta * f;
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
   * Gets the number of values that have been added.
   *
   * @return the number of values that have been added
   */
  public long getN() {
    return size;
  }

  /**
   * Create a copy.
   *
   * @return a copy
   */
  public Mean copy() {
    return new Mean(this);
  }
}
