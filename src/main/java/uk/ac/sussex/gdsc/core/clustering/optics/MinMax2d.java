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

package uk.ac.sussex.gdsc.core.clustering.optics;

/**
 * Provide space for storing cluster coordinates.
 */
class MinMax2d {
  private float minX;
  private float minY;
  private float maxX;
  private float maxY;

  /**
   * Create a new instance.
   */
  MinMax2d() {
    clear();
  }

  /**
   * Clear the data.
   */
  void clear() {
    minX = minY = Float.POSITIVE_INFINITY;
    maxX = maxY = Float.NEGATIVE_INFINITY;
  }

  /**
   * Adds the (x,y) value.
   *
   * @param x the x
   * @param y the y
   */
  void add(float x, float y) {
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
  }

  /**
   * Gets the minimum and maximum bounds.
   *
   * <pre>
   * [minX, maxX, minY, maxY]
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
    return new float[] {minX, maxX, minY, maxY};
  }
}
