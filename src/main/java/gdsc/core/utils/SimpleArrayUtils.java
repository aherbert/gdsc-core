package gdsc.core.utils;

import java.util.Arrays;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;

/*----------------------------------------------------------------------------- 
 * GDSC ImageJ Plugins Software
 * 
 * Copyright (C) 2017 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

/**
 * Class for manipulating arrays
 * 
 * @author Alex Herbert
 */
public class SimpleArrayUtils
{
	/**
	 * Merge the two sets into a single set using a hashset. The order is undefined. Input sets are unchanged.
	 *
	 * @param s1
	 *            the first set
	 * @param s2
	 *            the second set
	 * @return the merged set
	 */
	public static int[] merge(int[] s1, int[] s2)
	{
		return merge(s1, s2, false);
	}

	/**
	 * Merge the two sets into a single set using a hashset. The order is undefined. Input sets are unchanged.
	 *
	 * @param s1
	 *            the first set
	 * @param s2
	 *            the second set
	 * @param unique
	 *            Set to true if the values in the sets are unique (allows optimisation of hashset size)
	 * @return the merged set
	 */
	public static int[] merge(int[] s1, int[] s2, boolean unique)
	{
		TIntHashSet set;
		if (unique)
			set = new TIntHashSet(Math.max(s1.length, s2.length));
		else
			set = new TIntHashSet();
		set.addAll(s1);
		set.addAll(s2);
		return set.toArray();
	}

	/**
	 * Merge the two sets into a single set in ascending order. Both sets are destructively modified.
	 * <p>
	 * Note: This is rarely as fast as calling {@link #merge(int[], int[])} and then performing a sort.
	 *
	 * @param s1
	 *            the first set
	 * @param s2
	 *            the second set
	 * @return the merged set
	 * @deprecated Use merge and sort instead
	 */
	@Deprecated
	public static int[] sortMerge(int[] s1, int[] s2)
	{
		s1 = flatten(s1);
		s2 = flatten(s2);
		Arrays.sort(s2);

		int i = 0;
		int j = 0;
		TIntArrayList list = new TIntArrayList(Math.max(s1.length, s2.length));
		while (i < s1.length && j < s2.length)
		{
			if (s1[i] < s2[j])
			{
				// Advance s1
				list.add(s1[i++]);
				while (i < s1.length && s1[i] < s2[j])
				{
					list.add(s1[i++]);
				}
			}
			else if (s1[i] > s2[j])
			{
				// Advance s2
				list.add(s2[j++]);
				while (j < s2.length && s2[j] < s1[i])
				{
					list.add(s2[j++]);
				}
			}
			else
			{
				// Advance both
				list.add(s1[i++]);
				j++;
				while (i < s1.length && j < s2.length && s1[i] == s2[j])
				{
					list.add(s1[i++]);
					j++;
				}
			}
		}

		// Add the remaining data
		if (i != s1.length)
		{
			list.ensureCapacity(s1.length - i + list.size());
			while (i < s1.length)
			{
				list.add(s1[i++]);
			}
		}
		else if (j != s2.length)
		{
			list.ensureCapacity(s2.length - j + list.size());
			while (j < s2.length)
			{
				list.add(s2[j++]);
			}
		}

		return list.toArray();
	}

	/**
	 * Flatten the array into an ascending array of unique values. Input data are destructively modified.
	 * <p>
	 * Inputting null will return a zero sized array.
	 *
	 * @param s
	 *            the array
	 * @return the new array
	 */
	public static int[] flatten(int[] s)
	{
		if (s == null)
			return new int[0];
		if (s.length <= 1)
			return s;
		Arrays.sort(s);
		int c = 0;
		for (int i = 1; i < s.length; i++)
		{
			if (s[i] != s[c])
			{
				s[++c] = s[i];
			}
		}
		return Arrays.copyOf(s, c + 1);
	}

