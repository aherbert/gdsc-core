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
 * Provides functionality to partially sort an array
 *
 * @author Alex Herbert
 */
public class PartialSort
{
	/**
	 * Sort the final returned data. This takes precedence over {@link #OPTION_HEAD_FIRST}.
	 * <p>
	 * Note that if the number of points to return (n) is above half the total size of the input list (m, e.g. n/m >
	 * 0.5) then it is probably faster to sort the input list and take the top n.
	 */
	public final static int OPTION_SORT = 1;

	/**
	 * Remove NaN values from the returned data
	 */
	public final static int OPTION_REMOVE_NAN = 2;

	/**
	 * Return the head position at the first point in the returned data (i.e. if choosing the bottom N then array[0]
	 * will
	 * contain the Nth point. This is not compatible with {@link #OPTION_SORT}.
	 */
	public final static int OPTION_HEAD_FIRST = 4;

	/**
	 * Return a sorted array with no invalid data
	 */
	public final static int OPTION_CLEAN = OPTION_SORT | OPTION_REMOVE_NAN;

	/**
	 * Internal top direction flag
	 */
	private final static int OPTION_TOP = 8;

	/**
	 * Provide partial sort of double arrays
	 * <p>
	 * This class is based on ags.utils.dataStructures.trees.secondGenKD.ResultHeap.
	 * Ideas have been taken from:
	 * http://sites.fas.harvard.edu/~libs111/code/heaps/Heap.java
	 */
	public static class DoubleHeap
	{
		/**
		 * The number N to select
		 */
		final int n;
		/**
		 * Working storage
		 */
		private final double[] queue;

		/**
		 * Instantiates a new max double heap.
		 *
		 * @param n
		 *            the number to select
		 */
		public DoubleHeap(int n)
		{
			if (n < 1)
				throw new IllegalArgumentException("N must be strictly positive");
			this.queue = new double[n];
			this.n = n;
		}

		/**
		 * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
		 * <p>
		 * If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException} will occur.
		 *
		 * @param list
		 *            the data list
		 * @return The bottom N (passed as a reference to internal data structure)
		 */
		public double[] bottom(double[] list)
		{
			return bottom(OPTION_CLEAN, list, list.length);
		}

		/**
		 * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
		 * <p>
		 * If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException} will occur.
		 *
		 * @param list
		 *            the data list
		 * @param options
		 *            the options
		 * @return The bottom N (passed as a reference to internal data structure)
		 */
		public double[] bottom(int options, double[] list)
		{
			return bottom(options, list, list.length);
		}

		/**
		 * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
		 * <p>
		 * If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException} will occur.
		 *
		 * @param list
		 *            the data list
		 * @param size
		 *            The size of the list (must be equal or above N)
		 * @param options
		 *            the options
		 * @return The bottom N (passed as a reference to internal data structure)
		 */
		public double[] bottom(int options, double[] list, int size)
		{
			queue[0] = list[0];

			// Fill
			int i = 1;
			while (i < n)
			{
				// Insert new value at the end
				queue[i] = list[i];
				bottomUpHeapify(i);
				i++;
			}

			// Scan
			while (i < size)
			{
				// Replace if lower
				if (queue[0] > list[i])
				{
					queue[0] = list[i];
					bottomDownHeapify(0);
				}
				i++;
			}

			if (options == 0)
				return queue;

			return quickFinish(queue, options);
		}

		private void bottomUpHeapify(int c)
		{
			//for (int p = (c - 1) / 2; c != 0 && queue[c] > queue[p]; c = p, p = (c - 1) / 2)
			//{
			//	double pDist = queue[p];
			//	queue[p] = queue[c];
			//	queue[c] = pDist;
			//}

			// Reference to the sifted element
			final double toSift = queue[c];
			while (c > 0)
			{
				final int p = (c - 1) >>> 1;
				if (toSift > queue[p])
				{
					// Move parent down and move up one level in the tree.
					queue[c] = queue[p];
					c = p;
				}
				else
					break;
			}
			queue[c] = toSift;
		}

		private void bottomDownHeapify(int p)
		{
			// Reference to the sifted element
			final double toSift = queue[p];
			int c = p * 2 + 1;
			while (c < n)
			{
				// If the right child is bigger, compare with it.
				if (c + 1 < n && queue[c] < queue[c + 1])
					c++;
				if (toSift < queue[c])
				{
					// Move child up and move down one level in the tree.
					queue[p] = queue[c];
					p = c;
					c = p * 2 + 1;
				}
				else
					// Done
					break;
			}
			queue[p] = toSift;
		}

