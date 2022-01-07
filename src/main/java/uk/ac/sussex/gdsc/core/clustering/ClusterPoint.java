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
 * Used to store information about a point in the clustering analysis.
 */
public class ClusterPoint {
  /** The x position. */
  private final double x;

  /** The y position. */
  private final double y;

  /** The weight. */
  private final double weight;

  /** The id. */
  private final int id;

  /** The start time. */
  private final int startTime;

  /** The end time. */
  private final int endTime;

  /**
   * The next cluster point. Used to construct a single linked list of points.
   */
  private ClusterPoint next;

  /**
   * Create a cluster point.
   *
   * @param id the id
   * @param x the x
   * @param y the y
   * @return The cluster point
   */
  public static ClusterPoint newClusterPoint(int id, double x, double y) {
    return new ClusterPoint(id, x, y);
  }

  /**
   * Create a cluster point with weight information.
   *
   * @param id the id
   * @param x the x
   * @param y the y
   * @param weight the weight
   * @return The cluster point
   */
  public static ClusterPoint newClusterPoint(int id, double x, double y, double weight) {
    return new ClusterPoint(id, x, y, weight);
  }

  /**
   * Create a cluster point with time information.
   *
   * @param id the id
   * @param x the x
   * @param y the y
   * @param start the start
   * @param end the end
   * @return The cluster point
   */
  public static ClusterPoint newTimeClusterPoint(int id, double x, double y, int start, int end) {
    return new ClusterPoint(id, x, y, start, end);
  }

  /**
   * Create a cluster point with weight and time information.
   *
   * @param id the id
   * @param x the x
   * @param y the y
   * @param weight the weight
   * @param start the start
   * @param end the end
   * @return The cluster point
   */
  public static ClusterPoint newTimeClusterPoint(int id, double x, double y, double weight,
      int start, int end) {
    return new ClusterPoint(id, x, y, weight, start, end);
  }

  /**
   * Instantiates a new cluster point.
   *
   * @param id the id
   * @param x the x
   * @param y the y
   */
  protected ClusterPoint(int id, double x, double y) {
    this.id = id;
    this.x = x;
    this.y = y;
    weight = 1;
    startTime = endTime = 0;
  }

  /**
   * Instantiates a new cluster point.
   *
   * @param id the id
   * @param x the x
   * @param y the y
   * @param start the start
   * @param end the end
   */
  protected ClusterPoint(int id, double x, double y, int start, int end) {
    this.id = id;
    this.x = x;
    this.y = y;
    weight = 1;
    this.startTime = start;
    this.endTime = end;
  }

  /**
   * Instantiates a new cluster point.
   *
   * @param id the id
   * @param x the x
   * @param y the y
   * @param weight the weight
   */
  protected ClusterPoint(int id, double x, double y, double weight) {
    this.id = id;
    this.x = x;
    this.y = y;
    this.weight = weight;
    startTime = endTime = 0;
  }

  /**
   * Instantiates a new cluster point.
   *
   * @param id the id
   * @param x the x
   * @param y the y
   * @param weight the weight
   * @param start the start
   * @param end the end
   */
  protected ClusterPoint(int id, double x, double y, double weight, int start, int end) {
    this.id = id;
    this.x = x;
    this.y = y;
    this.weight = weight;
    this.startTime = start;
    this.endTime = end;
  }

  /**
   * Get the distance.
   *
   * @param other the other cluster point
   * @return the distance
   */
  public double distance(ClusterPoint other) {
    return Math.sqrt(distanceSquared(other));
  }

  /**
   * Get the squared distance.
   *
   * @param other the other cluster point
   * @return the squared distance
   */
  public double distanceSquared(ClusterPoint other) {
    final double dx = getX() - other.getX();
    final double dy = getY() - other.getY();
    return dx * dx + dy * dy;
  }

  /**
   * Get the time gap between the two points. If the points overlap then return 0.
   *
   * @param other the other cluster point
   * @return the time gap
   */
  public int gap(ClusterPoint other) {
    return ClusterUtils.gap(getStartTime(), getEndTime(), other.getStartTime(), other.getEndTime());
  }

  /**
   * Gets the x.
   *
   * @return the x
   */
  public double getX() {
    return x;
  }

  /**
   * Gets the y.
   *
   * @return the y
   */
  public double getY() {
    return y;
  }

  /**
   * Gets the weight.
   *
   * @return the weight
   */
  public double getWeight() {
    return weight;
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  public int getId() {
    return id;
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
   * Gets the end time.
   *
   * @return the end time
   */
  public int getEndTime() {
    return endTime;
  }

  /**
   * Gets the next cluster point. Used to construct a single linked list of points.
   *
   * @return the next cluster point
   */
  public ClusterPoint getNext() {
    return next;
  }

  /**
   * Sets the next cluster point. Used to construct a single linked list of points.
   *
   * @param next the new next cluster point
   */
  public void setNext(ClusterPoint next) {
    this.next = next;
  }
}
