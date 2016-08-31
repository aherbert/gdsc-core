package gdsc.core.match;

import java.util.Arrays;

/**
 * Calculates the precision and recall for a set of assignments.
 * <p>
 * Actual IDs must be ascending starting from 0. Predicted IDs must be in ranked order starting from 0.
 * Scores can be computed for the first N predictions. This can be done for all n from 1 to max N and a precision-recall
 * plot computed.
 */
public class AUCCalculator
{
	private final FractionalAssignment[] assignments;
	public final int nActual;

	/**
	 * Construct the calculator
	 * 
	 * @param assignments
	 *            The assignments
	 * @param nActual
	 *            The number of actual results (used to determine FN as [nActual - TP])
	 */
	public AUCCalculator(FractionalAssignment[] assignments, int nActual)
	{
		this.assignments = assignments;
		Arrays.sort(assignments);
		this.nActual = nActual;
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
		FractionalAssignment[] assignments2 = new FractionalAssignment[assignments.length];
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
			final boolean[] actualAssignment = new boolean[nActual];
			final double[] predictedAssignment = new double[nPredicted];

			double tp = 0;
			int nA = nActual;

			for (FractionalAssignment a : assignments)
			{
				if (!actualAssignment[a.targetId])
				{
					actualAssignment[a.targetId] = true;
					tp += a.score;
					predictedAssignment[a.predictedId] += a.score;
					if (nA == 0)
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
			final boolean[] actualAssignment = new boolean[nActual];
			final boolean[] predictedAssignment = new boolean[nPredicted];

			double tp = 0;
			int nP = nPredicted;
			int nA = nActual;

			for (FractionalAssignment a : assignments)
			{
				if (!actualAssignment[a.targetId])
				{
					if (!predictedAssignment[a.predictedId])
					{
						actualAssignment[a.targetId] = true;
						predictedAssignment[a.predictedId] = true;
						tp += a.score;
						nP--;
						nA--;
						if (nP == 0 || nA == 0)
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

	/**
	 * Calculates an estimate of the area under the precision-recall curve.
	 * <p>
	 * The estimate is computed using integration above the recall limit. Below the limit a simple linear interpolation
	 * is used from the given point to precision 1 at recall 0. This avoids noise in the lower recall section of the
	 * curve.
	 * <p>
	 * If no recall values are above the limit then the full integration is performed.
	 *
	 * @param precision
	 * @param recall
	 * @param recallLimit
	 *            Set to 0 to compute the full area.
	 * @return Area under the PR curve
	 */
	public static double auc(double[] precision, double[] recall, double recallLimit)
	{
		if (precision == null || recall == null)
			return 0;

		double area = 0.0;
		int k;

		if (recallLimit > 0)
		{
			// Move from high to low recall and find the first point below the limit
			k = recall.length - 1;
			while (k > 0 && recall[k] > recallLimit)
				k--;

			if (k > 0)
			{
				// Find the first point where precision was not 1
				int kk = 0;
				while (precision[kk + 1] == 1)
					kk++;

				// Full precision of 1 up to point kk
				area += (recall[kk] - recall[0]);

				// Interpolate from precision at kk to k
				area += (precision[k] + precision[kk]) * 0.5 * (recall[k] - recall[kk]);

				// Increment to start the remaining integral
				k++;
			}
		}
		else
		{
			// Complete integration from start
			k = 0;
		}

		// Integrate the rest
		double prevR = 0;
		double prevP = 1;
		if (recall[0] == 0)
			k++;

		for (; k < precision.length; k++)
		{
			final double delta = recall[k] - prevR;
			if (precision[k] == prevP)
				area += prevP * delta;
			else
				// Interpolate
				area += (precision[k] + prevP) * 0.5 * delta;
			prevR = recall[k];
			prevP = precision[k];
		}
		return area;
	}

	/**
	 * Calculates an estimate of the area under the precision-recall curve.
	 * <p>
	 * Assumes the first values in the two arrays are precision 1 at recall 0.
	 *
	 * @param precision
	 * @param recall
	 * @return Area under the PR curve
	 */
	public static double auc(double[] precision, double[] recall)
	{
		double area = 0.0;

		double prevR = 0;
		double prevP = 1;

		for (int k = 1; k < precision.length; k++)
		{
			final double delta = recall[k] - prevR;
			if (precision[k] == prevP)
				area += prevP * delta;
			else
				// Interpolate
				area += (precision[k] + prevP) * 0.5 * delta;
			prevR = recall[k];
			prevP = precision[k];
		}
		return area;
	}
}
