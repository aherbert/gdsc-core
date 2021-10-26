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

package uk.ac.sussex.gdsc.core.match;

import gnu.trove.list.array.TIntArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import org.apache.commons.lang3.tuple.Pair;
import uk.ac.sussex.gdsc.core.data.VisibleForTesting;

/**
 * Functions for bipartite graphs.
 */
public final class BiPartiteGraphs {
  /**
   * A fixed capacity array backed FIFO queue.
   */
  @VisibleForTesting
  static class IntQueue {
    private int pos;
    private int size;
    private final int[] data;

    /**
     * Create a new instance.
     *
     * @param capacity the capacity
     */
    IntQueue(int capacity) {
      data = new int[capacity];
    }

    /**
     * Clear the queue.
     */
    void clear() {
      pos = size = 0;
    }

    /**
     * Test if empty.
     *
     * @return true if empty
     */
    boolean empty() {
      return size == pos;
    }

    /**
     * Put the element at the end of the queue. No checks are made on the current capacity.
     *
     * @param e the element
     */
    void put(int e) {
      data[size++] = e;
    }

    /**
     * Take the head of the queue. No checks are made that the queue contains any data so only call
     * if {@link #empty()} is false.
     *
     * @return the element
     */
    int take() {
      return data[pos++];
    }
  }

  /**
   * Represents a predicate (boolean-valued function) of two arguments. This is the
   * {@code int}-consuming primitive type specialization of {@link BiPredicate}.
   */
  @FunctionalInterface
  @VisibleForTesting
  interface IntIntBiPredicate {
    /**
     * Evaluates this predicate on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     * @return {@code true} if the input arguments match the predicate, otherwise {@code false}
     */
    boolean test(int t, int u);
  }

  /** No public construction. */
  private BiPartiteGraphs() {}

  /**
   * Extract distinct (non-connected) sub-graphs of a bipartite graph between two sets of vertices.
   * Vertices in each set are assumed to be an index in {@code [0, size)}.
   *
   * <p>The sub-graphs will be a pair of two arrays of indexes corresponding to the vertices from
   * set A and B that are connected into a sub-graph. Vertices that do not connect will be excluded
   * from the results.
   *
   * @param sizeA the size of the first set of vertices
   * @param sizeB the size of the second set of vertices
   * @param edges the edges (connections) between vertices in the two sets
   * @return the list of sub-graphs
   */
  public static List<Pair<int[], int[]>> extractSubGraphs(int sizeA, int sizeB,
      IntIntBiPredicate edges) {
    // Assigned sub-graph identifiers
    final int[] idA = new int[sizeA];
    final int[] idB = new int[sizeB];
    int id = 0;
    // FIFO queue for vertices to search
    final IntQueue queue = new IntQueue(sizeA);

    // Loop over each vertex in A while there exist unassigned vertices in B
    int remaining = sizeB;
    for (int a = 0; a < sizeA && remaining != 0; a++) {
      if (idA[a] != 0) {
        continue;
      }

      // New sub-graph
      id++;
      remaining = expandSubGraph(sizeA, sizeB, edges, idA, idB, id, queue, remaining, a);
    }

    // Extract sub-graphs.
    // Only include those with connections from A to B.
    final ArrayList<Pair<int[], int[]>> list = new ArrayList<>(id);
    final TIntArrayList mA = new TIntArrayList(sizeA);
    final TIntArrayList mB = new TIntArrayList(sizeB);
    int start = 0;
    for (int i = 1; i <= id; i++) {
      // Note that all indexes in A will be assigned. Some may have no connections.
      // So search B for at least 1 connection.
      extractSubGraphIndices(idB, 0, i, mB);
      if (!mB.isEmpty()) {
        extractSubGraphIndices(idA, start, i, mA);
        start = mA.getQuick(0);
        list.add(Pair.of(mA.toArray(), mB.toArray()));
      }
    }
    return list;
  }

  /**
   * Expand the sub-graph.
   *
   * @param sizeA the size of the first set of vertices
   * @param sizeB the size of the second set of vertices
   * @param edges the edges (connections) between vertices in the two sets
   * @param idA the assigned sub-graph identifiers for vertices A
   * @param idB the assigned sub-graph identifiers for vertices B
   * @param id the sub-graph id
   * @param queue the working space for queueing search vertices
   * @param remaining the number of remaining vertices in B
   * @param a the vertex in A to start the sub-graph
   * @return the number of remaining vertices in B
   */
  private static int expandSubGraph(int sizeA, int sizeB, IntIntBiPredicate edges, final int[] idA,
      final int[] idB, int id, final IntQueue queue, int remaining, int a) {
    queue.clear();
    addToSearch(queue, a, idA, id);

    // Search from a vertex in the sub-graph
    while (!queue.empty()) {
      // Note: i >= a and idA[i] == id
      final int i = queue.take();
      // Check for a connection to a free vertex in B
      for (int b = 0; b < sizeB; b++) {
        if (idB[b] != 0 || !edges.test(i, b)) {
          continue;
        }
        // This is connected to this sub-graph
        idB[b] = id;
        remaining--;
        // Add all connected vertices in A to the search.
        // We only need search those remaining.
        for (int ii = a + 1; ii < sizeA; ii++) {
          if (idA[ii] != 0) {
            continue;
          }
          if (edges.test(ii, b)) {
            addToSearch(queue, ii, idA, id);
          }
        }
        // Check if it is possible to expand the sub-graph
        if (remaining == 0) {
          return 0;
        }
      }
    }
    return remaining;
  }

  /**
   * Extract all indices assigned to the specified sub-graph to the list.
   *
   * @param ids the assignments for each index
   * @param start the start index to begin the search
   * @param id the sub-graph id
   * @param list the list
   */
  private static void extractSubGraphIndices(int[] ids, int start, int id, TIntArrayList list) {
    list.clear();
    for (int i = 0; i < ids.length; i++) {
      if (ids[i] == id) {
        list.add(i);
      }
    }
  }

  /**
   * Adds the vertex to the search and assigns it to the sub-graph.
   *
   * @param queue the queue
   * @param a the vertex
   * @param idA the current sub-graph identifiers
   * @param id the sub-graph id
   */
  private static void addToSearch(IntQueue queue, int a, int[] idA, int id) {
    queue.put(a);
    idA[a] = id;
  }
}
