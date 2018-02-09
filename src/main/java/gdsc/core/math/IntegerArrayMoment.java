package gdsc.core.math;

import gdsc.core.data.IntegerType;
import gdsc.core.utils.NotImplementedException;

/*----------------------------------------------------------------------------- 
 * GDSC Software
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
 * Simple class to calculate the mean and variance of arrayed integer data using a fast summation algorithm that tracks
 * the sum of input values and the sum of squared input values. This may not be suitable for a large series of data
 * where the mean is far from zero due to floating point round-off error.
 * <p>
 * Only supports integer data types (byte, short, integer). A NotImplementedException is thrown for floating point data.
 * <p>
 * This class is only suitable if the user is assured that overflow of Long.MAX_VALUE will not occur. The sum
 * accumulates the input value squared. So each datum can have a value of (2^b)^2 where b is the bit-depth of the
 * integer data. A check can be made for potential overflow if the bit-depth and number of inputs is known.
 */
public class IntegerArrayMoment implements ArrayMoment
{

	/** The n. */
	private long n = 0;

	/** The sum of values that have been added. */
	private long[] s;

	/** The sum of squared values that have been added. */
	private long[] ss;

	/**
	 * Instantiates a new array moment with data.
	 */
	public IntegerArrayMoment()
	{
	}

	/**
	 * Instantiates a new array moment with data.
	 *
	 * @param data
	 *            the data
	 */
	public IntegerArrayMoment(byte[] data)
	{
		add(data);
	}

	/**
	 * Instantiates a new array moment with data.
	 *
	 * @param data
	 *            the data
	 */
	public IntegerArrayMoment(short[] data)
	{
		add(data);
	}

	/**
	 * Instantiates a new array moment with data.
	 *
	 * @param data
	 *            the data
	 */
	public IntegerArrayMoment(int[] data)
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
		throw new NotImplementedException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.ArrayMoment#add(double[])
	 */
	public void add(double[] data)
	{
		throw new NotImplementedException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.ArrayMoment#add(float[])
	 */
	public void add(float[] data)
	{
		throw new NotImplementedException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.ArrayMoment#add(int[])
	 */
	public void add(int[] data)
	{
		if (n == 0)
		{
			// Initialise the array lengths
			s = new long[data.length];
			ss = new long[data.length];
		}
		n++;
		for (int i = 0; i < data.length; i++)
		{
			s[i] += data[i];
			ss[i] += (long) data[i] * data[i];
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.ArrayMoment#add(short[])
	 */
	public void add(short[] data)
	{
		if (n == 0)
		{
			// Initialise the array lengths
			s = new long[data.length];
			ss = new long[data.length];
		}
		n++;
		for (int i = 0; i < data.length; i++)
		{
			s[i] += data[i];
			ss[i] += (long) data[i] * data[i];
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.ArrayMoment#add(byte[])
	 */
	public void add(byte[] data)
	{
		if (n == 0)
		{
			// Initialise the array lengths
			s = new long[data.length];
			ss = new long[data.length];
		}
		n++;
		for (int i = 0; i < data.length; i++)
		{
			s[i] += data[i];
			ss[i] += (long) data[i] * data[i];
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.ArrayMoment#addUnsigned(short[])
	 */
	public void addUnsigned(short[] data)
	{
		if (n == 0)
		{
			// Initialise the array lengths
			s = new long[data.length];
			ss = new long[data.length];
		}
		n++;
		//long t = System.nanoTime();
		for (int i = 0; i < data.length; i++)
		{
			long v = data[i] & 0xffff;
			s[i] += v;
			ss[i] += v * v;
		}
		//System.out.printf("Analysis Time = %f ms\n", (System.nanoTime()-t)/1e6);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.ArrayMoment#addUnsigned(byte[])
	 */
	public void addUnsigned(byte[] data)
	{
		if (n == 0)
		{
			// Initialise the array lengths
			s = new long[data.length];
			ss = new long[data.length];
		}
		n++;
		for (int i = 0; i < data.length; i++)
		{
			long v = data[i] & 0xff;
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
	public void add(IntegerArrayMoment arrayMoment)
	{
		if (arrayMoment.n == 0)
			return;

		final long nb = arrayMoment.n;
		final long[] sb = arrayMoment.s;
		final long[] ssb = arrayMoment.ss;

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
			s[i] = add(s[i], sb[i]);
			ss[i] = add(ss[i], ssb[i]);
		}
	}

	/**
	 * Adds the.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the long
	 */
	private static long add(long a, long b)
	{
		long c = a + b;
		if (c < 0)
			throw new IllegalStateException(String.format("Adding the moments results in overflow: %d + %d", a, b));
		return c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.ArrayMoment#getFirstMoment()
	 */
	public double[] getFirstMoment()
	{
		if (n == 0)
			return null;
		double[] m1 = new double[s.length];
		final double n = this.n;
		for (int i = 0; i < s.length; i++)
			m1[i] = s[i] / n;
		return m1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.ArrayMoment#getSecondMoment()
	 */
	public double[] getSecondMoment()
	{
		if (n == 0)
			return null;
		double[] m2 = new double[s.length];
		for (int i = 0; i < s.length; i++)
		{
			m2[i] = ss[i] - ((double) s[i] * s[i]) / n;
		}
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
			return new double[s.length];
		double[] v = getSecondMoment();
		final double n1 = (isBiasCorrected) ? n - 1 : n;
		for (int i = 0; i < v.length; i++)
			v[i] = positive(v[i] / n1);
		return v;
	}

	/**
	 * Positive.
	 *
	 * @param d
	 *            the d
	 * @return the double
	 */
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
			return new double[s.length];
		double[] v = getSecondMoment();
		final double n1 = (isBiasCorrected) ? n - 1 : n;
		for (int i = 0; i < v.length; i++)
			v[i] = positiveSqrt(v[i] / n1);
		return v;
	}

	/**
	 * Positive sqrt.
	 *
	 * @param d
	 *            the d
	 * @return the double
	 */
	private static double positiveSqrt(final double d)
	{
		return (d > 0) ? Math.sqrt(d) : 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.ArrayMoment#newInstance()
	 */
	public IntegerArrayMoment newInstance()
	{
		return new IntegerArrayMoment();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.math.ArrayMoment#add(gdsc.core.math.ArrayMoment)
	 */
	public void add(ArrayMoment arrayMoment)
	{
		if (arrayMoment == null)
			throw new NullPointerException();
		if (arrayMoment instanceof IntegerArrayMoment)
			add((IntegerArrayMoment) arrayMoment);
		else
			throw new IllegalArgumentException("Not compatible: " + arrayMoment.getClass());
	}

	/**
	 * Checks if it is valid to use this class for the expected data size.
	 * <p>
	 * This class is only suitable if the user is assured that overflow of Long.MAX_VALUE will not occur. The sum
	 * accumulates the input value squared. So each datum can have a value of (2^b)^2 where b is the bit-depth of the
	 * integer data. A check can be made for potential overflow if the bit-depth and number of inputs is known.
	 *
	 * @param integerType
	 *            the integer type
	 * @param size
	 *            the number of inputs
	 * @return true, if is valid
	 */
	public static boolean isValid(IntegerType integerType, int size)
	{
		long max = integerType.getAbsoluteMax();
		long l2 = max * max;
		if (l2 < 0)
			return false;
		if (l2 * (double) size > Long.MAX_VALUE)
			return false;
		return true;
	}
}