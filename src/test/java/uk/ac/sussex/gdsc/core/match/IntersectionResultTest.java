package uk.ac.sussex.gdsc.core.match;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link IntersectionResult}.
 */
@SuppressWarnings({"javadoc"})
public class IntersectionResultTest {
  @Test
  public void canCreate() {
    final int tp = 5;
    final int fp = 1;
    final int fn = 3;
    final IntersectionResult match = new IntersectionResult(tp, fp, fn);
    Assertions.assertEquals(tp, match.getTruePositives(), "tp");
    Assertions.assertEquals(fp, match.getFalsePositives(), "fp");
    Assertions.assertEquals(fn, match.getFalseNegatives(), "fn");
    Assertions.assertEquals(tp + fp, match.getNumberPredicted(), "predicted");
    Assertions.assertEquals(tp + fn, match.getNumberActual(), "actual");
  }
}
