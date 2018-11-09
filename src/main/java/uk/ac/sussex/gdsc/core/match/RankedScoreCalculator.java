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

package uk.ac.sussex.gdsc.core.match;

import uk.ac.sussex.gdsc.core.utils.TurboList;

import java.util.Arrays;
import java.util.BitSet;

/**
 * Calculates the match scoring statistics for the first N predictions in a set of assignments
 * between actual and predicted results.
 *
 * <p>Actual IDs must be ascending starting from 0. Predicted IDs must be in ranked order starting
 * from 0. Scores can be computed for the first N predictions. This can be done for all n from 1 to
 * max N.
 */
public class RankedScoreCalculator {
  private final FractionalAssignment[] assignments;
  private final int maxA;
  private final int maxP;
  private final int totalA;
  private final int totalP;

  private FractionalAssignment[] scoredAssignments;

  /**
   * Construct the calculator.
   *
   * <p>The input assignment data is wrapped and will be sorted for ranking.
   *
   * @param assignments The assignments
   * @param maxA The maximum actual ID in the assignments
   * @param maxP The maximum predicted ID in the assignments
   * @param totalA the total number of actual IDs in the assignments
   * @param totalP the total number of predicted IDs in the assignments
   */
  RankedScoreCalculator(FractionalAssignment[] assignments, int maxA, int maxP, int totalA,
      int totalP) {
    this.assignments = assignments;
    this.maxA = maxA;
    this.maxP = maxP;
    this.totalA = totalA;
    this.totalP = totalP;
  }

  /**
   * Construct the calculator.
   *
   * <p>The input assignment data is wrapped and will be sorted for ranking.
   *
   * @param assignments The assignments
   * @return the ranked score calculator
   */
  public static RankedScoreCalculator create(FractionalAssignment[] assignments) {
    // Count unique actual and predicted
    int maxA = 0;
    int maxP = 0;
    for (int i = 0; i < assignments.length; i++) {
      final FractionalAssignment a = assignments[i];
      if (maxA < a.getTargetId()) {
        maxA = a.getTargetId();
      }
      if (maxP < a.getPredictedId()) {
        maxP = a.getPredictedId();
      }
    }
    return create(assignments, maxA, maxP);
  }

  /**
   * Construct the calculator.
   *
   * <p>The input assignment data is wrapped and will be sorted for ranking.
   *
   * <p>If the input maximums are below the value of any of the assignments they will be increased
   * appropriately. They can be specified since the assignments may not contain all the IDs that
   * were present in the original data.
   *
   * @param assignments The assignments
   * @param maxA The maximum actual ID in the assignments
   * @param maxP The maximum predicted ID in the assignments
   * @return the ranked score calculator
   */
  public static RankedScoreCalculator create(FractionalAssignment[] assignments, int maxA,
      int maxP) {
    AssignmentComparator.sort(assignments);

    // Count unique actual and predicted.
    // Use a BitSet to allow expansion in the event that max A / P are incorrect.
    final BitSet obsA = new BitSet(maxA + 1);
    final BitSet obsP = new BitSet(maxP + 1);
    for (int i = 0; i < assignments.length; i++) {
      final FractionalAssignment a = assignments[i];
      obsA.set(a.getTargetId(), true);
      obsP.set(a.getPredictedId(), true);
    }

    final int newMaxA = obsA.length();
    final int newMaxP = obsP.length();
    final int totalA = obsA.cardinality();
    final int totalP = obsP.cardinality();

    return new RankedScoreCalculator(assignments, newMaxA, newMaxP, totalA, totalP);
  }

  /**
   * Gets the assignments.
   *
   * @return All the assignments.
   */
  public FractionalAssignment[] getAssignments() {
    return Arrays.copyOf(assignments, assignments.length);
  }

  /**
   * Gets the assignments up the first N predictions.
   *
   * @param numberOfPredictions the first N predictions
   * @return All the assignments up the first N predictions
   */
  public FractionalAssignment[] getAssignments(int numberOfPredictions) {
    if (maxP <= numberOfPredictions) {
      return getAssignments();
    }
    return getSubset(numberOfPredictions);
  }

  /**
   * Gets the assignments up the first N predictions.
   *
   * <p>Internal function with no clone of the results.
   *
   * @param numberOfPredictions the first N predictions
   * @return All the assignments up the first N predictions
   */
  private FractionalAssignment[] getAssignmentsInternal(int numberOfPredictions) {
    if (maxP <= numberOfPredictions) {
      return assignments; // No need to clone
    }
    return getSubset(numberOfPredictions);
  }

