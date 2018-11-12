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

package uk.ac.sussex.gdsc.core.clustering;

import uk.ac.sussex.gdsc.core.data.AsynchronousException;
import uk.ac.sussex.gdsc.core.data.ComputationException;
import uk.ac.sussex.gdsc.core.logging.NullTrackProgress;
import uk.ac.sussex.gdsc.core.logging.TrackProgress;
import uk.ac.sussex.gdsc.core.utils.ConcurrencyUtils;

import org.apache.commons.math3.util.FastMath;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Find clusters of points using a clustering algorithm.
 */
public class ClusteringEngine {
  /** The minimum block size for multi-threading. */
  private static final int MIN_BLOCK_SIZE = 3;
  /**
   * The maximum number of bins to use for the pairwise algorithm.
   *
   * <p>This has not been optimised. It is based on the assumption of handling data in pixel units
   * and the default of assigning coordinates to a 512x512 grid.
   */
  private static final int PAIRWISE_MAX_BINS = 512;

  private ExecutorService threadPool;
  private int xblock;
  private int yblock;

  private ClusteringAlgorithm clusteringAlgorithm;
  private TrackProgress tracker;
  private int pulseInterval;
  private boolean trackJoins;
  private int threadCount;
  private double[] intraIdDistances;
  private double[] interIdDistances;
  private int intraIdCount;
  private int interIdCount;

  /**
   * Flag to indicate that clustering with time will use the time range (the default is start time).
   */
  private boolean useRange;

  /**
   * Used for multi-threaded clustering to store the closest pair in a region of the search space.
   */
  private static class ClosestPair {
    double distance;
    int time;
    Object point1;
    Object point2;

    ClosestPair(double distance, Object point1, Object point2) {
      this.distance = distance;
      this.point1 = point1;
      this.point2 = point2;
    }

    ClosestPair(double distance, int time, Object point1, Object point2) {
      this.distance = distance;
      this.time = time;
      this.point1 = point1;
      this.point2 = point2;
    }

    ClosestPair() {
      // Allow empty constructor
    }
  }

  /**
   * Use a runnable to allow multi-threaded operation. Input parameters that are manipulated should
   * have synchronized methods.
   */
  private static class ParticleLinkageWorker implements Runnable {
    ClosestPair pair;
    ExtendedClusterPoint[][] grid;
    int nxbins;
    int nybins;
    double r2;
    int startxbin;
    int endxbin;
    int startybin;
    int endybin;

    ParticleLinkageWorker(ClosestPair pair, ExtendedClusterPoint[][] grid, int nxbins, int nybins,
        double r2, int startxbin, int endxbin, int startybin, int endybin) {
      this.pair = pair;
      this.grid = grid;
      this.nxbins = nxbins;
      this.nybins = nybins;
      this.r2 = r2;
      this.startxbin = startxbin;
      this.endxbin = endxbin;
      this.startybin = startybin;
      this.endybin = endybin;
    }

    @Override
    public void run() {
      final ClosestPair result =
          findClosestParticle(grid, nxbins, nybins, r2, startxbin, endxbin, startybin, endybin);
      if (result != null) {
        pair.distance = result.distance;
        pair.point1 = result.point1;
        pair.point2 = result.point2;
      }
    }
  }

  /**
   * Use a runnable to allow multi-threaded operation. Input parameters that are manipulated should
   * have synchronized methods.
   */
  private static class ClosestWorker implements Runnable {
    ClosestPair pair;
    Cluster[][] grid;
    int nxbins;
    int nybins;
    double r2;
    int startxbin;
    int endxbin;
    int startybin;
    int endybin;
    boolean single;

    ClosestWorker(ClosestPair pair, Cluster[][] grid, int nxbins, int nybins, double r2,
        int startxbin, int endxbin, int startybin, int endybin, boolean single) {
      this.pair = pair;
      this.grid = grid;
      this.nxbins = nxbins;
      this.nybins = nybins;
      this.r2 = r2;
      this.startxbin = startxbin;
      this.endxbin = endxbin;
      this.startybin = startybin;
      this.endybin = endybin;
      this.single = single;
    }

    @Override
    public void run() {
      ClosestPair result;
      if (single) {
        result =
            findClosestSingle(grid, nxbins, nybins, r2, startxbin, endxbin, startybin, endybin);
      } else {
        result = findClosest(grid, nxbins, nybins, r2, startxbin, endxbin, startybin, endybin);
      }

      if (result != null) {
        pair.distance = result.distance;
        pair.point1 = result.point1;
        pair.point2 = result.point2;
      }
    }
  }

  /**
   * Use a runnable to allow multi-threaded operation. Input parameters that are manipulated should
   * have synchronized methods.
   */
  private class ClosestPriorityWorker implements Runnable {
    boolean timePriority;
    ClosestPair pair;
    TimeCluster[][] grid;
    int nxbins;
    int nybins;
    double r2;
    int time;
    int startxbin;
    int endxbin;
    int startybin;
    int endybin;
    boolean single;

    ClosestPriorityWorker(boolean timePriority, ClosestPair pair, TimeCluster[][] grid, int nxbins,
        int nybins, double r2, int time, int startxbin, int endxbin, int startybin, int endybin,
        boolean single) {
      this.timePriority = timePriority;
      this.pair = pair;
      this.grid = grid;
      this.nxbins = nxbins;
      this.nybins = nybins;
      this.r2 = r2;
      this.time = time;
      this.startxbin = startxbin;
      this.endxbin = endxbin;
      this.startybin = startybin;
      this.endybin = endybin;
      this.single = single;
    }

    @Override
    public void run() {
      ClosestPair result = null;
      if (timePriority) {
        result = (single)
            ? findClosestParticleTimePriority(grid, nxbins, nybins, r2, time, startxbin, endxbin,
                startybin, endybin)
            : findClosestTimePriority(grid, nxbins, nybins, r2, time, startxbin, endxbin, startybin,
                endybin);
      } else {
        result = (single)
            ? findClosestParticleDistancePriority(grid, nxbins, nybins, r2, time, startxbin,
                endxbin, startybin, endybin)
            : findClosestDistancePriority(grid, nxbins, nybins, r2, time, startxbin, endxbin,
                startybin, endybin);
      }

      if (result != null) {
        pair.distance = result.distance;
        pair.time = result.time;
        pair.point1 = result.point1;
        pair.point2 = result.point2;
      }
    }
  }

  /**
   * Use a runnable to allow multi-threaded operation. Input parameters that are manipulated should
   * have synchronized methods.
   */
  private static class FindLinksWorker implements Runnable {
    boolean links;
    Cluster[][] grid;
    int nxbins;
    int nybins;
    double r2;
    int startxbin;
    int endxbin;
    int startybin;
    int endybin;

    FindLinksWorker(Cluster[][] grid, int nxbins, int nybins, double r2, int startxbin, int endxbin,
        int startybin, int endybin) {
      this.grid = grid;
      this.nxbins = nxbins;
      this.nybins = nybins;
      this.r2 = r2;
      this.startxbin = startxbin;
      this.endxbin = endxbin;
      this.startybin = startybin;
      this.endybin = endybin;
    }

    @Override
    public void run() {
      links = findLinksAndCountNeighbours(grid, nxbins, nybins, r2, startxbin, endxbin, startybin,
          endybin, FindLinksWorker::incrementNeighbour, FindLinksWorker::link);
    }

    static void incrementNeighbour(Cluster c1) {
      synchronized (c1) {
        c1.incrementNeighbour();
      }
    }

    static void link(Cluster c1, Cluster c2, double d2) {
      if (c1.canLink(c2, d2)) {
        synchronized (c1) {
          synchronized (c2) {
            c1.link(c2, d2);
          }
        }
      }
    }
  }

  /**
   * A simple counter.
   */
  private class Counter {
    int count;

    /**
     * Increment and get the new count.
     *
     * @return the count
     */
    int incrementAndGet() {
      count++;
      return count;
    }

    /**
     * Gets the count.
     *
     * @return the count
     */
    int get() {
      return count;
    }
  }

  /**
   * Instantiates a new clustering engine.
   */
  public ClusteringEngine() {
    this(1);
  }

  /**
   * Instantiates a new clustering engine.
   *
   * @param threadCount the thread count
   */
  public ClusteringEngine(int threadCount) {
    this(threadCount, ClusteringAlgorithm.PAIRWISE, null);
  }

  /**
   * Instantiates a new clustering engine.
   *
   * @param threadCount the thread count
   * @param clusteringAlgorithm the clustering algorithm
   */
  public ClusteringEngine(int threadCount, ClusteringAlgorithm clusteringAlgorithm) {
    this(threadCount, clusteringAlgorithm, null);
  }

  /**
   * Instantiates a new clustering engine.
   *
   * @param threadCount the thread count
   * @param custeringAlgorithm the custering algorithm
   * @param tracker the tracker
   */
  public ClusteringEngine(int threadCount, ClusteringAlgorithm custeringAlgorithm,
      TrackProgress tracker) {
    this.threadCount = threadCount;
    this.clusteringAlgorithm = custeringAlgorithm;
    this.tracker = NullTrackProgress.createIfNull(tracker);
  }

  /**
   * Find the clusters of points within the specified radius.
   *
   * @param points the points
   * @param radius the radius
   * @return the clusters
   */
  public List<Cluster> findClusters(List<ClusterPoint> points, double radius) {
    return findClusters(points, radius, 0);
  }

