/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2023 Alex Herbert
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

@SuppressWarnings({"javadoc"})
class TimeClusterTest {

  @Test
  void testTimeCluster() {
    final int start1 = 13;
    final int end1 = 23;
    final ClusterPoint p1 = new ClusterPoint(1, 3, 4, 1, start1, end1);
    final TimeCluster c1 = new TimeCluster(p1);
    Assertions.assertEquals(start1, c1.getStartTime());
    Assertions.assertEquals(end1, c1.getEndTime());

    final int start2 = 16;
    final int end2 = 25;
    final ClusterPoint p2 = new ClusterPoint(1, 3, 4, 1, start2, end2);
    c1.add(p2);
    Assertions.assertEquals(start1, c1.getStartTime());
    Assertions.assertEquals(end2, c1.getEndTime());

    final int start3 = 10;
    final int end3 = 12;
    final ClusterPoint p3 = new ClusterPoint(1, 3, 4, 1, start3, end3);
    c1.add(new TimeCluster(p3));
    Assertions.assertEquals(start3, c1.getStartTime());
    Assertions.assertEquals(end2, c1.getEndTime());

    c1.setPulseTime(45);
    Assertions.assertEquals(45, c1.getPulseTime());
  }

  @Test
  void testGap() {
    final ClusterPoint p1 = new ClusterPoint(1, 0, 0, 0, 1, 2);
    final ClusterPoint p2 = new ClusterPoint(1, 0, 0, 0, 1, 1);
    final ClusterPoint p3 = new ClusterPoint(1, 0, 0, 0, 1, 2);
    final ClusterPoint p4 = new ClusterPoint(1, 0, 0, 0, 2, 2);
    final ClusterPoint p5 = new ClusterPoint(1, 0, 0, 0, 3, 3);
    final TimeCluster c1 = new TimeCluster(p1);
    Assertions.assertEquals(0, c1.gap(new TimeCluster(p2)));
    Assertions.assertEquals(0, c1.gap(new TimeCluster(p3)));
    Assertions.assertEquals(0, c1.gap(new TimeCluster(p4)));
    Assertions.assertEquals(1, c1.gap(new TimeCluster(p5)));
  }

  @Test
  void testValidUnion() {
    final ClusterPoint p1 = new ClusterPoint(1, 0, 0, 0, 1, 2);
    final ClusterPoint p2 = new ClusterPoint(1, 0, 0, 0, 2, 2);
    final ClusterPoint p3 = new ClusterPoint(1, 0, 0, 0, 3, 3);
    final ClusterPoint p4 = new ClusterPoint(1, 0, 0, 0, 1, 1);
    final TimeCluster c1 = new TimeCluster(p1);
    final TimeCluster c2 = new TimeCluster(p2);
    final TimeCluster c3 = new TimeCluster(p3);
    final TimeCluster c4 = new TimeCluster(p4);
    Assertions.assertTrue(c1.validUnion(c2));
    Assertions.assertFalse(c1.validUnionRange(c2));
    Assertions.assertTrue(c1.validUnion(c3));
    Assertions.assertTrue(c1.validUnionRange(c3));
    Assertions.assertFalse(c1.validUnion(c4));
    Assertions.assertFalse(c1.validUnionRange(c4));
  }
}
