package uk.ac.sussex.gdsc.core.match;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link MatchResult}.
 */
@SuppressWarnings({"javadoc"})
public class MatchResultTest {
  @Test
  public void canCreate() {
    final int tp = 5;
    final int fp = 1;
    final int fn = 3;
    final double rmsd = 67.789;
    final MatchResult match = new MatchResult(tp, fp, fn, rmsd);
    Assertions.assertEquals(tp, match.getTruePositives(), "tp");
    Assertions.assertEquals(fp, match.getFalsePositives(), "fp");
    Assertions.assertEquals(fn, match.getFalseNegatives(), "fn");
    Assertions.assertEquals(rmsd, match.getRmsd(), "rmsd");
    Assertions.assertEquals(tp + fp, match.getNumberPredicted(), "predicted");
    Assertions.assertEquals(tp + fn, match.getNumberActual(), "actual");
    Assertions.assertEquals(MatchScores.calculateRecall(tp, fn), match.getRecall(), "recall");
    Assertions.assertEquals(MatchScores.calculatePrecision(tp, fp), match.getPrecision(),
        "precision");
    Assertions.assertEquals(MatchScores.calculateJaccard(tp, fp, fn), match.getJaccard(),
        "Jaccard");
    Assertions.assertEquals(MatchScores.calculateF1Score(tp, fp, fn), match.getF1Score(),
        "f1-score");
    Assertions.assertEquals(MatchScores.calculateFBetaScore(tp, fp, fn, 0.5), match.getFScore(0.5),
        "f-score");
  }
}
