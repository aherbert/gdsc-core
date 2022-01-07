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

package uk.ac.sussex.gdsc.core.match;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Compares assignments using the distance.
 */
public class AssignmentComparator implements Comparator<Assignment>, Serializable {
  /**
   * The serial version ID.
   */
  private static final long serialVersionUID = 1L;

  /** An instance. */
  private static final AssignmentComparator INSTANCE = new AssignmentComparator();

  @Override
  public int compare(Assignment o1, Assignment o2) {
    return Double.compare(o1.getDistance(), o2.getDistance());
  }

  /**
   * Sort the assignments.
   *
   * @param assignments the assignments
   */
  public static void sort(List<? extends Assignment> assignments) {
    Collections.sort(assignments, INSTANCE);
  }

  /**
   * Sort the assignments using the distance.
   *
   * @param assignments the assignments
   */
  public static void sort(Assignment[] assignments) {
    Arrays.sort(assignments, INSTANCE);
  }
}
