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
 * Copyright (C) 2011 - 2020 Alex Herbert
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

import uk.ac.sussex.gdsc.core.data.NotImplementedException;

/**
 * Used in the DBSCAN/OPTICS algorithms to represent 2D molecules.
 *
 * <p>Note: The class contains methods with no implementation for use by specialised sub-classes
 * which process molecules with distances.
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
  int predecessor;

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
    return getWorkingData();
  }

  /**
   * Sets the queue index.
   *
   * @param index the new queue index
   */
  public void setQueueIndex(int index) {
    setWorkingData(index);
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
   * Reset for fresh processing.
   */
  final void reset() {
    processed = false;
    predecessor = -1;
    setWorkingData(0);
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
    return getWorkingData();
  }

  /**
   * Sets the cluster origin.
   *
   * @param clusterId the new cluster id
   */
  public void setClusterOrigin(int clusterId) {
    setWorkingData(clusterId);
  }

  /**
   * Sets a member of the cluster.
   *
   * @param clusterId the new cluster id
   */
  public void setClusterMember(int clusterId) {
    setWorkingData(clusterId);
  }

  /**
   * Checks if is not in a cluster.
   *
   * @return true, if is not in a cluster
   */
  public boolean isNotInACluster() {
    return getWorkingData() == 0;
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

  /**
   * Gets the working data.
   *
   * <p>The working data usually represents the cluster that contains the molecule, or zero if not
   * in a cluster.
   *
   * <p>The working data may represent different meanings depending on the algorithm that is using
   * the molecule. As such defined methods are used to set the working data to enable clarity within
   * the algorithm.
   *
   * <p>Note: This means the molecule cannot be used for different algorithms at the same time.
   *
   * @return the working data
   */
  private int getWorkingData() {
    return workingData;
  }

  /**
   * Sets the working data.
   *
   * @param workingData the new working data
   */
  private void setWorkingData(int workingData) {
    this.workingData = workingData;
  }
}