	/**
	 * Convert the input array to a double
	 * 
	 * @param a
	 * @return The new array
	 */
	public static double[] toDouble(float[] a)
	{
		if (a == null)
			return null;
		double[] b = new double[a.length];
		for (int i = 0; i < a.length; i++)
			b[i] = a[i];
		return b;
	}

	/**
	 * Convert the input array to a double
	 * 
	 * @param a
	 * @return The new array
	 */
	public static double[] toDouble(int[] a)
	{
		if (a == null)
			return null;
		double[] b = new double[a.length];
		for (int i = 0; i < a.length; i++)
			b[i] = a[i];
		return b;
	}

	/**
	 * Convert the input array to a float
	 * 
	 * @param a
	 * @return The new array
	 */
	public static float[] toFloat(double[] a)
	{
		if (a == null)
			return null;
		float[] b = new float[a.length];
		for (int i = 0; i < a.length; i++)
			b[i] = (float) a[i];
		return b;
	}

	/**
	 * Convert the input array to a float
	 * 
	 * @param a
	 * @return The new array
	 */
	public static float[] toFloat(int[] a)
	{
		if (a == null)
			return null;
		float[] b = new float[a.length];
		for (int i = 0; i < a.length; i++)
			b[i] = (float) a[i];
		return b;
	}

	/**
	 * Create and fill an array
	 * 
	 * @param length
	 *            The length of the array
	 * @param start
	 *            The start
	 * @param increment
	 *            The increment
	 * @return The new array
	 */
	public static double[] newArray(int length, double start, double increment)
	{
		double[] data = new double[length];
		for (int i = 0; i < length; i++)
			data[i] = start + i * increment;
		return data;
	}

	/**
	 * Create and fill an array
	 * 
	 * @param length
	 *            The length of the array
	 * @param start
	 *            The start
	 * @param increment
	 *            The increment
	 * @return The new array
	 */
	public static float[] newArray(int length, float start, float increment)
	{
		float[] data = new float[length];
		for (int i = 0; i < length; i++)
			data[i] = start + i * increment;
		return data;
	}

	/**
	 * Create and fill an array
	 * 
	 * @param length
	 *            The length of the array
	 * @param start
	 *            The start
	 * @param increment
	 *            The increment
	 * @return The new array
	 */
	public static int[] newArray(int length, int start, int increment)
	{
		int[] data = new int[length];
		for (int i = 0; i < length; i++, start += increment)
			data[i] = start;
		return data;
	}

	/**
	 * Convert the data to strictly positive. Any value that is zero or below is set the minimum value above zero.
	 * <p>
	 * If no data is above zero then an array of zero is returned.
	 *
	 * @param data
	 *            the data
	 * @return the strictly positive variance
	 */
	public static float[] ensureStrictlyPositive(float[] data)
	{
		for (int i = 0, n = data.length; i < n; i++)
		{
			if (data[i] <= 0)
			{
				// Not strictly positive so create a clone

				final float[] v = new float[n];
				if (i != 0)
					// Copy the values that were positive
					System.arraycopy(data, 0, v, 0, i);

				// Find the min above zero
				float min = minAboveZero(data);
				if (min == 0)
					return v;

				v[i] = min; // We know this was not strictly positive

				// Check and copy the rest 
				while (++i < n)
				{
					v[i] = (data[i] <= 0) ? min : data[i];
				}
				return v;
			}
		}
		return data;
	}

	/**
	 * Find the minimum above zero.
	 * <p>
	 * Returns zero if no values are above zero.
	 *
	 * @param data
	 *            the data
	 * @return the minimum above zero
	 */
	public static float minAboveZero(float[] data)
	{
		float min = Float.POSITIVE_INFINITY;
		for (int i = 0, n = data.length; i < n; i++)
		{
			if (data[i] > 0 && min > data[i])
				min = data[i];
		}
		// Check if any values were above zero, else return zero
		return (min == Float.POSITIVE_INFINITY) ? 0 : min;
	}

	/**
	 * Create a new double array with the given value.
	 *
	 * @param length
	 *            the length
	 * @param value
	 *            the value
	 * @return the double array
	 */
	public static double[] newDoubleArray(int length, double value)
	{
		double[] data = new double[length];
		Arrays.fill(data, value);
		return data;
	}

