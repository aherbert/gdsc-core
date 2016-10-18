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

import java.util.Arrays;

/**
 * Calculates the match scoring statistics for the first N predictions in a set of assignments between actual and
 * predicted results.
 * <p>
 * Actual IDs must be ascending starting from 0. Predicted IDs must be in ranked order starting from 0.
 * Scores can be computed for the first N predictions. This can be done for all n from 1 to max N.
 */
public class RankedScoreCalculator
{
	private final FractionalAssignment[] assignments;
	private final int maxA, maxP, totalA, totalP;

	private FractionalAssignment[] scoredAssignments;

	/**
	 * Construct the calculator
	 * 
	 * @param assignments
	 *            The assignments
	 */
	public RankedScoreCalculator(FractionalAssignment[] assignments)
	{
		this.assignments = assignments;
		AssignmentComparator.sort(assignments);
		// Count unique actual and predicted
		int maxA = 0, maxP = 0;
		for (FractionalAssignment a : assignments)
		{
			if (maxA < a.getTargetId())
				maxA = a.getTargetId();
			if (maxP < a.getPredictedId())
				maxP = a.getPredictedId();
		}
		final boolean[] obsA = new boolean[maxA + 1];
		final boolean[] obsP = new boolean[maxP + 1];
		for (FractionalAssignment a : assignments)
		{
			obsA[a.getTargetId()] = true;
			obsP[a.getPredictedId()] = true;
		}
		this.maxA = maxA;
		this.maxP = maxP;
		totalA = count(obsA);
		totalP = count(obsP);
	}

	private static int count(boolean[] data)
	{
		int c = 0;
		for (int i = 0; i < data.length; i++)
			if (data[i])
				c++;
		return c;
	}

	/**
	 * @return All the assignments
	 */
	public FractionalAssignment[] getAssignments()
	{
		return Arrays.copyOf(assignments, assignments.length);
	}

	/**
	 * @param nPredicted
	 *            the first N predictions
	 * @return All the assignments up the first N predictions
	 */
	public FractionalAssignment[] getAssignments(int nPredicted)
	{
		if (maxP <= nPredicted)
			return getAssignments();
		return getSubset(nPredicted);
	}

	/**
	 * @param nPredicted
	 *            the first N predictions
	 * @return All the assignments up the first N predictions
	 */
	private FractionalAssignment[] getAssignmentsInternal(int nPredicted)
	{
		if (maxP <= nPredicted)
			return assignments; // No need to clone
		return getSubset(nPredicted);
	}

	private FractionalAssignment[] getSubset(int nPredicted)
	{
		final FractionalAssignment[] assignments2 = new FractionalAssignment[assignments.length];
		int count = 0;
		for (FractionalAssignment a : assignments)
			if (a.getPredictedId() < nPredicted)
				assignments2[count++] = a;
		return Arrays.copyOf(assignments2, count);
	}

	/**
	 * Returns the fractional tp and fp scores using only the first N predicted points. Also returns the integer count
	 * of the number of true positives and false positives.
	 * <p>
	 * When performing multiple matching the predicted points can match more than one actual point. In this case the
	 * match score is the combination of all the individual match scores.
	 *
	 * @param nPredicted
	 *            the n predicted
	 * @param multipleMatches
	 *            True if allowing multiple matches between a predicted point and multiple actual points
	 * @return The tp and fp scores, plus integer true positives and false positives [tp, fp, itp, ifp]
	 */
	public double[] score(int nPredicted, boolean multipleMatches)
	{
		return score(nPredicted, multipleMatches, false);
	}

