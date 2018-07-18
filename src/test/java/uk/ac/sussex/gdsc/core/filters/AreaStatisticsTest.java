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
package uk.ac.sussex.gdsc.core.filters;

import java.awt.Rectangle;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Test;

import ij.process.FloatProcessor;
import ij.process.ImageStatistics;
import uk.ac.sussex.gdsc.core.utils.Random;
import uk.ac.sussex.gdsc.core.utils.Statistics;
import uk.ac.sussex.gdsc.test.BaseTimingTask;
import uk.ac.sussex.gdsc.test.LogLevel;
import uk.ac.sussex.gdsc.test.TestAssert;
import uk.ac.sussex.gdsc.test.TestLog;
import uk.ac.sussex.gdsc.test.TestSettings;
import uk.ac.sussex.gdsc.test.TimingService;

@SuppressWarnings({ "javadoc" })
public class AreaStatisticsTest
{
	boolean[] rolling = new boolean[] { true, false };
	int[] boxSizes = new int[] { 15, 9, 5, 3, 2, 1 };
	int maxx = 97, maxy = 101;

	@Test
	public void canComputeGlobalStatistics()
	{
		final float[] data = createData(TestSettings.getRandomGenerator());
		final Statistics s = new Statistics(data);
		final AreaStatistics a = new AreaStatistics(data, maxx, maxy);
		for (final boolean r : rolling)
		{
			a.setRollingSums(r);
			double[] o = a.getStatistics(0, 0, maxy);
			Assert.assertEquals(s.getN(), o[AreaSum.N], 0);
			TestAssert.assertEqualsRelative(s.getSum(), o[AreaSum.SUM], 1e-6);
			TestAssert.assertEqualsRelative(s.getStandardDeviation(), o[AreaStatistics.SD], 1e-6);

			o = a.getStatistics(new Rectangle(maxx, maxy));
			Assert.assertEquals(s.getN(), o[AreaSum.N], 0);
			TestAssert.assertEqualsRelative(s.getSum(), o[AreaSum.SUM], 1e-6);
			TestAssert.assertEqualsRelative(s.getStandardDeviation(), o[AreaStatistics.SD], 1e-6);
		}
	}

	@Test
	public void canComputeNxNRegionStatistics()
	{
		final RandomGenerator r = TestSettings.getRandomGenerator();
		final float[] data = createData(r);
		final AreaStatistics a1 = new AreaStatistics(data, maxx, maxy);
		a1.setRollingSums(true);
		final AreaStatistics a2 = new AreaStatistics(data, maxx, maxy);
		a2.setRollingSums(false);

		final FloatProcessor fp = new FloatProcessor(maxx, maxy, data);

		for (final int x : Random.sample(5, maxx, r))
			for (final int y : Random.sample(5, maxy, r))
				for (final int n : boxSizes)
				{
					final double[] e = a1.getStatistics(x, y, n);
					final double[] o = a2.getStatistics(x, y, n);
					TestAssert.assertArrayEqualsRelative(e, o, 1e-6);
					//TestLog.debug("%s vs %s\n", toString(e), toString(o));

					// Check with ImageJ
					fp.setRoi(new Rectangle(x - n, y - n, 2 * n + 1, 2 * n + 1));
					final ImageStatistics s = fp.getStatistics();

					Assert.assertEquals(s.area, o[AreaSum.N], 0);
					final double sum = s.mean * s.area;
					TestAssert.assertEqualsRelative(sum, o[AreaSum.SUM], 1e-6);
					TestAssert.assertEqualsRelative(s.stdDev, o[AreaStatistics.SD], 1e-6);
				}
	}

