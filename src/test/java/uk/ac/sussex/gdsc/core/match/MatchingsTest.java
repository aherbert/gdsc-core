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

package uk.ac.sussex.gdsc.core.match;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.match.Matchings.CompositeMatchConsumer;
import uk.ac.sussex.gdsc.core.match.Matchings.IntersectionMatchConsumer;
import uk.ac.sussex.gdsc.core.match.Matchings.MatchConsumer;
import uk.ac.sussex.gdsc.core.match.Matchings.UnmatchedMatchConsumer;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;

/**
 * Test for {@link Matchings}.
 */
@SuppressWarnings({"javadoc"})
class MatchingsTest {
  interface MatchingFunction<T, U> {
    int compute(List<T> verticesA, List<U> verticesB, ToDoubleBiFunction<T, U> edges,
        double threshold, BiConsumer<T, U> matched, Consumer<T> unmatchedA, Consumer<U> unmatchedB);

    int compute(List<T> verticesA, List<U> verticesB, ToDoubleBiFunction<T, U> edges,
        double threshold);
  }

  static class NearestNeighbourMatchingFunction<T, U> implements MatchingFunction<T, U> {
    @SuppressWarnings("rawtypes")
    static final NearestNeighbourMatchingFunction INSTANCE = new NearestNeighbourMatchingFunction();

    static <T, U> MatchingFunction<T, U> instance() {
      return INSTANCE;
    }

    @Override
    public int compute(List<T> verticesA, List<U> verticesB, ToDoubleBiFunction<T, U> edges,
        double threshold, BiConsumer<T, U> matched, Consumer<T> unmatchedA,
        Consumer<U> unmatchedB) {
      return Matchings.nearestNeighbour(verticesA, verticesB, edges, threshold, matched, unmatchedA,
          unmatchedB);
    }

    @Override
    public int compute(List<T> verticesA, List<U> verticesB, ToDoubleBiFunction<T, U> edges,
        double threshold) {
      return Matchings.nearestNeighbour(verticesA, verticesB, edges, threshold, null, null, null);
    }
  }

  static class MinimumDistanceMatchingFunction<T, U> implements MatchingFunction<T, U> {
    @SuppressWarnings("rawtypes")
    static final MinimumDistanceMatchingFunction INSTANCE = new MinimumDistanceMatchingFunction();

    static <T, U> MatchingFunction<T, U> instance() {
      return INSTANCE;
    }

    @Override
    public int compute(List<T> verticesA, List<U> verticesB, ToDoubleBiFunction<T, U> edges,
        double threshold, BiConsumer<T, U> matched, Consumer<T> unmatchedA,
        Consumer<U> unmatchedB) {
      return Matchings.minimumDistance(verticesA, verticesB, edges, threshold, matched, unmatchedA,
          unmatchedB);
    }

    @Override
    public int compute(List<T> verticesA, List<U> verticesB, ToDoubleBiFunction<T, U> edges,
        double threshold) {
      return Matchings.minimumDistance(verticesA, verticesB, edges, threshold, null, null, null);
    }
  }

  @Test
  void testIntersectionMatchConsumer() {
    final int offsetA = 1;
    final int offsetB = 16;
    final int size = 5;
    final List<Integer> verticesA =
        IntStream.range(offsetA, offsetA + size).boxed().collect(Collectors.toList());
    final List<Integer> verticesB =
        IntStream.range(offsetB, offsetB + size).boxed().collect(Collectors.toList());

    Assertions.assertNull(IntersectionMatchConsumer.create(verticesA, verticesB, null));

    final List<Pair<Integer, Integer>> pairs = new ArrayList<>();
    final BiConsumer<Integer, Integer> matched = (u, v) -> {
      pairs.add(Pair.of(u, v));
    };
    final MatchConsumer consumer = IntersectionMatchConsumer.create(verticesA, verticesB, matched);
    // Indexes are 1-based
    consumer.accept(1, 2);
    consumer.finalise();
    Assertions.assertEquals(1, pairs.size());
    Assertions.assertEquals(offsetA, pairs.get(0).getLeft().intValue(), "Incorrect left");
    Assertions.assertEquals(offsetB + 1, pairs.get(0).getRight().intValue(), "Incorrect right");
  }

