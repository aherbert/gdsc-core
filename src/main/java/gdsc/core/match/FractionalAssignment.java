package gdsc.core.match;

/*----------------------------------------------------------------------------- 
 * GDSC SMLM Software
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
public interface FractionalAssignment extends Assignment
{
	/**
	 * The true-positive score (TP) (must be 0-1). The remaining score is assumed to be false positive.
	 * <p>
	 * To use this for classic binary scoring set the score to 1 for all assignments.
	 * <p>
	 * A score less than 1 allows partial matches to be computed. This is similar to computing scoring at multiple
	 * distance thresholds at the same time.
	 */
	public double getScore();
}