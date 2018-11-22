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

import uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen2.SimpleFloatKdTree2D;
import uk.ac.sussex.gdsc.core.clustering.CoordinateStore;
import uk.ac.sussex.gdsc.core.data.VisibleForTesting;
import uk.ac.sussex.gdsc.core.logging.TrackProgress;
import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.core.utils.TextUtils;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.PermutationSampler;
import org.apache.commons.rng.simple.RandomSource;

import java.awt.Rectangle;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Compute clustering using OPTICS.
 *
 * <p>This is an implementation of the OPTICS method. Mihael Ankerst, Markus M Breunig, Hans-Peter
 * Kriegel, and Jorg Sander. Optics: ordering points to identify the clustering structure. In ACM
 * Sigmod Record, volume 28, pages 49–60. ACM, 1999.
 */
public class OpticsManager extends CoordinateStore {
  /**
   * The UNDEFINED distance in the OPTICS algorithm. This is actually arbitrary as long as it is not
   * a real distance. Use a simple value so we can spot it when debugging.
   */
  static final float UNDEFINED = -1;

  /** The tracker. */
  private TrackProgress tracker;

  /** The options. */
  private EnumSet<Option> options = EnumSet.noneOf(Option.class);

  /** The class to compute local outlier probability. */
  private LoOp loOp;

  /** The number of threads. */
  private int numberOfThreads;

  /** The seed for random algorithms. */
  private long seed;

  /**
   * The grid. Package level for JUnit testing
   */
  MoleculeSpace grid;

  /** The heap for storing the top n distances. */
  private FloatHeap heap;

  /** The executor service used for FastOPTICS. */
  private ExecutorService executorService;

  /**
   * The KD tree used to store the points for an efficient neighbour search.
   *
   * <p>This can be cached as it is initialised with the current coordinates (that should not
   * change).
   */
  private SimpleFloatKdTree2D tree;

  /**
   * Options for the algorithms.
   */
  public enum Option {
    /**
     * Flag to indicate that memory structures should be cached. Set this when it is intended to
     * call multiple clustering methods.
     */
    CACHE,
    /**
     * Flag to indicate that a circular mask should be used on the 2D grid. This has performance
     * benefits since some distance computations can be avoided.
     *
     * <p>Note: This option is experimental and can cause slow-down
     */
    CIRCULAR_PROCESSING,
    /**
     * Flag to indicate that inner-circle processing should be used on the 2D grid. This has
     * performance benefits for DBSCAN since some distance computations can be assumed.
     *
     * <p>Note: This option is experimental and can cause slow-down
     */
    INNER_PROCESSING,
    /**
     * Flag to indicate that processing should use a 2D grid.
     *
     * <p>Note: If no options are provided then the memory structure will be chosen automatically.
     */
    GRID_PROCESSING,
    /**
     * Flag to indicate that OPTICS should sort objects using their input order (id) if the
     * reachability distance is equal.
     *
     * <p>Omitting this flag defaults to sorting objects using their reachability distance. If the
     * reachability distance is the same then the order will be dependent on the type of priority
     * queue, i.e. is non-deterministic.
     */
    OPTICS_STRICT_ID_ORDER,
    /**
     * Flag to indicate that OPTICS should sort objects using their reverse input order (id) if the
     * reachability distance is equal.
     *
     * <p>Omitting this flag defaults to sorting objects using their reachability distance. If the
     * reachability distance is the same then the order will be dependent on the type of priority
     * queue, i.e. is non-deterministic.
     *
     * <p>Note: This option matches the implementation in the ELKI framework.
     */
    OPTICS_STRICT_REVERSE_ID_ORDER,
    /**
     * Flag to indicate that OPTICS should sort objects using a simple priority queue. The default
     * is a binary heap.
     */
    OPTICS_SIMPLE_PRIORITY_QUEUE;
  }

  /**
   * Used in the DBSCAN algorithm to store a queue of molecules to process.
   */
  private static class MoleculeQueue extends MoleculeList {
    int next;

    MoleculeQueue(int capacity) {
      super(capacity);
    }

    void push(Molecule molecule) {
      add(molecule);
    }

    @Override
    void clear() {
      size = next = 0;
    }

    boolean hasNext() {
      return next < size;
    }

    Molecule getNext() {
      return list[next++];
    }
  }

  /**
   * Counter used in DBSCAN.
   */
  private class Counter {
    int next;
    int progress;
    int total;

    Counter(int total) {
      this.total = total;
    }

    int nextClusterId() {
      return ++next;
    }

    /**
     * Increment the counter and check if processing should stop.
     *
     * @return true, if processing should stop
     */
    boolean increment() {
      if (tracker != null) {
        tracker.progress(++progress, total);
        return tracker.isEnded();
      }
      return false;
    }

    int getTotalClusters() {
      return next;
    }
  }

  /**
   * Interface for the OPTICS priority queue. Molecules should be ordered by their reachability
   * distance.
   */
  private interface OpticsPriorityQueue {
    /**
     * Push the molecule to the queue and move up.
     *
     * @param molecule the molecule
     */
    void push(Molecule molecule);

    /**
     * Move the molecule up the queue (since the reachability distance has changed).
     *
     * @param molecule the molecule
     */
    void moveUp(Molecule molecule);

    /**
     * Checks for next.
     *
     * @return true, if successful
     */
    boolean hasNext();

    /**
     * Get the next molecule.
     *
     * @return the molecule
     */
    Molecule next();

    /**
     * Clear the queue.
     */
    void clear();
  }

  /**
   * Used in the OPTICS algorithm to store the next seed is a priority queue.
   */
  private class OpticsMoleculePriorityQueue extends MoleculeList implements OpticsPriorityQueue {
    int next;

    OpticsMoleculePriorityQueue(int capacity) {
      super(capacity);
    }

    @Override
    public void push(Molecule molecule) {
      set(molecule, size++);
      moveUp(molecule);
    }

    void set(Molecule molecule, int index) {
      list[index] = molecule;
      molecule.setQueueIndex(index);
    }

    @Override
    public void moveUp(Molecule object) {
      if (lower(object, list[next])) {
        swap(next, object.getQueueIndex());
      }
    }

    @Override
    public boolean hasNext() {
      return next < size;
    }

    @Override
    public Molecule next() {
      final Molecule molecule = list[next++];
      if (hasNext()) {
        // Find the next lowest molecule
        int lowest = next;
        for (int i = next + 1; i < size; i++) {
          if (lower(list[i], list[lowest])) {
            lowest = i;
          }
        }
        swap(next, lowest);
      }
      return molecule;
    }

    void swap(int index1, int index2) {
      final Molecule molecule = list[index1];
      set(list[index2], index1);
      set(molecule, index2);
    }

    @Override
    public void clear() {
      size = next = 0;
    }

    boolean lower(Molecule m1, Molecule m2) {
      return m1.reachabilityDistance < m2.reachabilityDistance;
    }
  }

  /**
   * Used in the OPTICS algorithm to store the next seed is a priority queue.
   *
   * <p>If distances are equal then IDs are used to sort the objects in order.
   */
  private class OpticsMoleculePriorityQueueIdOrdered extends OpticsMoleculePriorityQueue {
    OpticsMoleculePriorityQueueIdOrdered(int capacity) {
      super(capacity);
    }

    @Override
    boolean lower(Molecule m1, Molecule m2) {
      if (m1.reachabilityDistance < m2.reachabilityDistance) {
        return true;
      }
      if (m1.reachabilityDistance > m2.reachabilityDistance) {
        return false;
      }
      return m1.id < m2.id;
    }
  }