  @Test
  void testUnmatchedMatchConsumer() {
    final int offset = 1;
    final int size = 5;
    final List<Integer> vertices =
        IntStream.range(offset, offset + size).boxed().collect(Collectors.toList());

    Assertions.assertNull(UnmatchedMatchConsumer.create(vertices, true, null));

    final List<Integer> unmatched = new ArrayList<>();

    MatchConsumer consumer = UnmatchedMatchConsumer.create(vertices, true, unmatched::add);
    // Indexes are 1-based
    consumer.accept(1, 2);
    consumer.finalise();

    // Test the first side of the accept
    Assertions.assertEquals(size - 1, unmatched.size(), "Should be missing 1 entry");
    // Missing first index
    Assertions.assertFalse(unmatched.contains(vertices.get(0)), "Should be missing [0]");
    for (int i = 1; i < vertices.size(); i++) {
      Assertions.assertTrue(unmatched.contains(vertices.get(i)), "Should contain the rest");
    }

    // Test the second side of the accept
    unmatched.clear();
    consumer = UnmatchedMatchConsumer.create(vertices, false, unmatched::add);
    // Indexes are 1-based
    consumer.accept(1, 2);
    consumer.finalise();
    Assertions.assertEquals(size - 1, unmatched.size(), "Should be missing 1 entry");
    // Missing first index
    Assertions.assertFalse(unmatched.contains(vertices.get(1)), "Should be missing [1]");
    for (int i = 0; i < vertices.size(); i++) {
      if (i != 1) {
        Assertions.assertTrue(unmatched.contains(vertices.get(i)), "Should contain the rest");
      }
    }
  }

  @Test
  void testCompositeMatchConsumer() {
    final int offsetA = 1;
    final int offsetB = 16;
    final int size = 5;
    final List<Integer> verticesA =
        IntStream.range(offsetA, offsetA + size).boxed().collect(Collectors.toList());
    final List<Integer> verticesB =
        IntStream.range(offsetB, offsetB + size).boxed().collect(Collectors.toList());

    final List<Integer> unmatchedA = new ArrayList<>();
    final List<Integer> unmatchedB = new ArrayList<>();

    final MatchConsumer consumerA = UnmatchedMatchConsumer.create(verticesA, true, unmatchedA::add);
    final MatchConsumer consumerB =
        UnmatchedMatchConsumer.create(verticesB, false, unmatchedB::add);

    Assertions.assertNull(CompositeMatchConsumer.create(null, null));
    Assertions.assertSame(consumerA, CompositeMatchConsumer.create(consumerA, null), "Not A");
    Assertions.assertSame(consumerB, CompositeMatchConsumer.create(null, consumerB), "Not B");

    final MatchConsumer consumer = CompositeMatchConsumer.create(consumerA, consumerB);
    // Indexes are 1-based
    consumer.accept(1, 2);
    consumer.finalise();

    // Test the first side of the accept
    Assertions.assertEquals(size - 1, unmatchedA.size(), "Should be missing 1 entry");
    // Missing first index
    Assertions.assertFalse(unmatchedA.contains(verticesA.get(0)), "Should be missing [0]");
    for (int i = 1; i < verticesA.size(); i++) {
      Assertions.assertTrue(unmatchedA.contains(verticesA.get(i)), "Should contain the rest of A");
    }

    // Test the second side of the accept
    Assertions.assertEquals(size - 1, unmatchedB.size(), "Should be missing 1 entry");
    // Missing first index
    Assertions.assertFalse(unmatchedB.contains(verticesB.get(1)), "Should be missing [1]");
    for (int i = 0; i < verticesB.size(); i++) {
      if (i != 1) {
        Assertions.assertTrue(unmatchedB.contains(verticesB.get(i)),
            "Should contain the rest of B");
      }
    }
  }

