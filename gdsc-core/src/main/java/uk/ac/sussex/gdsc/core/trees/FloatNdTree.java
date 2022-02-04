/*
 * Copyright 2009 Rednaxela
 * Copyright 2019-2022 Alex Herbert
 *
 * This software is provided 'as-is', without any express or implied warranty. In no event will the
 * authors be held liable for any damages arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose, including commercial
 * applications, and to alter it and redistribute it freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not claim that you wrote the
 * original software. If you use this software in a product, an acknowledgment in the product
 * documentation would be appreciated but is not required.
 *
 * 2. This notice may not be removed or altered from any source distribution.
 */

package uk.ac.sussex.gdsc.core.trees;

import java.util.Arrays;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntToDoubleFunction;
import uk.ac.sussex.gdsc.core.trees.heaps.DoubleMinHeap;

/**
 * An efficient well-optimized kd-tree. The point coordinates use {@code double} and each entry can
 * store an associated {@code int} item.
 *
 * <p>The search methods are safe for concurrent use under the assumption that the tree does not
 * have any structural modification during the search (i.e. concurrent additions).
 */
class FloatNdTree implements FloatKdTree {
  /** The bucket size. */
  private static final int BUCKET_SIZE = 24;

  // All types

  /** The dimensions. */
  private final int dimensions;
  /** The parent. */
  private final FloatNdTree parent;
  /**
   * The location count. If a leaf this is the count of locations. If a stem this is a count of all
   * position below the current node.
   *
   * <p>The logic during tree construction ensures this is never zero for a node unless it is the
   * root. This is done by only splitting when the division into left and right creates non-empty
   * children.
   */
  private int locationCount;

  // Bounds

  /** The minimum limit of the points in the tree below the current node/leaf. */
  private float[] minLimit;
  /** The maximum limit of the points in the tree below the current node/leaf. */
  private float[] maxLimit;

  // Root only

  /**
   * The maximum depth of the tree. This is the maximum number of parents from a leaf node back to
   * the root. It can be used to optimise the storage of the search status for tree nodes.
   */
  private int maximumDepth;
  /** The dimension weight function. */
  private final IntToDoubleFunction dimensionWeight;

  // Leaf only

  /** The locations. */
  private float[][] locations;

  // Stem only

  /** The left child. */
  private FloatNdTree left;
  /** The right child. */
  private FloatNdTree right;
  /** The dimension for the split. */
  private int splitDimension;
  /** The value to split into left and right. */
  private double splitValue;

  /**
   * Construct a tree with a given number of dimensions.
   *
   * <p>The weight function is used to scale values in each dimension to a common scale. The
   * dimension with the largest range is used to split the data when a dividing a tree leaf.
   *
   * @param dimensions the dimensions
   * @param dimensionWeight the dimension weight
   */
  FloatNdTree(int dimensions, IntToDoubleFunction dimensionWeight) {
    this.dimensions = dimensions;
    this.dimensionWeight = dimensionWeight;

    // Init as leaf
    this.locations = new float[BUCKET_SIZE][];

    // Init as root
    this.parent = null;
  }

  /**
   * Constructor for child nodes. Internal use only.
   *
   * @param parent the parent
   * @param locations the locations
   * @param locationCount the location count (must be non-zero)
   */
  private FloatNdTree(FloatNdTree parent, float[][] locations, int locationCount) {
    this.dimensions = parent.dimensions;
    this.dimensionWeight = parent.dimensionWeight;

    // Init as leaf
    this.locations = locations;
    this.locationCount = locationCount;

    // Init as non-root
    this.parent = parent;

    // Update the bounds
    initialiseBounds(locations[0]);
    for (int i = 1; i < locationCount; i++) {
      extendBounds(locations[i]);
    }
  }

  @Override
  public int dimensions() {
    return dimensions;
  }

  @Override
  public int size() {
    return locationCount;
  }

  /**
   * {@inheritDoc}
   *
   * <p>The location is stored by reference.
   */
  @Override
  public void add(float[] location) {
    addPoint(location, (cursor, p) -> false);
  }

