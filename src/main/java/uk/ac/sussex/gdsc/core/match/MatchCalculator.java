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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import uk.ac.sussex.gdsc.core.data.VisibleForTesting;
import uk.ac.sussex.gdsc.core.utils.MathUtils;

/**
 * Calculates the match between a set of predicted points and the actual points.
 */
public final class MatchCalculator {
  /**
   * Compute the sum of the edges distance between matched vertices.
   *
   * @param <T> the generic type to be matched
   */
  private static class MatchedConsumer<T> implements BiConsumer<T, T> {
    /** The edge score function. */
    private final ToDoubleBiFunction<T, T> edges;
    /** The count. */
    private int count;
    /** The sum. */
    private double sum;

    /**
     * Create a new instance.
     *
     * @param edges the edges
     */
    MatchedConsumer(ToDoubleBiFunction<T, T> edges) {
      this.edges = edges;
    }

    @Override
    public void accept(T first, T second) {
      count++;
      sum += edges.applyAsDouble(first, second);
    }

    /**
     * Gets the count.
     *
     * @return the count
     */
    int getCount() {
      return count;
    }

    /**
     * Gets the sum.
     *
     * @return the sum
     */
    double getSum() {
      return sum;
    }
  }

  /** No public construction. */
  private MatchCalculator() {}

  /**
   * Calculate the match results for the given actual and predicted fluorophore pulses. Points that
   * are within the distance threshold are identified as a match. The score is calculated using half
   * the distance threshold and the overlap in time. Assignments are made using the highest scoring
   * matches.
   *
   * <p>The number of true positives, false positives and false negatives are calculated using pulse
   * counts and not time point counts (i.e. not factoring pulse duration into the totals).
   *
   * <p>The total score is stored in the RMSD field of the MatchResult. This score is divided by the
   * highest number of time points observed across all the pulses from either the predicted or
   * actual pulses. The score thus incorporates the pulse durations.
   *
   * <p>Note: The algorithm can only match pulses 1-to-1. It does not support matching a long pulse
   * to two short pulses if the short pulses do not overlap, for example if the short pulses are
   * actually a long pulse with an additional break in the sequence.
   *
   * @param actualPoints the actual points
   * @param predictedPoints the predicted points
   * @param distanceThreshold The distance threshold
   * @param truePositives True Positives (matched predicted)
   * @param falsePositives False Positives (unmatched predicted)
   * @param falseNegatives False Negatives (unmatched actual)
   * @param matches The matched true positives (point1 = actual, point2 = predicted)
   * @return The match results
   * @see Pulse#score(Pulse, double, double)
   */
  public static MatchResult analyseResults2D(Pulse[] actualPoints, Pulse[] predictedPoints,
      double distanceThreshold, List<Pulse> truePositives, List<Pulse> falsePositives,
      List<Pulse> falseNegatives, List<PointPair> matches) {
    final ToDoubleBiFunction<Pulse, Pulse> edges = createEdgeFunction(distanceThreshold);
    // Use zero for the distance threshold as the edge function returns 0 and below for valid edges.
    return analyseResults(actualPoints, predictedPoints, 0.0, truePositives, falsePositives,
        falseNegatives, matches, edges, matchedConsumer -> {
          // Every time-point has the chance to contribute to the score.
          // Normalise score by the maximum of the number of actual/predicted time points.
          // This penalises too few or too many predictions.
          // Note inverse the sum of the score so high is better.
          final int p1 = countTimePoints(actualPoints);
          final int p2 = countTimePoints(predictedPoints);
          return MathUtils.div0(-matchedConsumer.getSum(), Math.max(p1, p2));
        });
  }

  /**
   * Calculate the match results for the given actual and predicted points. Points that are within
   * the distance threshold are identified as a match. The number of true positives, false positives
   * and false negatives are calculated.
   *
   * <p>Uses a 2D Euclidean distance with the XY coordinates.
   *
   * @param actualPoints the actual points
   * @param predictedPoints the predicted points
   * @param distanceThreshold The distance threshold
   * @return The match results
   */
  public static MatchResult analyseResults2D(Coordinate[] actualPoints,
      Coordinate[] predictedPoints, double distanceThreshold) {
    return analyseResults2D(actualPoints, predictedPoints, distanceThreshold, null, null, null,
        null);
  }

  /**
   * Calculate the match results for the given actual and predicted points. Points that are within
   * the distance threshold are identified as a match. The number of true positives, false positives
   * and false negatives are calculated.
   *
   * <p>Uses a 2D Euclidean distance with the XY coordinates.
   *
   * @param actualPoints the actual points
   * @param predictedPoints the predicted points
   * @param distanceThreshold The distance threshold
   * @param truePositives True Positives (matched predicted)
   * @param falsePositives False Positives (unmatched predicted)
   * @param falseNegatives False Negatives (unmatched actual)
   * @return The match results
   */
  public static MatchResult analyseResults2D(Coordinate[] actualPoints,
      Coordinate[] predictedPoints, double distanceThreshold, List<Coordinate> truePositives,
      List<Coordinate> falsePositives, List<Coordinate> falseNegatives) {
    return analyseResults2D(actualPoints, predictedPoints, distanceThreshold, truePositives,
        falsePositives, falseNegatives, null);
  }

