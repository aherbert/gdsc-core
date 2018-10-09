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

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;


import java.util.Arrays;

import org.apache.commons.math3.util.FastMath;

/**
 * Provides a rolling median window on a data array.
 */
public class MedianWindowFloat {
  private final int radius;
  private int position = 0;
  private int cachePosition = -1;
  private final float[] data;
  private float[] cache = null;
  private float median = Float.NaN;
  private boolean invalid = true;
  private boolean sortedScan = false;

  /**
   * Instantiates a new median window float.
   *
   * @param data the data
   * @param radius the radius
   * @throws IllegalArgumentException if the radius is negative
   */
  public MedianWindowFloat(float[] data, int radius) {
    if (data == null) {
      throw new IllegalArgumentException("Input data must not be null");
    }
    if (radius < 0) {
      throw new IllegalArgumentException("Radius must not be negative");
    }
    this.data = data;
    this.radius = radius;
  }

  /**
   * Move the current position along the data array.
   *
   * @return True if the position is valid, False if the position is beyond the end of the array
   */
  public boolean increment() {
    invalid = true;
    return (++position < data.length);
  }

  /**
   * Move the current position along the data array the specified amount.
   *
   * @param size the size
   * @return True if the position is valid, False if the position is beyond the end of the array
   */
  public boolean increment(int size) {
    invalid = true;
    position += Math.abs(size);
    return (position < data.length);
  }

  /**
   * Gets the radius.
   *
   * @return the radius
   */
  public int getRadius() {
    return radius;
  }

  /**
   * Gets the position.
   *
   * @return the current position. This may be beyond the end of the array.
   * @see #increment()
   */
  public int getPosition() {
    return position;
  }

  /**
   * Set the position. Negative positions are set to zero.
   *
   * @param position Set the current position
   */
  public void setPosition(int position) {
    position = FastMath.max(0, position);
    // If moving backwards then delete the cache
    if (position < this.position) {
      cache = null;
    }
    invalid = this.position != position || cache == null;
    this.position = position;
  }

  /**
   * Checks if is valid position.
   *
   * @return True if the current position is valid
   */
  public boolean isValidPosition() {
    return position < data.length;
  }

  /**
   * Checks if is sorted scan.
   *
   * @return true if using the sorted scan method
   */
  public boolean isSortedScan() {
    return sortedScan;
  }

  /**
   * Set to true to use a sorted scan method to replace the cached window data with new values. In
   * this method the values to replace are extracted into a sorted array. The cached window data can
   * be scanned once in ascending order. <p> The default is to search the window data for each value
   * to replace directly resulting in N scans for the N replacements. This avoids an additional sort
   * method. Speed tests show the direct method is marginally faster.
   *
   * @param sortedScan the sortedScan to set
   */
  public void setSortedScan(boolean sortedScan) {
    this.sortedScan = sortedScan;
  }

  /**
   * Gets the median.
   *
   * @return The median (or NaN is the position is invalid)
   */
  public float getMedian() {
    if (invalid) {
      median = updateMedian();
    }
    return median;
  }

  private float updateMedian() {
    invalid = false;
    if (position >= data.length) {
      return Float.NaN;
    }

    // Special cases
    if (data.length == 1) {
      return data[0];
    }
    if (radius == 0) {
      return data[position];
    }
    // The position could be updated and then reset to the same position
    if (cachePosition == position) {
      return median;
    }

    // The position should always be above the cache position
    assert cachePosition < position : "Cache position is greater than the position";

    // The cache contains the sorted window from the cachePosition. The cache should cover
    // a set of the data that requires updating:
    // cachePosition
    // | Position
    // | |
    // Old -------------------------
    // New =========================
    // Remove ------
    // Keep +++++++++++++++++++
    // Add ======

    final int newStart = FastMath.max(0, position - radius);
    final int newEnd = FastMath.min(position + radius + 1, data.length);
    final int newLength = newEnd - newStart;

    // Speed tests have shown that if the total increment is more than half the radius it
    // is faster to recompute
    if (cache == null || position - cachePosition > radius / 2 || cache.length != newLength) {
      cache = new float[newLength];
      for (int i = newStart, j = 0; i < newEnd; i++, j++) {
        cache[j] = data[i];
      }
    } else {
      // This point is only reached when we have a set of sorted numbers in the cache
      // and we want to replace N of them with N new numbers.

      final int cacheStart = FastMath.max(0, cachePosition - radius);
      final int cacheEnd = FastMath.min(cachePosition + radius + 1, data.length);
      final int middle = cache.length / 2;
      final float middleValue = cache[middle];

      if (sortedScan) {
        // Method using search of the cached array with sorted numbers to remove

        // Extract numbers to remove
        final float[] dataToRemove = new float[position - cachePosition];
        for (int remove = cacheStart, i = 0; remove < newStart; remove++, i++) {
          dataToRemove[i] = data[remove];
        }
        Arrays.sort(dataToRemove);

        for (int remove = 0, add = cacheEnd, cachePosition =
            0; remove < dataToRemove.length; remove++) {
          final float toRemove = dataToRemove[remove];
          final int add2 = add;

          // Find the number in the cache
          for (; cachePosition < cache.length; cachePosition++) {
            if (cache[cachePosition] == toRemove) {
              // Replace with new data
              cache[cachePosition++] = data[add++];
              break;
            }
          }

          if (add == add2) {
            // This is bad. Just recompute the entire cache
            System.out.printf("MedianWindow : Failed to replace data in the cache\n");
            cache = new float[newLength];
            for (int i = newStart, j = 0; i < newEnd; i++, j++) {
              cache[j] = data[i];
            }
            break;
          }
        }
      } else {
        // Iterate over numbers to remove
        for (int remove = cacheStart, add = cacheEnd; remove < newStart; remove++) {
          final float toRemove = data[remove];
          final int add2 = add;
          // Find the number in the cache
          if (toRemove > middleValue) {
            for (int i = cache.length; i-- > 0;) {
              if (cache[i] == toRemove) {
                // Replace with new data
                cache[i] = data[add++];
                break;
              }
            }
          } else {
            for (int i = 0; i < cache.length; i++) {
              if (cache[i] == toRemove) {
                // Replace with new data
                cache[i] = data[add++];
                break;
              }
            }
          }

          if (add == add2) {
            // This is bad. Just recompute the entire cache
            System.out.printf("MedianWindow : Failed to replace data in the cache\n");
            cache = new float[newLength];
            for (int i = newStart, j = 0; i < newEnd; i++, j++) {
              cache[j] = data[i];
            }
            break;
          }
        }
      }
    }

    Arrays.sort(cache);
    cachePosition = position;

    return (cache[(cache.length - 1) / 2] + cache[cache.length / 2]) * 0.5f;
  }
}
