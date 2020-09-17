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

package uk.ac.sussex.gdsc.core.clustering;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.MathUtils;

@SuppressWarnings({"javadoc"})
class ClusterTest {

  @Test
  void testCluster() {
    final ClusterPoint p1 = new ClusterPoint(1, 3, 4);
    final ClusterPoint p2 = new ClusterPoint(2, 5, 7);
    final ClusterPoint p3 = new ClusterPoint(3, 10, 10);
    final ClusterPoint p4 = new ClusterPoint(4, 10, 10);
    final Cluster c1 = new Cluster(p1);
    Assertions.assertEquals(p1.getX(), c1.getX());
    Assertions.assertEquals(p1.getY(), c1.getY());
    Assertions.assertEquals(1, c1.getSumOfWeights());
    Assertions.assertEquals(1, c1.getSize());
    Assertions.assertSame(p1, c1.getHeadClusterPoint());
    Assertions.assertNull(c1.getNext());
    Assertions.assertNull(c1.getClosest());
    Assertions.assertEquals(0, c1.getDistanceSquared());
    Assertions.assertEquals(0, c1.getNeighbour());
    Assertions.assertEquals(0, c1.getXBin());
    Assertions.assertEquals(0, c1.getYBin());
    c1.add(p2);
    double x = (p1.getX() + p2.getX()) / 2;
    double y = (p1.getY() + p2.getY()) / 2;
    Assertions.assertEquals(x, c1.getX());
    Assertions.assertEquals(y, c1.getY());
    Assertions.assertEquals(2, c1.getSumOfWeights());
    Assertions.assertEquals(2, c1.getSize());
    Assertions.assertSame(p2, c1.getHeadClusterPoint());

    final Cluster c2 = new Cluster(p3);
    Assertions.assertEquals(MathUtils.distance(x, y, c2.getX(), c2.getY()), c1.distance(c2), 1e-10);
    Assertions.assertEquals(MathUtils.distance2(x, y, c2.getX(), c2.getY()), c1.distance2(c2),
        1e-10);
    c1.add(c2);
    x = (p1.getX() + p2.getX() + p3.getX()) / 3;
    y = (p1.getY() + p2.getY() + p3.getY()) / 3;
    Assertions.assertEquals(x, c1.getX());
    Assertions.assertEquals(y, c1.getY());

    final Cluster c3 = new Cluster(p4);
    c1.setNext(c2);
    Assertions.assertSame(c2, c1.getNext());
    c1.setClosest(c3);
    Assertions.assertSame(c3, c1.getClosest());
    c1.setDistanceSquared(44);
    Assertions.assertEquals(44, c1.getDistanceSquared());
    c1.setNeighbour(13);
    Assertions.assertEquals(13, c1.getNeighbour());
    c1.incrementNeighbour();
    Assertions.assertEquals(14, c1.getNeighbour());
    c1.setXBin(42);
    Assertions.assertEquals(42, c1.getXBin());
    c1.setYBin(68);
    Assertions.assertEquals(68, c1.getYBin());

    // Link
    Assertions.assertFalse(c3.validLink());
    c3.link(c1, 13);
    Assertions.assertSame(c1, c3.getClosest());
    Assertions.assertEquals(13, c1.getDistanceSquared());
    Assertions.assertSame(c3, c1.getClosest());
    Assertions.assertEquals(13, c3.getDistanceSquared());
    Assertions.assertTrue(c3.validLink());

    // Failed link
    c2.setClosest(c1);
    Assertions.assertFalse(c2.validLink());
    c2.setDistanceSquared(14);
    c3.link(c2, 100);
    Assertions.assertSame(c1, c3.getClosest());
    Assertions.assertSame(c1, c2.getClosest());

    // Big added to a small
    c3.add(c1);
    Assertions.assertEquals(4, c3.getSize());
    // c1 is partially cleared
    Assertions.assertEquals(0, c1.getSumOfWeights());
    Assertions.assertEquals(0, c1.getSize());
    Assertions.assertNull(c1.getHeadClusterPoint());
    //Assertions.assertNull(c1.getNext());
    Assertions.assertNull(c1.getClosest());
    Assertions.assertEquals(0, c1.getDistanceSquared());
    //Assertions.assertEquals(0, c1.getNeighbour());
    //Assertions.assertEquals(0, c1.getXBin());
    //Assertions.assertEquals(0, c1.getYBin());

    // Add same XY
    x = Math.PI;
    y = Math.sqrt(2);
    final ClusterPoint p5 = new ClusterPoint(5, x, y);
    final ClusterPoint p6 = new ClusterPoint(6, x, y);
    final ClusterPoint p7 = new ClusterPoint(7, x, y);
    final Cluster c4 = new Cluster(p5);
    c4.add(p6);
    c4.add(p7);
    Assertions.assertEquals(x, c4.getX());
    Assertions.assertEquals(y, c4.getY());
  }
}
