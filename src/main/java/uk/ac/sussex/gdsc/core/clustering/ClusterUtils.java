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
 * Copyright (C) 2011 - 2019 Alex Herbert
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

import org.apache.commons.math3.util.FastMath;

/**
 * Utilities used for clustering.
 */
final class ClusterUtils {

  private ClusterUtils() {}

  /**
   * Get the time gap between the two points. If the points overlap then return 0.
   *
   * @param start1 the start time of point 1
   * @param end1 the end time of point 1
   * @param start2 the start time of point 2
   * @param end2 the end time of point 2
   * @return the time gap
   */
  public static int gap(int start1, int end1, int start2, int end2) {
    // Overlap:
    // S-----------E
    // ......... S---------E
    //
    // Gap:
    // S-----------E
    // .............. S---------E
    return FastMath.max(0, FastMath.max(start1, start2) - FastMath.min(end1, end2));
  }
}
