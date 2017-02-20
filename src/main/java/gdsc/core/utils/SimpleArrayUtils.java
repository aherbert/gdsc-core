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
}