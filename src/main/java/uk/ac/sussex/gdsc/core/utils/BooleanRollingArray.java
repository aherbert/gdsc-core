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

/**
 * Provide a rolling array of booleans
 */
public class BooleanRollingArray
{
	private final boolean[] data;
	private final int capacity;
	private int index, count, sum;

	/**
	 * Create a rolling array.
	 *
	 * @param capacity
	 *            the capacity
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
			// Otherwise increase the count
			count++;
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
			while (n-- > 0)
				add(d);
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