		/**
		 * Pick the top N from the data using ascending order, i.e. find the top n largest values.
		 * <p>
		 * If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException} will occur.
		 *
		 * @param list
		 *            the data list
		 * @return The top N (passed as a reference to internal data structure)
		 */
		public double[] top(double[] list)
		{
			return top(OPTION_CLEAN, list, list.length);
		}

		/**
		 * Pick the top N from the data using ascending order, i.e. find the top n largest values.
		 * <p>
		 * If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException} will occur.
		 *
		 * @param options
		 *            the options
		 * @param list
		 *            the data list
		 * @return The top N (passed as a reference to internal data structure)
		 */
		public double[] top(int options, double[] list)
		{
			return top(options, list, list.length);
		}

		/**
		 * Pick the top N from the data using ascending order, i.e. find the top n largest values.
		 * <p>
		 * If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException} will occur.
		 *
		 * @param list
		 *            the data list
		 * @param size
		 *            The size of the list (must be equal or above N)
		 * @param options
		 *            the options
		 * @return The top N (passed as a reference to internal data structure)
		 */
		public double[] top(int options, double[] list, int size)
		{
			queue[0] = list[0];

			// Fill
			int i = 1;
			while (i < n)
			{
				// Insert new value at the end
				queue[i] = list[i];
				topUpHeapify(i);
				i++;
			}

			// Scan
			while (i < size)
			{
				// Replace if higher
				if (queue[0] < list[i])
				{
					queue[0] = list[i];
					topDownHeapify(0);
				}
				i++;
			}

			if (options == 0)
				return queue;

			return quickFinish(queue, options | OPTION_TOP);
		}

		private void topUpHeapify(int c)
		{
			//for (int p = (c - 1) / 2; c != 0 && queue[c] < queue[p]; c = p, p = (c - 1) / 2)
			//{
			//	double pDist = queue[p];
			//	queue[p] = queue[c];
			//	queue[c] = pDist;
			//}

			// Reference to the sifted element
			final double toSift = queue[c];
			while (c > 0)
			{
				final int p = (c - 1) >>> 1;
				if (toSift < queue[p])
				{
					// Move parent down and move up one level in the tree.
					queue[c] = queue[p];
					c = p;
				}
				else
					break;
			}
			queue[c] = toSift;
		}

		private void topDownHeapify(int p)
		{
			// Reference to the sifted element
			final double toSift = queue[p];
			int c = p * 2 + 1;
			while (c < n)
			{
				// If the right child is bigger, compare with it.
				if (c + 1 < n && queue[c] > queue[c + 1])
					c++;
				if (toSift > queue[c])
				{
					// Move child up and move down one level in the tree.
					queue[p] = queue[c];
					p = c;
					c = p * 2 + 1;
				}
				else
					// Done
					break;
			}
			queue[p] = toSift;
		}
	}

	/**
	 * Provide partial sort of double arrays
	 */
	public static class DoubleSelector
	{
		/**
		 * The number N to select
		 */
		final int n;
		/**
		 * Working storage
		 */
		private final double[] queue;

		/**
		 * Create a new DoubleSelector
		 *
		 * @param n
		 *            The number N to select
		 */
		public DoubleSelector(int n)
		{
			if (n < 1)
				throw new IllegalArgumentException("N must be strictly positive");
			this.n = n;
			queue = new double[n];
		}

		/**
		 * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
		 * <p>
		 * If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException} will occur.
		 *
		 * @param list
		 *            the data list
		 * @return The bottom N (passed as a reference to internal data structure)
		 */
		public double[] bottom(double[] list)
		{
			return bottom(OPTION_CLEAN, list, list.length);
		}

		/**
		 * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
		 * <p>
		 * If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException} will occur.
		 *
		 * @param list
		 *            the data list
		 * @param options
		 *            the options
		 * @return The bottom N (passed as a reference to internal data structure)
		 */
		public double[] bottom(int options, double[] list)
		{
			return bottom(options, list, list.length);
		}

