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
 * Contains the ordered result of the DBSCAN algorithm.
 */
public class DBSCANOrder
{
	/**
	 * The Id of the parent point object used when generating this result. Can be used to identify the coordinates
	 * from the original input data.
	 */
	public final int parent;
	/**
	 * The cluster identifier.
	 */
	int clusterId;
	/**
	 * The number of points within the generating distance.
	 */
	public final int nPts;

	/**
	 * Instantiates a new DBSCAN order result.
	 *
	 * @param parent
	 *            the parent
	 * @param coreDistance
	 *            the core distance
	 * @param nPts
	 *            The number of points in within the generating distance
	 */
	public DBSCANOrder(int parent, int clusterId, int nPts)
	{
		this.parent = parent;
		this.clusterId = clusterId;
		this.nPts = nPts;
	}

	/**
	 * Gets the cluster id.
	 *
	 * @return the cluster id
	 */
	public int getClusterId()
	{
		return clusterId;
	}
}