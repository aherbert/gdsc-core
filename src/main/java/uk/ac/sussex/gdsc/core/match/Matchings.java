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
import uk.ac.sussex.gdsc.core.match.HopcroftKarpMatching.IntBiConsumer;
import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.IntStream;

/**
 * Calculates the matching of two sets.
 */
public final class Matchings {
  /** The constant for no assignment. */
  private static final int NO_ASSIGNMENT = -1;
  /** The constant for the maximum cost distance. */
  private static final int MAX_COST = 1 << 16;

  // Allow u and v for the vertex names.
  // @CHECKSTYLE.OFF: ParameterName

  /**
   * Define a consumer of matches between two sets.
   */
  @VisibleForTesting
  static interface MatchConsumer extends IntBiConsumer {
    /**
     * Run any finalisation steps after the matches have all been consumed.
     */
    void finalise();
  }

  /**
   * A consumer of matched matches.
   *
   * @param <T> type of vertices A
   * @param <U> type of vertices B
   */
  @VisibleForTesting
  static class IntersectionMatchConsumer<T, U> implements MatchConsumer {
    private final List<T> verticesA;
    private final List<U> verticesB;
    private final BiConsumer<T, U> matched;

    /**
     * Create a new instance.
     *
     * @param verticesA the vertices A
     * @param verticesB the vertices B
     * @param matched the matched
     */
    private IntersectionMatchConsumer(List<T> verticesA, List<U> verticesB,
        BiConsumer<T, U> matched) {
      this.verticesA = verticesA;
      this.verticesB = verticesB;
      this.matched = matched;
    }

    /**
     * Create a new instance.
     *
     * @param <T> type of vertices A
     * @param <U> type of vertices B
     * @param verticesA the vertices A
     * @param verticesB the vertices B
     * @param matched the matched
     * @return the match consumer
     */
    static <T, U> MatchConsumer create(List<T> verticesA, List<U> verticesB,
        BiConsumer<T, U> matched) {
      return matched != null ? new IntersectionMatchConsumer<>(verticesA, verticesB, matched)
          : null;
    }

    @Override
    public void accept(int u, int v) {
      matched.accept(verticesA.get(u - 1), verticesB.get(v - 1));
    }

    @Override
    public void finalise() {
      // Do nothing
    }
  }

  /**
   * A consumer of matched matches.
   *
   * @param <T> type of vertices A
   */
  @VisibleForTesting
  static class UnmatchedMatchConsumer<T> implements MatchConsumer {
    private final List<T> vertices;
    private final boolean first;
    private final Consumer<T> unmatched;
    private final boolean[] matched;

    /**
     * Create a new instance.
     *
     * @param vertices the vertices
     * @param unmatched the unmatched
     * @param first flag to indicate if tracking the first or second item in the accept method
     */
    private UnmatchedMatchConsumer(List<T> vertices, boolean first, Consumer<T> unmatched) {
      this.vertices = vertices;
      this.first = first;
      this.unmatched = unmatched;
      this.matched = new boolean[vertices.size()];
    }

    /**
     * Create a new instance.
     *
     * @param <T> type of vertices A
     * @param vertices the vertices
     * @param unmatched the unmatched
     * @param first flag to indicate if tracking the first or second item in the accept method
     * @return the match consumer
     */
    static <T> MatchConsumer create(List<T> vertices, boolean first, Consumer<T> unmatched) {
      return unmatched != null ? new UnmatchedMatchConsumer<>(vertices, first, unmatched) : null;
    }

    @Override
    public void accept(int u, int v) {
      final int index = first ? u : v;
      matched[index - 1] = true;
    }

    @Override
    public void finalise() {
      for (int i = 0; i < matched.length; i++) {
        if (!matched[i]) {
          unmatched.accept(vertices.get(i));
        }
      }
    }
  }

  /**
   * A composite consumer that joins two consumers.
   */
  @VisibleForTesting
  static class CompositeMatchConsumer implements MatchConsumer {
    private final MatchConsumer first;
    private final MatchConsumer second;

    /**
     * Create a new instance.
     *
     * @param first the first match consumer
     * @param second the second match consumer
     */
    private CompositeMatchConsumer(MatchConsumer first, MatchConsumer second) {
      this.first = first;
      this.second = second;
    }

