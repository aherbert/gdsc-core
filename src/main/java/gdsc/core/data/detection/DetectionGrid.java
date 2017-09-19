package gdsc.core.data.detection;

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
 * Interface to compute collision detections between a 2D point and a set of objects
 */
public interface DetectionGrid
{
	/**
	 * List of Ids of the objects that contain the point
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @return the ids
	 */
	public int[] find(double x, double y);

	/**
	 * Get the number of objects in the grid
	 * 
	 * @return The number of objects
	 */
	public int size();
}
