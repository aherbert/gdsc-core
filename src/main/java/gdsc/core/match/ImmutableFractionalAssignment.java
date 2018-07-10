/*-
 * #%L
 * Genome Damage and Stability Centre ImageJ Core Package
 *
 * Contains code used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2018 Alex Herbert
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package gdsc.core.match;

/**
 * Stores an assignment between two identified points, the distance between them and the score for the match
 */
public class ImmutableFractionalAssignment extends ImmutableAssignment implements FractionalAssignment
{
	private final double score;

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
	public ImmutableFractionalAssignment(int targetId, int predictedId, double distance, double score)
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
	public ImmutableFractionalAssignment(int targetId, int predictedId, double distance)
	{
		this(targetId, predictedId, distance, 1);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.match.FractionalAssignment#getScore()
	 */
	@Override
	public double getScore()
	{
		return score;
	}
}
