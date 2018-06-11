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
package ags.utils.dataStructures.trees.thirdGenKD;

import ags.utils.dataStructures.BinaryHeap;
import ags.utils.dataStructures.MaxHeap;
import ags.utils.dataStructures.MinHeap;

/**
 *
 */
public abstract class KdTree<T> extends KdNode<T> {
    public KdTree() {
        super(24);
    }

    public KdTree(int bucketCapacity) {
        super(bucketCapacity);
    }

    public NearestNeighborIterator<T> getNearestNeighborIterator(double[] searchPoint, int maxPointsReturned, DistanceFunction distanceFunction) {
        return new NearestNeighborIterator<T>(this, searchPoint, maxPointsReturned, distanceFunction);
    }

    public MaxHeap<T> findNearestNeighbors(double[] searchPoint, int maxPointsReturned, DistanceFunction distanceFunction) {
        BinaryHeap.Min<KdNode<T>> pendingPaths = new BinaryHeap.Min<KdNode<T>>();
        BinaryHeap.Max<T> evaluatedPoints = new BinaryHeap.Max<T>();
        int pointsRemaining = Math.min(maxPointsReturned, size());
        pendingPaths.offer(0, this);

        while (pendingPaths.size() > 0 && (evaluatedPoints.size() < pointsRemaining || (pendingPaths.getMinKey() < evaluatedPoints.getMaxKey()))) {
            nearestNeighborSearchStep(pendingPaths, evaluatedPoints, pointsRemaining, distanceFunction, searchPoint);
        }

        return evaluatedPoints;
    }

    @SuppressWarnings("unchecked")
    protected static <T> void nearestNeighborSearchStep (
            MinHeap<KdNode<T>> pendingPaths, MaxHeap<T> evaluatedPoints, int desiredPoints,
            DistanceFunction distanceFunction, double[] searchPoint) {
        // If there are pending paths possibly closer than the nearest evaluated point, check it out
        KdNode<T> cursor = pendingPaths.getMin();
        pendingPaths.removeMin();

        // Descend the tree, recording paths not taken
        while (!cursor.isLeaf()) {
            KdNode<T> pathNotTaken;
            if (searchPoint[cursor.splitDimension] > cursor.splitValue) {
                pathNotTaken = cursor.left;
                cursor = cursor.right;
            }
            else {
                pathNotTaken = cursor.right;
                cursor = cursor.left;
            }
            double otherDistance = distanceFunction.distanceToRect(searchPoint, pathNotTaken.minBound, pathNotTaken.maxBound);
            // Only add a path if we either need more points or it's closer than furthest point on list so far
            if (evaluatedPoints.size() < desiredPoints || otherDistance <= evaluatedPoints.getMaxKey()) {
                pendingPaths.offer(otherDistance, pathNotTaken);
            }
        }

        if (cursor.singlePoint) {
            double nodeDistance = distanceFunction.distance(cursor.points[0], searchPoint);
            // Only add a point if either need more points or it's closer than furthest on list so far
            if (evaluatedPoints.size() < desiredPoints || nodeDistance <= evaluatedPoints.getMaxKey()) {
                for (int i = 0; i < cursor.size(); i++) {
                    T value = (T) cursor.data[i];

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
                double[] point = cursor.points[i];
                T value = (T) cursor.data[i];
                double distance = distanceFunction.distance(point, searchPoint);
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
