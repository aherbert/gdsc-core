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
	public final int nActual;
	private final int maxA, maxP, totalA, totalP;

	/**
	 * Construct the calculator
	 * 
	 * @param assignments
	 *            The assignments
	 * @param nActual
	 *            The number of actual results (used to determine FN as [nActual - TP])
	 */
	public RankedScoreCalculator(FractionalAssignment[] assignments, int nActual)
	{
		this.assignments = assignments;
		Arrays.sort(assignments);
		this.nActual = nActual;
		// Count unique actual and predicted
		int maxA = 0, maxP = 0;
		for (FractionalAssignment a : assignments)
		{
			if (maxA < a.targetId)
				maxA = a.targetId;
			if (maxP < a.predictedId)
				maxP = a.predictedId;
		}
		maxA++;
		maxP++;
		boolean[] obsA = new boolean[maxA];
		boolean[] obsP = new boolean[maxP];
		for (FractionalAssignment a : assignments)
		{
			obsA[a.targetId] = true;
			obsP[a.predictedId] = true;
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
		final FractionalAssignment[] assignments2 = new FractionalAssignment[assignments.length];
		int count = 0;
		for (FractionalAssignment a : assignments)
			if (a.predictedId < nPredicted)
				assignments2[count++] = a;
		return Arrays.copyOf(assignments2, count);
	}

	/**
	 * Returns the fractional tp and fp scores using only the first N predicted points.
	 * <p>
	 * When performing multiple matching the best scoring
	 *
	 * @param nPredicted
	 *            the n predicted
	 * @param multipleMatches
	 *            True if allowing multiple matches between a predicted point and multiple actual points
	 * @return The tp and fp scores
	 */
	public double[] score(int nPredicted, boolean multipleMatches)
	{
		FractionalAssignment[] assignments = getAssignments(nPredicted);

		// Assign matches
		if (multipleMatches)
		{
			final boolean[] actualAssignment = new boolean[maxP];
			final double[] predictedAssignment = new double[maxA];

			double tp = 0;
			int nA = totalA;

			for (FractionalAssignment a : assignments)
			{
				if (!actualAssignment[a.targetId])
				{
					actualAssignment[a.targetId] = true;
					tp += a.score;
					predictedAssignment[a.predictedId] += a.score;
					if (--nA == 0)
						break;
				}
			}

			// Compute the FP. 
			// Although a predicted point can accumulate more than 1 for TP matches (due 
			// to multiple matching), no predicted point can score less than 1.
			double fp = nPredicted;
			for (int i = 0; i < predictedAssignment[i]; i++)
			{
				if (predictedAssignment[i] > 1)
					predictedAssignment[i] = 1;
				fp -= predictedAssignment[i];
			}

			return new double[] { tp, fp };
		}
		else
		{
			final boolean[] actualAssignment = new boolean[maxA];
			final boolean[] predictedAssignment = new boolean[maxP];

			double tp = 0;
			int nP = totalP;
			int nA = totalA;

			for (FractionalAssignment a : assignments)
			{
				if (!actualAssignment[a.targetId])
				{
					if (!predictedAssignment[a.predictedId])
					{
						actualAssignment[a.targetId] = true;
						predictedAssignment[a.predictedId] = true;
						tp += a.score;
						if (--nA == 0 || --nP == 0)
							break;
					}
				}
			}

			return new double[] { tp, nPredicted - tp };
		}
	}

	/**
	 * Convert the score from {@link #score(int, boolean)} to a classification result
	 * <p>
	 * no checks are made that the input array is valid.
	 *
	 * @param score
	 *            the score
	 * @return the fraction classification result
	 */
	public FractionClassificationResult convert(double[] score)
	{
		return new FractionClassificationResult(score[0], score[1], 0, nActual - score[0]);
	}
}
