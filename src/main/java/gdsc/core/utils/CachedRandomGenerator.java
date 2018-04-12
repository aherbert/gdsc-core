package gdsc.core.utils;

/*----------------------------------------------------------------------------- 
 * GDSC Plugins for ImageJ
 * 
 * Copyright (C) 2018 Alex Herbert
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
 * Caches random numbers
 */
public class CachedRandomGenerator extends AbstractRandomGenerator
{
	/**
	 * Class to allow ignoring data when the capacity is full
	 */
	private final static class NullStoredData extends StoredData
	{
		@Override
		public void add(double value)
		{
			// Ignore
		}
	}

	private final static NullStoredData NULL_STORE = new NullStoredData();

	protected final StoredData sequence;
	protected StoredData store;
	protected final RandomGenerator source;
	protected int pos = 0;

	/**
	 * Instantiates a new cached random generator.
	 *
	 * @param source
	 *            the random source
	 * @throw {@link NullPointerException} if the generator is null
	 */
	public CachedRandomGenerator(RandomGenerator source)
	{
		this(100, source);
	}

	/**
	 * Instantiates a new cached random generator of the given size.
	 *
	 * @param size
	 *            the size
	 * @param source
	 *            the random source
	 * @throw {@link NullPointerException} if the generator is null
	 */
	public CachedRandomGenerator(int size, RandomGenerator source)
	{
		if (source == null)
			throw new NullPointerException("Source generator must not be null");
		sequence = new StoredData(Math.max(0, size));
		store = sequence;
		this.source = source;
	}

	/**
	 * Set the seed in the source random generator. This may not have the expected result of resetting the random
	 * numbers if the current position is behind the sequence. To ensure a new set of number is generated with the seed
	 * also call {@link #clearCache()}.
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.apache.commons.math3.random.AbstractRandomGenerator#setSeed(long)
	 * @see #reset()
	 * @see #clearCache()
	 */
	@Override
	public void setSeed(long seed)
	{
		source.setSeed(seed);
	}

	/**
	 * Reset the current position in the cached sequence. Any cached random numbers will be reused before new numbers
	 * are generated.
	 */
	public void reset()
	{
		pos = 0;
	}

	/**
	 * Clear the cached sequence.
	 */
	public void clearCache()
	{
		pos = 0;
		sequence.clear();
		store = sequence;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.math3.random.AbstractRandomGenerator#nextDouble()
	 */
	@Override
	public double nextDouble()
	{
		double d;
		if (pos < sequence.size())
		{
			d = sequence.getValue(pos);
		}
		else
		{
			d = source.nextDouble();
			try
			{
				store.add(d);
			}
			catch (NegativeArraySizeException e)
			{
				// No more capacity
				store = NULL_STORE;
			}
		}
		// Safe increment of position to avoid overflow
		if (pos != Integer.MAX_VALUE)
			pos++;
		return d;
	}

	/**
	 * Gets the sequence of random numbers.
	 *
	 * @return the sequence
	 */
	public double[] getSequence()
	{
		return sequence.getValues();
	}

	/**
	 * Gets the length of the sequence of random numbers.
	 *
	 * @return the length
	 */
	public int getLength()
	{
		return sequence.size();
	}

	/**
	 * Gets the pseudo random generator using the current sequence.
	 *
	 * @return the pseudo random generator
	 */
	public PseudoRandomGenerator getPseudoRandomGenerator()
	{
		return new PseudoRandomGenerator(getSequence());
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

}