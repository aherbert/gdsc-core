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

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

import gdsc.test.TestSettings;
import gdsc.test.TestSettings.LogLevel;
import gdsc.test.TestSettings.TestComplexity;

@SuppressWarnings({ "javadoc" })
public class MedianWindowTest
{
	int dataSize = 2000;
	int[] radii = new int[] { 0, 1, 2, 4, 8, 16 };
	double[] values = new double[] { 0, -1.1, 2.2 };
	int[] speedRadii = new int[] { 16, 32, 64 };
	int testSpeedRadius = speedRadii[speedRadii.length - 1];
	int[] speedIncrement = new int[] { 1, 2, 4, 8, 16 };

	@Test
	public void testClassCanComputeActualMedian()
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();

		// Verify the internal median method using the Apache commons maths library

		double[] data = createRandomData(rg, dataSize);
		for (final int radius : radii)
			for (int i = 0; i < data.length; i++)
			{
				final double median = calculateMedian(data, i, radius);
				final double median2 = calculateMedian2(data, i, radius);
				Assert.assertEquals(String.format("Position %d, Radius %d", i, radius), median2, median, 1e-6);
			}
		data = createRandomData(rg, dataSize + 1);
		for (final int radius : radii)
			for (int i = 0; i < data.length; i++)
			{
				final double median = calculateMedian(data, i, radius);
				final double median2 = calculateMedian2(data, i, radius);
				Assert.assertEquals(String.format("Position %d, Radius %d", i, radius), median2, median, 1e-6);
			}
	}

	@Test
	public void canComputeMedianForRandomDataUsingDynamicLinkedListIfNewDataIsAboveMedian()
	{
		final double[] data = new double[] { 1, 2, 3, 4, 5 };

		final MedianWindowDLL mw = new MedianWindowDLL(data);
		double median = mw.getMedian();
		double median2 = calculateMedian(data, 2, 2);
		Assert.assertEquals("Before insert", median2, median, 1e-6);

		mw.add(6);
		median = mw.getMedian();
		data[0] = 6;
		median2 = calculateMedian(data, 2, 2);
		Assert.assertEquals("After insert", median2, median, 1e-6);
	}

	@Test
	public void canComputeMedianForRandomDataUsingDynamicLinkedList()
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final double[] data = createRandomData(rg, dataSize);
		for (final int radius : radii)
		{
			final double[] startData = Arrays.copyOf(data, 2 * radius + 1);
			final MedianWindowDLL mw = new MedianWindowDLL(startData);
			for (int i = radius, j = startData.length; j < data.length; i++, j++)
			{
				final double median = mw.getMedian();
				mw.add(data[j]);
				final double median2 = calculateMedian(data, i, radius);
				TestSettings.info("Position %d, Radius %d : %g vs %g\n", i, radius, median2, median);
				Assert.assertEquals(String.format("Position %d, Radius %d", i, radius), median2, median, 1e-6);
			}
		}
	}

	@Test
	public void canComputeMedianForRandomDataUsingSingleIncrement()
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		canComputeMedianForDataUsingSingleIncrement(createRandomData(rg, dataSize));
	}

	@Test
	public void canComputeMedianForRandomDataUsingSetPosition()
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		canComputeMedianForDataUsingSetPosition(createRandomData(rg, dataSize));
	}

	@Test
	public void canComputeMedianForRandomDataUsingBigIncrement()
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		canComputeMedianForDataUsingBigIncrement(createRandomData(rg, dataSize));
	}

	@Test
	public void canComputeMedianForDuplicateDataUsingSingleIncrement()
	{
		for (final double value : values)
			canComputeMedianForDataUsingSingleIncrement(createDuplicateData(dataSize, value));
	}

	@Test
	public void canComputeMedianForDuplicateDataUsingSetPosition()
	{
		for (final double value : values)
			canComputeMedianForDataUsingSetPosition(createDuplicateData(dataSize, value));
	}

	@Test
	public void canComputeMedianForDuplicateDataUsingBigIncrement()
	{
		for (final double value : values)
			canComputeMedianForDataUsingBigIncrement(createDuplicateData(dataSize, value));
	}

	@Test
	public void canComputeMedianForSparseDataUsingSingleIncrement()
	{
		//canComputeMedianForDataUsingSingleIncrement(SimpleArrayUtils.newArray(10, 1.0, 1));

		for (final double value : values)
			canComputeMedianForDataUsingSingleIncrement(createSparseData(dataSize, value));
	}

	@Test
	public void canComputeMedianForSparseDataUsingSetPosition()
	{
		for (final double value : values)
			canComputeMedianForDataUsingSetPosition(createSparseData(dataSize, value));
	}

	@Test
	public void canComputeMedianForSparseDataUsingBigIncrement()
	{
		for (final double value : values)
			canComputeMedianForDataUsingBigIncrement(createSparseData(dataSize, value));
	}

	private void canComputeMedianForDataUsingSingleIncrement(double[] data)
	{
		for (final int radius : radii)
		{
			final MedianWindow mw = new MedianWindow(data, radius);
			for (int i = 0; i < data.length; i++)
			{
				final double median = mw.getMedian();
				mw.increment();
				final double median2 = calculateMedian(data, i, radius);
				//TestSettings.debug("%f vs %f\n", median, median2);
				Assert.assertEquals(String.format("Position %d, Radius %d", i, radius), median2, median, 1e-6);
			}
		}
	}

	private void canComputeMedianForDataUsingSetPosition(double[] data)
	{
		for (final int radius : radii)
		{
			final MedianWindow mw = new MedianWindow(data, radius);
			for (int i = 0; i < data.length; i += 10)
			{
				mw.setPosition(i);
				final double median = mw.getMedian();
				final double median2 = calculateMedian(data, i, radius);
				Assert.assertEquals(String.format("Position %d, Radius %d", i, radius), median2, median, 1e-6);
			}
		}
	}

	private void canComputeMedianForDataUsingBigIncrement(double[] data)
	{
		final int increment = 10;
		for (final int radius : radii)
		{
			final MedianWindow mw = new MedianWindow(data, radius);
			for (int i = 0; i < data.length; i += increment)
			{
				final double median = mw.getMedian();
				mw.increment(increment);
				final double median2 = calculateMedian(data, i, radius);
				Assert.assertEquals(String.format("Position %d, Radius %d", i, radius), median2, median, 1e-6);
			}
		}
	}

	@Test(expected = AssertionError.class)
	public void cannotComputeMedianBackToInputArrayUsingSingleIncrement()
	{
		final double[] data = SimpleArrayUtils.newArray(dataSize, 0.0, 1);
		for (final int radius : radii)
		{
			final double[] in = data.clone();
			final double[] e = new double[in.length];
			MedianWindow mw = new MedianWindow(in, radius);
			for (int i = 0; i < data.length; i++)
			{
				e[i] = mw.getMedian();
				mw.increment();
			}
			// Must create a new window
			mw = new MedianWindow(in, radius);
			for (int i = 0; i < data.length; i++)
			{
				in[i] = mw.getMedian();
				mw.increment();
			}
			Assert.assertArrayEquals(e, in, 0);
		}
	}

	@Test
	public void canIncrementThroughTheDataArray()
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final double[] data = createRandomData(rg, 300);
		for (final int radius : radii)
		{
			MedianWindow mw = new MedianWindow(data, radius);
			int i = 0;
			while (mw.isValidPosition())
			{
				final double median = mw.getMedian();
				final double median2 = calculateMedian(data, i, radius);
				Assert.assertEquals(String.format("Position %d, Radius %d", i, radius), median2, median, 1e-6);

				mw.increment();
				i++;
			}
			Assert.assertEquals("Not all data interated", i, data.length);

			mw = new MedianWindow(data, radius);
			i = 0;
			do
			{
				final double median = mw.getMedian();
				final double median2 = calculateMedian(data, i, radius);
				Assert.assertEquals(String.format("Position %d, Radius %d", i, radius), median2, median, 1e-6);

				i++;
			} while (mw.increment());
			Assert.assertEquals("Not all data interated", i, data.length);
		}
	}

	@Test
	public void canIncrementThroughTheDataArrayUsingBigIncrement()
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final double[] data = createRandomData(rg, 300);
		final int increment = 10;
		for (final int radius : radii)
		{
			final MedianWindow mw = new MedianWindow(data, radius);
			int i = 0;
			while (mw.isValidPosition())
			{
				final double median = mw.getMedian();
				final double median2 = calculateMedian(data, i, radius);
				Assert.assertEquals(String.format("Position %d, Radius %d", i, radius), median2, median, 1e-6);

				mw.increment(increment);
				i += increment;
			}
		}
	}

	@Test
	public void returnNaNForInvalidPositions()
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final double[] data = createRandomData(rg, 300);
		for (final int radius : radii)
		{
			MedianWindow mw = new MedianWindow(data, radius);
			for (int i = 0; i < data.length; i++)
				mw.increment();
			Assert.assertEquals(Double.NaN, mw.getMedian(), 1e-6);

			mw = new MedianWindow(data, radius);
			while (mw.isValidPosition())
				mw.increment();
			Assert.assertEquals(Double.NaN, mw.getMedian(), 1e-6);

			mw = new MedianWindow(data, radius);
			mw.setPosition(data.length + 10);
			Assert.assertEquals(Double.NaN, mw.getMedian(), 1e-6);
		}
	}

	@Test
	public void isFasterThanLocalSort()
	{
		TestSettings.assumeLowComplexity();
		final int[] speedRadii2 = (TestSettings.allow(LogLevel.INFO)) ? speedRadii : new int[] { testSpeedRadius };
		for (final int radius : speedRadii2)
			for (final int increment : speedIncrement)
				isFasterThanLocalSort(radius, increment);
	}

	private void isFasterThanLocalSort(int radius, int increment)
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final int iterations = 20;
		final double[][] data = new double[iterations][];
		for (int i = 0; i < iterations; i++)
			data[i] = createRandomData(rg, dataSize);

		final double[] m1 = new double[dataSize];
		// Initialise class
		MedianWindow mw = new MedianWindow(data[0], radius);
		long t1;
		if (increment == 1)
		{
			do
				mw.getMedian();
			while (mw.increment());

			final long s1 = System.nanoTime();
			for (int iter = 0; iter < iterations; iter++)
			{
				mw = new MedianWindow(data[iter], radius);
				int j = 0;
				do
					m1[j++] = mw.getMedian();
				while (mw.increment());
			}
			t1 = System.nanoTime() - s1;
		}
		else
		{
			while (mw.isValidPosition())
			{
				mw.getMedian();
				mw.increment(increment);
			}

			final long s1 = System.nanoTime();
			for (int iter = 0; iter < iterations; iter++)
			{
				mw = new MedianWindow(data[iter], radius);
				int j = 0;
				while (mw.isValidPosition())
				{
					m1[j++] = mw.getMedian();
					mw.increment(increment);
				}
			}
			t1 = System.nanoTime() - s1;
		}

		final double[] m2 = new double[dataSize];
		// Initialise
		for (int i = 0; i < dataSize; i += increment)
			calculateMedian(data[0], i, radius);
		final long s2 = System.nanoTime();
		for (int iter = 0; iter < iterations; iter++)
			for (int i = 0, j = 0; i < dataSize; i += increment)
				m2[j++] = calculateMedian(data[iter], i, radius);
		final long t2 = System.nanoTime() - s2;

		Assert.assertArrayEquals(m1, m2, 1e-6);
		TestSettings.info("Radius %d, Increment %d : window %d : standard %d = %fx faster\n", radius, increment, t1, t2,
				(double) t2 / t1);

		// Only test the largest radii
		if (radius == testSpeedRadius)
			Assert.assertTrue(String.format("Radius %d, Increment %d", radius, increment), t1 < t2);
	}

	@Test
	public void floatVersionIsFasterThanDoubleVersion()
	{
		TestSettings.assumeLowComplexity();
		final int[] speedRadii2 = (TestSettings.allow(LogLevel.INFO)) ? speedRadii : new int[] { testSpeedRadius };
		for (final int radius : speedRadii2)
			for (final int increment : speedIncrement)
				floatVersionIsFasterThanDoubleVersion(radius, increment);
	}

	private void floatVersionIsFasterThanDoubleVersion(int radius, int increment)
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final int iterations = 20;
		final double[][] data = new double[iterations][];
		final float[][] data2 = new float[iterations][];
		for (int i = 0; i < iterations; i++)
		{
			data[i] = createRandomData(rg, dataSize);
			data2[i] = copyData(data[i]);
		}

		final double[] m1 = new double[dataSize];
		// Initialise class
		MedianWindow mw = new MedianWindow(data[0], radius);
		long t1;
		if (increment == 1)
		{
			do
				mw.getMedian();
			while (mw.increment());

			final long s1 = System.nanoTime();
			for (int iter = 0; iter < iterations; iter++)
			{
				mw = new MedianWindow(data[iter], radius);
				int j = 0;
				do
					m1[j++] = mw.getMedian();
				while (mw.increment());
			}
			t1 = System.nanoTime() - s1;
		}
		else
		{
			while (mw.isValidPosition())
			{
				mw.getMedian();
				mw.increment(increment);
			}

			final long s1 = System.nanoTime();
			for (int iter = 0; iter < iterations; iter++)
			{
				mw = new MedianWindow(data[iter], radius);
				int j = 0;
				while (mw.isValidPosition())
				{
					m1[j++] = mw.getMedian();
					mw.increment(increment);
				}
			}
			t1 = System.nanoTime() - s1;
		}

		final double[] m2 = new double[dataSize];
		// Initialise
		MedianWindowFloat mw2 = new MedianWindowFloat(data2[0], radius);
		long t2;
		if (increment == 1)
		{
			do
				mw2.getMedian();
			while (mw2.increment());

			final long s2 = System.nanoTime();
			for (int iter = 0; iter < iterations; iter++)
			{
				mw2 = new MedianWindowFloat(data2[iter], radius);
				int j = 0;
				do
					m2[j++] = mw2.getMedian();
				while (mw2.increment());
			}
			t2 = System.nanoTime() - s2;
		}
		else
		{
			while (mw2.isValidPosition())
			{
				mw2.getMedian();
				mw2.increment(increment);
			}

			final long s2 = System.nanoTime();
			for (int iter = 0; iter < iterations; iter++)
			{
				mw2 = new MedianWindowFloat(data2[iter], radius);
				int j = 0;
				while (mw2.isValidPosition())
				{
					m2[j++] = mw2.getMedian();
					mw2.increment(increment);
				}
			}
			t2 = System.nanoTime() - s2;
		}

		Assert.assertArrayEquals(m1, m2, 1e-3);

		// Only test the largest radii
		if (radius == testSpeedRadius)
			// Allow a margin of error
			//Assert.assertTrue(String.format("Radius %d, Increment %d", radius, increment), t2 < t1 * 1.1);
			TestSettings.logSpeedTestResult(t2 < t1, "Radius %d, Increment %d : double %d : float %d = %fx faster\n",
					radius, increment, t1, t2, (double) t1 / t2);
		else
			TestSettings.info("Radius %d, Increment %d : double %d : float %d = %fx faster\n", radius, increment, t1,
					t2, (double) t1 / t2);
	}

	@Test
	public void intVersionIsFasterThanDoubleVersion()
	{
		TestSettings.assumeSpeedTest(TestComplexity.LOW);
		for (final int radius : speedRadii)
			for (final int increment : speedIncrement)
				intVersionIsFasterThanDoubleVersion(radius, increment);
	}

	private void intVersionIsFasterThanDoubleVersion(int radius, int increment)
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final int iterations = 20;
		final double[][] data = new double[iterations][];
		final int[][] data2 = new int[iterations][];
		for (int i = 0; i < iterations; i++)
		{
			data[i] = createRandomData(rg, dataSize);
			data2[i] = copyDataInt(data[i]);
		}

		// Initialise class
		MedianWindow mw = new MedianWindow(data[0], radius);
		long t1;
		if (increment == 1)
		{
			do
				mw.getMedian();
			while (mw.increment());

			final long s1 = System.nanoTime();
			for (int iter = 0; iter < iterations; iter++)
			{
				mw = new MedianWindow(data[iter], radius);
				do
					mw.getMedian();
				while (mw.increment());
			}
			t1 = System.nanoTime() - s1;
		}
		else
		{
			while (mw.isValidPosition())
			{
				mw.getMedian();
				mw.increment(increment);
			}

			final long s1 = System.nanoTime();
			for (int iter = 0; iter < iterations; iter++)
			{
				mw = new MedianWindow(data[iter], radius);
				while (mw.isValidPosition())
				{
					mw.getMedian();
					mw.increment(increment);
				}
			}
			t1 = System.nanoTime() - s1;
		}

		// Initialise
		MedianWindowInt mw2 = new MedianWindowInt(data2[0], radius);
		long t2;
		if (increment == 1)
		{
			do
				mw2.getMedian();
			while (mw2.increment());

			final long s2 = System.nanoTime();
			for (int iter = 0; iter < iterations; iter++)
			{
				mw2 = new MedianWindowInt(data2[iter], radius);
				do
					mw2.getMedian();
				while (mw2.increment());
			}
			t2 = System.nanoTime() - s2;
		}
		else
		{
			while (mw2.isValidPosition())
			{
				mw2.getMedian();
				mw2.increment(increment);
			}

			final long s2 = System.nanoTime();
			for (int iter = 0; iter < iterations; iter++)
			{
				mw2 = new MedianWindowInt(data2[iter], radius);
				while (mw2.isValidPosition())
				{
					mw2.getMedian();
					mw2.increment(increment);
				}
			}
			t2 = System.nanoTime() - s2;
		}

		// Only test the largest radii
		if (radius == testSpeedRadius)
			//Assert.assertTrue(String.format("Radius %d, Increment %d", radius, increment), t2 < t1);
			TestSettings.logSpeedTestResult(t2 < t1, "Radius %d, Increment %d : double %d : int %d = %fx faster\n",
					radius, increment, t1, t2, (double) t1 / t2);
		else
			TestSettings.info("Radius %d, Increment %d : double %d : int %d = %fx faster\n", radius, increment, t1, t2,
					(double) t1 / t2);
	}

	static double calculateMedian(double[] data, int position, int radius)
	{
		final int start = FastMath.max(0, position - radius);
		final int end = FastMath.min(position + radius + 1, data.length);
		final double[] cache = new double[end - start];
		for (int i = start, j = 0; i < end; i++, j++)
			cache[j] = data[i];
		//TestSettings.debugln(Arrays.toString(cache));
		Arrays.sort(cache);
		return (cache[(cache.length - 1) / 2] + cache[cache.length / 2]) * 0.5;
	}

	static double calculateMedian2(double[] data, int position, int radius)
	{
		final int start = FastMath.max(0, position - radius);
		final int end = FastMath.min(position + radius + 1, data.length);
		final double[] cache = new double[end - start];
		for (int i = start, j = 0; i < end; i++, j++)
			cache[j] = data[i];
		final DescriptiveStatistics stats = new DescriptiveStatistics(cache);
		return stats.getPercentile(50);
	}

	static double[] createRandomData(RandomGenerator random, int size)
	{
		final double[] data = new double[size];
		for (int i = 0; i < data.length; i++)
			data[i] = random.nextDouble() * size;
		return data;
	}

	double[] createDuplicateData(int size, double value)
	{
		final double[] data = new double[size];
		Arrays.fill(data, value);
		return data;
	}

	static double[] createSparseData(int size, double value)
	{
		final double[] data = new double[size];
		for (int i = 0; i < data.length; i++)
		{
			data[i] = value;
			if (i % 32 == 0)
				value++;
		}
		new Random(30051977).shuffle(data);
		return data;
	}

	static float[] copyData(double[] data)
	{
		final float[] data2 = new float[data.length];
		for (int i = 0; i < data.length; i++)
			data2[i] = (float) data[i];
		return data2;
	}

	static int[] copyDataInt(double[] data)
	{
		final int[] data2 = new int[data.length];
		for (int i = 0; i < data.length; i++)
			data2[i] = (int) data[i];
		return data2;
	}
}
