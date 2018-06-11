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
/**
 * Copyright 2009 Rednaxela
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

package ags.utils.dataStructures.trees.secondGenKD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * An efficient well-optimized kd-tree
 * 
 * @author Rednaxela
 */
public abstract class KdTree<T> extends KdTreeNode<T>
{
	// Root only
	private final LinkedList<double[]> locationStack;
	private final Integer sizeLimit;

	/**
	 * Construct a RTree with a given number of dimensions and a limit on
	 * maxiumum size (after which it throws away old points)
	 */
	private KdTree(int dimensions, Integer sizeLimit)
	{
		super(dimensions);

		// Init as root
		this.sizeLimit = sizeLimit;
		if (sizeLimit != null)
		{
			this.locationStack = new LinkedList<double[]>();
		}
		else
		{
			this.locationStack = null;
		}
	}

	/**
	 * Get the number of points in the tree
	 */
	public int size()
	{
		return locationCount;
	}

	/**
	 * Add a point and associated value to the tree
	 */
	public void addPoint(double[] location, T value)
	{
		KdTreeNode<T> cursor = this;

		while (cursor.locations == null || cursor.locationCount >= cursor.locations.length)
		{
			if (cursor.locations != null)
			{
				cursor.splitDimension = cursor.findWidestAxis();
				cursor.splitValue = (cursor.minLimit[cursor.splitDimension] + cursor.maxLimit[cursor.splitDimension]) *
						0.5;

				// Never split on infinity or NaN
				if (cursor.splitValue == Double.POSITIVE_INFINITY)
				{
					cursor.splitValue = Double.MAX_VALUE;
				}
				else if (cursor.splitValue == Double.NEGATIVE_INFINITY)
				{
					cursor.splitValue = -Double.MAX_VALUE;
				}
				else if (Double.isNaN(cursor.splitValue))
				{
					cursor.splitValue = 0;
				}

				// Don't split node if it has no width in any axis. Double the
				// bucket size instead
				if (cursor.minLimit[cursor.splitDimension] == cursor.maxLimit[cursor.splitDimension])
				{
					double[][] newLocations = new double[cursor.locations.length * 2][];
					System.arraycopy(cursor.locations, 0, newLocations, 0, cursor.locationCount);
					cursor.locations = newLocations;
					Object[] newData = new Object[newLocations.length];
					System.arraycopy(cursor.data, 0, newData, 0, cursor.locationCount);
					cursor.data = newData;
					break;
				}

				// Don't let the split value be the same as the upper value as
				// can happen due to rounding errors!
				if (cursor.splitValue == cursor.maxLimit[cursor.splitDimension])
				{
					cursor.splitValue = cursor.minLimit[cursor.splitDimension];
				}

				// Create child leaves
				KdTreeNode<T> left = new ChildNode(cursor, false);
				KdTreeNode<T> right = new ChildNode(cursor, true);

				// Move locations into children
				for (int i = 0; i < cursor.locationCount; i++)
				{
					double[] oldLocation = cursor.locations[i];
					Object oldData = cursor.data[i];
					if (oldLocation[cursor.splitDimension] > cursor.splitValue)
					{
						// Right
						right.locations[right.locationCount] = oldLocation;
						right.data[right.locationCount] = oldData;
						right.locationCount++;
						right.extendBounds(oldLocation);
					}
					else
					{
						// Left
						left.locations[left.locationCount] = oldLocation;
						left.data[left.locationCount] = oldData;
						left.locationCount++;
						left.extendBounds(oldLocation);
					}
				}

				// Make into stem
				cursor.left = left;
				cursor.right = right;
				cursor.locations = null;
				cursor.data = null;
			}

			cursor.locationCount++;
			cursor.extendBounds(location);

			if (location[cursor.splitDimension] > cursor.splitValue)
			{
				cursor = cursor.right;
			}
			else
			{
				cursor = cursor.left;
			}
		}

		cursor.locations[cursor.locationCount] = location;
		cursor.data[cursor.locationCount] = value;
		cursor.locationCount++;
		cursor.extendBounds(location);

		if (this.sizeLimit != null)
		{
			this.locationStack.add(location);
			if (this.locationCount > this.sizeLimit)
			{
				this.removeOld();
			}
		}
	}

