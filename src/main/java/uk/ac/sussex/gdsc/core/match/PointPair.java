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

package uk.ac.sussex.gdsc.core.match;

/**
 * Class to store a pair of coordinates.
 */
public class PointPair {
  /** The first point. */
  private final Coordinate point1;
  /** The second point. */
  private final Coordinate point2;

  /**
   * Instantiates a new point pair.
   *
   * @param point1 point 1
   * @param point2 point 2
   */
  public PointPair(Coordinate point1, Coordinate point2) {
    this.point1 = point1;
    this.point2 = point2;
  }

  /**
   * Gets point 1.
   *
   * @return point 1.
   */
  public Coordinate getPoint1() {
    return point1;
  }

  /**
   * Gets point 2.
   *
   * @return point 2.
   */
  public Coordinate getPoint2() {
    return point2;
  }

  /**
   * Gets the XYZ distance.
   *
   * @return the distance (or -1 if either point is null).
   */
  public double getXyzDistance() {
    if (point1 == null || point2 == null) {
      return -1;
    }

    return point1.distanceXyz(point2);
  }

  /**
   * Gets the XYZ distance 2.
   *
   * @return the squared distance (or -1 if either point is null).
   */
  public double getXyzDistanceSquared() {
    if (point1 == null || point2 == null) {
      return -1;
    }

    return point1.distanceXyzSquared(point2);
  }

  /**
   * Gets the XY distance.
   *
   * @return the XY distance (or -1 if either point is null).
   */
  public double getXyDistance() {
    if (point1 == null || point2 == null) {
      return -1;
    }

    return point1.distanceXy(point2);
  }

  /**
   * Gets the XY distance 2.
   *
   * @return the squared XY distance (or -1 if either point is null).
   */
  public double getXyDistanceSquared() {
    if (point1 == null || point2 == null) {
      return -1;
    }

    return point1.distanceXySquared(point2);
  }
}
