package gdsc.core.utils;

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
 * Simple class to calculate the mean and variance of arrayed data using a rolling algorithm.
 * <p>
 * Based on org.apache.commons.math3.stat.descriptive.moment.SecondMoment.
 */
public class RollingMoment
{
	private long n = 0;

	/** First moment of values that have been added */
	private double[] m1;

	/** Second moment of values that have been added */
	private double[] m2;

	/**
	 * Instantiates a new rolling moment with data.
	 */
	public RollingMoment()
	{
	}

	/**
	 * Instantiates a new rolling moment with data.
	 *
	 * @param data
	 *            the data
	 */
	public RollingMoment(double[] data)
	{
		add(data);
	}

	/**
	 * Instantiates a new rolling moment with data.
	 *
	 * @param data
	 *            the data
	 */
	public RollingMoment(float[] data)
	{
		add(data);
	}

	/**
	 * Instantiates a new rolling moment with data.
	 *
	 * @param data
	 *            the data
	 */
	public RollingMoment(int[] data)
	{
		add(data);
	}

	/**
	 * Add the data. The first set of data defines the number of individual moments to compute. All subsequent data must
	 * be the same size, e.g an array of length n will compute n first and second moments for the range 0<=i<n.
	 * <p>
	 * Note: If the user desires to maintain just a single moment then it advised to use the Apache class
	 * org.apache.commons.math3.stat.descriptive.moment.SecondMoment.
	 * <p>
	 * <li>For each additional value, update the first moment using:<br>
	 * <code>m = m + (new value - m) / (number of observations)</code></li>
	 * </ol>
	 * </p>
	 * <p>
	 * The following recursive updating formula is used for the second moment:
	 * </p>
	 * <p>
	 * Let
	 * <ul>
	 * <li>dev = (current obs - previous mean)</li>
	 * <li>n = number of observations (including current obs)</li>
	 * </ul>
	 * Then
	 * </p>
	 * <p>
	 * new value = old value + dev^2 * (n -1) / n.
	 * </p>
	 *
	 * @param data
	 *            the data
	 */
	public void add(double[] data)
	{
		if (n == 0)
		{
			n = 1;
			// Initialise the first moment to the input value
			m1 = data.clone();
			// Initialise sum-of-squared differences to zero
			m2 = new double[data.length];
		}
		else
		{
			final double n_1 = n;
			n++;
			final double n0 = n;
			for (int i = 0; i < data.length; i++)
			{
				final double dev = data[i] - m1[i];
				final double nDev = dev / n0;
				m1[i] += nDev;
				m2[i] += n_1 * dev * nDev;
			}
		}
	}

	/**
	 * Add the data. The first set of data defines the number of individual moments to compute. All subsequent data must
	 * be the same size, e.g an array of length n will compute n first and second moments for the range 0<=i<n.
	 * <p>
	 * Note: If the user desires to maintain just a single moment then it advised to use the Apache class
	 * org.apache.commons.math3.stat.descriptive.moment.SecondMoment.
	 * <p>
	 * <li>For each additional value, update the first moment using:<br>
	 * <code>m = m + (new value - m) / (number of observations)</code></li>
	 * </ol>
	 * </p>
	 * <p>
	 * The following recursive updating formula is used for the second moment:
	 * </p>
	 * <p>
	 * Let
	 * <ul>
	 * <li>dev = (current obs - previous mean)</li>
	 * <li>n = number of observations (including current obs)</li>
	 * </ul>
	 * Then
	 * </p>
	 * <p>
	 * new value = old value + dev^2 * (n -1) / n.
	 * </p>
	 *
	 * @param data
	 *            the data
	 */
	public void add(float[] data)
	{
		if (n == 0)
		{
			n = 1;
			// Initialise the first moment to the input value
			m1 = new double[data.length];
			for (int i = 0; i < data.length; i++)
				m1[i] = data[i];
			// Initialise sum-of-squared differences to zero
			m2 = new double[data.length];
		}
		else
		{
			final double n_1 = n;
			n++;
			final double n0 = n;
			for (int i = 0; i < data.length; i++)
			{
				final double dev = data[i] - m1[i];
				final double nDev = dev / n0;
				m1[i] += nDev;
				m2[i] += n_1 * dev * nDev;
			}
		}
	}

