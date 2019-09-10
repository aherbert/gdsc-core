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

/**
 * Class to store the result of a binary scoring analysis.
 *
 * <p>Can calculate the F-score statistic with a given beta weighting between the precision and
 * recall.
 *
 * @see "http://en.wikipedia.org/wiki/Precision_and_recall#F-measure"
 */
public class MatchResult {
  /** The true positives. */
  private final int tp;
  /** The false positives. */
  private final int fp;
  /** The false negatives. */
  private final int fn;
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
    this.tp = tp;
    this.fp = fp;
    this.fn = fn;
    this.rmsd = rmsd;

    precision = divide(tp, tp + fp);
    recall = divide(tp, tp + fn);
    jaccard = divide(tp, tp + fp + fn);
  }

  private static double divide(final double numerator, final int denominator) {
    if (denominator == 0) {
      return 0;
    }
    return numerator / denominator;
  }

  /**
   * Return the F-Score statistic, a weighted combination of the precision and recall.
   *
   * @param precision the precision
   * @param recall the recall
   * @param beta The weight
   * @return The F-Score
   */
  public static double calculateFScore(double precision, double recall, double beta) {
    final double b2 = beta * beta;
    final double f = ((1.0 + b2) * precision * recall) / (b2 * precision + recall);
    return (Double.isNaN(f) ? 0 : f);
  }

  /**
   * Return the F-Score statistic, a weighted combination of the precision and recall.
   *
   * @param beta The weight
   * @return The F-Score
   */
  public double getFScore(double beta) {
    return calculateFScore(precision, recall, beta);
  }

  /**
   * Return the F1-Score statistic, a equal weighted combination of the precision and recall.
   *
   * @return The F1-Score
   */
  public double getF1Score() {
    final double f = (2 * precision * recall) / (precision + recall);
    return (Double.isNaN(f) ? 0 : f);
  }

  /**
   * Gets the number of predicted points.
   *
   * @return the number of predicted points
   */
  public int getNumberPredicted() {
    return tp + fp;
  }

  /**
   * Gets the number of actual points.
   *
   * @return the number of actual points.
   */
  public int getNumberActual() {
    return tp + fn;
  }

  /**
   * Gets the true positives.
   *
   * @return the true positives
   */
  public int getTruePositives() {
    return tp;
  }

  /**
   * Gets the false positives.
   *
   * @return the false positives
   */
  public int getFalsePositives() {
    return fp;
  }

  /**
   * Gets the false negatives.
   *
   * @return the false negatives
   */
  public int getFalseNegatives() {
    return fn;
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
   * Gets the jaccard.
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
