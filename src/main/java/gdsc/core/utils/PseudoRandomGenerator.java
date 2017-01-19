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

/**
 * Contains a set of random numbers that are reused in sequence
 */
public class PseudoRandomGenerator extends AbstractRandomGenerator
{
	private final double[] sequence;

	private int position = 0;

	public PseudoRandomGenerator(double[] sequence)
	{
		this(sequence, true);
	}

	private PseudoRandomGenerator(double[] sequence, boolean check)
	{
		if (check)
		{
			if (sequence == null || sequence.length < 1)
				throw new IllegalArgumentException("Sequence must have a positive length");
			for (int i = sequence.length; i-- > 0;)
				if (sequence[i] < 0 || sequence[i] > 1)
					throw new IllegalArgumentException("Sequence must contain numbers between 0 and 1 inclusive");
		}
		this.sequence = sequence;
	}

	@Override
	public void setSeed(long seed)
	{
		position = (int) (seed % sequence.length);
	}

	@Override
	public double nextDouble()
	{
		double d = sequence[position++];
		if (position == sequence.length)
			position = 0;
		return d;
	}

	/**
	 * Copy the generator. The position in the sequence will be reset to zero for the copy.
	 *
	 * @return the pseudo random generator
	 */
	public PseudoRandomGenerator copy()
	{
		return new PseudoRandomGenerator(sequence.clone(), false);
	}
}