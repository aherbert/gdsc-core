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
package gdsc.core.threshold;

import gdsc.core.threshold.AutoThreshold.Method;
import gdsc.core.utils.Maths;

/**
 * Contains a histogram.
 * <p>
 * The histogram is implemented in this class using integer bin values starting from 0 (i.e. the
 * histogram constructed from an unsigned integer set of data, e.g. an 8-bit image)
 */
public class Histogram implements Cloneable
{
	/** The histogram counts */
	public final int[] h;

	/** The minimum bin than has a value above zero. */
	public final int minBin;

	/** The maximum bin than has a value above zero. */
	public final int maxBin;

	/**
	 * Instantiates a new histogram.
	 *
	 * @param h
	 *            the histogram
	 * @param minBin
	 *            the min bin
	 * @param maxBin
	 *            the max bin
	 */
	protected Histogram(int[] h, int minBin, int maxBin)
	{
		this.h = h;
		this.minBin = minBin;
		this.maxBin = maxBin;
	}

	/**
	 * Create a new histogram object with the specified histogram. Bin values are assumed to be an integer series
	 * starting from 0.
	 *
	 * @param h
	 *            The histogram
	 */
	public Histogram(int[] h)
	{
		// Find min and max bins
		int min = 0;
		int max = h.length - 1;
		while ((h[min] == 0) && (min < max))
			min++;
		while ((h[max] == 0) && (max > min))
			max--;
		minBin = min;
		maxBin = max;
		this.h = h;
	}

	/**
	 * Build a histogram using the input data values.
	 *
	 * @param data
	 *            The data
	 * @return The histogram
	 */
	public static Histogram buildHistogram(int[] data)
	{
		final int[] limits = Maths.limits(data);
		// Limits will be [0,0] if data is null or empty so the rest of the code is OK
		final int min = limits[0];
		final int max = limits[1];
		final int size = max - min + 1;
		final int[] h = new int[size];
		for (final int i : data)
			h[i - min]++;
		return new IntHistogram(h, min);
	}

	/**
	 * Compact the histogram to the specified number of bins. This is a method to be overridden by sub-classes.
	 * <p>
	 * Compaction is not supported in this class since the histogram is an integer histogram.
	 *
	 * @param size
	 *            the size
	 * @return this (since compaction is not supported)
	 */
	public Histogram compact(int size)
	{
		// Ignore
		return this;
	}

	/**
	 * Gets the value of the histogram for the given bin.
	 *
	 * @param bin
	 *            the bin
	 * @return the value
	 */
	public float getValue(int bin)
	{
		return bin;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Histogram clone()
	{
		return new Histogram(this.h.clone(), minBin, maxBin);
	}

	/**
	 * Gets the threshold.
	 *
	 * @param method
	 *            the method
	 * @return the threshold
	 */
	public float getThreshold(Method method)
	{
		return getAutoThreshold(method);
	}

	/**
	 * Gets the threshold. Assumes the histogram bin widths are equal.
	 *
	 * @param method
	 *            the method
	 * @return the threshold
	 */
	public final float getAutoThreshold(Method method)
	{
		final int[] statsHistogram;

		// Truncate if possible
		final int size = maxBin - minBin + 1;
		if (size < h.length)
		{
			statsHistogram = new int[size];
			System.arraycopy(h, minBin, statsHistogram, 0, size);
		}
		else
			statsHistogram = h;

		final int t = AutoThreshold.getThreshold(method, statsHistogram);

		// Convert back to an image value
		return getValue(t + minBin);
	}
}