  /**
   * {@inheritDoc}.
   *
   * <p>Location equality uses the {@code ==} operator on each coordinate, thus {@code -0.0} and
   * {@code 0.0} are considered equal.
   *
   * <p>The location is stored by reference.
   */
  @Override
  public boolean addIfAbsent(float[] location) {
    final BiPredicate<float[], float[]> equality = FloatArrayPredicates.equals(dimensions);
    return addPoint(location, (cursor, p) -> {
      for (int i = 0; i < cursor.locationCount; i++) {
        if (equality.test(location, cursor.locations[i])) {
          return true;
        }
      }
      return false;
    });
  }

  /**
   * Adds the point if not already present at the leaf node.
   *
   * @param location the location
   * @param value the value
   * @param filter the filter to test if the point is already present at the leaf node
   * @return true if added
   */
  private boolean addPoint(float[] location, BiPredicate<FloatNdTree, float[]> filter) {
    // Special case if empty where the bounds can just be initialised.
    if (locationCount == 0) {
      locations[locationCount] = location;
      locationCount = 1;
      initialiseBounds(location);
      return true;
    }

    FloatNdTree cursor = this;

    // Descend the tree until a leaf
    while (cursor.locations == null) {
      cursor = updateAndDescend(cursor, location);
    }

    // At a leaf. Check if present.
    if (filter.test(cursor, location)) {
      // Remove location counts added for the duplicate
      ascendAndDecrement(cursor);
      return false;
    }
    // Check if enough room.
    if (cursor.locationCount == cursor.locations.length) {
      // Split the leaf
      cursor.splitDimension = cursor.findWidestAxis();

      // Don't split node if it has no width in any axis.
      // Float the bucket size instead.
      if (cursor.minLimit[cursor.splitDimension] == cursor.maxLimit[cursor.splitDimension]) {
        final int newLength = cursor.locations.length * 2;
        cursor.locations = Arrays.copyOf(cursor.locations, newLength);
      } else {
        final double splitValue = cursor.splitValue = SplitStrategies.computeSplitValue(
            cursor.minLimit[cursor.splitDimension], cursor.maxLimit[cursor.splitDimension]);

        final int size = cursor.locationCount;
        final int dim = cursor.splitDimension;

        // Create child leaves.
        // Recycle storage space.
        final float[][] leftLocations = cursor.locations;
        final float[][] rightLocations = new float[size][];

        // Move locations into children
        int right = 0;
        int left = 0;
        for (int i = 0; i < size; i++) {
          final float[] oldLocation = leftLocations[i];
          if (oldLocation[dim] > splitValue) {
            // Right
            rightLocations[right] = oldLocation;
            right++;
          } else {
            // Left
            leftLocations[left] = oldLocation;
            left++;
          }
        }

        // Edge case for a bad split with NaN values in the locations.
        // Only check the left since minLimit != maxLimit it is not possible for all locations
        // to be '> splitValue' if the range was non-NaN:
        // [nan, x] = 0 => left can be zero
        // [x, nan] = 0 => left can be zero
        // [-inf, -max] = -max => left is zero
        // [max, inf] = max => left is non-zero
        // [-inf, inf] = 0 => left is non-zero
        if (left == size) {
          // Note:
          // This make happen continuously if the locations are all <= zero in the
          // dimension chosen for the split, for example if a single location was NaN
          // in all dimensions the widest axis is always 0 and the split value is 0.
          final int newLength = cursor.locations.length * 2;
          cursor.locations = Arrays.copyOf(cursor.locations, newLength);
        } else {
          // Make into stem. This should only happen when both left and right are not zero.
          cursor.left = new FloatNdTree(cursor, leftLocations, left);
          cursor.right = new FloatNdTree(cursor, rightLocations, right);
          cursor.locations = null;
          // Note: the locationCount is not reset. It switches to a count of all items
          // below the tree node.

          // New children were added.
          // Update the maximum depth of the tree.
          int depth = 1;
          for (FloatNdTree parent = cursor.parent; parent != null; parent = parent.parent) {
            depth++;
          }
          if (depth > maximumDepth) {
            maximumDepth = depth;
          }

          // Descend the new stem
          cursor = updateAndDescend(cursor, location);
        }
      }
    }

    // Add to the leaf
    cursor.locations[cursor.locationCount] = location;
    cursor.locationCount++;
    cursor.extendBounds(location);
    return true;
  }

  /**
   * Update the current tree node with the location (update bounds) and descend to the left or right
   * child.
   *
   * @param current the current position (must be a stem node)
   * @param location the location
   * @return the child
   */
  private static FloatNdTree updateAndDescend(FloatNdTree current, float[] location) {
    current.locationCount++;
    current.extendBounds(location);
    return location[current.splitDimension] > current.splitValue ? current.right : current.left;
  }

