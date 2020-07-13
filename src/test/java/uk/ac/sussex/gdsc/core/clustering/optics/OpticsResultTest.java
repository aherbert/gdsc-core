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

package uk.ac.sussex.gdsc.core.clustering.optics;

import gnu.trove.set.hash.TIntHashSet;
import java.util.Arrays;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.clustering.optics.OpticsResult.SteepDownArea;
import uk.ac.sussex.gdsc.core.clustering.optics.OpticsResult.SteepUpArea;
import uk.ac.sussex.gdsc.core.match.RandIndex;
import uk.ac.sussex.gdsc.core.math.hull.ConvexHull2d;
import uk.ac.sussex.gdsc.core.math.hull.Hull;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

@SuppressWarnings({"javadoc"})
public class OpticsResultTest {
  @Test
  public void testSteepDownArea() {
    final int start = 13;
    final int end = 42;
    final double max = 56.890;
    final double mib = 1.234;
    final SteepDownArea sda = new SteepDownArea(start, end, max);
    Assertions.assertEquals(start, sda.start);
    Assertions.assertEquals(end, sda.end);
    Assertions.assertEquals(max, sda.maximum);
    sda.mib = mib;
    final String s = sda.toString();
    Assertions.assertTrue(s.contains(Integer.toString(start)));
    Assertions.assertTrue(s.contains(Integer.toString(end)));
    Assertions.assertTrue(s.contains(Double.toString(max)));
    Assertions.assertTrue(s.contains(Double.toString(mib)));
  }

  @Test
  public void testSteepUpArea() {
    final int start = 13;
    final int end = 42;
    final double max = 56.890;
    final SteepUpArea sda = new SteepUpArea(start, end, max);
    Assertions.assertEquals(start, sda.start);
    Assertions.assertEquals(end, sda.end);
    Assertions.assertEquals(max, sda.maximum);
    final String s = sda.toString();
    Assertions.assertTrue(s.contains(Integer.toString(start)));
    Assertions.assertTrue(s.contains(Integer.toString(end)));
    Assertions.assertTrue(s.contains(Double.toString(max)));
  }

