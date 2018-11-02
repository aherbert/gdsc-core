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

import org.apache.commons.math3.util.FastMath;

/**
 * Provides a rolling median on a fixed size data set. The median is maintained using a float-linked
 * list data structure.
 *
 * <p>See Juhola, et al. (1991) Comparison of algorithms for standard median filtering. Signal
 * Processing.
 */
public class FloatLinkedMedianWindow {
  private static class Data {
    int index;
    float value;
    /** The smaller chain (s). */
    Data smaller;
    /** The greater chain (g). */
    Data greater;

    public Data(float value, int index) {
      this.value = value;
      this.index = index;
    }
  }

  /**
   * The latest insertion finger.
   *
   * <p>Points to the position where a value will be removed and replaced by a new value.
   */
  private int latestInsertion;
  /** The data. */
  private final Data[] data;
  /** The median. */
  private Data median;

  /**
   * Instantiates a new median window.
   *
   * @param values the values
   * @throws IllegalArgumentException if the input data is an even size
   */
  public FloatLinkedMedianWindow(float[] values) {
    if (values == null || values.length < 1) {
      throw new IllegalArgumentException("Input data must not be null or empty");
    }
    if (values.length % 2 == 0) {
      throw new IllegalArgumentException("Input data must not even in length");
    }
    this.data = new Data[values.length];

    // Store the data and create indices for sorting
    final int[] indices = new int[values.length];

    for (int i = 0; i < values.length; i++) {
      indices[i] = i;
      this.data[i] = new Data(values[i], i);
    }
    Sort.sort(indices, values);

    // Create the smaller and greater pointers.
    // (The sort is in descending order)
    for (int i = 0; i < values.length; i++) {
      if (i > 0) {
        // Set the smaller pointer to the data smaller than this
        data[indices[i]].greater = data[indices[i - 1]];
      }
      if (i < values.length - 1) {
        // Set the greater pointer to the data greater than this
        data[indices[i]].smaller = data[indices[i + 1]];
      }
    }

    // Set the median
    median = data[indices[indices.length / 2]];
  }

