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

package uk.ac.sussex.gdsc.core.match;

/**
 * Stores a 2D/3D point.
 */
public interface Coordinate {

  /**
   * Gets the X-coordinate.
   *
   * @return The X-coordinate.
   */
  float getX();

  /**
   * Gets the Y-coordinate.
   *
   * @return The Y-coordinate.
   */
  float getY();

  /**
   * Gets the Z-coordinate.
   *
   * @return The Z-coordinate.
   */
  float getZ();

  /**
   * Gets the X-coordinate cast to an int.
   *
   * @return The X-coordinate cast to an int.
   */
  int getXint();

  /**
   * Gets the Y-coordinate cast to an int.
   *
   * @return The Y-coordinate cast to an int.
   */
  int getYint();

  /**
   * Gets the Z-coordinate cast to an int.
   *
   * @return The Z-coordinate cast to an int.
   */
  int getZint();

  /**
   * Calculate the XY distance to the given coordinates.
   *
   * @param x the x
   * @param y the y
   * @return the distance
   */
  double distance(float x, float y);

  /**
   * Calculate the XYZ distance to the given coordinates.
   *
   * @param x the x
   * @param y the y
   * @param z the z
   * @return the distance
   */
  double distance(float x, float y, float z);

  /**
   * Calculate the XY squared distance to the given coordinates.
   *
   * @param x the x
   * @param y the y
   * @return the squared distance
   */
  double distanceSquared(float x, float y);

  /**
   * Calculate the XYZ squared distance to the given coordinates.
   *
   * @param x the x
   * @param y the y
   * @param z the z
   * @return the squared distance
   */
  double distanceSquared(float x, float y, float z);

  /**
   * Calculate the XY distance to the given coordinate.
   *
   * @param other the other
   * @return the distance
   */
  double distanceXy(Coordinate other);

  /**
   * Calculate the XY squared distance to the given coordinate.
   *
   * @param other the other
   * @return the squared distance
   */
  double distanceXySquared(Coordinate other);

  /**
   * Calculate the XYZ distance to the given coordinate.
   *
   * @param other the other
   * @return the distance
   */
  double distanceXyz(Coordinate other);

  /**
   * Calculate the XYZ squared distance to the given coordinate.
   *
   * @param other the other
   * @return the squared distance
   */
  double distanceXyzSquared(Coordinate other);
}
