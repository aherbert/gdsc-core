/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2023 Alex Herbert
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

import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;

/**
 * Compute sum using an area region of an 2D data frame.
 */
public class DoubleAreaSum extends AreaStatistics {
  /** The data. */
  protected final double[] data;
  /** The rolling sum table. */
  protected double[] rollingSum;

  /**
   * Instantiates a new area sum.
   *
   * @param data the data
   * @param maxx the maxx
   * @param maxy the maxy
   * @throws IllegalArgumentException if maxx * maxy != data.length or data is null or length zero
   */
  DoubleAreaSum(double[] data, int maxx, int maxy) {
    super(maxx, maxy);
    SimpleArrayUtils.hasData2D(maxx, maxy, data);
    this.data = data;
  }

  /**
   * Create a new area sum wrapping the provided data.
   *
   * @param data the data
   * @param maxx the maxx
   * @param maxy the maxy
   * @return the area sum
   * @throws IllegalArgumentException if maxx * maxy != data.length or data is null or length zero
   */
  public static DoubleAreaSum wrap(double[] data, int maxx, int maxy) {
    return new DoubleAreaSum(data, maxx, maxy);
  }

  /**
   * Gets the result for an area covering only 1 pixel.
   *
   * @param x the x
   * @param y the y
   * @return the single result
   */
  @Override
  protected double[] getSingleResult(int x, int y) {
    return getResults(data[getIndex(x, y)], 1);
  }

  /**
   * Calculate the rolling sum tables.
   */
  @Override
  protected void calculateRollingSums() {
    if (rollingSum != null) {
      return;
    }

    // Compute the rolling sum and sum of squares
    // s(u,v) = f(u,v) + s(u-1,v) + s(u,v-1) - s(u-1,v-1)
    // where s(u,v) = 0 when either u,v < 0

    rollingSum = new double[data.length];

    // First row
    double columnSum = 0; // Column sum
    for (int i = 0; i < maxx; i++) {
      columnSum += data[i];
      rollingSum[i] = columnSum;
    }

    // Remaining rows:
    // sum = rolling sum of row + sum of row above
    for (int y = 1; y < maxy; y++) {
      int index = y * maxx;
      columnSum = 0;

      // Remaining columns
      for (int x = 0; x < maxx; x++, index++) {
        columnSum += data[index];
        rollingSum[index] = rollingSum[index - maxx] + columnSum;
      }
    }
  }

  /**
   * Gets the statistics within a region from minU to maxU and minV to maxV. Lower bounds exclusive
   * and upper bounds inclusive.
   *
   * <p>Use the rolling sum table.
   *
   * @param minU the min U
   * @param maxU the max U
   * @param minV the min V
   * @param maxV the max V
   * @return the statistics
   */
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

    // + s(u+N-1,v+N-1)
    double sum = rollingSum[maxV * maxx + maxU];

    if (minU >= 0) {
      // - s(u-1,v+N-1)
      sum -= rollingSum[maxV * maxx + minU];

      if (minV >= 0) {
        // - s(u+N-1,v-1)
        sum -= rollingSum[minV * maxx + maxU];

        // + s(u-1,v-1)
        sum += rollingSum[minV * maxx + minU];
      }
    } else if (minV >= 0) {
      // - s(u+N-1,v-1)
      sum -= rollingSum[minV * maxx + maxU];
    }

    final int count = (maxU - minU) * (maxV - minV);

    return getResults(sum, count);
  }

  /**
   * Gets the statistics within a region from minU to maxU and minV to maxV. Lower bounds inclusive
   * and upper bounds exclusive.
   *
   * @param minU the min U
   * @param maxU the max U
   * @param minV the min V
   * @param maxV the max V
   * @return the statistics
   */
  @Override
  protected double[] getStatisticsSimple(int minU, int maxU, int minV, int maxV) {
    double sum = 0;
    for (int y = minV; y < maxV; y++) {
      for (int x = minU, i = getIndex(minU, y); x < maxU; x++, i++) {
        sum += data[i];
      }
    }

    final int count = (maxU - minU) * (maxV - minV);

    return getResults(sum, count);
  }
}
