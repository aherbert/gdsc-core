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

import java.awt.Rectangle;
import java.util.Comparator;

import gdsc.core.clustering.CoordinateStore;
import gdsc.core.ij.Utils;
import gdsc.core.logging.TrackProgress;
import gdsc.core.utils.Maths;

/**
 * Compute clustering using OPTICS.
 * <p>
 * This is an implementation of the OPTICS method. Mihael Ankerst, Markus M Breunig, Hans-Peter Kriegel, and Jorg
 * Sander. Optics: ordering points to identify the clustering structure. In ACM Sigmod Record, volume 28, pages
 * 49–60. ACM, 1999.
 */
public class OPTICSManager extends CoordinateStore
{
	/**
	 * The UNDEFINED distance in the OPTICS algorithm. This is actually arbitrary. Use a simple value so we can spot it
	 * when debugging.
	 */
	private static final float UNDEFINED = -1;

	/**
	 * Used in the OPTICS algorithm to represent 2D molecules.
	 */
	private class OPTICSMolecule
	{
		int id;
		float x, y;
		// Used to construct a single linked list of molecules
		public OPTICSMolecule next = null;

		private boolean processed;
		private float coreDistance;
		private float reachabilityDistance;

		private final int xBin, yBin;
		/**
		 * Working distance to current centre object
		 */
		private float d;

		/**
		 * The Id of the point that set the current min reachability distance. A value of -1 has no predecessor (and so
		 * was the first point chosen by the algorithm).
		 */
		private int predecessor = -1;

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

		OPTICSMolecule(int id, float x, float y, int xBin, int yBin, OPTICSMolecule next)
		{
			this.id = id;
			this.x = x;
			this.y = y;
			this.next = next;
			this.xBin = xBin;
			this.yBin = yBin;
			reset();
		}

		float distance2(OPTICSMolecule other)
		{
			final float dx = x - other.x;
			final float dy = y - other.y;
			return dx * dx + dy * dy;
		}

		void reset()
		{
			processed = false;
			workingData = 0;
			coreDistance = reachabilityDistance = UNDEFINED;
		}

		public double getReachabilityDistance()
		{
			return Math.sqrt(reachabilityDistance);
		}

		public double getCoreDistance()
		{
			return Math.sqrt(coreDistance);
		}

