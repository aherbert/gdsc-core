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

package uk.ac.sussex.gdsc.core.clustering.optics;

/**
 * Track the limits of 3D coordinates.
 */
final class MinMax3d {
  private float minX;
  private float minY;
  private float maxX;
  private float maxY;
  private float minZ;
  private float maxZ;

  /**
   * Create a new instance.
   */
  MinMax3d() {
    clear();
  }

  /**
   * Clear the data.
   */
  void clear() {
    minX = minY = minZ = Float.POSITIVE_INFINITY;
    maxX = maxY = maxZ = Float.NEGATIVE_INFINITY;
  }

  /**
   * Adds the (x,y,z) value.
   *
   * @param x the x
   * @param y the y
   * @param z the z
   */
  void add(float x, float y, float z) {
    if (maxX < x) {
      maxX = x;
    }
    if (minX > x) {
      minX = x;
    }
    if (maxY < y) {
      maxY = y;
    }
    if (minY > y) {
      minY = y;
    }
    if (maxZ < z) {
      maxZ = z;
    }
    if (minZ > z) {
      minZ = z;
    }
  }

  /**
   * Gets the minimum and maximum bounds.
   *
   * <pre>
   * [minX, maxX, minY, maxY, minZ, maxZ]
   * </pre>
   *
   * <p>Return null if no data has been added.
   *
   * @return the min and max
   */
  float[] getBounds() {
    if (maxX < minX) {
      // No data
      return null;
    }
    return new float[] {minX, maxX, minY, maxY, minZ, maxZ};
  }
}