  private FractionalAssignment[] getSubset(int numberOfPredictions) {
    final FractionalAssignment[] assignments2 = new FractionalAssignment[assignments.length];
    int count = 0;
    for (int i = 0; i < assignments.length; i++) {
      final FractionalAssignment a = assignments[i];
      if (a.getPredictedId() < numberOfPredictions) {
        assignments2[count++] = a;
      }
    }
    return Arrays.copyOf(assignments2, count);
  }

  /**
   * Returns the fractional tp and fp scores using only the first N predicted points. Also returns
   * the integer count of the number of true positives and false positives.
   *
   * <p>When performing multiple matching the predicted points can match more than one actual point.
   * In this case the match score is the combination of all the individual match scores.
   *
   * @param numberOfPredictions the n predicted
   * @param multipleMatches True if allowing multiple matches between a predicted point and multiple
   *        actual points
   * @return The tp and fp scores, plus integer true positives and false positives [tp, fp, itp,
   *         ifp]
   */
  public double[] score(int numberOfPredictions, boolean multipleMatches) {
    return score(numberOfPredictions, multipleMatches, false);
  }

  /**
   * Returns the fractional tp and fp scores using only the first N predicted points. Also returns
   * the integer count of the number of true positives and false positives.
   *
   * <p>When performing multiple matching the predicted points can match more than one actual point.
   * In this case the match score is the combination of all the individual match scores.
   *
   * @param numberOfPredictions the n predicted
   * @param multipleMatches True if allowing multiple matches between a predicted point and multiple
   *        actual points
   * @param save Save the assignments that were selected (accessed using
   *        {@link #getScoredAssignments()})
   * @return The tp and fp scores, plus integer true positives and false positives [tp, fp, itp,
   *         ifp]
   */
  @SuppressWarnings("null")
  public double[] score(int numberOfPredictions, boolean multipleMatches, boolean save) {
    final FractionalAssignment[] localAssignments = getAssignmentsInternal(numberOfPredictions);

    // Note: This currently uses boolean arrays.
    // A BitSet would be more memory efficient but slower unless the entire
    // set fits into cached memory and the standard boolean array does not.
    final boolean[] actualAssignment = new boolean[maxA + 1];
    final int sizeP = Math.min(numberOfPredictions, maxP + 1);
    final TurboList<FractionalAssignment> scored =
        (save) ? new TurboList<>(Math.min(sizeP, actualAssignment.length)) : null;

    // TODO - update the scoring algorithm to not require a sorted list.
    // This would mean a creating a different method since this current method may be
    // called repeatedly with different numberOfPredictions values. So store the assignments as a
    // raw list and then if necessary sort them for this method.

    // All we need to do is find the smallest value in the assignment list.
    // We could hold an array containing the next index to check from the current index.
    // As assignments are made we can update the next index to allow assignments
    // to be skipped over as we scan the array for the next smallest value.

    // Compare the speed of the two versions..

    double[] result;

    // Assign matches
    if (multipleMatches) {
      final double[] predictedAssignment = new double[sizeP];

      double tp = 0;
      int remainingA = totalA;

      for (int i = 0; i < localAssignments.length; i++) {
        final FractionalAssignment a = localAssignments[i];
        if (!actualAssignment[a.getTargetId()]) {
          actualAssignment[a.getTargetId()] = true;
          tp += a.getScore();
          predictedAssignment[a.getPredictedId()] += a.getScore();
          if (save) {
            scored.add(a);
          }
          if (--remainingA == 0) {
            break;
          }
        }
      }

      // Compute the FP.
      // Although a predicted point can accumulate more than 1 for TP matches (due
      // to multiple matching), no predicted point can score less than 1.
      double fp = numberOfPredictions;
      int itp = 0;
      for (int i = 0; i < predictedAssignment.length; i++) {
        if (predictedAssignment[i] == 0) {
          continue;
        }
        itp++;
        if (predictedAssignment[i] > 1) {
          fp -= 1.0;
        } else {
          fp -= predictedAssignment[i];
        }
      }

      result = new double[] {tp, fp, itp, numberOfPredictions - itp};
    } else {
      final boolean[] predictedAssignment = new boolean[sizeP];

      double tp = 0;
      int remainingP = totalP;
      int remainingA = totalA;

      for (int i = 0; i < localAssignments.length; i++) {
        final FractionalAssignment a = localAssignments[i];
        if (!actualAssignment[a.getTargetId()] && !predictedAssignment[a.getPredictedId()]) {
          actualAssignment[a.getTargetId()] = true;
          predictedAssignment[a.getPredictedId()] = true;
          tp += a.getScore();
          if (save) {
            scored.add(a);
          }
          if (--remainingP == 0 || --remainingA == 0) {
            break;
          }
        }
      }

      final int itp = totalP - remainingP;
      result = new double[] {tp, numberOfPredictions - tp, itp, numberOfPredictions - itp};
    }

    if (save) {
      scoredAssignments = scored.toArray(new FractionalAssignment[scored.size()]);
    }

    return result;
  }