		/**
		 * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
		 * <p>
		 * If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException} will occur.
		 *
		 * @param list
		 *            the data list
		 * @param size
		 *            The size of the list (must be equal or above N)
		 * @param options
		 *            the options
		 * @return The bottom N (passed as a reference to internal data structure)
		 */
		public double[] bottom(int options, double[] list, int size)
		{
			// We retain a pointer to the current highest value in the set.
			int max = 0;
			queue[0] = list[0];

			// Fill
			int i = 1;
			while (i < n)
			{
				queue[i] = list[i];
				if (queue[max] < queue[i])
					max = i;
				i++;
			}

			// Scan
			while (i < size)
			{
				// Replace if lower
				if (queue[max] > list[i])
				{
					queue[max] = list[i];
					// Find new max
					max = bottomMax(queue);
				}
				i++;
			}

			if (options == 0)
				return queue;

			if ((options & OPTION_HEAD_FIRST) != 0)
				swapHead(queue, max);

			return quickFinish(queue, options);
		}

		/**
		 * Pick the top N from the data using ascending order, i.e. find the top n largest values.
		 * <p>
		 * If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException} will occur.
		 *
		 * @param list
		 *            the data list
		 * @return The top N (passed as a reference to internal data structure)
		 */
		public double[] top(double[] list)
		{
			return top(OPTION_CLEAN, list, list.length);
		}

		/**
		 * Pick the top N from the data using ascending order, i.e. find the top n largest values.
		 * <p>
		 * If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException} will occur.
		 *
		 * @param options
		 *            the options
		 * @param list
		 *            the data list
		 * @return The top N (passed as a reference to internal data structure)
		 */
		public double[] top(int options, double[] list)
		{
			return top(options, list, list.length);
		}

		/**
		 * Pick the top N from the data using ascending order, i.e. find the top n largest values.
		 * <p>
		 * If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException} will occur.
		 *
		 * @param list
		 *            the data list
		 * @param size
		 *            The size of the list (must be equal or above N)
		 * @param options
		 *            the options
		 * @return The top N (passed as a reference to internal data structure)
		 */
		public double[] top(int options, double[] list, int size)
		{
			// We retain a pointer to the current highest value in the set.
			int max = 0;
			queue[0] = list[0];

			// Fill
			int i = 1;
			while (i < n)
			{
				queue[i] = list[i];
				if (queue[max] > queue[i])
					max = i;
				i++;
			}

			// Scan
			while (i < size)
			{
				// Replace if higher
				if (queue[max] < list[i])
				{
					queue[max] = list[i];
					// Find new max
					max = topMax(queue);
				}
				i++;
			}

			if (options == 0)
				return queue;

			if ((options & OPTION_HEAD_FIRST) != 0)
				swapHead(queue, max);

			return quickFinish(queue, options | OPTION_TOP);
		}
	}

	private static double[] quickFinish(double[] data, int options)
	{
		data = removeNaN(data, options);
		sort(data, options);
		return data;
	}

	private static double[] bottomFinish(double[] data, int options)
	{
		if (options == 0)
			return data;
		replaceHead(data, options);
		return quickFinish(data, options);
	}

	private static double[] topFinish(double[] data, int options)
	{
		if (options == 0)
			return data;
		options |= OPTION_TOP;
		replaceHead(data, options);
		return quickFinish(data, options);
	}

	private static void replaceHead(double[] data, int options)
	{
		if ((options & OPTION_HEAD_FIRST) != 0)
			swapHead(data, ((options & OPTION_TOP) != 0) ? topMax(data) : bottomMax(data));
	}

	private static int bottomMax(double[] data)
	{
		int max = 0;
		for (int i = 1; i < data.length; i++)
			if (data[max] < data[i])
				max = i;
		return max;
	}

	private static int topMax(double[] data)
	{
		int max = 0;
		for (int i = 1; i < data.length; i++)
			if (data[max] > data[i])
				max = i;
		return max;
	}

	private static void swapHead(double[] data, int max)
	{
		final double head = data[max];
		data[max] = data[0];
		data[0] = head;
	}

	private static void sort(double[] data, int options)
	{
		if ((options & OPTION_SORT) != 0)
		{
			Arrays.sort(data);
			if ((options & OPTION_TOP) != 0)
				SimpleArrayUtils.reverse(data);
		}
	}

	private static double[] removeNaN(double[] data, int options)
	{
		if ((options & OPTION_REMOVE_NAN) != 0)
		{
			int size = 0;
			for (int i = 0; i < data.length; i++)
			{
				if (Double.isNaN(data[i]))
					continue;
				data[size++] = data[i];
			}
			if (size == data.length)
				return data;
			if (size == 0)
				return new double[0];
			data = Arrays.copyOf(data, size);
		}
		return data;
	}