	/**
	 * Returns the fractional tp and fp scores using only the first N predicted points. Also returns the integer count
	 * of the number of true positives and false positives.
	 * <p>
	 * When performing multiple matching the predicted points can match more than one actual point. In this case the
	 * match score is the combination of all the individual match scores.
	 *
	 * @param nPredicted
	 *            the n predicted
	 * @param multipleMatches
	 *            True if allowing multiple matches between a predicted point and multiple actual points
	 * @param save
	 *            Save the assignments that were selected (accessed using {@link #getScoredAssignments()})
	 * @return The tp and fp scores, plus integer true positives and false positives [tp, fp, itp, ifp]
	 */
	public double[] score(int nPredicted, boolean multipleMatches, boolean save)
	{
		final FractionalAssignment[] assignments = getAssignmentsInternal(nPredicted);
		int scored = 0;
		scoredAssignments = (save) ? new FractionalAssignment[totalA] : null;

		// Assign matches
		if (multipleMatches)
		{
			final boolean[] actualAssignment = new boolean[maxA + 1];
			final double[] predictedAssignment = new double[maxP + 1];

			double tp = 0;
			int nA = totalA;

			for (int i = 0; i < assignments.length; i++)
			{
				final FractionalAssignment a = assignments[i];
				if (!actualAssignment[a.getTargetId()])
				{
					actualAssignment[a.getTargetId()] = true;
					tp += a.getScore();
					predictedAssignment[a.getPredictedId()] += a.getScore();
					if (save)
						scoredAssignments[scored++] = a;
					if (--nA == 0)
						break;
				}
			}

			// Compute the FP. 
			// Although a predicted point can accumulate more than 1 for TP matches (due 
			// to multiple matching), no predicted point can score less than 1.
			double fp = nPredicted;
			int p = 0;
			for (int i = 0; i < predictedAssignment.length; i++)
			{
				if (predictedAssignment[i] == 0)
					continue;
				p++;
				if (predictedAssignment[i] > 1)
					predictedAssignment[i] = 1;
				fp -= predictedAssignment[i];
			}

			if (save)
				scoredAssignments = Arrays.copyOf(scoredAssignments, scored);

			return new double[] { tp, fp, p, nPredicted - p };
		}
		else
		{
			final boolean[] actualAssignment = new boolean[maxA + 1];
			final boolean[] predictedAssignment = new boolean[maxP + 1];

			double tp = 0;
			int nP = totalP;
			int nA = totalA;

			for (int i = 0; i < assignments.length; i++)
			{
				final FractionalAssignment a = assignments[i];
				if (!actualAssignment[a.getTargetId()])
				{
					if (!predictedAssignment[a.getPredictedId()])
					{
						actualAssignment[a.getTargetId()] = true;
						predictedAssignment[a.getPredictedId()] = true;
						tp += a.getScore();
						if (save)
							scoredAssignments[scored++] = a;
						if (--nP == 0 || --nA == 0)
							break;
					}
				}
			}

			if (save)
				scoredAssignments = Arrays.copyOf(scoredAssignments, scored);

			final int p = totalP - nP;
			return new double[] { tp, nPredicted - tp, p, nPredicted - p };
		}
	}

	/**
	 * Gets the scored assignments from the last call to {@link #score(int, boolean, boolean)}
	 *
	 * @return the scored assignments
	 */
	public FractionalAssignment[] getScoredAssignments()
	{
		return scoredAssignments;
	}

	/**
	 * Convert the score from {@link #score(int, boolean)} to a fraction classification result using the fractional
	 * scoring totals
	 * <p>
	 * No checks are made that the input array is valid.
	 *
	 * @param score
	 *            the score
	 * @param nActual
	 *            The number of actual results (used to determine FN as [nActual - TP])
	 * @return the fraction classification result
	 */
	public static FractionClassificationResult toFractiontoClassificationResult(double[] score, int nActual)
	{
		final double tp = score[0];
		final double fp = score[1];
		return new FractionClassificationResult(tp, fp, 0, nActual - tp);
	}

	/**
	 * Convert the score from {@link #score(int, boolean)} to a classification result using the integer scoring totals
	 * <p>
	 * No checks are made that the input array is valid.
	 *
	 * @param score
	 *            the score
	 * @param nActual
	 *            The number of actual results (used to determine FN as [nActual - TP])
	 * @return the fraction classification result
	 */
	public static ClassificationResult toClassificationResult(double[] score, int nActual)
	{
		final int tp = (int) score[2];
		final int fp = (int) score[3];
		return new ClassificationResult(tp, fp, 0, nActual - tp);
	}
}
