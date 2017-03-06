package gdsc.core.utils;

import java.util.Arrays;

/*----------------------------------------------------------------------------- 
 * GDSC SMLM Software
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
 * Random number generator.
 */
public class Random
{
	private static int IA = 16807, IM = 2147483647, IQ = 127773, IR = 2836, NTAB = 32;
	private static int NDIV = (1 + (IM - 1) / NTAB);
	private static double EPS = 3.0e-16;
	private static float AM = (float) (1.0 / (float) (IM));
	private static float RNMX = (float) (1.0 - EPS);

	private int idum;
	private int iy = 0;
	private int[] iv = new int[NTAB];
	private boolean haveNextNextGaussian = false;
	private double nextNextGaussian = 0;

	/**
	 * Default constructor
	 */
	public Random()
	{
		// Require an integer seed
		int seed = (int) (System.currentTimeMillis() & 0xffffffff);
		init(seed);
	}

	/**
	 * Constructor
	 * 
	 * @param seed
	 *            The seed to use for the random number generator
	 */
	public Random(int seed)
	{
		init(seed);
	}

	private void init(int seed)
	{
		idum = (seed > 0) ? -seed : seed;
	}

	/**
	 * Returns a random number between 0 (included) and 1 (excluded)
	 * 
	 * @return Random number
	 */
	public float next()
	{
		return next(RNMX);
	}

	/**
	 * Returns a random number between 0 (included) and 1 (optionally included).
	 *
	 * @param includeOne
	 *            set to true to return a value up to 1 inclusive. The default is 1 exclusive.
	 * @return Random number
	 */
	public float next(boolean includeOne)
	{
		if (includeOne)
			return next(1f);
		return next(RNMX);
	}

	/**
	 * Returns a random number between 0 (included) and max.
	 *
	 * @param max
	 *            the max
	 * @return Random number
	 */
	private float next(float max)
	{
		int j, k;
		float temp;

		if (idum <= 0 || iy == 0)
		{
			if (-idum < 1)
				idum = 1;
			else
				idum = -idum;
			for (j = NTAB + 8; j-- > 0;)
			{
				k = idum / IQ;
				idum = IA * (idum - k * IQ) - IR * k;
				if (idum < 0)
					idum += IM;
				if (j < NTAB)
					iv[j] = idum;
			}
			iy = iv[0];
		}
		k = idum / IQ;
		idum = IA * (idum - k * IQ) - IR * k;
		if (idum < 0)
			idum += IM;
		j = iy / NDIV;
		iy = iv[j];
		iv[j] = idum;
		return ((temp = AM * iy) > max) ? max : temp;
	}

	/**
	 * Returns a random number from a Gaussian distribution with mean 0 and standard deviation 1
	 * 
	 * @return Random number
	 */
	public double nextGaussian()
	{
		if (haveNextNextGaussian)
		{
			haveNextNextGaussian = false;
			return nextNextGaussian;
		}
		else
		{
			double v1, v2, s;
			do
			{
				v1 = 2 * next() - 1; // between -1.0 and 1.0
				v2 = 2 * next() - 1; // between -1.0 and 1.0
				s = v1 * v1 + v2 * v2;
			} while (s >= 1 || s == 0);
			double multiplier = StrictMath.sqrt(-2 * StrictMath.log(s) / s);
			nextNextGaussian = v2 * multiplier;
			haveNextNextGaussian = true;
			return v1 * multiplier;
		}
	}

	/**
	 * Perform a Fisher-Yates shuffle on the data
	 * 
	 * @param data
	 *            the data
	 */
	public void shuffle(double[] data)
	{
		for (int i = data.length; i-- > 1;)
		{
			int j = (int) (next() * (i + 1));
			//if (i != j)  // This comparison is probably not worth it
			//{
			double tmp = data[i];
			data[i] = data[j];
			data[j] = tmp;
			//}
		}
	}

	/**
	 * Perform a Fisher-Yates shuffle on the data
	 * 
	 * @param data
	 *            the data
	 */
	public void shuffle(float[] data)
	{
		for (int i = data.length; i-- > 1;)
		{
			int j = (int) (next() * (i + 1));
			//if (i != j)  // This comparison is probably not worth it
			//{
			float tmp = data[i];
			data[i] = data[j];
			data[j] = tmp;
			//}
		}
	}

	/**
	 * Perform a Fisher-Yates shuffle on the data
	 * 
	 * @param data
	 *            the data
	 */
	public void shuffle(int[] data)
	{
		for (int i = data.length; i-- > 1;)
		{
			int j = (int) (next() * (i + 1));
			//if (i != j)  // This comparison is probably not worth it
			//{
			int tmp = data[i];
			data[i] = data[j];
			data[j] = tmp;
			//}
		}
	}

	/**
	 * Sample k objects without replacement from n objects. This is done using an in-line Fisher-Yates shuffle on an
	 * array of length n for the first k target indices.
	 * <p>
	 * Note: Returns an empty array if n or k are less than 1. Returns an ascending array of indices if k is equal or
	 * bigger than n.
	 *
	 * @param k
	 *            the k
	 * @param n
	 *            the n
	 * @return the sample
	 */
	public int[] sample(final int k, final int n)
	{
		// Avoid stupidity
		if (n < 1 || k < 1)
			return new int[0];

		// Create a range of data to sample
		final int[] data = new int[n];
		for (int i = 1; i < n; i++)
			data[i] = i;

		if (k >= n)
			// No sub-sample needed
			return data;

		// If k>n/2 then we can sample (n-k) and then construct the result 
		// by removing the selection from the original range.
		if (k > n / 2)
		{
			final int[] sample = inlineSelection(data.clone(), n - k);
			// Flag for removal
			for (int i = 0; i < sample.length; i++)
				data[sample[i]] = -1;
			// Remove from original series
			int c = 0;
			for (int i = 0; i < n; i++)
			{
				if (data[i] == -1)
					continue;
				data[c++] = data[i];
			}
			return Arrays.copyOf(data, c);
		}

		return inlineSelection(data, k);
	}

	private int[] inlineSelection(final int[] data, int k)
	{
		// Do an in-line Fisher-Yates shuffle into a result array
		final int[] result = new int[k];
		for (int i = data.length - 1; k-- > 0; i--)
		{
			int j = (int) (next() * (i + 1));
			// We don't need result[i] anymore so write direct to the result array 
			result[k] = data[j];
			data[j] = data[i];
		}
		return result;
	}

	/**
	 * Generate a random integer
	 *
	 * @return the integer
	 */
	public int nextInt()
	{
		return (int) ((2d * next(true) - 1d) * Integer.MAX_VALUE);
	}

	/**
	 * Generate a random integer between zero (included) and upper (excluded)
	 *
	 * @param upper
	 *            the upper
	 * @return the integer
	 */
	public int nextInt(int upper)
	{
		return (int) (next() * upper);
	}

	/**
	 * Generate a random integer between lower and upper (end points included)
	 *
	 * @param lower
	 *            the lower
	 * @param upper
	 *            the upper
	 * @return the integer
	 */
	public int nextInt(int lower, int upper)
	{
		final int max = (upper - lower) + 1;
		if (max <= 0)
		{
			// The range is too wide to fit in a positive int (larger
			// than 2^31); as it covers more than half the integer range,
			// we use a simple rejection method.
			while (true)
			{
				final int r = nextInt();
				if (r >= lower && r <= upper)
				{
					return r;
				}
			}
		}
		else
		{
			// We can shift the range and directly generate a positive int.
			return lower + nextInt(max);
		}
	}
}
