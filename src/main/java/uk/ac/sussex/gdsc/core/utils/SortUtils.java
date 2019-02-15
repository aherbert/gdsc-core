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

package uk.ac.sussex.gdsc.core.utils;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Provides sorting functionality.
 */
public final class SortUtils {

  /** No public construction. */
  private SortUtils() {}

  /**
   * Sorts the {@code indices} using their {@code values}.
   *
   * <p>The {@code indices} must be a valid index into the {@code values} array. The {@code values}
   * array does not have to match the length of the {@code indices}.
   *
   * <pre>
   * <code>
   *   int[] indices = {0, 1, 2, 1};
   *   int[] values = {44, 0, 1};
   *   SortUtils.sortIndices(indices, values, false);
   *   // indices == [ 1, 1, 2, 0 ];
   * </code>
   * </pre>
   *
   * @param indices the indices
   * @param values the values
   * @param descending set to true to sort in descending order
   */
  public static void sortIndices(int[] indices, int[] values, boolean descending) {
    // Convert data for sorting
    final Integer[] sortData = new Integer[indices.length];
    for (int i = indices.length; i-- > 0;) {
      sortData[i] = Integer.valueOf(indices[i]);
    }

    final Comparator<Integer> cmp = descending
        // Largest first
        ? (o1, o2) -> Integer.compare(values[o2.intValue()], values[o1.intValue()])
        // Smallest first
        : (o1, o2) -> Integer.compare(values[o1.intValue()], values[o2.intValue()]);
    Arrays.sort(sortData, cmp);

    // Copy back
    for (int i = sortData.length; i-- > 0;) {
      indices[i] = sortData[i].intValue();
    }
  }

  /**
   * Sorts the {@code indices} using their {@code values}.
   *
   * <p>The {@code indices} must be a valid index into the {@code values} array. The {@code values}
   * array does not have to match the length of the {@code indices}.
   *
   * <pre>
   * <code>
   *   int[] indices = {0, 1, 2, 1};
   *   float[] values = {44, 0, 1};
   *   SortUtils.sortIndices(indices, values, false);
   *   // indices == [ 1, 1, 2, 0 ];
   * </code>
   * </pre>
   *
   * @param indices the indices
   * @param values the values
   * @param descending set to true to sort in descending order
   */
  public static void sortIndices(int[] indices, float[] values, boolean descending) {
    // Convert data for sorting
    final Integer[] sortData = new Integer[indices.length];
    for (int i = indices.length; i-- > 0;) {
      sortData[i] = Integer.valueOf(indices[i]);
    }

    final Comparator<Integer> cmp = descending
        // Largest first
        ? (o1, o2) -> Float.compare(values[o2.intValue()], values[o1.intValue()])
        // Smallest first
        : (o1, o2) -> Float.compare(values[o1.intValue()], values[o2.intValue()]);
    Arrays.sort(sortData, cmp);

    // Copy back
    for (int i = sortData.length; i-- > 0;) {
      indices[i] = sortData[i].intValue();
    }
  }

  /**
   * Sorts the {@code indices} using their {@code values}.
   *
   * <p>The {@code indices} must be a valid index into the {@code values} array. The {@code values}
   * array does not have to match the length of the {@code indices}.
   *
   * <pre>
   * <code>
   *   int[] indices = {0, 1, 2, 1};
   *   double[] values = {44, 0, 1};
   *   SortUtils.sortIndices(indices, values, false);
   *   // indices == [ 1, 1, 2, 0 ];
   * </code>
   * </pre>
   *
   * @param indices the indices
   * @param values the values
   * @param descending set to true to sort in descending order
   */
  public static void sortIndices(int[] indices, double[] values, boolean descending) {
    // Convert data for sorting
    final Integer[] sortData = new Integer[indices.length];
    for (int i = indices.length; i-- > 0;) {
      sortData[i] = Integer.valueOf(indices[i]);
    }

    final Comparator<Integer> cmp = descending
        // Largest first
        ? (o1, o2) -> Double.compare(values[o2.intValue()], values[o1.intValue()])
        // Smallest first
        : (o1, o2) -> Double.compare(values[o1.intValue()], values[o2.intValue()]);
    Arrays.sort(sortData, cmp);

    // Copy back
    for (int i = sortData.length; i-- > 0;) {
      indices[i] = sortData[i].intValue();
    }
  }

  /**
   * Sorts the {@code data} using the provided {@code values}.
   *
   * <p>The {@code values} array must match the length of the {@code data} array.
   *
   * <pre>
   * <code>
   *   int[] data = {70, 80, 90};
   *   int[] values = {44, 0, 1};
   *   SortUtils.sortData(data, values, true, false);
   *   // data == [ 80, 90, 70 ];
   *   // values  == [ 0, 1, 44 ];
   * </code>
   * </pre>
   *
   * @param data the data
   * @param values the values
   * @param sortValues set to true to also sort the values
   * @param descending set to true to sort in descending order
   */
  public static void sortData(int[] data, int[] values, boolean sortValues, boolean descending) {
    // Convert sortData for sorting
    final int[][] sortData = new int[data.length][2];
    for (int i = sortData.length; i-- > 0;) {
      sortData[i][0] = values[i];
      sortData[i][1] = data[i];
    }

    final Comparator<int[]> cmp = descending
        // Largest first
        ? (o1, o2) -> Integer.compare(o2[0], o1[0])
        // Smallest first
        : (o1, o2) -> Integer.compare(o1[0], o2[0]);
    Arrays.sort(sortData, cmp);

    // Copy back
    if (sortValues) {
      for (int i = sortData.length; i-- > 0;) {
        values[i] = sortData[i][0];
        data[i] = sortData[i][1];
      }
    } else {
      for (int i = sortData.length; i-- > 0;) {
        data[i] = sortData[i][1];
      }
    }
  }