  /**
   * Find the clusters of points within the specified radius and time separation.
   *
   * @param points the points
   * @param radius the radius
   * @param time the time
   * @return the clusters
   * @throws AsynchronousException if interrupted when using a multi-threaded algorithm
   */
  public List<Cluster> findClusters(List<ClusterPoint> points, double radius, int time) {
    if (clusteringAlgorithm == ClusteringAlgorithm.PARTICLE_SINGLE_LINKAGE) {
      return runParticleSingleLinkage(points, radius);
    }

    // Get the density around each point. Points with no density cannot be clustered.
    // Ensure that we only ignore points that could never be within radius of the centroid
    // of any other pair:
    //
    // Set point C on the circle drawn with radius R around both points A and B. Distance A-B > R.
    // When the angle ACB is <90 then the line C-B intersects A's circle, i.e. joining C to B can
    // create a new centroid within the search radius of A. The same is true if joining C to A, the
    // new centroid could be closer to A than distance R. If ACB = 90 then the
    // line C-B cannot intersect A's circle. Distance AB = sqrt(2R^2) = sqrt(2) * R
    //@formatter:off
    //
    //        -------   ------
    //       /       \ /       \
    //      /         C         \
    //     /         / \         \
    //     |     A   | |   B     |
    //     \         \ /         /
    //      \         X         /
    //       \       / \       /
    //        -------   -------
    //
    //@formatter:on

    final int[] density = calculateDensity(points, 1.4142 * radius);

    // Extract initial cluster points using molecules with a density above 1
    // (All other points cannot be clustered at this radius)
    final List<Cluster> candidates = new ArrayList<>(density.length);
    final List<Cluster> singles = new ArrayList<>(density.length);
    int index = 0;
    for (final ClusterPoint p : points) {
      final Cluster c = new Cluster(p);
      if (density[index] > 0) {
        candidates.add(c);
      } else {
        singles.add(c);
      }
      index++;
    }

    if (candidates.isEmpty()) {
      return singles;
    }

    // Check for time information if required
    switch (clusteringAlgorithm) {
      case CENTROID_LINKAGE_DISTANCE_PRIORITY:
      case CENTROID_LINKAGE_TIME_PRIORITY:
        if (noTimeInformation(candidates)) {
          tracker.log("No time information among candidates");
          clusteringAlgorithm = ClusteringAlgorithm.CENTROID_LINKAGE;
        }
        break;

      // All other methods do not use time information
      default:
        break;
    }

    tracker.log("Starting clustering : %d singles, %d cluster candidates", singles.size(),
        candidates.size());
    tracker.log("Algorithm = %s", clusteringAlgorithm.toString());

    // Find the bounds of the candidates
    double minx = candidates.get(0).getX();
    double miny = candidates.get(0).getY();
    double maxx = minx;
    double maxy = miny;
    for (final Cluster c : candidates) {
      if (minx > c.getX()) {
        minx = c.getX();
      } else if (maxx < c.getX()) {
        maxx = c.getX();
      }
      if (miny > c.getY()) {
        miny = c.getY();
      } else if (maxy < c.getY()) {
        maxy = c.getY();
      }
    }

    // Assign to a grid.
    // If tracking potential neighbours then the cells must be larger to cover the increased
    // distance
    final double cellSize =
        (clusteringAlgorithm == ClusteringAlgorithm.PAIRWISE) ? radius : radius * 1.4142;
    final double xbinWidth = FastMath.max(cellSize, (maxx - minx) / PAIRWISE_MAX_BINS);
    final double ybinWidth = FastMath.max(cellSize, (maxy - miny) / PAIRWISE_MAX_BINS);
    final int nxbins = 1 + (int) ((maxx - minx) / xbinWidth);
    final int nybins = 1 + (int) ((maxy - miny) / ybinWidth);
    final Cluster[][] grid = new Cluster[nxbins][nybins];
    for (final Cluster c : candidates) {
      final int xbin = (int) ((c.getX() - minx) / xbinWidth);
      final int ybin = (int) ((c.getY() - miny) / ybinWidth);
      // Build a single linked list
      c.setXBin(xbin);
      c.setYBin(ybin);
      c.setNext(grid[xbin][ybin]);
      grid[xbin][ybin] = c;
    }

    final double r2 = radius * radius;

    tracker.log("Clustering " + clusteringAlgorithm.toString() + " ...");
    try {
      final List<Cluster> clusters = runFindClusters(time, candidates, singles, minx, miny,
          xbinWidth, ybinWidth, nxbins, nybins, grid, r2);
      reportResult(clusters);
      return clusters;
    } finally {
      shutdownMultithreading();
    }
  }

  private void reportResult(final List<Cluster> clusters) {
    tracker.progress(1);
    tracker.log("Found %d clusters", (clusters == null) ? 0 : clusters.size());
  }

  private List<Cluster> runFindClusters(int time, final List<Cluster> candidates,
      final List<Cluster> singles, double minx, double miny, final double xbinWidth,
      final double ybinWidth, final int nxbins, final int nybins, final Cluster[][] grid,
      final double r2) {
    List<Cluster> clusters;
    switch (clusteringAlgorithm) {
      case PAIRWISE:
        clusters = runPairwise(grid, nxbins, nybins, r2, minx, miny, xbinWidth, ybinWidth,
            candidates, singles);
        break;

      case PAIRWISE_WITHOUT_NEIGHBOURS:
        clusters = runPairwiseWithoutNeighbours(grid, nxbins, nybins, r2, minx, miny, xbinWidth,
            ybinWidth, candidates, singles);
        break;

      case PARTICLE_CENTROID_LINKAGE_TIME_PRIORITY:
      case CENTROID_LINKAGE_TIME_PRIORITY:
        clusters = runClosestTimePriority(grid, nxbins, nybins, r2, time, minx, miny, xbinWidth,
            ybinWidth, candidates, singles,
            clusteringAlgorithm == ClusteringAlgorithm.PARTICLE_CENTROID_LINKAGE_TIME_PRIORITY);
        break;

      case PARTICLE_CENTROID_LINKAGE_DISTANCE_PRIORITY:
      case CENTROID_LINKAGE_DISTANCE_PRIORITY:
        clusters = runClosestDistancePriority(grid, nxbins, nybins, r2, time, minx, miny, xbinWidth,
            ybinWidth, candidates, singles,
            clusteringAlgorithm == ClusteringAlgorithm.PARTICLE_CENTROID_LINKAGE_DISTANCE_PRIORITY);
        break;

      case PARTICLE_CENTROID_LINKAGE:
      case CENTROID_LINKAGE:
      default:
        clusters =
            runClosest(grid, nxbins, nybins, r2, minx, miny, xbinWidth, ybinWidth, candidates,
                singles, clusteringAlgorithm == ClusteringAlgorithm.PARTICLE_CENTROID_LINKAGE);
    }
    return clusters;
  }

  /**
   * Join the closest unlinked particle to its neighbour particle/cluster.
   *
   * @param points the points
   * @param radius the radius
   * @return The clusters
   */
  private List<Cluster> runParticleSingleLinkage(List<ClusterPoint> points, double radius) {
    final int[] density = calculateDensity(points, radius);

    // Extract initial cluster points using molecules with a density above 1
    // (All other points cannot be clustered at this radius)
    final List<ExtendedClusterPoint> candidates = new ArrayList<>(density.length);
    final List<Cluster> singles = new ArrayList<>(density.length);
    int index = 0;
    int id = 0;
    for (final ClusterPoint p : points) {
      if (density[index] > 0) {
        // Store the point using the next pointer of a new point which will be used for clustering
        candidates.add(new ExtendedClusterPoint(id++, p.getX(), p.getY(), 0, p));
      } else {
        singles.add(new Cluster(p));
      }
      index++;
    }

    if (candidates.isEmpty()) {
      return singles;
    }

    tracker.log("Starting clustering : %d singles, %d cluster candidates", singles.size(),
        candidates.size());
    tracker.log("Algorithm = %s", clusteringAlgorithm.toString());

    // Find the bounds of the candidates
    double minx = candidates.get(0).getX();
    double miny = candidates.get(0).getY();
    double maxx = minx;
    double maxy = miny;
    for (final ExtendedClusterPoint c : candidates) {
      if (minx > c.getX()) {
        minx = c.getX();
      } else if (maxx < c.getX()) {
        maxx = c.getX();
      }
      if (miny > c.getY()) {
        miny = c.getY();
      } else if (maxy < c.getY()) {
        maxy = c.getY();
      }
    }

    // Assign to a grid
    final int maxbins = 500;
    final double cellSize = radius * 1.01; // Add an error margin
    final double xbinWidth = FastMath.max(cellSize, (maxx - minx) / maxbins);
    final double ybinWidth = FastMath.max(cellSize, (maxy - miny) / maxbins);
    final int nxbins = 1 + (int) ((maxx - minx) / xbinWidth);
    final int nybins = 1 + (int) ((maxy - miny) / ybinWidth);
    final ExtendedClusterPoint[][] grid = new ExtendedClusterPoint[nxbins][nybins];
    for (final ExtendedClusterPoint c : candidates) {
      final int xbin = (int) ((c.getX() - minx) / xbinWidth);
      final int ybin = (int) ((c.getY() - miny) / ybinWidth);
      // Build a single linked list
      c.setNextExtended(grid[xbin][ybin]);
      grid[xbin][ybin] = c;
    }

    final double r2 = radius * radius;

    tracker.log("Clustering " + clusteringAlgorithm.toString() + " ...");

    try {
      final List<Cluster> clusters =
          runParticleSingleLinkage(grid, nxbins, nybins, r2, candidates, singles);
      reportResult(clusters);
      return clusters;
    } finally {
      shutdownMultithreading();
    }
  }

  private List<Cluster> runParticleSingleLinkage(ExtendedClusterPoint[][] grid, int nxbins,
      int nybins, double r2, List<ExtendedClusterPoint> candidates, List<Cluster> currentClusters) {
    final int totalCandidates = candidates.size();
    int candidatesProcessed = 0;
    final Counter clusterCount = new Counter(); // Incremented within joinClosestParticle(...)

    // Used to store the cluster for each candidate
    final int[] clusterId = new int[candidates.size()];

    if (trackJoins) {
      interIdDistances = new double[candidates.size()];
      intraIdDistances = new double[candidates.size()];
      interIdCount = 0;
      intraIdCount = 0;
    }

    initialiseMultithreading(nxbins, nybins);
    int processed = joinClosestParticle(grid, nxbins, nybins, r2, clusterId, clusterCount);
    while (processed > 0) {
      if (tracker.isEnded()) {
        return null;
      }
      candidatesProcessed += processed;
      tracker.progress(candidatesProcessed, totalCandidates);
      // Repeat
      processed = joinClosestParticle(grid, nxbins, nybins, r2, clusterId, clusterCount);
    }

    if (trackJoins) {
      interIdDistances = Arrays.copyOf(interIdDistances, interIdCount);
      intraIdDistances = Arrays.copyOf(intraIdDistances, intraIdCount);
    }

    tracker.log("Processed %d / %d", candidatesProcessed, totalCandidates);
    tracker.log("%d candidates linked into %d clusters", candidates.size(), clusterCount.get());

    // Create clusters from the original cluster points using the assignments
    final Cluster[] clusters = new Cluster[clusterCount.get()];
    final int originalSize = currentClusters.size();
    for (int i = 0; i < clusterId.length; i++) {
      final ClusterPoint originalPoint = candidates.get(i).getNext();
      if (clusterId[i] == 0) {
        // tracker.log("Failed to assign a cluster to a candidate particle: " + i)
        currentClusters.add(new Cluster(originalPoint));
      } else {
        // The next points were used to store the original cluster points
        final int clusterIndex = clusterId[i] - 1;
        if (clusters[clusterIndex] == null) {
          clusters[clusterIndex] = new Cluster(originalPoint);
        } else {
          clusters[clusterIndex].add(originalPoint);
        }
      }
    }
    final int failed = currentClusters.size() - originalSize;
    tracker.log("Failed to assign %d candidates", failed);

    for (int i = 0; i < clusters.length; i++) {
      if (clusters[i] != null) {
        currentClusters.add(clusters[i]);
      }
    }
    return currentClusters;
  }

