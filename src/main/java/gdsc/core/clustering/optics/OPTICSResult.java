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

import java.util.ArrayList;
import java.util.List;

import gdsc.core.utils.ConvexHull;
import gdsc.core.utils.TurboList;
import gdsc.core.utils.TurboList.SimplePredicate;

/**
 * Contains the result of the OPTICS algorithm
 */
public class OPTICSResult
{
	/**
	 * Used to provide access to the raw coordinates
	 */
	private final OPTICSManager opticsManager;

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
	final OPTICSOrder[] opticsResults;

	/**
	 * Cluster hierarchy assigned by extractClustering(...).
	 */
	private ArrayList<OPTICSCluster> clustering;

	/**
	 * Convex hulls assigned by computeConvexHulls()
	 */
	private ConvexHull[] hulls = null;

	/**
	 * Instantiates a new Optics result.
	 *
	 * @param opticsManager
	 *            the optics manager
	 * @param minPts
	 *            the min points
	 * @param generatingDistance
	 *            the generating distance
	 * @param opticsResults
	 *            the optics results
	 */
	OPTICSResult(OPTICSManager opticsManager, int minPts, float generatingDistance, OPTICSOrder[] opticsResults)
	{
		this.opticsManager = opticsManager;
		this.minPts = minPts;
		this.generatingDistance = generatingDistance;
		this.opticsResults = opticsResults;
	}

	/**
	 * Get the number of results
	 *
	 * @return the number of results
	 */
	public int size()
	{
		return opticsResults.length;
	}

	/**
	 * Get the result.
	 *
	 * @param index
	 *            the index
	 * @return the OPTICS result
	 */
	public OPTICSOrder get(int index)
	{
		return opticsResults[index];
	}

	/**
	 * Gets the reachability distance profile. Points with no reachability distance (stored as infinity) can be
	 * converted to the generating distance.
	 *
	 * @param convert
	 *            convert unreachable spots to have a reachability distance of the generating distance
	 * @return the reachability distance profile
	 */
	public double[] getReachabilityDistanceProfile(boolean convert)
	{
		final double[] data = new double[size()];
		for (int i = size(); i-- > 0;)
			data[i] = opticsResults[i].reachabilityDistance;
		if (convert)
			convert(data);
		return data;
	}

	private void convert(double[] data)
	{
		for (int i = data.length; i-- > 0;)
			if (data[i] == Double.POSITIVE_INFINITY)
				data[i] = generatingDistance;
	}

	/**
	 * Gets the core distance profile. Points with no core distance (stored as infinity) can be
	 * converted to the generating distance.
	 *
	 * @param convert
	 *            convert non-core spots to have a core distance of the generating distance
	 * @return the core distance profile
	 */
	public double[] getCoreDistanceProfile(boolean convert)
	{
		final double[] data = new double[size()];
		for (int i = size(); i-- > 0;)
			data[i] = opticsResults[i].coreDistance;
		if (convert)
			convert(data);
		return data;
	}

	/**
	 * Gets the OPTICS order of the original input points.
	 *
	 * @return the order
	 */
	public int[] getOrder()
	{
		int[] data = new int[size()];
		for (int i = size(); i-- > 0;)
			data[opticsResults[i].parent] = i + 1;
		return data;
	}

	/**
	 * Gets the OPTICS predecessor of the original input points.
	 *
	 * @return the order
	 */
	public int[] getPredecessor()
	{
		int[] data = new int[size()];
		for (int i = size(); i-- > 0;)
			data[opticsResults[i].parent] = opticsResults[i].predecessor;
		return data;
	}

	/**
	 * Reset cluster ids to NOISE. Remove the clustering hierarchy and convex hulls.
	 */
	public void resetClusterIds()
	{
		for (int i = size(); i-- > 0;)
			opticsResults[i].clusterId = NOISE;

		setClustering(null);
		hulls = null;
	}

	private void setClustering(ArrayList<OPTICSCluster> clustering)
	{
		this.clustering = clustering;
	}