	/**
	 * Create a new float array with the given value.
	 *
	 * @param length
	 *            the length
	 * @param value
	 *            the value
	 * @return the float array
	 */
	public static float[] newFloatArray(int length, float value)
	{
		float[] data = new float[length];
		Arrays.fill(data, value);
		return data;
	}

	/**
	 * Create a new int array with the given value.
	 *
	 * @param length
	 *            the length
	 * @param value
	 *            the value
	 * @return the int array
	 */
	public static int[] newIntArray(int length, int value)
	{
		int[] data = new int[length];
		Arrays.fill(data, value);
		return data;
	}

	/**
	 * Create a new byte array with the given value.
	 *
	 * @param length
	 *            the length
	 * @param value
	 *            the value
	 * @return the byte array
	 */
	public static byte[] newByteArray(int length, byte value)
	{
		byte[] data = new byte[length];
		Arrays.fill(data, value);
		return data;
	}

	/**
	 * Reverse the array order
	 * 
	 * @param data
	 */
	public static void reverse(int[] data)
	{
		int left = 0;
		int right = data.length - 1;

		while (left < right)
		{
			// swap the values at the left and right indices
			int temp = data[left];
			data[left] = data[right];
			data[right] = temp;

			// move the left and right index pointers in toward the center
			left++;
			right--;
		}
	}

	/**
	 * Reverse the array order
	 * 
	 * @param data
	 */
	public static void reverse(float[] data)
	{
		int left = 0;
		int right = data.length - 1;

		while (left < right)
		{
			// swap the values at the left and right indices
			float temp = data[left];
			data[left] = data[right];
			data[right] = temp;

			// move the left and right index pointers in toward the center
			left++;
			right--;
		}
	}

	/**
	 * Reverse the array order
	 * 
	 * @param data
	 */
	public static void reverse(double[] data)
	{
		int left = 0;
		int right = data.length - 1;

		while (left < right)
		{
			// swap the values at the left and right indices
			double temp = data[left];
			data[left] = data[right];
			data[right] = temp;

			// move the left and right index pointers in toward the center
			left++;
			right--;
		}
	}

	/**
	 * Checks if all the values have an integer representation.
	 *
	 * @param x
	 *            the x
	 * @return true, if is integer
	 */
	public static boolean isInteger(double[] x)
	{
		for (int i = 0; i < x.length; i++)
			if ((int) x[i] != x[i])
				return false;
		return true;
	}

	/**
	 * Checks if all the values have an integer representation.
	 *
	 * @param x
	 *            the x
	 * @return true, if is integer
	 */
	public static boolean isInteger(float[] x)
	{
		for (int i = 0; i < x.length; i++)
			if ((int) x[i] != x[i])
				return false;
		return true;
	}

	/**
	 * Checks if all the values have a uniform interval between them. The interval between each successive pair is
	 * compared to the mean interval. If the error is greater than the tolerance then return false. If any interval is
	 * zero then return false. If any interval reverses direction then return false. This ensures that the function
	 * returns true only if the sequence is monotonic and evenly sampled within the tolerance.
	 * <p>
	 * Note that the tolerance is absolute. You can create this from a relative tolerance using:
	 * <code>(x[1]-x[0])*relativeTolerance</code>.
	 *
	 * @param x
	 *            the x
	 * @param uniformTolerance
	 *            the uniform tolerance
	 * @return true, if is uniform
	 */
	public static boolean isUniform(double[] x, double uniformTolerance)
	{
		if (x.length <= 2)
			return true;
		double sum = 0;
		double reference = 0;
		double direction = Math.signum(x[1] - x[0]);
		if (direction == 0.0)
			return false;
		for (int i = 1; i < x.length; i++)
		{
			double interval = x[i] - x[i - 1];
			if (Math.signum(interval) != direction)
				return false;
			sum += interval;
			// Difference from last. Use this to avoid having to compute the mean if the intervals are very different.
			if (i != 1)
			{
				if (interval > reference)
				{
					if (interval - reference > uniformTolerance)
						return false;
				}
				else
				{
					if (reference - interval > uniformTolerance)
						return false;
				}
			}
			reference = interval;
		}
		// Check against the mean 
		reference = sum / (x.length - 1);
		for (int i = 1; i < x.length; i++)
		{
			double interval = x[i] - x[i - 1];
			if (interval > reference)
			{
				if (interval - reference > uniformTolerance)
					return false;
			}
			else
			{
				if (reference - interval > uniformTolerance)
					return false;
			}
		}
		return true;
	}

