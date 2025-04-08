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
 * Copyright (C) 2011 - 2025 Alex Herbert
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
/*
 *
 */

package uk.ac.sussex.gdsc.core.math;

import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

/**
 * Compute the radial statistics of 2D data.
 */
public final class RadialStatisticsUtils {

  /** No public construction. */
  private RadialStatisticsUtils() {}

  /**
   * Compute the radial sum of circles up to radius size/2. The sum includes all pixels that are at
   * a radius (r) equal to or greater than n and less than n+1.
   *
   * @param size the size (in one dimension)
   * @param data the data (size*size)
   * @return the sum
   */
  public static double[] radialSum(int size, float[] data) {
    SimpleArrayUtils.hasData2D(size, size, data);

    // Centre
    final int cx = size / 2;

    // Maximum distance from centre in each dimension
    final int max = size - cx;

    // Squared distance
    final int[] d2 = new int[max + 1];
    for (int i = 1; i < d2.length; i++) {
      d2[i] = i * i;
    }

    final double[] sum = new double[max];

    // Centre
    final int cxi = size * cx + cx;
    sum[0] = data[cxi];

    // Do the central row
    for (int xi = 1, i1 = cxi - 1, i2 = cxi + 1; xi < max; xi++, i1++, i2--) {
      sum[xi] = data[i1] + data[i2];
    }
    // Do the central column
    for (int xi = 1, i1 = cxi + size, i2 = cxi - size; xi < max; xi++, i1 += size, i2 -= size) {
      sum[xi] += data[i1] + data[i2];
    }

    // Sweep out from centre
    Y: for (int y1 = cx + 1, y2 = cx - 1, yi = 1; yi < max; y1++, y2--, yi++) {
      final int d2y = d2[yi];
      //@formatter:off
      // Initialise for sweep of 2 rows (below (y1) and above (y2))
      // from the centre outwards in both directions. missing the initial column.
      for (int xi = 1,
          xyi = yi, // This will be the initial squared distance index
          i1 = size * y1 + cx - 1,
          i2 = i1 + 2,
          i3 = size * y2 + cx - 1,
          i4 = i3 + 2;
          // Condition
          xi < max;
          // Move indices
          xi++, i1--, i2++, i3--, i4++) {
        //@formatter:on
        // Find index in squared distance array:
        // d2[xyi] <= d < d2[xyi+1]
        // No need for loop as we are only moving a max of 1 pixel distance increment
        if (d2[xyi + 1] <= d2[xi] + d2y) {
          xyi++;
          if (xyi == max) {
            continue Y;
          }
        }
        sum[xyi] += data[i1] + data[i2] + data[i3] + data[i4];
      }
    }

    return sum;
  }

  /**
   * Compute the radial sum of circles up to radius size/2. The sum includes all pixels that are at
   * a radius (r) equal to or greater than n and less than n+1.
   *
   * <p>This is a utility method to compute multiple radial sums concurrently.
   *
   * @param size the size (in one dimension)
   * @param data the data (m arrays of size*size)
   * @return the sum (m arrays)
   * @throws IllegalArgumentException if the size is not strictly positive, or if each array in the
   *         data is not is not size*size.
   */
  public static double[][] radialSum(int size, float[]... data) {
    final int m = checkData(size, data);

    // Centre
    final int cx = size / 2;

    // Maximum distance from centre in each dimension
    final int max = size - cx;

    // Squared distance
    final int[] d2 = new int[max + 1];
    for (int i = 1; i < d2.length; i++) {
      d2[i] = i * i;
    }

    final double[][] sum = new double[m][max];

    // Centre
    final int cxi = size * cx + cx;
    for (int mi = 0; mi < m; mi++) {
      sum[mi][0] = data[mi][cxi];
    }

    // Do the central row
    for (int xi = 1, i1 = cxi - 1, i2 = cxi + 1; xi < max; xi++, i1++, i2--) {
      for (int mi = 0; mi < m; mi++) {
        sum[mi][xi] = data[mi][i1] + data[mi][i2];
      }
    }
    // Do the central column
    for (int xi = 1, i1 = cxi + size, i2 = cxi - size; xi < max; xi++, i1 += size, i2 -= size) {
      for (int mi = 0; mi < m; mi++) {
        sum[mi][xi] += data[mi][i1] + data[mi][i2];
      }
    }

    // Sweep out from centre
    Y: for (int y1 = cx + 1, y2 = cx - 1, yi = 1; yi < max; y1++, y2--, yi++) {
      final int d2y = d2[yi];
      //@formatter:off
      // Initialise for sweep of 2 rows (below (y1) and above (y2))
      // from the centre outwards in both directions. missing the initial column.
      for (int xi = 1,
          xyi = yi, // This will be the initial squared distance index
          i1 = size * y1 + cx - 1,
          i2 = i1 + 2,
          i3 = size * y2 + cx - 1,
          i4 = i3 + 2;
          // Condition
          xi < max;
          // Move indices
          xi++, i1--, i2++, i3--, i4++) {
        //@formatter:on
        // Find index in squared distance array:
        // d2[xyi] <= d < d2[xyi+1]
        // No need for loop as we are only moving a max of 1 pixel distance increment
        if (d2[xyi + 1] <= d2[xi] + d2y) {
          xyi++;
          if (xyi == max) {
            continue Y;
          }
        }
        for (int mi = 0; mi < m; mi++) {
          sum[mi][xyi] += data[mi][i1] + data[mi][i2] + data[mi][i3] + data[mi][i4];
        }
      }
    }

    return sum;
  }