  /**
   * Calculate the match results for the given actual and predicted points. Points that are within
   * the distance threshold are identified as a match. The number of true positives, false positives
   * and false negatives are calculated.
   *
   * <p>Uses a 2D Euclidean distance with the XY coordinates.
   *
   * @param actualPoints the actual points
   * @param predictedPoints the predicted points
   * @param distanceThreshold The distance threshold
   * @param truePositives True Positives (matched predicted)
   * @param falsePositives False Positives (unmatched predicted)
   * @param falseNegatives False Negatives (unmatched actual)
   * @param matches The matched true positives (point1 = actual, point2 = predicted)
   * @return The match results
   */
  public static MatchResult analyseResults2D(Coordinate[] actualPoints,
      Coordinate[] predictedPoints, double distanceThreshold, List<Coordinate> truePositives,
      List<Coordinate> falsePositives, List<Coordinate> falseNegatives, List<PointPair> matches) {
    return analyseResultsCoordinates(actualPoints, predictedPoints,
        distanceThreshold * distanceThreshold, truePositives, falsePositives, falseNegatives,
        matches, Coordinate::distanceXySquared);
  }

  /**
   * Calculate the match results for the given actual and predicted points. Points that are within
   * the distance threshold are identified as a match. The number of true positives, false positives
   * and false negatives are calculated.
   *
   * <p>Uses a 3D Euclidean distance with the XYZ coordinates.
   *
   * @param actualPoints the actual points
   * @param predictedPoints the predicted points
   * @param distanceThreshold The distance threshold
   * @return The match results
   */
  public static MatchResult analyseResults3D(Coordinate[] actualPoints,
      Coordinate[] predictedPoints, double distanceThreshold) {
    return analyseResults3D(actualPoints, predictedPoints, distanceThreshold, null, null, null,
        null);
  }

  /**
   * Calculate the match results for the given actual and predicted points. Points that are within
   * the distance threshold are identified as a match. The number of true positives, false positives
   * and false negatives are calculated.
   *
   * <p>Uses a 3D Euclidean distance with the XYZ coordinates.
   *
   * @param actualPoints the actual points
   * @param predictedPoints the predicted points
   * @param distanceThreshold The distance threshold
   * @param truePositives True Positives (matched predicted)
   * @param falsePositives False Positives (unmatched predicted)
   * @param falseNegatives False Negatives (unmatched actual)
   * @return The match results
   */
  public static MatchResult analyseResults3D(Coordinate[] actualPoints,
      Coordinate[] predictedPoints, double distanceThreshold, List<Coordinate> truePositives,
      List<Coordinate> falsePositives, List<Coordinate> falseNegatives) {
    return analyseResults3D(actualPoints, predictedPoints, distanceThreshold, truePositives,
        falsePositives, falseNegatives, null);
  }

  /**
   * Calculate the match results for the given actual and predicted points. Points that are within
   * the distance threshold are identified as a match. The number of true positives, false positives
   * and false negatives are calculated.
   *
   * <p>Uses a 3D Euclidean distance with the XYZ coordinates.
   *
   * @param actualPoints the actual points
   * @param predictedPoints the predicted points
   * @param distanceThreshold The distance threshold
   * @param truePositives True Positives (matched predicted)
   * @param falsePositives False Positives (unmatched predicted)
   * @param falseNegatives False Negatives (unmatched actual)
   * @param matches The matched true positives (point1 = actual, point2 = predicted)
   * @return The match results
   */
  public static MatchResult analyseResults3D(Coordinate[] actualPoints,
      Coordinate[] predictedPoints, double distanceThreshold, List<Coordinate> truePositives,
      List<Coordinate> falsePositives, List<Coordinate> falseNegatives, List<PointPair> matches) {
    return analyseResultsCoordinates(actualPoints, predictedPoints,
        distanceThreshold * distanceThreshold, truePositives, falsePositives, falseNegatives,
        matches, Coordinate::distanceXyzSquared);
  }

  /**
   * Calculate the match results for the given actual and predicted points. Points that are within
   * the distance threshold are identified as a match. The number of true positives, false positives
   * and false negatives are calculated.
   *
   * <p>Note: For the MatchResult to contain a RMSD it is assumed the edges function will return a
   * squared Euclidean distance and the distance threshold is appropriate, i.e. is the squared
   * radius of the hyper sphere for the appropriate number of dimensions (1, 2, or 3).
   *
   * @param actualPoints the actual points
   * @param predictedPoints the predicted points
   * @param distanceThreshold The distance threshold
   * @param truePositives True Positives (matched predicted)
   * @param falsePositives False Positives (unmatched predicted)
   * @param falseNegatives False Negatives (unmatched actual)
   * @param matches The matched true positives (point1 = actual, point2 = predicted)
   * @param edges function used to identify distance between the vertices ({@code A -> B})
   * @return The match results
   */
  public static MatchResult analyseResultsCoordinates(Coordinate[] actualPoints,
      Coordinate[] predictedPoints, double distanceThreshold, List<Coordinate> truePositives,
      List<Coordinate> falsePositives, List<Coordinate> falseNegatives, List<PointPair> matches,
      ToDoubleBiFunction<Coordinate, Coordinate> edges) {
    return analyseResults(actualPoints, predictedPoints, distanceThreshold, truePositives,
        falsePositives, falseNegatives, matches, edges, matchedConsumer ->
        // Compute RMSD
        matchedConsumer.getCount() == 0 ? 0
            : Math.sqrt(matchedConsumer.getSum() / matchedConsumer.getCount()));
  }

