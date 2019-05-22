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

import uk.ac.sussex.gdsc.core.utils.MathUtils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.FastMath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Calculates the match between a set of predicted points and the actual points.
 */
public final class MatchCalculator {

  /** The maximum size to use the single pass algorithm. */
  private static final int SINGLE_PASS_MAX_SIZE = 100000;

  /** No public construction. */
  private MatchCalculator() {}

  /**
   * Calculate the match results for the given actual and predicted points. Points that are within
   * the distance threshold are identified as a match. The number of true positives, false positives
   * and false negatives are calculated.
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
   * @param actualPoints the actual points
   * @param predictedPoints the predicted points
   * @param distanceThreshold The distance threshold
   * @param truePositives True Positives
   * @param falsePositives False Positives
   * @param falseNegatives False Negatives
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
   * @param actualPoints the actual points
   * @param predictedPoints the predicted points
   * @param distanceThreshold The distance threshold
   * @param truePositives True Positives
   * @param falsePositives False Positives
   * @param falseNegatives False Negatives
   * @param matches The matched true positives (point1 = actual, point2 = predicted)
   * @return The match results
   */
  @SuppressWarnings("null")
  public static MatchResult analyseResults2D(Coordinate[] actualPoints,
      Coordinate[] predictedPoints, double distanceThreshold, List<Coordinate> truePositives,
      List<Coordinate> falsePositives, List<Coordinate> falseNegatives, List<PointPair> matches) {
    final int predictedPointsLength = ArrayUtils.getLength(predictedPoints);
    final int actualPointsLength = ArrayUtils.getLength(actualPoints);

    // If the number of possible pairs is small then use a one pass algorithm
    if (predictedPointsLength * actualPointsLength < SINGLE_PASS_MAX_SIZE) {
      return analyseResults2DSinglePass(actualPoints, predictedPoints, distanceThreshold,
          truePositives, falsePositives, falseNegatives, matches);
    }

    int tp = 0; // true positives (actual with matched predicted point)
    int fp = predictedPointsLength; // false positives (actual with no matched predicted point)
    int fn = actualPointsLength; // false negatives (predicted point with no actual point)
    double rmsd = 0;

    clear(truePositives, falsePositives, falseNegatives, matches);
    if (predictedPointsLength == 0 || actualPointsLength == 0) {
      if (falsePositives != null) {
        falsePositives.addAll(asList(predictedPoints));
      }
      if (falseNegatives != null) {
        falseNegatives.addAll(asList(actualPoints));
      }
      return new MatchResult(tp, fp, fn, rmsd);
    }

    // loop over the two arrays assigning the closest unassigned pair
    final boolean[] resultAssignment = new boolean[predictedPointsLength];
    final boolean[] roiAssignment = new boolean[fn];
    final ArrayList<ImmutableAssignment> assignments = new ArrayList<>(predictedPointsLength);

    final boolean[] matchedPredicted =
        (falsePositives == null) ? null : new boolean[predictedPointsLength];
    final boolean[] matchedActual =
        (falseNegatives == null) ? null : new boolean[actualPointsLength];

    final double distanceThresholdSquared = distanceThreshold * distanceThreshold;

    do {
      assignments.clear();

      // Process each result
      for (int predictedId = predictedPointsLength; predictedId-- > 0;) {
        if (resultAssignment[predictedId]) {
          continue; // Already assigned
        }

        final float x = predictedPoints[predictedId].getX();
        final float y = predictedPoints[predictedId].getY();

        // Find closest ROI point
        double d2Min = distanceThresholdSquared;
        int targetId = -1;
        for (int actualId = actualPointsLength; actualId-- > 0;) {
          if (roiAssignment[actualId]) {
            continue; // Already assigned
          }

          final Coordinate actualPoint = actualPoints[actualId];

          // Calculate in steps for increased speed (allows early exit)
          final double d2 = actualPoint.distanceSquared(x, y);
          if (d2 <= d2Min) {
            d2Min = d2;
            targetId = actualId;
          }
        }

        // Store closest ROI point
        if (targetId > -1) {
          assignments.add(new ImmutableAssignment(targetId, predictedId, d2Min));
        }
      }

      // If there are assignments
      if (!assignments.isEmpty()) {
        // Pick the closest pair to be assigned
        AssignmentComparator.sort(assignments);

        // Process in order
        for (final ImmutableAssignment closest : assignments) {
          // Skip those that have already been assigned since this will be a lower score.
          // Note at least one assignment should be processed as potential assignments are made
          // using only unassigned points.
          if (resultAssignment[closest.getPredictedId()] || roiAssignment[closest.getTargetId()]) {
            continue;
          }
          resultAssignment[closest.getPredictedId()] = true;
          roiAssignment[closest.getTargetId()] = true;

          // If within accuracy then classify as a match
          if (closest.getDistance() <= distanceThresholdSquared) {
            tp++;
            fn--;
            fp--;
            rmsd += closest.getDistance(); // Already a squared distance

            if (truePositives != null) {
              truePositives.add(predictedPoints[closest.getPredictedId()]);
            }
            if (matchedPredicted != null) {
              matchedPredicted[closest.getPredictedId()] = true;
            }
            if (matchedActual != null) {
              matchedActual[closest.getTargetId()] = true;
            }
            if (matches != null) {
              matches.add(new PointPair(actualPoints[closest.getTargetId()],
                  predictedPoints[closest.getPredictedId()]));
            }
          } else {
            // No more assignments within the distance threshold
            break;
          }
        }
      }
    } while (!assignments.isEmpty());

    // Add to lists
    if (falsePositives != null) {
      for (int i = 0; i < predictedPointsLength; i++) {
        if (!matchedPredicted[i]) {
          falsePositives.add(predictedPoints[i]);
        }
      }
    }
    if (falseNegatives != null) {
      for (int i = 0; i < actualPointsLength; i++) {
        if (!matchedActual[i]) {
          falseNegatives.add(actualPoints[i]);
        }
      }
    }

    if (tp > 0) {
      rmsd = Math.sqrt(rmsd / tp);
    }
    return new MatchResult(tp, fp, fn, rmsd);
  }

