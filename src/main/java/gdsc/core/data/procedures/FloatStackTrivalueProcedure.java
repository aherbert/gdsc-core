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
 * Custom implementation for accessing a value in three dimensions using a stack of float data
 */
public class FloatStackTrivalueProcedure implements TrivalueProcedure
{
	/** The x axis values. */
	public double[] x;

	/** The y axis values. */
	public double[] y;

	/** The z axis values. */
	public double[] z;

	/** The value. This is a stack of z slices of XY data packed in YZ order */
	public float[][] value;

	private int maxx;

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
		value = new float[maxz][maxx * maxy];
		this.maxx = maxx;
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
		this.value[k][j * maxx + i] = (float) value;
		return true;
	}
}