	/**
	 * Remove the oldest value from the tree. Note: This cannot trim the bounds
	 * of nodes, nor empty nodes, and thus you can't expect it to perfectly
	 * preserve the speed of the tree as you keep adding.
	 */
	private void removeOld()
	{
		double[] location = this.locationStack.removeFirst();
		KdTreeNode<T> cursor = this;

		// Find the node where the point is
		while (cursor.locations == null)
		{
			if (location[cursor.splitDimension] > cursor.splitValue)
			{
				cursor = cursor.right;
			}
			else
			{
				cursor = cursor.left;
			}
		}

		for (int i = 0; i < cursor.locationCount; i++)
		{
			if (cursor.locations[i] == location)
			{
				System.arraycopy(cursor.locations, i + 1, cursor.locations, i, cursor.locationCount - i - 1);
				cursor.locations[cursor.locationCount - 1] = null;
				System.arraycopy(cursor.data, i + 1, cursor.data, i, cursor.locationCount - i - 1);
				cursor.data[cursor.locationCount - 1] = null;
				do
				{
					cursor.locationCount--;
					cursor = cursor.parent;
				} while (cursor.parent != null);
				return;
			}
		}
		// If we got here... we couldn't find the value to remove. Weird...
	}

	/**
	 * Stores a distance and value to output
	 */
	public static class Entry<T>
	{
		public final double distance;
		public final T value;

		private Entry(double distance, T value)
		{
			this.distance = distance;
			this.value = value;
		}
	}

	/**
	 * Calculates the nearest 'count' points to 'location'
	 */
	@SuppressWarnings("unchecked")
	public List<Entry<T>> nearestNeighbor(double[] location, int count, boolean sequentialSorting)
	{
		KdTreeNode<T> cursor = this;
		cursor.status = Status.NONE;
		double range = Double.POSITIVE_INFINITY;
		ResultHeap<T> resultHeap = new ResultHeap<T>(count);

		do
		{
			if (cursor.status == Status.ALLVISITED)
			{
				// At a fully visited part. Move up the tree
				cursor = cursor.parent;
				continue;
			}

			if (cursor.status == Status.NONE && cursor.locations != null)
			{
				// At a leaf. Use the data.
				if (cursor.locationCount > 0)
				{
					if (cursor.singularity)
					{
						double dist = pointDist(cursor.locations[0], location);
						if (dist <= range)
						{
							for (int i = 0; i < cursor.locationCount; i++)
							{
								resultHeap.addValueFast(dist, cursor.data[i]);
							}
						}
					}
					else
					{
						for (int i = 0; i < cursor.locationCount; i++)
						{
							double dist = pointDist(cursor.locations[i], location);
							resultHeap.addValueFast(dist, cursor.data[i]);
						}
					}
					range = resultHeap.getMaxDist();
				}

				if (cursor.parent == null)
				{
					break;
				}
				cursor = cursor.parent;
				continue;
			}

			// Going to descend
			KdTreeNode<T> nextCursor = null;
			if (cursor.status == Status.NONE)
			{
				// At a fresh node, descend the most probably useful direction
				if (location[cursor.splitDimension] > cursor.splitValue)
				{
					// Descend right
					nextCursor = cursor.right;
					cursor.status = Status.RIGHTVISITED;
				}
				else
				{
					// Descend left;
					nextCursor = cursor.left;
					cursor.status = Status.LEFTVISITED;
				}
			}
			else if (cursor.status == Status.LEFTVISITED)
			{
				// Left node visited, descend right.
				nextCursor = cursor.right;
				cursor.status = Status.ALLVISITED;
			}
			else if (cursor.status == Status.RIGHTVISITED)
			{
				// Right node visited, descend left.
				nextCursor = cursor.left;
				cursor.status = Status.ALLVISITED;
			}

			// Check if it's worth descending. Assume it is if it's sibling has
			// not been visited yet.
			if (cursor.status == Status.ALLVISITED)
			{
				if (nextCursor.locationCount == 0 || (!nextCursor.singularity &&
						pointRegionDist(location, nextCursor.minLimit, nextCursor.maxLimit) > range))
				{
					continue;
				}
			}

			// Descend down the tree
			cursor = nextCursor;
			cursor.status = Status.NONE;
		} while (cursor.parent != null || cursor.status != Status.ALLVISITED);

		ArrayList<Entry<T>> results = new ArrayList<Entry<T>>(resultHeap.values);
		if (sequentialSorting)
		{
			while (resultHeap.values > 0)
			{
				resultHeap.removeLargest();
				results.add(new Entry<T>(resultHeap.removedDist, (T) resultHeap.removedData));
			}
		}
		else
		{
			for (int i = 0; i < resultHeap.values; i++)
			{
				results.add(new Entry<T>(resultHeap.distance[i], (T) resultHeap.data[i]));
			}
		}

		return results;
	}

