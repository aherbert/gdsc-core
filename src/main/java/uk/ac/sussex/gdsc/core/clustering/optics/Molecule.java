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

package uk.ac.sussex.gdsc.core.clustering.optics;

import uk.ac.sussex.gdsc.core.utils.NotImplementedException;

/**
 * Used in the DBSCAN/OPTICS algorithms to represent 2D molecules.
 */
class Molecule {
  /** The id. */
  final int id;
  /** The x. */
  final float x;
  /** The y. */
  final float y;
  /** The processed flag. */
  private boolean processed;
  /** The core distance. */
  float coreDistance;
  /** The reachability distance. */
  float reachabilityDistance;

  /**
   * The Id of the point that set the current min reachability distance. A value of -1 has no
   * predecessor (and so was the first point chosen by the algorithm).
   */
  int predecessor = -1;

  /**
   * Working data used in algorithm support.
   */
  private int workingData;

  /**
   * Gets the queue index.
   *
   * @return the queue index
   */
  public int getQueueIndex() {
    return workingData;
  }

  /**
   * Sets the queue index.
   *
   * @param index the new queue index
   */
  public void setQueueIndex(int index) {
    workingData = index;
  }

  /**
   * Instantiates a new molecule.
   *
   * @param id the id
   * @param x the x
   * @param y the y
   */
  Molecule(int id, float x, float y) {
    this.id = id;
    this.x = x;
    this.y = y;
    reset();
  }

  /**
   * Get the squared distance to the other molecule.
   *
   * @param other the other
   * @return the squared distance
   */
  float distanceSquared(Molecule other) {
    final float dx = x - other.x;
    final float dy = y - other.y;
    return dx * dx + dy * dy;
  }

  /**
   * Get the distance to the other molecule.
   *
   * @param other the other
   * @return the distance
   */
  double distance(Molecule other) {
    return Math.sqrt(distanceSquared(other));
  }

  /**
   * Reset for fresh processing.
   */
  void reset() {
    processed = false;
    predecessor = -1;
    workingData = 0;
    coreDistance = reachabilityDistance = OpticsManager.UNDEFINED;
  }

  /**
   * Gets the reachability distance.
   *
   * @return the reachability distance
   */
  public double getReachabilityDistance() {
    return Math.sqrt(reachabilityDistance);
  }

  /**
   * Gets the core distance.
   *
   * @return the core distance
   */
  public double getCoreDistance() {
    return Math.sqrt(coreDistance);
  }

  /**
   * Convert to an OPTICS result.
   *
   * @return the OPTICS order
   */
  public OpticsOrder toOpticsResult() {
    final double actualCoreDistance =
        (coreDistance == OpticsManager.UNDEFINED) ? Double.POSITIVE_INFINITY : getCoreDistance();
    final double actualReachabilityDistance =
        (reachabilityDistance == OpticsManager.UNDEFINED) ? Double.POSITIVE_INFINITY
            : getReachabilityDistance();
    return new OpticsOrder(id, predecessor, actualCoreDistance, actualReachabilityDistance);
  }

  /**
   * Checks if is not processed.
   *
   * @return true, if is not processed
   */
  public boolean isNotProcessed() {
    return !processed;
  }

  /**
   * Mark processed.
   */
  public void markProcessed() {
    processed = true;
  }

  /**
   * Sets the number of points.
   *
   * @param numberOfPoints the new number of points
   */
  public void setNumberOfPoints(int numberOfPoints) {
    // Use the core distance to store this
    coreDistance = numberOfPoints;
  }

  /**
   * Gets the number of points.
   *
   * @return the number of points
   */
  public int getNumberOfPoints() {
    return (int) coreDistance;
  }

  /**
   * Gets the cluster id.
   *
   * @return the cluster id
   */
  public int getClusterId() {
    return workingData;
  }

  /**
   * Sets the cluster origin.
   *
   * @param clusterId the new cluster id
   */
  public void setClusterOrigin(int clusterId) {
    workingData = clusterId;
  }

  /**
   * Sets a member of the cluster member.
   *
   * @param clusterId the new cluster id
   */
  public void setClusterMember(int clusterId) {
    workingData = clusterId;
  }

  /**
   * Checks if is not in a cluster.
   *
   * @return true, if is not in a cluster
   */
  public boolean isNotInACluster() {
    return workingData == 0;
  }

  /**
   * Convert to a DBSCAN result.
   *
   * @return the DBSCAN order
   */
  public DbscanOrder toDbscanResult() {
    return new DbscanOrder(id, getClusterId(), getNumberOfPoints());
  }

  /**
   * Gets the next molecule.
   *
   * @return the next molecule
   */
  public Molecule getNext() {
    throw new NotImplementedException();
  }

  /**
   * Sets the next molecule.
   *
   * @param next the new next molecule
   */
  public void setNext(Molecule next) {
    throw new NotImplementedException();
  }

  /**
   * Gets the x bin.
   *
   * @return the x bin
   */
  int getXBin() {
    throw new NotImplementedException();
  }

  /**
   * Gets the y bin.
   *
   * @return the y bin
   */
  int getYBin() {
    throw new NotImplementedException();
  }

  /**
   * Gets the distance.
   *
   * @return the distance
   */
  float getD() {
    throw new NotImplementedException();
  }

  /**
   * Sets the distance.
   *
   * @param distance the new distance
   */
  void setD(float distance) {
    throw new NotImplementedException();
  }
}
