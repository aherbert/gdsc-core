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

import java.util.Arrays;
import java.util.Comparator;

/**
 * Provides sorting functionality.
 */
public final class SortUtils {

  /** No public construction. */
  private SortUtils() {}

  // TODO - rename descending sort methods to sortDescending for clarity.

  /**
   * Sorts the indices in descending order of their values.
   *
   * @param indices the indices
   * @param values the values
   * @return The indices
   */
  public static int[] sort(int[] indices, final int[] values) {
    return sort(indices, values, false, false);
  }

  /**
   * Sorts the indices in descending order of their values.
   *
   * @param indices the indices
   * @param values the values
   * @param sortValues set to true to also sort the values
   * @return The indices
   */
  public static int[] sort(int[] indices, final int[] values, boolean sortValues) {
    return sort(indices, values, sortValues, false);
  }

  /**
   * Sorts the indices in descending order of their values.
   *
   * @param indices the indices
   * @param values the values
   * @param sortValues set to true to also sort the values
   * @param ascending set to true to sort in ascending order
   * @return The indices
   */
  public static int[] sort(int[] indices, final int[] values, boolean sortValues,
      boolean ascending) {
    // Convert data for sorting
    final int[][] data = new int[indices.length][2];
    for (int i = indices.length; i-- > 0;) {
      data[i][0] = values[indices[i]];
      data[i][1] = indices[i];
    }

    final Comparator<int[]> comp = ascending
        // Smallest first
        ? (o1, o2) -> Integer.compare(o1[0], o2[0])
        // Largest first
        : (o1, o2) -> Integer.compare(o2[0], o1[0]);
    Arrays.sort(data, comp);

    // Copy back
    for (int i = indices.length; i-- > 0;) {
      indices[i] = data[i][1];
    }
    if (sortValues) {
      for (int i = indices.length; i-- > 0;) {
        values[i] = data[i][0];
      }
    }

    return indices;
  }

  /**
   * Sorts the indices in descending order of their values.
   *
   * @param indices the indices
   * @param values the values
   * @return The indices
   */
  public static int[] sort(int[] indices, final float[] values) {
    return sort(indices, values, false);
  }

  /**
   * Sorts the indices in descending order of their values.
   *
   * @param indices the indices
   * @param values the values
   * @param sortValues set to true to also sort the values
   * @return The indices
   */
  public static int[] sort(int[] indices, final float[] values, boolean sortValues) {
    return sort(indices, values, sortValues, false);
  }

  /**
   * Sorts the indices in descending order of their values.
   *
   * @param indices the indices
   * @param values the values
   * @param sortValues set to true to also sort the values
   * @param ascending set to true to sort in ascending order
   * @return The indices
   */
  public static int[] sort(int[] indices, final float[] values, boolean sortValues,
      boolean ascending) {
    // Convert data for sorting
    final float[][] data = new float[indices.length][2];
    for (int i = indices.length; i-- > 0;) {
      data[i][0] = values[indices[i]];
      // This is required to handle integers that do not fit in a float.
      // Speed test shows it is also faster than the cast.
      data[i][1] = Float.intBitsToFloat(indices[i]);
    }

    final Comparator<float[]> comp = ascending
        // Smallest first
        ? (o1, o2) -> Float.compare(o1[0], o2[0])
        // Largest first
        : (o1, o2) -> Float.compare(o2[0], o1[0]);
    Arrays.sort(data, comp);

    // Copy back
    for (int i = indices.length; i-- > 0;) {
      indices[i] = Float.floatToRawIntBits(data[i][1]);
    }
    if (sortValues) {
      for (int i = indices.length; i-- > 0;) {
        values[i] = data[i][0];
      }
    }

    return indices;
  }

  /**
   * Sorts the indices in descending order of their values.
   *
   * @param indices the indices
   * @param values the values
   * @return The indices
   */
  public static int[] sort(int[] indices, final double[] values) {
    return sort(indices, values, false);
  }

  /**
   * Sorts the indices in descending order of their values.
   *
   * @param indices the indices
   * @param values the values
   * @param sortValues set to true to also sort the values
   * @return The indices
   */
  public static int[] sort(int[] indices, final double[] values, boolean sortValues) {
    return sort(indices, values, sortValues, false);
  }

  /**
   * Sorts the indices in descending order of their values.
   *
   * @param indices the indices
   * @param values the values
   * @param sortValues set to true to also sort the values
   * @param ascending set to true to sort in ascending order
   * @return The indices
   */
  public static int[] sort(int[] indices, final double[] values, boolean sortValues,
      boolean ascending) {
    // Convert data for sorting
    final double[][] data = new double[indices.length][2];
    for (int i = indices.length; i-- > 0;) {
      data[i][0] = values[indices[i]];
      data[i][1] = indices[i];
    }

    final Comparator<double[]> comp = ascending
        // Smallest first
        ? (o1, o2) -> Double.compare(o1[0], o2[0])
        // Largest first
        : (o1, o2) -> Double.compare(o2[0], o1[0]);
    Arrays.sort(data, comp);

    // Copy back
    for (int i = indices.length; i-- > 0;) {
      indices[i] = (int) data[i][1];
    }
    if (sortValues) {
      for (int i = indices.length; i-- > 0;) {
        values[i] = data[i][0];
      }
    }

    return indices;
  }

  // Legacy API methods to sort ascending with/without sorting values

  /**
   * Sorts the indices in ascending order of their values.
   *
   * @param indices the indices
   * @param values the values
   * @return The indices
   */
  public static int[] sortAscending(int[] indices, final int[] values) {
    return sort(indices, values, false, true);
  }

  /**
   * Sorts the indices in ascending order of their values.
   *
   * @param indices the indices
   * @param values the values
   * @param sortValues set to true to also sort the values
   * @return The indices
   */
  public static int[] sortAscending(int[] indices, final int[] values, boolean sortValues) {
    return sort(indices, values, sortValues, true);
  }

  /**
   * Sorts the indices in ascending order of their values.
   *
   * @param indices the indices
   * @param values the values
   * @return The indices
   */
  public static int[] sortAscending(int[] indices, final float[] values) {
    return sort(indices, values, false, true);
  }

  /**
   * Sorts the indices in ascending order of their values.
   *
   * @param indices the indices
   * @param values the values
   * @param sortValues set to true to also sort the values
   * @return The indices
   */
  public static int[] sortAscending(int[] indices, final float[] values, boolean sortValues) {
    return sort(indices, values, sortValues, true);
  }

  /**
   * Sorts the indices in ascending order of their values.
   *
   * @param indices the indices
   * @param values the values
   * @return The indices
   */
  public static int[] sortAscending(int[] indices, final double[] values) {
    return sort(indices, values, false, true);
  }

  /**
   * Sorts the indices in ascending order of their values.
   *
   * @param indices the indices
   * @param values the values
   * @param sortValues set to true to also sort the values
   * @return The indices
   */
  public static int[] sortAscending(int[] indices, final double[] values, boolean sortValues) {
    return sort(indices, values, sortValues, true);
  }
}
