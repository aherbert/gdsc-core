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

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.MathArrays;

import de.lmu.ifi.dbs.elki.math.MathUtil;
import gdsc.core.ij.Utils;
import gdsc.core.logging.TrackProgress;
import gdsc.core.utils.NotImplementedException;
import gdsc.core.utils.Sort;
import gdsc.core.utils.TurboList;
import gnu.trove.set.hash.TIntHashSet;

/**
 * Store molecules and allows generation of random projections
 * <p>
 * This class is a port of de.lmu.ifi.dbs.elki.index.preprocessed.fastoptics.RandomProjectedNeighborsAndDensities.
 * Copyright (C) 2015.
 * Johannes Schneider, ABB Research, Switzerland, johannes.schneider@alumni.ethz.ch.
 * Released under the GPL v3 licence.
 */
class ProjectedMoleculeSpace extends MoleculeSpace
{
	/**
	 * Used for access to the raw coordinates
	 */
	protected final OPTICSManager opticsManager;

	private TrackProgress tracker;

	/**
	 * Default constant used to compute number of projections as well as number of
	 * splits of point set, ie. constant *log N*d
	 */
	// constant in O(log N*d) used to compute number of projections as well as
	// number of splits of point set
	private static final int logOProjectionConst = 20;

	/**
	 * Sets used for neighborhood computation should be about minSplitSize Sets
	 * are still used if they deviate by less (1+/- sizeTolerance)
	 */
	private static final float sizeTolerance = 2f / 3;

	/**
	 * minimum size for which a point set is further partitioned (roughly
	 * corresponds to minPts in OPTICS)
	 */
	int minSplitSize;

	/**
	 * sets that resulted from recursive split of entire point set
	 */
	TurboList<int[]> splitsets;

	/**
	 * all projected points
	 */
	double[][] projectedPoints;

	/**
	 * Random factory.
	 */
	RandomGenerator rand;

	/**
	 * Count the number of distance computations.
	 */
	long distanceComputations;

	/**
	 * The neighbours of each point
	 */
	int[][] allNeighbours;

	ProjectedMoleculeSpace(OPTICSManager opticsManager, float generatingDistanceE, RandomGenerator rand)
	{
		super(opticsManager.getSize(), generatingDistanceE);

		this.opticsManager = opticsManager;
		this.rand = rand;
	}

	@Override
	public String toString()
	{
		return String.format("%s", this.getClass().getSimpleName());
	}

