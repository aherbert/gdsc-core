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

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import gdsc.core.utils.FixedIntList;
import gdsc.core.utils.TurboList;

/**
 * Calculate the density of classes of molecules around a given position.
 */
public class DensityCounter
{
	private static final double ROOT2 = Math.sqrt(2.0);

	/**
	 * Specify a molecule.
	 */
	public interface Molecule
	{
		/**
		 * Gets the x coordinate. This must remain constant for efficient molecule processing.
		 *
		 * @return the x
		 */
		public float getX();

		/**
		 * Gets the y coordinate. This must remain constant for efficient molecule processing.
		 *
		 * @return the y
		 */
		public float getY();

		/**
		 * Gets the id. Must be zero or above.
		 *
		 * @return the id
		 */
		public int getID();
	}

	/**
	 * Provide a simple class that implements the Molecule interface
	 */
	public static class SimpleMolecule implements DensityCounter.Molecule
	{
		private final float x, y;
		private int id;

		/**
		 * Instantiates a new simple molecule.
		 *
		 * @param x
		 *            the x
		 * @param y
		 *            the y
		 */
		public SimpleMolecule(float x, float y)
		{
			this(x, y, 0);
		}

		/**
		 * Instantiates a new simple molecule.
		 *
		 * @param x
		 *            the x
		 * @param y
		 *            the y
		 * @param id
		 *            the id
		 */
		public SimpleMolecule(float x, float y, int id)
		{
			this.x = x;
			this.y = y;
			setID(id);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see gdsc.core.clustering.DensityCounter.Molecule#getX()
		 */
		@Override
		public float getX()
		{
			return x;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see gdsc.core.clustering.DensityCounter.Molecule#getY()
		 */
		@Override
		public float getY()
		{
			return y;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see gdsc.core.clustering.DensityCounter.Molecule#getID()
		 */
		@Override
		public int getID()
		{
			return id;
		}

		/**
		 * Sets the id.
		 *
		 * @param id
		 *            the new id
		 */
		public void setID(int id)
		{
			if (id < 0)
				throw new IllegalArgumentException("Id must be positive");
			this.id = id;
		}
	}

	/**
	 * Wrap input molecules with an index to the original input order
	 */
	private class IndexMolecule implements Molecule
	{
		/** The molecule. */
		Molecule molecule;

		/** The index. */
		final int index;

		/**
		 * Instantiates a new index molecule.
		 *
		 * @param molecule
		 *            the molecule
		 * @param index
		 *            the index
		 */
		IndexMolecule(Molecule molecule, int index)
		{
			this.molecule = molecule;
			this.index = index;
		}

		/**
		 * Gets the x.
		 *
		 * @return the x
		 */
		@Override
		public float getX()
		{
			return molecule.getX();
		}

		/**
		 * Gets the y.
		 *
		 * @return the y
		 */
		@Override
		public float getY()
		{
			return molecule.getY();
		}

		/**
		 * Gets the id.
		 *
		 * @return the id
		 */
		@Override
		public int getID()
		{
			return molecule.getID();
		}
	}

	private class MoleculeList
	{
		int size = 0;
		IndexMolecule[] data = new IndexMolecule[1];

		void add(Molecule m, int index)
		{
			if (size == data.length)
				data = Arrays.copyOf(data, 2 * size);
			data[size++] = new IndexMolecule(m, index);
		}

		IndexMolecule[] toArray()
		{
			return Arrays.copyOf(data, size);
		}
	}

	/** The radius. */
	public final float radius;

	private final float r2;
	private final float xmin, ymin, binWidth;
	private final int nXBins, nYBins, nMolecules;
	private final IndexMolecule[][] grid;

	private final int nonEmpty, maxCellSize;
	
	/** The grid priority. */
	int[] gridPriority = null;

	// Note:
	// Multi-threading is not faster unless the
	// number of molecules is very large and/or the radius (i.e. the total
	// number of comparisons). However at low numbers of comparison the
	// speed slow-down for multi-threading will likely not be noticed.
	private int nThreads = -1;

	/** Synchronised mode. */
	static byte MODE_SYNC = 0;
	/** Non-synchronised mode. */
	static byte MODE_NON_SYNC = 1;
	/** Multi-thread mode. */
	byte multiThreadMode = MODE_NON_SYNC;

	/**
	 * Gets the number of threads to use for multi-threaded algorithms.
	 *
	 * @return the number of threads
	 */
	public int getNumberOfThreads()
	{
		if (nThreads == -1)
			nThreads = Runtime.getRuntime().availableProcessors();
		return nThreads;
	}

	/**
	 * Sets the number of threads to use for multi-threaded algorithms.
	 *
	 * @param nThreads
	 *            the new number of threads
	 */
	public void setNumberOfThreads(int nThreads)
	{
		if (nThreads > 0)
			this.nThreads = nThreads;
		else
			this.nThreads = 1;
	}

	/**
	 * Instantiates a new density counter.
	 *
	 * @param molecules
	 *            the molecules
	 * @param radius
	 *            the radius at which to count the density
	 * @param zeroOrigin
	 *            Set to true if the molecules have a zero origin
	 * @throws IllegalArgumentException
	 *             if results are null or empty
	 */
	public DensityCounter(Molecule[] molecules, float radius, boolean zeroOrigin)
	{
		if (molecules == null || molecules.length == 0)
			throw new IllegalArgumentException("Molecules must not be empty");
		if (Float.isInfinite(radius) || Float.isNaN(radius) || radius <= 0)
			throw new IllegalArgumentException("Radius must be a positive real number");
		this.radius = radius;
		r2 = radius * radius;
		nMolecules = molecules.length;

		if (zeroOrigin)
			xmin = ymin = 0;
		else
		{
			float minx = Float.POSITIVE_INFINITY;
			float miny = Float.POSITIVE_INFINITY;
			for (int i = 0; i < molecules.length; i++)
			{
				final Molecule m = molecules[i];
				if (minx > m.getX())
					minx = m.getX();
				if (miny > m.getY())
					miny = m.getY();
			}
			xmin = minx;
			ymin = miny;
		}

		float xmax = 0;
		float ymax = 0;
		for (int i = 0; i < molecules.length; i++)
		{
			final Molecule m = molecules[i];
			if (xmax < m.getX())
				xmax = m.getX();
			if (ymax < m.getY())
				ymax = m.getY();
		}

		// Create grid
		final float xrange = xmax - xmin;
		final float yrange = ymax - ymin;
		binWidth = determineBinWidth(xrange, yrange, radius);

		nXBins = (int) (1 + Math.floor(xrange / binWidth));
		nYBins = (int) (1 + Math.floor(yrange / binWidth));

		// Assign to grid
		final MoleculeList[] tmp = new MoleculeList[nXBins * nYBins];
		for (int i = 0; i < tmp.length; i++)
			tmp[i] = new MoleculeList();
		for (int i = 0; i < molecules.length; i++)
		{
			final Molecule m = molecules[i];
			tmp[getBin(m.getX(), m.getY())].add(m, i);
		}

		// Convert for efficiency
		grid = new IndexMolecule[tmp.length][];
		int c = 0, max = 0;
		for (int i = 0; i < tmp.length; i++)
		{
			if (tmp[i].size != 0)
			{
				if (max < tmp[i].size)
					max = tmp[i].size;
				c++;
				grid[i] = tmp[i].toArray();
			}
			tmp[i] = null;
		}
		nonEmpty = c;
		maxCellSize = max;
	}

	/**
	 * Gets the x bin.
	 *
	 * @param x
	 *            the x
	 * @return the x bin
	 */
	private int getXBin(float x)
	{
		return (int) ((x - xmin) / binWidth);
	}

	/**
	 * Gets the y bin.
	 *
	 * @param y
	 *            the y
	 * @return the y bin
	 */
	private int getYBin(float y)
	{
		return (int) ((y - ymin) / binWidth);
	}

	/**
	 * Gets the bin.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @return the bin
	 */
	private int getBin(float x, float y)
	{
		return getBin(getXBin(x), getYBin(y));
	}

	/**
	 * Gets the bin.
	 *
	 * @param xbin
	 *            the xbin
	 * @param ybin
	 *            the ybin
	 * @return the bin
	 */
	private int getBin(int xbin, int ybin)
	{
		return ybin * nXBins + xbin;
	}

	/**
	 * Gets the bin.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @return the bin
	 */
	private int getBinSafe(float x, float y)
	{
		final int xBin = clip(nXBins, getXBin(x));
		final int yBin = clip(nYBins, getYBin(y));
		return getBin(xBin, yBin);
	}

	private static int clip(int upper, int value)
	{
		if (value < 0)
			return 0;
		if (value >= upper)
			return upper - 1;
		return value;
	}

	/**
	 * Determine bin width. This must be equal to or greater than the radius. The iwdth is increased if the total number
	 * of bins is prohibitively large.
	 *
	 * @param xrange
	 *            the xrange
	 * @param yrange
	 *            the yrange
	 * @param radius
	 *            the radius
	 * @return the bin width
	 */
	private static float determineBinWidth(float xrange, float yrange, float radius)
	{
		float binWidth = radius;
		while (getBins(xrange, yrange, binWidth) > 100000)
			// Dumb implementation that increase the bin width so that each cell doubles in size.
			// A better solution would be to conduct a search for the value with a number of bins close
			// to the target.
			binWidth *= ROOT2;
		return binWidth;
	}

	/**
	 * Gets the bins.
	 *
	 * @param xrange
	 *            the xrange
	 * @param yrange
	 *            the yrange
	 * @param binWidth
	 *            the bin width
	 * @return the bins
	 */
	private static double getBins(float xrange, float yrange, float binWidth)
	{
		// Use a double in case the numbers are very high. This occurs when the bin width is too small.
		final double x = xrange / binWidth;
		final double y = yrange / binWidth;
		if (x * y > Integer.MAX_VALUE)
			return x * y;

		final double nXBins = 1 + Math.floor(x);
		final double nYBins = 1 + Math.floor(y);
		return nXBins * nYBins;
	}

	/**
	 * Count the density of each class of molecule around each molecule. Counts are returned using the original input
	 * order of molecules.
	 * <p>
	 * This method allows the molecule ID to be changed following creation of the counter but coordinates must be the
	 * same. The maximum ID must be input to allow efficient counting.
	 * <p>
	 * This method is optimised for use when the number of IDs is small. If the number of IDs is large then the routine
	 * may run out of memory.
	 * <p>
	 * NOTE: Package level for JUnit test only
	 *
	 * @param maxID
	 *            the max ID of molecules
	 * @return the counts
	 */
	int[][] countAllSimple(int maxID)
	{
		return countAllSimple(getMolecules(), r2, maxID);
	}

	/**
	 * Gets the molecules. This returns a copy of the molecule extracted from their storage format into a new array.
	 *
	 * @return the molecules
	 */
	public Molecule[] getMolecules()
	{
		// Extract the molecules
		final Molecule[] molecules = new Molecule[nMolecules];
		for (int i = 0; i < grid.length; i++)
		{
			final IndexMolecule[] cell1 = grid[i];
			if (cell1 == null)
				continue;
			for (int j = cell1.length; j-- > 0;)
				molecules[cell1[j].index] = cell1[j].molecule;
		}
		return molecules;
	}

	/**
	 * Count the density of each class of molecule around each molecule. Counts are returned using the original input
	 * order of molecules.
	 * <p>
	 * This method is optimised for use when the number of IDs is small. If the number of IDs is large then the routine
	 * may run out of memory.
	 *
	 * @param molecules
	 *            the molecules
	 * @param r
	 *            the radius distance
	 * @param maxID
	 *            the max ID of molecules
	 * @return the counts
	 */
	public static int[][] countAll(Molecule[] molecules, float r, int maxID)
	{
		return countAllSimple(molecules, r * r, maxID);
	}

	/**
	 * Count the density of each class of molecule around each molecule. Counts are returned using the original input
	 * order of molecules.
	 * <p>
	 * This method is optimised for use when the number of IDs is small. If the number of IDs is large then the routine
	 * may run out of memory.
	 *
	 * @param molecules
	 *            the molecules
	 * @param r2
	 *            the squared radius distance
	 * @param maxID
	 *            the max ID of molecules
	 * @return the counts
	 */
	private static int[][] countAllSimple(Molecule[] molecules, float r2, int maxID)
	{
		final int nMolecules = molecules.length;
		final int[][] results = new int[nMolecules][maxID + 1];

		// All-vs-all
		for (int i = 0; i < nMolecules; i++)
		{
			final Molecule m1 = molecules[i];
			final int[] count1 = results[i];
			final float x = m1.getX();
			final float y = m1.getY();
			final int id = m1.getID();

			// Self count
			count1[id]++;

			for (int j = i + 1; j < nMolecules; j++)
			{
				final Molecule m2 = molecules[j];
				if (distance2(x, y, m2) < r2)
				{
					final int[] count2 = results[j];
					count1[m2.getID()]++;
					count2[id]++;
				}
			}
		}

		return results;
	}

	/**
	 * Count the density of each class of molecule around each input molecule. Counts are returned using the original
	 * input order of molecules.
	 * <p>
	 * This method is optimised for use when the number of IDs is small. If the number of IDs is large then the routine
	 * may run out of memory.
	 * <p>
	 * NOTE: Package level for JUnit test only
	 *
	 * @param molecules2
	 *            the molecules to around which to search
	 * @param maxID
	 *            the max ID of molecules
	 * @return the counts
	 */
	int[][] countAllSimple(Molecule[] molecules2, int maxID)
	{
		return countAllSimple(getMolecules(), molecules2, r2, maxID);
	}

	/**
	 * Count the density of each class of molecule around each input molecule. Counts are returned using the original
	 * input order of molecules.
	 * <p>
	 * This method is optimised for use when the number of IDs is small. If the number of IDs is large then the routine
	 * may run out of memory.
	 *
	 * @param molecules
	 *            the molecules to create the density space
	 * @param molecules2
	 *            the molecules to around which to search
	 * @param r
	 *            the radius distance
	 * @param maxID
	 *            the max ID of molecules
	 * @return the counts
	 */
	public static int[][] countAll(Molecule[] molecules, Molecule[] molecules2, float r, int maxID)
	{
		return countAllSimple(molecules, molecules2, r * r, maxID);
	}

	/**
	 * Count the density of each class of molecule around each input molecule. Counts are returned using the original
	 * input order of molecules.
	 * <p>
	 * This method is optimised for use when the number of IDs is small. If the number of IDs is large then the routine
	 * may run out of memory.
	 *
	 * @param molecules
	 *            the molecules to create the density space
	 * @param molecules2
	 *            the molecules to around which to search
	 * @param r2
	 *            the squared radius distance
	 * @param maxID
	 *            the max ID of molecules
	 * @return the counts
	 */
	private static int[][] countAllSimple(Molecule[] molecules, Molecule[] molecules2, float r2, int maxID)
	{
		final int nMolecules = molecules2.length;
		final int[][] results = new int[nMolecules][maxID + 1];

		// All-vs-all
		for (int i = 0; i < nMolecules; i++)
		{
			final Molecule m1 = molecules2[i];
			final int[] count1 = results[i];
			final float x = m1.getX();
			final float y = m1.getY();

			for (int j = 0; j < molecules.length; j++)
			{
				final Molecule m2 = molecules[j];
				if (distance2(x, y, m2) < r2)
					count1[m2.getID()]++;
			}
		}

		return results;
	}

	/**
	 * Count the density of each class of molecule around each molecule. Counts are returned using the original input
	 * order of molecules.
	 * <p>
	 * This method allows the molecule ID to be changed following creation of the counter but coordinates must be the
	 * same. The maximum ID must be input to allow efficient counting.
	 * <p>
	 * This method is optimised for use when the number of IDs is small. If the number of IDs is large then the routine
	 * may run out of memory.
	 *
	 * @param maxID
	 *            the max ID of molecules
	 * @return the counts
	 */
	public int[][] countAll(int maxID)
	{
		final int[][] results = new int[nMolecules][maxID + 1];

		// Single threaded
		if (getNumberOfThreads() == 1)
		{
			final int[] neighbours = new int[4];
			for (int i = 0; i < grid.length; i++)
			{
				final IndexMolecule[] cell1 = grid[i];
				if (cell1 == null)
					continue;

				final int count = getNeighbours4(neighbours, i);

				for (int j = cell1.length; j-- > 0;)
				{
					final IndexMolecule m1 = cell1[j];
					final int[] count1 = results[m1.index];
					final float x = m1.getX();
					final float y = m1.getY();
					final int id = m1.getID();

					// Self count
					count1[id]++;

					// Compare all inside the bin
					for (int k = j; k-- > 0;)
					{
						final IndexMolecule m2 = cell1[k];
						if (distance2(x, y, m2) < r2)
						{
							final int[] count2 = results[m2.index];
							count1[m2.getID()]++;
							count2[id]++;
						}
					}

					// Compare to neighbours
					for (int c = count; c-- > 0;)
					{
						final IndexMolecule[] cell2 = grid[neighbours[c]];
						for (int k = cell2.length; k-- > 0;)
						{
							final IndexMolecule m2 = cell2[k];
							if (distance2(x, y, m2) < r2)
							{
								final int[] count2 = results[m2.index];
								count1[m2.getID()]++;
								count2[id]++;
							}
						}
					}
				}
			}
		}
		else
		{
			// Multi-threaded

			createGridPriority();

			final int nThreads = Math.min(this.nThreads, gridPriority.length);

			// Split the entries evenly over each thread
			// This should fairly allocate the density to all processing threads
			final int[] process = new int[nonEmpty];
			for (int i = 0, j = 0, k = 0; i < gridPriority.length; i++)
			{
				process[i] = gridPriority[j];
				j += nThreads;
				if (j >= gridPriority.length)
					j = ++k;
			}

			// Use an executor service so that we know when complete
			final ExecutorService executor = Executors.newFixedThreadPool(nThreads);
			final TurboList<Future<?>> futures = new TurboList<>(nThreads);

			final int nPerThread = (int) Math.ceil((double) process.length / nThreads);
			for (int from = 0; from < process.length;)
			{
				final int to = Math.min(from + nPerThread, process.length);
				if (multiThreadMode == MODE_NON_SYNC)
					futures.add(executor.submit(new CountWorker2(results, process, from, to)));
				else
					futures.add(executor.submit(new CountWorker(results, process, from, to)));
				from = to;
			}
			// Wait for all to finish
			for (int t = futures.size(); t-- > 0;)
				try
				{
					// The future .get() method will block until completed
					futures.get(t).get();
				}
				catch (final Exception e)
				{
					// This should not happen.
					// Ignore it and allow processing to continue (the number of neighbour samples will just be smaller).
					e.printStackTrace();
				}

			executor.shutdown();
		}

		return results;
	}

	private void createGridPriority()
	{
		if (gridPriority == null)
		{
			// Histogram the size of each cell.
			// This should not be a memory problem as no cell will be larger than nMolecules.
			final int[] h = new int[maxCellSize + 1];
			for (int i = 0; i < grid.length; i++)
			{
				final IndexMolecule[] cell1 = grid[i];
				if (cell1 == null)
					continue;
				h[cell1.length]++;
			}

			// Allocate storage
			final FixedIntList[] indices = new FixedIntList[h.length];
			for (int i = h.length; i-- > 0;)
				if (h[i] > 0)
					indices[i] = new FixedIntList(h[i]);

			// Layout
			for (int i = 0; i < grid.length; i++)
			{
				final IndexMolecule[] cell1 = grid[i];
				if (cell1 == null)
					continue;
				indices[cell1.length].add(i);
			}

			// Record in reverse order (largest first)
			gridPriority = new int[nonEmpty];
			for (int i = indices.length, j = 0; i-- > 0;)
				if (indices[i] != null)
				{
					indices[i].copy(gridPriority, j);
					j += indices[i].size();
				}

			// Old method uses a full sort

			//			// Count the number of entries in each grid cell
			//			int[][] count = new int[nonEmpty][2];
			//			for (int i = 0, c = 0; i < grid.length; i++)
			//			{
			//				final IndexMolecule[] cell1 = grid[i];
			//				if (cell1 == null)
			//					continue;
			//				count[c][0] = cell1.length;
			//				count[c][1] = i;
			//				c++;
			//			}
			//
			//			// Sort by the size
			//			Arrays.sort(count, new Comparator<int[]>()
			//			{
			//				public int compare(int[] o1, int[] o2)
			//				{
			//					return o2[0] - o1[0];
			//				}
			//			});
			//			gridPriority = new int[nonEmpty];
			//			for (int i = 0; i < gridPriority.length; i++)
			//				gridPriority[i] = count[i][1];
		}
	}

	private int addNeighbour(int[] neighbours, int count, int index)
	{
		if (grid[index] != null)
			neighbours[count++] = index;
		return count;
	}

	/**
	 * For processing the countAll method using thread-safe writing to the results array with synchronized
	 */
	private class CountWorker implements Runnable
	{
		final int[][] results;
		final int[] process;
		final int from;
		final int to;

		// For thread safety we create a stash of updates to the results which are then applied
		// in a synchronised method
		int c = 0;
		int[] indexData = new int[2000];
		int[] idData = new int[2000];

		void addSingle(int index, int id)
		{
			indexData[c] = index;
			idData[c] = id;
			if (++c == indexData.length)
				flushSingle();
		}

		void flushSingle()
		{
			synchronized (results)
			{
				while (c-- > 0)
					results[indexData[c]][idData[c]]++;
			}
			c = 0;
		}

		public CountWorker(int[][] results, int[] process, int from, int to)
		{
			this.results = results;
			this.process = process;
			this.from = from;
			this.to = to;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			// Temp storage
			final int countSize = results[0].length;
			int n = 0;
			for (int index = from; index < to; index++)
				n += grid[process[index]].length;
			final int[][] results1 = new int[n][countSize + 1];

			final int[] neighbours = new int[4];
			for (int index = from; index < to; index++)
			{
				final int i = process[index];
				final IndexMolecule[] cell1 = grid[i];

				final int count = getNeighbours4(neighbours, i);

				for (int j = cell1.length; j-- > 0;)
				{
					final IndexMolecule m1 = cell1[j];
					final float x = m1.getX();
					final float y = m1.getY();
					final int id = m1.getID();

					// Reset
					final int[] count1 = results1[--n];

					// Self count
					count1[countSize] = m1.index;
					count1[id]++;

					// Compare all inside the bin
					for (int k = j; k-- > 0;)
					{
						final IndexMolecule m2 = cell1[k];
						if (distance2(x, y, m2) < r2)
						{
							count1[m2.getID()]++;
							addSingle(m2.index, id);
						}
					}

					// Compare to neighbours
					for (int c = count; c-- > 0;)
					{
						final IndexMolecule[] cell2 = grid[neighbours[c]];
						for (int k = cell2.length; k-- > 0;)
						{
							final IndexMolecule m2 = cell2[k];
							if (distance2(x, y, m2) < r2)
							{
								count1[m2.getID()]++;
								addSingle(m2.index, id);
							}
						}
					}
				}
			}

			synchronized (results)
			{
				while (c-- > 0)
					results[indexData[c]][idData[c]]++;

				for (int i = 0; i < results1.length; i++)
				{
					final int[] count1 = results1[i];
					// We store the index at the end of the array
					final int[] count = results[count1[countSize]];
					for (int j = 0; j < countSize; j++)
						count[j] += count1[j];
				}
			}
		}
	}

	/**
	 * For processing the countAll method using an all neighbour cell comparison. This doubles the number of distance
	 * comparisons but does not require synchronisation.
	 */
	private class CountWorker2 implements Runnable
	{
		final int[][] results;
		final int[] process;
		final int from;
		final int to;

		public CountWorker2(int[][] results, int[] process, int from, int to)
		{
			this.results = results;
			this.process = process;
			this.from = from;
			this.to = to;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			final int[] neighbours = new int[8];
			for (int index = from; index < to; index++)
			{
				final int i = process[index];
				final IndexMolecule[] cell1 = grid[i];

				final int count = getNeighbours8(neighbours, i);

				for (int j = cell1.length; j-- > 0;)
				{
					final IndexMolecule m1 = cell1[j];
					final int[] count1 = results[m1.index];
					final float x = m1.getX();
					final float y = m1.getY();

					// Compare all inside the bin. This will self-count
					for (int k = cell1.length; k-- > 0;)
					{
						final IndexMolecule m2 = cell1[k];
						if (distance2(x, y, m2) < r2)
							count1[m2.getID()]++;
					}

					// Compare to neighbours
					for (int c = count; c-- > 0;)
					{
						final IndexMolecule[] cell2 = grid[neighbours[c]];
						for (int k = cell2.length; k-- > 0;)
						{
							final IndexMolecule m2 = cell2[k];
							if (distance2(x, y, m2) < r2)
								count1[m2.getID()]++;
						}
					}
				}
			}
		}
	}

	/**
	 * Build a list of which cells to compare up to a maximum of 4
	 *
	 * @param neighbours
	 *            the neighbours
	 * @param i
	 *            the grid index
	 * @return the number of neighbours
	 */
	private int getNeighbours4(int[] neighbours, int i)
	{
		// Build a list of which cells to compare up to a maximum of 4
		//      | 0,0  |  1,0
		// ------------+-----
		// -1,1 | 0,1  |  1,1
		int count = 0;
		final int xBin = i % nXBins;
		final int yBin = i / nXBins;
		if (yBin < nYBins - 1)
		{
			if (xBin > 0)
				count = addNeighbour(neighbours, count, i + nXBins - 1);
			count = addNeighbour(neighbours, count, i + nXBins);
			if (xBin < nXBins - 1)
			{
				count = addNeighbour(neighbours, count, i + nXBins + 1);
				count = addNeighbour(neighbours, count, i + 1);
			}
		}
		else if (xBin < nXBins - 1)
			count = addNeighbour(neighbours, count, i + 1);
		return count;
	}

	/**
	 * Build a list of which cells to compare up to a maximum of 8
	 *
	 * @param neighbours
	 *            the neighbours
	 * @param i
	 *            the grid index
	 * @return the number of neighbours
	 */
	private int getNeighbours8(int[] neighbours, int i)
	{
		// Build a list of which cells to compare up to a maximum of 8
		// -1,-1 | 0,-1  |  1,-1
		// ------------+--------
		// -1, 0 | 0, 0  |  1, 0
		// ------------+--------
		// -1, 1 | 0, 1  |  1, 1
		int count = 0;
		final int xBin = i % nXBins;
		final int yBin = i / nXBins;
		final boolean lowerY = yBin > 0;
		final boolean upperY = yBin < nYBins - 1;
		if (xBin > 0)
		{
			count = addNeighbour(neighbours, count, i - 1);
			if (lowerY)
				count = addNeighbour(neighbours, count, i - 1 - nXBins);
			if (upperY)
				count = addNeighbour(neighbours, count, i - 1 + nXBins);
		}
		if (lowerY)
			count = addNeighbour(neighbours, count, i - nXBins);
		if (upperY)
			count = addNeighbour(neighbours, count, i + nXBins);
		if (xBin < nXBins - 1)
		{
			count = addNeighbour(neighbours, count, i + 1);
			if (lowerY)
				count = addNeighbour(neighbours, count, i + 1 - nXBins);
			if (upperY)
				count = addNeighbour(neighbours, count, i + 1 + nXBins);
		}
		return count;
	}

	/**
	 * Build a list of which cells to compare up to a maximum of 9
	 *
	 * @param neighbours
	 *            the neighbours
	 * @param i
	 *            the grid index
	 * @return the number of neighbours
	 */
	private int getNeighbours9(int[] neighbours, int i)
	{
		// Build a list of which cells to compare up to a maximum of 9
		// -1,-1 | 0,-1  |  1,-1
		// ------------+--------
		// -1, 0 | 0, 0  |  1, 0
		// ------------+--------
		// -1, 1 | 0, 1  |  1, 1
		int count = addNeighbour(neighbours, 0, i);
		final int xBin = i % nXBins;
		final int yBin = i / nXBins;
		final boolean lowerY = yBin > 0;
		final boolean upperY = yBin < nYBins - 1;
		if (xBin > 0)
		{
			count = addNeighbour(neighbours, count, i - 1);
			if (lowerY)
				count = addNeighbour(neighbours, count, i - 1 - nXBins);
			if (upperY)
				count = addNeighbour(neighbours, count, i - 1 + nXBins);
		}
		if (lowerY)
			count = addNeighbour(neighbours, count, i - nXBins);
		if (upperY)
			count = addNeighbour(neighbours, count, i + nXBins);
		if (xBin < nXBins - 1)
		{
			count = addNeighbour(neighbours, count, i + 1);
			if (lowerY)
				count = addNeighbour(neighbours, count, i + 1 - nXBins);
			if (upperY)
				count = addNeighbour(neighbours, count, i + 1 + nXBins);
		}
		return count;
	}

	/**
	 * The squared distance
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param molecule
	 *            the index molecule
	 * @return the squared distance
	 */
	private static float distance2(float x, float y, Molecule molecule)
	{
		final float dx = x - molecule.getX();
		final float dy = y - molecule.getY();
		return dx * dx + dy * dy;
	}

	/**
	 * Count the density of each class of molecule around the input molecule. Counts are returned using the input
	 * order of molecules. The ID of the input molecules is ignored.
	 * <p>
	 * This method allows the molecule ID to be changed following creation of the counter but coordinates must be the
	 * same. The maximum ID must be input to allow efficient counting.
	 * <p>
	 * This method is optimised for use when the number of IDs is small. If the number of IDs is large then the routine
	 * may run out of memory.
	 *
	 * @param m1
	 *            the molecule
	 * @param maxID
	 *            the max ID of molecules
	 * @return the counts
	 */
	public int[] count(Molecule m1, int maxID)
	{
		return count(m1, maxID, new int[9]);
	}

	private int[] count(Molecule m1, int maxID, int[] neighbours)
	{
		final int[] count1 = new int[maxID + 1];

		final float x = m1.getX();
		final float y = m1.getY();
		int c = getNeighbours9(neighbours, getBinSafe(x, y));

		// Compare to neighbours
		while (c-- > 0)
		{
			final IndexMolecule[] cell2 = grid[neighbours[c]];
			for (int k = cell2.length; k-- > 0;)
			{
				final IndexMolecule m2 = cell2[k];
				if (distance2(x, y, m2) < r2)
					count1[m2.getID()]++;
			}
		}
		return count1;
	}

	/**
	 * Count the density of each class of molecule around each input molecule. Counts are returned using the input
	 * order of molecules. The ID of the input molecules is ignored.
	 * <p>
	 * This method allows the molecule ID to be changed following creation of the counter but coordinates must be the
	 * same. The maximum ID must be input to allow efficient counting.
	 * <p>
	 * This method is optimised for use when the number of IDs is small. If the number of IDs is large then the routine
	 * may run out of memory.
	 *
	 * @param molecules2
	 *            the molecules to around which to search
	 * @param maxID
	 *            the max ID of molecules
	 * @return the counts
	 */
	public int[][] countAll(Molecule[] molecules2, int maxID)
	{
		final int nMolecules = (molecules2 == null) ? 0 : molecules2.length;
		final int[][] results = new int[nMolecules][];
		if (nMolecules == 0)
			return results;

		// Single threaded
		if (getNumberOfThreads() == 1)
		{
			final int[] neighbours = new int[9];
			for (int i = 0; i < nMolecules; i++)
				results[i] = count(molecules2[i], maxID, neighbours);
		}
		else
		{
			// Multi-threaded
			final int nThreads = Math.min(this.nThreads, nMolecules);

			// Use an executor service so that we know when complete
			final ExecutorService executor = Executors.newFixedThreadPool(nThreads);
			final TurboList<Future<?>> futures = new TurboList<>(nThreads);

			final int nPerThread = (int) Math.ceil((double) nMolecules / nThreads);
			for (int from = 0; from < nMolecules;)
			{
				final int to = Math.min(from + nPerThread, nMolecules);
				futures.add(executor.submit(new MoleculeCountWorker(molecules2, maxID, results, from, to)));
				from = to;
			}

			// Wait for all to finish
			for (int t = futures.size(); t-- > 0;)
				try
				{
					// The future .get() method will block until completed
					futures.get(t).get();
				}
				catch (final Exception e)
				{
					// This should not happen.
					// Ignore it and allow processing to continue (the number of neighbour samples will just be smaller).
					e.printStackTrace();
				}

			executor.shutdown();
		}

		return results;
	}

	/**
	 * For processing the countAll method using an all neighbour cell comparison.
	 */
	private class MoleculeCountWorker implements Runnable
	{
		final Molecule[] molecules2;
		final int maxID;
		final int[][] results;
		final int from;
		final int to;

		public MoleculeCountWorker(Molecule[] molecules2, int maxID, int[][] results, int from, int to)
		{
			this.molecules2 = molecules2;
			this.maxID = maxID;
			this.results = results;
			this.from = from;
			this.to = to;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			final int[] neighbours = new int[9];
			for (int i = from; i < to; i++)
				results[i] = count(molecules2[i], maxID, neighbours);
		}
	}
}
