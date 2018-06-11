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
package ags.utils.dataStructures;

import java.util.Arrays;

/**
 * An implementation of an implicit binary heap. Min-heap and max-heap both supported
 */
public abstract class BinaryHeap<T>
{
	protected static final int defaultCapacity = 64;
	private final int direction;
	private Object[] data;
	private double[] keys;
	private int capacity;
	private int size;

	protected BinaryHeap(int capacity, int direction)
	{
		this.direction = direction;
		this.data = new Object[capacity];
		this.keys = new double[capacity];
		this.capacity = capacity;
		this.size = 0;
	}

	public void offer(double key, T value)
	{
		// If move room is needed, double array size
		if (size >= capacity)
		{
			capacity *= 2;
			data = Arrays.copyOf(data, capacity);
			keys = Arrays.copyOf(keys, capacity);
		}

		// Insert new value at the end
		data[size] = value;
		keys[size] = key;
		siftUp(size);
		size++;
	}

	protected void removeTip()
	{
		if (size == 0)
		{
			throw new IllegalStateException();
		}

		size--;
		data[0] = data[size];
		keys[0] = keys[size];
		data[size] = null;
		siftDown(0);
	}

	protected void replaceTip(double key, T value)
	{
		if (size == 0)
		{
			throw new IllegalStateException();
		}

		data[0] = value;
		keys[0] = key;
		siftDown(0);
	}

	@SuppressWarnings("unchecked")
	protected T getTip()
	{
		if (size == 0)
		{
			throw new IllegalStateException();
		}

		return (T) data[0];
	}

	protected double getTipKey()
	{
		if (size == 0)
		{
			throw new IllegalStateException();
		}

		return keys[0];
	}

	private void siftUp(int c)
	{
		for (int p = (c - 1) / 2; c != 0 && direction * keys[c] > direction * keys[p]; c = p, p = (c - 1) / 2)
		{
			Object pData = data[p];
			double pDist = keys[p];
			data[p] = data[c];
			keys[p] = keys[c];
			data[c] = pData;
			keys[c] = pDist;
		}
	}

	private void siftDown(int p)
	{
		for (int c = p * 2 + 1; c < size; p = c, c = p * 2 + 1)
		{
			if (c + 1 < size && direction * keys[c] < direction * keys[c + 1])
			{
				c++;
			}
			if (direction * keys[p] < direction * keys[c])
			{
				// Swap the points
				Object pData = data[p];
				double pDist = keys[p];
				data[p] = data[c];
				keys[p] = keys[c];
				data[c] = pData;
				keys[c] = pDist;
			}
			else
			{
				break;
			}
		}
	}

	public int size()
	{
		return size;
	}

	public int capacity()
	{
		return capacity;
	}

	public static final class Max<T> extends BinaryHeap<T> implements MaxHeap<T>
	{
		public Max()
		{
			super(defaultCapacity, 1);
		}

		public Max(int capacity)
		{
			super(capacity, 1);
		}

		@Override
		public void removeMax()
		{
			removeTip();
		}

		@Override
		public void replaceMax(double key, T value)
		{
			replaceTip(key, value);
		}

		@Override
		public T getMax()
		{
			return getTip();
		}

		@Override
		public double getMaxKey()
		{
			return getTipKey();
		}
	}

	public static final class Min<T> extends BinaryHeap<T> implements MinHeap<T>
	{
		public Min()
		{
			super(defaultCapacity, -1);
		}

		public Min(int capacity)
		{
			super(capacity, -1);
		}

		@Override
		public void removeMin()
		{
			removeTip();
		}

		@Override
		public void replaceMin(double key, T value)
		{
			replaceTip(key, value);
		}

		@Override
		public T getMin()
		{
			return getTip();
		}

		@Override
		public double getMinKey()
		{
			return getTipKey();
		}
	}
}
