/*
 * 
 */
package gdsc.core.math;

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
 * Compute the radial sum of 2D data.
 */
public class RadialSum
{

	/**
	 * Compute the radial sum of circles up to radius size/2. The sum includes all pixels that are at a radius (r) equal
	 * to or greater than r and less then r+1.
	 *
	 * @param data
	 *            the data (size*size)
	 * @param size
	 *            the size (in one dimension
	 * @return the sum
	 */
	public static double[] radialSum(float[] data, int size)
	{
		if (data == null)
			throw new IllegalArgumentException("Data is null");
		if (size < 1)
			throw new IllegalArgumentException("Size must be positive");
		if (data.length != size * size)
			throw new IllegalArgumentException("Data is incorrect size");

		// Centre at upper pixel
		// 1x1 => 0,0
		// 2x2 => 1,1
		// 3x3 => 1,1
		// 4x4 => 2,2
		int cx = size / 2;

		// Maximum distance from centre in each dimension
		int max = size - cx;

		// Squared distance
		int[] d2 = new int[max + 1];
		for (int i = 1; i < d2.length; i++)
		{
			d2[i] = i * i;
		}

		double[] sum = new double[max];

		// Centre
		int cxi = size * cx + cx;
		sum[0] = data[cxi];

		// Do the central row
		for (int x1 = cx + 1, xi = 1, i1 = cxi - 1, i2 = cxi + 1; x1 < size; x1++, xi++, i1++, i2--)
		{
			sum[xi] += data[i1] + data[i2];
		}
		// Do the central row
		for (int y1 = cx + 1, xi = 1, i1 = cxi + size, i2 = cxi - size; y1 < size; y1++, xi++, i1 += size, i2 -= size)
		{
			sum[xi] += data[i1] + data[i2];
		}

		// Sweep out from centre
		Y: for (int y1 = cx + 1, y2 = cx - 1, yi = 1; y1 < size; y1++, y2--, yi++)
		{
			int d2y = d2[yi];
			//@formatter:off
			// Initialise for sweep of 2 rows (below (y1) and above (y2)) 
			// from the centre outwards in both directions. missing the initial column.
			for (int x1 = cx + 1, 
					xi = 1, 
					xyi = yi, // This will be the initial squared distance index
					i1 = size * y1 + cx - 1, 
					i2 = size * y1 + cx + 1, 
					i3 = size * y2 + cx - 1, 
					i4 = size * y2 + cx + 1;
					// Condition
					x1 < size; 
					// Move indices
					x1++, xi++, i1--, i2++, i3--, i4++)
				//@formatter:on
			{
				int d = d2[xi] + d2y;
				// Find index in squared distance array:
				// d2[xyi] <= d < d2[xyi+1]
				// No need for loop as we are only moving a max of 1 pixel distance increment
				if (d2[xyi + 1] <= d)
				{
					xyi++;
					if (xyi == max)
						continue Y;
				}
				sum[xyi] += data[i1] + data[i2] + data[i3] + data[i4];
			}
		}

		return sum;
	}
}