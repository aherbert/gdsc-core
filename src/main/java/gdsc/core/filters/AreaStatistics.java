package gdsc.core.filters;

import java.awt.Rectangle;

import org.apache.commons.math3.util.FastMath;

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
 * Compute statistics using an area region of an 2D data frame.
 */
public class AreaStatistics
{
	/** The index of the count in the results. */
	public final static int N = 0;
	/** The index of the mean in the results. */
	public final static int MEAN = 1;
	/** The index of the standard deviation in the results. */
	public final static int SD = 2;

	private boolean rollingSums = false;

	private final int maxx, maxy;
	private final float[] data;
	private double[] s_ = null;
	private double[] ss = null;

	/**
	 * Instantiates a new area statistics.
	 *
	 * @param data
	 *            the data
	 * @param maxx
	 *            the maxx
	 * @param maxy
	 *            the maxy
	 */
	public AreaStatistics(float[] data, int maxx, int maxy)
	{
		this.maxx = maxx;
		this.maxy = maxy;
		this.data = data;
	}

	/**
	 * Calculate the rolling sum tables.
	 */
	private void calculateRollingSums()
	{
		if (s_ != null)
			return;

		// Compute the rolling sum and sum of squares
		// s(u,v) = f(u,v) + s(u-1,v) + s(u,v-1) - s(u-1,v-1) 
		// ss(u,v) = f(u,v) * f(u,v) + ss(u-1,v) + ss(u,v-1) - ss(u-1,v-1)
		// where s(u,v) = ss(u,v) = 0 when either u,v < 0

		s_ = new double[data.length];
		ss = new double[data.length];

		// First row
		double cs_ = 0; // Column sum
		double css = 0; // Column sum-squares
		for (int i = 0; i < maxx; i++)
		{
			double d = data[i];
			cs_ += d;
			css += d * d;
			s_[i] = cs_;
			ss[i] = css;
		}

		// Remaining rows:
		// sum = rolling sum of row + sum of row above
		for (int y = 1; y < maxy; y++)
		{
			int i = y * maxx;
			cs_ = 0;
			css = 0;

			// Remaining columns
			for (int x = 0; x < maxx; x++, i++)
			{
				double d = data[i];
				cs_ += d;
				css += d * d;

				s_[i] = s_[i - maxx] + cs_;
				ss[i] = ss[i - maxx] + css;
			}
		}
	}

	/**
	 * Gets the statistics within a region from minU to maxU and minV to maxV. Lower bounds exclusive and upper bounds
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
	private double[] getStatistics(int minU, int maxU, int minV, int maxV)
	{
		return (rollingSums) ? getStatisticsRollingSums(minU, maxU, minV, maxV)
				: getStatisticsSimple(minU, maxU, minV, maxV);
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
	private double[] getStatisticsRollingSums(int minU, int maxU, int minV, int maxV)
	{
		calculateRollingSums();

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

		// Clip to upper limit
		maxU = FastMath.min(maxU, maxx - 1);
		maxV = FastMath.min(maxV, maxy - 1);

		// + s(u+N-1,v+N-1) 
		int index = maxV * maxx + maxU;
		double sum = s_[index];
		double sumSquares = ss[index];

		if (minU >= 0)
		{
			// - s(u-1,v+N-1)
			index = maxV * maxx + minU;
			sum -= s_[index];
			sumSquares -= ss[index];
		}
		if (minV >= 0)
		{
			// - s(u+N-1,v-1)
			index = minV * maxx + maxU;
			sum -= s_[index];
			sumSquares -= ss[index];

			if (minU >= 0)
			{
				// + s(u-1,v-1)
				index = minV * maxx + minU;
				sum += s_[index];
				sumSquares += ss[index];
			}
		}

		// Reset to bounds to calculate the number of pixels
		if (minU < 0)
			minU = -1;
		if (minV < 0)
			minV = -1;

		double[] stats = new double[3];

		int n = (maxU - minU) * (maxV - minV);

		stats[N] = n;
		stats[MEAN] = sum / n;

		if (n > 1)
		{
			// Get the sum of squared differences
			double residuals = sumSquares - (sum * sum) / n;
			if (residuals > 0.0)
				stats[SD] = Math.sqrt(residuals / (n - 1.0));
		}

		return stats;
	}

	/**
	 * Gets the statistics within a region from minU to maxU and minV to maxV. Lower bounds exclusive and upper bounds
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
	private double[] getStatisticsSimple(int minU, int maxU, int minV, int maxV)
	{
		// Clip to limits
		minU = FastMath.max(0, minU + 1);
		minV = FastMath.max(0, minV + 1);
		maxU = FastMath.min(maxU, maxx - 1);
		maxV = FastMath.min(maxV, maxy - 1);

		double sum = 0;
		double sumSquares = 0;
		//int n = 0;
		for (int y = minV; y <= maxV; y++)
			for (int x = minU, i = getIndex(minU, y); x <= maxU; x++, i++)
			{
				float value = data[i];
				sum += value;
				sumSquares += value * value;
				//n++;
			}

		double[] stats = new double[3];

		int n = (maxU - minU + 1) * (maxV - minV + 1);

		stats[N] = n;
		stats[MEAN] = sum / n;

		if (n > 1)
		{
			// Get the sum of squared differences
			double residuals = sumSquares - (sum * sum) / n;
			if (residuals > 0.0)
				stats[SD] = Math.sqrt(residuals / (n - 1.0));
		}

		return stats;
	}

	/**
	 * Gets the statistics within a region +/- n.
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
			return new double[3];
		// Special case for 1 data point
		if (n == 0)
			return new double[] { 1, data[getIndex(x, y)], 0 };
		// Lower bounds exclusive
		int minU = x - n - 1;
		int minV = y - n - 1;
		// Upper bounds inclusive
		int maxU = x + n;
		int maxV = y + n;
		return getStatistics(minU, maxU, minV, maxV);
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
	private int getIndex(int x, int y)
	{
		return y * maxx + x;
	}

	/**
	 * Gets the statistics within a region.
	 *
	 * @param region
	 *            the region
	 * @return the statistics
	 */
	public double[] getStatistics(Rectangle region)
	{
		// Upper bounds inclusive
		int maxU = region.x + region.width - 1;
		int maxV = region.y + region.height - 1;
		// Bounds check
		if (region.width < 0 || region.height < 0 || region.x >= maxx || region.y >= maxy || maxU < 0 || maxV < 0)
			return new double[3];
		// Lower bounds exclusive
		int minU = region.x - 1;
		int minV = region.y - 1;
		return getStatistics(minU, maxU, minV, maxV);
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
