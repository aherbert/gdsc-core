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
 * Copyright (C) 2011 - 2025 Alex Herbert
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
public class Cluster {
  /** The x position. */
  private double x;

  /** The y position. */
  private double y;

  /** The sum of x. */
  private double sumx;

  /** The sum of y. */
  private double sumy;

  /** The sum of the weights. */
  private double sumw;

  /** The number of points in the cluster. */
  private int size;

  /**
   * The next cluster. Used to construct a single linked list of clusters.
   */
  private Cluster next;

  /**
   * The closest. Used to store potential clustering links.
   */
  private Cluster closest;

  /** The squared distance. */
  private double distanceSquared;

  /**
   * The neighbour. Used to indicate this cluster has a neighbour.
   */
  private int neighbour;

  /** The head. Used to construct a single linked list of cluster points. */
  private ClusterPoint headClusterPoint;

  /** The x bin for allocating to a grid. */
  private int xbin;

  /** The y bin for allocating to a grid. */
  private int ybin;

  /**
   * Instantiates a new cluster.
   *
   * @param point the point
   */
  public Cluster(ClusterPoint point) {
    point.setNext(null);
    headClusterPoint = point;
    sumx = point.getX() * point.getWeight();
    sumy = point.getY() * point.getWeight();
    sumw = point.getWeight();
    size = 1;
    x = point.getX();
    y = point.getY();
  }

  /**
   * Get the distance.
   *
   * @param other the other cluster
   * @return the distance
   */
  public double distance(Cluster other) {
    final double dx = getX() - other.getX();
    final double dy = getY() - other.getY();
    return Math.sqrt(dx * dx + dy * dy);
  }

  /**
   * Get the squared distance.
   *
   * @param other the other cluster
   * @return the squared distance
   */
  public double distance2(Cluster other) {
    final double dx = getX() - other.getX();
    final double dy = getY() - other.getY();
    return dx * dx + dy * dy;
  }

  /**
   * Adds the cluster to this one.
   *
   * @param other the other cluster
   */
  public void add(Cluster other) {
    // Do not check if the other cluster is null or has no points

    // Add to this list
    // Find the tail of the shortest list
    ClusterPoint big;
    ClusterPoint small;
    if (size < other.size) {
      small = headClusterPoint;
      big = other.headClusterPoint;
    } else {
      small = other.headClusterPoint;
      big = headClusterPoint;
    }

    ClusterPoint tail = small;
    while (tail.getNext() != null) {
      tail = tail.getNext();
    }

    // Join the small list to the long list
    tail.setNext(big);
    headClusterPoint = small;

    merge(other.getX(), other.getY(), other.sumx, other.sumy, other.sumw, other.size);

    // Free the other cluster
    other.clear();
  }

  /**
   * Adds the point to this cluster.
   *
   * @param point the point
   */
  public void add(ClusterPoint point) {
    point.setNext(headClusterPoint);
    headClusterPoint = point;

    merge(point.getX(), point.getY(), point.getX() * point.getWeight(),
        point.getY() * point.getWeight(), point.getWeight(), 1);
  }

  /**
   * Find the new centroid when merging with the given parameters.
   *
   * @param otherX the other X
   * @param otherY the other Y
   * @param otherSumX the other sum X
   * @param otherSumY the other sum Y
   * @param otherSumW the other sum W
   * @param otherN the other N
   */
  private void merge(double otherX, double otherY, double otherSumX, double otherSumY,
      double otherSumW, int otherN) {
    sumx += otherSumX;
    sumy += otherSumY;
    sumw += otherSumW;
    size += otherN;

    // Avoid minor drift during merge by only updating the (x,y) position if
    // it is different.
    //
    // This can effect the particle linkage algorithm when
    // merged points have the same coordinates. This is because clusters may have new coordinates
    // that are moved slightly and so the remaining points on the original coordinates join to
    // each other rather than the cluster.
    // This could be improved by changing the particle linkage algorithm to have a minimum distance
    // under which it prefers to join to clusters if they exist.
    if (x != otherX) {
      x = sumx / sumw;
    }
    if (y != otherY) {
      y = sumy / sumw;
    }
  }

