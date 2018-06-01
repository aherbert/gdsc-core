package gdsc.core.utils;

import org.apache.commons.math3.distribution.TDistribution;

/*----------------------------------------------------------------------------- 
 * GDSC Software
 * 
 * Copyright (C) 2013 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

/**
 * Simple class to calculate the mean and standard deviation of data
 */
public class Statistics
{
	protected int n = 0;
	protected double s = 0, ss = 0;

	public Statistics()
	{
	}

	public Statistics(float[] data)
	{
		add(data);
	}

	public Statistics(double[] data)
	{
		add(data);
	}

	public Statistics(int[] data)
	{
		add(data);
	}

	/**
	 * Add the data
	 * 
	 * @param data
	 */
	public void add(float[] data)
	{
		if (data == null)
			return;
		addInternal(data, 0, data.length);
	}

	/**
	 * Add the data
	 * 
	 * @param data
	 */
	public void add(double[] data)
	{
		if (data == null)
			return;
		addInternal(data, 0, data.length);
	}

	/**
	 * Add the data
	 * 
	 * @param data
	 */
	public void add(int[] data)
	{
		if (data == null)
			return;
		addInternal(data, 0, data.length);
	}

	/**
	 * Checks that {@code fromIndex} and {@code toIndex} are in
	 * the range and throws an exception if they aren't.
	 */
	private static void rangeCheck(int arrayLength, int fromIndex, int toIndex)
	{
		if (fromIndex > toIndex)
		{
			throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
		}
		if (fromIndex < 0)
		{
			throw new ArrayIndexOutOfBoundsException(fromIndex);
		}
		if (toIndex > arrayLength)
		{
			throw new ArrayIndexOutOfBoundsException(toIndex);
		}
	}

	/**
	 * Add the data.
	 *
	 * @param data
	 *            the data
	 * @param from
	 *            the from index (inclusive)
	 * @param to
	 *            the to index (exclusive)
	 */
	public void add(float[] data, int from, int to)
	{
		if (data == null)
			return;
		rangeCheck(data.length, from, to);
		addInternal(data, from, to);
	}

	/**
	 * Add the data
	 *
	 * @param data
	 *            the data
	 * @param from
	 *            the from index (inclusive)
	 * @param to
	 *            the to index (exclusive)
	 */
	public void add(double[] data, int from, int to)
	{
		if (data == null)
			return;
		rangeCheck(data.length, from, to);
		addInternal(data, from, to);
	}

	/**
	 * Add the data
	 * 
	 *
	 * @param data
	 *            the data
	 * @param from
	 *            the from index (inclusive)
	 * @param to
	 *            the to index (exclusive)
	 */
	public void add(int[] data, int from, int to)
	{
		if (data == null)
			return;
		rangeCheck(data.length, from, to);
		addInternal(data, from, to);
	}

	/**
	 * Add the data
	 * 
	 *
	 * @param data
	 *            the data
	 * @param from
	 *            the from index (inclusive)
	 * @param to
	 *            the to index (exclusive)
	 */
	protected void addInternal(float[] data, int from, int to)
	{
		for (int i = from; i < to; i++)
		{
			final double value = data[i];
			s += value;
			ss += value * value;
		}
		n += (to - from);
	}

	/**
	 * Add the data
	 * 
	 *
	 * @param data
	 *            the data
	 * @param from
	 *            the from index (inclusive)
	 * @param to
	 *            the to index (exclusive)
	 */
	protected void addInternal(double[] data, int from, int to)
	{
		for (int i = from; i < to; i++)
		{
			final double value = data[i];
			s += value;
			ss += value * value;
		}
		n += (to - from);
	}

	/**
	 * Add the data
	 * 
	 *
	 * @param data
	 *            the data
	 * @param from
	 *            the from index (inclusive)
	 * @param to
	 *            the to index (exclusive)
	 */
	protected void addInternal(int[] data, int from, int to)
	{
		for (int i = from; i < to; i++)
		{
			final double value = data[i];
			s += value;
			ss += value * value;
		}
		n += (to - from);
	}

