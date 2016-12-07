package gdsc.core.clustering;

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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TurboList;
import java.util.TurboList.SimplePredicate;

import org.apache.commons.math3.util.FastMath;

import gdsc.core.ij.Utils;
import gdsc.core.logging.TrackProgress;
import gdsc.core.utils.Maths;

/**
 * Calculate the density of localisations around a given position using a square block of specified width
 */
public class DensityManager
{
	/**
	 * The UNDEFINED distance in the OPTICS algorithm. This is actually arbitrary. Use a simple value so we can spot it
	 * when debugging.
	 */
	private static final float UNDEFINED = -1;
	/**
	 * A result not part of any cluster
	 */
	private static final int NOISE = 0;

	/**
	 * Contains the ordered result of the OPTICS algorithm
	 */
	public class OPTICSResult
	{
		/**
		 * The parent object used when generating this result. Can be used to identify the coordinates from the original
		 * input data.
		 */
		final int parent;
		/**
		 * The Id of the point that set the reachability distance.
		 */
		final int predecessor;
		/**
		 * The cluster identifier
		 */
		int clusterId;
		/**
		 * The core distance. Set to positive infinity if not a core point.
		 */
		final double coreDistance;
		/**
		 * The reachability distance. Set to positive infinity if not a reachable point, or the first core point of a
		 * new grouping.
		 */
		final double reachabilityDistance;

		// TODO - Add anything requires for the DBSCAN method. E.g. should we store 
		// the original point index for use in seeding clusters with the lowest core
		// distance?

		/**
		 * Instantiates a new OPTICS result.
		 *
		 * @param clusterId
		 *            the cluster id
		 * @param coreDistance
		 *            the core distance
		 * @param reachabilityDistance
		 *            the reachability distance
		 * @param maxDistance
		 */
		public OPTICSResult(int parent, int predecessor, int clusterId, double coreDistance,
				double reachabilityDistance)
		{
			this.parent = parent;
			this.predecessor = predecessor;
			this.clusterId = clusterId;
			this.coreDistance = coreDistance;
			this.reachabilityDistance = reachabilityDistance;
		}
	}

	/**
	 * Contains the result of the OPTICS algorithm
	 */
	public class Optics
	{
		public final int minPts;
		public final float generatingDistance;
		private final OPTICSResult[] opticsResults;
		/**
		 * Cluster hierarchy assigned by .
		 */
		public OPTICSCluster[] clustering;

		/**
		 * Instantiates a new optics.
		 *
		 * @param minPts
		 *            the min points
		 * @param generatingDistance
		 *            the generating distance
		 * @param opticsResults
		 *            the optics results
		 */
		private Optics(int minPts, float generatingDistance, OPTICSResult[] opticsResults)
		{
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
		public OPTICSResult get(int index)
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
		 * Gets the core distance profile. Points with no reachability distance (stored as infinity) can be
		 * converted to the generating distance.
		 *
		 * @param convert
		 *            convert non-core spots to have a reachability distance of the generating distance
		 * @return the reachability distance profile
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
		 * Reset cluster ids to NOISE.
		 */
		public void resetClusterIds()
		{
			for (int i = size(); i-- > 0;)
				opticsResults[i].clusterId = NOISE;
		}
	}

	public class Molecule
	{
		int id;
		public float x, y;

		// Used to construct a single linked list of molecules
		public Molecule next = null;

		public Molecule(int id, float x, float y, Molecule next)
		{
			this.id = id;
			this.x = x;
			this.y = y;
			this.next = next;
		}

		public float distance(Molecule other)
		{
			final float dx = x - other.x;
			final float dy = y - other.y;
			return (float) Math.sqrt(dx * dx + dy * dy);
		}

		public float distance2(Molecule other)
		{
			final float dx = x - other.x;
			final float dy = y - other.y;
			return dx * dx + dy * dy;
		}
	}

	private class OPTICSMolecule extends Molecule
	{
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
		public int predecessor = -1;

		public OPTICSMolecule(int id, float x, float y, int xBin, int yBin, Molecule next)
		{
			super(id, x, y, next);
			this.xBin = xBin;
			this.yBin = yBin;
			reset();
		}

		void reset()
		{
			processed = false;
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

		public OPTICSResult toResult()
		{
			double actualCoreDistance = (coreDistance == UNDEFINED) ? Double.POSITIVE_INFINITY : getCoreDistance();
			double actualReachabilityDistance = (reachabilityDistance == UNDEFINED) ? Double.POSITIVE_INFINITY
					: getReachabilityDistance();
			return new OPTICSResult(id, predecessor, NOISE, actualCoreDistance, actualReachabilityDistance);
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
			return 0;
		}
	}

	private final static OPTICSComparator opticsComparator = new OPTICSComparator();

	/**
	 * Used in the OPTICS algorithm
	 */
	private class OPTICSMoleculeList
	{
		final OPTICSMolecule[] list;
		int size = 0;

		OPTICSMoleculeList(int capacity)
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

		void sort(int next)
		{
			Arrays.sort(list, next, size, opticsComparator);
		}

