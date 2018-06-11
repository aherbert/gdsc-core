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
package gdsc.core.data.detection;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Class to compute collision detections between a point and a set of rectangles
 */
public class BinarySearchDetectionGrid implements DetectionGrid
{
	private static final int[] EMPTY = new int[0];

	private final int size;
	private final int[] minxIds, maxxIds, minyIds, maxyIds;
	private final double[] minx, maxx, miny, maxy;

	public BinarySearchDetectionGrid(Rectangle2D[] rectangles)
	{
		if (rectangles == null)
			throw new IllegalArgumentException("Rectangle2Ds must not be null");

		size = rectangles.length;

		// Store the ids of each rectangle sorted by index of the top-left and bottom-right corners
		minxIds = new int[size];
		minx = new double[size];
		maxx = new double[size];
		miny = new double[size];
		maxy = new double[size];
		for (int i = 0; i < size; i++)
		{
			minxIds[i] = i;
			minx[i] = rectangles[i].getMinX();
			maxx[i] = rectangles[i].getMaxX();
			miny[i] = rectangles[i].getMinY();
			maxy[i] = rectangles[i].getMaxY();
		}
		maxxIds = minxIds.clone();
		minyIds = minxIds.clone();
		maxyIds = minxIds.clone();

		sort(minxIds, minx);
		sort(maxxIds, maxx);
		sort(minyIds, miny);
		sort(maxyIds, maxy);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.detection.DetectionGrid#size()
	 */
	@Override
	public int size()
	{
		return size;
	}

	/**
	 * Sorts the indices in ascending order of their values
	 * 
	 * @param indices
	 * @param values
	 * @return The indices
	 */
	public static int[] sort(int[] indices, final double[] values)
	{
		// Convert data for sorting
		double[][] data = new double[indices.length][2];
		for (int i = indices.length; i-- > 0;)
		{
			data[i][0] = values[indices[i]];
			data[i][1] = indices[i];
		}

		Arrays.sort(data, new Comparator<double[]>()
		{
			@Override
			public int compare(double[] o1, double[] o2)
			{
				// Smallest first
				if (o1[0] < o2[0])
					return -1;
				if (o1[0] > o2[0])
					return 1;
				return 0;
			}
		});

		// Copy back
		for (int i = indices.length; i-- > 0;)
		{
			indices[i] = (int) data[i][1];
			values[i] = data[i][0];
		}

		return indices;
	}

	private final static byte TWO = 0x02;
	private final static byte FOUR = 0x04;

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.detection.DetectionGrid#find(double, double)
	 */
	@Override
	public int[] find(double x, double y)
	{
		// Perform a binary search to find the insert location of the index
		byte[] data = new byte[size];
		for (int i = findIndexUpToAndIncluding(minx, x) + 1; i-- > 0;)
			data[minxIds[i]]++;
		for (int i = findIndexAfter(maxx, x); i < size; i++)
			data[maxxIds[i]]++;

		if (!contains(data, TWO))
			return EMPTY;

		for (int i = findIndexUpToAndIncluding(miny, y) + 1; i-- > 0;)
			data[minyIds[i]]++;
		//if (!contains(data, 0x03))
		//	return EMPTY;
		for (int i = findIndexAfter(maxy, y); i < size; i++)
			data[maxyIds[i]]++;

		int count = count(data, FOUR);
		if (count == 0)
			return EMPTY;

		int[] list = new int[count];
		for (int i = size; i-- > 0;)
		{
			if (data[i] == FOUR)
			{
				list[--count] = i;
				if (count == 0)
					break;
			}
		}
		return list;
	}

	private boolean contains(byte[] data, byte value)
	{
		for (int i = data.length; i-- > 0;)
			if (data[i] == value)
				return true;
		return false;
	}

	private int count(byte[] data, byte value)
	{
		int count = 0;
		for (int i = data.length; i-- > 0;)
			if (data[i] == value)
				count++;
		return count;
	}

	/**
	 * Find the index such that all indices up to and including that point have a sum equal to
	 * or below p.
	 * 
	 * @param sum
	 * @param p
	 * @return the index (or -1)
	 */
	static int findIndexUpToAndIncluding(double[] sum, double p)
	{
		// index of the search key, if it is contained in the array; 
		// otherwise, (-(insertion point) - 1)
		int i = Arrays.binarySearch(sum, p);
		if (i < 0)
		{
			// The insertion point is defined as the point at which the key would be 
			// inserted into the array: the index of the first element greater than the key
			// or a.length if all elements in the array are less than the specified key.
			int insert = -(i + 1);
			return insert - 1;
		}
		else
		{
			// We found a match. Ensure we return the last index in the event of equality.
			while ((i + 1) < sum.length && sum[i + 1] == p)
				i++;
			return i;
		}
	}

	/**
	 * Find the index such that all indices including and after that point have a sum equal to
	 * or above p.
	 * 
	 * @param sum
	 * @param p
	 * @return the index (or -1)
	 */
	static int findIndexIncludingAndAfter(double[] sum, double p)
	{
		// index of the search key, if it is contained in the array; 
		// otherwise, (-(insertion point) - 1)
		int i = Arrays.binarySearch(sum, p);
		if (i < 0)
		{
			// The insertion point is defined as the point at which the key would be 
			// inserted into the array: the index of the first element greater than the key
			// or a.length if all elements in the array are less than the specified key.
			int insert = -(i + 1);
			return insert;
		}
		else
		{
			// We found a match. Ensure we return the first index in the event of equality.
			while (i > 0 && sum[i - 1] == p)
				i--;
			return i;
		}
	}

	/**
	 * Find the index such that all indices including and after that point have a sum above p.
	 * 
	 * @param sum
	 * @param p
	 * @return the index (or -1)
	 */
	static int findIndexAfter(double[] sum, double p)
	{
		// index of the search key, if it is contained in the array; 
		// otherwise, (-(insertion point) - 1)
		int i = Arrays.binarySearch(sum, p);
		if (i < 0)
		{
			// The insertion point is defined as the point at which the key would be 
			// inserted into the array: the index of the first element greater than the key
			// or a.length if all elements in the array are less than the specified key.
			int insert = -(i + 1);
			return insert;
		}
		else
		{
			// We found a match. Ensure we return the last index in the event of equality.
			while ((i + 1) < sum.length && sum[i + 1] == p)
				i++;
			return i + 1; // After
		}
	}
}