  /**
   * Compute the radial sum of circles up to radius size/2. The sum includes all pixels that are at
   * a radius (r) equal to or greater than n and less than n+1.
   *
   * <p>This is a utility method to compute multiple radial sums concurrently. A final array is
   * appended to the results containing the count of the number of pixels at each distance.
   *
   * @param size the size (in one dimension)
   * @param data the data (m arrays of size*size)
   * @return the sum (m+1 arrays: m arrays of sums and a final array of counts)
   * @throws IllegalArgumentException if the size is not strictly positive, or if each array in the
   *         data is not is not size*size.
   */
  public static double[][] radialSumAndCount(int size, float[]... data) {
    final int m = checkData(size, data);

    // Centre
    final int cx = size / 2;

    // Maximum distance from centre in each dimension
    final int max = size - cx;

    // Squared distance
    final int[] d2 = new int[max + 1];
    for (int i = 1; i < d2.length; i++) {
      d2[i] = i * i;
    }

    final double[][] sum = new double[m + 1][max];

    // Centre
    final int cxi = size * cx + cx;
    for (int mi = 0; mi < m; mi++) {
      sum[mi][0] = data[mi][cxi];
    }
    sum[m][0] = 1;

    // Do the central row
    for (int xi = 1, i1 = cxi - 1, i2 = cxi + 1; xi < max; xi++, i1++, i2--) {
      for (int mi = 0; mi < m; mi++) {
        sum[mi][xi] = data[mi][i1] + data[mi][i2];
      }
      sum[m][xi] = 2;
    }
    // Do the central column
    for (int xi = 1, i1 = cxi + size, i2 = cxi - size; xi < max; xi++, i1 += size, i2 -= size) {
      for (int mi = 0; mi < m; mi++) {
        sum[mi][xi] += data[mi][i1] + data[mi][i2];
      }
      sum[m][xi] += 2;
    }

    // Sweep out from centre
    Y: for (int y1 = cx + 1, y2 = cx - 1, yi = 1; yi < max; y1++, y2--, yi++) {
      final int d2y = d2[yi];
      //@formatter:off
      // Initialise for sweep of 2 rows (below (y1) and above (y2))
      // from the centre outwards in both directions. missing the initial column.
      for (int xi = 1,
          xyi = yi, // This will be the initial squared distance index
          i1 = size * y1 + cx - 1,
          i2 = i1 + 2,
          i3 = size * y2 + cx - 1,
          i4 = i3 + 2;
          // Condition
          xi < max;
          // Move indices
          xi++, i1--, i2++, i3--, i4++) {
        //@formatter:on
        // Find index in squared distance array:
        // d2[xyi] <= d < d2[xyi+1]
        // No need for loop as we are only moving a max of 1 pixel distance increment
        if (d2[xyi + 1] <= d2[xi] + d2y) {
          xyi++;
          if (xyi == max) {
            continue Y;
          }
        }
        for (int mi = 0; mi < m; mi++) {
          sum[mi][xyi] += data[mi][i1] + data[mi][i2] + data[mi][i3] + data[mi][i4];
        }
        sum[m][xyi] += 4;
      }
    }

    return sum;
  }

  /**
   * Check all input arrays in the data are the correct length (size*size).
   *
   * @param size the size
   * @param data the data
   * @return the number of input arrays
   */
  private static int checkData(int size, float[]... data) {
    if (data.length == 0) {
      throw new IllegalArgumentException("No data");
    }
    ValidationUtils.checkStrictlyPositive(size, "size");
    final int m = data.length;
    final int length = SimpleArrayUtils.check2DSize(size, size);
    for (int mi = 0; mi < m; mi++) {
      if (data[mi].length != length) {
        throw new IllegalArgumentException("Data array " + mi + " is incorrect size");
      }
    }
    return m;
  }

  /**
   * Compute the radial sum of circles up to radius size/2. The sum includes all pixels that are at
   * a radius (r) equal to or greater than n and less than n+1.
   *
   * @param size the size (in one dimension)
   * @return the sum
   * @throws IllegalArgumentException if the size is not strictly positive
   */
  public static int[] radialCount(int size) {
    ValidationUtils.checkStrictlyPositive(size, "size");

    // Centre
    final int cx = size / 2;

    // Maximum distance from centre in each dimension
    final int max = size - cx;

    // Squared distance
    final int[] d2 = new int[max + 1];
    for (int i = 1; i < d2.length; i++) {
      d2[i] = i * i;
    }

    final int[] sum = new int[max];

    // Centre
    sum[0] = 1;

    // Do the central row. This is mirrored to the column
    for (int xi = 1; xi < max; xi++) {
      sum[xi] = 4;
    }

    // Sweep out from centre
    Y: for (int yi = 1; yi < max; yi++) {
      final int d2y = d2[yi];
      //@formatter:off
      // Initialise for sweep of 2 rows (below (y1) and above (y2))
      // from the centre outwards in both directions. missing the initial column.
      for (int xi = 1,
          xyi = yi; // This will be the initial squared distance index
          // Condition
          xi < max;
          // Move indices
          xi++) {
        //@formatter:on
        // Find index in squared distance array:
        // d2[xyi] <= d < d2[xyi+1]
        // No need for loop as we are only moving a max of 1 pixel distance increment
        if (d2[xyi + 1] <= d2[xi] + d2y) {
          xyi++;
          if (xyi == max) {
            continue Y;
          }
        }
        sum[xyi] += 4;
      }
    }

    return sum;
  }
}