	/**
	 * Calculates the neighbour points within 'range' to 'location' and puts them in the results store
	 */
	@SuppressWarnings("unchecked")
	public void findNeighbor(double[] location, double range, NeighbourStore<T> results)
	{
		KdTreeNode<T> cursor = this;
		cursor.status = Status.NONE;

		do
		{
			if (cursor.status == Status.ALLVISITED)
			{
				// At a fully visited part. Move up the tree
				cursor = cursor.parent;
				continue;
			}

			if (cursor.status == Status.NONE && cursor.locations != null)
			{
				// At a leaf. Use the data.
				if (cursor.locationCount > 0)
				{
					if (cursor.singularity)
					{
						double dist = pointDist(cursor.locations[0], location);
						if (dist <= range)
						{
							for (int i = 0; i < cursor.locationCount; i++)
							{
								results.add(dist, (T) cursor.data[i]);
							}
						}
					}
					else
					{
						for (int i = 0; i < cursor.locationCount; i++)
						{
							double dist = pointDist(cursor.locations[i], location);
							if (dist <= range)
							{
								results.add(dist, (T) cursor.data[i]);
							}
						}
					}
				}

				if (cursor.parent == null)
				{
					break;
				}
				cursor = cursor.parent;
				continue;
			}

			// Going to descend
			KdTreeNode<T> nextCursor = null;
			if (cursor.status == Status.NONE)
			{
				// At a fresh node, descend the most probably useful direction
				if (location[cursor.splitDimension] > cursor.splitValue)
				{
					// Descend right
					nextCursor = cursor.right;
					cursor.status = Status.RIGHTVISITED;
				}
				else
				{
					// Descend left;
					nextCursor = cursor.left;
					cursor.status = Status.LEFTVISITED;
				}
			}
			else if (cursor.status == Status.LEFTVISITED)
			{
				// Left node visited, descend right.
				nextCursor = cursor.right;
				cursor.status = Status.ALLVISITED;
			}
			else if (cursor.status == Status.RIGHTVISITED)
			{
				// Right node visited, descend left.
				nextCursor = cursor.left;
				cursor.status = Status.ALLVISITED;
			}

			// Check if it's worth descending. Assume it is if it's sibling has
			// not been visited yet.
			if (cursor.status == Status.ALLVISITED)
			{
				if (nextCursor.locationCount == 0 || (!nextCursor.singularity &&
						pointRegionDist(location, nextCursor.minLimit, nextCursor.maxLimit) > range))
				{
					continue;
				}
			}

			// Descend down the tree
			cursor = nextCursor;
			cursor.status = Status.NONE;
		} while (cursor.parent != null || cursor.status != Status.ALLVISITED);
	}

	/**
	 * Internal class for child nodes
	 */
	private class ChildNode extends KdTreeNode<T>
	{
		private ChildNode(KdTreeNode<T> parent, boolean right)
		{
			super(parent, right);
		}

		// Distance measurements are always called from the root node
		@Override
		protected double pointDist(double[] p1, double[] p2)
		{
			throw new IllegalStateException();
		}

		@Override
		protected double pointRegionDist(double[] point, double[] min, double[] max)
		{
			throw new IllegalStateException();
		}
	}

	/**
	 * Class for tree with Weighted Squared Euclidean distancing
	 */
	public static class WeightedSqrEuclid<T> extends KdTree<T>
	{
		private double[] weights;

		public WeightedSqrEuclid(int dimensions, Integer sizeLimit)
		{
			super(dimensions, sizeLimit);
			this.weights = new double[dimensions];
			Arrays.fill(this.weights, 1.0);
		}

		public void setWeights(double[] weights)
		{
			this.weights = weights;
		}

		@Override
		protected double getAxisWeightHint(int i)
		{
			return weights[i];
		}

		@Override
		protected double pointDist(double[] p1, double[] p2)
		{
			double d = 0;

			for (int i = 0; i < p1.length; i++)
			{
				double diff = (p1[i] - p2[i]) * weights[i];
				if (!Double.isNaN(diff))
				{
					d += diff * diff;
				}
			}

			return d;
		}

		@Override
		protected double pointRegionDist(double[] point, double[] min, double[] max)
		{
			double d = 0;

			for (int i = 0; i < point.length; i++)
			{
				double diff = 0;
				if (point[i] > max[i])
				{
					diff = (point[i] - max[i]) * weights[i];
				}
				else if (point[i] < min[i])
				{
					diff = (point[i] - min[i]) * weights[i];
				}

				if (!Double.isNaN(diff))
				{
					d += diff * diff;
				}
			}

			return d;
		}
	}

