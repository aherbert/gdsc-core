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
 * Stores an assignment between two identified points and the distance between them.
 */
public class ImmutableAssignment implements Assignment {
  /** The target id. */
  private final int targetId;
  /** The predicted id. */
  private final int predictedId;
  /** The distance. */
  private final double distance;

  /**
   * Instantiates a new assignment.
   *
   * @param targetId the target id
   * @param predictedId the predicted id
   * @param distance the distance
   */
  public ImmutableAssignment(int targetId, int predictedId, double distance) {
    this.targetId = targetId;
    this.predictedId = predictedId;
    this.distance = distance;
  }

  @Override
  public int getTargetId() {
    return targetId;
  }

  @Override
  public int getPredictedId() {
    return predictedId;
  }

  @Override
  public double getDistance() {
    return distance;
  }
}