  /**
   * Ascend from the current tree node through the tree and decrement the location count of each
   * node.
   *
   * @param current the current position
   */
  private static void ascendAndDecrement(FloatNdTree current) {
    while (current.parent != null) {
      current = current.parent;
      current.locationCount--;
    }
  }

  /**
   * Initialise the bounds of this node to the location.
   *
   * @param location the location
   */
  private void initialiseBounds(float[] location) {
    minLimit = new float[dimensions];
    System.arraycopy(location, 0, minLimit, 0, dimensions);
    maxLimit = minLimit.clone();
  }

  /**
   * Extends the bounds of this node to include a new location.
   *
   * @param location the location
   */
  private void extendBounds(float[] location) {
    for (int i = 0; i < dimensions; i++) {
      if (Float.isNaN(location[i])) {
        minLimit[i] = Float.NaN;
        maxLimit[i] = Float.NaN;
      } else if (minLimit[i] > location[i]) {
        minLimit[i] = location[i];
      } else if (maxLimit[i] < location[i]) {
        maxLimit[i] = location[i];
      }
    }
  }

  /**
   * Find the widest axis of the bounds of this node.
   *
   * @return the axis
   */
  private int findWidestAxis() {
    int widest = 0;
    double width = (maxLimit[0] - minLimit[0]) * dimensionWeight.applyAsDouble(0);
    if (Double.isNaN(width)) {
      width = 0;
    }
    for (int i = 1; i < dimensions; i++) {
      double nwidth = (maxLimit[i] - minLimit[i]) * dimensionWeight.applyAsDouble(i);
      if (Double.isNaN(nwidth)) {
        nwidth = 0;
      }
      if (nwidth > width) {
        widest = i;
        width = nwidth;
      }
    }
    return widest;
  }

  @Override
  public boolean nearestNeighbours(double[] location, int count, boolean sorted,
      FloatDistanceFunction distanceFunction, DoubleConsumer results) {
    if (locationCount == 0 || count < 1) {
      return false;
    }

    // Current point in tree
    FloatNdTree cursor = this;
    // The status of the path taken to the point in the tree
    final StatusStack searchStatus = StatusStack.create(maximumDepth);
    int status = Status.NONE;
    double range = Double.POSITIVE_INFINITY;
    final DoubleMinHeap resultHeap = new DoubleMinHeap(count);

    while (cursor != null) {
      if (status == Status.NONE) {
        // Drop down the unvisited tree until a leaf
        while (cursor.locations == null) {
          if (location[cursor.splitDimension] > cursor.splitValue) {
            cursor = cursor.right;
            searchStatus.push(Status.RIGHTVISITED);
          } else {
            cursor = cursor.left;
            searchStatus.push(Status.LEFTVISITED);
          }
        }

        // At a leaf. Use the data.
        for (int i = 0; i < cursor.locationCount; i++) {
          final double dist = distanceFunction.distance(location, cursor.locations[i]);
          resultHeap.offer(dist);
        }
        range = resultHeap.getThreshold();

        // Ascend back up the tree
        cursor = cursor.parent;
        status = searchStatus.pop();
      } else if (status == Status.ALLVISITED) {
        // At a fully visited part. Move up the tree
        cursor = cursor.parent;
        status = searchStatus.pop();
      } else {
        // Part visited, descend other direction
        final FloatNdTree nextCursor = status == Status.LEFTVISITED ? cursor.right : cursor.left;
        // Check if it's worth descending.
        if (distanceFunction.distanceToRectangle(location, nextCursor.minLimit,
            nextCursor.maxLimit) > range) {
          // Ascend back up the tree
          cursor = cursor.parent;
          status = searchStatus.pop();
        } else {
          searchStatus.push(Status.ALLVISITED);
          cursor = nextCursor;
          status = Status.NONE;
        }
      }
    }

    // Happens for example if the search point was NaN and the distance was NaN
    if (resultHeap.getSize() == 0) {
      return false;
    }

    if (sorted) {
      while (resultHeap.getSize() > 0) {
        results.accept(resultHeap.remove());
      }
    } else {
      for (int i = 0; i < resultHeap.getSize(); i++) {
        results.accept(resultHeap.getValue(i));
      }
    }

    return true;
  }

