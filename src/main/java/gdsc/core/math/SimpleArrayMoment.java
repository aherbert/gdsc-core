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
package gdsc.core.math;

/**
 * Simple class to calculate the mean and variance of arrayed data using a fast summation algorithm that tracks the sum
 * of input values and the sum of squared input values. This may not be suitable for a large series of data where the
 * mean is far from zero due to floating point round-off error.
 */
public class SimpleArrayMoment implements ArrayMoment
{
	private long n = 0;

	/** The sum of values that have been added */
	private double[] s;

	/** The sum of squared values that have been added */
	private double[] ss;

	/**
	 * Instantiates a new array moment with data.
	 */
	public SimpleArrayMoment()
	{
	}

	/**
	 * Instantiates a new array moment with data.
	 *
	 * @param data
	 *            the data
	 */
	public SimpleArrayMoment(double[] data)
	{
		add(data);
	}

	/**
	 * Instantiates a new array moment with data.
	 *
	 * @param data
	 *            the data
	 */
	public SimpleArrayMoment(float[] data)
	{
		add(data);
	}

	/**
	 * Instantiates a new array moment with data.
	 *
	 * @param data
	 *            the data
	 */
	public SimpleArrayMoment(int[] data)
	{
		add(data);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.ArrayMoment#add(double)
	 */
	@Override
	public void add(double data)
	{
		if (n == 0)
		{
			// Initialise the array lengths
			s = new double[1];
			ss = new double[1];
		}
		n++;
		s[0] += data;
		ss[0] += data * data;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.ArrayMoment#add(double[])
	 */
	@Override
	public void add(double[] data)
	{
		if (n == 0)
		{
			// Initialise the array lengths
			s = new double[data.length];
			ss = new double[data.length];
		}
		n++;
		for (int i = 0; i < data.length; i++)
		{
			s[i] += data[i];
			ss[i] += data[i] * data[i];
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.ArrayMoment#add(float[])
	 */
	@Override
	public void add(float[] data)
	{
		if (n == 0)
		{
			// Initialise the array lengths
			s = new double[data.length];
			ss = new double[data.length];
		}
		n++;
		for (int i = 0; i < data.length; i++)
		{
			s[i] += data[i];
			ss[i] += (double) data[i] * data[i];
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.ArrayMoment#add(int[])
	 */
	@Override
	public void add(int[] data)
	{
		if (n == 0)
		{
			// Initialise the array lengths
			s = new double[data.length];
			ss = new double[data.length];
		}
		n++;
		for (int i = 0; i < data.length; i++)
		{
			s[i] += data[i];
			ss[i] += (double) data[i] * data[i];
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.ArrayMoment#add(short[])
	 */
	@Override
	public void add(short[] data)
	{
		if (n == 0)
		{
			// Initialise the array lengths
			s = new double[data.length];
			ss = new double[data.length];
		}
		n++;
		for (int i = 0; i < data.length; i++)
		{
			s[i] += data[i];
			ss[i] += (double) data[i] * data[i];
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.ArrayMoment#add(byte[])
	 */
	@Override
	public void add(byte[] data)
	{
		if (n == 0)
		{
			// Initialise the array lengths
			s = new double[data.length];
			ss = new double[data.length];
		}
		n++;
		for (int i = 0; i < data.length; i++)
		{
			s[i] += data[i];
			ss[i] += (double) data[i] * data[i];
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.ArrayMoment#addUnsigned(short[])
	 */
	@Override
	public void addUnsigned(short[] data)
	{
		if (n == 0)
		{
			// Initialise the array lengths
			s = new double[data.length];
			ss = new double[data.length];
		}
		n++;
		for (int i = 0; i < data.length; i++)
		{
			final double v = data[i] & 0xffff;
			s[i] += v;
			ss[i] += v * v;
		}
	}

	@Override
	public void addUnsigned(byte[] data)
	{
		if (n == 0)
		{
			// Initialise the array lengths
			s = new double[data.length];
			ss = new double[data.length];
		}
		n++;
		for (int i = 0; i < data.length; i++)
		{
			final double v = data[i] & 0xff;
			s[i] += v;
			ss[i] += v * v;
		}
	}

	/**
	 * Adds the data in the array moment.
	 *
	 * @param arrayMoment
	 *            the array moment
	 */
	public void add(SimpleArrayMoment arrayMoment)
	{
		if (arrayMoment.n == 0)
			return;

		final long nb = arrayMoment.n;
		final double[] sb = arrayMoment.s;
		final double[] ssb = arrayMoment.ss;

		if (n == 0)
		{
			// Copy
			n = nb;
			s = sb.clone();
			ss = ssb.clone();
			return;
		}

		if (sb.length != s.length)
			throw new IllegalArgumentException("Different number of moments: " + sb.length + " != " + s.length);

		n += nb;
		for (int i = 0; i < s.length; i++)
		{
			s[i] += sb[i];
			ss[i] += ssb[i];
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.ArrayMoment#getFirstMoment()
	 */
	@Override
	public double[] getFirstMoment()
	{
		if (n == 0)
			return null;
		final double[] m1 = s.clone();
		final double n = this.n;
		for (int i = 0; i < s.length; i++)
			m1[i] /= n;
		return m1;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.ArrayMoment#getSecondMoment()
	 */
	@Override
	public double[] getSecondMoment()
	{
		if (n == 0)
			return null;
		final double[] m2 = new double[s.length];
		final double n = this.n;
		for (int i = 0; i < s.length; i++)
			m2[i] = ss[i] - (s[i] * s[i]) / n;
		return m2;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.ArrayMoment#getN()
	 */
	@Override
	public long getN()
	{
		return n;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.ArrayMoment#getVariance()
	 */
	@Override
	public double[] getVariance()
	{
		return getVariance(true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.ArrayMoment#getVariance(boolean)
	 */
	@Override
	public double[] getVariance(boolean isBiasCorrected)
	{
		if (n == 0)
			return null;
		if (n == 1)
			return new double[s.length];
		final double[] v = getSecondMoment();
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
	@Override
	public double[] getStandardDeviation()
	{
		return getStandardDeviation(true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.ArrayMoment#getStandardDeviation(boolean)
	 */
	@Override
	public double[] getStandardDeviation(boolean isBiasCorrected)
	{
		if (n == 0)
			return null;
		if (n == 1)
			return new double[s.length];
		final double[] v = getSecondMoment();
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
	@Override
	public SimpleArrayMoment newInstance()
	{
		return new SimpleArrayMoment();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.math.ArrayMoment#add(gdsc.core.math.ArrayMoment)
	 */
	@Override
	public void add(ArrayMoment arrayMoment)
	{
		if (arrayMoment == null)
			throw new NullPointerException();
		if (arrayMoment instanceof SimpleArrayMoment)
			add((SimpleArrayMoment) arrayMoment);
		else
			throw new IllegalArgumentException("Not compatible: " + arrayMoment.getClass());
	}
}
