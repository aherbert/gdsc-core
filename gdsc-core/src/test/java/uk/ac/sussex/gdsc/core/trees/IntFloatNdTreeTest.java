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

package uk.ac.sussex.gdsc.core.trees;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import java.util.Arrays;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.LocalList;
import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.core.utils.PartialSort;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;
import uk.ac.sussex.gdsc.test.utils.functions.IndexSupplier;

@SuppressWarnings({"javadoc"})
class IntFloatNdTreeTest {
  private static final int BUCKET_SIZE = 24;

  @Test
  void testEmptyTree() {
    final IntFloatKdTree tree = KdTrees.newIntFloatKdTree(2);
    Assertions.assertEquals(2, tree.dimensions());
    Assertions.assertEquals(0, tree.size());
    final IntArrayList items = new IntArrayList();
    final DoubleArrayList distances = new DoubleArrayList();
    final FloatDistanceFunction distanceFunction = FloatDistanceFunctions.SQUARED_EUCLIDEAN_2D;
    final double[] point = new double[2];

    final double min = tree.nearestNeighbour(point, distanceFunction, (t, dist) -> {
      items.add(t);
      distances.add(dist);
    });
    Assertions.assertEquals(0, min);
    Assertions.assertTrue(items.isEmpty());
    Assertions.assertTrue(distances.isEmpty());

    boolean result = tree.nearestNeighbours(point, 1, true, distanceFunction, (t, dist) -> {
      items.add(t);
      distances.add(dist);
    });
    Assertions.assertFalse(result);
    Assertions.assertTrue(items.isEmpty());
    Assertions.assertTrue(distances.isEmpty());

    result = tree.nearestNeighbours(point, 1, true, distanceFunction, t -> true, (t, dist) -> {
      items.add(t);
      distances.add(dist);
    });
    Assertions.assertFalse(result);
    Assertions.assertTrue(items.isEmpty());
    Assertions.assertTrue(distances.isEmpty());

    result = tree.findNeighbours(point, 10.0, distanceFunction, (t, dist) -> {
      items.add(t);
      distances.add(dist);
    });
    Assertions.assertFalse(result);
    Assertions.assertTrue(items.isEmpty());
    Assertions.assertTrue(distances.isEmpty());
  }

  @Test
  void testComputeKnnWithSmallTree() {
    final float[][] data = new float[][] {{0, 0}, {0, 1}, {1, 0}, {1, 1}};
    assertComputeKnn(data);
  }

  @Test
  void testComputeKnnWithSingleTree() {
    final float[][] data = new float[][] {{0, 0}};
    assertComputeKnn(data);
  }

  @Test
  void testComputeKnnWithSingularity() {
    final LocalList<float[]> list = new LocalList<>(BUCKET_SIZE * 3);
    final float[] point1 = new float[2];
    // Create some data.
    // Make the first split on a singularity.
    IntStream.range(0, BUCKET_SIZE).forEach(i -> list.add(point1));
    IntStream.range(0, BUCKET_SIZE + 2).mapToObj(i -> new float[] {0, i}).forEach(list::add);
    final float[][] data = list.toArray(new float[0][]);
    assertComputeKnn(data);
  }

  @SeededTest
  void testComputeKnn(RandomSeed seed) {
    assertComputeKnn(seed, false);
  }

  @SeededTest
  void testComputeKnnWithDuplicates(RandomSeed seed) {
    assertComputeKnn(seed, true);
  }

  private static void assertComputeKnn(RandomSeed seed, boolean allowDuplicates) {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    for (final int n : KdTreeTestUtils.ns) {
      final float[][] data =
          KdTreeTestUtils.createFloatData(r, KdTreeTestUtils.size, n, allowDuplicates);
      assertComputeKnn(data);
    }
  }