  @Test
  void testMaximumCardinality() {
    final List<Integer> verticesA = Arrays.asList(1, 2, 3, 4, 5);
    final List<Integer> verticesB = Arrays.asList(1, 2, 3, 4, 5);
    // Data from Wikipedia:
    // https://en.wikipedia.org/wiki/Hopcroft%E2%80%93Karp_algorithm#/media/File:HopcroftKarpExample.png
    final boolean[][] connections = new boolean[6][6];
    connections[1][1] = true;
    connections[1][2] = true;
    connections[2][1] = true;
    connections[2][5] = true;
    connections[3][3] = true;
    connections[3][4] = true;
    connections[4][1] = true;
    connections[4][5] = true;
    connections[5][2] = true;
    connections[5][4] = true;
    final BiPredicate<Integer, Integer> edges = (u, v) -> {
      return connections[u][v];
    };
    int max = Matchings.maximumCardinality(verticesA, verticesB, edges, null, null, null);
    Assertions.assertEquals(5, max, "Cardinality with no consumers");

    final List<Pair<Integer, Integer>> pairs = new ArrayList<>();
    final BiConsumer<Integer, Integer> matched = (u, v) -> {
      pairs.add(Pair.of(u, v));
    };

    max = Matchings.maximumCardinality(verticesA, verticesB, edges, matched, null, null);
    Assertions.assertEquals(5, max, "Cardinality with consumers");
    Assertions.assertEquals(5, pairs.size(), "Size of matched");

    final int[][] expected = new int[][] {{1, 2}, {2, 5}, {3, 3}, {4, 1}, {5, 4}};
    for (final Pair<Integer, Integer> pair : pairs) {
      boolean found = false;
      for (final int[] edge : expected) {
        if (pair.getLeft() == edge[0] && pair.getRight() == edge[1]) {
          found = true;
          break;
        }
      }
      Assertions.assertTrue(found, "Missing an expected edge");
    }
  }

  @Test
  void testNearestNeighbourWithNoVertices() {
    assertMatchingFunctionWithNoVertices(NearestNeighbourMatchingFunction.instance());
  }

  @Test
  void testNearestNeighbourWithNoEdges() {
    assertMatchingFunctionWithNoEdges(NearestNeighbourMatchingFunction.instance());
  }

  @Test
  void testNearestNeighbourWithMaxCardinality() {
    assertMatchingFunctionWithMaxCardinality(NearestNeighbourMatchingFunction.instance());
  }

  @Test
  void testNearestNeighbour() {
    final double[][] connections = new double[5][];
    for (int i = 0; i < connections.length; i++) {
      connections[i] = SimpleArrayUtils.newDoubleArray(4, Double.MAX_VALUE);
    }
    connections[1][1] = 0;
    connections[1][2] = 0.25;
    connections[4][1] = 0.33;
    connections[4][2] = 0.75;
    connections[4][3] = 1;

    // This is greedy so the matches are: 0 + 0.75
    final int[][] expected = new int[][] {{1, 1}, {4, 2}};
    assertMatchingFunction(NearestNeighbourMatchingFunction.instance(), connections, 1, expected);
  }

  @Test
  void testMinimumDistanceWithNoVertices() {
    assertMatchingFunctionWithNoVertices(MinimumDistanceMatchingFunction.instance());
  }

  @Test
  void testMinimumDistanceWithNoEdges() {
    assertMatchingFunctionWithNoEdges(MinimumDistanceMatchingFunction.instance());
  }

  @Test
  void testMinimumDistanceWithMaxCardinality() {
    assertMatchingFunctionWithMaxCardinality(MinimumDistanceMatchingFunction.instance());
  }

  @Test
  void testMinimumDistance() {
    final double[][] connections = new double[5][];
    for (int i = 0; i < connections.length; i++) {
      connections[i] = SimpleArrayUtils.newDoubleArray(4, Double.MAX_VALUE);
    }
    connections[1][1] = 0;
    connections[1][2] = 0.25;
    connections[4][1] = 0.33;
    connections[4][2] = 0.75;
    connections[4][3] = 1;

    // This is minimum distance so the matches are: 0.25 + 0.33
    final int[][] expected = new int[][] {{1, 2}, {4, 1}};
    assertMatchingFunction(MinimumDistanceMatchingFunction.instance(), connections, 1, expected);
  }

  @Test
  void testMinimumDistanceWithSubGraphs() {
    final double[][] connections = new double[10][];
    for (int i = 0; i < connections.length; i++) {
      connections[i] = SimpleArrayUtils.newDoubleArray(10, Double.MAX_VALUE);
    }
    connections[1][1] = 0;
    connections[1][2] = 0.25;
    connections[4][1] = 0.33;
    connections[4][2] = 0.75;
    connections[4][3] = 1;
    connections[6][7] = 0.1;
    connections[7][9] = 0.1;
    connections[9][7] = 0;
    connections[9][9] = 0.1;

    // Two sub-graphs with minimum distance so the matches are: (0.25 + 0.33) + (0.1 + 0)
    final int[][] expected = new int[][] {{1, 2}, {4, 1}, {7, 9}, {9, 7}};
    assertMatchingFunction(MinimumDistanceMatchingFunction.instance(), connections, 1, expected);
  }

