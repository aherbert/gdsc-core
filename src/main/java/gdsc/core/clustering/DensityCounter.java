package gdsc.core.clustering;

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
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
		float getX();

		/**
		 * Gets the y coordinate. This must remain constant for efficient molecule processing.
		 *
		 * @return the y
		 */
		float getY();

		/**
		 * Gets the id. Must be zero or above.
		 *
		 * @return the id
		 */
		int getID();
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
		public float getX()
		{
			return x;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see gdsc.core.clustering.DensityCounter.Molecule#getY()
		 */
		public float getY()
		{
			return y;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see gdsc.core.clustering.DensityCounter.Molecule#getID()
		 */
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
		public float getX()
		{
			return molecule.getX();
		}

		/**
		 * Gets the y.
		 *
		 * @return the y
		 */
		public float getY()
		{
			return molecule.getY();
		}

		/**
		 * Gets the id.
		 *
		 * @return the id
		 */
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

	private final float r2;
	private final float xmin, ymin, binWidth;
	private final int nXBins, nYBins, nMolecules;
	private final IndexMolecule[][] grid;

	private final int nonEmpty;
	private int[] gridPriority = null;

	private int nThreads = -1;

	/**
	 * Gets the number of threads to use for multi-threaded algorithms.
	 * <p>
	 * Note: This is initialised to the number of processors available to the JVM.
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
		r2 = radius * radius;
		nMolecules = molecules.length;

		if (zeroOrigin)
		{
			xmin = ymin = 0;
		}
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
		float xrange = xmax - xmin;
		float yrange = ymax - ymin;
		binWidth = determineBinWidth(xrange, yrange, radius);

		nXBins = (int) (1 + Math.floor(xrange / binWidth));
		nYBins = (int) (1 + Math.floor(yrange / binWidth));

		// Assign to grid
		MoleculeList[] tmp = new MoleculeList[nXBins * nYBins];
		for (int i = 0; i < tmp.length; i++)
			tmp[i] = new MoleculeList();
		for (int i = 0; i < molecules.length; i++)
		{
			final Molecule m = molecules[i];
			tmp[getBin(m.getX(), m.getY())].add(m, i);
		}

		// Convert for efficiency
		grid = new IndexMolecule[tmp.length][];
		int c = 0;
		for (int i = 0; i < tmp.length; i++)
		{
			if (tmp[i].size != 0)
			{
				c++;
				grid[i] = tmp[i].toArray();
			}
			tmp[i] = null;
		}
		nonEmpty = c;
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
	private float determineBinWidth(float xrange, float yrange, float radius)
	{
		float binWidth = radius;
		while (getBins(xrange, yrange, binWidth) > 100000)
		{
			// Dumb implementation that increase the bin width so that each cell doubles in size. 
			// A better solution would be to conduct a search for the value with a number of bins close 
			// to the target.
			binWidth *= ROOT2;
		}
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
	private double getBins(float xrange, float yrange, float binWidth)
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
		Molecule[] molecules = new Molecule[nMolecules];
		for (int i = 0; i < grid.length; i++)
		{
			final IndexMolecule[] cell1 = grid[i];
			if (cell1 == null)
				continue;
			for (int j = cell1.length; j-- > 0;)
			{
				molecules[cell1[j].index] = cell1[j].molecule;
			}
		}
		return molecules;
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
	 * This method allows the molecule ID to be changed following creation of the counter but coordinates must be the
	 * same. The maximum ID must be input to allow efficient counting.
	 * <p>
	 * This method is optimised for use when the number of IDs is small. If the number of IDs is large then the routine
	 * may run out of memory.
	 * <p>
	 * NOTE: Package level for JUnit test only
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
		int nMolecules = molecules.length;
		int[][] results = new int[nMolecules][maxID + 1];

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
				Molecule m2 = molecules[j];
				if (distance2(x, y, m2) < r2)
				{
					int[] count2 = results[j];
					count1[m2.getID()]++;
					count2[id]++;
				}
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
		int[][] results = new int[nMolecules][maxID + 1];

		// Single threaded
		if (getNumberOfThreads() == 1)
		{
			int[] neighbours = new int[4];
			for (int i = 0; i < grid.length; i++)
			{
				final IndexMolecule[] cell1 = grid[i];
				if (cell1 == null)
					continue;

				final int xBin = i % nXBins;
				final int yBin = i / nXBins;

				// Build a list of which cells to compare up to a maximum of 4
				//      | 0,0  |  1,0
				// ------------+-----
				// -1,1 | 0,1  |  1,1

				int count = 0;
				if (yBin < nYBins - 1)
				{
					if (xBin > 0)
						neighbours[count++] = i + nXBins - 1;
					neighbours[count++] = i + nXBins;
					if (xBin < nXBins - 1)
					{
						neighbours[count++] = i + nXBins + 1;
						neighbours[count++] = i + 1;
					}
				}
				else if (xBin < nXBins - 1)
				{
					neighbours[count++] = i + 1;
				}

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
						IndexMolecule m2 = cell1[k];
						if (distance2(x, y, m2) < r2)
						{
							int[] count2 = results[m2.index];
							count1[m2.getID()]++;
							count2[id]++;
						}
					}

					// Compare to neighbours
					for (int c = count; c-- > 0;)
					{
						final IndexMolecule[] cell2 = grid[neighbours[c]];
						if (cell2 == null)
							continue;
						for (int k = cell2.length; k-- > 0;)
						{
							IndexMolecule m2 = cell2[k];
							if (distance2(x, y, m2) < r2)
							{
								int[] count2 = results[m2.index];
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

			if (gridPriority == null)
			{
				// Count the number of entries in each grid cell
				int[][] count = new int[nonEmpty][2];
				for (int i = 0, c = 0; i < grid.length; i++)
				{
					final IndexMolecule[] cell1 = grid[i];
					if (cell1 == null)
						continue;
					count[c][0] = cell1.length;
					count[c][1] = i;
					c++;
				}

				// Sort by the size
				Arrays.sort(count, new Comparator<int[]>()
				{
					public int compare(int[] o1, int[] o2)
					{
						return o2[0] - o1[0];
					}
				});
				gridPriority = new int[nonEmpty];
				for (int i = 0; i < gridPriority.length; i++)
					gridPriority[i] = count[i][1];
			}

			// Split the entries evenly over each thread
			// This should fairly allocate the density to all processing threads
			int[] process = new int[nonEmpty];
			for (int i = 0, j = 0, k = 0; i < gridPriority.length; i++)
			{
				process[i] = gridPriority[j];
				j += nThreads;
				if (j >= gridPriority.length)
					j = ++k;
			}

			//			// Check we have all the indices
			//			{
			//				int[] p = process.clone();
			//				int[] g = gridPriority.clone();
			//				Arrays.sort(p);
			//				Arrays.sort(g);
			//				for (int i = 0; i < p.length; i++)
			//					if (p[i] != g[i])
			//					{
			//						throw new RuntimeException(String.format("[%d] %f != %f", i, p[i], g[i]));
			//					}
			//			}

			// Use an executor service so that we know when complete
			ExecutorService executor = Executors.newFixedThreadPool(nThreads);
			TurboList<Future<?>> futures = new TurboList<Future<?>>(nThreads);

			int nPerThread = (int) Math.ceil((double) process.length / nThreads);
			for (int from = 0; from < process.length;)
			{
				int to = Math.min(from + nPerThread, process.length);
				futures.add(executor.submit(new CountWorker(results, process, from, to)));
				from = to;
			}
			// Wait for all to finish
			for (int t = futures.size(); t-- > 0;)
			{
				try
				{
					// The future .get() method will block until completed
					futures.get(t).get();
				}
				catch (Exception e)
				{
					// This should not happen. 
					// Ignore it and allow processing to continue (the number of neighbour samples will just be smaller).  
					e.printStackTrace();
				}
			}

			executor.shutdown();
		}

		return results;
	}

	/**
	 * The Class SetWorker.
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
			{
				flushSingle();
			}
		}

		void flushSingle()
		{
			synchronized (results)
			{
				while (c-- > 0)
				{
					results[indexData[c]][idData[c]]++;
				}
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
		public void run()
		{
			// Temp storage
			int countSize = results[0].length;
			int n = 0;
			for (int index = from; index < to; index++)
				n += grid[process[index]].length;
			int[][] results1 = new int[n][countSize + 1];

			int[] neighbours = new int[4];
			for (int index = from; index < to; index++)
			{
				int i = process[index];
				final IndexMolecule[] cell1 = grid[i];

				// We ensure the indices to process are not empty
				//if (cell1 == null) 
				//	continue;

				final int xBin = i % nXBins;
				final int yBin = i / nXBins;

				// Build a list of which cells to compare up to a maximum of 4
				//      | 0,0  |  1,0
				// ------------+-----
				// -1,1 | 0,1  |  1,1

				int count = 0;
				if (yBin < nYBins - 1)
				{
					if (xBin > 0)
						neighbours[count++] = i + nXBins - 1;
					neighbours[count++] = i + nXBins;
					if (xBin < nXBins - 1)
					{
						neighbours[count++] = i + nXBins + 1;
						neighbours[count++] = i + 1;
					}
				}
				else if (xBin < nXBins - 1)
				{
					neighbours[count++] = i + 1;
				}

				for (int j = cell1.length; j-- > 0;)
				{
					final IndexMolecule m1 = cell1[j];
					final float x = m1.getX();
					final float y = m1.getY();
					final int id = m1.getID();

					// Reset
					int[] count1 = results1[--n];

					// Self count
					count1[countSize] = m1.index;
					count1[id]++;

					// Compare all inside the bin
					for (int k = j; k-- > 0;)
					{
						IndexMolecule m2 = cell1[k];
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
						if (cell2 == null)
							continue;
						for (int k = cell2.length; k-- > 0;)
						{
							IndexMolecule m2 = cell2[k];
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
				{
					results[indexData[c]][idData[c]]++;
				}

				for (int i = 0; i < results1.length; i++)
				{
					int[] count1 = results1[i];
					// We store the index at the end of the array
					int[] count = results[count1[countSize]];
					for (int j = 0; j < countSize; j++)
					{
						count[j] += count1[j];
					}
				}
			}
		}
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
}
