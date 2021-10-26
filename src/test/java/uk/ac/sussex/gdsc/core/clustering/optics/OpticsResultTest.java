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

package uk.ac.sussex.gdsc.core.clustering.optics;

import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.shape.UnitBallSampler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.clustering.optics.OpticsResult.SteepDownArea;
import uk.ac.sussex.gdsc.core.clustering.optics.OpticsResult.SteepUpArea;
import uk.ac.sussex.gdsc.core.match.RandIndex;
import uk.ac.sussex.gdsc.core.math.hull.ConvexHull2d;
import uk.ac.sussex.gdsc.core.math.hull.Hull;
import uk.ac.sussex.gdsc.core.math.hull.Hull.Builder;
import uk.ac.sussex.gdsc.core.math.hull.Hull2d;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

@SuppressWarnings({"javadoc"})
class OpticsResultTest {
  /**
   * An interface for a fake Hull.
   */
  interface FakeHull extends Hull {
    boolean contains(double[] point);
  }

  /**
   * A special hull builder that will fail on the first hull. Used to test building a hull from the
   * points in a child cluster when the hull for the child cluster is null.
   */
  static class FailingHull2dBuilder implements Builder {
    final Hull.Builder hb = ConvexHull2d.newBuilder();
    int count;

    @Override
    public Hull.Builder clear() {
      hb.clear();
      return this;
    }

    @Override
    public Hull build() {
      return count++ == 0 ? null : hb.build();
    }

    @Override
    public Hull.Builder add(double... point) {
      hb.add(point);
      return this;
    }
  }

  /**
   * A special hull builder that will fail on the first hull. Used to test building a hull from the
   * points in a child cluster when the hull for the child cluster is null.
   */
  static class FailingHull3dBuilder extends FailingHull2dBuilder {
    @Override
    public FakeHull build() {
      if (count++ == 0) {
        return null;
      }
      final Hull2d hull = (Hull2d) hb.build();
      // Fake that this is a 3D hull. Works like a 2D hull with zero for the z dimension.
      return new FakeHull() {
        @Override
        public int dimensions() {
          return 3;
        }

        @Override
        public int getNumberOfVertices() {
          return hull.getNumberOfVertices();
        }

        @Override
        public double[][] getVertices() {
          final double[][] fake = new double[getNumberOfVertices()][];
          final double[][] real = hull.getVertices();
          for (int i = 0; i < real.length; i++) {
            fake[i] = Arrays.copyOf(real[i], 3);
          }
          return fake;
        }

        @Override
        public boolean contains(double[] point) {
          return hull.contains(point);
        }
      };
    }
  }

