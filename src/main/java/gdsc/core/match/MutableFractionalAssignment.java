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
 * Stores an assignment between two identified points, the distance between them and the score for the match
 */
public class MutableFractionalAssignment extends MutableAssignment implements FractionalAssignment
{
	private double score;

	/**
	 * Instantiates a new fractional assignment.
	 *
	 * @param targetId
	 *            the target id
	 * @param predictedId
	 *            the predicted id
	 * @param distance
	 *            the distance (zero is perfect match)
	 * @param score
	 *            The true positive score (must be 0-1)
	 */
	public MutableFractionalAssignment(int targetId, int predictedId, double distance, double score)
	{
		super(targetId, predictedId, distance);
		this.score = score;
	}

	/**
	 * Instantiates a new fractional assignment. The score for a match is 1.
	 *
	 * @param targetId
	 *            the target id
	 * @param predictedId
	 *            the predicted id
	 * @param distance
	 *            the distance
	 */
	public MutableFractionalAssignment(int targetId, int predictedId, double distance)
	{
		this(targetId, predictedId, distance, 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.match.FractionalAssignment#getScore()
	 */
	public double getScore()
	{
		return score;
	}

	/**
	 * Set the score
	 * 
	 * @param score
	 *            the score to set
	 */
	public void setScore(double score)
	{
		this.score = score;
	}
}