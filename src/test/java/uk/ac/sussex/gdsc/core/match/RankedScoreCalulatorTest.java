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
 * Copyright (C) 2011 - 2020 Alex Herbert
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link RankedScoreCalculator}.
 */
@SuppressWarnings({"javadoc"})
class RankedScoreCalulatorTest {
  @Test
  void testToFractionClassificationResult() {
    final double[] score = {787.98, 5657.898};
    final int numberOfActual = 56;
    final FractionClassificationResult result =
        RankedScoreCalculator.toFractiontoClassificationResult(score, numberOfActual);
    Assertions.assertEquals(score[0], result.getTruePositives(), "true positives");
    Assertions.assertEquals(score[1], result.getFalsePositives(), "false positives");
    Assertions.assertEquals(0, result.getTrueNegatives(), "true negatives");
    Assertions.assertEquals(numberOfActual - score[0], result.getFalseNegatives(),
        "false negatives");
  }

  @Test
  void testToClassificationResult() {
    final double[] score = {787.98, 5657.898, 678, 345};
    final int numberOfActual = 56;
    final ClassificationResult result =
        RankedScoreCalculator.toClassificationResult(score, numberOfActual);
    Assertions.assertEquals((int) score[2], result.getTruePositives(), "true positives");
    Assertions.assertEquals((int) score[3], result.getFalsePositives(), "false positives");
    Assertions.assertEquals(0, result.getTrueNegatives(), "true negatives");
    Assertions.assertEquals(numberOfActual - score[2], result.getFalseNegatives(),
        "false negatives");
  }

  @Test
  void testGetMatchScore() {
    final FractionalAssignment[] assignments = createAssignments(
        // id 0
        0, 0, 0, 3,
        // id 0
        1, 0, 0, 2,
        // id 1
        1, 1, 0, 6);
    final double[] score = RankedScoreCalculator.getMatchScore(assignments, 2);
    // The score is the sum of all scores for the same prediction ID
    Assertions.assertArrayEquals(new double[] {3 + 2, 6}, score);
  }

  @Test
  void testPrecisionRecallCurve() {
    // 3 predictions to test each edge case.
    final int numberOfPredictions = 3;
    final FractionalAssignment[] assignments = createAssignments(
        // score >= 1
        0, 0, 0, 1.5,
        // score < 1
        1, 1, 0, 0.5,
        // score = 0
        2, 2, 0, 0);
    // Make the number of actual different
    final int numberOfActual = 4;
    final double[][] curves = RankedScoreCalculator.getPrecisionRecallCurve(assignments,
        numberOfActual, numberOfPredictions);
    Assertions.assertEquals(3, curves.length, "Incorrect number of curves");
    for (final double[] curve : curves) {
      Assertions.assertEquals(numberOfPredictions + 1, curve.length, "Incorrect curve length");
    }
    final double[] precision = curves[0];
    final double[] recall = curves[1];
    final double[] jaccard = curves[2];

    final double[] tp = {1.5, 0.5, 0};
    final double[] fp = {0.0, 0.5, 1.0};
    // @formatter:off
    // Precision = tp / (tp + fp)
    Assertions.assertArrayEquals(new double[] {
        1.0,
        tp[0] / (tp[0] + fp[0]),
        (tp[0] + tp[1]) / (tp[0] + tp[1] + fp[0] + fp[1]),
        (tp[0] + tp[1] + tp[2]) / (tp[0] + tp[1] + tp[2] + fp[0] + fp[1] + fp[2])
    }, precision, "Precision");
    // Recall = tp / (tp + fn) = tp / numberOfActual
    Assertions.assertArrayEquals(new double[] {
        0.0,
        tp[0] / numberOfActual,
        (tp[0] + tp[1]) / numberOfActual,
        (tp[0] + tp[1] + tp[2]) / numberOfActual
    }, recall, "Recall");
    // Jaccard = tp / (tp + fp + fn) = tp / (fp + numberOfActual)
    Assertions.assertArrayEquals(new double[] {
        0.0,
        tp[0] / (fp[0]  + numberOfActual),
        (tp[0] + tp[1]) / (fp[0] + fp[1] + numberOfActual),
        (tp[0] + tp[1] + tp[2]) / (fp[0] + fp[1] + fp[2] + numberOfActual)
    }, jaccard, "Jaccard");
    // @formatter:on
  }

