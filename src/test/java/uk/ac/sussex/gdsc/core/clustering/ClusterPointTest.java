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
 * Copyright (C) 2011 - 2021 Alex Herbert
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

package uk.ac.sussex.gdsc.core.clustering;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.MathUtils;

@SuppressWarnings({"javadoc"})
class ClusterPointTest {

  @Test
  void testClusterPoint() {
    final int id = 13;
    final double x = 1.2345;
    final double y = 2.687;
    final double x2 = 6.78;
    final double y2 = -9.87;
    final double weight = 99.543;
    final int start = 11;
    final int end = 15;

    ClusterPoint p1 = ClusterPoint.newClusterPoint(id, x, y);
    final ClusterPoint p2 = new ClusterPoint(0, x2, y2);
    Assertions.assertEquals(id, p1.getId());
    Assertions.assertEquals(x, p1.getX());
    Assertions.assertEquals(y, p1.getY());
    Assertions.assertEquals(1, p1.getWeight());
    Assertions.assertEquals(0, p1.getStartTime());
    Assertions.assertEquals(0, p1.getEndTime());
    Assertions.assertNull(p1.getNext());

    p1.setNext(p2);
    Assertions.assertSame(p2, p1.getNext());

    p1 = ClusterPoint.newClusterPoint(id, x, y, weight);
    Assertions.assertEquals(id, p1.getId());
    Assertions.assertEquals(x, p1.getX());
    Assertions.assertEquals(y, p1.getY());
    Assertions.assertEquals(weight, p1.getWeight());
    Assertions.assertEquals(0, p1.getStartTime());
    Assertions.assertEquals(0, p1.getEndTime());
    Assertions.assertNull(p1.getNext());

    p1 = ClusterPoint.newTimeClusterPoint(id, x, y, start, end);
    Assertions.assertEquals(id, p1.getId());
    Assertions.assertEquals(x, p1.getX());
    Assertions.assertEquals(y, p1.getY());
    Assertions.assertEquals(1, p1.getWeight());
    Assertions.assertEquals(start, p1.getStartTime());
    Assertions.assertEquals(end, p1.getEndTime());
    Assertions.assertNull(p1.getNext());

    p1 = ClusterPoint.newTimeClusterPoint(id, x, y, weight, start, end);
    Assertions.assertEquals(id, p1.getId());
    Assertions.assertEquals(x, p1.getX());
    Assertions.assertEquals(y, p1.getY());
    Assertions.assertEquals(weight, p1.getWeight());
    Assertions.assertEquals(start, p1.getStartTime());
    Assertions.assertEquals(end, p1.getEndTime());
    Assertions.assertNull(p1.getNext());

    Assertions.assertEquals(MathUtils.distance(x, y, x2, y2), p1.distance(p2));
    Assertions.assertEquals(MathUtils.distance2(x, y, x2, y2), p1.distanceSquared(p2));
  }
}
