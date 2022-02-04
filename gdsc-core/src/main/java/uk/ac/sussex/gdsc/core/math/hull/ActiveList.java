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

package uk.ac.sussex.gdsc.core.math.hull;

import java.util.Arrays;

/**
 * A class to maintain a list of active indexes.
 */
final class ActiveList {
  private final boolean[] enabled;
  private int count;

  /**
   * Create an instance with no active indexes.
   *
   * @param size the size
   */
  ActiveList(int size) {
    enabled = new boolean[size];
  }

  /**
   * Enable all the indexes.
   */
  void enableAll() {
    Arrays.fill(enabled, true);
    count = enabled.length;
  }

  /**
   * Disable the index.
   *
   * @param index the index
   */
  void disable(int index) {
    enabled[index] = false;
    count--;
  }

  /**
   * Enable the index.
   *
   * @param index the index
   */
  void enable(int index) {
    enabled[index] = true;
    count++;
  }

  /**
   * Checks if the index is enabled.
   *
   * @param index the index
   * @return true if enabled
   */
  boolean isEnabled(int index) {
    return enabled[index];
  }

  /**
   * Checks if the index is disabled.
   *
   * @param index the index
   * @return true if disabled
   */
  boolean isDisabled(int index) {
    return !enabled[index];
  }

  /**
   * Get the count of active indexes.
   *
   * @return the count
   */
  int size() {
    return count;
  }
}