  /**
   * Calculate the match results for the given actual and predicted fluorophore pulses. Points that
   * are within the distance threshold are identified as a match. The score is calculated using half
   * the distance threshold and the overlap in time. Assignments are made using the highest scoring
   * matches.
   *
   * <p>The total score is stored in the RMSD field of the MatchResult. The number of true
   * positives, false positives and false negatives are calculated.
   *
   * @param actualPoints the actual points
   * @param predictedPoints the predicted points
   * @param distanceThreshold The distance threshold
   * @param truePositives True Positives
   * @param falsePositives False Positives
   * @param falseNegatives False Negatives
   * @param matches The matched true positives (point1 = actual, point2 = predicted)
   * @return The match results
   */
  @SuppressWarnings("null")
  public static MatchResult analyseResults2D(Pulse[] actualPoints, Pulse[] predictedPoints,
      double distanceThreshold, List<Pulse> truePositives, List<Pulse> falsePositives,
      List<Pulse> falseNegatives, List<PointPair> matches) {
    final int predictedPointsLength = ArrayUtils.getLength(predictedPoints);
    final int actualPointsLength = ArrayUtils.getLength(actualPoints);

    int tp = 0; // true positives (actual with matched predicted point)
    int fp = predictedPointsLength; // false positives (actual with no matched predicted point)
    int fn = actualPointsLength; // false negatives (predicted point with no actual point)
    double score = 0;

    clear(truePositives, falsePositives, falseNegatives, matches);
    if (predictedPointsLength == 0 || actualPointsLength == 0) {
      if (falsePositives != null) {
        falsePositives.addAll(asList(predictedPoints));
      }
      if (falseNegatives != null) {
        falseNegatives.addAll(asList(actualPoints));
      }
      return new MatchResult(tp, fp, fn, score);
    }

    // loop over the two arrays assigning the closest unassigned pair
    final boolean[] resultAssignment = new boolean[predictedPointsLength];
    final boolean[] roiAssignment = new boolean[fn];
    final ArrayList<ImmutableAssignment> assignments = new ArrayList<>(predictedPointsLength);

    final boolean[] matchedPredicted =
        (falsePositives == null) ? null : new boolean[predictedPointsLength];
    final boolean[] matchedActual =
        (falseNegatives == null) ? null : new boolean[actualPointsLength];

    // Sort by time to allow efficient looping
    Arrays.sort(actualPoints, PulseTimeComparator.getInstance());
    Arrays.sort(predictedPoints, PulseTimeComparator.getInstance());

    // Pre-calculate all-vs-all distance matrix if it can fit in memory
    final int size = predictedPointsLength * actualPointsLength;
    float[][] distanceMatrix = null;
    if (size < 200 * 200) {
      distanceMatrix = new float[predictedPointsLength][actualPointsLength];
      for (int predictedId = 0; predictedId < predictedPointsLength; predictedId++) {
        final float x = predictedPoints[predictedId].getX();
        final float y = predictedPoints[predictedId].getY();
        for (int actualId = 0; actualId < actualPointsLength; actualId++) {
          distanceMatrix[predictedId][actualId] =
              (float) actualPoints[actualId].distanceSquared(x, y);
        }
      }
    }

    // We will use the squared distance for speed
    final double halfDistanceThresholdSquared = MathUtils.pow2(distanceThreshold * 0.5);
    final float floatDistanceThreshold = (float) distanceThreshold;
    final double distanceThresholdSquared = distanceThreshold * distanceThreshold;

    do {
      assignments.clear();

      // Process each result
      for (int predictedId = 0; predictedId < predictedPointsLength; predictedId++) {
        if (resultAssignment[predictedId]) {
          continue; // Already assigned
        }

        final float x = predictedPoints[predictedId].getX();
        final float y = predictedPoints[predictedId].getY();
        final int start = predictedPoints[predictedId].getStart();
        final int end = predictedPoints[predictedId].getEnd();

        // Find first overlapping pulse
        int actualId = 0;
        while (actualId < actualPointsLength && actualPoints[actualId].getEnd() < start) {
          actualId++;
        }

        // Find highest scoring point within the distance limit
        double scoreMax = 0;
        int targetId = -1;
        for (; actualId < actualPointsLength; actualId++) {
          if (roiAssignment[actualId]) {
            continue; // Already assigned
          }
          if (actualPoints[actualId].getStart() > end) {
            break; // No more overlap in time
          }

          double d2;
          if (distanceMatrix == null) {
            final Coordinate actualPoint = actualPoints[actualId];

            // Calculate in steps for increased speed (allows early exit)
            final float dx = abs(actualPoint.getX() - x);
            if (dx > floatDistanceThreshold) {
              continue;
            }
            final float dy = abs(actualPoint.getY() - y);
            if (dy > floatDistanceThreshold) {
              continue;
            }
            d2 = dx * dx + dy * dy;
          } else {
            d2 = distanceMatrix[predictedId][actualId];
          }

          // Do we need to exclude using the distance threshold? This is useful for binary
          // classification
          // but will truncate the continuous nature of the score.
          if (d2 > distanceThresholdSquared) {
            continue;
          }

          final double s = predictedPoints[predictedId].score(actualPoints[actualId], d2,
              halfDistanceThresholdSquared);
          if (scoreMax < s) {
            scoreMax = s;
            targetId = actualId;
          }
        }

        // Store highest scoring point
        if (targetId > -1) {
          assignments.add(new ImmutableAssignment(targetId, predictedId, scoreMax));
        }
      }

      // If there are assignments
      if (!assignments.isEmpty()) {
        // Process highest scoring first
        AssignmentComparator.sort(assignments);
        Collections.reverse(assignments);

        // Process in order of score
        for (final ImmutableAssignment closest : assignments) {
          // Skip those that have already been assigned since this will be a lower score.
          // Note at least one assignment should be processed as potential assignments are made
          // using only unassigned points.
          if (resultAssignment[closest.getPredictedId()] || roiAssignment[closest.getTargetId()]) {
            continue;
          }

          resultAssignment[closest.getPredictedId()] = true;
          roiAssignment[closest.getTargetId()] = true;

          tp++;
          fn--;
          fp--;
          score += closest.getDistance(); // This is the scoreMax (not the distance)

          if (truePositives != null) {
            truePositives.add(predictedPoints[closest.getPredictedId()]);
          }
          if (matchedPredicted != null) {
            matchedPredicted[closest.getPredictedId()] = true;
          }
          if (matchedActual != null) {
            matchedActual[closest.getTargetId()] = true;
          }
          if (matches != null) {
            matches.add(new PointPair(actualPoints[closest.getTargetId()],
                predictedPoints[closest.getPredictedId()]));
          }
        }
      }
    } while (!assignments.isEmpty());

    // Add to lists
    if (falsePositives != null) {
      for (int i = 0; i < predictedPointsLength; i++) {
        if (!matchedPredicted[i]) {
          falsePositives.add(predictedPoints[i]);
        }
      }
    }
    if (falseNegatives != null) {
      for (int i = 0; i < actualPointsLength; i++) {
        if (!matchedActual[i]) {
          falseNegatives.add(actualPoints[i]);
        }
      }
    }

    // Every time-point has the chance to contribute to the score.
    // Normalise score by the maximum of the number of actual/predicted time points.
    // This penalises too few or too many predictions
    final int p1 = countTimePoints(actualPoints);
    final int p2 = countTimePoints(predictedPoints);
    score /= FastMath.max(p1, p2);

    return new MatchResult(tp, fp, fn, score);
  }