  /**
   * Used in the OPTICS algorithm to store the next seed is a priority queue.
   *
   * <p>If distances are equal then IDs are used to sort the objects in reverse order.
   */
  private class OpticsMoleculePriorityQueueReverseIdOrdered extends OpticsMoleculePriorityQueue {
    OpticsMoleculePriorityQueueReverseIdOrdered(int capacity) {
      super(capacity);
    }

    @Override
    boolean lower(Molecule m1, Molecule m2) {
      if (m1.reachabilityDistance < m2.reachabilityDistance) {
        return true;
      }
      if (m1.reachabilityDistance > m2.reachabilityDistance) {
        return false;
      }
      return m1.id > m2.id;
    }
  }

  /**
   * An implementation of a binary heap respecting minimum order.
   *
   * <p>This class is based on ags.utils.dataStructures.BinaryHeap
   */
  private class OpticsMoleculeBinaryHeap extends MoleculeList implements OpticsPriorityQueue {
    OpticsMoleculeBinaryHeap(int capacity) {
      super(capacity);
    }

    @Override
    public void push(Molecule molecule) {
      set(molecule, size++);
      moveUp(molecule);
    }

    void set(Molecule molecule, int index) {
      list[index] = molecule;
      molecule.setQueueIndex(index);
    }

    @Override
    public boolean hasNext() {
      return size != 0;
    }

    @Override
    public Molecule next() {
      final Molecule m = list[0];
      set(list[--size], 0);
      siftDown(0);
      return m;
    }

    @Override
    public void moveUp(Molecule object) {
      siftUp(object.getQueueIndex());
    }

    void siftUp(int index) {
      // Remove unnecessary loop set-up statements, i.e. where p is not needed
      int child = index;
      while (child > 0) {
        final int parent = (child - 1) >>> 1;
        if (lower(list[child], list[parent])) {
          swap(parent, child);
          child = parent;
        } else {
          break;
        }
      }
    }

    void swap(int index1, int index2) {
      final Molecule m = list[index1];
      set(list[index2], index1);
      set(m, index2);
    }

    void siftDown(int index) {
      for (int parent = index, child = parent * 2 + 1; child < size; parent = child, child =
          parent * 2 + 1) {
        if (child + 1 < size && higher(list[child], list[child + 1])) {
          child++;
        }
        if (higher(list[parent], list[child])) {
          swap(parent, child);
        } else {
          break;
        }
      }
    }

    @Override
    public void clear() {
      // Make public the inherited method to match the OpticsPriorityQueue interface
      super.clear();
    }

    /**
     * Check if molecule 1 has a lower reachability distance than molecule 2.
     *
     * @param molecule1 the molecule 1
     * @param molecule2 the molecule 2
     * @return true, if successful
     */
    boolean lower(Molecule molecule1, Molecule molecule2) {
      return molecule1.reachabilityDistance < molecule2.reachabilityDistance;
    }

    /**
     * Check if molecule 1 has a higher reachability distance than molecule 2.
     *
     * @param molecule1 the molecule 1
     * @param molecule2 the molecule 2
     * @return true, if successful
     */
    boolean higher(Molecule molecule1, Molecule molecule2) {
      return molecule1.reachabilityDistance > molecule2.reachabilityDistance;
    }
  }

  /**
   * An implementation of a binary heap respecting minimum order.
   *
   * <p>If distances are equal then IDs are used to sort the objects in order.
   */
  private class OpticsMoleculeBinaryHeapIdOrdered extends OpticsMoleculeBinaryHeap {
    OpticsMoleculeBinaryHeapIdOrdered(int capacity) {
      super(capacity);
    }

    @Override
    boolean lower(Molecule m1, Molecule m2) {
      if (m1.reachabilityDistance < m2.reachabilityDistance) {
        return true;
      }
      if (m1.reachabilityDistance > m2.reachabilityDistance) {
        return false;
      }
      return m1.id < m2.id;
    }

    @Override
    boolean higher(Molecule m1, Molecule m2) {
      if (m1.reachabilityDistance > m2.reachabilityDistance) {
        return true;
      }
      if (m1.reachabilityDistance < m2.reachabilityDistance) {
        return false;
      }
      return m1.id > m2.id;
    }
  }

  /**
   * An implementation of a binary heap respecting minimum order.
   *
   * <p>If distances are equal then IDs are used to sort the objects in reverse order.
   */
  private class OpticsMoleculeBinaryHeapReverseIdOrdered extends OpticsMoleculeBinaryHeap {
    OpticsMoleculeBinaryHeapReverseIdOrdered(int capacity) {
      super(capacity);
    }

    @Override
    boolean lower(Molecule m1, Molecule m2) {
      if (m1.reachabilityDistance < m2.reachabilityDistance) {
        return true;
      }
      if (m1.reachabilityDistance > m2.reachabilityDistance) {
        return false;
      }
      return m1.id > m2.id;
    }

    @Override
    boolean higher(Molecule m1, Molecule m2) {
      if (m1.reachabilityDistance > m2.reachabilityDistance) {
        return true;
      }
      if (m1.reachabilityDistance < m2.reachabilityDistance) {
        return false;
      }
      return m1.id < m2.id;
    }
  }

  /**
   * Used in the OPTICS algorithm to store the output results.
   */
  private class OpticsResultList {
    final OpticsOrder[] list;
    int size;

    OpticsResultList(int capacity) {
      list = new OpticsOrder[capacity];
    }

    /**
     * Adds the molecule to the results. Send progress to the tracker and checks for a shutdown
     * signal.
     *
     * @param molecule the molecule
     * @return true, if a shutdown signal has been received
     */
    boolean add(Molecule molecule) {
      list[size++] = molecule.toOpticsResult();
      if (tracker != null) {
        tracker.progress(size, list.length);
        return tracker.isEnded();
      }
      return false;
    }
  }

  /**
   * An optimised heap structure for selecting the top n values.
   */
  private static class FloatHeap {
    /**
     * The number N to select, i.e. the size.
     */
    final int size;
    /**
     * Working storage.
     */
    private final float[] queue;

    /**
     * Instantiates a new heap.
     *
     * @param size the size
     */
    private FloatHeap(int size) {
      ValidationUtils.checkArgument(size > 0, "N must be strictly positive");
      this.queue = new float[size];
      this.size = size;
    }

    /**
     * Add the first value to the heap.
     *
     * @param value the value
     */
    private void start(float value) {
      queue[0] = value;
    }

    /**
     * Put the next value into the heap. This method is used to fill the heap from {@code i=1} to
     * {@code i<n}.
     *
     * @param i the index
     * @param value the value
     */
    private void put(int index, float value) {
      queue[index] = value;
      upHeapify(index);
    }

    /**
     * Push a value onto a full heap. This method is used to add more values to a full heap.
     *
     * @param value the value
     */
    private void push(float value) {
      if (queue[0] > value) {
        queue[0] = value;
        downHeapify(0);
      }
    }

    private float getMaxValue() {
      return queue[0];
    }

    private void upHeapify(int index) {
      int child = index;
      while (child > 0) {
        final int parent = (child - 1) >>> 1;
        if (queue[child] > queue[parent]) {
          final float pDist = queue[parent];
          queue[parent] = queue[child];
          queue[child] = pDist;
          child = parent;
        } else {
          break;
        }
      }
    }

    private void downHeapify(int index) {
      for (int parent = index, child = parent * 2 + 1; child < size; parent = child, child =
          parent * 2 + 1) {
        if (child + 1 < size && queue[child] < queue[child + 1]) {
          child++;
        }
        if (queue[parent] < queue[child]) {
          // Swap the points
          final float pDist = queue[parent];
          queue[parent] = queue[child];
          queue[child] = pDist;
        } else {
          break;
        }
      }
    }
  }

