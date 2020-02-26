package uk.ac.sussex.gdsc.core.math.interpolation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;

/**
 * Test for {@link FloatCubicSplineData}.
 */
@SuppressWarnings({"javadoc"})
public class FloatCubicSplineDataTest {
  @Test
  public void testToArray() {
    final float[] exp = SimpleArrayUtils.newArray(64, 1.0f, 1.0f);
    FloatCubicSplineData data = new FloatCubicSplineData(exp);
    final float[] obs = new float[64];
    data.toArray(obs);
    Assertions.assertArrayEquals(exp, obs);
  }

  @Test
  public void testScale() {
    final float[] exp = SimpleArrayUtils.newArray(64, 1.0f, 1.0f);
    final int scale = 3;
    FloatCubicSplineData data = new FloatCubicSplineData(exp).scale(scale);
    final float[] obs = new float[64];
    data.toArray(obs);
    SimpleArrayUtils.multiply(exp, scale);
    Assertions.assertArrayEquals(exp, obs);
  }
}
