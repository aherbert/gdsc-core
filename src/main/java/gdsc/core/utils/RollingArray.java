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
 * Provide a rolling array of doubles
 */
public class RollingArray
{
	private final double[] data;
	private final int capacity;
	private int index, count;
	private double sum;

	/**
	 * Create a rolling array
	 * 
	 * @param capacity
	 */
	public RollingArray(int capacity)
	{
		this.capacity = capacity;
		this.data = new double[capacity];
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
	 * @param d The number
	 */
	public void add(double d)
	{
		// If at capacity
		if (isFull())
		{
			// Subtract the item to be replaced
			sum -= data[index];
		}
		else
		{
			// Otherwise increase the count
			count++;
		}
		// Add to the total
		sum += d;
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
	public void add(int d, int n)
	{
		if (n >= capacity)
		{
			// Saturate
			Arrays.fill(data, d);
			sum = n * d;
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
	 * @return The count of numbers stored in the array
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
	 * @return The sum using the rolling sum of the numbers (may accumulate errors)
	 */
	public double getSum()
	{
		return sum;
	}

	/**
	 * @return The recomputed sum using the current set of numbers
	 */
	public double getSum2()
	{
		double s = 0;
		// If full 'count' will be the length of the data array
		for (int i = 0; i < count; i++)
			s += data[i];

		// Debug
		//System.out.printf("Error = %g\n", DoubleEquality.relativeError(s, sum));

		// Reset the sum
		return sum = s;
	}

	/**
	 * @return The average using the rolling sum of the numbers
	 */
	public double getAverage()
	{
		return sum / count;
	}

	/**
	 * @return The average using a recomputed sum of the current numbers
	 */
	public double getAverage2()
	{
		return getSum2() / count;
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
	public double[] toArray()
	{
		return Arrays.copyOf(data, count);
	}
}
