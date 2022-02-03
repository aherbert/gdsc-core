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
 * Copyright (C) 2011 - 2022 Alex Herbert
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
 * Used to store all the information about a cluster in the clustering analysis.
 *
 * <p>Note: this class has a natural ordering that is inconsistent with equals.
 */
public class TimeCluster extends Cluster {
  /** The start time. */
  private int startTime;
  /** The end time. */
  private int endTime;
  /** The pulse time. */
  private int pulseTime;

  /**
   * Instantiates a new time cluster.
   *
   * @param point the point
   */
  public TimeCluster(ClusterPoint point) {
    super(point);
    startTime = point.getStartTime();
    endTime = point.getEndTime();
  }

  /**
   * Get the time gap between the two clusters. If the clusters overlap then return 0.
   *
   * @param other the other cluster
   * @return the time gap
   */
  public int gap(TimeCluster other) {
    return ClusterUtils.gap(getStartTime(), getEndTime(), other.getStartTime(), other.getEndTime());
  }

  /**
   * Check if the union of the cluster points has unique time values using the gap between each
   * cluster point.
   *
   * <p>This check is only relevant if the {@link #gap(TimeCluster)} function returns zero.
   *
   * @param other the other cluster
   * @return true, if successful
   */
  public boolean validUnionRange(TimeCluster other) {
    for (ClusterPoint p1 = getHeadClusterPoint(); p1 != null; p1 = p1.getNext()) {
      for (ClusterPoint p2 = other.getHeadClusterPoint(); p2 != null; p2 = p2.getNext()) {
        if (p1.gap(p2) == 0) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Check if the union of the cluster points has unique time values using the start time of each
   * cluster point.
   *
   * <p>This check is only relevant if the {@link #gap(TimeCluster)} function returns zero.
   *
   * @param other the other cluster
   * @return true, if successful
   */
  public boolean validUnion(TimeCluster other) {
    for (ClusterPoint p1 = getHeadClusterPoint(); p1 != null; p1 = p1.getNext()) {
      for (ClusterPoint p2 = other.getHeadClusterPoint(); p2 != null; p2 = p2.getNext()) {
        if (p1.getStartTime() == p2.getStartTime()) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public void add(ClusterPoint point) {
    super.add(point);

    // Update the start and end points
    setStartTime(Math.min(getStartTime(), point.getStartTime()));
    setEndTime(Math.max(getEndTime(), point.getEndTime()));
  }

  /**
   * Adds the other cluster.
   *
   * @param other the other cluster
   */
  public void add(TimeCluster other) {
    super.add(other);

    // Update the start and end points
    setStartTime(Math.min(getStartTime(), other.getStartTime()));
    setEndTime(Math.max(getEndTime(), other.getEndTime()));
  }

  /**
   * Gets the start time.
   *
   * @return the start time
   */
  public int getStartTime() {
    return startTime;
  }

  /**
   * Sets the start time.
   *
   * @param startTime the new start time
   */
  public void setStartTime(int startTime) {
    this.startTime = startTime;
  }

  /**
   * Gets the end time.
   *
   * @return the end time
   */
  public int getEndTime() {
    return endTime;
  }

  /**
   * Sets the end time.
   *
   * @param endTime the new end time
   */
  public void setEndTime(int endTime) {
    this.endTime = endTime;
  }

  /**
   * Gets the pulse time.
   *
   * @return the pulse time
   */
  public int getPulseTime() {
    return pulseTime;
  }

  /**
   * Sets the pulse time.
   *
   * @param pulseTime the new pulse time
   */
  public void setPulseTime(int pulseTime) {
    this.pulseTime = pulseTime;
  }
}
