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
import java.util.List;

/**
 * An efficient well-optimized kd-tree
 * <p>
 * This is a basic copy of the KdTree class but limited to 2 dimensions. Functionality to limit the tree size has been
 * removed. The status property has been removed to allow multi-threaded search.
 * 
 * @author Alex Herbert
 */
public abstract class FloatIntKdTree2D extends FloatIntKdTreeNode2D
{

	/** The next id. */
	// For child nodes
	private int nextId = 1;

	/**
	 * Gets the number of nodes in the tree.
	 *
	 * @return the number of nodes
	 */
	public int getNumberOfNodes()
	{
		return nextId;
	}

	/**
	 * Get the number of points in the tree.
	 *
	 * @return the size
	 */
	public int size()
	{
		return locationCount;
	}

	/**
	 * Add a point and associated value to the tree.
	 *
	 * @param location
	 *            the location
	 * @param value
	 *            the value
	 */
	public void addPoint(float[] location, int value)
	{
		FloatIntKdTreeNode2D cursor = this;

		while (cursor.locations == null || cursor.locationCount >= cursor.locations.length)
		{
			if (cursor.locations != null)
			{
				cursor.splitDimension = cursor.findWidestAxis();
				cursor.splitValue = (cursor.minLimit[cursor.splitDimension] + cursor.maxLimit[cursor.splitDimension]) *
						0.5f;

				// Never split on infinity or NaN
				if (cursor.splitValue == Float.POSITIVE_INFINITY)
				{
					cursor.splitValue = Float.MAX_VALUE;
				}
				else if (cursor.splitValue == Float.NEGATIVE_INFINITY)
				{
					cursor.splitValue = -Float.MAX_VALUE;
				}
				else if (Float.isNaN(cursor.splitValue))
				{
					cursor.splitValue = 0;
				}

				// Don't split node if it has no width in any axis. Float the
				// bucket size instead
				if (cursor.minLimit[cursor.splitDimension] == cursor.maxLimit[cursor.splitDimension])
				{
					float[][] newLocations = new float[cursor.locations.length * 2][];
					System.arraycopy(cursor.locations, 0, newLocations, 0, cursor.locationCount);
					cursor.locations = newLocations;
					int[] newData = new int[newLocations.length];
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
				FloatIntKdTreeNode2D left = new ChildNode(cursor, nextId++);
				FloatIntKdTreeNode2D right = new ChildNode(cursor, nextId++);

				// Move locations into children
				for (int i = 0; i < cursor.locationCount; i++)
				{
					float[] oldLocation = cursor.locations[i];
					int oldData = cursor.data[i];
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
	}

	/**
	 * Stores a distance and value to output.
	 */
	public static class Entry
	{
		/** The distance. */
		public final float distance;

		/** The value. */
		public final int value;

		/**
		 * Instantiates a new entry.
		 *
		 * @param distance
		 *            the distance
		 * @param value
		 *            the value
		 */
		private Entry(float distance, int value)
		{
			this.distance = distance;
			this.value = value;
		}
	}

	/**
	 * Calculates the nearest 'count' points to 'location'.
	 *
	 * @param location
	 *            the location
	 * @param count
	 *            the count
	 * @param sequentialSorting
	 *            Set to true to return the points in order, largest distance first
	 * @return the list
	 */
	public List<Entry> nearestNeighbor(float[] location, int count, boolean sequentialSorting)
	{
		return nearestNeighbor(location, count, sequentialSorting, new Status[getNumberOfNodes()]);
	}

	/**
	 * Calculates the nearest 'count' points to 'location'.
	 *
	 * @param location
	 *            the location
	 * @param count
	 *            the count
	 * @param sequentialSorting
	 *            Set to true to return the points in order, largest distance first
	 * @param status
	 *            the status array. Must equal the number of nodes in the tree
	 * @return the list
	 */
	public List<Entry> nearestNeighbor(float[] location, int count, boolean sequentialSorting, Status[] status)
	{
		FloatIntKdTreeNode2D cursor = this;
		status[cursor.id] = Status.NONE;
		float range = Float.POSITIVE_INFINITY;
		FloatIntResultHeap resultHeap = new FloatIntResultHeap(count);

		do
		{
			if (status[cursor.id] == Status.ALLVISITED)
			{
				// At a fully visited part. Move up the tree
				cursor = cursor.parent;
				continue;
			}

			if (status[cursor.id] == Status.NONE && cursor.locations != null)
			{
				// At a leaf. Use the data.
				if (cursor.locationCount > 0)
				{
					if (cursor.singularity)
					{
						float dist = pointDist(cursor.locations[0], location);
						if (dist <= range)
						{
							for (int i = 0; i < cursor.locationCount; i++)
							{
								resultHeap.addValue(dist, cursor.data[i]);
							}
						}
					}
					else
					{
						for (int i = 0; i < cursor.locationCount; i++)
						{
							float dist = pointDist(cursor.locations[i], location);
							resultHeap.addValue(dist, cursor.data[i]);
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
			FloatIntKdTreeNode2D nextCursor = null;
			if (status[cursor.id] == Status.NONE)
			{
				// At a fresh node, descend the most probably useful direction
				if (location[cursor.splitDimension] > cursor.splitValue)
				{
					// Descend right
					nextCursor = cursor.right;
					status[cursor.id] = Status.RIGHTVISITED;
				}
				else
				{
					// Descend left;
					nextCursor = cursor.left;
					status[cursor.id] = Status.LEFTVISITED;
				}
			}
			else if (status[cursor.id] == Status.LEFTVISITED)
			{
				// Left node visited, descend right.
				nextCursor = cursor.right;
				status[cursor.id] = Status.ALLVISITED;
			}
			else if (status[cursor.id] == Status.RIGHTVISITED)
			{
				// Right node visited, descend left.
				nextCursor = cursor.left;
				status[cursor.id] = Status.ALLVISITED;
			}

			// Check if it's worth descending. Assume it is if it's sibling has
			// not been visited yet.
			if (status[cursor.id] == Status.ALLVISITED)
			{
				if (nextCursor.locationCount == 0 || (!nextCursor.singularity &&
						pointRegionDist(location, nextCursor.minLimit, nextCursor.maxLimit) > range))
				{
					continue;
				}
			}

			// Descend down the tree
			cursor = nextCursor;
			status[cursor.id] = Status.NONE;
		} while (cursor.parent != null || status[cursor.id] != Status.ALLVISITED);

		ArrayList<Entry> results = new ArrayList<Entry>(resultHeap.values);
		if (sequentialSorting)
		{
			while (resultHeap.values > 0)
			{
				resultHeap.removeLargest();
				results.add(new Entry(resultHeap.removedDist, resultHeap.removedData));
			}
		}
		else
		{
			for (int i = 0; i < resultHeap.values; i++)
			{
				results.add(new Entry(resultHeap.distance[i], resultHeap.data[i]));
			}
		}

		return results;
	}

	/**
	 * Calculates the nearest 'count' points to 'location'.
	 *
	 * @param location
	 *            the location
	 * @param count
	 *            the count
	 * @param results
	 *            the results
	 */
	public void nearestNeighbor(float[] location, int count, IntNeighbourStore results)
	{
		nearestNeighbor(location, count, results, new Status[getNumberOfNodes()]);
	}

	/**
	 * Calculates the nearest 'count' points to 'location'.
	 *
	 * @param location
	 *            the location
	 * @param count
	 *            the count
	 * @param results
	 *            the results
	 * @param status
	 *            the status array. Must equal the number of nodes in the tree
	 */
	public void nearestNeighbor(float[] location, int count, IntNeighbourStore results, Status[] status)
	{
		FloatIntKdTreeNode2D cursor = this;
		status[cursor.id] = Status.NONE;
		float range = Float.POSITIVE_INFINITY;
		FloatIntResultHeap resultHeap = new FloatIntResultHeap(count);

		do
		{
			if (status[cursor.id] == Status.ALLVISITED)
			{
				// At a fully visited part. Move up the tree
				cursor = cursor.parent;
				continue;
			}

			if (status[cursor.id] == Status.NONE && cursor.locations != null)
			{
				// At a leaf. Use the data.
				if (cursor.locationCount > 0)
				{
					if (cursor.singularity)
					{
						float dist = pointDist(cursor.locations[0], location);
						if (dist <= range)
						{
							for (int i = 0; i < cursor.locationCount; i++)
							{
								resultHeap.addValue(dist, cursor.data[i]);
							}
						}
					}
					else
					{
						for (int i = 0; i < cursor.locationCount; i++)
						{
							float dist = pointDist(cursor.locations[i], location);
							resultHeap.addValue(dist, cursor.data[i]);
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
			FloatIntKdTreeNode2D nextCursor = null;
			if (status[cursor.id] == Status.NONE)
			{
				// At a fresh node, descend the most probably useful direction
				if (location[cursor.splitDimension] > cursor.splitValue)
				{
					// Descend right
					nextCursor = cursor.right;
					status[cursor.id] = Status.RIGHTVISITED;
				}
				else
				{
					// Descend left;
					nextCursor = cursor.left;
					status[cursor.id] = Status.LEFTVISITED;
				}
			}
			else if (status[cursor.id] == Status.LEFTVISITED)
			{
				// Left node visited, descend right.
				nextCursor = cursor.right;
				status[cursor.id] = Status.ALLVISITED;
			}
			else if (status[cursor.id] == Status.RIGHTVISITED)
			{
				// Right node visited, descend left.
				nextCursor = cursor.left;
				status[cursor.id] = Status.ALLVISITED;
			}

			// Check if it's worth descending. Assume it is if it's sibling has
			// not been visited yet.
			if (status[cursor.id] == Status.ALLVISITED)
			{
				if (nextCursor.locationCount == 0 || (!nextCursor.singularity &&
						pointRegionDist(location, nextCursor.minLimit, nextCursor.maxLimit) > range))
				{
					continue;
				}
			}

			// Descend down the tree
			cursor = nextCursor;
			status[cursor.id] = Status.NONE;
		} while (cursor.parent != null || status[cursor.id] != Status.ALLVISITED);

		for (int i = 0; i < resultHeap.values; i++)
		{
			results.add(resultHeap.distance[i], resultHeap.data[i]);
		}
	}

	/**
	 * Calculates the neighbour points within 'range' to 'location' and puts them in the results store.
	 *
	 * @param location
	 *            the location
	 * @param range
	 *            the range
	 * @param results
	 *            the results
	 */
	public void findNeighbor(float[] location, float range, IntNeighbourStore results)
	{
		findNeighbor(location, range, results, new Status[getNumberOfNodes()]);
	}

	/**
	 * Calculates the neighbour points within 'range' to 'location' and puts them in the results store.
	 *
	 * @param location
	 *            the location
	 * @param range
	 *            the range
	 * @param results
	 *            the results
	 * @param status
	 *            the status array. Must equal the number of nodes in the tree
	 */
	public void findNeighbor(float[] location, float range, IntNeighbourStore results, Status[] status)
	{
		FloatIntKdTreeNode2D cursor = this;
		status[cursor.id] = Status.NONE;

		do
		{
			if (status[cursor.id] == Status.ALLVISITED)
			{
				// At a fully visited part. Move up the tree
				cursor = cursor.parent;
				continue;
			}

			if (status[cursor.id] == Status.NONE && cursor.locations != null)
			{
				// At a leaf. Use the data.
				if (cursor.locationCount > 0)
				{
					if (cursor.singularity)
					{
						float dist = pointDist(cursor.locations[0], location);
						if (dist <= range)
						{
							for (int i = 0; i < cursor.locationCount; i++)
							{
								results.add(dist, cursor.data[i]);
							}
						}
					}
					else
					{
						for (int i = 0; i < cursor.locationCount; i++)
						{
							float dist = pointDist(cursor.locations[i], location);
							if (dist <= range)
							{
								results.add(dist, cursor.data[i]);
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
			FloatIntKdTreeNode2D nextCursor = null;
			if (status[cursor.id] == Status.NONE)
			{
				// At a fresh node, descend the most probably useful direction
				if (location[cursor.splitDimension] > cursor.splitValue)
				{
					// Descend right
					nextCursor = cursor.right;
					status[cursor.id] = Status.RIGHTVISITED;
				}
				else
				{
					// Descend left;
					nextCursor = cursor.left;
					status[cursor.id] = Status.LEFTVISITED;
				}
			}
			else if (status[cursor.id] == Status.LEFTVISITED)
			{
				// Left node visited, descend right.
				nextCursor = cursor.right;
				status[cursor.id] = Status.ALLVISITED;
			}
			else if (status[cursor.id] == Status.RIGHTVISITED)
			{
				// Right node visited, descend left.
				nextCursor = cursor.left;
				status[cursor.id] = Status.ALLVISITED;
			}

			// Check if it's worth descending. Assume it is if it's sibling has
			// not been visited yet.
			if (status[cursor.id] == Status.ALLVISITED)
			{
				if (nextCursor.locationCount == 0 || (!nextCursor.singularity &&
						pointRegionDist(location, nextCursor.minLimit, nextCursor.maxLimit) > range))
				{
					continue;
				}
			}

			// Descend down the tree
			cursor = nextCursor;
			status[cursor.id] = Status.NONE;
		} while (cursor.parent != null || status[cursor.id] != Status.ALLVISITED);
	}

	/**
	 * Internal class for child nodes.
	 */
	private class ChildNode extends FloatIntKdTreeNode2D
	{
		/**
		 * Instantiates a new child node.
		 *
		 * @param parent
		 *            the parent
		 * @param id
		 *            the id
		 */
		private ChildNode(FloatIntKdTreeNode2D parent, int id)
		{
			super(parent, id);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ags.utils.dataStructures.trees.secondGenKD.FloatIntKdTreeNode2D#pointDist(float[], float[])
		 */
		// Distance measurements are always called from the root node
		@Override
		protected float pointDist(float[] p1, float[] p2)
		{
			throw new IllegalStateException();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ags.utils.dataStructures.trees.secondGenKD.FloatIntKdTreeNode2D#pointRegionDist(float[], float[],
		 * float[])
		 */
		@Override
		protected float pointRegionDist(float[] point, float[] min, float[] max)
		{
			throw new IllegalStateException();
		}
	}

	/**
	 * Class for tree with Unweighted Squared Euclidean distancing assuming 2 dimensions with no NaN distance checking
	 * <p>
	 * This is an optimised version for use in the GDSC Core project.
	 * 
	 * @author Alex Herbert
	 */
	public static class SqrEuclid2D extends FloatIntKdTree2D
	{
		/*
		 * (non-Javadoc)
		 * 
		 * @see ags.utils.dataStructures.trees.secondGenKD.FloatIntKdTreeNode2D#pointDist(float[], float[])
		 */
		@Override
		protected float pointDist(float[] p1, float[] p2)
		{
			float dx = p1[0] - p2[0];
			float dy = p1[1] - p2[1];
			return dx * dx + dy * dy;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ags.utils.dataStructures.trees.secondGenKD.FloatIntKdTreeNode2D#pointRegionDist(float[], float[],
		 * float[])
		 */
		@Override
		protected float pointRegionDist(float[] point, float[] min, float[] max)
		{
			float dx = (point[0] > max[0]) ? point[0] - max[0] : (point[0] < min[0]) ? min[0] - point[0] : 0;
			float dy = (point[1] > max[1]) ? point[1] - max[1] : (point[1] < min[1]) ? min[1] - point[1] : 0;
			return dx * dx + dy * dy;
		}
	}
}
