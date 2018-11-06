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

package uk.ac.sussex.gdsc.core.clustering.optics;

import uk.ac.sussex.gdsc.core.data.AsynchronousException;
import uk.ac.sussex.gdsc.core.ij.ImageJUtils;
import uk.ac.sussex.gdsc.core.logging.Ticker;
import uk.ac.sussex.gdsc.core.logging.TrackProgress;
import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.core.utils.NotImplementedException;
import uk.ac.sussex.gdsc.core.utils.PseudoRandomGenerator;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.core.utils.Sort;
import uk.ac.sussex.gdsc.core.utils.TextUtils;
import uk.ac.sussex.gdsc.core.utils.TurboList;
import uk.ac.sussex.gdsc.core.utils.TurboRandomGenerator;

import gnu.trove.set.hash.TIntHashSet;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.UnitSphereSampler;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Store molecules and allows generation of random projections.
 *
 * <p>This class is an adaption of
 * de.lmu.ifi.dbs.elki.index.preprocessed.fastoptics.RandomProjectedNeighborsAndDensities. Copyright
 * (C) 2015. Johannes Schneider, ABB Research, Switzerland, johannes.schneider@alumni.ethz.ch.
 * Released under the GPL v3 licence.
 *
 * <p>Modifications have been made for multi-threading and different neighbour sampling modes. The
 * partitioning of the sets is essentially unchanged.
 */
class ProjectedMoleculeSpace extends MoleculeSpace {

  /**
   * Default constant used to compute number of projections as well as number of splits of point
   * set. E.G. constant *log N*d.
   */
  private static final int LOG_O_PROJECTION_CONSTANT = 20;

  /**
   * Sets used for neighbourhood computation should be about minSplitSize.
   *
   * <p>Sets are still used if they deviate by less (1+/- sizeTolerance).
   */
  private static final float SIZE_TOLERANCE = 2f / 3;

  /** Used for access to the raw coordinates. */
  protected final OpticsManager opticsManager;

  /** The tracker. */
  private TrackProgress tracker;

  /**
   * Store the results of a split of the dataset.
   */
  private static class Split {

    /** The sets. */
    final TurboList<int[]> sets;

    /**
     * Instantiates a new split.
     *
     * @param sets the sets
     */
    Split(TurboList<int[]> sets) {
      this.sets = sets;
    }
  }

  /** Sets that resulted from recursive split of entire point set. */
  TurboList<Split> splitSets;

  /**
   * Random factory.
   */
  private final UniformRandomProvider rand;

  /** The neighbours of each point. */
  int[][] allNeighbours;

  /**
   * The number of splits to compute (if below 1 it will be auto-computed using the size of the
   * data).
   */
  int numberOfSplits = 0;

  /**
   * The number of projections to compute (if below 1 it will be auto-computed using the size of the
   * data).
   */
  int numberOfProjections = 0;

  /**
   * Set to true to save all sets that are approximately min split size. The default is to only save
   * sets smaller than min split size.
   */
  boolean saveApproximateSets = false;

  /** The sample mode. */
  private SampleMode sampleMode;

  /**
   * Set to true to use random vectors for the projections. The default is to uniformly create
   * vectors on the semi-circle interval.
   */
  boolean useRandomVectors = false;

  /** The number of threads to use. */
  int numberOfThreads = 1;

  /** The number of distance computations. */
  AtomicInteger distanceComputations = new AtomicInteger();

  /**
   * Instantiates a new projected molecule space.
   *
   * @param opticsManager the optics manager
   * @param generatingDistanceE the generating distance (E)
   * @param rand the random source
   */
  ProjectedMoleculeSpace(OpticsManager opticsManager, float generatingDistanceE,
      UniformRandomProvider rand) {
    super(opticsManager.getSize(), generatingDistanceE);

    this.opticsManager = opticsManager;
    this.rand = rand;
  }

  @Override
  public String toString() {
    return String.format("%s", this.getClass().getSimpleName());
  }

  @Override
  Molecule[] generate() {
    final float[] xcoord = opticsManager.getXData();
    final float[] ycoord = opticsManager.getYData();

    setOfObjects = new Molecule[xcoord.length];
    for (int i = 0; i < xcoord.length; i++) {
      final float x = xcoord[i];
      final float y = ycoord[i];
      setOfObjects[i] = new Molecule(i, x, y);
    }

    return setOfObjects;
  }

