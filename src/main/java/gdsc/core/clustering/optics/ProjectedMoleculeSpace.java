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

import gdsc.core.ij.Utils;
import gdsc.core.logging.TrackProgress;
import gdsc.core.utils.NotImplementedException;
import gdsc.core.utils.Sort;
import gdsc.core.utils.TurboList;
import gdsc.core.utils.TurboRandomGenerator;
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
	 * sets that resulted from recursive split of entire point set
	 */
	TurboList<int[]> splitsets;

	/**
	 * Random factory.
	 */
	RandomGenerator rand;

	private TurboRandomGenerator pseudoRandom = null;

	/**
	 * Count the number of distance computations.
	 */
	long distanceComputations;

	/**
	 * The neighbours of each point
	 */
	int[][] allNeighbours;

	/**
	 * The number of splits to compute (if below 1 it will be auto-computed using the size of the data)
	 */
	public int nSplits = 0;

	/**
	 * The number of projections to compute (if below 1 it will be auto-computed using the size of the data)
	 */
	public int nProjections = 0;

	/**
	 * Set to true to compute the neighbours using the distance to the median of the projected set. The alternative is
	 * to randomly sample neighbours from the set.
	 */
	public boolean isDistanceToMedian = true;

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
		splitsets = new TurboList<int[]>();

		// Edge cases
		if (minSplitSize < 2 || size < 1)
			return;

		if (size < 2)
		{
			// No point performing projections and splits
			splitsets.add(new int[] { 0, 1 });
			return;
		}

		final int dim = 2;

		// FastOPTICS paper states you can use c0*log(N) sets and c1*log(N) projections.
		// The ELKI framework increase this for the number of dimensions. However I have stuck
		// with the original (as it is less so will be faster).
		// Note: In most computer science contexts log is in base 2.
		int nPointSetSplits, nProject1d;

		nPointSetSplits = (nSplits > 0) ? nSplits : (int) (logOProjectionConst * log2(size));
		nProject1d = (nProjections > 0) ? nProjections : (int) (logOProjectionConst * log2(size));

		// perform O(log N+log dim) splits of the entire point sets projections
		//nPointSetSplits = (int) (logOProjectionConst * log2(size * dim + 1));
		// perform O(log N+log dim) projections of the point set onto a random line
		//nProject1d = (int) (logOProjectionConst * log2(size * dim + 1));

		// perform projections of points
		float[][] projectedPoints = new float[nProject1d][];

		long time = System.currentTimeMillis();
		int interval = Utils.getProgressInterval(nProject1d);
		if (tracker != null)
		{
			tracker.log("Computing projections ...");
		}
		// TODO - This can be multi-threaded
		for (int j = 0; j < nProject1d; j++)
		{
			if (tracker != null)
			{
				if (j % interval == 0)
					tracker.progress(j, nProject1d);
			}
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
			float[] currPro = new float[size];
			for (int it = size; it-- > 0;)
			{
				Molecule m = setOfObjects[it];
				// Dot product:
				currPro[it] = (float) (currRp[0] * m.x + currRp[1] * m.y);
			}
			projectedPoints[j] = currPro;
		}

		// split entire point set, reuse projections by shuffling them
		int[] proind = Utils.newArray(nProject1d, 0, 1);
		interval = Utils.getProgressInterval(nPointSetSplits);
		if (tracker != null)
		{
			long time2 = System.currentTimeMillis();
			tracker.log("Computed projections ... " + Utils.timeToString(time2 - time));
			time = time2;
			tracker.log("Splitting data ...");
		}

		// The splits do not have to be that random so we can use a pseudo random sequence.
		// The sets will be randomly sized between 1 and minSplitSize. Ensure we have enough 
		// numbers for all the splits.
		double expectedSetSize = (1 + minSplitSize) * 0.5;
		int expectedSets = (int) Math.round(size / expectedSetSize);
		pseudoRandom = new TurboRandomGenerator(Math.max(200, minSplitSize + 2 * expectedSets), rand);

		// TODO - This can be multi-threaded
		for (int avgP = 0; avgP < nPointSetSplits; avgP++)
		{
			if (tracker != null)
			{
				if (avgP % interval == 0)
					tracker.progress(avgP, nPointSetSplits);
			}

			// shuffle projections
			float[][] shuffledProjectedPoints = new float[nProject1d][];
			if (avgP != 0)
				pseudoRandom.shuffle(proind);
			for (int i = 0; i < nProject1d; i++)
			{
				shuffledProjectedPoints[i] = projectedPoints[proind[i]];
			}

			// split point set
			// TODO - to multi-thread we just clone the random generator, set the seed and pass in the projections
			pseudoRandom.setSeed(avgP);
			splitupNoSort(shuffledProjectedPoints, Utils.newArray(size, 0, 1), 0, size, 0, pseudoRandom, minSplitSize);

			// TODO: The ELKI implementation includes all sets within a tolerance of the minSplitSize
			// Note though that if a set is included at the upper tolerance it may be split unevenly
			// (e.g. n into n-1 and 1) and basically the same set included again.
			// Add an implementation that is true to the FastOPTICS paper. Data is split until they are
			// less than minPoints.
		}
		if (tracker != null)
		{
			time = System.currentTimeMillis() - time;
			tracker.log("Split data ... " + Utils.timeToString(time));
			tracker.progress(1);
		}
	}

	/**
	 * 1. / log(2)
	 */
	public static final double ONE_BY_LOG2 = 1. / Math.log(2.);

	/**
	 * Compute the base 2 logarithm.
	 *
	 * @param x
	 *            X
	 * @return Logarithm base 2.
	 */
	public static double log2(double x)
	{
		return Math.log(x) * ONE_BY_LOG2;
	}

	/**
	 * Recursively splits entire point set until the set is below a threshold.
	 *
	 * @param projectedPoints
	 *            the projected points
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
	 * @param minSplitSize
	 *            minimum size for which a point set is further
	 *            partitioned (roughly corresponds to minPts in OPTICS)
	 */
	private void splitupNoSort(float[][] projectedPoints, int[] ind, int begin, int end, int dim, RandomGenerator rand,
			int minSplitSize)
	{
		final int nele = end - begin;
		
		if (nele < 2)
		{
			// Nothing to split. Also ensures we only add to the sets if neighbours can be sampled.
			return;
		}
		
		dim = dim % projectedPoints.length;// choose a projection of points
		float[] tpro = projectedPoints[dim];

		// save set such that used for density or neighborhood computation
		// sets should be roughly minSplitSize
		if (nele > minSplitSize * (1 - sizeTolerance) && nele < minSplitSize * (1 + sizeTolerance))
		{
			int[] indices = Arrays.copyOfRange(ind, begin, end);
			if (isDistanceToMedian)
			{
				// sort set, since need median element later
				// (when computing distance to the middle of the set)
				Sort.sort(indices, tpro);
			}
			addToSets(indices);
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
			splitupNoSort(projectedPoints, ind, begin, splitpos, dim + 1, rand, minSplitSize);
			splitupNoSort(projectedPoints, ind, splitpos, end, dim + 1, rand, minSplitSize);
		}
	}

	private void addToSets(int[] indices)
	{
		// TODO - this should be synchronized when multi-threading the split
		splitsets.add(indices);
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
	public static int splitRandomly(int[] ind, int begin, int end, float[] tpro, RandomGenerator rand)
	{
		final int nele = end - begin;

		// pick random splitting element based on position
		float rs = tpro[ind[begin + rand.nextInt(nele)]];
		int minInd = begin, maxInd = end - 1;
		// permute elements such that all points smaller than the splitting
		// element are on the right and the others on the left in the array
		while (minInd < maxInd)
		{
			float currEle = tpro[ind[minInd]];
			if (currEle > rs)
			{
				while (minInd < maxInd && tpro[ind[maxInd]] > rs)
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
	public static int splitByDistance(int[] ind, int begin, int end, float[] tpro, RandomGenerator rand)
	{
		// pick random splitting point based on distance
		float rmin = tpro[ind[begin]], rmax = rmin;
		for (int it = begin + 1; it < end; it++)
		{
			float currEle = tpro[ind[it]];
			if (currEle < rmin)
				rmin = currEle;
			else if (currEle > rmax)
				rmax = currEle;
		}

		if (rmin != rmax)
		{ // if not all elements are the same
			float rs = (float) (rmin + rand.nextDouble() * (rmax - rmin));

			int minInd = begin, maxInd = end - 1;

			// permute elements such that all points smaller than the splitting
			// element are on the right and the others on the left in the array
			while (minInd < maxInd)
			{
				float currEle = tpro[ind[minInd]];
				if (currEle > rs)
				{
					while (minInd < maxInd && tpro[ind[maxInd]] > rs)
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
	 * Gets the core distance. We actually return the squared distance.
	 *
	 * @param sum
	 *            the sum of distances
	 * @param count
	 *            the count of distances
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
	 * Compute for each point the average distance to a point in a projected set and list of neighbors for each point
	 * from sets resulting from projection
	 *
	 * @return list of neighbours for each point
	 */
	public int[][] computeAverageDistInSetAndNeighbours()
	{
		// TODO: The ELKI implementation computes the neighbours using all items in a set to 
		// the middle of the set, and each item in the set to the middle of the set. The FastOPTICS
		// paper states that any neighbour is valid but further neighbours can be excluded using an
		// f-factor (with f 0:1). If f=1 then all neighbours are included. Below this then only some
		// of the neighbours are included using the projected distance values. Neighbours to be 
		// included are picked at random.

		double[] davg = new double[size];
		int[] nDists = new int[size];
		TIntHashSet[] neighs = new TIntHashSet[size];
		for (int it = size; it-- > 0;)
			neighs[it] = new TIntHashSet();

		final int n = splitsets.size();
		long time = System.currentTimeMillis();
		if (tracker != null)
		{
			tracker.log("Computing density and neighbourhoods ...");
		}
		final int interval = Utils.getProgressInterval(n);
		for (int i = 0; i < n; i++)
		{
			if (tracker != null)
			{
				if (i % interval == 0)
					tracker.progress(i, n);
			}

			if (isDistanceToMedian)
			{
				// ELKI uses the distance to the median of the set
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
			}
			else
			{
				// For each point A choose a neighbour from the set B.
				// Note: This only works if the set has size 2 or more.
				int[] pinSet = splitsets.get(i);

				// For a fast implementation we just shuffle the set and pick consecutive 
				// points as neighbours.
				// For speed we can use the pseudoRandom generator that was 
				// created when the sets were generated.
				pseudoRandom.shuffle(pinSet);

				for (int j = pinSet.length, k = 0; j-- > 0;)
				{
					int a = pinSet[j];
					int b = pinSet[k];

					k = j;

					double dist = setOfObjects[a].distance(setOfObjects[b]);
					++distanceComputations;

					davg[a] += dist;
					neighs[a].add(b);

					// Mirror this to get another neighbour without extra distance computations
					davg[b] += dist;
					neighs[b].add(a);
					
					// Count the distances. Each object will have 2 due to mirroring
					nDists[a] += 2;
				}
			}
		}
		if (tracker != null)
		{
			time = System.currentTimeMillis() - time;
			tracker.log("Computed density and neighbourhoods ... " + Utils.timeToString(time));
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