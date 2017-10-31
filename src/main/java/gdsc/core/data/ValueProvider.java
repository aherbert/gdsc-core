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
 * Provide data on 1-axis. This is a simple interface to allow passing XY data stored in a different layout without
 * rewriting the data.
 */
public interface ValueProvider
{
	/**
	 * Gets the length.
	 *
	 * @return the length
	 */
	public int getLength();

	/**
	 * Gets the value.
	 *
	 * @param x
	 *            the x (must be positive)
	 * @return the value
	 */
	public double get(int x);

	/**
	 * Gets the 3 values around the index. If the index is at the bounds then the result is undefined.
	 *
	 * @param x
	 *            the x (must be positive)
	 * @param values
	 *            the values
	 */
	public void get(int x, double[] values);
	
	/**
	 * Convert to an array.
	 *
	 * @return the array
	 */
	public double[] toArray();
}
