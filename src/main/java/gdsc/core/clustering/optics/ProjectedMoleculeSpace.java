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

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.RandomVectorGenerator;
import org.apache.commons.math3.random.UnitSphereRandomVectorGenerator;

import gdsc.core.ij.Utils;
import gdsc.core.logging.TrackProgress;
import gdsc.core.utils.NotImplementedException;
import gdsc.core.utils.PseudoRandomGenerator;
import gdsc.core.utils.SimpleArrayUtils;
import gdsc.core.utils.Sort;
import gdsc.core.utils.TurboList;
import gdsc.core.utils.TurboRandomGenerator;
import gnu.trove.set.hash.TIntHashSet;

/**
 * Store molecules and allows generation of random projections
 * <p>
 * This class is an adaption of de.lmu.ifi.dbs.elki.index.preprocessed.fastoptics.RandomProjectedNeighborsAndDensities.
 * Copyright (C) 2015.
 * Johannes Schneider, ABB Research, Switzerland, johannes.schneider@alumni.ethz.ch.
 * Released under the GPL v3 licence.
 * <p>
 * Modifications have been made for multi-threading and different neighbour sampling modes. The partitioning of the sets
 * is
 * essentially unchanged.
 * 
 * @author Alex Herbert
 */
class ProjectedMoleculeSpace extends MoleculeSpace
{

	/** Used for access to the raw coordinates. */
	protected final OPTICSManager opticsManager;

	/** The tracker. */
	private TrackProgress tracker;

	/**
	 * Default constant used to compute number of projections as well as number of
	 * splits of point set, ie. constant *log N*d
	 */
	// constant in O(log N*d) used to compute number of projections as well as
	// number of splits of point set
	private static final int logOProjectionConst = 20;

	/**
	 * Sets used for neighborhood computation should be about minSplitSize Sets are still used if they deviate by less
	 * (1+/- sizeTolerance).
	 */
	private static final float sizeTolerance = 2f / 3;

	/**
	 * Store the results of a split of the dataset.
	 */
	class Split
	{

		/** The number. */
		final int number;

		/** The sets. */
		final TurboList<int[]> sets;

		/**
		 * Instantiates a new split.
		 *
		 * @param number
		 *            the number
		 * @param sets
		 *            the sets
		 */
		Split(int number, TurboList<int[]> sets)
		{
			this.number = number;
			this.sets = sets;
		}

		/**
		 * Instantiates a new split.
		 *
		 * @param number
		 *            the number
		 * @param sets
		 *            the sets
		 */
		Split(int number, int[]... sets)
		{
			this.number = number;
			this.sets = new TurboList<int[]>(Arrays.asList(sets));
		}
	}

	/** Sets that resulted from recursive split of entire point set. */
	TurboList<Split> splitSets;

	/**
	 * Random factory.
	 */
	RandomGenerator rand;

	/** The neighbours of each point. */
	int[][] allNeighbours;

	/** The number of splits to compute (if below 1 it will be auto-computed using the size of the data). */
	public int nSplits = 0;

	/** The number of projections to compute (if below 1 it will be auto-computed using the size of the data). */
	public int nProjections = 0;

	/**
	 * Set to true to save all sets that are approximately min split size. The default is to only save sets smaller than
	 * min split size.
	 */
	public boolean saveApproximateSets = false;

	/** The sample mode. */
	private SampleMode sampleMode;

	/**
	 * Set to true to use random vectors for the projections. The default is to uniformly create vectors on the
	 * semi-circle interval.
	 */
	public boolean useRandomVectors = false;

	/** The number of threads to use. */
	public int nThreads = 1;

	/** The number of distance computations. */
	public AtomicInteger distanceComputations = new AtomicInteger();

