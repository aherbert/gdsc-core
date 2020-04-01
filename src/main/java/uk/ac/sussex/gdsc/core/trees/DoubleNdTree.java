/*
 * Copyright 2009 Rednaxela
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
class DoubleNdTree implements DoubleKdTree {
  /** The bucket size. */
  private static final int BUCKET_SIZE = 24;

  // Root only

  /**
   * The maximum depth of the tree. This is the maximum number of parents from a leaf node back to
   * the root. It can be used to optimise the storage of the search status for tree nodes.
   */
  private int maximumDepth;

  // All types

  /** The dimensions. */
  private final int dimensions;
  /** The parent. */
  private final DoubleNdTree parent;
  /** The dimension weight function. */
  private final IntToDoubleFunction dimensionWeight;

  // Leaf only

  /** The locations. */
  private double[][] locations;
  /**
   * The location count. If a leaf this is the count of locations. If a stem this is a count of all
   * position below the current node.
   *
   * <p>The logic during tree construction ensures this is never zero for a node unless it is the
   * root. This is done by only splitting when the division into left and right creates non-empty
   * children.
   */
  private int locationCount;

  // Stem only

  /** The left child. */
  private DoubleNdTree left;
  /** The right child. */
  private DoubleNdTree right;
  /** The dimension for the split. */
  private int splitDimension;
  /** The value to split into left and right. */
  private double splitValue;

  // Bounds

  /** The minimum limit of the points in the tree below the current node/leaf. */
  private double[] minLimit;
  /** The maximum limit of the points in the tree below the current node/leaf. */
  private double[] maxLimit;
  /** The singularity flag. Set to true if all points have the same location. */
  private boolean singularity = true;

  /**
   * Construct a tree with a given number of dimensions.
   *
   * <p>The weight function is used to scale values in each dimension to a common scale. The
   * dimension with the largest range is used to split the data when a dividing a tree leaf.
   *
   * @param dimensions the dimensions
   * @param dimensionWeight the dimension weight
   */
  DoubleNdTree(int dimensions, IntToDoubleFunction dimensionWeight) {
    this.dimensions = dimensions;
    this.dimensionWeight = dimensionWeight;

    // Init as leaf
    this.locations = new double[BUCKET_SIZE][];

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
  private DoubleNdTree(DoubleNdTree parent, double[][] locations, int locationCount) {
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

  @Override
  public void addPoint(double[] location) {
    DoubleNdTree cursor = this;

    // Descend the tree until a leaf
    while (cursor.locations == null) {
      cursor = updateAndDescend(cursor, location);
    }

    // At a leaf. Check if enough room.
    if (cursor.locationCount == cursor.locations.length) {
      // Split the leaf
      cursor.splitDimension = cursor.findWidestAxis();

      // Don't split node if it has no width in any axis.
      // Double the bucket size instead.
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
        final double[][] leftLocations = cursor.locations;
        final double[][] rightLocations = new double[size][];

        // Move locations into children
        int right = 0;
        int left = 0;
        for (int i = 0; i < size; i++) {
          final double[] oldLocation = leftLocations[i];
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
          cursor.left = new DoubleNdTree(cursor, leftLocations, left);
          cursor.right = new DoubleNdTree(cursor, rightLocations, right);
          cursor.locations = null;
          // Note: the locationCount is not reset. It switches to a count of all items
          // below the tree node.

          // New children were added.
          // Update the maximum depth of the tree.
          int depth = 1;
          for (DoubleNdTree parent = cursor.parent; parent != null; parent = parent.parent) {
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
    cursor.initialiseOrExtendBounds(location);
  }

  /**
   * Update the current tree node with the location (update bounds) and descend to the left or right
   * child.
   *
   * @param current the current position (must be a stem node)
   * @param location the location
   * @return the child
   */
  private static DoubleNdTree updateAndDescend(DoubleNdTree current, double[] location) {
    current.locationCount++;
    current.initialiseOrExtendBounds(location);
    return location[current.splitDimension] > current.splitValue ? current.right : current.left;
  }

  /**
   * Extends the bounds of this node to include a new location.
   *
   * @param location the location
   */
  private final void initialiseOrExtendBounds(double[] location) {
    if (minLimit == null) {
      initialiseBounds(location);
    } else {
      extendBounds(location);
    }
  }

  /**
   * Initialise the bounds of this node to the location.
   *
   * @param location the location
   */
  private final void initialiseBounds(double[] location) {
    minLimit = new double[dimensions];
    System.arraycopy(location, 0, minLimit, 0, dimensions);
    maxLimit = minLimit.clone();
  }

  /**
   * Extends the bounds of this node to include a new location.
   *
   * @param location the location
   */
  private final void extendBounds(double[] location) {
    for (int i = 0; i < dimensions; i++) {
      if (Double.isNaN(location[i])) {
        minLimit[i] = Double.NaN;
        maxLimit[i] = Double.NaN;
        singularity = false;
      } else if (minLimit[i] > location[i]) {
        minLimit[i] = location[i];
        singularity = false;
      } else if (maxLimit[i] < location[i]) {
        maxLimit[i] = location[i];
        singularity = false;
      }
    }
  }

  /**
   * Find the widest axis of the bounds of this node.
   *
   * @return the axis
   */
  private final int findWidestAxis() {
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
      DoubleDistanceFunction distanceFunction, DoubleConsumer results) {
    if (locationCount == 0 || count < 1) {
      return false;
    }

    // Current point in tree
    DoubleNdTree cursor = this;
    // The status of the path taken to the point in the tree
    final StatusStack searchStatus = new StatusStack(maximumDepth);
    Status status = Status.NONE;
    double range = Double.POSITIVE_INFINITY;
    final DoubleMinHeap resultHeap = new DoubleMinHeap(count);

    do {
      if (status == Status.ALLVISITED) {
        // At a fully visited part. Move up the tree
        cursor = cursor.parent;
        status = searchStatus.pop();
        continue;
      }

      if (cursor.locations != null) {
        // At a leaf. Use the data.
        if (cursor.singularity) {
          final double dist = distanceFunction.distance(location, cursor.locations[0]);
          if (dist <= range) {
            for (int i = 0; i < cursor.locationCount; i++) {
              resultHeap.offer(dist);
            }
          }
        } else {
          for (int i = 0; i < cursor.locationCount; i++) {
            final double dist = distanceFunction.distance(location, cursor.locations[i]);
            resultHeap.offer(dist);
          }
        }
        range = resultHeap.getThreshold();

        // If at the root of the tree then stop
        if (cursor.parent == null) {
          break;
        }
        // Ascend back up the tree
        cursor = cursor.parent;
        status = searchStatus.pop();
        continue;
      }

      // Going to descend
      DoubleNdTree nextCursor;
      if (status == Status.NONE) {
        // At a fresh node, descend the most probably useful direction
        if (location[cursor.splitDimension] > cursor.splitValue) {
          // Descend right
          nextCursor = cursor.right;
          status = Status.RIGHTVISITED;
        } else {
          // Descend left
          nextCursor = cursor.left;
          status = Status.LEFTVISITED;
        }
      } else {
        if (status == Status.LEFTVISITED) {
          // Left node visited, descend right.
          nextCursor = cursor.right;
          status = Status.ALLVISITED;
        } else {
          // Right node visited, descend left.
          nextCursor = cursor.left;
          status = Status.ALLVISITED;
        }
        // Check if it's worth descending.
        if (distanceFunction.distanceToRectangle(location, nextCursor.minLimit,
            nextCursor.maxLimit) > range) {
          continue;
        }
      }

      // Descend down the tree
      cursor = nextCursor;
      searchStatus.push(status);
      status = Status.NONE;
    } while (cursor.parent != null || status != Status.ALLVISITED);

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
      DoubleDistanceFunction distanceFunction, DoubleConsumer results) {
    if (locationCount == 0) {
      return false;
    }

    DoubleNdTree cursor = this;
    // The status of the path taken to the point in the tree
    final StatusStack searchStatus = new StatusStack(maximumDepth);
    Status status = Status.NONE;
    boolean found = false;

    do {
      if (status == Status.ALLVISITED) {
        // At a fully visited part. Move up the tree
        cursor = cursor.parent;
        status = searchStatus.pop();
        continue;
      }

      if (cursor.locations != null) {
        // At a leaf. Use the data.
        if (cursor.singularity) {
          final double dist = distanceFunction.distance(location, cursor.locations[0]);
          if (dist <= range) {
            for (int i = 0; i < cursor.locationCount; i++) {
              results.accept(dist);
            }
            found = true;
          }
        } else {
          for (int i = 0; i < cursor.locationCount; i++) {
            final double dist = distanceFunction.distance(location, cursor.locations[i]);
            if (dist <= range) {
              results.accept(dist);
              found = true;
            }
          }
        }

        if (cursor.parent == null) {
          break;
        }
        cursor = cursor.parent;
        status = searchStatus.pop();
        continue;
      }

      // Going to descend
      DoubleNdTree nextCursor;
      if (status == Status.NONE) {
        // At a fresh node, descend the most probably useful direction
        if (location[cursor.splitDimension] > cursor.splitValue) {
          // Descend right
          nextCursor = cursor.right;
          status = Status.RIGHTVISITED;
        } else {
          // Descend left
          nextCursor = cursor.left;
          status = Status.LEFTVISITED;
        }
      } else {
        if (status == Status.LEFTVISITED) {
          // Left node visited, descend right.
          nextCursor = cursor.right;
          status = Status.ALLVISITED;
        } else {
          // Right node visited, descend left.
          nextCursor = cursor.left;
          status = Status.ALLVISITED;
        }
        // Check if it's worth descending.
        if (distanceFunction.distanceToRectangle(location, nextCursor.minLimit,
            nextCursor.maxLimit) > range) {
          continue;
        }
      }

      // Descend down the tree
      cursor = nextCursor;
      searchStatus.push(status);
      status = Status.NONE;
    } while (cursor.parent != null || status != Status.ALLVISITED);

    return found;
  }

  @Override
  public double nearestNeighbour(double[] location, DoubleDistanceFunction distanceFunction,
      DoubleConsumer result) {
    if (locationCount == 0) {
      return 0;
    }

    DoubleNdTree cursor = this;
    final StatusStack searchStatus = new StatusStack(maximumDepth);
    Status status = Status.NONE;
    double range = Double.POSITIVE_INFINITY;
    boolean found = false;

    do {
      if (status == Status.ALLVISITED) {
        // At a fully visited part. Move up the tree
        cursor = cursor.parent;
        status = searchStatus.pop();
        continue;
      }

      if (cursor.locations != null) {
        // At a leaf. Use the data.
        if (cursor.singularity) {
          final double dist = distanceFunction.distance(location, cursor.locations[0]);
          if (dist <= range) {
            range = dist;
            found = true;
          }
        } else {
          for (int i = 0; i < cursor.locationCount; i++) {
            final double dist = distanceFunction.distance(location, cursor.locations[i]);
            if (dist <= range) {
              range = dist;
              found = true;
            }
          }
        }

        if (cursor.parent == null) {
          break;
        }
        cursor = cursor.parent;
        status = searchStatus.pop();
        continue;
      }

      // Going to descend
      DoubleNdTree nextCursor;
      if (status == Status.NONE) {
        // At a fresh node, descend the most probably useful direction
        if (location[cursor.splitDimension] > cursor.splitValue) {
          // Descend right
          nextCursor = cursor.right;
          status = Status.RIGHTVISITED;
        } else {
          // Descend left
          nextCursor = cursor.left;
          status = Status.LEFTVISITED;
        }
      } else {
        if (status == Status.LEFTVISITED) {
          // Left node visited, descend right.
          nextCursor = cursor.right;
          status = Status.ALLVISITED;
        } else {
          // Right node visited, descend left.
          nextCursor = cursor.left;
          status = Status.ALLVISITED;
        }
        // Check if it's worth descending.
        if (distanceFunction.distanceToRectangle(location, nextCursor.minLimit,
            nextCursor.maxLimit) > range) {
          continue;
        }
      }

      // Descend down the tree
      cursor = nextCursor;
      searchStatus.push(status);
      status = Status.NONE;
    } while (cursor.parent != null || status != Status.ALLVISITED);

    if (!found) {
      // Invalid distance
      return Double.NaN;
    }

    if (result != null) {
      result.accept(range);
    }
    return range;
  }
}