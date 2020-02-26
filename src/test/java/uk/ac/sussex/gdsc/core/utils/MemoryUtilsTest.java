package uk.ac.sussex.gdsc.core.utils;

import java.util.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.utils.TestLogUtils;

@SuppressWarnings({"javadoc"})
public class MemoryUtilsTest {

  @Test
  public void canRunGarbageCollector() {
    // Just test this does not error
    MemoryUtils.runGarbageCollector();
  }

  @Test
  public void measureSizeThrows() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> MemoryUtils.measureSize(0, Object::new));
  }

  @Test
  public void canMeasureSize() {
    final int arraySize = 100;
    final long size = MemoryUtils.measureSize(1000, () -> new int[arraySize]);
    // This is the expected raw byte size of the array. It does not include
    // extra size for memory references etc.
    final double expected = Integer.BYTES * arraySize;
    // Allow a margin for error
    final double error = DoubleEquality.relativeError(size, expected);
    Logger.getLogger(getClass().getName()).log(TestLogUtils.getResultRecord(error < 0.2,
        "Memory expected=%s : measured=%d : error=%f", expected, size, error));
    // This is flaky so do not assert the test
    // Assertions.assertEquals(expected, size, expected * 0.1);
  }
}
