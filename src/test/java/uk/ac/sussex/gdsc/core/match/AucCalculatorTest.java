package uk.ac.sussex.gdsc.core.match;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link AucCalculator}.
 */
@SuppressWarnings({"javadoc"})
public class AucCalculatorTest {
  @Test
  public void testBadArguments() {
    // Length mismatch
    double[] precision = new double[1];
    double[] recall = new double[2];
    Assertions.assertThrows(NullPointerException.class, () -> AucCalculator.auc(null, recall));
    Assertions.assertThrows(NullPointerException.class, () -> AucCalculator.auc(precision, null));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> AucCalculator.auc(precision, recall));
  }

  @Test
  public void testAuc() {
    double[] precision = {1, 0.75, 0.8, 0.8, 0.6};
    double[] recall = {0, 0.1, 0.2, 0.35, 0.4};
    // @formatter:off
    double expected =
        (recall[1] - recall[0]) * (precision[1] + precision[0]) / 2 +
        (recall[2] - recall[1]) * (precision[2] + precision[1]) / 2 +
        (recall[3] - recall[2]) * (precision[3] + precision[2]) / 2 +
        (recall[4] - recall[3]) * (precision[4] + precision[3]) / 2;
    // @formatter:on
    Assertions.assertEquals(expected, AucCalculator.auc(precision, recall));
  }

  @Test
  public void testAucWithoutRecallZero() {
    double[] precision = {0.75, 0.8, 0.8, 0.6};
    double[] recall = {0.1, 0.2, 0.35, 0.4};
    // @formatter:off
    double expected =
        (recall[0] -       0.0) * (precision[0] +          1.0) / 2 +
        (recall[1] - recall[0]) * (precision[1] + precision[0]) / 2 +
        (recall[2] - recall[1]) * (precision[2] + precision[1]) / 2 +
        (recall[3] - recall[2]) * (precision[3] + precision[2]) / 2;
    // @formatter:on
    Assertions.assertEquals(expected, AucCalculator.auc(precision, recall));
  }
}
