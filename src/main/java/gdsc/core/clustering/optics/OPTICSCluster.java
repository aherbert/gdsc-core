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


import java.util.LinkedList;

/**
 * Represents a cluster from the OPTICS algorithm
 */
public class OPTICSCluster
{
	/** The start of the cluster (inclusive). */
	public final int start;

	/** The end of the cluster (inclusive). */
	public final int end;

	/** The cluster id. */
	int clusterId;

	/** The children. */
	LinkedList<OPTICSCluster> children = null;

	/** The level in the hierarchy */
	private int level;

	/**
	 * Instantiates a new cluster.
	 *
	 * @param start
	 *            the start
	 * @param end
	 *            the end
	 * @param clusterId
	 */
	public OPTICSCluster(int start, int end, int clusterId)
	{
		this.start = start;
		this.end = end;
		this.clusterId = clusterId;
	}

	void addChildCluster(OPTICSCluster child)
	{
		if (children == null)
			children = new LinkedList<OPTICSCluster>();
		children.add(child);
		child.increaseLevel();
	}

	private void increaseLevel()
	{
		level++;
		if (children == null)
			return;
		for (OPTICSCluster child : children)
			child.increaseLevel();
	}

	public int nChildren()
	{
		return (children == null) ? 0 : children.size();
	}

	public int getLevel()
	{
		return level;
	}

	@Override
	public String toString()
	{
		return String.format("s=%d, e=%d, level=%d, id=%d", start, end, level, clusterId);
	}

	/**
	 * Get the length of the cluster on the reachability profile (start to end inclusive)
	 *
	 * @return the length
	 */
	public int length()
	{
		return end - start + 1;
	}
	
	/**
	 * Get the size of the cluster
	 *
	 * @return the size
	 */
	public int size()
	{
		return length();
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
