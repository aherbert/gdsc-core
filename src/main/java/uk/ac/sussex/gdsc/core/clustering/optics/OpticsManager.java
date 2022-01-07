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
 * Copyright (C) 2011 - 2022 Alex Herbert
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

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.IntConsumer;
import java.util.function.ToDoubleBiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.PermutationSampler;
import uk.ac.sussex.gdsc.core.clustering.CoordinateStore;
import uk.ac.sussex.gdsc.core.data.VisibleForTesting;
import uk.ac.sussex.gdsc.core.logging.NullTrackProgress;
import uk.ac.sussex.gdsc.core.logging.TrackProgress;
import uk.ac.sussex.gdsc.core.trees.FloatDistanceFunction;
import uk.ac.sussex.gdsc.core.trees.FloatDistanceFunctions;
import uk.ac.sussex.gdsc.core.trees.FloatKdTree;
import uk.ac.sussex.gdsc.core.trees.KdTrees;
import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.core.utils.TextUtils;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;
import uk.ac.sussex.gdsc.core.utils.rng.UniformRandomProviders;

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
  private LoOp loopObject;

  /** The seed for random algorithms. */
  private long seed;

  /** The molecule space. Package level for JUnit testing. */
  MoleculeSpace moleculeSpace;

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
  private FloatKdTree tree;

  /** The distance function for the number of dimensions. */
  final ToDoubleBiFunction<Molecule, Molecule> distanceFunction;

  /** The optional coordinates for the z dimension. */
  private final float[] zcoord;
  /** The min Z coord. */
  public final float minZCoord;
  /** The max Z coord. */
  public final float maxZCoord;

  /** The volume of the coordinates. */
  private final double volume;

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
     * <p>Note: This option is experimental and can cause slow-down.
     */
    CIRCULAR_PROCESSING,
    /**
     * Flag to indicate that inner-circle processing should be used on the 2D grid. This has
     * performance benefits for DBSCAN since some distance computations can be assumed.
     *
     * <p>Note: This option is experimental and can cause slow-down.
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
     *
     * <p>Note: This option matches the implementation in the ELKI framework version 0.7.5.
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
     * <p>Note: This option matches the implementation in the ELKI framework version 0.7.1.
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
  @VisibleForTesting
  static class MoleculeQueue extends MoleculeList {

    /** The next. */
    int next;

    /**
     * Instantiates a new molecule queue.
     *
     * @param capacity the capacity
     */
    MoleculeQueue(int capacity) {
      super(capacity);
    }

    /**
     * Push.
     *
     * @param molecule the molecule
     */
    void push(Molecule molecule) {
      add(molecule);
    }

    @Override
    void clear() {
      size = next = 0;
    }

    /**
     * Checks for next.
     *
     * @return true, if successful
     */
    boolean hasNext() {
      return next < size;
    }

    /**
     * Gets the next.
     *
     * @return the next
     */
    Molecule getNext() {
      return list[next++];
    }
  }

  /**
   * Counter used in DBSCAN.
   */
  private class Counter {
    /** Constants instance of the tracker throughout processing. */
    final TrackProgress tracker;

    /** The next cluster id. */
    int next;

    /** The progress. */
    int progress;

    /** The total. */
    int total;

    /**
     * Instantiates a new counter.
     *
     * @param total the total
     */
    Counter(int total) {
      tracker = NullTrackProgress.createIfNull(OpticsManager.this.getTracker());
      this.total = total;
    }

    /**
     * Next cluster id.
     *
     * @return the int
     */
    int nextClusterId() {
      return ++next;
    }

    /**
     * Increment the counter and check if processing should stop.
     *
     * @return true, if processing should stop
     */
    boolean increment() {
      tracker.progress(++progress, total);
      return tracker.isEnded();
    }

    /**
     * Gets the total clusters.
     *
     * @return the total clusters
     */
    int getTotalClusters() {
      return next;
    }
  }

  /**
   * Interface for the OPTICS priority queue. Molecules should be ordered by their reachability
   * distance.
   */
  @VisibleForTesting
  interface OpticsPriorityQueue {
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
   * Used in the OPTICS algorithm to store the next seed in a priority queue.
   */
  @VisibleForTesting
  static class OpticsMoleculePriorityQueue extends MoleculeList implements OpticsPriorityQueue {

    /** The next index. */
    int nextIndex;

    /**
     * Instantiates a new optics molecule priority queue.
     *
     * @param capacity the capacity
     */
    OpticsMoleculePriorityQueue(int capacity) {
      super(capacity);
    }

    @Override
    public void push(Molecule molecule) {
      set(molecule, size++);
      moveUp(molecule);
    }

    /**
     * Sets the molecule at the index.
     *
     * @param molecule the molecule
     * @param index the index
     */
    void set(Molecule molecule, int index) {
      list[index] = molecule;
      molecule.setQueueIndex(index);
    }

    @Override
    public void moveUp(Molecule object) {
      if (lower(object, list[nextIndex])) {
        swap(nextIndex, object.getQueueIndex());
      }
    }

    @Override
    public boolean hasNext() {
      return nextIndex < size;
    }

    @Override
    public Molecule next() {
      final Molecule molecule = list[nextIndex++];
      if (hasNext()) {
        // Find the next lowest molecule
        int lowest = nextIndex;
        for (int i = nextIndex + 1; i < size; i++) {
          if (lower(list[i], list[lowest])) {
            lowest = i;
          }
        }
        swap(nextIndex, lowest);
      }
      return molecule;
    }

    /**
     * Swap.
     *
     * @param index1 the index 1
     * @param index2 the index 2
     */
    void swap(int index1, int index2) {
      final Molecule molecule = list[index1];
      set(list[index2], index1);
      set(molecule, index2);
    }

    @Override
    public void clear() {
      size = nextIndex = 0;
    }

    /**
     * Lower.
     *
     * @param m1 the m 1
     * @param m2 the m 2
     * @return true, if successful
     */
    boolean lower(Molecule m1, Molecule m2) {
      return m1.reachabilityDistance < m2.reachabilityDistance;
    }
  }

  /**
   * Used in the OPTICS algorithm to store the next seed in a priority queue.
   *
   * <p>If distances are equal then IDs are used to sort the objects in order.
   */
  @VisibleForTesting
  static class OpticsMoleculePriorityQueueIdOrdered extends OpticsMoleculePriorityQueue {

    /**
     * Instantiates a new optics molecule priority queue id ordered.
     *
     * @param capacity the capacity
     */
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
   * Used in the OPTICS algorithm to store the next seed in a priority queue.
   *
   * <p>If distances are equal then IDs are used to sort the objects in reverse order.
   */
  @VisibleForTesting
  static class OpticsMoleculePriorityQueueReverseIdOrdered extends OpticsMoleculePriorityQueue {

    /**
     * Instantiates a new optics molecule priority queue reverse id ordered.
     *
     * @param capacity the capacity
     */
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
   * <p>This class is based on uk.ac.sussex.gdsc.core.trees.heaps.ObjDoubleMinHeap.
   */
  @VisibleForTesting
  static class OpticsMoleculeBinaryHeap extends MoleculeList implements OpticsPriorityQueue {

    /**
     * Instantiates a new optics molecule binary heap.
     *
     * @param capacity the capacity
     */
    OpticsMoleculeBinaryHeap(int capacity) {
      super(capacity);
    }

    @Override
    public void push(Molecule molecule) {
      set(molecule, size++);
      moveUp(molecule);
    }

    /**
     * Sets the molecule at the index.
     *
     * @param molecule the molecule
     * @param index the index
     */
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

    /**
     * Sift up.
     *
     * @param index the index
     */
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

    /**
     * Swap.
     *
     * @param index1 the index 1
     * @param index2 the index 2
     */
    void swap(int index1, int index2) {
      final Molecule m = list[index1];
      set(list[index2], index1);
      set(m, index2);
    }

    /**
     * Sift down.
     *
     * @param index the index
     */
    void siftDown(int index) {
      for (int parent = index, child = parent * 2 + 1; child < size;
          parent = child, child = parent * 2 + 1) {
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
  @VisibleForTesting
  static class OpticsMoleculeBinaryHeapIdOrdered extends OpticsMoleculeBinaryHeap {

    /**
     * Instantiates a new optics molecule binary heap id ordered.
     *
     * @param capacity the capacity
     */
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
  @VisibleForTesting
  static class OpticsMoleculeBinaryHeapReverseIdOrdered extends OpticsMoleculeBinaryHeap {

    /**
     * Instantiates a new optics molecule binary heap reverse id ordered.
     *
     * @param capacity the capacity
     */
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
    /** Constants instance of the tracker throughout processing. */
    final TrackProgress tracker;

    /** The list. */
    final OpticsOrder[] list;

    /** The size. */
    int size;

    /**
     * Instantiates a new optics result list.
     *
     * @param capacity the capacity
     */
    OpticsResultList(int capacity) {
      tracker = NullTrackProgress.createIfNull(OpticsManager.this.getTracker());
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
      tracker.progress(size, list.length);
      return tracker.isEnded();
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
    FloatHeap(int size) {
      ValidationUtils.checkArgument(size > 0, "N must be strictly positive");
      this.queue = new float[size];
      this.size = size;
    }

    /**
     * Add the first value to the heap.
     *
     * @param value the value
     */
    void start(float value) {
      queue[0] = value;
    }

    /**
     * Put the next value into the heap. This method is used to fill the heap from {@code i=1} to
     * {@code i<n}.
     *
     * @param index the index
     * @param value the value
     */
    void put(int index, float value) {
      queue[index] = value;
      upHeapify(index);
    }

    /**
     * Push a value onto a full heap. This method is used to add more values to a full heap.
     *
     * @param value the value
     */
    void push(float value) {
      if (queue[0] > value) {
        queue[0] = value;
        downHeapify(0);
      }
    }

    /**
     * Gets the max value.
     *
     * @return the max value
     */
    float getMaxValue() {
      return queue[0];
    }

    /**
     * Up heapify.
     *
     * @param index the index
     */
    void upHeapify(int index) {
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

    /**
     * Down heapify.
     *
     * @param index the index
     */
    void downHeapify(int index) {
      for (int parent = index, child = parent * 2 + 1; child < size;
          parent = child, child = parent * 2 + 1) {
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
   * A fixed size double list.
   */
  private static class DoubleList {
    /** The size. */
    int size;

    /** The values. */
    double[] values;

    /**
     * Create a new instance.
     *
     * @param capacity the capacity
     */
    DoubleList(int capacity) {
      values = new double[capacity];
    }

    /**
     * Adds the value.
     *
     * @param value the value
     */
    void add(double value) {
      values[size++] = value;
    }

    /**
     * Clear the list.
     */
    void clear() {
      size = 0;
    }

    /**
     * Gets the value.
     *
     * @param index the index
     * @return the value
     */
    double get(int index) {
      return values[index];
    }
  }

  /**
   * Input arrays are modified.
   *
   * @param xcoord the xcoord
   * @param ycoord the ycoord
   * @param area the volume of the coordinates (width by height)
   * @throws IllegalArgumentException if results are null or empty
   */
  public OpticsManager(float[] xcoord, float[] ycoord, double area) {
    super(xcoord, ycoord, area);
    distanceFunction = MoleculeDistanceFunctions.SQUARED_EUCLIDEAN_2D;
    zcoord = null;
    volume = this.area;
    minZCoord = maxZCoord = 0;
  }

  /**
   * Input arrays are modified.
   *
   * @param xcoord the xcoord
   * @param ycoord the ycoord
   * @param zcoord the zcoord
   * @param volume the volume of the coordinates (width by height by depth)
   * @throws IllegalArgumentException if results are null or empty
   */
  public OpticsManager(float[] xcoord, float[] ycoord, float[] zcoord, double volume) {
    super(xcoord, ycoord, volume);
    if (zcoord == null || xcoord.length != zcoord.length) {
      throw new IllegalArgumentException("Results are null or empty or mismatched in length");
    }
    distanceFunction = MoleculeDistanceFunctions.SQUARED_EUCLIDEAN_3D;
    this.zcoord = zcoord;

    // Ensure the volume stores at least the minimum volume to contain the coordinates
    final float[] limits = MathUtils.limits(zcoord);
    minZCoord = limits[0];
    maxZCoord = limits[1];
    // Ignore dimensions with no range by ensuring a non-zero number
    final double vol = computeDeltaOrOne(maxXCoord, minXCoord)
        * computeDeltaOrOne(maxYCoord, minYCoord) * computeDeltaOrOne(maxZCoord, minZCoord);
    this.volume = Math.max(vol, volume);
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
    seed = source.seed;
    distanceFunction = source.distanceFunction;
    zcoord = source.zcoord;
    volume = source.volume;
    minZCoord = source.minZCoord;
    maxZCoord = source.maxZCoord;
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
    final float workingGeneratingDistance = moleculeSpace.generatingDistanceE;

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
    final Molecule[] setOfObjects = moleculeSpace.setOfObjects;

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

  /**
   * Pleuralise cluster count.
   *
   * @param count the count
   * @return the string
   */
  private static String pleuraliseClusterCount(int count) {
    return TextUtils.pleuralise(count, "Cluster", "Clusters");
  }

  /**
   * Creates the queue.
   *
   * @param size the size
   * @return the optics priority queue
   */
  @VisibleForTesting
  OpticsPriorityQueue createQueue(final int size) {
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

    final Class<?> clazz = getPreferredMoleculeSpace(false);
    final float workingGeneratingDistanceE = generatingDistanceE;
    initialise(workingGeneratingDistanceE, minPts, clazz);
  }

  /**
   * Initialise dbscan.
   *
   * @param generatingDistanceE the generating distance E
   * @param minPts the min pts
   */
  private void initialiseDbscan(float generatingDistanceE, int minPts) {
    Class<?> clazz = getPreferredMoleculeSpace(true);
    float workingGeneratingDistanceE = generatingDistanceE;
    // Optimise the default selection of the molecule space
    if (clazz == null) {
      // 2D DBSCAN will benefit from inner radial processing if the number of comparisons is high.
      // Compute the expected number of molecules in the area.
      if (!is3d()) {
        // Ensure the distance is valid
        workingGeneratingDistanceE = getWorkingGeneratingDistance(generatingDistanceE, minPts);

        final double nMoleculesInCircle = getMoleculesInCircle(workingGeneratingDistanceE);

        if (nMoleculesInCircle > RadialMoleculeSpace.N_MOLECULES_FOR_NEXT_RESOLUTION_INNER) {
          clazz = InnerRadialMoleculeSpace.class;
        }
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
    // If the range is zero the points are colocated (or there is 1 point).
    // Return 1 so the area can be computed.
    return (delta == 0) ? 1 : delta;
  }

  /**
   * Returned the preferred class for the molecule space using the options.
   *
   * @param allowNull Set to true to return null if no options are set
   * @return the preferred class for the molecule space
   */
  @VisibleForTesting
  Class<?> getPreferredMoleculeSpace(boolean allowNull) {
    // 3D
    if (is3d()) {
      return FloatTreeMoleculeSpace.class;
    }
    // 2D optimisations
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
   * @param generatingDistanceE1 the generating distance E 1
   * @param minPts the min points for a core object
   * @param clazz the preferred class for the molecule space
   */
  private void initialise(float generatingDistanceE1, int minPts, Class<?> clazz) {
    // Ensure the distance is valid
    final float safeGeneratingDistanceE =
        getWorkingGeneratingDistance(generatingDistanceE1, minPts);

    final Class<?> moleculeSpaceClass = (clazz == null) ? getPreferredMoleculeSpace(false) : clazz;

    // Compare to the existing grid
    if (moleculeSpace == null || moleculeSpace.generatingDistanceE != safeGeneratingDistanceE
        || moleculeSpace.getClass() != moleculeSpaceClass) {
      if (tracker != null) {
        tracker.log("Initialising ...");
      }

      // Control the type of space we use to store the data
      if (moleculeSpaceClass == ProjectedMoleculeSpace.class) {
        moleculeSpace =
            new ProjectedMoleculeSpace(this, safeGeneratingDistanceE, getRandomGenerator());
      } else if (moleculeSpaceClass == InnerRadialMoleculeSpace.class) {
        moleculeSpace = new InnerRadialMoleculeSpace(this, safeGeneratingDistanceE);
      } else if (moleculeSpaceClass == RadialMoleculeSpace.class) {
        moleculeSpace = new RadialMoleculeSpace(this, safeGeneratingDistanceE);
      } else if (moleculeSpaceClass == FloatTreeMoleculeSpace.class) {
        moleculeSpace = new FloatTreeMoleculeSpace(this, safeGeneratingDistanceE);
      } else {
        moleculeSpace = new GridMoleculeSpace(this, safeGeneratingDistanceE);
      }

      moleculeSpace.generate();
    } else {
      // This is the same distance so the objects can be reused
      moleculeSpace.reset();
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
    final float zrange = maxZCoord - minZCoord;
    if (xrange == 0 && yrange == 0 && zrange == 0) {
      // Occurs when only 1 point or colocated data. A distance of zero is invalid so set to 1.
      return 1;
    }

    // If not set then compute the generating distance
    if (!Double.isFinite(generatingDistanceE) || generatingDistanceE <= 0) {
      return computeGeneratingDistance(minPts);
    }

    // Compute the upper distance we can expect
    final double maxDistance = Math.sqrt(xrange * xrange + yrange * yrange + zrange * zrange);
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
    return moleculeSpace != null;
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
    moleculeSpace = null;
    heap = null;
  }

  /**
   * Expand cluster order.
   *
   * @param object the object
   * @param generatingDistance the generating distance
   * @param minPts the min points for a core object
   * @param orderedFile the results
   * @param orderSeeds the order seeds
   * @return true, if the algorithm has received a shutdown signal
   */
  private boolean opticsExpandClusterOrder(Molecule object, float generatingDistance, int minPts,
      OpticsResultList orderedFile, OpticsPriorityQueue orderSeeds) {
    moleculeSpace.findNeighboursAndDistances(minPts, object, generatingDistance);

    // Note: The original OPTICS algorithm adds the point to the ordered file even if
    // it does not have a core distance. This means that a point that may be reachable but
    // is on the edge of a cluster is marked with no reachability (as it has been processed).

    // The alternative is to add to the ordered file only if the core distance is defined.
    // This would allow an edge point that is reachable to be included in the reachability profile
    // even if it was not a core point itself.
    // This requires changing the data structure to allow identification of processed points
    // that are not core points. These can be included in the order seeds but should not
    // be searched again for neighbours. They should just be marked with a reachability and
    // added to the ordered file. This is a change from object.isNotProcessed() to
    // object.isNotProcessedOrIsNotReachable() (Note that cluster seeds will not have a
    // reachability distance but will have a core distance. So is-not-reachable should be a
    // flag rather than using the reachability distance as a marker.)
    // It also requires all unreachable noise points are added to the ordered file at the end
    // of processing.

    object.markProcessed();
    setCoreDistance(object, minPts, moleculeSpace.neighbours);
    if (orderedFile.add(object)) {
      return true;
    }

    if (object.coreDistance != UNDEFINED) {
      // Create seed-list for further expansion.
      fill(orderSeeds, moleculeSpace.neighbours, object);

      while (orderSeeds.hasNext()) {
        final Molecule nextObject = orderSeeds.next();
        moleculeSpace.findNeighboursAndDistances(minPts, nextObject, generatingDistance);
        nextObject.markProcessed();
        setCoreDistance(nextObject, minPts, moleculeSpace.neighbours);
        if (orderedFile.add(nextObject)) {
          return true;
        }

        if (nextObject.coreDistance != UNDEFINED) {
          opticsUpdateSearch(orderSeeds, moleculeSpace.neighbours, nextObject);
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
  private void setCoreDistance(Molecule object, int minPts, MoleculeList neighbours) {
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
    return computeGeneratingDistance(minPts, volume, is3d(), xcoord.length);
  }

  /**
   * Compute the OPTICS generating distance assuming a uniform distribution in the data space.
   *
   * @param minPts the min points for a core object
   * @param volume the volume of the data space
   * @param is3d true for 3D, otherwise assume 2D
   * @param numberOfPoints the number of points in the data space
   * @return the generating distance
   */
  static float computeGeneratingDistance(int minPts, double volume, boolean is3d,
      int numberOfPoints) {
    // Taken from section 4.1 of the OPTICS paper.

    // Number of dimensions
    // d = 2
    // Volume of the data space (DS)
    final double volumeDataSpace = volume;
    // Expected k-nearest-neighbours
    final int k = minPts;

    // Compute the volume of the hypersphere required to contain k neighbours,
    // assuming a uniform spread.

    final double volumeHyperSphere = (volumeDataSpace / numberOfPoints) * k;

    if (is3d) {
      // Note: Volume S(r) for a 3D hypersphere = 4 * pi * r^3 / 3
      return (float) Math.cbrt(3 * volumeHyperSphere / (4 * Math.PI));
    }
    // Note: Volume S(r) for a 2D hypersphere = pi * r^2
    return (float) Math.sqrt(volumeHyperSphere / Math.PI);
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
    final float workingGeneratingDistance = moleculeSpace.generatingDistanceE;

    if (tracker != null) {
      tracker.log("Running DBSCAN ... Distance=%g, minPts=%d", workingGeneratingDistance, minPts);
      tracker.progress(0, xcoord.length);
    }

    // The generating distance (E) used in the paper is the maximum distance at which cluster
    // centres will be formed. This implementation uses the squared distance to avoid sqrt()
    // function calls.
    final float e = workingGeneratingDistance * workingGeneratingDistance;

    final int size = xcoord.length;
    final Molecule[] setOfObjects = moleculeSpace.setOfObjects;

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

  /**
   * Dbscan expand cluster.
   *
   * @param object the object
   * @param generatingDistance the generating distance
   * @param minPts the min pts
   * @param counter the counter
   * @param seeds the seeds
   * @return true, if successful
   */
  private boolean dbscanExpandCluster(Molecule object, float generatingDistance, int minPts,
      Counter counter, MoleculeQueue seeds) {
    moleculeSpace.findNeighbours(minPts, object, generatingDistance);
    if (counter.increment()) {
      return true;
    }

    object.markProcessed();
    object.setNumberOfPoints(moleculeSpace.neighbours.size);
    if (moleculeSpace.neighbours.size >= minPts) {
      // New cluster
      final int clusterId = counter.nextClusterId();
      object.setClusterOrigin(clusterId);

      // Expand through the grid.neighbours
      seeds.clear();
      dbscanUpdateSearch(seeds, moleculeSpace.neighbours, clusterId);

      while (seeds.hasNext()) {
        final Molecule nextObject = seeds.getNext();
        moleculeSpace.findNeighbours(minPts, nextObject, generatingDistance);
        if (counter.increment()) {
          return true;
        }

        nextObject.markProcessed();
        nextObject.setNumberOfPoints(moleculeSpace.neighbours.size);
        if (moleculeSpace.neighbours.size >= minPts) {
          dbscanUpdateSearch(seeds, moleculeSpace.neighbours, clusterId);
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
   * Gets the raw z data.
   *
   * @return the raw z data
   */
  float[] getZData() {
    return zcoord;
  }

  /**
   * Checks if the coordinates are 3D.
   *
   * @return true if 3D, otherwise 2D
   * @see #getData()
   */
  public boolean is3d() {
    return zcoord != null;
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
   * Gets the minimum Z.
   *
   * @return the minimum Z
   */
  public float getMinimumZ() {
    return minZCoord;
  }

  /**
   * Gets the maximum Z.
   *
   * @return the maximum Z
   */
  public float getMaximumZ() {
    return maxZCoord;
  }

  @Override
  public float[][] getData() {
    if (is3d()) {
      return new float[][] {xcoord.clone(), ycoord.clone(), zcoord.clone()};
    }
    return super.getData();
  }

  @Override
  public double[][] getDoubleData() {
    if (is3d()) {
      final double[] x = new double[xcoord.length];
      final double[] y = new double[xcoord.length];
      final double[] z = new double[xcoord.length];
      for (int i = x.length; i-- > 0;) {
        x[i] = xcoord[i];
        y[i] = ycoord[i];
        z[i] = zcoord[i];
      }
      return new double[][] {x, y, z};
    }
    return super.getDoubleData();
  }

  /**
   * Compute (a sample of) the k-nearest neighbour distance for objects from the data. The plot of
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
    if (size <= 1) {
      // No neighbours for single (or zero) points
      return ArrayUtils.EMPTY_FLOAT_ARRAY;
    }

    long time = System.currentTimeMillis();

    // Bounds check k
    int numNeighbours = MathUtils.clip(1, size - 1, numberOfNeighbours);

    // Optionally compute all samples if samples is not positive
    final int sampleSize = Math.min((samples <= 0) ? size : samples, size);
    final float[] d = new float[sampleSize];

    if (tracker != null) {
      tracker.log("Computing %d nearest-neighbour distances, samples=%d", numNeighbours,
          sampleSize);
      tracker.progress(0, sampleSize);
    }

    int[] indices;
    if (sampleSize == size) {
      // Compute all
      indices = SimpleArrayUtils.natural(sampleSize);
    } else {
      // Random sample
      indices = new PermutationSampler(UniformRandomProviders.create(), size, sampleSize).sample();
    }

    // Use a KDtree to allow search of the space
    if (tree == null) {
      if (is3d()) {
        tree = KdTrees.newFloatKdTree(3);
        for (int i = 0; i < size; i++) {
          tree.add(new float[] {xcoord[i], ycoord[i], zcoord[i]});
        }
      } else {
        tree = KdTrees.newFloatKdTree(2);
        for (int i = 0; i < size; i++) {
          tree.add(new float[] {xcoord[i], ycoord[i]});
        }
      }
    }

    // Note: The k-nearest neighbour search will include the actual point so increment by 1
    numNeighbours++;

    final double[] location = new double[tree.dimensions()];
    final FloatDistanceFunction distanceFunction =
        FloatDistanceFunctions.squaredEuclidean(tree.dimensions());
    IntConsumer locationSetup;
    if (is3d()) {
      locationSetup = i -> {
        location[0] = xcoord[i];
        location[1] = ycoord[i];
        location[2] = zcoord[i];
      };
    } else {
      locationSetup = i -> {
        location[0] = xcoord[i];
        location[1] = ycoord[i];
      };
    }

    final DoubleList tmp = new DoubleList(numNeighbours);
    for (int i = 0; i < sampleSize; i++) {
      if (tracker != null) {
        tracker.progress(i, sampleSize);
      }
      locationSetup.accept(indices[i]);
      tmp.clear();
      tree.nearestNeighbours(location, numNeighbours, false, distanceFunction,
          dist -> tmp.add(dist));
      // The tree will use the squared distance so compute the root.
      // This assumes the first result is the maximum
      d[i] = (float) (Math.sqrt(tmp.get(0)));
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
   * <p>This implementation is based on the reference version in the <a
   * href="https://elki-project.github.io/">ELKI framework</a> and adapted for fast processing of 2D
   * data from a confined region, i.e. data that can be efficiently assigned to a grid.
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
   * <p>This implementation is based on the reference version in the <a
   * href="https://elki-project.github.io/">ELKI framework</a> and adapted for fast processing of 2D
   * data from a confined region, i.e. data that can be efficiently assigned to a grid.
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
              + "approxSets=%b, sampleMode=%s",
          minPts, numSplits, numProjections, useRandomVectors, saveApproximateSets, sampleMode);
    }

    // Compute projections and find neighbours
    final ProjectedMoleculeSpace space = (ProjectedMoleculeSpace) moleculeSpace;

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
    final Molecule[] setOfObjects = moleculeSpace.setOfObjects;

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
      final int nClusters = optics.extractDbscanClustering(moleculeSpace.generatingDistanceE);
      if (tracker != null) {
        final long end = System.currentTimeMillis();
        time = end - time;
        time2 = end - time2;
        tracker.log("Finished OPTICS: %d %s @ %s (Time = %s)", nClusters,
            pleuraliseClusterCount(nClusters), MathUtils.rounded(moleculeSpace.generatingDistanceE),
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
    moleculeSpace.findNeighbours(minPts, object, 0);
    object.markProcessed();
    if (orderedFile.add(object)) {
      return true;
    }

    if (object.coreDistance != UNDEFINED) {
      // Create seed-list for further expansion.
      fillWithComputeDistance(orderSeeds, moleculeSpace.neighbours, object);

      while (orderSeeds.hasNext()) {
        final Molecule nextObject = orderSeeds.next();
        moleculeSpace.findNeighbours(minPts, nextObject, 0);
        nextObject.markProcessed();
        if (orderedFile.add(nextObject)) {
          return true;
        }

        if (nextObject.coreDistance != UNDEFINED) {
          updateWithComputeDistance(orderSeeds, moleculeSpace.neighbours, nextObject);
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
  private void fillWithComputeDistance(OpticsPriorityQueue orderSeeds, MoleculeList neighbours,
      Molecule centreObject) {
    orderSeeds.clear();

    final float coreDist = centreObject.coreDistance;
    for (int i = neighbours.size; i-- > 0;) {
      final Molecule object = neighbours.get(i);
      if (object.isNotProcessed()) {
        // This is new so add it to the list
        object.reachabilityDistance =
            max(coreDist, (float) distanceFunction.applyAsDouble(object, centreObject));
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
  private void updateWithComputeDistance(OpticsPriorityQueue orderSeeds, MoleculeList neighbours,
      Molecule centreObject) {
    final float coreDist = centreObject.coreDistance;
    for (int i = neighbours.size; i-- > 0;) {
      final Molecule object = neighbours.get(i);
      if (object.isNotProcessed()) {
        final float newReachabilityDistance =
            max(coreDist, (float) distanceFunction.applyAsDouble(object, centreObject));
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
   * Gets (or creates) the random generator used for FastOPTICS.
   *
   * @return the random generator
   */
  private UniformRandomProvider getRandomGenerator() {
    if (seed == 0) {
      return UniformRandomProviders.create();
    }
    return UniformRandomProviders.create(seed);
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
    if (size <= 1) {
      // No neighbours for single (or zero) point
      return new float[size];
    }

    final long startTime = System.currentTimeMillis();

    // Bounds check k
    final int safeNumberOfNeighbours = MathUtils.clip(1, size - 1, numberOfNeighbours);

    if (tracker != null) {
      tracker.log("Computing Local Outlier Probability scores, k=%d, Lambda=%s",
          safeNumberOfNeighbours, MathUtils.rounded(lambda));
    }

    if (loopObject == null) {
      loopObject = is3d() ? new LoOp(xcoord, ycoord, zcoord) : new LoOp(xcoord, ycoord);
    }
    loopObject.setExecutorService(getExecutorService());

    try {
      final double[] scores = loopObject.run(safeNumberOfNeighbours, lambda);

      // Convert to float
      final float[] result = SimpleArrayUtils.toFloat(scores);

      if (tracker != null) {
        final long time = System.currentTimeMillis() - startTime;
        tracker.log("Finished LoOP computation (Time = " + TextUtils.millisToString(time) + ")");
      }

      return result;
    } catch (final InterruptedException ex) {
      // Restore interrupted state...
      Thread.currentThread().interrupt();
      handleLoopException(ex);
    } catch (final ExecutionException ex) {
      handleLoopException(ex);
    } finally {
      if (!cache) {
        loopObject = null;
      }
    }
    // Fall through from exceptions to return null
    return null;
  }

  /**
   * Handle loop exception.
   *
   * @param ex the ex
   */
  private void handleLoopException(Exception ex) {
    if (tracker != null) {
      tracker.log("Failed LoOP computation: " + ex.getMessage());
    }
    Logger.getLogger(getClass().getName()).log(Level.WARNING, ex,
        () -> "Failed LoOP computation: " + ex.getMessage());
  }

  /**
   * Sets the executor service used by multi-threaded algorithms (FastOPTICS and LoOP).
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
    return executorService;
  }
}
