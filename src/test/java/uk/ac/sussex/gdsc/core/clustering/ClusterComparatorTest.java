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

import java.util.ArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class ClusterComparatorTest {

  @Test
  void canSortClusters() {
    // Sort order is size, X, Y, sumW.
    // Build a list of differing size, then x, then y, then weight
    final ArrayList<Cluster> list = new ArrayList<>();
    final Cluster c1 = new Cluster(new ClusterPoint(0, 0, 0));
    final Cluster c2 = new Cluster(new ClusterPoint(0, 1, 0)); // new x
    final Cluster c3 = new Cluster(new ClusterPoint(0, 1, 1)); // new y
    final Cluster c4 = new Cluster(new ClusterPoint(0, 1, 1, 2)); // new weight
    final Cluster c5 = new Cluster(new ClusterPoint(0, 0, 0)); // new size
    c5.add(new ClusterPoint(0, 0, 0));
    list.add(c1);
    list.add(c2);
    list.add(c3);
    list.add(c4);
    list.add(c5);

    final ClusterComparator comparator = ClusterComparator.getInstance();

    // Test the comparison is reversible
    for (final Cluster first : list) {
      for (final Cluster second : list) {
        final int r1 = comparator.compare(first, second);
        final int r2 = comparator.compare(second, first);
        Assertions.assertEquals(r1, -r2, "Comparator not reversible");
      }
    }

    // Check order
    Assertions.assertEquals(0, comparator.compare(c1, c1), "Same cluster");
    Assertions.assertEquals(-1, comparator.compare(c1, c2), "Lower x not before");
    Assertions.assertEquals(-1, comparator.compare(c2, c3), "Lower y not before");
    Assertions.assertEquals(-1, comparator.compare(c3, c4), "Lower weight not before");
    Assertions.assertEquals(-1, comparator.compare(c4, c5), "Lower size not before");
  }
}
