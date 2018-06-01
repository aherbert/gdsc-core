package gdsc.core.utils;

/*----------------------------------------------------------------------------- 
 * GDSC Software
 * 
 * Copyright (C) 2018 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

/**
 * Simple class to calculate the mean and standard deviation of data using a rolling algorithm. This should be used when
 * the numbers are large, e.g. 10^9 + 4, 10^9 + 7, 10^9 + 13, 10^9 + 16.
 * <p>
 * Based on org.apache.commons.math3.stat.descriptive.moment.SecondMoment.
 */
public class RollingStatistics extends Statistics
{
	public RollingStatistics()
	{
	}

	public RollingStatistics(float[] data)
	{
		super(data);
	}

	public RollingStatistics(double[] data)
	{
		super(data);
	}

	public RollingStatistics(int[] data)
	{
		super(data);
	}

	@Override
	protected void addInternal(float[] data, int from, int to)
	{
		for (int i = from; i < to; i++)
			add(data[i]);
	}

	@Override
	protected void addInternal(double[] data, int from, int to)
	{
		for (int i = from; i < to; i++)
			add(data[i]);
	}

	@Override
	protected void addInternal(int[] data, int from, int to)
	{
		for (int i = from; i < to; i++)
			add(data[i]);

	}

	@Override
	public void add(final double value)
	{
		// This has changed the meaning of the inherited values s and ss
		// s : sum -> mean 
		// ss : sum-squares -> sum (x-mean)^2
		// See https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online_algorithm
		// This has been adapted from org.apache.commons.math3.stat.descriptive.moment.SecondMoment
		final double delta = value - s;
		final double nB = n;
		n++;
		final double delta_n = delta / n;
		s += delta_n;
		ss += nB * delta * delta_n;
	}

	@Override
	public void add(int nA, double value)
	{
		// Note: for the input mean value the
		// deviation from mean is 0 (ss=0)
		double delta = value - s;
		final int nB = n;
		n += nA;
		s = (nA * value + nB * s) / n;
		ss += delta * delta * nA * nB / n;
	}

	@Override
	public double getSum()
	{
		return s * n;
	}

	@Override
	public double getSumOfSquares()
	{
		throw new NotImplementedException("Sum-of-squares not computed");
	}

	@Override
	public double getMean()
	{
		return s;
	}

	@Override
	public double getStandardDeviation()
	{
		return (n > 1) ? Math.sqrt(ss / (n - 1)) : (n == 1) ? 0 : Double.NaN;
	}

	@Override
	public double getVariance()
	{
		return (n > 1) ? ss / (n - 1) : (n == 1) ? 0 : Double.NaN;
	}

	@Override
	public void add(Statistics statistics)
	{
		if (statistics instanceof RollingStatistics)
		{
			RollingStatistics extra = (RollingStatistics) statistics;
			if (extra.n > 0)
			{
				// https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Parallel_algorithm
				double delta = extra.s - s;
				final int nA = extra.n;
				final int nB = n;
				n += nA;
				s = (nA * extra.s + nB * s) / n;
				ss += extra.ss + delta * delta * nA * nB / n;
			}
			return;
		}
		throw new NotImplementedException("Not a RollingStatistics instance");
	}
}