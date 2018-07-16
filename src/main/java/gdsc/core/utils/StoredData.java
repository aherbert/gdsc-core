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
import java.util.Iterator;

/**
 * Expandable store for data backed by a double array.
 */
public class StoredData implements Iterable<Double>, DoubleData
{
	private double[] values = new double[0];
	private int n = 0;

	/**
	 * Instantiates a new stored data.
	 */
	public StoredData()
	{
	}

	/**
	 * Instantiates a new stored data.
	 *
	 * @param capacity
	 *            the capacity
	 */
	public StoredData(int capacity)
	{
		values = new double[capacity];
	}

	/**
	 * Instantiates a new stored data.
	 *
	 * @param data
	 *            the data
	 */
	public StoredData(float[] data)
	{
		add(data);
	}

	/**
	 * Instantiates a new stored data.
	 *
	 * @param data
	 *            the data
	 */
	public StoredData(double[] data)
	{
		this(data, true);
	}

	/**
	 * Instantiates a new stored data.
	 *
	 * @param data
	 *            the data
	 * @param clone
	 *            the clone
	 */
	public StoredData(double[] data, boolean clone)
	{
		if (data != null)
		{
			values = (clone) ? data.clone() : data;
			n = data.length;
		}
	}

	/**
	 * Instantiates a new stored data.
	 *
	 * @param data
	 *            the data
	 */
	public StoredData(int[] data)
	{
		add(data);
	}

	/**
	 * Add the data.
	 *
	 * @param data
	 *            the data
	 */
	public void add(float[] data)
	{
		if (data == null)
			return;
		checkCapacity(data.length);
		for (int i = 0; i < data.length; i++)
			values[n++] = data[i];
	}

	/**
	 * Ensure that the specified number of elements can be added to the array.
	 * <p>
	 * This is not synchronized. However any class using the safeAdd() methods in different threads should be using the
	 * same synchronized method to add data thus this method will be within synchronized code.
	 *
	 * @param length
	 *            the length
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
			final double[] newValues = new double[newCapacity];
			System.arraycopy(values, 0, newValues, 0, n);
			values = newValues;
		}
	}

	/**
	 * Add the data.
	 *
	 * @param data
	 *            the data
	 */
	public void add(double[] data)
	{
		if (data == null)
			return;
		checkCapacity(data.length);
		System.arraycopy(data, 0, values, n, data.length);
		n += data.length;
		//for (int i = 0; i < data.length; i++)
		//{
		//	values[n++] = data[i];
		//}
	}

	/**
	 * Add the data.
	 *
	 * @param data
	 *            the data
	 */
	public void add(int[] data)
	{
		if (data == null)
			return;
		checkCapacity(data.length);
		for (int i = 0; i < data.length; i++)
			values[n++] = data[i];
	}

	/**
	 * Add the value.
	 *
	 * @param value
	 *            the value
	 */
	public void add(final double value)
	{
		checkCapacity(1);
		values[n++] = value;
	}

	/**
	 * Add the value n times.
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
	 *            the data
	 */
	synchronized public void safeAdd(float[] data)
	{
		if (data == null)
			return;
		checkCapacity(data.length);
		for (int i = 0; i < data.length; i++)
			values[n++] = data[i];
	}

	/**
	 * Add the data. Synchronized for thread safety. (Multiple threads must all use the same safeAdd method to ensure
	 * thread safety.)
	 *
	 * @param data
	 *            the data
	 */
	synchronized public void safeAdd(double[] data)
	{
		if (data == null)
			return;
		checkCapacity(data.length);
		for (int i = 0; i < data.length; i++)
			values[n++] = data[i];
	}

	/**
	 * Add the value. Synchronized for thread safety. (Multiple threads must all use the same safeAdd method to ensure
	 * thread safety.)
	 *
	 * @param value
	 *            the value
	 */
	synchronized public void safeAdd(final double value)
	{
		checkCapacity(1);
		values[n++] = value;
	}

	/**
	 * Gets the values.
	 *
	 * @return A copy of the values added
	 */
	public double[] getValues()
	{
		return Arrays.copyOf(values, n);
	}

	/**
	 * Gets the current values array. This may be larger than the current size.
	 *
	 * @return The values array
	 */
	public double[] getValuesRef()
	{
		return values;
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
	 * Gets the float values.
	 *
	 * @return A copy of the values added
	 */
	public float[] getFloatValues()
	{
		final float[] data = new float[n];
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
			this.n += data.n;
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
	@Override
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

		@Override
		public boolean hasNext()
		{
			return cursor != n;
		}

		@Override
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

		@Override
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
	@Override
	public int size()
	{
		return n;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.utils.DoubleData#values()
	 */
	@Override
	public double[] values()
	{
		return getValues();
	}

	/**
	 * Clear the store (but keep the capacity).
	 */
	public void clear()
	{
		n = 0;
	}

	/**
	 * Get the capacity of the store.
	 *
	 * @return the capacity
	 */
	public int capacity()
	{
		return values.length;
	}
}
