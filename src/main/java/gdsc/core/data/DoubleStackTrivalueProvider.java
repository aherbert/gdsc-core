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
 * Provide data on 3-axes from a stack of XY double data
 */
public class DoubleStackTrivalueProvider implements TrivalueProvider
{
	private final int maxx;
	private final int maxy;
	private final double[][] val;

	/**
	 * Instantiates a new double stack trivalue provider.
	 *
	 * @param val
	 *            the stack of values. Each array is packed in yx order.
	 * @param maxx
	 *            the length in the x-dimension
	 * @param maxy
	 *            the length in the y-dimension
	 * @throws DataException
	 *             If the stack is missing data
	 */
	public DoubleStackTrivalueProvider(double[][] val, int maxx, int maxy) throws DataException
	{
		if (val.length == 0)
			throw new DataException("No data");
		int size = maxx * maxy;
		for (int z = 0; z < val.length; z++)
		{
			if (size != val[z].length)
				throw new DataException("XY data must be length " + size);
		}
		this.val = val;
		this.maxx = maxx;
		this.maxy = maxy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.TrivalueProvider#getLengthX()
	 */
	public int getLengthX()
	{
		return maxx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.TrivalueProvider#getLengthY()
	 */
	public int getLengthY()
	{
		return maxy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.TrivalueProvider#getLengthZ()
	 */
	public int getLengthZ()
	{
		return val.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.TrivalueProvider#get(int, int, int)
	 */
	public double get(int x, int y, int z)
	{
		return val[z][getIndex(x, y)];
	}

	/**
	 * Gets the index of the point in the XY data.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @return the index
	 */
	public int getIndex(int x, int y)
	{
		return y * maxx + x;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.TrivalueProvider#getCube(int, int, int, double[][])
	 */
	public void get(int x, int y, int z, double[][][] values)
	{
		final int cXcY = getIndex(x, y);
		final int pXcY = cXcY - 1;
		final int nXcY = cXcY + 1;
		final int cXpY = cXcY - maxx;
		final int pXpY = cXpY - 1;
		final int nXpY = cXpY + 1;
		final int cXnY = cXcY + maxx;
		final int pXnY = cXnY - 1;
		final int nXnY = cXnY + 1;
		final int pZ = z - 1;
		final int cZ = z;
		final int nZ = z + 1;

		values[0][0][0] = val[pZ][pXpY];
		values[0][0][1] = val[cZ][pXpY];
		values[0][0][2] = val[nZ][pXpY];
		values[0][1][0] = val[pZ][pXcY];
		values[0][1][1] = val[cZ][pXcY];
		values[0][1][2] = val[nZ][pXcY];
		values[0][2][0] = val[pZ][pXnY];
		values[0][2][1] = val[cZ][pXnY];
		values[0][2][2] = val[nZ][pXnY];
		values[1][0][0] = val[pZ][cXpY];
		values[1][0][1] = val[cZ][cXpY];
		values[1][0][2] = val[nZ][cXpY];
		values[1][1][0] = val[pZ][cXcY];
		values[1][1][1] = val[cZ][cXcY];
		values[1][1][2] = val[nZ][cXcY];
		values[1][2][0] = val[pZ][cXnY];
		values[1][2][1] = val[cZ][cXnY];
		values[1][2][2] = val[nZ][cXnY];
		values[2][0][0] = val[pZ][nXpY];
		values[2][0][1] = val[cZ][nXpY];
		values[2][0][2] = val[nZ][nXpY];
		values[2][1][0] = val[pZ][nXcY];
		values[2][1][1] = val[cZ][nXcY];
		values[2][1][2] = val[nZ][nXcY];
		values[2][2][0] = val[pZ][nXnY];
		values[2][2][1] = val[cZ][nXnY];
		values[2][2][2] = val[nZ][nXnY];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.TrivalueProvider#toArray()
	 */
	public double[][][] toArray()
	{
		double[][][] xyz = new double[maxx][maxy][getLengthZ()];
		for (int z = 0; z < val.length; z++)
		{
			double[] data = val[z];
			for (int y = 0, i = 0; y < maxy; y++)
				for (int x = 0; x < maxx; x++, i++)
					xyz[x][y][z] = data[i];
		}
		return xyz;
	}
}