	/**
	 * Checks if all the values have a uniform interval between them. The interval between each successive pair is
	 * compared to the first interval. If different then return false. If the first interval is zero then return false.
	 * This ensures that the function returns true only if the sequence is monotonic and evenly sampled.
	 *
	 * @param x
	 *            the x
	 * @return true, if is uniform
	 */
	public static boolean isUniform(int[] x)
	{
		if (x.length <= 2)
			return true;
		final int reference = x[1] - x[0];
		if (reference == 0)
			return false;
		for (int i = 2; i < x.length; i++)
		{
			int interval = x[i] - x[i - 1];
			if (interval != reference)
				return false;
		}
		return true;
	}

	/**
	 * Multiply the data in-place.
	 *
	 * @param x
	 *            the x
	 * @param factor
	 *            the factor
	 */
	public static void multiply(float[] x, float factor)
	{
		for (int i = 0; i < x.length; i++)
			x[i] *= factor;
	}

	/**
	 * Multiply the data in-place.
	 *
	 * @param x
	 *            the x
	 * @param factor
	 *            the factor
	 */
	public static void multiply(float[] x, double factor)
	{
		for (int i = 0; i < x.length; i++)
			x[i] *= factor;
	}

	/**
	 * Scale the data in-place.
	 *
	 * @param x
	 *            the x
	 * @param addition
	 *            the scale
	 */
	public static void add(float[] x, float addition)
	{
		for (int i = 0; i < x.length; i++)
			x[i] += addition;
	}

	/**
	 * Multiply the data in-place.
	 *
	 * @param x
	 *            the x
	 * @param factor
	 *            the factor
	 */
	public static void multiply(double[] x, double factor)
	{
		for (int i = 0; i < x.length; i++)
			x[i] *= factor;
	}

	/**
	 * Scale the data in-place.
	 *
	 * @param x
	 *            the x
	 * @param addition
	 *            the scale
	 */
	public static void add(double[] x, double addition)
	{
		for (int i = 0; i < x.length; i++)
			x[i] += addition;
	}

	/**
	 * Find min index
	 *
	 * @param data
	 *            the data
	 * @return the min index
	 */
	public static int findMinIndex(int[] data)
	{
		int min = 0;
		for (int i = 0; i < data.length; i++)
			if (data[i] < data[min])
				min = i;
		return min;
	}

	/**
	 * Find max index.
	 *
	 * @param data
	 *            the data
	 * @return the max index
	 */
	public static int findMaxIndex(int[] data)
	{
		int max = 0;
		for (int i = 0; i < data.length; i++)
			if (data[i] > data[max])
				max = i;
		return max;
	}

	/**
	 * Find min index
	 *
	 * @param data
	 *            the data
	 * @return the min/max index
	 */
	public static int[] findMinMaxIndex(int[] data)
	{
		int min = 0, max = 0;

		for (int i = 0; i < data.length; i++)
			if (data[i] < data[min])
				min = i;
			else if (data[i] > data[min])
				max = i;
		return new int[] { min, max };
	}

	/**
	 * Find min index
	 *
	 * @param data
	 *            the data
	 * @return the min index
	 */
	public static int findMinIndex(float[] data)
	{
		int min = 0;
		for (int i = 0; i < data.length; i++)
			if (data[i] < data[min])
				min = i;
		return min;
	}

