package gdsc.core.data;

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
 * Provide data on 1-axis from an array of doubles.
 */
public class DoubleArrayValueProvider implements ValueProvider
{
	private final double[] val;

	/**
	 * Instantiates a new double array trivalue provider.
	 *
	 * @param val
	 *            the val
	 * @throws DataException
	 *             If the array is missing data
	 */
	public DoubleArrayValueProvider(double[] val) throws DataException
	{
		if (val.length == 0)
			throw new DataException("No data");
		this.val = val;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.ValueProvider#getLength()
	 */
	public int getLength()
	{
		return val.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.ValueProvider#get(int)
	 */
	public double get(int x)
	{
		return val[x];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.ValueProvider#get(int, double[])
	 */
	public void get(int x, double[] values)
	{
		values[0] = val[x - 1];
		values[1] = val[x];
		values[2] = val[x + 1];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.ValueProvider#toArray()
	 */
	public double[] toArray()
	{
		return val;
	}
}
