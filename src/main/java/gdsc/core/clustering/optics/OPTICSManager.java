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
import java.util.EnumSet;

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
	 * Options for the algorithms
	 */
	public enum Option
	{
		/**
		 * Flag to indicate that memory structures should be cached. Set this when it is intended to call multiple
		 * clustering methods.
		 */
		CACHE,
		/**
		 * Flag to indicate that a high resolution 2D grid should be used. This has performance benefits when the number
		 * of molecules is high since unnecessary distance computations can be avoided. This required more memory.
		 */
		HIGH_RESOLUTION,
		/**
		 * Flag to indicate that radial processing should be used on the 2D grid should be used. This has performance
		 * benefits when the resolution is high since some distance computations can be assumed.
		 */
		RADIAL_PROCESSING;
	}

	private EnumSet<Option> options = EnumSet.noneOf(Option.class);

	/**
	 * Sets the option.
	 *
	 * @param option
	 *            the new option
	 */
	public void setOption(Option option)
	{
		options.add(option);
	}

	/**
	 * Sets the options.
	 *
	 * @param options
	 *            the new options
	 */
	public void setOptions(Option... options)
	{
		for (Option option : options)
			this.options.add(option);
	}
	
	/**
	 * Sets the options.
	 *
	 * @param options
	 *            the new options
	 */
	public void setOptions(EnumSet<Option> options)
	{
		if (options == null)
			throw new IllegalArgumentException("Options cannot be null");
		this.options = options;
	}

	/**
	 * Gets the options.
	 *
	 * @return the options
	 */
	public EnumSet<Option> getOptions()
	{
		return options;
	}

	/**
	 * Used in the DBSCAN/OPTICS algorithms to represent 2D molecules.
	 */
	private class Molecule
	{
		int id;
		float x, y;
		// Used to construct a single linked list of molecules
		public Molecule next = null;

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

		/**
		 * Reset for fresh processing.
		 */
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

		public OPTICSOrder toOPTICSResult()
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
		public void setCoreDistance(int minPts, MoleculeList neighbours)
		{
			processed = true;

			final int size = neighbours.size;
			if (size < minPts)
				// Not a core point
				return;

			final Molecule[] list = neighbours.list;

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

	/**
	 * Used in the algorithms to store molecules
	 */
	private abstract class MoleculeArray
	{
		final Molecule[] list;
		int size = 0;

		MoleculeArray(int capacity)
		{
			list = new Molecule[capacity];
		}

		void add(Molecule m)
		{
			list[size++] = m;
		}

		void clear()
		{
			size = 0;
		}
	}

	/**
	 * Used in the algorithms to store molecules in an indexable list
	 */
	private class MoleculeList extends MoleculeArray
	{
		MoleculeList(int capacity)
		{
			super(capacity);
		}

		Molecule get(int i)
		{
			return list[i];
		}

		void add(Molecule[] molecules)
		{
			System.arraycopy(molecules, 0, list, size, molecules.length);
			size += molecules.length;
		}
	}

	/**
	 * Used in the DBSCAN algorithm to store a queue of molecules to process
	 */
	private class MoleculeQueue extends MoleculeArray
	{
		int next = 0;

		MoleculeQueue(int capacity)
		{
			super(capacity);
		}

		void push(Molecule m)
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

		public Molecule next()
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

	private static class OPTICSComparator implements Comparator<Molecule>
	{
		public int compare(Molecule o1, Molecule o2)
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
	 * Used in the OPTICS algorithm to store the next seed is a priority queue
	 */
	private class OPTICSMoleculePriorityQueue extends MoleculeArray
	{
		int next = 0;

		OPTICSMoleculePriorityQueue(int capacity)
		{
			super(capacity);
		}

		void push(Molecule m)
		{
			set(m, size++);
			moveUp(m);
		}

		void set(Molecule m, int index)
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

		public Molecule next()
		{
			Molecule m = list[next++];
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
			Molecule m = list[i];
			set(list[j], i);
			set(m, j);
		}

		public void moveUp(Molecule object)
		{
			if (opticsComparator.compare(object, list[next]) < 0)
				swap(next, object.getQueueIndex());
		}
	}

	/**
	 * Used in the OPTICS algorithm
	 */
	private abstract class MoleculeSpace
	{
		final float generatingDistanceE;
		final Molecule[] setOfObjects;
		final int size = xcoord.length;
		// Working storage for find neighbours
		final MoleculeList neighbours = new MoleculeList(size);

		MoleculeSpace(float generatingDistanceE)
		{
			this.generatingDistanceE = generatingDistanceE;
			setOfObjects = generate();
		}

		/**
		 * Generate the molecule space. Return the list of molecules that will be processed.
		 *
		 * @return the molecule list
		 */
		abstract Molecule[] generate();

		/**
		 * Reset all the molecules for fresh processing.
		 */
		void reset()
		{
			for (int i = setOfObjects.length; i-- > 0;)
				setOfObjects[i].reset();
		}

		/**
		 * Find neighbours closer than the generating distance. The neighbours are written to the working memory store.
		 * <p>
		 * If the number of points is definitely below the minimum number of points then no distances are computed (to
		 * save
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
		abstract void findNeighbours(int minPts, Molecule object, float e);

		/**
		 * Find neighbours closer than the generating distance. The neighbours are written to the working memory store.
		 * The
		 * distances are stored in the objects encountered.
		 * <p>
		 * If the number of points is definitely below the minimum number of points then no distances are computed (to
		 * save
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
		abstract void findNeighboursAndDistances(int minPts, Molecule object, float e);
	}

	/**
	 * Store molecules in a 2D grid
	 */
	private class GridMoleculeSpace extends MoleculeSpace
	{
		int resolution;
		float binWidth;
		int xBins;
		int yBins;
		Molecule[][] grid;
		final int[] fastForward;

		GridMoleculeSpace(float generatingDistanceE)
		{
			super(generatingDistanceE);

			// Traverse the grid and store the index to the next position that contains data
			int index = grid.length;
			fastForward = new int[index];
			for (int i = index; i-- > 0;)
			{
				fastForward[i] = index;
				if (grid[i] != null)
					index = i;
			}
		}

		Molecule[] generate()
		{
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
					adjustResolution(xrange, yrange);
					binWidth = generatingDistanceE / resolution;
				}
			}

			// Assign to a grid
			xBins = 1 + (int) (xrange / binWidth);
			yBins = 1 + (int) (yrange / binWidth);

			Molecule[][] linkedListGrid = new Molecule[xBins][yBins];
			Molecule[] setOfObjects = new Molecule[xcoord.length];
			for (int i = 0; i < xcoord.length; i++)
			{
				final float x = xcoord[i];
				final float y = ycoord[i];
				final int xBin = (int) ((x - minXCoord) / binWidth);
				final int yBin = (int) ((y - minYCoord) / binWidth);
				// Build a single linked list
				final Molecule m = new Molecule(i, x, y, xBin, yBin, linkedListGrid[xBin][yBin]);
				setOfObjects[i] = m;
				linkedListGrid[xBin][yBin] = m;
			}

			// Convert grid to arrays ...
			grid = new Molecule[xBins * yBins][];
			for (int yBin = yBins, index = 0; yBin-- > 0;)
			{
				for (int xBin = xBins; xBin-- > 0; index++)
				{
					if (linkedListGrid[xBin][yBin] == null)
						continue;
					int count = 0;
					for (Molecule m = linkedListGrid[xBin][yBin]; m != null; m = m.next)
						count++;
					final Molecule[] list = new Molecule[count];
					for (Molecule m = linkedListGrid[xBin][yBin]; m != null; m = m.next)
						list[--count] = m;
					grid[index] = list;
				}
			}

			return setOfObjects;
		}

		int determineResolution(float xrange, float yrange)
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

		void adjustResolution(final float xrange, final float yrange)
		{
			if (options.contains(Option.HIGH_RESOLUTION))
				return;

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

		/*
		 * (non-Javadoc)
		 * 
		 * @see gdsc.core.clustering.optics.OPTICSManager.MoleculeSpace#findNeighbours(int,
		 * gdsc.core.clustering.optics.OPTICSManager.Molecule, float)
		 */
		void findNeighbours(int minPts, Molecule object, float e)
		{
			boolean noFF = true;

			// Match findNeighboursAndDistances(minPts, object, e);
			// But do not store the distances

			final int xBin = object.xBin;
			final int yBin = object.yBin;

			neighbours.clear();

			// Pre-compute range
			final int minx = Math.max(xBin - resolution, 0);
			final int maxx = Math.min(xBin + resolution + 1, xBins);
			final int miny = Math.max(yBin - resolution, 0);
			final int maxy = Math.min(yBin + resolution + 1, yBins);

			// Count if there are enough neighbours
			int count = minPts;
			counting: for (int y = miny; y < maxy; y++)
			{
				if (noFF)
				{
					for (int x = minx, index = y * xBins + minx; x < maxx; x++, index++)
					{
						if (grid[index] != null)
						{
							count -= grid[index].length;
							if (count <= 0)
								break counting;
						}
					}
				}
				else
				{
					// Use fast-forward to skip to the next position with data
					int index = getIndex(minx, y);
					if (grid[index] == null)
						index = fastForward[index];
					int endIndex = getIndex(maxx, y);
					while (index < endIndex)
					{
						count -= grid[index].length;
						if (count <= 0)
							break counting;
						index = fastForward[index];
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
			for (int y = miny; y < maxy; y++)
			{
				if (noFF)
				{
					for (int x = minx, index = y * xBins + minx; x < maxx; x++, index++)
					{
						if (grid[index] != null)
						{
							final Molecule[] list = grid[index];
							for (int i = list.length; i-- > 0;)
							{
								if (object.distance2(list[i]) <= e)
								{
									// Build a list of all the neighbours
									neighbours.add(list[i]);
								}
							}
						}
					}
				}
				else
				{
					// Use fast-forward to skip to the next position with data
					int index = getIndex(minx, y);
					if (grid[index] == null)
						index = fastForward[index];
					int endIndex = getIndex(maxx, y);
					while (index < endIndex)
					{
						final Molecule[] list = grid[index];
						for (int i = list.length; i-- > 0;)
						{
							if (object.distance2(list[i]) <= e)
							{
								// Build a list of all the neighbours
								neighbours.add(list[i]);
							}
						}
						index = fastForward[index];
					}
				}
			}
		}

		int getIndex(final int x, final int y)
		{
			return y * xBins + x;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see gdsc.core.clustering.optics.OPTICSManager.MoleculeSpace#findNeighboursAndDistances(int,
		 * gdsc.core.clustering.optics.OPTICSManager.Molecule, float)
		 */
		void findNeighboursAndDistances(int minPts, Molecule object, float e)
		{
			boolean noFF = true;

			final int xBin = object.xBin;
			final int yBin = object.yBin;

			neighbours.clear();

			// Pre-compute range
			final int minx = Math.max(xBin - resolution, 0);
			final int maxx = Math.min(xBin + resolution + 1, xBins);
			final int miny = Math.max(yBin - resolution, 0);
			final int maxy = Math.min(yBin + resolution + 1, yBins);

			// Count if there are enough neighbours
			int count = minPts;
			counting: for (int y = miny; y < maxy; y++)
			{
				if (noFF)
				{
					for (int x = minx, index = y * xBins + minx; x < maxx; x++, index++)
					{
						if (grid[index] != null)
						{
							count -= grid[index].length;
							if (count <= 0)
								break counting;
						}
					}
				}
				else
				{
					// Use fast-forward to skip to the next position with data
					int index = getIndex(minx, y);
					if (grid[index] == null)
						index = fastForward[index];
					int endIndex = getIndex(maxx, y);
					while (index < endIndex)
					{
						count -= grid[index].length;
						if (count <= 0)
							break counting;
						index = fastForward[index];
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
			for (int y = miny; y < maxy; y++)
			{
				if (noFF)
				{
					for (int x = minx, index = y * xBins + minx; x < maxx; x++, index++)
					{
						if (grid[index] != null)
						{
							final Molecule[] list = grid[index];
							for (int i = list.length; i-- > 0;)
							{
								final float d = object.distance2(list[i]);
								if (d <= e)
								{
									// Build a list of all the neighbours and their working distance
									final Molecule otherObject = list[i];
									otherObject.d = d;
									neighbours.add(otherObject);
								}
							}
						}
					}
				}
				else
				{
					// Use fast-forward to skip to the next position with data
					int index = getIndex(minx, y);
					if (grid[index] == null)
						index = fastForward[index];
					int endIndex = getIndex(maxx, y);
					while (index < endIndex)
					{
						final Molecule[] list = grid[index];
						for (int i = list.length; i-- > 0;)
						{
							final float d = object.distance2(list[i]);
							if (d <= e)
							{
								// Build a list of all the neighbours and their working distance
								final Molecule otherObject = list[i];
								otherObject.d = d;
								neighbours.add(otherObject);
							}
						}
						index = fastForward[index];
					}
				}
			}
		}
	}

	/**
	 * Store start and end x for each strip in the radial mask
	 */
	private class Offset
	{
		final int start;
		final int startInternal;
		final int endInternal;
		final int end;

		Offset(int start, int startInternal, int endInternal, int end)
		{
			this.start = start;
			this.startInternal = startInternal;
			this.endInternal = endInternal;
			this.end = end;
		}
	}

	/**
	 * Store molecules in a high resolution 2D grid and perform distance computation
	 */
	private class RadialMoleculeSpace extends GridMoleculeSpace
	{
		Offset[] offset;

		RadialMoleculeSpace(float generatingDistanceE)
		{
			super(generatingDistanceE);
		}

		Molecule[] generate()
		{
			// Generate the grid
			Molecule[] m = super.generate();

			// TODO - Build a search space that can use a circular mask to only search over the required 
			// points in the 2D grid
			int size = 2 * resolution + 1;
			offset = new Offset[size];

			// TODO find the internal start and end:
			// Any edge point that is only 8-connected to the row below creates 
			// the requirement for extra internal cells to ensure we have a 4 connected edge
			// 
			// .....X
			// ....Xx
			// ....X
			// ....Xx
			// .....X     x are extra internal points

			for (int i = 0; i < resolution; i++)
			{
				offset[i] = new Offset(-resolution, 0, 0, resolution + 1);
			}

			// The central row cannot have more than 1 pixel internal 
			offset[resolution] = new Offset(-resolution, 0, 0, resolution + 1);
			//offset[resolution] = new Offset(-resolution, -resolution + 1, resolution, resolution + 1);

			// Mirror
			for (int i = 0, j = offset.length - 1; i < resolution; i++, j--)
			{
				offset[j] = offset[i];
			}

			return m;
		}

		@Override
		int determineResolution(float xrange, float yrange)
		{
			// TODO - determine a good resolution for the given generating distance

			return super.determineResolution(xrange, yrange);
		}

		@Override
		void adjustResolution(float xrange, float yrange)
		{
			// TODO - prevent the resolution from being reduced too small 

			super.adjustResolution(xrange, yrange);
		}

		void findNeighbours(int minPts, Molecule object, float e)
		{
			if (true)
			{
				super.findNeighbours(minPts, object, e);
				return;
			}

			final int xBin = object.xBin;
			final int yBin = object.yBin;

			neighbours.clear();

			// TODO - Use a circle mask over the grid to enumerate the correct cells
			// Only compute distances at the edge of the mask 

			// Pre-compute range
			final int miny = Math.max(yBin - resolution, 0);
			final int maxy = Math.min(yBin + resolution + 1, yBins);
			final int startRow = Math.max(resolution - yBin, 0);

			// Count if there are enough neighbours
			int count = minPts;
			counting: for (int y = miny, row = startRow; y < maxy; y++, row++)
			{
				// Dynamically compute the search strip 
				final int minx = Math.max(xBin + offset[row].start, 0);
				final int maxx = Math.min(xBin + offset[row].end, xBins);

				// Use fast-forward to skip to the next position with data
				int index = getIndex(minx, y);
				if (grid[index] == null)
					index = fastForward[index];
				int endIndex = getIndex(maxx, y);
				while (index < endIndex)
				{
					count -= grid[index].length;
					if (count <= 0)
						break counting;
					index = fastForward[index];
				}
			}

			if (count > 0)
			{
				// Not a core point so do not compute distances
				//System.out.println("Skipping distance computation (not a core point)");
				return;
			}

			// Compute distances
			for (int y = miny, row = startRow; y < maxy; y++, row++)
			{
				// Dynamically compute the search strip 
				final int minx = Math.max(xBin + offset[row].start, 0);
				final int maxx = Math.min(xBin + offset[row].end, xBins);

				final int columnShift = getIndex(xBin - resolution, y);

				// Use fast-forward to skip to the next position with data
				int index = getIndex(minx, y);
				if (grid[index] == null)
					index = fastForward[index];
				int endIndex = getIndex(maxx, y);
				while (index < endIndex)
				{
					final Molecule[] list = grid[index];

					// Build a list of all the neighbours

					// Find the column in the circular mask
					final int col = index - columnShift;

					// TODO - Can this be made more efficient with an internal flag (i.e. 1 comparison per loop)?

					//					// If internal just add all the points
					//					if (col >= offset[row].startInternal && col < offset[row].endInternal)
					//					{
					//						neighbours.add(list);
					//					}
					//					else
					//					{
					// If at the edge then compute distances
					for (int i = list.length; i-- > 0;)
					{
						if (object.distance2(list[i]) <= e)
						{
							neighbours.add(list[i]);
						}
					}
					//					}

					index = fastForward[index];
				}
			}
		}

		void findNeighboursAndDistances(int minPts, Molecule object, float e)
		{
			// TODO - could this be implemented to use concentric rings around the current pixel
			// We would need to pre-compute all the bounds for each concentric ring.
			// then process from the central point outward. When the min points is achieved 
			// we then compute the core distance using the molecules in the most recent ring. 
			// For all remaining points outside the core distance
			// we only need to compute the reachability distance if it is currently UNDEFINED
			// or it is greater than the core distance.

			super.findNeighboursAndDistances(minPts, object, e);
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
		boolean add(Molecule m)
		{
			list[size++] = m.toOPTICSResult();
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
	 * @return the results (or null if the algorithm was stopped using the tracker)
	 */
	public OPTICSResult optics(float generatingDistanceE, int minPts)
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
		Molecule[] setOfObjects = grid.setOfObjects;

		OPTICSMoleculePriorityQueue orderSeeds = new OPTICSMoleculePriorityQueue(size);
		OPTICSResultList results = new OPTICSResultList(size);

		for (int i = 0; i < size; i++)
		{
			final Molecule object = setOfObjects[i];
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

		if (!options.contains(Option.CACHE))
			clearMemory();

		return optics;
	}

	private MoleculeSpace grid;
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
				tracker.log("Initialising ...");

			// Control the type of space we use to store the data
			if (options.contains(Option.RADIAL_PROCESSING))
				grid = new RadialMoleculeSpace(generatingDistanceE);
			else
				grid = new GridMoleculeSpace(generatingDistanceE);
		}
		else
		{
			// This is the same distance so the objects can be reused
			grid.reset();
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
	private boolean expandClusterOrder(Molecule object, float e, int minPts, OPTICSResultList orderedFile,
			OPTICSMoleculePriorityQueue orderSeeds)
	{
		grid.findNeighboursAndDistances(minPts, object, e);
		object.markProcessed();
		object.setCoreDistance(minPts, grid.neighbours);
		if (orderedFile.add(object))
			return true;

		if (object.coreDistance != UNDEFINED)
		{
			// Create seed-list for further expansion.
			// The next counter is used to ensure we sort only the remaining entries in the seed list.
			orderSeeds.clear();
			update(orderSeeds, grid.neighbours, object);

			while (orderSeeds.hasNext())
			{
				object = orderSeeds.next();
				grid.findNeighboursAndDistances(minPts, object, e);
				object.markProcessed();
				object.setCoreDistance(minPts, grid.neighbours);
				if (orderedFile.add(object))
					return true;

				if (object.coreDistance != UNDEFINED)
					update(orderSeeds, grid.neighbours, object);
			}
		}
		return false;
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
	private void update(OPTICSMoleculePriorityQueue orderSeeds, MoleculeList neighbours, Molecule centreObject)
	{
		final float c_dist = centreObject.coreDistance;
		for (int i = neighbours.size; i-- > 0;)
		{
			final Molecule object = neighbours.get(i);
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
	 * This creates a large memory structure. It can be held in memory for re-use when using a different number of min
	 * points. The tracker can be used to follow progress (see {@link #setTracker(TrackProgress)}).
	 *
	 * @param generatingDistanceE
	 *            the generating distance E (set to zero to auto calibrate)
	 * @param minPts
	 *            the min points for a core object
	 * @return the results (or null if the algorithm was stopped using the tracker)
	 */
	public DBSCANResult dbscan(float generatingDistanceE, int minPts)
	{
		if (minPts < 1)
			minPts = 1;

		// Use the same structure as OPTICS (since the algorithm is very similar)
		initialiseOPTICS(generatingDistanceE, minPts);

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
		Molecule[] setOfObjects = grid.setOfObjects;

		// Working storage
		MoleculeQueue seeds = new MoleculeQueue(size);
		Counter counter = new Counter(size);

		for (int i = 0; i < size; i++)
		{
			final Molecule object = setOfObjects[i];
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
				tracker.log("Aborted DBSCAN");
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

		if (!options.contains(Option.CACHE))
			clearMemory();

		return dbscanResult;
	}

	private boolean expandCluster(Molecule object, float e, int minPts, Counter counter, MoleculeQueue seeds)
	{
		grid.findNeighbours(minPts, object, e);
		if (counter.increment())
			return true;

		object.markProcessed();
		object.setNumberOfPoints(grid.neighbours.size);
		if (grid.neighbours.size >= minPts)
		{
			// New cluster
			int clusterId = counter.nextClusterId();
			object.setClusterOrigin(clusterId);

			// Expand through the grid.neighbours
			seeds.clear();
			update(seeds, grid.neighbours, clusterId);

			while (seeds.hasNext())
			{
				object = seeds.next();
				grid.findNeighbours(minPts, object, e);
				if (counter.increment())
					return true;

				object.markProcessed();
				object.setNumberOfPoints(grid.neighbours.size);
				if (grid.neighbours.size >= minPts)
					update(seeds, grid.neighbours, clusterId);
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
	private void update(MoleculeQueue pointsToSearch, MoleculeList neighbours, int clusterId)
	{
		for (int i = neighbours.size; i-- > 0;)
		{
			final Molecule object = neighbours.get(i);
			if (object.isNotInACluster())
			{
				object.setClusterMember(clusterId);

				// Ensure that the search is not repeated
				if (object.isNotProcessed())
					pointsToSearch.push(object);
			}
		}
	}

	@Override
	public OPTICSManager clone()
	{
		OPTICSManager m = (OPTICSManager) super.clone();
		m.options = options.clone();
		m.clearMemory();
		return m;
	}
}
