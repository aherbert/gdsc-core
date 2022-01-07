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

import org.apache.commons.rng.UniformRandomProvider;
import uk.ac.sussex.gdsc.core.math.hull.Hull;

/**
 * Contains the clustering result of the DBSCAN/OPTICS algorithm.
 */
public interface ClusteringResult {
  /**
   * Checks for hulls.
   *
   * @return true, if successful
   */
  boolean hasHulls();

  /**
   * Compute hulls and bounds for each cluster.
   *
   * <p>This may recompute the existing hulls. A check can be made for existing hulls using
   * {@link #hasHulls()}.
   *
   * @param builder the builder
   */
  void computeHulls(Hull.Builder builder);

  /**
   * Gets the convex hull for the cluster. The hull includes any points within child clusters. Hulls
   * are computed by {@link #computeHulls(Hull.Builder)}.
   *
   * @param clusterId the cluster id
   * @return the hull (or null if not available)
   */
  Hull getHull(int clusterId);

  /**
   * Gets the bounds for the cluster. The bounds includes any points within child clusters. Bounds
   * are computed by {@link #computeHulls(Hull.Builder)}.
   *
   * <p>The bounds are packed as {@code [min, max, min, max, ...]} for successive dimensions.
   *
   * @param clusterId the cluster id
   * @return the bounds (or null if not available)
   */
  float[] getBounds(int clusterId);

  /**
   * Gets the cluster Id for each parent object.
   *
   * @return the clusters
   */
  int[] getClusters();

  /**
   * Scramble the cluster numbers assigned to the results.
   *
   * <p>Since clusters are arbitrary this ensures that clusters close in proximity that are created
   * sequentially will not have sequential cluster Ids.
   *
   * @param rng the random generator
   */
  void scrambleClusters(UniformRandomProvider rng);

  /**
   * Gets the parent Ids for each cluster in the list of cluster Ids. If hierarchical clusters are
   * specified then the parent Ids for each child cluster are also returned (i.e. all members of the
   * cluster or its children).
   *
   * @param clusterIds the cluster ids
   * @return the parent ids
   */
  int[] getParents(int[] clusterIds);
}
