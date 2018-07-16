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
	@Override
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
	@Override
	public void setX(int i, double value)
	{
		x[i] = value;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.data.procedures.TrivalueProcedure#setY(int, double)
	 */
	@Override
	public void setY(int j, double value)
	{
		y[j] = value;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.data.procedures.TrivalueProcedure#setZ(int, double)
	 */
	@Override
	public void setZ(int k, double value)
	{
		z[k] = value;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.data.procedures.TrivalueProcedure#setValue(int, int, int, double)
	 */
	@Override
	public void setValue(int i, int j, int k, double value)
	{
		this.value[i][j][k] = value;
	}
}