    /**
     * Create a composition of two consumers.
     *
     * @param first the first
     * @param second the second
     * @return the composed match consumer
     */
    static MatchConsumer create(MatchConsumer first, MatchConsumer second) {
      if (first == null) {
        return second;
      }
      if (second == null) {
        return first;
      }
      return new CompositeMatchConsumer(first, second);
    }

    @Override
    public void accept(int u, int v) {
      first.accept(u, v);
      second.accept(u, v);
    }

    @Override
    public void finalise() {
      first.finalise();
      second.finalise();
    }
  }

  // @CHECKSTYLE.ON: ParameterName

  /** No public construction. */
  private Matchings() {}

  /**
   * Calculates the maximum cardinality matching of a bipartite graph between two sets of vertices.
   *
   * <p>The output matched and those unmatched from each set can be obtained using the consumer
   * functions.
   *
   * @param <T> type of vertices A
   * @param <U> type of vertices B
   * @param verticesA the vertices A
   * @param verticesB the vertices B
   * @param edges test used to identify edges between the vertices ({@code A -> B})
   * @param matched consumer for matched vertices (can be null)
   * @param unmatchedA consumer for the unmatched items in A (can be null)
   * @param unmatchedB consumer for the unmatched items in B (can be null)
   * @return the maximum cardinality
   * @see <a href="https://en.wikipedia.org/wiki/Maximum_cardinality_matching">Maximum cardinality
   *      matching</a>
   */
  public static <T, U> int maximumCardinality(List<T> verticesA, List<U> verticesB,
      BiPredicate<T, U> edges, BiConsumer<T, U> matched, Consumer<T> unmatchedA,
      Consumer<U> unmatchedB) {
    ValidationUtils.checkNotNull(verticesA, "vertices A");
    ValidationUtils.checkNotNull(verticesB, "vertices B");
    ValidationUtils.checkNotNull(edges, "Matcher");

    // Build bipartite graph
    final HopcroftKarpMatching matching = new HopcroftKarpMatching();
    final int sizeA = verticesA.size();
    final int sizeB = verticesB.size();
    for (int u = 0; u < sizeA; u++) {
      final T itemA = verticesA.get(u);
      for (int v = 0; v < sizeB; v++) {
        if (edges.test(itemA, verticesB.get(v))) {
          matching.addEdge(u + 1, v + 1);
        }
      }
    }
    // Set-up to consume matches
    final MatchConsumer consumer =
        createMatchConsumer(verticesA, verticesB, matched, unmatchedA, unmatchedB);
    final int max = matching.compute(consumer);
    if (consumer != null) {
      consumer.finalise();
    }
    return max;
  }

  /**
   * Creates the match consumer.
   *
   * @param <T> type of vertices A
   * @param <U> type of vertices B
   * @param verticesA the vertices A
   * @param verticesB the vertices B
   * @param matched consumer for matched vertices (can be null)
   * @param unmatchedA consumer for the unmatched items in A (can be null)
   * @param unmatchedB consumer for the unmatched items in B (can be null)
   * @return the match consumer
   */
  private static <T, U> MatchConsumer createMatchConsumer(List<T> verticesA, List<U> verticesB,
      BiConsumer<T, U> matched, Consumer<T> unmatchedA, Consumer<U> unmatchedB) {
    final MatchConsumer intersectionConsumer =
        IntersectionMatchConsumer.create(verticesA, verticesB, matched);
    final MatchConsumer unmatchedAConsumer =
        UnmatchedMatchConsumer.create(verticesA, true, unmatchedA);
    final MatchConsumer unmatchedBConsumer =
        UnmatchedMatchConsumer.create(verticesB, false, unmatchedB);
    return CompositeMatchConsumer.create(
        CompositeMatchConsumer.create(intersectionConsumer, unmatchedAConsumer),
        unmatchedBConsumer);
  }