  @Override
  void findNeighbours(int minPts, Molecule object, float generatingDistance) {
    // Return the neighbours found in {@link #computeAverageDistInSetAndNeighbours()}.
    // Assume allNeighbours has been computed.
    neighbours.clear();
    final int[] list = allNeighbours[object.id];
    for (int i = list.length; i-- > 0;) {
      neighbours.add(setOfObjects[list[i]]);
    }
  }

  @Override
  void findNeighboursAndDistances(int minPts, Molecule object, float generatingDistance) {
    // Return the neighbours found in {@link #computeAverageDistInSetAndNeighbours()}.
    // Assume allNeighbours has been computed.
    neighbours.clear();
    final int[] list = allNeighbours[object.id];
    for (int i = list.length; i-- > 0;) {
      final Molecule otherObject = setOfObjects[list[i]];
      otherObject.setD(object.distanceSquared(otherObject));
      neighbours.add(otherObject);
    }
  }

  /**
   * Sets the tracker.
   *
   * @param tracker the new tracker
   */
  public void setTracker(TrackProgress tracker) {
    this.tracker = tracker;
  }

  /**
   * The Class Job.
   */
  private abstract class Job {

    /** The index. */
    final int index;

    /**
     * Instantiates a new job.
     *
     * @param index the index
     */
    Job(int index) {
      this.index = index;
    }
  }

  /**
   * The Class ProjectionJob.
   */
  private class ProjectionJob extends Job {

    /** The vector. */
    final double[] vector;

    /**
     * Instantiates a new projection job.
     *
     * @param index the index
     * @param vector the vector
     */
    ProjectionJob(int index, double[] vector) {
      super(index);
      this.vector = vector;
    }
  }

  /**
   * The Class SplitJob.
   */
  private class SplitJob extends Job {

    /** The projected points. */
    final float[][] projectedPoints;

    /** The rand. */
    final TurboRandomGenerator rand;

    /**
     * Instantiates a new split job.
     *
     * @param index the index
     * @param projectedPoints the projected points
     * @param rand the rand
     */
    SplitJob(int index, float[][] projectedPoints, TurboRandomGenerator rand) {
      super(index);
      this.projectedPoints = projectedPoints;
      this.rand = rand;
    }
  }

  /**
   * The Class ProjectionWorker.
   */
  private class ProjectionWorker implements Runnable {
    /** The finished. */
    volatile boolean finished = false;

    /** The ticker. */
    final Ticker ticker;

    /** The jobs. */
    final BlockingQueue<ProjectionJob> jobs;

    /** The projected points. */
    final float[][] projectedPoints;

    /**
     * Instantiates a new projection worker.
     *
     * @param ticker the ticker
     * @param jobs the jobs
     * @param projectedPoints the projected points
     */
    public ProjectionWorker(Ticker ticker, BlockingQueue<ProjectionJob> jobs,
        float[][] projectedPoints) {
      this.ticker = ticker;
      this.jobs = jobs;
      this.projectedPoints = projectedPoints;
    }

    @Override
    public void run() {
      try {
        while (true) {
          final ProjectionJob job = jobs.take();
          if (job.index < 0) {
            break;
          }
          if (!finished) {
            // Only run jobs when not finished. This allows the queue to be emptied.
            run(job);
          }
        }
      } catch (final InterruptedException ex) {
        // Restore interrupted state...
        Thread.currentThread().interrupt();
        System.out.println(ex.toString());
        throw new AsynchronousException(ex);
      } finally {
        finished = true;
      }
    }

    /**
     * Run.
     *
     * @param job the job
     */
    private void run(ProjectionJob job) {
      final double[] v = job.vector;

      // Project points to the vector and compute the distance along the vector from the origin
      final float[] currPro = new float[size];
      for (int it = size; it-- > 0;) {
        final Molecule m = setOfObjects[it];
        // Dot product:
        currPro[it] = (float) (v[0] * m.x + v[1] * m.y);
      }
      projectedPoints[job.index] = currPro;

      ticker.tick();
    }
  }

  /**
   * The Class SplitWorker.
   */
  private class SplitWorker implements Runnable {

    /** The finished. */
    volatile boolean finished = false;

    /** The ticker. */
    final Ticker ticker;

    /** The jobs. */
    final BlockingQueue<SplitJob> jobs;