  /**
   * Calculate the match results for the given actual and predicted points. Points that are within
   * the distance threshold are identified as a match. The number of true positives, false positives
   * and false negatives are calculated.
   *
   * <p>Use a single pass algorithm suitable if the total number of possible pairs is small
   * (&lt;100000)
   *
   * @param actualPoints the actual points
   * @param predictedPoints the predicted points
   * @param distanceThreshold The distance threshold
   * @param truePositives True Positives
   * @param falsePositives False Positives
   * @param falseNegatives False Negatives
   * @param matches The matched true positives (point1 = actual, point2 = predicted)
   * @return The match results
   */
  @SuppressWarnings("null")
  public static MatchResult analyseResults2DSinglePass(Coordinate[] actualPoints,
      Coordinate[] predictedPoints, double distanceThreshold, List<Coordinate> truePositives,
      List<Coordinate> falsePositives, List<Coordinate> falseNegatives, List<PointPair> matches) {
    final int predictedPointsLength = ArrayUtils.getLength(predictedPoints);
    final int actualPointsLength = ArrayUtils.getLength(actualPoints);

    int tp = 0; // true positives (actual with matched predicted point)
    int fp = predictedPointsLength; // false positives (actual with no matched predicted point)
    int fn = actualPointsLength; // false negatives (predicted point with no actual point)
    double rmsd = 0;

    clear(truePositives, falsePositives, falseNegatives, matches);
    if (predictedPointsLength == 0 || actualPointsLength == 0) {
      if (falsePositives != null) {
        falsePositives.addAll(asList(predictedPoints));
      }
      if (falseNegatives != null) {
        falseNegatives.addAll(asList(actualPoints));
      }
      return new MatchResult(tp, fp, fn, rmsd);
    }

    // loop over the two arrays assigning the closest unassigned pair
    final ArrayList<ImmutableAssignment> assignments = new ArrayList<>(predictedPointsLength);
    final double distanceThresholdSquared = distanceThreshold * distanceThreshold;

    for (int predictedId = predictedPointsLength; predictedId-- > 0;) {
      final float x = predictedPoints[predictedId].getX();
      final float y = predictedPoints[predictedId].getY();
      for (int actualId = actualPointsLength; actualId-- > 0;) {
        final double d2 = actualPoints[actualId].distanceSquared(x, y);
        if (d2 <= distanceThresholdSquared) {
          assignments.add(new ImmutableAssignment(actualId, predictedId, d2));
        }
      }
    }

    AssignmentComparator.sort(assignments);

    final boolean[] predictedAssignment = new boolean[predictedPointsLength];
    final boolean[] actualAssignment = new boolean[actualPointsLength];

    for (final ImmutableAssignment a : assignments) {
      if (!actualAssignment[a.getTargetId()] && !predictedAssignment[a.getPredictedId()]) {
        actualAssignment[a.getTargetId()] = true;
        predictedAssignment[a.getPredictedId()] = true;
        tp++;
        fn--;
        fp--;
        rmsd += a.getDistance(); // Already a squared distance
        if (matches != null) {
          matches.add(
              new PointPair(actualPoints[a.getTargetId()], predictedPoints[a.getPredictedId()]));
        }
        if (truePositives != null) {
          truePositives.add(predictedPoints[a.getPredictedId()]);
        }
      }
    }

    // Add to lists
    if (falsePositives != null) {
      for (int i = 0; i < predictedPointsLength; i++) {
        if (!predictedAssignment[i]) {
          falsePositives.add(predictedPoints[i]);
        }
      }
    }
    if (falseNegatives != null) {
      for (int i = 0; i < actualPointsLength; i++) {
        if (!actualAssignment[i]) {
          falseNegatives.add(actualPoints[i]);
        }
      }
    }

    if (tp > 0) {
      rmsd = Math.sqrt(rmsd / tp);
    }
    return new MatchResult(tp, fp, fn, rmsd);
  }