	/**
	 * Class for tree with Unweighted Squared Euclidean distancing
	 */
	public static class SqrEuclid<T> extends KdTree<T>
	{
		public SqrEuclid(int dimensions, Integer sizeLimit)
		{
			super(dimensions, sizeLimit);
		}

		@Override
		protected double pointDist(double[] p1, double[] p2)
		{
			double d = 0;

			for (int i = 0; i < p1.length; i++)
			{
				double diff = (p1[i] - p2[i]);
				if (!Double.isNaN(diff))
				{
					d += diff * diff;
				}
			}

			return d;
		}

		@Override
		protected double pointRegionDist(double[] point, double[] min, double[] max)
		{
			double d = 0;

			for (int i = 0; i < point.length; i++)
			{
				double diff = 0;
				if (point[i] > max[i])
				{
					diff = (point[i] - max[i]);
				}
				else if (point[i] < min[i])
				{
					diff = (point[i] - min[i]);
				}

				if (!Double.isNaN(diff))
				{
					d += diff * diff;
				}
			}

			return d;
		}
	}

	/**
	 * Class for tree with Unweighted Squared Euclidean distancing assuming 2 dimensions with no NaN distance checking
	 * <p>
	 * This is an optimised version for use in the GDSC Core project.
	 * 
	 * @author Alex Herbert
	 */
	public static class SqrEuclid2D<T> extends KdTree<T>
	{
		public SqrEuclid2D(Integer sizeLimit)
		{
			super(2, sizeLimit);
		}

		@Override
		protected double pointDist(double[] p1, double[] p2)
		{
			double dx = p1[0] - p2[0];
			double dy = p1[1] - p2[1];
			return dx * dx + dy * dy;
		}

		@Override
		protected double pointRegionDist(double[] point, double[] min, double[] max)
		{
			double dx = (point[0] > max[0]) ? point[0] - max[0] : (point[0] < min[0]) ? min[0] - point[0] : 0;
			double dy = (point[1] > max[1]) ? point[1] - max[1] : (point[1] < min[1]) ? min[1] - point[1] : 0;
			return dx * dx + dy * dy;
		}
	}

	/**
	 * Class for tree with Weighted Manhattan distancing
	 */
	public static class WeightedManhattan<T> extends KdTree<T>
	{
		private double[] weights;

		public WeightedManhattan(int dimensions, Integer sizeLimit)
		{
			super(dimensions, sizeLimit);
			this.weights = new double[dimensions];
			Arrays.fill(this.weights, 1.0);
		}

		public void setWeights(double[] weights)
		{
			this.weights = weights;
		}

		@Override
		protected double getAxisWeightHint(int i)
		{
			return weights[i];
		}

		@Override
		protected double pointDist(double[] p1, double[] p2)
		{
			double d = 0;

			for (int i = 0; i < p1.length; i++)
			{
				double diff = (p1[i] - p2[i]);
				if (!Double.isNaN(diff))
				{
					d += ((diff < 0) ? -diff : diff) * weights[i];
				}
			}

			return d;
		}

		@Override
		protected double pointRegionDist(double[] point, double[] min, double[] max)
		{
			double d = 0;

			for (int i = 0; i < point.length; i++)
			{
				double diff = 0;
				if (point[i] > max[i])
				{
					diff = (point[i] - max[i]);
				}
				else if (point[i] < min[i])
				{
					diff = (min[i] - point[i]);
				}

				if (!Double.isNaN(diff))
				{
					d += diff * weights[i];
				}
			}

			return d;
		}
	}

	/**
	 * Class for tree with Manhattan distancing
	 */
	public static class Manhattan<T> extends KdTree<T>
	{
		public Manhattan(int dimensions, Integer sizeLimit)
		{
			super(dimensions, sizeLimit);
		}

		@Override
		protected double pointDist(double[] p1, double[] p2)
		{
			double d = 0;

			for (int i = 0; i < p1.length; i++)
			{
				double diff = (p1[i] - p2[i]);
				if (!Double.isNaN(diff))
				{
					d += (diff < 0) ? -diff : diff;
				}
			}

			return d;
		}

		@Override
		protected double pointRegionDist(double[] point, double[] min, double[] max)
		{
			double d = 0;

			for (int i = 0; i < point.length; i++)
			{
				double diff = 0;
				if (point[i] > max[i])
				{
					diff = (point[i] - max[i]);
				}
				else if (point[i] < min[i])
				{
					diff = (min[i] - point[i]);
				}

				if (!Double.isNaN(diff))
				{
					d += diff;
				}
			}

			return d;
		}
	}
}
