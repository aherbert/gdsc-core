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

/**
 * An efficient well-optimized kd-tree
 * 
 * @author Rednaxela
 */
abstract class KdTreeNode<T>
{
	// Static variables
	static final int bucketSize = 24;

	// All types
	final int dimensions;
	final KdTreeNode<T> parent;

	// Leaf only
	double[][] locations;
	Object[] data;
	int locationCount;

	// Stem only
	KdTreeNode<T> left, right;
	int splitDimension;
	double splitValue;

	// Bounds
	double[] minLimit, maxLimit;
	boolean singularity;

	// Temporary
	Status status;

	/**
	 * Construct a RTree with a given number of dimensions
	 */
	KdTreeNode(int dimensions)
	{
		this.dimensions = dimensions;

		// Init as leaf
		this.locations = new double[bucketSize][];
		this.data = new Object[bucketSize];
		this.locationCount = 0;
		this.singularity = true;

		// Init as root
		this.parent = null;
	}

	/**
	 * Constructor for child nodes. Internal use only.
	 */
	KdTreeNode(KdTreeNode<T> parent, boolean right)
	{
		this.dimensions = parent.dimensions;

		// Init as leaf
		this.locations = new double[Math.max(bucketSize, parent.locationCount)][];
		this.data = new Object[locations.length];
		this.locationCount = 0;
		this.singularity = true;

		// Init as non-root
		this.parent = parent;
	}

	/**
	 * Extends the bounds of this node do include a new location
	 */
	final void extendBounds(double[] location)
	{
		if (minLimit == null)
		{
			minLimit = new double[dimensions];
			System.arraycopy(location, 0, minLimit, 0, dimensions);
			maxLimit = new double[dimensions];
			System.arraycopy(location, 0, maxLimit, 0, dimensions);
			return;
		}

		for (int i = 0; i < dimensions; i++)
		{
			if (Double.isNaN(location[i]))
			{
				minLimit[i] = Double.NaN;
				maxLimit[i] = Double.NaN;
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
	}

	/**
	 * Find the widest axis of the bounds of this node
	 */
	final int findWidestAxis()
	{
		int widest = 0;
		double width = (maxLimit[0] - minLimit[0]) * getAxisWeightHint(0);
		if (Double.isNaN(width))
			width = 0;
		for (int i = 1; i < dimensions; i++)
		{
			double nwidth = (maxLimit[i] - minLimit[i]) * getAxisWeightHint(i);
			if (Double.isNaN(nwidth))
				nwidth = 0;
			if (nwidth > width)
			{
				widest = i;
				width = nwidth;
			}
		}
		return widest;
	}

	// Override in subclasses
	protected abstract double pointDist(double[] p1, double[] p2);

	protected abstract double pointRegionDist(double[] point, double[] min, double[] max);

	protected double getAxisWeightHint(int i)
	{
		return 1.0;
	}
}
