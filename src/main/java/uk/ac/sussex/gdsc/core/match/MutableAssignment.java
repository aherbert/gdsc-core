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
 * Copyright (C) 2011 - 2018 Alex Herbert
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

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;


/**
 * Stores an assignment between two identified points and the distance between them
 */
public class MutableAssignment implements Assignment {
  private int targetId;
  private int predictedId;
  private double distance;

  /**
   * Instantiates a new assignment.
   *
   * @param targetId the target id
   * @param predictedId the predicted id
   * @param distance the distance (zero is perfect match)
   */
  public MutableAssignment(int targetId, int predictedId, double distance) {
    this.targetId = targetId;
    this.predictedId = predictedId;
    this.distance = distance;
  }

  /** {@inheritDoc} */
  @Override
  public int getTargetId() {
    return targetId;
  }

  /** {@inheritDoc} */
  @Override
  public int getPredictedId() {
    return predictedId;
  }

  /** {@inheritDoc} */
  @Override
  public double getDistance() {
    return distance;
  }

  /**
   * Set the target Id.
   *
   * @param targetId the new target id
   */
  public void setTargetId(int targetId) {
    this.targetId = targetId;
  }

  /**
   * Set the predicted Id
   *
   * @param predictedId the predicted Id to set
   */
  public void setPredictedId(int predictedId) {
    this.predictedId = predictedId;
  }

  /**
   * Set the distance
   *
   * @param distance the distance to set
   */
  public void setDistance(double distance) {
    this.distance = distance;
  }
}
