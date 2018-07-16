/*
 * Copyright 2009 Rednaxela
 *
 * Modifications to the code have been made by Alex Herbert for a smaller 
 * memory footprint and optimised 2D processing for use with image data
 * as part of the Genome Damage and Stability Centre ImageJ Core Package.
 * 
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *    1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 *
 *    2. This notice may not be removed or altered from any source
 *    distribution.
 */
package ags.utils.dataStructures.trees.thirdGenKD;

import java.util.Arrays;
import java.util.Iterator;

import ags.utils.dataStructures.BinaryHeap;
import ags.utils.dataStructures.IntervalHeap;
import ags.utils.dataStructures.MinHeap;

/**
 * The Class NearestNeighborIterator.
 *
 * @param <T>
 *            the generic type
 */
public class NearestNeighborIterator<T> implements Iterator<T>, Iterable<T>
{
	/** The distance function. */
	private final DistanceFunction distanceFunction;

	/** The search point. */
	private final double[] searchPoint;

	/** The pending paths. */
	private final MinHeap<KdNode<T>> pendingPaths;

	/** The evaluated points. */
	private final IntervalHeap<T> evaluatedPoints;

	/** The points remaining. */
	private int pointsRemaining;

	/** The last distance returned. */
	private double lastDistanceReturned;

	/**
	 * Instantiates a new nearest neighbor iterator.
	 *
	 * @param treeRoot
	 *            the tree root
	 * @param searchPoint
	 *            the search point
	 * @param maxPointsReturned
	 *            the max points returned
	 * @param distanceFunction
	 *            the distance function
	 */
	protected NearestNeighborIterator(KdNode<T> treeRoot, double[] searchPoint, int maxPointsReturned,
			DistanceFunction distanceFunction)
	{
		this.searchPoint = Arrays.copyOf(searchPoint, searchPoint.length);
		this.pointsRemaining = Math.min(maxPointsReturned, treeRoot.size());
		this.distanceFunction = distanceFunction;
		this.pendingPaths = new BinaryHeap.Min<>();
		this.pendingPaths.offer(0, treeRoot);
		this.evaluatedPoints = new IntervalHeap<>();
	}

	/* -------- INTERFACE IMPLEMENTATION -------- */

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext()
	{
		return pointsRemaining > 0;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Iterator#next()
	 */
	@Override
	public T next()
	{
		if (!hasNext())
			throw new IllegalStateException("NearestNeighborIterator has reached end!");

		while (pendingPaths.size() > 0 &&
				(evaluatedPoints.size() == 0 || (pendingPaths.getMinKey() < evaluatedPoints.getMinKey())))
			KdTree.nearestNeighborSearchStep(pendingPaths, evaluatedPoints, pointsRemaining, distanceFunction,
					searchPoint);

		// Return the smallest distance point
		pointsRemaining--;
		lastDistanceReturned = evaluatedPoints.getMinKey();
		final T value = evaluatedPoints.getMin();
		evaluatedPoints.removeMin();
		return value;
	}

	/**
	 * Distance.
	 *
	 * @return the double
	 */
	public double distance()
	{
		return lastDistanceReturned;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator()
	{
		return this;
	}
}
