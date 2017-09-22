package gdsc.core.clustering.optics;

/*----------------------------------------------------------------------------- 
 * GDSC ImageJ Software
 * 
 * Copyright (C) 2017 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

/**
 * Represents a cluster from the OPTICS algorithm when the reachability profile is used to extract DBSCAN-like
 * clustering
 */
public class OPTICSDBSCANCluster extends OPTICSCluster
{
	private final int size;

	/**
	 * Instantiates a new cluster.
	 *
	 * @param start
	 *            the start
	 * @param end
	 *            the end
	 * @param clusterId
	 */
	public OPTICSDBSCANCluster(int start, int end, int clusterId, int size)
	{
		super(start, end, clusterId);
		this.size = size;
	}

	@Override
	public String toString()
	{
		return String.format("s=%d, e=%d, level=%d, id=%d, size=%d", start, end, getLevel(), clusterId, size);
	}

	/**
	 * Get the size of the cluster
	 *
	 * @return the size
	 */
	@Override
	public int size()
	{
		return size;
	}
}