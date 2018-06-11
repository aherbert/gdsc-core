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
 * Expandable store for boolean data backed by an array
 */
public class BooleanArray implements Iterable<Boolean>
{
	// TODO - Copy the functionality from Trove TArrayList (which does not have TBoolArrayList)
	
	private boolean[] values;
	private int n = 0;

	public BooleanArray()
	{
		values = new boolean[10];
	}

	public BooleanArray(int capacity)
	{
		values = new boolean[capacity];
	}

	public BooleanArray(boolean[] data, boolean clone)
	{
		if (data != null)
		{
			values = (clone) ? data.clone() : data;
			n = data.length;
		}
	}

	/**
	 * Add the data
	 * 
	 * @param data
	 */
	public void add(boolean[] data)
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
			boolean[] newValues = new boolean[newCapacity];
			System.arraycopy(values, 0, newValues, 0, n);
			values = newValues;
		}
	}

	/**
	 * Add the value
	 * 
	 * @param value
	 */
	public void add(final boolean value)
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
	public void add(int n, boolean value)
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
	synchronized public void safeAdd(boolean[] data)
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
	synchronized public void safeAdd(final boolean value)
	{
		checkCapacity(1);
		values[n++] = value;
	}

	/**
	 * @return A copy of the values added
	 */
	public boolean[] toArray()
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
	public boolean get(int i)
	{
		if (i >= n)
			throw new ArrayIndexOutOfBoundsException(i);
		return values[i];
	}

	/**
	 * Gets the value without bounds checking.
	 *
	 * @param i
	 *            the index
	 * @return the value
	 */
	public boolean getf(int i)
	{
		return values[i];
	}

	/**
	 * Adds the data to this store.
	 *
	 * @param data
	 *            the data
	 */
	public void add(BooleanArray data)
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
	synchronized public void safeAdd(BooleanArray data)
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
	public Iterator<Boolean> iterator()
	{
		return new Itr();
	}

	/**
	 * Copied from ArrayList and removed unrequired code
	 */
	private class Itr implements Iterator<Boolean>
	{
		int cursor; // index of next element to return

		public boolean hasNext()
		{
			return cursor != n;
		}

		public Boolean next()
		{
			// Simple implementation. Will throw index-out-of-bounds eventually
			return BooleanArray.this.values[cursor++];

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

	/**
	 * Get the size.
	 *
	 * @return the size
	 */
	public int size()
	{
		return n;
	}

	/**
	 * Clear the array.
	 */
	public void clear()
	{
		n = 0;
	}

	/**
	 * Compact the array to the current size.
	 */
	public void compact()
	{
		values = toArray();
	}
}
