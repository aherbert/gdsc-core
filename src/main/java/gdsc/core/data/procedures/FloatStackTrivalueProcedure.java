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
package gdsc.core.data.procedures;


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
	public void setX(int i, double value)
	{
		x[i] = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.procedures.TrivalueProcedure#setY(int, double)
	 */
	public void setY(int j, double value)
	{
		y[j] = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.procedures.TrivalueProcedure#setZ(int, double)
	 */
	public void setZ(int k, double value)
	{
		z[k] = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.procedures.TrivalueProcedure#setValue(int, int, int, double)
	 */
	public void setValue(int i, int j, int k, double value)
	{
		this.value[k][j * maxx + i] = (float) value;
	}
}