	/**
	 * Add the value
	 * 
	 * @param value
	 */
	public void add(final double value)
	{
		n++;
		s += value;
		ss += value * value;
	}

	/**
	 * Add the value n times
	 * 
	 * @param n
	 *            The number of times
	 * @param value
	 *            The value
	 */
	public void add(int n, double value)
	{
		this.n += n;
		s += n * value;
		ss += n * value * value;
	}

	/**
	 * Add the data. Synchronized for thread safety.
	 * 
	 * @param data
	 */
	synchronized public void safeAdd(float[] data)
	{
		add(data);
	}

	/**
	 * Add the data. Synchronized for thread safety.
	 * 
	 * @param data
	 */
	synchronized public void safeAdd(double[] data)
	{
		add(data);
	}

	/**
	 * Add the data. Synchronized for thread safety.
	 * 
	 * @param data
	 */
	synchronized public void safeAdd(int[] data)
	{
		add(data);
	}

	/**
	 * Add the value. Synchronized for thread safety.
	 * 
	 * @param value
	 */
	synchronized public void safeAdd(final double value)
	{
		add(value);
	}

	/**
	 * @return The number of data points
	 */
	public int getN()
	{
		return n;
	}

	/**
	 * @return The sum of the data points
	 */
	public double getSum()
	{
		return s;
	}

	/**
	 * @return The sum of squares of the data points
	 */
	public double getSumOfSquares()
	{
		return ss;
	}

	/**
	 * @return The mean of the data points
	 */
	public double getMean()
	{
		return s / n;
	}

	/**
	 * @return The unbiased standard deviation of the data points
	 */
	public double getStandardDeviation()
	{
		double stdDev = ss - ((double) s * s) / n;
		if (stdDev > 0.0)
			stdDev = Math.sqrt(stdDev / (n - 1));
		else
			stdDev = 0.0;
		return stdDev;
	}

	/**
	 * @return The unbiased variance of the data points
	 */
	public double getVariance()
	{
		double variance = ss - ((double) s * s) / n;
		if (variance > 0.0)
			variance = variance / (n - 1);
		else
			variance = 0.0;
		return variance;
	}

	/**
	 * The standard error is the standard deviation of the sample-mean's estimate of a population mean.
	 * <p>
	 * Uses the unbiased standard deviation divided by the square root of the sample size.
	 * 
	 * @return The standard error
	 */
	public double getStandardError()
	{
		if (n > 0)
		{
			return getStandardDeviation() / Math.sqrt(n);
		}
		return 0;
	}

	/**
	 * Add the statistics to the data
	 * 
	 * @param statistics
	 */
	public void add(Statistics statistics)
	{
		n += statistics.n;
		s += statistics.s;
		ss += statistics.ss;
	}

	/**
	 * Add the statistics to the data. Synchronized for thread safety.
	 * 
	 * @param statistics
	 */
	synchronized public void safeAdd(Statistics statistics)
	{
		add(statistics);
	}

	/**
	 * Gets the confidence interval around the mean using the given confidence level. This is computed using the
	 * critical value from the two-sided T-distribution multiplied by the standard error.
	 * 
	 * <p>
	 * If the number of samples is less than 2 then the result is positive infinity. If the confidence level is one then
	 * the result is positive infinity. If the confidence level is zero then the result is 0.
	 * 
	 * @see https://en.wikipedia.org/wiki/Confidence_interval#Basic_steps.
	 *
	 * @param c
	 *            the confidence level of the test (in the range 0-1)
	 * @return the confidence interval
	 * @throws IllegalArgumentException
	 *             if the confidence level is not in the range 0-1
	 */
	public double getConfidenceInterval(double c)
	{
		if (n < 2)
			return Double.POSITIVE_INFINITY;
		if (c < 0 || c > 1)
			throw new IllegalArgumentException("Confidence level must be in the range 0-1");
		double se = getStandardError();
		double alpha = 1 - (1 - c) * 0.5; // Two-sided, e.g. 0.95 -> 0.975
		int degreesOfFreedom = n - 1;
		TDistribution t = new TDistribution(degreesOfFreedom);
		return t.inverseCumulativeProbability(alpha) * se;
	}
}