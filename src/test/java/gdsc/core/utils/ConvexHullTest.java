package gdsc.core.utils;

import java.awt.Polygon;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Assert;
import org.junit.Test;

import ij.gui.PolygonRoi;

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
			for (int m : new int[] { 10 })
			{
				compute(size, 0, 0, m);
			}
	}

	@Test
	public void canComputeConvexHullFromOriginXY()
	{
		for (int size : new int[] { 10 })
			for (int ox : new int[] { -5, 5 })
				for (int oy : new int[] { -5, 5 })
					for (int m : new int[] { 10 })
					{
						compute(size, ox, oy, m);
					}
	}

	private void compute(int size, int ox, int oy, int m)
	{
		int[][] data = createData(size, ox, oy, m);
		float[] x = new float[size];
		float[] y = new float[size];
		for (int i = 0; i < size; i++)
		{
			x[i] = data[i][0];
			y[i] = data[i][1];
		}
		ConvexHull hull = ConvexHull.create(x, y);

		// Check
		PolygonRoi roi = new PolygonRoi(x, y, PolygonRoi.POLYGON);
		Polygon expected = roi.getConvexHull();

		if (expected == null)
		{
			Assert.assertTrue(hull == null);
			return;
		}

		Assert.assertEquals(expected.npoints, hull.x.length);

		//for (int i = 0; i < expected.npoints; i++)
		//{
		//	System.out.printf("[%d] %d==%f (%f), %d==%f (%f)\n", i, expected.xpoints[i], hull.x[i],
		//			hull.x[i] - expected.xpoints[i], expected.ypoints[i], hull.y[i], hull.y[i] - expected.ypoints[i]);
		//}

		for (int i = 0; i < expected.npoints; i++)
		{
			Assert.assertEquals(expected.xpoints[i], hull.x[i], 0);
			Assert.assertEquals(expected.ypoints[i], hull.y[i], 0);
		}
	}

	private int[][] createData(int size, int ox, int oy, int m)
	{
		int[][] data = new int[size][2];
		for (int i = 0; i < size; i++)
		{
			data[i][0] = ox + r.nextInt(m);
			data[i][1] = oy + r.nextInt(m);
		}
		return data;
	}
}
