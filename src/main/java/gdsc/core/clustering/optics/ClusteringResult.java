package gdsc.core.clustering.optics;

/*----------------------------------------------------------------------------- 
 * GDSC ImageJ Software
 * 
 * Copyright (C) 2016 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

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
	 * Compute convex hulls for each cluster.
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
	 * Gets the cluster Id for each parent object.
	 *
	 * @return the clusters
	 */
	public int[] getClusters();
}