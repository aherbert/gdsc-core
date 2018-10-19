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

package uk.ac.sussex.gdsc.core.filters;

/**
 * Compute statistics using an area region of an 2D data frame.
 */
public class AreaStatistics extends AreaSum {
  /** The index of the standard deviation in the results. */
  public static final int INDEX_SD = 2;

  private static final double[] EMPTY;

  static {
    EMPTY = new double[3];
    EMPTY[INDEX_COUNT] = 0;
    EMPTY[INDEX_SUM] = Double.NaN;
    EMPTY[INDEX_SD] = Double.NaN;
  }

  /** The rolling sum of squares. */
  private double[] rollingSumSq = null;

  /**
   * Instantiates a new area statistics.
   *
   * @param data the data
   * @param maxx the maxx
   * @param maxy the maxy
   * @throws IllegalArgumentException if maxx * maxy != data.length or data is null or length zero
   */
  AreaStatistics(float[] data, int maxx, int maxy) {
    super(data, maxx, maxy);
  }

  /**
   * Create a new area statistics wrapping the provided data.
   *
   * @param data the data
   * @param maxx the maxx
   * @param maxy the maxy
   * @return the area statistics
   * @throws IllegalArgumentException if maxx * maxy != data.length or data is null or length zero
   */
  public static AreaStatistics wrap(float[] data, int maxx, int maxy) {
    return new AreaStatistics(data, maxx, maxy);
  }

  @Override
  protected void calculateRollingSums() {
    if (rollingSum != null) {
      return;
    }

    // Compute the rolling sum and sum of squares
    // s(u,v) = f(u,v) + s(u-1,v) + s(u,v-1) - s(u-1,v-1)
    // ss(u,v) = f(u,v) * f(u,v) + ss(u-1,v) + ss(u,v-1) - ss(u-1,v-1)
    // where s(u,v) = ss(u,v) = 0 when either u,v < 0

    rollingSum = new double[data.length];
    rollingSumSq = new double[data.length];

    // First row
    double columnSum = 0; // Column sum
    double columnSumSq = 0; // Column sum-squares
    for (int i = 0; i < maxx; i++) {
      final double value = data[i];
      columnSum += value;
      columnSumSq += value * value;
      rollingSum[i] = columnSum;
      rollingSumSq[i] = columnSumSq;
    }

    // Remaining rows:
    // sum = rolling sum of row + sum of row above
    for (int y = 1; y < maxy; y++) {
      int index = y * maxx;
      columnSum = 0;
      columnSumSq = 0;

      // Remaining columns
      for (int x = 0; x < maxx; x++, index++) {
        final double d = data[index];
        columnSum += d;
        columnSumSq += d * d;

        rollingSum[index] = rollingSum[index - maxx] + columnSum;
        rollingSumSq[index] = rollingSumSq[index - maxx] + columnSumSq;
      }
    }
  }

  @Override
  protected double[] getStatisticsRollingSums(int minU, int maxU, int minV, int maxV) {
    // Compute sum from rolling sum using:
    // sum(u,v) =
    // + s(u+N,v+N)
    // - s(u-N-1,v+N)
    // - s(u+N,v-N-1)
    // + s(u-N-1,v-N-1)
    // Note:
    // s(u,v) = 0 when either u,v < 0
    // s(u,v) = s(umax,v) when u>umax
    // s(u,v) = s(u,vmax) when v>vmax
    // s(u,v) = s(umax,vmax) when u>umax,v>vmax
    // Likewise for ss

    // Clip to limits
    if (maxU >= maxx) {
      maxU = maxx - 1;
    }
    if (maxV >= maxy) {
      maxV = maxy - 1;
    }

    // + s(u+N-1,v+N-1)
    int index = maxV * maxx + maxU;
    double sum = rollingSum[index];
    double sumSquares = rollingSumSq[index];

    if (minU >= 0) {
      // - s(u-1,v+N-1)
      index = maxV * maxx + minU;
      sum -= rollingSum[index];
      sumSquares -= rollingSumSq[index];

      if (minV >= 0) {
        // - s(u+N-1,v-1)
        index = minV * maxx + maxU;
        sum -= rollingSum[index];
        sumSquares -= rollingSumSq[index];

        // + s(u-1,v-1)
        index = minV * maxx + minU;
        sum += rollingSum[index];
        sumSquares += rollingSumSq[index];
      } else {
        // Reset to bounds to calculate the number of pixels
        minV = -1;
      }
    } else {
      // Reset to bounds to calculate the number of pixels
      minU = -1;

      if (minV >= 0) {
        // - s(u+N-1,v-1)
        index = minV * maxx + maxU;
        sum -= rollingSum[index];
        sumSquares -= rollingSumSq[index];

      } else {
        // Reset to bounds to calculate the number of pixels
        minV = -1;
      }
    }

    final int count = (maxU - minU) * (maxV - minV);

    return getResults(sum, sumSquares, count);
  }

  /**
   * Gets the results.
   *
   * @param sum the sum
   * @param sumSquares the sum squares
   * @param count the count
   * @return the results
   */
  private static double[] getResults(double sum, double sumSquares, int count) {
    final double[] stats = new double[3];

    stats[INDEX_COUNT] = count;
    // Note: We do not consider n==0 since the methods are not called with an empty region
    stats[INDEX_SUM] = sum;

    if (count > 1) {
      // Get the sum of squared differences
      final double residuals = sumSquares - (sum * sum) / count;
      if (residuals > 0.0) {
        stats[INDEX_SD] = Math.sqrt(residuals / (count - 1));
      }
    }

    return stats;
  }

  @Override
  protected double[] getSingleResult(int x, int y) {
    return new double[] {1, data[getIndex(x, y)], 0};
  }

  @Override
  protected double[] getStatisticsSimple(int minU, int maxU, int minV, int maxV) {
    // Clip to limits
    if (minU < 0) {
      minU = 0;
    }
    if (minV < 0) {
      minV = 0;
    }
    if (maxU > maxx) {
      maxU = maxx;
    }
    if (maxV > maxy) {
      maxV = maxy;
    }

    double sum = 0;
    double sumSquares = 0;
    for (int y = minV; y < maxV; y++) {
      for (int x = minU, i = getIndex(minU, y); x < maxU; x++, i++) {
        final double d = data[i];
        sum += d;
        sumSquares += d * d;
      }
    }

    final int n = (maxU - minU) * (maxV - minV);

    return getResults(sum, sumSquares, n);
  }
}
