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

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Calculate the mean and standard deviation of data. Stores the data for later retrieval.
 */
public class StoredDataStatistics extends Statistics implements Iterable<Double>, DoubleData
{
	private double[] values = new double[0];
	private DescriptiveStatistics stats = null;

	/**
	 * Instantiates a new stored data statistics.
	 */
	public StoredDataStatistics()
	{
	}

	/**
	 * Instantiates a new stored data statistics.
	 *
	 * @param capacity
	 *            the capacity
	 */
	public StoredDataStatistics(int capacity)
	{
		values = new double[capacity];
	}

	/**
	 * Instantiates a new stored data statistics.
	 *
	 * @param data
	 *            the data
	 */
	public StoredDataStatistics(float[] data)
	{
		super(data);
	}

	/**
	 * Instantiates a new stored data statistics.
	 *
	 * @param data
	 *            the data
	 */
	public StoredDataStatistics(double[] data)
	{
		super(data);
	}

	/**
	 * Instantiates a new stored data statistics.
	 *
	 * @param data
	 *            the data
	 */
	public StoredDataStatistics(int[] data)
	{
		super(data);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.utils.Statistics#addInternal(float[], int, int)
	 */
	@Override
	protected void addInternal(float[] data, int from, int to)
	{
		if (data == null)
			return;
		checkCapacity(to - from);
		for (int i = from; i < to; i++)
		{
			final double value = data[i];
			values[n++] = value;
			s += value;
			ss += value * value;
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
		stats = null;
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

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.utils.Statistics#addInternal(double[], int, int)
	 */
	@Override
	protected void addInternal(double[] data, int from, int to)
	{
		if (data == null)
			return;
		checkCapacity(to - from);
		for (int i = from; i < to; i++)
		{
			final double value = data[i];
			values[n++] = value;
			s += value;
			ss += value * value;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.utils.Statistics#addInternal(int[], int, int)
	 */
	@Override
	protected void addInternal(int[] data, int from, int to)
	{
		if (data == null)
			return;
		checkCapacity(to - from);
		for (int i = from; i < to; i++)
		{
			final double value = data[i];
			values[n++] = value;
			s += value;
			ss += value * value;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.utils.Statistics#add(double)
	 */
	@Override
	public void add(final double value)
	{
		checkCapacity(1);
		values[n++] = value;
		s += value;
		ss += value * value;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.utils.Statistics#add(int, double)
	 */
	@Override
	public void add(int n, double value)
	{
		checkCapacity(n);
		for (int i = 0; i < n; i++)
			values[this.n++] = value;
		s += n * value;
		ss += n * value * value;
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
	 * Gets the statistics.
	 *
	 * @return object used to compute descriptive statistics. The object is cached
	 * @see org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
	 */
	public DescriptiveStatistics getStatistics()
	{
		if (stats == null)
			stats = new DescriptiveStatistics(values);
		return stats;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.utils.Statistics#add(gdsc.core.utils.Statistics)
	 */
	@Override
	public void add(Statistics statistics)
	{
		if (statistics instanceof StoredDataStatistics)
		{
			final StoredDataStatistics extra = (StoredDataStatistics) statistics;
			if (extra.n > 0)
			{
				checkCapacity(extra.n);
				System.arraycopy(extra.values, 0, values, n, extra.n);
			}
		}
		super.add(statistics);
	}

	/**
	 * Gets the median.
	 *
	 * @return The median
	 */
	public double getMedian()
	{
		// Check for negatives
		for (final double d : values)
			if (d < 0)
			{
				if (n == 0)
					return Double.NaN;
				if (n == 1)
					return values[0];

				final double[] data = getValues();
				Arrays.sort(data);
				return (data[(data.length - 1) / 2] + data[data.length / 2]) * 0.5;
			}

		// This does not work when the array contains negative data due to the
		// implementation of the library using partially sorted data
		return getStatistics().getPercentile(50);
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
			return StoredDataStatistics.this.values[cursor++];

			// Copied from ArrayList and removed unrequired code
			//int i = cursor;
			//if (i >= n)
			//	throw new NoSuchElementException();
			//final double[] elementData = StoredDataStatistics.this.values;
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
	 * {@inheritDoc}
	 * <p>
	 * Note: This does not reset the allocated storage.
	 *
	 * @see gdsc.core.utils.Statistics#reset()
	 */
	@Override
	public void reset()
	{
		super.reset();
		stats = null;
	}
}
