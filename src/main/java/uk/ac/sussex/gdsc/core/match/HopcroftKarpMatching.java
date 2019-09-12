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
 * Copyright (C) 2011 - 2019 Alex Herbert
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

import uk.ac.sussex.gdsc.core.data.VisibleForTesting;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Class to compute the maximum cardinality matching of a bipartite graph using the Hopcroft–Karp
 * algorithm.
 *
 * <p>The bipartite graph G(U, V, E) is stored using a sparse representation of edges E between
 * source vertices U and target vertices V ({@code u -> v}).
 *
 * @see <a href="https://en.wikipedia.org/wiki/Bipartite_graph">Bipartite graph</a>
 * @see <a href="https://en.wikipedia.org/wiki/Hopcroft%E2%80%93Karp_algorithm">Hopcroft–Karp
 *      algorithm</a>
 *
 * @since 2.0
 */
public class HopcroftKarpMatching {
  /** The special null vertex. */
  private static final int NIL = 0;
  /** The special value for infinite distance. */
  private static final int INF = Integer.MAX_VALUE;

  /** The maximum vertex in V. This is stored to avoid dynamic computation from all the edges. */
  private int sizeV;

  /**
   * The adjacent vertices for each vertex u (e.g. edges E in the bipartite graph G(U union V, E).
   */
  private IntList[] adj;

  /** The working queue of vertices. */
  private final IntQueue queue;

  /**
   * A simple list.
   */
  @VisibleForTesting
  static class IntList {
    private int[] data;
    private int size;

    /**
     * Create a new instance.
     *
     * @param capacity the capacity
     */
    IntList(int capacity) {
      data = new int[capacity];
    }

    /**
     * Inserts the specified element into this queue if it is possible to do so immediately without
     * violating capacity restrictions.
     *
     * @param value the element to add
     */
    void add(int value) {
      if (size == data.length) {
        data = Arrays.copyOf(data, increaseCapacity(size));
      }
      data[size++] = value;
    }

    /**
     * Get the size.
     *
     * @return the size
     */
    int size() {
      return size;
    }

    /**
     * Get the item data. This may be larger than the list size.
     *
     * @return the item data
     */
    int[] getData() {
      return data;
    }

    /**
     * Clear the list.
     */
    void clear() {
      size = 0;
    }
  }

  /**
   * A simple FIFO queue.
   */
  @VisibleForTesting
  static class IntQueue {
    private int[] data;
    private int head;
    private int tail;

    /**
     * Create a new instance.
     *
     * @param capacity the capacity
     */
    IntQueue(int capacity) {
      data = new int[capacity];
    }

    /**
     * Inserts the specified element into this queue if it is possible to do so immediately without
     * violating capacity restrictions.
     *
     * @param value the element to add
     */
    void add(int value) {
      if (tail == data.length) {
        // If the size is less than half the capacity then recycle the space
        if (head > tail / 2) {
          final int length = tail - head;
          System.arraycopy(data, head, data, 0, length);
          tail -= head;
          head = 0;
        } else {
          // Increase capacity
          data = Arrays.copyOf(data, increaseCapacity(tail));
        }
      }
      data[tail++] = value;
    }

    /**
     * Retrieves and removes the head of this queue.
     *
     * <p>Only call when the queue is not empty or else the results are undefined.
     *
     * @return the head of this queue
     */
    int remove() {
      return data[head++];
    }

    /**
     * Check if the queue is empty.
     *
     * @return true if empty
     */
    boolean empty() {
      return tail == head;
    }

    /**
     * Clear the queue.
     */
    void clear() {
      head = tail = 0;
    }
  }

  // Allow u and v for the vertex names.
  // @CHECKSTYLE.OFF: ParameterName

  /**
   * Represents an operation that accepts two input arguments and returns no result.
   */
  @FunctionalInterface
  public interface IntBiConsumer {
    /**
     * Performs this operation on the given arguments.
     *
     * @param u the first input argument
     * @param v the second input argument
     */
    void accept(int u, int v);
  }

