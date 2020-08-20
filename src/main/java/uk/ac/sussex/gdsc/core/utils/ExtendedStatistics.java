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

package uk.ac.sussex.gdsc.core.utils;

/**
 * Simple class to calculate the min, max, mean and standard deviation of data.
 */
public class ExtendedStatistics extends Statistics {
  /** The min. */
  private double min = Double.POSITIVE_INFINITY;

  /** The max. */
  private double max = Double.NEGATIVE_INFINITY;

  /**
   * Instantiates a new extended statistics.
   *
   * @param data the data
   * @return the stored data statistics
   */
  public static ExtendedStatistics create(float[] data) {
    final ExtendedStatistics stats = new ExtendedStatistics();
    stats.add(data);
    return stats;
  }

  /**
   * Instantiates a new extended statistics.
   *
   * @param data the data
   * @return the stored data statistics
   */
  public static ExtendedStatistics create(double[] data) {
    final ExtendedStatistics stats = new ExtendedStatistics();
    stats.add(data);
    return stats;
  }

  /**
   * Instantiates a new extended statistics.
   *
   * @param data the data
   * @return the stored data statistics
   */
  public static ExtendedStatistics create(int[] data) {
    final ExtendedStatistics stats = new ExtendedStatistics();
    stats.add(data);
    return stats;
  }

  @Override
  protected void addInternal(float[] data, int from, int to) {
    if (size == 0 && from < to) {
      min = max = data[from];
    }
    for (int i = from; i < to; i++) {
      final double value = data[i];
      sum += value;
      sumSq += value * value;
      updateMinMax(value);
    }
    size += (to - from);
  }

  @Override
  protected void addInternal(double[] data, int from, int to) {
    if (size == 0 && from < to) {
      min = max = data[from];
    }
    for (int i = from; i < to; i++) {
      final double value = data[i];
      sum += value;
      sumSq += value * value;
      updateMinMax(value);
    }
    size += (to - from);
  }

  @Override
  protected void addInternal(int[] data, int from, int to) {
    if (size == 0 && from < to) {
      min = max = data[from];
    }
    for (int i = from; i < to; i++) {
      final double value = data[i];
      sum += value;
      sumSq += value * value;
      updateMinMax(value);
    }
    size += (to - from);
  }

  @Override
  protected void addInternal(final double value) {
    if (size == 0) {
      min = max = value;
    } else {
      updateMinMax(value);
    }
    super.addInternal(value);
  }

  @Override
  protected void addInternal(int n, double value) {
    if (size == 0) {
      min = max = value;
    } else {
      updateMinMax(value);
    }
    super.addInternal(n, value);
  }

  /**
   * Update the min and max.
   *
   * <p>This should only be called when the count is above zero (i.e. min/max have been set with a
   * valid value).
   *
   * @param value the value
   */
  private void updateMinMax(final double value) {
    if (min > value) {
      min = value;
    } else if (max < value) {
      max = value;
    }
  }

  /**
   * Gets the minimum. Returns {@link Double#NaN } if no data has been added.
   *
   * @return the minimum
   */
  public double getMin() {
    return (size == 0) ? Double.NaN : min;
  }

  /**
   * Gets the maximum. Returns {@link Double#NaN } if no data has been added.
   *
   * @return the maximum
   */
  public double getMax() {
    return (size == 0) ? Double.NaN : max;
  }

  @Override
  public void add(Statistics statistics) {
    if (statistics instanceof ExtendedStatistics) {
      final ExtendedStatistics extra = (ExtendedStatistics) statistics;
      if (extra.size > 0) {
        size += statistics.size;
        sum += statistics.sum;
        sumSq += statistics.sumSq;
        if (min > extra.min) {
          min = extra.min;
        }
        if (max < extra.max) {
          max = extra.max;
        }
      }
      return;
    }
    throw new IllegalArgumentException("Not an ExtendedStatistics instance");
  }

  @Override
  public void reset() {
    super.reset();
    min = Double.POSITIVE_INFINITY;
    max = Double.NEGATIVE_INFINITY;
  }
}
