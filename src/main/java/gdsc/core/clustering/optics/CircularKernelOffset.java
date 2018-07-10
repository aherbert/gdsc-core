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
package gdsc.core.clustering.optics;

/**
 * Represent a circular kernel around a central pixel with size 2 * resolution + 1.
 */
public class CircularKernelOffset
{
	final int start;
	final int startInternal;
	final int endInternal;
	final int end;
	final boolean internal;

	CircularKernelOffset(int start, int startInternal, int endInternal, int end)
	{
		this.start = start;
		this.startInternal = startInternal;
		this.endInternal = endInternal;
		this.end = end;
		internal = endInternal > startInternal;
	}

	/**
	 * Create the offsets for a circular kernel. This represents a circular kernel around a central pixel with size 2 *
	 * resolution + 1. The centre pixel is (0,0), returned as row index [resolution] in the final array. The offsets are
	 * returned so that the following is valid to iterate over all the pixels in a row of the kernel:
	 *
	 * <pre>
	 * int x,y = ...;
	 * for (int yy=-resolution, i=0; i&lt;offset.length; yy++, i++) {
	 *   for (int xx=offset[i].start; xx&lt;offset[i].end; xx++) {
	 *      // Address in yx block order
	 *      data[y+yy][x+xx] = ...;
	 *   }
	 * }
	 * //offset.start will be &gt;= -resolution and &lt;= 0
	 * //offset.end will be &lt;= resolution + 1 and &gt;= 0
	 * </pre>
	 *
	 * @param resolution
	 *            The resolution
	 * @return the offsets
	 */
	public static CircularKernelOffset[] create(int resolution)
	{
		// Build a s circular mask to only search over the required points in the 2D grid
		final int size = 2 * resolution + 1;
		final CircularKernelOffset[] offset = new CircularKernelOffset[size];

		// We process the data in horizontal stripes.
		// Find the external and internal start and end for each stripe.
		// External: A pixel that cannot contain a distance within the generating distance
		// Internal: A pixel that could not contain a distance above the generating distance

		// We build this using only a quarter circle from the origin. The rest is filled using symmetry.
		// XXX
		// ...XXX
		// ......XX
		// ........XX
		// ..........X
		// ..........X

		final double generatingDistanceE = 1;
		final double binWidth = generatingDistanceE / resolution;

		final double e = generatingDistanceE * generatingDistanceE;

		// External:
		// The closest distance to the origin is above the generating distance

		// The outer pixel inner corner (0,0) must be compared to the closest point in the origin pixel:
		// 1,0 for the first row
		// 1,1 for the rest
		// 0,1 when the outerX is <= 0
		// As we draw the arc anti-clockwise we update the origin.
		int origX = 1;
		int origY = 0;
		int outerX = resolution;

		// Internal:
		// The farthest distance to the origin is below the generating distance.

		// The outer pixel outer corner (1,1) must be compared to the farthest point in the origin pixel:
		int innerX = resolution;

		for (int y = 0; y <= resolution; y++)
		{
			// Move the outer pixel if it is above the limit and the minimum distance is currently outside.
			//while (outerX > 0 && distance2(origX, origY, outerX, y) > e)
			double target = e - distance2(origY, y, binWidth);
			while (outerX > 0 && distance2(origX, outerX, binWidth) > target)
			{
				outerX--;
				// Update origin
				if (outerX == 0)
					origX = 0;
			}

			// Update origin for subsequent rows
			origY = 1;

			// Move the inner pixel if it is above the limit and the maximum distance is not currently inside.
			// This may be at the limit so check before distance calculations
			if (innerX != -1)
			{
				//while (innerX > -1 && distance2(0, 0, innerX + 1, y + 1) > e)
				target = e - distance2(y + 1, binWidth);
				while (innerX > -1 && distance2(innerX + 1, binWidth) > target)
					innerX--;
			}

			// Mirror. Add 1 to the end points so we can use i=start; i<end; i++.
			final int start = -outerX;
			final int end = outerX + 1;
			final int startInternal = -innerX;
			final int endInternal = innerX + 1;
			offset[resolution - y] = new CircularKernelOffset(start, startInternal, endInternal, end);
		}

		// Initialise and mirror
		for (int i = 0, j = offset.length - 1; i <= resolution; i++, j--)
			offset[j] = offset[i];

		return offset;
	}

	private static double distance2(int x, int x2, double binWidth)
	{
		final double dx = (x - x2) * binWidth;
		return dx * dx;
	}

	private static double distance2(int x, double binWidth)
	{
		final double dx = x * binWidth;
		return dx * dx;
	}
}
