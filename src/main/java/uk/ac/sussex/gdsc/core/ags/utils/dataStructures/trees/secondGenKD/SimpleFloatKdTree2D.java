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
package uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.secondGenKD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An efficient well-optimized kd-tree
 * <p>
 * This is a basic copy of the KdTree class but limited to 2 dimensions. Functionality to limit the tree size has been
 * removed.
 *
 * @author Alex Herbert
 */
public abstract class SimpleFloatKdTree2D extends SimpleFloatKdTreeNode2D
{
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
	 */
	public void addPoint(float[] location)
	{
		SimpleFloatKdTreeNode2D cursor = this;

		while (cursor.locations == null || cursor.locationCount >= cursor.locations.length)
		{
			if (cursor.locations != null)
			{
				cursor.splitDimension = cursor.findWidestAxis();
				cursor.splitValue = (cursor.minLimit[cursor.splitDimension] + cursor.maxLimit[cursor.splitDimension]) *
						0.5f;

				// Never split on infinity or NaN
				if (cursor.splitValue == Float.POSITIVE_INFINITY)
					cursor.splitValue = Float.MAX_VALUE;
				else if (cursor.splitValue == Float.NEGATIVE_INFINITY)
					cursor.splitValue = -Float.MAX_VALUE;
				else if (Float.isNaN(cursor.splitValue))
					cursor.splitValue = 0;

				// Don't split node if it has no width in any axis. Float the
				// bucket size instead
				if (cursor.minLimit[cursor.splitDimension] == cursor.maxLimit[cursor.splitDimension])
				{
					final float[][] newLocations = new float[cursor.locations.length * 2][];
					System.arraycopy(cursor.locations, 0, newLocations, 0, cursor.locationCount);
					cursor.locations = newLocations;
					break;
				}

				// Don't let the split value be the same as the upper value as
				// can happen due to rounding errors!
				if (cursor.splitValue == cursor.maxLimit[cursor.splitDimension])
					cursor.splitValue = cursor.minLimit[cursor.splitDimension];

				// Create child leaves
				final SimpleFloatKdTreeNode2D left = new ChildNode(cursor);
				final SimpleFloatKdTreeNode2D right = new ChildNode(cursor);

				// Move locations into children
				for (int i = 0; i < cursor.locationCount; i++)
				{
					final float[] oldLocation = cursor.locations[i];
					if (oldLocation[cursor.splitDimension] > cursor.splitValue)
					{
						// Right
						right.locations[right.locationCount] = oldLocation;
						right.locationCount++;
						right.extendBounds(oldLocation);
					}
					else
					{
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

			if (location[cursor.splitDimension] > cursor.splitValue)
				cursor = cursor.right;
			else
				cursor = cursor.left;
		}

		cursor.locations[cursor.locationCount] = location;
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
		public final float[] value;

		/**
		 * Instantiates a new entry.
		 *
		 * @param distance
		 *            the distance
		 * @param value
		 *            the value
		 */
		private Entry(float distance, float[] value)
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
	 *            the sequential sorting
	 * @return the list
	 */
	@SuppressWarnings("null")
	public List<Entry> nearestNeighbor(float[] location, int count, boolean sequentialSorting)
	{
		SimpleFloatKdTreeNode2D cursor = this;
		cursor.status = Status.NONE;
		float range = Float.POSITIVE_INFINITY;
		final FloatResultHeap<float[]> resultHeap = new FloatResultHeap<>(count);

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
						final float dist = pointDist(cursor.locations[0], location);
						if (dist <= range)
							for (int i = 0; i < cursor.locationCount; i++)
								resultHeap.addValue(dist, cursor.locations[i]);
					}
					else
						for (int i = 0; i < cursor.locationCount; i++)
						{
							final float dist = pointDist(cursor.locations[i], location);
							resultHeap.addValue(dist, cursor.locations[i]);
						}
					range = resultHeap.getMaxDist();
				}

				if (cursor.parent == null)
					break;
				cursor = cursor.parent;
				continue;
			}

			// Going to descend
			SimpleFloatKdTreeNode2D nextCursor = null;
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
				if (nextCursor.locationCount == 0 || (!nextCursor.singularity &&
						pointRegionDist(location, nextCursor.minLimit, nextCursor.maxLimit) > range))
					continue;

			// Descend down the tree
			cursor = nextCursor;
			cursor.status = Status.NONE;
		} while (cursor.parent != null || cursor.status != Status.ALLVISITED);

		final ArrayList<Entry> results = new ArrayList<>(resultHeap.values);
		if (sequentialSorting)
			while (resultHeap.values > 0)
			{
				resultHeap.removeLargest();
				results.add(new Entry(resultHeap.removedDist, (float[]) resultHeap.removedData));
			}
		else
			for (int i = 0; i < resultHeap.values; i++)
				results.add(new Entry(resultHeap.distance[i], (float[]) resultHeap.data[i]));

		return results;
	}