  /**
   * Input arrays are modified.
   *
   * @param xcoord the xcoord
   * @param ycoord the ycoord
   * @param bounds the bounds
   * @throws IllegalArgumentException if results are null or empty
   */
  public OpticsManager(float[] xcoord, float[] ycoord, Rectangle bounds) {
    super(xcoord, ycoord, bounds);
  }

  /**
   * Instantiates a new optics manager.
   *
   * @param source the source
   * @param deepCopy Set to true to copy the coordinate arrays
   */
  private OpticsManager(OpticsManager source, boolean deepCopy) {
    super(source, deepCopy);
    // Copy properties
    options = EnumSet.copyOf(source.options);
    numberOfThreads = source.numberOfThreads;
    seed = source.seed;
    // Do not copy the state used for algorithms:
    // tracker
    // loop
    // grid
    // heap
  }

  /**
   * Compute the core radius for each point to have n closest neighbours and the minimum
   * reachability distance of a point from another core point.
   *
   * <p>This is an implementation of the OPTICS method. Mihael Ankerst, Markus M Breunig, Hans-Peter
   * Kriegel, and Jorg Sander. Optics: ordering points to identify the clustering structure. In ACM
   * Sigmod Record, volume 28, pages 49–60. ACM, 1999.
   *
   * <p>The returned results are the output of {@link OpticsResult#extractDbscanClustering(float)}
   * with the configured generating distance. Note that the generating distance may have been
   * modified if invalid. If it is not strictly positive or not finite then it is set using
   * {@link #computeGeneratingDistance(int)}. If it is larger than the data range allows it is set
   * to the maximum distance that can be computed for the data range. If the data are colocated the
   * distance is set to 1. The distance is stored in the results.
   *
   * <p>This creates a large memory structure. It can be held in memory for re-use when using a
   * different number of min points. The tracker can be used to follow progress (see
   * {@link #setTracker(TrackProgress)}).
   *
   * @param generatingDistanceE the generating distance (E) (set to zero to auto calibrate)
   * @param minPoints the min points for a core object (recommended range around 4)
   * @return the results (or null if the algorithm was stopped using the tracker)
   */
  public OpticsResult optics(float generatingDistanceE, int minPoints) {
    final int minPts = Math.max(1, minPoints);

    long time = System.currentTimeMillis();
    initialiseOptics(generatingDistanceE, minPts);

    // The distance may be updated
    final float workingGeneratingDistance = grid.generatingDistanceE;

    if (tracker != null) {
      tracker.log("Running OPTICS ... Distance=%g, minPts=%d", workingGeneratingDistance, minPts);
      tracker.progress(0, xcoord.length);
    }

    // Note: The method and variable names used in this function are designed to match
    // the pseudocode implementation from the 1999 OPTICS paper.
    // The generating distance (E) used in the paper is the maximum distance at which cluster
    // centres will be formed. This implementation uses the squared distance to avoid sqrt()
    // function calls.
    final float e = workingGeneratingDistance * workingGeneratingDistance;

    final int size = xcoord.length;
    final Molecule[] setOfObjects = grid.setOfObjects;

    // Allow different queue implementations.
    // Note:
    // The ELKI code de.lmu.ifi.dbs.elki.algorithm.clustering.optics.OPTICSHeapEntry
    // Returns the opposite of an id comparison:
    // return -DBIDUtil.compare(objectID, o.objectID)
    // I do not know why this is but I have added it so the functionality
    // is identical in order to pass the JUnit tests
    final OpticsPriorityQueue orderSeeds = createQueue(size);

    final OpticsResultList results = new OpticsResultList(size);

    for (int i = 0; i < size; i++) {
      final Molecule object = setOfObjects[i];
      if (object.isNotProcessed()
          && opticsExpandClusterOrder(object, e, minPts, results, orderSeeds)) {
        break;
      }
    }

    boolean stopped = false;
    if (tracker != null) {
      stopped = tracker.isEnded();
      tracker.progress(1.0);

      if (stopped) {
        tracker.log("Aborted OPTICS");
      }
    }

    OpticsResult optics = null;
    if (!stopped) {
      optics = new OpticsResult(this, minPts, workingGeneratingDistance, results.list);
      final int nClusters = optics.extractDbscanClustering(workingGeneratingDistance);
      if (tracker != null) {
        time = System.currentTimeMillis() - time;
        tracker.log("Finished OPTICS: %d %s @ %s (Time = %s)", nClusters,
            pleuraliseClusterCount(nClusters), MathUtils.rounded(workingGeneratingDistance),
            TextUtils.millisToString(time));
      }
    }

    finish();

    return optics;
  }

  private static String pleuraliseClusterCount(int count) {
    return TextUtils.pleuralise(count, "Cluster", "Clusters");
  }

  private OpticsPriorityQueue createQueue(final int size) {
    OpticsPriorityQueue orderSeeds;
    if (options.contains(Option.OPTICS_SIMPLE_PRIORITY_QUEUE)) {
      if (options.contains(Option.OPTICS_STRICT_ID_ORDER)) {
        orderSeeds = new OpticsMoleculePriorityQueueIdOrdered(size);
      } else if (options.contains(Option.OPTICS_STRICT_REVERSE_ID_ORDER)) {
        orderSeeds = new OpticsMoleculePriorityQueueReverseIdOrdered(size);
      } else {
        orderSeeds = new OpticsMoleculePriorityQueue(size);
      }
    } else if (options.contains(Option.OPTICS_STRICT_ID_ORDER)) {
      orderSeeds = new OpticsMoleculeBinaryHeapIdOrdered(size);
    } else if (options.contains(Option.OPTICS_STRICT_REVERSE_ID_ORDER)) {
      orderSeeds = new OpticsMoleculeBinaryHeapReverseIdOrdered(size);
    } else {
      orderSeeds = new OpticsMoleculeBinaryHeap(size);
    }
    return orderSeeds;
  }

  /**
   * Initialise the memory structure for the OPTICS algorithm. This can be cached if the
   * generatingDistanceE does not change.
   *
   * @param generatingDistanceE the generating distance (E)
   * @param minPts the min points for a core object
   */
  private void initialiseOptics(float generatingDistanceE, int minPts) {
    // TODO - See if OPTICS can be made faster with a specialised MoleculeSpace.
    // For now this is disabled.
    // Class<?> clazz = getPreferredMoleculeSpace(true)

    Class<?> clazz = getPreferredMoleculeSpace(false);
    float workingGeneratingDistanceE = generatingDistanceE;
    if (clazz != null) {
      // Ensure the distance is valid
      workingGeneratingDistanceE = getWorkingGeneratingDistance(workingGeneratingDistanceE, minPts);

      // OPTICS will benefit from circular processing if the density is high.
      // This is because we can skip distance computation to molecules outside the circle.
      // This is roughly pi/4
      // Compute the expected number of molecules in the area.

      final double nMoleculesInCircle = getMoleculesInCircle(workingGeneratingDistanceE);

      // TODO - JUnit test to show when to use a circle to avoid distance comparisons.
      // We can miss 1 - pi/4 = 21% of the area.
      if (nMoleculesInCircle > RadialMoleculeSpace.N_MOLECULES_FOR_NEXT_RESOLUTION_OUTER) {
        clazz = RadialMoleculeSpace.class;
      }
    }
    initialise(workingGeneratingDistanceE, minPts, clazz);
  }