	/**
	 * Provide partial sort of float arrays
	 * <p>
	 * This class is based on ags.utils.dataStructures.trees.secondGenKD.ResultHeap.
	 * Ideas have been taken from:
	 * http://sites.fas.harvard.edu/~libs111/code/heaps/Heap.java
	 */
	public static class FloatHeap
	{
		/**
		 * The number N to select
		 */
		final int n;
		/**
		 * Working storage
		 */
		private final float[] queue;

		/**
		 * Instantiates a new max float heap.
		 *
		 * @param n
		 *            the number to select
		 */
		public FloatHeap(int n)
		{
			if (n < 1)
				throw new IllegalArgumentException("N must be strictly positive");
			this.queue = new float[n];
			this.n = n;
		}

		/**
		 * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
		 * <p>
		 * If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException} will occur.
		 *
		 * @param list
		 *            the data list
		 * @return The bottom N (passed as a reference to internal data structure)
		 */
		public float[] bottom(float[] list)
		{
			return bottom(OPTION_CLEAN, list, list.length);
		}

		/**
		 * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
		 * <p>
		 * If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException} will occur.
		 *
		 * @param list
		 *            the data list
		 * @param options
		 *            the options
		 * @return The bottom N (passed as a reference to internal data structure)
		 */
		public float[] bottom(int options, float[] list)
		{
			return bottom(options, list, list.length);
		}

		/**
		 * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
		 * <p>
		 * If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException} will occur.
		 *
		 * @param list
		 *            the data list
		 * @param size
		 *            The size of the list (must be equal or above N)
		 * @param options
		 *            the options
		 * @return The bottom N (passed as a reference to internal data structure)
		 */
		public float[] bottom(int options, float[] list, int size)
		{
			queue[0] = list[0];

			// Fill
			int i = 1;
			while (i < n)
			{
				// Insert new value at the end
				queue[i] = list[i];
				bottomUpHeapify(i);
				i++;
			}

			// Scan
			while (i < size)
			{
				// Replace if lower
				if (queue[0] > list[i])
				{
					queue[0] = list[i];
					bottomDownHeapify(0);
				}
				i++;
			}

			if (options == 0)
				return queue;

			return quickFinish(queue, options);
		}

		private void bottomUpHeapify(int c)
		{
			//for (int p = (c - 1) / 2; c != 0 && queue[c] > queue[p]; c = p, p = (c - 1) / 2)
			//{
			//	float pDist = queue[p];
			//	queue[p] = queue[c];
			//	queue[c] = pDist;
			//}

			// Reference to the sifted element
			final float toSift = queue[c];
			while (c > 0)
			{
				final int p = (c - 1) >>> 1;
				if (toSift > queue[p])
				{
					// Move parent down and move up one level in the tree.
					queue[c] = queue[p];
					c = p;
				}
				else
					break;
			}
			queue[c] = toSift;
		}

		private void bottomDownHeapify(int p)
		{
			// Reference to the sifted element
			final float toSift = queue[p];
			int c = p * 2 + 1;
			while (c < n)
			{
				// If the right child is bigger, compare with it.
				if (c + 1 < n && queue[c] < queue[c + 1])
					c++;
				if (toSift < queue[c])
				{
					// Move child up and move down one level in the tree.
					queue[p] = queue[c];
					p = c;
					c = p * 2 + 1;
				}
				else
					// Done
					break;
			}
			queue[p] = toSift;
		}

		/**
		 * Pick the top N from the data using ascending order, i.e. find the top n largest values.
		 * <p>
		 * If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException} will occur.
		 *
		 * @param list
		 *            the data list
		 * @return The top N (passed as a reference to internal data structure)
		 */
		public float[] top(float[] list)
		{
			return top(OPTION_CLEAN, list, list.length);
		}

		/**
		 * Pick the top N from the data using ascending order, i.e. find the top n largest values.
		 * <p>
		 * If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException} will occur.
		 *
		 * @param options
		 *            the options
		 * @param list
		 *            the data list
		 * @return The top N (passed as a reference to internal data structure)
		 */
		public float[] top(int options, float[] list)
		{
			return top(options, list, list.length);
		}

		/**
		 * Pick the top N from the data using ascending order, i.e. find the top n largest values.
		 * <p>
		 * If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException} will occur.
		 *
		 * @param list
		 *            the data list
		 * @param size
		 *            The size of the list (must be equal or above N)
		 * @param options
		 *            the options
		 * @return The top N (passed as a reference to internal data structure)
		 */
		public float[] top(int options, float[] list, int size)
		{
			queue[0] = list[0];

			// Fill
			int i = 1;
			while (i < n)
			{
				// Insert new value at the end
				queue[i] = list[i];
				topUpHeapify(i);
				i++;
			}

			// Scan
			while (i < size)
			{
				// Replace if lower
				if (queue[0] > list[i])
				{
					queue[0] = list[i];
					topDownHeapify(0);
				}
				i++;
			}

			if (options == 0)
				return queue;

			return quickFinish(queue, options | OPTION_TOP);
		}