		public OPTICSOrder toResult()
		{
			double actualCoreDistance = (coreDistance == UNDEFINED) ? Double.POSITIVE_INFINITY : getCoreDistance();
			double actualReachabilityDistance = (reachabilityDistance == UNDEFINED) ? Double.POSITIVE_INFINITY
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

		/**
		 * Set the core distance. Used in OPTICS
		 *
		 * @param minPts
		 *            the min points to be a core point
		 * @param neighbours
		 *            the neighbours
		 */
		public void setCoreDistance(int minPts, OPTICSMoleculeList neighbours)
		{
			processed = true;

			final int size = neighbours.size;
			if (size < minPts)
				// Not a core point
				return;

			final OPTICSMolecule[] list = neighbours.list;

			// Avoid a full sort using a priority queue structure.
			// We retain a pointer to the current highest value in the set. 
			int max = 0;
			floatArray[0] = list[0].d;

			// Fill 
			int i = 1;
			while (i < minPts)
			{
				floatArray[i] = list[i].d;
				if (floatArray[max] < floatArray[i])
					max = i;
				i++;
			}

			// Scan
			while (i < size)
			{
				// Replace if lower
				if (floatArray[max] > list[i].d)
				{
					floatArray[max] = list[i].d;
					// Find new max
					for (int j = minPts; j-- > 0;)
					{
						if (floatArray[max] < floatArray[j])
							max = j;
					}
				}
				i++;
			}

			coreDistance = floatArray[max];

			// Full sort 
			//		for (int i = size; i-- > 0;)
			//			floatArray[i] = list[i].d;
			//		Arrays.sort(floatArray, 0, size);
			//		currentObject.coreDistance = floatArray[minPts - 1];
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

	private static class OPTICSComparator implements Comparator<OPTICSMolecule>
	{
		public int compare(OPTICSMolecule o1, OPTICSMolecule o2)
		{
			// Sort by reachability distance
			if (o1.reachabilityDistance < o2.reachabilityDistance)
				return -1;
			if (o1.reachabilityDistance > o2.reachabilityDistance)
				return 1;
			// The ELKI code de.lmu.ifi.dbs.elki.algorithm.clustering.optics.OPTICSHeapEntry
			// Returns the opposite of an id comparison:
			// return -DBIDUtil.compare(objectID, o.objectID);
			// I do not know why this is but I have added it so the functionality 
			// is identical in order to pass the JUnit tests
			return o2.id - o1.id;
		}
	}

	private final static OPTICSComparator opticsComparator = new OPTICSComparator();

	/**
	 * Used in the OPTICS algorithm to store molecules
	 */
	private abstract class OPTICSMoleculeArray
	{
		final OPTICSMolecule[] list;
		int size = 0;

		OPTICSMoleculeArray(int capacity)
		{
			list = new OPTICSMolecule[capacity];
		}

		void add(OPTICSMolecule m)
		{
			list[size++] = m;
		}

		void clear()
		{
			size = 0;
		}
	}

	/**
	 * Used in the OPTICS algorithm to store molecules in a list
	 */
	private class OPTICSMoleculeList extends OPTICSMoleculeArray
	{
		OPTICSMoleculeList(int capacity)
		{
			super(capacity);
		}

		OPTICSMolecule get(int i)
		{
			return list[i];
		}
	}

	/**
	 * Used in the DBSCAN algorithm to store a queue of molecules to process
	 */
	private class OPTICSMoleculeQueue extends OPTICSMoleculeArray
	{
		int next = 0;

		OPTICSMoleculeQueue(int capacity)
		{
			super(capacity);
		}

		void push(OPTICSMolecule m)
		{
			add(m);
		}

		void clear()
		{
			size = next = 0;
		}

		boolean hasNext()
		{
			return next < size;
		}

		public OPTICSMolecule next()
		{
			return list[next++];
		}
	}

	/**
	 * Counter used in DBSCAN
	 */
	private class Counter
	{
		int next, progress, total;

		Counter(int total)
		{
			this.total = total;
		}

		int nextClusterId()
		{
			return ++next;
		}

		/**
		 * Increment the counter and check if processing should stop.
		 *
		 * @return true, if processing should stop
		 */
		boolean increment()
		{
			if (tracker != null)
			{
				tracker.progress(++progress, total);
				return tracker.isEnded();
			}
			return false;
		}

		int getTotalClusters()
		{
			return next;
		}
	}

	/**
	 * Used in the OPTICS algorithm to store the next seed is a priority queue
	 */
	private class OPTICSMoleculePriorityQueue extends OPTICSMoleculeArray
	{
		int next = 0;

		OPTICSMoleculePriorityQueue(int capacity)
		{
			super(capacity);
		}

		void push(OPTICSMolecule m)
		{
			set(m, size++);
			moveUp(m);
		}

		void set(OPTICSMolecule m, int index)
		{
			list[index] = m;
			m.setQueueIndex(index);
		}

		void clear()
		{
			size = next = 0;
		}

		boolean hasNext()
		{
			return next < size;
		}

		public OPTICSMolecule next()
		{
			OPTICSMolecule m = list[next++];
			if (hasNext())
			{
				// Find the next lowest molecule
				int lowest = next;
				for (int i = next + 1; i < size; i++)
				{
					if (opticsComparator.compare(list[i], list[lowest]) < 0)
						lowest = i;
				}
				swap(next, lowest);
			}
			return m;
		}

		private void swap(int i, int j)
		{
			OPTICSMolecule m = list[i];
			set(list[j], i);
			set(m, j);
		}

		public void moveUp(OPTICSMolecule object)
		{
			if (opticsComparator.compare(object, list[next]) < 0)
				swap(next, object.getQueueIndex());
		}
	}

	/**
	 * Used in the OPTICS algorithm
	 */
	private class OPTICSMoleculeGrid
	{
		int resolution;
		final float generatingDistanceE;
		final float binWidth;
		final OPTICSMolecule[][][] grid;
		final OPTICSMolecule[] setOfObjects;
		final int xBins;
		final int yBins;

		OPTICSMoleculeGrid(float generatingDistanceE)
		{
			this.generatingDistanceE = generatingDistanceE;

			final float xrange = maxXCoord - minXCoord;
			final float yrange = maxYCoord - minYCoord;

			if (xrange == 0 && yrange == 0)
			{
				resolution = 1;
				binWidth = 1;
			}
			else
			{
				// Use a higher resolution grid to avoid too many distance comparisons
				resolution = determineResolution(xrange, yrange);

				if (resolution == 0)
				{
					// Handle a resolution of zero. This will happen when the generating distance is very small.
					// In this instance we can use a resolution of 1 but change the bin width to something larger.
					resolution = 1;
					binWidth = determineBinWidth(xrange, yrange);
				}
				else
				{
					// Do not increase the resolution so high we have thousands of blocks
					// and not many expected points.		
					// Determine the number of molecules we would expect in a square block if they are uniform.
					double blockArea = 4 * generatingDistanceE;
					double expected = xcoord.length * blockArea / (xrange * yrange);

					// It is OK if 25-50% of the blocks are full
					int newResolution = 1;

					double target = expected / 0.25;

					// Closest
					//				double minDelta = Math.abs(getNeighbourBlocks(newResolution) - target);
					//				while (newResolution < resolution)
					//				{
					//					double delta = Math.abs(getNeighbourBlocks(newResolution + 1) - target);
					//					if (delta < minDelta)
					//					{
					//						minDelta = delta;
					//						newResolution++;
					//					}
					//					else
					//						break;
					//				}

					// Next size up
					while (newResolution < resolution)
					{
						if (getNeighbourBlocks(newResolution) < target)
							newResolution++;
						else
							break;
					}

					resolution = newResolution;

					//System.out.printf("Expected %.2f [%d]\n", expected, (2 * resolution + 1) * (2 * resolution + 1));

					binWidth = generatingDistanceE / resolution;
				}
			}

			// Assign to a grid
			xBins = 1 + (int) (xrange / binWidth);
			yBins = 1 + (int) (yrange / binWidth);

			OPTICSMolecule[][] linkedListGrid = new OPTICSMolecule[xBins][yBins];
			setOfObjects = new OPTICSMolecule[xcoord.length];
			for (int i = 0; i < xcoord.length; i++)
			{
				final float x = xcoord[i];
				final float y = ycoord[i];
				final int xBin = (int) ((x - minXCoord) / binWidth);
				final int yBin = (int) ((y - minYCoord) / binWidth);
				// Build a single linked list
				final OPTICSMolecule m = new OPTICSMolecule(i, x, y, xBin, yBin, linkedListGrid[xBin][yBin]);
				setOfObjects[i] = m;
				linkedListGrid[xBin][yBin] = m;
			}

			// Convert grid to arrays ...
			grid = new OPTICSMolecule[xBins][yBins][];
			for (int xBin = xBins; xBin-- > 0;)
			{
				for (int yBin = yBins; yBin-- > 0;)
				{
					if (linkedListGrid[xBin][yBin] == null)
						continue;
					int count = 0;
					for (OPTICSMolecule m = linkedListGrid[xBin][yBin]; m != null; m = m.next)
						count++;
					final OPTICSMolecule[] list = new OPTICSMolecule[count];
					for (OPTICSMolecule m = linkedListGrid[xBin][yBin]; m != null; m = m.next)
						list[--count] = m;
					grid[xBin][yBin] = list;
				}
			}
		}

		private int determineResolution(float xrange, float yrange)
		{
			int resolution = 0;
			// What is a good maximum limit for the memory allocation?
			while (getBins(xrange, yrange, generatingDistanceE, resolution + 1) < 100000)
			{
				resolution++;
			}
			//System.out.printf("d=%.3f  [%d]\n", generatingDistanceE, resolution);
			// We handle a resolution of zero in the calling function
			return resolution;
		}

		private float determineBinWidth(float xrange, float yrange)
		{
			float binWidth = generatingDistanceE;
			while (getBins(xrange, yrange, binWidth, 1) > 100000)
			{
				// Dumb implementation that doubles the bin width. A better solution
				// would be to conduct a search for the value with a number of bins close 
				// to the target.
				binWidth *= 2;
			}
			return binWidth;
		}

		private int getBins(float xrange, float yrange, float distance, int resolution)
		{
			final float binWidth = distance / resolution;
			final int nXBins = 1 + (int) (xrange / binWidth);
			final int nYBins = 1 + (int) (yrange / binWidth);
			final int nBins = nXBins * nYBins;
			//System.out.printf("d=%.3f  %d => %d\n", generatingDistanceE, resolution, nBins);
			return nBins;
		}

		private int getNeighbourBlocks(int resolution)
		{
			int size = 2 * resolution + 1;
			return size * size;
		}

		void reset()
		{
			for (int i = setOfObjects.length; i-- > 0;)
				setOfObjects[i].reset();
		}
	}

	/**
	 * Used in the OPTICS algorithm to store the output results
	 */
	private class OPTICSResultList
	{
		final OPTICSOrder[] list;
		int size = 0;

		OPTICSResultList(int capacity)
		{
			list = new OPTICSOrder[capacity];
		}

		/**
		 * Adds the molecule to the results. Send progress to the tracker and checks for a shutdown signal.
		 *
		 * @param m
		 *            the m
		 * @param clusterId
		 *            the cluster id
		 * @return true, if a shutdown signal has been received
		 */
		boolean add(OPTICSMolecule m)
		{
			list[size++] = m.toResult();
			if (tracker != null)
			{
				tracker.progress(size, list.length);
				return tracker.isEnded();
			}
			return false;
		}
	}

	/**
	 * Input arrays are modified
	 * 
	 * @param xcoord
	 * @param ycoord
	 * @param bounds
	 * @throws IllegalArgumentException
	 *             if results are null or empty
	 */
	public OPTICSManager(float[] xcoord, float[] ycoord, Rectangle bounds)
	{
		super(xcoord, ycoord, bounds);
	}

	/**
	 * Compute the core radius for each point to have n closest neighbours and the minimum reachability distance of a
	 * point from another core point.
	 * <p>
	 * This is an implementation of the OPTICS method. Mihael Ankerst, Markus M Breunig, Hans-Peter Kriegel, and Jorg
	 * Sander. Optics: ordering points to identify the clustering structure. In ACM Sigmod Record, volume 28, pages
	 * 49–60. ACM, 1999.
	 * <p>
	 * The returned results are the output of {@link #extractDBSCANClustering(OPTICSOrder[], float)} with the
	 * configured generating distance. Note that the generating distance is set using
	 * {@link #computeGeneratingDistance(int)}. The distance is stored in the results.
	 * <p>
	 * The tracker can be used to follow progress (see {@link #setTracker(TrackProgress)}).
	 *
	 * @param minPts
	 *            the min points for a core object (recommended range 10-20)
	 * @return the results
	 */
	public OPTICSResult optics(int minPts)
	{
		return optics(0, minPts, true);
	}

	/**
	 * Compute the core radius for each point to have n closest neighbours and the minimum reachability distance of a
	 * point from another core point.
	 * <p>
	 * This is an implementation of the OPTICS method. Mihael Ankerst, Markus M Breunig, Hans-Peter Kriegel, and Jorg
	 * Sander. Optics: ordering points to identify the clustering structure. In ACM Sigmod Record, volume 28, pages
	 * 49–60. ACM, 1999.
	 * <p>
	 * The returned results are the output of {@link #extractDBSCANClustering(OPTICSOrder[], float)} with the
	 * configured generating distance. Note that the generating distance may have been modified if invalid. If it is
	 * not strictly positive or not finite then it is set using {@link #computeGeneratingDistance(int)}. If it is larger
	 * than the data range allows it is set to the maximum distance that can be computed for the data range. If the data
	 * are colocated the distance is set to 1. The distance is stored in the results.
	 * <p>
	 * The tracker can be used to follow progress (see {@link #setTracker(TrackProgress)}).
	 *
	 * @param generatingDistanceE
	 *            the generating distance E (set to zero to auto calibrate)
	 * @param minPts
	 *            the min points for a core object (recommended range 10-20)
	 * @return the results
	 */
	public OPTICSResult optics(float generatingDistanceE, int minPts)
	{
		return optics(generatingDistanceE, minPts, true);
	}

	/**
	 * Compute the core radius for each point to have n closest neighbours and the minimum reachability distance of a
	 * point from another core point.
	 * <p>
	 * This is an implementation of the OPTICS method. Mihael Ankerst, Markus M Breunig, Hans-Peter Kriegel, and Jorg
	 * Sander. Optics: ordering points to identify the clustering structure. In ACM Sigmod Record, volume 28, pages
	 * 49–60. ACM, 1999.
	 * <p>
	 * The returned results are the output of {@link #extractDBSCANClustering(OPTICSOrder[], float)} with the
	 * configured generating distance. Note that the generating distance may have been modified if invalid. If it is
	 * not strictly positive or not finite then it is set using {@link #computeGeneratingDistance(int)}. If it is larger
	 * than the data range allows it is set to the maximum distance that can be computed for the data range. If the data
	 * are colocated the distance is set to 1. The distance is stored in the results.
	 * <p>
	 * This creates a large memory structure. It can be held in memory for re-use when using a different number of min
	 * points. The tracker can be used to follow progress (see {@link #setTracker(TrackProgress)}).
	 *
	 * @param generatingDistanceE
	 *            the generating distance E (set to zero to auto calibrate)
	 * @param minPts
	 *            the min points for a core object (recommended range 10-20)
	 * @param clearMemory
	 *            Set to true to clear the memory structure
	 * @return the results (or null if the algorithm was stopped using the tracker)
	 */
	public OPTICSResult optics(float generatingDistanceE, int minPts, boolean clearMemory)
	{
		if (minPts < 1)
			minPts = 1;

		initialiseOPTICS(generatingDistanceE, minPts);

		// The distance may be updated
		generatingDistanceE = grid.generatingDistanceE;

		if (tracker != null)
		{
			tracker.log("Running OPTICS ...");
			tracker.progress(0, xcoord.length);
		}

		// Note: The method and variable names used in this function are designed to match 
		// the pseudocode implementation from the 1999 OPTICS paper.
		// The generating distance (E) used in the paper is the maximum distance at which cluster
		// centres will be formed. This implementation uses the squared distance to avoid sqrt() 
		// function calls.
		final float e = generatingDistanceE * generatingDistanceE;

		final int size = xcoord.length;
		OPTICSMolecule[] setOfObjects = grid.setOfObjects;

		OPTICSMoleculePriorityQueue orderSeeds = new OPTICSMoleculePriorityQueue(size);
		OPTICSResultList results = new OPTICSResultList(size);

		for (int i = 0; i < size; i++)
		{
			final OPTICSMolecule object = setOfObjects[i];
			if (object.isNotProcessed())
			{
				if (expandClusterOrder(object, e, minPts, results, orderSeeds))
					break;
			}
		}

		boolean stopped = false;
		if (tracker != null)
		{
			stopped = tracker.isEnded();
			tracker.progress(1.0);

			if (stopped)
				tracker.log("Aborted OPTICS");
		}

		OPTICSResult optics = null;
		if (!stopped)
		{
			optics = new OPTICSResult(minPts, generatingDistanceE, results.list);
			final int nClusters = optics.extractDBSCANClustering(generatingDistanceE);
			if (tracker != null)
			{
				tracker.log("Finished OPTICS: " + Utils.pleural(nClusters, "Cluster"));
			}
		}

		if (clearMemory)
			clearMemory();

		return optics;
	}

	private OPTICSMoleculeGrid grid;
	private OPTICSMoleculeList neighbours;
	private float[] floatArray;

	/**
	 * Initialise the memory structure for the OPTICS algorithm. This can be cached if the generatingDistanceE does not
	 * change.
	 *
	 * @param generatingDistanceE
	 *            the generating distance E
	 * @param minPts
	 *            the min points for a core object
	 */
	private void initialiseOPTICS(float generatingDistanceE, int minPts)
	{
		// Ensure the distance is valid
		generatingDistanceE = getWorkingGeneratingDistance(generatingDistanceE, minPts);

		// Compare to the existing grid
		if (grid == null || grid.generatingDistanceE != generatingDistanceE)
		{
			if (tracker != null)
				tracker.log("Initialising OPTICS ...");

			grid = new OPTICSMoleculeGrid(generatingDistanceE);

			final int size = xcoord.length;
			neighbours = new OPTICSMoleculeList(size);
		}
		else
		{
			// This is the same distance so the objects can be reused
			grid.reset();
			neighbours.clear();
		}

		if (floatArray == null || floatArray.length < minPts)
			floatArray = new float[minPts];
	}

	/**
	 * Gets the working generating distance. Ensure the generating distance is not too high for the data range. Also set
	 * it to the max value if the generating distance is not valid.
	 *
	 * @param generatingDistanceE
	 *            the generating distance E
	 * @param minPts
	 *            the min points for a core object
	 * @return the working generating distance
	 */
	private float getWorkingGeneratingDistance(float generatingDistanceE, int minPts)
	{
		final float xrange = maxXCoord - minXCoord;
		final float yrange = maxYCoord - minYCoord;
		if (xrange == 0 && yrange == 0)
			// Occurs when only 1 point or colocated data. A distance of zero is invalid so set to 1.
			return 1;

		// If not set then compute the generating distance
		if (!Maths.isFinite(generatingDistanceE) || generatingDistanceE <= 0)
			return computeGeneratingDistance(minPts);

		// Compute the upper distance we can expect
		double maxDistance = Math.sqrt(xrange * xrange + yrange * yrange);
		if (generatingDistanceE > maxDistance)
			return (float) maxDistance;

		// Stick to the user input
		return generatingDistanceE;
	}

	/**
	 * Checks for search algorithm structures stored in memory.
	 *
	 * @return true, if successful
	 */
	public boolean hasMemory()
	{
		return grid != null;
	}

	/**
	 * Clear memory used by the search algorithm
	 */
	public void clearMemory()
	{
		grid = null;
		neighbours = null;
		floatArray = null;
	}

	/**
	 * Expand cluster order.
	 *
	 * @param object
	 *            the object
	 * @param e
	 *            the generating distance (squared)
	 * @param minPts
	 *            the min points for a core object
	 * @param orderedFile
	 *            the results
	 * @param orderSeeds
	 *            the order seeds
	 * @return true, if the algorithm has received a shutdown signal
	 */
	private boolean expandClusterOrder(OPTICSMolecule object, float e, int minPts, OPTICSResultList orderedFile,
			OPTICSMoleculePriorityQueue orderSeeds)
	{
		// TODO: Re-write the algorithm so that each connected cluster is generated
		// in order of reachability distance

		// Note: The original paper just processes the next unprocessed object in an UNDEFINED order.
		// However once started the remaining neighbours are processed
		// in order of the reachability distance, which is equal to or larger
		// than the most recent core distance. But the subsequent objects visited may
		// have a lower core distance than the first (due to the arbitrary start point),
		// i.e. they are in a region of higher density. This also means that: 
		// 1. Points that have already been processed may have been reachable from a closer
		// point (i.e. from another direction) and so should have a lower reachability 
		// distance.
		// 2. Points which are ignored since they have been processed may have a lower reachability
		// distance.
		// This means that the output reachability distance is only indicative of the walk the algorithm
		// took through the data. It should not be used to perform actual clustering (e.g. using 
		// the pseudocode ExtractDBSCAN-Clustering from the original paper). The authors even note 
		// this in their paper: 
		// "The clustering created from a cluster-ordered data set by Ex-
		// tractDBSCAN-Clustering is nearly indistinguishable from a
		// clustering created by DBSCAN. Only some border objects may
		// be missed when extracted by the algorithm ExtractDBSCAN-
		// Clustering if they were processed by the algorithm OPTICS be-
		// fore a core object of the corresponding cluster had been found."

		// To overcome this we could switch the point at which points are ignored. 
		// The orderSeeds contains all the reachable points within the current connected cluster.
		// We ensure that the reachable distance is updated even if the point has been processed.
		// We just do not repeat process the neighbours of a point that has been processed.

		findNeighbours(minPts, object, e);
		object.markProcessed();
		object.setCoreDistance(minPts, neighbours);
		if (orderedFile.add(object))
			return true;

		if (object.coreDistance != UNDEFINED)
		{
			// Create seed-list for further expansion.
			// The next counter is used to ensure we sort only the remaining entries in the seed list.
			orderSeeds.clear();
			update(orderSeeds, neighbours, object);

			while (orderSeeds.hasNext())
			{
				object = orderSeeds.next();
				findNeighbours(minPts, object, e);
				object.markProcessed();
				object.setCoreDistance(minPts, neighbours);
				if (orderedFile.add(object))
					return true;

				if (object.coreDistance != UNDEFINED)
					update(orderSeeds, neighbours, object);
			}
		}
		return false;
	}

	/**
	 * Find neighbours closer than the generating distance. The neighbours are written to the working memory store. The
	 * distances are stored in the objects encountered.
	 * <p>
	 * If the number of points is definitely below the minimum number of points then no distances are computed (to save
	 * time).
	 * <p>
	 * The neighbours includes the actual point in the list of neighbours (where the distance would be 0).
	 *
	 * @param minPts
	 *            the min pts
	 * @param object
	 *            the object
	 * @param e
	 *            the generating distance
	 */
	private void findNeighbours(int minPts, OPTICSMolecule object, float e)
	{
		final int xBin = object.xBin;
		final int yBin = object.yBin;

		neighbours.clear();

		// Pre-compute range
		final int resolution = grid.resolution;
		final int minx = Math.max(xBin - resolution, 0);
		final int maxx = Math.min(xBin + resolution + 1, grid.xBins);
		final int miny = Math.max(yBin - resolution, 0);
		final int maxy = Math.min(yBin + resolution + 1, grid.yBins);

		// Count if there are enough neighbours
		int count = minPts;
		counting: for (int x = minx; x < maxx; x++)
		{
			final OPTICSMolecule[][] column = grid.grid[x];
			for (int y = miny; y < maxy; y++)
			{
				final OPTICSMolecule[] list = column[y];
				if (list != null)
				{
					count -= list.length;
					if (count <= 0)
						break counting;
				}
			}
		}

		if (count > 0)
		{
			// Not a core point so do not compute distances
			//System.out.println("Skipping distance computation (not a core point)");
			return;
		}

		// Compute distances
		for (int x = minx; x < maxx; x++)
		{
			final OPTICSMolecule[][] column = grid.grid[x];
			for (int y = miny; y < maxy; y++)
			{
				final OPTICSMolecule[] list = column[y];
				if (list != null)
				{
					for (int i = list.length; i-- > 0;)
					{
						final float d = object.distance2(list[i]);
						if (d <= e)
						{
							// Build a list of all the neighbours and their working distance
							final OPTICSMolecule otherObject = list[i];
							otherObject.d = d;
							neighbours.add(otherObject);
						}
					}
				}
			}
		}
	}

	/**
	 * Update the ordered seeds with the neighbours of the current object. Set the reachability distance and reorder.
	 *
	 * @param orderSeeds
	 *            the order seeds
	 * @param neighbours
	 *            the neighbours
	 * @param centreObject
	 *            the object
	 * @param next
	 */
	private void update(OPTICSMoleculePriorityQueue orderSeeds, OPTICSMoleculeList neighbours,
			OPTICSMolecule centreObject)
	{
		final float c_dist = centreObject.coreDistance;
		for (int i = neighbours.size; i-- > 0;)
		{
			final OPTICSMolecule object = neighbours.get(i);
			if (object.isNotProcessed())
			{
				final float new_r_dist = max(c_dist, object.d);
				if (object.reachabilityDistance == UNDEFINED)
				{
					// This is new so add it to the list
					object.reachabilityDistance = new_r_dist;
					object.predecessor = centreObject.id;
					orderSeeds.push(object);
				}
				else
				{
					// This is already in the list
					// Here is the difference between OPTICS and DBSCAN.
					// In this case the order of points to process can be changed based on the reachability.
					if (new_r_dist < object.reachabilityDistance)
					{
						object.reachabilityDistance = new_r_dist;
						object.predecessor = centreObject.id;
						orderSeeds.moveUp(object);
					}
				}
			}
		}
	}

	/**
	 * Find the max fast (ignore the possibility of NaN or infinity
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the max
	 */
	public static float max(float a, float b)
	{
		return (a >= b) ? a : b;
	}

	/**
	 * Compute the OPTICS generating distance assuming a uniform distribution in the data space.
	 *
	 * @param minPts
	 *            the min points for a core object
	 * @return the generating distance
	 */
	public float computeGeneratingDistance(int minPts)
	{
		return computeGeneratingDistance(minPts, area, xcoord.length);
	}

	/**
	 * Compute the OPTICS generating distance assuming a uniform distribution in the data space.
	 *
	 * @param minPts
	 *            the min points for a core object
	 * @param area
	 *            the area of the data space
	 * @param N
	 *            the number of points in the data space
	 * @return the generating distance
	 */
	public static float computeGeneratingDistance(int minPts, double area, int N)
	{
		// Taken from section 4.1 of the OPTICS paper.

		// Number of dimensions
		// d = 2;
		// Volume of the data space (DS)
		double volumeDS = area;
		// Expected k-nearest-neighbours
		int k = minPts;

		// Compute the volume of the hypersphere required to contain k neighbours,
		// assuming a uniform spread.
		// VolumeS = (VolumeDS/N) x k

		double volumeS = (volumeDS / N) * k;

		// Note Volume S(r) for a 2D hypersphere = pi * r^2
		return (float) Math.sqrt(volumeS / Math.PI);
	}

	/**
	 * Compute the core points as any that have more than min points within the distance. All points within the radius
	 * are reachable points that are processed in turn. Any new core points expand the search space.
	 * <p>
	 * This is an implementation of the DBSCAN method. Ester, Martin; Kriegel, Hans-Peter; Sander, Jörg; Xu, Xiaowei
	 * (1996). Simoudis, Evangelos; Han, Jiawei; Fayyad, Usama M., eds. A density-based algorithm for discovering
	 * clusters in large spatial databases with noise. Proceedings of the Second International Conference on Knowledge
	 * Discovery and Data Mining (KDD-96). AAAI Press. pp. 226–231.
	 * <p>
	 * Note that the generating distance may have been modified if invalid. If it is not strictly positive or not finite
	 * then it is set using {@link #computeGeneratingDistance(int)}. If it is larger than the data range allows it is
	 * set to the maximum distance that can be computed for the data range. If the data are colocated the distance is
	 * set to 1. The distance is stored in the results.
	 * <p>
	 * The tracker can be used to follow progress (see {@link #setTracker(TrackProgress)}).
	 *
	 * @param generatingDistanceE
	 *            the generating distance E (set to zero to auto calibrate)
	 * @param minPts
	 *            the min points for a core object
	 * @return the results (or null if the algorithm was stopped using the tracker)
	 */
	public DBSCANResult dbscan(float generatingDistanceE, int minPts)
	{
		return dbscan(generatingDistanceE, minPts, true);
	}

	/**
	 * Compute the core points as any that have more than min points within the distance. All points within the radius
	 * are reachable points that are processed in turn. Any new core points expand the search space.
	 * <p>
	 * This is an implementation of the DBSCAN method. Ester, Martin; Kriegel, Hans-Peter; Sander, Jörg; Xu, Xiaowei
	 * (1996). Simoudis, Evangelos; Han, Jiawei; Fayyad, Usama M., eds. A density-based algorithm for discovering
	 * clusters in large spatial databases with noise. Proceedings of the Second International Conference on Knowledge
	 * Discovery and Data Mining (KDD-96). AAAI Press. pp. 226–231.
	 * <p>
	 * Note that the generating distance may have been modified if invalid. If it is not strictly positive or not finite
	 * then it is set using {@link #computeGeneratingDistance(int)}. If it is larger than the data range allows it is
	 * set to the maximum distance that can be computed for the data range. If the data are colocated the distance is
	 * set to 1. The distance is stored in the results.
	 * <p>
	 * This creates a large memory structure. It can be held in memory for re-use when using a different number of min
	 * points. The tracker can be used to follow progress (see {@link #setTracker(TrackProgress)}).
	 *
	 * @param generatingDistanceE
	 *            the generating distance E (set to zero to auto calibrate)
	 * @param minPts
	 *            the min points for a core object
	 * @param clearMemory
	 *            Set to true to clear the memory structure
	 * @return the results (or null if the algorithm was stopped using the tracker)
	 */
	public DBSCANResult dbscan(float generatingDistanceE, int minPts, boolean clearMemory)
	{
		if (minPts < 1)
			minPts = 1;

		// Use the same structure as OPTICS (since the algorithm is very similar)
		initialiseOPTICS(generatingDistanceE, minPts);

		// *************
		// Note:
		// Since DBSCAN just does counting in a neighbourhood we could change the implementation
		// to use a high resolution 2D histogram. This can be searched using a circular mask.
		// Only the points in the border cells of the circular mask need to be have a distance 
		// computation performed. If the resolution is high enough (relative to the generating 
		// distance) then the distance computation can be omitted altogether. This may have very 
		// little effect on the final output clusters.
		// *************

		// The distance may be updated
		generatingDistanceE = grid.generatingDistanceE;

		if (tracker != null)
		{
			tracker.log("Running DBSCAN ...");
			tracker.progress(0, xcoord.length);
		}

		// The generating distance (E) used in the paper is the maximum distance at which cluster
		// centres will be formed. This implementation uses the squared distance to avoid sqrt() 
		// function calls.
		final float e = generatingDistanceE * generatingDistanceE;

		final int size = xcoord.length;
		OPTICSMolecule[] setOfObjects = grid.setOfObjects;

		// Working storage
		OPTICSMoleculeQueue seeds = new OPTICSMoleculeQueue(size);
		Counter counter = new Counter(size);

		for (int i = 0; i < size; i++)
		{
			final OPTICSMolecule object = setOfObjects[i];
			if (object.isNotProcessed())
			{
				if (expandCluster(object, e, minPts, counter, seeds))
					break;
			}
		}

		boolean stopped = false;
		if (tracker != null)
		{
			stopped = tracker.isEnded();
			tracker.progress(1.0);

			if (stopped)
				tracker.log("Aborted OPTICS");
		}

		DBSCANResult dbscanResult = null;
		if (!stopped)
		{
			// Convert the working data structure to the output
			DBSCANOrder[] dbscanOrder = new DBSCANOrder[size];
			for (int i = 0; i < size; i++)
				dbscanOrder[i] = setOfObjects[i].toDBSCANResult();
			dbscanResult = new DBSCANResult(minPts, generatingDistanceE, dbscanOrder);
			if (tracker != null)
			{
				tracker.log("Finished DBSCAN: " + Utils.pleural(counter.getTotalClusters(), "Cluster"));
			}
		}

		if (clearMemory)
			clearMemory();

		return dbscanResult;
	}

	private boolean expandCluster(OPTICSMolecule object, float e, int minPts, Counter counter,
			OPTICSMoleculeQueue seeds)
	{
		findNeighbours(minPts, object, e);
		if (counter.increment())
			return true;

		object.markProcessed();
		object.setNumberOfPoints(neighbours.size);
		if (neighbours.size >= minPts)
		{
			// New cluster
			int clusterId = counter.nextClusterId();
			object.setClusterOrigin(clusterId);

			// Expand through the neighbours
			seeds.clear();
			update(seeds, neighbours, clusterId);

			while (seeds.hasNext())
			{
				object = seeds.next();
				findNeighbours(minPts, object, e);
				if (counter.increment())
					return true;

				object.markProcessed();
				object.setNumberOfPoints(neighbours.size);
				if (neighbours.size >= minPts)
					update(seeds, neighbours, clusterId);
			}
		}

		return false;
	}

	/**
	 * Update the set of points to search with any as yet unvisited points from the list of neighbours. Set the cluster
	 * Id of any unassigned points.
	 *
	 * @param pointsToSearch
	 *            the points to search
	 * @param neighbours
	 *            the neighbours
	 * @param clusterId
	 *            the cluster id
	 */
	private void update(OPTICSMoleculeQueue pointsToSearch, OPTICSMoleculeList neighbours, int clusterId)
	{
		for (int i = neighbours.size; i-- > 0;)
		{
			final OPTICSMolecule object = neighbours.get(i);
			if (object.isNotInACluster())
			{
				object.setClusterMember(clusterId);

				// Ensure that the search is not repeated
				if (object.isNotProcessed())
					pointsToSearch.push(object);
			}
		}
	}
}
