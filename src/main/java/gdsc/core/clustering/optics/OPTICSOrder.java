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
 * Contains the ordered result of the OPTICS algorithm
 */
public class OPTICSOrder
{
	/**
	 * The Id of the parent point object used when generating this result. Can be used to identify the coordinates
	 * from the original input data.
	 */
	public final int parent;
	/**
	 * The Id of the point that set the reachability distance.
	 */
	public final int predecessor;
	/**
	 * The cluster identifier. This may be modified if
	 */
	int clusterId;	
	/**
	 * Gets the cluster id.
	 *
	 * @return the cluster id
	 */
	public int getClusterId()
	{
		return clusterId;
	}
	/**
	 * The core distance. Set to positive infinity if not a core point.
	 */
	public final double coreDistance;
	/**
	 * The reachability distance. Set to positive infinity if not a reachable point, or the first core point of a
	 * new grouping.
	 */
	public final double reachabilityDistance;

	/**
	 * Instantiates a new OPTICS order result.
	 *
	 * @param clusterId
	 *            the cluster id
	 * @param coreDistance
	 *            the core distance
	 * @param reachabilityDistance
	 *            the reachability distance
	 * @param maxDistance
	 */
	public OPTICSOrder(int parent, int predecessor, double coreDistance, double reachabilityDistance)
	{
		this.parent = parent;
		this.predecessor = predecessor;
		this.coreDistance = coreDistance;
		this.reachabilityDistance = reachabilityDistance;
	}
}