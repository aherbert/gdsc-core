/*
 * Copyright 2009 Rednaxela
 *
 * Modifications to the code have been made by Alex Herbert for a smaller memory footprint and
 * optimised 2D processing for use with image data as part of the Genome Damage and Stability Centre
 * ImageJ Core Package.
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

package uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen2;

import java.util.ArrayList;
import java.util.List;
import uk.ac.sussex.gdsc.core.ags.utils.data.DistanceUtils;

/**
 * An efficient well-optimized kd-tree.
 *
 * <p>This is a basic copy of the KdTree class but has no ability to store object data.
 * Functionality to limit the tree size has been removed.
 *
 * @author Alex Herbert
 */
public abstract class SimpleKdTree extends SimpleKdTreeNode {
  /**
   * Construct a RTree with a given number of dimensions.
   *
   * @param dimensions the dimensions
   */
  public SimpleKdTree(int dimensions) {
    super(dimensions);
  }

  /**
   * Get the number of points in the tree.
   *
   * @return the size
   */
  public int size() {
    return locationCount;
  }

  /**
   * Add a point to the tree.
   *
   * @param location the location
   */
  public void addPoint(double[] location) {
    SimpleKdTreeNode cursor = this;

    while (cursor.locations == null || cursor.locationCount >= cursor.locations.length) {
      if (cursor.locations != null) {
        cursor.splitDimension = cursor.findWidestAxis();
        cursor.splitValue =
            (cursor.minLimit[cursor.splitDimension] + cursor.maxLimit[cursor.splitDimension]) * 0.5;

        // Never split on infinity or NaN
        if (cursor.splitValue == Double.POSITIVE_INFINITY) {
          cursor.splitValue = Double.MAX_VALUE;
        } else if (cursor.splitValue == Double.NEGATIVE_INFINITY) {
          cursor.splitValue = -Double.MAX_VALUE;
        } else if (Double.isNaN(cursor.splitValue)) {
          cursor.splitValue = 0;
        }

        // Don't split node if it has no width in any axis. Double the
        // bucket size instead
        if (cursor.minLimit[cursor.splitDimension] == cursor.maxLimit[cursor.splitDimension]) {
          final double[][] newLocations = new double[cursor.locations.length * 2][];
          System.arraycopy(cursor.locations, 0, newLocations, 0, cursor.locationCount);
          cursor.locations = newLocations;
          break;
        }

        // Don't let the split value be the same as the upper value as
        // can happen due to rounding errors!
        if (cursor.splitValue == cursor.maxLimit[cursor.splitDimension]) {
          cursor.splitValue = cursor.minLimit[cursor.splitDimension];
        }

        // Create child leaves
        final SimpleKdTreeNode left = new ChildNode(cursor);
        final SimpleKdTreeNode right = new ChildNode(cursor);

        // Move locations into children
        for (int i = 0; i < cursor.locationCount; i++) {
          final double[] oldLocation = cursor.locations[i];
          if (oldLocation[cursor.splitDimension] > cursor.splitValue) {
            // Right
            right.locations[right.locationCount] = oldLocation;
            right.locationCount++;
            right.extendBounds(oldLocation);
          } else {
            // Left
            left.locations[left.locationCount] = oldLocation;
            left.locationCount++;
            left.extendBounds(oldLocation);
          }
        }

        // Make into stem
        cursor.left = left;
        cursor.right = right;
        cursor.locations = null;
      }

      cursor.locationCount++;
      cursor.extendBounds(location);

      if (location[cursor.splitDimension] > cursor.splitValue) {
        cursor = cursor.right;
      } else {
        cursor = cursor.left;
      }
    }

    cursor.locations[cursor.locationCount] = location;
    cursor.locationCount++;
    cursor.extendBounds(location);
  }

  /**
   * Stores a distance and value to output.
   */
  public static class Entry {
    /** The distance. */
    private final double distance;

    /** The value. */
    private final double[] value;

    /**
     * Instantiates a new entry.
     *
     * @param distance the distance
     * @param value the value
     */
    public Entry(double distance, double[] value) {
      this.distance = distance;
      this.value = value;
    }

    /**
     * Gets the distance.
     *
     * @return the distance
     */
    public double getDistance() {
      return distance;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public double[] getValue() {
      return value;
    }
  }

