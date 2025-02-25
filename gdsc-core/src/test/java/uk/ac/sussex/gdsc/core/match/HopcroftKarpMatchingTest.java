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

package uk.ac.sussex.gdsc.core.match;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.match.HopcroftKarpMatching.IntBiConsumer;
import uk.ac.sussex.gdsc.core.match.HopcroftKarpMatching.IntList;
import uk.ac.sussex.gdsc.core.match.HopcroftKarpMatching.IntQueue;
import uk.ac.sussex.gdsc.test.utils.functions.IndexSupplier;

/**
 * Test for {@link HopcroftKarpMatching}.
 */
@SuppressWarnings({"javadoc"})
class HopcroftKarpMatchingTest {
  @Test
  void testCheckValidIndex() {
    HopcroftKarpMatching.checkValidIndex(1, "test");
    HopcroftKarpMatching.checkValidIndex(Integer.MAX_VALUE - 1, "test");

    for (final int value : new int[] {-1, 0, Integer.MAX_VALUE}) {
      Assertions.assertThrows(IllegalArgumentException.class,
          () -> HopcroftKarpMatching.checkValidIndex(value, "test"));
    }
  }

  @Test
  void testIncreaseCapacity() {
    Assertions.assertEquals(Integer.MAX_VALUE,
        HopcroftKarpMatching.increaseCapacity(Integer.MAX_VALUE - 2));
    Assertions.assertEquals(Integer.MAX_VALUE,
        HopcroftKarpMatching.increaseCapacity(Integer.MAX_VALUE - 1));
    Assertions.assertEquals(Integer.MAX_VALUE,
        HopcroftKarpMatching.increaseCapacity(Integer.MAX_VALUE));

    int size = 0;
    for (;;) {
      final int newSize = HopcroftKarpMatching.increaseCapacity(size);
      Assertions.assertTrue(newSize > size);
      if (newSize == Integer.MAX_VALUE) {
        break;
      }
      size = newSize;
    }
  }

  @Test
  void testGetSize() {
    Assertions.assertEquals(0, HopcroftKarpMatching.getSize(null));
    final IntList list = new IntList(1);
    Assertions.assertEquals(0, HopcroftKarpMatching.getSize(list));
    list.add(67868);
    Assertions.assertEquals(1, HopcroftKarpMatching.getSize(list));
  }

  @Test
  void testIntLst() {
    final IntList list = new IntList(1);
    for (int i = 1; i <= 10; i++) {
      list.add(i);
      Assertions.assertEquals(i, list.size(), "Size");
    }
    final int[] data = list.getData();
    for (int i = 1; i <= 10; i++) {
      Assertions.assertEquals(i, data[i - 1], "list data");
    }
    list.clear();
    Assertions.assertEquals(0, list.size(), "List should be cleared");
  }

  @Test
  void testIntQueue() {
    final IntQueue queue = new IntQueue(1);
    for (int i = 1; i <= 10; i++) {
      queue.add(i);
    }
    for (int i = 1; i <= 10; i++) {
      final int value = queue.remove();
      Assertions.assertEquals(i, value, "First empty");
    }
    for (int i = 1; i <= 10; i++) {
      queue.add(i * 2);
    }
    for (int i = 1; i <= 10; i++) {
      final int value = queue.remove();
      Assertions.assertEquals(i * 2, value, "Second empty");
    }
  }

  @Test
  void computeWithPairData() {
    // @formatter:off
    // U : 1   2
    //      \ / \
    // V :   1   2
    // @formatter:on
    final HopcroftKarpMatching matching = new HopcroftKarpMatching();
    Assertions.assertEquals(0, matching.getU(), "U");
    Assertions.assertEquals(0, matching.getV(), "V");
    Assertions.assertEquals(0, matching.compute(), "matching");
    matching.addEdge(1, 1);
    matching.addEdge(2, 1);
    matching.addEdge(2, 2);
    Assertions.assertEquals(2, matching.getU(), "U");
    Assertions.assertEquals(2, matching.getV(), "V");
    final EdgeCollector ec = new EdgeCollector();
    Assertions.assertEquals(2, matching.compute(ec), "matching");
    assertEdges(new int[][] {{1, 1}, {2, 2}}, ec);

    // Test clearing the data
    matching.clear();
    ec.clear();
    Assertions.assertEquals(0, matching.getU(), "U");
    Assertions.assertEquals(0, matching.getV(), "V");
    Assertions.assertEquals(0, matching.compute(ec), "matching");
    assertEdges(new int[0][0], ec);
  }

  @Test
  void computeWithLargePairData() {
    // @formatter:off
    // U : 10  2
    //      \ / \
    // V :  100  2
    // @formatter:on
    final HopcroftKarpMatching matching = new HopcroftKarpMatching();
    matching.addEdge(10, 100);
    matching.addEdge(2, 100);
    matching.addEdge(2, 2);
    Assertions.assertEquals(10, matching.getU(), "U");
    Assertions.assertEquals(100, matching.getV(), "V");
    Assertions.assertEquals(2, matching.compute(), "matching");
  }

