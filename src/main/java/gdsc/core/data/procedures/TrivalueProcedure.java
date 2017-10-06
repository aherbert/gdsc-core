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
 * Interface for accessing a value in three dimensions.
 */
public interface TrivalueProcedure
{
	/**
	 * Sets the dimensions. This will be called first to allow the procedure to prepare to process the results. If the
	 * dimensions cannot be processed then return false to abort.
	 *
	 * @param maxx
	 *            the maxx
	 * @param maxy
	 *            the maxy
	 * @param maxz
	 *            the maxz
	 * @return true, if it is OK to continue
	 */
	boolean setDimensions(int maxx, int maxy, int maxz);

	/**
	 * Sets the X axis value.
	 *
	 * @param i
	 *            the index
	 * @param value
	 *            the value
	 */
	void setX(int i, double value);

	/**
	 * Sets the Y axis value.
	 *
	 * @param j
	 *            the index
	 * @param value
	 *            the value
	 */
	void setY(int j, double value);

	/**
	 * Sets the Z axis value.
	 *
	 * @param k
	 *            the index
	 * @param value
	 *            the value
	 */
	void setZ(int k, double value);

	/**
	 * Set the value.
	 *
	 * @param i
	 *            the x index
	 * @param j
	 *            the y index
	 * @param k
	 *            the z index
	 * @param value
	 *            the value
	 */
	void setValue(int i, int j, int k, double value);
}
