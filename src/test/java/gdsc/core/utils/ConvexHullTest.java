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
		float[] x = new float[] { 0, 0, 10, 10 };
		float[] y = new float[] { 0, 10, 10, 0 };
		ConvexHull hull = ConvexHull.create(x, y);
		check(x, y, hull);
	}

	@Test
	public void canComputeConvexHullFromSquareWithInternalPoint()
	{
		float[] x = new float[] { 0, 0, 10, 10, 5 };
		float[] y = new float[] { 0, 10, 10, 0, 5 };
		float[] ex = new float[] { 0, 0, 10, 10 };
		float[] ey = new float[] { 0, 10, 10, 0 };
		ConvexHull hull = ConvexHull.create(x, y);
		check(ex, ey, hull);
	}

	@Test
	public void canComputeConvexHullFromSquareWithInternalPoint2()
	{
		float[] x = new float[] { 0, 0, 5, 10, 10 };
		float[] y = new float[] { 0, 10, 5, 10, 0 };
		float[] ex = new float[] { 0, 0, 10, 10 };
		float[] ey = new float[] { 0, 10, 10, 0 };
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

		//for (int i = 0; i < expected.npoints; i++)
		//{
		//	System.out.printf("[%d] %d==%f (%f), %d==%f (%f)\n", i, expected.xpoints[i], hull.x[i],
		//			hull.x[i] - expected.xpoints[i], expected.ypoints[i], hull.y[i], hull.y[i] - expected.ypoints[i]);
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
			//	System.out.printf("[%d] %f,%f\n", i, data[0][i], data[1][i]);
			//if (hull != null)
			//{
			//	for (int i = 0; i < hull.x.length; i++)
			//		System.out.printf("H[%d] %f,%f\n", i, hull.x[i], hull.y[i]);
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
}
