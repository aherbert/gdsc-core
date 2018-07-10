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
package gdsc.core.clustering.optics;

import java.awt.geom.Rectangle2D;

import org.apache.commons.math3.random.RandomGenerator;

import gdsc.core.utils.ConvexHull;

/**
 * Contains the clustering result of the DBSCAN/OPTICS algorithm
 */
public interface ClusteringResult
{
	/**
	 * Checks for convex hulls.
	 *
	 * @return true, if successful
	 */
	public boolean hasConvexHulls();

	/**
	 * Compute convex hulls and bounds for each cluster.
	 */
	public void computeConvexHulls();

	/**
	 * Gets the convex hull for the cluster. The hull includes any points within child clusters. Hulls are computed by
	 * {@link #computeConvexHulls()}.
	 *
	 * @param clusterId
	 *            the cluster id
	 * @return the convex hull (or null if not available)
	 */
	public ConvexHull getConvexHull(int clusterId);

	/**
	 * Gets the bounds for the cluster. The bounds includes any points within child clusters. Bounds are computed by
	 * {@link #computeConvexHulls()}.
	 *
	 * @param clusterId
	 *            the cluster id
	 * @return the convex hull (or null if not available)
	 */
	public Rectangle2D getBounds(int clusterId);

	/**
	 * Gets the cluster Id for each parent object.
	 *
	 * @return the clusters
	 */
	public int[] getClusters();

	/**
	 * Scramble the cluster numbers assigned to the results.
	 * <p>
	 * Since clusters are arbitrary this ensures that clusters close in proximity that are created sequentially will not
	 * have sequential cluster Ids.
	 *
	 * @param rng
	 *            the random generator
	 */
	public void scrambleClusters(RandomGenerator rng);

	/**
	 * Gets the parent ids for each cluster in the list of Ids. If hierarchical clusters are specified then the child
	 * ids are also returned (i.e. all members of the cluster or its children).
	 *
	 * @param clusterIds
	 *            the cluster ids
	 * @return the parent ids
	 */
	public int[] getParents(int[] clusterIds);
}