  /**
   * Create a new instance.
   */
  public HopcroftKarpMatching() {
    adj = new IntList[10];
    queue = new IntQueue(10);
  }

  /**
   * Adds the edge of a bipartite graph from u to v (and v to u).
   *
   * <p>The vertices are 1-indexed.
   *
   * <p>Note that this method may be called to add new vertices after a computation using
   * {@link #compute()}.
   *
   * <p><strong>Warning</strong>: There is no check for duplicate vertices. These should not affect
   * the algorithm results but may affect the run-time. The duplicates are not filtered and will be
   * reported by the {@link #getEdges(IntBiConsumer)} method.
   *
   * @param u the source node
   * @param v the target node
   * @throws IllegalArgumentException if {@code u} or {@code v} are not {@code > 0} and less than
   *         {@link Integer#MAX_VALUE}
   */
  public void addEdge(int u, int v) {
    // Check bad arguments
    checkValidIndex(u, "source node (u)");
    checkValidIndex(v, "target node (v)");

    // Support dynamic sizing
    if (sizeV < v) {
      sizeV = v;
    }
    if (adj.length <= u) {
      adj = Arrays.copyOf(adj, increaseCapacity(adj.length));
    }
    if (adj[u] == null) {
      adj[u] = new IntList(1);
    }
    // Q. Should this check for duplicates?
    // A. The algorithm should handle duplicates. A second occurrence of v in the list
    // results will effect the DFS. It will either be ignored or be processed again if
    // the matching can be improved since the first occurrence was observed.
    adj[u].add(v);
  }

  /**
   * Gets the number of source vertices (U) in the bipartite graph. This is the maximum vertex
   * number u added to the graph and thus includes vertices that may not have been explicitly added
   * using an edge.
   *
   * <p>This can be used to determine the size of the graph before consuming the graph edges using
   * {@link #getEdges(IntBiConsumer)}.
   *
   * @return the number of source vertices
   * @see #addEdge(int, int)
   */
  public int getU() {
    // Compute dynamically the highest u with an edge
    for (int u = adj.length - 1; u > 0; u--) {
      if (getSize(adj[u]) != 0) {
        return u;
      }
    }
    return 0;
  }

  /**
   * Gets the number of target vertices (V) in the bipartite graph. This is the maximum vertex
   * number v added to the graph and thus includes vertices that may not have been explicitly added
   * using an edge.
   *
   * <p>This can be used to determine the size of the graph before consuming the graph edges using
   * {@link #getEdges(IntBiConsumer)}.
   *
   * @return the number of target vertices
   * @see #addEdge(int, int)
   */
  public int getV() {
    return sizeV;
  }

  /**
   * Gets the edges ({@code u -> v}) in the bipartite graph.
   *
   * @param edgeConsumer the edge consumer
   */
  public void getEdges(IntBiConsumer edgeConsumer) {
    Objects.requireNonNull(edgeConsumer, "edge consumer must not be null");
    for (int u = 1; u < adj.length; u++) {
      final int size = getSize(adj[u]);
      if (size != 0) {
        final int[] list = adj[u].getData();
        for (int i = 0; i < size; i++) {
          edgeConsumer.accept(u, list[i]);
        }
      }
    }
  }

  /**
   * Clear the set of vertices and edges.
   *
   * <p>This will not release allocated memory allowing reuse of the space for a similar sized
   * graph.
   */
  public void clear() {
    sizeV = 0;
    for (final IntList edge : adj) {
      if (edge != null) {
        edge.clear();
      }
    }
  }

  /**
   * Compute the maximum cardinality matching in the bipartite graph.
   *
   * @return the maximum cardinality matching
   */
  public int compute() {
    return compute(null);
  }