	/**
	 * Calculates the nearest 'count' point distances to 'location'. Results are unsorted. The first entry in the
	 * returned results in the max distance.
	 *
	 * @param location
	 *            the location
	 * @param count
	 *            the count
	 * @return the float[]
	 */
	@SuppressWarnings("null")
	public float[] nearestNeighbor(float[] location, int count)
	{
		SimpleFloatKdTreeNode2D cursor = this;
		cursor.status = Status.NONE;
		float range = Float.POSITIVE_INFINITY;
		final FloatHeap resultHeap = new FloatHeap(count);

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
						final float dist = pointDist(cursor.locations[0], location);
						if (dist <= range)
							for (int i = 0; i < cursor.locationCount; i++)
								resultHeap.addValue(dist);
					}
					else
						for (int i = 0; i < cursor.locationCount; i++)
						{
							final float dist = pointDist(cursor.locations[i], location);
							resultHeap.addValue(dist);
						}
					range = resultHeap.getMaxDist();
				}

				if (cursor.parent == null)
					break;
				cursor = cursor.parent;
				continue;
			}

			// Going to descend
			SimpleFloatKdTreeNode2D nextCursor = null;
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
				if (nextCursor.locationCount == 0 || (!nextCursor.singularity &&
						pointRegionDist(location, nextCursor.minLimit, nextCursor.maxLimit) > range))
					continue;

			// Descend down the tree
			cursor = nextCursor;
			cursor.status = Status.NONE;
		} while (cursor.parent != null || cursor.status != Status.ALLVISITED);

		if (count == resultHeap.values)
			return resultHeap.distance;
		return Arrays.copyOf(resultHeap.distance, resultHeap.values);
	}

	/**
	 * Internal class for child nodes.
	 */
	private class ChildNode extends SimpleFloatKdTreeNode2D
	{
		/**
		 * Instantiates a new child node.
		 *
		 * @param parent
		 *            the parent
		 */
		private ChildNode(SimpleFloatKdTreeNode2D parent)
		{
			super(parent);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see ags.utils.dataStructures.trees.secondGenKD.SimpleFloatKdTreeNode2D#pointDist(float[], float[])
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
		 * @see ags.utils.dataStructures.trees.secondGenKD.SimpleFloatKdTreeNode2D#pointRegionDist(float[], float[],
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
	public static class SqrEuclid2D extends SimpleFloatKdTree2D
	{
		/*
		 * (non-Javadoc)
		 *
		 * @see ags.utils.dataStructures.trees.secondGenKD.SimpleFloatKdTreeNode2D#pointDist(float[], float[])
		 */
		@Override
		protected float pointDist(float[] p1, float[] p2)
		{
			final float dx = p1[0] - p2[0];
			final float dy = p1[1] - p2[1];
			return dx * dx + dy * dy;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see ags.utils.dataStructures.trees.secondGenKD.SimpleFloatKdTreeNode2D#pointRegionDist(float[], float[],
		 * float[])
		 */
		@Override
		protected float pointRegionDist(float[] point, float[] min, float[] max)
		{
			final float dx = (point[0] > max[0]) ? point[0] - max[0] : (point[0] < min[0]) ? min[0] - point[0] : 0;
			final float dy = (point[1] > max[1]) ? point[1] - max[1] : (point[1] < min[1]) ? min[1] - point[1] : 0;
			return dx * dx + dy * dy;
		}
	}
}
