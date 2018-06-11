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
