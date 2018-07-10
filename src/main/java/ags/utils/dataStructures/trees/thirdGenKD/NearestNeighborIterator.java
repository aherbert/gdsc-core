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
