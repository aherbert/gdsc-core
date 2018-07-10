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
package gdsc.core.utils;

import java.util.Arrays;

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
	 * Create a rolling array.
	 *
	 * @param capacity
	 *            the capacity
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
	 * 
	 * @param d
	 *            The number
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
