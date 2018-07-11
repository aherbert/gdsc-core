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
 * The Class KdTree.
 *
 * @param <T>
 *            the generic type
 */
public abstract class KdTree<T> extends KdNode<T>
{
	/**
	 * Instantiates a new kd tree.
	 */
	public KdTree()
	{
		super(24);
	}

	/**
	 * Instantiates a new kd tree.
	 *
	 * @param bucketCapacity
	 *            the bucket capacity
	 */
	public KdTree(int bucketCapacity)
	{
		super(bucketCapacity);
	}

	/**
	 * Gets the nearest neighbor iterator.
	 *
	 * @param searchPoint
	 *            the search point
	 * @param maxPointsReturned
	 *            the max points returned
	 * @param distanceFunction
	 *            the distance function
	 * @return the nearest neighbor iterator
	 */
	public NearestNeighborIterator<T> getNearestNeighborIterator(double[] searchPoint, int maxPointsReturned,
			DistanceFunction distanceFunction)
	{
		return new NearestNeighborIterator<>(this, searchPoint, maxPointsReturned, distanceFunction);
	}

	/**
	 * Find nearest neighbors.
	 *
	 * @param searchPoint
	 *            the search point
	 * @param maxPointsReturned
	 *            the max points returned
	 * @param distanceFunction
	 *            the distance function
	 * @return the max heap
	 */
	public MaxHeap<T> findNearestNeighbors(double[] searchPoint, int maxPointsReturned,
			DistanceFunction distanceFunction)
	{
		final BinaryHeap.Min<KdNode<T>> pendingPaths = new BinaryHeap.Min<>();
		final BinaryHeap.Max<T> evaluatedPoints = new BinaryHeap.Max<>();
		final int pointsRemaining = Math.min(maxPointsReturned, size());
		pendingPaths.offer(0, this);

		while (pendingPaths.size() > 0 &&
				(evaluatedPoints.size() < pointsRemaining || (pendingPaths.getMinKey() < evaluatedPoints.getMaxKey())))
			nearestNeighborSearchStep(pendingPaths, evaluatedPoints, pointsRemaining, distanceFunction, searchPoint);

		return evaluatedPoints;
	}

	/**
	 * Perform a nearest neighbor search step.
	 *
	 * @param <T>
	 *            the generic type
	 * @param pendingPaths
	 *            the pending paths
	 * @param evaluatedPoints
	 *            the evaluated points
	 * @param desiredPoints
	 *            the desired points
	 * @param distanceFunction
	 *            the distance function
	 * @param searchPoint
	 *            the search point
	 */
	@SuppressWarnings("unchecked")
	protected static <T> void nearestNeighborSearchStep(MinHeap<KdNode<T>> pendingPaths, MaxHeap<T> evaluatedPoints,
			int desiredPoints, DistanceFunction distanceFunction, double[] searchPoint)
	{
		// If there are pending paths possibly closer than the nearest evaluated point, check it out
		KdNode<T> cursor = pendingPaths.getMin();
		pendingPaths.removeMin();

		// Descend the tree, recording paths not taken
		while (!cursor.isLeaf())
		{
			KdNode<T> pathNotTaken;
			if (searchPoint[cursor.splitDimension] > cursor.splitValue)
			{
				pathNotTaken = cursor.left;
				cursor = cursor.right;
			}
			else
			{
				pathNotTaken = cursor.right;
				cursor = cursor.left;
			}
			final double otherDistance = distanceFunction.distanceToRect(searchPoint, pathNotTaken.minBound,
					pathNotTaken.maxBound);
			// Only add a path if we either need more points or it's closer than furthest point on list so far
			if (evaluatedPoints.size() < desiredPoints || otherDistance <= evaluatedPoints.getMaxKey())
				pendingPaths.offer(otherDistance, pathNotTaken);
		}

		if (cursor.singlePoint)
		{
			final double nodeDistance = distanceFunction.distance(cursor.points[0], searchPoint);
			// Only add a point if either need more points or it's closer than furthest on list so far
			if (evaluatedPoints.size() < desiredPoints || nodeDistance <= evaluatedPoints.getMaxKey())
				for (int i = 0; i < cursor.size(); i++)
				{
					final T value = (T) cursor.data[i];

					// If we don't need any more, replace max
					if (evaluatedPoints.size() == desiredPoints)
						evaluatedPoints.replaceMax(nodeDistance, value);
					else
						evaluatedPoints.offer(nodeDistance, value);
				}
		}
		else
			// Add the points at the cursor
			for (int i = 0; i < cursor.size(); i++)
			{
				final double[] point = cursor.points[i];
				final T value = (T) cursor.data[i];
				final double distance = distanceFunction.distance(point, searchPoint);
				// Only add a point if either need more points or it's closer than furthest on list so far
				if (evaluatedPoints.size() < desiredPoints)
					evaluatedPoints.offer(distance, value);
				else if (distance < evaluatedPoints.getMaxKey())
					evaluatedPoints.replaceMax(distance, value);
			}
	}
}
