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
package gdsc.core.utils;

import java.awt.geom.Rectangle2D;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Assert;
import org.junit.Test;

public class ConvexHullTest
{
	RandomGenerator r = new Well19937c(30051977);

	@Test
	public void canComputeConvexHullFromSquare()
	{
		float[] x = new float[] { 0, 10, 10, 0 };
		float[] y = new float[] { 0, 0, 10, 10 };
		ConvexHull hull = ConvexHull.create(x, y);
		check(x, y, hull);
	}

	@Test
	public void canComputeConvexHullFromSquareWithInternalPoint()
	{
		float[] x = new float[] { 0, 0, 10, 10, 5 };
		float[] y = new float[] { 0, 10, 10, 0, 5 };
		float[] ex = new float[] { 0, 10, 10, 0 };
		float[] ey = new float[] { 0, 0, 10, 10 };
		ConvexHull hull = ConvexHull.create(x, y);
		check(ex, ey, hull);
	}

	@Test
	public void canComputeConvexHullFromSquareWithInternalPoint2()
	{
		float[] x = new float[] { 0, 0, 5, 10, 10 };
		float[] y = new float[] { 0, 10, 5, 10, 0 };
		float[] ex = new float[] { 0, 10, 10, 0 };
		float[] ey = new float[] { 0, 0, 10, 10 };
		ConvexHull hull = ConvexHull.create(x, y);
		check(ex, ey, hull);
	}

	private void check(float[] ex, float[] ey, ConvexHull hull)
	{
		if (ex == null)
		{
			Assert.assertTrue(hull == null);
			return;
		}
		int n = ex.length;

		Assert.assertEquals(n, hull.x.length);

		//for (int i = 0; i < ex.length; i++)
		//{
		//	TestSettings.info("[%d] %f==%f (%f), %f==%f (%f)\n", i, ex[i], hull.x[i],
		//			hull.x[i] - ex[i], ey[i], hull.y[i], hull.y[i] - ey[i]);
		//}

		for (int i = 0; i < n; i++)
		{
			Assert.assertEquals(ex[i], hull.x[i], 0);
			Assert.assertEquals(ey[i], hull.y[i], 0);
		}
	}

	@Test
	public void canComputeConvexHullFromOrigin00()
	{
		for (int size : new int[] { 10 })
			for (float w : new float[] { 10, 5 })
				for (float h : new float[] { 10, 5 })
				{
					compute(size, 0, 0, w, h);
				}
	}

	@Test
	public void canComputeConvexHullFromOriginXY()
	{
		for (int size : new int[] { 10 })
			for (float ox : new float[] { -5, 5 })
				for (float oy : new float[] { -5, 5 })
					for (float w : new float[] { 10, 5 })
						for (float h : new float[] { 10, 5 })
						{
							compute(size, ox, oy, w, h);
						}
	}

	private void compute(int size, float ox, float oy, float w, float h)
	{
		float[][] data = createData(size, ox, oy, w, h);
		ConvexHull hull = ConvexHull.create(data[0], data[1]);

		// Simple check of the bounds
		try
		{
			Assert.assertNotNull(hull);
			Rectangle2D.Double bounds = hull.getFloatBounds();
			Assert.assertTrue("xmin " + ox + " " + bounds.getX(), ox <= bounds.getX());
			Assert.assertTrue("ymin " + oy + " " + bounds.getY(), oy <= bounds.getY());
			Assert.assertTrue("xmax " + (ox + w) + " " + bounds.getMaxX(), ox + w >= bounds.getMaxX());
			Assert.assertTrue("ymax " + (ox + h) + " " + bounds.getMaxY(), oy + h >= bounds.getMaxY());
		}
		catch (AssertionError e)
		{
			// Debug
			//for (int i = 0; i < size; i++)
			//	TestSettings.info("[%d] %f,%f\n", i, data[0][i], data[1][i]);
			//if (hull != null)
			//{
			//	for (int i = 0; i < hull.x.length; i++)
			//		TestSettings.info("H[%d] %f,%f\n", i, hull.x[i], hull.y[i]);
			//}
			throw e;
		}
	}

	private float[][] createData(int size, float ox, float oy, float w, float h)
	{
		float[][] data = new float[2][size];
		for (int i = 0; i < size; i++)
		{
			data[0][i] = ox + r.nextFloat() * w;
			data[1][i] = oy + r.nextFloat() * h;
		}
		return data;
	}

	@Test
	public void canCreateWithNoPoints()
	{
		float[] x = new float[0];
		Assert.assertNull(ConvexHull.create(x, x));
	}

	@Test
	public void canCreateWithOnePoint()
	{
		canCreateWithNPoints(1);
	}

	@Test
	public void canCreateWithTwoPoints()
	{
		canCreateWithNPoints(2);
	}

	@Test
	public void canCreateWithThreePoints()
	{
		canCreateWithNPoints(3);
	}

	public void canCreateWithNPoints(int n)
	{
		// Assumes that no random points will be colinear
		float[][] data = createData(n, 0, 0, 10, 10);
		ConvexHull hull = ConvexHull.create(data[0], data[1]);
		Assert.assertEquals(n, hull.size());
		if (n > 1)
			Assert.assertTrue(hull.getLength() > 0);
		else
			Assert.assertTrue(hull.getLength() == 0);
		if (n > 2)
			Assert.assertTrue(hull.getArea() > 0);
		else
			Assert.assertTrue(hull.getArea() == 0);
	}

	@Test
	public void canComputeLengthAndArea()
	{
		// Parallelogram
		float[] x = new float[] { 0, 10, 11, 1 };
		float[] y = new float[] { 0, 0, 10, 10 };
		ConvexHull hull = ConvexHull.create(x, y);
		Assert.assertEquals(2 * 10 + 2 * Math.sqrt(1 * 1 + 10 * 10), hull.getLength(), 1e-6);
		Assert.assertEquals(100, hull.getArea(), 1e-6);

		// Rotated square
		x = new float[] { 0, 10, 9, -1 };
		y = new float[] { 0, 1, 11, 10 };
		hull = ConvexHull.create(x, y);
		double edgeLengthSquared = 1 * 1 + 10 * 10;
		Assert.assertEquals(4 * Math.sqrt(edgeLengthSquared), hull.getLength(), 1e-6);
		Assert.assertEquals(edgeLengthSquared, hull.getArea(), 1e-6);

		// Polygon circle
		int n = 1000;
		double r = 4;
		x = new float[n];
		y = new float[n];
		for (int i = 0; i < 1000; i++)
		{
			double a = i * 2 * Math.PI / n;
			x[i] = (float) (Math.sin(a) * r);
			y[i] = (float) (Math.cos(a) * r);
		}
		hull = ConvexHull.create(x, y);
		Assert.assertEquals(2 * Math.PI * r, hull.getLength(), 1e-2);
		Assert.assertEquals(Math.PI * r * r, hull.getArea(), 1e-2);
	}

	@Test
	public void conComputeContains()
	{
		float[] x = new float[] { 0, 10, 11, 1 };
		float[] y = new float[] { 0, 0, 10, 10 };
		ConvexHull hull = ConvexHull.create(x, y);
		// Contains does not match outer bounds on right or bottom
		Assert.assertTrue(hull.contains(x[0], y[0]));
		for (int i = 1; i < x.length; i++)
			Assert.assertFalse(hull.contains(x[i], y[i]));
		Assert.assertTrue(hull.contains(5, 5));
		Assert.assertFalse(hull.contains(-5, 5));
		Assert.assertFalse(hull.contains(5, -5));
	}
}
