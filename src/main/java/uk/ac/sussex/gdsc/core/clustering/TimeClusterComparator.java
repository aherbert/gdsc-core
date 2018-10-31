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

package uk.ac.sussex.gdsc.core.clustering;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Used to sort clusters.
 *
 * <p>Sort by size, centroid x, centroid y, total weight, start time, and end time. The sort is
 * arbitrary but allows comparison of two lists after sorting.
 */
public class TimeClusterComparator implements Comparator<TimeCluster>, Serializable {

  /** The instance. */
  private static final TimeClusterComparator INSTANCE = new TimeClusterComparator();

  /**
   * The serial version ID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Compare clusters.
   * 
   * <p>Sort by size, centroid x, centroid y, total weight, start time, and end time.
   *
   * @param first the first cluster
   * @param second the second cluster
   * @return the comparison result
   */
  public int compareClusters(TimeCluster first, TimeCluster second) {
    int result = ClusterComparator.compareClusters(first, second);
    if (result != 0) {
      return result;
    }
    // Compare using the start and end time
    if (first.getStartTime() < second.getStartTime()) {
      return -1;
    }
    if (first.getStartTime() > second.getStartTime()) {
      return 1;
    }
    if (first.getEndTime() < second.getEndTime()) {
      return -1;
    }
    if (first.getEndTime() > second.getEndTime()) {
      return 1;
    }
    return 0;
  }

  /**
   * Gets an instance.
   *
   * @return an instance
   */
  public static TimeClusterComparator getInstance() {
    return INSTANCE;
  }

  @Override
  public int compare(TimeCluster o1, TimeCluster o2) {
    return compareClusters(o1, o2);
  }
}
