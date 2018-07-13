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
package gdsc.core.utils;

/**
 * Simple class to calculate the min, max, mean and standard deviation of data.
 */
public class ExtendedStatistics extends Statistics
{
	/** The min. */
	private double min = Double.POSITIVE_INFINITY;

	/** The max. */
	private double max = Double.NEGATIVE_INFINITY;

	/**
	 * Instantiates a new extended statistics.
	 */
	public ExtendedStatistics()
	{
	}

	/**
	 * Instantiates a new extended statistics.
	 *
	 * @param data
	 *            the data
	 */
	public ExtendedStatistics(float[] data)
	{
		super(data);
	}

	/**
	 * Instantiates a new extended statistics.
	 *
	 * @param data
	 *            the data
	 */
	public ExtendedStatistics(double[] data)
	{
		super(data);
	}

	/**
	 * Instantiates a new extended statistics.
	 *
	 * @param data
	 *            the data
	 */
	public ExtendedStatistics(int[] data)
	{
		super(data);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.utils.Statistics#addInternal(float[], int, int)
	 */
	@Override
	protected void addInternal(float[] data, int from, int to)
	{
		if (n == 0 && from < to)
			min = max = data[from];
		for (int i = from; i < to; i++)
		{
			final double value = data[i];
			s += value;
			ss += value * value;
			updateMinMax(value);
		}
		n += (to - from);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.utils.Statistics#addInternal(double[], int, int)
	 */
	@Override
	protected void addInternal(double[] data, int from, int to)
	{
		if (n == 0 && from < to)
			min = max = data[from];
		for (int i = from; i < to; i++)
		{
			final double value = data[i];
			s += value;
			ss += value * value;
			updateMinMax(value);
		}
		n += (to - from);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.utils.Statistics#addInternal(int[], int, int)
	 */
	@Override
	protected void addInternal(int[] data, int from, int to)
	{
		if (n == 0 && from < to)
			min = max = data[from];
		for (int i = from; i < to; i++)
		{
			final double value = data[i];
			s += value;
			ss += value * value;
			updateMinMax(value);
		}
		n += (to - from);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.utils.Statistics#addInternal(double)
	 */
	@Override
	protected void addInternal(final double value)
	{
		if (n == 0)
			min = max = value;
		else
			updateMinMax(value);
		super.addInternal(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.utils.Statistics#addInternal(int, double)
	 */
	@Override
	protected void addInternal(int nA, double value)
	{
		if (n == 0)
			min = max = value;
		else
			updateMinMax(value);
		super.addInternal(nA, value);
	}

	/**
	 * Update the min and max.
	 * <p>
	 * This should only be called when the count is above zero (i.e. min/max have
	 * been set with a valid value).
	 *
	 * @param value
	 *            the value
	 */
	private void updateMinMax(final double value)
	{
		if (min > value)
			min = value;
		else if (max < value)
			max = value;
	}

	/**
	 * Gets the minimum. Returns {@link Double#NaN } if no data has been added.
	 *
	 * @return the minimum
	 */
	public double getMin()
	{
		return (n == 0) ? Double.NaN : min;
	}

	/**
	 * Gets the maximum. Returns {@link Double#NaN } if no data has been added.
	 *
	 * @return the maximum
	 */
	public double getMax()
	{
		return (n == 0) ? Double.NaN : max;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.utils.Statistics#add(gdsc.core.utils.Statistics)
	 */
	@Override
	public void add(Statistics statistics)
	{
		if (statistics instanceof ExtendedStatistics)
		{
			final ExtendedStatistics extra = (ExtendedStatistics) statistics;
			if (extra.n > 0)
			{
				n += statistics.n;
				s += statistics.s;
				ss += statistics.ss;
				if (min > extra.min)
					min = extra.min;
				if (max < extra.max)
					max = extra.max;
			}
			return;
		}
		throw new NotImplementedException("Not a ExtendedStatistics instance");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.utils.Statistics#reset()
	 */
	@Override
	public void reset()
	{
		super.reset();
		min = Double.POSITIVE_INFINITY;
		max = Double.NEGATIVE_INFINITY;
	}
}