  /**
   * Calculates the nearest 'count' points to 'location'.
   *
   * @param location the location
   * @param count the count
   * @param sequentialSorting the sequential sorting
   * @return the list
   */
  @SuppressWarnings({"null"})
  public List<Entry> nearestNeighbor(double[] location, int count, boolean sequentialSorting) {
    SimpleKdTreeNode cursor = this;
    cursor.status = Status.NONE;
    double range = Double.POSITIVE_INFINITY;
    final TDoubleHeap<double[]> resultHeap = new TDoubleHeap<>(count);

    do {
      if (cursor.status == Status.ALLVISITED) {
        // At a fully visited part. Move up the tree
        cursor = cursor.parent;
        continue;
      }

      if (cursor.status == Status.NONE && cursor.locations != null) {
        // At a leaf. Use the data.
        if (cursor.locationCount > 0) {
          if (cursor.singularity) {
            final double dist = pointDist(cursor.locations[0], location);
            if (dist <= range) {
              for (int i = 0; i < cursor.locationCount; i++) {
                resultHeap.addValue(dist, cursor.locations[i]);
              }
            }
          } else {
            for (int i = 0; i < cursor.locationCount; i++) {
              final double dist = pointDist(cursor.locations[i], location);
              resultHeap.addValue(dist, cursor.locations[i]);
            }
          }
          range = resultHeap.getMaxDist();
        }

        if (cursor.parent == null) {
          break;
        }
        cursor = cursor.parent;
        continue;
      }

      // Going to descend
      SimpleKdTreeNode nextCursor = null;
      if (cursor.status == Status.NONE) {
        // At a fresh node, descend the most probably useful direction
        if (location[cursor.splitDimension] > cursor.splitValue) {
          // Descend right
          nextCursor = cursor.right;
          cursor.status = Status.RIGHTVISITED;
        } else {
          // Descend left
          nextCursor = cursor.left;
          cursor.status = Status.LEFTVISITED;
        }
      } else if (cursor.status == Status.LEFTVISITED) {
        // Left node visited, descend right.
        nextCursor = cursor.right;
        cursor.status = Status.ALLVISITED;
      } else if (cursor.status == Status.RIGHTVISITED) {
        // Right node visited, descend left.
        nextCursor = cursor.left;
        cursor.status = Status.ALLVISITED;
      }

      // Check if it's worth descending. Assume it is if it's sibling has
      // not been visited yet.
      if (cursor.status == Status.ALLVISITED
          && (nextCursor.locationCount == 0 || (!nextCursor.singularity
              && pointRegionDist(location, nextCursor.minLimit, nextCursor.maxLimit) > range))) {
        continue;
      }

      // Descend down the tree
      cursor = nextCursor;
      cursor.status = Status.NONE;
    } while (cursor.parent != null || cursor.status != Status.ALLVISITED);

    final ArrayList<Entry> results = new ArrayList<>(resultHeap.getSize());
    if (sequentialSorting) {
      while (resultHeap.getSize() > 0) {
        resultHeap.removeLargest();
        results.add(new Entry(resultHeap.getRemovedDistance(), resultHeap.getRemovedData()));
      }
    } else {
      for (int i = 0; i < resultHeap.getSize(); i++) {
        results.add(new Entry(resultHeap.getDistance(i), (double[]) resultHeap.getData(i)));
      }
    }

    return results;
  }

  /**
   * Calculates the minimum distance of any point to 'location'.
   *
   * <p>In the special case that the tree is empty the distance is zero.
   *
   * @param location the location
   * @return the distance
   */
  @SuppressWarnings({"null"})
  public double minimumDistance(double[] location) {
    if (size() == 0) {
      return 0;
    }
    SimpleKdTreeNode cursor = this;
    cursor.status = Status.NONE;
    double range = Double.POSITIVE_INFINITY;

    do {
      if (cursor.status == Status.ALLVISITED) {
        // At a fully visited part. Move up the tree
        cursor = cursor.parent;
        continue;
      }

      if (cursor.status == Status.NONE && cursor.locations != null) {
        // At a leaf. Use the data.
        if (cursor.locationCount > 0) {
          if (cursor.singularity) {
            final double dist = pointDist(cursor.locations[0], location);
            if (dist < range) {
              range = dist;
            }
          } else {
            for (int i = 0; i < cursor.locationCount; i++) {
              final double dist = pointDist(cursor.locations[i], location);
              if (dist < range) {
                range = dist;
              }
            }
          }
        }

        if (cursor.parent == null) {
          break;
        }
        cursor = cursor.parent;
        continue;
      }

      // Going to descend
      SimpleKdTreeNode nextCursor = null;
      if (cursor.status == Status.NONE) {
        // At a fresh node, descend the most probably useful direction
        if (location[cursor.splitDimension] > cursor.splitValue) {
          // Descend right
          nextCursor = cursor.right;
          cursor.status = Status.RIGHTVISITED;
        } else {
          // Descend left
          nextCursor = cursor.left;
          cursor.status = Status.LEFTVISITED;
        }
      } else if (cursor.status == Status.LEFTVISITED) {
        // Left node visited, descend right.
        nextCursor = cursor.right;
        cursor.status = Status.ALLVISITED;
      } else if (cursor.status == Status.RIGHTVISITED) {
        // Right node visited, descend left.
        nextCursor = cursor.left;
        cursor.status = Status.ALLVISITED;
      }

      // Check if it's worth descending. Assume it is if it's sibling has
      // not been visited yet.
      if (cursor.status == Status.ALLVISITED
          && (nextCursor.locationCount == 0 || (!nextCursor.singularity
              && pointRegionDist(location, nextCursor.minLimit, nextCursor.maxLimit) > range))) {
        continue;
      }

      // Descend down the tree
      cursor = nextCursor;
      cursor.status = Status.NONE;
    } while (cursor.parent != null || cursor.status != Status.ALLVISITED);

    return range;
  }

