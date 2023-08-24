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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.Pair;
import uk.ac.sussex.gdsc.core.data.VisibleForTesting;
import uk.ac.sussex.gdsc.core.match.HopcroftKarpMatching.IntBiConsumer;
import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

/**
 * Calculates the matching of two sets.
 */
public final class Matchings {
  /** The constant for no assignment. */
  private static final int NO_ASSIGNMENT = -1;
  /** The constant for the maximum cost distance. */
  private static final int MAX_COST = 1 << 16;

  /**
   * Define a consumer of matches between two sets.
   */
  @VisibleForTesting
  interface MatchConsumer extends IntBiConsumer {
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
      return matched == null ? null
          : new IntersectionMatchConsumer<>(verticesA, verticesB, matched);
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
      return unmatched == null ? null : new UnmatchedMatchConsumer<>(vertices, first, unmatched);
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

  // CHECKSTYLE.ON: ParameterName

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
    final List<ImmutableAssignment> neighbours =
        findNeighbours(verticesA, verticesB, edges, threshold, sizeA, sizeB);

    if (neighbours.isEmpty()) {
      consume(verticesA, unmatchedA);
      consume(verticesB, unmatchedB);
      return 0;
    }
    if (neighbours.size() == 1) {
      int a = neighbours.get(0).getTargetId();
      int b = neighbours.get(0).getPredictedId();
      if (matched != null) {
        matched.accept(verticesA.get(a), verticesB.get(b));
      }
      consumeUnmatched(verticesA, unmatchedA, a);
      consumeUnmatched(verticesB, unmatchedB, b);
      return 1;
    }

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
  private static <T, U> List<ImmutableAssignment> findNeighbours(List<T> verticesA,
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
   * Pass items from the list that are not the matched item to the consumer (null-safe).
   *
   * @param <T> the generic type
   * @param list the list
   * @param consumer the consumer (can be null)
   * @param matched the matched item
   */
  private static <T> void consumeUnmatched(List<T> list, Consumer<T> consumer, int matched) {
    if (consumer != null) {
      final int length = list.size();
      for (int i = 0; i < length; i++) {
        if (i != matched) {
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
   * <p>The cardinality of the match may not be maximal as an alternative matching with higher
   * cardinality may have a greater or equal sum of distances.
   *
   * <p>The output matched and those unmatched from each set can be obtained using the consumer
   * functions. Use the matched consumer to obtain the sum of distances between matched vertices.
   *
   * <p>This uses an assignment algorithm based on an all-vs-all distance matrix. Distances below
   * the threshold are mapped linearly to 16-bit unsigned integers. Any vertex not within the
   * distance threshold to a vertex in the opposite set is excluded.
   *
   * <p>The distances should be constructed to avoid the range between maximum and minimum distance
   * being infinite. This will break the linear mapping and an exception is raised.
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
   * @throws IllegalArgumentException If the distance threshold is not finite, or the range of the
   *         maximum to minimum distance is infinite.
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
    final IntArrayList mapA = new IntArrayList(sizeA);
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
    final IntArrayList mapB = new IntArrayList(sizeB);
    IntStream.range(0, sizeB).filter(v -> matchedB[v]).forEachOrdered(mapB::add);

    // There may be non-connected sub-graphs within the entire set of connections.
    // The cost matrix can be divided into separate sub-matrices for each graph.
    // This increases the resolution when mapping to the 16-bit integer range for
    // each sub-graph (if a sub-graph has a different [min:max] range to the global range).
    // It will also increase speed for processing large input data.

    final int[] aa = mapA.elements();
    final int[] bb = mapB.elements();
    final List<Pair<int[], int[]>> subGraphs =
        BiPartiteGraphs.extractSubGraphs(mapA.size(), mapB.size(),
            // The list of indices for a is the sorted original index value of all B closer
            // than the distance threshold. Binary search will work. It may be prohibitively
            // slow if each a has many connections. The alternative is to materialise the all-vs-all
            // connections as a matrix for fast look-up.
            (a, b) -> Arrays.binarySearch(pairV.get(a), bb[b]) >= 0);

    // The final assignments. These are for the reduced indices for vertices that are close.
    final int[] assignments = SimpleArrayUtils.newIntArray(mapA.size(), -1);

    // Create mapping from original B index to a reduced index
    final int[] originalToReduced = new int[sizeB];

    // Process each sub graph
    for (final Pair<int[], int[]> sg : subGraphs) {
      // The value in these arrays are reduced indices.
      // Convert to the original indices using map.get(i).
      final int[] setA = sg.getKey();
      final int[] setB = sg.getValue();
      // Note that we do not require setB to extract the sub-graph from the pairs as
      // they implicitly already contain only B indices that are within the distance threshold.
      // All we require is to extract the cost for each connection from the subset A to B.
      // The cost matrix is compacted again from the already reduced indices. We have to map
      // the original index B to a value in [0, setB.length).
      for (int i = 0; i < setB.length; i++) {
        originalToReduced[bb[setB[i]]] = i;
      }
      final int[] mappedAssignments =
          computeMappedAssignments(setA, pairV, pairD, setB.length, originalToReduced);
      for (int i = 0; i < mappedAssignments.length; i++) {
        if (mappedAssignments[i] != -1) {
          assignments[setA[i]] = setB[mappedAssignments[i]];
        }
      }
    }

    // Remove those above the match distance.
    int max = 0;
    final boolean[] matchedA = new boolean[sizeA];
    Arrays.fill(matchedB, false);
    for (int u = 0; u < assignments.length; u++) {
      final int v = assignments[u];
      if (v != NO_ASSIGNMENT) {
        // Check this was a valid connection.
        // This is a precaution against bad assignments that minimise
        // the sum of distances by including a pair above the maximum cost.
        final int index = Arrays.binarySearch(pairV.get(u), bb[v]);
        if (index >= 0) {
          max++;
          matchedA[aa[u]] = true;
          matchedB[bb[v]] = true;
          // Feed consumer
          if (matched != null) {
            final T itemA = verticesA.get(aa[u]);
            final U itemB = verticesB.get(bb[v]);
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
      ToDoubleBiFunction<T, U> edges, double threshold, final List<int[]> pairV,
      final List<double[]> pairD, IntArrayList mapA, boolean[] matchedB) {
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
   * Compute the mapped assignments.
   *
   * <p>Create the cost matrix data for the specified mapped vertices in A to the set of vertices B
   * of the specified size. A mapping from the original index to the reduced index in the range [0,
   * sizeB) is required.
   *
   * <p>Computes the assignments from the cost matrix for the reduced index.
   *
   * @param verticesA the mapped vertices A
   * @param pairV the matching vertices B for each mapped vertex A
   * @param pairD the distance to vertices B for each mapped vertex A
   * @param sizeB the size of the vertices B
   * @param originalToReduced the mapping from original B index to the reduced index
   * @return the assignments
   */
  private static int[] computeMappedAssignments(int[] verticesA, List<int[]> pairV,
      List<double[]> pairD, int sizeB, int[] originalToReduced) {
    // Create a cost matrix.
    // The matrix is re-mapped to integers to avoid float-point cumulative errors in
    // the Munkres algorithm which uses additions and subtractions.

    // Find the limits
    final double[] limits = MathUtils.limits(pairD.get(verticesA[0]));
    for (int i = 1; i < verticesA.length; i++) {
      MathUtils.limits(limits, pairD.get(verticesA[i]));
    }

    final double min = limits[0];
    // The Math.round() function will return 0 for a NaN input when converting to integer.
    final double range = limits[1] - min;
    // Note: If the range is 0 then the assignments are arbitrary
    if (range == 0) {
      final int[] result = new int[verticesA.length];
      // Assign the columns to the rows using the diagonal
      final int n = Math.min(result.length, sizeB);
      for (int i = 0; i < n; i++) {
        result[i] = i;
      }
      for (int i = n; i < result.length; i++) {
        result[i] = NO_ASSIGNMENT;
      }
      return result;
    }

    ValidationUtils.checkArgument(Double.isFinite(range),
        "Max (%f) - min (%f) distance is not finite", limits[1], min);

    // The cost of matches above the distance threshold is set to a prohibitive value.
    // This should be set so that the algorithm will never choose such an assignment as
    // the sum of all other possible combinations is less. This can be set using the max
    // cost and the number of possible assignments.
    // The number of assignments is the minimum of the matrix dimensions.
    // Prevent overflow when approaching the largest supported square matrix.
    final int n = Math.min(verticesA.length, sizeB);
    final int notAllowed = (int) Math.min(1L << 30, (long) MAX_COST * n + 1);
    final int[][] cost = new int[verticesA.length][sizeB];

    // Write in the known costs to the matrix
    for (int i = 0; i < verticesA.length; i++) {
      final int[] c = cost[i];
      Arrays.fill(c, notAllowed);
      final int a = verticesA[i];
      final int[] tmpV = pairV.get(a);
      final double[] tmpD = pairD.get(a);
      for (int j = 0; j < tmpV.length; j++) {
        // Convert to integer
        c[originalToReduced[tmpV[j]]] = (int) Math.round(MAX_COST * ((tmpD[j] - min) / range));
      }
    }

    // LAPJV is "always" faster than Kuhn-Munkres and handles non-square matrices
    return JonkerVolgenantAssignment.compute(cost);
  }
}