	/**
	 * Find max index.
	 *
	 * @param data
	 *            the data
	 * @return the max index
	 */
	public static int findMaxIndex(float[] data)
	{
		int max = 0;
		for (int i = 0; i < data.length; i++)
			if (data[i] > data[max])
				max = i;
		return max;
	}

	/**
	 * Find min index
	 *
	 * @param data
	 *            the data
	 * @return the min/max index
	 */
	public static int[] findMinMaxIndex(float[] data)
	{
		int min = 0, max = 0;

		for (int i = 0; i < data.length; i++)
			if (data[i] < data[min])
				min = i;
			else if (data[i] > data[min])
				max = i;
		return new int[] { min, max };
	}

	/**
	 * Find min index
	 *
	 * @param data
	 *            the data
	 * @return the min index
	 */
	public static int findMinIndex(double[] data)
	{
		int min = 0;
		for (int i = 0; i < data.length; i++)
			if (data[i] < data[min])
				min = i;
		return min;
	}

	/**
	 * Find max index.
	 *
	 * @param data
	 *            the data
	 * @return the max index
	 */
	public static int findMaxIndex(double[] data)
	{
		int max = 0;
		for (int i = 0; i < data.length; i++)
			if (data[i] > data[max])
				max = i;
		return max;
	}

	/**
	 * Find min index
	 *
	 * @param data
	 *            the data
	 * @return the min/max index
	 */
	public static int[] findMinMaxIndex(double[] data)
	{
		int min = 0, max = 0;

		for (int i = 0; i < data.length; i++)
			if (data[i] < data[min])
				min = i;
			else if (data[i] > data[min])
				max = i;
		return new int[] { min, max };
	}

	/**
	 * Gets the ranges of continuous ascending indices in pairs, e.g [0,1,3,4,5,7] returns [0,1,3,5,7,7] (pairs 0-1, 3-5
	 * and 7-7).
	 * <p>
	 * This method will eliminate duplicate indices as it returns the start and end of the range, e.g. [0,1,2,2,3]
	 * returns [0,3].
	 *
	 * @param indices
	 *            the indices
	 * @return the ranges
	 */
	public static int[] getRanges(int[] indices)
	{
		if (indices == null || indices.length == 0)
			return new int[0];

		if (indices.length == 1)
			return new int[] { indices[0], indices[0] };

		// Sort and look for continuous ranges
		Arrays.sort(indices);

		TIntArrayList list = new TIntArrayList(indices.length);
		for (int i = 0; i < indices.length; i++)
		{
			int start = indices[i];
			int end = start;
			// Allow eliminating duplicates
			while (i + 1 < indices.length && indices[i + 1] <= end + 1)
			{
				end = indices[++i];
			}
			list.add(start);
			list.add(end);
		}

		return list.toArray();
	}

	/**
	 * Check the 2D size (width * height) is within the limits of an integer (so suitable for an array index).
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @return the size
	 * @throws IllegalArgumentException
	 *             If width or height are not positive
	 * @throws IllegalArgumentException
	 *             If width * height is too large for an integer
	 */
	public static int check2DSize(int width, int height) throws IllegalArgumentException
	{
		if (width < 0)
			throw new IllegalArgumentException("Width cannot be less than 1");
		if (height < 0)
			throw new IllegalArgumentException("Height cannot be less than 1");
		long size = (long) width * height;
		if (size > Integer.MAX_VALUE)
			throw new IllegalArgumentException("width*height is too large");
		return (int) size;
	}

	/**
	 * Check the 2D array can contain data. The array must not be length zero and (width * height) is the same as
	 * data.length. The contents of the array are not checked.
	 * <p>
	 * Note that data.length == 0 will cause an exception.
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param data
	 *            the data
	 * @return the size
	 * @throws IllegalArgumentException
	 *             If width or height are not strictly positive
	 * @throws IllegalArgumentException
	 *             If width * height is too large for an integer
	 * @throws NullPointerException
	 *             If the data is null
	 */
	public static void hasData2D(int width, int height, float[] data)
			throws IllegalArgumentException, NullPointerException
	{
		if (data == null || data.length == 0)
			throw new IllegalArgumentException("data is empty");
		if (check2DSize(width, height) != data.length)
			throw new IllegalArgumentException("data is not the correct array size");
	}