  /**
   * Calculates a matching of a bipartite graph between two sets of weighted vertices using
   * nearest-neighbours. The distance must be less than or equal to the threshold to be considered a
   * neighbour ({@code distance <= threshold}).
   *
   * <p>This uses a single pass algorithm and holds all the neighbours in memory.
   *
   * <p>The output matched and those unmatched from each set can be obtained using the consumer
   * functions.
   *
   * @param <T> type of vertices A
   * @param <U> type of vertices B
   * @param verticesA the vertices A
   * @param verticesB the vertices B
   * @param edges function used to identify distance between the vertices ({@code A -> B})
   * @param threshold the distance threshold
   * @param matched consumer for matched vertices (can be null)
   * @param unmatchedA consumer for the unmatched items in A (can be null)
   * @param unmatchedB consumer for the unmatched items in B (can be null)
   * @return the cardinality
   * @see <a href="https://en.wikipedia.org/wiki/Maximum_cardinality_matching">Maximum cardinality
   *      matching</a>
   */
  public static <T, U> int nearestNeighbour(List<T> verticesA, List<U> verticesB,
      ToDoubleBiFunction<T, U> edges, double threshold, BiConsumer<T, U> matched,
      Consumer<T> unmatchedA, Consumer<U> unmatchedB) {
    if (verticesA.isEmpty() || verticesB.isEmpty()) {
      consume(verticesA, unmatchedA);
      consume(verticesB, unmatchedB);
      return 0;
    }

    // Determine neighbours
    final int sizeA = verticesA.size();
    final int sizeB = verticesB.size();
    final ArrayList<ImmutableAssignment> neighbours =
        findNeighbours(verticesA, verticesB, edges, threshold, sizeA, sizeB);

    AssignmentComparator.sort(neighbours);

    final boolean[] matchedA = new boolean[sizeA];
    final boolean[] matchedB = new boolean[sizeB];

    int max = 0;
    final int limit = Math.min(sizeA, sizeB);
    for (final ImmutableAssignment neighbour : neighbours) {
      if (!matchedA[neighbour.getTargetId()] && !matchedB[neighbour.getPredictedId()]) {
        matchedA[neighbour.getTargetId()] = true;
        matchedB[neighbour.getPredictedId()] = true;
        // Feed consumer
        if (matched != null) {
          matched.accept(verticesA.get(neighbour.getTargetId()),
              verticesB.get(neighbour.getPredictedId()));
        }
        max++;
        if (max == limit) {
          // Maximum cardinality has been reached
          break;
        }
      }
    }

    // Feed consumers
    consumeUnmatched(verticesA, unmatchedA, matchedA);
    consumeUnmatched(verticesB, unmatchedB, matchedB);

    return max;
  }

  /**
   * Find the neighbours in the bipartite graph.
   *
   * @param <T> type of vertices A
   * @param <U> type of vertices B
   * @param verticesA the vertices A
   * @param verticesB the vertices B
   * @param edges function used to identify distance between the vertices ({@code A -> B})
   * @param threshold the distance threshold
   * @param sizeA the size A
   * @param sizeB the size B
   * @return the neighbours
   */
  private static <T, U> ArrayList<ImmutableAssignment> findNeighbours(List<T> verticesA,
      List<U> verticesB, ToDoubleBiFunction<T, U> edges, double threshold, final int sizeA,
      final int sizeB) {
    final ArrayList<ImmutableAssignment> neighbours = new ArrayList<>(sizeA);
    for (int u = 0; u < sizeA; u++) {
      final T itemA = verticesA.get(u);
      for (int v = 0; v < sizeB; v++) {
        final double distance = edges.applyAsDouble(itemA, verticesB.get(v));
        if (distance <= threshold) {
          neighbours.add(new ImmutableAssignment(u, v, distance));
        }
      }
    }
    return neighbours;
  }

  /**
   * Pass all the list to the consumer (null-safe).
   *
   * @param <T> the generic type
   * @param list the list
   * @param consumer the consumer (can be null)
   */
  private static <T> void consume(List<T> list, Consumer<T> consumer) {
    if (consumer != null) {
      list.forEach(consumer);
    }
  }

  /**
   * Pass items from the list that are not matched to the consumer (null-safe).
   *
   * @param <T> the generic type
   * @param list the list
   * @param consumer the consumer (can be null)
   * @param matched the matched items
   */
  private static <T> void consumeUnmatched(List<T> list, Consumer<T> consumer, boolean[] matched) {
    if (consumer != null) {
      for (int i = 0; i < matched.length; i++) {
        if (!matched[i]) {
          consumer.accept(list.get(i));
        }
      }
    }
  }