  /**
   * Internal class for child nodes.
   */
  private class ChildNode extends SimpleKdTreeNode {
    /**
     * Instantiates a new child node.
     *
     * @param parent the parent
     */
    private ChildNode(SimpleKdTreeNode parent) {
      super(parent);
    }

    // Distance measurements are always called from the root node
    @Override
    protected double pointDist(double[] p1, double[] p2) {
      throw new IllegalStateException();
    }

    @Override
    protected double pointRegionDist(double[] point, double[] min, double[] max) {
      throw new IllegalStateException();
    }
  }

  /**
   * Class for tree with Unweighted Squared Euclidean distancing with no NaN
   * distance checking.
   */
  public static class SqrEuclid extends SimpleKdTree {
    /**
     * Create a new instance.
     *
     * @param dimensions the dimensions
     */
    public SqrEuclid(int dimensions) {
      super(dimensions);
    }

    @Override
    protected double pointDist(double[] p1, double[] p2) {
      double distance = 0;

      for (int i = 0; i < p1.length; i++) {
        final double diff = (p1[i] - p2[i]);
        distance += diff * diff;
      }

      return distance;
    }

    @Override
    protected double pointRegionDist(double[] point, double[] min, double[] max) {
      double distance = 0;

      for (int i = 0; i < point.length; i++) {
        double diff = 0;
        if (point[i] > max[i]) {
          diff = (point[i] - max[i]);
        } else if (point[i] < min[i]) {
          diff = (point[i] - min[i]);
        }

        distance += diff * diff;
      }

      return distance;
    }
  }

  /**
   * Class for tree with Unweighted Squared Euclidean distancing assuming 2 dimensions with no NaN
   * distance checking.
   *
   * <p>This is an optimised version for use in the GDSC Core project.
   *
   * @author Alex Herbert
   */
  public static class SqrEuclid2D extends SimpleKdTree {
    /**
     * Create a new instance.
     */
    public SqrEuclid2D() {
      super(2);
    }

    @Override
    protected double pointDist(double[] p1, double[] p2) {
      final double dx = p1[0] - p2[0];
      final double dy = p1[1] - p2[1];
      return dx * dx + dy * dy;
    }

    @Override
    protected double pointRegionDist(double[] point, double[] min, double[] max) {
      final double dx = DistanceUtils.getDistanceOutsideRange(point[0], min[0], max[0]);
      final double dy = DistanceUtils.getDistanceOutsideRange(point[1], min[1], max[1]);
      return dx * dx + dy * dy;
    }
  }

  /**
   * Class for tree with Unweighted Squared Euclidean distancing assuming 3 dimensions with no NaN
   * distance checking.
   *
   * <p>This is an optimised version for use in the GDSC Core project.
   *
   * @author Alex Herbert
   */
  public static class SqrEuclid3D extends SimpleKdTree {
    /**
     * Create a new instance.
     */
    public SqrEuclid3D() {
      super(3);
    }

    @Override
    protected double pointDist(double[] p1, double[] p2) {
      final double dx = p1[0] - p2[0];
      final double dy = p1[1] - p2[1];
      final double dz = p1[2] - p2[2];
      return dx * dx + dy * dy + dz * dz;
    }

    @Override
    protected double pointRegionDist(double[] point, double[] min, double[] max) {
      final double dx = DistanceUtils.getDistanceOutsideRange(point[0], min[0], max[0]);
      final double dy = DistanceUtils.getDistanceOutsideRange(point[1], min[1], max[1]);
      final double dz = DistanceUtils.getDistanceOutsideRange(point[2], min[2], max[2]);
      return dx * dx + dy * dy + dz * dz;
    }
  }
}