  /**
   * Compute the maximum cardinality matching in the bipartite graph. The final edges of the
   * matching ({@code u -> v}) are passed to the consumer if supplied.
   *
   * @param edgeConsumer the edge consumer
   * @return the maximum cardinality matching
   */
  public int compute(IntBiConsumer edgeConsumer) {
    // Only process the vertices u that have an edge
    final int[] activeU =
        IntStream.range(1, adj.length).filter(u -> getSize(adj[u]) != 0).toArray();

    if (activeU.length == 0) {
      return 0;
    }

    // Allocate working space
    final int sizeU = activeU[activeU.length - 1];
    final int[] pairU = new int[sizeU + 1];
    final int[] pairV = new int[sizeV + 1];
    final int[] dist = new int[pairU.length];

    final int maximalMatching = Math.min(sizeU, sizeV);
    int matching = 0;
    while (bfs(activeU, dist, pairU, pairV)) {
      for (final int u : activeU) {
        if (pairU[u] == NIL && dfs(u, dist, pairU, pairV)) {
          matching++;
        }
      }
      // Check for maximum matching
      if (matching == maximalMatching) {
        break;
      }
    }

    // Report the maximum matching
    if (edgeConsumer != null) {
      for (final int u : activeU) {
        if (pairU[u] != NIL) {
          edgeConsumer.accept(u, pairU[u]);
        }
      }
    }

    return matching;
  }

  /**
   * Perform a Breadth-First Search (BFS).
   *
   * @param activeU the active U
   * @param dist The working distance of source U.
   * @param pairV The working pairing of source U.
   * @param pairV The working pairing of target V.
   * @return true if successful
   */
  private boolean bfs(int[] activeU, int[] dist, int[] pairU, int[] pairV) {
    queue.clear();

    for (final int u : activeU) {
      if (pairU[u] == NIL) {
        dist[u] = 0;
        queue.add(u);
      } else {
        dist[u] = INF;
      }
    }

    dist[NIL] = INF;

    while (!queue.empty()) {
      final int u = queue.remove();

      if (dist[u] < dist[NIL]) {
        final int[] list = adj[u].getData();
        for (int i = adj[u].size(); i-- > 0;) {
          final int v = list[i];
          if (dist[pairV[v]] == INF) {
            dist[pairV[v]] = dist[u] + 1;
            queue.add(pairV[v]);
          }
        }
      }
    }

    return dist[NIL] != INF;
  }

  /**
   * Perform a Depth-First Search (DFS).
   *
   * @param u the vertex u
   * @param pairV The working pairing of source U.
   * @param pairV The working pairing of target V.
   * @param dist The working distance of source U.
   * @return true if successful
   */
  private boolean dfs(int u, int[] dist, int[] pairU, int[] pairV) {
    if (u != NIL) {
      final int[] list = adj[u].getData();
      for (int i = adj[u].size(); i-- > 0;) {
        final int v = list[i];
        if (dist[pairV[v]] == dist[u] + 1 && dfs(pairV[v], dist, pairU, pairV)) {
          pairV[v] = u;
          pairU[u] = v;
          return true;
        }
      }

      dist[u] = INF;
      return false;
    }
    return true;
  }

  /**
   * Check the specified value is a valid array index above zero.
   *
   * @param value the value
   * @param name the name of the value
   * @throws IllegalArgumentException if {@code value} is not {@code > 0} and less than
   *         {@link Integer#MAX_VALUE}
   */
  @VisibleForTesting
  static void checkValidIndex(int value, String name) {
    if (value <= 0 || value == Integer.MAX_VALUE) {
      throw new IllegalArgumentException(name + " is not in the range [1, max int): " + value);
    }
  }

  /**
   * Increase the capacity up to the maximum positive int value.
   *
   * @param size the size
   * @return the new size
   */
  @VisibleForTesting
  static int increaseCapacity(int size) {
    final int newSize = size * 2 + 1;
    return newSize <= 0 ? Integer.MAX_VALUE : newSize;
  }

  /**
   * Gets the size of the list (null-safe).
   *
   * @param list the list (can be null)
   * @return the size
   */
  @VisibleForTesting
  static int getSize(IntList list) {
    return list != null ? list.size() : 0;
  }
}
