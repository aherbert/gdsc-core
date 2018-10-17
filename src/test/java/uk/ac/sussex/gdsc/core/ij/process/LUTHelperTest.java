package uk.ac.sussex.gdsc.core.ij.process;

import uk.ac.sussex.gdsc.core.ij.process.LutHelper.DefaultLutMapper;
import uk.ac.sussex.gdsc.core.ij.process.LutHelper.LutMapper;
import uk.ac.sussex.gdsc.core.ij.process.LutHelper.NonZeroLutMapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class LUTHelperTest {
  @Test
  public void canMapTo0to255() {
    mapTo0to255(0, 0);
    mapTo0to255(0, 1);
    mapTo0to255(0, 255);
    mapTo0to255(0, 1000);

    mapTo0to255(4.3f, 32.5f);
    mapTo0to255(-4.3f, 0f);
    mapTo0to255(-4.3f, 32.5f);
    mapTo0to255(0f, 32.5f);
  }

  @Test
  public void canMapTo1to255() {
    mapTo1to255(1, 1);
    mapTo1to255(1, 2);
    mapTo1to255(1, 255);
    mapTo1to255(1, 1000);

    mapTo1to255(4.3f, 32.5f);
    mapTo1to255(-4.3f, 0f);
    mapTo1to255(-4.3f, 32.5f);
    mapTo1to255(0f, 32.5f);
  }

  private static void mapTo0to255(float min, float max) {
    final LutMapper map = new DefaultLutMapper(min, max);
    Assertions.assertEquals(0, map.map(min));
    if (max != min) {
      Assertions.assertEquals(255, map.map(max));
    }
  }

  private static void mapTo1to255(float min, float max) {
    final LutMapper map = new NonZeroLutMapper(min, max);
    Assertions.assertEquals(1, map.map(min));
    if (max != min) {
      Assertions.assertEquals(255, map.map(max));
    }
  }
}
