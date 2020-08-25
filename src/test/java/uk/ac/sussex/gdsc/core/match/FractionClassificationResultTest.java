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
 * Test for {@link FractionClassificationResult}.
 */
@SuppressWarnings({"javadoc"})
class FractionClassificationResultTest {
  @Test
  void canCreate() {
    final double tp = 5.12;
    final double fp = 1.11;
    final double tn = 2.32;
    final double fn = 3.45;
    final int positives = 62;
    final int negatives = 55;
    final FractionClassificationResult match =
        new FractionClassificationResult(tp, fp, tn, fn, positives, negatives);
    Assertions.assertEquals(tp, match.getTruePositives(), "tp");
    Assertions.assertEquals(fp, match.getFalsePositives(), "fp");
    Assertions.assertEquals(tn, match.getTrueNegatives(), "tn");
    Assertions.assertEquals(fn, match.getFalseNegatives(), "fn");
    Assertions.assertEquals(positives, match.getNumberOfPositives(), "number of positives");
    Assertions.assertEquals(negatives, match.getNumberOfNegatives(), "number of negatives");
    Assertions.assertEquals(tp + fp + tn + fn, match.getTotal(), "total");
    Assertions.assertEquals(tp + fp, match.getPositives(), "positives");
    Assertions.assertEquals(tn + fn, match.getNegatives(), "negatives");
    Assertions.assertEquals(tp / (tp + fn), match.getRecall(), "recall");
    Assertions.assertEquals(tp / (tp + fp), match.getPrecision(), "precision");
    Assertions.assertEquals(tp / (tp + fp + fn), match.getJaccard(), "Jaccard");

    final double delta = 1e-10;
    Assertions.assertEquals(2 * tp / (2 * tp + fn + fp), match.getF1Score(), delta, "f1-score");
    final double beta = 0.5;
    Assertions.assertEquals(
        (1 + beta * beta) * tp / ((1 + beta * beta) * tp + beta * beta * fn + fp),
        match.getFScore(beta), delta, "f-score");

    // https://en.wikipedia.org/wiki/Sensitivity_and_specificity
    Assertions.assertEquals(tp / (tp + fn), match.getTruePositiveRate(), "tpr");
    Assertions.assertEquals(tn / (fp + tn), match.getTrueNegativeRate(), "tnr");
    Assertions.assertEquals(tp / (tp + fp), match.getPositivePredictiveValue(), "ppv");
    Assertions.assertEquals(tn / (tn + fn), match.getNegativePredictiveValue(), "npv");
    Assertions.assertEquals(fp / (fp + tn), match.getFalsePositiveRate(), "fpr");
    Assertions.assertEquals(fn / (fn + tp), match.getFalseNegativeRate(), "fnr");
    Assertions.assertEquals(fp / (fp + tp), match.getFalseDiscoveryRate(), "fdr");
    Assertions.assertEquals((tp + tn) / (tp + fp + tn + fn), match.getAccuracy(), "accuracy");
    Assertions.assertEquals(
        (tp * tn - fp * fn) / Math.sqrt((tp + fp) * (tp + fn) * (tn + fp) * (tn + fn)),
        match.getMatthewsCorrelationCoefficient(), "mcc");
    // https://en.wikipedia.org/wiki/Youden%27s_J_statistic
    Assertions.assertEquals(tp / (tp + fn) + tn / (tn + fp) - 1, match.getInformedness(),
        "informedness");
    // MK = PPV + NPV - 1
    Assertions.assertEquals(tp / (tp + fp) + tn / (tn + fn) - 1, match.getMarkedness(),
        "markedness");
  }

  @Test
  void canCreateWithoutNumberOfPositivesOrNegatives() {
    final double tp = 5.12;
    final double fp = 1.11;
    final double tn = 2.32;
    final double fn = 3.45;
    final FractionClassificationResult match = new FractionClassificationResult(tp, fp, tn, fn);
    Assertions.assertEquals(0, match.getNumberOfPositives(), "number of positives");
    Assertions.assertEquals(0, match.getNumberOfNegatives(), "number of negatives");
  }

  @Test
  void testMatthewsCorrelationCoefficientWithZeroDistance() {
    final double tp = 0;
    final double fp = 0;
    final double tn = 0;
    final double fn = 0;
    final FractionClassificationResult match = new FractionClassificationResult(tp, fp, tn, fn);
    Assertions.assertEquals(0, match.getMatthewsCorrelationCoefficient(), "mcc");
  }
}
