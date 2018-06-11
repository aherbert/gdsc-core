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
 * Used to store all the information about a cluster in the clustering analysis
 */
public class TimeCluster extends Cluster
{
	public int start, end, pulse;

	public TimeCluster(ClusterPoint point)
	{
		super(point);
		start = point.start;
		end = point.end;
	}

	/**
	 * Get the time gap between the two clusters. If the clusters overlap then return 0.
	 * 
	 * @param other
	 * @return
	 */
	public int gap(TimeCluster other)
	{
		// Overlap:
		// |-----------|
		//         |---------|
		//
		// Gap:
		// |-----------|
		//                  |---------|
		return FastMath.max(0, FastMath.max(start, other.start) - FastMath.min(end, other.end));
	}

	/**
	 * Check if the union of the cluster points has unique time values using the gap between each cluster point.
	 * <p>
	 * This check is only relevant if the {@link #gap(TimeCluster)} function returns zero.
	 * 
	 * @param other
	 * @return
	 */
	public boolean validUnionRange(TimeCluster other)
	{
		for (ClusterPoint p1 = head; p1 != null; p1 = p1.next)
			for (ClusterPoint p2 = other.head; p2 != null; p2 = p2.next)
				if (p1.gap(p2) == 0)
					return false;
		return true;
	}

	/**
	 * Check if the union of the cluster points has unique time values using the start time of each cluster point.
	 * <p>
	 * This check is only relevant if the {@link #gap(TimeCluster)} function returns zero.
	 * 
	 * @param other
	 * @return
	 */
	public boolean validUnion(TimeCluster other)
	{
		for (ClusterPoint p1 = head; p1 != null; p1 = p1.next)
			for (ClusterPoint p2 = other.head; p2 != null; p2 = p2.next)
				if (p1.start == p2.start)
					return false;
		return true;
	}

	public void add(TimeCluster other)
	{
		super.add(other);

		// Update the start and end points
		start = FastMath.min(start, other.start);
		end = FastMath.max(end, other.end);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(TimeCluster o)
	{
		final int result = super.compareTo(o);
		if (result != 0)
			return result;
		// Compare using the start and end time
		if (start < o.start)
			return -1;
		if (start > o.start)
			return 1;
		if (end < o.end)
			return -1;
		if (end > o.end)
			return 1;
		return 0;
	}
}
