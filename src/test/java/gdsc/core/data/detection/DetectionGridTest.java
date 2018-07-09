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
package gdsc.core.data.detection;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import gdsc.core.utils.SimpleArrayUtils;
import gdsc.test.BaseTimingTask;
import gdsc.test.TestSettings;
import gdsc.test.TimingService;
import gdsc.test.TestSettings.LogLevel;
import gdsc.test.TestSettings.TestComplexity;

@SuppressWarnings({ "javadoc" })
public class DetectionGridTest
{
	@Test
	public void canDetectCollisionsUsingSimpleGrid()
	{
		Rectangle[] r = new Rectangle[3];
		r[0] = new Rectangle(0, 0, 10, 10);
		r[1] = new Rectangle(0, 5, 10, 5);
		r[2] = new Rectangle(5, 5, 5, 5);
		SimpleDetectionGrid g = new SimpleDetectionGrid(r);
		Assert.assertArrayEquals(new int[] { 0 }, g.find(0, 0));
		Assert.assertArrayEquals(new int[] { 0, 1, 2 }, g.find(5, 5));
		Assert.assertArrayEquals(new int[0], g.find(-5, 5));

		// Definition of insideness
		Assert.assertArrayEquals(new int[0], g.find(10, 10));
		g.includeOuterEdge = true;
		Assert.assertArrayEquals(new int[] { 0, 1, 2 }, g.find(10, 10));
	}

	@Test
	public void canFindIndicesUsingBinaryTreeGrid()
	{
		double[] data = SimpleArrayUtils.newArray(10, 0, 1.0);
		int i1, i2;
		for (int i = 0; i < data.length; i++)
		{
			i1 = BinarySearchDetectionGrid.findIndexUpToAndIncluding(data, data[i]);
			Assert.assertEquals(i, i1);
			i1 = BinarySearchDetectionGrid.findIndexUpToAndIncluding(data, data[i] + 0.1);
			Assert.assertEquals(i, i1);
			i1 = BinarySearchDetectionGrid.findIndexUpToAndIncluding(data, data[i] - 0.1);
			Assert.assertEquals(i - 1, i1);

			i2 = BinarySearchDetectionGrid.findIndexIncludingAndAfter(data, data[i]);
			Assert.assertEquals(i, i2);
			i2 = BinarySearchDetectionGrid.findIndexIncludingAndAfter(data, data[i] - 0.1);
			Assert.assertEquals(i, i2);
			i2 = BinarySearchDetectionGrid.findIndexIncludingAndAfter(data, data[i] + 0.1);
			Assert.assertEquals(i + 1, i2);

			i2 = BinarySearchDetectionGrid.findIndexAfter(data, data[i]);
			Assert.assertEquals(i + 1, i2);
			i2 = BinarySearchDetectionGrid.findIndexAfter(data, data[i] - 0.1);
			Assert.assertEquals(i, i2);
			i2 = BinarySearchDetectionGrid.findIndexAfter(data, data[i] + 0.1);
			Assert.assertEquals(i + 1, i2);
		}

		// Handle identity by testing with duplicates
		for (int i = 0; i < data.length; i++)
			data[i] = i / 2;

		for (int i = 0; i < data.length; i++)
		{
			i1 = BinarySearchDetectionGrid.findIndexUpToAndIncluding(data, data[i]);
			Assert.assertEquals(i + (i + 1) % 2, i1);
			i1 = BinarySearchDetectionGrid.findIndexUpToAndIncluding(data, data[i] + 0.1);
			Assert.assertEquals(i + (i + 1) % 2, i1);
			i1 = BinarySearchDetectionGrid.findIndexUpToAndIncluding(data, data[i] - 0.1);
			Assert.assertEquals(i - i % 2 - 1, i1);

			i2 = BinarySearchDetectionGrid.findIndexIncludingAndAfter(data, data[i]);
			Assert.assertEquals(i - i % 2, i2);
			i2 = BinarySearchDetectionGrid.findIndexIncludingAndAfter(data, data[i] - 0.1);
			Assert.assertEquals(i - i % 2, i2);
			i2 = BinarySearchDetectionGrid.findIndexIncludingAndAfter(data, data[i] + 0.1);
			Assert.assertEquals(i - i % 2 + 2, i2);

			i2 = BinarySearchDetectionGrid.findIndexAfter(data, data[i]);
			Assert.assertEquals(i - i % 2 + 2, i2);
			i2 = BinarySearchDetectionGrid.findIndexAfter(data, data[i] - 0.1);
			Assert.assertEquals(i - i % 2, i2);
			i2 = BinarySearchDetectionGrid.findIndexAfter(data, data[i] + 0.1);
			Assert.assertEquals(i - i % 2 + 2, i2);
		}
	}

	@Test
	public void canDetectCollisionsUsingBinaryTreeGrid()
	{
		Rectangle[] r = new Rectangle[3];
		r[0] = new Rectangle(0, 0, 10, 10);
		r[1] = new Rectangle(0, 5, 10, 5);
		r[2] = new Rectangle(5, 5, 5, 5);
		BinarySearchDetectionGrid g = new BinarySearchDetectionGrid(r);
		Assert.assertArrayEquals(new int[] { 0 }, g.find(0, 0));
		Assert.assertArrayEquals(new int[] { 0, 1, 2 }, g.find(5, 5));
		Assert.assertArrayEquals(new int[0], g.find(-5, 5));

		// Respect the insideness definition
		Assert.assertArrayEquals(new int[0], g.find(10, 10));
	}