    /** The min split size. */
    final int minSplitSize;

    /** The split sets. */
    final TurboList<Split> splitSets = new TurboList<>();

    /**
     * Instantiates a new split worker.
     *
     * @param ticker the ticker
     * @param jobs the jobs
     * @param minSplitSize the min split size
     */
    public SplitWorker(Ticker ticker, BlockingQueue<SplitJob> jobs, int minSplitSize) {
      this.ticker = ticker;
      this.jobs = jobs;
      this.minSplitSize = minSplitSize;
    }

    @Override
    public void run() {
      try {
        while (true) {
          final SplitJob job = jobs.take();
          if (job.index < 0) {
            break;
          }
          if (!finished) {
            // Only run jobs when not finished. This allows the queue to be emptied.
            run(job);
          }
        }
      } catch (final InterruptedException ex) {
        // Restore interrupted state...
        Thread.currentThread().interrupt();
        throw new AsynchronousException(ex);
      } finally {
        finished = true;
      }
    }

    /**
     * Run.
     *
     * @param job the job
     */
    private void run(SplitJob job) {
      final TurboList<int[]> sets = new TurboList<>();
      splitupNoSort(sets, job.projectedPoints, SimpleArrayUtils.newArray(size, 0, 1), 0, size, 0,
          job.rand, minSplitSize);
      splitSets.add(new Split(sets));
      ticker.tick();
    }
  }

  /**
   * The Class SetWorker.
   */
  private class SetWorker implements Runnable {

    /** The sum distances. */
    final double[] sumDistances;

    /** The n distances. */
    final int[] countDistances;

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
     * @param sumDistances the sum distances
     * @param countDistances the n distances
     * @param neighbours the neighbours
     * @param sets the sets
     * @param from the from
     * @param to the to
     */
    public SetWorker(double[] sumDistances, int[] countDistances, TIntHashSet[] neighbours,
        TurboList<int[]> sets, int from, int to) {
      this.sumDistances = sumDistances;
      this.countDistances = countDistances;
      this.neighbours = neighbours;
      this.sets = sets;
      this.from = from;
      this.to = to;
    }

    @Override
    public void run() {
      sampleNeighbours(sumDistances, countDistances, neighbours, sets, from, to);
    }
  }

