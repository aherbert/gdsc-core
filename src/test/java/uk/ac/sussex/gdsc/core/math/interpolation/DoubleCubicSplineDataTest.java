package uk.ac.sussex.gdsc.core.math.interpolation;

import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link DoubleCubicSplineData}.
 */
@SuppressWarnings({"javadoc"})
public class DoubleCubicSplineDataTest {
  @Test
  public void testToArray() {
    final double[] exp = SimpleArrayUtils.newArray(64, 1.0, 1.0);
    DoubleCubicSplineData data = new DoubleCubicSplineData(exp);
    final double[] obs = new double[64];
    data.toArray(obs);
    Assertions.assertArrayEquals(exp, obs);
  }

  @Test
  public void testScale() {
    final double[] exp = SimpleArrayUtils.newArray(64, 1.0, 1.0);
    final int scale = 3;
    DoubleCubicSplineData data = new DoubleCubicSplineData(exp).scale(scale);
    final double[] obs = new double[64];
    data.toArray(obs);
    SimpleArrayUtils.multiply(exp, scale);
    Assertions.assertArrayEquals(exp, obs);
  }
}