  private void initialiseMultithreading(int nxbins, int nybins) {
    // Do not use threads if the number of bins is small
    if (nxbins < MIN_BLOCK_SIZE && nybins < MIN_BLOCK_SIZE) {
      return;
    }

    if (threadCount > 1) {
      // Set up for multi-threading
      threadPool = Executors.newFixedThreadPool(threadCount);

      // Ensure a minimum block size to avoid wasting time.
      xblock = FastMath.max(nxbins / threadCount, MIN_BLOCK_SIZE);
      yblock = FastMath.max(nybins / threadCount, MIN_BLOCK_SIZE);

      // Increment the block size until the number of blocks to process is just above the thread
      // count. This reduces thread overhead but still processes across many threads.
      int counter = 0;
      while (countBlocks(nxbins, nybins) > 2 * threadCount) {
        if (counter++ % 2 == 0) {
          xblock++;
        } else {
          yblock++;
        }
      }
    }
  }

  private void shutdownMultithreading() {
    if (threadPool != null) {
      threadPool.shutdownNow();
      threadPool = null;
    }
  }

  private int countBlocks(int nxbins, int nybins) {
    int blocks = 0;
    for (int startybin = 0; startybin < nybins; startybin += yblock) {
      for (int startxbin = 0; startxbin < nxbins; startxbin += xblock) {
        blocks++;
      }
    }
    return blocks;
  }

  /**
   * Search for the closest pair of particles, one of which is not in a cluster, below the squared
   * radius distance and join them.
   *
   * @param grid the grid
   * @param nxbins the n X bins
   * @param nybins the n Y bins
   * @param r2 The squared radius distance
   * @param clusterId the cluster id
   * @param clusterCount the cluster count
   * @return The number of points assigned to clusters (either 0, 1, or 2)
   */
  private int joinClosestParticle(ExtendedClusterPoint[][] grid, final int nxbins, final int nybins,
      final double r2, int[] clusterId, Counter clusterCount) {
    ClosestPair closest = null;

    // Blocks must be overlapping by one bin to calculate all distances. There is no point
    // multi-threading
    // if there are not enough blocks since each overlap is double processed.

    if (threadPool == null) {
      closest = findClosestParticle(grid, nxbins, nybins, r2, 0, nxbins, 0, nybins);
    } else {
      // Use threads to find the closest pairs in blocks
      final List<Future<?>> futures = new LinkedList<>();
      final List<ClosestPair> results = new LinkedList<>();

      for (int startybin = 0; startybin < nybins; startybin += yblock) {
        final int endybin = FastMath.min(nybins, startybin + yblock);
        for (int startxbin = 0; startxbin < nxbins; startxbin += xblock) {
          final int endxbin = FastMath.min(nxbins, startxbin + xblock);

          final ClosestPair pair = new ClosestPair();
          results.add(pair);
          futures.add(threadPool.submit(new ParticleLinkageWorker(pair, grid, nxbins, nybins, r2,
              startxbin, endxbin, startybin, endybin)));
        }
      }

      // Finish processing data
      ConcurrencyUtils.waitForCompletionOrError(futures);
      futures.clear();

      // Find the closest pair from all the results
      for (final ClosestPair result : results) {
        if (result.point1 != null && (closest == null || result.distance < closest.distance)) {
          closest = result;
        }
      }
    }

    // Assign the closest pair.
    if (closest != null) {
      final ExtendedClusterPoint pair1 = (ExtendedClusterPoint) closest.point1;
      final ExtendedClusterPoint pair2 = (ExtendedClusterPoint) closest.point2;

      int processed = 1;

      if (pair1.isInCluster() && pair2.isInCluster()) {
        // Error
        throw new ComputationException("Linkage between two particles already in a cluster");
      } else if (pair1.isInCluster()) {
        clusterId[pair2.getId()] = clusterId[pair1.getId()];
        pair2.setInCluster(true);
      } else if (pair2.isInCluster()) {
        clusterId[pair1.getId()] = clusterId[pair2.getId()];
        pair1.setInCluster(true);
      } else {
        // Create a new cluster if necessary
        processed = 2;
        clusterId[pair1.getId()] = clusterId[pair2.getId()] = clusterCount.incrementAndGet();
        pair1.setInCluster(true);
        pair2.setInCluster(true);
      }

      if (trackJoins) {
        if (pair1.getNext().getId() == pair2.getNext().getId()) {
          intraIdDistances[intraIdCount++] = Math.sqrt(closest.distance);
        } else {
          interIdDistances[interIdCount++] = Math.sqrt(closest.distance);
        }
      }

      return processed;
    }

    // This should not happen if the density manager counted neighbours correctly
    // (i.e. all points should have at least one neighbour)
    return 0;
  }

  /**
   * Search for the closest pair of particles, one of which is not in a cluster, below the squared
   * radius distance.
   *
   * @param grid the grid
   * @param nxbins the n X bins
   * @param nybins the n Y bins
   * @param r2 The squared radius distance
   * @param minx the minx
   * @param miny the miny
   * @param startxbin the start X bin
   * @param endxbin the end X bin
   * @param startybin the start Y bin
   * @param endybin the end Y bin
   * @return The pair of closest points (or null)
   */
  private static ClosestPair findClosestParticle(ExtendedClusterPoint[][] grid, final int nxbins,
      final int nybins, final double r2, int startxbin, int endxbin, int startybin, int endybin) {
    double min = r2;
    ExtendedClusterPoint pair1 = null;
    ExtendedClusterPoint pair2 = null;
    for (int ybin = startybin; ybin < endybin; ybin++) {
      for (int xbin = startxbin; xbin < endxbin; xbin++) {
        for (ExtendedClusterPoint c1 = grid[xbin][ybin]; c1 != null; c1 = c1.getNextExtended()) {
          final boolean cluster1 = c1.isInCluster();

          // Build a list of which cells to compare up to a maximum of 4
          //@formatter:off
          //      | 0,0 | 1,0
          // ------------+-----
          // -1,1 | 0,1 | 1,1
          //@formatter:on

          // Compare to neighbours and find the closest.
          // Use either the radius threshold or the current closest distance
          // which may have been set by an earlier comparison.
          ExtendedClusterPoint other = null;

          for (ExtendedClusterPoint c2 = c1.getNextExtended(); c2 != null; c2 =
              c2.getNextExtended()) {
            // Ignore comparing points that are both in a cluster
            if (cluster1 && c2.isInCluster()) {
              continue;
            }

            final double d2 = c1.distanceSquared(c2);
            if (d2 < min) {
              min = d2;
              other = c2;
            }
          }

          if (ybin < nybins - 1) {
            for (ExtendedClusterPoint c2 = grid[xbin][ybin + 1]; c2 != null; c2 =
                c2.getNextExtended()) {
              // Ignore comparing points that are both in a cluster
              if (cluster1 && c2.isInCluster()) {
                continue;
              }
              final double d2 = c1.distanceSquared(c2);
              if (d2 < min) {
                min = d2;
                other = c2;
              }
            }
            if (xbin > 0) {
              for (ExtendedClusterPoint c2 = grid[xbin - 1][ybin + 1]; c2 != null; c2 =
                  c2.getNextExtended()) {
                // Ignore comparing points that are both in a cluster
                if (cluster1 && c2.isInCluster()) {
                  continue;
                }
                final double d2 = c1.distanceSquared(c2);
                if (d2 < min) {
                  min = d2;
                  other = c2;
                }
              }
            }
          }
          if (xbin < nxbins - 1) {
            for (ExtendedClusterPoint c2 = grid[xbin + 1][ybin]; c2 != null; c2 =
                c2.getNextExtended()) {
              // Ignore comparing points that are both in a cluster
              if (cluster1 && c2.isInCluster()) {
                continue;
              }
              final double d2 = c1.distanceSquared(c2);
              if (d2 < min) {
                min = d2;
                other = c2;
              }
            }
            if (ybin < nybins - 1) {
              for (ExtendedClusterPoint c2 = grid[xbin + 1][ybin + 1]; c2 != null; c2 =
                  c2.getNextExtended()) {
                // Ignore comparing points that are both in a cluster
                if (cluster1 && c2.isInCluster()) {
                  continue;
                }
                final double d2 = c1.distanceSquared(c2);
                if (d2 < min) {
                  min = d2;
                  other = c2;
                }
              }
            }
          }

          // Store the details of the closest pair
          if (other != null) {
            pair1 = c1;
            pair2 = other;
          }
        }
      }
    }
    if (pair1 != null) {
      return new ClosestPair(min, pair1, pair2);
    }
    return null;
  }

  /**
   * Count the number of points around each point.
   *
   * @param points the points
   * @param radius the radius
   * @return The density count
   */
  private static int[] calculateDensity(List<ClusterPoint> points, double radius) {
    final float[] xcoord = new float[points.size()];
    final float[] ycoord = new float[points.size()];
    int index = 0;
    for (final ClusterPoint p : points) {
      xcoord[index] = (float) p.getX();
      ycoord[index] = (float) p.getY();
      index++;
    }
    // The bounds are not used in the density calculation when not adjusting for borders
    final Rectangle bounds = new Rectangle();
    final DensityManager dm = new DensityManager(xcoord, ycoord, bounds);
    return dm.calculateDensity((float) radius, false);
  }

