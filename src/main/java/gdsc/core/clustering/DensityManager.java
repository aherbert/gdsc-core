package gdsc.core.clustering;

/*----------------------------------------------------------------------------- 
 * GDSC SMLM Software
 * 
 * Copyright (C) 2013 Alex Herbert
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

import org.apache.commons.math3.util.FastMath;

import gdsc.core.logging.TrackProgress;
import gdsc.core.utils.Maths;

/**
 * Calculate the density of localisations around a given position using a square block of specified width
 */
public class DensityManager
{
	/**
	 * Contains the result of the OPTICS algorithm
	 */
	public class OPTICSResult implements Comparable<OPTICSResult>
	{
		final int order;
		final double coreDistance;
		final double reachabilityDistance;

		// TODO - Add anything requires for the DBSCAN method. E.g. should we store 
		// the original point index for use in seeding clusters with the lowest core
		// distance?

		/**
		 * Instantiates a new OPTICS result.
		 *
		 * @param order
		 *            the order
		 * @param coreDistance
		 *            the core distance
		 * @param reachabilityDistance
		 *            the reachability distance
		 */
		public OPTICSResult(int order, double coreDistance, double reachabilityDistance)
		{
			this.order = order;
			this.coreDistance = coreDistance;
			this.reachabilityDistance = reachabilityDistance;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(OPTICSResult o)
		{
			return order - o.order;
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
		private int processed = 0;
		private float c;
		private float r;

		private final int xBin, yBin;
		/**
		 * Working distance to current centre object
		 */
		private float d;

		public OPTICSMolecule(int id, float x, float y, int xBin, int yBin, Molecule next, float undefined)
		{
			super(id, x, y, next);
			this.xBin = xBin;
			this.yBin = yBin;
			c = r = undefined;
		}

		public double getReachabilityDistance()
		{
			return Math.sqrt(r);
		}

		public double getCoreDistance()
		{
			return Math.sqrt(c);
		}

		public OPTICSResult toResult(float undefined, double maxDistance)
		{
			double coreDistance = (r == undefined) ? maxDistance : getCoreDistance();
			double reachabilityDistance = (r == undefined) ? maxDistance : getReachabilityDistance();
			return new OPTICSResult(processed, coreDistance, reachabilityDistance);
		}
	}

	private static class OPTICSComparator implements Comparator<OPTICSMolecule>
	{
		public int compare(OPTICSMolecule o1, OPTICSMolecule o2)
		{
			// Sort by reachability distance
			if (o1.r < o2.r)
				return -1;
			if (o1.r > o2.r)
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
		OPTICSMolecule[] list;
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

		int size()
		{
			return size;
		}

		OPTICSMolecule get(int i)
		{
			return list[i];
		}
	}

	private TrackProgress tracker = null;
	private float[] xcoord, ycoord;
	private float minXCoord, minYCoord, maxXCoord, maxYCoord;
	private int area;

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
		initialise(xcoord, ycoord, bounds);
	}

	private void initialise(float[] xcoord, float[] ycoord, Rectangle bounds)
	{
		if (xcoord == null || ycoord == null || xcoord.length == 0 || xcoord.length != ycoord.length)
			throw new IllegalArgumentException("Results are null or empty or mismatched in length");

		this.xcoord = xcoord;
		this.ycoord = ycoord;

		// Assign localisations & get min bounds
		minXCoord = Float.POSITIVE_INFINITY;
		minYCoord = Float.POSITIVE_INFINITY;
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
		maxXCoord = 0;
		maxYCoord = 0;
		for (int i = 0; i < xcoord.length; i++)
		{
			xcoord[i] -= shiftx;
			ycoord[i] -= shifty;
			if (maxXCoord < xcoord[i])
				maxXCoord = xcoord[i];
			if (maxYCoord < ycoord[i])
				maxYCoord = ycoord[i];
		}

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
	 * Compute the threshold radius for each point to have n closest neighbours.
	 * <p>
	 * This is an implementation of the OPTICS method. Mihael Ankerst, Markus M Breunig, Hans-Peter Kriegel, and Jorg
	 * Sander. Optics: ordering points to identify the clustering structure. In ACM Sigmod Record, volume 28, pages
	 * 49–60. ACM, 1999.
	 * <p>
	 * For each point find the radius where there are n closest neighbours.
	 *
	 * @param generatingDistanceE
	 *            the generating distance E
	 * @param minPts
	 *            the min points for a core object
	 * @return the results
	 */
	public OPTICSResult[] optics(float generatingDistanceE, int minPts)
	{
		if (minPts < 1)
			minPts = 1;
		if (tracker != null)
			tracker.log("Initialising OPTICS ...");

		final float minx = minXCoord;
		final float miny = minYCoord;
		final float maxx = maxXCoord;
		final float maxy = maxYCoord;
		final float xrange = maxx - minx;
		final float yrange = maxy - miny;

		// Ensure the generating distance is not too high. Also set it the max value if it is not valid.
		double maxDistance = Math.sqrt(xrange * xrange + yrange * yrange);
		if (!Maths.isFinite(generatingDistanceE) || generatingDistanceE <= 0 || generatingDistanceE > maxDistance)
			generatingDistanceE = (float) maxDistance;

		// Note: The method and variable names used in this function are designed to match 
		// the pseudocode implementation from the 1999 OPTICS paper.
		// The generating distance (E) used in the paper is the maximum distance at which cluster
		// centres will be formed. This implementation uses the squared distance to avoid sqrt() 
		// function calls.

		final float e = generatingDistanceE * generatingDistanceE;
		// This is actually arbitrary. Use a simple value so we can spot it when debugging.
		final float undefined = -1; // (float) (1.01 * e);

		// Speed this up with a higher resolution grid
		int resolution = determineResolution(xrange, yrange, generatingDistanceE);

		final float binWidth;
		if (resolution == 0)
		{
			// Handle a resolution of zero. This will happen when the generating distance is very small.
			// In this instance we can use a resolution of 1 but change the bin width to something larger.
			resolution = 1;
			binWidth = determineBinWidth(xrange, yrange, generatingDistanceE);
		}
		else
		{
			binWidth = generatingDistanceE / resolution;
		}

		// Assign to a grid
		final int nXBins = 1 + (int) (xrange / binWidth);
		final int nYBins = 1 + (int) (yrange / binWidth);

		OPTICSMolecule[][] grid = new OPTICSMolecule[nXBins][nYBins];
		OPTICSMolecule[] setOfObjects = new OPTICSMolecule[xcoord.length];
		for (int i = 0; i < xcoord.length; i++)
		{
			final float x = xcoord[i];
			final float y = ycoord[i];
			final int xBin = (int) ((x - minx) / binWidth);
			final int yBin = (int) ((y - miny) / binWidth);
			// Build a single linked list
			OPTICSMolecule m = new OPTICSMolecule(i, x, y, xBin, yBin, grid[xBin][yBin], undefined);
			setOfObjects[i] = m;
			grid[xBin][yBin] = m;
		}

		if (tracker != null)
		{
			tracker.log("Running OPTICS ...");
			tracker.progress(0, xcoord.length);
		}

		OPTICSMoleculeList orderSeeds = new OPTICSMoleculeList(xcoord.length);
		OPTICSMoleculeList neighbours = new OPTICSMoleculeList(xcoord.length);
		int order = 0;
		for (int i = 0; i < setOfObjects.length; i++)
		{
			if (setOfObjects[i].processed == 0)
				order = expandClusterOrder(grid, resolution, setOfObjects, setOfObjects[i], e, minPts, undefined,
						orderSeeds, order, neighbours);
		}

		if (tracker != null)
		{
			tracker.log("Finalising OPTICS ...");
			tracker.progress(1.0);
		}

		// Ensure the undefined distance is above the generating distance
		maxDistance = generatingDistanceE * 1.01;
		OPTICSResult[] results = new OPTICSResult[setOfObjects.length];
		for (int i = 0; i < setOfObjects.length; i++)
			results[i] = setOfObjects[i].toResult(undefined, maxDistance);

		// Change to the cluster-order
		Arrays.sort(results);

		return results;
	}

	private int determineResolution(float xrange, float yrange, float generatingDistanceE)
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

	private float determineBinWidth(float xrange, float yrange, float binWidth)
	{
		while (getBins(xrange, yrange, binWidth, 1) > 100000)
		{
			// Dumb implementation that doubles the bin width. A better solution
			// would be to conduct a search for the value with a number of bins close 
			// to the target.
			binWidth *= 2;
		}
		return binWidth;
	}

	private int getBins(float xrange, float yrange, float generatingDistanceE, int resolution)
	{
		final float binWidth = generatingDistanceE / resolution;
		final int nXBins = 1 + (int) (xrange / binWidth);
		final int nYBins = 1 + (int) (yrange / binWidth);
		final int nBins = nXBins * nYBins;
		//System.out.printf("d=%.3f  %d => %d\n", generatingDistanceE, resolution, nBins);
		return nBins;
	}

	private int expandClusterOrder(OPTICSMolecule[][] grid, int resolution, OPTICSMolecule[] setOfObjects,
			OPTICSMolecule object, float e, int minPts, float undefined, OPTICSMoleculeList orderSeeds, int order,
			OPTICSMoleculeList neighbours)
	{
		// TODO: Re-write the algorithm so that each connected cluster is generated
		// in order of reachability distance

		// Note: The original paper just processes the next unprocessed object in an undefined order.
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

		// For tracking progress
		final long total = orderSeeds.list.length;

		findNeighbours(grid, resolution, object, e, neighbours);
		object.processed = ++order;
		if (tracker != null)
			tracker.progress(order, total);

		if (neighbours.size < minPts)
			return order;

		float[] d = new float[neighbours.size];
		d = setCoreDistance(d, minPts, neighbours, object);

		// Create seed-list for further expansion.
		// The next counter is used to ensure we sort only the remaining entries in the seed list.
		int next = 0;
		orderSeeds.clear();
		update(orderSeeds, neighbours, object, undefined, next);

		while (next < orderSeeds.size())
		{
			OPTICSMolecule currentObject = orderSeeds.get(next++);
			//			if (currentObject.processed != 0)
			//			{
			//				System.out.println("Error");
			//				continue;
			//			}
			findNeighbours(grid, resolution, currentObject, e, neighbours);
			currentObject.processed = ++order;
			if (tracker != null)
				tracker.progress(order, total);

			if (neighbours.size() < minPts)
				continue;

			d = setCoreDistance(d, minPts, neighbours, currentObject);

			update(orderSeeds, neighbours, currentObject, undefined, next);
		}

		return order;
	}

	private float[] setCoreDistance(float[] d, int minPts, OPTICSMoleculeList neighbours, OPTICSMolecule currentObject)
	{
		final int size = neighbours.size();
		if (d.length < size)
			d = new float[size];
		OPTICSMolecule[] list = neighbours.list;
		for (int i = size; i-- > 0;)
			d[i] = list[i].d;
		Arrays.sort(d, 0, size);
		currentObject.c = d[minPts - 1];
		return d;
	}

	/**
	 * Find neighbours. Note that the OPTICS paper appears to include the actual point in the list of neighbours (where
	 * the distance would be 0).
	 *
	 * @param grid
	 *            the grid
	 * @param resolution
	 * @param object
	 *            the object
	 * @param e
	 *            the generating distance
	 * @param neighbours
	 *            The working list of neighbours
	 */
	private void findNeighbours(OPTICSMolecule[][] grid, int resolution, OPTICSMolecule object, float e,
			OPTICSMoleculeList neighbours)
	{
		final int xBin = object.xBin;
		final int yBin = object.yBin;

		neighbours.clear();

		// Pre-compute x-bins
		final int[] xBins = new int[2 * resolution + 1];
		int nBins = 0;
		for (int x = -resolution; x <= resolution; x++)
		{
			final int xBin2 = xBin + x;
			if (xBin2 < 0)
				continue;
			if (xBin2 >= grid.length)
				break;
			xBins[nBins++] = xBin2;
		}

		for (int y = -resolution; y <= resolution; y++)
		{
			final int yBin2 = yBin + y;
			if (yBin2 < 0)
				continue;
			if (yBin2 >= grid[0].length)
				break;

			for (int bin = nBins; bin-- > 0;)
			{
				for (Molecule other = grid[xBins[bin]][yBin2]; other != null; other = other.next)
				{
					final float d = object.distance2(other);
					if (d <= e)
					{
						// Build a list of all the neighbours and their working distance
						OPTICSMolecule otherObject = (OPTICSMolecule) other;
						otherObject.d = d;
						neighbours.add(otherObject);
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
	 * @param undefined
	 * @param next
	 */
	private void update(OPTICSMoleculeList orderSeeds, OPTICSMoleculeList neighbours, OPTICSMolecule centreObject,
			float undefined, int next)
	{
		final float c_dist = centreObject.c;
		for (int i = neighbours.size(); i-- > 0;)
		{
			final OPTICSMolecule object = neighbours.get(i);
			if (object.processed == 0)
			{
				final float new_r_dist = Math.max(c_dist, object.d);
				if (object.r == undefined)
				{
					object.r = new_r_dist;
					orderSeeds.add(object);
				}
				else // This is already in the list
				{
					if (new_r_dist < object.r)
						object.r = new_r_dist;
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
		orderSeeds.sort(next);
	}
}
