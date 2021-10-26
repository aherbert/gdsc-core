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

package uk.ac.sussex.gdsc.core.clustering;

/**
 * Extends the {@link ClusterPoint} class to hold additional information for use in clustering.
 */
public class ExtendedClusterPoint extends ClusterPoint {
  /** The next cluster point. Used for linked lists. */
  private ExtendedClusterPoint next;

  /** Flag indicating if this is in a cluster. */
  private boolean inCluster;

  /**
   * Instantiates a new extended cluster point.
   *
   * @param id the id
   * @param x the x
   * @param y the y
   * @param time the time
   * @param other the other cluster point (used to create a linked-list)
   */
  public ExtendedClusterPoint(int id, double x, double y, int time, ClusterPoint other) {
    super(id, x, y, time, time);
    super.setNext(other);
  }

  /**
   * Checks if is in a cluster.
   *
   * @return true, if is in a cluster
   */
  public boolean isInCluster() {
    return inCluster;
  }

  /**
   * Sets the in cluster flag.
   *
   * @param inCluster true if is in a cluster
   */
  public void setInCluster(boolean inCluster) {
    this.inCluster = inCluster;
  }

  /**
   * Gets the next extended cluster point. Used to construct a single linked list of points.
   *
   * @return the next extended cluster point
   */
  public ExtendedClusterPoint getNextExtended() {
    return next;
  }

  /**
   * Sets the next extended cluster point. Used to construct a single linked list of points.
   *
   * @param next the new next extended cluster point
   */
  public void setNextExtended(ExtendedClusterPoint next) {
    this.next = next;
  }
}
