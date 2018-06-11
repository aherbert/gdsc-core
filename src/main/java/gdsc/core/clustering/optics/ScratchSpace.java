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

import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import gdsc.core.utils.ConvexHull;


/**
 * Provide space for storing cluster coordinates
 * 
 * @author Alex Herbert
 */
class ScratchSpace
{
	float[] x, y;
	int n;

	ScratchSpace(int capacity)
	{
		x = new float[capacity];
		y = new float[capacity];
		n = 0;
	}

	void resize(int capacity)
	{
		if (x.length < capacity)
		{
			x = new float[capacity];
			y = new float[capacity];
		}
		n = 0;
	}

	void add(float xx, float yy)
	{
		x[n] = xx;
		y[n] = yy;
		n++;
	}

	void add(float[] xx, float[] yy)
	{
		int size = xx.length;
		System.arraycopy(xx, 0, x, n, size);
		System.arraycopy(yy, 0, y, n, size);
		n += size;
	}

	void safeAdd(float xx, float yy)
	{
		if (x.length == n)
		{
			int size = x.length * 2;
			x = Arrays.copyOf(x, size);
			y = Arrays.copyOf(y, size);
		}

		x[n] = xx;
		y[n] = yy;
		n++;
	}

	Rectangle2D getBounds()
	{
		if (n == 0)
			return null;
		float minX = x[0];
		float minY = y[0];
		float maxX = minX;
		float maxY = minY;
		for (int i = 1; i < n; i++)
		{
			if (maxX < x[i])
				maxX = x[i];
			else if (minX > x[i])
				minX = x[i];
			if (maxY < y[i])
				maxY = y[i];
			else if (minY > y[i])
				minY = y[i];
		}
		return new Rectangle2D.Float(minX, minY, maxX - minX, maxY - minY);
	}

	ConvexHull getConvexHull()
	{
		return ConvexHull.create(x, y, n);
	}
}
