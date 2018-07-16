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

/**
 * Provides sampling from a 2D histogram
 * <p>
 * Adapted from The GNU Scientific library (http://www.gnu.org/software/gsl/)
 */
public class PDF2DGSL
{
	private final double[] sum;
	/** The X-dimension size */
	public final int nx;
	/** The Y-dimension size */
	public final int ny;
	/** The size ({@link #nx} * {@link #ny}) */
	public final int n;
	/**
	 * The cumulative sum of the original input data
	 */
	public final double cumulative;

	/**
	 * Default constructor. Assumes the x-range and y-range increment from zero in integers.
	 *
	 * @param data
	 *            The data (packed in XY order, i = nx*y + x)
	 * @param nx
	 *            The X-dimension size
	 * @param ny
	 *            The y-dimension size
	 * @throws IllegalArgumentException
	 *             if the dimensions are not above zero
	 * @throws IllegalArgumentException
	 *             if the input data length is not at least nx * ny
	 * @throws IllegalArgumentException
	 *             if the input data contains negatives
	 */
	public PDF2DGSL(double[] data, int nx, int ny)
	{
		if (nx < 1 || ny < 1)
			throw new IllegalArgumentException("Dimensions must be above zero");
		this.nx = nx;
		this.ny = ny;
		n = nx * ny;

		if (data == null || data.length < n)
			throw new IllegalArgumentException("Input data must be at least equal to nx * ny");
		this.sum = new double[n + 1];

		double mean = 0, sum = 0;
		double c = 0;

		for (int i = 0; i < n; i++)
		{
			if (data[i] < 0)
				throw new IllegalArgumentException("Histogram bins must be non-negative");
			mean += (data[i] - mean) / (i + 1);
			c += data[i];
		}

		cumulative = c;

		this.sum[0] = 0;

		for (int i = 0; i < n; i++)
		{
			sum += (data[i] / mean) / n;
			this.sum[i + 1] = sum;
		}
	}

	/**
	 * Sample from the histogram using two uniform random numbers (in the range 0-1).
	 *
	 * @param r1
	 *            the first random number
	 * @param r2
	 *            the second random number
	 * @param point
	 *            The output coordinates buffer
	 * @return true if a sample was produced
	 */
	public boolean sample(double r1, double r2, double[] point)
	{
		if (point == null || point.length < 2)
			return false;

		/*
		 * Wrap the exclusive top of the bin down to the inclusive bottom of
		 * the bin. Since this is a single point it should not affect the
		 * distribution.
		 */

		if (r2 >= 1.0 || r2 < 0)
			r2 = 0.0;
		if (r1 >= 1.0 || r1 < 0)
			r1 = 0.0;

		final int k = find(r1);

		if (k == -1)
			return false;

		final int y = k / nx;
		final int x = k - (y * nx);
		final double delta = (r1 - sum[k]) / (sum[k + 1] - sum[k]);

		// Assume the x-range and y-range increment from zero in integers.
		// We could extend this class to support non-uniform ranges as per the GSL library
		//point[0]= xrange[x] + delta * (xrange[x + 1] - xrange[x]);
		//point[0] = yrange[y] + r2 * (yrange[y + 1] - yrange[y]);
		point[0] = x + delta;
		point[1] = y + r2;

		return true;
	}

	private int find(double x)
	{
		if (x >= sum[n])
			return -1;

		/* perform binary search */

		int upper = n;
		int lower = 0;

		while (upper - lower > 1)
		{
			final int mid = (upper + lower) >>> 1;

			if (x >= sum[mid])
				lower = mid;
			else
				upper = mid;
		}

		/* sanity check the result */

		if (x < sum[lower] || x >= sum[lower + 1])
			return -1;

		return lower;
	}

	/**
	 * Return the cumulative probability for the given coordinates.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @return p
	 */
	double get(int x, int y)
	{
		if (x < 0 || x >= nx || y < 0 || y > ny)
			return 0;
		return sum[y * nx + x];
	}
}