		private void topUpHeapify(int c)
		{
			//for (int p = (c - 1) / 2; c != 0 && queue[c] < queue[p]; c = p, p = (c - 1) / 2)
			//{
			//	float pDist = queue[p];
			//	queue[p] = queue[c];
			//	queue[c] = pDist;
			//}

			// Reference to the sifted element
			final float toSift = queue[c];
			while (c > 0)
			{
				final int p = (c - 1) >>> 1;
				if (toSift < queue[p])
				{
					// Move parent down and move up one level in the tree.
					queue[c] = queue[p];
					c = p;
				}
				else
					break;
			}
			queue[c] = toSift;
		}

		private void topDownHeapify(int p)
		{
			// Reference to the sifted element
			final float toSift = queue[p];
			int c = p * 2 + 1;
			while (c < n)
			{
				// If the right child is bigger, compare with it.
				if (c + 1 < n && queue[c] > queue[c + 1])
					c++;
				if (toSift > queue[c])
				{
					// Move child up and move down one level in the tree.
					queue[p] = queue[c];
					p = c;
					c = p * 2 + 1;
				}
				else
					// Done
					break;
			}
			queue[p] = toSift;
		}
	}

	/**
	 * Provide partial sort of float arrays
	 */
	public static class FloatSelector
	{
		/**
		 * The number N to select
		 */
		final int n;
		/**
		 * Working storage
		 */
		private final float[] queue;

		/**
		 * Create a new FloatSelector
		 *
		 * @param n
		 *            The number N to select
		 */
		public FloatSelector(int n)
		{
			if (n < 1)
				throw new IllegalArgumentException("N must be strictly positive");
			this.n = n;
			queue = new float[n];
		}

		/**
		 * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
		 * <p>
		 * If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException} will occur.
		 *
		 * @param list
		 *            the data list
		 * @return The bottom N (passed as a reference to internal data structure)
		 */
		public float[] bottom(float[] list)
		{
			return bottom(OPTION_CLEAN, list, list.length);
		}

		/**
		 * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
		 * <p>
		 * If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException} will occur.
		 *
		 * @param list
		 *            the data list
		 * @param options
		 *            the options
		 * @return The bottom N (passed as a reference to internal data structure)
		 */
		public float[] bottom(int options, float[] list)
		{
			return bottom(options, list, list.length);
		}

		/**
		 * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
		 * <p>
		 * If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException} will occur.
		 *
		 * @param list
		 *            the data list
		 * @param size
		 *            The size of the list (must be equal or above N)
		 * @param options
		 *            the options
		 * @return The bottom N (passed as a reference to internal data structure)
		 */
		public float[] bottom(int options, float[] list, int size)
		{
			// We retain a pointer to the current highest value in the set.
			int max = 0;
			queue[0] = list[0];

			// Fill
			int i = 1;
			while (i < n)
			{
				queue[i] = list[i];
				if (queue[max] < queue[i])
					max = i;
				i++;
			}

			// Scan
			while (i < size)
			{
				// Replace if lower
				if (queue[max] > list[i])
				{
					queue[max] = list[i];
					// Find new max
					max = bottomMax(queue);
				}
				i++;
			}

			if (options == 0)
				return queue;

			if ((options & OPTION_HEAD_FIRST) != 0)
				swapHead(queue, max);

			return quickFinish(queue, options);
		}

		/**
		 * Pick the top N from the data using ascending order, i.e. find the top n largest values.
		 * <p>
		 * If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException} will occur.
		 *
		 * @param list
		 *            the data list
		 * @return The top N (passed as a reference to internal data structure)
		 */
		public float[] top(float[] list)
		{
			return top(OPTION_CLEAN, list, list.length);
		}

		/**
		 * Pick the top N from the data using ascending order, i.e. find the top n largest values.
		 * <p>
		 * If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException} will occur.
		 *
		 * @param options
		 *            the options
		 * @param list
		 *            the data list
		 * @return The top N (passed as a reference to internal data structure)
		 */
		public float[] top(int options, float[] list)
		{
			return top(options, list, list.length);
		}