  private static void assertComputeKnn(float[][] data) {
    final IntArrayList items = new IntArrayList();
    final DoubleArrayList distances = new DoubleArrayList();
    final FloatDistanceFunction distanceFunction = FloatDistanceFunctions.SQUARED_EUCLIDEAN_2D;
    final int n = data.length;

    // Create the KDtree
    final IntFloatKdTree tree = KdTrees.newIntFloatKdTree(2);
    Assertions.assertEquals(2, tree.dimensions());
    int item = 0;
    for (final float[] location : data) {
      tree.add(location, Integer.valueOf(item++));
    }
    Assertions.assertEquals(n, tree.size());

    // Compute all-vs-all distances
    final double[][] d = new double[n][n];
    for (int i = 0; i < n; i++) {
      for (int j = i + 1; j < n; j++) {
        d[i][j] =
            d[j][i] = MathUtils.distance2((double) data[i][0], data[i][1], data[j][0], data[j][1]);
      }
    }

    // For each point
    final double[] searchPoint = new double[2];
    for (int i = 0; i < n; i++) {
      searchPoint[0] = data[i][0];
      searchPoint[1] = data[i][1];
      // Get the sorted distances to neighbours. Include extra for the filter test.
      final double[] d2 =
          PartialSort.bottom(d[i], KdTreeTestUtils.ks[KdTreeTestUtils.ks.length - 1] + 2);

      // Check the closest
      items.clear();
      distances.clear();
      final double min = tree.nearestNeighbour(searchPoint, distanceFunction, (t, dist) -> {
        items.add(t);
        distances.add(dist);
      });
      Assertions.assertEquals(min, d2[0]);
      Assertions.assertEquals(min, distances.getDouble(0));
      int ii = items.getInt(0);
      double e = d[i][ii];
      Assertions.assertEquals(e, min);

      // Nearest neighbour handles a null consumer
      final double min2 = tree.nearestNeighbour(searchPoint, distanceFunction, null);
      Assertions.assertEquals(min, min2);

      // Nearest neighbours ignores a bad count
      items.clear();
      distances.clear();
      boolean result =
          tree.nearestNeighbours(searchPoint, -1, true, distanceFunction, (t, dist) -> {
            items.add(t);
            distances.add(dist);
          });
      Assertions.assertFalse(result);
      Assertions.assertTrue(items.isEmpty());
      Assertions.assertTrue(distances.isEmpty());

      result =
          tree.nearestNeighbours(searchPoint, -1, true, distanceFunction, t -> true, (t, dist) -> {
            items.add(t);
            distances.add(dist);
          });
      Assertions.assertFalse(result);
      Assertions.assertTrue(items.isEmpty());
      Assertions.assertTrue(distances.isEmpty());

      // Nearest neighbour ignores a failing predicate
      final double distance =
          tree.nearestNeighbour(searchPoint, distanceFunction, t -> false, (t, dist) -> {
            items.add(t);
            distances.add(dist);
          });
      Assertions.assertTrue(Double.isNaN(distance));
      Assertions.assertTrue(items.isEmpty());
      Assertions.assertTrue(distances.isEmpty());

      // Get the knn
      for (final int k : KdTreeTestUtils.ks) {
        items.clear();
        distances.clear();
        result = tree.nearestNeighbours(searchPoint, k, true, distanceFunction, (t, dist) -> {
          items.add(t);
          distances.add(dist);
        });
        Assertions.assertTrue(result);
        // Neighbours will be in reverse order
        DoubleArrays.reverse(distances.elements(), 0, distances.size());
        double[] observed = distances.toDoubleArray();
        double[] expected = Arrays.copyOf(d2, Math.min(n, k));

        Assertions.assertArrayEquals(expected, observed);

        // Get the items
        IntArrays.reverse(items.elements(), 0, items.size());
        // Note: in the event of a distance tie the item returned will be random
        // Just check the item is has the correct distance
        for (int j = 0; j < observed.length; j++) {
          ii = items.getInt(j);
          e = d[i][ii];
          Assertions.assertEquals(e, observed[j]);
        }

        // Same results can be obtained unsorted
        result = tree.nearestNeighbours(searchPoint, k, false, distanceFunction, (t, dist) -> {
          final int index = items.indexOf(t);
          Assertions.assertTrue(index >= 0);
          Assertions.assertEquals(dist, distances.getDouble(index));
        });
        Assertions.assertTrue(result);

        // Filter some of the close neighbours
        if (Math.min(n - 2, k) > 5) {
          final int p1 = items.getInt(3);
          final int p2 = items.getInt(5);
          final IntPredicate filter = t -> {
            return t != p1 && t != p2;
          };
          items.clear();
          distances.clear();
          result =
              tree.nearestNeighbours(searchPoint, k, true, distanceFunction, filter, (t, dist) -> {
                items.add(t);
                distances.add(dist);
              });
          Assertions.assertTrue(result);
          // Neighbours will be in reverse order
          DoubleArrays.reverse(distances.elements(), 0, distances.size());
          observed = distances.toDoubleArray();
          final DoubleArrayList tmp = new DoubleArrayList(Arrays.copyOf(d2, k + 2));
          tmp.removeDouble(5);
          tmp.removeDouble(3);
          expected = tmp.toDoubleArray();

          Assertions.assertArrayEquals(expected, observed);

          // Get the items
          IntArrays.reverse(items.elements(), 0, items.size());
          // Note: in the event of a distance tie the item returned will be random
          // Just check the item has the correct distance
          for (int j = 0; j < observed.length; j++) {
            ii = items.getInt(j);
            e = d[i][ii];
            Assertions.assertEquals(e, observed[j]);
          }
        }
      }

      // Get the neighbours within a range
      for (final double range : KdTreeTestUtils.ranges) {
        items.clear();
        distances.clear();
        result = tree.findNeighbours(searchPoint, range, distanceFunction, (t, dist) -> {
          items.add(t);
          distances.add(dist);
        });
        // Check
        int count = 0;
        for (int j = 0; j < n; j++) {
          if (d[i][j] <= range) {
            count++;
          }
        }
        Assertions.assertEquals(count, distances.size());
        Assertions.assertEquals(result, !distances.isEmpty());
        for (int j = 0; j < distances.size(); j++) {
          ii = items.getInt(j);
          e = d[i][ii];
          Assertions.assertEquals(e, distances.getDouble(j));
          Assertions.assertTrue(e <= range);
        }
      }

      // Second closest
      if (tree.size() > 1) {
        items.clear();
        final double dA = tree.nearestNeighbour(searchPoint, distanceFunction, (t, dist) -> {
          items.add(t);
        });
        final int ignore = items.getInt(0);
        final double dB =
            tree.nearestNeighbour(searchPoint, distanceFunction, t -> t != ignore, (t, dist) -> {
              items.add(t);
            });
        Assertions.assertNotEquals(ignore, items.getInt(1));
        Assertions.assertTrue(dB >= dA);
      }
    }
  }