	/**
	 * Check the 2D array can contain data. The array must not be length zero and (width * height) is the same as
	 * data.length. The contents of the array are not checked.
	 * <p>
	 * Note that data.length == 0 will cause an exception.
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param data
	 *            the data
	 * @return the size
	 * @throws IllegalArgumentException
	 *             If width or height are not strictly positive
	 * @throws IllegalArgumentException
	 *             If width * height is too large for an integer
	 * @throws NullPointerException
	 *             If the data is null
	 */
	public static void hasData2D(int width, int height, double[] data)
			throws IllegalArgumentException, NullPointerException
	{
		if (data == null || data.length == 0)
			throw new IllegalArgumentException("data is empty");
		if (check2DSize(width, height) != data.length)
			throw new IllegalArgumentException("data is not the correct array size");
	}

	/**
	 * Check the 2D array can contain data. The array must not be length zero and (width * height) is the same as
	 * data.length. The contents of the array are not checked.
	 * <p>
	 * Note that data.length == 0 will cause an exception.
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param data
	 *            the data
	 * @return the size
	 * @throws IllegalArgumentException
	 *             If width or height are not strictly positive
	 * @throws IllegalArgumentException
	 *             If width * height is too large for an integer
	 * @throws NullPointerException
	 *             If the data is null
	 */
	public static void hasData2D(int width, int height, int[] data)
			throws IllegalArgumentException, NullPointerException
	{
		if (data == null || data.length == 0)
			throw new IllegalArgumentException("data is empty");
		if (check2DSize(width, height) != data.length)
			throw new IllegalArgumentException("data is not the correct array size");
	}

	/**
	 * Check the 2D array can contain data. The array must not be length zero and (width * height) is the same as
	 * data.length. The contents of the array are not checked.
	 * <p>
	 * Note that data.length == 0 will cause an exception.
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param data
	 *            the data
	 * @return the size
	 * @throws IllegalArgumentException
	 *             If width or height are not strictly positive
	 * @throws IllegalArgumentException
	 *             If width * height is too large for an integer
	 * @throws NullPointerException
	 *             If the data is null
	 */
	public static void hasData2D(int width, int height, byte[] data)
			throws IllegalArgumentException, NullPointerException
	{
		if (data == null || data.length == 0)
			throw new IllegalArgumentException("data is empty");
		if (check2DSize(width, height) != data.length)
			throw new IllegalArgumentException("data is not the correct array size");
	}

	/**
	 * Checks if the object is an array.
	 *
	 * @param o
	 *            the object
	 * @return true, if is array
	 */
	public static boolean isArray(Object o)
	{
		return o != null && o.getClass().isArray();
	}

	/**
	 * Returns a string representation of the object. If an array then the appropriate Arrays.toString(...) method is
	 * called depending on the array type.
	 *
	 * @param o
	 *            the object
	 * @return the string
	 */
	public static String toString(Object o)
	{
		if (o != null)
		{
			if (o.getClass().isArray())
			{
				//@formatter:off
				if (o instanceof int      []) return Arrays.toString((int       []) o);
				if (o instanceof double   []) return Arrays.toString((double    []) o);
				if (o instanceof float    []) return Arrays.toString((float     []) o);
				if (o instanceof Object   []) return Arrays.toString((Object    []) o);
				if (o instanceof boolean  []) return Arrays.toString((boolean   []) o);
				if (o instanceof byte     []) return Arrays.toString((byte      []) o);
				if (o instanceof long     []) return Arrays.toString((long      []) o);
				if (o instanceof short    []) return Arrays.toString((short     []) o);
				if (o instanceof char     []) return Arrays.toString((char      []) o);
				//@formatter:on
			}
			return o.toString();
		}
		return "null";
	}
}