  /**
   * Calculate the match results for the given actual and predicted points. Points that are within
   * the distance threshold are identified as a match. The number of true positives, false positives
   * and false negatives are calculated.
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
   * @param actualPoints the actual points
   * @param predictedPoints the predicted points
   * @param distanceThreshold The distance threshold
   * @param truePositives True Positives
   * @param falsePositives False Positives
   * @param falseNegatives False Negatives
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
   * @param actualPoints the actual points
   * @param predictedPoints the predicted points
   * @param distanceThreshold The distance threshold
   * @param truePositives True Positives
   * @param falsePositives False Positives
   * @param falseNegatives False Negatives
   * @param matches The matched true positives (point1 = actual, point2 = predicted)
   * @return The match results
   */
  @SuppressWarnings("null")
  public static MatchResult analyseResults3D(Coordinate[] actualPoints,
      Coordinate[] predictedPoints, double distanceThreshold, List<Coordinate> truePositives,
      List<Coordinate> falsePositives, List<Coordinate> falseNegatives, List<PointPair> matches) {
    final int predictedPointsLength = ArrayUtils.getLength(predictedPoints);
    final int actualPointsLength = ArrayUtils.getLength(actualPoints);

    // If the number of possible pairs is small then use a one pass algorithm
    if (predictedPointsLength * actualPointsLength < SINGLE_PASS_MAX_SIZE) {
      return analyseResults3DSinglePass(actualPoints, predictedPoints, distanceThreshold,
          truePositives, falsePositives, falseNegatives, matches);
    }

    int tp = 0; // true positives (actual with matched predicted point)
    int fp = predictedPointsLength; // false positives (actual with no matched predicted point)
    int fn = actualPointsLength; // false negatives (predicted point with no actual point)
    double rmsd = 0;

    clear(truePositives, falsePositives, falseNegatives, matches);
    if (predictedPointsLength == 0 || actualPointsLength == 0) {
      if (falsePositives != null) {
        falsePositives.addAll(asList(predictedPoints));
      }
      if (falseNegatives != null) {
        falseNegatives.addAll(asList(actualPoints));
      }
      return new MatchResult(tp, fp, fn, rmsd);
    }

    // loop over the two arrays assigning the closest unassigned pair
    final boolean[] resultAssignment = new boolean[predictedPointsLength];
    final boolean[] roiAssignment = new boolean[fn];
    final ArrayList<ImmutableAssignment> assignments = new ArrayList<>(predictedPointsLength);

    final boolean[] matchedPredicted =
        (falsePositives == null) ? null : new boolean[predictedPointsLength];
    final boolean[] matchedActual =
        (falseNegatives == null) ? null : new boolean[actualPointsLength];

    final double distanceThresholdSquared = distanceThreshold * distanceThreshold;

    do {
      assignments.clear();

      // Process each result
      for (int predictedId = predictedPointsLength; predictedId-- > 0;) {
        if (resultAssignment[predictedId]) {
          continue; // Already assigned
        }

        final float x = predictedPoints[predictedId].getX();
        final float y = predictedPoints[predictedId].getY();
        final float z = predictedPoints[predictedId].getZ();

        // Find closest ROI point
        float d2Min = (float) distanceThresholdSquared; // Float.MAX_VALUE
        int targetId = -1;
        for (int actualId = actualPointsLength; actualId-- > 0;) {
          if (roiAssignment[actualId]) {
            continue; // Already assigned
          }

          final Coordinate actualPoint = actualPoints[actualId];

          // Calculate in steps for increased speed (allows early exit)
          float dx = actualPoint.getX() - x;
          dx *= dx;
          if (dx <= d2Min) {
            float dy = actualPoint.getY() - y;
            dy *= dy;
            if (dy <= d2Min) {
              float dz = actualPoint.getZ() - z;
              dz *= dz;
              if (dz <= d2Min) {
                final float d2 = dx + dy + dz;
                if (d2 <= d2Min) {
                  d2Min = d2;
                  targetId = actualId;
                }
              }
            }
          }
        }

        // Store closest ROI point
        if (targetId > -1) {
          assignments.add(new ImmutableAssignment(targetId, predictedId, d2Min));
        }
      }

      // If there are assignments
      if (!assignments.isEmpty()) {
        // Pick the closest pair to be assigned
        AssignmentComparator.sort(assignments);

        // Process in order
        for (final ImmutableAssignment closest : assignments) {
          // Skip those that have already been assigned since this will be a lower score.
          // Note at least one assignment should be processed as potential assignments are made
          // using only unassigned points.
          if (resultAssignment[closest.getPredictedId()] || roiAssignment[closest.getTargetId()]) {
            continue;
          }

          resultAssignment[closest.getPredictedId()] = true;
          roiAssignment[closest.getTargetId()] = true;

          // If within accuracy then classify as a match
          if (closest.getDistance() <= distanceThresholdSquared) {
            tp++;
            fn--;
            fp--;
            rmsd += closest.getDistance();

            if (truePositives != null) {
              truePositives.add(predictedPoints[closest.getPredictedId()]);
            }
            if (matchedPredicted != null) {
              matchedPredicted[closest.getPredictedId()] = true;
            }
            if (matchedActual != null) {
              matchedActual[closest.getTargetId()] = true;
            }
            if (matches != null) {
              matches.add(new PointPair(actualPoints[closest.getTargetId()],
                  predictedPoints[closest.getPredictedId()]));
            }
          } else {
            // No more assignments within the distance threshold
            break;
          }
        }
      }
    } while (!assignments.isEmpty());

    // Add to lists
    if (falsePositives != null) {
      for (int i = 0; i < predictedPointsLength; i++) {
        if (!matchedPredicted[i]) {
          falsePositives.add(predictedPoints[i]);
        }
      }
    }
    if (falseNegatives != null) {
      for (int i = 0; i < actualPointsLength; i++) {
        if (!matchedActual[i]) {
          falseNegatives.add(actualPoints[i]);
        }
      }
    }

    if (tp > 0) {
      rmsd = Math.sqrt(rmsd / tp);
    }
    return new MatchResult(tp, fp, fn, rmsd);
  }