  private void initialiseDbscan(float generatingDistanceE, int minPts) {
    Class<?> clazz = getPreferredMoleculeSpace(true);
    float workingGeneratingDistanceE = generatingDistanceE;
    if (clazz != null) {
      // Ensure the distance is valid
      workingGeneratingDistanceE = getWorkingGeneratingDistance(generatingDistanceE, minPts);

      // DBSCAN will benefit from inner radial processing if the number of comparisons is high.
      // Compute the expected number of molecules in the area.

      final double nMoleculesInCircle = getMoleculesInCircle(workingGeneratingDistanceE);

      if (nMoleculesInCircle > RadialMoleculeSpace.N_MOLECULES_FOR_NEXT_RESOLUTION_INNER) {
        clazz = InnerRadialMoleculeSpace.class;
      }
    }
    initialise(workingGeneratingDistanceE, minPts, clazz);
  }

  /**
   * Gets the number of molecules in the circle defined by the generating distance. This assumes all
   * the molecules are uniformly spread across the bounding region of the coordinates.
   *
   * @param generatingDistanceE the generating distance E
   * @return the molecules in circle
   */
  private double getMoleculesInCircle(float generatingDistanceE) {
    final double xrange = computeDeltaOrOne(maxXCoord, minXCoord);
    final double yrange = computeDeltaOrOne(maxYCoord, minYCoord);
    final double area = xrange * yrange;
    final double nMoleculesInPixel = getSize() / area;
    return Math.PI * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;
  }

  /**
   * Compute the difference (delta) between the max and min or one.
   *
   * @param max the max
   * @param min the min
   * @return the double
   */
  private static double computeDeltaOrOne(float max, float min) {
    final double delta = max - min;
    // If the range is zero the points are colocated (or there is 1 point). Return 1 so the
    // area can be computed.
    return (delta != 0) ? delta : 1;
  }

  /**
   * Returned the preferred class for the molecule space using the options.
   *
   * @param allowNull Set to true to return null if no options are set
   * @return the preferred class for the molecule space
   */
  private Class<?> getPreferredMoleculeSpace(boolean allowNull) {
    if (options.contains(Option.CIRCULAR_PROCESSING)) {
      if (options.contains(Option.INNER_PROCESSING)) {
        return InnerRadialMoleculeSpace.class;
      }
      return RadialMoleculeSpace.class;
    }
    if (options.contains(Option.GRID_PROCESSING)) {
      return GridMoleculeSpace.class;
    }
    return (allowNull) ? null : GridMoleculeSpace.class;
  }

  /**
   * Initialise the memory structure for the OPTICS algorithm. This can be cached if the
   * generatingDistanceE does not change.
   *
   * @param generatingDistanceE the generating distance (E)
   * @param minPts the min points for a core object
   * @param clazz the preferred class for the molecule space
   */
  private void initialise(float generatingDistanceE1, int minPts, Class<?> clazz) {
    // Ensure the distance is valid
    final float safeGeneratingDistanceE =
        getWorkingGeneratingDistance(generatingDistanceE1, minPts);

    final Class<?> moleculeSpaceClass = (clazz == null) ? getPreferredMoleculeSpace(false) : clazz;

    // Compare to the existing grid
    if (grid == null || grid.generatingDistanceE != safeGeneratingDistanceE
        || grid.getClass() != moleculeSpaceClass) {
      if (tracker != null) {
        tracker.log("Initialising ...");
      }

      // Control the type of space we use to store the data
      if (moleculeSpaceClass == ProjectedMoleculeSpace.class) {
        grid = new ProjectedMoleculeSpace(this, safeGeneratingDistanceE, getRandomGenerator());
      } else if (moleculeSpaceClass == InnerRadialMoleculeSpace.class) {
        grid = new InnerRadialMoleculeSpace(this, safeGeneratingDistanceE);
      } else if (moleculeSpaceClass == RadialMoleculeSpace.class) {
        grid = new RadialMoleculeSpace(this, safeGeneratingDistanceE);
      } else {
        grid = new GridMoleculeSpace(this, safeGeneratingDistanceE);
      }

      grid.generate();
    } else {
      // This is the same distance so the objects can be reused
      grid.reset();
    }

    if (heap == null || heap.size != minPts) {
      heap = new FloatHeap(minPts);
    }
  }

  /**
   * Initialise the memory structure for the OPTICS algorithm. This can be cached if the
   * generatingDistanceE does not change.
   *
   * @param minPts the min points for a core object
   */
  private void initialiseFastOptics(int minPts) {
    initialise(0, minPts, ProjectedMoleculeSpace.class);
  }

  /**
   * Gets the working generating distance. Ensure the generating distance is not too high for the
   * data range. Also set it to the max value if the generating distance is not valid.
   *
   * @param generatingDistanceE the generating distance (E)
   * @param minPts the min points for a core object
   * @return the working generating distance
   */
  private float getWorkingGeneratingDistance(float generatingDistanceE, int minPts) {
    final float xrange = maxXCoord - minXCoord;
    final float yrange = maxYCoord - minYCoord;
    if (xrange == 0 && yrange == 0) {
      // Occurs when only 1 point or colocated data. A distance of zero is invalid so set to 1.
      return 1;
    }

    // If not set then compute the generating distance
    if (!Double.isFinite(generatingDistanceE) || generatingDistanceE <= 0) {
      return computeGeneratingDistance(minPts);
    }

    // Compute the upper distance we can expect
    final double maxDistance = Math.sqrt(xrange * xrange + yrange * yrange);
    if (generatingDistanceE > maxDistance) {
      return (float) maxDistance;
    }

    // Stick to the user input
    return generatingDistanceE;
  }

  /**
   * Checks for search algorithm structures stored in memory.
   *
   * @return true, if successful
   */
  public boolean hasMemory() {
    return grid != null;
  }

  /**
   * Clear memory used by the search algorithm.
   */
  private void finish() {
    if (!options.contains(Option.CACHE)) {
      clearMemory();
    }
  }

  /**
   * Clear memory used by the search algorithm.
   */
  public void clearMemory() {
    grid = null;
    heap = null;
  }

  /**
   * Expand cluster order.
   *
   * @param object the object
   * @param e the generating distance (squared)
   * @param minPts the min points for a core object
   * @param orderedFile the results
   * @param orderSeeds the order seeds
   * @return true, if the algorithm has received a shutdown signal
   */
  private boolean opticsExpandClusterOrder(Molecule object, float generatingDistance, int minPts,
      OpticsResultList orderedFile, OpticsPriorityQueue orderSeeds) {
    grid.findNeighboursAndDistances(minPts, object, generatingDistance);
    object.markProcessed();
    setCoreDistance(object, minPts, grid.neighbours);
    if (orderedFile.add(object)) {
      return true;
    }

    if (object.coreDistance != UNDEFINED) {
      // Create seed-list for further expansion.
      fill(orderSeeds, grid.neighbours, object);

      while (orderSeeds.hasNext()) {
        final Molecule nextObject = orderSeeds.next();
        grid.findNeighboursAndDistances(minPts, nextObject, generatingDistance);
        nextObject.markProcessed();
        setCoreDistance(nextObject, minPts, grid.neighbours);
        if (orderedFile.add(nextObject)) {
          return true;
        }

        if (nextObject.coreDistance != UNDEFINED) {
          opticsUpdateSearch(orderSeeds, grid.neighbours, nextObject);
        }
      }
    }
    return false;
  }

