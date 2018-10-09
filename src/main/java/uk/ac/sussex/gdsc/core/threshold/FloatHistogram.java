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
package uk.ac.sussex.gdsc.core.threshold;

import uk.ac.sussex.gdsc.core.threshold.AutoThreshold.Method;

import java.util.Arrays;

/**
 * Contains a histogram. <p> The histogram is implemented in this class using float bin values.
 */
public class FloatHistogram extends Histogram {
  /** The histogram bin values. */
  public final float[] value;

  /**
   * Instantiates a new float histogram.
   *
   * @param h the histogram
   * @param value the bin values
   * @param minBin the min bin
   * @param maxBin the max bin
   */
  private FloatHistogram(int[] h, float[] value, int minBin, int maxBin) {
    super(h, minBin, maxBin);
    this.value = value;
  }

  /**
   * Instantiates a new float histogram.
   *
   * @param value the value
   * @param h the histogram bin counts
   */
  public FloatHistogram(float[] value, int[] h) {
    super(h);
    this.value = value;
  }

  /**
   * Build a histogram using the input data values.
   *
   * <p>The input array is unchanged.
   * 
   * @param data The data
   * @param doSort True if the data should be sorted
   * @return The histogram
   * @see #buildHistogram(float[], boolean, boolean)
   */
  public static FloatHistogram buildHistogram(float[] data, boolean doSort) {
    return buildHistogram(data, doSort, false);
  }

  /**
   * Build a histogram using the input data values.
   * 
   * <p>Note that input data array is modified if the in-place option is specified. Otherwise the
   * input array is unchanged.
   *
   * @param data The data
   * @param doSort True if the data should be sorted
   * @param inPlace Set to true to use the data in-place
   * @return The histogram
   */
  public static FloatHistogram buildHistogram(float[] data, boolean doSort, boolean inPlace) {
    if (data == null || data.length == 0) {
      // Empty histogram
      return new FloatHistogram(new float[1], new int[1]);
    }

    // Create histogram values (optionally reusing the data in-place)
    float[] value = (inPlace) ? data : data.clone();
    int[] h = new int[data.length];

    if (doSort) {
      Arrays.sort(value);
    }

    float lastValue = value[0];
    int count = 0;

    int size = 0;

    for (int i = 0; i < value.length; i++) {
      if (lastValue != value[i]) {
        // Re-use the array in-place
        value[size] = lastValue;
        h[size++] = count;
        count = 0;
      }
      lastValue = value[i];
      count++;
    }
    // Final count
    value[size] = lastValue;
    h[size++] = count;

    // Truncate
    if (size < value.length) {
      h = Arrays.copyOf(h, size);
      value = Arrays.copyOf(value, size);
    }

    return new FloatHistogram(value, h);
  }

  /**
   * Compact the current float histogram into a histogram with the specified number of bins. The
   * returned histogram may be an integer histogram (if the data is integer and fits within the bin
   * range) or a float histogram. The returned histogram has evenly spaced bin widths.
   *
   * @param size the size
   * @return the new histogram
   * @see uk.ac.sussex.gdsc.core.threshold.Histogram#compact(int)
   */
  @Override
  public Histogram compact(int size) {
    if (minBin == maxBin) {
      return this;
    }
    final float min = getValue(minBin);
    final float max = getValue(maxBin);

    if ((int) min == min && (int) max == max && (max - min) <= size) {
      // Check if we can convert to integer histogram
      if (integerData()) {
        return integerHistogram(size);
      }
    }

    // Compress non-integer data
    final int size_1 = size - 1;
    final float binSize = (max - min) / size_1;
    final int[] newH = new int[size];
    for (int i = 0; i < h.length; i++) {
      final int bin = (int) ((getValue(i) - min) / binSize + 0.5);
      if (bin < 0) {
        newH[0] += h[i];
      } else if (bin >= size) {
        newH[size_1] += h[i];
      } else {
        newH[bin] += h[i];
      }
    }
    // Create the new values
    final float[] newValue = new float[size];
    for (int i = 0; i < size; i++) {
      newValue[i] = min + i * binSize;
    }
    return new FloatHistogram(newValue, newH);
  }

  /**
   * Check if the values are integer.
   *
   * @return true, if successful
   */
  private boolean integerData() {
    for (final float f : value) {
      if ((int) f != f) {
        return false;
      }
    }
    return true;
  }

  /**
   * Return a new Integer histogram using the current data.
   *
   * @param size the size
   * @return the histogram
   */
  private Histogram integerHistogram(int size) {
    final int min = (int) getValue(minBin);
    final int max = (int) getValue(maxBin);
    if (min >= 0 && max < size) {
      // Pure integer histogram. Do a direct conversion.
      final int[] h = new int[size];
      for (int i = minBin; i <= maxBin; i++) {
        h[(int) value[i]] += this.h[i];
      }
      return new Histogram(h);
    }

    // Build with offset

    // No need to check size since this has been done already
    final int[] h = new int[size];
    for (int i = 0; i < value.length; i++) {
      h[(int) value[i] - min] += this.h[i];
    }

    return new IntHistogram(h, min);
  }

  /** {@inheritDoc} */
  @Override
  public float getValue(int i) {
    return value[i];
  }

  /** {@inheritDoc} */
  @Override
  public FloatHistogram clone() {
    return new FloatHistogram(this.h.clone(), this.value.clone(), minBin, maxBin);
  }

  /**
   * {@inheritDoc} <p> Compacts the histogram to evenly spaced bin widths and then runs the
   * threshold method. If the compaction does not work (e.g. if minBin == maxBin) then thresholding
   * is not possible and -Infinity is returned.
   *
   * @see uk.ac.sussex.gdsc.core.threshold.Histogram#getThreshold(uk.ac.sussex.gdsc.core.threshold.AutoThreshold.Method)
   */
  @Override
  public float getThreshold(Method method) {
    return getThreshold(method, 4096);
  }

  /**
   * Compacts the histogram to evenly spaced bin widths and then runs the threshold method. If the
   * compaction does not work (e.g. if minBin == maxBin) then thresholding is not possible and
   * -Infinity is returned.
   *
   * @param method the method
   * @param bins the number of bins
   * @return the threshold
   */
  public float getThreshold(Method method, int bins) {
    // Convert to a histogram with even bin widths
    final Histogram histogram = this.compact(bins);
    if (histogram == this) {
      // Cannot compact
      return Float.NEGATIVE_INFINITY;
    }
    // Call the auto threshold method directly to avoid infinite recursion if this a float histogram
    return histogram.getAutoThreshold(method);
  }
}