	Molecule[] generate()
	{
		final float[] xcoord = opticsManager.getXData();
		final float[] ycoord = opticsManager.getYData();

		setOfObjects = new Molecule[xcoord.length];
		for (int i = 0; i < xcoord.length; i++)
		{
			final float x = xcoord[i];
			final float y = ycoord[i];
			setOfObjects[i] = new Molecule(i, x, y);
		}

		return setOfObjects;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.clustering.optics.OPTICSManager.MoleculeSpace#findNeighbours(int,
	 * gdsc.core.clustering.optics.OPTICSManager.Molecule, float)
	 */
	void findNeighbours(int minPts, Molecule object, float e)
	{
		// Return the neighbours found in {@link #getNeighbours()}.
		// Assume allNeighbours has been computed.
		neighbours.clear();
		int[] list = allNeighbours[object.id];
		for (int i = list.length; i-- > 0;)
			neighbours.add(setOfObjects[list[i]]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.clustering.optics.OPTICSManager.MoleculeSpace#findNeighboursAndDistances(int,
	 * gdsc.core.clustering.optics.OPTICSManager.Molecule, float)
	 */
	void findNeighboursAndDistances(int minPts, Molecule object, float e)
	{
		throw new NotImplementedException();
	}

	public void setTracker(TrackProgress tracker)
	{
		this.tracker = tracker;
	}

	/**
	 * Create random projections, project points and put points into sets of size
	 * about minSplitSize/2
	 *
	 * @param minSplitSize
	 *            minimum size for which a point set is further
	 *            partitioned (roughly corresponds to minPts in OPTICS)
	 */
	public void computeSets(int minSplitSize)
	{
		final int dim = 2;

		// perform O(log N+log dim) splits of the entire point sets projections
		int nPointSetSplits = (int) (logOProjectionConst * MathUtil.log2(size * dim + 1));
		// perform O(log N+log dim) projections of the point set onto a random line
		int nProject1d = (int) (logOProjectionConst * MathUtil.log2(size * dim + 1));

		splitsets = new TurboList<int[]>();

		// perform projections of points
		projectedPoints = new double[nProject1d][];
		double[][] tmpPro = new double[nProject1d][];

		if (tracker != null)
		{
			tracker.log("Computing projections ...");
			tracker.progress(0, nProject1d);
		}
		for (int j = 0; j < nProject1d; j++)
		{
			// Create a random unit vector
			double[] currRp = new double[dim];
			double sum = 0;
			for (int i = 0; i < dim; i++)
			{
				double fl = rand.nextDouble() - 0.5;
				currRp[i] = fl;
				sum += fl * fl;
			}
			sum = Math.sqrt(sum);
			for (int i = 0; i < dim; i++)
			{
				currRp[i] /= sum;
			}

			// Project points to the vector and compute the distance along the vector from the origin
			double[] currPro = new double[size];
			for (int it = size; it-- > 0;)
			{
				Molecule m = setOfObjects[it];
				// Dot product:
				currPro[it] = currRp[0] * m.x + currRp[1] * m.y;
			}
			projectedPoints[j] = currPro;

			if (tracker != null)
			{
				tracker.progress(j + 1, nProject1d);
			}
		}

		// split entire point set, reuse projections by shuffling them
		int[] proind = Utils.newArray(nProject1d, 0, 1);
		if (tracker != null)
		{
			tracker.log("Splitting data ...");
			tracker.progress(0, nPointSetSplits);
		}
		for (int avgP = 0; avgP < nPointSetSplits; avgP++)
		{
			// shuffle projections
			for (int i = 0; i < nProject1d; i++)
			{
				tmpPro[i] = projectedPoints[i];
			}
			MathArrays.shuffle(proind, rand);
			for (int i = 0; i < nProject1d; i++)
			{
				projectedPoints[i] = tmpPro[proind[i]];
				tmpPro[i] = projectedPoints[i];
			}

			// split point set
			splitupNoSort(Utils.newArray(size, 0, 1), 0, size, 0, rand);
			if (tracker != null)
			{
				tracker.progress(avgP = 1, nPointSetSplits);
			}
		}
	}

	/**
	 * Recursively splits entire point set until the set is below a threshold
	 *
	 * @param ind
	 *            points that are in the current set
	 * @param begin
	 *            Interval begin in the ind array
	 * @param end
	 *            Interval end in the ind array
	 * @param dim
	 *            depth of projection (how many times point set has been split
	 *            already)
	 * @param rand
	 *            Random generator
	 */
	public void splitupNoSort(int[] ind, int begin, int end, int dim, RandomGenerator rand)
	{
		final int nele = end - begin;
		dim = dim % projectedPoints.length;// choose a projection of points
		double[] tpro = projectedPoints[dim];

		// save set such that used for density or neighborhood computation
		// sets should be roughly minSplitSize
		if (nele > minSplitSize * (1 - sizeTolerance) && nele < minSplitSize * (1 + sizeTolerance))
		{
			// sort set, since need median element later
			// (when computing distance to the middle of the set)
			int[] indices = Arrays.copyOfRange(ind, begin, end);
			Sort.sort(indices, tpro);
			splitsets.add(indices);
		}

		// compute splitting element
		// do not store set or even sort set, since it is too large
		if (nele > minSplitSize)
		{
			// splits can be performed either by distance (between min,maxCoord) or by
			// picking a point randomly(picking index of point)
			// outcome is similar

			// int minInd splitByDistance(ind, nele, tpro);
			int minInd = splitRandomly(ind, begin, end, tpro, rand);

			// split set recursively
			// position used for splitting the projected points into two
			// sets used for recursive splitting
			int splitpos = minInd + 1;
			splitupNoSort(ind, begin, splitpos, dim + 1, rand);
			splitupNoSort(ind, splitpos, end, dim + 1, rand);
		}
	}

	/**
	 * Split the data set randomly.
	 *
	 * @param ind
	 *            Object index
	 * @param begin
	 *            Interval begin
	 * @param end
	 *            Interval end
	 * @param tpro
	 *            Projection
	 * @param rand
	 *            Random generator
	 * @return Splitting point
	 */
	public static int splitRandomly(int[] ind, int begin, int end, double[] tpro, RandomGenerator rand)
	{
		final int nele = end - begin;

		// pick random splitting element based on position
		double rs = tpro[begin + rand.nextInt(nele)];
		int minInd = begin, maxInd = end - 1;
		// permute elements such that all points smaller than the splitting
		// element are on the right and the others on the left in the array
		while (minInd < maxInd)
		{
			double currEle = tpro[minInd];
			if (currEle > rs)
			{
				while (minInd < maxInd && tpro[maxInd] > rs)
				{
					maxInd--;
				}
				if (minInd == maxInd)
				{
					break;
				}
				swap(ind, minInd, maxInd);
				maxInd--;
			}
			minInd++;
		}
		// if all elements are the same split in the middle
		if (minInd == end - 1)
		{
			minInd = (begin + end) >>> 1;
		}
		return minInd;
	}

	private static void swap(int[] data, int i, int j)
	{
		int tmp = data[i];
		data[i] = data[j];
		data[j] = tmp;
	}

	/**
	 * Split the data set by distances.
	 *
	 * @param ind
	 *            Object index
	 * @param begin
	 *            Interval begin
	 * @param end
	 *            Interval end
	 * @param tpro
	 *            Projection
	 * @param rand
	 *            Random generator
	 * @return Splitting point
	 */
	public static int splitByDistance(int[] ind, int begin, int end, double[] tpro, RandomGenerator rand)
	{
		// pick random splitting point based on distance
		double rmin = tpro[begin], rmax = rmin;
		for (int it = begin + 1; it < end; it++)
		{
			double currEle = tpro[it];
			if (currEle < rmin)
				rmin = currEle;
			else if (currEle > rmax)
				rmax = currEle;
		}

		if (rmin != rmax)
		{ // if not all elements are the same
			double rs = rmin + rand.nextDouble() * (rmax - rmin);

			int minInd = begin, maxInd = end - 1;

			// permute elements such that all points smaller than the splitting
			// element are on the right and the others on the left in the array
			while (minInd < maxInd)
			{
				double currEle = tpro[minInd];
				if (currEle > rs)
				{
					while (minInd < maxInd && tpro[maxInd] > rs)
					{
						maxInd--;
					}
					if (minInd == maxInd)
					{
						break;
					}
					swap(ind, minInd, maxInd);
					maxInd--;
				}
				minInd++;
			}
			return minInd;
		}
		else
		{
			// if all elements are the same split in the middle
			return (begin + end) >>> 1;
		}
	}

	/**
	 * Compute for each point the average distance to a point in a projected set
	 */
	public void computeAverageDistInSet()
	{
		double[] davg = new double[size];
		int[] nDists = new int[size];
		final int n = splitsets.size();
		if (tracker != null)
		{
			tracker.log("Computing density ...");
			tracker.progress(0, n);
		}
		final int interval = Utils.getProgressInterval(n);
		for (int i = 0; i < n; i++)
		{
			int[] pinSet = splitsets.get(i);
			final int len = pinSet.length;
			final int indoff = len >> 1;
			int v = pinSet[indoff];
			Molecule midpoint = setOfObjects[v];
			for (int j = len; j-- > 0;)
			{
				int it = pinSet[j];
				if (it == v)
				{
					continue;
				}
				double dist = midpoint.distance(setOfObjects[it]);
				++distanceComputations;
				davg[v] += dist;
				nDists[v]++;
				davg[it] += dist;
				nDists[it]++;
			}
			if (tracker != null)
			{
				if (i % interval == 0)
					tracker.progress(i, n);
			}
		}
		if (tracker != null)
		{
			tracker.progress(1);
		}

		// Finalise averages
		for (int it = size; it-- > 0;)
		{
			setOfObjects[it].coreDistance = getCoreDistance(davg[it], nDists[it]);
		}
	}

	/**
	 * Gets the core distance. We actually return the squared distance.
	 *
	 * @param sum the sum of distances
	 * @param count the count of distances
	 * @return the squared average core distance
	 */
	private float getCoreDistance(double sum, int count)
	{
		// it might be that a point does not occur for a certain size of a
		// projection (likely if too few projections, in this case there is no avg
		// distance)
		if (count == 0)
			return OPTICSManager.UNDEFINED;
		double d = sum / count;
		// We actually want the squared distance
		return (float) (d * d);
	}

	/**
	 * Compute list of neighbors for each point from sets resulting from
	 * projection
	 *
	 * @return list of neighbors for each point
	 */
	public int[][] getNeighbours()
	{
		// init lists
		TIntHashSet[] neighs = new TIntHashSet[size];
		for (int it = size; it-- > 0;)
			neighs[it] = new TIntHashSet();

		final int n = splitsets.size();
		if (tracker != null)
		{
			tracker.log("Computing neighbourhoods ...");
			tracker.progress(0, n);
		}
		final int interval = Utils.getProgressInterval(n);
		for (int i = 0; i < n; i++)
		{
			int[] pinSet = splitsets.get(i);
			final int len = pinSet.length;
			final int indoff = len >> 1;
			int v = pinSet[indoff];
			// add all points as neighbors to middle point
			// Note: This is now done below (ignoring self)
			//neighs[v].addAll(pinSet);

			// and the the middle point to all other points in set
			for (int j = len; j-- > 0;)
			{
				int it = pinSet[j];
				if (it == v)
				{
					continue;
				}
				neighs[it].add(v);
				neighs[v].add(it);
			}
			if (tracker != null)
			{
				if (i % interval == 0)
					tracker.progress(i, n);
			}
		}
		if (tracker != null)
		{
			tracker.progress(1);
		}

		// Convert to simple arrays
		allNeighbours = new int[size][];
		for (int it = size; it-- > 0;)
		{
			allNeighbours[it] = neighs[it].toArray();
			neighs[it] = null; // Allow garbage collection
		}

		return allNeighbours;
	}

	/**
	 * Compute for each point the average distance to a point in a projected set and list of neighbors for each point
	 * from sets resulting from projection
	 *
	 * @return list of neighbours for each point
	 */
	public int[][] computeAverageDistInSetAndNeighbours()
	{
		double[] davg = new double[size];
		int[] nDists = new int[size];
		TIntHashSet[] neighs = new TIntHashSet[size];
		for (int it = size; it-- > 0;)
			neighs[it] = new TIntHashSet();

		final int n = splitsets.size();
		if (tracker != null)
		{
			tracker.log("Computing density and neighbourhoods ...");
			tracker.progress(0, n);
		}
		final int interval = Utils.getProgressInterval(n);
		for (int i = 0; i < n; i++)
		{
			int[] pinSet = splitsets.get(i);
			final int len = pinSet.length;
			final int indoff = len >> 1;
			int v = pinSet[indoff];
			nDists[v] += len - 1;
			Molecule midpoint = setOfObjects[v];
			for (int j = len; j-- > 0;)
			{
				int it = pinSet[j];
				if (it == v)
				{
					continue;
				}
				double dist = midpoint.distance(setOfObjects[it]);
				++distanceComputations;
				davg[v] += dist;
				davg[it] += dist;
				nDists[it]++;

				neighs[it].add(v);
				neighs[v].add(it);
			}
			if (tracker != null)
			{
				if (i % interval == 0)
					tracker.progress(i, n);
			}
		}
		if (tracker != null)
		{
			tracker.progress(1);
		}

		// Finalise averages
		// Convert to simple arrays
		allNeighbours = new int[size][];
		for (int it = size; it-- > 0;)
		{
			setOfObjects[it].coreDistance = getCoreDistance(davg[it], nDists[it]);

			allNeighbours[it] = neighs[it].toArray();
			neighs[it] = null; // Allow garbage collection
		}

		return allNeighbours;
	}
}