	/**
	 * Instantiates a new projected molecule space.
	 *
	 * @param opticsManager
	 *            the optics manager
	 * @param generatingDistanceE
	 *            the generating distance E
	 * @param rand
	 *            the rand
	 */
	ProjectedMoleculeSpace(OPTICSManager opticsManager, float generatingDistanceE, RandomGenerator rand)
	{
		super(opticsManager.getSize(), generatingDistanceE);

		this.opticsManager = opticsManager;
		this.rand = rand;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.clustering.optics.MoleculeSpace#toString()
	 */
	@Override
	public String toString()
	{
		return String.format("%s", this.getClass().getSimpleName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.clustering.optics.MoleculeSpace#generate()
	 */
	@Override
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
	@Override
	void findNeighbours(int minPts, Molecule object, float e)
	{
		// Return the neighbours found in {@link #computeAverageDistInSetAndNeighbours()}.
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
	@Override
	void findNeighboursAndDistances(int minPts, Molecule object, float e)
	{
		// Return the neighbours found in {@link #computeAverageDistInSetAndNeighbours()}.
		// Assume allNeighbours has been computed.
		neighbours.clear();
		int[] list = allNeighbours[object.id];
		for (int i = list.length; i-- > 0;)
		{
			Molecule otherObject = setOfObjects[list[i]];
			otherObject.setD(object.distance2(otherObject));
			neighbours.add(otherObject);
		}
	}

	/**
	 * Sets the tracker.
	 *
	 * @param tracker
	 *            the new tracker
	 */
	public void setTracker(TrackProgress tracker)
	{
		this.tracker = tracker;
	}

	/** The total progress. */
	int progress, stepProgress, totalProgress;

	/**
	 * Sets the up progress.
	 *
	 * @param total
	 *            the new up progress
	 */
	private void setUpProgress(int total)
	{
		totalProgress = total;
		stepProgress = Utils.getProgressInterval(totalProgress);
		progress = 0;
	}

	/**
	 * Show progress.
	 */
	private synchronized void showProgress()
	{
		if (progress % stepProgress == 0)
		{
			if (tracker != null)
				tracker.progress(progress, totalProgress);
		}
		progress++;
	}

	/**
	 * The Class Job.
	 */
	private abstract class Job
	{

		/** The index. */
		final int index;

		/**
		 * Instantiates a new job.
		 *
		 * @param index
		 *            the index
		 */
		Job(int index)
		{
			this.index = index;
		}
	}

	/**
	 * The Class ProjectionJob.
	 */
	private class ProjectionJob extends Job
	{

		/** The v. */
		final double[] v;

		/**
		 * Instantiates a new projection job.
		 *
		 * @param index
		 *            the index
		 * @param v
		 *            the v
		 */
		ProjectionJob(int index, double[] v)
		{
			super(index);
			this.v = v;
		}
	}

	/**
	 * The Class SplitJob.
	 */
	private class SplitJob extends Job
	{

		/** The projected points. */
		final float[][] projectedPoints;

		/** The rand. */
		final TurboRandomGenerator rand;

		/**
		 * Instantiates a new split job.
		 *
		 * @param index
		 *            the index
		 * @param projectedPoints
		 *            the projected points
		 * @param rand
		 *            the rand
		 */
		SplitJob(int index, float[][] projectedPoints, TurboRandomGenerator rand)
		{
			super(index);
			this.projectedPoints = projectedPoints;
			this.rand = rand;
		}
	}

	/**
	 * The Class ProjectionWorker.
	 */
	private class ProjectionWorker implements Runnable
	{

		/** The finished. */
		volatile boolean finished = false;

		/** The jobs. */
		final BlockingQueue<ProjectionJob> jobs;

		/** The projected points. */
		final float[][] projectedPoints;

		/**
		 * Instantiates a new projection worker.
		 *
		 * @param jobs
		 *            the jobs
		 * @param projectedPoints
		 *            the projected points
		 */
		public ProjectionWorker(BlockingQueue<ProjectionJob> jobs, float[][] projectedPoints)
		{
			this.jobs = jobs;
			this.projectedPoints = projectedPoints;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			try
			{
				while (true)
				{
					ProjectionJob job = jobs.take();
					if (job.index < 0)
						break;
					if (!finished)
						// Only run jobs when not finished. This allows the queue to be emptied.
						run(job);
				}
			}
			catch (InterruptedException e)
			{
				System.out.println(e.toString());
				throw new RuntimeException(e);
			}
			finally
			{
				finished = true;
			}
		}

		/**
		 * Run.
		 *
		 * @param job
		 *            the job
		 */
		private void run(ProjectionJob job)
		{
			//if (Utils.isInterrupted())
			//{
			//	finished = true;
			//	return;
			//}

			showProgress();

			final double[] v = job.v;

			// Project points to the vector and compute the distance along the vector from the origin
			float[] currPro = new float[size];
			for (int it = size; it-- > 0;)
			{
				Molecule m = setOfObjects[it];
				// Dot product:
				currPro[it] = (float) (v[0] * m.x + v[1] * m.y);
			}
			projectedPoints[job.index] = currPro;
		}
	}

	/**
	 * The Class SplitWorker.
	 */
	private class SplitWorker implements Runnable
	{

		/** The finished. */
		volatile boolean finished = false;

		/** The jobs. */
		final BlockingQueue<SplitJob> jobs;

		/** The min split size. */
		final int minSplitSize;

		/** The split sets. */
		final TurboList<Split> splitSets = new TurboList<Split>();

		/**
		 * Instantiates a new split worker.
		 *
		 * @param jobs
		 *            the jobs
		 * @param minSplitSize
		 *            the min split size
		 */
		public SplitWorker(BlockingQueue<SplitJob> jobs, int minSplitSize)
		{
			this.jobs = jobs;
			this.minSplitSize = minSplitSize;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			try
			{
				while (true)
				{
					SplitJob job = jobs.take();
					if (job.index < 0)
						break;
					if (!finished)
						// Only run jobs when not finished. This allows the queue to be emptied.
						run(job);
				}
			}
			catch (InterruptedException e)
			{
				System.out.println(e.toString());
				throw new RuntimeException(e);
			}
			finally
			{
				finished = true;
			}
		}

		/**
		 * Run.
		 *
		 * @param job
		 *            the job
		 */
		private void run(SplitJob job)
		{
			//if (Utils.isInterrupted())
			//{
			//	finished = true;
			//	return;
			//}

			showProgress();

			final TurboList<int[]> sets = new TurboList<int[]>();
			splitupNoSort(sets, job.projectedPoints, SimpleArrayUtils.newArray(size, 0, 1), 0, size, 0, job.rand,
					minSplitSize);
			splitSets.add(new Split(job.index, sets));
		}
	}

	/**
	 * The Class SetWorker.
	 */
	private class SetWorker implements Runnable
	{

		/** The sum distances. */
		final double[] sumDistances;

		/** The n distances. */
		final int[] nDistances;

		/** The neighbours. */
		final TIntHashSet[] neighbours;

		/** The sets. */
		final TurboList<int[]> sets;

		/** The from. */
		final int from;

		/** The to. */
		final int to;

		/**
		 * Instantiates a new sets the worker.
		 *
		 * @param sumDistances
		 *            the sum distances
		 * @param nDistances
		 *            the n distances
		 * @param neighbours
		 *            the neighbours
		 * @param sets
		 *            the sets
		 * @param from
		 *            the from
		 * @param to
		 *            the to
		 */
		public SetWorker(double[] sumDistances, int[] nDistances, TIntHashSet[] neighbours, TurboList<int[]> sets,
				int from, int to)
		{
			this.sumDistances = sumDistances;
			this.nDistances = nDistances;
			this.neighbours = neighbours;
			this.sets = sets;
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
			sampleNeighbours(sumDistances, nDistances, neighbours, sets, from, to);
		}
	}

	/**
	 * Create random projections, project points and put points into sets of size
	 * about minSplitSize/2.
	 *
	 * @param minSplitSize
	 *            minimum size for which a point set is further
	 *            partitioned (roughly corresponds to minPts in OPTICS)
	 */
	public void computeSets(int minSplitSize)
	{
		splitSets = new TurboList<Split>();

		// Edge cases
		if (minSplitSize < 2 || size <= 1)
			return;

		if (size == 2)
		{
			// No point performing projections and splits
			splitSets.add(new Split(0, new int[] { 0, 1 }));
			return;
		}

		final int dim = 2;

		// FastOPTICS paper states you can use c0*log(N) sets and c1*log(N) projections.
		// The ELKI framework increase this for the number of dimensions. However I have stuck
		// with the original (as it is less so will be faster).
		// Note: In most computer science contexts log is in base 2.
		int nPointSetSplits, nProject1d;

		nPointSetSplits = getNumberOfSplitSets(nSplits, size);
		nProject1d = getNumberOfProjections(nProjections, size);

		// perform O(log N+log dim) splits of the entire point sets projections
		//nPointSetSplits = (int) (logOProjectionConst * log2(size * dim + 1));
		// perform O(log N+log dim) projections of the point set onto a random line
		//nProject1d = (int) (logOProjectionConst * log2(size * dim + 1));

		if (nPointSetSplits < 1 || nProject1d < 1)
			return; // Nothing to do

		// perform projections of points
		float[][] projectedPoints = new float[nProject1d][];

		long time = System.currentTimeMillis();
		setUpProgress(nProject1d);
		if (tracker != null)
		{
			tracker.log("Computing projections ...");
		}

		// Multi-thread this for speed
		int nThreads = Math.min(this.nThreads, nPointSetSplits);
		final TurboList<Thread> threads = new TurboList<Thread>(nThreads);

		final BlockingQueue<ProjectionJob> projectionJobs = new ArrayBlockingQueue<ProjectionJob>(nThreads * 2);
		final TurboList<ProjectionWorker> projectionWorkers = new TurboList<ProjectionWorker>(nThreads);
		for (int i = 0; i < nThreads; i++)
		{
			final ProjectionWorker worker = new ProjectionWorker(projectionJobs, projectedPoints);
			final Thread t = new Thread(worker);
			projectionWorkers.addf(worker);
			threads.addf(t);
			t.start();
		}

		// Create random vectors or uniform distribution
		RandomVectorGenerator vectorGen = (useRandomVectors) ? new UnitSphereRandomVectorGenerator(2, rand) : null;
		final double increment = Math.PI / nProject1d;
		for (int i = 0; i < nProject1d; i++)
		{
			// Create a random unit vector
			double[] currRp;
			if (useRandomVectors)
			{
				currRp = vectorGen.nextVector();
			}
			else
			{
				// For a 2D vector we can just uniformly distribute them around a semi-circle
				currRp = new double[dim];
				double a = i * increment;
				currRp[0] = Math.sin(a);
				currRp[1] = Math.cos(a);
			}
			put(projectionJobs, new ProjectionJob(i, currRp));
		}
		// Finish all the worker threads by passing in a null job
		for (int i = 0; i < nThreads; i++)
		{
			put(projectionJobs, new ProjectionJob(-1, null));
		}

		// Wait for all to finish
		for (int i = 0; i < nThreads; i++)
		{
			try
			{
				threads.get(i).join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		threads.clear();

		if (tracker != null)
		{
			tracker.progress(1);
			long time2 = System.currentTimeMillis();
			tracker.log("Computed projections ... " + Utils.timeToString(time2 - time));
			time = time2;
			tracker.log("Splitting data ...");
		}

		// split entire point set, reuse projections by shuffling them
		int[] proind = SimpleArrayUtils.newArray(nProject1d, 0, 1);
		setUpProgress(nPointSetSplits);

		// The splits do not have to be that random so we can use a pseudo random sequence.
		// The sets will be randomly sized between 1 and minSplitSize. Ensure we have enough 
		// numbers for all the splits.
		double expectedSetSize = (1 + minSplitSize) * 0.5;
		int expectedSets = (int) Math.round(size / expectedSetSize);
		TurboRandomGenerator pseudoRandom = new TurboRandomGenerator(Math.max(200, minSplitSize + 2 * expectedSets), rand);

		// Multi-thread this for speed
		final BlockingQueue<SplitJob> splitJobs = new ArrayBlockingQueue<SplitJob>(nThreads * 2);
		final TurboList<SplitWorker> splitWorkers = new TurboList<SplitWorker>(nThreads);
		for (int i = 0; i < nThreads; i++)
		{
			final SplitWorker worker = new SplitWorker(splitJobs, minSplitSize);
			final Thread t = new Thread(worker);
			splitWorkers.addf(worker);
			threads.addf(t);
			t.start();
		}

		for (int i = 0; i < nPointSetSplits; i++)
		{
			// shuffle projections
			float[][] shuffledProjectedPoints = new float[nProject1d][];
			pseudoRandom.shuffle(proind);
			for (int j = 0; j < nProject1d; j++)
			{
				shuffledProjectedPoints[j] = projectedPoints[proind[j]];
			}

			// New random generator
			TurboRandomGenerator rand = (TurboRandomGenerator) pseudoRandom.clone();
			rand.setSeed(i);

			put(splitJobs, new SplitJob(i, shuffledProjectedPoints, rand));
		}

		// Finish all the worker threads by passing in a null job
		for (int i = 0; i < nThreads; i++)
		{
			put(splitJobs, new SplitJob(-1, null, null));
		}

		// Wait for all to finish
		int total = 0;
		for (int i = 0; i < nThreads; i++)
		{
			try
			{
				threads.get(i).join();
				total += splitWorkers.get(i).splitSets.size();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		threads.clear();

		// Merge the split-sets
		splitSets = splitWorkers.get(0).splitSets;
		splitSets.ensureCapacity(total);
		for (int i = 1; i < nThreads; i++)
			splitSets.addAll(splitWorkers.get(i).splitSets);

		if (tracker != null)
		{
			time = System.currentTimeMillis() - time;
			tracker.log("Split data ... " + Utils.timeToString(time));
			tracker.progress(1);
		}
	}

	/**
	 * Put.
	 *
	 * @param <T>
	 *            the generic type
	 * @param jobs
	 *            the jobs
	 * @param job
	 *            the job
	 */
	private <T> void put(BlockingQueue<T> jobs, T job)
	{
		try
		{
			jobs.put(job);
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException("Unexpected interruption", e);
		}
	}

	/**
	 * Gets the number of split sets.
	 *
	 * @param nSplits
	 *            The number of splits to compute (if below 1 it will be auto-computed using the size of the data)
	 * @param size
	 *            the size
	 * @return the number of split sets
	 */
	public static int getNumberOfSplitSets(int nSplits, int size)
	{
		if (size < 2)
			return 0;
		return (nSplits > 0) ? nSplits : (int) (logOProjectionConst * log2(size));
	}

	/**
	 * Gets the number of projections.
	 *
	 * @param nProjections
	 *            The number of projections to compute (if below 1 it will be auto-computed using the size of the data)
	 * @param size
	 *            the size
	 * @return the number of projections
	 */
	public static int getNumberOfProjections(int nProjections, int size)
	{
		return getNumberOfSplitSets(nProjections, size);
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
	 * @param splitSets
	 *            the split sets
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
	private void splitupNoSort(TurboList<int[]> splitSets, float[][] projectedPoints, int[] ind, int begin, int end,
			int dim, PseudoRandomGenerator rand, int minSplitSize)
	{
		final int nele = end - begin;

		if (nele < 2)
		{
			// Nothing to split. Also ensures we only add to the sets if neighbours can be sampled.
			return;
		}

		dim = dim % projectedPoints.length;// choose a projection of points
		float[] tpro = projectedPoints[dim];

		if (saveApproximateSets)
		{
			// save set such that used for density or neighborhood computation
			// sets should be roughly minSplitSize
			// -=-=-
			// Note: This is the method used in ELKI which uses the distance to the median of the set 
			// (thus no distances are computed that are between points very far apart, e.g. each end 
			// of the set).
			if (nele > minSplitSize * (1 - sizeTolerance) && nele < minSplitSize * (1 + sizeTolerance))
			{
				saveSet(splitSets, ind, begin, end, rand, tpro);
			}
		}

		// compute splitting element
		// do not store set or even sort set, since it is too large
		if (nele > minSplitSize)
		{
			// splits can be performed either by distance (between min,maxCoord) or by
			// picking a point randomly(picking index of point)
			// outcome is similar

			//int minInd = splitByDistance(ind, begin, end, tpro, rand);
			int minInd = splitRandomly(ind, begin, end, tpro, rand);

			// split set recursively
			// position used for splitting the projected points into two
			// sets used for recursive splitting
			int splitpos = minInd + 1;
			splitupNoSort(splitSets, projectedPoints, ind, begin, splitpos, dim + 1, rand, minSplitSize);
			splitupNoSort(splitSets, projectedPoints, ind, splitpos, end, dim + 1, rand, minSplitSize);
		}
		else if (!saveApproximateSets)
		{
			// It it wasn't saved as an approximate set then make sure it is saved as it is less than minSplitSize
			saveSet(splitSets, ind, begin, end, rand, tpro);
		}
	}

	private void saveSet(TurboList<int[]> splitSets, int[] ind, int begin, int end, PseudoRandomGenerator rand,
			float[] tpro)
	{
		int[] indices = Arrays.copyOfRange(ind, begin, end);
		if (sampleMode == SampleMode.RANDOM)
		{
			// Ensure the indices are random
			rand.shuffle(indices);
		}
		else if (sampleMode == SampleMode.MEDIAN)
		{
			// sort set, since need median element later
			// (when computing distance to the middle of the set)
			Sort.sort(indices, tpro);
		}
		splitSets.add(indices);
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

	/**
	 * Swap.
	 *
	 * @param data
	 *            the data
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 */
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
	 * from sets resulting from projection.
	 *
	 * @return list of neighbours for each point
	 */
	public int[][] computeAverageDistInSetAndNeighbours()
	{
		distanceComputations.set(0);

		// Q. Are the neighbours worked out using the core distance?
		// The FastOPTICS paper discusses a merging distance as the min of the core distance for A and B.
		// Only those below the merge distance are candidates for a merge.
		// However in a later discussion of FastOPTICS they state that reachability is only computed for
		// the sampled neighbours (and these may be above the merge distance).
		// A. Here we assume that any point-pair in the split set can be neighbours but we do not 
		// compute all pairs but only a sub-sample of them.

		// Note: The ELKI implementation computes the neighbours using all items in a set to 
		// the middle of the set, and each item in the set to the middle of the set. The FastOPTICS
		// paper states that any neighbour is valid but further neighbours can be excluded using an
		// f-factor (with f 0:1). If f=1 then all neighbours are included. Below this then only some
		// of the neighbours are included using the projected distance values. Neighbours to be 
		// included are picked at random.

		final int n = splitSets.size();
		long time = System.currentTimeMillis();
		if (tracker != null)
		{
			tracker.log("Computing density and neighbourhoods ...");
		}

		double[] sumDistances = new double[size];
		int[] nDistances = new int[size];
		TIntHashSet[] neighbours = new TIntHashSet[size];
		for (int it = size; it-- > 0;)
		{
			neighbours[it] = new TIntHashSet();
		}

		// Multi-thread the hash set operations for speed. 
		// We can do this if each split uses each index only once.
		int nThreads = Math.min(this.nThreads, n);
		boolean multiThread = (n > 1 && !saveApproximateSets);

		// Use an executor service so that we know the entire split has been processed before 
		// doing the next split.
		ExecutorService executor = null;
		TurboList<Future<?>> futures = null;
		if (multiThread)
		{
			executor = Executors.newFixedThreadPool(nThreads);
			futures = new TurboList<Future<?>>(nThreads);
		}

		final int interval = Utils.getProgressInterval(n);
		for (int i = 0; i < n; i++)
		{
			if (tracker != null)
			{
				if (i % interval == 0)
					tracker.progress(i, n);
			}

			Split split = splitSets.get(i);
			if (multiThread)
			{
				// If the indices are unique within each split set then we can multi-thread the 
				// sampling of neighbours (since each index in the cumulative arrays will only 
				// be accessed concurrently by a single thread).
				int nPerThread = (int) Math.ceil((double) split.sets.size() / nThreads);
				for (int from = 0; from < split.sets.size();)
				{
					int to = Math.min(from + nPerThread, split.sets.size());
					futures.add(
							executor.submit(new SetWorker(sumDistances, nDistances, neighbours, split.sets, from, to)));
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
				futures.clear();
			}
			else
			{
				sampleNeighbours(sumDistances, nDistances, neighbours, split.sets, 0, split.sets.size());
			}
		}

		if (multiThread)
			executor.shutdown();

		// Finalise averages
		// Convert to simple arrays
		allNeighbours = new int[size][];
		for (int it = size; it-- > 0;)
		{
			setOfObjects[it].coreDistance = getCoreDistance(sumDistances[it], nDistances[it]);

			allNeighbours[it] = neighbours[it].toArray();
			neighbours[it] = null; // Allow garbage collection
		}

		if (tracker != null)
		{
			time = System.currentTimeMillis() - time;
			tracker.log("Computed density and neighbourhoods (%d distances) ... %s", distanceComputations.get(),
					Utils.timeToString(time));
			tracker.progress(1);
		}

		return allNeighbours;
	}

	/**
	 * Sample neighbours for each set in the split sets between the from index (inclusive) and to index (exclusive).
	 *
	 * @param sumDistances
	 *            the neighbour sum of distances
	 * @param nDistances
	 *            the neighbour count of distances
	 * @param neighbours
	 *            the neighbour hash sets
	 * @param sets
	 *            the split sets
	 * @param from
	 *            the from index
	 * @param to
	 *            the to index
	 */
	private void sampleNeighbours(double[] sumDistances, int[] nDistances, TIntHashSet[] neighbours,
			TurboList<int[]> sets, int from, int to)
	{
		switch (sampleMode)
		{
			case RANDOM:
				for (int i = from; i < to; i++)
					sampleNeighboursRandom(sumDistances, nDistances, neighbours, sets.get(i));
				break;
			case MEDIAN:
				for (int i = from; i < to; i++)
					sampleNeighboursUsingMedian(sumDistances, nDistances, neighbours, sets.get(i));
				break;
			case ALL:
				for (int i = from; i < to; i++)
					sampleNeighboursAll(sumDistances, nDistances, neighbours, sets.get(i));
				break;
			default:
				throw new NotImplementedException("Unsupported sample mode: " + sampleMode);
		}
	}

	/**
	 * Sample neighbours using median. The distance of each point is computed to the median which is added as a
	 * neighbour. The median point has all the other points added as a neighbours.
	 * 
	 * @param sumDistances
	 *            the neighbour sum of distances
	 * @param nDistances
	 *            the neighbour count of distances
	 * @param neighbours
	 *            the neighbour hash sets
	 * @param indices
	 *            the indices of objects in the set
	 */
	private void sampleNeighboursUsingMedian(double[] sumDistances, int[] nDistances, TIntHashSet[] neighbours,
			int[] indices)
	{
		final int len = indices.length;
		final int indoff = len >> 1;
		int v = indices[indoff];
		int delta = len - 1;
		distanceComputations.addAndGet(delta);
		nDistances[v] += delta;
		Molecule midpoint = setOfObjects[v];
		for (int j = len; j-- > 0;)
		{
			int it = indices[j];
			if (it == v)
			{
				continue;
			}
			double dist = midpoint.distance(setOfObjects[it]);
			sumDistances[v] += dist;
			sumDistances[it] += dist;
			nDistances[it]++;

			neighbours[it].add(v);
			neighbours[v].add(it);
		}
	}

	/**
	 * Sample neighbours randomly. For each point A choose a neighbour from the set B. This is mirrored this to get
	 * another neighbour without extra distance computations. The distance between A and B is used to increment the
	 * input distance arrays and each is added to the set of the other.
	 * <p>
	 * This method works for sets of size 2 and above.
	 * 
	 * @param sumDistances
	 *            the neighbour sum of distances
	 * @param nDistances
	 *            the neighbour count of distances
	 * @param neighbours
	 *            the neighbour hash sets
	 * @param indices
	 *            the indices of objects in the set
	 */
	private void sampleNeighboursRandom(double[] sumDistances, int[] nDistances, TIntHashSet[] neighbours,
			int[] indices)
	{
		if (indices.length == 2)
		{
			distanceComputations.incrementAndGet();

			// Only one set of neighbours
			int a = indices[0];
			int b = indices[1];

			double dist = setOfObjects[a].distance(setOfObjects[b]);

			sumDistances[a] += dist;
			sumDistances[b] += dist;
			nDistances[a]++;
			nDistances[b]++;

			neighbours[a].add(b);
			neighbours[b].add(a);
		}
		else
		{
			distanceComputations.addAndGet(indices.length);

			// For a fast implementation we just pick consecutive 
			// points as neighbours since the order is random.
			// Note: This only works if the set has size 3 or more.

			for (int j = indices.length, k = 0; j-- > 0;)
			{
				int a = indices[j];
				int b = indices[k];
				k = j;

				double dist = setOfObjects[a].distance(setOfObjects[b]);

				sumDistances[a] += dist;
				sumDistances[b] += dist;
				nDistances[a] += 2; // Each object will have 2 due to mirroring.

				neighbours[a].add(b);
				neighbours[b].add(a);
			}
		}
	}

	/**
	 * Sample neighbours all-vs-all.
	 * 
	 * @param sumDistances
	 *            the neighbour sum of distances
	 * @param nDistances
	 *            the neighbour count of distances
	 * @param neighbours
	 *            the neighbour hash sets
	 * @param indices
	 *            the indices of objects in the set
	 */
	private void sampleNeighboursAll(double[] sumDistances, int[] nDistances, TIntHashSet[] neighbours, int[] indices)
	{
		int n = indices.length;
		int n1 = n - 1;

		// for all-vs-all = n(n-1)/2
		distanceComputations.addAndGet((n * n1) >>> 1);

		for (int i = 0; i < n1; i++)
		{
			int a = indices[i];
			nDistances[a] += n1;
			double d = 0;
			Molecule ma = setOfObjects[a];
			TIntHashSet na = neighbours[a];

			for (int j = i + 1; j < n; j++)
			{
				int b = indices[j];

				double dist = ma.distance(setOfObjects[b]);

				d += dist;
				sumDistances[b] += dist;

				na.add(b);
				neighbours[b].add(a);
			}

			sumDistances[a] += d;
		}

		// For the last index that was skipped in the outer loop.
		// The set will always be a positive size so do not worry about index bounds.
		nDistances[indices[n1]] += n1;
	}

	/**
	 * Gets the sample mode.
	 *
	 * @return the sample mode
	 */
	public SampleMode getSampleMode()
	{
		return sampleMode;
	}

	/**
	 * Sets the sample mode.
	 *
	 * @param sampleMode
	 *            the new sample mode
	 */
	public void setSampleMode(SampleMode sampleMode)
	{
		if (sampleMode == null)
			sampleMode = SampleMode.RANDOM;
		this.sampleMode = sampleMode;
	}
}