	@Test
	public void canComputeNxMRegionStatistics()
	{
		final RandomGenerator r = TestSettings.getRandomGenerator();
		final float[] data = createData(r);
		final AreaStatistics a1 = new AreaStatistics(data, maxx, maxy);
		a1.setRollingSums(true);
		final AreaStatistics a2 = new AreaStatistics(data, maxx, maxy);
		a2.setRollingSums(false);

		final FloatProcessor fp = new FloatProcessor(maxx, maxy, data);

		for (final int x : Random.sample(5, maxx, r))
			for (final int y : Random.sample(5, maxy, r))
				for (final int nx : boxSizes)
					for (final int ny : boxSizes)
					{
						final double[] e = a1.getStatistics(x, y, nx, ny);
						final double[] o = a2.getStatistics(x, y, nx, ny);
						TestAssert.assertArrayEqualsRelative(e, o, 1e-6);
						//TestLog.debug("%s vs %s\n", toString(e), toString(o));

						// Check with ImageJ
						fp.setRoi(new Rectangle(x - nx, y - ny, 2 * nx + 1, 2 * ny + 1));
						final ImageStatistics s = fp.getStatistics();

						Assert.assertEquals(s.area, o[AreaSum.N], 0);
						TestAssert.assertEqualsRelative(s.mean * s.area, o[AreaSum.SUM], 1e-6);
						TestAssert.assertEqualsRelative(s.stdDev, o[AreaStatistics.SD], 1e-6);
					}
	}

	@Test
	public void canComputeRectangleRegionStatistics()
	{
		final RandomGenerator r = TestSettings.getRandomGenerator();
		final float[] data = createData(r);
		final AreaStatistics a1 = new AreaStatistics(data, maxx, maxy);
		a1.setRollingSums(true);
		final AreaStatistics a2 = new AreaStatistics(data, maxx, maxy);
		a2.setRollingSums(false);

		final int width = 10, height = 12;
		final Rectangle roi = new Rectangle(width, height);

		final FloatProcessor fp = new FloatProcessor(maxx, maxy, data);

		for (final int x : Random.sample(5, maxx - width, r))
			for (final int y : Random.sample(5, maxy - height, r))
			{
				roi.x = x;
				roi.y = y;
				final double[] e = a1.getStatistics(roi);
				final double[] o = a2.getStatistics(roi);
				TestAssert.assertArrayEqualsRelative(e, o, 1e-6);
				//TestLog.debug("%s vs %s\n", toString(e), toString(o));

				// Check with ImageJ
				fp.setRoi(roi);
				final ImageStatistics s = fp.getStatistics();

				Assert.assertEquals(s.area, o[AreaSum.N], 0);
				TestAssert.assertEqualsRelative(s.mean * s.area, o[AreaSum.SUM], 1e-6);
				TestAssert.assertEqualsRelative(s.stdDev, o[AreaStatistics.SD], 1e-6);
			}
	}

	@Test
	public void canComputeStatisticsWithinClippedBounds()
	{
		final float[] data = new float[] { 1, 2, 3, 4 };
		final AreaStatistics a = new AreaStatistics(data, 2, 2);
		final Statistics stats = new Statistics(data);
		final int c = stats.getN();
		final double u = stats.getSum();
		final double s = stats.getStandardDeviation();
		for (final boolean r : rolling)
		{
			a.setRollingSums(r);
			for (final int n : boxSizes)
			{
				double[] o = a.getStatistics(0, 0, n);
				Assert.assertEquals(c, o[AreaSum.N], 0);
				TestAssert.assertEqualsRelative(u, o[AreaSum.SUM], 1e-6);
				TestAssert.assertEqualsRelative(s, o[AreaStatistics.SD], 1e-6);

				final Rectangle bounds = new Rectangle(2 * n + 1, 2 * n + 1);
				o = a.getStatistics(bounds);
				Assert.assertEquals(c, o[AreaSum.N], 0);
				TestAssert.assertEqualsRelative(u, o[AreaSum.SUM], 1e-6);
				TestAssert.assertEqualsRelative(s, o[AreaStatistics.SD], 1e-6);

				bounds.x--;
				bounds.y--;
				o = a.getStatistics(bounds);
				Assert.assertEquals(c, o[AreaSum.N], 0);
				TestAssert.assertEqualsRelative(u, o[AreaSum.SUM], 1e-6);
				TestAssert.assertEqualsRelative(s, o[AreaStatistics.SD], 1e-6);
			}
		}
	}