	/**
	 * Add the data. The first set of data defines the number of individual moments to compute. All subsequent data must
	 * be the same size, e.g an array of length n will compute n first and second moments for the range 0<=i<n.
	 * <p>
	 * Note: If the user desires to maintain just a single moment then it advised to use the Apache class
	 * org.apache.commons.math3.stat.descriptive.moment.SecondMoment.
	 * <p>
	 * <li>For each additional value, update the first moment using:<br>
	 * <code>m = m + (new value - m) / (number of observations)</code></li>
	 * </ol>
	 * </p>
	 * <p>
	 * The following recursive updating formula is used for the second moment:
	 * </p>
	 * <p>
	 * Let
	 * <ul>
	 * <li>dev = (current obs - previous mean)</li>
	 * <li>n = number of observations (including current obs)</li>
	 * </ul>
	 * Then
	 * </p>
	 * <p>
	 * new value = old value + dev^2 * (n -1) / n.
	 * </p>
	 *
	 * @param data
	 *            the data
	 */
	public void add(int[] data)
	{
		if (n == 0)
		{
			n = 1;
			// Initialise the first moment to the input value
			m1 = new double[data.length];
			for (int i = 0; i < data.length; i++)
				m1[i] = data[i];
			// Initialise sum-of-squared differences to zero
			m2 = new double[data.length];
		}
		else
		{
			final double n_1 = n;
			n++;
			final double n0 = n;
			for (int i = 0; i < data.length; i++)
			{
				final double dev = data[i] - m1[i];
				final double nDev = dev / n0;
				m1[i] += nDev;
				m2[i] += n_1 * dev * nDev;
			}
		}
	}

	/**
	 * Adds the.
	 *
	 * @param rollingMoment
	 *            the rolling moment
	 */
	public void add(RollingMoment rollingMoment)
	{
		if (rollingMoment.n == 0)
			return;

		final long nb = rollingMoment.n;
		final double[] m1b = rollingMoment.m1;
		final double[] m2b = rollingMoment.m2;

		if (n == 0)
		{
			// Copy
			this.n = nb;
			m1 = m1b.clone();
			m2 = m2b.clone();
			return;
		}

		if (m1b.length != m1.length)
			throw new IllegalArgumentException("Different number of moments");

		// Adapted from org.apache.commons.math3.stat.regression.SimpleRegression.append(SimpleRegression)
        final double f1 = nb / (double) (nb + n);
        final double f2 = n * nb / (double) (nb + n);
		for (int i = 0; i < m1.length; i++)
		{
	        final double dev = m1b[i] - m1[i];
	        m1[i] += dev * f1;
	        m2[i] += m2b[i] + dev * dev * f2;
		}
		n += nb;
	}

	/**
	 * Gets the first moment (the sample mean).
	 *
	 * @return the first moment (null if no data has been added)
	 */
	public double[] getFirstMoment()
	{
		return m1;
	}

	/**
	 * Gets the second moment (sum of squared deviations from the sample mean).
	 *
	 * @return the second moment (null if no data has been added)
	 */
	public double[] getSecondMoment()
	{
		return m2;
	}

	/**
	 * Gets the number of observations.
	 *
	 * @return the n
	 */
	public long getN()
	{
		return n;
	}

	/**
	 * Gets the unbiased estimate of the variance.
	 *
	 * @return the variance (null if no data has been added)
	 */
	public double[] getVariance()
	{
		if (m2 == null)
			return null;
		if (n == 1)
			return new double[m2.length];
		double[] v = m2.clone();
		final double n1 = n - 1;
		for (int i = 0; i < v.length; i++)
			v[i] /= n1;
		return v;
	}
}