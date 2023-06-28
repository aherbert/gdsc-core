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

/**
 * Stores an assignment between two identified points, the distance between them and the score for
 * the match.
 */
public class MutableFractionalAssignment extends MutableAssignment implements FractionalAssignment {

  /** The score. */
  private double score;

  /**
   * Instantiates a new fractional assignment.
   *
   * @param targetId the target id
   * @param predictedId the predicted id
   * @param distance the distance
   * @param score the score
   */
  public MutableFractionalAssignment(int targetId, int predictedId, double distance, double score) {
    super(targetId, predictedId, distance);
    this.score = score;
  }

  @Override
  public double getScore() {
    return score;
  }

  /**
   * Set the score.
   *
   * @param score the score to set
   */
  public void setScore(double score) {
    this.score = score;
  }
}