  @Test
  void computeWithTripletData() {
    // @formatter:off
    // U : 1   2   3   4
    //      \ / \ / \ /
    // V :   1   2   3
    // @formatter:on
    final HopcroftKarpMatching matching = new HopcroftKarpMatching();
    matching.addEdge(1, 1);
    matching.addEdge(2, 1);
    matching.addEdge(2, 2);
    matching.addEdge(3, 2);
    matching.addEdge(3, 3);
    matching.addEdge(4, 3);
    Assertions.assertEquals(4, matching.getU(), "U");
    Assertions.assertEquals(3, matching.getV(), "V");
    final EdgeCollector ec = new EdgeCollector();
    Assertions.assertEquals(3, matching.compute(ec), "matching");

    // Note: There are two possible maximal matchings
    try {
      assertEdges(new int[][] {{1, 1}, {2, 2}, {3, 3}}, ec);
    } catch (final AssertionError ex) {
      assertEdges(new int[][] {{2, 1}, {3, 2}, {4, 3}}, ec);
    }
  }

  @Test
  void computeWithNetworkData() {
    // Data from Wikipedia:
    // https://en.wikipedia.org/wiki/Hopcroft%E2%80%93Karp_algorithm#/media/File:HopcroftKarpExample.png
    final HopcroftKarpMatching matching = new HopcroftKarpMatching();
    matching.addEdge(1, 1);
    matching.addEdge(1, 2);
    matching.addEdge(2, 1);
    matching.addEdge(2, 5);
    matching.addEdge(3, 3);
    matching.addEdge(3, 4);
    matching.addEdge(4, 1);
    matching.addEdge(4, 5);
    matching.addEdge(5, 2);
    matching.addEdge(5, 4);
    Assertions.assertEquals(5, matching.getU(), "U");
    Assertions.assertEquals(5, matching.getV(), "V");
    final EdgeCollector ec = new EdgeCollector();
    Assertions.assertEquals(5, matching.compute(ec), "matching");
    assertEdges(new int[][] {{1, 2}, {2, 5}, {3, 3}, {4, 1}, {5, 4}}, ec);
  }

  @Test
  void computeWithDuplicateNetworkData() {
    // Data from Wikipedia:
    // https://en.wikipedia.org/wiki/Hopcroft%E2%80%93Karp_algorithm#/media/File:HopcroftKarpExample.png
    final HopcroftKarpMatching matching = new HopcroftKarpMatching();
    matching.addEdge(1, 1);
    matching.addEdge(1, 2);
    matching.addEdge(2, 1);
    matching.addEdge(2, 5);
    matching.addEdge(3, 3);
    matching.addEdge(3, 4);
    matching.addEdge(4, 1);
    matching.addEdge(4, 5);
    matching.addEdge(5, 2);
    matching.addEdge(5, 4);

    final EdgeCollector ec = new EdgeCollector();
    matching.getEdges(ec);
    assertEdges(new int[][] {{1, 1}, {1, 2}, {2, 1}, {2, 5}, {3, 3}, {3, 4}, {4, 1}, {4, 5}, {5, 2},
        {5, 4}}, ec);

    // Duplicate to test if the algorithm is robust to duplicate edges
    for (final int[] edge : ec.getEdges()) {
      matching.addEdge(edge[0], edge[1]);
    }

    Assertions.assertEquals(5, matching.getU(), "U");
    Assertions.assertEquals(5, matching.getV(), "V");
    ec.clear();
    Assertions.assertEquals(5, matching.compute(ec), "matching");
    assertEdges(new int[][] {{1, 2}, {2, 5}, {3, 3}, {4, 1}, {5, 4}}, ec);
  }

  @Test
  void computeWithSparseData() {
    final HopcroftKarpMatching matching = new HopcroftKarpMatching();
    matching.addEdge(1, 1);
    matching.addEdge(1, 2);
    matching.addEdge(2, 1);
    matching.addEdge(3, 1);
    matching.addEdge(4, 1);
    matching.addEdge(5, 1);
    matching.addEdge(6, 2);
    matching.addEdge(7, 2);
    Assertions.assertEquals(7, matching.getU());
    Assertions.assertEquals(2, matching.getV());
    Assertions.assertEquals(2, matching.compute());
  }

  /**
   * Assert the edges are correct.
   *
   * @param expected the expected edges
   * @param ec the edge collector
   */
  private static void assertEdges(int[][] expected, EdgeCollector ec) {
    final List<int[]> edges = ec.getEdges();
    Assertions.assertEquals(expected.length, edges.size(), "edge count");
    final IndexSupplier msg = new IndexSupplier(1, "edge ", null);
    for (int i = 0; i < expected.length; i++) {
      Assertions.assertArrayEquals(expected[i], edges.get(i), msg.set(0, i));
    }
  }

  private static class EdgeCollector implements IntBiConsumer {
    private final ArrayList<int[]> edges = new ArrayList<>();

    @Override
    public void accept(int uu, int vv) {
      edges.add(new int[] {uu, vv});
    }

    List<int[]> getEdges() {
      return edges;
    }

    void clear() {
      edges.clear();
    }
  }
}
