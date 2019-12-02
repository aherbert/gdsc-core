package uk.ac.sussex.gdsc.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class DoubleDataTest {
  @Test
  public void canWrapDoubleArray() {
    Assertions.assertThrows(NullPointerException.class, () -> DoubleData.wrap(null));
    final double[] data = {0, 1, 2, 3, 4};
    final DoubleData dd = DoubleData.wrap(data);
    Assertions.assertEquals(data.length, dd.size());
    Assertions.assertSame(data, dd.values());
  }
}