  /**
   * Gets the scored assignments from the last call to {@link #score(int, boolean, boolean)}.
   *
   * @return the scored assignments
   */
  public FractionalAssignment[] getScoredAssignments() {
    return scoredAssignments;
  }

  /**
   * Convert the score from {@link #score(int, boolean)} to a fraction classification result using
   * the fractional scoring totals.
   *
   * <p>No checks are made that the input array is valid.
   *
   * @param score the score
   * @param numberOfActual The number of actual results (used to determine FN as [numberOfActual -
   *        TP])
   * @return the fraction classification result
   */
  public static FractionClassificationResult toFractiontoClassificationResult(double[] score,
      int numberOfActual) {
    final double tp = score[0];
    final double fp = score[1];
    return new FractionClassificationResult(tp, fp, 0, numberOfActual - tp);
  }

  /**
   * Convert the score from {@link #score(int, boolean)} to a classification result using the
   * integer scoring totals.
   *
   * <p>No checks are made that the input array is valid.
   *
   * @param score the score
   * @param numberOfActual The number of actual results (used to determine FN as [numberOfActual -
   *        TP])
   * @return the fraction classification result
   */
  public static ClassificationResult toClassificationResult(double[] score, int numberOfActual) {
    final int tp = (int) score[2];
    final int fp = (int) score[3];
    return new ClassificationResult(tp, fp, 0, numberOfActual - tp);
  }

  /**
   * Gets the match score for each predicted id from the assignments.
   *
   * <p>Note: The match score may be greater than 1 if multiple matching was used.
   *
   * @param assignments the assignments (e.g. following a call to {@link #getScoredAssignments()})
   * @param numberOfPredictions the total number of predicted points. This should be higher that the
   *        sum of the scores in the assignments.
   * @return the match score
   * @throws ArrayIndexOutOfBoundsException If the assignments have an id equal or bigger than the
   *         nPrecited
   */
  public static double[] getMatchScore(FractionalAssignment[] assignments,
      int numberOfPredictions) {
    final double[] matchScore = new double[numberOfPredictions];
    for (int i = 0; i < assignments.length; i++) {
      final FractionalAssignment a = assignments[i];
      matchScore[a.getPredictedId()] += a.getScore();
    }
    return matchScore;
  }

  /**
   * Gets the precision, recall and Jaccard curve. The curves are computed for each additional point
   * added to the scored set, beginning with no points. The precision is assumed to be 1 when no
   * points have been considered. Assuming the predicted points IDs are a ranking this produces a
   * precision-recall curve where the area underneath the curve is an indicator of the quality of
   * the ranking of predicted points.
   *
   * @param assignments the assignments (e.g. following a call to {@link #getScoredAssignments()})
   * @param numberOfActual the total number of actual points. This should be higher than all the ids
   *        in the assignments.
   * @param numberOfPredictions the total number of predicted points. This should be higher that the
   *        sum of the scores in the assignments.
   * @return the precision, recall and Jaccard curves
   * @throws ArrayIndexOutOfBoundsException If the assignments have an id equal or bigger than the
   *         nPrecited
   */
  public static double[][] getPrecisionRecallCurve(FractionalAssignment[] assignments,
      int numberOfActual, int numberOfPredictions) {
    final double[] score = getMatchScore(assignments, numberOfPredictions);
    final double[] p = new double[numberOfPredictions + 1];
    final double[] r = new double[p.length];
    final double[] j = new double[p.length];
    double tp = 0;
    double fp = 0;
    p[0] = 1;
    for (int i = 0; i < score.length;) {
      if (score[i] == 0) {
        fp += 1.0;
      } else if (score[i] > 1.0) {
        tp += score[i];
      } else {
        tp += score[i];
        fp += (1.0 - score[i]);
      }
      i++; // Increment since the arrays are offset by 1
      r[i] = tp / numberOfActual;
      p[i] = tp / (tp + fp);
      j[i] = tp / (fp + numberOfActual); // (tp+fp+fn) == (fp+n) since tp+fn=n
    }
    return new double[][] {p, r, j};
  }
}
