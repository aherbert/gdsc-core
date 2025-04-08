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
 * Copyright (C) 2011 - 2025 Alex Herbert
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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import uk.ac.sussex.gdsc.core.trees.FloatDistanceFunction;
import uk.ac.sussex.gdsc.core.trees.FloatDistanceFunctions;
import uk.ac.sussex.gdsc.core.trees.IntFloatKdTree;
import uk.ac.sussex.gdsc.core.trees.KdTrees;
import uk.ac.sussex.gdsc.core.utils.LocalList;
import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.core.utils.concurrent.ConcurrencyUtils;
import uk.ac.sussex.gdsc.core.utils.function.IntDoubleConsumer;

/**
 * LoOP: Local Outlier Probabilities.
 *
 * <p>Distance/density based algorithm similar to Local Outlier Factor (LOF) to detect outliers, but
 * with statistical methods to achieve better result stability and scaling results to the range
 * [0:1].
 *
 * <p>Reference:
 *
 * <p>Hans-Peter Kriegel, Peer Kröger, Erich Schubert, Arthur Zimek:<br> LoOP: Local Outlier
 * Probabilities<br> In Proceedings of the 18th International Conference on Information and
 * Knowledge Management (CIKM), Hong Kong, China, 2009.
 */
public class LoOp {

  /** The executor service. */
  private ExecutorService executorService;

  /**
   * The KD tree used to store the points for an efficient neighbour search.
   *
   * <p>Package private to allow access from worker inner classes.
   */
  final IntFloatKdTree tree;

  /** The distance function for the number of dimensions. */
  final FloatDistanceFunction distanceFunction;

  /**
   * The points.
   *
   * <p>Package private to allow access from worker inner classes.
   */
  final float[][] points;

  /**
   * Class to store the nearest neighbours of each point.
   */
  private static class KnnStore implements IntDoubleConsumer {

    /** The neighbours. */
    final int[][] neighbours;

    /** The size of the current list of neighbours. */
    int size;

    /** This is a reference to the neighbours at the current index. */
    int[] neighboursAtIndex;

    /** Sum-of-squared distances for the current neighbours. */
    double sumDistances;

    KnnStore(int[][] neighbours) {
      this.neighbours = neighbours;
    }

    @Override
    public void accept(int neighbour, double distance) {
      // Assume results are in sorted order, largest distance first.
      // Ignore the final neighbour as it will be the search point.
      if (size == neighboursAtIndex.length) {
        // Ignore self
        return;
      }
      sumDistances += distance;
      neighboursAtIndex[size++] = neighbour;
    }

    /**
     * Reset for processing the specified index.
     *
     * @param index the index
     */
    void reset(int index) {
      sumDistances = 0;
      size = 0;
      neighboursAtIndex = neighbours[index];
    }
  }

  /**
   * Class to count the nearest neighbours of each point.
   */
  private class KnnWorker implements Runnable {
    final int numberOfNeigbours;
    final double[] pd;
    final int from;
    final int to;
    final KnnStore store;

    KnnWorker(int[][] neighbours, int numberOfNeigbours, double[] pd, int from, int to) {
      this.numberOfNeigbours = numberOfNeigbours;
      this.pd = pd;
      this.from = from;
      this.to = to;
      store = new KnnStore(neighbours);
    }

    @Override
    public void run() {
      // Note: The numberOfNeigbours-nearest neighbour search will include the actual
      // point so increment by 1
      final int k1 = numberOfNeigbours + 1;
      final double[] search = new double[tree.dimensions()];
      for (int i = from; i < to; i++) {
        store.reset(i);
        for (int j = 0; j < search.length; j++) {
          search[j] = points[i][j];
        }
        tree.nearestNeighbours(search, k1, true, distanceFunction, store);
        pd[i] = Math.sqrt(store.sumDistances / numberOfNeigbours);
      }
    }
  }

  /**
   * Class to compute the Probabilistic Local Outlier Factors (PLOF).
   */
  private static class PlofWorker implements Runnable {
    final int[][] neighbours;
    final int numberOfNeigbours;
    final double[] pd;
    final double[] plofs;
    final int from;
    final int to;
    double nplof;

    PlofWorker(int[][] neighbours, int numberOfNeigbours, double[] pd, double[] plofs, int from,
        int to) {
      this.neighbours = neighbours;
      this.numberOfNeigbours = numberOfNeigbours;
      this.pd = pd;
      this.plofs = plofs;
      this.from = from;
      this.to = to;
    }

    @Override
    public void run() {
      for (int i = from; i < to; i++) {
        double sum = 0;
        final int[] list = neighbours[i];
        for (int j = numberOfNeigbours; j-- > 0;) {
          sum += pd[list[j]];
        }
        double plof = (sum == 0) ? 1 : max(pd[i] * numberOfNeigbours / sum, 1.0);
        if (Double.isFinite(plof)) {
          nplof += MathUtils.pow2(plof - 1.0);
        } else {
          plof = 1.0;
        }
        plofs[i] = plof;
      }
    }