	/**
	 * Gets the clustering hierarchy produced by the OPTICS xi algorithm.
	 * 
	 * @see {@link #extractClusters(double, boolean, boolean)}
	 *
	 * @return the clustering hierarchy
	 */
	public ArrayList<OPTICSCluster> getClusteringHierarchy()
	{
		return clustering;
	}

	/**
	 * Gets the all clusters produced by the OPTICS xi algorithm in a single list.
	 * 
	 * @see {@link #extractClusters(double, boolean, boolean)}
	 *
	 * @return the clusters list
	 */
	public ArrayList<OPTICSCluster> getAllClusters()
	{
		ArrayList<OPTICSCluster> list = new ArrayList<OPTICSCluster>();
		addClusters(clustering, list);
		return list;
	}

	/**
	 * Descend the hierachy and add the clusters to the list.
	 *
	 * @param hierarchy
	 *            the hierarchy
	 * @param list
	 *            the list
	 */
	private void addClusters(List<OPTICSCluster> hierarchy, ArrayList<OPTICSCluster> list)
	{
		if (hierarchy == null)
			return;

		for (OPTICSCluster c : hierarchy)
		{
			addClusters(c.children, list);
			list.add(c);
		}
	}

	private static class ScratchSpace
	{
		float[] x, y;

		ScratchSpace(int n)
		{
			x = new float[n];
			y = new float[n];
		}

		void resize(int n)
		{
			if (x.length < n)
			{
				x = new float[n];
				y = new float[n];
			}
		}
	}

	/**
	 * Compute convex hulls for each cluster.
	 */
	public void computeConvexHulls()
	{
		if (hulls != null)
			return;

		if (clustering == null)
			return;

		// Get the number of clusters
		int nClusters = getNumberOfClusters();
		hulls = new ConvexHull[nClusters];

		// Descend the hierarchy and compute the hulls, smallest first
		ScratchSpace scratch = new ScratchSpace(100);
		computeConvexHulls(clustering, scratch);
	}

	private void computeConvexHulls(List<OPTICSCluster> hierarchy, ScratchSpace scratch)
	{
		if (hierarchy == null)
			return;
		for (OPTICSCluster c : hierarchy)
		{
			// Compute the hulls of the children
			computeConvexHulls(c.children, scratch);

			// Count the unique points at this level of the hierarchy
			int n = 0;
			for (int i = c.start; i <= c.end; i++)
			{
				if (opticsResults[i].clusterId == c.clusterId)
					n++;
			}

			// Add the hull points in the children
			if (c.children != null)
			{
				for (OPTICSCluster child : c.children)
				{
					n += getConvexHull(child.clusterId).size();
				}
			}

			// Ensure we have the scratch space
			scratch.resize(n);
			float[] x = scratch.x;
			float[] y = scratch.y;

			// Extract all the points
			n = 0;
			for (int i = c.start; i <= c.end; i++)
			{
				if (opticsResults[i].clusterId == c.clusterId)
				{
					x[n] = opticsManager.getOriginalX(opticsResults[i].parent);
					y[n] = opticsManager.getOriginalY(opticsResults[i].parent);
					n++;
				}
			}

			// Add the hulls from the children
			if (c.children != null)
			{
				for (OPTICSCluster child : c.children)
				{
					ConvexHull h = getConvexHull(child.clusterId);
					int size = h.size();
					System.arraycopy(h.x, 0, x, n, size);
					System.arraycopy(h.y, 0, y, n, size);
					n += size;
				}
			}

			// Compute the hull
			hulls[c.clusterId - 1] = ConvexHull.create(x, y, n);
		}
	}

	/**
	 * Count the number of clusters in the clustering hierarchy.
	 */
	public int getNumberOfClusters()
	{
		return getNumberOfClusters(clustering, 0);
	}

	private int getNumberOfClusters(List<OPTICSCluster> hierarchy, int count)
	{
		if (hierarchy == null)
			return count;
		for (OPTICSCluster c : hierarchy)
		{
			// Count the children
			count = getNumberOfClusters(c.children, count);
			// Now count this cluster
			count++;
		}
		return count;
	}