	private class MyTimingtask extends BaseTimingTask
	{
		boolean rolling;
		int n;
		float[][] data;
		int[] sample;

		public MyTimingtask(boolean rolling, int n, float[][] data, int[] sample)
		{
			super(((rolling) ? "Rolling" : "Simple") + n);
			this.rolling = rolling;
			this.n = n;
			this.data = data;
			this.sample = sample;
		}

		@Override
		public int getSize()
		{
			return data.length;
		}

		@Override
		public Object getData(int i)
		{
			return data[i];
		}

		@Override
		public Object run(Object data)
		{
			final float[] d = (float[]) data;
			final AreaStatistics a = new AreaStatistics(d, maxx, maxy);
			a.setRollingSums(rolling);
			for (int i = 0; i < sample.length; i += 2)
				a.getStatistics(sample[i], sample[i + 1], n);
			return null;
		}
	}

	@Test
	public void simpleIsfasterAtLowDensityAndNLessThan10()
	{
		// Test the speed for computing the noise around spots at a density of roughly 1 / 100 pixels.
		speedTest(1.0 / 100, false, 1, 10);
	}

	@Test
	public void simpleIsfasterAtMediumDensityAndNLessThan5()
	{
		// Test the speed for computing the noise around each 3x3 box
		// using a region of 3x3 (n=1) to 9x9 (n=4)
		speedTest(1.0 / 9, false, 1, 4);
	}

	@Test
	public void rollingIsfasterAtHighDensity()
	{
		// Since this is a slow test
		TestSettings.assumeMediumComplexity();

		// Test for sampling half the pixels. Ignore the very small box size
		speedTest(0.5, true, 2, Integer.MAX_VALUE);
	}

	private void speedTest(double density, boolean rollingIsFaster, int minN, int maxN)
	{
		final RandomGenerator r = TestSettings.getRandomGenerator();

		final int k = (int) Math.round(maxx * maxy * density);
		final int[] x = Random.sample(k, maxx, r);
		final int[] y = Random.sample(k, maxy, r);
		final int[] sample = new int[k * 2];
		for (int i = 0, j = 0; i < x.length; i++)
		{
			sample[j++] = x[i];
			sample[j++] = y[i];
		}

		final float[][] data = new float[10][];
		for (int i = 0; i < data.length; i++)
			data[i] = createData(r);

		final TimingService ts = new TimingService();
		for (final int n : boxSizes)
		{
			if (n < minN || n > maxN)
				continue;
			ts.execute(new MyTimingtask(true, n, data, sample));
			ts.execute(new MyTimingtask(false, n, data, sample));
		}
		final int size = ts.getSize();
		ts.repeat();
		if (TestSettings.allow(LogLevel.INFO))
			ts.report(size);
		// Do not let this fail the test suite
		//Assert.assertEquals(ts.get(-2).getMean() < ts.get(-1).getMean(), rollingIsFaster);
		TestLog.logSpeedTestResult(ts.get(-2).getMean() < ts.get(-1).getMean() == rollingIsFaster,
				"AreaStatistics Density=%g RollingIsFaster=%b N=%d:%d: rolling %s vs simple %s", density,
				rollingIsFaster, minN, maxN, ts.get(-2).getMean(), ts.get(-1).getMean());
	}

	private float[] createData(RandomGenerator r)
	{
		final float[] d = new float[maxx * maxy];
		for (int i = 0; i < d.length; i++)
			d[i] = r.nextFloat();
		return d;
	}

	static String toString(double[] d)
	{
		return java.util.Arrays.toString(d);
	}
}
