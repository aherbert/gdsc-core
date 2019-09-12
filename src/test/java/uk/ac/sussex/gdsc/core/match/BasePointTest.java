package uk.ac.sussex.gdsc.core.match;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link BasePoint}.
 */
@SuppressWarnings({"javadoc"})
public class BasePointTest {
  @Test
  public void canCreate() {
    final float x = 4.567f;
    final float y = 9.958f;
    final float z = 456.21323f;
    BasePoint data = new BasePoint(x, y, z);
    Assertions.assertEquals(x, data.getX(), "X");
    Assertions.assertEquals(y, data.getY(), "Y");
    Assertions.assertEquals(z, data.getZ(), "Z");

    Assertions.assertEquals((int) x, data.getXint(), "Xint");
    Assertions.assertEquals((int) y, data.getYint(), "Yint");
    Assertions.assertEquals((int) z, data.getZint(), "Zint");

    data = new BasePoint(x, y, 0);
    Assertions.assertEquals(x, data.getX(), "X");
    Assertions.assertEquals(y, data.getY(), "Y");
    Assertions.assertEquals(0.0f, data.getZ(), "Z");
  }

  @Test
  public void testEquals() {
    final float x = 4.567f;
    final float y = 9.958f;
    final float z = 456.21323f;
    final BasePoint data = new BasePoint(x, y, z);
    Assertions.assertTrue(data.equals(data));
    Assertions.assertFalse(data.equals(null));
    Assertions.assertFalse(data.equals(new Object()));

    final float[] shifts = {0, 1};
    for (final float dx : shifts) {
      for (final float dy : shifts) {
        for (final float dz : shifts) {
          if (dx + dy + dz != 0) {
            Assertions.assertFalse(data.equals(new BasePoint(x + dx, y + dy, z + dz)));
          } else {
            Assertions.assertTrue(data.equals(new BasePoint(x + dx, y + dy, z + dz)));
          }
        }
      }
    }
  }

  @Test
  public void testHashCode() {
    final float x = 4.567f;
    final float y = 9.958f;
    final float z = 456.21323f;
    final BasePoint data1 = new BasePoint(x, y, z);
    final BasePoint data2 = new BasePoint(x, y, z);
    final BasePoint data3 = new BasePoint(x, y);
    Assertions.assertEquals(data1.hashCode(), data2.hashCode());
    Assertions.assertNotEquals(data1.hashCode(), data3.hashCode());
  }

  @Test
  public void testShift() {
    final float x = 4.567f;
    final float y = 9.958f;
    final float z = 456.21323f;
    final float[] shifts = {4.56f, 0f, -77.2f};
    for (final float dx : shifts) {
      for (final float dy : shifts) {
        for (final float dz : shifts) {
          final BasePoint data = new BasePoint(x, y, z).shift(dx, dy, dz);
          Assertions.assertEquals(x + dx, data.getX(), "X");
          Assertions.assertEquals(y + dy, data.getY(), "Y");
          Assertions.assertEquals(z + dz, data.getZ(), "Z");
        }
      }
    }
  }

  @Test
  public void testDistance() {
    final float x1 = 0.678f;
    final float y1 = 2.23434f;
    final float z1 = 3.234f;
    final float x2 = 45.65f;
    final float y2 = -2.789f;
    final float z2 = -3.79887f;
    final BasePoint data1 = new BasePoint(x1, y1, z1);
    final BasePoint data2 = new BasePoint(x2, y2, z2);
    final double delta = 1e-10;
    // @formatter:off
    Assertions.assertEquals(pow2(x2 - x1) + pow2(y2 - y1),
        data1.distanceXySquared(data2), delta, "distanceXySquared");
    Assertions.assertEquals(pow2(x2 - x1) + pow2(y2 - y1) + pow2(z2 - z1),
        data1.distanceXyzSquared(data2), delta, "distanceXyzSquared");
    Assertions.assertEquals(Math.sqrt(pow2(x2 - x1) + pow2(y2 - y1)),
        data1.distanceXy(data2), delta, "distanceXy");
    Assertions.assertEquals(Math.sqrt(pow2(x2 - x1) + pow2(y2 - y1) + pow2(z2 - z1)),
        data1.distanceXyz(data2), delta, "distanceXyz");
    Assertions.assertEquals(pow2(x2 - x1) + pow2(y2 - y1),
        data1.distanceSquared(x2, y2), delta, "distanceSquared x,y");
    Assertions.assertEquals(pow2(x2 - x1) + pow2(y2 - y1) + pow2(z2 - z1),
        data1.distanceSquared(x2, y2, z2), delta, "distanceSquared x,y,z");
    Assertions.assertEquals(Math.sqrt(pow2(x2 - x1) + pow2(y2 - y1)),
        data1.distance(x2, y2), delta, "distance x,y");
    Assertions.assertEquals(Math.sqrt(pow2(x2 - x1) + pow2(y2 - y1) + pow2(z2 - z1)),
        data1.distance(x2, y2, z2), delta, "distance x,y,z");
    // @formatter:on
  }

  private static double pow2(double value) {
    return value * value;
  }
}
