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

// @formatter:off
/**
 * Class to store the result of a binary scoring analysis between two sets A and B. Requires the
 * size of the intersect between sets and the size of the unmatched regions to be known.
 *
 * <ul>
 * <li>True Positives (|A| intersect |B|)
 * <li>False Positives (|A| - TP)
 * <li>False Negatives (|B| - TP)
 * </ul>
 *
 * <p>This is a specialisation of a general intersection between sets as it stores a mean distance
 * score between matched items obtained during distance matching. The metrics precision, recall
 * and Jaccard are precomputed.
 */
//@formatter:on
public class MatchResult extends IntersectionResult {
  // TODO - There is a lot of duplicate code in
  // MatchResult, ClassificationResult and FractionClassificationResult
  // Determine what code is needed and remove redundant code.

  /** The precision. */
  private final double precision;
  /** The recall. */
  private final double recall;
  /** The jaccard. */
  private final double jaccard;
  /** The root mean squared distance between true positives. */
  private final double rmsd;

  /**
   * Instantiates a new match result.
   *
   * @param tp The number of true positives
   * @param fp The number of false positives
   * @param fn The number of false negatives
   * @param rmsd The root mean squared distance between true positives
   */
  public MatchResult(int tp, int fp, int fn, double rmsd) {
    super(tp, fp, fn);
    this.rmsd = rmsd;

    precision = MatchScores.calculatePrecision(tp, fp);
    recall = MatchScores.calculateRecall(tp, fn);
    jaccard = MatchScores.calculateJaccard(tp, fp, fn);
  }

  /**
   * Return the F-Score statistic, a weighted combination of the precision and recall.
   *
   * @param beta The weight
   * @return The F-Score
   */
  public double getFScore(double beta) {
    return MatchScores.calculateFBetaScore(getPrecision(), getRecall(), beta);
  }

  /**
   * Return the F1-Score statistic, a equal weighted combination of the precision and recall.
   *
   * @return The F1-Score
   */
  public double getF1Score() {
    return MatchScores.calculateF1Score(getPrecision(), getRecall());
  }

  /**
   * Gets the number of predicted points.
   *
   * @return the number of predicted points
   */
  public int getNumberPredicted() {
    return getSizeA();
  }

  /**
   * Gets the number of actual points.
   *
   * @return the number of actual points.
   */
  public int getNumberActual() {
    return getSizeB();
  }

  /**
   * Gets the true positives.
   *
   * @return the true positives (hit).
   */
  public int getTruePositives() {
    return getIntersection();
  }

  /**
   * Gets the false positives.
   *
   * @return the false positives (false alarm, Type 1 error).
   */
  public int getFalsePositives() {
    return getSizeAMinusIntersection();
  }

  /**
   * Gets the false negatives.
   *
   * @return the false negatives (miss, Type 2 error).
   */
  public int getFalseNegatives() {
    return getSizeBMinusIntersection();
  }

  /**
   * Gets the precision.
   *
   * @return the precision.
   */
  public double getPrecision() {
    return precision;
  }

  /**
   * Gets the recall.
   *
   * @return the recall.
   */
  public double getRecall() {
    return recall;
  }

  /**
   * Gets the Jaccard.
   *
   * @return the Jaccard index (defined as the size of the intersection divided by the size of the
   *         union of the sample sets)
   */
  public double getJaccard() {
    return jaccard;
  }

  /**
   * Gets the root mean squared distance between true positives.
   *
   * @return the root mean squared distance between true positives.
   */
  public double getRmsd() {
    return rmsd;
  }
}
