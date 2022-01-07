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

package uk.ac.sussex.gdsc.core.math;

/**
 * Interface to calculate the mean (first raw moment) and variance (second central moment) of a 2D
 * data array. The mean and variance will be computed for each column index in the 2D array by
 * processing rows incrementally.
 */
public interface ArrayMoment {
  /**
   * Add the data. The first set of data defines the number of individual moments to compute. All
   * subsequent data must be the same size, e.g an array of length n will compute n first and second
   * moments for the range {@code 0 <= i < n}.
   *
   * @param data the data
   */
  void add(double[] data);

  /**
   * Add the data. The first set of data defines the number of individual moments to compute. All
   * subsequent data must be the same size, e.g an array of length n will compute n first and second
   * moments for the range {@code 0 <= i < n}.
   *
   * @param data the data
   */
  void add(float[] data);

  /**
   * Add the data. The first set of data defines the number of individual moments to compute. All
   * subsequent data must be the same size, e.g an array of length n will compute n first and second
   * moments for the range {@code 0 <= i < n}.
   *
   * @param data the data
   */
  void add(int[] data);

  /**
   * Add the data. The first set of data defines the number of individual moments to compute. All
   * subsequent data must be the same size, e.g an array of length n will compute n first and second
   * moments for the range {@code 0 <= i < n}.
   *
   * @param data the data
   */
  void add(short[] data);

  /**
   * Add the data. The first set of data defines the number of individual moments to compute. All
   * subsequent data must be the same size, e.g an array of length n will compute n first and second
   * moments for the range {@code 0 <= i < n}.
   *
   * @param data the data
   */
  void add(byte[] data);

  /**
   * Adds the data in the array moment. This can throw an illegal argument exception is the input is
   * not compatible.
   *
   * @param arrayMoment the array moment
   */
  void add(ArrayMoment arrayMoment);

  /**
   * Add the data. The first set of data defines the number of individual moments to compute. All
   * subsequent data must be the same size, e.g an array of length n will compute n first and second
   * moments for the range {@code 0 <= i < n}.
   *
   * @param data the data
   */
  void addUnsigned(short[] data);

  /**
   * Add the data. The first set of data defines the number of individual moments to compute. All
   * subsequent data must be the same size, e.g an array of length n will compute n first and second
   * moments for the range {@code 0 <= i < n}.
   *
   * @param data the data
   */
  void addUnsigned(byte[] data);

  /**
   * Gets the arithmetic mean.
   *
   * @return the mean (empty array if no data has been added)
   */
  double[] getMean();

  /**
   * Gets the sum of squared deviations from the sample mean.
   *
   * @return the sum of squared deviations from the sample mean (empty array if no data has been
   *         added)
   */
  double[] getSumOfSquares();

  /**
   * Gets the number of observations.
   *
   * @return the n
   */
  long getN();

  /**
   * Gets the unbiased estimate of the variance.
   *
   * @return the variance (empty array if no data has been added)
   * @see #getVariance(boolean)
   */
  default double[] getVariance() {
    return getVariance(true);
  }

  /**
   * Gets the estimate of the variance.
   *
   * <p>The unbiased sample variance formula is:
   *
   * <pre>
   * variance = sum((x_i - mean) ^ 2) / (n - 1)
   * </pre>
   *
   * <p>The biased population variance formula is:
   *
   * <pre>
   * variance = sum((x_i - mean) ^ 2) / n
   * </pre>
   *
   * @param isBiasCorrected Set to true to be bias corrected, i.e. unbiased sample variance;
   *        otherwise the biased population variance is computed.
   * @return the variance (empty array if no data has been added)
   */
  double[] getVariance(boolean isBiasCorrected);

  /**
   * Gets the unbiased estimate of the standard deviation.
   *
   * <p>This will be the square root of the unbiased sample variance.
   *
   * @return the standard deviation (empty array if no data has been added)
   * @see #getVariance(boolean)
   */
  default double[] getStandardDeviation() {
    return getStandardDeviation(true);
  }

  /**
   * Gets the estimate of the standard deviation.
   *
   * <p>This will be the square root of the biased population variance.
   *
   * @param isBiasCorrected Set to true to be bias corrected, i.e. unbiased
   * @return the standard deviation (empty array if no data has been added)
   * @see #getVariance(boolean)
   */
  double[] getStandardDeviation(boolean isBiasCorrected);

  /**
   * Create a new instance.
   *
   * @return the array moment
   */
  ArrayMoment newInstance();
}