  /**
   * Test that the distance function can be used with a singularity to exclude points.
   */
  @Test
  void testSingularityWithDistanceAboveRange() {
    final LocalList<float[]> list = new LocalList<>(BUCKET_SIZE * 3);
    final float[] point1 = {0, 0};
    final float[] point2 = {5, 4};
    // Create some data.
    list.add(point1);
    list.add(point2);
    // This should create two singularities with a split value roughly halfway between
    // 5 and 0 (i.e. 2.5)
    IntStream.range(1, BUCKET_SIZE).forEach(i -> {
      list.add(point1);
      list.add(point2);
    });
    final float[][] data = list.toArray(new float[0][]);

    final IntArrayList items = new IntArrayList();
    final DoubleArrayList distances = new DoubleArrayList();
    final FloatDistanceFunction distanceFunction = FloatDistanceFunctions.SQUARED_EUCLIDEAN_2D;

    // Create the KDtree
    final IntFloatKdTree tree = KdTrees.newIntFloatKdTree(2);
    Assertions.assertEquals(2, tree.dimensions());
    int item = 0;
    for (final float[] location : data) {
      tree.add(location, Integer.valueOf(item++));
    }

    // Search with a point that is on the left side of the split value but actually closer to the
    // other point data.
    final double[] p1 = {2, 4};
    items.clear();
    distances.clear();
    boolean result = tree.findNeighbours(p1, 9, distanceFunction, (t, dist) -> {
      items.add(t);
      distances.add(dist);
    });
    Assertions.assertTrue(result);
    Assertions.assertEquals(BUCKET_SIZE, items.size());
    items.forEach(i -> Assertions.assertEquals(point2, data[i]));
    distances.forEach(d -> Assertions.assertEquals(9, d));

    // For nearest neighbours we use a NaN point so the distance is invalid
    final double[] p2 = {2, Double.NaN};
    items.clear();
    distances.clear();
    result = tree.nearestNeighbours(p2, BUCKET_SIZE, false, distanceFunction, (t, dist) -> {
      items.add(t);
      distances.add(dist);
    });
    Assertions.assertFalse(result);
    Assertions.assertTrue(items.isEmpty());
    Assertions.assertTrue(distances.isEmpty());

    final double d = tree.nearestNeighbour(p2, distanceFunction, (t, dist) -> {
      items.add(t);
      distances.add(dist);
    });
    Assertions.assertEquals(Double.NaN, d);
    Assertions.assertTrue(items.isEmpty());
    Assertions.assertTrue(distances.isEmpty());
  }