  @Test
  void canCreateCalculatorAndGetAssignments() {
    final FractionalAssignment[] assignments = createAssignments(
        // distance 0.5
        0, 1, 0.5, 3,
        // distance 0
        1, 0, 0, 2);
    final RankedScoreCalculator calc = RankedScoreCalculator.create(assignments);
    Assertions.assertNull(calc.getScoredAssignments(), "scored assignments");

    final FractionalAssignment[] assignments2 = calc.getAssignments();
    Assertions.assertNotSame(assignments, assignments2, "Returned assignments should be a copy");

    // They should be sorted (by distance)
    AssignmentComparator.sort(assignments);

    assertEqualAssignments(assignments, assignments2, assignments.length);
    for (int size = 0; size <= 3; size++) {
      assertEqualAssignments(assignments, calc.getAssignments(size),
          Math.min(size, assignments.length));
    }
  }

  @Test
  void testScoreWithSingleMatches() {
    final FractionalAssignment[] assignments = createAssignments(
        // distance 0 is matched
        0, 0, 0, 0.6,
        // distance 0.3 is ignored
        0, 1, 0.3, 0.1,
        // distance 0.5 is ignored
        1, 0, 0.5, 0.25,
        // uncontested match
        1, 2, 0.75, 1.0);
    final RankedScoreCalculator calc = RankedScoreCalculator.create(assignments);

    // Complete results
    final double tp = 1.6;
    final double fp = 3 - tp;
    final int itp = 2;
    final int ifp = 1;

    assertScore(calc.score(3, false), tp, fp, itp, ifp);

    // Smaller number of predictions ignores the uncontested match
    assertScore(calc.score(2, false), tp - 1, fp, itp - 1, ifp);

    // Test saving
    assertScore(calc.score(3, false, true), tp, fp, itp, ifp);
    final FractionalAssignment[] scoredAssignments = calc.getScoredAssignments();
    assertEqualAssignments(new FractionalAssignment[] {assignments[0], assignments[3]},
        scoredAssignments, 2);
  }

  @Test
  void testScoreWithMultipleMatches() {
    final FractionalAssignment[] assignments = createAssignments(
        // distance 0 is matched
        0, 0, 0, 0.6,
        // distance 0.3 is ignored
        0, 1, 0.3, 0.1,
        // distance 0.5 is a multi-match
        1, 0, 0.5, 0.25,
        // no second match to target id 1
        1, 2, 0.75, 1.0,
        // Uncontested match
        2, 3, 1.0, 1);
    final RankedScoreCalculator calc = RankedScoreCalculator.create(assignments);

    // Complete results
    final double tp = 1.85;
    final double fp = 4 - tp;
    final int itp = 2;
    final int ifp = 2;

    assertScore(calc.score(4, true), tp, fp, itp, ifp);

    // Smaller number of predictions to hit edge case when all assignments are iterated
    assertScore(calc.score(1, true), tp - 1, 0.15, 1, 0);

    // Test saving
    assertScore(calc.score(4, true, true), tp, fp, itp, ifp);
    final FractionalAssignment[] scoredAssignments = calc.getScoredAssignments();
    assertEqualAssignments(
        new FractionalAssignment[] {assignments[0], assignments[2], assignments[4]},
        scoredAssignments, 3);
  }

  /**
   * Creates the assignments.
   *
   * @param data the data [targetId, predictedId, distance, score [...]]
   * @return the fractional assignments
   */
  private static FractionalAssignment[] createAssignments(double... data) {
    final int size = data.length / 4;
    final FractionalAssignment[] assignments = new FractionalAssignment[size];
    for (int i = 0; i < assignments.length; i++) {
      assignments[i] = new ImmutableFractionalAssignment((int) data[i * 4], (int) data[i * 4 + 1],
          data[i * 4 + 2], data[i * 4 + 3]);
    }
    return assignments;
  }

  private static void assertEqualAssignments(FractionalAssignment[] assignments,
      FractionalAssignment[] assignments2, int length) {
    Assertions.assertEquals(length, assignments2.length, "length");
    for (int i = 0; i < length; i++) {
      final FractionalAssignment expected = assignments[i];
      final FractionalAssignment actual = assignments2[i];
      Assertions.assertEquals(expected.getPredictedId(), actual.getPredictedId(), "predicted Id");
      Assertions.assertEquals(expected.getTargetId(), actual.getTargetId(), "target Id");
      Assertions.assertEquals(expected.getDistance(), actual.getDistance(), "distance");
      Assertions.assertEquals(expected.getScore(), actual.getScore(), "score");
    }
  }

  private static void assertScore(double[] score, double tp, double fp, int itp, int ifp) {
    // Allow margin for error in summations
    Assertions.assertEquals(tp, score[0], 1e-6, "true positives");
    Assertions.assertEquals(fp, score[1], 1e-6, "false positives");
    Assertions.assertEquals(itp, score[2], "count true positives");
    Assertions.assertEquals(ifp, score[3], "count false positives");
  }
}