    private static double max(double value1, double value2) {
      return value1 >= value2 ? value1 : value2;
    }
  }

  /**
   * Class to normalise the Probabilistic Local Outlier Factors (PLOF).
   */
  private static class NormWorker implements Runnable {
    final double[] plofs;
    final double norm;
    final int from;
    final int to;

    NormWorker(double[] plofs, double norm, int from, int to) {
      this.plofs = plofs;
      this.norm = norm;
      this.from = from;
      this.to = to;
    }

    @Override
    public void run() {
      for (int i = from; i < to; i++) {
        // Use an approximation for speed
        plofs[i] = MathUtils.erf((plofs[i] - 1.0) * norm);
      }
    }
  }

  /**
   * Create a new instance for 2D.
   *
   * @param x the x
   * @param y the y
   */
  public LoOp(float[] x, float[] y) {
    points = new float[x.length][];
    tree = KdTrees.newIntFloatKdTree(2);
    distanceFunction = FloatDistanceFunctions.SQUARED_EUCLIDEAN_2D;
    for (int i = 0; i < x.length; i++) {
      points[i] = new float[] {x[i], y[i]};
      tree.add(points[i], i);
    }
  }

  /**
   * Create a new instance for 3D.
   *
   * @param x the x
   * @param y the y
   * @param z the z
   */
  public LoOp(float[] x, float[] y, float[] z) {
    points = new float[x.length][];
    tree = KdTrees.newIntFloatKdTree(3);
    distanceFunction = FloatDistanceFunctions.SQUARED_EUCLIDEAN_3D;
    for (int i = 0; i < x.length; i++) {
      points[i] = new float[] {x[i], y[i], z[i]};
      tree.add(points[i], i);
    }
  }

  /**
   * Run the Local Outlier Probability computation using the given number of neighbours.
   *
   * @param numberOfNeighbours the number of neighbours (excluding self)
   * @param lambda The number of standard deviations to consider for density computation.
   * @return the LoOP scores
   * @throws InterruptedException if the current thread was interrupted while waiting
   * @throws ExecutionException if the computation threw an exception
   */
  public double[] run(int numberOfNeighbours, double lambda)
      throws InterruptedException, ExecutionException {
    final int size = size();

    // Bounds check the neighbour count (k)
    final int neighbourCount = MathUtils.clip(1, size - 1, numberOfNeighbours);

    // Multi-thread
    final int threadCount = Runtime.getRuntime().availableProcessors();
    ExecutorService executor = getExecutorService();
    boolean shutdown = false;
    if (executor == null) {
      executor = Executors.newSingleThreadExecutor();
      shutdown = true;
    }
    final LocalList<Future<?>> futures = new LocalList<>(threadCount);
    final int nPerThread = (int) Math.ceil((double) size / threadCount);

    // Find neighbours for each point and
    // compute probabilistic distances
    final int[][] neighbours = new int[size][neighbourCount];
    final double[] pd = new double[size];

    for (int from = 0; from < size;) {
      final int to = Math.min(from + nPerThread, size);
      futures.add(executor.submit(new KnnWorker(neighbours, neighbourCount, pd, from, to)));
      from = to;
    }
    wait(futures);

    // Compute Probabilistic Local Outlier Factors (PLOF)
    final double[] plofs = new double[size];
    final LocalList<PlofWorker> workers = new LocalList<>(threadCount);
    for (int from = 0; from < size;) {
      final int to = Math.min(from + nPerThread, size);
      final PlofWorker w = new PlofWorker(neighbours, neighbourCount, pd, plofs, from, to);
      workers.add(w);
      futures.add(executor.submit(w));
      from = to;
    }
    wait(futures);

    // Get the final normalisation factor
    double nplof = 0;
    for (final PlofWorker w : workers) {
      nplof += w.nplof;
    }
    nplof = lambda * Math.sqrt(nplof / size);
    if (nplof <= 0) {
      nplof = 1;
    }

    // Normalise
    final double norm = 1. / (nplof * Math.sqrt(2.));
    for (int from = 0; from < size;) {
      final int to = Math.min(from + nPerThread, size);
      futures.add(executor.submit(new NormWorker(plofs, norm, from, to)));
      from = to;
    }
    wait(futures);

    if (shutdown) {
      executor.shutdown();
    }

    return plofs;
  }

  private static void wait(LocalList<Future<?>> futures)
      throws InterruptedException, ExecutionException {
    ConcurrencyUtils.waitForCompletion(futures);
    futures.clear();
  }

  /**
   * Get the number of points.
   *
   * @return the number of points
   */
  public int size() {
    return points.length;
  }

  /**
   * Sets the executor service used for multi-threading.
   *
   * @param executorService the new executor service
   */
  public void setExecutorService(ExecutorService executorService) {
    this.executorService = executorService;
  }

  /**
   * Gets the executor service. This is null if the service has not been set or a previous service
   * has been shutdown.
   *
   * @return the executor service
   */
  public ExecutorService getExecutorService() {
    ExecutorService service = executorService;
    if (service != null && service.isShutdown()) {
      service = executorService = null;
    }
    return service;
  }
}
