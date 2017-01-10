package gdsc.core.utils;

/*----------------------------------------------------------------------------- 
 * GDSC ImageJ Software
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

import java.util.Arrays;

/**
 * Contains a list of fixed capacity. This is a simple wrapper around an array providing get/set index methods and
 * dynamic addition of data to the end of the array up to the capacity.
 */
public class FixedIntList
{
	/** The data */
	private final int[] data;

	/** The size */
	private int size = 0;

	/**
	 * Instantiates a new fixed int list.
	 *
	 * @param capacity
	 *            the capacity
	 */
	public FixedIntList(int capacity)
	{
		this.data = new int[capacity];
	}

	/**
	 * Instantiates a new fixed int list with values. All the input values are stored so the fixed capacity will be
	 * equal (or greater if specified) than the number of input values.
	 *
	 * @param capacity
	 *            the capacity
	 * @param values
	 *            the values
	 */
	public FixedIntList(int capacity, int... values)
	{
		if (values.length < capacity)
		{
			this.data = new int[capacity];
			add(values);
		}
		else
		{
			this.data = values;
			size = values.length;
		}
	}

	/**
	 * Get the capacity.
	 *
	 * @return the capacity
	 */
	public int capacity()
	{
		return data.length;
	}

	/**
	 * Get the size.
	 *
	 * @return the size
	 */
	public int size()
	{
		return size;
	}

	/**
	 * Adds the value. No bounds checks are made against capacity.
	 *
	 * @param value
	 *            the value
	 */
	public void add(int value)
	{
		data[size++] = value;
	}

	/**
	 * Adds the values. No bounds checks are made against capacity or for a valid input array.
	 *
	 * @param values
	 *            the values
	 */
	public void add(int[] values)
	{
		int length = values.length;
		System.arraycopy(values, 0, data, size, length);
		size += length;
	}

	/**
	 * Adds the values. No bounds checks are made against capacity or for a valid input array.
	 *
	 * @param values
	 *            the values
	 */
	public void addValues(int... values)
	{
		add(values);
	}

	/**
	 * Adds the values. No bounds checks are made against capacity or for a valid input array.
	 *
	 * @param values
	 *            the values
	 */
	public void add(FixedIntList values)
	{
		int length = values.size;
		System.arraycopy(values.data, 0, data, size, length);
		size += length;
	}
	
	/**
	 * Gets the value at the given index. No bounds checks are made against the size.
	 *
	 * @param index
	 *            the index
	 * @return the value
	 */
	public int get(int index)
	{
		return data[index];
	}

	/**
	 * Sets the value at the given index. No bounds checks are made against the size and the size is not increased.
	 *
	 * @param index
	 *            the index
	 * @param value
	 *            the value
	 */
	public void set(int index, int value)
	{
		data[index] = value;
	}

	/**
	 * Convert the current values to an array.
	 *
	 * @return the int[] array
	 */
	public int[] toArray()
	{
		return Arrays.copyOf(data, size);
	}

	/**
	 * Clear the list.
	 */
	public void clear()
	{
		size = 0;
	}
}