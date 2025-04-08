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

package uk.ac.sussex.gdsc.core.utils;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Arrays;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;
import java.util.function.LongUnaryOperator;
import java.util.function.Supplier;
import org.apache.commons.lang3.ArrayUtils;
import uk.ac.sussex.gdsc.core.annotation.NotNull;
import uk.ac.sussex.gdsc.core.annotation.Nullable;
import uk.ac.sussex.gdsc.core.utils.function.FloatPredicate;
import uk.ac.sussex.gdsc.core.utils.function.FloatUnaryOperator;

/**
 * Class for manipulating arrays.
 */
public final class SimpleArrayUtils {

  private static final String DATA_EMPTY = "Data is empty";
  private static final String DATA_INCORRECT_SIZE = "Data is not the correct array size";
  /** The minimum size of an array where intervals can be compared. */
  private static final int MIN_INTERVAL_SIZE = 3;

  /** No public construction. */
  private SimpleArrayUtils() {}

  /**
   * Merge the two sets into a single set using a hashset. The order is undefined. Input sets are
   * unchanged.
   *
   * @param s1 the first set
   * @param s2 the second set
   * @return the merged set
   */
  public static int[] merge(int[] s1, int[] s2) {
    return merge(s1, s2, false);
  }

  /**
   * Merge the two sets into a single set using a hashset. The order is undefined. Input sets are
   * unchanged.
   *
   * @param s1 the first set
   * @param s2 the second set
   * @param unique Set to true if the values in the sets are unique (allows optimisation of hashset
   *        size)
   * @return the merged set
   */
  public static int[] merge(int[] s1, int[] s2, boolean unique) {
    IntOpenHashSet set;
    if (unique) {
      set = new IntOpenHashSet(Math.max(s1.length, s2.length));
    } else {
      set = new IntOpenHashSet();
    }
    set.addAll(IntArrayList.wrap(s1));
    set.addAll(IntArrayList.wrap(s2));
    return set.toIntArray();
  }

  /**
   * Flatten the array into an ascending array of unique values. Input data are destructively
   * modified.
   *
   * <p>Inputting null will return a zero sized array.
   *
   * @param array the array
   * @return the new array
   */
  public static int[] flatten(int[] array) {
    if (array == null) {
      return ArrayUtils.EMPTY_INT_ARRAY;
    }
    if (array.length <= 1) {
      return array;
    }
    Arrays.sort(array);
    int count = 0;
    for (int i = 1; i < array.length; i++) {
      if (array[i] != array[count]) {
        array[++count] = array[i];
      }
    }
    return Arrays.copyOf(array, count + 1);
  }

  /**
   * Convert the input array to a double.
   *
   * @param array the array
   * @return The new array
   */
  public static double[] toDouble(float[] array) {
    if (array == null) {
      return ArrayUtils.EMPTY_DOUBLE_ARRAY;
    }
    final int len = array.length;
    final double[] out = new double[len];
    for (int i = 0; i < len; i++) {
      out[i] = array[i];
    }
    return out;
  }

  /**
   * Convert the input array to a double.
   *
   * @param array the array
   * @return The new array
   */
  public static double[] toDouble(int[] array) {
    if (array == null) {
      return ArrayUtils.EMPTY_DOUBLE_ARRAY;
    }
    final int len = array.length;
    final double[] out = new double[len];
    for (int i = 0; i < len; i++) {
      out[i] = array[i];
    }
    return out;
  }

  /**
   * Convert the input array to array float.
   *
   * @param array the array
   * @return The new array
   */
  public static float[] toFloat(double[] array) {
    if (array == null) {
      return new float[0];
    }
    final int len = array.length;
    final float[] out = new float[len];
    for (int i = 0; i < len; i++) {
      out[i] = (float) array[i];
    }
    return out;
  }

  /**
   * Convert the input array to array float.
   *
   * @param array the array
   * @return The new array
   */
  public static float[] toFloat(int[] array) {
    if (array == null) {
      return new float[0];
    }
    final int len = array.length;
    final float[] out = new float[len];
    for (int i = 0; i < len; i++) {
      out[i] = array[i];
    }
    return out;
  }

  /**
   * Create and fill an array.
   *
   * @param length The length of the array
   * @param start The start
   * @param increment The increment
   * @return The new array
   */
  public static double[] newArray(int length, double start, double increment) {
    final double[] data = new double[length];
    for (int i = 0; i < length; i++) {
      data[i] = start + i * increment;
    }
    return data;
  }

  /**
   * Create and fill an array.
   *
   * @param length The length of the array
   * @param start The start
   * @param increment The increment
   * @return The new array
   */
  public static float[] newArray(int length, float start, float increment) {
    final float[] data = new float[length];
    for (int i = 0; i < length; i++) {
      data[i] = start + i * increment;
    }
    return data;
  }

  /**
   * Create and fill an array.
   *
   * @param length The length of the array
   * @param start The start
   * @param increment The increment
   * @return The new array
   */
  public static int[] newArray(int length, int start, int increment) {
    final int[] data = new int[length];
    for (int i = 0; i < length; i++) {
      data[i] = start + i * increment;
    }
    return data;
  }

  /**
   * Create a natural sequence from {@code 0} up to {@code length - 1}.
   *
   * @param length The length of the array
   * @return The new array
   */
  public static int[] natural(int length) {
    final int[] data = new int[length];
    for (int i = 0; i < length; i++) {
      data[i] = i;
    }
    return data;
  }