	/**
	 * Gets the convex hull for the cluster. The hull includes any points within child clusters. Hulls are computed by
	 * {@link #computeConvexHulls()}.
	 *
	 * @param clusterId
	 *            the cluster id
	 * @return the convex hull (or null if not available)
	 */
	public ConvexHull getConvexHull(int clusterId)
	{
		if (hulls == null || clusterId <= 0 || clusterId > hulls.length)
			return null;
		return hulls[clusterId - 1];
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
				if (opticsResults[i].isCorePoint())
				{
					int id = opticsResults[i].parent;
					clusters[id] = opticsResults[i].clusterId;
				}
			}
		}
		else
		{
			for (int i = size(); i-- > 0;)
			{
				int id = opticsResults[i].parent;
				clusters[id] = opticsResults[i].clusterId;
			}
		}
		return clusters;
	}

	/**
	 * Extract DBSCAN clustering from the cluster ordered objects returned from {@link #optics(float, int, boolean)}.
	 * <p>
	 * The generating distance E must be less than or equal to the generating distance used during OPTICS.
	 *
	 * @param generatingDistanceE
	 *            the generating distance E
	 * @return the number of clusters
	 */
	public int extractDBSCANClustering(float generatingDistanceE)
	{
		if (generatingDistanceE > generatingDistance)
			throw new IllegalArgumentException(
					"The generating distance must not be above the distance used during OPTICS");

		// Reset cluster Id
		int clusterId = NOISE;
		final OPTICSOrder[] clusterOrderedObjects = opticsResults;
		// Store the clusters. We use this for processing the convex hull of each cluster.
		ArrayList<OPTICSCluster> setOfClusters = new ArrayList<OPTICSCluster>();
		int cstart = -1, cend = 0;
		resetClusterIds();
		for (int i = 0; i < clusterOrderedObjects.length; i++)
		{
			final OPTICSOrder object = clusterOrderedObjects[i];
			if (object.reachabilityDistance > generatingDistanceE)
			{
				// This is a point not connected to the previous one.
				// Note that the reachability-distance of the first object in
				// the cluster-ordering is always UNDEFINED and that we as-
				// sume UNDEFINED to be greater than any defined distance
				if (object.coreDistance <= generatingDistanceE)
				{
					// New cluster

					// Record the last cluster
					if (clusterId != 0)
					{
						setOfClusters.add(new OPTICSCluster(cstart, cend, clusterId));
					}

					clusterId++;
					object.clusterId = clusterId;

					cstart = cend = i;
				}
				else
				{
					// This is noise
					object.clusterId = NOISE;
				}
			}
			else
			{
				// Extend the current cluster
				cend = i;
				object.clusterId = clusterId;
			}
		}

		// Add last cluster
		if (clusterId != 0)
		{
			setOfClusters.add(new OPTICSCluster(cstart, cend, clusterId));
		}

		// Write clusters
		//resetClusterIds();
		//for (int i = 0; i < setOfClusters.size(); i++)
		//{
		//	OPTICSCluster cluster = setOfClusters.get(i);
		//	//System.out.println(cluster);
		//	for (int c = cluster.start; c <= cluster.end; c++)
		//		clusterOrderedObjects[c].clusterId = cluster.clusterId;
		//}

		setClustering(setOfClusters);
		return clusterId;
	}

	/**
	 * Represent a Steep Area. This is used in the OPTICS algorithm to extract clusters.
	 */
	private abstract class SteepArea
	{
		int s, e;
		double maximum;

		SteepArea(int s, int e, double maximum)
		{
			this.s = s;
			this.e = e;
			this.maximum = maximum;
		}
	}

	/**
	 * Represent a Steep Down Area. This is used in the OPTICS algorithm to extract clusters.
	 */
	private class SteepDownArea extends SteepArea
	{
		double mib;

		SteepDownArea(int s, int e, double maximum)
		{
			super(s, e, maximum);
			mib = 0;
		}

		@Override
		public String toString()
		{
			return String.format("s=%d, e=%d, max=%f, mib=%f", s, e, maximum, mib);
		}
	}

	/**
	 * Represent a Steep Down Area. This is used in the OPTICS algorithm to extract clusters.
	 */
	private class SteepUpArea extends SteepArea
	{
		SteepUpArea(int s, int e, double maximum)
		{
			super(s, e, maximum);
		}

		@Override
		public String toString()
		{
			return String.format("s=%d, e=%d, max=%f, mib=%f", s, e, maximum);
		}
	}

	private class RemovePredicate implements SimplePredicate<OPTICSCluster>
	{
		int counter = 0;
		boolean[] remove;

		public RemovePredicate(boolean[] remove)
		{
			this.remove = remove;
		}

		public boolean test(OPTICSCluster t)
		{
			return remove[counter++];
		}
	}

	/**
	 * Use to return only top-level clusters that do not contain other clusters
	 */
	public static final int XI_OPTION_TOP_LEVEL = 1;
	/**
	 * Use to not correct the ends of steep up areas (matching the original algorithm)
	 */
	public static final int XI_OPTION_NO_CORRECT = 2;

	/**
	 * Extract clusters from the reachability distance profile.
	 * <p>
	 * The min points should be equal to the min points used during OPTICS. The xi parameter can be used to control
	 * the steepness of the points a cluster starts with and ends with. Higher ξ-values can be used to
	 * find only the most significant clusters, lower ξ-values to find less significant clusters.
	 *
	 * @param xi
	 *            the clustering parameter (xi).
	 */
	public void extractClusters(double xi)
	{
		extractClusters(xi, 0);
	}

	/**
	 * Extract clusters from the reachability distance profile.
	 * <p>
	 * The min points should be equal to the min points used during OPTICS. The xi parameter can be used to control
	 * the steepness of the points a cluster starts with and ends with. Higher ξ-values can be used to
	 * find only the most significant clusters, lower ξ-values to find less significant clusters.
	 *
	 * @param xi
	 *            the clustering parameter (xi).
	 * @param options
	 *            the options
	 */
	public void extractClusters(double xi, int options)
	{
		boolean topLevel = (options & XI_OPTION_TOP_LEVEL) != 0;
		boolean noCorrect = (options & XI_OPTION_NO_CORRECT) != 0;

		// This code is based on the original OPTICS paper and an R-implementation available here:
		// https://cran.r-project.org/web/packages/dbscan/ 
		// There is also a Java implementation within the ELKI project:
		// https://elki-project.github.io/
		// The ELKI project is used for JUnit testing this implementation.

		TurboList<SteepDownArea> setOfSteepDownAreas = new TurboList<SteepDownArea>();
		TurboList<OPTICSCluster> setOfClusters = new TurboList<OPTICSCluster>();
		int index = 0;
		double mib = 0;
		final int size = size();
		final double ixi = 1 - xi;
		// For simplicity we assume that the profile does not contain NaN values.
		// Positive infinity values are for points with no reachability distance.
		final double[] r = getReachabilityDistanceProfile(false);
		int clusterId = 0;
		resetClusterIds();
		while (valid(index, size))
		{
			mib = Math.max(mib, r[index]);
			// The last point cannot be the start of a steep area so end.
			if (!valid(index + 1, size))
				break;
			// Test if this is a steep down area 
			if (steepDown(index, r, ixi))
			{
				// Update mib values with current mib and filter
				updateFilterSDASet(mib, setOfSteepDownAreas, ixi);
				double startValue = r[index];
				mib = 0;
				int startSteep = index;
				int endSteep = index + 1;
				for (index++; valid(index, size); index++)
				{
					// Continue down the steep area
					if (steepDown(index, r, ixi))
					{
						endSteep = index + 1;
						continue;
					}
					// Stop looking if not going downward or after minPts of non steep area
					if (!steepDown(index, r, 1) || index - endSteep > minPts)
					{
						break;
					}
				}
				SteepDownArea sda = new SteepDownArea(startSteep, endSteep, startValue);
				//System.out.println("New steep down area:" + sda);
				setOfSteepDownAreas.add(sda);
				continue;
			}
			if (steepUp(index, r, ixi))
			{
				// Update mib values with current mib and filter
				updateFilterSDASet(mib, setOfSteepDownAreas, ixi);
				SteepUpArea sua;
				{
					int startSteep = index;
					int endSteep = index + 1;
					mib = r[index];
					double eSuccessor = getNextReachability(index, size, r);
					if (eSuccessor != Double.POSITIVE_INFINITY)
					{
						for (index++; valid(index, size); index++)
						{
							if (steepUp(index, r, ixi))
							{
								endSteep = index + 1;
								mib = r[index];
								eSuccessor = getNextReachability(index, size, r);
								if (eSuccessor == Double.POSITIVE_INFINITY)
								{
									endSteep--;
									break;
								}
								continue;
							}
							// Stop looking if not going upward or after minPts of non steep area
							if (!steepUp(index, r, 1) || index - endSteep > minPts)
							{
								break;
							}
						}
					}
					else
					{
						endSteep--;
						index++;
					}
					sua = new SteepUpArea(startSteep, endSteep, eSuccessor);
					//System.out.println("New steep up area:" + sua);
				}
				//final double threshold = mib * ixi;
				for (int i = setOfSteepDownAreas.size(); i-- > 0;)
				{
					final SteepDownArea sda = setOfSteepDownAreas.getf(i);

					// Condition 3B:  end-of-steep-up > maximum-in-between lower
					//if (threshold < sda.mib)
					if (mib * ixi < sda.mib)
						continue;

					// Default values 
					int cstart = sda.s;
					int cend = sua.e;

					// Credit to ELKI
					// NOT in original OPTICS article: never include infinity-reachable 
					// points at the end of the cluster.
					if (!noCorrect)
					{
						while (cend > cstart && r[cend] == Double.POSITIVE_INFINITY)
						{
							cend--;
						}
					}

					// Condition 4
					{
						// Case b
						if (sda.maximum * ixi >= sua.maximum)
						{
							while (cstart < cend && r[cstart + 1] > sua.maximum)
								cstart++;
						}
						// Case c
						else if (sua.maximum * ixi >= sda.maximum)
						{
							while (cend > cstart && r[cend - 1] > sda.maximum)
								cend--;
						}
						// Case a is the default
					}

					// This NOT in the original article - credit to ELKI for finding this.
					// See http://elki.dbs.ifi.lmu.de/browser/elki/elki/src/main/java/de/lmu/ifi/dbs/elki/algorithm/clustering/optics/OPTICSXi.java
					// Ensure that the predecessor is in the current cluster. This filter
					// removes common artifacts from the Xi method.
					if (!noCorrect)
					{
						simplify: while (cend > cstart)
						{
							int predecessor = get(cend).predecessor;
							for (int c = cstart; c < cend; c++)
							{
								if (predecessor == get(c).parent)
									break simplify;
							}
							// Not found.
							cend--;
						}
					}

					// This is the R-code but I do not know why so I leave it out. 
					// Ensure the last steep up point is not included if it's xi significant
					//if (steepUp(index - 1, r, ixi))
					//{
					//	cend--;
					//}

					// Condition 3A: obey minpts 
					if (cend - cstart + 1 < minPts)
						continue;

					// Build the cluster 
					clusterId++;
					OPTICSCluster cluster;
					if (topLevel)
					{
						// Do not support nested hierarchy
						// Search for children and remove them.
						// Take the lowest cluster Id of the children.
						int lowestId = clusterId;

						final boolean[] remove = new boolean[setOfClusters.size()];
						for (int ii = 0; ii < setOfClusters.size(); ii++)
						{
							OPTICSCluster child = setOfClusters.getf(ii);
							if (cstart <= child.start && child.end <= cend)
							{
								if (lowestId > child.clusterId)
									lowestId = child.clusterId;
								remove[ii] = true;
							}
						}
						// Assume the removeIf method will go linearly through the array
						setOfClusters.removeIf(new RemovePredicate(remove));

						clusterId = lowestId;
						cluster = new OPTICSCluster(cstart, cend, clusterId);

						// Assign all points
						for (int ii = cstart; ii <= cend; ii++)
						{
							get(ii).clusterId = clusterId;
						}
					}
					else
					{
						cluster = new OPTICSCluster(cstart, cend, clusterId);

						// Assign all points not currently in a cluster (thus respecting the hierarchy)
						for (int ii = cstart; ii <= cend; ii++)
						{
							if (get(ii).clusterId == NOISE)
								get(ii).clusterId = clusterId;
						}

						// Build the hierarchy of clusters
						final boolean[] remove = new boolean[setOfClusters.size()];
						for (int ii = 0; ii < setOfClusters.size(); ii++)
						{
							OPTICSCluster child = setOfClusters.getf(ii);
							if (cstart <= child.start && child.end <= cend)
							{
								cluster.addChildCluster(child);
								remove[ii] = true;
							}
						}
						setOfClusters.removeIf(new RemovePredicate(remove));
					}
					setOfClusters.add(cluster);
					//System.out.printf("> %s\n", cluster); // Level not correct
				}
			}
			else
			{
				// Not steep so move on
				index++;
			}
		}

		// Finalise
		setClustering(new ArrayList<OPTICSCluster>(setOfClusters));

		//for (OPTICSCluster cluster : getAllClusters())
		//	System.out.printf("> %s\n", cluster);
	}

	/**
	 * Update filter SDA set. Remove obsolete steep areas
	 *
	 * @param mib
	 *            the mib
	 * @param setOfSteepDownAreas
	 *            the set of steep down areas
	 * @param ixi
	 *            the ixi
	 */
	private void updateFilterSDASet(final double mib, TurboList<SteepDownArea> setOfSteepDownAreas, final double ixi)
	{
		//final double threshold = mib / ixi;
		setOfSteepDownAreas.removeIf(new SimplePredicate<SteepArea>()
		{
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.TurboList.SimplePredicate#test(java.lang.Object)
			 */
			public boolean test(SteepArea sda)
			{
				// Return true to remove.
				// "we filter all steep down areas from SDASet whose start multiplied by (1-ξ)
				// is smaller than the global mib -value"
				return sda.maximum * ixi <= mib;
				//return sda.maximum < threshold;
			}
		});
		// Update mib-values
		for (int i = setOfSteepDownAreas.size(); i-- > 0;)
			if (mib > setOfSteepDownAreas.getf(i).mib)
				setOfSteepDownAreas.getf(i).mib = mib;
	}

	/**
	 * Check for a steep up region. Determines if the reachability distance at the current index 'i' is (xi)
	 * significantly lower than the next index
	 *
	 * @param i
	 *            the i
	 * @param r
	 *            the r
	 * @param ixi
	 *            the ixi
	 * @return true, if successful
	 */
	private boolean steepUp(int i, double[] r, double ixi)
	{
		if (r[i] == Double.POSITIVE_INFINITY)
			return false;
		if (!valid(i + 1, r.length))
			return true;
		return (r[i] <= r[i + 1] * ixi);
	}

	/**
	 * Check for a steep down region. Determines if the reachability distance at the current index 'i' is (xi)
	 * significantly higher than the next index
	 *
	 * @param i
	 *            the i
	 * @param r
	 *            the r
	 * @param ixi
	 *            the ixi
	 * @return true, if successful
	 */
	private boolean steepDown(int i, double[] r, double ixi)
	{
		if (!valid(i + 1, r.length))
			return false;
		if (r[i + 1] == Double.POSITIVE_INFINITY)
			return false;
		return (r[i] * ixi >= r[i + 1]);
	}

	/**
	 * Check if the index is valid.
	 *
	 * @param index
	 *            the index
	 * @param size
	 *            the size of the results
	 * @return true, if valid
	 */
	private boolean valid(int index, int size)
	{
		return index < size;
	}

	/**
	 * Gets the next reachability (or positive infinity).
	 *
	 * @param index
	 *            the index
	 * @param size
	 *            the size
	 * @param r
	 *            the r
	 * @return the next reachability
	 */
	private double getNextReachability(int index, final int size, final double[] r)
	{
		return (valid(index + 1, size)) ? r[index + 1] : Double.POSITIVE_INFINITY;
	}
}