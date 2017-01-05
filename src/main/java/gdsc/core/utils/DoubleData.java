package gdsc.core.utils;

/*----------------------------------------------------------------------------- 
 * GDSC SMLM Software
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
 * Specify double data
 */
public interface DoubleData
{
	/**
	 * The number of values.
	 *
	 * @return the number of values
	 */
	public int size();
	
	/**
	 * Get the values.
	 *
	 * @return the values
	 */
	public double[] values();
}