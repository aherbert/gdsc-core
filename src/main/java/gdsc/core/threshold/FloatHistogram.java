package gdsc.core.threshold;

import gdsc.core.threshold.AutoThreshold.Method;

// TODO: Auto-generated Javadoc
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
 * The histogram is implemented in this class using float bin values.
 */
public class FloatHistogram extends Histogram
{
	/** The histogram bin values. */
	public final float[] value;

	/**
	 * Instantiates a new float histogram.
	 *
	 * @param h
	 *            the histogram
	 * @param value
	 *            the bin values
	 * @param minBin
	 *            the min bin
	 * @param maxBin
	 *            the max bin
	 */
	private FloatHistogram(int[] h, float[] value, int minBin, int maxBin)
	{
		super(h, minBin, maxBin);
		this.value = value;
	}

	/**
	 * Instantiates a new float histogram.
	 *
	 * @param values
	 *            the bin values
	 * @param h
	 *            the histogram bin counts
	 */
	public FloatHistogram(float[] value, int[] h)
	{
		super(h);
		this.value = value;
	}

	/**
	 * Compact the current float histogram into a histogram with the specified number of bins. The returned histogram
	 * may be an integer histogram (if the data is integer and fits within the bin range) or a float histogram.
	 * The returned histogram has evenly spaced bin widths.
	 *
	 * @param size
	 *            the size
	 * @return the new histogram
	 * @see gdsc.core.threshold.Histogram#compact(int)
	 */
	@Override
	public Histogram compact(int size)
	{
		if (minBin == maxBin)
			return this;
		final float min = getValue(minBin);
		final float max = getValue(maxBin);

		if ((int) min == min && (int) max == max && (max - min) < size)
		{
			// Check if we can convert to integer histogram
			if (integerData())
				return integerHistogram(size);
		}

		// Compress non-integer data
		final int size_1 = size - 1;
		final float binSize = (max - min) / size_1;
		final int[] newH = new int[size];
		for (int i = 0; i < h.length; i++)
		{
			int bin = (int) ((getValue(i) - min) / binSize + 0.5);
			if (bin < 0)
				bin = 0;
			if (bin >= size)
				bin = size_1;
			newH[bin] += h[i];
		}
		// Create the new values
		final float[] newValue = new float[size];
		for (int i = 0; i < size; i++)
			newValue[i] = min + i * binSize;
		return new FloatHistogram(newValue, newH);
	}

	/**
	 * Check if the values are integer.
	 *
	 * @return true, if successful
	 */
	private boolean integerData()
	{
		for (float f : value)
			if ((int) f != f)
				return false;
		return true;
	}

	/**
	 * Return a new Integer histogram using the current data.
	 *
	 * @param size
	 *            the size
	 * @return the histogram
	 */
	private Histogram integerHistogram(int size)
	{
		final float min = getValue(minBin);
		int offset = 0;
		if (min < 0)
		{
			// build with offset
			offset = (int) min;
		}

		// No need to check size since this has been done already
		int[] h = new int[size];
		for (int i = 0; i < value.length; i++)
			h[(int) value[i] - offset] += this.h[i];

		if (offset != 0)
			return new IntHistogram(h, offset);

		return new Histogram(h);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.threshold.Histogram#getValue(int)
	 */
	public float getValue(int i)
	{
		return value[i];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.threshold.Histogram#clone()
	 */
	@Override
	public FloatHistogram clone()
	{
		return new FloatHistogram(this.h.clone(), this.value.clone(), minBin, maxBin);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Compacts the histogram to evenly spaced bin widths and then runs the threshold method. If the compaction does not
	 * work (e.g. if minBin == maxBin) then thresholding is not possible and -Infinity is returned.
	 * 
	 * @see gdsc.core.threshold.Histogram#getThreshold(gdsc.core.threshold.AutoThreshold.Method)
	 */
	@Override
	public float getThreshold(Method method)
	{
		return getThreshold(method, 4096);
	}

	/**
	 * Compacts the histogram to evenly spaced bin widths and then runs the threshold method. If the compaction does not
	 * work (e.g. if minBin == maxBin) then thresholding is not possible and -Infinity is returned.
	 *
	 * @param method
	 *            the method
	 * @param bins
	 *            the number of bins
	 * @return the threshold
	 */
	public float getThreshold(Method method, int bins)
	{
		// Convert to a histogram with even bin widths
		Histogram histogram = this.compact(bins);
		if (histogram == this)
			// Cannot compact
			return Float.NEGATIVE_INFINITY;
		// Call the auto threshold method directly to avoid infinite recursion if this a float histogram
		return histogram.getAutoThreshold(method);
	}
}