		/**
		 * Pick the top N from the data using ascending order, i.e. find the top n largest values.
		 * <p>
		 * If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException} will occur.
		 *
		 * @param list
		 *            the data list
		 * @param size
		 *            The size of the list (must be equal or above N)
		 * @param options
		 *            the options
		 * @return The top N (passed as a reference to internal data structure)
		 */
		public float[] top(int options, float[] list, int size)
		{
			// We retain a pointer to the current highest value in the set.
			int max = 0;
			queue[0] = list[0];

			// Fill
			int i = 1;
			while (i < n)
			{
				queue[i] = list[i];
				if (queue[max] > queue[i])
					max = i;
				i++;
			}

			// Scan
			while (i < size)
			{
				// Replace if higher
				if (queue[max] < list[i])
				{
					queue[max] = list[i];
					// Find new max
					max = topMax(queue);
				}
				i++;
			}

			if (options == 0)
				return queue;

			if ((options & OPTION_HEAD_FIRST) != 0)
				swapHead(queue, max);

			return quickFinish(queue, options | OPTION_TOP);
		}
	}

	private static int bottomMax(float[] data)
	{
		int max = 0;
		for (int i = 1; i < data.length; i++)
			if (data[max] < data[i])
				max = i;
		return max;
	}

	private static int topMax(float[] data)
	{
		int max = 0;
		for (int i = 1; i < data.length; i++)
			if (data[max] > data[i])
				max = i;
		return max;
	}

	private static float[] quickFinish(float[] data, int options)
	{
		data = removeNaN(data, options);
		sort(data, options);
		return data;
	}

	private static float[] bottomFinish(float[] data, int options)
	{
		if (options == 0)
			return data;
		replaceHead(data, options);
		return quickFinish(data, options);
	}

	private static float[] topFinish(float[] data, int options)
	{
		if (options == 0)
			return data;
		options |= OPTION_TOP;
		replaceHead(data, options);
		return quickFinish(data, options);
	}

	private static void replaceHead(float[] data, int options)
	{
		if ((options & OPTION_HEAD_FIRST) != 0)
			swapHead(data, ((options & OPTION_TOP) != 0) ? topMax(data) : bottomMax(data));
	}

	private static void swapHead(float[] data, int max)
	{
		final float head = data[max];
		data[max] = data[0];
		data[0] = head;
	}

	private static float[] removeNaN(float[] data, int options)
	{
		if ((options & OPTION_REMOVE_NAN) != 0)
		{
			int size = 0;
			for (int i = 0; i < data.length; i++)
			{
				if (Float.isNaN(data[i]))
					continue;
				data[size++] = data[i];
			}
			if (size == data.length)
				return data;
			if (size == 0)
				return new float[0];
			data = Arrays.copyOf(data, size);
		}
		return data;
	}

	private static void sort(float[] data, int options)
	{
		if ((options & OPTION_SORT) != 0)
		{
			Arrays.sort(data);
			if ((options & OPTION_TOP) != 0)
				SimpleArrayUtils.reverse(data);
		}
	}

	// XXX - Copy bottom methods from here

	/**
	 * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
	 *
	 * @param list
	 *            the data list
	 * @param n
	 *            The number N to select
	 * @return The bottom N
	 */
	public static double[] bottom(double[] list, int n)
	{
		if (list == null)
			return new double[0];
		return bottom(list, list.length, n);
	}

	/**
	 * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
	 *
	 * @param list
	 *            the data list
	 * @param size
	 *            The size of the list
	 * @param n
	 *            The number N to select
	 * @return The bottom N
	 */
	public static double[] bottom(double[] list, int size, int n)
	{
		return bottom(OPTION_CLEAN, list, size, n);
	}

	/**
	 * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
	 *
	 * @param options
	 *            the options
	 * @param list
	 *            the data list
	 * @param n
	 *            The number N to select
	 * @return The bottom N
	 */
	public static double[] bottom(int options, double[] list, int n)
	{
		if (list == null)
			return new double[0];
		return bottom(options, list, list.length, n);
	}

	/**
	 * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
	 *
	 * @param options
	 *            the options
	 * @param list
	 *            the data list
	 * @param size
	 *            The size of the list
	 * @param n
	 *            The number N to select
	 * @return The bottom N
	 */
	public static double[] bottom(int options, double[] list, int size, int n)
	{
		if (list == null || size <= 0)
			return new double[0];
		size = Math.min(size, list.length);
		if (size <= n)
			list = bottomFinish(list.clone(), options);
		else if (n < 5)
			list = new DoubleSelector(n).bottom(options, list, size);
		else
			list = new DoubleHeap(n).bottom(options, list, size);
		return list;
	}

