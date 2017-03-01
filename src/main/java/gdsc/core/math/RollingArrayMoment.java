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
 * Simple class to calculate the mean and variance of arrayed data using a rolling algorithm.
 * <p>
 * Based on org.apache.commons.math3.stat.descriptive.moment.SecondMoment.
 */
public class RollingArrayMoment implements ArrayMoment
{
	private long n = 0;

	/** First moment of values that have been added */
	private double[] m1;

	/** Second moment of values that have been added */
	private double[] m2;

	/**
	 * Instantiates a new array moment with data.
	 */
	public RollingArrayMoment()
	{
	}

	/**
	 * Instantiates a new array moment with data.
	 *
	 * @param data
	 *            the data
	 */
	public RollingArrayMoment(double[] data)
	{
		add(data);
	}

	/**
	 * Instantiates a new array moment with data.
	 *
	 * @param data
	 *            the data
	 */
	public RollingArrayMoment(float[] data)
	{
		add(data);
	}

	/**
	 * Instantiates a new array moment with data.
	 *
	 * @param data
	 *            the data
	 */
	public RollingArrayMoment(int[] data)
	{
		add(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.ArrayMoment#add(double)
	 */
	public void add(double data)
	{
		if (n == 0)
		{
			n = 1;
			// Initialise the first moment to the input value
			m1 = new double[] { data };
			// Initialise sum-of-squared differences to zero
			m2 = new double[1];
		}
		else
		{
			final double n_1 = n;
			n++;
			final double n0 = n;
			final double dev = data - m1[0];
			final double nDev = dev / n0;
			m1[0] += nDev;
			m2[0] += n_1 * dev * nDev;
		}
	}

	/**
	 * {@inheritDoc}
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
	 * {@inheritDoc}
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
	 * {@inheritDoc}
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
	 * Adds the data in the array moment.
	 *
	 * @param arrayMoment
	 *            the array moment
	 */
	public void add(RollingArrayMoment arrayMoment)
	{
		if (arrayMoment.n == 0)
			return;

		final long nb = arrayMoment.n;
		final double[] m1b = arrayMoment.m1;
		final double[] m2b = arrayMoment.m2;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.ArrayMoment#getFirstMoment()
	 */
	public double[] getFirstMoment()
	{
		return m1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.ArrayMoment#getSecondMoment()
	 */
	public double[] getSecondMoment()
	{
		return m2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.ArrayMoment#getN()
	 */
	public long getN()
	{
		return n;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.ArrayMoment#getVariance()
	 */
	public double[] getVariance()
	{
		return getVariance(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.ArrayMoment#getVariance(boolean)
	 */
	public double[] getVariance(boolean isBiasCorrected)
	{
		if (n == 0)
			return null;
		if (n == 1)
			return new double[m2.length];
		double[] v = m2.clone();
		final double n1 = (isBiasCorrected) ? n - 1 : n;
		for (int i = 0; i < v.length; i++)
			v[i] = positive(v[i] / n1);
		return v;
	}

	private static double positive(final double d)
	{
		return (d > 0) ? d : 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.ArrayMoment#getStandardDeviation()
	 */
	public double[] getStandardDeviation()
	{
		return getStandardDeviation(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.ArrayMoment#getStandardDeviation(boolean)
	 */
	public double[] getStandardDeviation(boolean isBiasCorrected)
	{
		if (n == 0)
			return null;
		if (n == 1)
			return new double[m2.length];
		double[] v = m2.clone();
		final double n1 = (isBiasCorrected) ? n - 1 : n;
		for (int i = 0; i < v.length; i++)
			v[i] = positiveSqrt(v[i] / n1);
		return v;
	}

	private static double positiveSqrt(final double d)
	{
		return (d > 0) ? Math.sqrt(d) : 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.ArrayMoment#newInstance()
	 */
	public RollingArrayMoment newInstance()
	{
		return new RollingArrayMoment();
	}
}