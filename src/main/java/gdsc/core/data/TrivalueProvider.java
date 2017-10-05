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
 * Provide data on 3-axes. This is a simple interface to allow passing XYZ data stored in a different layout without
 * rewriting the data.
 */
public interface TrivalueProvider
{
	/**
	 * Gets the max X index.
	 *
	 * @return the max X index
	 */
	public int getMaxX();

	/**
	 * Gets the max Y index.
	 *
	 * @return the max Y index
	 */
	public int getMaxY();

	/**
	 * Gets the max Z index.
	 *
	 * @return the max Z index
	 */
	public int getMaxZ();

	/**
	 * Gets the value.
	 *
	 * @param x
	 *            the x (must be positive)
	 * @param y
	 *            the y (must be positive)
	 * @param z
	 *            the z (must be positive)
	 * @return the value
	 */
	public double get(int x, int y, int z);

	/**
	 * Gets the 3x3x3 values around the index. If the index is at the bounds then the result is undefined.
	 *
	 * @param x
	 *            the x (must be positive)
	 * @param y
	 *            the y (must be positive)
	 * @param z
	 *            the z (must be positive)
	 * @param values
	 *            the values
	 */
	public void get(int x, int y, int z, double[][][] values);
	
	/**
	 * Convert to an array.
	 *
	 * @return the array
	 */
	public double[][][] toArray();
}