	/**
	 * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
	 *
	 * @param list
	 *            the data list
	 * @param n
	 *            The number N to select
	 * @return The bottom N
	 */
	public static float[] bottom(float[] list, int n)
	{
		if (list == null)
			return new float[0];
		return bottom(list, list.length, n);
	}

	/**
	 * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
	 *
	 * @param list
	 *            the data list
	 * @param size
	 *            The size of the list
	 * @param n
	 *            The number N to select
	 * @return The bottom N
	 */
	public static float[] bottom(float[] list, int size, int n)
	{
		return bottom(OPTION_CLEAN, list, size, n);
	}

	/**
	 * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
	 *
	 * @param options
	 *            the options
	 * @param list
	 *            the data list
	 * @param n
	 *            The number N to select
	 * @return The bottom N
	 */
	public static float[] bottom(int options, float[] list, int n)
	{
		if (list == null)
			return new float[0];
		return bottom(options, list, list.length, n);
	}

	/**
	 * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
	 *
	 * @param options
	 *            the options
	 * @param list
	 *            the data list
	 * @param size
	 *            The size of the list
	 * @param n
	 *            The number N to select
	 * @return The bottom N
	 */
	public static float[] bottom(int options, float[] list, int size, int n)
	{
		if (list == null || size <= 0)
			return new float[0];
		size = Math.min(size, list.length);
		if (size <= n)
			list = bottomFinish(list.clone(), options);
		else if (n < 5)
			list = new FloatSelector(n).bottom(options, list, size);
		else
			list = new FloatHeap(n).bottom(options, list, size);
		return list;
	}

	// XXX - Copy to here

	/**
	 * Pick the top N from the data using ascending order, i.e. find the top n smallest values.
	 *
	 * @param list
	 *            the data list
	 * @param n
	 *            The number N to select
	 * @return The top N
	 */
	public static double[] top(double[] list, int n)
	{
		if (list == null)
			return new double[0];
		return top(list, list.length, n);
	}

	/**
	 * Pick the top N from the data using ascending order, i.e. find the top n smallest values.
	 *
	 * @param list
	 *            the data list
	 * @param size
	 *            The size of the list
	 * @param n
	 *            The number N to select
	 * @return The top N
	 */
	public static double[] top(double[] list, int size, int n)
	{
		return top(OPTION_CLEAN, list, size, n);
	}

	/**
	 * Pick the top N from the data using ascending order, i.e. find the top n smallest values.
	 *
	 * @param options
	 *            the options
	 * @param list
	 *            the data list
	 * @param n
	 *            The number N to select
	 * @return The top N
	 */
	public static double[] top(int options, double[] list, int n)
	{
		if (list == null)
			return new double[0];
		return top(options, list, list.length, n);
	}

	/**
	 * Pick the top N from the data using ascending order, i.e. find the top n smallest values.
	 *
	 * @param options
	 *            the options
	 * @param list
	 *            the data list
	 * @param size
	 *            The size of the list
	 * @param n
	 *            The number N to select
	 * @return The top N
	 */
	public static double[] top(int options, double[] list, int size, int n)
	{
		if (list == null || size <= 0)
			return new double[0];
		size = Math.min(size, list.length);
		if (size <= n)
			list = topFinish(list.clone(), options);
		else if (n < 5)
			list = new DoubleSelector(n).top(options, list, size);
		else
			list = new DoubleHeap(n).top(options, list, size);
		return list;
	}

	/**
	 * Pick the top N from the data using ascending order, i.e. find the top n smallest values.
	 *
	 * @param list
	 *            the data list
	 * @param n
	 *            The number N to select
	 * @return The top N
	 */
	public static float[] top(float[] list, int n)
	{
		if (list == null)
			return new float[0];
		return top(list, list.length, n);
	}

	/**
	 * Pick the top N from the data using ascending order, i.e. find the top n smallest values.
	 *
	 * @param list
	 *            the data list
	 * @param size
	 *            The size of the list
	 * @param n
	 *            The number N to select
	 * @return The top N
	 */
	public static float[] top(float[] list, int size, int n)
	{
		return top(OPTION_CLEAN, list, size, n);
	}

	/**
	 * Pick the top N from the data using ascending order, i.e. find the top n smallest values.
	 *
	 * @param options
	 *            the options
	 * @param list
	 *            the data list
	 * @param n
	 *            The number N to select
	 * @return The top N
	 */
	public static float[] top(int options, float[] list, int n)
	{
		if (list == null)
			return new float[0];
		return top(options, list, list.length, n);
	}