  /**
   * Calculate the match results for the given actual and predicted points. Points that are within
   * the distance threshold are identified as a match. The number of true positives, false positives
   * and false negatives are calculated.
   *
   * @param <T> the generic type
   * @param actualPoints the actual points
   * @param predictedPoints the predicted points
   * @param distanceThreshold The distance threshold
   * @param truePositives True Positives (matched predicted)
   * @param falsePositives False Positives (unmatched predicted)
   * @param falseNegatives False Negatives (unmatched actual)
   * @param matches The matched true positives (point1 = actual, point2 = predicted)
   * @param edges function used to identify distance between the vertices ({@code A -> B})
   * @param resultFunction the function to create the result
   * @return The match results
   */
  private static <T extends Coordinate> MatchResult analyseResults(T[] actualPoints,
      T[] predictedPoints, double distanceThreshold, List<T> truePositives, List<T> falsePositives,
      List<T> falseNegatives, List<PointPair> matches, ToDoubleBiFunction<T, T> edges,
      ToDoubleFunction<MatchedConsumer<T>> resultFunction) {

    // Delegate to the Matchings class.
    // This currently uses nearestNeighbour but could be changed to use maximumCardinality
    // or minimumDistance. Note that downstream consumers of this method were created based
    // on it using the greedy nearest neighbour method.

    final List<T> verticesA = toList(predictedPoints);
    final List<T> verticesB = toList(actualPoints);
    final MatchedConsumer<T> matchedConsumer = new MatchedConsumer<>(edges);
    BiConsumer<T, T> matched = matchedConsumer;
    if (truePositives != null) {
      truePositives.clear();
      matched = matched.andThen((u, v) -> truePositives.add(u));
    }
    if (matches != null) {
      matches.clear();
      // Note that (u, v) are (predicted, actual) so they are switched for the PointPair
      matched = matched.andThen((u, v) -> matches.add(new PointPair(v, u)));
    }
    final Consumer<T> unmatchedA = toConsumer(falsePositives);
    final Consumer<T> unmatchedB = toConsumer(falseNegatives);

    final int tp = Matchings.nearestNeighbour(verticesA, verticesB, edges, distanceThreshold,
        matched, unmatchedA, unmatchedB);

    return new MatchResult(tp, verticesA.size() - tp, verticesB.size() - tp,
        resultFunction.applyAsDouble(matchedConsumer));
  }

  /**
   * Convert to a list (null-safe).
   *
   * @param <T> the generic type
   * @param array the array
   * @return the list
   */
  private static <T> List<T> toList(T[] array) {
    if (array != null) {
      return Arrays.asList(array);
    }
    return Collections.emptyList();
  }

  /**
   * Convert to a consumer (null-safe).
   *
   * @param <T> the generic type
   * @param list the list
   * @return the consumer (or null)
   */
  private static <T> Consumer<T> toConsumer(List<T> list) {
    if (list != null) {
      list.clear();
      return list::add;
    }
    return null;
  }

  /**
   * Creates the edge function to score the distance between pulses (lower score is better).
   *
   * <p>A value of {@code <= 0} represents a valid score, i.e. zero is valid.
   *
   * @param distanceThreshold the distance threshold
   * @return the edge function
   */
  @VisibleForTesting
  static ToDoubleBiFunction<Pulse, Pulse> createEdgeFunction(double distanceThreshold) {
    // Custom distance function
    final double halfDistanceThresholdSquared = MathUtils.pow2(distanceThreshold * 0.5);
    final double distanceThresholdSquared = distanceThreshold * distanceThreshold;
    return (u, v) -> {
      // Determine time overlap
      if (u.calculateOverlap(v) == 0) {
        return 1.0;
      }

      // Determine distance
      final double distanceSquared = u.distanceXySquared(v);
      if (distanceSquared > distanceThresholdSquared) {
        return 1.0;
      }

      // Compute weighted score but negate so lower is better.
      // Note: This will compute the overlap again.
      return -u.score(v, distanceSquared, halfDistanceThresholdSquared);
    };
  }

  /**
   * Count the total time points across all pulses.
   *
   * @param points the points
   * @return the total time points
   */
  private static int countTimePoints(Pulse[] points) {
    int p1 = 0;
    for (final Pulse p : points) {
      p1 += p.getEnd() - p.getStart() + 1;
    }
    return p1;
  }
}