  /**
   * Convert the data to strictly positive. Any value that is zero or below is set the minimum value
   * above zero.
   *
   * <p>If no data is above zero then an array of zero is returned.
   *
   * @param data the data
   * @return the strictly positive variance
   */
  public static float[] ensureStrictlyPositive(float[] data) {
    final int index = indexOfNotStrictlyPositive(data);
    if (index == -1) {
      // All data are positive
      return data;
    }

    // Not strictly positive so create a clone
    final float[] v = new float[data.length];
    // Copy the values that were positive
    System.arraycopy(data, 0, v, 0, index);

    // Find the min above zero
    final float min = minAboveZero(data);
    if (Float.isNaN(min)) {
      // No values are above zero. Return an array of zero.
      return v;
    }

    // We know this was not strictly positive
    v[index] = min;

    // Check and copy the rest
    for (int i = index + 1; i < data.length; i++) {
      v[i] = (data[i] <= 0) ? min : data[i];
    }
    return v;
  }

  /**
   * Find the first index where the value is not strictly positive.
   *
   * @param data the data
   * @return the index (or -1)
   */
  private static int indexOfNotStrictlyPositive(float[] data) {
    for (int i = 0; i < data.length; i++) {
      if (data[i] <= 0) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Find the minimum above zero.
   *
   * <p>Returns {@link Float#NaN} if no values are above zero.
   *
   * @param data the data
   * @return the minimum above zero
   */
  public static float minAboveZero(float[] data) {
    float min = Float.NaN;
    for (final float value : data) {
      if (value > 0 && (min > value || Float.isNaN(min))) {
        min = value;
      }
    }
    return min;
  }

  /**
   * Create a new double array with the given value.
   *
   * @param length the length
   * @param value the value
   * @return the double array
   */
  public static double[] newDoubleArray(int length, double value) {
    final double[] data = new double[length];
    Arrays.fill(data, value);
    return data;
  }

  /**
   * Create a new float array with the given value.
   *
   * @param length the length
   * @param value the value
   * @return the float array
   */
  public static float[] newFloatArray(int length, float value) {
    final float[] data = new float[length];
    Arrays.fill(data, value);
    return data;
  }

  /**
   * Create a new int array with the given value.
   *
   * @param length the length
   * @param value the value
   * @return the int array
   */
  public static int[] newIntArray(int length, int value) {
    final int[] data = new int[length];
    Arrays.fill(data, value);
    return data;
  }

  /**
   * Create a new byte array with the given value.
   *
   * @param length the length
   * @param value the value
   * @return the byte array
   */
  public static byte[] newByteArray(int length, byte value) {
    final byte[] data = new byte[length];
    Arrays.fill(data, value);
    return data;
  }

  /**
   * Reverse the array order.
   *
   * @param data the data
   */
  public static void reverse(int[] data) {
    for (int left = 0, right = data.length - 1; left < right; left++, right--) {
      swap(data, left, right);
    }
  }

  /**
   * Reverse the array order.
   *
   * @param data the data
   */
  public static void reverse(float[] data) {
    for (int left = 0, right = data.length - 1; left < right; left++, right--) {
      swap(data, left, right);
    }
  }

  /**
   * Reverse the array order.
   *
   * @param data the data
   */
  public static void reverse(double[] data) {
    for (int left = 0, right = data.length - 1; left < right; left++, right--) {
      swap(data, left, right);
    }
  }

  /**
   * Reverse the array order.
   *
   * @param data the data
   */
  public static void reverse(byte[] data) {
    for (int left = 0, right = data.length - 1; left < right; left++, right--) {
      swap(data, left, right);
    }
  }

  /**
   * Reverse the array order.
   *
   * @param data the data
   */
  public static void reverse(short[] data) {
    for (int left = 0, right = data.length - 1; left < right; left++, right--) {
      swap(data, left, right);
    }
  }

  /**
   * Checks if all the values have an {@code integer} representation.
   *
   * @param x the x
   * @return true if all the values have an integer representation
   */
  public static boolean isInteger(double[] x) {
    for (final double value : x) {
      if ((int) value != value) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if all the values have an {@code integer} representation.
   *
   * @param x the x
   * @return true if all the values have an integer representation
   */
  public static boolean isInteger(float[] x) {
    for (final float value : x) {
      if ((int) value != value) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if all the values have a uniform interval between them. The interval between each
   * successive pair is compared to the first interval. If different then return false. If the first
   * interval is zero then return false. This ensures that the function returns true only if the
   * sequence is monotonic and evenly sampled.
   *
   * @param x the x
   * @return true, if is uniform
   */
  public static boolean isUniform(int[] x) {
    if (x.length < MIN_INTERVAL_SIZE) {
      // No intervals to measure
      return true;
    }
    try {
      final int reference = Math.subtractExact(x[1], x[0]);
      if (reference == 0) {
        return false;
      }
      for (int i = 2; i < x.length; i++) {
        final int interval = Math.subtractExact(x[i], x[i - 1]);
        if (interval != reference) {
          return false;
        }
      }
      return true;
    } catch (final ArithmeticException ex) {
      // Overflow so this is not a uniform ascending/descending series.
      return false;
    }
  }

  /**
   * Checks if all the values have a uniform interval between them. The interval between each
   * successive pair is compared to the mean interval. If the error is greater than the tolerance
   * then return false. If any interval is zero then return false. If any interval reverses
   * direction then return false. This ensures that the function returns true only if the sequence
   * is monotonic and evenly sampled within the tolerance.
   *
   * <p>Note that the tolerance is absolute. You can create this from a relative tolerance using:
   * {@code (x[1]-x[0])*relativeTolerance}.
   *
   * @param x the x
   * @param uniformTolerance the uniform tolerance
   * @return true, if is uniform
   */
  public static boolean isUniform(double[] x, double uniformTolerance) {
    if (x.length < MIN_INTERVAL_SIZE) {
      // No intervals to measure
      return true;
    }

    try {
      double interval1 = getInterval(x[1], x[0]);
      final double direction = Math.signum(interval1);

      double interval2 = getInterval(x[2], x[1]);

      // Check each step is roughly the same size and in the same direction.
      // Do this by checking successive steps are equal within the tolerance.
      if (!isUniformInterval(interval2, interval1, direction, uniformTolerance)) {
        return false;
      }

      // The first two steps are within tolerance.
      // Check the rest and sum the intervals to compute the mean.
      double sum = interval1 + interval2;

      for (int i = 3; i < x.length; i++) {
        interval1 = interval2;
        interval2 = getInterval(x[i], x[i - 1]);
        if (!isUniformInterval(interval2, interval1, direction, uniformTolerance)) {
          return false;
        }
        sum += interval2;
      }

      // Each step is within tolerance of the last step.
      // But steps could be getting increasingly larger or smaller so check against the mean.
      final double meanInterval = sum / (x.length - 1);
      for (int i = 1; i < x.length; i++) {
        // All intervals are valid so just subtract
        final double interval = x[i] - x[i - 1];
        if (!isWithinTolerance(interval, meanInterval, uniformTolerance)) {
          return false;
        }
      }
      return true;
    } catch (final ArithmeticException ex) {
      // Bad interval
      return false;
    }
  }

  /**
   * Gets the finite interval between 2 values.
   *
   * @param value1 the value 1
   * @param value2 the value 2
   * @return the interval
   * @throws ArithmeticException if the interval is zero or non-finite
   */
  private static double getInterval(double value1, double value2) {
    final double interval = value1 - value2;
    if (interval == 0 || !Double.isFinite(interval)) {
      throw new ArithmeticException();
    }
    return interval;
  }

  /**
   * Checks if is the interval is is the correct direction and within tolerance of the last
   * interval.
   *
   * @param interval the interval
   * @param lastInterval the last interval
   * @param direction the direction
   * @param tolerance the tolerance
   * @return true, if is uniform interval
   */
  private static boolean isUniformInterval(double interval, double lastInterval, double direction,
      double tolerance) {
    return (Math.signum(interval) == direction
        && isWithinTolerance(interval, lastInterval, tolerance));
  }

  /**
   * Checks if the absolute difference is within tolerance.
   *
   * @param value1 the value 1
   * @param value2 the value 2
   * @param tolerance the tolerance
   * @return true, if is within tolerance
   */
  private static boolean isWithinTolerance(double value1, double value2, double tolerance) {
    return Math.abs(value1 - value2) <= tolerance;
  }

  /**
   * Multiply the data in-place.
   *
   * @param x the x
   * @param factor the factor
   */
  public static void multiply(float[] x, float factor) {
    for (int i = 0; i < x.length; i++) {
      x[i] *= factor;
    }
  }

  /**
   * Multiply the data in-place.
   *
   * @param x the x
   * @param factor the factor
   */
  public static void multiply(float[] x, double factor) {
    for (int i = 0; i < x.length; i++) {
      x[i] *= factor;
    }
  }

  /**
   * Multiply the data in-place.
   *
   * @param x the x
   * @param factor the factor
   */
  public static void multiply(double[] x, double factor) {
    for (int i = 0; i < x.length; i++) {
      x[i] *= factor;
    }
  }

  /**
   * Add to the data in-place.
   *
   * @param x the x
   * @param value the value
   */
  public static void add(double[] x, double value) {
    for (int i = 0; i < x.length; i++) {
      x[i] += value;
    }
  }

  /**
   * Add to the data in-place.
   *
   * @param x the x
   * @param value the value
   */
  public static void add(float[] x, float value) {
    for (int i = 0; i < x.length; i++) {
      x[i] += value;
    }
  }

  /**
   * Add to the data in-place.
   *
   * @param x the x
   * @param value the value
   */
  public static void add(int[] x, int value) {
    for (int i = 0; i < x.length; i++) {
      x[i] += value;
    }
  }

  /**
   * Subtract from the data in-place.
   *
   * @param x the x
   * @param value the value
   */
  public static void subtract(int[] x, int value) {
    for (int i = 0; i < x.length; i++) {
      x[i] -= value;
    }
  }

  /**
   * Find the index of minimum element. The minimum is identified using the {@code <} operator.
   * Special cases:
   *
   * <ul>
   *
   * <li>If the array is empty this returns zero.
   *
   * <li>If multiple equal elements are the minimum this returns the lowest index.
   *
   * </ul>
   *
   * @param data the data
   * @return the minimum index
   */
  public static int findMinIndex(int[] data) {
    int min = 0;
    final int len = data.length;
    for (int i = 1; i < len; i++) {
      if (data[i] < data[min]) {
        min = i;
      }
    }
    return min;
  }

  /**
   * Find the index of minimum element. The minimum is identified using the {@code <} operator.
   * Special cases:
   *
   * <ul>
   *
   * <li>If the array is empty this returns zero.
   *
   * <li>If multiple equal elements are the minimum this returns the lowest index.
   *
   * </ul>
   *
   * @param data the data
   * @return the minimum index
   */
  public static int findMinIndex(float[] data) {
    int min = 0;
    final int len = data.length;
    for (int i = 1; i < len; i++) {
      if (data[i] < data[min]) {
        min = i;
      }
    }
    return min;
  }

  /**
   * Find the index of minimum element. The minimum is identified using the {@code <} operator.
   * Special cases:
   *
   * <ul>
   *
   * <li>If the array is empty this returns zero.
   *
   * <li>If multiple equal elements are the minimum this returns the lowest index.
   *
   * </ul>
   *
   * @param data the data
   * @return the minimum index
   */
  public static int findMinIndex(double[] data) {
    int min = 0;
    final int len = data.length;
    for (int i = 1; i < len; i++) {
      if (data[i] < data[min]) {
        min = i;
      }
    }
    return min;
  }

  /**
   * Find the index of maximum element. The maximum is identified using the {@code >} operator.
   * Special cases:
   *
   * <ul>
   *
   * <li>If the array is empty this returns zero.
   *
   * <li>If multiple equal elements are the maximum this returns the lowest index.
   *
   * </ul>
   *
   * @param data the data
   * @return the maximum index
   */
  public static int findMaxIndex(int[] data) {
    int max = 0;
    final int len = data.length;
    for (int i = 1; i < len; i++) {
      if (data[i] > data[max]) {
        max = i;
      }
    }
    return max;
  }

  /**
   * Find the index of maximum element. The maximum is identified using the {@code >} operator.
   * Special cases:
   *
   * <ul>
   *
   * <li>If the array is empty this returns zero.
   *
   * <li>If multiple equal elements are the maximum this returns the lowest index.
   *
   * </ul>
   *
   * @param data the data
   * @return the maximum index
   */
  public static int findMaxIndex(float[] data) {
    int max = 0;
    final int len = data.length;
    for (int i = 1; i < len; i++) {
      if (data[i] > data[max]) {
        max = i;
      }
    }
    return max;
  }

  /**
   * Find the index of maximum element. The maximum is identified using the {@code >} operator.
   * Special cases:
   *
   * <ul>
   *
   * <li>If the array is empty this returns zero.
   *
   * <li>If multiple equal elements are the maximum this returns the lowest index.
   *
   * </ul>
   *
   * @param data the data
   * @return the maximum index
   */
  public static int findMaxIndex(double[] data) {
    int max = 0;
    final int len = data.length;
    for (int i = 1; i < len; i++) {
      if (data[i] > data[max]) {
        max = i;
      }
    }
    return max;
  }

  /**
   * Find the index of minimum and maximum elements. The minimum or maximum are identified using the
   * {@code <} and {@code >} operators. Special cases:
   *
   * <ul>
   *
   * <li>If the array is empty this returns zero for both indices: [0, 0].
   *
   * <li>If multiple equal elements are the minimum or maximum this returns the lowest index.
   *
   * </ul>
   *
   * @param data the data
   * @return the minimum and maximum index
   */
  public static int[] findMinMaxIndex(int[] data) {
    int min = 0;
    int max = 0;
    final int len = data.length;
    for (int i = 1; i < len; i++) {
      if (data[i] < data[min]) {
        min = i;
      } else if (data[i] > data[max]) {
        max = i;
      }
    }
    return new int[] {min, max};
  }

  /**
   * Find the index of minimum and maximum elements. The minimum or maximum are identified using the
   * {@code <} and {@code >} operators. Special cases:
   *
   * <ul>
   *
   * <li>If the array is empty this returns zero for both indices: [0, 0].
   *
   * <li>If multiple equal elements are the minimum or maximum this returns the lowest index.
   *
   * </ul>
   *
   * @param data the data
   * @return the minimum and maximum index
   */
  public static int[] findMinMaxIndex(float[] data) {
    int min = 0;
    int max = 0;
    final int len = data.length;
    for (int i = 1; i < len; i++) {
      if (data[i] < data[min]) {
        min = i;
      } else if (data[i] > data[max]) {
        max = i;
      }
    }
    return new int[] {min, max};
  }

  /**
   * Find the index of minimum and maximum elements. The minimum or maximum are identified using the
   * {@code <} and {@code >} operators. Special cases:
   *
   * <ul>
   *
   * <li>If the array is empty this returns zero for both indices: [0, 0].
   *
   * <li>If multiple equal elements are the minimum or maximum this returns the lowest index.
   *
   * </ul>
   *
   * @param data the data
   * @return the minimum and maximum index
   */
  public static int[] findMinMaxIndex(double[] data) {
    int min = 0;
    int max = 0;
    final int len = data.length;
    for (int i = 1; i < len; i++) {
      if (data[i] < data[min]) {
        min = i;
      } else if (data[i] > data[max]) {
        max = i;
      }
    }
    return new int[] {min, max};
  }

  /**
   * Returns the index of the first element in the array that matches the given filter, or -1 if the
   * array does not contain a match. More formally, returns the lowest index {@code i} such that
   * {@code filter.test(data[i]) == true}, or -1 if there is no such index.
   *
   * @param data the data
   * @param filter a filter to identify the element
   * @return the index of the first match in the array, or -1 if the array does not contain a
   *         matching element
   */
  public static int findIndex(int[] data, IntPredicate filter) {
    final int len = data.length;
    for (int i = 0; i < len; i++) {
      if (filter.test(data[i])) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns the index of the first element in the array that matches the given filter, or -1 if the
   * array does not contain a match. More formally, returns the lowest index {@code i} such that
   * {@code filter.test(data[i]) == true}, or -1 if there is no such index.
   *
   * @param data the data
   * @param filter a filter to identify the element
   * @return the index of the first match in the array, or -1 if the array does not contain a
   *         matching element
   */
  public static int findIndex(float[] data, FloatPredicate filter) {
    final int len = data.length;
    for (int i = 0; i < len; i++) {
      if (filter.test(data[i])) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns the index of the first element in the array that matches the given filter, or -1 if the
   * array does not contain a match. More formally, returns the lowest index {@code i} such that
   * {@code filter.test(data[i]) == true}, or -1 if there is no such index.
   *
   * @param data the data
   * @param filter a filter to identify the element
   * @return the index of the first match in the array, or -1 if the array does not contain a
   *         matching element
   */
  public static int findIndex(double[] data, DoublePredicate filter) {
    final int len = data.length;
    for (int i = 0; i < len; i++) {
      if (filter.test(data[i])) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns the index of the last element in the array that matches the given filter, or -1 if the
   * array does not contain a match. More formally, returns the highest index {@code i} such that
   * {@code filter.test(data[i]) == true}, or -1 if there is no such index.
   *
   * @param data the data
   * @param filter a filter to identify the element
   * @return the index of the last match in the array, or -1 if the array does not contain a
   *         matching element
   */
  public static int findLastIndex(int[] data, IntPredicate filter) {
    for (int i = data.length - 1; i >= 0; i--) {
      if (filter.test(data[i])) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns the index of the last element in the array that matches the given filter, or -1 if the
   * array does not contain a match. More formally, returns the highest index {@code i} such that
   * {@code filter.test(data[i]) == true}, or -1 if there is no such index.
   *
   * @param data the data
   * @param filter a filter to identify the element
   * @return the index of the last match in the array, or -1 if the array does not contain a
   *         matching element
   */
  public static int findLastIndex(float[] data, FloatPredicate filter) {
    for (int i = data.length - 1; i >= 0; i--) {
      if (filter.test(data[i])) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns the index of the last element in the array that matches the given filter, or -1 if the
   * array does not contain a match. More formally, returns the highest index {@code i} such that
   * {@code filter.test(data[i]) == true}, or -1 if there is no such index.
   *
   * @param data the data
   * @param filter a filter to identify the element
   * @return the index of the last match in the array, or -1 if the array does not contain a
   *         matching element
   */
  public static int findLastIndex(double[] data, DoublePredicate filter) {
    for (int i = data.length - 1; i >= 0; i--) {
      if (filter.test(data[i])) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Gets the ranges of continuous ascending indices in pairs, e.g [0,1,3,4,5,7] returns
   * [0,1,3,5,7,7] (pairs 0-1, 3-5 and 7-7).
   *
   * <p>This method will eliminate duplicate indices as it returns the start and end of the range,
   * e.g. [0,1,2,2,3] returns [0,3].
   *
   * @param indices the indices
   * @return the ranges
   */
  public static int[] getRanges(int[] indices) {
    if (indices == null || indices.length == 0) {
      return ArrayUtils.EMPTY_INT_ARRAY;
    }

    if (indices.length == 1) {
      return new int[] {indices[0], indices[0]};
    }

    // Sort and look for continuous ranges
    Arrays.sort(indices);

    final IntArrayList list = new IntArrayList(indices.length);
    for (int i = 0; i < indices.length; i++) {
      final int start = indices[i];
      int end = start;
      // Allow eliminating duplicates
      while (i + 1 < indices.length && indices[i + 1] <= end + 1) {
        end = indices[++i];
      }
      list.add(start);
      list.add(end);
    }

    return list.toIntArray();
  }

  /**
   * Check the 2D size (width * height) is within the limits of an integer (so suitable for an array
   * index).
   *
   * @param width the width
   * @param height the height
   * @return the size
   * @throws IllegalArgumentException If width or height are not positive
   * @throws IllegalArgumentException If width * height is too large for an integer
   */
  public static int check2DSize(int width, int height) {
    ValidationUtils.checkPositive(width, "Width");
    ValidationUtils.checkPositive(height, "Height");
    final long size = (long) width * height;
    if (size > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("width*height is too large");
    }
    return (int) size;
  }

  /**
   * Check the 2D array can contain data. The array must not be length zero and (width * height) is
   * the same as data.length. The contents of the array are not checked.
   *
   * @param width the width
   * @param height the height
   * @param data the data
   * @throws IllegalArgumentException If the array is null, empty or not equal to width * height
   */
  public static void hasData2D(int width, int height, float[] data) {
    if (ArrayUtils.getLength(data) == 0) {
      throw new IllegalArgumentException(DATA_EMPTY);
    }
    if (check2DSize(width, height) != data.length) {
      throw new IllegalArgumentException(DATA_INCORRECT_SIZE);
    }
  }

  /**
   * Check the 2D array can contain data. The array must not be length zero and (width * height) is
   * the same as data.length. The contents of the array are not checked.
   *
   * @param width the width
   * @param height the height
   * @param data the data
   * @throws IllegalArgumentException If the array is null, empty or not equal to width * height
   */
  public static void hasData2D(int width, int height, double[] data) {
    if (ArrayUtils.getLength(data) == 0) {
      throw new IllegalArgumentException(DATA_EMPTY);
    }
    if (check2DSize(width, height) != data.length) {
      throw new IllegalArgumentException(DATA_INCORRECT_SIZE);
    }
  }

  /**
   * Check the 2D array can contain data. The array must not be length zero and (width * height) is
   * the same as data.length. The contents of the array are not checked.
   *
   * @param width the width
   * @param height the height
   * @param data the data
   * @throws IllegalArgumentException If the array is null, empty or not equal to width * height
   */
  public static void hasData2D(int width, int height, int[] data) {
    if (ArrayUtils.getLength(data) == 0) {
      throw new IllegalArgumentException(DATA_EMPTY);
    }
    if (check2DSize(width, height) != data.length) {
      throw new IllegalArgumentException(DATA_INCORRECT_SIZE);
    }
  }

  /**
   * Check the 2D array can contain data. The array must not be length zero and (width * height) is
   * the same as data.length. The contents of the array are not checked.
   *
   * @param width the width
   * @param height the height
   * @param data the data
   * @throws IllegalArgumentException If the array is null, empty or not equal to width * height
   */
  public static void hasData2D(int width, int height, byte[] data) {
    if (ArrayUtils.getLength(data) == 0) {
      throw new IllegalArgumentException(DATA_EMPTY);
    }
    if (check2DSize(width, height) != data.length) {
      throw new IllegalArgumentException(DATA_INCORRECT_SIZE);
    }
  }

  /**
   * Checks if the object is an array.
   *
   * @param object the object
   * @return true, if is array
   */
  public static boolean isArray(Object object) {
    return object != null && object.getClass().isArray();
  }

  /**
   * Returns a string representation of the object. If an array then the appropriate
   * Arrays.toString(...) method is called depending on the array type.
   *
   * <p>Note: If an instance of Object[] then {@link Arrays#deepToString(Object[])} is called
   * allowing recursion for nested arrays, e.g. int[][].
   *
   * @param object the object
   * @return the string
   */
  public static String toString(Object object) {
    return toString(object, true);
  }

  /**
   * Returns a string representation of the object. If an array then the appropriate
   * Arrays.toString(...) method is called depending on the array type.
   *
   * <p>Note: If an instance of Object[] then optionally {@link Arrays#deepToString(Object[])} is
   * called allowing recursion for nested arrays, e.g. int[][].
   *
   * @param object the object
   * @param deepToString Set to true to call Arrays#deepToString(Object[]) for Object arrays
   * @return the string
   */
  public static String toString(Object object, boolean deepToString) {
    if (object == null) {
      return "null";
    }
    final Class<?> clazz = object.getClass();
    if (clazz.isArray()) {
      return arrayToString(object, deepToString, clazz);
    }
    return object.toString();
  }

  /**
   * Returns a string representation of the array object using the appropriate Arrays.toString(...)
   * method depending on the array type.
   *
   * <p>Note: If an instance of Object[] then optionally {@link Arrays#deepToString(Object[])} is
   * called allowing recursion for nested arrays, e.g. int[][].
   *
   * @param object the object
   * @param deepToString Set to true to call Arrays#deepToString(Object[]) for Object arrays
   * @param clazz the array class
   * @return the string
   */
  private static String arrayToString(Object object, boolean deepToString, final Class<?> clazz) {
    // Check primitive types
    //@formatter:off
    if (clazz ==  int      [].class) {
      return Arrays.toString((int       []) object);
    }
    if (clazz ==  double   [].class) {
      return Arrays.toString((double    []) object);
    }
    if (clazz ==  float    [].class) {
      return Arrays.toString((float     []) object);
    }
    if (clazz ==  boolean  [].class) {
      return Arrays.toString((boolean   []) object);
    }
    if (clazz ==  byte     [].class) {
      return Arrays.toString((byte      []) object);
    }
    if (clazz ==  long     [].class) {
      return Arrays.toString((long      []) object);
    }
    if (clazz ==  short    [].class) {
      return Arrays.toString((short     []) object);
    }
    if (clazz ==  char     [].class) {
      return Arrays.toString((char      []) object);
    }
    // Support optional recursion
    return (deepToString)
        ? Arrays.deepToString((Object[]) object)
        : Arrays.toString(    (Object[]) object);
    //@formatter:on
  }

  /**
   * Deep copy the values.
   *
   * @param values the values
   * @return the copy
   * @throws NullPointerException If any array reference is null
   */
  public static double[][] deepCopy(double[][] values) {
    return Arrays.stream(values).map(double[]::clone).toArray(double[][]::new);
  }

  /**
   * Deep copy the values.
   *
   * @param values the values
   * @return the copy
   * @throws NullPointerException If any array reference is null
   */
  public static float[][] deepCopy(float[][] values) {
    return Arrays.stream(values).map(float[]::clone).toArray(float[][]::new);
  }

  /**
   * Deep copy the values.
   *
   * @param values the values
   * @return the copy
   * @throws NullPointerException If any array reference is null
   */
  public static int[][] deepCopy(int[][] values) {
    return Arrays.stream(values).map(int[]::clone).toArray(int[][]::new);
  }

  /**
   * Deep copy the values.
   *
   * @param values the values
   * @return the copy
   * @throws NullPointerException If any array reference is null
   */
  public static double[][][] deepCopy(double[][][] values) {
    return Arrays.stream(values).map(
        // Function to clone each double[][] element of double[][][]
        SimpleArrayUtils::deepCopy).toArray(double[][][]::new);
  }

  /**
   * Deep copy the values.
   *
   * @param values the values
   * @return the copy
   * @throws NullPointerException If any array reference is null
   */
  public static float[][][] deepCopy(float[][][] values) {
    return Arrays.stream(values).map(
        // Function to clone each float[][] element of float[][][]
        SimpleArrayUtils::deepCopy).toArray(float[][][]::new);
  }

  /**
   * Deep copy the values.
   *
   * @param values the values
   * @return the copy
   * @throws NullPointerException If any array reference is null
   */
  public static int[][][] deepCopy(int[][][] values) {
    return Arrays.stream(values).map(
        // Function to clone each int[][] element of int[][][]
        SimpleArrayUtils::deepCopy).toArray(int[][][]::new);
  }

  /**
   * Checks if all values pass {@link Double#isFinite(double)}.
   *
   * @param values the values
   * @return true if all values are finite
   */
  public static boolean isFinite(double[] values) {
    for (final double value : values) {
      if (!Double.isFinite(value)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if all values pass {@link Float#isFinite(float)}.
   *
   * @param values the values
   * @return true if all values are finite
   */
  public static boolean isFinite(float[] values) {
    for (final float value : values) {
      if (!Float.isFinite(value)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Swap the value of the two indices.
   *
   * @param data the data
   * @param index1 the first index
   * @param index2 the second index
   */
  public static void swap(int[] data, int index1, int index2) {
    final int tmp = data[index1];
    data[index1] = data[index2];
    data[index2] = tmp;
  }

  /**
   * Swap the value of the two indices.
   *
   * @param data the data
   * @param index1 the first index
   * @param index2 the second index
   */
  public static void swap(float[] data, int index1, int index2) {
    final float tmp = data[index1];
    data[index1] = data[index2];
    data[index2] = tmp;
  }

  /**
   * Swap the value of the two indices.
   *
   * @param data the data
   * @param index1 the first index
   * @param index2 the second index
   */
  public static void swap(double[] data, int index1, int index2) {
    final double tmp = data[index1];
    data[index1] = data[index2];
    data[index2] = tmp;
  }

  /**
   * Swap the value of the two indices.
   *
   * @param data the data
   * @param index1 the first index
   * @param index2 the second index
   */
  public static void swap(byte[] data, int index1, int index2) {
    final byte tmp = data[index1];
    data[index1] = data[index2];
    data[index2] = tmp;
  }

  /**
   * Swap the value of the two indices.
   *
   * @param data the data
   * @param index1 the first index
   * @param index2 the second index
   */
  public static void swap(short[] data, int index1, int index2) {
    final short tmp = data[index1];
    data[index1] = data[index2];
    data[index2] = tmp;
  }

  /**
   * Swap the value of the two indices.
   *
   * @param <T> the array type
   * @param data the data
   * @param index1 the first index
   * @param index2 the second index
   */
  public static <T> void swap(T[] data, int index1, int index2) {
    final T tmp = data[index1];
    data[index1] = data[index2];
    data[index2] = tmp;
  }

  /**
   * Ensure the array can hold up to {@code size} values. The input {@code array} will be reused if
   * not {@code null} and has a length of at least {@code size}. Otherwise a new array will be
   * created.
   *
   * <p>Note that no values are preserved from the old array if a new array is returned. The
   * returned array may be larger than {@code size}.
   *
   * @param array the current array
   * @param size the size
   * @return the array
   */
  public static @NotNull int[] ensureSize(@Nullable int[] array, int size) {
    if (array == null || array.length < size) {
      return new int[size];
    }
    return array;
  }

  /**
   * Ensure the array can hold up to {@code size} values. The input {@code array} will be reused if
   * not {@code null} and has a length of at least {@code size}. Otherwise a new array will be
   * created.
   *
   * <p>Note that no values are preserved from the old array if a new array is returned. The
   * returned array may be larger than {@code size}.
   *
   * @param array the current array
   * @param size the size
   * @return the array
   */
  public static @NotNull float[] ensureSize(@Nullable float[] array, int size) {
    if (array == null || array.length < size) {
      return new float[size];
    }
    return array;
  }

  /**
   * Ensure the array can hold up to {@code size} values. The input {@code array} will be reused if
   * not {@code null} and has a length of at least {@code size}. Otherwise a new array will be
   * created.
   *
   * <p>Note that no values are preserved from the old array if a new array is returned. The
   * returned array may be larger than {@code size}.
   *
   * @param array the current array
   * @param size the size
   * @return the array
   */
  public static @NotNull double[] ensureSize(@Nullable double[] array, int size) {
    if (array == null || array.length < size) {
      return new double[size];
    }
    return array;
  }

  /**
   * Ensure the array can hold up to {@code size} values. The input {@code array} will be reused if
   * not {@code null} and has a length of at least {@code size}. Otherwise a new array will be
   * created.
   *
   * <p>Note that no values are preserved from the old array if a new array is returned. The
   * returned array may be larger than {@code size}.
   *
   * @param array the current array
   * @param size the size
   * @return the array
   */
  public static @NotNull byte[] ensureSize(@Nullable byte[] array, int size) {
    if (array == null || array.length < size) {
      return new byte[size];
    }
    return array;
  }

  /**
   * Ensure the array can hold up to {@code size} values. The input {@code array} will be reused if
   * not {@code null} and has a length of at least {@code size}. Otherwise a new array will be
   * created.
   *
   * <p>Note that no values are preserved from the old array if a new array is returned. The
   * returned array may be larger than {@code size}.
   *
   * @param array the current array
   * @param size the size
   * @return the array
   */
  public static @NotNull short[] ensureSize(@Nullable short[] array, int size) {
    if (array == null || array.length < size) {
      return new short[size];
    }
    return array;
  }

  /**
   * Get the value from the array if the index is valid, otherwise return a default value. The index
   * must be within the range zero, inclusive, to {@code size}, exclusive.
   *
   * @param <T> the array type
   * @param index the index
   * @param array the array
   * @param defaultValue the default value (can be null)
   * @return the value
   * @throws NullPointerException if the array is {@code null}
   */
  public static <T> T getIndex(int index, T[] array, @Nullable T defaultValue) {
    return (index < 0 || index >= array.length) ? defaultValue : array[index];
  }

  /**
   * Apply the operator to each value in the array.
   *
   * @param array the array
   * @param operator the operator
   */
  public static void apply(int[] array, IntUnaryOperator operator) {
    final int len = array.length;
    for (int i = 0; i < len; i++) {
      array[i] = operator.applyAsInt(array[i]);
    }
  }

  /**
   * Apply the operator to each value in the array.
   *
   * @param array the array
   * @param operator the operator
   */
  public static void apply(long[] array, LongUnaryOperator operator) {
    final int len = array.length;
    for (int i = 0; i < len; i++) {
      array[i] = operator.applyAsLong(array[i]);
    }
  }

  /**
   * Apply the operator to each value in the array.
   *
   * @param array the array
   * @param operator the operator
   */
  public static void apply(float[] array, FloatUnaryOperator operator) {
    final int len = array.length;
    for (int i = 0; i < len; i++) {
      array[i] = operator.applyAsFloat(array[i]);
    }
  }

  /**
   * Apply the operator to each value in the array.
   *
   * @param array the array
   * @param operator the operator
   */
  public static void apply(double[] array, DoubleUnaryOperator operator) {
    final int len = array.length;
    for (int i = 0; i < len; i++) {
      array[i] = operator.applyAsDouble(array[i]);
    }
  }

  /**
   * Apply the operator to each value in the array interval.
   *
   * @param array the array
   * @param from low point (inclusive) of the array
   * @param to high point (exclusive) of the array
   * @param operator the operator
   */
  public static void apply(int[] array, int from, int to, IntUnaryOperator operator) {
    checkRangeForSubList(from, to, array.length);
    for (int i = from; i < to; i++) {
      array[i] = operator.applyAsInt(array[i]);
    }
  }

  /**
   * Apply the operator to each value in the array interval.
   *
   * @param array the array
   * @param from low point (inclusive) of the array
   * @param to high point (exclusive) of the array
   * @param operator the operator
   */
  public static void apply(long[] array, int from, int to, LongUnaryOperator operator) {
    checkRangeForSubList(from, to, array.length);
    for (int i = from; i < to; i++) {
      array[i] = operator.applyAsLong(array[i]);
    }
  }

  /**
   * Apply the operator to each value in the array interval.
   *
   * @param array the array
   * @param from low point (inclusive) of the array
   * @param to high point (exclusive) of the array
   * @param operator the operator
   */
  public static void apply(float[] array, int from, int to, FloatUnaryOperator operator) {
    checkRangeForSubList(from, to, array.length);
    for (int i = from; i < to; i++) {
      array[i] = operator.applyAsFloat(array[i]);
    }
  }

  /**
   * Apply the operator to each value in the array interval.
   *
   * @param array the array
   * @param from low point (inclusive) of the array
   * @param to high point (exclusive) of the array
   * @param operator the operator
   */
  public static void apply(double[] array, int from, int to, DoubleUnaryOperator operator) {
    checkRangeForSubList(from, to, array.length);
    for (int i = from; i < to; i++) {
      array[i] = operator.applyAsDouble(array[i]);
    }
  }

  /**
   * Check the interval [fromIndex, toIndex) is within the range [0, size), thus it is a valid range
   * for a sub-list within the list of the specified size.
   *
   * @param fromIndex low point (inclusive) of the sub-list
   * @param toIndex high point (exclusive) of the sub-list
   * @param size the size
   * @throws IndexOutOfBoundsException if {@code fromIndex < 0} or {@code toIndex > size}
   * @throws IllegalArgumentException if {@code toIndex < fromIndex}
   */
  private static void checkRangeForSubList(int fromIndex, int toIndex, int size) {
    if (fromIndex < 0) {
      throw new IndexOutOfBoundsException("From index " + fromIndex);
    }
    if (toIndex > size) {
      throw new IndexOutOfBoundsException("To index " + toIndex + " not valid for size " + size);
    }
    if (toIndex < fromIndex) {
      throw new IllegalArgumentException("Invalid range, to " + toIndex + " < from " + fromIndex);
    }
  }

  /**
   * Fill an array using the provided generator.
   *
   * <p>This function is a dynamic alternative to {@link Arrays#fill(Object[], Object)}.
   *
   * @param <T> the array type
   * @param array the array
   * @param generator the generator of the element for the array index
   * @return The array
   * @see Arrays#fill(Object[], Object)
   */
  public static <T> T[] fill(T[] array, IntFunction<T> generator) {
    final int len = array.length;
    for (int i = 0; i < len; i++) {
      array[i] = generator.apply(i);
    }
    return array;
  }

  /**
   * Fill an array using the provided supplier.
   *
   * <p>This function is a dynamic alternative to {@link Arrays#fill(Object[], Object)}.
   *
   * @param <T> the array type
   * @param array the array
   * @param supplier the supplier of the elements for the array
   * @return The array
   * @see Arrays#fill(Object[], Object)
   */
  public static <T> T[] fill(T[] array, Supplier<T> supplier) {
    final int len = array.length;
    for (int i = 0; i < len; i++) {
      array[i] = supplier.get();
    }
    return array;
  }
}
