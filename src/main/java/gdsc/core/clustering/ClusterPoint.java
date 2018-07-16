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

import org.apache.commons.math3.util.FastMath;

/**
 * Used to store information about a point in the clustering analysis.
 */
public class ClusterPoint
{
	/** The x position. */
	public double x;

	/** The y position. */
	public double y;

	/** The weight. */
	public double weight;

	/** The id. */
	public int id;

	/** The start frame. */
	public int start;

	/** The end frame. */
	public int end;

	/**
	 * The next.
	 * Used to construct a single linked list of points.
	 */
	public ClusterPoint next = null;

	/**
	 * Create a cluster point.
	 *
	 * @param id
	 *            the id
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @return The cluster point
	 */
	public static ClusterPoint newClusterPoint(int id, double x, double y)
	{
		return new ClusterPoint(id, x, y);
	}

	/**
	 * Create a cluster point with time information.
	 *
	 * @param id
	 *            the id
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param start
	 *            the start
	 * @param end
	 *            the end
	 * @return The cluster point
	 */
	public static ClusterPoint newTimeClusterPoint(int id, double x, double y, int start, int end)
	{
		return new ClusterPoint(id, x, y, start, end);
	}

	/**
	 * Create a cluster point with weight information.
	 *
	 * @param id
	 *            the id
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param weight
	 *            the weight
	 * @return The cluster point
	 */
	public static ClusterPoint newClusterPoint(int id, double x, double y, double weight)
	{
		return new ClusterPoint(id, x, y, weight);
	}

	/**
	 * Create a cluster point with weight and time information.
	 *
	 * @param id
	 *            the id
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param weight
	 *            the weight
	 * @param start
	 *            the start
	 * @param end
	 *            the end
	 * @return The cluster point
	 */
	public static ClusterPoint newTimeClusterPoint(int id, double x, double y, double weight, int start, int end)
	{
		return new ClusterPoint(id, x, y, weight, start, end);
	}

	/**
	 * Instantiates a new cluster point.
	 *
	 * @param id
	 *            the id
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 */
	protected ClusterPoint(int id, double x, double y)
	{
		this.id = id;
		this.x = x;
		this.y = y;
		weight = 1;
		start = end = 0;
	}

	/**
	 * Instantiates a new cluster point.
	 *
	 * @param id
	 *            the id
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param start
	 *            the start
	 * @param end
	 *            the end
	 */
	protected ClusterPoint(int id, double x, double y, int start, int end)
	{
		this.id = id;
		this.x = x;
		this.y = y;
		weight = 1;
		this.start = start;
		this.end = end;
	}

	/**
	 * Instantiates a new cluster point.
	 *
	 * @param id
	 *            the id
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param weight
	 *            the weight
	 */
	protected ClusterPoint(int id, double x, double y, double weight)
	{
		this.id = id;
		this.x = x;
		this.y = y;
		this.weight = weight;
		start = end = 0;
	}

	/**
	 * Instantiates a new cluster point.
	 *
	 * @param id
	 *            the id
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param weight
	 *            the weight
	 * @param start
	 *            the start
	 * @param end
	 *            the end
	 */
	protected ClusterPoint(int id, double x, double y, double weight, int start, int end)
	{
		this.id = id;
		this.x = x;
		this.y = y;
		this.weight = weight;
		this.start = start;
		this.end = end;
	}

	/**
	 * Get the distance.
	 *
	 * @param other
	 *            the other cluster point
	 * @return the distance
	 */
	public double distance(ClusterPoint other)
	{
		final double dx = x - other.x;
		final double dy = y - other.y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * Get the squared distance.
	 *
	 * @param other
	 *            the other cluster point
	 * @return the squared distance
	 */
	public double distance2(ClusterPoint other)
	{
		final double dx = x - other.x;
		final double dy = y - other.y;
		return dx * dx + dy * dy;
	}

	/**
	 * Get the time gap between the two points. If the points overlap then return 0.
	 *
	 * @param other
	 *            the other cluster point
	 * @return the time gap
	 */
	public int gap(ClusterPoint other)
	{
		// Overlap:
		// S-----------E
		//         S---------E
		//
		// Gap:
		// S-----------E
		//                  S---------E
		return FastMath.max(0, FastMath.max(start, other.start) - FastMath.min(end, other.end));
	}
}
