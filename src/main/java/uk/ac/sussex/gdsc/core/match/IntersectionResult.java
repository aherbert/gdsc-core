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
 */
//@formatter:on
public class IntersectionResult {
  /** The true positives. */
  private final int tp;
  /** The false positives. */
  private final int fp;
  /** The false negatives. */
  private final int fn;

  /**
   * Create a new instance.
   *
   * @param tp The number of true positives
   * @param fp The number of false positives
   * @param fn The number of false negatives
   */
  public IntersectionResult(int tp, int fp, int fn) {
    this.tp = tp;
    this.fp = fp;
    this.fn = fn;
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
}
