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
 * Used in the DBSCAN/OPTICS algorithms to represent 2D molecules.
 */
class Molecule
{
	final int id;
	final float x, y;
	// Used to construct a single linked list of molecules
	public Molecule next = null;

	private boolean processed;
	float coreDistance;
	float reachabilityDistance;

	final int xBin;
	final int yBin;
	/**
	 * Working distance to current centre object
	 */
	float d;

	/**
	 * The Id of the point that set the current min reachability distance. A value of -1 has no predecessor (and so
	 * was the first point chosen by the algorithm).
	 */
	int predecessor = -1;

	/**
	 * Working data used in algorithm support
	 */
	private int workingData;

	public int getQueueIndex()
	{
		return workingData;
	}

	public void setQueueIndex(int index)
	{
		workingData = index;
	}

	Molecule(int id, float x, float y, int xBin, int yBin, Molecule next)
	{
		this.id = id;
		this.x = x;
		this.y = y;
		this.next = next;
		this.xBin = xBin;
		this.yBin = yBin;
		reset();
	}

	float distance2(Molecule other)
	{
		final float dx = x - other.x;
		final float dy = y - other.y;
		return dx * dx + dy * dy;
	}

	double distance(Molecule other)
	{
		return Math.sqrt(distance2(other));
	}

	/**
	 * Reset for fresh processing.
	 */
	void reset()
	{
		processed = false;
		workingData = 0;
		coreDistance = reachabilityDistance = OPTICSManager.UNDEFINED;
	}

	public double getReachabilityDistance()
	{
		return Math.sqrt(reachabilityDistance);
	}

	public double getCoreDistance()
	{
		return Math.sqrt(coreDistance);
	}

	public OPTICSOrder toOPTICSResult()
	{
		double actualCoreDistance = (coreDistance == OPTICSManager.UNDEFINED) ? Double.POSITIVE_INFINITY : getCoreDistance();
		double actualReachabilityDistance = (reachabilityDistance == OPTICSManager.UNDEFINED) ? Double.POSITIVE_INFINITY
				: getReachabilityDistance();
		return new OPTICSOrder(id, predecessor, actualCoreDistance, actualReachabilityDistance);
	}

	public boolean isNotProcessed()
	{
		return !processed;
	}

	public void markProcessed()
	{
		processed = true;
	}

	public void setNumberOfPoints(int nPts)
	{
		// Use the core distance to store this
		coreDistance = nPts;
	}

	public int getNumberOfPoints()
	{
		return (int) coreDistance;
	}

	public int getClusterId()
	{
		return workingData;
	}

	/**
	 * Sets the cluster origin.
	 *
	 * @param clusterId
	 *            the new cluster id
	 */
	public void setClusterOrigin(int clusterId)
	{
		workingData = clusterId;
	}

	/**
	 * Sets a member of the cluster member.
	 *
	 * @param clusterId
	 *            the new cluster id
	 */
	public void setClusterMember(int clusterId)
	{
		workingData = clusterId;
	}

	public boolean isNotInACluster()
	{
		return workingData == 0;
	}

	public DBSCANOrder toDBSCANResult()
	{
		return new DBSCANOrder(id, getClusterId(), getNumberOfPoints());
	}
}