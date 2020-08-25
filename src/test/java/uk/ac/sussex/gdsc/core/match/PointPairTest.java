/*-
 * #%L
 * Genome Damage and Stability Centre ImageJ Core Package
 *
 * Contains code used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2020 Alex Herbert
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package uk.ac.sussex.gdsc.core.match;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link PointPair}.
 */
@SuppressWarnings({"javadoc"})
class PointPairTest {
  @Test
  void canCreate() {
    final Coordinate point1 = new BasePoint(0, 1);
    final Coordinate point2 = new BasePoint(2, 4);
    final PointPair pair = new PointPair(point1, point2);
    Assertions.assertSame(point1, pair.getPoint1(), "point1");
    Assertions.assertSame(point2, pair.getPoint2(), "point2");
  }

  @Test
  void testDistance() {
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
  void testDistanceWithNullPoint() {
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
