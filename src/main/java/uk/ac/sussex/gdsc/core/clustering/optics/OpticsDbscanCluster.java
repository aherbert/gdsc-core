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
 * Represents a cluster from the OPTICS algorithm when the reachability profile is used to extract
 * DBSCAN-like clustering.
 */
public class OpticsDbscanCluster extends OpticsCluster {

  /** The size. */
  private final int size;

  /**
   * Instantiates a new cluster.
   *
   * @param start the start
   * @param end the end
   * @param clusterId the cluster id
   * @param size the size
   */
  public OpticsDbscanCluster(int start, int end, int clusterId, int size) {
    super(start, end, clusterId);
    this.size = size;
  }

  @Override
  public String toString() {
    return String.format("s=%d, e=%d, level=%d, id=%d, size=%d", start, end, getLevel(), clusterId,
        size);
  }

  /**
   * Get the size of the cluster.
   *
   * @return the size
   */
  @Override
  public int size() {
    return size;
  }
}
