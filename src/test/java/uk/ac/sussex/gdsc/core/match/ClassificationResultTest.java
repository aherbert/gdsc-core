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
 * Test for {@link ClassificationResult}.
 */
@SuppressWarnings({"javadoc"})
public class ClassificationResultTest {
  @Test
  public void canCreate() {
    final int tp = 5;
    final int fp = 1;
    final int tn = 2;
    final int fn = 3;
    final ClassificationResult match = new ClassificationResult(tp, fp, tn, fn);
    Assertions.assertEquals(tp, match.getTruePositives(), "tp");
    Assertions.assertEquals(fp, match.getFalsePositives(), "fp");
    Assertions.assertEquals(tn, match.getTrueNegatives(), "tn");
    Assertions.assertEquals(fn, match.getFalseNegatives(), "fn");
    Assertions.assertEquals(tp + fp + tn + fn, match.getTotal(), "total");
    Assertions.assertEquals(tp + fp, match.getPositives(), "positives");
    Assertions.assertEquals(tn + fn, match.getNegatives(), "negatives");

    // Must exactly match the MatchScores
    Assertions.assertEquals(MatchScores.calculateRecall(tp, fn), match.getRecall(), "recall");
    Assertions.assertEquals(MatchScores.calculatePrecision(tp, fp), match.getPrecision(),
        "precision");
    Assertions.assertEquals(MatchScores.calculateJaccard(tp, fp, fn), match.getJaccard(),
        "Jaccard");
    Assertions.assertEquals(MatchScores.calculateF1Score(tp, fp, fn), match.getF1Score(),
        "f1-score");
    Assertions.assertEquals(MatchScores.calculateFBetaScore(tp, fp, fn, 0.5), match.getFScore(0.5),
        "f-score");

    // https://en.wikipedia.org/wiki/Sensitivity_and_specificity
    Assertions.assertEquals((double) tp / (tp + fn), match.getTruePositiveRate(), "tpr");
    Assertions.assertEquals((double) tn / (fp + tn), match.getTrueNegativeRate(), "tnr");
    Assertions.assertEquals((double) tp / (tp + fp), match.getPositivePredictiveValue(), "ppv");
    Assertions.assertEquals((double) tn / (tn + fn), match.getNegativePredictiveValue(), "npv");
    Assertions.assertEquals((double) fp / (fp + tn), match.getFalsePositiveRate(), "fpr");
    Assertions.assertEquals((double) fn / (fn + tp), match.getFalseNegativeRate(), "fnr");
    Assertions.assertEquals((double) fp / (fp + tp), match.getFalseDiscoveryRate(), "fdr");
    Assertions.assertEquals((double) (tp + tn) / (tp + fp + tn + fn), match.getAccuracy(),
        "accuracy");
    Assertions.assertEquals(
        (tp * tn - fp * fn) / Math.sqrt((tp + fp) * (tp + fn) * (tn + fp) * (tn + fn)),
        match.getMatthewsCorrelationCoefficient(), "mcc");
    // https://en.wikipedia.org/wiki/Youden%27s_J_statistic
    Assertions.assertEquals((double) tp / (tp + fn) + (double) tn / (tn + fp) - 1,
        match.getInformedness(), "informedness");
    // MK = PPV + NPV - 1
    Assertions.assertEquals((double) tp / (tp + fp) + (double) tn / (tn + fn) - 1,
        match.getMarkedness(), "markedness");
  }

  @Test
  public void testMatthewsCorrelationCoefficientWithZeroDistance() {
    final int tp = 0;
    final int fp = 0;
    final int tn = 0;
    final int fn = 0;
    final ClassificationResult match = new ClassificationResult(tp, fp, tn, fn);
    Assertions.assertEquals(0, match.getMatthewsCorrelationCoefficient(), "mcc");
  }
}
