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
public class FractionalAssignment extends Assignment
{
	/**
	 * The true-positive score (TP) (must be 0-1). The remaining score is assumed to be false positive.
	 * <p>
	 * To use this for classic binary scoring set the score to 1 for all assignments.
	 * <p>
	 * A score less than 1 allows partial matches to be computed. This is similar to computing scoring at multiple
	 * distance thresholds at the same time. When using partial matching the distance for sorting assignments is set to
	 * 1-score.
	 */
	final public double score;

	/**
	 * Instantiates a new fractional assignment.
	 *
	 * @param targetId
	 *            the target id
	 * @param predictedId
	 *            the predicted id
	 * @param distance
	 *            the distance
	 * @param score
	 *            The true positive score (must be 0-1)
	 */
	private FractionalAssignment(int targetId, int predictedId, double distance, double score)
	{
		super(targetId, predictedId, distance);
		this.score = score;
	}

	/**
	 * Instantiates a new fractional assignment using the given distance between the pair. Assignments are sorted using
	 * the distance for use in computing nearest-neighbour matching. The for a match is 1.
	 *
	 * @param targetId
	 *            the target id
	 * @param predictedId
	 *            the predicted id
	 * @param distance
	 *            the distance
	 */
	public FractionalAssignment(int targetId, int predictedId, double distance)
	{
		this(targetId, predictedId, distance, 1);
	}

	/**
	 * Creates a new fractional assignment.
	 * <p>
	 * To use this for classic binary scoring set the score to the distance between matches. A match will be scored 1.
	 * <p>
	 * To use this for fractional matching provide a score between 0 and 1 for the quality of the match. When using
	 * partial matching the distance for sorting assignments is set to 1-score.
	 *
	 * @param targetId
	 *            the target id
	 * @param predictedId
	 *            the predicted id
	 * @param score
	 *            the score
	 * @param scoreIsFractionTP
	 *            the score is fraction match (ie a TP score from 0-1)
	 * @return the fractional assignment
	 */
	public static FractionalAssignment create(int targetId, int predictedId, double score, boolean scoreIsFractionTP)
	{
		if (scoreIsFractionTP)
			return new FractionalAssignment(targetId, predictedId, 1 - score, score);
		else
			return new FractionalAssignment(targetId, predictedId, score);
	}
}