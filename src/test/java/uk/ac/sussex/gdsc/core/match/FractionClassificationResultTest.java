package uk.ac.sussex.gdsc.core.match;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link FractionClassificationResult}.
 */
@SuppressWarnings({"javadoc"})
public class FractionClassificationResultTest {
  @Test
  public void canCreate() {
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
  public void canCreateWithoutNumberOfPositivesOrNegatives() {
    final double tp = 5.12;
    final double fp = 1.11;
    final double tn = 2.32;
    final double fn = 3.45;
    final FractionClassificationResult match = new FractionClassificationResult(tp, fp, tn, fn);
    Assertions.assertEquals(0, match.getNumberOfPositives(), "number of positives");
    Assertions.assertEquals(0, match.getNumberOfNegatives(), "number of negatives");
  }

  @Test
  public void testMatthewsCorrelationCoefficientWithZeroDistance() {
    final double tp = 0;
    final double fp = 0;
    final double tn = 0;
    final double fn = 0;
    final FractionClassificationResult match = new FractionClassificationResult(tp, fp, tn, fn);
    Assertions.assertEquals(0, match.getMatthewsCorrelationCoefficient(), "mcc");
  }
}
