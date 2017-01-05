package gdsc.core.utils;

import java.util.Arrays;
import java.util.Iterator;

/*----------------------------------------------------------------------------- 
 * GDSC SMLM Software
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
 * Expandable store for data backed by a double array
 */
public class StoredData implements Iterable<Double>, DoubleData
{
	private double[] values = new double[0];
	private int n = 0;

	public StoredData()
	{
	}

	public StoredData(int capacity)
	{
		values = new double[capacity];
	}

	public StoredData(float[] data)
	{
		add(data);
	}

	public StoredData(double[] data)
	{
		add(data);
	}

	public StoredData(int[] data)
	{
		add(data);
	}

	/**
	 * Add the data
	 * 
	 * @param data
	 */
	public void add(float[] data)
	{
		if (data == null)
			return;
		checkCapacity(data.length);
		for (int i = 0; i < data.length; i++)
		{
			values[n++] = data[i];
		}
	}

	/**
	 * Ensure that the specified number of elements can be added to the array.
	 * <p>
	 * This is not synchronized. However any class using the safeAdd() methods in different threads should be using the
	 * same synchronized method to add data thus this method will be within synchronized code.
	 * 
	 * @param length
	 */
	private void checkCapacity(int length)
	{
		final int minCapacity = n + length;
		final int oldCapacity = values.length;
		if (minCapacity > oldCapacity)
		{
			int newCapacity = (oldCapacity * 3) / 2 + 1;
			if (newCapacity < minCapacity)
				newCapacity = minCapacity;
			double[] newValues = new double[newCapacity];
			System.arraycopy(values, 0, newValues, 0, n);
			values = newValues;
		}
	}

	/**
	 * Add the data
	 * 
	 * @param data
	 */
	public void add(double[] data)
	{
		if (data == null)
			return;
		checkCapacity(data.length);
		for (int i = 0; i < data.length; i++)
		{
			values[n++] = data[i];
		}
	}

	/**
	 * Add the data
	 * 
	 * @param data
	 */
	public void add(int[] data)
	{
		if (data == null)
			return;
		checkCapacity(data.length);
		for (int i = 0; i < data.length; i++)
		{
			values[n++] = data[i];
		}
	}

	/**
	 * Add the value
	 * 
	 * @param value
	 */
	public void add(final double value)
	{
		checkCapacity(1);
		values[n++] = value;
	}

	/**
	 * Add the value n times
	 * 
	 * @param n
	 *            The number of times
	 * @param value
	 *            The value
	 */
	public void add(int n, double value)
	{
		checkCapacity(n);
		for (int i = 0; i < n; i++)
			values[this.n++] = value;
	}

	/**
	 * Add the data. Synchronized for thread safety. (Multiple threads must all use the same safeAdd method to ensure
	 * thread safety.)
	 * 
	 * @param data
	 */
	synchronized public void safeAdd(float[] data)
	{
		if (data == null)
			return;
		checkCapacity(data.length);
		for (int i = 0; i < data.length; i++)
		{
			values[n++] = data[i];
		}
	}

	/**
	 * Add the data. Synchronized for thread safety. (Multiple threads must all use the same safeAdd method to ensure
	 * thread safety.)
	 * 
	 * @param data
	 */
	synchronized public void safeAdd(double[] data)
	{
		if (data == null)
			return;
		checkCapacity(data.length);
		for (int i = 0; i < data.length; i++)
		{
			values[n++] = data[i];
		}
	}

	/**
	 * Add the value. Synchronized for thread safety. (Multiple threads must all use the same safeAdd method to ensure
	 * thread safety.)
	 * 
	 * @param value
	 */
	synchronized public void safeAdd(final double value)
	{
		checkCapacity(1);
		values[n++] = value;
	}

	/**
	 * @return A copy of the values added
	 */
	public double[] getValues()
	{
		return Arrays.copyOf(values, n);
	}

	/**
	 * Gets the value.
	 *
	 * @param i
	 *            the index
	 * @return the value
	 */
	public double getValue(int i)
	{
		return values[i];
	}

	/**
	 * @return A copy of the values added
	 */
	public float[] getFloatValues()
	{
		float[] data = new float[n];
		for (int i = 0; i < n; i++)
			data[i] = (float) values[i];
		return data;
	}

	/**
	 * Adds the data to this store.
	 *
	 * @param data
	 *            the data
	 */
	public void add(StoredData data)
	{
		if (data.n > 0)
		{
			checkCapacity(data.n);
			System.arraycopy(data.values, 0, values, n, data.n);
		}
	}

	/**
	 * Adds the data to this store. Synchronized for thread safety. (Multiple threads must all use the same safeAdd
	 * method to ensure thread safety.)
	 *
	 * @param data
	 *            the data
	 */
	synchronized public void safeAdd(StoredData data)
	{
		this.add(data);
	}

	/**
	 * Returns a list iterator over the elements in this list (in proper
	 * sequence).
	 *
	 * @return a list iterator over the elements in this list (in proper
	 *         sequence)
	 */
	public Iterator<Double> iterator()
	{
		return new Itr();
	}

	/**
	 * Copied from ArrayList and removed unrequired code
	 */
	private class Itr implements Iterator<Double>
	{
		int cursor; // index of next element to return

		public boolean hasNext()
		{
			return cursor != n;
		}

		public Double next()
		{
			// Simple implementation. Will throw index-out-of-bounds eventually
			return StoredData.this.values[cursor++];

			// Copied from ArrayList and removed unrequired code
			//int i = cursor;
			//if (i >= n)
			//	throw new NoSuchElementException();
			//final double[] elementData = StoredData.this.values;
			//if (i >= elementData.length)
			//	throw new ConcurrentModificationException();
			//cursor = i + 1;
			//return elementData[i];
		}

		public void remove()
		{
			throw new UnsupportedOperationException("remove");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.utils.DoubleData#size()
	 */
	public int size()
	{
		return n;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.utils.DoubleData#values()
	 */
	public double[] values()
	{
		return getValues();
	}
}