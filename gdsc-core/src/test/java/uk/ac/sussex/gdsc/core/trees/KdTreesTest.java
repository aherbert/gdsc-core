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

package uk.ac.sussex.gdsc.core.trees;

import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.ac.sussex.gdsc.test.rng.RngFactory;

@SuppressWarnings({"javadoc"})
class KdTreesTest {
  @ParameterizedTest
  @ValueSource(ints = {0, -1, -42})
  void testInvalidDimensions(int dimensions) {
    Assertions.assertThrows(IllegalArgumentException.class, () -> KdTrees.builder(dimensions));
  }

  @Test
  void testInvalidDimensionWeights() {
    final KdTrees.Builder builder = KdTrees.builder(2);
    Assertions.assertThrows(NullPointerException.class, () -> builder.setDimensionWeight(null));
  }

  @Test
  void testInvalidSplitStrategy() {
    final KdTrees.Builder builder = KdTrees.builder(2);
    Assertions.assertThrows(NullPointerException.class, () -> builder.setSplitStratgey(null));
  }

  @ParameterizedTest
  @MethodSource
  @Disabled("Performance test code for the split strategy shows no significant changes")
  void testPerformance(int n, int m, int k, int[] nn, SplitStrategy splitStrategy) {
    // Create the data: (n, m, 2)

    // Creating random points does not provide data where the split strategy impacts
    // either construction time or NN search performance.
    final UniformRandomProvider rng = RngFactory.createWithFixedSeed();
    final double[][][] data = IntStream.range(0, n).mapToObj(i -> IntStream.range(0, m)
        // When points are random the MIDDLE method is fastest to construct and
        // the search speed is similar for all methods
         .mapToObj(j -> new double[] {rng.nextDouble(), rng.nextDouble()})

        // Use ints to have collisions in the x,y coords
        // For very high numbers of points the mean and median are faster than the middle.
        //.mapToObj(j -> new double[] {rng.nextInt(5), rng.nextInt(5)})

        .toArray(double[][]::new)).toArray(double[][][]::new);

    // Create the trees
    final DoubleKdTree[] trees = new DoubleKdTree[n];
    long timeCreate = System.nanoTime();
    for (int i = 0; i < n; i++) {
      final DoubleKdTree tree = KdTrees.builder(2).setSplitStratgey(splitStrategy).buildDouble();
      trees[i] = tree;
      final double[][] p = data[i];
      for (int j = 0; j < m; j++) {
        tree.add(p[j]);
      }
    }
    timeCreate = System.nanoTime() - timeCreate;

    System.out.printf("create    %4d  %4d  %4d  %-7s         %-25s%n", n, m, k, splitStrategy,
        timeCreate * 1e-9);

    // Search for k points in [0, m)
    final int[] points = rng.ints(k, 0, m).toArray();
    for (final int nnn : nn) {
      final double[] sum = {0};
      long timeSearch = System.nanoTime();
      for (int i = 0; i < n; i++) {
        final DoubleKdTree tree = trees[i];
        //final double[][] p = data[i];
        // Search for points from another tree
        final double[][] p = data[i == 0 ? n - 1 : i - 1];
        for (int j = 0; j < k; j++) {
          tree.nearestNeighbours(p[points[j]], nnn, false,
              DoubleDistanceFunctions.SQUARED_EUCLIDEAN_2D, d -> sum[0] += d);
        }
      }
      timeSearch = System.nanoTime() - timeSearch;
      System.out.printf("  search  %4d  %4d  %4d  %-7s  nn=%2d  %s%n", n, m, k, splitStrategy, nnn,
          timeSearch * 1e-9);
    }
  }

  static Stream<Arguments> testPerformance() {
    final int[] nn = {1, 1, 5, 5, 25, 25};
    return Stream.of(
        Arguments.of(1000, 100000, 1000, nn, SplitStrategy.MIDDLE),
        Arguments.of(1000, 100000, 1000, nn, SplitStrategy.MEAN),
        Arguments.of(1000, 100000, 1000, nn, SplitStrategy.MEDIAN));
  }
}
