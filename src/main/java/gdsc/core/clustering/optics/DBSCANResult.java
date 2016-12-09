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

/**
 * Contains the result of the DBSCAN algorithm
 */
public class DBSCANResult
{
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
	 * Instantiates a new DBSCAN result
	 *
	 * @param minPts
	 *            the min points
	 * @param generatingDistance
	 *            the generating distance
	 * @param dbscanResults
	 *            the DBSCAN results
	 */
	DBSCANResult(int minPts, float generatingDistance, DBSCANOrder[] dbscanResults)
	{
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

	/**
	 * Gets the cluster Id for each parent object. This can be set by {@link #extractDBSCANClustering(float)} or
	 * {@link #extractClusters(double, boolean, boolean)}.
	 *
	 * @return the clusters
	 */
	public int[] getClusters()
	{
		return getClusters(false);
	}

	/**
	 * Gets the cluster Id for each parent object. This can be set by {@link #extractDBSCANClustering(float)} or
	 * {@link #extractClusters(double, boolean, boolean)}.
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

}