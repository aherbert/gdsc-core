package gdsc.core.match;

/*----------------------------------------------------------------------------- 
 * GDSC Software
 * 
 * Copyright (C) 2016 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

/**
 * Stores an assignment between two identified points and the distance between them
 */
public interface Assignment
{
	/**
	 * @return the target Id
	 */
	public int getTargetId();

	/**
	 * @return the predicted Id
	 */
	public int getPredictedId();

	/**
	 * @return the distance
	 */
	public double getDistance();

	// For Java 1.8
	///**
	// * @param o
	// * @return
	// */
	//default public int compareTo(Assignment o)
	//{
	//	if (getDistance() < o.getDistance())
	//		return -1;
	//	if (getDistance() > o.getDistance())
	//		return 1;
	//	return 0;
	//}
}