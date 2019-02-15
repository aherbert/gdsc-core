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
 * Copyright (C) 2011 - 2019 Alex Herbert
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

package uk.ac.sussex.gdsc.core.data.detection;

import org.apache.commons.lang3.ArrayUtils;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;

/**
 * Class to compute collision detections between a point and a set of rectangles.
 */
public class BinarySearchDetectionGrid implements DetectionGrid {

  /** A count of two. */
  private static final int TWO = 2;
  /** A count of four. */
  private static final int FOUR = 4;

  /** The number of rectangles. */
  private final int size;

  /** The min x of each rectangle, sorted in ascending order. */
  private final double[] minx;
  /** The max x of each rectangle, sorted in ascending order. */
  private final double[] maxx;
  /** The min y of each rectangle, sorted in ascending order. */
  private final double[] miny;
  /** The max y of each rectangle, sorted in ascending order. */
  private final double[] maxy;

  /** The original rectangle Id for each index in the sorted minx array. */
  private final int[] minxIds;
  /** The original rectangle Id for each index in the sorted maxx array. */
  private final int[] maxxIds;
  /** The original rectangle Id for each index in the sorted miny array. */
  private final int[] minyIds;
  /** The original rectangle Id for each index in the sorted maxy array. */
  private final int[] maxyIds;

  /**
   * Instantiates a new binary search detection grid.
   *
   * @param rectangles the rectangles
   */
  public BinarySearchDetectionGrid(Rectangle2D[] rectangles) {
    if (rectangles == null) {
      throw new IllegalArgumentException("Rectangle2Ds must not be null");
    }

    size = rectangles.length;

    // Store the ids of each rectangle sorted by index of the top-left and bottom-right corners
    minxIds = new int[size];
    minx = new double[size];
    maxx = new double[size];
    miny = new double[size];
    maxy = new double[size];
    for (int i = 0; i < size; i++) {
      minxIds[i] = i;
      minx[i] = rectangles[i].getMinX();
      maxx[i] = rectangles[i].getMaxX();
      miny[i] = rectangles[i].getMinY();
      maxy[i] = rectangles[i].getMaxY();
    }
    maxxIds = minxIds.clone();
    minyIds = minxIds.clone();
    maxyIds = minxIds.clone();

    sort(minxIds, minx);
    sort(maxxIds, maxx);
    sort(minyIds, miny);
    sort(maxyIds, maxy);
  }

  @Override
  public int size() {
    return size;
  }

  /**
   * Sorts the indices in ascending order of their values.
   *
   * @param indices the indices
   * @param values the values
   * @return The indices
   */
  public static int[] sort(int[] indices, final double[] values) {
    // Convert data for sorting
    final double[][] data = new double[indices.length][2];
    for (int i = indices.length; i-- > 0;) {
      data[i][0] = values[indices[i]];
      data[i][1] = indices[i];
    }

    Arrays.sort(data, (o1, o2) -> Double.compare(o1[0], o2[0]));

    // Copy back
    for (int i = indices.length; i-- > 0;) {
      indices[i] = (int) data[i][1];
      values[i] = data[i][0];
    }

    return indices;
  }

  @Override
  public int[] find(double x, double y) {
    // Perform a binary search to find the insert location of the index
    final byte[] data = new byte[size];
    for (int i = findIndexUpToAndIncluding(minx, x); i >= 0; i--) {
      data[minxIds[i]]++;
    }
    for (int i = findIndexAfter(maxx, x); i < size; i++) {
      data[maxxIds[i]]++;
    }

    if (!contains(data, TWO)) {
      // The x position is not within any rectangle
      return ArrayUtils.EMPTY_INT_ARRAY;
    }

    for (int i = findIndexUpToAndIncluding(miny, y); i >= 0; i--) {
      data[minyIds[i]]++;
    }
    for (int i = findIndexAfter(maxy, y); i < size; i++) {
      data[maxyIds[i]]++;
    }

    int count = count(data, FOUR);
    if (count == 0) {
      // The (x,y) position is not within any rectangle
      return ArrayUtils.EMPTY_INT_ARRAY;
    }

    final int[] list = new int[count];
    for (int i = size; i-- > 0;) {
      if (data[i] == FOUR) {
        list[--count] = i;
        if (count == 0) {
          break;
        }
      }
    }
    return list;
  }

  private static boolean contains(byte[] data, int value) {
    for (int i = data.length; i-- > 0;) {
      if (data[i] == value) {
        return true;
      }
    }
    return false;
  }

  private static int count(byte[] data, int value) {
    int count = 0;
    for (int i = data.length; i-- > 0;) {
      if (data[i] == value) {
        count++;
      }
    }
    return count;
  }

  /**
   * Find the index such that all indices up to and including that point have a sum equal to or
   * below the target sum.
   *
   * @param sum the sum
   * @param targetSum the target sum
   * @return the index (or -1)
   */
  static int findIndexUpToAndIncluding(double[] sum, double targetSum) {
    // index of the search key, if it is contained in the array,
    // otherwise, (-(insertion point) - 1)
    int index = Arrays.binarySearch(sum, targetSum);
    if (index < 0) {
      // The insertion point is defined as the point at which the key would be
      // inserted into the array: the index of the first element greater than the key
      // or a.length if all elements in the array are less than the specified key.
      final int insert = -(index + 1);
      return insert - 1;
    }
    // We found a match. Ensure we return the last index in the event of equality.
    while ((index + 1) < sum.length && sum[index + 1] == targetSum) {
      index++;
    }
    return index;
  }

  /**
   * Find the index such that all indices including and after that point have a sum equal to or
   * above the target sum.
   *
   * @param sum the sum
   * @param targetSum the target sum
   * @return the index (or -1)
   */
  static int findIndexIncludingAndAfter(double[] sum, double targetSum) {
    // index of the search key, if it is contained in the array,
    // otherwise, (-(insertion point) - 1)
    int index = Arrays.binarySearch(sum, targetSum);
    if (index < 0) {
      // The insertion point is defined as the point at which the key would be
      // inserted into the array: the index of the first element greater than the key
      // or a.length if all elements in the array are less than the specified key.
      return -(index + 1);
    }
    // We found a match. Ensure we return the first index in the event of equality.
    while (index > 0 && sum[index - 1] == targetSum) {
      index--;
    }
    return index;
  }

  /**
   * Find the index such that all indices including and after that point have a sum above the target
   * sum.
   *
   * @param sum the sum
   * @param targetSum the target sum
   * @return the index (or -1)
   */
  static int findIndexAfter(double[] sum, double targetSum) {
    // index of the search key, if it is contained in the array,
    // otherwise, (-(insertion point) - 1)
    int index = Arrays.binarySearch(sum, targetSum);
    if (index < 0) {
      // The insertion point is defined as the point at which the key would be
      // inserted into the array: the index of the first element greater than the key
      // or a.length if all elements in the array are less than the specified key.
      return -(index + 1);
    }
    // We found a match. Ensure we return the last index in the event of equality.
    while ((index + 1) < sum.length && sum[index + 1] == targetSum) {
      index++;
    }
    return index + 1; // After
  }
}