  /**
   * Calculate the match results for the given actual and predicted points. Points that are within
   * the distance threshold are identified as a match. The number of true positives, false positives
   * and false negatives are calculated.
   *
   * <p>Use a single pass algorithm suitable if the total number of possible pairs is small
   * (&lt;100000)
   *
   * @param actualPoints the actual points
   * @param predictedPoints the predicted points
   * @param distanceThreshold The distance threshold
   * @param truePositives True Positives
   * @param falsePositives False Positives
   * @param falseNegatives False Negatives
   * @param matches The matched true positives (point1 = actual, point2 = predicted)
   * @return The match results
   */
  @SuppressWarnings("null")
  public static MatchResult analyseResults3DSinglePass(Coordinate[] actualPoints,
      Coordinate[] predictedPoints, double distanceThreshold, List<Coordinate> truePositives,
      List<Coordinate> falsePositives, List<Coordinate> falseNegatives, List<PointPair> matches) {
    final int predictedPointsLength = ArrayUtils.getLength(predictedPoints);
    final int actualPointsLength = ArrayUtils.getLength(actualPoints);

    int tp = 0; // true positives (actual with matched predicted point)
    int fp = predictedPointsLength; // false positives (actual with no matched predicted point)
    int fn = actualPointsLength; // false negatives (predicted point with no actual point)
    double rmsd = 0;

    clear(truePositives, falsePositives, falseNegatives, matches);
    if (predictedPointsLength == 0 || actualPointsLength == 0) {
      if (falsePositives != null) {
        falsePositives.addAll(asList(predictedPoints));
      }
      if (falseNegatives != null) {
        falseNegatives.addAll(asList(actualPoints));
      }
      return new MatchResult(tp, fp, fn, rmsd);
    }

    // loop over the two arrays assigning the closest unassigned pair
    final ArrayList<ImmutableAssignment> assignments = new ArrayList<>(predictedPointsLength);
    final double distanceThresholdSquared = distanceThreshold * distanceThreshold;

    for (int predictedId = predictedPointsLength; predictedId-- > 0;) {
      final float x = predictedPoints[predictedId].getX();
      final float y = predictedPoints[predictedId].getY();
      final float z = predictedPoints[predictedId].getZ();
      for (int actualId = actualPointsLength; actualId-- > 0;) {
        final double d2 = actualPoints[actualId].distanceSquared(x, y, z);
        if (d2 <= distanceThresholdSquared) {
          assignments.add(new ImmutableAssignment(actualId, predictedId, d2));
        }
      }
    }

    AssignmentComparator.sort(assignments);

    final boolean[] predictedAssignment = new boolean[predictedPointsLength];
    final boolean[] actualAssignment = new boolean[actualPointsLength];

    for (final ImmutableAssignment a : assignments) {
      if (!actualAssignment[a.getTargetId()] && !predictedAssignment[a.getPredictedId()]) {
        actualAssignment[a.getTargetId()] = true;
        predictedAssignment[a.getPredictedId()] = true;
        tp++;
        fn--;
        fp--;
        rmsd += a.getDistance(); // Already a squared distance
        if (matches != null) {
          matches.add(
              new PointPair(actualPoints[a.getTargetId()], predictedPoints[a.getPredictedId()]));
        }
        if (truePositives != null) {
          truePositives.add(predictedPoints[a.getPredictedId()]);
        }
      }
    }

    // Add to lists
    if (falsePositives != null) {
      for (int i = 0; i < predictedPointsLength; i++) {
        if (!predictedAssignment[i]) {
          falsePositives.add(predictedPoints[i]);
        }
      }
    }
    if (falseNegatives != null) {
      for (int i = 0; i < actualPointsLength; i++) {
        if (!actualAssignment[i]) {
          falseNegatives.add(actualPoints[i]);
        }
      }
    }

    if (tp > 0) {
      rmsd = Math.sqrt(rmsd / tp);
    }
    return new MatchResult(tp, fp, fn, rmsd);
  }

  private static Collection<Coordinate> asList(Coordinate[] points) {
    if (points != null) {
      return Arrays.asList(points);
    }
    return new ArrayList<>(0);
  }

  private static Collection<Pulse> asList(Pulse[] points) {
    if (points != null) {
      return Arrays.asList(points);
    }
    return new ArrayList<>(0);
  }

  private static float abs(final float value) {
    return (value < 0) ? -value : value;
  }

  private static int countTimePoints(Pulse[] actualPoints) {
    int p1 = 0;
    for (final Pulse p : actualPoints) {
      p1 += p.getEnd() - p.getStart() + 1;
    }
    return p1;
  }

  private static <T, U, V> void clear(List<T> truePositives, List<T> falsePositives,
      List<U> falseNegatives, List<V> matches) {
    clear(truePositives);
    clear(falsePositives);
    clear(falseNegatives);
    clear(matches);
  }

  private static <T> void clear(List<T> list) {
    if (list != null) {
      list.clear();
    }
  }
}
