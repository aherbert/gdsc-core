/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2023 Alex Herbert
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

import com.google.common.util.concurrent.AtomicDoubleArray;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
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
import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.core.utils.SortUtils;
import uk.ac.sussex.gdsc.core.utils.TextUtils;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;
import uk.ac.sussex.gdsc.core.utils.concurrent.ConcurrencyUtils;
import uk.ac.sussex.gdsc.core.utils.rng.RandomUtils;
import uk.ac.sussex.gdsc.core.utils.rng.SplittableUniformRandomProvider;
import uk.ac.sussex.gdsc.core.utils.rng.UniformRandomProviders;

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

  //////////////////////////////////////////////////////////////////////////////
  // Implementation notes
  //
  // This implementation is different from the original FOPTICS (Fast Optics) paper.
  //
  // FOPTICS partitions the data points using random projections from ND space onto a
  // 1D line. A random point on the line is used as a cut and the two parts are
  // partitioned again using a new projection. This repeats until the sets are less
  // than a minimum splitting size. When a set is below the min split size FOPTICS
  // specifies that neighbours of each point are picked by choosing a point up to
  // dPts away from the point on the line. dPts is the input to the algorithm. This
  // must occur when the set size (|S|) is > 2 * dPts. Thus min split size must be
  // at least 2 * dPts, although the paper does not provide a recommended size, or
  // minimum number of splits before forming a final set. FOPTICS collects all the
  // neighbours of each point in a combined set. The average distance to the
  // neighbours (Davg) is an estimate of the inverse density around the point.
  // This may use all the points in the set or it may rank the distances and use
  // a fraction of the distance as specified by an f-value. E.g f=1.0 for all points
  // in the set, f=0.5 for the closest 50%, etc.
  //
  // The paper states that two points may only be merged during clustering if the
  // minimum of the Davg for each point is above the distance between them. This
  // appears to apply only to clustering and not to OPTICS which does not cluster
  // points but produces a reachability profile. For the OPTICS algorithm the
  // core distance of a point is set as Davg and the neighbours are those identified
  // in random selections from all the split sets.
  //
  // Differences:
  //
  // - Splitting occurs using a minimum points parameter. The data is split until
  // it is <= minPoints. An alternative option splits the data until is within
  // a tolerance of the minimum points (+/- 67%).
  //
  // - Selection of neighbours has a few options. There is no dPts parameter.
  // a) The ELKI version orders the points by a projection, picks the median and
  // adds all other points as the neighbours of the median. The median is added as a
  // neighbour to the other points.
  // b) All points in the set are randomly ordered. The 2 adjacent points are
  // added as neighbours to each point using a circular loop.
  // c) All points in the set are added as neighbours to all other points.
  //
  // - Computation of Davg is different. Instead of computing the distance to
  // distinct neighbours, the distances are computed to neighbours including repeats.
  // The effect is that close points which will have a high recurrence as neighbours
  // are all counted; low probability far away neighbours have a reduced effect on
  // the average. This effectively reduces the average distance by oversampling
  // closer points.
  //
  // Thus is this version the split sets are smaller than those of FOPTICS, the sets
  // are used differently to compute the neighbours, and distances are computed to
  // duplicates of neighbours. There is no f-value (fraction of neighbours) used to
  // compute Davg. This would require collation of the sets first, computation of
  // distances to distinct neighbours and then the fraction average of the sorted
  // distances.
  //
  // I have tried variations of the ELKI Fast OPTICS used here on 2D and 3D data:
  //
  // - Computation of Davg using distinct neighbours. With an f-value between 0.1
  // and 1.0 the correlation between the Davg and this method that counts duplicates
  // has an r^2 value of 0.6 - 0.9, and the slope of the correlation varies from 0.9
  // to 2.5. Thus the Davg is related and does provide an approximation of the
  // inverse density of a point.
  //
  // - Setting the core distance not to Davg but to the more rigid core distance
  // specified in OPTICS as the distance to the N-th closest point with
  // N = min points. In this case the core distance for high density points is much
  // lower. This results in a reachability profile with low points closer to that
  // output by the standard OPTICS algorithm. With the Davg method used here the
  // core distances for dense points can be much higher. This can lead to clusters
  // being less obvious on the reachability profile.
  //
  // - Only allowing the neighbours of a point to include points that are within
  // the merge distance: D(A, B) < min(Davg(A), Davg(B)). Note that when this option
  // is used if the core distance is Davg then the reachability distance of all
  // neighbours is the same and the OPTICS order does not distinguish neighbours.
  // Thus this option should be used when a different
  // method is used to set the core distance (e.g. distance to N-th closest point)
  // to allow the core distance to be less than the distance to some neighbours
  // which can then be distinguished be reachability distance.
  //
  // In summary there does not appear to be a variant combination of options from
  // the ELKI implementation or the FOPTICS paper that improves the OPTICS
  // reachability profile. This may be due to the fact that this is low
  // dimensionality data. Thus projections can result in point sets of points that
  // are far apart as this requires only that a projection vector is orthogonal to
  // the Euclidean line between two points. This has lower probability as the
  // number of dimensions increases with the same fixed number of points.
  // Given that it is simple to use KNN index structures to implement OPTICS in low
  // dimensions for efficient neighbour search the advantages of FOPTICS are less
  // obvious, but the disadvantages of identifying far away neighbours are evident
  // in the output OPTICS order, spanning tree and the hierarchical clustering. In
  // particular it is possible for a noise point to have neighbours in two distinct
  // clusters and then join those clusters in the spanning tree. Use of a fixed
  // distance threshold (in OPTICS) for neighbours prevents long linkages occurring.
  //
  // Thus this implementation is left to closely align with the ELKI variant and can
  // be used to explore data when a specific neighbour search distance is not known.
  // When a neighbour search distance can be set based on knowledge of the input
  // data then using the standard OPTICS algorithm produces better results.
  //////////////////////////////////////////////////////////////////////////////

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
  private SampleMode sampleMode = SampleMode.RANDOM;

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

    // perform O(log N+log dim) splits of the entire point sets projections
    // numberOfSplitSets = (int) (logOProjectionConst * log2(size * dim + 1))
    // perform O(log N+log dim) projections of the point set onto a random line
    // localNumberOfProjections = (int) (logOProjectionConst * log2(size * dim + 1))
    final int numberOfSplitSets = getOrComputeNumberOfSplitSets(numberOfSplits, size);
    final int localNumberOfProjections = getOrComputeNumberOfProjections(numberOfProjections, size);

    // Since size >= 2 numberOfSplitSets & localNumberOfProjections are >= 1

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

    // The splits do not have to be that random and the sets will be randomly sized between 1
    // and minSplitSize. Use a special generator that can be used to create non-overlapping
    // sequences.
    final SplittableUniformRandomProvider rng =
        UniformRandomProviders.createSplittable(rand.nextLong());

    final List<Split> syncSplitSets = Collections.synchronizedList(splitSets);
    final Ticker ticker2 = Ticker.createStarted(tracker, numberOfSplitSets, true);
    for (int i = 0; i < numberOfSplitSets; i++) {
      // shuffle projections
      final float[][] shuffledProjectedPoints = projectedPoints.clone();
      RandomUtils.shuffle(projectedPoints, rand);

      final UniformRandomProvider rng2 = rng.split();
      tasks.add(() -> {
        final LocalList<int[]> sets = new LocalList<>();
        // New random generator using the split
        splitupNoSort(sets, shuffledProjectedPoints, SimpleArrayUtils.natural(size), 0, size, 0,
            rng2, minSplitSize);
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
    return (numberOfSplits > 0) ? numberOfSplits
        : (int) (LOG_O_PROJECTION_CONSTANT * MathUtils.log2(size));
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
   * Creates the unit vector supplier for random projections.
   *
   * @param localNumberOfProjections the number of projections
   * @return the supplier
   */
  private Supplier<double[]> createUnitVectorGenerator(int localNumberOfProjections) {
    if (useRandomVectors || opticsManager.is3d()) {
      return UnitSphereSampler.of(rand, opticsManager.is3d() ? 3 : 2)::sample;
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
      // No further splits
      return;
    }

    // compute splitting element
    // do not store set or even sort set, since it is too large
    if (nele > minSplitSize) {
      // splits can be performed either by distance (between min,maxCoord) or by
      // picking a point randomly(picking index of point)
      // outcome is similar

      // final int minInd = splitByDistance(ind, begin, end, tpro, rand);
      final int minInd = splitRandomly(ind, begin, end, tpro, rand);

      // split set recursively
      // position used for splitting the projected points into two
      // sets used for recursive splitting
      final int splitpos = minInd + 1;
      splitupNoSort(splitSets, projectedPoints, ind, begin, splitpos, depth + 1, rand,
          minSplitSize);
      splitupNoSort(splitSets, projectedPoints, ind, splitpos, end, depth + 1, rand, minSplitSize);
    } else {
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
   * <p>All distances after the split position should be larger.
   *
   * @param ind Object index
   * @param begin Interval begin
   * @param end Interval end
   * @param tpro Projection
   * @param rand Random generator
   * @return Splitting point
   */
  static int splitRandomly(int[] ind, int begin, int end, float[] tpro,
      UniformRandomProvider rand) {
    final int nele = end - begin;

    // pick random splitting element based on position
    final float rs = tpro[ind[begin + rand.nextInt(nele)]];
    int minInd = begin;
    int maxInd = end - 1;
    // permute elements such that all points smaller or equal than the splitting
    // element are on the left (lower part) and the others on the right (upper part) in the array.
    // Keep a flag to show that the data can be split.
    boolean noSplit = true;
    while (minInd < maxInd) {
      final float currEle = tpro[ind[minInd]];
      if (currEle > rs) {
        noSplit = false;
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
    if (noSplit) {
      // Either:
      // 1. All elements are the same -> split in the middle
      // 2. The splitting element is the maximum. In this case there is no
      // split. Recursive calls should split the data next time.
      // If there is no data range then we can split in the middle.
      if (tpro[ind[begin]] == rs) {
        return (begin + end - 1) >>> 1;
      }
    } else {
      // Ensure the algorithm did not stop with the index above the split distance
      while (tpro[ind[minInd]] > rs) {
        minInd--;
      }
    }
    return minInd;
  }

  /**
   * Split the data set by distances.
   *
   * <p>All distances after the split position should be larger.
   *
   * @param ind Object index
   * @param begin Interval begin
   * @param end Interval end
   * @param tpro Projection
   * @param rand Random generator
   * @return Splitting point
   */
  static int splitByDistance(int[] ind, int begin, int end, float[] tpro,
      UniformRandomProvider rand) {
    // find min and max distance
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

    // pick random splitting point based on distance
    final float rs = rmin + rand.nextFloat() * (rmax - rmin);
    // Ensure the split is random between 25-75% of the data
    // final float rs = rmin + (0.25f + rand.nextFloat() / 2) * (rmax - rmin);
    if (rs != rmax) {
      // A split is possible
      int minInd = begin;
      int maxInd = end - 1;

      // permute elements such that all points smaller or equal than the splitting
      // element are on the left (lower part) and the others on the right (upper part) in the array.
      // Keep a flag to show that the data can be split.
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
      // Ensure the algorithm did not stop with the index above the split distance
      while (tpro[ind[minInd]] > rs) {
        minInd--;
      }
      return minInd;
    }
    // Either:
    // 1. All elements are the same -> split in the middle
    // 2. The splitting element is the maximum. In this case there is no
    // split. Recursive calls should split the data next time.
    // If there is no data range then we can split in the middle.
    if (rmin == rmax) {
      return (begin + end - 1) >>> 1;
    }
    return end - 1;
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

    // These must be thread-safe
    final AtomicDoubleArray sumDistances = new AtomicDoubleArray(size);
    final AtomicIntegerArray countDistances = new AtomicIntegerArray(size);
    @SuppressWarnings("unchecked")
    final Set<Integer>[] neighbours = new Set[size];
    final Integer[] keys = new Integer[size];
    for (int it = size; it-- > 0;) {
      neighbours[it] = ConcurrentHashMap.newKeySet();
      keys[it] = Integer.valueOf(it);
    }

    // Multi-thread the hash set operations for speed.
    final LocalList<Future<?>> futures =
        (executorService != null && n > 1) ? new LocalList<>() : null;

    final Ticker ticker = Ticker.createStarted(tracker, n, futures != null);
    final int numberOfProcessors = Runtime.getRuntime().availableProcessors();
    for (int i = 0; i < n; i++) {
      final Split split = splitSets.unsafeGet(i);
      if (futures == null) {
        sampleNeighbours(keys, sumDistances, countDistances, neighbours, split.sets, 0,
            split.sets.size());
      } else {
        final int taskSize = (int) Math.ceil((double) split.sets.size() / numberOfProcessors);
        for (int j = 0; j < split.sets.size(); j += taskSize) {
          final int from = j;
          // Overflow safe range limit
          final int to =
              from + taskSize - split.sets.size() > 0 ? split.sets.size() : from + taskSize;
          futures.add(executorService.submit(() -> sampleNeighbours(keys, sumDistances,
              countDistances, neighbours, split.sets, from, to)));
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
      setOfObjects[it].coreDistance = getCoreDistance(sumDistances.get(it), countDistances.get(it));

      allNeighbours[it] = neighbours[it].stream().mapToInt(Integer::intValue).toArray();
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
   * @param keys the keys array using a natural sequence of integer objects (used to avoid auto
   *        boxing of integers)
   * @param sumDistances the neighbour sum of distances
   * @param countDistances the neighbour count of distances
   * @param neighbours the neighbour hash sets
   * @param sets the split sets
   * @param from the from index
   * @param to the to index
   */
  @VisibleForTesting
  void sampleNeighbours(Integer[] keys, AtomicDoubleArray sumDistances,
      AtomicIntegerArray countDistances, Set<Integer>[] neighbours, LocalList<int[]> sets, int from,
      int to) {
    switch (sampleMode) {
      case RANDOM:
        for (int i = from; i < to; i++) {
          sampleNeighboursRandom(keys, sumDistances, countDistances, neighbours, sets.unsafeGet(i));
        }
        break;
      case MEDIAN:
        for (int i = from; i < to; i++) {
          sampleNeighboursUsingMedian(keys, sumDistances, countDistances, neighbours,
              sets.unsafeGet(i));
        }
        break;
      case ALL:
        for (int i = from; i < to; i++) {
          sampleNeighboursAll(keys, sumDistances, countDistances, neighbours, sets.unsafeGet(i));
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
   * @param keys the keys array using a natural sequence of integer objects (used to avoid auto
   *        boxing of integers)
   * @param sumDistances the neighbour sum of distances
   * @param countDistances the neighbour count of distances
   * @param neighbours the neighbour hash sets
   * @param indices the indices of objects in the set
   */
  private void sampleNeighboursUsingMedian(Integer[] keys, AtomicDoubleArray sumDistances,
      AtomicIntegerArray countDistances, Set<Integer>[] neighbours, int[] indices) {
    final int len = indices.length;
    final int indoff = len >> 1;
    final int v = indices[indoff];
    final int delta = len - 1;
    distanceComputations.addAndGet(delta);
    countDistances.getAndAdd(v, delta);
    final Molecule midpoint = setOfObjects[v];
    final ToDoubleBiFunction<Molecule, Molecule> distanceFunction = opticsManager.distanceFunction;
    for (int j = len; j-- > 0;) {
      final int it = indices[j];
      if (it == v) {
        continue;
      }
      final double dist = Math.sqrt(distanceFunction.applyAsDouble(midpoint, setOfObjects[it]));
      sumDistances.addAndGet(v, dist);
      sumDistances.addAndGet(it, dist);
      countDistances.getAndIncrement(it);

      neighbours[it].add(keys[v]);
      neighbours[v].add(keys[it]);
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
   * @param keys the keys array using a natural sequence of integer objects (used to avoid auto
   *        boxing of integers)
   * @param sumDistances the neighbour sum of distances
   * @param countDistances the neighbour count of distances
   * @param neighbours the neighbour hash sets
   * @param indices the indices of objects in the set
   */
  private void sampleNeighboursRandom(Integer[] keys, AtomicDoubleArray sumDistances,
      AtomicIntegerArray countDistances, Set<Integer>[] neighbours, int[] indices) {
    final ToDoubleBiFunction<Molecule, Molecule> distanceFunction = opticsManager.distanceFunction;
    if (indices.length == MIN_NEIGHBOURS_SIZE) {
      distanceComputations.incrementAndGet();

      // Only one set of neighbours
      final int a = indices[0];
      final int b = indices[1];

      final double dist =
          Math.sqrt(distanceFunction.applyAsDouble(setOfObjects[a], setOfObjects[b]));

      sumDistances.addAndGet(a, dist);
      sumDistances.addAndGet(b, dist);
      countDistances.getAndIncrement(a);
      countDistances.getAndIncrement(b);

      neighbours[a].add(keys[b]);
      neighbours[b].add(keys[a]);
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

        sumDistances.addAndGet(a, dist);
        sumDistances.addAndGet(b, dist);
        // Each object will have 2 due to circular loop.
        countDistances.addAndGet(a, 2);

        neighbours[a].add(keys[b]);
        neighbours[b].add(keys[a]);
      }
    }
  }

  /**
   * Sample neighbours all-vs-all.
   *
   * @param keys the keys array using a natural sequence of integer objects (used to avoid auto
   *        boxing of integers)
   * @param sumDistances the neighbour sum of distances
   * @param countDistances the neighbour count of distances
   * @param neighbours the neighbour hash sets
   * @param indices the indices of objects in the set
   */
  private void sampleNeighboursAll(Integer[] keys, AtomicDoubleArray sumDistances,
      AtomicIntegerArray countDistances, Set<Integer>[] neighbours, int[] indices) {
    final int n = indices.length;
    final int n1 = n - 1;

    // for all-vs-all = n(n-1)/2
    distanceComputations.addAndGet((n * n1) >>> 1);

    final ToDoubleBiFunction<Molecule, Molecule> distanceFunction = opticsManager.distanceFunction;
    for (int i = 0; i < n1; i++) {
      final int a = indices[i];
      double sum = 0;
      final Molecule ma = setOfObjects[a];
      final Set<Integer> na = neighbours[a];

      for (int j = i + 1; j < n; j++) {
        final int b = indices[j];

        final double dist = Math.sqrt(distanceFunction.applyAsDouble(ma, setOfObjects[b]));

        sum += dist;
        sumDistances.addAndGet(b, dist);

        na.add(keys[b]);
        neighbours[b].add(keys[a]);
      }

      sumDistances.addAndGet(a, sum);
      countDistances.addAndGet(a, n1);
    }

    // For the last index that was skipped in the outer loop.
    countDistances.addAndGet(indices[n1], n1);
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