  /**
   * Set the core distance.
   *
   * @param object the object
   * @param minPts the min points to be a core point
   * @param neighbours the neighbours
   */
  public void setCoreDistance(Molecule object, int minPts, MoleculeList neighbours) {
    final int size = neighbours.size;
    if (size < minPts) {
      // Not a core point
      return;
    }

    // Special case where we find the max value
    if (size == minPts) {
      float max = neighbours.get(0).getD();
      for (int i = 1; i < size; i++) {
        if (max < neighbours.get(i).getD()) {
          max = neighbours.get(i).getD();
        }
      }
      object.coreDistance = max;
      return;
    }

    // Use a heap structure. This should out perform a pointer to the max value when
    // minPts is much lower than the number of neighbours. When it is similar then
    // the speed is fast no matter what method is used since minPts is expected to be low
    // (somewhere around 5 for 2D data).

    heap.start(neighbours.get(0).getD());
    int index = 1;
    while (index < minPts) {
      heap.put(index, neighbours.get(index).getD());
      index++;
    }
    // Scan
    while (index < size) {
      heap.push(neighbours.get(index++).getD());
    }

    object.coreDistance = heap.getMaxValue();
  }

  /**
   * Clear the seeds and fill with the unprocessed neighbours of the current object. Set the
   * reachability distance and reorder.
   *
   * @param orderSeeds the order seeds
   * @param neighbours the neighbours
   * @param centreObject the object
   */
  private static void fill(OpticsPriorityQueue orderSeeds, MoleculeList neighbours,
      Molecule centreObject) {
    orderSeeds.clear();

    final float coreDist = centreObject.coreDistance;
    for (int i = neighbours.size; i-- > 0;) {
      final Molecule object = neighbours.get(i);
      if (object.isNotProcessed()) {
        // This is new so add it to the list
        object.reachabilityDistance = max(coreDist, object.getD());
        object.predecessor = centreObject.id;
        orderSeeds.push(object);
      }
    }
  }

  /**
   * Update the ordered seeds with the neighbours of the current object. Set the reachability
   * distance and reorder.
   *
   * @param orderSeeds the order seeds
   * @param neighbours the neighbours
   * @param centreObject the object
   */
  private static void opticsUpdateSearch(OpticsPriorityQueue orderSeeds, MoleculeList neighbours,
      Molecule centreObject) {
    final float coreDist = centreObject.coreDistance;
    for (int i = neighbours.size; i-- > 0;) {
      final Molecule object = neighbours.get(i);
      if (object.isNotProcessed()) {
        final float newReachabilityDistance = max(coreDist, object.getD());
        if (object.reachabilityDistance == UNDEFINED) {
          // This is new so add it to the list
          object.reachabilityDistance = newReachabilityDistance;
          object.predecessor = centreObject.id;
          orderSeeds.push(object);

          // ELSE:
          // This is already in the list
          // Here is the difference between OPTICS and DBSCAN.
          // In this case the order of points to process can be changed based on the reachability.
        } else if (newReachabilityDistance < object.reachabilityDistance) {
          object.reachabilityDistance = newReachabilityDistance;
          object.predecessor = centreObject.id;
          orderSeeds.moveUp(object);
        }
      }
    }
  }

  /**
   * Find the max (ignore the possibility of NaN or infinity.
   *
   * @param value1 the value 1
   * @param value2 the value 2
   * @return the max
   */
  public static float max(float value1, float value2) {
    return (value1 >= value2) ? value1 : value2;
  }

  /**
   * Compute the OPTICS generating distance assuming a uniform distribution in the data space.
   *
   * @param minPts the min points for a core object
   * @return the generating distance
   */
  public float computeGeneratingDistance(int minPts) {
    return computeGeneratingDistance(minPts, area, xcoord.length);
  }

  /**
   * Compute the OPTICS generating distance assuming a uniform distribution in the data space.
   *
   * @param minPts the min points for a core object
   * @param area the area of the data space
   * @param numberOfPoints the number of points in the data space
   * @return the generating distance
   */
  public static float computeGeneratingDistance(int minPts, double area, int numberOfPoints) {
    // Taken from section 4.1 of the OPTICS paper.

    // Number of dimensions
    // d = 2
    // Volume of the data space (DS)
    final double volumeDataSpace = area;
    // Expected k-nearest-neighbours
    final int k = minPts;

    // Compute the volume of the hypersphere required to contain k neighbours,
    // assuming a uniform spread.

    final double volumeSphere = (volumeDataSpace / numberOfPoints) * k;

    // Note: Volume S(r) for a 2D hypersphere = pi * r^2
    return (float) Math.sqrt(volumeSphere / Math.PI);
  }

  /**
   * Compute the core points as any that have more than min points within the distance. All points
   * within the radius are reachable points that are processed in turn. Any new core points expand
   * the search space.
   *
   * <p>This is an implementation of the DBSCAN method. Ester, Martin; Kriegel, Hans-Peter; Sander,
   * Jörg; Xu, Xiaowei (1996). Simoudis, Evangelos; Han, Jiawei; Fayyad, Usama M., eds. A
   * density-based algorithm for discovering clusters in large spatial databases with noise.
   * Proceedings of the Second International Conference on Knowledge Discovery and Data Mining
   * (KDD-96). AAAI Press. pp. 226–231.
   *
   * <p>Note that the generating distance may have been modified if invalid. If it is not strictly
   * positive or not finite then it is set using {@link #computeGeneratingDistance(int)}. If it is
   * larger than the data range allows it is set to the maximum distance that can be computed for
   * the data range. If the data are colocated the distance is set to 1. The distance is stored in
   * the results.
   *
   * <p>This creates a large memory structure. It can be held in memory for re-use when using a
   * different number of min points. The tracker can be used to follow progress (see
   * {@link #setTracker(TrackProgress)}).
   *
   * @param generatingDistanceE the generating distance (E) (set to zero to auto calibrate)
   * @param minPoints the min points for a core object
   * @return the results (or null if the algorithm was stopped using the tracker)
   */
  public DbscanResult dbscan(float generatingDistanceE, int minPoints) {
    final int minPts = Math.max(1, minPoints);

    long time = System.currentTimeMillis();

    initialiseDbscan(generatingDistanceE, minPts);

    // The distance may be updated
    final float workingGeneratingDistance = grid.generatingDistanceE;

    if (tracker != null) {
      tracker.log("Running DBSCAN ... Distance=%g, minPts=%d", workingGeneratingDistance, minPts);
      tracker.progress(0, xcoord.length);
    }

    // The generating distance (E) used in the paper is the maximum distance at which cluster
    // centres will be formed. This implementation uses the squared distance to avoid sqrt()
    // function calls.
    final float e = workingGeneratingDistance * workingGeneratingDistance;

    final int size = xcoord.length;
    final Molecule[] setOfObjects = grid.setOfObjects;

    // Working storage
    final MoleculeQueue seeds = new MoleculeQueue(size);
    final Counter counter = new Counter(size);

    for (int i = 0; i < size; i++) {
      final Molecule object = setOfObjects[i];
      if (object.isNotProcessed() && dbscanExpandCluster(object, e, minPts, counter, seeds)) {
        break;
      }
    }

    boolean stopped = false;
    if (tracker != null) {
      stopped = tracker.isEnded();
      tracker.progress(1.0);

      if (stopped) {
        tracker.log("Aborted DBSCAN");
      }
    }

    DbscanResult dbscanResult = null;
    if (!stopped) {
      // Convert the working data structure to the output
      final DbscanOrder[] dbscanOrder = new DbscanOrder[size];
      for (int i = 0; i < size; i++) {
        dbscanOrder[i] = setOfObjects[i].toDbscanResult();
      }
      dbscanResult = new DbscanResult(this, minPts, workingGeneratingDistance, dbscanOrder);
      if (tracker != null) {
        time = System.currentTimeMillis() - time;
        tracker.log("Finished DBSCAN: %d %s (Time = %s)", counter.getTotalClusters(),
            pleuraliseClusterCount(counter.getTotalClusters()), TextUtils.millisToString(time));
      }
    }

    finish();

    return dbscanResult;
  }

