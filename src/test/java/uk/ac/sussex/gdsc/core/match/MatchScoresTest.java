package uk.ac.sussex.gdsc.core.match;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link MatchScores}.
 */
@SuppressWarnings({"javadoc"})
public class MatchScoresTest {
  @Test
  public void testPrecision() {
    Assertions.assertEquals(0, MatchScores.calculatePrecision(0, 0));
    final int tp = 5;
    final int fp = 1;
    Assertions.assertEquals((double) tp / (tp + fp), MatchScores.calculatePrecision(tp, fp));
  }

  @Test
  public void testRecall() {
    Assertions.assertEquals(0, MatchScores.calculateRecall(0, 0));
    final int tp = 5;
    final int fn = 1;
    Assertions.assertEquals((double) tp / (tp + fn), MatchScores.calculateRecall(tp, fn));
  }

  @Test
  public void testJaccard() {
    Assertions.assertEquals(0, MatchScores.calculateJaccard(0, 0, 0));
    final int tp = 5;
    final int fp = 1;
    final int fn = 1;
    Assertions.assertEquals((double) tp / (tp + fp + fn), MatchScores.calculateJaccard(tp, fp, fn));
  }

  @Test
  public void testF1Score() {
    Assertions.assertEquals(0, MatchScores.calculateF1Score(0.0, 0.0));
    Assertions.assertEquals(0, MatchScores.calculateF1Score(0, 0, 0));
    final int tp = 5;
    final int fp = 1;
    final int fn = 1;
    Assertions.assertEquals(computeFScore(tp, fn, fp, 1.0),
        MatchScores.calculateF1Score(tp, fn, fp));
    final double precision = MatchScores.calculatePrecision(tp, fp);
    final double recall = MatchScores.calculateRecall(tp, fn);
    Assertions.assertEquals(computeFScore(tp, fn, fp, 1),
        MatchScores.calculateF1Score(precision, recall));
  }

  @Test
  public void testFBetaScore() {
    final int tp = 5;
    final int fp = 1;
    final int fn = 1;
    for (final double beta : new double[] {0.5, 2}) {
      Assertions.assertEquals(0, MatchScores.calculateFBetaScore(0.0, 0.0, beta));
      Assertions.assertEquals(0, MatchScores.calculateFBetaScore(0, 0, 0, beta));
      Assertions.assertEquals(computeFScore(tp, fn, fp, beta),
          MatchScores.calculateFBetaScore(tp, fn, fp, beta));
      final double precision = MatchScores.calculatePrecision(tp, fp);
      final double recall = MatchScores.calculateRecall(tp, fn);
      Assertions.assertEquals(computeFScore(tp, fn, fp, beta),
          MatchScores.calculateFBetaScore(precision, recall, beta));
    }
  }

  private static double computeFScore(int tp, int fn, int fp, double beta) {
    // from https://en.wikipedia.org/wiki/F1_score
    final double b2 = beta * beta;
    return (1 + b2) * tp / ((1 + b2) * tp + b2 * fn + fp);
  }
}
