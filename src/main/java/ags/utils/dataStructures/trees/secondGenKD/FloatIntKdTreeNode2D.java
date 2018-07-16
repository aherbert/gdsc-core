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
package ags.utils.dataStructures.trees.secondGenKD;

/**
 * An efficient well-optimized kd-tree
 * <p>
 * This is a basic copy of the KdTree class but limited to 2 dimensions. Stores only integer Ids and not objects.
 *
 * @author Alex Herbert
 */
abstract class FloatIntKdTreeNode2D
{
	/** The bucket size. */
	static final int bucketSize = 24;

	// All types

	/** The parent. */
	final FloatIntKdTreeNode2D parent;

	// Leaf only

	/** The locations. */
	float[][] locations;
	/** The data. */
	int[] data;
	/** The location count. */
	int locationCount;

	// Stem only

	/** The left. */
	FloatIntKdTreeNode2D left;
	/** The right. */
	FloatIntKdTreeNode2D right;
	/** The split dimension. */
	int splitDimension;
	/** The split value. */
	float splitValue;

	// Bounds

	/** The min limit. */
	float[] minLimit;
	/** The max limit. */
	float[] maxLimit;
	/** The singularity flag. */
	boolean singularity;
	/** The id. */
	final int id;

	/**
	 * Construct a RTree with 2 dimensions
	 */
	FloatIntKdTreeNode2D()
	{
		// Init as leaf
		this.locations = new float[bucketSize][];
		this.data = new int[bucketSize];
		this.locationCount = 0;
		this.singularity = true;

		// Init as root
		this.parent = null;
		id = 0;
	}

	/**
	 * Constructor for child nodes. Internal use only.
	 *
	 * @param parent
	 *            the parent
	 * @param id
	 *            the id
	 */
	FloatIntKdTreeNode2D(FloatIntKdTreeNode2D parent, int id)
	{
		// Init as leaf
		this.locations = new float[Math.max(bucketSize, parent.locationCount)][];
		this.data = new int[locations.length];
		this.locationCount = 0;
		this.singularity = true;

		// Init as non-root
		this.parent = parent;
		this.id = id;
	}

	/**
	 * Extends the bounds of this node do include a new location.
	 *
	 * @param location
	 *            the location
	 */
	final void extendBounds(float[] location)
	{
		if (minLimit == null)
		{
			minLimit = new float[] { location[0], location[1] };
			maxLimit = new float[] { location[0], location[1] };
			return;
		}

		for (int i = 2; i-- > 0;)
			if (Double.isNaN(location[i]))
			{
				minLimit[i] = Float.NaN;
				maxLimit[i] = Float.NaN;
				singularity = false;
			}
			else if (minLimit[i] > location[i])
			{
				minLimit[i] = location[i];
				singularity = false;
			}
			else if (maxLimit[i] < location[i])
			{
				maxLimit[i] = location[i];
				singularity = false;
			}
	}

	/**
	 * Find the widest axis of the bounds of this node.
	 *
	 * @return the axis
	 */
	final int findWidestAxis()
	{
		float width = (maxLimit[0] - minLimit[0]);
		if (Double.isNaN(width))
			width = 0;

		float nwidth = (maxLimit[1] - minLimit[1]);
		if (Double.isNaN(nwidth))
			nwidth = 0;
		if (nwidth > width)
			return 1;

		return 0;
	}

	// Override in subclasses

	/**
	 * Compute the point distance.
	 *
	 * @param p1
	 *            the p 1
	 * @param p2
	 *            the p 2
	 * @return the distance
	 */
	protected abstract float pointDist(float[] p1, float[] p2);

	/**
	 * Compute the point region distance.
	 *
	 * @param point
	 *            the point
	 * @param min
	 *            the min of the region
	 * @param max
	 *            the max of the region
	 * @return the distance
	 */
	protected abstract float pointRegionDist(float[] point, float[] min, float[] max);
}