  private boolean dbscanExpandCluster(Molecule object, float generatingDistance, int minPts,
      Counter counter, MoleculeQueue seeds) {
    grid.findNeighbours(minPts, object, generatingDistance);
    if (counter.increment()) {
      return true;
    }

    object.markProcessed();
    object.setNumberOfPoints(grid.neighbours.size);
    if (grid.neighbours.size >= minPts) {
      // New cluster
      final int clusterId = counter.nextClusterId();
      object.setClusterOrigin(clusterId);

      // Expand through the grid.neighbours
      seeds.clear();
      dbscanUpdateSearch(seeds, grid.neighbours, clusterId);

      while (seeds.hasNext()) {
        final Molecule nextObject = seeds.getNext();
        grid.findNeighbours(minPts, nextObject, generatingDistance);
        if (counter.increment()) {
          return true;
        }

        nextObject.markProcessed();
        nextObject.setNumberOfPoints(grid.neighbours.size);
        if (grid.neighbours.size >= minPts) {
          dbscanUpdateSearch(seeds, grid.neighbours, clusterId);
        }
      }
    }

    return false;
  }

  /**
   * Update the set of points to search with any as yet unvisited points from the list of
   * neighbours. Set the cluster Id of any unassigned points.
   *
   * @param pointsToSearch the points to search
   * @param neighbours the neighbours
   * @param clusterId the cluster id
   */
  private static void dbscanUpdateSearch(MoleculeQueue pointsToSearch, MoleculeList neighbours,
      int clusterId) {
    for (int i = neighbours.size; i-- > 0;) {
      final Molecule object = neighbours.get(i);
      if (object.isNotInACluster()) {
        object.setClusterMember(clusterId);

        // Ensure that the search is not repeated
        // ---
        // Note: It is possible to speed up the algorithm by skipping those at zero distance
        // (since no more points can be found from a second search from the same location).
        // However we do not currently require that we compute neighbour distances so this
        // cannot be done at present.
        if (object.isNotProcessed()) {
          pointsToSearch.push(object);
        }
      }
    }
  }

  /**
   * Create a copy.
   *
   * <p>Copies the underlying coordinate data and algorithm properties. Data structures used during
   * computation are not copied. This includes the tracker.
   *
   * @param deepCopy Set to true to copy the coordinate arrays
   * @return the copy
   */
  @Override
  public OpticsManager copy(boolean deepCopy) {
    return new OpticsManager(this, deepCopy);
  }

  /**
   * Gets the raw x data.
   *
   * @return the raw x data
   */
  float[] getXData() {
    return xcoord;
  }

  /**
   * Gets the raw y data.
   *
   * @return the raw y data
   */
  float[] getYData() {
    return ycoord;
  }

  /**
   * Gets the original X.
   *
   * @param index the index
   * @return the original X
   */
  float getOriginalX(int index) {
    return xcoord[index] + originx;
  }

  /**
   * Gets the original Y.
   *
   * @param index the index
   * @return the original Y
   */
  float getOriginalY(int index) {
    return ycoord[index] + originy;
  }

  /**
   * Compute (a sample of) the k-nearest neighbour distance for objects from the data The plot of
   * the sorted k-distance can be used to pick the generating distance. Or it can be done
   * automatically using a % noise threshold.
   *
   * <p>See: Jörg Sander, Martin Ester, Hans-Peter Kriegel, Xiaowei Xu Density-Based Clustering in
   * Spatial Databases: The Algorithm GDBSCAN and Its Applications Data Mining and Knowledge
   * Discovery, 1998.
   *
   * @param numberOfNeighbours the k (automatically bounded between 1 and size-1)
   * @param samples the number of samples (set to below 1 to compute all samples)
   * @param cache Set to true to cache the KD-tree used for the nearest neighbour search
   * @return the k-nearest neighbour distances
   */
  public float[] nearestNeighbourDistance(int numberOfNeighbours, int samples, boolean cache) {
    final int size = xcoord.length;
    if (size < 2) {
      return new float[0];
    }

    long time = System.currentTimeMillis();

    // Bounds check k
    int numNeighbours = MathUtils.clip(1, size - 1, numberOfNeighbours);

    // Optionally compute all samples if samples is not positive
    final int n = Math.min((samples <= 0) ? size : samples, size);
    final float[] d = new float[n];

    if (tracker != null) {
      tracker.log("Computing %d nearest-neighbour distances, samples=%d", numNeighbours, n);
      tracker.progress(0, n);
    }

    int[] indices;
    if (n <= size) {
      // Compute all
      indices = SimpleArrayUtils.natural(n);
    } else {
      // Random sample
      indices = new PermutationSampler(RandomSource.create(RandomSource.MWC_256), size, n).sample();
    }

    // Use a KDtree to allow search of the space
    if (tree == null) {
      tree = new SimpleFloatKdTree2D.SqrEuclid2D();
      for (int i = 0; i < size; i++) {
        tree.addPoint(new float[] {xcoord[i], ycoord[i]});
      }
    }

    // Note: The k-nearest neighbour search will include the actual point so increment by 1
    numNeighbours++;

    for (int i = 0; i < n; i++) {
      if (tracker != null) {
        tracker.progress(i, n);
      }
      final int index = indices[i];
      final float[] location = new float[] {xcoord[index], ycoord[index]};
      // The tree will use the squared distance so compute the root
      d[i] = (float) (Math.sqrt(tree.nearestNeighbor(location, numNeighbours)[0]));
    }
    if (tracker != null) {
      time = System.currentTimeMillis() - time;
      tracker.log("Finished KNN computation (Time = " + TextUtils.millisToString(time) + ")");
      tracker.progress(1);
    }

    if (!cache) {
      tree = null;
    }

    return d;
  }

  /**
   * Compute the core radius for each point to have n closest neighbours and the minimum
   * reachability distance of a point from another core point.
   *
   * <p>This is an implementation of the Fast OPTICS method. J. Schneider and M. Vlachos. Fast
   * parameterless density-based clustering via random projections. Proc. 22nd ACM international
   * conference on Conference on Information &amp; Knowledge Management (CIKM).
   *
   * <p>Fast OPTICS uses random projections of the data into a linear space. The linear space is
   * then divided into sets until each set is below a minimum size. The process is repeated multiple
   * times to generate many sets of neighbours. Analysis for each object is then performed using
   * only those other objects that are found in the same sets as it (the union of the neighbours in
   * all sets containing an object defines the object neighbourhood). This method therefore does not
   * require a distance threshold as each object will always have a neighbourhood and a core
   * distance which is the average distance to all the other objects in its neighbourhood. The
   * method allows clustering of data with widely varying densities across the set.
   *
   * <p>Note that once the neighbourhood and core distance are computed for each object the
   * remaining algorithm follows that of OPTICS. Note that despite the name this is not faster than
   * the OPTICS method implemented here due to the low number of dimensions. Speed is significantly
   * faster for high dimensional data.
   *
   * <p>The returned results are the output of {@link OpticsResult#extractDbscanClustering(float)}
   * with the auto-configured generating distance set using {@link #computeGeneratingDistance(int)}.
   * If it is larger than the data range allows it is set to the maximum distance that can be
   * computed for the data range. If the data are colocated the distance is set to 1. The distance
   * is stored in the results.
   *
   * <p>This implementation is a port of the version in the ELKI framework:
   * https://elki-project.github.io/.
   *
   * @param minPts the min points for a core object (recommended range around 4)
   * @return the results (or null if the algorithm was stopped using the tracker)
   */
  public OpticsResult fastOptics(int minPts) {
    return fastOptics(minPts, 0, 0, false, false, SampleMode.RANDOM);
  }

