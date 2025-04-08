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

package uk.ac.sussex.gdsc.core.match;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.match.BiPartiteGraphs.IntQueue;

/**
 * Test for {@link BiPartiteGraphs}.
 */
@SuppressWarnings({"javadoc"})
class BiPartiteGraphsTest {
  @Test
  void testIntQueue() {
    final IntQueue queue = new IntQueue(10);
    Assertions.assertTrue(queue.empty());
    queue.put(42);
    Assertions.assertFalse(queue.empty());
    queue.put(99);
    Assertions.assertEquals(42, queue.take());
    Assertions.assertFalse(queue.empty());
    Assertions.assertEquals(99, queue.take());
    Assertions.assertTrue(queue.empty());
  }

  @Test
  void testExtractSubGraphsEmpty() {
    assertExtractSubGraphs(new int[][] {{}});
  }

  @Test
  void testExtractSubGraphs2x2Empty() {
    // @formatter:off
    assertExtractSubGraphs(new int[][] {
      { 0, 0 },
      { 0, 0 },
    });
    // @formatter:on
  }

  @Test
  void testExtractSubGraphs2x2() {
    // @formatter:off
    assertExtractSubGraphs(new int[][] {
      { 1, 0 },
      { 1, 1 },
    }, new int[] {0, 1}, new int[] {0, 1});
    // @formatter:on
  }

  @Test
  void testExtractSubGraphs4x4WithNonConnectedA() {
    // @formatter:off
    assertExtractSubGraphs(new int[][] {
      { 1, 0, 0, 0 },
      { 1, 1, 1, 1 },
      { 0, 0, 0, 0 },
      { 0, 0, 0, 1 },
    }, new int[] {0, 1, 3}, new int[] {0, 1, 2, 3});
    // @formatter:on
  }

  @Test
  void testExtractSubGraphs4x4WithNonConnectedB() {
    // @formatter:off
    assertExtractSubGraphs(new int[][] {
      { 1, 0, 0, 0 },
      { 1, 1, 0, 1 },
      { 0, 1, 0, 0 },
      { 0, 0, 0, 1 },
    }, new int[] {0, 1, 2, 3}, new int[] {0, 1, 3});
    // @formatter:on
  }

  @Test
  void testExtractSubGraphs4x4WithNonConnectedAB() {
    // @formatter:off
    assertExtractSubGraphs(new int[][] {
      { 0, 0, 0, 0 },
      { 1, 1, 0, 1 },
      { 0, 1, 0, 0 },
      { 0, 0, 0, 1 },
    }, new int[] {1, 2, 3}, new int[] {0, 1, 3});
    // @formatter:on
  }

  @Test
  void testExtractSubGraphs4x4TwoSubGraphs() {
    // @formatter:off
    assertExtractSubGraphs(new int[][] {
      { 1, 0, 0, 0 },
      { 1, 1, 0, 0 },
      { 0, 0, 2, 2 },
      { 0, 0, 0, 2 },
    }, new int[] {0, 1}, new int[] {0, 1},
       new int[] {2, 3}, new int[] {2, 3});
    // @formatter:on
  }

  @Test
  void testExtractSubGraphs4x4TwoMixedSubGraphs() {
    // @formatter:off
    assertExtractSubGraphs(new int[][] {
      { 1, 0, 0, 0 },
      { 0, 0, 2, 2 },
      { 1, 1, 0, 0 },
      { 0, 0, 0, 2 },
    }, new int[] {0, 2}, new int[] {0, 1},
       new int[] {1, 3}, new int[] {2, 3});
    // @formatter:on
  }

  private static void assertExtractSubGraphs(int[][] connections, int[]... expected) {
    final int sizeA = connections.length;
    final int sizeB = connections[0].length;
    final List<Pair<int[], int[]>> actual =
        BiPartiteGraphs.extractSubGraphs(sizeA, sizeB, (a, b) -> connections[a][b] != 0);
    Assertions.assertEquals(expected.length / 2, actual.size());
    for (int i = 0; i < expected.length; i += 2) {
      final int[] setA = expected[i];
      final int[] setB = expected[i + 1];
      // Find in the sub-graphs
      boolean found = false;
      for (final Pair<int[], int[]> pair : actual) {
        if (Arrays.equals(setA, pair.getKey()) && Arrays.equals(setB, pair.getValue())) {
          found = true;
          break;
        }
      }
      Assertions.assertTrue(found,
          () -> String.format("Missing: %s %s", Arrays.toString(setA), Arrays.toString(setB)));
    }
  }
}