  /**
   * Create random projections, project points and put points into sets of size about
   * minSplitSize/2.
   *
   * @param minSplitSize minimum size for which a point set is further partitioned (roughly
   *        corresponds to minPts in OPTICS)
   */
  public void computeSets(int minSplitSize) {
    splitSets = new TurboList<>();

    // Edge cases
    if (minSplitSize < 2 || size <= 1) {
      return;
    }

    if (size == 2) {
      // No point performing projections and splits
      TurboList<int[]> sets = new TurboList<>(1);
      sets.add(new int[] {0, 1});
      splitSets.add(new Split(sets));
      return;
    }

    final int dim = 2;

    // FastOPTICS paper states you can use c0*log(N) sets and c1*log(N) projections.
    // The ELKI framework increase this for the number of dimensions. However I have stuck
    // with the original (as it is less so will be faster).
    // Note: In most computer science contexts log is in base 2.
    final int numberOfSplitSets = getNumberOfSplitSets(numberOfSplits, size);
    final int localNumberOfProjections = getNumberOfProjections(numberOfProjections, size);

    // perform O(log N+log dim) splits of the entire point sets projections
    // numberOfSplitSets = (int) (logOProjectionConst * log2(size * dim + 1))
    // perform O(log N+log dim) projections of the point set onto a random line
    // localNumberOfProjections = (int) (logOProjectionConst * log2(size * dim + 1))

    if (numberOfSplitSets < 1 || localNumberOfProjections < 1) {
      return; // Nothing to do
    }

    // perform projections of points
    final float[][] projectedPoints = new float[localNumberOfProjections][];

    long time = System.currentTimeMillis();
    if (tracker != null) {
      tracker.log("Computing projections ...");
    }

    // Multi-thread this for speed
    final int threadCount = Math.min(this.numberOfThreads, numberOfSplitSets);
    final TurboList<Thread> threads = new TurboList<>(threadCount);
    Ticker ticker = Ticker.create(tracker, localNumberOfProjections, threadCount > 1);

    final BlockingQueue<ProjectionJob> projectionJobs = new ArrayBlockingQueue<>(threadCount * 2);
    final TurboList<ProjectionWorker> projectionWorkers = new TurboList<>(threadCount);
    for (int i = 0; i < threadCount; i++) {
      final ProjectionWorker worker = new ProjectionWorker(ticker, projectionJobs, projectedPoints);
      final Thread t = new Thread(worker);
      projectionWorkers.addf(worker);
      threads.addf(t);
      t.start();
    }

    // Create random vectors or uniform distribution

    final UnitSphereSampler vectorGen = (useRandomVectors) ? new UnitSphereSampler(2, rand) : null;
    final double increment = Math.PI / localNumberOfProjections;
    for (int i = 0; i < localNumberOfProjections; i++) {
      // Create a random unit vector
      double[] randomVector;
      if (vectorGen != null) {
        randomVector = vectorGen.nextVector();
      } else {
        // For a 2D vector we can just uniformly distribute them around a semi-circle
        randomVector = new double[dim];
        final double a = i * increment;
        randomVector[0] = Math.sin(a);
        randomVector[1] = Math.cos(a);
      }
      put(projectionJobs, new ProjectionJob(i, randomVector));
    }
    // Finish all the worker threads by passing in a null job
    for (int i = 0; i < threadCount; i++) {
      put(projectionJobs, new ProjectionJob(-1, null));
    }

    // Wait for all to finish
    for (int i = 0; i < threadCount; i++) {
      try {
        threads.getf(i).join();
      } catch (final InterruptedException ex) {
        // Restore interrupted state...
        Thread.currentThread().interrupt();
        ex.printStackTrace();
      }
    }
    threads.clear();

    if (tracker != null) {
      tracker.progress(1);
      final long time2 = System.currentTimeMillis();
      tracker.log("Computed projections ... " + TextUtils.millisToString(time2 - time));
      time = time2;
      tracker.log("Splitting data ...");
    }

    // split entire point set, reuse projections by shuffling them
    final int[] proind = SimpleArrayUtils.newArray(localNumberOfProjections, 0, 1);

    // The splits do not have to be that random so we can use a pseudo random sequence.
    // The sets will be randomly sized between 1 and minSplitSize. Ensure we have enough
    // numbers for all the splits.
    final double expectedSetSize = (1 + minSplitSize) * 0.5;
    final int expectedSets = (int) Math.round(size / expectedSetSize);
    final TurboRandomGenerator pseudoRandom = new TurboRandomGenerator(
        MathUtils.max(numberOfSplitSets, 200, minSplitSize + 2 * expectedSets), rand);

    // Multi-thread this for speed
    final BlockingQueue<SplitJob> splitJobs = new ArrayBlockingQueue<>(threadCount * 2);
    final TurboList<SplitWorker> splitWorkers = new TurboList<>(threadCount);
    ticker = Ticker.create(tracker, numberOfSplitSets, threadCount > 1);

    for (int i = 0; i < threadCount; i++) {
      final SplitWorker worker = new SplitWorker(ticker, splitJobs, minSplitSize);
      final Thread t = new Thread(worker);
      splitWorkers.addf(worker);
      threads.addf(t);
      t.start();
    }

    for (int i = 0; i < numberOfSplitSets; i++) {
      // shuffle projections
      final float[][] shuffledProjectedPoints = new float[localNumberOfProjections][];
      pseudoRandom.shuffle(proind);
      for (int j = 0; j < localNumberOfProjections; j++) {
        shuffledProjectedPoints[j] = projectedPoints[proind[j]];
      }

      // New random generator
      final TurboRandomGenerator pseudoRandomCopy = pseudoRandom.copy();
      pseudoRandomCopy.setSeed(i);

      put(splitJobs, new SplitJob(i, shuffledProjectedPoints, pseudoRandomCopy));
    }

    // Finish all the worker threads by passing in a null job
    for (int i = 0; i < threadCount; i++) {
      put(splitJobs, new SplitJob(-1, null, null));
    }

    // Wait for all to finish
    int total = 0;
    for (int i = 0; i < threadCount; i++) {
      try {
        threads.getf(i).join();
        total += splitWorkers.getf(i).splitSets.size();
      } catch (final InterruptedException ex) {
        // Restore interrupted state...
        Thread.currentThread().interrupt();
        ex.printStackTrace();
      }
    }
    threads.clear();

    // Merge the split-sets
    splitSets = splitWorkers.getf(0).splitSets;
    splitSets.ensureCapacity(total);
    for (int i = 1; i < threadCount; i++) {
      splitSets.addAll(splitWorkers.getf(i).splitSets);
    }

    if (tracker != null) {
      time = System.currentTimeMillis() - time;
      tracker.log("Split data ... " + TextUtils.millisToString(time));
      tracker.progress(1);
    }
  }

