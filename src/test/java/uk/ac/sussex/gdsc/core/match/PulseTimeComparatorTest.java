package uk.ac.sussex.gdsc.core.match;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link PulseTimeComparator}.
 */
@SuppressWarnings({"javadoc"})
public class PulseTimeComparatorTest {
  @Test
  public void canCompare() {
    // Compare using start
    assertOverlap(0, 1, 1, 1, 1);
    assertOverlap(-1, 1, 1, 2, 2);
    assertOverlap(1, 2, 2, 1, 1);
    // Equal start should compare using end, lowest first
    assertOverlap(1, 1, 2, 1, 1);
    assertOverlap(-1, 1, 1, 1, 2);
  }

  private static void assertOverlap(int result, int start1, int end1, int start2, int end2) {
    final Pulse data1 = new Pulse(0f, 0f, start1, end1);
    final Pulse data2 = new Pulse(0f, 0f, start2, end2);
    Assertions.assertEquals(result, PulseTimeComparator.getInstance().compare(data1, data2));
  }
}