  /**
   * Add a new value to the set.
   *
   * @param value the value
   */
  public void add(final float value) {
    // Replaces y by x using the latest insertion finger
    // after which the s and g chains are updated accordingly. An appro-
    // priate node is found by comparing x to the previously inserted sam
    // ple and advancing either s or g chains depending on the comparison.
    // Both links and the latest insertion finger are updated. If the median
    // is between x and y in the s (and g) chain, it is changed by moving
    // the median finger one node towards the inserted sample along the
    // s or g chain. The same scheme is followed if the sample to be
    // deleted is the median itself.

    final Data point = data[latestInsertion];
    final float removedValue = point.value;
    if (value == removedValue) {
      latestInsertion = (latestInsertion + 1) % data.length;
      return;
    }

    final float m = median.value;

    // Replace y by x
    point.value = value;

    // Sort the data and update the median
    if (value < removedValue) {
      // Move along the s chain until sorted
      Data movePast = point;
      for (Data s = point.smaller; s != null && s.value > value; s = s.smaller) {
        movePast = s;
      }
      if (movePast != point) {
        if (removedValue == m && median != point) {
          // The value removed matches the median, however it
          // could have been below or above the median in the linked list.
          // The new value is lower. Check if the position is above the median.
          final boolean shift = aboveMedian(point);

          // Update the sorted list:
          // 1. Remove the point
          if (point.greater != null) {
            point.greater.smaller = point.smaller;
          }
          point.smaller.greater = point.greater;

          // 2. Insert into new location
          if (movePast.smaller != null) {
            movePast.smaller.greater = point;
          }
          point.smaller = movePast.smaller;
          movePast.smaller = point;
          point.greater = movePast;

          if (shift) {
            median = median.smaller;
          }
        } else {
          final Data aboveMedian = median.greater;

          // Update the sorted list:
          // 1. Remove the point
          if (point.greater != null) {
            point.greater.smaller = point.smaller;
          }
          point.smaller.greater = point.greater;

          // 2. Insert into new location
          if (movePast.smaller != null) {
            movePast.smaller.greater = point;
          }
          point.smaller = movePast.smaller;
          movePast.smaller = point;
          point.greater = movePast;

          // If we moved the median then update using the unmoved node next to the median
          if (median == point) {
            median = aboveMedian.smaller;
          } else if (value < m && removedValue > m) {
            median = median.smaller;
          }
        }
      }
    } else {
      // value > removedValue.
      // Move along the g chain until sorted
      Data movePast = point;
      for (Data g = point.greater; g != null && g.value < value; g = g.greater) {
        movePast = g;
      }
      if (movePast != point) {
        if (removedValue == m && median != point) {
          // The value removed matches the median, however it
          // could have been below or above the median in the linked list.
          // The new value is higher. Check if the position is above the median.
          final boolean shift = !aboveMedian(point);

          // Update the sorted list:
          // 1. Remove the point
          if (point.smaller != null) {
            point.smaller.greater = point.greater;
          }
          point.greater.smaller = point.smaller;

          // 2. Insert into new location
          if (movePast.greater != null) {
            movePast.greater.smaller = point;
          }
          point.greater = movePast.greater;
          movePast.greater = point;
          point.smaller = movePast;

          if (shift) {
            median = median.greater;
          }
        } else {
          final Data belowMedian = median.smaller;

          // Update the sorted list:
          // 1. Remove the point
          if (point.smaller != null) {
            point.smaller.greater = point.greater;
          }
          point.greater.smaller = point.smaller;

          // 2. Insert into new location
          if (movePast.greater != null) {
            movePast.greater.smaller = point;
          }
          point.greater = movePast.greater;
          movePast.greater = point;
          point.smaller = movePast;

          // If we moved the median then update using the unmoved node next to the median
          if (median == point) {
            median = belowMedian.greater;
          } else if (value > m && removedValue < m) {
            median = median.greater;
          }
        }
      }
    }

    // Update the latest insertion finger
    latestInsertion = (latestInsertion + 1) % data.length;
  }

  private boolean aboveMedian(Data point) {
    for (Data p = median.greater; p != null; p = p.greater) {
      if (p == point) {
        return true;
      }
      if (p.value != median.value) {
        return false;
      }
    }
    return false;
  }

  /**
   * Gets the current median of the entire range of time points.
   *
   * @return The median.
   */
  public float getMedian() {
    return median.value;
  }

  /**
   * Compute the median for the input data using a range of time points. The first time point added
   * is t=0. Time points after that have a positive index. The maximum allowed index is the data
   * length-1.
   *
   * @param start the start
   * @param end the end
   * @return the median
   */
  public float getMedian(int start, int end) {
    end = FastMath.min(data.length - 1, Math.abs(end));
    start = FastMath.max(0, Math.abs(start));

    final int length = end - start + 1;
    if (length == 0) {
      return Float.NaN;
    }

    // Find the head of the list
    Data head = median;
    while (head.smaller != null) {
      head = head.smaller;
    }

    // Create a list of the data using only the desired time points
    final Data[] list = new Data[length];

    // Extract the data into a list. This should be sorted.
    int index = 0;
    while (head != null) {
      final int age = (data.length + head.index - latestInsertion) % data.length;
      if (age >= start && age <= end) {
        list[index++] = head;
      }
      head = head.greater;
    }

    return (list[(list.length - 1) / 2].value + list[list.length / 2].value) * 0.5f;
  }

  /**
   * Compute the median for the input data using the oldest n data points.
   *
   * @param oldestN the oldest N
   * @return the median
   */
  public float getMedianOldest(int oldestN) {
    return getMedian(0, oldestN - 1);
  }

  /**
   * Compute the median for the input data using the youngest n data points.
   *
   * @param youngestN the youngest N
   * @return the median
   */
  public float getMedianYoungest(int youngestN) {
    final int end = data.length - 1;
    return getMedian(end - youngestN + 1, end);
  }

  /**
   * Gets the size.
   *
   * @return The size of the rolling window.
   */
  public int getSize() {
    return data.length;
  }
}