  /**
   * Put.
   *
   * @param <T> the generic type
   * @param jobs the jobs
   * @param job the job
   */
  private static <T> void put(BlockingQueue<T> jobs, T job) {
    try {
      jobs.put(job);
    } catch (final InterruptedException ex) {
      // Restore interrupted state...
      Thread.currentThread().interrupt();
      throw new AsynchronousException("Unexpected interruption", ex);
    }
  }

  /**
   * Gets the number of split sets.
   *
   * @param numberOfSplits The number of splits to compute (if below 1 it will be auto-computed
   *        using the size of the data)
   * @param size the size
   * @return the number of split sets
   */
  public static int getNumberOfSplitSets(int numberOfSplits, int size) {
    if (size < 2) {
      return 0;
    }
    return (numberOfSplits > 0) ? numberOfSplits : (int) (LOG_O_PROJECTION_CONSTANT * log2(size));
  }

  /**
   * Gets the number of projections.
   *
   * @param numberOfProjections The number of projections to compute (if below 1 it will be
   *        auto-computed using the size of the data)
   * @param size the size
   * @return the number of projections
   */
  public static int getNumberOfProjections(int numberOfProjections, int size) {
    return getNumberOfSplitSets(numberOfProjections, size);
  }

  /**
   * 1. / log(2)
   */
  public static final double ONE_BY_LOG2 = 1. / Math.log(2.);

  /**
   * Compute the base 2 logarithm.
   *
   * @param x X
   * @return Logarithm base 2.
   */
  public static double log2(double x) {
    return Math.log(x) * ONE_BY_LOG2;
  }

  /**
   * Recursively splits entire point set until the set is below a threshold.
   *
   * @param splitSets the split sets
   * @param projectedPoints the projected points
   * @param ind points that are in the current set
   * @param begin Interval begin in the ind array
   * @param end Interval end in the ind array
   * @param dim depth of projection (how many times point set has been split already)
   * @param rand Random generator
   * @param minSplitSize minimum size for which a point set is further partitioned (roughly
   *        corresponds to minPts in OPTICS)
   */
  private void splitupNoSort(TurboList<int[]> splitSets, float[][] projectedPoints, int[] ind,
      int begin, int end, int dim, PseudoRandomGenerator rand, int minSplitSize) {
    final int nele = end - begin;

    if (nele < 2) {
      // Nothing to split. Also ensures we only add to the sets if neighbours can be sampled.
      return;
    }

    dim = dim % projectedPoints.length;// choose a projection of points
    final float[] tpro = projectedPoints[dim];

    if (saveApproximateSets) {
      // save set such that used for density or neighbourhood computation
      // sets should be roughly minSplitSize
      // -=-=-
      // Note: This is the method used in ELKI which uses the distance to the median of the set
      // (thus no distances are computed that are between points very far apart, e.g. each end
      // of the set).
      if (nele > minSplitSize * (1 - SIZE_TOLERANCE)
          && nele < minSplitSize * (1 + SIZE_TOLERANCE)) {
        saveSet(splitSets, ind, begin, end, rand, tpro);
      }
    }

    // compute splitting element
    // do not store set or even sort set, since it is too large
    if (nele > minSplitSize) {
      // splits can be performed either by distance (between min,maxCoord) or by
      // picking a point randomly(picking index of point)
      // outcome is similar

      // int minInd = splitByDistance(ind, begin, end, tpro, rand)
      final int minInd = splitRandomly(ind, begin, end, tpro, rand);

      // split set recursively
      // position used for splitting the projected points into two
      // sets used for recursive splitting
      final int splitpos = minInd + 1;
      splitupNoSort(splitSets, projectedPoints, ind, begin, splitpos, dim + 1, rand, minSplitSize);
      splitupNoSort(splitSets, projectedPoints, ind, splitpos, end, dim + 1, rand, minSplitSize);
    } else if (!saveApproximateSets) {
      // It it wasn't saved as an approximate set then make sure it is saved as it is less than
      // minSplitSize
      saveSet(splitSets, ind, begin, end, rand, tpro);
    }
  }

