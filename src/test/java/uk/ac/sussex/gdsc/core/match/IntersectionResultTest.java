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
    Assertions.assertEquals(tp, match.getIntersection(), "intersection");
    Assertions.assertEquals(fp, match.getSizeAMinusIntersection(), "A - intersection");
    Assertions.assertEquals(fn, match.getSizeBMinusIntersection(), "B - intersection");
    Assertions.assertEquals(tp + fp, match.getSizeA(), "A");
    Assertions.assertEquals(tp + fn, match.getSizeB(), "B");
  }
}
