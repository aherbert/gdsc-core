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
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.ToDoubleBiFunction;

/**
 * Calculates the matching of two sets.
 */
public final class Matchings {

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
   * Calculates the a matching of a bipartite graph between two sets of vertices using
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
   * @return the maximum cardinality
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

    // Add to lists
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
}
