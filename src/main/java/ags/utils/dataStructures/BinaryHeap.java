/*
 * Copyright 2009 Rednaxela
 *
 * Modifications to the code have been made by Alex Herbert for a smaller 
 * memory footprint and optimised 2D processing for use with image data
 * as part of the Genome Damage and Stability Centre ImageJ Core Package.
 * 
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *    1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 *
 *    2. This notice may not be removed or altered from any source
 *    distribution.
 */
package ags.utils.dataStructures;

import java.util.Arrays;

/**
 * An implementation of an implicit binary heap. Min-heap and max-heap both supported
 *
 * @param <T>
 *            the generic type
 */
public abstract class BinaryHeap<T>
{
	/** The Constant defaultCapacity. */
	protected static final int defaultCapacity = 64;

	/** The direction. */
	private final int direction;

	/** The data. */
	private Object[] data;

	/** The keys. */
	private double[] keys;

	/** The capacity. */
	private int capacity;

	/** The size. */
	private int size;

	/**
	 * Instantiates a new binary heap.
	 *
	 * @param capacity
	 *            the capacity
	 * @param direction
	 *            the direction
	 */
	protected BinaryHeap(int capacity, int direction)
	{
		this.direction = direction;
		this.data = new Object[capacity];
		this.keys = new double[capacity];
		this.capacity = capacity;
		this.size = 0;
	}

	/**
	 * Offer.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
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

	/**
	 * Removes the tip.
	 */
	protected void removeTip()
	{
		if (size == 0)
			throw new IllegalStateException();

		size--;
		data[0] = data[size];
		keys[0] = keys[size];
		data[size] = null;
		siftDown(0);
	}

	/**
	 * Replace tip.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	protected void replaceTip(double key, T value)
	{
		if (size == 0)
			throw new IllegalStateException();

		data[0] = value;
		keys[0] = key;
		siftDown(0);
	}

	/**
	 * Gets the tip.
	 *
	 * @return the tip
	 */
	@SuppressWarnings("unchecked")
	protected T getTip()
	{
		if (size == 0)
			throw new IllegalStateException();

		return (T) data[0];
	}

	/**
	 * Gets the tip key.
	 *
	 * @return the tip key
	 */
	protected double getTipKey()
	{
		if (size == 0)
			throw new IllegalStateException();

		return keys[0];
	}

	/**
	 * Sift up.
	 *
	 * @param c
	 *            the c
	 */
	private void siftUp(int c)
	{
		for (int p = (c - 1) / 2; c != 0 && direction * keys[c] > direction * keys[p]; c = p, p = (c - 1) / 2)
		{
			final Object pData = data[p];
			final double pDist = keys[p];
			data[p] = data[c];
			keys[p] = keys[c];
			data[c] = pData;
			keys[c] = pDist;
		}
	}

	/**
	 * Sift down.
	 *
	 * @param p
	 *            the p
	 */
	private void siftDown(int p)
	{
		for (int c = p * 2 + 1; c < size; p = c, c = p * 2 + 1)
		{
			if (c + 1 < size && direction * keys[c] < direction * keys[c + 1])
				c++;
			if (direction * keys[p] < direction * keys[c])
			{
				// Swap the points
				final Object pData = data[p];
				final double pDist = keys[p];
				data[p] = data[c];
				keys[p] = keys[c];
				data[c] = pData;
				keys[c] = pDist;
			}
			else
				break;
		}
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
	 * Get the capacity.
	 *
	 * @return the capacity
	 */
	public int capacity()
	{
		return capacity;
	}

	/**
	 * An implementation of an implicit binary max heap.
	 *
	 * @param <T>
	 *            the generic type
	 */
	public static final class Max<T> extends BinaryHeap<T> implements MaxHeap<T>
	{
		/**
		 * Instantiates a new max.
		 */
		public Max()
		{
			super(defaultCapacity, 1);
		}

		/**
		 * Instantiates a new max.
		 *
		 * @param capacity
		 *            the capacity
		 */
		public Max(int capacity)
		{
			super(capacity, 1);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see ags.utils.dataStructures.MaxHeap#removeMax()
		 */
		@Override
		public void removeMax()
		{
			removeTip();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see ags.utils.dataStructures.MaxHeap#replaceMax(double, java.lang.Object)
		 */
		@Override
		public void replaceMax(double key, T value)
		{
			replaceTip(key, value);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see ags.utils.dataStructures.MaxHeap#getMax()
		 */
		@Override
		public T getMax()
		{
			return getTip();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see ags.utils.dataStructures.MaxHeap#getMaxKey()
		 */
		@Override
		public double getMaxKey()
		{
			return getTipKey();
		}
	}

	/**
	 * An implementation of an implicit binary min heap.
	 *
	 * @param <T>
	 *            the generic type
	 */
	public static final class Min<T> extends BinaryHeap<T> implements MinHeap<T>
	{
		/**
		 * Instantiates a new min.
		 */
		public Min()
		{
			super(defaultCapacity, -1);
		}

		/**
		 * Instantiates a new min.
		 *
		 * @param capacity
		 *            the capacity
		 */
		public Min(int capacity)
		{
			super(capacity, -1);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see ags.utils.dataStructures.MinHeap#removeMin()
		 */
		@Override
		public void removeMin()
		{
			removeTip();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see ags.utils.dataStructures.MinHeap#replaceMin(double, java.lang.Object)
		 */
		@Override
		public void replaceMin(double key, T value)
		{
			replaceTip(key, value);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see ags.utils.dataStructures.MinHeap#getMin()
		 */
		@Override
		public T getMin()
		{
			return getTip();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see ags.utils.dataStructures.MinHeap#getMinKey()
		 */
		@Override
		public double getMinKey()
		{
			return getTipKey();
		}
	}
}