  /**
   * Check there are at least two different time points in the data. Also check if any candidates
   * have a different start and end time.
   *
   * @param candidates the candidates
   * @return true if there are no different time points
   */
  private boolean noTimeInformation(List<Cluster> candidates) {
    useRange = checkForTimeRange(candidates);
    final int firstT = candidates.get(0).getHeadClusterPoint().getStartTime();
    if (useRange) {
      final int lastT = candidates.get(0).getHeadClusterPoint().getEndTime();
      for (final Cluster c : candidates) {
        if (firstT != c.getHeadClusterPoint().getStartTime()) {
          return false;
        }
        if (lastT != c.getHeadClusterPoint().getEndTime()) {
          return false;
        }
      }
    } else {
      for (final Cluster c : candidates) {
        if (firstT != c.getHeadClusterPoint().getStartTime()) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Check if any of the candidates have a different start and end time.
   *
   * @param candidates the candidates
   * @return true, if successful
   */
  private static boolean checkForTimeRange(List<Cluster> candidates) {
    for (final Cluster c : candidates) {
      if (c.getHeadClusterPoint().getStartTime() != c.getHeadClusterPoint().getEndTime()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Sweep the all-vs-all clusters and make potential links between clusters. If a link can be made
   * to a closer cluster then break the link and rejoin. Then join all the links into clusters.
   *
   * @param grid the grid
   * @param nxbins the n X bins
   * @param nybins the n Y bins
   * @param r2 the r 2
   * @param minx the minx
   * @param miny the miny
   * @param xbinWidth the x bin width
   * @param ybinWidth the y bin width
   * @param candidates the candidates
   * @param singles the singles
   * @return The clusters
   */
  private List<Cluster> runPairwise(Cluster[][] grid, final int nxbins, final int nybins,
      final double r2, final double minx, final double miny, final double xbinWidth,
      final double ybinWidth, List<Cluster> candidates, List<Cluster> singles) {
    while (findPairwiseLinks(grid, nxbins, nybins, r2)) {
      if (tracker.isEnded()) {
        return null;
      }

      joinPairwiseLinks(grid, nxbins, nybins, candidates);

      // Reassign the grid
      for (final Cluster c : candidates) {
        final int xbin = (int) ((c.getX() - minx) / xbinWidth);
        final int ybin = (int) ((c.getY() - miny) / ybinWidth);
        // Build a single linked list
        c.setNext(grid[xbin][ybin]);
        grid[xbin][ybin] = c;
      }
    }

    candidates.addAll(singles);
    return candidates;
  }

  /**
   * Search for potential links between clusters that are below the squared radius distance. Store
   * if the clusters have any neighbours within 2*r^2.
   *
   * @param grid the grid
   * @param nxbins the n X bins
   * @param nybins the n Y bins
   * @param r2 The squared radius distance
   * @return True if any links were made
   */
  private static boolean findPairwiseLinks(Cluster[][] grid, final int nxbins, final int nybins,
      final double r2) {
    final Cluster[] neighbours = new Cluster[5];
    boolean linked = false;
    for (int ybin = 0; ybin < nybins; ybin++) {
      for (int xbin = 0; xbin < nxbins; xbin++) {
        for (Cluster c1 = grid[xbin][ybin]; c1 != null; c1 = c1.getNext()) {
          // Build a list of which cells to compare up to a maximum of 5
          //@formatter:off
          //      | 0,0 | 1,0
          // ------------+-----
          // -1,1 | 0,1 | 1,1
          //@formatter:on

          int count = 0;
          neighbours[count++] = c1.getNext();

          if (ybin < nybins - 1) {
            neighbours[count++] = grid[xbin][ybin + 1];
            if (xbin > 0) {
              neighbours[count++] = grid[xbin - 1][ybin + 1];
            }
          }
          if (xbin < nxbins - 1) {
            neighbours[count++] = grid[xbin + 1][ybin];
            if (ybin < nybins - 1) {
              neighbours[count++] = grid[xbin + 1][ybin + 1];
            }
          }

          // Compare to neighbours and find the closest.
          // Use either the radius threshold or the current closest distance
          // which may have been set by an earlier comparison.
          double min = (c1.getClosest() == null) ? r2 : c1.getDistanceSquared();
          Cluster other = null;
          while (count-- > 0) {
            for (Cluster c2 = neighbours[count]; c2 != null; c2 = c2.getNext()) {
              final double d2 = c1.distance2(c2);
              if (d2 < min) {
                min = d2;
                other = c2;
              }
            }
          }

          if (other != null) {
            // Store the potential link between the two clusters
            c1.link(other, min);
            linked = true;
          }
        }
      }
    }
    return linked;
  }

  /**
   * Join valid links between clusters. Resets the link candidates.
   *
   * @param grid the grid
   * @param nxbins the n X bins
   * @param nybins the n Y bins
   * @param candidates Re-populate will all the remaining clusters
   */
  private static void joinPairwiseLinks(Cluster[][] grid, int nxbins, int nybins,
      List<Cluster> candidates) {
    candidates.clear();

    for (int ybin = 0; ybin < nybins; ybin++) {
      for (int xbin = 0; xbin < nxbins; xbin++) {
        for (Cluster c1 = grid[xbin][ybin]; c1 != null; c1 = c1.getNext()) {
          if (c1.validLink()) {
            c1.add(c1.getClosest());
          }
          // Reset the link candidates
          c1.setClosest(null);

          // Store all remaining clusters
          if (c1.getSize() != 0) {
            candidates.add(c1);
          }
        }

        // Reset the grid
        grid[xbin][ybin] = null;
      }
    }
  }

  /**
   * Sweep the all-vs-all clusters and make potential links between clusters. If a link can be made
   * to a closer cluster then break the link and rejoin. Then join all the links into clusters only
   * if the pair has no other neighbours. Default to joining the closest pair.
   *
   * @param grid the grid
   * @param nxbins the n X bins
   * @param nybins the n Y bins
   * @param r2 the r 2
   * @param minx the minx
   * @param miny the miny
   * @param xbinWidth the x bin width
   * @param ybinWidth the y bin width
   * @param candidates the candidates
   * @param singles the singles
   * @return the list of clusters
   */
  private List<Cluster> runPairwiseWithoutNeighbours(Cluster[][] grid, final int nxbins,
      final int nybins, final double r2, final double minx, final double miny,
      final double xbinWidth, final double ybinWidth, List<Cluster> candidates,
      List<Cluster> singles) {
    // The find method is multi-threaded
    // The remaining join and update of the grid is single threaded
    initialiseMultithreading(nxbins, nybins);

    // Store if the clusters have any neighbours within sqrt(2)*r and remove them
    // from the next loop.
    final int totalCandidates = candidates.size();
    final List<Cluster> joined = new ArrayList<>();
    while (findLinksAndCountNeighbours(grid, nxbins, nybins, r2)) {
      if (tracker.isEnded()) {
        return null;
      }

      final int joins = joinLinks(grid, nxbins, nybins, r2, candidates, joined, singles);
      if (joins == 0) {
        break; // This should not happen
      }

      tracker.progress((long) totalCandidates - candidates.size(), totalCandidates);

      // TODO - determine at what point it is faster to reassign the grid
      if (joins < candidates.size() / 5) {
        // Reassigning the whole grid is a costly step when the number of joins is small.
        // In that case check the clusters that were updated and reassign them to a new
        // grid position if necessary.
        for (final Cluster c : candidates) {
          if (c.getNeighbour() != 0) {
            c.setNeighbour(0);
            final int xbin = (int) ((c.getX() - minx) / xbinWidth);
            final int ybin = (int) ((c.getY() - miny) / ybinWidth);

            // Check the current grid position.
            if (xbin != c.getXBin() || ybin != c.getYBin()) {
              remove(grid, c);

              c.setXBin(xbin);
              c.setYBin(ybin);
              c.setNext(grid[xbin][ybin]);
              grid[xbin][ybin] = c;
            }
          }
        }

        // We must remove the joined clusters from the grid
        for (final Cluster c : joined) {
          remove(grid, c);
        }
      } else {
        // Reassign the grid.
        for (int xbin = 0; xbin < nxbins; xbin++) {
          for (int ybin = 0; ybin < nybins; ybin++) {
            grid[xbin][ybin] = null;
          }
        }
        for (final Cluster c : candidates) {
          // Only candidates that have been flagged as a join may have changed
          // their grid position
          if (c.getNeighbour() != 0) {
            c.setXBin((int) ((c.getX() - minx) / xbinWidth));
            c.setYBin((int) ((c.getY() - miny) / ybinWidth));
          }
          // Build a single linked list
          c.setNext(grid[c.getXBin()][c.getYBin()]);
          grid[c.getXBin()][c.getYBin()] = c;
          c.setNeighbour(0);
        }
      }
    }

    return combine(singles, grid, nxbins, nybins);
  }

  /**
   * Search for potential links between clusters that are below the squared radius distance. Store
   * if the clusters have any neighbours within 2*r^2.
   *
   * @param grid the grid
   * @param nxbins the n X bins
   * @param nybins the n Y bins
   * @param r2 The squared radius distance
   * @return True if any links were made
   */
  private boolean findLinksAndCountNeighbours(Cluster[][] grid, final int nxbins, final int nybins,
      final double r2) {
    if (threadPool == null) {
      return findLinksAndCountNeighbours(grid, nxbins, nybins, r2, 0, nxbins, 0, nybins,
          // No synchronisation required, just call the methods direct
          // Increment the neighbours
          (c) -> c.incrementNeighbour(),
          // Link two clusters if the distance is an improvement
          (c1, c2, d2) -> c1.link(c2, d2));
    }
    // Use threads to find the closest pairs in blocks
    final List<Future<?>> futures = new LinkedList<>();
    final List<FindLinksWorker> results = new LinkedList<>();

    for (int startybin = 0; startybin < nybins; startybin += yblock) {
      final int endybin = FastMath.min(nybins, startybin + yblock);
      for (int startxbin = 0; startxbin < nxbins; startxbin += xblock) {
        final int endxbin = FastMath.min(nxbins, startxbin + xblock);

        final FindLinksWorker worker =
            new FindLinksWorker(grid, nxbins, nybins, r2, startxbin, endxbin, startybin, endybin);
        results.add(worker);
        futures.add(threadPool.submit(worker));
      }
    }

    // Finish processing data
    ConcurrencyUtils.waitForCompletionOrError(futures);

    for (final FindLinksWorker worker : results) {
      if (worker.links) {
        return true;
      }
    }
    return false;
  }

  /**
   * Allow cluster neighbour to be incremented.
   */
  @FunctionalInterface
  private interface ClusterNeighbourIncrementer {
    /**
     * Increment the neighbour count.
     *
     * @param cluster the cluster
     */
    void incrementNeighbour(Cluster cluster);
  }

  /**
   * Allow clusters to be linked.
   */
  @FunctionalInterface
  private interface ClusterLinker {
    /**
     * Link the two clusters if neither is already joined at a distance less that the squared
     * distance.
     *
     * @param c1 the first cluster
     * @param c2 the second cluster
     * @param d2 the squared distance
     */
    void link(Cluster c1, Cluster c2, double d2);
  }

  /**
   * Search for potential links between clusters that are below the squared radius distance. Store
   * if the clusters have any neighbours within 2*r^2.
   *
   * @param grid the grid
   * @param nxbins the n X bins
   * @param nybins the n Y bins
   * @param r2 The squared radius distance
   * @param startxbin the start X bin
   * @param endxbin the end X bin
   * @param startybin the start Y bin
   * @param endybin the end Y bin
   * @param requireSynchronized Set to true if cluster updates require synchronized
   * @return True if any links were made
   */
  private static boolean findLinksAndCountNeighbours(Cluster[][] grid, final int nxbins,
      final int nybins, final double r2, int startxbin, int endxbin, int startybin, int endybin,
      ClusterNeighbourIncrementer clusterNeighbourIncrementer, ClusterLinker clusterLinker) {
    final Cluster[] neighbours = new Cluster[5];
    boolean linked = false;
    final double neighbourDistance = 2 * r2;

    for (int ybin = startybin; ybin < endybin; ybin++) {
      for (int xbin = startxbin; xbin < endxbin; xbin++) {
        for (Cluster c1 = grid[xbin][ybin]; c1 != null; c1 = c1.getNext()) {
          // Build a list of which cells to compare up to a maximum of 5
          //@formatter:off
          //      | 0,0 | 1,0
          // ------------+-----
          // -1,1 | 0,1 | 1,1
          //@formatter:on

          int count = 0;
          neighbours[count++] = c1.getNext();

          if (ybin < nybins - 1) {
            neighbours[count++] = grid[xbin][ybin + 1];
            if (xbin > 0) {
              neighbours[count++] = grid[xbin - 1][ybin + 1];
            }
          }
          if (xbin < nxbins - 1) {
            neighbours[count++] = grid[xbin + 1][ybin];
            if (ybin < nybins - 1) {
              neighbours[count++] = grid[xbin + 1][ybin + 1];
            }
          }

          // Compare to neighbours and find the closest.
          // Use either the radius threshold or the current closest distance
          // which may have been set by an earlier comparison.
          double min = (c1.getClosest() == null) ? r2 : c1.getDistanceSquared();
          Cluster other = null;
          while (count-- > 0) {
            for (Cluster c2 = neighbours[count]; c2 != null; c2 = c2.getNext()) {
              final double d2 = c1.distance2(c2);
              if (d2 < neighbourDistance) {
                clusterNeighbourIncrementer.incrementNeighbour(c1);
                clusterNeighbourIncrementer.incrementNeighbour(c2);
                if (d2 < min) {
                  min = d2;
                  other = c2;
                }
              }
            }
          }

          if (other != null) {
            // Store the potential link between the two clusters
            clusterLinker.link(c1, other, min);
            linked = true;
          }
        }
      }
    }
    return linked;
  }

  /**
   * Join valid links between clusters. Resets the link candidates. Use the neighbour count property
   * to flag if the candidate was joined to another cluster.
   *
   * @param grid the grid
   * @param nxbins the n X bins
   * @param nybins the n Y bins
   * @param r2 the r 2
   * @param candidates Re-populate will all the remaining clusters
   * @param joined the joined
   * @param singles Add any clusters with no neighbours
   * @return The number of joins that were made
   */
  private static int joinLinks(Cluster[][] grid, int nxbins, int nybins, double r2,
      List<Cluster> candidates, List<Cluster> joined, List<Cluster> singles) {
    candidates.clear();
    joined.clear();

    double min = r2;
    Cluster cluster1 = null;
    Cluster cluster2 = null;
    for (int ybin = 0; ybin < nybins; ybin++) {
      for (int xbin = 0; xbin < nxbins; xbin++) {
        Cluster previous = null;
        for (Cluster c1 = grid[xbin][ybin]; c1 != null; c1 = c1.getNext()) {
          int joinFlag = 0;
          if (c1.validLink()) {
            // Check if each cluster only has 1 neighbour
            if (c1.getNeighbour() == 1 && c1.getClosest().getNeighbour() == 1) {
              c1.add(c1.getClosest());
              joined.add(c1.getClosest());
              joinFlag = 1;
            } else if (c1.getDistanceSquared() < min) {
              // Otherwise find the closest pair in case no joins are made
              min = c1.getDistanceSquared();
              cluster1 = c1;
              cluster2 = c1.getClosest();
            }
          }
          // Reset the link candidates
          c1.setClosest(null);

          // Check for singles
          if (c1.getNeighbour() == 0) {
            // Add singles to the singles list and remove from the grid
            singles.add(c1);
            if (previous == null) {
              grid[xbin][ybin] = c1.getNext();
            } else {
              previous.setNext(c1.getNext());
            }
          } else {
            previous = c1;

            // Store all remaining clusters
            if (c1.getSize() != 0) {
              candidates.add(c1);
              c1.setNeighbour(joinFlag);
            }
          }
        }
      }
    }

    // Always join the closest pair if it has not been already
    if (cluster1 != null && cluster1.getNeighbour() == 0) {
      cluster1.add(cluster2);
      // Remove cluster 2 from the candidates
      candidates.remove(cluster2);
      joined.add(cluster2);
      cluster1.setNeighbour(1);
    }

    return joined.size();
  }

  /**
   * The process should iterate finding the closest nodes, joining them and repeating. The iterative
   * process of joining the closest pair will be slow. Hopefully it will be manageable.
   *
   * @param grid the grid
   * @param nxbins the n X bins
   * @param nybins the n Y bins
   * @param r2 the r 2
   * @param minx the minx
   * @param miny the miny
   * @param xbinWidth the x bin width
   * @param ybinWidth the y bin width
   * @param candidates the candidates
   * @param singles the singles
   * @param single True if only singles can be joined to another cluster
   * @return the list of clusters
   */
  private List<Cluster> runClosest(Cluster[][] grid, int nxbins, int nybins, double r2, double minx,
      double miny, double xbinWidth, double ybinWidth, List<Cluster> candidates,
      List<Cluster> singles, boolean single) {
    final int totalCandidates = candidates.size();
    int candidatesProcessed = 0;
    int singlesSize = singles.size();
    final boolean trackProgress = (tracker.getClass() != NullTrackProgress.class);
    initialiseMultithreading(nxbins, nybins);
    while (joinClosest(grid, nxbins, nybins, r2, minx, miny, xbinWidth, ybinWidth, single)) {
      if (tracker.isEnded()) {
        return null;
      }

      // The number of candidates that have been processed is incremented by the number of singles
      if (trackProgress) {
        candidatesProcessed += singles.size() - singlesSize;
        singlesSize = singles.size();
        tracker.progress(candidatesProcessed++, totalCandidates);
      }
    }

    return combine(singles, grid, nxbins, nybins);
  }

  /**
   * Search for the closest pair of clusters that are below the squared radius distance and join
   * them.
   *
   * @param grid the grid
   * @param nxbins the n X bins
   * @param nybins the n Y bins
   * @param r2 The squared radius distance
   * @param minx the minx
   * @param miny the miny
   * @param xbinWidth the x bin width
   * @param ybinWidth the y bin width
   * @param single True if only singles can be joined to another cluster
   * @return True if a join was made
   */
  private boolean joinClosest(Cluster[][] grid, final int nxbins, final int nybins, final double r2,
      double minx, double miny, double xbinWidth, double ybinWidth, boolean single) {
    ClosestPair closest = null;

    if (threadPool == null) {
      closest = (single) ? findClosestSingle(grid, nxbins, nybins, r2, 0, nxbins, 0, nybins)
          : findClosest(grid, nxbins, nybins, r2, 0, nxbins, 0, nybins);
    } else {
      // Use threads to find the closest pairs in blocks
      final List<Future<?>> futures = new LinkedList<>();
      final List<ClosestPair> results = new LinkedList<>();

      for (int startybin = 0; startybin < nybins; startybin += yblock) {
        final int endybin = FastMath.min(nybins, startybin + yblock);
        for (int startxbin = 0; startxbin < nxbins; startxbin += xblock) {
          final int endxbin = FastMath.min(nxbins, startxbin + xblock);
          // System.out.printf("Block [%d-%d, %d-%d]\n", startxbin, endxbin, startybin, endybin)

          final ClosestPair pair = new ClosestPair();
          results.add(pair);
          futures.add(threadPool.submit(new ClosestWorker(pair, grid, nxbins, nybins, r2, startxbin,
              endxbin, startybin, endybin, single)));
        }
      }

      // Finish processing data
      ConcurrencyUtils.waitForCompletionOrError(futures);

      // Find the closest pair from all the results
      for (final ClosestPair result : results) {
        if (result.point1 != null && (closest == null || result.distance < closest.distance)) {
          closest = result;
        }
      }
    }

    // Join the closest pair
    if (closest != null) {
      Cluster pair1 = (Cluster) closest.point1;
      Cluster pair2 = (Cluster) closest.point2;

      if (single) {
        // Check
        if (pair1.getSize() > 1 && pair2.getSize() > 1) {
          throw new ComputationException(
              "Linkage between two clusters (not a single particle and a single/cluster)");
        }

        // Add the single to the cluster
        if (pair2.getSize() < pair1.getSize()) {
          final Cluster tmp = pair1;
          pair1 = pair2;
          pair2 = tmp;
        }
      }

      pair2.add(pair1);

      remove(grid, pair1);

      // Reassign the updated grid position
      final int xbin = (int) ((pair2.getX() - minx) / xbinWidth);
      final int ybin = (int) ((pair2.getY() - miny) / ybinWidth);

      if (xbin != pair2.getXBin() || ybin != pair2.getYBin()) {
        remove(grid, pair2);

        // Build a single linked list
        pair2.setXBin(xbin);
        pair2.setYBin(ybin);
        pair2.setNext(grid[xbin][ybin]);
        grid[xbin][ybin] = pair2;
      }

      return true;
    }

    return false;
  }

  /**
   * Search for the closest pair of clusters that are below the squared radius distance.
   *
   * @param grid the grid
   * @param nxbins the n X bins
   * @param nybins the n Y bins
   * @param r2 The squared radius distance
   * @param startxbin the start X bin
   * @param endxbin the end X bin
   * @param startybin the start Y bin
   * @param endybin the end Y bin
   * @return The closest pair
   */
  private static ClosestPair findClosest(Cluster[][] grid, final int nxbins, final int nybins,
      final double r2, int startxbin, int endxbin, int startybin, int endybin) {
    double min = r2;
    Cluster pair1 = null;
    Cluster pair2 = null;
    for (int ybin = startybin; ybin < endybin; ybin++) {
      for (int xbin = startxbin; xbin < endxbin; xbin++) {
        for (Cluster c1 = grid[xbin][ybin]; c1 != null; c1 = c1.getNext()) {
          // Build a list of which cells to compare up to a maximum of 4
          //@formatter:off
          //      | 0,0 | 1,0
          // ------------+-----
          // -1,1 | 0,1 | 1,1
          //@formatter:on

          // Compare to neighbours and find the closest.
          // Use either the radius threshold or the current closest distance
          // which may have been set by an earlier comparison.
          Cluster other = null;

          for (Cluster c2 = c1.getNext(); c2 != null; c2 = c2.getNext()) {
            final double d2 = c1.distance2(c2);
            if (d2 < min) {
              min = d2;
              other = c2;
            }
          }

          if (ybin < nybins - 1) {
            for (Cluster c2 = grid[xbin][ybin + 1]; c2 != null; c2 = c2.getNext()) {
              final double d2 = c1.distance2(c2);
              if (d2 < min) {
                min = d2;
                other = c2;
              }
            }
            if (xbin > 0) {
              for (Cluster c2 = grid[xbin - 1][ybin + 1]; c2 != null; c2 = c2.getNext()) {
                final double d2 = c1.distance2(c2);
                if (d2 < min) {
                  min = d2;
                  other = c2;
                }
              }
            }
          }
          if (xbin < nxbins - 1) {
            for (Cluster c2 = grid[xbin + 1][ybin]; c2 != null; c2 = c2.getNext()) {
              final double d2 = c1.distance2(c2);
              if (d2 < min) {
                min = d2;
                other = c2;
              }
            }
            if (ybin < nybins - 1) {
              for (Cluster c2 = grid[xbin + 1][ybin + 1]; c2 != null; c2 = c2.getNext()) {
                final double d2 = c1.distance2(c2);
                if (d2 < min) {
                  min = d2;
                  other = c2;
                }
              }
            }
          }

          // Store the details of the closest pair
          if (other != null) {
            pair1 = c1;
            pair2 = other;
          }
        }
      }
    }
    if (pair1 != null) {
      return new ClosestPair(min, pair1, pair2);
    }
    return null;
  }

  /**
   * Remove cluster from the grid by sweeping the linked list grid position.
   *
   * @param grid the grid
   * @param cluster the cluster
   */
  private static void remove(Cluster[][] grid, Cluster cluster) {
    Cluster previous = null;
    for (Cluster c1 = grid[cluster.getXBin()][cluster.getYBin()]; c1 != null; c1 = c1.getNext()) {
      if (c1 == cluster) {
        if (previous == null) {
          grid[cluster.getXBin()][cluster.getYBin()] = c1.getNext();
        } else {
          previous.setNext(c1.getNext());
        }
        return;
      }
      previous = c1;
    }
  }

  /**
   * Add the clusters in the grid to the existing singles.
   *
   * @param singles the singles
   * @param grid the grid
   * @param nxbins the n X bins
   * @param nybins the n Y bins
   * @return The combined clusters
   */
  private static List<Cluster> combine(List<Cluster> singles, Cluster[][] grid, int nxbins,
      int nybins) {
    for (int xbin = 0; xbin < nxbins; xbin++) {
      for (int ybin = 0; ybin < nybins; ybin++) {
        for (Cluster c1 = grid[xbin][ybin]; c1 != null; c1 = c1.getNext()) {
          singles.add(c1);
        }
      }
    }
    return singles;
  }

  /**
   * Search for the closest pair of a single particle and any existing single/cluster that are below
   * the squared radius distance.
   *
   * @param grid the grid
   * @param nxbins the n X bins
   * @param nybins the n Y bins
   * @param r2 The squared radius distance
   * @param startxbin the start X bin
   * @param endxbin the end X bin
   * @param startybin the start Y bin
   * @param endybin the end Y bin
   * @return The closest pair
   */
  private static ClosestPair findClosestSingle(Cluster[][] grid, final int nxbins, final int nybins,
      final double r2, int startxbin, int endxbin, int startybin, int endybin) {
    double min = r2;
    Cluster pair1 = null;
    Cluster pair2 = null;
    for (int ybin = startybin; ybin < endybin; ybin++) {
      for (int xbin = startxbin; xbin < endxbin; xbin++) {
        for (Cluster c1 = grid[xbin][ybin]; c1 != null; c1 = c1.getNext()) {
          final boolean cluster1 = c1.getSize() > 1;

          // Build a list of which cells to compare up to a maximum of 4
          //@formatter:off
          //      | 0,0 | 1,0
          // ------------+-----
          // -1,1 | 0,1 | 1,1
          //@formatter:on

          // Compare to neighbours and find the closest.
          // Use either the radius threshold or the current closest distance
          // which may have been set by an earlier comparison.
          Cluster other = null;

          for (Cluster c2 = c1.getNext(); c2 != null; c2 = c2.getNext()) {
            if (cluster1 && c2.getSize() > 1) {
              continue;
            }
            final double d2 = c1.distance2(c2);
            if (d2 < min) {
              min = d2;
              other = c2;
            }
          }

          if (ybin < nybins - 1) {
            for (Cluster c2 = grid[xbin][ybin + 1]; c2 != null; c2 = c2.getNext()) {
              if (cluster1 && c2.getSize() > 1) {
                continue;
              }
              final double d2 = c1.distance2(c2);
              if (d2 < min) {
                min = d2;
                other = c2;
              }
            }
            if (xbin > 0) {
              for (Cluster c2 = grid[xbin - 1][ybin + 1]; c2 != null; c2 = c2.getNext()) {
                if (cluster1 && c2.getSize() > 1) {
                  continue;
                }
                final double d2 = c1.distance2(c2);
                if (d2 < min) {
                  min = d2;
                  other = c2;
                }
              }
            }
          }
          if (xbin < nxbins - 1) {
            for (Cluster c2 = grid[xbin + 1][ybin]; c2 != null; c2 = c2.getNext()) {
              if (cluster1 && c2.getSize() > 1) {
                continue;
              }
              final double d2 = c1.distance2(c2);
              if (d2 < min) {
                min = d2;
                other = c2;
              }
            }
            if (ybin < nybins - 1) {
              for (Cluster c2 = grid[xbin + 1][ybin + 1]; c2 != null; c2 = c2.getNext()) {
                if (cluster1 && c2.getSize() > 1) {
                  continue;
                }
                final double d2 = c1.distance2(c2);
                if (d2 < min) {
                  min = d2;
                  other = c2;
                }
              }
            }
          }

          // Store the details of the closest pair
          if (other != null) {
            pair1 = c1;
            pair2 = other;
          }
        }
      }
    }
    if (pair1 != null) {
      return new ClosestPair(min, pair1, pair2);
    }
    return null;
  }

  /**
   * The process should iterate finding the closest nodes, joining them and repeating. Join closest
   * in time and then distance.
   *
   * @param grid the grid
   * @param nxbins the n X bins
   * @param nybins the n Y bins
   * @param r2 the r 2
   * @param time the time
   * @param minx the minx
   * @param miny the miny
   * @param xbinWidth the x bin width
   * @param ybinWidth the y bin width
   * @param candidates the candidates
   * @param singles the singles
   * @param single True if only singles can be joined to another cluster
   * @return the list of clusters
   */
  private List<Cluster> runClosestTimePriority(Cluster[][] grid, int nxbins, int nybins, double r2,
      int time, double minx, double miny, double xbinWidth, double ybinWidth,
      List<Cluster> candidates, List<Cluster> singles, boolean single) {
    final int totalCandidates = candidates.size();
    int candidatesProcessed = 0;
    int singleSize = singles.size();
    final boolean trackProgress = (tracker.getClass() != NullTrackProgress.class);
    final TimeCluster[][] newGrid = convertGrid(grid, nxbins, nybins);
    initialiseMultithreading(nxbins, nybins);
    while (joinClosestTimePriority(newGrid, nxbins, nybins, r2, time, minx, miny, xbinWidth,
        ybinWidth, single)) {
      if (tracker.isEnded()) {
        return null;
      }

      // The number of candidates that have been processed is incremented by the number of singles
      if (trackProgress) {
        candidatesProcessed += singles.size() - singleSize;
        singleSize = singles.size();
        tracker.progress(candidatesProcessed++, totalCandidates);
      }
    }

    return combine(singles, newGrid, nxbins, nybins);
  }

  private TimeCluster[][] convertGrid(Cluster[][] grid, int nxbins, int nybins) {
    final TimeCluster[][] newGrid = new TimeCluster[nxbins][nybins];
    for (int ybin = 0; ybin < nybins; ybin++) {
      for (int xbin = 0; xbin < nxbins; xbin++) {
        for (Cluster c1 = grid[xbin][ybin]; c1 != null; c1 = c1.getNext()) {
          // Build a single linked list
          final TimeCluster c = new TimeCluster(c1.getHeadClusterPoint());
          c.setPulseTime(getPulse(c.getStartTime()));
          c.setXBin(xbin);
          c.setYBin(ybin);
          c.setNext(newGrid[xbin][ybin]);
          newGrid[xbin][ybin] = c;
        }
      }
    }
    return newGrid;
  }

  /**
   * Search for the closest pair of clusters that are below the squared radius distance and join
   * them. Join closest in time and then distance.
   *
   * @param grid the grid
   * @param nxbins the n X bins
   * @param nybins the n Y bins
   * @param r2 The squared radius distance
   * @param time The time threshold
   * @param minx the minx
   * @param miny the miny
   * @param xbinWidth the x bin width
   * @param ybinWidth the y bin width
   * @param single True if only singles can be joined to another cluster
   * @return True if a join was made
   */
  private boolean joinClosestTimePriority(TimeCluster[][] grid, final int nxbins, final int nybins,
      final double r2, final int time, double minx, double miny, double xbinWidth, double ybinWidth,
      boolean single) {
    ClosestPair closest = null;

    if (threadPool == null) {
      closest = (single)
          ? findClosestParticleTimePriority(grid, nxbins, nybins, r2, time, 0, nxbins, 0, nybins)
          : findClosestTimePriority(grid, nxbins, nybins, r2, time, 0, nxbins, 0, nybins);
    } else {
      // Use threads to find the closest pairs in blocks
      final List<Future<?>> futures = new LinkedList<>();
      final List<ClosestPair> results = new LinkedList<>();

      for (int startybin = 0; startybin < nybins; startybin += yblock) {
        final int endybin = FastMath.min(nybins, startybin + yblock);
        for (int startxbin = 0; startxbin < nxbins; startxbin += xblock) {
          final int endxbin = FastMath.min(nxbins, startxbin + xblock);

          final ClosestPair pair = new ClosestPair();
          results.add(pair);
          futures.add(threadPool.submit(new ClosestPriorityWorker(true, pair, grid, nxbins, nybins,
              r2, time, startxbin, endxbin, startybin, endybin, single)));
        }
      }

      // Finish processing data
      ConcurrencyUtils.waitForCompletionOrError(futures);
      futures.clear();

      // Find the closest pair from all the results
      for (final ClosestPair result : results) {
        if (result.point1 != null && (closest == null || (result.time < closest.time)
            || (result.time <= closest.time && result.distance < closest.distance))) {
          closest = result;
        }
      }
    }

    // Join the closest pair
    if (closest != null) {
      final TimeCluster pair1 = (TimeCluster) closest.point1;
      final TimeCluster pair2 = (TimeCluster) closest.point2;

      pair2.add(pair1);

      remove(grid, pair1);

      // Reassign the updated grid position
      final int xbin = (int) ((pair2.getX() - minx) / xbinWidth);
      final int ybin = (int) ((pair2.getY() - miny) / ybinWidth);

      if (xbin != pair2.getXBin() || ybin != pair2.getYBin()) {
        remove(grid, pair2);

        // Build a single linked list
        pair2.setXBin(xbin);
        pair2.setYBin(ybin);
        pair2.setNext(grid[xbin][ybin]);
        grid[xbin][ybin] = pair2;
      }

      return true;
    }

    return false;
  }

  /**
   * Search for the closest pair of clusters that are below the squared radius distance. Find the
   * closest in time and then distance.
   *
   * @param grid the grid
   * @param nxbins the n X bins
   * @param nybins the n Y bins
   * @param r2 The squared radius distance
   * @param time the time
   * @param startxbin the start X bin
   * @param endxbin the end X bin
   * @param startybin the start Y bin
   * @param endybin the end Y bin
   * @return True if a join was made
   */
  private ClosestPair findClosestTimePriority(TimeCluster[][] grid, final int nxbins,
      final int nybins, final double r2, final int time, int startxbin, int endxbin, int startybin,
      int endybin) {
    double minD = Double.POSITIVE_INFINITY;
    int minT = Integer.MAX_VALUE;
    TimeCluster pair1 = null;
    TimeCluster pair2 = null;
    final TimeCluster[] neighbourCells = new TimeCluster[5];
    final boolean checkPulseInterval = pulseInterval > 0;
    for (int ybin = startybin; ybin < endybin; ybin++) {
      for (int xbin = startxbin; xbin < endxbin; xbin++) {
        for (TimeCluster c1 = grid[xbin][ybin]; c1 != null; c1 = (TimeCluster) c1.getNext()) {
          // Build a list of which cells to compare up to a maximum of 4
          //@formatter:off
          //      | 0,0 | 1,0
          // ------------+-----
          // -1,1 | 0,1 | 1,1
          //@formatter:on

          // Compare to neighbours and find the closest.
          // Use either the radius threshold or the current closest distance
          // which may have been set by an earlier comparison.
          TimeCluster other = null;
          int cells = 1;
          neighbourCells[0] = (TimeCluster) c1.getNext();
          if (ybin < nybins - 1) {
            neighbourCells[cells++] = grid[xbin][ybin + 1];
            if (xbin > 0) {
              neighbourCells[cells++] = grid[xbin - 1][ybin + 1];
            }
          }
          if (xbin < nxbins - 1) {
            neighbourCells[cells++] = grid[xbin + 1][ybin];
            if (ybin < nybins - 1) {
              neighbourCells[cells++] = grid[xbin + 1][ybin + 1];
            }
          }

          for (int c = 0; c < cells; c++) {
            for (TimeCluster c2 = neighbourCells[c]; c2 != null; c2 = (TimeCluster) c2.getNext()) {
              if (checkPulseInterval && c1.getPulseTime() != c2.getPulseTime()) {
                continue;
              }

              final int gap = c1.gap(c2);
              if (gap <= time) {
                final double d2 = c1.distance2(c2);

                if (d2 <= r2) {
                  // Check if the two clusters can be merged
                  if (invalidUnion(gap, c1, c2)) {
                    continue;
                  }

                  // This is within the time and distance thresholds.
                  // Find closest pair with time priority
                  if ((gap < minT) || (gap <= minT && d2 < minD)) {
                    minD = d2;
                    minT = gap;
                    other = c2;
                  }
                }
              }
            }
          }

          // Store the details of the closest pair
          if (other != null) {
            pair1 = c1;
            pair2 = other;
          }
        }
      }
    }
    if (pair1 != null) {
      return new ClosestPair(minD, minT, pair1, pair2);
    }
    return null;
  }

  /**
   * Check if the union of two clusters time points is invalid.
   *
   * <p>If the gap is non zero then this is valid.
   *
   * <p>If the time gap is zero then the union of the two clusters time points is checked to ensure
   * distinct time points exists across all the cluster points.
   *
   * @param gap the time gap between the clusters (zero signals overlap)
   * @param c1 the first cluster
   * @param c2 the second cluster
   * @return true if invalid
   */
  private boolean invalidUnion(int gap, TimeCluster c1, TimeCluster c2) {
    return (gap == 0) && !validUnion(c1, c2);
  }

  /**
   * Check if the union of two clusters time points is valid.
   *
   * @param c1 the first cluster
   * @param c2 the second cluster
   * @return true if valid
   */
  private boolean validUnion(TimeCluster c1, TimeCluster c2) {
    return (useRange) ? c1.validUnionRange(c2) : c1.validUnion(c2);
  }

  /**
   * Search for the closest pair of a single particle and any existing single/cluster that are below
   * the squared radius distance. Find the closest in time and then distance.
   *
   * @param grid the grid
   * @param nxbins the n X bins
   * @param nybins the n Y bins
   * @param r2 The squared radius distance
   * @param time the time
   * @param startxbin the start X bin
   * @param endxbin the end X bin
   * @param startybin the start Y bin
   * @param endybin the end Y bin
   * @return True if a join was made
   */
  private ClosestPair findClosestParticleTimePriority(TimeCluster[][] grid, final int nxbins,
      final int nybins, final double r2, final int time, int startxbin, int endxbin, int startybin,
      int endybin) {
    double minD = Double.POSITIVE_INFINITY;
    int minT = Integer.MAX_VALUE;
    TimeCluster pair1 = null;
    TimeCluster pair2 = null;
    final TimeCluster[] neighbourCells = new TimeCluster[5];
    final boolean checkPulseInterval = pulseInterval > 0;
    for (int ybin = startybin; ybin < endybin; ybin++) {
      for (int xbin = startxbin; xbin < endxbin; xbin++) {
        for (TimeCluster c1 = grid[xbin][ybin]; c1 != null; c1 = (TimeCluster) c1.getNext()) {
          final boolean cluster1 = c1.getSize() > 1;

          // Build a list of which cells to compare up to a maximum of 4
          //@formatter:off
          //      | 0,0 | 1,0
          // ------------+-----
          // -1,1 | 0,1 | 1,1
          //@formatter:on

          // Compare to neighbours and find the closest.
          // Use either the radius threshold or the current closest distance
          // which may have been set by an earlier comparison.
          TimeCluster other = null;
          int cells = 1;
          neighbourCells[0] = (TimeCluster) c1.getNext();
          if (ybin < nybins - 1) {
            neighbourCells[cells++] = grid[xbin][ybin + 1];
            if (xbin > 0) {
              neighbourCells[cells++] = grid[xbin - 1][ybin + 1];
            }
          }
          if (xbin < nxbins - 1) {
            neighbourCells[cells++] = grid[xbin + 1][ybin];
            if (ybin < nybins - 1) {
              neighbourCells[cells++] = grid[xbin + 1][ybin + 1];
            }
          }

          for (int c = 0; c < cells; c++) {
            for (TimeCluster c2 = neighbourCells[c]; c2 != null; c2 = (TimeCluster) c2.getNext()) {
              if (cluster1 && c2.getSize() > 1) {
                continue;
              }
              if (checkPulseInterval && c1.getPulseTime() != c2.getPulseTime()) {
                continue;
              }

              final int gap = c1.gap(c2);
              if (gap <= time) {
                final double d2 = c1.distance2(c2);

                if (d2 <= r2) {
                  // Check if the two clusters can be merged
                  if (invalidUnion(gap, c1, c2)) {
                    continue;
                  }

                  // This is within the time and distance thresholds.
                  // Find closest pair with time priority
                  if ((gap < minT) || (gap <= minT && d2 < minD)) {
                    minD = d2;
                    minT = gap;
                    other = c2;
                  }
                }
              }
            }
          }

          // Store the details of the closest pair
          if (other != null) {
            pair1 = c1;
            pair2 = other;
          }
        }
      }
    }
    if (pair1 != null) {
      return new ClosestPair(minD, minT, pair1, pair2);
    }
    return null;
  }

  /**
   * The process should iterate finding the closest nodes, joining them and repeating. Join closest
   * in distance and then time.
   *
   * @param grid the grid
   * @param nxbins the n X bins
   * @param nybins the n Y bins
   * @param r2 the r 2
   * @param time the time
   * @param minx the minx
   * @param miny the miny
   * @param xbinWidth the x bin width
   * @param ybinWidth the y bin width
   * @param candidates the candidates
   * @param singles the singles
   * @param single True if only singles can be joined to another cluster
   * @return the list of clusters
   */
  private List<Cluster> runClosestDistancePriority(Cluster[][] grid, int nxbins, int nybins,
      double r2, int time, double minx, double miny, double xbinWidth, double ybinWidth,
      List<Cluster> candidates, List<Cluster> singles, boolean single) {
    final int totalCandidates = candidates.size();
    int candidatesProcessed = 0;
    int singlesSize = singles.size();
    final boolean trackProgress = (tracker.getClass() != NullTrackProgress.class);
    final TimeCluster[][] newGrid = convertGrid(grid, nxbins, nybins);
    initialiseMultithreading(nxbins, nybins);
    while (joinClosestDistancePriority(newGrid, nxbins, nybins, r2, time, minx, miny, xbinWidth,
        ybinWidth, single)) {
      if (tracker.isEnded()) {
        return null;
      }

      // The number of candidates that have been processed is incremented by the number of singles
      if (trackProgress) {
        candidatesProcessed += singles.size() - singlesSize;
        singlesSize = singles.size();
        tracker.progress(candidatesProcessed++, totalCandidates);
      }
    }

    return combine(singles, newGrid, nxbins, nybins);
  }

  /**
   * Search for the closest pair of clusters that are below the squared radius distance and join
   * them. Join closest in distance and then time.
   *
   * @param grid the grid
   * @param nxbins the n X bins
   * @param nybins the n Y bins
   * @param r2 The squared radius distance
   * @param time The time threshold
   * @param minx the minx
   * @param miny the miny
   * @param xbinWidth the x bin width
   * @param ybinWidth the y bin width
   * @param single True if only singles can be joined to another cluster
   * @return True if a join was made
   */
  private boolean joinClosestDistancePriority(TimeCluster[][] grid, final int nxbins,
      final int nybins, final double r2, int time, double minx, double miny, double xbinWidth,
      double ybinWidth, boolean single) {
    ClosestPair closest = null;

    if (threadPool == null) {
      closest = (single)
          ? findClosestParticleDistancePriority(grid, nxbins, nybins, r2, time, 0, nxbins, 0,
              nybins)
          : findClosestDistancePriority(grid, nxbins, nybins, r2, time, 0, nxbins, 0, nybins);
    } else {
      // Use threads to find the closest pairs in blocks
      final List<Future<?>> futures = new LinkedList<>();
      final List<ClosestPair> results = new LinkedList<>();

      for (int startybin = 0; startybin < nybins; startybin += yblock) {
        final int endybin = FastMath.min(nybins, startybin + yblock);
        for (int startxbin = 0; startxbin < nxbins; startxbin += xblock) {
          final int endxbin = FastMath.min(nxbins, startxbin + xblock);

          final ClosestPair pair = new ClosestPair();
          results.add(pair);
          futures.add(threadPool.submit(new ClosestPriorityWorker(false, pair, grid, nxbins, nybins,
              r2, time, startxbin, endxbin, startybin, endybin, single)));
        }
      }

      // Finish processing data
      ConcurrencyUtils.waitForCompletionOrError(futures);
      futures.clear();

      // Find the closest pair from all the results
      for (final ClosestPair result : results) {
        if (result.point1 != null && (closest == null || (result.distance < closest.distance)
            || (result.distance <= closest.distance && result.time < closest.time))) {
          closest = result;
        }
      }
    }

    // Join the closest pair
    if (closest != null) {
      final TimeCluster pair1 = (TimeCluster) closest.point1;
      final TimeCluster pair2 = (TimeCluster) closest.point2;

      pair2.add(pair1);

      remove(grid, pair1);

      // Reassign the updated grid position
      final int xbin = (int) ((pair2.getX() - minx) / xbinWidth);
      final int ybin = (int) ((pair2.getY() - miny) / ybinWidth);

      if (xbin != pair2.getXBin() || ybin != pair2.getYBin()) {
        remove(grid, pair2);

        // Build a single linked list
        pair2.setXBin(xbin);
        pair2.setYBin(ybin);
        pair2.setNext(grid[xbin][ybin]);
        grid[xbin][ybin] = pair2;
      }

      return true;
    }

    return false;
  }

  /**
   * Search for the closest pair of clusters that are below the squared radius distance. Find the
   * closest in distance and then time.
   *
   * @param grid the grid
   * @param nxbins the n X bins
   * @param nybins the n Y bins
   * @param r2 The squared radius distance
   * @param time the time
   * @param startxbin the start X bin
   * @param endxbin the end X bin
   * @param startybin the start Y bin
   * @param endybin the end Y bin
   * @return True if a join was made
   */
  private ClosestPair findClosestDistancePriority(TimeCluster[][] grid, final int nxbins,
      final int nybins, final double r2, final int time, int startxbin, int endxbin, int startybin,
      int endybin) {
    double minD = Double.POSITIVE_INFINITY;
    int minT = Integer.MAX_VALUE;
    TimeCluster pair1 = null;
    TimeCluster pair2 = null;
    final TimeCluster[] neighbourCells = new TimeCluster[5];
    final boolean checkPulseInterval = pulseInterval > 0;
    for (int ybin = startybin; ybin < endybin; ybin++) {
      for (int xbin = startxbin; xbin < endxbin; xbin++) {
        for (TimeCluster c1 = grid[xbin][ybin]; c1 != null; c1 = (TimeCluster) c1.getNext()) {
          // Build a list of which cells to compare up to a maximum of 4
          //@formatter:off
          //      | 0,0 | 1,0
          // ------------+-----
          // -1,1 | 0,1 | 1,1
          //@formatter:on

          // Compare to neighbours and find the closest.
          // Use either the radius threshold or the current closest distance
          // which may have been set by an earlier comparison.
          TimeCluster other = null;
          int cells = 1;
          neighbourCells[0] = (TimeCluster) c1.getNext();
          if (ybin < nybins - 1) {
            neighbourCells[cells++] = grid[xbin][ybin + 1];
            if (xbin > 0) {
              neighbourCells[cells++] = grid[xbin - 1][ybin + 1];
            }
          }
          if (xbin < nxbins - 1) {
            neighbourCells[cells++] = grid[xbin + 1][ybin];
            if (ybin < nybins - 1) {
              neighbourCells[cells++] = grid[xbin + 1][ybin + 1];
            }
          }

          for (int c = 0; c < cells; c++) {
            for (TimeCluster c2 = neighbourCells[c]; c2 != null; c2 = (TimeCluster) c2.getNext()) {
              if (checkPulseInterval && c1.getPulseTime() != c2.getPulseTime()) {
                continue;
              }

              final int gap = c1.gap(c2);
              if (gap <= time) {
                final double d2 = c1.distance2(c2);
                if (d2 <= r2) {
                  // Check if the two clusters can be merged
                  if (invalidUnion(gap, c1, c2)) {
                    continue;
                  }

                  // This is within the time and distance thresholds.
                  // Find closest pair with distance priority
                  if ((d2 < minD) || (d2 <= minD && gap < minT)) {
                    minD = d2;
                    minT = gap;
                    other = c2;
                  }
                }
              }
            }
          }

          // Store the details of the closest pair
          if (other != null) {
            pair1 = c1;
            pair2 = other;
          }
        }
      }
    }
    if (pair1 != null) {
      return new ClosestPair(minD, minT, pair1, pair2);
    }
    return null;
  }

  /**
   * Search for the closest pair of a single particle and any existing single/cluster that are below
   * the squared radius distance. Find the closest in distance and then time.
   *
   * @param grid the grid
   * @param nxbins the n X bins
   * @param nybins the n Y bins
   * @param r2 The squared radius distance
   * @param time the time
   * @param startxbin the start X bin
   * @param endxbin the end X bin
   * @param startybin the start Y bin
   * @param endybin the end Y bin
   * @return True if a join was made
   */
  private ClosestPair findClosestParticleDistancePriority(TimeCluster[][] grid, final int nxbins,
      final int nybins, final double r2, final int time, int startxbin, int endxbin, int startybin,
      int endybin) {
    double minD = Double.POSITIVE_INFINITY;
    int minT = Integer.MAX_VALUE;
    TimeCluster pair1 = null;
    TimeCluster pair2 = null;
    final TimeCluster[] neighbourCells = new TimeCluster[5];
    final boolean checkPulseInterval = pulseInterval > 0;
    for (int ybin = startybin; ybin < endybin; ybin++) {
      for (int xbin = startxbin; xbin < endxbin; xbin++) {
        for (TimeCluster c1 = grid[xbin][ybin]; c1 != null; c1 = (TimeCluster) c1.getNext()) {
          final boolean cluster1 = c1.getSize() > 1;

          // Build a list of which cells to compare up to a maximum of 4
          //@formatter:off
          //      | 0,0 | 1,0
          // ------------+-----
          // -1,1 | 0,1 | 1,1
          //@formatter:on

          // Compare to neighbours and find the closest.
          // Use either the radius threshold or the current closest distance
          // which may have been set by an earlier comparison.
          TimeCluster other = null;
          int cells = 1;
          neighbourCells[0] = (TimeCluster) c1.getNext();
          if (ybin < nybins - 1) {
            neighbourCells[cells++] = grid[xbin][ybin + 1];
            if (xbin > 0) {
              neighbourCells[cells++] = grid[xbin - 1][ybin + 1];
            }
          }
          if (xbin < nxbins - 1) {
            neighbourCells[cells++] = grid[xbin + 1][ybin];
            if (ybin < nybins - 1) {
              neighbourCells[cells++] = grid[xbin + 1][ybin + 1];
            }
          }

          for (int c = 0; c < cells; c++) {
            for (TimeCluster c2 = neighbourCells[c]; c2 != null; c2 = (TimeCluster) c2.getNext()) {
              if (cluster1 && c2.getSize() > 1) {
                continue;
              }
              if (checkPulseInterval && c1.getPulseTime() != c2.getPulseTime()) {
                continue;
              }

              final int gap = c1.gap(c2);
              if (gap <= time) {
                final double d2 = c1.distance2(c2);
                if (d2 <= r2) {
                  // Check if the two clusters can be merged
                  if (invalidUnion(gap, c1, c2)) {
                    continue;
                  }

                  // This is within the time and distance thresholds.
                  // Find closest pair with distance priority
                  if ((d2 < minD) || (d2 <= minD && gap < minT)) {
                    minD = d2;
                    minT = gap;
                    other = c2;
                  }
                }
              }
            }
          }

          // Store the details of the closest pair
          if (other != null) {
            pair1 = c1;
            pair2 = other;
          }
        }
      }
    }
    if (pair1 != null) {
      return new ClosestPair(minD, minT, pair1, pair2);
    }
    return null;
  }

  /**
   * Gets the clustering algorithm.
   *
   * @return the clustering algorithm
   */
  public ClusteringAlgorithm getClusteringAlgorithm() {
    return clusteringAlgorithm;
  }

  /**
   * Sets the clustering algorithm.
   *
   * @param clusteringAlgorithm The algorithm
   */
  public void setClusteringAlgorithm(ClusteringAlgorithm clusteringAlgorithm) {
    this.clusteringAlgorithm = clusteringAlgorithm;
  }

  /**
   * Gets the tracker.
   *
   * @return the tracker
   */
  public TrackProgress getTracker() {
    return tracker;
  }

  /**
   * Sets the tracker.
   *
   * @param tracker the tracker to set
   */
  public void setTracker(TrackProgress tracker) {
    this.tracker = NullTrackProgress.createIfNull(tracker);
  }

  /**
   * Gets the pulse interval.
   *
   * @return the pulse interval
   */
  public int getPulseInterval() {
    return pulseInterval;
  }

  /**
   * Set a pulse interval. Clusters will only be created by joining localisations within each pulse.
   * Pulses are assumed to start at t=1.
   *
   * <p>This only applies to the algorithms that use time and distance thresholds.
   *
   * @param pulseInterval the pulse interval
   */
  public void setPulseInterval(int pulseInterval) {
    this.pulseInterval = FastMath.max(0, pulseInterval);
  }

  /**
   * Get the pulse for the specified time. Assumes pulses start at t=1. Returns zero if no pulse
   * interval is defined.
   *
   * @param time the time
   * @return the pulse
   */
  public int getPulse(int time) {
    if (pulseInterval == 0) {
      return 0;
    }
    return ((time - 1) / pulseInterval);
  }

  /**
   * Checks if recording the distances between particles that were joined.
   *
   * @return true if recording the distances between particles that were joined.
   */
  public boolean isTrackJoins() {
    return trackJoins;
  }

  /**
   * Set to true to record the distances between particles that were joined. Only applies to the
   * particle linkage algorithm. The distances can be retrieved after the
   * {@link #findClusters(List, double)} method has been called.
   *
   * @param trackJoins the new track joins
   */
  public void setTrackJoins(boolean trackJoins) {
    this.trackJoins = trackJoins;
  }

  /**
   * Gets the intra id distances.
   *
   * @return the intra-Id distances from joins in the particle linkage algorithm
   */
  public double[] getIntraIdDistances() {
    return intraIdDistances;
  }

  /**
   * Gets the inter id distances.
   *
   * @return the inter-Id distances from joins in the particle linkage algorithm
   */
  public double[] getInterIdDistances() {
    return interIdDistances;
  }

  /**
   * Gets the thread count for multi-thread compatible algorithms.
   *
   * @return the thread count for multi-thread compatible algorithms
   */
  public int getThreadCount() {
    return threadCount;
  }

  /**
   * Sets the thread count for multi-thread compatible algorithms.
   *
   * @param threadCount the thread count for multi-thread compatible algorithms
   */
  public void setThreadCount(int threadCount) {
    this.threadCount = threadCount;
  }
}