  @Test
  void testComputeKnnWithNaN() {
    final LocalList<float[]> list = new LocalList<>(BUCKET_SIZE * 2);
    final float[] point1 = {0, 0};
    final float[] point2 = {Float.NaN, Float.NaN};
    // Create some data.
    list.add(point1);
    list.add(point2);
    // This should split once and have to handle the NaN data
    IntStream.range(1, BUCKET_SIZE).mapToObj(i -> new float[] {0, i}).forEach(list::add);

    final float[][] data = list.toArray(new float[0][]);

    final IntArrayList items = new IntArrayList();
    final DoubleArrayList distances = new DoubleArrayList();
    final FloatDistanceFunction distanceFunction = FloatDistanceFunctions.SQUARED_EUCLIDEAN_2D;

    // Create the KDtree
    final IntFloatKdTree tree = KdTrees.newIntFloatKdTree(2);
    Assertions.assertEquals(2, tree.dimensions());
    int item = 0;
    for (final float[] location : data) {
      tree.add(location, Integer.valueOf(item++));
    }

    // Search with a point in the tree.
    final double[] p1 = {0, 4};
    items.clear();
    distances.clear();
    boolean result = tree.findNeighbours(p1, 1, distanceFunction, (t, dist) -> {
      items.add(t);
      distances.add(dist);
    });
    Assertions.assertTrue(result);
    Assertions.assertEquals(3, items.size());

    // For nearest neighbours we use a NaN point so the distance is invalid
    items.clear();
    distances.clear();
    result = tree.nearestNeighbours(p1, 3, false, distanceFunction, (t, dist) -> {
      items.add(t);
      distances.add(dist);
    });
    Assertions.assertTrue(result);
    Assertions.assertEquals(3, items.size());
    Assertions.assertEquals(3, distances.size());

    items.clear();
    distances.clear();
    final double d = tree.nearestNeighbour(p1, distanceFunction, (t, dist) -> {
      items.add(t);
      distances.add(dist);
    });
    Assertions.assertEquals(0.0, d);
    Assertions.assertEquals(1, items.size());
    Assertions.assertEquals(1, distances.size());
    Assertions.assertEquals(5, items.getInt(0));
    Assertions.assertEquals(0, distances.getDouble(0));
  }

  @Test
  void testAddIfAbsent() {
    assertAddIfAbsent(2);
    assertAddIfAbsent(3);
    assertAddIfAbsent(4);
  }

  private static void assertAddIfAbsent(int dim) {
    final IntFloatKdTree tree = KdTrees.newIntFloatKdTree(dim);
    final int n = dim - 1;
    for (int i = 0; i < 30; i++) {
      final float[] point = new float[dim];
      point[n] = i;
      Assertions.assertTrue(tree.addIfAbsent(point, 0));
      Assertions.assertEquals(i + 1, tree.size(), () -> "Incorrect size. dim=" + dim);
      Assertions.assertFalse(tree.addIfAbsent(point.clone(), 0), () -> "Point added. dim=" + dim);
      Assertions.assertEquals(i + 1, tree.size(), () -> "Incorrect size. dim=" + dim);
    }
    // -0.0 and 0.0 are equal
    final float[] point = new float[dim];
    point[n] = -0f;
    Assertions.assertFalse(tree.addIfAbsent(point, 0));
  }

  @Test
  void testForEach() {
    final IntFloatKdTree tree = KdTrees.newIntFloatKdTree(2);
    final IndexSupplier msg = new IndexSupplier(1, "item ", "");
    for (final int size : new int[] {0, 1, 4, 16, 64, 256}) {
      for (int i = tree.size(); i < size; i++) {
        tree.add(new float[] {i, 0}, i);
      }
      final int[] count = new int[size];
      tree.forEach((p, t) -> {
        final int item = (int) p[0];
        Assertions.assertEquals(item, t);
        count[item]++;
      });
      for (int i = 0; i < size; i++) {
        Assertions.assertEquals(1, count[i], msg.set(0, i));
      }
    }
  }
}
