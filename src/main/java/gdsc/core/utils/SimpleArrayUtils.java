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
		for (int i = 0; i < length; i++, start += increment)
			data[i] = start;
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
}