  /**
   * Compute the core radius for each point to have n closest neighbours and the minimum
   * reachability distance of a point from another core point.
   *
   * <p>This is an implementation of the Fast OPTICS method. J. Schneider and M. Vlachos. Fast
   * parameterless density-based clustering via random projections. Proc. 22nd ACM international
   * conference on Conference on Information &amp; Knowledge Management (CIKM).
   *
   * <p>Fast OPTICS uses random projections of the data into a linear space. The linear space is
   * then divided into sets until each set is below a minimum size. The process is repeated multiple
   * times to generate many sets of neighbours. Analysis for each object is then performed using
   * only those other objects that are found in the same sets as it (the union of the neighbours in
   * all sets containing an object defines the object neighbourhood). This method therefore does not
   * require a distance threshold as each object will always have a neighbourhood and a core
   * distance which is the average distance to all the other objects in its neighbourhood. The
   * method allows clustering of data with widely varying densities across the set.
   *
   * <p>Note that once the neighbourhood and core distance are computed for each object the
   * remaining algorithm follows that of OPTICS. Note that despite the name this is not faster than
   * the OPTICS method implemented here due to the low number of dimensions. Speed is significantly
   * faster for high dimensional data.
   *
   * <p>The returned results are the output of {@link OpticsResult#extractDbscanClustering(float)}
   * with the auto-configured generating distance set using {@link #computeGeneratingDistance(int)}.
   * If it is larger than the data range allows it is set to the maximum distance that can be
   * computed for the data range. If the data are colocated the distance is set to 1. The distance
   * is stored in the results.
   *
   * <p>This implementation is a port of the version in the ELKI framework:
   * https://elki-project.github.io/.
   *
   * @param minPoints the min points for a core object (recommended range around 4)
   * @param numberOfSplits The number of splits to compute (if below 1 it will be auto-computed
   *        using the size of the data)
   * @param numberOfProjections The number of projections to compute (if below 1 it will be
   *        auto-computed using the size of the data)
   * @param useRandomVectors Set to true to use random vectors for the projections. The default is
   *        to uniformly create vectors on the semi-circle interval.
   * @param saveApproximateSets Set to true to save all sets that are approximately minPts size. The
   *        default is to only save sets smaller than minPts size.
   * @param sampleMode the sample mode to select neighbours within each split set
   * @return the results (or null if the algorithm was stopped using the tracker)
   */
  public OpticsResult fastOptics(int minPoints, int numberOfSplits, int numberOfProjections,
      boolean useRandomVectors, boolean saveApproximateSets, SampleMode sampleMode) {
    final int minPts = Math.max(1, minPoints);

    long time = System.currentTimeMillis();
    initialiseFastOptics(minPts);

    final int numSplits =
        ProjectedMoleculeSpace.getOrComputeNumberOfSplitSets(numberOfSplits, getSize());
    final int numProjections =
        ProjectedMoleculeSpace.getOrComputeNumberOfProjections(numberOfProjections, getSize());

    if (tracker != null) {
      tracker.log(
          "Running FastOPTICS ... minPts=%d, splits=%d, projections=%d, randomVectors=%b, "
              + "approxSets=%b, sampleMode=%s, threads=%d",
          minPts, numSplits, numProjections, useRandomVectors, saveApproximateSets, sampleMode,
          getNumberOfThreads());
    }

    // Compute projections and find neighbours
    final ProjectedMoleculeSpace space = (ProjectedMoleculeSpace) grid;

    space.setNumberOfSplits(numSplits);
    space.setNumberOfProjections(numProjections);
    space.setUseRandomVectors(useRandomVectors);
    space.setSaveApproximateSets(saveApproximateSets);
    space.setSampleMode(sampleMode);
    space.setExecutorService(getExecutorService());

    space.setTracker(tracker);
    space.computeSets(minPts); // project points
    space.computeAverageDistInSetAndNeighbours();

    // Run OPTICS
    long time2 = 0;
    if (tracker != null) {
      time2 = System.currentTimeMillis();
      tracker.log("Running OPTICS ...");
      tracker.progress(0, xcoord.length);
    }

    // Note: The method and variable names used in this function are designed to match
    // the pseudocode implementation from the 1999 OPTICS paper.

    final int size = xcoord.length;
    final Molecule[] setOfObjects = grid.setOfObjects;

    final OpticsPriorityQueue orderSeeds = createQueue(size);

    final OpticsResultList results = new OpticsResultList(size);

    for (int i = 0; i < size; i++) {
      final Molecule object = setOfObjects[i];
      if (object.isNotProcessed()
          && fastOpticsExpandClusterOrder(object, minPts, results, orderSeeds)) {
        break;
      }
    }

    boolean stopped = false;
    if (tracker != null) {
      stopped = tracker.isEnded();
      tracker.progress(1.0);

      if (stopped) {
        tracker.log("Aborted OPTICS");
      }
    }

    OpticsResult optics = null;
    if (!stopped) {
      optics = new OpticsResult(this, minPts, getMaxReachability(results.list), results.list);
      final int nClusters = optics.extractDbscanClustering(grid.generatingDistanceE);
      if (tracker != null) {
        final long end = System.currentTimeMillis();
        time = end - time;
        time2 = end - time2;
        tracker.log("Finished OPTICS: %d %s @ %s (Time = %s)", nClusters,
            pleuraliseClusterCount(nClusters), MathUtils.rounded(grid.generatingDistanceE),
            TextUtils.millisToString(time2));
        tracker.log("Finished FastOPTICS ... " + TextUtils.millisToString(time));
      }
    }

    finish();

    return optics;
  }

  /**
   * Gets the number of split sets to use for FastOPTICS.
   *
   * @param numberOfSplits The number of splits to compute (if below 1 it will be auto-computed
   *        using the size of the data)
   * @return the number of split sets
   */
  public int getNumberOfSplitSets(int numberOfSplits) {
    return ProjectedMoleculeSpace.getOrComputeNumberOfSplitSets(numberOfSplits, getSize());
  }

  /**
   * Expand cluster order.
   *
   * @param object the object
   * @param minPts the min points for a core object
   * @param orderedFile the results
   * @param orderSeeds the order seeds
   * @return true, if the algorithm has received a shutdown signal
   */
  private boolean fastOpticsExpandClusterOrder(Molecule object, int minPts,
      OpticsResultList orderedFile, OpticsPriorityQueue orderSeeds) {
    grid.findNeighbours(minPts, object, 0);
    object.markProcessed();
    if (orderedFile.add(object)) {
      return true;
    }

    if (object.coreDistance != UNDEFINED) {
      // Create seed-list for further expansion.
      fillWithComputeDistance(orderSeeds, grid.neighbours, object);

      while (orderSeeds.hasNext()) {
        final Molecule nextObject = orderSeeds.next();
        grid.findNeighbours(minPts, nextObject, 0);
        nextObject.markProcessed();
        if (orderedFile.add(nextObject)) {
          return true;
        }

        if (nextObject.coreDistance != UNDEFINED) {
          updateWithComputeDistance(orderSeeds, grid.neighbours, nextObject);
        }
      }
    }
    return false;
  }

