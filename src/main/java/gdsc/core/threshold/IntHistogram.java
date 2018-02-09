package gdsc.core.threshold;

/*----------------------------------------------------------------------------- 
 * GDSC Plugins for ImageJ
 * 
 * Copyright (C) 2016 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

/**
 * Contains a histogram.
 * <p>
 * The histogram is implemented in this class using integer bin values starting from an offset
 */
public class IntHistogram extends Histogram
{
	/** The offset. */
	final int offset;

	/**
	 * Instantiates a new int histogram.
	 *
	 * @param h
	 *            the histogram counts
	 * @param minBin
	 *            the min bin
	 * @param maxBin
	 *            the max bin
	 * @param offset
	 *            the offset
	 */
	protected IntHistogram(int[] h, int minBin, int maxBin, int offset)
	{
		super(h, minBin, maxBin);
		this.offset = offset;
	}

	/**
	 * Instantiates a new int histogram.
	 *
	 * @param h
	 *            the histogram counts
	 * @param offset
	 *            the offset
	 */
	public IntHistogram(int[] h, int offset)
	{
		super(h);
		this.offset = offset;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.threshold.Histogram#getValue(int)
	 */
	public float getValue(int i)
	{
		return offset + i;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.threshold.Histogram#clone()
	 */
	@Override
	public IntHistogram clone()
	{
		return new IntHistogram(this.h.clone(), minBin, maxBin, offset);
	}
}