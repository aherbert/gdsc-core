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

/**
 * Provides sampling from a 1D histogram.
 *
 * <p>Adapted from The GNU Scientific library (http://www.gnu.org/software/gsl/)
 */
public class Pdf1D {
  private final double[] sum;

  /**
   * The cumulative sum of the original input data.
   */
  private final double cumulativeSum;

  /**
   * Default constructor. Assumes the range increments from zero in integers.
   *
   * @param data The data
   * @throws IllegalArgumentException if the input data length is zero, contains negatives or is all
   *         zero
   */
  public Pdf1D(double[] data) {
    if (data == null || data.length < 1) {
      throw new IllegalArgumentException("Input data must be at least 1");
    }

    this.sum = new double[data.length + 1];

    double mean = 0;
    double total = 0;

    for (int i = 0; i < data.length; i++) {
      if (data[i] < 0) {
        throw new IllegalArgumentException("Histogram bins must be non-negative");
      }
      mean += (data[i] - mean) / (i + 1);
      total += data[i];
    }

    if (total == 0) {
      throw new IllegalArgumentException("Histogram is empty");
    }

    cumulativeSum = total;

    this.sum[0] = 0;

    total = 0;
    for (int i = 0; i < data.length; i++) {
      total += (data[i] / mean) / data.length;
      this.sum[i + 1] = total;
    }
  }

  /**
   * Sample from the PDF using a uniform random number (in the range 0 inclusive to 1 exclusive).
   *
   * @param r1 the random number
   * @return the sample (or -1 on error)
   */
  public double sample(double r1) {
    /*
     * Wrap the exclusive top of the bin down to the inclusive bottom of the bin. Since this is a
     * single point it should not affect the distribution.
     */

    if (r1 >= 1.0 || r1 < 0) {
      r1 = 0.0;
    }

    final int k = find(r1);

    if (k == -1) {
      return -1;
    }

    // Assume the x-range and y-range increment from zero in integers.
    final double delta = (r1 - sum[k]) / (sum[k + 1] - sum[k]);

    return k + delta;
  }

  private int find(double x) {
    if (x >= sum[sum.length - 1]) {
      return -1;
    }

    /* perform binary search */

    int upper = sum.length - 1;
    int lower = 0;

    while (upper - lower > 1) {
      final int mid = (upper + lower) >>> 1;

      if (x >= sum[mid]) {
        lower = mid;
      } else {
        upper = mid;
      }
    }

    /* sanity check the result */

    if (x < sum[lower] || x >= sum[lower + 1]) {
      return -1;
    }

    return lower;
  }

  /**
   * Gets the cumulative sum of the original input data.
   *
   * @return the cumulative sum
   */
  public double getCumulative() {
    return cumulativeSum;
  }
}