  private void saveSet(TurboList<int[]> splitSets, int[] ind, int begin, int end,
      PseudoRandomGenerator rand, float[] tpro) {
    final int[] indices = Arrays.copyOfRange(ind, begin, end);
    if (sampleMode == SampleMode.RANDOM) {
      // Ensure the indices are random
      rand.shuffle(indices);
    } else if (sampleMode == SampleMode.MEDIAN) {
      // sort set, since need median element later
      // (when computing distance to the middle of the set)
      Sort.sort(indices, tpro);
    }
    splitSets.add(indices);
  }

  /**
   * Split the data set randomly.
   *
   * @param ind Object index
   * @param begin Interval begin
   * @param end Interval end
   * @param tpro Projection
   * @param rand Random generator
   * @return Splitting point
   */
  public static int splitRandomly(int[] ind, int begin, int end, float[] tpro,
      RandomGenerator rand) {
    final int nele = end - begin;

    // pick random splitting element based on position
    final float rs = tpro[ind[begin + rand.nextInt(nele)]];
    int minInd = begin;
    int maxInd = end - 1;
    // permute elements such that all points smaller than the splitting
    // element are on the right and the others on the left in the array
    while (minInd < maxInd) {
      final float currEle = tpro[ind[minInd]];
      if (currEle > rs) {
        while (minInd < maxInd && tpro[ind[maxInd]] > rs) {
          maxInd--;
        }
        if (minInd == maxInd) {
          break;
        }
        swap(ind, minInd, maxInd);
        maxInd--;
      }
      minInd++;
    }
    // if all elements are the same split in the middle
    if (minInd == end - 1) {
      minInd = (begin + end) >>> 1;
    }
    return minInd;
  }

  /**
   * Swap the value of the two indices.
   *
   * @param data the data
   * @param index1 the index1
   * @param index2 the index2
   */
  private static void swap(int[] data, int index1, int index2) {
    final int tmp = data[index1];
    data[index1] = data[index2];
    data[index2] = tmp;
  }

  /**
   * Split the data set by distances.
   *
   * @param ind Object index
   * @param begin Interval begin
   * @param end Interval end
   * @param tpro Projection
   * @param rand Random generator
   * @return Splitting point
   */
  public static int splitByDistance(int[] ind, int begin, int end, float[] tpro,
      RandomGenerator rand) {
    // pick random splitting point based on distance
    float rmin = tpro[ind[begin]];
    float rmax = rmin;
    for (int it = begin + 1; it < end; it++) {
      final float currEle = tpro[ind[it]];
      if (currEle < rmin) {
        rmin = currEle;
      } else if (currEle > rmax) {
        rmax = currEle;
      }
    }

    if (rmin != rmax) { // if not all elements are the same
      final float rs = (float) (rmin + rand.nextDouble() * (rmax - rmin));

      int minInd = begin;
      int maxInd = end - 1;

      // permute elements such that all points smaller than the splitting
      // element are on the right and the others on the left in the array
      while (minInd < maxInd) {
        final float currEle = tpro[ind[minInd]];
        if (currEle > rs) {
          while (minInd < maxInd && tpro[ind[maxInd]] > rs) {
            maxInd--;
          }
          if (minInd == maxInd) {
            break;
          }
          swap(ind, minInd, maxInd);
          maxInd--;
        }
        minInd++;
      }
      return minInd;
    }
    // if all elements are the same split in the middle
    return (begin + end) >>> 1;
  }

  /**
   * Gets the core distance. We actually return the squared distance.
   *
   * @param sum the sum of distances
   * @param count the count of distances
   * @return the squared average core distance
   */
  private static float getCoreDistance(double sum, int count) {
    // it might be that a point does not occur for a certain size of a
    // projection (likely if too few projections, in this case there is no avg
    // distance)
    if (count == 0) {
      return OpticsManager.UNDEFINED;
    }
    final double d = sum / count;
    // We actually want the squared distance
    return (float) (d * d);
  }