  /**
   * Clear the cluster.
   */
  protected void clear() {
    headClusterPoint = null;
    setClosest(null);
    size = 0;
    x = y = sumx = sumy = sumw = setDistanceSquared(0);
  }

  /**
   * Link the two clusters as potential merge candidates only if the squared distance is smaller
   * than the other cluster's current closest.
   *
   * @param other the other cluster
   * @param d2 the squared distance
   */
  public void link(Cluster other, double d2) {
    // Check if the other cluster has a closer candidate
    if (canLink(other, d2)) {
      doLink(other, d2);
    }
  }

  /**
   * Check if this cluster can link to the other cluster. This is true if the other cluster has no
   * current closest cluster or the distance to its closest cluster is above the given distance.
   *
   * @param other the other
   * @param d2 the squared distance
   * @return true, if successful
   */
  public boolean canLink(Cluster other, double d2) {
    return other.getClosest() == null || other.getDistanceSquared() > d2;
  }

  /**
   * Link the two clusters as potential merge candidates.
   *
   * @param other the other cluster
   * @param d2 the squared distance
   */
  private void doLink(Cluster other, double d2) {
    other.setClosest(this);
    other.setDistanceSquared(d2);

    this.setClosest(other);
    this.setDistanceSquared(d2);
  }

  /**
   * Increment the neighbour counter.
   */
  public void incrementNeighbour() {
    neighbour++;
  }

  /**
   * Valid link.
   *
   * @return True if the closest cluster links back to this cluster
   */
  public boolean validLink() {
    // Check if the other cluster has an updated link to another candidate
    if (getClosest() != null) {
      // Valid if the other cluster links back to this cluster
      return getClosest().getClosest() == this;
    }
    return false;
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
   * Gets the sum of the weights of all the cluster points.
   *
   * @return the sum of the weights
   */
  public double getSumOfWeights() {
    return sumw;
  }

  /**
   * Gets the size.
   *
   * @return the size
   */
  public int getSize() {
    return size;
  }

  /**
   * Gets the head cluster point in the linked list of the cluster's points.
   *
   * @return the head cluster point
   */
  public ClusterPoint getHeadClusterPoint() {
    return headClusterPoint;
  }

  /**
   * Gets the next. Used to construct a single linked list of clusters.
   *
   * @return the next
   */
  public Cluster getNext() {
    return next;
  }

  /**
   * Sets the next. Used to construct a single linked list of clusters.
   *
   * @param next the new next
   */
  public void setNext(Cluster next) {
    this.next = next;
  }

  /**
   * Gets the closest. Used to store potential clustering links.
   *
   * @return the closest
   */
  public Cluster getClosest() {
    return closest;
  }

  /**
   * Sets the closest. Used to store potential clustering links.
   *
   * @param closest the new closest
   */
  public void setClosest(Cluster closest) {
    this.closest = closest;
  }

  /**
   * Gets the distance squared.
   *
   * @return the distance squared
   */
  public double getDistanceSquared() {
    return distanceSquared;
  }

  /**
   * Sets the distance squared.
   *
   * @param distanceSquared the distance squared
   * @return the double
   */
  public double setDistanceSquared(double distanceSquared) {
    this.distanceSquared = distanceSquared;
    return distanceSquared;
  }

  /**
   * Gets the neighbour. Used to indicate this cluster has a neighbour.
   *
   * @return the neighbour
   */
  public int getNeighbour() {
    return neighbour;
  }

  /**
   * Sets the neighbour. Used to indicate this cluster has a neighbour.
   *
   * @param neighbour the new neighbour
   */
  public void setNeighbour(int neighbour) {
    this.neighbour = neighbour;
  }

  /**
   * Gets the x bin for allocating to a grid.
   *
   * @return the x bin
   */
  public int getXBin() {
    return xbin;
  }

  /**
   * Sets the x bin for allocating to a grid.
   *
   * @param xbin the new x bin
   */
  public void setXBin(int xbin) {
    this.xbin = xbin;
  }

  /**
   * Gets the y bin for allocating to a grid.
   *
   * @return the y bin
   */
  public int getYBin() {
    return ybin;
  }

  /**
   * Sets the y bin for allocating to a grid.
   *
   * @param ybin the new y bin
   */
  public void setYBin(int ybin) {
    this.ybin = ybin;
  }
}