  @Test
  void testMinimumDistanceWithPerfectAssignments() {
    // This should be detected as arbitrary and assign the diagonal
    final double[][] connections2x2 = new double[2][2];
    final int[][] expected2x2 = new int[][] {{0, 0}, {1, 1}};
    assertMatchingFunction(MinimumDistanceMatchingFunction.instance(), connections2x2, 1,
        expected2x2);
    final double[][] connections3x2 = new double[3][2];
    final int[][] expected3x2 = new int[][] {{0, 0}, {1, 1}};
    assertMatchingFunction(MinimumDistanceMatchingFunction.instance(), connections3x2, 1,
        expected3x2);
    final double[][] connections2x3 = new double[2][3];
    final int[][] expected2x3 = new int[][] {{0, 0}, {1, 1}};
    assertMatchingFunction(MinimumDistanceMatchingFunction.instance(), connections2x3, 1,
        expected2x3);
  }

  @Test
  void testMinimumDistanceWithIncompleteAssignments() {
    final double[][] connections = new double[3][];
    for (int i = 0; i < connections.length; i++) {
      connections[i] = SimpleArrayUtils.newDoubleArray(2, 1);
    }
    connections[0][1] = 0;
    connections[1][0] = 0;

    final int[][] expected = new int[][] {{0, 1}, {1, 0}};
    assertMatchingFunction(MinimumDistanceMatchingFunction.instance(), connections, 1, expected);
  }

  @Test
  void testMinimumDistanceWithAssignmentsAboveMatchDistance() {
    // 1 is max distance. 2 is above distance threshold.
    //@formatter:off
    final double[][] connections = new double[][] {
      {0, 2, 2, 1},
      {2, 0, 2, 2},
      {2, 2, 2, 1},
      {2, 2, 1, 0},
    };
    //@formatter:on

    // Here the Matchings mapping will convert those distances above the threshold to more than
    // double any other value. Thus {2,3} + {3,2} is favoured over {2,2} + {3,3}.
    final int[][] expected = new int[][] {{0, 0}, {1, 1}, {2, 3}, {3, 2}};
    assertMatchingFunction(MinimumDistanceMatchingFunction.instance(), connections, 1, expected);
  }

  @Disabled("The minimum distance matching does not currently totally exclude those above the distance threshold")
  @Test
  void testMinimumDistanceWithAssignmentsAboveMatchDistanceB() {
    // 1 is max distance. 2 is above distance threshold.
    //@formatter:off
    final double[][] connections = new double[][] {
      {0, 1, 2},
      {2, 0, 1},
      {1, 2, 2},
    };
    //@formatter:on

    // Possible:
    // 1A 0
    // 2B 0
    // 3C undef

    // This should be preferred (i.e. include allowed connections and ignore those above
    // the distance threshold)
    // 1B max
    // 2C max
    // 3A max

    final int[][] expected = new int[][] {{0, 1}, {1, 2}, {2, 0}};
    assertMatchingFunction(MinimumDistanceMatchingFunction.instance(), connections, 1, expected);
  }

  @Test
  void testMinimumDistanceThrowsWithInfiniteRange() {
    //@formatter:off
    final double[][] connections = new double[][] {
      {Double.MAX_VALUE, 0},
      {-Double.MAX_VALUE, 0},
    };
    //@formatter:on

    final List<Integer> verticesA =
        IntStream.range(0, connections.length).boxed().collect(Collectors.toList());
    final List<Integer> verticesB =
        IntStream.range(0, connections[0].length).boxed().collect(Collectors.toList());
    final ToDoubleBiFunction<Integer, Integer> edges = (u, v) -> {
      return connections[u][v];
    };
    final double threshold = Double.MAX_VALUE;

    Assertions.assertThrows(IllegalArgumentException.class,
        () -> Matchings.minimumDistance(verticesA, verticesB, edges, threshold, null, null, null));
  }

