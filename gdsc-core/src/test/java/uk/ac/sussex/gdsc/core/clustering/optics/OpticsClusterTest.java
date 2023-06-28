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

package uk.ac.sussex.gdsc.core.clustering.optics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class OpticsClusterTest {
  @Test
  void testNoLevel() {
    final int start = 13;
    final int end = 42;
    final int clusterId = 6;
    final OpticsCluster c = new OpticsCluster(start, end, clusterId);
    Assertions.assertEquals(start, c.start);
    Assertions.assertEquals(end, c.end);
    Assertions.assertEquals(clusterId, c.getClusterId());
    Assertions.assertEquals(end - start + 1, c.length());
    Assertions.assertEquals(end - start + 1, c.size());
    Assertions.assertEquals(0, c.getLevel());
    Assertions.assertEquals(0, c.getNumberOfChildren());
    final String s = c.toString();
    Assertions.assertTrue(s.contains(Integer.toString(start)));
    Assertions.assertTrue(s.contains(Integer.toString(end)));
    Assertions.assertTrue(s.contains(Integer.toString(clusterId)));
  }

  @Test
  void testChildren() {
    final int start = 13;
    final int end = 42;
    final int clusterId = 3;
    // children are created first and added to larger clusters
    final OpticsCluster c = new OpticsCluster(start + 2, end - 2, clusterId);
    Assertions.assertEquals(0, c.getLevel());
    Assertions.assertEquals(0, c.getNumberOfChildren());
    final OpticsCluster c2 = new OpticsCluster(start + 1, end - 1, clusterId + 1);
    c2.addChildCluster(c);
    Assertions.assertEquals(1, c2.getNumberOfChildren());
    Assertions.assertEquals(1, c.getLevel());
    Assertions.assertEquals(0, c2.getLevel());
    final OpticsCluster c3 = new OpticsCluster(start, end, clusterId + 2);
    c3.addChildCluster(c2);
    Assertions.assertEquals(1, c3.getNumberOfChildren());
    Assertions.assertEquals(1, c2.getNumberOfChildren());
    Assertions.assertEquals(2, c.getLevel());
    Assertions.assertEquals(1, c2.getLevel());
    Assertions.assertEquals(0, c3.getLevel());
  }
}
