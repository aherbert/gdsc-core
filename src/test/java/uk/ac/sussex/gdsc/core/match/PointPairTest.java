package uk.ac.sussex.gdsc.core.match;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link PointPair}.
 */
@SuppressWarnings({"javadoc"})
public class PointPairTest {
  @Test
  public void canCreate() {
    final Coordinate point1 = new BasePoint(0, 1);
    final Coordinate point2 = new BasePoint(2, 4);
    final PointPair pair = new PointPair(point1, point2);
    Assertions.assertSame(point1, pair.getPoint1(), "point1");
    Assertions.assertSame(point2, pair.getPoint2(), "point2");
  }

  @Test
  public void testDistance() {
    final float x1 = 0.678f;
    final float y1 = 2.23434f;
    final float z1 = 3.234f;
    final float x2 = 45.65f;
    final float y2 = -2.789f;
    final float z2 = -3.79887f;
    final Coordinate point1 = new BasePoint(x1, y1, z1);
    final Coordinate point2 = new BasePoint(x2, y2, z2);
    final PointPair pair = new PointPair(point1, point2);
    // @formatter:off
    Assertions.assertEquals(
        point1.distanceXySquared(point2), pair.getXyDistanceSquared(), "distanceXySquared");
    Assertions.assertEquals(
        point1.distanceXyzSquared(point2), pair.getXyzDistanceSquared(), "distanceXyzSquared");
    Assertions.assertEquals(
        point1.distanceXy(point2), pair.getXyDistance(), "distanceXy");
    Assertions.assertEquals(
        point1.distanceXyz(point2), pair.getXyzDistance(), "distanceXyz");
    // @formatter:on
  }

  @Test
  public void testDistanceWithNullPoint() {
    final float x1 = 0.678f;
    final float y1 = 2.23434f;
    final float z1 = 3.234f;
    final Coordinate point1 = new BasePoint(x1, y1, z1);
    final Coordinate point2 = null;
    PointPair pair = new PointPair(point1, point2);
    Assertions.assertEquals(-1, pair.getXyDistanceSquared(), "distanceXySquared");
    Assertions.assertEquals(-1, pair.getXyzDistanceSquared(), "distanceXyzSquared");
    Assertions.assertEquals(-1, pair.getXyDistance(), "distanceXy");
    Assertions.assertEquals(-1, pair.getXyzDistance(), "distanceXyz");

    pair = new PointPair(point2, point1);
    Assertions.assertEquals(-1, pair.getXyDistanceSquared(), "distanceXySquared");
    Assertions.assertEquals(-1, pair.getXyzDistanceSquared(), "distanceXyzSquared");
    Assertions.assertEquals(-1, pair.getXyDistance(), "distanceXy");
    Assertions.assertEquals(-1, pair.getXyzDistance(), "distanceXyz");
  }
}
