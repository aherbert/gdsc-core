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
 * Provide data on 2-axes from an array of doubles.
 */
public class DoubleArrayBivalueProvider implements BivalueProvider
{
	private final int maxx;
	private final int maxy;
	private final double[][] val;

	/**
	 * Instantiates a new double array trivalue provider.
	 *
	 * @param val
	 *            the val
	 * @throws DataException
	 *             If the array is missing data
	 */
	public DoubleArrayBivalueProvider(double[][] val) throws DataException
	{
		if (val.length == 0)
			throw new DataException("No X data");
		if (val[0].length == 0)
			throw new DataException("No Y data");
		this.val = val;
		maxx = val.length;
		maxy = val[0].length;
		for (int x = 0; x < maxx; x++)
		{
			if (maxy != val[x].length)
				throw new DataException("Y data must be the same length");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.BivalueProvider#getLengthX()
	 */
	public int getLengthX()
	{
		return maxx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.BivalueProvider#getLengthY()
	 */
	public int getLengthY()
	{
		return maxy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.BivalueProvider#get(int, int)
	 */
	public double get(int x, int y)
	{
		return val[x][y];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.BivalueProvider#get(int, int, double[][])
	 */
	public void get(int x, int y, double[][] values)
	{
		final int nX = x + 1;
		final int pX = x - 1;
		final int nY = y + 1;
		final int pY = y - 1;

		values[0][0] = val[pX][pY];
		values[0][1] = val[pX][y];
		values[0][2] = val[pX][nY];
		values[1][0] = val[x][pY];
		values[1][1] = val[x][y];
		values[1][2] = val[x][nY];
		values[2][0] = val[nX][pY];
		values[2][1] = val[nX][y];
		values[2][2] = val[nX][nY];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.BivalueProvider#toArray()
	 */
	public double[][] toArray()
	{
		return val;
	}
}
