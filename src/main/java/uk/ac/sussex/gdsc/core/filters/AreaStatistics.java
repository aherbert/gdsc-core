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

package uk.ac.sussex.gdsc.core.filters;

import java.awt.Rectangle;

/**
 * Compute statistics using an area region of an 2D data frame.
 *
 * <p>The algorithm can use a rolling sum table. Variants exist for processing different data types
 * and computing different statistics. Statistics that are not computed will have a default value.
 */
public abstract class AreaStatistics {
  /** The index of the count in the results. */
  public static final int INDEX_COUNT = 0;
  /**
   * The index of the sum in the results.
   *
   * <p>Defaults to {@link Double#isNaN()}.
   */
  public static final int INDEX_SUM = 1;
  /**
   * The index of the standard deviation in the results.
   *
   * <p>Defaults to {@link Double#isNaN()}.
   */
  public static final int INDEX_SD = 2;

  /** The result when there is no area to analyse. */
  private static final double[] NO_AREA_RESULT;

  static {
    NO_AREA_RESULT = new double[3];
    NO_AREA_RESULT[INDEX_SUM] = Double.NaN;
    NO_AREA_RESULT[INDEX_SD] = Double.NaN;
  }

  /** Set to true if using a rolling sum table algorithm. */
  private boolean rollingSums;

  /** The max x dimension. */
  public final int maxx;
  /** The max y dimension. */
  public final int maxy;

  /**
   * Instantiates a new area statistics.
   *
   * @param maxx the maxx
   * @param maxy the maxy
   */
  AreaStatistics(int maxx, int maxy) {
    this.maxx = maxx;
    this.maxy = maxy;
  }

  /**
   * Gets the statistics within a region +/- n.
   *
   * <p>Statistics can be accessed using the static properties in this class. Note that some
   * implementation may not compute all the statistics.
   *
   * @param x the x
   * @param y the y
   * @param n the n
   * @return the statistics
   */
  public double[] getStatistics(int x, int y, int n) {
    // Bounds check
    if (x < 0 || y < 0 || x >= maxx || y >= maxy || n < 0) {
      return getNoAreaResult();
    }
    // Special case for 1 data point
    if (n == 0) {
      return getSingleResult(x, y);
    }
    // Lower bounds inclusive
    final int minU = x - n;
    final int minV = y - n;
    // Upper bounds inclusive
    final int maxU = x + n;
    final int maxV = y + n;
    return getStatisticsInternal(minU, maxU, minV, maxV);
  }

  /**
   * Gets the statistics within a region +/- n.
   *
   * <p>Statistics can be accessed using the static properties in this class. Note that some
   * implementation may not compute all the statistics.
   *
   * @param x the x
   * @param y the y
   * @param nx the nx
   * @param ny the ny
   * @return the statistics
   */
  public double[] getStatistics(int x, int y, int nx, int ny) {
    // Bounds check
    if (x < 0 || y < 0 || x >= maxx || y >= maxy || nx < 0 || ny < 0) {
      return getNoAreaResult();
    }
    // Special case for 1 data point
    if (nx == 0 && ny == 0) {
      return getSingleResult(x, y);
    }
    // Lower bounds inclusive
    final int minU = x - nx;
    final int minV = y - ny;
    // Upper bounds inclusive
    final int maxU = x + nx;
    final int maxV = y + ny;
    return getStatisticsInternal(minU, maxU, minV, maxV);
  }

  /**
   * Gets the statistics within a region.
   *
   * <p>Statistics can be accessed using the static properties in this class. Note that some
   * implementation may not compute all the statistics.
   *
   * @param region the region
   * @return the statistics
   */
  public double[] getStatistics(Rectangle region) {
    // Upper bounds inclusive
    final int maxU = region.x + region.width - 1;
    final int maxV = region.y + region.height - 1;
    // Bounds check
    if (region.width <= 0 || region.height <= 0 || region.x >= maxx || region.y >= maxy || maxU < 0
        || maxV < 0) {
      return getNoAreaResult();
    }
    // Lower bounds inclusive
    final int minU = region.x;
    final int minV = region.y;
    return getStatisticsInternal(minU, maxU, minV, maxV);
  }

  /**
   * Gets the result for an area covering 0 pixels.
   *
   * @return the no area result
   */
  protected static double[] getNoAreaResult() {
    return NO_AREA_RESULT.clone();
  }