  @Test
  void testSteepDownArea() {
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
  void testSteepUpArea() {
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
  void testProfiles() {
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
  void testOpticsResult() {
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
    // Do not ignore request to build new hulls
    result.computeHulls(ConvexHull2d.newBuilder());
    Assertions.assertNotSame(h1, result.getHull(1));
    Assertions.assertNotSame(b1, result.getBounds(1));

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
  void testScrambleClusters() {
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
  void testScrambleClustersWithNoClusters() {
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
  void testComputeHulls3d() {
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

  // Test the upper and lower limit by scanning the reachability profile for the range and clipping
  // it. Check the clustering results change.

  @Test
  void testHierarchicalResults() {
    // This test depends on the output from the UnitDiskSampler and is not robust to
    // using any seed.
    final UniformRandomProvider rng = RngUtils.create(1234L);

    // Create blobs on an image using uniform random circles.
    // Put some circles inside others. This should trigger Optics Xi to create
    // hierarchical clusters.
    final TFloatArrayList x = new TFloatArrayList();
    final TFloatArrayList y = new TFloatArrayList();
    final UnitBallSampler s = UnitBallSampler.of(rng, 2);
    // Two circles, one inside the other
    addCircle(s, 10, 10, 10, 50, x, y);
    addCircle(s, 9, 9, 3, 50, x, y);
    // One isolated circle
    addCircle(s, 30, 30, 10, 50, x, y);
    final OpticsManager om = new OpticsManager(x.toArray(), y.toArray(), 0);
    final OpticsResult result = om.optics(3, 4);

    result.resetClusterIds();
    Assertions.assertArrayEquals(new int[0], result.getClustersFromOrder(10, 20, true));

    result.extractClusters(0.05);

    Assertions.assertArrayEquals(new int[0], result.getClustersFromOrder(1000, 2000, true));
    Assertions.assertArrayEquals(new int[0], result.getClustersFromOrder(-10, -2, true));

    // There should be a hierarchy of clusters to test descending the children
    Assertions.assertTrue(result.getNumberOfLevels() > 1);

    // Check getClustersFromOrder
    final List<OpticsCluster> allClusters = result.getAllClusters();
    assertGetClustersFromOrder(allClusters, result, 20, 30, true);
    assertGetClustersFromOrder(allClusters, result, 20, 30, false);
    assertGetClustersFromOrder(allClusters, result, 20, 20, false);

    // Check getParents
    assertGetParents(allClusters, result, new int[] {3});
    assertGetParents(allClusters, result, new int[] {3, 4, 5});
    assertGetParents(allClusters, result, new int[] {0, 3, 4, 5, 1000});
    // Require a top level cluster with children. Just do them all.
    final TIntArrayList ids = new TIntArrayList();
    final List<OpticsCluster> topLevel = result.getClusteringHierarchy();
    topLevel.stream().mapToInt(OpticsCluster::getClusterId).forEach(ids::add);
    // For the final cluster with children add some of its children.
    for (int i = topLevel.size(); i-- > 0;) {
      if (topLevel.get(i).children != null) {
        ids.add(topLevel.get(i).children.get(0).getClusterId());
        break;
      }
    }
    assertGetParents(allClusters, result, ids.toArray());

    // Check getTopLevelClusters
    // Assert that they are top level
    final int[] c1 = result.getTopLevelClusters();
    final TIntHashSet top = new TIntHashSet();
    topLevel.stream().mapToInt(OpticsCluster::getClusterId).forEach(top::add);
    for (int i = 0; i < c1.length; i++) {
      if (c1[i] > 0) {
        Assertions.assertTrue(top.contains(c1[i]));
      }
    }

    // Check computation of Hulls.
    // Make 1 computation fail to hit code coverage.
    result.computeHulls(new FailingHull2dBuilder());
    final int id = topLevel.get(0).getClusterId();
    final Hull2d h = (Hull2d) result.getHull(id);
    for (int i = 0; i < c1.length; i++) {
      final double[] point = new double[] {x.get(i), y.get(i)};
      // Because this is a convex hull some points not in the cluster
      // can still be inside the hull. So only test those in the cluster.
      if (c1[i] == id) {
        // Should be inside.
        boolean inside = h.contains(point);
        // May be on the boundary.
        if (!inside) {
          for (final double[] p : h.getVertices()) {
            if (p[0] == point[0] && p[1] == point[1]) {
              inside = true;
              break;
            }
          }
        }
        Assertions.assertTrue(inside);
      }
    }

    // Check it works for 3D to build a 2D hull.
    // Clustering will be different as the ordering of neighbour processing
    // is different for a 2D grid or 3D-tree.
    final OpticsManager om2 =
        new OpticsManager(x.toArray(), y.toArray(), new float[x.size()], om.area);
    final OpticsResult result2 = om2.optics(3, 4);
    result2.extractClusters(0.05);
    result2.computeHulls(new FailingHull3dBuilder());
    final int id2 = result2.getClusteringHierarchy().get(0).getClusterId();
    final int[] c2 = result.getTopLevelClusters();
    final FakeHull h2 = (FakeHull) result2.getHull(id2);
    for (int i = 0; i < c2.length; i++) {
      final double[] point = new double[] {x.get(i), y.get(i)};
      // Because this is a convex hull some points not in the cluster
      // can still be inside the hull. So only test those in the cluster.
      if (c2[i] == id) {
        // Should be inside.
        boolean inside = h2.contains(point);
        // May be on the boundary.
        if (!inside) {
          for (final double[] p : h2.getVertices()) {
            if (p[0] == point[0] && p[1] == point[1]) {
              inside = true;
              break;
            }
          }
          Assertions.assertTrue(inside);
        }
      }
    }
  }

  @Test
  void testReachabilityLimits() {
    final UniformRandomProvider rng = RngUtils.create(123L);
    // Create blobs on an image using uniform random circles.
    // Put some circles inside others. This should trigger Optics Xi to create
    // hierarchical clusters.
    final TFloatArrayList x = new TFloatArrayList();
    final TFloatArrayList y = new TFloatArrayList();
    final UnitBallSampler s = UnitBallSampler.of(rng, 2);
    // Two circles, one inside the other
    addCircle(s, 10, 10, 10, 50, x, y);
    addCircle(s, 9, 9, 3, 50, x, y);
    // One isolated circle
    addCircle(s, 30, 30, 10, 50, x, y);
    final OpticsManager om = new OpticsManager(x.toArray(), y.toArray(), 0);
    final OpticsResult result = om.optics(3, 4);

    result.resetClusterIds();
    final double xi = 0.05;
    result.extractClusters(xi);

    final double[] reachability = result.getReachabilityDistanceProfile(false);
    final int[] clusters = result.getClusters();

    result.setUpperLimit(10);
    Assertions.assertEquals(10, result.getUpperLimit());
    result.setUpperLimit(-10);
    Assertions.assertEquals(Double.POSITIVE_INFINITY, result.getUpperLimit());
    result.setUpperLimit(Double.NaN);
    Assertions.assertEquals(Double.POSITIVE_INFINITY, result.getUpperLimit());
    result.setLowerLimit(10);
    Assertions.assertEquals(10, result.getLowerLimit());
    result.setLowerLimit(-10);
    Assertions.assertEquals(-10, result.getLowerLimit());
    result.setLowerLimit(Double.NaN);
    Assertions.assertEquals(0, result.getLowerLimit());

    result.extractClusters(xi,
        OpticsResult.XI_OPTION_LOWER_LIMIT | OpticsResult.XI_OPTION_UPPER_LIMIT);
    Assertions.assertArrayEquals(clusters, result.getClusters());

    // Find some limits that will effect clustering
    final Percentile p = new Percentile();
    p.setData(reachability);
    final double lower = p.evaluate(35);
    final double upper = p.evaluate(65);
    result.setLowerLimit(lower);
    result.setUpperLimit(upper);

    // Re-run without options -> no change
    // Re-run with options and the clustering should change
    result.extractClusters(xi);
    Assertions.assertArrayEquals(clusters, result.getClusters());
    result.extractClusters(xi, OpticsResult.XI_OPTION_LOWER_LIMIT);
    Assertions.assertFalse(Arrays.equals(clusters, result.getClusters()));
    result.extractClusters(xi, OpticsResult.XI_OPTION_UPPER_LIMIT);
    Assertions.assertFalse(Arrays.equals(clusters, result.getClusters()));
    result.extractClusters(xi,
        OpticsResult.XI_OPTION_LOWER_LIMIT | OpticsResult.XI_OPTION_UPPER_LIMIT);
    Assertions.assertFalse(Arrays.equals(clusters, result.getClusters()));
  }

  @Test
  void testOpticsXiOptions() {
    final UniformRandomProvider rng = RngUtils.create(123L);
    // Create blobs on an image using uniform random circles.
    // Put some circles inside others. This should trigger Optics Xi to create
    // hierarchical clusters.
    final TFloatArrayList x = new TFloatArrayList();
    final TFloatArrayList y = new TFloatArrayList();
    final UnitBallSampler s = UnitBallSampler.of(rng, 2);
    // Two circles, one inside the other
    addCircle(s, 10, 10, 10, 50, x, y);
    addCircle(s, 9, 9, 3, 50, x, y);
    // One isolated circle
    addCircle(s, 30, 30, 10, 50, x, y);
    final OpticsManager om = new OpticsManager(x.toArray(), y.toArray(), 0);
    final OpticsResult result = om.optics(3, 4);

    final double xi = 0.05;
    result.extractClusters(xi);

    final int[] clusters = result.getClusters();

    // This just tests the results are different, not the actual change in the clusters.

    result.extractClusters(xi, OpticsResult.XI_OPTION_NO_CORRECT);
    Assertions.assertFalse(Arrays.equals(clusters, result.getClusters()));

    result.extractClusters(xi, OpticsResult.XI_OPTION_EXCLUDE_LAST_STEEP_UP_IF_SIGNIFICANT);
    Assertions.assertFalse(Arrays.equals(clusters, result.getClusters()));
  }

  private static void assertIdEquals(int[] expected, int[] actual) {
    Arrays.sort(actual);
    Assertions.assertArrayEquals(expected, actual);
  }

  private static void addCircle(UnitBallSampler s, float cx, float cy, float r, int n,
      TFloatArrayList x, TFloatArrayList y) {
    for (int i = 0; i < n; i++) {
      final double[] p = s.sample();
      x.add((float) (cx + p[0] * r));
      y.add((float) (cy + p[1] * r));
    }
  }

  private static void assertGetClustersFromOrder(List<OpticsCluster> allClusters,
      OpticsResult result, int start, int end, boolean includeChildren) {
    final int[] co = result.getClustersFromOrder(start, end, includeChildren);
    final TIntHashSet set = new TIntHashSet();
    final Predicate<OpticsCluster> p = includeChildren ? c -> true : c -> c.getLevel() == 0;
    allClusters.stream().filter(p)
        .filter(c -> (c.start < start) ? c.end >= start - 1 : c.start < end)
        .mapToInt(OpticsCluster::getClusterId).forEach(set::add);
    final int[] exp = set.toArray();
    Arrays.sort(exp);
    assertIdEquals(exp, co);
  }

  private static void assertGetParents(List<OpticsCluster> allClusters, OpticsResult result,
      int[] ids) {
    final int[] co = result.getParents(ids);
    final TIntHashSet set = new TIntHashSet();
    allClusters.stream().filter(c -> ArrayUtils.indexOf(ids, c.getClusterId()) >= 0).forEach(c -> {
      for (int i = c.start; i <= c.end; i++) {
        set.add(result.get(i).parent);
      }
    });
    final int[] exp = set.toArray();
    Arrays.sort(exp);
    assertIdEquals(exp, co);
  }
}
