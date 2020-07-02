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
 * Copyright (C) 2011 - 2020 Alex Herbert
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

import gnu.trove.set.hash.TIntHashSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToDoubleBiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.concurrent.ConcurrentRuntimeException;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.UnitSphereSampler;
import uk.ac.sussex.gdsc.core.data.NotImplementedException;
import uk.ac.sussex.gdsc.core.data.VisibleForTesting;
import uk.ac.sussex.gdsc.core.logging.Ticker;
import uk.ac.sussex.gdsc.core.logging.TrackProgress;
import uk.ac.sussex.gdsc.core.utils.LocalList;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.core.utils.SortUtils;
import uk.ac.sussex.gdsc.core.utils.TextUtils;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;
import uk.ac.sussex.gdsc.core.utils.concurrent.ConcurrencyUtils;
import uk.ac.sussex.gdsc.core.utils.rng.Pcg32;
import uk.ac.sussex.gdsc.core.utils.rng.RandomUtils;

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
   * 1. / log(2)
   */
  public static final double ONE_BY_LOG2 = 1. / Math.log(2.);

  /**
   * Sets used for neighbourhood computation should be about minSplitSize.
   *
   * <p>Sets are still used if they deviate by less (1+/- sizeTolerance).
   */
  private static final float SIZE_TOLERANCE = 2f / 3;

  /** Minimum size where neighbours are possible. */
  private static final int MIN_NEIGHBOURS_SIZE = 2;

  /** Used for access to the raw coordinates and distance function. */
  final OpticsManager opticsManager;

  /**
   * Random factory.
   */
  private final UniformRandomProvider rand;

  /** The tracker. */
  private TrackProgress tracker;

  /**
   * The number of splits to compute (if below 1 it will be auto-computed using the size of the
   * data).
   */
  private int numberOfSplits;

  /**
   * The number of projections to compute (if below 1 it will be auto-computed using the size of the
   * data).
   */
  private int numberOfProjections;

  /**
   * Set to true to save all sets that are approximately min split size. The default is to only save
   * sets smaller than min split size.
   */
  private boolean saveApproximateSets;

  /** The sample mode. */
  private SampleMode sampleMode;

  /**
   * Set to true to use random vectors for the projections. The default is to uniformly create
   * vectors on the semi-circle interval.
   */
  private boolean useRandomVectors;

  /** The executorService service to use for multi-threading. */
  private ExecutorService executorService;

  /** Sets that resulted from recursive split of entire point set. */
  private LocalList<Split> splitSets;

  /** The neighbours of each point. */
  private int[][] allNeighbours;

  /** The number of distance computations. */
  private final AtomicInteger distanceComputations = new AtomicInteger();

  /**
   * Store the results of a split of the dataset.
   */
  private static class Split {

    /** The sets. */
    final LocalList<int[]> sets;

    /**
     * Instantiates a new split.
     *
     * @param sets the sets
     */
    Split(LocalList<int[]> sets) {
      this.sets = sets;
    }
  }

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

    IntFunction<Molecule> factory;
    if (opticsManager.is3d()) {
      final float[] zcoord = opticsManager.getZData();
      factory = i -> {
        final float x = xcoord[i];
        final float y = ycoord[i];
        final float z = zcoord[i];
        return new Molecule3d(i, x, y, z);
      };
    } else {
      factory = i -> {
        final float x = xcoord[i];
        final float y = ycoord[i];
        return new Molecule(i, x, y);
      };
    }
    setOfObjects = new Molecule[xcoord.length];
    for (int i = 0; i < xcoord.length; i++) {
      setOfObjects[i] = factory.apply(i);
    }

    return setOfObjects;
  }

  /**
   * Create random projections, project points and put points into sets of size about
   * minSplitSize/2.
   *
   * @param minSplitSize minimum size for which a point set is further partitioned (roughly
   *        corresponds to minPts in OPTICS)
   * @throws ConcurrentRuntimeException If interrupted while computing
   */
  public void computeSets(int minSplitSize) {
    splitSets = new LocalList<>();

    // Edge cases
    if (minSplitSize < MIN_NEIGHBOURS_SIZE || size < MIN_NEIGHBOURS_SIZE) {
      return;
    }

    if (size == MIN_NEIGHBOURS_SIZE) {
      // No point performing projections and splits
      final LocalList<int[]> sets = new LocalList<>(1);
      sets.add(new int[] {0, 1});
      splitSets.add(new Split(sets));
      return;
    }

    // FastOPTICS paper states you can use c0*log(N) sets and c1*log(N) projections.
    // The ELKI framework increase this for the number of dimensions. However I have stuck
    // with the original (as it is less so will be faster).
    // Note: In most computer science contexts log is in base 2.
    final int numberOfSplitSets = getOrComputeNumberOfSplitSets(numberOfSplits, size);
    final int localNumberOfProjections = getOrComputeNumberOfProjections(numberOfProjections, size);

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

    // Create random vectors or uniform distribution
    final Supplier<double[]> vectorGen = createUnitVectorGenerator(localNumberOfProjections);
    final Function<double[], float[]> projector = createProjector();

    final Ticker ticker = Ticker.createStarted(tracker, localNumberOfProjections, true);
    final LocalList<Runnable> tasks = new LocalList<>();
    for (int i = 0; i < localNumberOfProjections; i++) {
      final double[] randomVector = vectorGen.get();
      final int index = i;
      tasks.add(() -> {
        projectedPoints[index] = projector.apply(randomVector);
        ticker.tick();
      });
    }

    runTasks(tasks);
    tasks.clear();

    if (tracker != null) {
      tracker.progress(1);
      final long time2 = System.currentTimeMillis();
      tracker.log("Computed projections ... " + TextUtils.millisToString(time2 - time));
      time = time2;
      tracker.log("Splitting data ...");
    }

    // split entire point set, reuse projections by shuffling them
    final int[] proind = SimpleArrayUtils.natural(localNumberOfProjections);

    // The splits do not have to be that random and the sets will be randomly sized between 1
    // and minSplitSize. Use a special generator that can be used to create non-overlapping
    // sequences.
    final Pcg32 rng = Pcg32.xshrs(rand.nextLong());

    final List<Split> syncSplitSets = Collections.synchronizedList(splitSets);
    final Ticker ticker2 = Ticker.createStarted(tracker, numberOfSplitSets, true);
    for (int i = 0; i < numberOfSplitSets; i++) {
      // shuffle projections
      RandomUtils.shuffle(proind, rand);
      final float[][] shuffledProjectedPoints = new float[localNumberOfProjections][];
      for (int j = 0; j < localNumberOfProjections; j++) {
        shuffledProjectedPoints[j] = projectedPoints[proind[j]];
      }

      tasks.add(() -> {
        final LocalList<int[]> sets = new LocalList<>();
        // New random generator using the split
        splitupNoSort(sets, shuffledProjectedPoints, SimpleArrayUtils.natural(size), 0, size, 0,
            rng.split(), minSplitSize);
        syncSplitSets.add(new Split(sets));
        ticker2.tick();
      });
    }

    runTasks(tasks);

    if (tracker != null) {
      time = System.currentTimeMillis() - time;
      tracker.log("Split data ... " + TextUtils.millisToString(time));
      tracker.progress(1);
    }
  }

  /**
   * Gets the number of split sets (or computes it using the size).
   *
   * @param numberOfSplits The number of splits (if below 1 it will be auto-computed using the size
   *        of the data)
   * @param size the size
   * @return the number of split sets
   */
  public static int getOrComputeNumberOfSplitSets(int numberOfSplits, int size) {
    if (size < MIN_NEIGHBOURS_SIZE) {
      return 0;
    }
    return (numberOfSplits > 0) ? numberOfSplits : (int) (LOG_O_PROJECTION_CONSTANT * log2(size));
  }

  /**
   * Gets the number of projections (or computes it using the size).
   *
   * @param numberOfProjections The number of projections (if below 1 it will be auto-computed using
   *        the size of the data)
   * @param size the size
   * @return the number of projections
   */
  public static int getOrComputeNumberOfProjections(int numberOfProjections, int size) {
    return getOrComputeNumberOfSplitSets(numberOfProjections, size);
  }

  /**
   * Compute the base 2 logarithm.
   *
   * @param x X
   * @return Logarithm base 2.
   */
  private static double log2(double x) {
    return Math.log(x) * ONE_BY_LOG2;
  }

  /**
   * Creates the unit vector supplier for random projections.
   *
   * @param localNumberOfProjections the number of projections
   * @return the supplier
   */
  private Supplier<double[]> createUnitVectorGenerator(int localNumberOfProjections) {
    if (useRandomVectors || opticsManager.is3d()) {
      return new UnitSphereSampler(opticsManager.is3d() ? 3 : 2, rand)::nextVector;
    }
    // For a 2D vector we can just uniformly distribute them around a semi-circle
    final double increment = Math.PI / localNumberOfProjections;
    final int[] multiple = {0};
    return () -> {
      final double[] randomVector = new double[2];
      final double angle = multiple[0]++ * increment;
      randomVector[0] = Math.sin(angle);
      randomVector[1] = Math.cos(angle);
      return randomVector;
    };
  }

  /**
   * Creates the projector to project points to the vector and compute the distance along the vector
   * from the origin. This is performed using a scalar projection with the dot product of the
   * coordinates and the unit vector.
   *
   * @return the projector
   */
  private Function<double[], float[]> createProjector() {
    if (opticsManager.is3d()) {
      return randomVector -> {
        final float[] projection = new float[size];
        for (int it = size; it-- > 0;) {
          final Molecule m = setOfObjects[it];
          // Dot product:
          projection[it] =
              (float) (randomVector[0] * m.x + randomVector[1] * m.y + randomVector[2] * m.getZ());
        }
        return projection;
      };
    }
    return randomVector -> {
      final float[] projection = new float[size];
      for (int it = size; it-- > 0;) {
        final Molecule m = setOfObjects[it];
        // Dot product:
        projection[it] = (float) (randomVector[0] * m.x + randomVector[1] * m.y);
      }
      return projection;
    };
  }

  /**
   * Run tasks. Uses the executor service if available or just runs on the current thread.
   *
   * @param tasks the tasks
   */
  private void runTasks(LocalList<Runnable> tasks) {
    if (executorService == null) {
      for (final Runnable task : tasks) {
        task.run();
      }
    } else {
      final List<Future<?>> futures = new LocalList<>();
      for (final Runnable task : tasks) {
        futures.add(executorService.submit(task));
      }
      ConcurrencyUtils.waitForCompletionUnchecked(futures, ProjectedMoleculeSpace::logException);
    }
  }

  /**
   * Log an exception.
   *
   * @param ex the exception
   */
  private static void logException(Exception ex) {
    Logger.getLogger(ProjectedMoleculeSpace.class.getName()).log(Level.WARNING,
        () -> "Failed to perform computation: " + ex.getMessage());
  }

  /**
   * Recursively splits entire point set until the set is below a threshold.
   *
   * @param splitSets the split sets
   * @param projectedPoints the projected points
   * @param ind points that are in the current set
   * @param begin Interval begin in the ind array
   * @param end Interval end in the ind array
   * @param depth depth of projection (how many times point set has been split already)
   * @param rand Random generator
   * @param minSplitSize minimum size for which a point set is further partitioned (roughly
   *        corresponds to minPts in OPTICS)
   */
  private void splitupNoSort(LocalList<int[]> splitSets, float[][] projectedPoints, int[] ind,
      int begin, int end, int depth, UniformRandomProvider rand, int minSplitSize) {
    final int nele = end - begin;

    if (nele < MIN_NEIGHBOURS_SIZE) {
      // Nothing to split. Also ensures we only add to the sets if neighbours can be sampled.
      return;
    }

    // choose a projection of points
    final int dim = depth % projectedPoints.length;
    final float[] tpro = projectedPoints[dim];

    // save set such that used for density or neighbourhood computation
    // sets should be roughly minSplitSize
    // -=-=-
    // Note: This is the method used in ELKI which uses the distance to the median of the set
    // (thus no distances are computed that are between points very far apart, e.g. each end
    // of the set).
    if (saveApproximateSets && nele > minSplitSize * (1 - SIZE_TOLERANCE)
        && nele < minSplitSize * (1 + SIZE_TOLERANCE)) {
      saveSet(splitSets, ind, begin, end, rand, tpro);
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
      // If it wasn't saved as an approximate set then make sure it is saved as it is less than
      // minSplitSize
      saveSet(splitSets, ind, begin, end, rand, tpro);
    }
  }

  private void saveSet(LocalList<int[]> splitSets, int[] ind, int begin, int end,
      UniformRandomProvider rand, float[] tpro) {
    final int[] indices = Arrays.copyOfRange(ind, begin, end);
    if (sampleMode == SampleMode.RANDOM) {
      // Ensure the indices are random
      RandomUtils.shuffle(indices, rand);
    } else if (sampleMode == SampleMode.MEDIAN) {
      // Sort the set, since we need the median element later
      // (when computing distance to the middle of the set).
      SortUtils.sortIndices(indices, tpro, false);
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
      UniformRandomProvider rand) {
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
        SimpleArrayUtils.swap(ind, minInd, maxInd);
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
      UniformRandomProvider rand) {
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
          SimpleArrayUtils.swap(ind, minInd, maxInd);
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
   * @throws ConcurrentRuntimeException If interrupted while computing
   */
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
    final LocalList<Future<?>> futures =
        (executorService != null && n > 1 && !saveApproximateSets) ? new LocalList<>() : null;

    final Ticker ticker = Ticker.createStarted(tracker, n, futures != null);
    for (int i = 0; i < n; i++) {
      final Split split = splitSets.unsafeGet(i);
      if (futures == null) {
        sampleNeighbours(sumDistances, countDistances, neighbours, split.sets, 0,
            split.sets.size());
      } else {
        // If the indices are unique within each split set then we can multi-thread the
        // sampling of neighbours (since each index in the cumulative arrays will only
        // be accessed concurrently by a single splitting task).
        // Use the number of threads from OPTICS manager to get an idea of the task size.
        final int taskSize =
            (int) Math.ceil((double) split.sets.size() / opticsManager.getNumberOfThreads());
        for (int j = 0; j < split.sets.size(); j += taskSize) {
          final int from = j;
          final int to = Math.min(from + taskSize, split.sets.size());
          futures.add(executorService.submit(() -> sampleNeighbours(sumDistances, countDistances,
              neighbours, split.sets, from, to)));
        }
        ConcurrencyUtils.waitForCompletionUnchecked(futures, ProjectedMoleculeSpace::logException);
        futures.clear();
      }
      ticker.tick();
    }
    ticker.stop();

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
      TIntHashSet[] neighbours, LocalList<int[]> sets, int from, int to) {
    switch (sampleMode) {
      case RANDOM:
        for (int i = from; i < to; i++) {
          sampleNeighboursRandom(sumDistances, countDistances, neighbours, sets.unsafeGet(i));
        }
        break;
      case MEDIAN:
        for (int i = from; i < to; i++) {
          sampleNeighboursUsingMedian(sumDistances, countDistances, neighbours, sets.unsafeGet(i));
        }
        break;
      case ALL:
        for (int i = from; i < to; i++) {
          sampleNeighboursAll(sumDistances, countDistances, neighbours, sets.unsafeGet(i));
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
    final ToDoubleBiFunction<Molecule, Molecule> distanceFunction = opticsManager.distanceFunction;
    for (int j = len; j-- > 0;) {
      final int it = indices[j];
      if (it == v) {
        continue;
      }
      final double dist = Math.sqrt(distanceFunction.applyAsDouble(midpoint, setOfObjects[it]));
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
    final ToDoubleBiFunction<Molecule, Molecule> distanceFunction = opticsManager.distanceFunction;
    if (indices.length == MIN_NEIGHBOURS_SIZE) {
      distanceComputations.incrementAndGet();

      // Only one set of neighbours
      final int a = indices[0];
      final int b = indices[1];

      final double dist =
          Math.sqrt(distanceFunction.applyAsDouble(setOfObjects[a], setOfObjects[b]));

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

        final double dist =
            Math.sqrt(distanceFunction.applyAsDouble(setOfObjects[a], setOfObjects[b]));

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

    final ToDoubleBiFunction<Molecule, Molecule> distanceFunction = opticsManager.distanceFunction;
    for (int i = 0; i < n1; i++) {
      final int a = indices[i];
      countDistances[a] += n1;
      double sum = 0;
      final Molecule ma = setOfObjects[a];
      final TIntHashSet na = neighbours[a];

      for (int j = i + 1; j < n; j++) {
        final int b = indices[j];

        final double dist = Math.sqrt(distanceFunction.applyAsDouble(ma, setOfObjects[b]));

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
    final ToDoubleBiFunction<Molecule, Molecule> distanceFunction = opticsManager.distanceFunction;
    for (int i = list.length; i-- > 0;) {
      final Molecule otherObject = setOfObjects[list[i]];
      otherObject.setD((float) distanceFunction.applyAsDouble(object, otherObject));
      neighbours.add(otherObject);
    }
  }

  /**
   * Sets the tracker.
   *
   * @param tracker the new tracker
   */
  void setTracker(TrackProgress tracker) {
    this.tracker = tracker;
  }

  /**
   * Sets the number of splits to compute (if below 1 it will be auto-computed using the size of the
   * data).
   *
   * @param numberOfSplits the new number of splits
   */
  void setNumberOfSplits(int numberOfSplits) {
    this.numberOfSplits = numberOfSplits;
  }

  /**
   * Sets the number of projections to compute (if below 1 it will be auto-computed using the size
   * of the data).
   *
   * @param numberOfProjections the new number of projections
   */
  void setNumberOfProjections(int numberOfProjections) {
    this.numberOfProjections = numberOfProjections;
  }

  /**
   * Sets the save approximate sets flag. Set to true to save all sets that are approximately
   * minimum split size. The default is to only save sets smaller than minimum split size.
   *
   * @param saveApproximateSets the new save approximate sets
   */
  void setSaveApproximateSets(boolean saveApproximateSets) {
    this.saveApproximateSets = saveApproximateSets;
  }

  /**
   * Sets the sample mode.
   *
   * @param sampleMode the new sample mode
   */
  void setSampleMode(SampleMode sampleMode) {
    this.sampleMode = ValidationUtils.defaultIfNull(sampleMode, SampleMode.RANDOM);
  }

  /**
   * Sets the use random vectors flag. Set to true to use random vectors for the projections. The
   * default is to uniformly create vectors on the semi-circle interval.
   *
   * <p>Random vectors are always used for 3D data.
   *
   * @param useRandomVectors the new use random vectors
   */
  void setUseRandomVectors(boolean useRandomVectors) {
    this.useRandomVectors = useRandomVectors;
  }

  /**
   * Sets the executorService service.
   *
   * @param executorService the new executorService service
   */
  void setExecutorService(ExecutorService executorService) {
    this.executorService = executorService;
  }

  /**
   * Gets the all neighbours.
   *
   * @return the all neighbours
   */
  @VisibleForTesting
  int[][] getAllNeighbours() {
    return SimpleArrayUtils.deepCopy(allNeighbours);
  }
}
