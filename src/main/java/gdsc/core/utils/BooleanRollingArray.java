package gdsc.core.utils;

import java.util.Arrays;

/*----------------------------------------------------------------------------- 
 * GDSC Software
 * 
 * Copyright (C) 2015 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

/**
 * Provide a rolling array of booleans
 */
public class BooleanRollingArray
{
	private final boolean[] data;
	private final int capacity;
	private int index, count, sum;

	/**
	 * Create a rolling array
	 * 
	 * @param capacity
	 */
	public BooleanRollingArray(int capacity)
	{
		this.capacity = capacity;
		this.data = new boolean[capacity];
	}

	/**
	 * Remove all the numbers from the array
	 */
	public void clear()
	{
		sum = 0;
		index = 0;
		count = 0;
	}

	/**
	 * Add a number to the array
	 * 
	 * @param d
	 *            The number
	 */
	public void add(boolean d)
	{
		// If at capacity
		if (isFull())
		{
			// Subtract the item to be replaced
			if (data[index])
				sum--;
		}
		else
		{
			// Otherwise increase the count
			count++;
		}
		// Add to the true count
		if (d)
			sum++;
		// Replace the item
		data[index++] = d;
		// Wrap the index
		if (index == capacity)
			index = 0;
	}

	/**
	 * Add a number to the array n times.
	 *
	 * @param d
	 *            The number
	 * @param n
	 *            the number of times
	 */
	public void add(boolean d, int n)
	{
		if (n >= capacity)
		{
			// Saturate
			Arrays.fill(data, d);
			sum = (d) ? n : 0;
			index = 0;
			count = capacity;
		}
		else
		{
			while (n-- > 0)
				add(d);
		}
	}

	/**
	 * @return The count of items stored in the array
	 */
	public int getCount()
	{
		return count;
	}

	/**
	 * @return The capacity of the array
	 */
	public int getCapacity()
	{
		return capacity;
	}

	/**
	 * @return The number of true items stored in the array
	 */
	public int getTrueCount()
	{
		return sum;
	}

	/**
	 * @return The number of false items stored in the array
	 */
	public int getFalseCount()
	{
		return count - sum;
	}

	/**
	 * @return True if full
	 */
	public boolean isFull()
	{
		return count == capacity;
	}

	/**
	 * Convert to an array.
	 *
	 * @return the array
	 */
	public boolean[] toArray()
	{
		return Arrays.copyOf(data, count);
	}
}
