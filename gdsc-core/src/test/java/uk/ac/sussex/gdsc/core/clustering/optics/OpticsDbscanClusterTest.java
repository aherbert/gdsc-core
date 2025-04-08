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
 * Copyright (C) 2011 - 2025 Alex Herbert
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
class OpticsDbscanClusterTest {
  @Test
  void testNoLevel() {
    final int start = 13;
    final int end = 42;
    final int clusterId = 6;
    final int size = 7;
    final OpticsDbscanCluster c = new OpticsDbscanCluster(start, end, clusterId, size);
    Assertions.assertEquals(start, c.start);
    Assertions.assertEquals(end, c.end);
    Assertions.assertEquals(clusterId, c.getClusterId());
    Assertions.assertEquals(end - start + 1, c.length());
    Assertions.assertEquals(size, c.size());
    Assertions.assertEquals(0, c.getLevel());
    Assertions.assertEquals(0, c.getNumberOfChildren());
    final String s = c.toString();
    Assertions.assertTrue(s.contains(Integer.toString(start)));
    Assertions.assertTrue(s.contains(Integer.toString(end)));
    Assertions.assertTrue(s.contains(Integer.toString(clusterId)));
    Assertions.assertTrue(s.contains(Integer.toString(size)));
  }
}
