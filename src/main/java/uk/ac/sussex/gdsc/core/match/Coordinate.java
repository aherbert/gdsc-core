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
 * Stores a 2D/3D point
 */
public interface Coordinate {
  /**
   * @return The X-coordinate
   */
  public float getX();

  /**
   * @return The Y-coordinate
   */
  public float getY();

  /**
   * @return The Z-coordinate
   */
  public float getZ();

  /**
   * @return The X-coordinate cast to an int
   */
  public int getXint();

  /**
   * @return The Y-coordinate cast to an int
   */
  public int getYint();

  /**
   * @return The Z-coordinate cast to an int
   */
  public int getZint();

  /**
   * Calculate the XY distance to the given coordinates.
   *
   * @param x the x
   * @param y the y
   * @return the distance
   */
  public double distance(float x, float y);

  /**
   * Calculate the XYZ distance to the given coordinates.
   *
   * @param x the x
   * @param y the y
   * @param z the z
   * @return the distance
   */
  public double distance(float x, float y, float z);

  /**
   * Calculate the XY squared distance to the given coordinates.
   *
   * @param x the x
   * @param y the y
   * @return the squared distance
   */
  public double distance2(float x, float y);

  /**
   * Calculate the XYZ squared distance to the given coordinates.
   *
   * @param x the x
   * @param y the y
   * @param z the z
   * @return the squared distance
   */
  public double distance2(float x, float y, float z);

  /**
   * Calculate the XY distance to the given coordinate.
   *
   * @param other the other
   * @return the distance
   */
  public double distanceXY(Coordinate other);

  /**
   * Calculate the XY squared distance to the given coordinate.
   *
   * @param other the other
   * @return the squared distance
   */
  public double distanceXY2(Coordinate other);

  /**
   * Calculate the XYZ distance to the given coordinate.
   *
   * @param other the other
   * @return the distance
   */
  public double distanceXYZ(Coordinate other);

  /**
   * Calculate the XYZ squared distance to the given coordinate.
   *
   * @param other the other
   * @return the squared distance
   */
  public double distanceXYZ2(Coordinate other);
}