  /**
   * Calculates a matching of a bipartite graph between two sets of weighted vertices using the
   * minimum sum of distances. The distance must be less than or equal to the threshold to be
   * considered a neighbour ({@code distance <= threshold}).
   *
   * <p>The cardinality of the match may not be maximal as an alternative matching may have a lower
   * or equal sum of distances.
   *
   * <p>The output matched and those unmatched from each set can be obtained using the consumer
   * functions. Use the matched consumer to obtain the sum of distances between matched vertices.
   *
   * <p>This uses an assignment algorithm based on an all-vs-all distance matrix. Distances below
   * the threshold are mapped linearly to 16-bit integers. Any vertex not within the distance
   * threshold to any other vertices is excluded.
   *
   * @param <T> type of vertices A
   * @param <U> type of vertices B
   * @param verticesA the vertices A
   * @param verticesB the vertices B
   * @param edges function used to identify distance between the vertices ({@code A -> B})
   * @param threshold the distance threshold
   * @param matched consumer for matched vertices (can be null)
   * @param unmatchedA consumer for the unmatched items in A (can be null)
   * @param unmatchedB consumer for the unmatched items in B (can be null)
   * @return the cardinality
   * @throws IllegalArgumentException If the distance threshold is not finite
   * @throws ArithmeticException if there is an overflow in the cost matrix
   * @see <a href="https://en.wikipedia.org/wiki/Assignment_problem">Assignment problem</a>
   * @see KuhnMunkresAssignment
   */
  public static <T, U> int minimumDistance(List<T> verticesA, List<U> verticesB,
      ToDoubleBiFunction<T, U> edges, double threshold, BiConsumer<T, U> matched,
      Consumer<T> unmatchedA, Consumer<U> unmatchedB) {
    if (verticesA.isEmpty() || verticesB.isEmpty()) {
      consume(verticesA, unmatchedA);
      consume(verticesB, unmatchedB);
      return 0;
    }

    ValidationUtils.checkArgument(Double.isFinite(threshold),
        "Distance threshold is not finite: %s", threshold);

    // Find the costs cost matrix. This may not be all-vs-all so
    // map indices of input lists to those in the cost matrix.
    final int sizeA = verticesA.size();
    final int sizeB = verticesB.size();
    // A pair of the matching and the cost stored as two lists
    final ArrayList<int[]> pairV = new ArrayList<>(sizeA);
    final ArrayList<double[]> pairD = new ArrayList<>(sizeA);
    final TIntArrayList mapA = new TIntArrayList(sizeA);
    // Mark all those in B that match something
    final boolean[] matchedB = new boolean[sizeB];

    findCosts(verticesA, verticesB, edges, threshold, pairV, pairD, mapA, matchedB);

    // There may be nothing within the distance threshold
    if (mapA.isEmpty()) {
      consume(verticesA, unmatchedA);
      consume(verticesB, unmatchedB);
      return 0;
    }

    // Map any B that were observed
    final TIntArrayList mapB = new TIntArrayList(sizeB);
    IntStream.range(0, sizeB).filter(v -> matchedB[v]).forEachOrdered(mapB::add);

    // Create the final cost matrix and compute assignments
    final int[] cost2 = createCostMatrix(pairV, pairD, sizeB, mapB);

    final int[] assignments = KuhnMunkresAssignment.compute(cost2, mapA.size(), mapB.size());

    // Remove those above the match distance.
    // The cost matrix is modified so recompute the distance.
    int max = 0;
    final boolean[] matchedA = new boolean[sizeA];
    Arrays.fill(matchedB, false);
    for (int u = 0; u < assignments.length; u++) {
      final int v = assignments[u];
      if (v != NO_ASSIGNMENT) {
        // Check the distance as a precaution against bad assignments that minimise
        // the sum of distances by including a pair above the maximum cost.
        final T itemA = verticesA.get(mapA.getQuick(u));
        final U itemB = verticesB.get(mapB.getQuick(v));
        if (edges.applyAsDouble(itemA, itemB) <= threshold) {
          max++;
          matchedA[mapA.getQuick(u)] = true;
          matchedB[mapB.getQuick(v)] = true;
          // Feed consumer
          if (matched != null) {
            matched.accept(itemA, itemB);
          }
        }
      }
    }

    // Feed consumers
    consumeUnmatched(verticesA, unmatchedA, matchedA);
    consumeUnmatched(verticesB, unmatchedB, matchedB);

    return max;
  }

