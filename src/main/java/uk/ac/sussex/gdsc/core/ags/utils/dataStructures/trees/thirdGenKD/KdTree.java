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
package uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.thirdGenKD;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;


import uk.ac.sussex.gdsc.core.ags.utils.dataStructures.BinaryHeap;
import uk.ac.sussex.gdsc.core.ags.utils.dataStructures.MaxHeap;
import uk.ac.sussex.gdsc.core.ags.utils.dataStructures.MinHeap;

/**
 * The Class KdTree.
 *
 * @param <T> the generic type
 */
public abstract class KdTree<T> extends KdNode<T> {
  /**
   * Instantiates a new kd tree.
   */
  public KdTree() {
    super(24);
  }

  /**
   * Instantiates a new kd tree.
   *
   * @param bucketCapacity the bucket capacity
   */
  public KdTree(int bucketCapacity) {
    super(bucketCapacity);
  }

  /**
   * Gets the nearest neighbor iterator.
   *
   * @param searchPoint the search point
   * @param maxPointsReturned the max points returned
   * @param distanceFunction the distance function
   * @return the nearest neighbor iterator
   */
  public NearestNeighborIterator<T> getNearestNeighborIterator(double[] searchPoint,
      int maxPointsReturned, DistanceFunction distanceFunction) {
    return new NearestNeighborIterator<>(this, searchPoint, maxPointsReturned, distanceFunction);
  }

  /**
   * Find nearest neighbors.
   *
   * @param searchPoint the search point
   * @param maxPointsReturned the max points returned
   * @param distanceFunction the distance function
   * @return the max heap
   */
  public MaxHeap<T> findNearestNeighbors(double[] searchPoint, int maxPointsReturned,
      DistanceFunction distanceFunction) {
    final BinaryHeap.Min<KdNode<T>> pendingPaths = new BinaryHeap.Min<>();
    final BinaryHeap.Max<T> evaluatedPoints = new BinaryHeap.Max<>();
    final int pointsRemaining = Math.min(maxPointsReturned, size());
    pendingPaths.offer(0, this);

    while (pendingPaths.size() > 0 && (evaluatedPoints.size() < pointsRemaining
        || (pendingPaths.getMinKey() < evaluatedPoints.getMaxKey()))) {
      nearestNeighborSearchStep(pendingPaths, evaluatedPoints, pointsRemaining, distanceFunction,
          searchPoint);
    }

    return evaluatedPoints;
  }

  /**
   * Perform a nearest neighbor search step.
   *
   * @param <T> the generic type
   * @param pendingPaths the pending paths
   * @param evaluatedPoints the evaluated points
   * @param desiredPoints the desired points
   * @param distanceFunction the distance function
   * @param searchPoint the search point
   */
  @SuppressWarnings("unchecked")
  protected static <T> void nearestNeighborSearchStep(MinHeap<KdNode<T>> pendingPaths,
      MaxHeap<T> evaluatedPoints, int desiredPoints, DistanceFunction distanceFunction,
      double[] searchPoint) {
    // If there are pending paths possibly closer than the nearest evaluated point, check it out
    KdNode<T> cursor = pendingPaths.getMin();
    pendingPaths.removeMin();

    // Descend the tree, recording paths not taken
    while (!cursor.isLeaf()) {
      KdNode<T> pathNotTaken;
      if (searchPoint[cursor.splitDimension] > cursor.splitValue) {
        pathNotTaken = cursor.left;
        cursor = cursor.right;
      } else {
        pathNotTaken = cursor.right;
        cursor = cursor.left;
      }
      final double otherDistance = distanceFunction.distanceToRect(searchPoint,
          pathNotTaken.minBound, pathNotTaken.maxBound);
      // Only add a path if we either need more points or it's closer than furthest point on list so
      // far
      if (evaluatedPoints.size() < desiredPoints || otherDistance <= evaluatedPoints.getMaxKey()) {
        pendingPaths.offer(otherDistance, pathNotTaken);
      }
    }

    if (cursor.singlePoint) {
      final double nodeDistance = distanceFunction.distance(cursor.points[0], searchPoint);
      // Only add a point if either need more points or it's closer than furthest on list so far
      if (evaluatedPoints.size() < desiredPoints || nodeDistance <= evaluatedPoints.getMaxKey()) {
        for (int i = 0; i < cursor.size(); i++) {
          final T value = (T) cursor.data[i];

          // If we don't need any more, replace max
          if (evaluatedPoints.size() == desiredPoints) {
            evaluatedPoints.replaceMax(nodeDistance, value);
          } else {
            evaluatedPoints.offer(nodeDistance, value);
          }
        }
      }
    } else {
      // Add the points at the cursor
      for (int i = 0; i < cursor.size(); i++) {
        final double[] point = cursor.points[i];
        final T value = (T) cursor.data[i];
        final double distance = distanceFunction.distance(point, searchPoint);
        // Only add a point if either need more points or it's closer than furthest on list so far
        if (evaluatedPoints.size() < desiredPoints) {
          evaluatedPoints.offer(distance, value);
        } else if (distance < evaluatedPoints.getMaxKey()) {
          evaluatedPoints.replaceMax(distance, value);
        }
      }
    }
  }
}
