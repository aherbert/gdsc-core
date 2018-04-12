package gdsc.core.utils;

/*----------------------------------------------------------------------------- 
 * GDSC Plugins for ImageJ
 * 
 * Copyright (C) 2017 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

import org.apache.commons.math3.random.AbstractRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Contains a set of random numbers that are reused in sequence
 */
public class PseudoRandomGenerator extends AbstractRandomGenerator implements Cloneable
{
	protected final double[] sequence;
	protected final int length;

	private int position = 0;

	/**
	 * Instantiates a new pseudo random generator.
	 *
	 * @param sequence
	 *            the sequence (must contains numbers in the interval 0 to 1)
	 * @throw {@link IllegalArgumentException} if the sequence is not positive in length and contains numbers outside
	 *        the interval 0 to 1.
	 */
	public PseudoRandomGenerator(double[] sequence)
	{
		this(sequence, sequence.length);
	}

	/**
	 * Instantiates a new pseudo random generator.
	 *
	 * @param sequence
	 *            the sequence (must contains numbers in the interval 0 to 1)
	 * @param length
	 *            the length (if greater than sequence.length it is set to length)
	 * @throw {@link IllegalArgumentException} if the sequence is not positive in length and contains numbers outside
	 *        the interval 0 to 1.
	 */
	public PseudoRandomGenerator(double[] sequence, int length)
	{
		if (sequence == null || length < 1)
			throw new IllegalArgumentException("Sequence must have a positive length");
		if (length > sequence.length)
			length = sequence.length;
		for (int i = length; i-- > 0;)
			if (sequence[i] < 0 || sequence[i] > 1)
				throw new IllegalArgumentException("Sequence must contain numbers between 0 and 1 inclusive");
		this.sequence = sequence;
		this.length = length;
	}

	/**
	 * Instantiates a new pseudo random generator of the given size.
	 *
	 * @param size
	 *            the size
	 * @param source
	 *            the random source
	 * @throw {@link IllegalArgumentException} if the size is not positive
	 * @throw {@link NullPointerException} if the generator is null
	 */
	public PseudoRandomGenerator(int size, RandomGenerator source)
	{
		if (size < 1)
			throw new IllegalArgumentException("Sequence must have a positive length");
		if (source == null)
			throw new NullPointerException("Source generator must not be null");
		sequence = new double[size];
		while (size-- > 0)
		{
			sequence[size] = source.nextDouble();
		}
		length = size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.math3.random.AbstractRandomGenerator#setSeed(long)
	 */
	@Override
	public void setSeed(long seed)
	{
		clear();
		position = (int) (Math.abs(seed) % length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.math3.random.AbstractRandomGenerator#nextDouble()
	 */
	@Override
	public double nextDouble()
	{
		double d = sequence[position++];
		if (position == length)
			position = 0;
		return d;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public PseudoRandomGenerator clone()
	{
		try
		{
			return (PseudoRandomGenerator) super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			// This should not happen
			return new PseudoRandomGenerator(sequence, length);
		}
	}

	/**
	 * Gets a copy of the sequence of random numbers.
	 *
	 * @return the sequence
	 */
	public double[] getSequence()
	{
		return sequence.clone();
	}

	/**
	 * Gets the length of the sequence of random numbers.
	 *
	 * @return the length
	 */
	public int getLength()
	{
		return length;
	}

	/**
	 * Returns a pseudorandom, uniformly distributed {@code int} value
	 * between 0 (inclusive) and the specified value (exclusive), drawn from
	 * this random number generator's sequence.
	 * <p>
	 * The default implementation returns:
	 * 
	 * <pre>
	 * <code>(int) (nextDouble() * n</code>
	 * </pre>
	 * <p>
	 * Warning: No check is made that n is positive so use with caution.
	 *
	 * @param n
	 *            the bound on the random number to be returned. Must be
	 *            positive.
	 * @return a pseudorandom, uniformly distributed {@code int}
	 *         value between 0 (inclusive) and n (exclusive).
	 */
	public int nextIntFast(int n)
	{
		int result = (int) (nextDouble() * n);
		return result < n ? result : n - 1;
	}

	/**
	 * Perform a Fischer-Yates shuffle on the data.
	 *
	 * @param data
	 *            the data
	 */
	public void shuffle(int[] data)
	{
		for (int i = data.length; i-- > 1;)
		{
			int j = nextIntFast(i + 1);
			int tmp = data[i];
			data[i] = data[j];
			data[j] = tmp;
		}
	}
}