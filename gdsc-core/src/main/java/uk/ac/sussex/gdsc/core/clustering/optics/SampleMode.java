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
 * Copyright (C) 2011 - 2023 Alex Herbert
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

import uk.ac.sussex.gdsc.core.utils.MathUtils;

/**
 * The sample mode to sample neighbours in FastOPTICS.
 */
public enum SampleMode {
  /** Randomly sample a maximum of two neighbours from each set. */
  RANDOM("Random"),
  /** The median of the project set is the neighbour of all points in the set. */
  MEDIAN("Median"),
  /** Sample all-vs-all from each set. */
  ALL("All");

  /** The values of the enum. */
  private static final SampleMode[] VALUES = values();

  /** The nice name. */
  private final String niceName;

  /**
   * Instantiates a new sample mode.
   *
   * @param niceName the nice name
   */
  SampleMode(String niceName) {
    this.niceName = niceName;
  }

  @Override
  public String toString() {
    return niceName;
  }

  /**
   * Gets the sample mode.
   *
   * <p>If the ordinal is outside the range of the enum then the value of the closest declared
   * constant in this enum is returned.
   *
   * @param ordinal the ordinal
   * @return the sample mode
   */
  public static SampleMode forOrdinal(int ordinal) {
    return VALUES[MathUtils.clip(0, VALUES.length - 1, ordinal)];
  }
}
