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
package gdsc.core.filters;

import java.awt.Rectangle;

import gdsc.core.utils.SimpleArrayUtils;

/**
 * Compute sum using an area region of an 2D data frame.
 */
public class DAreaSum
{
	/** The index of the count in the results. */
	public final static int N = 0;
	/** The index of the sum in the results. */
	public final static int SUM = 1;

	private final static double[] EMPTY;
	static
	{
		EMPTY = new double[2];
		EMPTY[N] = 0;
		EMPTY[SUM] = Double.NaN;
	}

	private boolean rollingSums = false;

	/** The max x dimension. */
	public final int maxx;
	/** The max y dimension. */
	public final int maxy;
	/** The data. */
	protected final double[] data;
	/** The rolling sum table. */
	protected double[] s_ = null;

	/**
	 * Instantiates a new area statistics.
	 *
	 * @param data
	 *            the data
	 * @param maxx
	 *            the maxx
	 * @param maxy
	 *            the maxy
	 * @throws IllegalArgumentException
	 *             if maxx * maxy != data.length or data is null or length zero
	 */
	public DAreaSum(double[] data, int maxx, int maxy) throws IllegalArgumentException
	{
		SimpleArrayUtils.hasData2D(maxx, maxy, data);
		this.maxx = maxx;
		this.maxy = maxy;
		this.data = data;
	}

	/**
	 * Calculate the rolling sum tables.
	 */
	protected void calculateRollingSums()
	{
		if (s_ != null)
			return;

		// Compute the rolling sum and sum of squares
		// s(u,v) = f(u,v) + s(u-1,v) + s(u,v-1) - s(u-1,v-1)
		// where s(u,v) = 0 when either u,v < 0

		s_ = new double[data.length];

		// First row
		double cs_ = 0; // Column sum
		for (int i = 0; i < maxx; i++)
		{
			final double d = data[i];
			cs_ += d;
			s_[i] = cs_;
		}

		// Remaining rows:
		// sum = rolling sum of row + sum of row above
		for (int y = 1; y < maxy; y++)
		{
			int i = y * maxx;
			cs_ = 0;

			// Remaining columns
			for (int x = 0; x < maxx; x++, i++)
			{
				final double d = data[i];
				cs_ += d;

				s_[i] = s_[i - maxx] + cs_;
			}
		}
	}

	/**
	 * Gets the statistics within a region from minU to maxU and minV to maxV. Lower bounds inclusive and upper bounds
	 * inclusive.
	 *
	 * @param minU
	 *            the min U
	 * @param maxU
	 *            the max U
	 * @param minV
	 *            the min V
	 * @param maxV
	 *            the max V
	 * @return the statistics
	 */
	private double[] getStatisticsInternal(int minU, int maxU, int minV, int maxV)
	{
		// Note that the two methods use different bounds for their implementation
		if (rollingSums)
		{
			calculateRollingSums();
			// Lower bounds exclusive, Upper inclusive
			return getStatisticsRollingSums(minU - 1, maxU, minV - 1, maxV);
		}
		// Lower bounds inclusive, Upper exclusive
		return getStatisticsSimple(minU, maxU + 1, minV, maxV + 1);
	}

	/**
	 * Gets the statistics within a region from minU to maxU and minV to maxV. Lower bounds exclusive and upper bounds
	 * inclusive.
	 * <p>
	 * Use the rolling sum table.
	 *
	 * @param minU
	 *            the min U
	 * @param maxU
	 *            the max U
	 * @param minV
	 *            the min V
	 * @param maxV
	 *            the max V
	 * @return the statistics
	 */
	protected double[] getStatisticsRollingSums(int minU, int maxU, int minV, int maxV)
	{
		// Compute sum from rolling sum using:
		// sum(u,v) =
		// + s(u+N,v+N)
		// - s(u-N-1,v+N)
		// - s(u+N,v-N-1)
		// + s(u-N-1,v-N-1)
		// Note:
		// s(u,v) = 0 when either u,v < 0
		// s(u,v) = s(umax,v) when u>umax
		// s(u,v) = s(u,vmax) when v>vmax
		// s(u,v) = s(umax,vmax) when u>umax,v>vmax
		// Likewise for ss

		// Clip to limits
		if (maxU >= maxx)
			maxU = maxx - 1;
		if (maxV >= maxy)
			maxV = maxy - 1;

		// + s(u+N-1,v+N-1)
		double sum = s_[maxV * maxx + maxU];

		if (minU >= 0)
		{
			// - s(u-1,v+N-1)
			sum -= s_[maxV * maxx + minU];

			if (minV >= 0)
			{
				// - s(u+N-1,v-1)
				sum -= s_[minV * maxx + maxU];

				// + s(u-1,v-1)
				sum += s_[minV * maxx + minU];
			}
			else
				// Reset to bounds to calculate the number of pixels
				minV = -1;
		}
		else
		{
			// Reset to bounds to calculate the number of pixels
			minU = -1;

			if (minV >= 0)
				// - s(u+N-1,v-1)
				sum -= s_[minV * maxx + maxU];
			else
				// Reset to bounds to calculate the number of pixels
				minV = -1;
		}

		final int n = (maxU - minU) * (maxV - minV);

		return getResults(sum, n);
	}

