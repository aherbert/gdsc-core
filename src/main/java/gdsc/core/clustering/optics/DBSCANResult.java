package gdsc.core.clustering.optics;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.MathArrays;

import gdsc.core.ij.Utils;
import gdsc.core.utils.ConvexHull;
import gdsc.core.utils.Maths;

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

/**
 * Contains the result of the DBSCAN algorithm
 */
public class DBSCANResult implements ClusteringResult
{
	/**
	 * Used to provide access to the raw coordinates
	 */
	private final OPTICSManager opticsManager;

	/**
	 * A result not part of any cluster
	 */
	public static final int NOISE = 0;

	/**
	 * The min points for a core object
	 */
	public final int minPts;
	/**
	 * The generating distance for a core object
	 */
	public final float generatingDistance;

	/**
	 * The order results
	 */
	final DBSCANOrder[] results;

	/**
	 * Clusters assigned by extractClusters(...)
	 */
	private int[] clusters = null;

	/**
	 * Convex hulls assigned by computeConvexHulls()
	 */
	private ConvexHull[] hulls = null;

	/**
	 * Instantiates a new DBSCAN result.
	 *
	 * @param opticsManager
	 *            the optics manager
	 * @param minPts
	 *            the min points
	 * @param generatingDistance
	 *            the generating distance
	 * @param dbscanResults
	 *            the DBSCAN results
	 */
	DBSCANResult(OPTICSManager opticsManager, int minPts, float generatingDistance, DBSCANOrder[] dbscanResults)
	{
		this.opticsManager = opticsManager;
		this.minPts = minPts;
		this.generatingDistance = generatingDistance;
		this.results = dbscanResults;
	}

	/**
	 * Get the number of results
	 *
	 * @return the number of results
	 */
	public int size()
	{
		return results.length;
	}

	/**
	 * Get the result.
	 *
	 * @param index
	 *            the index
	 * @return the DBSCAN result
	 */
	public DBSCANOrder get(int index)
	{
		return results[index];
	}

	/**
	 * Gets the DBSCAN order of the original input points.
	 *
	 * @return the order
	 */
	public int[] getOrder()
	{
		int[] data = new int[size()];
		for (int i = size(); i-- > 0;)
			data[results[i].parent] = i + 1;
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gdsc.core.clustering.optics.ClusteringResult#scrambleClusters(org.apache.commons.math3.random.RandomGenerator)
	 */
	public void scrambleClusters(RandomGenerator rng)
	{
		clusters = null;
		hulls = null;

		int max = 0;
		for (int i = size(); i-- > 0;)
		{
			if (max < results[i].clusterId)
				max = results[i].clusterId;
		}
		if (max == 0)
			return;

		int[] map = Utils.newArray(max, 1, 1);
		MathArrays.shuffle(map, rng);

		for (int i = size(); i-- > 0;)
		{
			if (results[i].clusterId > 0)
				results[i].clusterId = map[results[i].clusterId - 1];
		}
	}

	/**
	 * Extract the clusters and store a reference to them for return by {@link #getClusters()}. Deletes the cached
	 * convex hulls for previous clusters.
	 *
	 * @param core
	 *            the core
	 */
	public void extractClusters(boolean core)
	{
		clusters = getClusters(core);
		hulls = null;
	}

	/**
	 * This can be set by {@link #extractClusters(boolean)}.
	 * <p>
	 * {@inheritDoc}
	 *
	 * @see gdsc.core.clustering.optics.ClusteringResult#getClusters()
	 */
	public int[] getClusters()
	{
		return clusters;
	}

	/**
	 * Gets the cluster Id for each parent object.
	 *
	 * @param core
	 *            Set to true to get the clusters using only the core points
	 * @return the clusters
	 */
	public int[] getClusters(boolean core)
	{
		int[] clusters = new int[size()];
		if (core)
		{
			for (int i = size(); i-- > 0;)
			{
				if (results[i].nPts >= minPts)
				{
					int id = results[i].parent;
					clusters[id] = results[i].clusterId;
				}
			}
		}
		else
		{
			for (int i = size(); i-- > 0;)
			{
				int id = results[i].parent;
				clusters[id] = results[i].clusterId;
			}
		}
		return clusters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.clustering.optics.ClusteringResult#hasConvexHulls()
	 */
	public boolean hasConvexHulls()
	{
		return hulls != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.clustering.optics.ClusteringResult#computeConvexHulls()
	 */
	public void computeConvexHulls()
	{
		if (hasConvexHulls())
			return;

		if (clusters == null)
			return;

		// Get the number of clusters
		int nClusters = Maths.max(clusters);
		hulls = new ConvexHull[nClusters];

		// Descend the hierarchy and compute the hulls, smallest first
		ScratchSpace scratch = new ScratchSpace(100);
		for (int clusterId = 1; clusterId <= nClusters; clusterId++)
			computeConvexHull(clusterId, scratch);
	}

	private void computeConvexHull(int clusterId, ScratchSpace scratch)
	{
		scratch.n = 0;
		for (int i = size(); i-- > 0;)
		{
			if (clusterId == clusters[i])
			{
				scratch.safeAdd(opticsManager.getOriginalX(results[i].parent),
						opticsManager.getOriginalY(results[i].parent));
			}
		}

		// Compute the hull
		ConvexHull h = ConvexHull.create(scratch.x, scratch.y, scratch.n);
		if (h != null)
			hulls[clusterId - 1] = h;
		else
		{
			System.out.printf("No hull: n=%d\n", scratch.n);
			for (int i = 0; i < scratch.n; i++)
				System.out.printf("%d: %f,%f\n", i, scratch.x[i], scratch.y[i]);
		}
	}

	/**
	 * Gets the convex hull for the cluster. The hull includes any points within child clusters. Hulls are computed by
	 * {@link #computeConvexHulls()}.
	 *
	 * @param clusterId
	 *            the cluster id
	 * @return the convex hull (or null if not available)
	 */
	public ConvexHull getConvexHull(int clusterId)
	{
		if (hulls == null || clusterId <= 0 || clusterId > hulls.length)
			return null;
		return hulls[clusterId - 1];
	}
}