  /**
   * Sorts the {@code data} using the provided {@code values}.
   *
   * <p>The {@code values} array must match the length of the {@code data} array.
   *
   * <pre>
   * <code>
   *   float[] data = {70, 80, 90};
   *   float[] values = {44, 0, 1};
   *   SortUtils.sortData(data, values, true, false);
   *   // data == [ 80, 90, 70 ];
   *   // values  == [ 0, 1, 44 ];
   * </code>
   * </pre>
   *
   * @param data the data
   * @param values the values
   * @param sortValues set to true to also sort the values
   * @param descending set to true to sort in descending order
   */
  public static void sortData(float[] data, float[] values, boolean sortValues,
      boolean descending) {
    // Convert sortData for sorting
    final float[][] sortData = new float[data.length][2];
    for (int i = sortData.length; i-- > 0;) {
      sortData[i][0] = values[i];
      sortData[i][1] = data[i];
    }

    final Comparator<float[]> cmp = descending
        // Largest first
        ? (o1, o2) -> Float.compare(o2[0], o1[0])
        // Smallest first
        : (o1, o2) -> Float.compare(o1[0], o2[0]);
    Arrays.sort(sortData, cmp);

    // Copy back
    if (sortValues) {
      for (int i = sortData.length; i-- > 0;) {
        values[i] = sortData[i][0];
        data[i] = sortData[i][1];
      }
    } else {
      for (int i = sortData.length; i-- > 0;) {
        data[i] = sortData[i][1];
      }
    }
  }

  /**
   * Sorts the {@code data} using the provided {@code values}.
   *
   * <p>The {@code values} array must match the length of the {@code data} array.
   *
   * <pre>
   * <code>
   *   double[] data = {70, 80, 90};
   *   double[] values = {44, 0, 1};
   *   SortUtils.sortData(data, values, true, false);
   *   // data == [ 80, 90, 70 ];
   *   // values  == [ 0, 1, 44 ];
   * </code>
   * </pre>
   *
   * @param data the data
   * @param values the values
   * @param sortValues set to true to also sort the values
   * @param descending set to true to sort in descending order
   */
  public static void sortData(double[] data, double[] values, boolean sortValues,
      boolean descending) {
    // Convert sortData for sorting
    final double[][] sortData = new double[data.length][2];
    for (int i = sortData.length; i-- > 0;) {
      sortData[i][0] = values[i];
      sortData[i][1] = data[i];
    }

    final Comparator<double[]> cmp = descending
        // Largest first
        ? (o1, o2) -> Double.compare(o2[0], o1[0])
        // Smallest first
        : (o1, o2) -> Double.compare(o1[0], o2[0]);
    Arrays.sort(sortData, cmp);

    // Copy back
    if (sortValues) {
      for (int i = sortData.length; i-- > 0;) {
        values[i] = sortData[i][0];
        data[i] = sortData[i][1];
      }
    } else {
      for (int i = sortData.length; i-- > 0;) {
        data[i] = sortData[i][1];
      }
    }
  }


  /**
   * Sorts the {@code data} using the provided {@code values}.
   *
   * <p>The {@code values} array must match the length of the {@code data} array.
   *
   * <pre>
   * <code>
   *   int[] data = {70, 80, 90};
   *   double[] values = {44, 0, 1};
   *   SortUtils.sortData(data, values, true, false);
   *   // data == [ 80, 90, 70 ];
   *   // values  == [ 0, 1, 44 ];
   * </code>
   * </pre>
   *
   * @param data the data
   * @param values the values
   * @param sortValues set to true to also sort the values
   * @param descending set to true to sort in descending order
   */
  public static void sortData(int[] data, double[] values, boolean sortValues, boolean descending) {
    // Convert sortData for sorting
    final double[][] sortData = new double[data.length][2];
    for (int i = sortData.length; i-- > 0;) {
      sortData[i][0] = values[i];
      sortData[i][1] = data[i];
    }

    final Comparator<double[]> cmp = descending
        // Largest first
        ? (o1, o2) -> Double.compare(o2[0], o1[0])
        // Smallest first
        : (o1, o2) -> Double.compare(o1[0], o2[0]);
    Arrays.sort(sortData, cmp);

    // Copy back
    if (sortValues) {
      for (int i = sortData.length; i-- > 0;) {
        values[i] = sortData[i][0];
        data[i] = (int) sortData[i][1];
      }
    } else {
      for (int i = sortData.length; i-- > 0;) {
        data[i] = (int) sortData[i][1];
      }
    }
  }
}
