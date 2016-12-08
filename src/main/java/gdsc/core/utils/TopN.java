package gdsc.core.utils;

import java.util.Arrays;

/*----------------------------------------------------------------------------- 
 * GDSC ImageJ Software
 * 
 * Copyright (C) 2016 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

/**
 * Provides functionality to pick the top N items in an array
 */
public class TopN
{
	/**
	 * The number N to select
	 */
	final int n;
	/**
	 * Working storage
	 */
	private final double[] queue;

	/**
	 * Create a new TopN selector
	 * 
	 * @param n
	 *            The number N to select
	 */
	public TopN(int n)
	{
		if (n < 1)
			throw new IllegalArgumentException("N must be strictly positive");
		this.n = n;
		queue = new double[n];
	}

	/**
	 * Pick the top N from the data using ascending order, i.e. find the top n smallest values.
	 * <p>
	 * If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException} will occur.
	 * 
	 * @param list
	 *            the data list
	 * @return The top N (passed as a reference to internal data structure)
	 */
	public double[] pick(double[] list)
	{
		return pick(list, list.length);
	}

	/**
	 * Pick the top N from the data using ascending order, i.e. find the top n smallest values.
	 * <p>
	 * If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException} will occur.
	 * 
	 * @param list
	 *            the data list
	 * @param size
	 *            The size of the list (must be equal or above N)
	 * @return The top N (passed as a reference to internal data structure)
	 */
	public double[] pick(double[] list, int size)
	{
		// We retain a pointer to the current highest value in the set. 
		int max = 0;
		queue[0] = list[0];

		// Fill 
		int i = 1;
		while (i < n)
		{
			queue[i] = list[i];
			if (queue[max] < queue[i])
				max = i;
			i++;
		}

		// Scan
		while (i < size)
		{
			// Replace if lower
			if (queue[max] > list[i])
			{
				queue[max] = list[i];
				// Find new max
				for (int j = n; j-- > 0;)
				{
					if (queue[max] < queue[j])
						max = j;
				}
			}
			i++;
		}

		return queue;
	}

	/**
	 * Pick the top N from the data using ascending order, i.e. find the top n smallest values.
	 * <p>
	 * If the input data size is smaller than N then only size will be returned.
	 * 
	 * @param list
	 *            the data list
	 * @param sortResult
	 *            Set to true to sort the top N
	 * @return The top N (passed as a new array)
	 */
	public double[] safePick(double[] list, boolean sortResult)
	{
		if (list == null)
			return new double[0];
		return safePick(list, list.length, sortResult);
	}

	/**
	 * Pick the top N from the data using ascending order, i.e. find the top n smallest values.
	 * <p>
	 * If the input data size is smaller than N then only size will be returned.
	 * 
	 * @param list
	 *            the data list
	 * @param size
	 *            The size of the list
	 * @param sortResult
	 *            Set to true to sort the top N
	 * @return The top N (passed as a new array)
	 */
	public double[] safePick(double[] list, int size, boolean sortResult)
	{
		if (list == null || size <= 0)
			return new double[0];
		size = Math.min(size, list.length);
		if (size < n)
		{
			list = list.clone();
		}
		else
		{
			list = pick(list, size).clone();
		}
		if (sortResult)
			Arrays.sort(list);
		return list;
	}
}