  /**
   * Find the costs. Add to the map of A if any vertex in A matches B.
   *
   * @param <T> type of vertices A
   * @param <U> type of vertices B
   * @param verticesA the vertices A
   * @param verticesB the vertices B
   * @param edges function used to identify distance between the vertices ({@code A -> B})
   * @param threshold the distance threshold
   * @param pairV the list to hold all the matching vertices from mapped A
   * @param pairD the list to hold all the matching distances from mapped A
   * @param mapA the map of those A with a valid cost
   * @param matchedB the array to set to true for each matched B
   */
  private static <T, U> void findCosts(List<T> verticesA, List<U> verticesB,
      ToDoubleBiFunction<T, U> edges, double threshold, final ArrayList<int[]> pairV,
      final ArrayList<double[]> pairD, TIntArrayList mapA, boolean[] matchedB) {
    final int sizeA = verticesA.size();
    final int sizeB = verticesB.size();
    // Working space
    final int[] tmpV = new int[sizeB];
    final double[] tmpD = new double[sizeB];

    for (int u = 0; u < sizeA; u++) {
      final T itemA = verticesA.get(u);
      int count = 0;
      for (int v = 0; v < sizeB; v++) {
        final double distance = edges.applyAsDouble(itemA, verticesB.get(v));
        if (distance <= threshold) {
          matchedB[v] = true;
          tmpV[count] = v;
          tmpD[count] = distance;
          count++;
        }
      }
      if (count != 0) {
        mapA.add(u);
        pairV.add(Arrays.copyOf(tmpV, count));
        pairD.add(Arrays.copyOf(tmpD, count));
      }
    }
  }


  /**
   * Create the packed cost matrix data from only those matches within the distance.
   *
   * @param pairV the matching vertices B for each mapped vertex A
   * @param pairD the distance to vertices B for each mapped vertex A
   * @param sizeB the original size of B
   * @param mapB the map of those B with a valid cost
   * @return the cost matrix
   */
  private static int[] createCostMatrix(ArrayList<int[]> pairV, ArrayList<double[]> pairD,
      int sizeB, TIntArrayList mapB) {
    // Create mapping from original B index to reduced index
    int[] originalToReduced = new int[sizeB];
    for (int i = 0; i < mapB.size(); i++) {
      originalToReduced[mapB.getQuick(i)] = i;
    }

    // Create a cost matrix.
    // The matrix is re-mapped to integers to avoid float-point cumulative errors in
    // the Munkres algorithm which uses additions and subtractions.
    final Iterator<double[]> it = pairD.iterator();
    double[] limits = MathUtils.limits(it.next());
    it.forEachRemaining(distances -> MathUtils.limits(limits, distances));
    final double min = limits[0];
    // Note: It does not matter if the range is 0.
    // The Math.round() function will return 0 for a NaN input when converting to integer.
    final double range = limits[1] - min;

    // The cost of matches above the distance threshold is set to twice the maximum cost + 1.
    // This ensures the algorithm will favour two matches at max cost instead of 1 perfect match
    // and 1 disallowed match (since (2 * max) < (0 + 2 * max + 1)).
    final int[] cost = new int[pairV.size() * mapB.size()];
    Arrays.fill(cost, MAX_COST * 2 + 1);

    // Write in the known costs to the matrix
    for (int i = 0; i < pairV.size(); i++) {
      // Data is packed as i * cols + j.
      // Compute the start offset for this row.
      int rowOffset = i * mapB.size();
      final int[] tmpV = pairV.get(i);
      final double[] tmpD = pairD.get(i);
      for (int j = 0; j < tmpV.length; j++) {
        // Convert to integer
        cost[rowOffset + originalToReduced[tmpV[j]]] =
            (int) Math.round(MAX_COST * ((tmpD[j] - min) / range));
      }
    }

    return cost;
  }
}
