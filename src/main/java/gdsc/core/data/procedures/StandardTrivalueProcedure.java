package gdsc.core.data.procedures;

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
 * Standard implementation for accessing a value in three dimensions.
 */
public class StandardTrivalueProcedure implements TrivalueProcedure
{
	/** The x axis values. */
	public double[] x;

	/** The y axis values. */
	public double[] y;

	/** The z axis values. */
	public double[] z;

	/** The value. */
	public double[][][] value;

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.procedures.TrivalueProcedure#setDimensions(int, int, int)
	 */
	public boolean setDimensions(int maxx, int maxy, int maxz)
	{
		x = new double[maxx];
		y = new double[maxy];
		z = new double[maxz];
		value = new double[maxx][maxy][maxz];
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.procedures.TrivalueProcedure#setX(int, double)
	 */
	public boolean setX(int i, double value)
	{
		x[i] = value;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.procedures.TrivalueProcedure#setY(int, double)
	 */
	public boolean setY(int j, double value)
	{
		y[j] = value;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.procedures.TrivalueProcedure#setZ(int, double)
	 */
	public boolean setZ(int k, double value)
	{
		z[k] = value;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.procedures.TrivalueProcedure#setValue(int, int, int, double)
	 */
	public boolean setValue(int i, int j, int k, double value)
	{
		this.value[i][j][k] = value;
		return true;
	}
}