	/**
	 * Gets the results.
	 *
	 * @param sum
	 *            the sum
	 * @param n
	 *            the n
	 * @return the results
	 */
	private static double[] getResults(double sum, int n)
	{
		final double[] stats = new double[2];
		stats[N] = n;
		stats[SUM] = sum;
		return stats;
	}

	/**
	 * Gets the statistics within a region from minU to maxU and minV to maxV. Lower bounds inclusive and upper bounds
	 * exclusive.
	 *
	 * @param minU
	 *            the min U
	 * @param maxU
	 *            the max U
	 * @param minV
	 *            the min V
	 * @param maxV
	 *            the max V
	 * @return the statistics
	 */
	protected double[] getStatisticsSimple(int minU, int maxU, int minV, int maxV)
	{
		// Clip to limits
		if (minU < 0)
			minU = 0;
		if (minV < 0)
			minV = 0;
		if (maxU > maxx)
			maxU = maxx;
		if (maxV > maxy)
			maxV = maxy;

		double sum = 0;
		for (int y = minV; y < maxV; y++)
			for (int x = minU, i = getIndex(minU, y); x < maxU; x++, i++)
				sum += data[i];

		final int n = (maxU - minU) * (maxV - minV);

		return getResults(sum, n);
	}

	/**
	 * Gets the statistics within a region +/- n.
	 * <p>
	 * Statistics can be accessed using the static properties in this class.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param n
	 *            the n
	 * @return the statistics
	 */
	public double[] getStatistics(int x, int y, int n)
	{
		// Bounds check
		if (x < 0 || y < 0 || x >= maxx || y >= maxy || n < 0)
			return EMPTY.clone();
		// Special case for 1 data point
		if (n == 0)
			return getSingleResult(x, y);
		// Lower bounds inclusive
		final int minU = x - n;
		final int minV = y - n;
		// Upper bounds inclusive
		final int maxU = x + n;
		final int maxV = y + n;
		return getStatisticsInternal(minU, maxU, minV, maxV);
	}

	/**
	 * Gets the result for an area covering only 1 pixel.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @return the single result
	 */
	protected double[] getSingleResult(int x, int y)
	{
		return new double[] { 1, data[getIndex(x, y)] };
	}

	/**
	 * Gets the index in the data.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @return the index
	 */
	protected int getIndex(int x, int y)
	{
		return y * maxx + x;
	}

	/**
	 * Gets the statistics within a region +/- n.
	 * <p>
	 * Statistics can be accessed using the static properties in this class.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param nx
	 *            the nx
	 * @param ny
	 *            the ny
	 * @return the statistics
	 */
	public double[] getStatistics(int x, int y, int nx, int ny)
	{
		// Bounds check
		if (x < 0 || y < 0 || x >= maxx || y >= maxy || nx < 0 || ny < 0)
			return EMPTY.clone();
		// Special case for 1 data point
		if (nx == 0 && ny == 0)
			return getSingleResult(x, y);
		// Lower bounds inclusive
		final int minU = x - nx;
		final int minV = y - ny;
		// Upper bounds inclusive
		final int maxU = x + nx;
		final int maxV = y + ny;
		return getStatisticsInternal(minU, maxU, minV, maxV);
	}

	/**
	 * Gets the statistics within a region.
	 * <p>
	 * Statistics can be accessed using the static properties in this class.
	 *
	 * @param region
	 *            the region
	 * @return the statistics
	 */
	public double[] getStatistics(Rectangle region)
	{
		// Upper bounds inclusive
		final int maxU = region.x + region.width - 1;
		final int maxV = region.y + region.height - 1;
		// Bounds check
		if (region.width <= 0 || region.height <= 0 || region.x >= maxx || region.y >= maxy || maxU < 0 || maxV < 0)
			return EMPTY.clone();
		// Lower bounds inclusive
		final int minU = region.x;
		final int minV = region.y;
		return getStatisticsInternal(minU, maxU, minV, maxV);
	}

	/**
	 * Checks if using a rolling sum table. This is faster for repeat calls over large areas.
	 *
	 * @return true, if using a rolling sum table
	 */
	public boolean isRollingSums()
	{
		return rollingSums;
	}

	/**
	 * Set to true to use a rolling sum table. This is faster for repeat calls over large areas.
	 *
	 * @param rollingSums
	 *            the new rolling sums
	 */
	public void setRollingSums(boolean rollingSums)
	{
		this.rollingSums = rollingSums;
	}
}