	@Test
	public void canDetectTheSameCollisions()
	{
		int size = 512;
		RandomDataGenerator rdg = new RandomDataGenerator(TestSettings.getRandomGenerator());
		Rectangle2D[] r = generateRectangles(rdg, 1000, size);

		SimpleDetectionGrid g1 = new SimpleDetectionGrid(r);
		BinarySearchDetectionGrid g2 = new BinarySearchDetectionGrid(r);

		double[][] points = generatePoints(rdg, 500, size);

		for (double[] p : points)
		{
			int[] e = g1.find(p[0], p[1]);
			int[] o = g2.find(p[0], p[1]);
			Arrays.sort(e);
			Arrays.sort(o);
			//TestSettings.debugln(Arrays.toString(e));
			//TestSettings.debugln(Arrays.toString(o));
			Assert.assertArrayEquals(e, o);
		}
	}

	private Rectangle2D[] generateRectangles(RandomDataGenerator rdg, int n, int size)
	{
		Rectangle2D[] r = new Rectangle2D[n];
		double[][] p1 = generatePoints(rdg, n, size);
		double[][] p2 = generatePoints(rdg, n, size);
		for (int i = 0; i < r.length; i++)
		{
			double x1 = p1[i][0];
			double x2 = p1[i][1];
			double y1 = p2[i][0];
			double y2 = p2[i][1];
			if (x2 < x1)
			{
				double tmp = x2;
				x2 = x1;
				x1 = tmp;
			}
			if (y2 < y1)
			{
				double tmp = y2;
				y2 = y1;
				y1 = tmp;
			}
			r[i] = new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
		}
		return r;
	}

	private Rectangle2D[] generateSmallRectangles(RandomDataGenerator rdg, int n, int size, int width)
	{
		Rectangle2D[] r = new Rectangle2D[n];
		double[][] p1 = generatePoints(rdg, n, size);
		for (int i = 0; i < r.length; i++)
		{
			double x1 = p1[i][0];
			double y1 = p1[i][1];
			double w = rdg.nextUniform(1, width);
			double h = rdg.nextUniform(1, width);
			r[i] = new Rectangle2D.Double(x1, y1, w, h);
		}
		return r;
	}

	private double[][] generatePoints(RandomDataGenerator rdg, int n, int size)
	{
		double[][] x = new double[n][];
		while (n-- > 0)
		{
			x[n] = new double[] { rdg.nextUniform(0, size), rdg.nextUniform(0, size) };
		}
		return x;
	}

	private class MyTimingtask extends BaseTimingTask
	{
		DetectionGrid g;
		double[][] points;

		public MyTimingtask(DetectionGrid g, double[][] points)
		{
			super(g.getClass().getSimpleName() + g.size());
			this.g = g;
			this.points = points;
		}

		@Override
		public int getSize()
		{
			return 1;
		}

		@Override
		public Object getData(int i)
		{
			return points;
		}

		@Override
		public Object run(Object data)
		{
			double[][] points = (double[][]) data;
			for (double[] p : points)
				g.find(p[0], p[1]);
			return null;
		}
	}

	@Test
	public void binaryTreeIsFasterWithBigRectangles()
	{
		int size = 512;
		int width = 200;
		int n = 10000;
		int np = 500;
		speedTest(size, width, n, np);
	}

	@Test
	public void binaryTreeIsFasterWithSmallRectangles()
	{
		int size = 512;
		int width = 10;
		int n = 10000;
		int np = 500;
		speedTest(size, width, n, np);
	}

	private void speedTest(int size, int width, int n, int np)
	{
		Assume.assumeTrue(TestSettings.allow(LogLevel.INFO, TestComplexity.MEDIUM));

		RandomDataGenerator rdg = new RandomDataGenerator(TestSettings.getRandomGenerator());

		TimingService ts = new TimingService();
		while (n > 500)
		{
			Rectangle2D[] r = generateSmallRectangles(rdg, n, size, width);

			SimpleDetectionGrid g1 = new SimpleDetectionGrid(r);
			BinarySearchDetectionGrid g2 = new BinarySearchDetectionGrid(r);

			double[][] points = generatePoints(rdg, np, size);
			ts.execute(new MyTimingtask(g1, points));
			ts.execute(new MyTimingtask(g2, points));
			n /= 2;
		}
		int i = ts.getSize();
		ts.repeat();
		ts.report();
		for (int i1 = -1, i2 = -2; i > 0; i -= 2, i1 -= 2, i2 -= 2)
		{
			double t1 = ts.get(i1).getMean();
			double t2 = ts.get(i2).getMean();
			//TestSettings.debug("%f < %f\n", t1, t2);
			Assert.assertTrue(String.format("%f < %f\n", t1, t2), t1 < t2);
		}
	}
}
