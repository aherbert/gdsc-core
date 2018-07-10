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
package gdsc.core.clustering;

/**
 * Used to store all the information about a cluster in the clustering analysis.
 */
public class Cluster implements Comparable<Cluster>
{
	/** The x position. */
	public double x;

	/** The y position. */
	public double y;

	/** The sum of x. */
	public double sumx;

	/** The sum of y. */
	public double sumy;

	/** The sum of the weights. */
	public double sumw;

	/** The number in the cluster. */
	public int n;

	/**
	 * The next cluster.
	 * Used to construct a single linked list of clusters
	 */
	public Cluster next = null;

	/**
	 * The closest. Used to store potential clustering links
	 */
	public Cluster closest = null;

	/** The squared distance. */
	public double d2;

	/**
	 * The neighbour.
	 * Used to indicate this cluster has a neighbour.
	 */
	public int neighbour = 0;

	/** The head. Used to construct a single linked list of cluster points */
	public ClusterPoint head = null;

	/** The x bin for allocating to a grid. */
	public int xBin;

	/** The y bin for allocating to a grid. */
	public int yBin;

	/**
	 * Instantiates a new cluster.
	 *
	 * @param point
	 *            the point
	 */
	public Cluster(ClusterPoint point)
	{
		point.next = null;
		head = point;
		sumx = point.x * point.weight;
		sumy = point.y * point.weight;
		sumw = point.weight;
		n = 1;
		this.x = point.x;
		this.y = point.y;
	}

	/**
	 * Get the distance.
	 *
	 * @param other
	 *            the other cluster
	 * @return the distance
	 */
	public double distance(Cluster other)
	{
		final double dx = x - other.x;
		final double dy = y - other.y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * Get the squared distance.
	 *
	 * @param other
	 *            the other cluster
	 * @return the squared distance
	 */
	public double distance2(Cluster other)
	{
		final double dx = x - other.x;
		final double dy = y - other.y;
		return dx * dx + dy * dy;
	}

	/**
	 * Adds the cluster to this one.
	 *
	 * @param other
	 *            the other cluster
	 */
	public void add(Cluster other)
	{
		// Do not check if the other cluster is null or has no points

		// Add to this list
		// Find the tail of the shortest list
		ClusterPoint big, small;
		if (n < other.n)
		{
			small = head;
			big = other.head;
		}
		else
		{
			small = other.head;
			big = head;
		}

		ClusterPoint tail = small;
		while (tail.next != null)
			tail = tail.next;

		// Join the small list to the long list 
		tail.next = big;
		head = small;

		merge(other.x, other.y, other.sumx, other.sumy, other.sumw, other.n);

		// Free the other cluster
		other.clear();
	}

	/**
	 * Find the new centroid when merging with the given parameters.
	 *
	 * @param otherX
	 *            the other X
	 * @param otherY
	 *            the other Y
	 * @param otherSumX
	 *            the other sum X
	 * @param otherSumY
	 *            the other sum Y
	 * @param otherSumW
	 *            the other sum W
	 * @param otherN
	 *            the other N
	 */
	private void merge(double otherX, double otherY, double otherSumX, double otherSumY, double otherSumW, int otherN)
	{
		sumx += otherSumX;
		sumy += otherSumY;
		sumw += otherSumW;
		n += otherN;

		// Avoid minor drift during merge. This can effect the particle linkage algorithm when 
		// merged points have the same coordinates. This is because clusters may have new coordinates 
		// that are moved slightly and so the remaining points on the original coordinates join to 
		// each other rather than the cluster.
		// This could be improved by changing the particle linkage algorithm to have a minimum distance
		// under which it prefers to join to clusters if they exist.
		if (x != otherX)
			x = sumx / sumw;
		if (y != otherY)
			y = sumy / sumw;
	}

	/**
	 * Adds the point to this cluster.
	 *
	 * @param point
	 *            the point
	 */
	public void add(ClusterPoint point)
	{
		point.next = null;
		head = point;

		merge(point.x, point.y, point.x * point.weight, point.y * point.weight, point.weight, 1);
	}

	/**
	 * Clear the cluster.
	 */
	protected void clear()
	{
		head = null;
		closest = null;
		n = 0;
		x = y = sumx = sumy = sumw = d2 = 0;
	}

	/**
	 * Link the two clusters as potential merge candidates only if the squared distance is smaller than the other
	 * cluster's current closest.
	 *
	 * @param other
	 *            the other cluster
	 * @param d2
	 *            the squared distance 
	 */
	public void link(Cluster other, double d2)
	{
		// Check if the other cluster has a closer candidate
		if (other.closest != null && other.d2 < d2)
			return;

		other.closest = this;
		other.d2 = d2;

		this.closest = other;
		this.d2 = d2;
	}

	/**
	 * Increment the neighbour counter in a thread safe method.
	 */
	public synchronized void incrementNeighbour()
	{
		neighbour++;
	}

	/**
	 * Link the two clusters as potential merge candidates only if the squared distance is smaller than the other
	 * cluster's current closest.
	 * <p>
	 * Thread safe.
	 *
	 * @param other
	 *            the other cluster
	 * @param d2
	 *            the squared distance
	 */
	public synchronized void linkSynchronized(Cluster other, double d2)
	{
		// The entire method should be synchronized since the other cluster d2 is checked. 
		// This may be updated by another thread unless the updates only occur within a 
		// synchronized block
		link(other, d2);
	}

	/**
	 * Valid link.
	 *
	 * @return True if the closest cluster links back to this cluster
	 */
	public boolean validLink()
	{
		// Check if the other cluster has an updated link to another candidate
		if (closest != null)
		{
			// Valid if the other cluster links back to this cluster
			return closest.closest == this;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Cluster o)
	{
		// Sort by size, then centroid x then y, then the total weight. 
		// The sort is arbitrary but allows comparison of two lists after sorting.
		if (n < o.n)
			return -1;
		if (n > o.n)
			return 1;
		if (x < o.x)
			return -1;
		if (x > o.x)
			return 1;
		if (y < o.y)
			return -1;
		if (y > o.y)
			return 1;
		if (sumw < o.sumw)
			return -1;
		if (sumw > o.sumw)
			return 1;
		return 0;
	}
}