		OPTICSMolecule get(int i)
		{
			return list[i];
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
					for (Molecule m = linkedListGrid[xBin][yBin]; m != null; m = m.next)
						count++;
					final OPTICSMolecule[] list = new OPTICSMolecule[count];
					for (Molecule m = linkedListGrid[xBin][yBin]; m != null; m = m.next)
						list[--count] = (OPTICSMolecule) m;
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
		final OPTICSResult[] list;
		int size = 0;

		OPTICSResultList(int capacity, double maxDistance)
		{
			list = new OPTICSResult[capacity];
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

	private TrackProgress tracker = null;
	private final float[] xcoord, ycoord;
	private final float minXCoord, minYCoord, maxXCoord, maxYCoord;
	private final int area;

	/**
	 * Input arrays are modified
	 * 
	 * @param xcoord
	 * @param ycoord
	 * @param bounds
	 * @throws IllegalArgumentException
	 *             if results are null or empty
	 */
	public DensityManager(float[] xcoord, float[] ycoord, Rectangle bounds)
	{
		if (xcoord == null || ycoord == null || xcoord.length == 0 || xcoord.length != ycoord.length)
			throw new IllegalArgumentException("Results are null or empty or mismatched in length");

		this.xcoord = xcoord;
		this.ycoord = ycoord;

		// Assign localisations & get min bounds
		float minXCoord = Float.POSITIVE_INFINITY;
		float minYCoord = Float.POSITIVE_INFINITY;
		for (int i = 0; i < xcoord.length; i++)
		{
			if (minXCoord > xcoord[i])
				minXCoord = xcoord[i];
			if (minYCoord > ycoord[i])
				minYCoord = ycoord[i];
		}

		// Round down and shift to origin (so all coords are >=0 for efficient grid allocation)
		final float shiftx = (float) Math.floor(minXCoord);
		final float shifty = (float) Math.floor(minYCoord);

		// Get max bounds
		minXCoord -= shiftx;
		minYCoord -= shifty;
		float maxXCoord = 0;
		float maxYCoord = 0;
		for (int i = 0; i < xcoord.length; i++)
		{
			xcoord[i] -= shiftx;
			ycoord[i] -= shifty;
			if (maxXCoord < xcoord[i])
				maxXCoord = xcoord[i];
			if (maxYCoord < ycoord[i])
				maxYCoord = ycoord[i];
		}

		this.minXCoord = minXCoord;
		this.minYCoord = minYCoord;
		this.maxXCoord = maxXCoord;
		this.maxYCoord = maxYCoord;
		// Store the area of the input results
		area = bounds.width * bounds.height;
	}

	/**
	 * Calculate the density for the results.
	 * <p>
	 * A square block is used around each result of the specified radius. The results are assigned to a grid using a
	 * cell size of radius / resolution. The totals of each cell are then counted for the range +/- radius around each
	 * result.
	 * <p>
	 * If the block overlaps the border of the image the density will suffer from under-counting. The value can be
	 * optionally scaled using the fraction of the overlap area.
	 * <p>
	 * Note that the score is the number of molecules surrounding the given molecule, so the molecule itself is not
	 * counted.
	 * 
	 * @param radius
	 * @param resolution
	 * @param adjustForBorder
	 * @return
	 */
	public int[] calculateSquareDensity(float radius, int resolution, boolean adjustForBorder)
	{
		if (radius < 0)
			throw new IllegalArgumentException("Radius must be positive");
		if (resolution < 1)
			throw new IllegalArgumentException("Resolution must be positive");

		final float cellSize = radius / resolution;

		int maxx = (int) (maxXCoord / cellSize) + 1;
		int maxy = (int) (maxYCoord / cellSize) + 1;

		// Allocate counts to the cells
		int[] data = new int[maxx * maxy];
		for (int i = 0; i < xcoord.length; i++)
		{
			int x = (int) (xcoord[i] / cellSize);
			int y = (int) (ycoord[i] / cellSize);
			data[y * maxx + x]++;
		}

		// Create rolling sum table. Re-use the storage
		// First row
		int cs_ = 0; // Column sum
		for (int i = 0; i < maxx; i++)
		{
			cs_ += data[i];
			data[i] = cs_;
		}

		// Remaining rows:
		// sum = rolling sum of row + sum of row above
		for (int y = 1; y < maxy; y++)
		{
			int i = y * maxx;
			cs_ = 0;

			// Remaining columns
			for (int x = 0; x < maxx; x++, i++)
			{
				cs_ += data[i];
				data[i] = data[i - maxx] + cs_;
			}
		}

		// Used for debugging
		//		FileWriter out = null;
		//		try
		//		{
		//			out = new FileWriter("/tmp/check.txt");
		//		}
		//		catch (IOException e)
		//		{
		//			// Ignore
		//		}

		// For each localisation, compute the sum of counts within a square box radius
		final float area = 4 * resolution * resolution;
		int[] density = new int[xcoord.length];
		for (int i = 0; i < xcoord.length; i++)
		{
			int u = (int) (xcoord[i] / cellSize);
			int v = (int) (ycoord[i] / cellSize);

			// Note: Subtract 1 to discount the current localisation. Should this be done?
			int sum = -1;

			// Get the bounds
			int minU = u - resolution - 1;
			int maxU = FastMath.min(u + resolution, maxx - 1);
			int minV = v - resolution - 1;
			int maxV = FastMath.min(v + resolution, maxy - 1);

			// Compute sum from rolling sum using:
			// sum(u,v) = 
			// + s(maxU,maxV) 
			// - s(minU,maxV)
			// - s(maxU,minV)
			// + s(minU,minV)
			// Note: 
			// s(u,v) = 0 when either u,v < 0
			// s(u,v) = s(umax,v) when u>umax
			// s(u,v) = s(u,vmax) when v>vmax
			// s(u,v) = s(umax,vmax) when u>umax,v>vmax

			// + s(maxU,maxV) 
			int index = maxV * maxx + maxU;
			sum += data[index];

			boolean clipped = false;
			if (minU >= 0)
			{
				// - s(minU,maxV)
				index = maxV * maxx + minU;
				sum -= data[index];
			}
			else
			{
				clipped = true;
				minU = -1;
			}
			if (minV >= 0)
			{
				// - s(maxU,minV)
				index = minV * maxx + maxU;
				sum -= data[index];

				if (minU >= 0)
				{
					// + s(minU,minV)
					index = minV * maxx + minU;
					sum += data[index];
				}
			}
			else
			{
				clipped = true;
				minV = -1;
			}

			// Adjust for area
			if (adjustForBorder && clipped)
			{
				sum *= area / ((maxU - minU - 1) * (maxV - minV - 1));
			}

			density[i] = sum;

			//			// Check
			//			if (out != null)
			//			{
			//				int sum2 = 0;
			//				float xlower = xcoord[i] - radius;
			//				float xupper = xcoord[i] + radius;
			//				float ylower = ycoord[i] - radius;
			//				float yupper = ycoord[i] + radius;
			//				for (int j = 0; j < xcoord.length; j++)
			//				{
			//					if (j == i)
			//						continue;
			//					if (xcoord[j] < xlower || xcoord[j] > xupper)
			//						continue;
			//					if (ycoord[j] < ylower || ycoord[j] > yupper)
			//						continue;
			//					sum2++;
			//				}
			//
			//				try
			//				{
			//					out.write(String.format("%d %d\n", sum, sum2));
			//				}
			//				catch (IOException e)
			//				{
			//					// Just shutdown
			//					try
			//					{
			//						out.close();
			//					}
			//					catch (IOException e1)
			//					{
			//						// Ignore
			//					}
			//					out = null;
			//				}
			//			}
		}

		//		if (out != null)
		//		{
		//			try
		//			{
		//				out.close();
		//			}
		//			catch (IOException e)
		//			{
		//				// Ignore
		//			}
		//		}

		return density;
	}

	/**
	 * Calculate the local density for the results using square blocks of the specified radius. The returned array is
	 * equal in size to the number of blocks. The score is the number of molecules within the 3x3 region surrounding
	 * each block.
	 *
	 * @param radius
	 *            the radius
	 * @return the block density array
	 */
	public int[] calculateBlockDensity(final float radius)
	{
		if (radius < 0)
			throw new IllegalArgumentException("Radius must be positive");

		// Note: We do not subtract min from the value for speed:
		// final int maxx = (int) ((maxXCoord-minXCoord) / radius) + 1;
		// minXCoord will be in the range 0-1 after initialisation.		

		final int maxx = (int) (maxXCoord / radius) + 1;
		final int maxy = (int) (maxYCoord / radius) + 1;

		// Allocate counts to the cells
		final int[] data = new int[maxx * maxy];
		for (int i = 0; i < xcoord.length; i++)
		{
			final int x = (int) (xcoord[i] / radius);
			final int y = (int) (ycoord[i] / radius);
			data[y * maxx + x]++;
		}

		// Create rolling sum table. Re-use the storage
		// First row
		int cs_ = 0; // Column sum
		for (int i = 0; i < maxx; i++)
		{
			cs_ += data[i];
			data[i] = cs_;
		}

		// Remaining rows:
		// sum = rolling sum of row + sum of row above
		for (int y = 1; y < maxy; y++)
		{
			int i = y * maxx;
			cs_ = 0;

			// Remaining columns
			for (int x = 0; x < maxx; x++, i++)
			{
				cs_ += data[i];
				data[i] = data[i - maxx] + cs_;
			}
		}

		// Pre-compute U bounds
		final int[] minU = new int[maxx];
		final int[] maxU = new int[maxx];
		final boolean[] minUOK = new boolean[maxx];
		for (int u = maxx; u-- > 0;)
		{
			minU[u] = u - 2;
			maxU[u] = FastMath.min(u + 1, maxx - 1);
			minUOK[u] = u >= 2;
		}

		// For each block, compute the sum of counts within a 3x3 box radius
		int[] density = new int[data.length];
		for (int v = maxy; v-- > 0;)
		{
			final int minV = v - 2;
			final int maxV = FastMath.min(v + 1, maxy - 1);
			final boolean minVOK = (minV >= 0);
			final int lowerIndex = minV * maxx;

			for (int u = maxx; u-- > 0;)
			{
				// Compute sum from rolling sum using:
				// sum(u,v) = 
				// + s(maxU,maxV) 
				// - s(minU,maxV)
				// - s(maxU,minV)
				// + s(minU,minV)
				// Note: 
				// s(u,v) = 0 when either u,v < 0
				// s(u,v) = s(umax,v) when u>umax
				// s(u,v) = s(u,vmax) when v>vmax
				// s(u,v) = s(umax,vmax) when u>umax,v>vmax

				// + s(maxU,maxV) 
				final int upperIndex = maxV * maxx;
				int sum = data[upperIndex + maxU[u]];

				if (minUOK[u])
				{
					// - s(minU,maxV)
					sum -= data[upperIndex + minU[u]];
				}
				if (minVOK)
				{
					// - s(maxU,minV)
					sum -= data[lowerIndex + maxU[u]];

					if (minUOK[u])
					{
						// + s(minU,minV)
						sum += data[lowerIndex + minU[u]];
					}
				}

				density[v * maxx + u] = sum;
			}
		}

		return density;
	}

	/**
	 * Calculate the local density for the results using square blocks of the specified radius. The returned array is
	 * equal in size to the number of blocks. The score is the number of molecules within the 3x3 region surrounding
	 * each block.
	 *
	 * @param radius
	 *            the radius
	 * @return the block density array
	 */
	public int[] calculateBlockDensity2(final float radius)
	{
		final float maxx = maxXCoord;
		final float maxy = maxYCoord;

		// Assign to a grid
		final float binWidth = radius;
		final int nXBins = 1 + (int) ((maxx) / binWidth);
		final int nYBins = 1 + (int) ((maxy) / binWidth);
		int[][] grid = new int[nXBins][nYBins];
		for (int i = 0; i < xcoord.length; i++)
		{
			final int xBin = (int) ((xcoord[i]) / binWidth);
			final int yBin = (int) ((ycoord[i]) / binWidth);
			grid[xBin][yBin]++;
		}

		int[] density = new int[nXBins * nYBins];
		boolean withinY = false;
		for (int yBin = nYBins; yBin-- > 0; withinY = true)
		{
			boolean withinX = false;
			for (int xBin = nXBins; xBin-- > 0; withinX = true)
			{
				int i = yBin * nXBins + xBin;
				final int iCount = grid[xBin][yBin];
				density[i] += iCount;

				// Compare up to a maximum of 4 neighbours
				//      | 0,0  |  1,0
				// ------------+-----
				// -1,1 | 0,1  |  1,1

				if (withinY)
				{
					add(density, grid, nXBins, i, iCount, xBin, yBin + 1);
					if (xBin > 0)
						add(density, grid, nXBins, i, iCount, xBin - 1, yBin + 1);

					if (withinX)
					{
						add(density, grid, nXBins, i, iCount, xBin + 1, yBin);
						add(density, grid, nXBins, i, iCount, xBin + 1, yBin + 1);
					}
				}
				else
				{
					if (withinX)
					{
						add(density, grid, nXBins, i, iCount, xBin + 1, yBin);
					}
				}
			}
		}

		return density;
	}

	private static void add(final int[] density, final int[][] grid, final int nXBins, final int i, final int iCount,
			final int xBin, final int yBin)
	{
		density[i] += grid[xBin][yBin];
		density[yBin * nXBins + xBin] += iCount;
	}

	/**
	 * Calculate the local density for the results using square blocks of the specified radius. The returned array is
	 * equal in size to the number of blocks. The score is the number of molecules within the 3x3 region surrounding
	 * each block.
	 *
	 * @param radius
	 *            the radius
	 * @return the block density array
	 */
	public int[] calculateBlockDensity3(final float radius)
	{
		final float maxx = maxXCoord;
		final float maxy = maxYCoord;

		// Assign to a grid
		final float binWidth = radius;
		final int nXBins = 1 + (int) ((maxx) / binWidth);
		final int nYBins = 1 + (int) ((maxy) / binWidth);
		int[][] grid = new int[nXBins][nYBins];
		for (int i = 0; i < xcoord.length; i++)
		{
			final int xBin = (int) ((xcoord[i]) / binWidth);
			final int yBin = (int) ((ycoord[i]) / binWidth);
			grid[xBin][yBin]++;
		}

		// Simple sweep
		int[] density = new int[nXBins * nYBins];
		for (int yBin = 0; yBin < nYBins; yBin++)
		{
			for (int xBin = 0; xBin < nXBins; xBin++)
			{
				int sum = 0;
				for (int y = -1; y <= 1; y++)
				{
					int yBin2 = yBin + y;
					if (yBin2 < 0 || yBin2 >= nYBins)
						continue;
					for (int x = -1; x <= 1; x++)
					{
						int xBin2 = xBin + x;
						if (xBin2 < 0 || xBin2 >= nYBins)
							continue;
						sum += grid[xBin2][yBin2];
					}
				}
				density[yBin * nXBins + xBin] = sum;
			}
		}

		return density;
	}

	/**
	 * @return the tracker
	 */
	public TrackProgress getTracker()
	{
		return tracker;
	}

	/**
	 * @param tracker
	 *            the tracker to set
	 */
	public void setTracker(TrackProgress tracker)
	{
		this.tracker = tracker;
	}

	/**
	 * Calculate the density for the results.
	 * <p>
	 * A circle is used around each result of the specified radius and the number of neighbours counted for each result.
	 * <p>
	 * If the block overlaps the border of the image the density will suffer from under-counting. The value can be
	 * optionally scaled using the fraction of the overlap area.
	 * <p>
	 * Note that the score is the number of molecules surrounding the given molecule, so the molecule itself is not
	 * counted.
	 * 
	 * @param radius
	 * @param adjustForBorder
	 * @return
	 */
	public int[] calculateDensity(float radius, boolean adjustForBorder)
	{
		if (radius < 0)
			throw new IllegalArgumentException("Radius must be positive");

		// For each localisation, compute the sum of counts within a circle radius
		// TODO - Determine the optimum parameters to switch to using the grid method.
		int[] density = (xcoord.length < 200) ? calculateDensityTriangle(radius) : calculateDensityGrid(radius);

		// Adjust for area
		if (adjustForBorder)
		{
			// Boundary
			final float upperX = maxXCoord - radius;
			final float upperY = maxYCoord - radius;

			for (int i = 0; i < xcoord.length; i++)
			{
				int sum = density[i];
				final float x = xcoord[i];
				final float y = ycoord[i];

				// Calculate the area of the circle that has been missed
				// http://stackoverflow.com/questions/622287/area-of-intersection-between-circle-and-rectangle
				// Assume: Circle centre will be within the rectangle

				//   S1       S2       S3
				//
				//        |        |
				//    A1  |________|   A3      SA
				//        /        \
				//       /|   A2   |\        
				// -----/-|--------|-\-----
				//     |  |        |  |    
				//     |B1|   B2   |B3|        SB
				//     |  |        |  |
				// -----\-|--------|-/-----
				//       \|   C2   |/   C3     SC
				//   C1   \________/
				//        |        |

				// Note: A1,A3,C1,C3 are inside the circle
				// S1 = Slice 1, SA = Slice A, etc

				// Calculate if the upper/lower boundary of the rectangle slices the circle
				// -- Calculate the slice area using the formula for a segment
				// -- Check if the second boundary is slices the circle (i.e. a vertex is inside the circle)
				// ---- Calculate the corner section area to subtract from the overlapping slices
				// Missed = S1 + S3 + SA + SC - A1 - A3 - C1 - C3
				double S1 = 0, S3 = 0, SA = 0, SC = 0, A1 = 0, A3 = 0, C1 = 0, C3 = 0;

				// Note all coords are shifted the origin so simply compare the radius and the 
				// max bounds minus the radius

				if (x < radius)
				{
					S1 = getSegmentArea(radius, radius - x);
					if (y < radius)
					{
						A1 = getCornerArea(radius, x, y);
					}
					if (y > upperY)
					{
						C1 = getCornerArea(radius, x, maxYCoord - y);
					}
				}
				if (x > upperX)
				{
					float dx = maxXCoord - x;
					S1 = getSegmentArea(radius, radius - dx);
					if (y < radius)
					{
						A3 = getCornerArea(radius, dx, y);
					}
					if (y > upperY)
					{
						C3 = getCornerArea(radius, dx, maxYCoord - y);
					}
				}
				if (y < radius)
				{
					SA = getSegmentArea(radius, radius - y);
				}
				if (y > upperY)
				{
					float dy = maxYCoord - y;
					SC = getSegmentArea(radius, radius - dy);
				}

				double missed = S1 + S3 + SA + SC - A1 - A3 - C1 - C3;
				if (missed > 0)
				{
					double adjustment = area / (area - missed);
					//					if (missed > area)
					//					{
					//						System.out.printf("Ooops %f > %f\n", missed, area);
					//					}
					//					else
					//					{
					//						System.out.printf("increase %f @ %f %f\n", adjustment, x, y);
					//					}
					sum *= adjustment;
				}

				density[i] = sum;
			}
		}

		return density;
	}

	/**
	 * Calculate the density for the results using an all-vs-all analysis.
	 * <p>
	 * A circle is used around each result of the specified radius and the number of neighbours counted for each result.
	 * <p>
	 * If the block overlaps the border of the image the density will suffer from under-counting.
	 * <p>
	 * Note that the score is the number of molecules surrounding the given molecule, so the molecule itself is not
	 * counted.
	 * 
	 * @param radius
	 * @return
	 */
	public int[] calculateDensity(float radius)
	{
		final float r2 = radius * radius;
		int[] density = new int[xcoord.length];
		for (int i = 0; i < xcoord.length; i++)
		{
			int sum = density[i];
			final float x = xcoord[i];
			final float y = ycoord[i];
			for (int j = 0; j < xcoord.length; j++)
			{
				if (i == j)
					continue;
				final float dx = x - xcoord[j];
				final float dy = y - ycoord[j];
				if (dx * dx + dy * dy < r2)
				{
					sum++;
				}
			}

			density[i] = sum;
		}
		return density;
	}

	/**
	 * Calculate the density for the results using an all-vs-all analysis in the lower triangle of comparisons.
	 * <p>
	 * A circle is used around each result of the specified radius and the number of neighbours counted for each result.
	 * <p>
	 * If the block overlaps the border of the image the density will suffer from under-counting.
	 * <p>
	 * Note that the score is the number of molecules surrounding the given molecule, so the molecule itself is not
	 * counted.
	 * 
	 * @param radius
	 * @return
	 */
	public int[] calculateDensityTriangle(float radius)
	{
		final float r2 = radius * radius;
		int[] density = new int[xcoord.length];
		for (int i = 0; i < xcoord.length; i++)
		{
			int sum = density[i];
			final float x = xcoord[i];
			final float y = ycoord[i];
			for (int j = i + 1; j < xcoord.length; j++)
			{
				final float dx = x - xcoord[j];
				final float dy = y - ycoord[j];
				if (dx * dx + dy * dy < r2)
				{
					sum++;
					density[j]++;
				}
			}

			density[i] = sum;
		}
		return density;
	}

	/**
	 * Calculate the density for the results using a nearest neighbour cell grid analysis.
	 * <p>
	 * A circle is used around each result of the specified radius and the number of neighbours counted for each result.
	 * <p>
	 * If the block overlaps the border of the image the density will suffer from under-counting.
	 * <p>
	 * Note that the score is the number of molecules surrounding the given molecule, so the molecule itself is not
	 * counted.
	 * 
	 * @param radius
	 * @param adjustForBorder
	 * @return
	 */
	public int[] calculateDensityGrid(float radius)
	{
		int[] density = new int[xcoord.length];

		final float minx = minXCoord;
		final float miny = minYCoord;
		final float maxx = maxXCoord;
		final float maxy = maxYCoord;

		// Assign to a grid
		final float binWidth = radius * 1.01f;
		final int nXBins = 1 + (int) ((maxx - minx) / binWidth);
		final int nYBins = 1 + (int) ((maxy - miny) / binWidth);
		Molecule[][] grid = new Molecule[nXBins][nYBins];
		for (int i = 0; i < xcoord.length; i++)
		{
			final float x = xcoord[i];
			final float y = ycoord[i];
			final int xBin = (int) ((x - minx) / binWidth);
			final int yBin = (int) ((y - miny) / binWidth);
			// Build a single linked list
			grid[xBin][yBin] = new Molecule(i, x, y, grid[xBin][yBin]);
		}

		Molecule[] neighbours = new Molecule[5];
		final float radius2 = radius * radius;
		for (int yBin = 0; yBin < nYBins; yBin++)
		{
			for (int xBin = 0; xBin < nXBins; xBin++)
			{
				for (Molecule m1 = grid[xBin][yBin]; m1 != null; m1 = m1.next)
				{
					// Build a list of which cells to compare up to a maximum of 4
					//      | 0,0  |  1,0
					// ------------+-----
					// -1,1 | 0,1  |  1,1

					int count = 1;
					neighbours[0] = m1.next;

					if (yBin < nYBins - 1)
					{
						neighbours[count++] = grid[xBin][yBin + 1];
						if (xBin > 0)
							neighbours[count++] = grid[xBin - 1][yBin + 1];
					}
					if (xBin < nXBins - 1)
					{
						neighbours[count++] = grid[xBin + 1][yBin];
						if (yBin < nYBins - 1)
							neighbours[count++] = grid[xBin + 1][yBin + 1];
					}

					// Compare to neighbours
					while (count-- > 0)
					{
						for (Molecule m2 = neighbours[count]; m2 != null; m2 = m2.next)
						{
							if (m1.distance2(m2) < radius2)
							{
								density[m1.id]++;
								density[m2.id]++;
							}
						}
					}
				}
			}
		}

		return density;
	}

	/**
	 * Calculate the area of circular segment, a portion of a disk whose upper boundary is a (circular) arc and whose
	 * lower boundary is a chord making a central angle of theta radians.
	 * <p>
	 * See http://mathworld.wolfram.com/CircularSegment.html
	 * 
	 * @param R
	 *            the radius of the circle
	 * @param h
	 *            the height of the arced portion
	 * @return The area
	 */
	private double getSegmentArea(double R, double h)
	{
		return R * R * Math.acos((R - h) / R) - (R - h) * Math.sqrt(2 * R * h - h * h);
	}

	/**
	 * Get the area taken by a corner of a rectangle within a circle of radius R
	 * 
	 * @param R
	 *            the radius of the circle
	 * @param x
	 *            The corner X position
	 * @param y
	 *            The corner Y position
	 * @return The area
	 */
	private double getCornerArea(double R, double x, double y)
	{
		// 1 vertex is inside the circle: The sum of the areas of a circular segment and a triangle.

		//                            (x,y)
		//	    XXXXX                   XXXXXXXXX p2
		//     X     X       Triangle ->X     _-X
		//    X       X                 X   _-  X 
		//    X    +--X--+              X _-   X <- Circular segment 
		//     X   | X   |              X-   XX 
		//	    XXXXX    |              XXXXX
		//	       |     |             p1

		// Assume: circle at origin, x & y are positive, x^2 + y^2 < radius^2

		// Get the point p1 & p2
		double x1 = x;
		double y1 = otherSide(x, R);
		double x2 = otherSide(y, R);
		double y2 = y;

		// Calculate half the length of the chord cutting the circle between p1 & p2
		final double dx = x2 - x1;
		final double dy = y2 - y1;
		double halfChord = Math.sqrt(dx * dx + dy * dy);

		// Calculate the height of the arced portion
		double h = R - otherSide(halfChord, R);

		// Get the area as the circular segment plus the triangle
		return getSegmentArea(R, h) + 0.5 * dx * dy;
	}

	/**
	 * Returns a from a right angle triangle where a^2 + b^2 = c^2
	 * 
	 * @param b
	 * @param c
	 * @return a
	 */
	private double otherSide(double b, double c)
	{
		return Math.sqrt(c * c - b * b);
	}

	/**
	 * Compute Ripley's K-function.
	 * <p>
	 * See http://en.wikipedia.org/wiki/Spatial_descriptive_statistics#Ripley.27s_K_and_L_functions
	 * 
	 * @param radius
	 *            The radius
	 * @return The K-function score
	 */
	public double ripleysKFunction(double radius)
	{
		if (radius < 0)
			throw new IllegalArgumentException("Radius must be positive");

		// Count the number of points within the distance 
		int sum = calculateSumGrid((float) radius);

		// Normalise
		double scale = area / ((double) xcoord.length * (double) xcoord.length);
		double k = sum * scale;

		return k;
	}

	/**
	 * Calculate the number of pairs within the given radius.
	 * <p>
	 * The sum is over i<n, j<n, i!=j
	 * 
	 * @param radius
	 * @return
	 */
	public int calculateSum(float radius)
	{
		final float r2 = (float) (radius * radius);
		int sum = 0;
		for (int i = 0; i < xcoord.length; i++)
		{
			final float x = xcoord[i];
			final float y = ycoord[i];
			for (int j = i + 1; j < xcoord.length; j++)
			{
				final float dx = x - xcoord[j];
				final float dy = y - ycoord[j];
				if (dx * dx + dy * dy < r2)
				{
					sum++;
				}
			}
		}

		// Note that the sum should be computed over:
		//   i<n, j<n, i!=j
		// Thus it should be doubled to account for j iterating from zero.
		sum *= 2;
		return sum;
	}

	/**
	 * Calculate the number of pairs within the given radius using a nearest neighbour cell grid analysis.
	 * <p>
	 * The sum is over i<n, j<n, i!=j
	 * 
	 * @param radius
	 * @return
	 */
	public int calculateSumGrid(float radius)
	{
		int sum = 0;

		final float minx = minXCoord;
		final float miny = minYCoord;
		final float maxx = maxXCoord;
		final float maxy = maxYCoord;

		// Assign to a grid
		final float binWidth = radius * 1.01f;
		final int nXBins = 1 + (int) ((maxx - minx) / binWidth);
		final int nYBins = 1 + (int) ((maxy - miny) / binWidth);
		Molecule[][] grid = new Molecule[nXBins][nYBins];
		for (int i = 0; i < xcoord.length; i++)
		{
			final float x = xcoord[i];
			final float y = ycoord[i];
			final int xBin = (int) ((x - minx) / binWidth);
			final int yBin = (int) ((y - miny) / binWidth);
			// Build a single linked list
			grid[xBin][yBin] = new Molecule(i, x, y, grid[xBin][yBin]);
		}

		Molecule[] neighbours = new Molecule[5];
		final float radius2 = radius * radius;
		for (int yBin = 0; yBin < nYBins; yBin++)
		{
			for (int xBin = 0; xBin < nXBins; xBin++)
			{
				for (Molecule m1 = grid[xBin][yBin]; m1 != null; m1 = m1.next)
				{
					// Build a list of which cells to compare up to a maximum of 4
					//      | 0,0  |  1,0
					// ------------+-----
					// -1,1 | 0,1  |  1,1

					int count = 1;
					neighbours[0] = m1.next;

					if (yBin < nYBins - 1)
					{
						neighbours[count++] = grid[xBin][yBin + 1];
						if (xBin > 0)
							neighbours[count++] = grid[xBin - 1][yBin + 1];
					}
					if (xBin < nXBins - 1)
					{
						neighbours[count++] = grid[xBin + 1][yBin];
						if (yBin < nYBins - 1)
							neighbours[count++] = grid[xBin + 1][yBin + 1];
					}

					// Compare to neighbours
					while (count-- > 0)
					{
						for (Molecule m2 = neighbours[count]; m2 != null; m2 = m2.next)
						{
							if (m1.distance2(m2) < radius2)
							{
								sum++;
							}
						}
					}
				}
			}
		}

		return sum * 2;
	}

	/**
	 * Compute Ripley's L-function.
	 * <p>
	 * See http://en.wikipedia.org/wiki/Spatial_descriptive_statistics#Ripley.27s_K_and_L_functions
	 * 
	 * @param radius
	 *            The radius
	 * @return The L-function score
	 */
	public double ripleysLFunction(double radius)
	{
		double k = ripleysKFunction(radius);
		return Math.sqrt(k / Math.PI);
	}

	/**
	 * Compute Ripley's K-function.
	 * <p>
	 * See http://en.wikipedia.org/wiki/Spatial_descriptive_statistics#Ripley.27s_K_and_L_functions
	 * 
	 * @param density
	 *            The density score for each particle
	 * @param radius
	 *            The radius at which the density was computed
	 * @return The K-function score
	 * @see {@link #calculateDensity(float, boolean)}
	 * @see {@link #calculateSquareDensity(float, int, boolean)}
	 */
	public double ripleysKFunction(int[] density, double radius)
	{
		if (radius < 0)
			throw new IllegalArgumentException("Radius must be positive");
		if (density.length != xcoord.length)
			throw new IllegalArgumentException("Input density array must match the number of coordinates");

		// Count the number of points within the distance 
		int sum = 0;
		for (int d : density)
		{
			sum += d;
		}

		// Normalise
		double scale = area / ((double) density.length * (double) density.length);
		double k = sum * scale;

		return k;
	}

	/**
	 * Compute Ripley's L-function.
	 * <p>
	 * See http://en.wikipedia.org/wiki/Spatial_descriptive_statistics#Ripley.27s_K_and_L_functions
	 * 
	 * @param density
	 *            The density score for each particle
	 * @param radius
	 *            The radius at which the density was computed
	 * @return The K-function score
	 * @see {@link #calculateDensity(float, boolean)}
	 * @see {@link #calculateSquareDensity(float, int, boolean)}
	 */
	public double ripleysLFunction(int[] density, double radius)
	{
		double k = ripleysKFunction(density, radius);
		return Math.sqrt(k / Math.PI);
	}

	/**
	 * Compute the core radius for each point to have n closest neighbours and the minimum reachability distance of a
	 * point from another core point.
	 * <p>
	 * This is an implementation of the OPTICS method. Mihael Ankerst, Markus M Breunig, Hans-Peter Kriegel, and Jorg
	 * Sander. Optics: ordering points to identify the clustering structure. In ACM Sigmod Record, volume 28, pages
	 * 49–60. ACM, 1999.
	 * <p>
	 * The returned results are the output of {@link #extractDBSCANClustering(OPTICSResult[], float)} with the
	 * configured generating distance. Note that the generating distance is set using
	 * {@link #computeGeneratingDistance(int)}. The distance is stored in the results.
	 * <p>
	 * The tracker can be used to follow progress (see {@link #setTracker(TrackProgress)}).
	 *
	 * @param minPts
	 *            the min points for a core object (recommended range 10-20)
	 * @return the results
	 */
	public Optics optics(int minPts)
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
	 * The returned results are the output of {@link #extractDBSCANClustering(OPTICSResult[], float)} with the
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
	public Optics optics(float generatingDistanceE, int minPts)
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
	 * The returned results are the output of {@link #extractDBSCANClustering(OPTICSResult[], float)} with the
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
	public Optics optics(float generatingDistanceE, int minPts, boolean clearMemory)
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
		// Ensure the UNDEFINED distance is above the generating distance
		double maxDistance = generatingDistanceE * 1.01;
		OPTICSResultList results = new OPTICSResultList(size, maxDistance);
		for (int i = 0; i < size; i++)
		{
			if (!grid.setOfObjects[i].processed)
			{
				if (expandClusterOrder(grid.setOfObjects[i], e, minPts, results))
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

		Optics optics = null;
		if (!stopped)
		{
			optics = new Optics(minPts, generatingDistanceE, results.list);
			final int nClusters = extractDBSCANClustering(optics, generatingDistanceE);
			if (tracker != null)
			{
				tracker.log("Finished OPTICS: " + Utils.pleural(nClusters, "Cluster"));
			}
		}

		if (clearMemory)
			clearOpticsMemory();

		return optics;
	}

	private OPTICSMoleculeGrid grid;
	private OPTICSMoleculeList orderSeeds;
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
			orderSeeds = new OPTICSMoleculeList(size);
			neighbours = new OPTICSMoleculeList(size);
			floatArray = new float[size];
		}
		else
		{
			// This is the same distance so the objects can be reused
			grid.reset();
			orderSeeds.clear();
			neighbours.clear();
		}
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
	 * Checks for optics memory structures stored in memory.
	 *
	 * @return true, if successful
	 */
	public boolean hasOpticsMemory()
	{
		return grid != null;
	}

	/**
	 * Clear memory used by the OPTICS algorithm
	 */
	public void clearOpticsMemory()
	{
		grid = null;
		orderSeeds = null;
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
	 * @param clusterId
	 *            the cluster id
	 * @param orderedFile
	 *            the results
	 * @return true, if the algorithm has received a shutdown signal
	 */
	private boolean expandClusterOrder(OPTICSMolecule object, float e, int minPts, OPTICSResultList orderedFile)
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

		findNeighbours(object, e);
		object.processed = true;
		setCoreDistance(minPts, neighbours, object);
		if (orderedFile.add(object))
			return true;
		if (object.coreDistance != UNDEFINED)
		{
			// Create seed-list for further expansion.
			// The next counter is used to ensure we sort only the remaining entries in the seed list.
			int next = 0;
			orderSeeds.clear();
			update(orderSeeds, neighbours, object, UNDEFINED, next);

			while (next < orderSeeds.size)
			{
				final OPTICSMolecule currentObject = orderSeeds.get(next++);
				//			if (currentObject.processed)
				//			{
				//				System.out.println("Error");
				//				continue;
				//			}
				findNeighbours(currentObject, e);
				currentObject.processed = true;
				setCoreDistance(minPts, neighbours, currentObject);
				if (orderedFile.add(currentObject))
					return true;

				if (object.coreDistance != UNDEFINED)
					update(orderSeeds, neighbours, currentObject, UNDEFINED, next);
			}
		}
		return false;
	}

	private void setCoreDistance(int minPts, OPTICSMoleculeList neighbours, OPTICSMolecule currentObject)
	{
		final int size = neighbours.size;
		if (size < minPts)
			return;
		final OPTICSMolecule[] list = neighbours.list;
		for (int i = size; i-- > 0;)
			floatArray[i] = list[i].d;
		Arrays.sort(floatArray, 0, size);
		currentObject.coreDistance = floatArray[minPts - 1];
	}

	/**
	 * Find neighbours. Note that the OPTICS paper appears to include the actual point in the list of neighbours (where
	 * the distance would be 0).
	 *
	 * @param object
	 *            the object
	 * @param e
	 *            the generating distance
	 */
	private void findNeighbours(OPTICSMolecule object, float e)
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
	 * @param UNDEFINED
	 * @param next
	 */
	private void update(OPTICSMoleculeList orderSeeds, OPTICSMoleculeList neighbours, OPTICSMolecule centreObject,
			float UNDEFINED, int next)
	{
		final float c_dist = centreObject.coreDistance;
		for (int i = neighbours.size; i-- > 0;)
		{
			final OPTICSMolecule object = neighbours.get(i);
			if (!object.processed)
			{
				final float new_r_dist = Math.max(c_dist, object.d);
				if (object.reachabilityDistance == UNDEFINED)
				{
					object.reachabilityDistance = new_r_dist;
					object.predecessor = centreObject.id;

					// TODO - Insert in the seeds at the correct position to avoid the full sort
					orderSeeds.add(object);
				}
				else // This is already in the list
				{
					if (new_r_dist < object.reachabilityDistance)
					{
						object.reachabilityDistance = new_r_dist;
						object.predecessor = centreObject.id;

						// TODO - Move up the order to the correct position to avoid the full sort
					}
				}
			}
			//			// Q. What if it has been processed but the reachability distance is lower?
			//			else if (Math.max(c_dist, object.d) < object.r)
			//			{
			//				// This is a 'feature' of the original algorithm. 
			//				// This may be acceptable as the shape of the cluster-order plot has minima 
			//				// representing the centres of clusters.
			//			}
		}

		// Order by reachability distance
		// Note the original algorithm uses an ordered queue. 
		// Here we just sort the list which is slower (but works).
		orderSeeds.sort(next);
	}

	/**
	 * Extract DBSCAN clustering from the cluster ordered objects returned from {@link #optics(float, int, boolean)}.
	 * <p>
	 * The generating distance E must be less than or equal to the generating distance used during OPTICS.
	 *
	 * @param clusterOrderedObjects
	 *            the cluster ordered objects
	 * @param generatingDistanceE
	 *            the generating distance E
	 * @return the number of clusters
	 */
	public int extractDBSCANClustering(Optics optics, float generatingDistanceE)
	{
		if (generatingDistanceE > optics.generatingDistance)
			throw new IllegalArgumentException(
					"The generating distance must not be above the distance used during OPTICS");

		// Reset cluster Id
		int clusterId = NOISE;
		final OPTICSResult[] clusterOrderedObjects = optics.opticsResults;
		for (int i = 0; i < clusterOrderedObjects.length; i++)
		{
			final OPTICSResult object = clusterOrderedObjects[i];
			if (object.reachabilityDistance > generatingDistanceE)
			{
				// This is a point not connected to the previous one.
				// Note that the reachability-distance of the first object in
				// the cluster-ordering is always UNDEFINED and that we as-
				// sume UNDEFINED to be greater than any defined distance
				if (object.coreDistance <= generatingDistanceE)
				{
					// new cluster
					clusterId++;
					object.clusterId = clusterId;
				}
				else
					object.clusterId = NOISE;
			}
			else
				object.clusterId = clusterId;
		}
		return clusterId;
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
	 * Represent a Steep Area. This is used in the OPTICS algorithm to extract clusters.
	 */
	private class SteepArea
	{
		int s, e;
		double maximum;

		SteepArea(int s, int e, double maximum)
		{
			this.s = s;
			this.e = e;
			this.maximum = maximum;
		}

		@Override
		public String toString()
		{
			return String.format("s=%d, e=%d, max=%f", s, e, maximum);
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
	 * Represents a cluster from the OPTICS algorithm
	 */
	public class OPTICSCluster
	{
		/** The start of the cluster. */
		final int start;

		/** The end of the cluster. */
		final int end;

		/** The cluster id. */
		final int clusterId;

		/** The children. */
		private LinkedList<OPTICSCluster> children = null;

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

		private void addChildCluster(OPTICSCluster child)
		{
			if (children == null)
				children = new LinkedList<OPTICSCluster>();
			children.add(child);
		}

		public int nChildren()
		{
			return (children == null) ? 0 : children.size();
		}
	}

	/**
	 * Extract clusters from the reachability distance profile.
	 * <p>
	 * The min points should be equal to the min points used during OPTICS. The xi parameter can be used to control
	 * the steepness of the points a cluster starts with and ends with. Higher ξ-values can be used to
	 * find only the most significant clusters, lower ξ-values to find less significant clusters.
	 *
	 * @param optics
	 *            the optics result
	 * @param xi
	 *            the clustering parameter (xi).
	 * @param minimum
	 *            Set to true to return only clusters that do not contain other clusters
	 * @param noCorrect
	 *            Set to true to not correct the ends of steep up areas (matching the original algorithm)
	 */
	public void extractClusters(Optics optics, double xi, boolean minimum, boolean noCorrect)
	{
		// This code is based on the original OPTICS paper and an R-implementation available here:
		// https://cran.r-project.org/web/packages/dbscan/ 
		// There is also a Java implementation within the ELKI project:
		// https://elki-project.github.io/
		// The ELKI project is used for JUnit testing this implementation.

		TurboList<SteepDownArea> setOfSteepDownAreas = new TurboList<SteepDownArea>();
		TurboList<OPTICSCluster> setOfClusters = new TurboList<OPTICSCluster>();
		int index = 0;
		double mib = 0;
		final int size = optics.size();
		final int minPts = optics.minPts;
		final double ixi = 1 - xi;
		// For simplicity we assume that the profile does not contain NaN values.
		// Positive infinity values are for points with no reachability distance.
		final double[] r = optics.getReachabilityDistanceProfile(false);
		int clusterId = 0;
		optics.resetClusterIds();
		while (index < size)
		{
			mib = Math.max(mib, r[index]);
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
				while (valid(index + 1, size))
				{
					index++;
					if (steepDown(index, r, ixi))
					{
						endSteep = index + 1;
						continue;
					}
					if (!steepDown(index, r, 1) || index - endSteep > minPts)
					{
						break;
					}
				}
				SteepDownArea sda = new SteepDownArea(startSteep, endSteep, startValue);
				System.out.println("New steep down area:" + sda);
				setOfSteepDownAreas.add(sda);
				continue;
			}
			if (steepUp(index, r, ixi))
			{
				// Update mib values with current mib and filter
				updateFilterSDASet(mib, setOfSteepDownAreas, ixi);
				SteepArea sua;
				{
					int startSteep = index;
					int endSteep = index + 1;
					mib = r[index];
					double eSuccessor = (!valid(index + 1, size)) ? Double.POSITIVE_INFINITY : r[index + 1];
					if (eSuccessor != Double.POSITIVE_INFINITY)
					{
						while (valid(index + 1, size))
						{
							index++;
							if (steepUp(index, r, ixi))
							{
								endSteep = index + 1;
								mib = r[index];
								eSuccessor = (!valid(index + 1, size)) ? Double.POSITIVE_INFINITY : r[index + 1];
								if (eSuccessor == Double.POSITIVE_INFINITY)
								{
									endSteep--;
									break;
								}
								continue;
							}
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
					sua = new SteepArea(startSteep, endSteep, eSuccessor);
					System.out.println("New steep up area:" + sua);
				}
				final double threshold = mib * ixi;
				for (int i = setOfSteepDownAreas.size(); i-- > 0;)
				{
					final SteepDownArea sda = setOfSteepDownAreas.getf(i);

					// Condition 3B 
					if (threshold < sda.mib)
						continue;

					// Default values 
					int cstart = sda.s;
					int cend = sua.e;

					// Credit to ELKI
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
					}

					// This NOT in the original article - credit to ELKI for finding this.
					// See http://elki.dbs.ifi.lmu.de/browser/elki/elki/src/main/java/de/lmu/ifi/dbs/elki/algorithm/clustering/optics/OPTICSXi.java
					// Ensure that the predecessor is in the current cluster. This filter
					// removes common artifacts from the Xi method.
					if (!noCorrect)
					{
						while (cend > cstart)
						{
							int tmp2 = optics.get(cend).predecessor;
							if (tmp2 >= cstart && tmp2 < cend)
								break;
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
					OPTICSCluster cluster = new OPTICSCluster(cstart, cend, ++clusterId);

					// Assign all points not currently in a cluster (thus respecting the hierarchy)
					for (int ii = cstart; ii <= cend; ii++)
					{
						// Use the original order
						if (optics.get(ii).clusterId == 0)
							optics.get(ii).clusterId = clusterId;
					}

					{
						// Build the hierarchy of clusters
						Iterator<OPTICSCluster> iter = setOfClusters.iterator();
						while (iter.hasNext())
						{
							OPTICSCluster child = iter.next();
							if (cluster.start <= child.start && child.end <= cluster.end)
							{
								cluster.addChildCluster(child);
								iter.remove();
							}
						}
					}
					setOfClusters.add(cluster);
				}
			}
			else
			{
				// Not steep so move on
				index++;
			}
		}

		// Finalise
		optics.clustering = setOfClusters.toArray(new OPTICSCluster[setOfClusters.size()]);
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
	private void updateFilterSDASet(double mib, TurboList<SteepDownArea> setOfSteepDownAreas, double ixi)
	{
		final double threshold = mib / ixi;
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
				//return sda.maximum*ixi < mib;
				return sda.maximum < threshold;
			}
		});
		// Update mib-values
		for (int i = setOfSteepDownAreas.size(); i-- > 0;)
			if (setOfSteepDownAreas.getf(i).mib < mib)
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
		if (i + 1 == r.length)
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

	// TODO - Provide method to compute the hull of a set of clusters
	public void hull()
	{

	}
}
