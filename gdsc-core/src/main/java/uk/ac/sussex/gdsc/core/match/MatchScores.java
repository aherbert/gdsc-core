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

import uk.ac.sussex.gdsc.core.utils.MathUtils;

/**
 * Class to compute scores for a binary match analysis.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Binary_classification">Binary classification</a>
 */
public final class MatchScores {
  /** No public construction. */
  private MatchScores() {}

  /**
   * Calculates the precision.
   *
   * <blockquote> tp / (tp + fp) </blockquote>
   *
   * @param tp the true positives
   * @param fp the false positives
   * @return the precision.
   * @see <a href="https://en.wikipedia.org/wiki/Precision_and_recall">Precision and recall</a>
   */
  public static double calculatePrecision(int tp, int fp) {
    return MathUtils.div0(tp, tp + fp);
  }

  /**
   * Calculates the recall.
   *
   * <blockquote> tp / (tp + fn) </blockquote>
   *
   * @param tp the true positives
   * @param fn the false negatives
   * @return the recall.
   * @see <a href="https://en.wikipedia.org/wiki/Precision_and_recall">Precision and recall</a>
   */
  public static double calculateRecall(int tp, int fn) {
    return MathUtils.div0(tp, tp + fn);
  }

  /**
   * Calculates the Jaccard (defined as the size of the intersection divided by the size of the
   * union of the sample sets).
   *
   * <blockquote> tp / (tp + fp + fn) </blockquote>
   *
   * @param tp the true positives
   * @param fp the false positives
   * @param fn the false negatives
   * @return the Jaccard.
   * @see <a href="https://en.wikipedia.org/wiki/Jaccard_index">Jaccard</a>
   */
  public static double calculateJaccard(int tp, int fp, int fn) {
    return MathUtils.div0(tp, tp + fp + fn);
  }

  /**
   * Calculates the F-score statistic, a weighted combination of the precision and recall.
   *
   * <blockquote> ((1+beta)^2 * precision * recall) / (beta^2 * precision + recall) </blockquote>
   *
   * @param precision the precision
   * @param recall the recall
   * @param beta The weight
   * @return The F-score
   * @see <a href="https://en.wikipedia.org/wiki/F1_score">F-score</a>
   */
  public static double calculateFBetaScore(double precision, double recall, double beta) {
    final double b2 = beta * beta;
    return MathUtils.div0((1.0 + b2) * precision * recall, b2 * precision + recall);
  }

  /**
   * Calculates the F-score statistic, a weighted combination of the precision and recall..
   *
   * <blockquote> ((1+beta)^2 * tp) / ((1+beta)^2 * tp + beta^2 * fn + fp) </blockquote>
   *
   * @param tp the true positives
   * @param fp the false positives
   * @param fn the false negatives
   * @param beta the beta
   * @return the F-score
   * @see <a href="https://en.wikipedia.org/wiki/F1_score">F-score</a>
   */
  public static double calculateFBetaScore(int tp, int fp, int fn, double beta) {
    final double b2 = beta * beta;
    return MathUtils.div0((1 + b2) * tp, (1 + b2) * tp + b2 * fn + fp);
  }

  /**
   * Calculates the F1-score statistic, an equal weighted combination of the precision and recall.
   *
   * <blockquote> (2 * precision * recall) / (precision + recall) </blockquote>
   *
   * @param precision the precision
   * @param recall the recall
   * @return The F1-score
   * @see <a href="https://en.wikipedia.org/wiki/F1_score">F-score</a>
   */
  public static double calculateF1Score(double precision, double recall) {
    return MathUtils.div0(2 * precision * recall, precision + recall);
  }

  /**
   * Calculates the F-score statistic, a weighted combination of the precision and recall..
   *
   * <blockquote> ((1+beta)^2 * tp) / ((1+beta)^2 * tp + beta^2 * fn + fp) </blockquote>
   *
   * @param tp the true positives
   * @param fp the false positives
   * @param fn the false negatives
   * @return the F-score
   * @see <a href="https://en.wikipedia.org/wiki/F1_score">F-score</a>
   */
  public static double calculateF1Score(int tp, int fp, int fn) {
    return MathUtils.div0(2.0 * tp, 2.0 * tp + fn + fp);
  }
}
