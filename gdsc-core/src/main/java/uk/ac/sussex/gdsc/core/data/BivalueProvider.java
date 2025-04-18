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

package uk.ac.sussex.gdsc.core.data;

/**
 * Provide data on 2-axes. This is a simple interface to allow passing XY data stored in a different
 * layout without rewriting the data.
 */
public interface BivalueProvider {
  /**
   * Gets the length of the X-dimension.
   *
   * @return the length
   */
  int getLengthX();

  /**
   * Gets the length of the Y-dimension.
   *
   * @return the length
   */
  int getLengthY();

  /**
   * Gets the value.
   *
   * @param x the x (must be positive)
   * @param y the y (must be positive)
   * @return the value
   */
  double get(int x, int y);

  /**
   * Gets the 3x3 values around the index. If the index is at the bounds then the result is
   * undefined.
   *
   * @param x the x (must be positive)
   * @param y the y (must be positive)
   * @param values the values
   */
  void get(int x, int y, double[][] values);

  /**
   * Convert to an array.
   *
   * @return the array
   */
  double[][] toArray();
}