  @Override
  public boolean findNeighbours(double[] location, double range,
      FloatDistanceFunction distanceFunction, DoubleConsumer results) {
    if (locationCount == 0) {
      return false;
    }

    FloatNdTree cursor = this;
    // The status of the path taken to the point in the tree
    final StatusStack searchStatus = StatusStack.create(maximumDepth);
    int status = Status.NONE;
    boolean found = false;

    while (cursor != null) {
      if (status == Status.NONE) {
        // Drop down the unvisited tree until a leaf
        while (cursor.locations == null) {
          if (location[cursor.splitDimension] > cursor.splitValue) {
            cursor = cursor.right;
            searchStatus.push(Status.RIGHTVISITED);
          } else {
            cursor = cursor.left;
            searchStatus.push(Status.LEFTVISITED);
          }
        }

        // At a leaf. Use the data.
        for (int i = 0; i < cursor.locationCount; i++) {
          final double dist = distanceFunction.distance(location, cursor.locations[i]);
          if (dist <= range) {
            results.accept(dist);
            found = true;
          }
        }

        // Ascend back up the tree
        cursor = cursor.parent;
        status = searchStatus.pop();
      } else if (status == Status.ALLVISITED) {
        // At a fully visited part. Move up the tree
        cursor = cursor.parent;
        status = searchStatus.pop();
      } else {
        // Part visited, descend other direction
        final FloatNdTree nextCursor = status == Status.LEFTVISITED ? cursor.right : cursor.left;
        // Check if it's worth descending.
        if (distanceFunction.distanceToRectangle(location, nextCursor.minLimit,
            nextCursor.maxLimit) > range) {
          // Ascend back up the tree
          cursor = cursor.parent;
          status = searchStatus.pop();
        } else {
          searchStatus.push(Status.ALLVISITED);
          cursor = nextCursor;
          status = Status.NONE;
        }
      }
    }

    return found;
  }

  @Override
  public double nearestNeighbour(double[] location, FloatDistanceFunction distanceFunction,
      DoubleConsumer result) {
    if (locationCount == 0) {
      return 0;
    }

    FloatNdTree cursor = this;
    final StatusStack searchStatus = StatusStack.create(maximumDepth);
    int status = Status.NONE;
    double range = Double.POSITIVE_INFINITY;
    boolean found = false;

    while (cursor != null) {
      if (status == Status.NONE) {
        // Drop down the unvisited tree until a leaf
        while (cursor.locations == null) {
          if (location[cursor.splitDimension] > cursor.splitValue) {
            cursor = cursor.right;
            searchStatus.push(Status.RIGHTVISITED);
          } else {
            cursor = cursor.left;
            searchStatus.push(Status.LEFTVISITED);
          }
        }

        // At a leaf. Use the data.
        for (int i = 0; i < cursor.locationCount; i++) {
          final double dist = distanceFunction.distance(location, cursor.locations[i]);
          if (dist <= range) {
            range = dist;
            found = true;
          }
        }

        // Ascend back up the tree
        cursor = cursor.parent;
        status = searchStatus.pop();
      } else if (status == Status.ALLVISITED) {
        // At a fully visited part. Move up the tree
        cursor = cursor.parent;
        status = searchStatus.pop();
      } else {
        // Part visited, descend other direction
        final FloatNdTree nextCursor = status == Status.LEFTVISITED ? cursor.right : cursor.left;
        // Check if it's worth descending.
        if (distanceFunction.distanceToRectangle(location, nextCursor.minLimit,
            nextCursor.maxLimit) > range) {
          // Ascend back up the tree
          cursor = cursor.parent;
          status = searchStatus.pop();
        } else {
          searchStatus.push(Status.ALLVISITED);
          cursor = nextCursor;
          status = Status.NONE;
        }
      }
    }

    if (!found) {
      // Invalid distance
      return Double.NaN;
    }

    if (result != null) {
      result.accept(range);
    }
    return range;
  }

  @Override
  public void forEach(Consumer<float[]> action) {
    forEach(this, action);
  }

  private void forEach(FloatNdTree cursor, Consumer<float[]> action) {
    if (cursor.locations != null) {
      // Leaf node
      for (int i = 0; i < cursor.locationCount; i++) {
        action.accept(cursor.locations[i]);
      }
    } else {
      // Stem node
      forEach(cursor.left, action);
      forEach(cursor.right, action);
    }
  }
}
