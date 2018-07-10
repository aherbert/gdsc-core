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

import org.apache.commons.math3.distribution.TDistribution;

/**
 * Simple class to calculate the mean and standard deviation of data.
 */
public class Statistics
{
	protected int n = 0;
	protected double s = 0;
	protected double ss = 0;

	/**
	 * Instantiates a new statistics.
	 */
	public Statistics()
	{
	}

	/**
	 * Instantiates a new statistics.
	 *
	 * @param data
	 *            the data
	 */
	public Statistics(float[] data)
	{
		add(data);
	}

	/**
	 * Instantiates a new statistics.
	 *
	 * @param data
	 *            the data
	 */
	public Statistics(double[] data)
	{
		add(data);
	}

	/**
	 * Instantiates a new statistics.
	 *
	 * @param data
	 *            the data
	 */
	public Statistics(int[] data)
	{
		add(data);
	}

	/**
	 * Add the data.
	 *
	 * @param data
	 *            the data
	 */
	public void add(float[] data)
	{
		if (data == null)
			return;
		addInternal(data, 0, data.length);
	}

	/**
	 * Add the data.
	 *
	 * @param data
	 *            the data
	 */
	public void add(double[] data)
	{
		if (data == null)
			return;
		addInternal(data, 0, data.length);
	}

	/**
	 * Add the data.
	 *
	 * @param data
	 *            the data
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
			throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
		if (fromIndex < 0)
			throw new ArrayIndexOutOfBoundsException(fromIndex);
		if (toIndex > arrayLength)
			throw new ArrayIndexOutOfBoundsException(toIndex);
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
	 * Add the data.
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
	 * Add the data.
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
	 * Add the data.
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
	 * Add the data.
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
	 * Add the data.
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
	 * Add the value.
	 *
	 * @param value
	 *            the value
	 */
	public void add(final double value)
	{
		n++;
		s += value;
		ss += value * value;
	}

	/**
	 * Add the value n times.
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
	 *            the data
	 */
	synchronized public void safeAdd(float[] data)
	{
		add(data);
	}

	/**
	 * Add the data. Synchronized for thread safety.
	 *
	 * @param data
	 *            the data
	 */
	synchronized public void safeAdd(double[] data)
	{
		add(data);
	}

	/**
	 * Add the data. Synchronized for thread safety.
	 *
	 * @param data
	 *            the data
	 */
	synchronized public void safeAdd(int[] data)
	{
		add(data);
	}

	/**
	 * Add the value. Synchronized for thread safety.
	 *
	 * @param value
	 *            the value
	 */
	synchronized public void safeAdd(final double value)
	{
		add(value);
	}

	/**
	 * Gets the n.
	 *
	 * @return The number of data points
	 */
	public int getN()
	{
		return n;
	}

	/**
	 * Gets the sum.
	 *
	 * @return The sum of the data points
	 */
	public double getSum()
	{
		return s;
	}

	/**
	 * Gets the sum of squares.
	 *
	 * @return The sum of squares of the data points
	 */
	public double getSumOfSquares()
	{
		return ss;
	}

	/**
	 * Gets the mean.
	 *
	 * @return The mean of the data points
	 */
	public double getMean()
	{
		return s / n;
	}

	/**
	 * Gets the standard deviation.
	 *
	 * @return The unbiased standard deviation of the data points
	 */
	public double getStandardDeviation()
	{
		double stdDev = ss - (s * s) / n;
		if (stdDev > 0.0)
			stdDev = Math.sqrt(stdDev / (n - 1));
		else
			stdDev = 0.0;
		return stdDev;
	}

	/**
	 * Gets the variance.
	 *
	 * @return The unbiased variance of the data points
	 */
	public double getVariance()
	{
		double variance = ss - (s * s) / n;
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
			return getStandardDeviation() / Math.sqrt(n);
		return 0;
	}

	/**
	 * Add the statistics to the data.
	 *
	 * @param statistics
	 *            the statistics
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
	 *            the statistics
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
	 * @param c
	 *            the confidence level of the test (in the range 0-1)
	 * @return the confidence interval
	 * @throws IllegalArgumentException
	 *             if the confidence level is not in the range 0-1
	 * @see "https://en.wikipedia.org/wiki/Confidence_interval#Basic_steps"
	 */
	public double getConfidenceInterval(double c)
	{
		if (n < 2)
			return Double.POSITIVE_INFINITY;
		if (c < 0 || c > 1)
			throw new IllegalArgumentException("Confidence level must be in the range 0-1");
		final double se = getStandardError();
		final double alpha = 1 - (1 - c) * 0.5; // Two-sided, e.g. 0.95 -> 0.975
		final int degreesOfFreedom = n - 1;
		final TDistribution t = new TDistribution(degreesOfFreedom);
		return t.inverseCumulativeProbability(alpha) * se;
	}

	/**
	 * Reset the statistics.
	 */
	public void reset()
	{
		n = 0;
		s = 0;
		ss = 0;
	}
}