  /**
   * Gets the result for an area covering only 1 pixel.
   *
   * @param x the x
   * @param y the y
   * @return the single result
   */
  protected abstract double[] getSingleResult(int x, int y);

  /**
   * Gets the results.
   *
   * @param sum the sum
   * @param count the count
   * @return the results
   */
  protected static double[] getResults(double sum, int count) {
    final double[] stats = getNoAreaResult();
    stats[INDEX_COUNT] = count;
    stats[INDEX_SUM] = sum;
    return stats;
  }

  /**
   * Gets the results.
   *
   * @param sum the sum
   * @param sumSquares the sum squares
   * @param count the count
   * @return the results
   */
  protected static double[] getResults(double sum, double sumSquares, int count) {
    final double[] stats = getNoAreaResult();
    stats[INDEX_COUNT] = count;
    stats[INDEX_SUM] = sum;
    stats[INDEX_SD] = getStandardDeviation(sum, sumSquares, count);
    return stats;
  }

  private static double getStandardDeviation(double sum, double sumSquares, int count) {
    // Note: We do not consider count==0 since the methods are not called with an empty region
    if (count > 1) {
      // Get the sum of squared differences
      final double residuals = sumSquares - (sum * sum) / count;
      if (residuals > 0) {
        return Math.sqrt(residuals / (count - 1));
      }
    }
    // Either count == 1 or no residuals
    return 0;
  }

  /**
   * Gets the statistics within a region from minU to maxU and minV to maxV. Lower bounds inclusive
   * and upper bounds inclusive.
   *
   * @param minU the min U
   * @param maxU the max U
   * @param minV the min V
   * @param maxV the max V
   * @return the statistics
   */
  private double[] getStatisticsInternal(int minU, int maxU, int minV, int maxV) {
    // Note that the two methods use different bounds for their implementation
    if (rollingSums) {
      calculateRollingSums();
      // Clip the ranges.
      // Lower bounds exclusive, Upper inclusive.
      final int x1 = Math.max(-1, minU - 1);
      final int x2 = Math.min(maxx - 1, maxU);
      final int y1 = Math.max(-1, minV - 1);
      final int y2 = Math.min(maxy - 1, maxV);
      return getStatisticsRollingSums(x1, x2, y1, y2);
    }
    // Clip the ranges.
    // Lower bounds inclusive, Upper exclusive.
    final int x1 = Math.max(0, minU);
    final int x2 = Math.min(maxx, maxU + 1);
    final int y1 = Math.max(0, minV);
    final int y2 = Math.min(maxy, maxV + 1);
    return getStatisticsSimple(x1, x2, y1, y2);
  }

  /**
   * Calculate the rolling sum tables.
   */
  protected abstract void calculateRollingSums();

  /**
   * Gets the statistics within a region from minU to maxU and minV to maxV. Lower bounds exclusive
   * and upper bounds inclusive.
   *
   * <p>Use the rolling sum table.
   *
   * @param minU the min U (in the range [-1 to maxx-1])
   * @param maxU the max U (in the range [-1 to maxx-1])
   * @param minV the min V (in the range [-1 to maxy-1])
   * @param maxV the max V (in the range [-1 to maxy-1])
   * @return the statistics
   */
  protected abstract double[] getStatisticsRollingSums(int minU, int maxU, int minV, int maxV);

  /**
   * Gets the statistics within a region from minU to maxU and minV to maxV. Lower bounds inclusive
   * and upper bounds exclusive.
   *
   * @param minU the min U (in the range [0 to maxx])
   * @param maxU the max U (in the range [0 to maxx])
   * @param minV the min V (in the range [0 to maxy])
   * @param maxV the max V (in the range [0 to maxy])
   * @return the statistics
   */
  protected abstract double[] getStatisticsSimple(int minU, int maxU, int minV, int maxV);

  /**
   * Gets the index in the 2D data.
   *
   * @param x the x
   * @param y the y
   * @return the index
   */
  protected int getIndex(int x, int y) {
    return y * maxx + x;
  }

  /**
   * Checks if using a rolling sum table. This is faster for repeat calls over large areas.
   *
   * @return true, if using a rolling sum table
   */
  public boolean isRollingSums() {
    return rollingSums;
  }

  /**
   * Set to true to use a rolling sum table. This is faster for repeat calls over large areas.
   *
   * @param rollingSums the new rolling sums
   */
  public void setRollingSums(boolean rollingSums) {
    this.rollingSums = rollingSums;
  }
}
