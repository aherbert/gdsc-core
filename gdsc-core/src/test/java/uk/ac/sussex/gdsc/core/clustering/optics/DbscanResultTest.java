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
 * Copyright (C) 2011 - 2022 Alex Herbert
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

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Arrays;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.match.RandIndex;
import uk.ac.sussex.gdsc.core.math.hull.ConvexHull2d;
import uk.ac.sussex.gdsc.core.math.hull.Hull;
import uk.ac.sussex.gdsc.test.rng.RngFactory;

@SuppressWarnings({"javadoc"})
class DbscanResultTest {
  @Test
  void testDbscanResult() {
    final float[] x = {0, 1, 0, 1, 100, 10, 11, 10, 11, 12};
    final float[] y = {0, 0, 1, 1, 100, 10, 10, 11, 11, 12};
    final OpticsManager om = new OpticsManager(x, y, 0);
    final float distance = 2;
    final int minPoints = 3;
    final DbscanResult result = om.dbscan(distance, minPoints);
    Assertions.assertEquals(distance, result.getGeneratingDistance());
    Assertions.assertEquals(minPoints, result.getMinPoints());

    final int[] order = result.getOrder();
    final IntOpenHashSet set = new IntOpenHashSet(order);
    Assertions.assertEquals(x.length, set.size());
    Assertions.assertNull(result.getClusters());
    Assertions.assertFalse(result.hasHulls());
    for (int i = 0; i < order.length; i++) {
      Assertions.assertTrue(set.remove(result.get(i).parent + 1));
    }

    // Cannot compute hulls when no clusters
    result.computeHulls(ConvexHull2d.newBuilder());
    Assertions.assertNull(result.getHull(1));
    Assertions.assertNull(result.getBounds(1));

    // Compute with non-core points
    result.extractClusters(false);
    final int[] expected = {1, 1, 1, 1, 0, 2, 2, 2, 2, 2};
    int[] c = result.getClusters();
    Assertions.assertEquals(1.0, RandIndex.randIndex(expected, c));
    assertIdEquals(new int[] {0, 1, 2, 3}, result.getParents(new int[] {1}));
    assertIdEquals(new int[] {5, 6, 7, 8, 9}, result.getParents(new int[] {2}));
    assertIdEquals(new int[] {0, 1, 2, 3, 5, 6, 7, 8, 9}, result.getParents(new int[] {1, 2}));
    assertIdEquals(new int[] {}, result.getParents(null));
    assertIdEquals(new int[] {}, result.getParents(new int[] {0}));
    assertIdEquals(new int[] {}, result.getParents(new int[] {10}));
    assertIdEquals(new int[] {5, 6, 7, 8, 9}, result.getParents(new int[] {2, 2}));

    // Compute with core points. The final point is not a core point.
    result.extractClusters(true);
    expected[9] = 0;
    c = result.getClusters();
    Assertions.assertEquals(1.0, RandIndex.randIndex(expected, c));
    assertIdEquals(new int[] {0, 1, 2, 3}, result.getParents(new int[] {1}));
    assertIdEquals(new int[] {5, 6, 7, 8}, result.getParents(new int[] {2}));
    assertIdEquals(new int[] {0, 1, 2, 3, 5, 6, 7, 8}, result.getParents(new int[] {1, 2}));

    // Should be able to compute the hulls
    result.computeHulls(ConvexHull2d.newBuilder());
    Assertions.assertTrue(result.hasHulls());
    final Hull h1 = result.getHull(1);
    final float[] b1 = result.getBounds(1);
    Assertions.assertNotNull(h1);
    Assertions.assertNotNull(b1);
    // Do not ignore request to build new hulls
    result.computeHulls(ConvexHull2d.newBuilder());
    Assertions.assertNotSame(h1, result.getHull(1));
    Assertions.assertNotSame(b1, result.getBounds(1));

    Assertions.assertNull(result.getHull(0));
    Assertions.assertNull(result.getBounds(0));
    Assertions.assertNull(result.getHull(10));
    Assertions.assertNull(result.getBounds(10));
  }

  @Test
  void testScrambleClusters() {
    final float[] x = {0, 1, 0, 1, 100, 10, 11, 10, 11, 12};
    final float[] y = {0, 0, 1, 1, 100, 10, 10, 11, 11, 12};
    final OpticsManager om = new OpticsManager(x, y, new float[x.length], 0);
    final float distance = 2;
    final int minPoints = 3;
    final DbscanResult result = om.dbscan(distance, minPoints);

    // Compute with non-core points
    result.extractClusters(false);
    final int[] expected = {1, 1, 1, 1, 0, 2, 2, 2, 2, 2};
    final int[] c = result.getClusters();
    Assertions.assertEquals(1.0, RandIndex.randIndex(expected, c));

    final UniformRandomProvider rng = RngFactory.create(56871256342L);
    boolean changed = false;
    for (int i = 0; i < 5; i++) {
      result.scrambleClusters(rng);
      result.extractClusters(false);
      final int[] c2 = result.getClusters();
      Assertions.assertEquals(1.0, RandIndex.randIndex(expected, c2));
      changed |= Arrays.equals(c, c2);
    }
    Assertions.assertTrue(changed);
  }

  @Test
  void testScrambleClustersWithNoClusters() {
    final float[] x = {0, 1};
    final float[] y = {0, 0};
    final OpticsManager om = new OpticsManager(x, y, 0);
    final float distance = 2;
    final int minPoints = 3;
    final DbscanOrder[] dbscanResults = new DbscanOrder[x.length];
    dbscanResults[0] = new DbscanOrder(0, 0, 0);
    dbscanResults[1] = new DbscanOrder(1, 0, 0);
    final DbscanResult result = new DbscanResult(om, minPoints, distance, dbscanResults);

    // Compute with non-core points
    result.extractClusters(false);
    final int[] expected = {0, 0};
    final int[] c = result.getClusters();
    Assertions.assertArrayEquals(expected, c);

    final UniformRandomProvider rng = null;
    result.scrambleClusters(rng);
    result.extractClusters(false);
    final int[] c2 = result.getClusters();
    Assertions.assertArrayEquals(expected, c2);
  }

  @Test
  void testComputeHulls3d() {
    final float[] x = {0, 1, 0, 1, 100, 10, 11, 10, 11, 12};
    final float[] y = {0, 0, 1, 1, 100, 10, 10, 11, 11, 12};
    final OpticsManager om2 = new OpticsManager(x, y, 0);
    final OpticsManager om3 = new OpticsManager(x, y, new float[x.length], 0);
    final float distance = 2;
    final int minPoints = 3;
    final DbscanResult result2 = om2.dbscan(distance, minPoints);
    final DbscanResult result3 = om3.dbscan(distance, minPoints);

    // Compute with non-core points
    result2.extractClusters(false);
    result3.extractClusters(false);
    Assertions.assertArrayEquals(result2.getClusters(), result3.getClusters());

    result2.computeHulls(ConvexHull2d.newBuilder());
    result3.computeHulls(ConvexHull2d.newBuilder());
    Assertions.assertArrayEquals(result2.getHull(1).getVertices(),
        result3.getHull(1).getVertices());
  }

  private static void assertIdEquals(int[] expected, int[] actual) {
    Arrays.sort(actual);
    Assertions.assertArrayEquals(expected, actual);
  }
}