  @Test
  public void testProfiles() {
    final float distance = 2;
    final int minPoints = 3;
    final OpticsOrder[] opticsResults = new OpticsOrder[5];
    opticsResults[0] = new OpticsOrder(0, -1, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    opticsResults[1] = new OpticsOrder(1, 0, 3, 4);
    opticsResults[2] = new OpticsOrder(3, 1, 2.5, 3);
    opticsResults[3] = new OpticsOrder(2, 3, 2, 2.5);
    opticsResults[4] = new OpticsOrder(4, 2, 3.5, 5);
    final float[] x = {opticsResults.length};
    final OpticsManager om = new OpticsManager(x, x, 1);
    final OpticsResult result = new OpticsResult(om, minPoints, distance, opticsResults);

    double[] d;
    // Optics order
    d = result.getReachabilityDistanceProfile(false);
    Assertions.assertArrayEquals(new double[] {Double.POSITIVE_INFINITY, 4, 3, 2.5, 5}, d);
    d = result.getReachabilityDistanceProfile(true);
    Assertions.assertArrayEquals(new double[] {distance, 4, 3, 2.5, 5}, d);
    // Original order
    d = result.getReachabilityDistance(false);
    Assertions.assertArrayEquals(new double[] {Double.POSITIVE_INFINITY, 4, 2.5, 3, 5}, d);
    d = result.getReachabilityDistance(true);
    Assertions.assertArrayEquals(new double[] {distance, 4, 2.5, 3, 5}, d);
    // Optics order
    d = result.getCoreDistanceProfile(false);
    Assertions.assertArrayEquals(new double[] {Double.POSITIVE_INFINITY, 3, 2.5, 2, 3.5}, d);
    d = result.getCoreDistanceProfile(true);
    Assertions.assertArrayEquals(new double[] {distance, 3, 2.5, 2, 3.5}, d);
    // Original order
    d = result.getCoreDistance(false);
    Assertions.assertArrayEquals(new double[] {Double.POSITIVE_INFINITY, 3, 2, 2.5, 3.5}, d);
    d = result.getCoreDistance(true);
    Assertions.assertArrayEquals(new double[] {distance, 3, 2, 2.5, 3.5}, d);
    int[] o;
    o = result.getOrder();
    Assertions.assertArrayEquals(new int[] {1, 2, 4, 3, 5}, o);
    o = result.getPredecessor();
    Assertions.assertArrayEquals(new int[] {-1, 0, 3, 1, 2}, o);
  }

  @Test
  public void testOpticsResult() {
    final float[] x = {0, 1, 0, 1, 100, 10, 11, 10, 11, 12};
    final float[] y = {0, 0, 1, 1, 100, 10, 10, 11, 11, 12};
    final OpticsManager om = new OpticsManager(x, y, 0);
    final float distance = 2;
    final int minPoints = 3;
    final OpticsResult result = om.optics(distance, minPoints);
    Assertions.assertEquals(distance, result.getGeneratingDistance());
    Assertions.assertEquals(minPoints, result.getMinPoints());

    final int[] order = result.getOrder();
    final TIntHashSet set = new TIntHashSet(order);
    Assertions.assertEquals(x.length, set.size());
    Assertions.assertNotNull(result.getClusters(), "Optics should generated DBSCAN clustering");
    Assertions.assertEquals(1, result.getNumberOfLevels());
    Assertions.assertFalse(result.hasHulls());
    for (int i = 0; i < order.length; i++) {
      Assertions.assertTrue(set.remove(result.get(i).parent + 1));
    }

    // No hulls
    Assertions.assertNull(result.getHull(1));
    Assertions.assertNull(result.getBounds(1));

    // Compute with non-core points
    result.extractDbscanClustering(distance, false);
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
    result.extractDbscanClustering(distance, true);
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
    result.computeHulls(ConvexHull2d.newBuilder());
    Assertions.assertSame(h1, result.getHull(1));
    Assertions.assertSame(b1, result.getBounds(1));

    Assertions.assertNull(result.getHull(0));
    Assertions.assertNull(result.getBounds(0));
    Assertions.assertNull(result.getHull(10));
    Assertions.assertNull(result.getBounds(10));

    // Reset
    result.resetClusterIds();
    Assertions.assertEquals(0, result.getNumberOfLevels());
    Assertions.assertNull(result.getClusteringHierarchy());
    Assertions.assertNull(result.getHull(1));
    Assertions.assertNull(result.getBounds(1));
    result.computeHulls(ConvexHull2d.newBuilder());
    Assertions.assertFalse(result.hasHulls());
    Assertions.assertNull(result.getHull(1));
    Assertions.assertNull(result.getBounds(1));
  }

  @Test
  public void testScrambleClusters() {
    final float[] x = {0, 1, 0, 1, 100, 10, 11, 10, 11, 12};
    final float[] y = {0, 0, 1, 1, 100, 10, 10, 11, 11, 12};
    final OpticsManager om = new OpticsManager(x, y, new float[x.length], 0);
    final float distance = 2;
    final int minPoints = 3;
    final OpticsResult result = om.optics(distance, minPoints);

    // Compute with non-core points
    result.extractDbscanClustering(distance, false);
    final int[] expected = {1, 1, 1, 1, 0, 2, 2, 2, 2, 2};
    final int[] c = result.getClusters();
    Assertions.assertEquals(1.0, RandIndex.randIndex(expected, c));

    final UniformRandomProvider rng = RngUtils.create(263874682L);
    boolean changed = false;
    for (int i = 0; i < 5; i++) {
      result.scrambleClusters(rng);
      result.extractDbscanClustering(distance, false);
      final int[] c2 = result.getClusters();
      Assertions.assertEquals(1.0, RandIndex.randIndex(expected, c2));
      changed |= Arrays.equals(c, c2);
    }
    Assertions.assertTrue(changed);
  }

  @Test
  public void testScrambleClustersWithNoClusters() {
    final float[] x = {0, 1};
    final float[] y = {0, 0};
    final OpticsManager om = new OpticsManager(x, y, 0);
    final float distance = 2;
    final int minPoints = 3;
    final OpticsOrder[] opticsResults = new OpticsOrder[x.length];
    opticsResults[0] = new OpticsOrder(0, -1, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    opticsResults[1] = new OpticsOrder(1, -1, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    final OpticsResult result = new OpticsResult(om, minPoints, distance, opticsResults);

    // Compute with non-core points
    result.extractDbscanClustering(distance, false);
    final int[] expected = {0, 0};
    final int[] c = result.getClusters();
    Assertions.assertArrayEquals(expected, c);

    final UniformRandomProvider rng = null;
    result.scrambleClusters(rng);
    result.extractDbscanClustering(distance, false);
    final int[] c2 = result.getClusters();
    Assertions.assertArrayEquals(expected, c2);
  }

  @Test
  public void testComputeHulls3d() {
    final float[] x = {0, 1, 0, 1, 100, 10, 11, 10, 11, 12};
    final float[] y = {0, 0, 1, 1, 100, 10, 10, 11, 11, 12};
    final OpticsManager om2 = new OpticsManager(x, y, 0);
    final OpticsManager om3 = new OpticsManager(x, y, new float[x.length], 0);
    final float distance = 2;
    final int minPoints = 3;
    final OpticsResult result2 = om2.optics(distance, minPoints);
    final OpticsResult result3 = om3.optics(distance, minPoints);

    // Compute with non-core points
    result2.extractDbscanClustering(distance, false);
    result3.extractDbscanClustering(distance, false);
    Assertions.assertArrayEquals(result2.getClusters(), result3.getClusters());

    result2.computeHulls(ConvexHull2d.newBuilder());
    result3.computeHulls(ConvexHull2d.newBuilder());
    Assertions.assertArrayEquals(result2.getHull(1).getVertices(),
        result3.getHull(1).getVertices());
  }

  // TODO - Generate a hierarchical clustering with Optics Xi and test:
  // getNumberOfLevels (must be above 1 for a hierarchy to test other methods)
  // getClustersFromOrder
  // getParents
  // computeHulls
  //
  // Do not have to control the optics profile. Just extract it using getOrder and getClustering
  // check the methods return the correct data.
  //
  // Test the upper and lower limit by scanning the reachability profile for the range and clipping
  // it. Check the clustering results change.

  @Test
  public void testHierarchicalResults() {
  }

  private static void assertIdEquals(int[] expected, int[] actual) {
    Arrays.sort(actual);
    Assertions.assertArrayEquals(expected, actual);
  }
}