  /**
   * Compute for each point the average distance to a point in a projected set and list of neighbors
   * for each point from sets resulting from projection.
   *
   * @return list of neighbours for each point
   */
  @SuppressWarnings("null")
  public int[][] computeAverageDistInSetAndNeighbours() {
    distanceComputations.set(0);

    // Q. Are the neighbours worked out using the core distance?
    // The FastOPTICS paper discusses a merging distance as the min of the core distance for A and
    // B.
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
    if (tracker != null) {
      tracker.log("Computing density and neighbourhoods ...");
    }

    final double[] sumDistances = new double[size];
    final int[] countDistances = new int[size];
    final TIntHashSet[] neighbours = new TIntHashSet[size];
    for (int it = size; it-- > 0;) {
      neighbours[it] = new TIntHashSet();
    }

    // Multi-thread the hash set operations for speed.
    // We can do this if each split uses each index only once.
    final int nThreads = Math.min(this.numberOfThreads, n);
    final boolean multiThread = (n > 1 && !saveApproximateSets);

    // Use an executor service so that we know the entire split has been processed before
    // doing the next split.
    ExecutorService executor = null;
    TurboList<Future<?>> futures = null;
    if (multiThread) {
      executor = Executors.newFixedThreadPool(nThreads);
      futures = new TurboList<>(nThreads);
    }

    final int interval = ImageJUtils.getProgressInterval(n);
    for (int i = 0; i < n; i++) {
      if (tracker != null && i % interval == 0) {
        tracker.progress(i, n);
      }

      final Split split = splitSets.getf(i);
      if (multiThread) {
        // If the indices are unique within each split set then we can multi-thread the
        // sampling of neighbours (since each index in the cumulative arrays will only
        // be accessed concurrently by a single thread).
        final int nPerThread = (int) Math.ceil((double) split.sets.size() / nThreads);
        for (int from = 0; from < split.sets.size();) {
          final int to = Math.min(from + nPerThread, split.sets.size());
          futures.add(executor.submit(
              new SetWorker(sumDistances, countDistances, neighbours, split.sets, from, to)));
          from = to;
        }
        // Wait for all to finish
        for (int t = futures.size(); t-- > 0;) {
          try {
            // The future .get() method will block until completed
            futures.get(t).get();
          } catch (final Exception ex) {
            // This should not happen.
            // Ignore it and allow processing to continue (the number of neighbour samples will just
            // be smaller).
            ex.printStackTrace();
          }
        }
        futures.clear();
      } else {
        sampleNeighbours(sumDistances, countDistances, neighbours, split.sets, 0,
            split.sets.size());
      }
    }

    // Finalise averages
    // Convert to simple arrays
    allNeighbours = new int[size][];
    for (int it = size; it-- > 0;) {
      setOfObjects[it].coreDistance = getCoreDistance(sumDistances[it], countDistances[it]);

      allNeighbours[it] = neighbours[it].toArray();
      neighbours[it] = null; // Allow garbage collection
    }

    if (tracker != null) {
      time = System.currentTimeMillis() - time;
      tracker.log("Computed density and neighbourhoods (%d distances) ... %s",
          distanceComputations.get(), TextUtils.millisToString(time));
      tracker.progress(1);
    }

    return allNeighbours;
  }

  /**
   * Sample neighbours for each set in the split sets between the from index (inclusive) and to
   * index (exclusive).
   *
   * @param sumDistances the neighbour sum of distances
   * @param countDistances the neighbour count of distances
   * @param neighbours the neighbour hash sets
   * @param sets the split sets
   * @param from the from index
   * @param to the to index
   */
  private void sampleNeighbours(double[] sumDistances, int[] countDistances,
      TIntHashSet[] neighbours, TurboList<int[]> sets, int from, int to) {
    switch (sampleMode) {
      case RANDOM:
        for (int i = from; i < to; i++) {
          sampleNeighboursRandom(sumDistances, countDistances, neighbours, sets.getf(i));
        }
        break;
      case MEDIAN:
        for (int i = from; i < to; i++) {
          sampleNeighboursUsingMedian(sumDistances, countDistances, neighbours, sets.getf(i));
        }
        break;
      case ALL:
        for (int i = from; i < to; i++) {
          sampleNeighboursAll(sumDistances, countDistances, neighbours, sets.getf(i));
        }
        break;
      default:
        throw new NotImplementedException("Unsupported sample mode: " + sampleMode);
    }
  }