  /**
   * Clear the seeds and fill with the unprocessed neighbours of the current object. Set the
   * reachability distance and reorder.
   *
   * @param orderSeeds the order seeds
   * @param neighbours the neighbours
   * @param centreObject the object
   */
  private static void fillWithComputeDistance(OpticsPriorityQueue orderSeeds,
      MoleculeList neighbours, Molecule centreObject) {
    orderSeeds.clear();

    final float coreDist = centreObject.coreDistance;
    for (int i = neighbours.size; i-- > 0;) {
      final Molecule object = neighbours.get(i);
      if (object.isNotProcessed()) {
        // This is new so add it to the list
        object.reachabilityDistance = max(coreDist, object.distanceSquared(centreObject));
        object.predecessor = centreObject.id;
        orderSeeds.push(object);
      }
    }
  }

  /**
   * Update the ordered seeds with the neighbours of the current object. Set the reachability
   * distance and reorder.
   *
   * @param orderSeeds the order seeds
   * @param neighbours the neighbours
   * @param centreObject the object
   */
  private static void updateWithComputeDistance(OpticsPriorityQueue orderSeeds,
      MoleculeList neighbours, Molecule centreObject) {
    final float coreDist = centreObject.coreDistance;
    for (int i = neighbours.size; i-- > 0;) {
      final Molecule object = neighbours.get(i);
      if (object.isNotProcessed()) {
        final float newReachabilityDistance = max(coreDist, object.distanceSquared(centreObject));
        if (object.reachabilityDistance == UNDEFINED) {
          // This is new so add it to the list
          object.reachabilityDistance = newReachabilityDistance;
          object.predecessor = centreObject.id;
          orderSeeds.push(object);

          // ELSE:
          // This is already in the list
          // Here is the difference between OPTICS and DBSCAN.
          // In this case the order of points to process can be changed based on the reachability.
        } else if (newReachabilityDistance < object.reachabilityDistance) {
          object.reachabilityDistance = newReachabilityDistance;
          object.predecessor = centreObject.id;
          orderSeeds.moveUp(object);
        }
      }
    }
  }

  /**
   * Gets the max reachability distance in the results.
   *
   * @param list the results list
   * @return the max reachability distance
   */
  private static float getMaxReachability(OpticsOrder[] list) {
    double max = 0;
    for (int i = list.length; i-- > 0;) {
      if (list[i].isReachablePoint() && max < list[i].getReachabilityDistance()) {
        max = list[i].getReachabilityDistance();
      }
    }
    return (float) max;
  }

  /**
   * Gets the number of threads to use for multi-threaded algorithms (FastOPTICS).
   *
   * <p>Note: This is initialised to the number of processors available to the JVM.
   *
   * @return the number of threads
   */
  public int getNumberOfThreads() {
    if (numberOfThreads == 0) {
      numberOfThreads = Runtime.getRuntime().availableProcessors();
    }
    return numberOfThreads;
  }

  /**
   * Sets the number of threads to use for multi-threaded algorithms (FastOPTICS).
   *
   * <p>Multi-threaded algorithms use an {@link ExecutorService} if the number of threads is above
   * 1. The service can be shutdown using {@link #shutdownExecutorService()}.
   *
   * <p>Note: Changing the number of threads will shutdown a previously created service.
   *
   * @param numberOfThreads the new number of threads
   */
  public void setNumberOfThreads(int numberOfThreads) {
    final int newNumberOfThreads = Math.max(1, numberOfThreads);
    if (newNumberOfThreads != numberOfThreads) {
      shutdownExecutorService();
    }
    this.numberOfThreads = newNumberOfThreads;
  }

  /**
   * Gets (or creates) the random generator used for FastOPTICS.
   *
   * @return the random generator
   */
  private UniformRandomProvider getRandomGenerator() {
    if (seed == 0) {
      return RandomSource.create(RandomSource.MWC_256);
    }
    return RandomSource.create(RandomSource.MWC_256, seed);
  }

  /**
   * Gets the random seed used for FastOPTICS.
   *
   * @return the random seed
   */
  public long getRandomSeed() {
    return seed;
  }

  /**
   * Sets the random seed used for FastOPTICS.
   *
   * @param seed the new random seed
   */
  public void setRandomSeed(long seed) {
    this.seed = seed;
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
    this.tracker = tracker;
  }

  /**
   * Adds the options to the current options.
   *
   * @param options the options
   */
  public void addOptions(Option... options) {
    for (final Option option : options) {
      this.options.add(option);
    }
  }

  /**
   * Removes the options from the current options.
   *
   * @param options the options
   */
  public void removeOptions(Option... options) {
    for (final Option option : options) {
      this.options.remove(option);
    }
  }

  /**
   * Gets the options. This is a reference to the current options.
   *
   * @return the options
   */
  public Set<Option> getOptions() {
    return options;
  }

  /**
   * Compute the Local Outlier Probability scores. Scores are normalised to the range [0:1] with the
   * expected value of 0 (not an outlier).
   *
   * <p>See: Hans-Peter Kriegel, Peer Kröger, Erich Schubert, Arthur Zimek:<br> LoOP: Local Outlier
   * Probabilities<br> In Proceedings of the 18th International Conference on Information and
   * Knowledge Management (CIKM), Hong Kong, China, 2009
   *
   * @param numberOfNeighbours the number of neighbours (automatically bounded between 1 and size-1)
   * @param lambda The number of standard deviations to consider for density computation.
   * @param cache Set to true to cache the KD-tree used for the nearest neighbour search
   * @return the k-nearest neighbour distances
   */
  public float[] loop(int numberOfNeighbours, double lambda, boolean cache) {
    final int size = xcoord.length;
    if (size < 2) {
      return new float[size];
    }

    long time = System.currentTimeMillis();

    // Bounds check k
    final int safeNumberOfNeighbours = MathUtils.clip(1, size - 1, numberOfNeighbours);

    if (tracker != null) {
      tracker.log("Computing Local Outlier Probability scores, k=%d, Lambda=%s",
          safeNumberOfNeighbours, MathUtils.rounded(lambda));
    }

    if (loOp == null) {
      loOp = new LoOp(xcoord, ycoord);
    }
    loOp.setNumberOfThreads(getNumberOfThreads());

    double[] scores;
    try {
      scores = loOp.run(safeNumberOfNeighbours, lambda);
    } catch (final ExecutionException | InterruptedException ex) {
      if (tracker != null) {
        tracker.log("Failed LoOP computation: " + ex.getMessage());
      }
      Logger.getLogger(getClass().getName()).log(Level.WARNING, ex,
          () -> "Failed LoOP computation: " + ex.getMessage());
      return null;
    }

    // Convert to float
    final float[] result = new float[scores.length];
    for (int i = 0; i < scores.length; i++) {
      result[i] = (float) scores[i];
    }

    if (tracker != null) {
      time = System.currentTimeMillis() - time;
      tracker.log("Finished LoOP computation (Time = " + TextUtils.millisToString(time) + ")");
    }

    if (!cache) {
      loOp = null;
    }

    return result;
  }

  /**
   * Shutdown the executor service. This is created for multi-threaded algorithms (FastOPTICS) when
   * the number of threads is above 1.
   *
   * <p>Calling this has no effect if the executor service is not present or already shutdown.
   */
  public void shutdownExecutorService() {
    if (executorService != null) {
      executorService.shutdown();
      executorService = null;
    }
  }

  /**
   * Gets the executor service. This is null if the number of threads is 1 or below. It reuses the
   * same executor service if previously created.
   *
   * @return the executor service
   */
  @VisibleForTesting
  ExecutorService getExecutorService() {
    if (numberOfThreads <= 1) {
      return null;
    }
    if (executorService == null || executorService.isShutdown()) {
      executorService = Executors.newFixedThreadPool(numberOfThreads);
    }
    return executorService;
  }
}
