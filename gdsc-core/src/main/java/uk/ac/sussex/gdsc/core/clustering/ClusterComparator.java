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

package uk.ac.sussex.gdsc.core.clustering;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Used to sort clusters.
 *
 * <p>Sort by size, centroid x, centroid y, and total weight. The sort is arbitrary but allows
 * comparison of two lists after sorting.
 */
public class ClusterComparator implements Comparator<Cluster>, Serializable {

  /** The instance. */
  private static final ClusterComparator INSTANCE = new ClusterComparator();

  /**
   * The serial version ID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Compare clusters.
   *
   * <p>Sort by size, centroid x, centroid y, and total weight.
   *
   * @param first the first cluster
   * @param second the second cluster
   * @return the comparison result
   */
  public static int compareClusters(Cluster first, Cluster second) {
    if (first.getSize() < second.getSize()) {
      return -1;
    }
    if (first.getSize() > second.getSize()) {
      return 1;
    }
    int result = Double.compare(first.getX(), second.getX());
    if (result != 0) {
      return result;
    }
    result = Double.compare(first.getY(), second.getY());
    if (result != 0) {
      return result;
    }
    return Double.compare(first.getSumOfWeights(), second.getSumOfWeights());
  }

  /**
   * Gets an instance.
   *
   * @return an instance
   */
  public static ClusterComparator getInstance() {
    return INSTANCE;
  }

  @Override
  public int compare(Cluster o1, Cluster o2) {
    return compareClusters(o1, o2);
  }
}
