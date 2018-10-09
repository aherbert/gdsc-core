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

/**
 * Extends the {@link uk.ac.sussex.gdsc.core.clustering.ClusterPoint } class to hold additional
 * information for use in clustering
 */
public class ExtendedClusterPoint extends ClusterPoint {
  /** The next cluster point. Used for linked lists. */
  public ExtendedClusterPoint nextE = null;

  /** Flag indicating if this is in a cluster. */
  public boolean inCluster = false;

  /**
   * Instantiates a new extended cluster point.
   *
   * @param id the id
   * @param x the x
   * @param y the y
   * @param t the t
   * @param other the other cluster point (used to create a linked-list)
   */
  public ExtendedClusterPoint(int id, double x, double y, int t, ClusterPoint other) {
    super(id, x, y, t, t);
    super.next = other;
  }
}