  private static void
      assertMatchingFunctionWithNoVertices(MatchingFunction<Integer, Integer> function) {
    final List<Integer> empty = new ArrayList<>();
    final List<Integer> notEmpty = Arrays.asList(1, 2, 3, 4, 5);
    final ToDoubleBiFunction<Integer, Integer> edges = (u, v) -> {
      return 0;
    };
    final int threshold = 1;
    final List<Integer> unmatchedA = new ArrayList<>();
    final List<Integer> unmatchedB = new ArrayList<>();
    int max =
        function.compute(empty, notEmpty, edges, threshold, null, unmatchedA::add, unmatchedB::add);
    Assertions.assertEquals(0, max, "Empty A");
    Assertions.assertEquals(empty, unmatchedA, "Empty A != Unmatched A");
    Assertions.assertEquals(notEmpty, unmatchedB, "NotEmpty B != Unmatched B");

    unmatchedA.clear();
    unmatchedB.clear();
    max =
        function.compute(notEmpty, empty, edges, threshold, null, unmatchedA::add, unmatchedB::add);
    Assertions.assertEquals(0, max, "Empty B");
    Assertions.assertEquals(notEmpty, unmatchedA, "NotEmpty A != Unmatched A");
    Assertions.assertEquals(empty, unmatchedB, "Empty B != Unmatched B");
  }

  private static void
      assertMatchingFunctionWithNoEdges(MatchingFunction<Integer, Integer> function) {
    final double[][] connections = new double[6][];
    for (int i = 0; i < connections.length; i++) {
      connections[i] = SimpleArrayUtils.newDoubleArray(6, Double.MAX_VALUE);
    }

    final int[][] expected = new int[0][0];
    assertMatchingFunction(function, connections, 1, expected);
  }

  private static void
      assertMatchingFunctionWithMaxCardinality(MatchingFunction<Integer, Integer> function) {
    final double[][] connections = new double[6][];
    for (int i = 0; i < connections.length; i++) {
      connections[i] = SimpleArrayUtils.newDoubleArray(2, Double.MAX_VALUE);
    }
    connections[1][0] = 0;
    connections[4][1] = 0.75;

    final int[][] expected = new int[][] {{1, 0}, {4, 1}};
    assertMatchingFunction(function, connections, 1, expected);
  }

  private static void assertMatchingFunction(MatchingFunction<Integer, Integer> function,
      double[][] connections, double threshold, int[][] expected) {
    assertMatchingFunction(function, connections, threshold, expected, true);
  }

  /**
   * Assert matching function.
   *
   * @param function the function
   * @param connections the connections
   * @param threshold the threshold
   * @param expected the expected
   * @param testEdges set to true to test the expected edges; set to false if the solution is not
   *        unique and edges may differ.
   */
  private static void assertMatchingFunction(MatchingFunction<Integer, Integer> function,
      double[][] connections, double threshold, int[][] expected, boolean testEdges) {
    final List<Integer> verticesA =
        IntStream.range(0, connections.length).boxed().collect(Collectors.toList());
    final List<Integer> verticesB =
        IntStream.range(0, connections[0].length).boxed().collect(Collectors.toList());
    final ToDoubleBiFunction<Integer, Integer> edges = (u, v) -> {
      return connections[u][v];
    };
    int max = function.compute(verticesA, verticesB, edges, threshold, null, null, null);
    Assertions.assertEquals(expected.length, max, "Cardinality with no consumers");

    final List<Pair<Integer, Integer>> pairs = new ArrayList<>();
    final BiConsumer<Integer, Integer> matched = (u, v) -> {
      pairs.add(Pair.of(u, v));
    };

    final List<Integer> unmatchedA = new ArrayList<>();
    final List<Integer> unmatchedB = new ArrayList<>();

    max = function.compute(verticesA, verticesB, edges, threshold, matched, unmatchedA::add,
        unmatchedB::add);
    Assertions.assertEquals(expected.length, max, "Cardinality with consumers");
    Assertions.assertEquals(expected.length, pairs.size(), "Size of matched");

    if (testEdges) {
      for (final Pair<Integer, Integer> pair : pairs) {
        boolean found = false;
        for (final int[] edge : expected) {
          if (pair.getLeft() == edge[0] && pair.getRight() == edge[1]) {
            found = true;
            break;
          }
        }
        Assertions.assertTrue(found, "Missing an expected edge");
      }
    }

    verticesA.removeIf(i -> {
      for (final int[] edge : expected) {
        if (i == edge[0]) {
          return true;
        }
      }
      return false;
    });
    Collections.sort(verticesA);
    Collections.sort(unmatchedA);
    Assertions.assertEquals(verticesA, unmatchedA, "Unmatched A");

    verticesB.removeIf(i -> {
      for (final int[] edge : expected) {
        if (i == edge[1]) {
          return true;
        }
      }
      return false;
    });
    Collections.sort(verticesB);
    Collections.sort(unmatchedB);
    Assertions.assertEquals(verticesB, unmatchedB, "Unmatched B");
  }
}