	/**
	 * Pick the top N from the data using ascending order, i.e. find the top n smallest values.
	 *
	 * @param options
	 *            the options
	 * @param list
	 *            the data list
	 * @param size
	 *            The size of the list
	 * @param n
	 *            The number N to select
	 * @return The top N
	 */
	public static float[] top(int options, float[] list, int size, int n)
	{
		if (list == null || size <= 0)
			return new float[0];
		size = Math.min(size, list.length);
		if (size <= n)
			list = topFinish(list.clone(), options);
		else if (n < 5)
			list = new FloatSelector(n).top(options, list, size);
		else
			list = new FloatHeap(n).top(options, list, size);
		return list;
	}

	// The following select routine is copied from:
	// Numerical Recipes in C++, The Art of Scientific Computing, 2nd Edition, Cambridge Press.

	/**
	 * Given k in [0..n-1] returns an array value from arr[0..n-1] such that k array values are less than or equal to
	 * the one returned. The input array will be rearranged to have this value in arr[k], with all smaller elements
	 * moved to arr[0..k-1] (in arbitrary order) and all larger elements in arr[k+1..n-1] (also in arbitrary order).
	 *
	 * @param k
	 *            the k
	 * @param n
	 *            the n
	 * @param arr
	 *            the arr
	 * @return the value
	 */
	public static double select(int k, int n, double[] arr)
	{
		int i, ir, j, l, mid;
		double a;

		l = 0;
		ir = n - 1;
		for (;;)
		{
			if (ir <= l + 1)
			{
				if (ir == l + 1 && arr[ir] < arr[l])
					SWAP(arr, l, ir);
				return arr[k];
			}
			mid = (l + ir) >>> 1;
			SWAP(arr, mid, l + 1);
			if (arr[l] > arr[ir])
				SWAP(arr, l, ir);
			if (arr[l + 1] > arr[ir])
				SWAP(arr, l + 1, ir);
			if (arr[l] > arr[l + 1])
				SWAP(arr, l, l + 1);
			i = l + 1;
			j = ir;
			a = arr[l + 1];
			for (;;)
			{
				do
					i++;
				while (arr[i] < a);
				do
					j--;
				while (arr[j] > a);
				if (j < i)
					break;
				SWAP(arr, i, j);
			}
			arr[l + 1] = arr[j];
			arr[j] = a;
			if (j >= k)
				ir = j - 1;
			if (j <= k)
				l = i;
		}
	}

	private static void SWAP(double[] data, int a, int b)
	{
		final double temp = data[a];
		data[a] = data[b];
		data[b] = temp;
	}

	/**
	 * Given k in [0..n-1] returns an array value from arr[0..n-1] such that k array values are less than or equal to
	 * the one returned. The input array will be rearranged to have this value in arr[k], with all smaller elements
	 * moved to arr[0..k-1] (in arbitrary order) and all larger elements in arr[k+1..n-1] (also in arbitrary order).
	 *
	 * @param k
	 *            the k
	 * @param n
	 *            the n
	 * @param arr
	 *            the arr
	 * @return the value
	 */
	public static float select(int k, int n, float[] arr)
	{
		int i, ir, j, l, mid;
		float a;

		l = 0;
		ir = n - 1;
		for (;;)
		{
			if (ir <= l + 1)
			{
				if (ir == l + 1 && arr[ir] < arr[l])
					SWAP(arr, l, ir);
				return arr[k];
			}
			mid = (l + ir) >>> 1;
			SWAP(arr, mid, l + 1);
			if (arr[l] > arr[ir])
				SWAP(arr, l, ir);
			if (arr[l + 1] > arr[ir])
				SWAP(arr, l + 1, ir);
			if (arr[l] > arr[l + 1])
				SWAP(arr, l, l + 1);
			i = l + 1;
			j = ir;
			a = arr[l + 1];
			for (;;)
			{
				do
					i++;
				while (arr[i] < a);
				do
					j--;
				while (arr[j] > a);
				if (j < i)
					break;
				SWAP(arr, i, j);
			}
			arr[l + 1] = arr[j];
			arr[j] = a;
			if (j >= k)
				ir = j - 1;
			if (j <= k)
				l = i;
		}
	}

	private static void SWAP(float[] data, int a, int b)
	{
		final float temp = data[a];
		data[a] = data[b];
		data[b] = temp;
	}
}
