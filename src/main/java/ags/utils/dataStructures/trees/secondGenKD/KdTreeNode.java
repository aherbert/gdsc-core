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

/**
 * An efficient well-optimized kd-tree.
 *
 * @author Rednaxela
 * @param <T>
 *            the generic type
 */
abstract class KdTreeNode<T>
{
	/** The Constant bucketSize. */
	static final int bucketSize = 24;

	// All types

	/** The dimensions. */
	final int dimensions;
	/** The parent. */
	final KdTreeNode<T> parent;

	// Leaf only

	/** The locations. */
	double[][] locations;
	/** The data. */
	Object[] data;
	/** The location count. */
	int locationCount;

	// Stem only

	/** The left. */
	KdTreeNode<T> left;
	/** The right. */
	KdTreeNode<T> right;
	/** The split dimension. */
	int splitDimension;
	/** The split value. */
	double splitValue;

	// Bounds

	/** The min limit. */
	double[] minLimit;
	/** The max limit. */
	double[] maxLimit;
	/** The singularity flag. */
	boolean singularity;

	// Temporary

	/** The status. */
	Status status;

	/**
	 * Construct a RTree with a given number of dimensions.
	 *
	 * @param dimensions
	 *            the dimensions
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
	 *
	 * @param parent
	 *            the parent
	 */
	KdTreeNode(KdTreeNode<T> parent)
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
	 * Extends the bounds of this node do include a new location.
	 *
	 * @param location
	 *            the location
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

	/**
	 * Find the widest axis of the bounds of this node.
	 *
	 * @return the int
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

	/**
	 * Compute the point distance.
	 *
	 * @param p1
	 *            the p 1
	 * @param p2
	 *            the p 2
	 * @return the distance
	 */
	protected abstract double pointDist(double[] p1, double[] p2);

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
	protected abstract double pointRegionDist(double[] point, double[] min, double[] max);

	/**
	 * Gets the axis weight hint.
	 *
	 * @param i
	 *            the dimension index
	 * @return the axis weight hint
	 */
	protected double getAxisWeightHint(int i)
	{
		return 1.0;
	}
}
