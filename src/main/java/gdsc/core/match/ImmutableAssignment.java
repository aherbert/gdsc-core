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
public class ImmutableAssignment implements Assignment
{
	final private int targetId;
	final private int predictedId;
	final private double distance;

	/**
	 * Instantiates a new assignment.
	 *
	 * @param targetId
	 *            the target id
	 * @param predictedId
	 *            the predicted id
	 * @param distance
	 *            the distance (zero is perfect match)
	 */
	public ImmutableAssignment(int targetId, int predictedId, double distance)
	{
		this.targetId = targetId;
		this.predictedId = predictedId;
		this.distance = distance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.match.Assignment#getTargetId()
	 */
	public int getTargetId()
	{
		return targetId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.match.Assignment#getPredictedId()
	 */
	public int getPredictedId()
	{
		return predictedId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.match.Assignment#getDistance()
	 */
	public double getDistance()
	{
		return distance;
	}
}