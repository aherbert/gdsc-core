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
package ags.utils.dataStructures.trees.secondGenKD;

import java.util.Arrays;

/**
 * Class for tracking up to 'size' closest values
 */
public class DoubleHeap
{
	final double[] distance;
	private final int size;
	int values;
	public double removedDist;

	public DoubleHeap(int size)
	{
		this.distance = new double[size];
		this.size = size;
		this.values = 0;
	}

	public void addValue(double dist)
	{
		// If there is still room in the heap
		if (values < size)
		{
			// Insert new value at the end
			distance[values] = dist;
			upHeapify(values);
			values++;
		}
		// If there is no room left in the heap, and the new entry is lower
		// than the max entry
		else if (dist < distance[0])
		{
			// Replace the max entry with the new entry
			distance[0] = dist;
			downHeapify(0);
		}
	}

	public void removeLargest()
	{
		if (values == 0)
		{
			throw new IllegalStateException();
		}

		removedDist = distance[0];
		values--;
		distance[0] = distance[values];
		downHeapify(0);
	}

	private void upHeapify(int c)
	{
		while (c > 0)
		{
			final int p = (c - 1) >>> 1;
			if (distance[c] > distance[p])
			{
				double pDist = distance[p];
				distance[p] = distance[c];
				distance[c] = pDist;
				c = p;
			}
			else
			{
				break;
			}
		}
	}

	private void downHeapify(int p)
	{
		for (int c = p * 2 + 1; c < values; p = c, c = p * 2 + 1)
		{
			if (c + 1 < values && distance[c] < distance[c + 1])
			{
				c++;
			}
			if (distance[p] < distance[c])
			{
				// Swap the points
				double pDist = distance[p];
				distance[p] = distance[c];
				distance[c] = pDist;
			}
			else
			{
				break;
			}
		}
	}

	public double getMaxDist()
	{
		if (values < size)
		{
			return Double.POSITIVE_INFINITY;
		}
		return distance[0];
	}

	public int getSize()
	{
		return values;
	}

	public int getCapacity()
	{
		return size;
	}

	public double[] getDistance()
	{
		return Arrays.copyOf(distance, values);
	}
}