  /**
   * Sample neighbours using median. The distance of each point is computed to the median which is
   * added as a neighbour. The median point has all the other points added as a neighbours.
   *
   * @param sumDistances the neighbour sum of distances
   * @param countDistances the neighbour count of distances
   * @param neighbours the neighbour hash sets
   * @param indices the indices of objects in the set
   */
  private void sampleNeighboursUsingMedian(double[] sumDistances, int[] countDistances,
      TIntHashSet[] neighbours, int[] indices) {
    final int len = indices.length;
    final int indoff = len >> 1;
    final int v = indices[indoff];
    final int delta = len - 1;
    distanceComputations.addAndGet(delta);
    countDistances[v] += delta;
    final Molecule midpoint = setOfObjects[v];
    for (int j = len; j-- > 0;) {
      final int it = indices[j];
      if (it == v) {
        continue;
      }
      final double dist = midpoint.distance(setOfObjects[it]);
      sumDistances[v] += dist;
      sumDistances[it] += dist;
      countDistances[it]++;

      neighbours[it].add(v);
      neighbours[v].add(it);
    }
  }

  /**
   * Sample neighbours randomly. For each point A choose a neighbour from the set B. This is
   * mirrored this to get another neighbour without extra distance computations. The distance
   * between A and B is used to increment the input distance arrays and each is added to the set of
   * the other.
   *
   * <p>This method works for sets of size 2 and above.
   *
   * @param sumDistances the neighbour sum of distances
   * @param countDistances the neighbour count of distances
   * @param neighbours the neighbour hash sets
   * @param indices the indices of objects in the set
   */
  private void sampleNeighboursRandom(double[] sumDistances, int[] countDistances,
      TIntHashSet[] neighbours, int[] indices) {
    if (indices.length == 2) {
      distanceComputations.incrementAndGet();

      // Only one set of neighbours
      final int a = indices[0];
      final int b = indices[1];

      final double dist = setOfObjects[a].distance(setOfObjects[b]);

      sumDistances[a] += dist;
      sumDistances[b] += dist;
      countDistances[a]++;
      countDistances[b]++;

      neighbours[a].add(b);
      neighbours[b].add(a);
    } else {
      distanceComputations.addAndGet(indices.length);

      // For a fast implementation we just pick consecutive
      // points as neighbours since the order is random.
      // Note: This only works if the set has size 3 or more.

      for (int j = indices.length, k = 0; j-- > 0;) {
        final int a = indices[j];
        final int b = indices[k];
        k = j;

        final double dist = setOfObjects[a].distance(setOfObjects[b]);

        sumDistances[a] += dist;
        sumDistances[b] += dist;
        countDistances[a] += 2; // Each object will have 2 due to mirroring.

        neighbours[a].add(b);
        neighbours[b].add(a);
      }
    }
  }

  /**
   * Sample neighbours all-vs-all.
   *
   * @param sumDistances the neighbour sum of distances
   * @param countDistances the neighbour count of distances
   * @param neighbours the neighbour hash sets
   * @param indices the indices of objects in the set
   */
  private void sampleNeighboursAll(double[] sumDistances, int[] countDistances,
      TIntHashSet[] neighbours, int[] indices) {
    final int n = indices.length;
    final int n1 = n - 1;

    // for all-vs-all = n(n-1)/2
    distanceComputations.addAndGet((n * n1) >>> 1);

    for (int i = 0; i < n1; i++) {
      final int a = indices[i];
      countDistances[a] += n1;
      double sum = 0;
      final Molecule ma = setOfObjects[a];
      final TIntHashSet na = neighbours[a];

      for (int j = i + 1; j < n; j++) {
        final int b = indices[j];

        final double dist = ma.distance(setOfObjects[b]);

        sum += dist;
        sumDistances[b] += dist;

        na.add(b);
        neighbours[b].add(a);
      }

      sumDistances[a] += sum;
    }

    // For the last index that was skipped in the outer loop.
    // The set will always be a positive size so do not worry about index bounds.
    countDistances[indices[n1]] += n1;
  }

  /**
   * Gets the sample mode.
   *
   * @return the sample mode
   */
  public SampleMode getSampleMode() {
    return sampleMode;
  }

  /**
   * Sets the sample mode.
   *
   * @param sampleMode the new sample mode
   */
  public void setSampleMode(SampleMode sampleMode) {
    if (sampleMode == null) {
      sampleMode = SampleMode.RANDOM;
    }
    this.sampleMode = sampleMode;
  }
}
