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
package uk.ac.sussex.gdsc.core.utils;

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import uk.ac.sussex.gdsc.test.TestComplexity;
import uk.ac.sussex.gdsc.test.TestLog;
import uk.ac.sussex.gdsc.test.TestSettings;
import uk.ac.sussex.gdsc.test.junit5.ExtraAssertions;
import uk.ac.sussex.gdsc.test.junit5.ExtraAssumptions;
import uk.ac.sussex.gdsc.test.junit5.SpeedTest;

@SuppressWarnings({ "javadoc" })
public class MedianWindowDLLTest
{
	int dataSize = 2000;
	int[] radii = new int[] { 0, 1, 2, 4, 8, 16 };
	double[] values = new double[] { 0, -1.1, 2.2 };
	int[] speedRadii = new int[] { 16, 32, 64 };
	int[] speedIncrement = new int[] { 1, 2, 4, 6, 8, 12, 16, 24, 32, 48 };

	@Test
	public void canComputeMedianForRandomDataUsingDynamicLinkedListIfNewDataIsAboveMedian()
	{
		final double[] data = new double[] { 1, 2, 3, 4, 5 };

		final MedianWindowDLL mw = new MedianWindowDLL(data);
		double median = mw.getMedian();
		double median2 = MedianWindowTest.calculateMedian(data, 2, 2);
		Assertions.assertEquals(median2, median, 1e-6, "Before insert");

		final double[] insert = new double[] { 6, 7, 6, 7 };
		for (int i = 0; i < insert.length; i++)
		{
			mw.add(insert[i]);
			median = mw.getMedian();
			data[i] = insert[i];
			median2 = MedianWindowTest.calculateMedian(data, 2, 2);
			Assertions.assertEquals(median2, median, 1e-6, "After insert");
		}
	}

	@Test
	public void canComputeMedianForRandomDataUsingDynamicLinkedListIfNewDataIsBelowMedian()
	{
		final double[] data = new double[] { 4, 5, 6, 7, 8 };

		final MedianWindowDLL mw = new MedianWindowDLL(data);
		double median = mw.getMedian();
		double median2 = MedianWindowTest.calculateMedian(data, 2, 2);
		Assertions.assertEquals(median2, median, 1e-6, "Before insert");

		final double[] insert = new double[] { 3, 2, 3, 2 };
		for (int i = 0; i < insert.length; i++)
		{
			mw.add(insert[i]);
			median = mw.getMedian();
			data[i] = insert[i];
			median2 = MedianWindowTest.calculateMedian(data, 2, 2);
			Assertions.assertEquals(median2, median, 1e-6, "After insert");
		}
	}

	@Test
	public void canComputeMedianForRandomDataUsingDynamicLinkedListIfNewDataIsMedianOrAbove()
	{
		final double[] data = new double[] { 1, 2, 3, 4, 5 };

		final MedianWindowDLL mw = new MedianWindowDLL(data);
		double median = mw.getMedian();
		double median2 = MedianWindowTest.calculateMedian(data, 2, 2);
		Assertions.assertEquals(median2, median, 1e-6, "Before insert");

		final double[] insert = new double[] { 3, 6, 3, 6 };
		for (int i = 0; i < insert.length; i++)
		{
			mw.add(insert[i]);
			median = mw.getMedian();
			data[i] = insert[i];
			median2 = MedianWindowTest.calculateMedian(data, 2, 2);
			Assertions.assertEquals(median2, median, 1e-6, "After insert");
		}
	}

	@Test
	public void canComputeMedianForRandomDataUsingDynamicLinkedListIfNewDataIsMedianOrBelow()
	{
		final double[] data = new double[] { 1, 2, 3, 4, 5 };

		final MedianWindowDLL mw = new MedianWindowDLL(data);
		double median = mw.getMedian();
		double median2 = MedianWindowTest.calculateMedian(data, 2, 2);
		Assertions.assertEquals(median2, median, 1e-6, "Before insert");

		final double[] insert = new double[] { 3, 0, 3, 0 };
		for (int i = 0; i < insert.length; i++)
		{
			mw.add(insert[i]);
			median = mw.getMedian();
			data[i] = insert[i];
			median2 = MedianWindowTest.calculateMedian(data, 2, 2);
			Assertions.assertEquals(median2, median, 1e-6, "After insert");
		}
	}

	@Test
	public void canComputeMedianForRandomDataUsingDynamicLinkedList()
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final double[] data = MedianWindowTest.createRandomData(rg, dataSize);
		for (final int radius : radii)
		{
			final double[] startData = Arrays.copyOf(data, 2 * radius + 1);
			final MedianWindowDLL mw = new MedianWindowDLL(startData);
			int p = 0;
			for (int i = 0; i < radius; i++, p++)
			{
				final double median = mw.getMedianOldest(i + 1 + radius);
				final double median2 = MedianWindowTest.calculateMedian(data, p, radius);
				//TestLog.debug("Position %d, Radius %d : %g vs %g\n", p, radius, median2, median);
				ExtraAssertions.assertEquals(median2, median, 1e-6, "Position %d, Radius %d", p, radius);
			}
			for (int j = startData.length; j < data.length; j++, p++)
			{
				final double median = mw.getMedian();
				mw.add(data[j]);
				final double median2 = MedianWindowTest.calculateMedian(data, p, radius);
				//TestLog.debug("Position %d, Radius %d : %g vs %g\n", p, radius, median2, median);
				ExtraAssertions.assertEquals(median2, median, 1e-6, "Position %d, Radius %d", p, radius);
			}
			for (int i = 2 * radius + 1; i-- > 0; p++)
			{
				final double median = mw.getMedianYoungest(i + 1);
				final double median2 = MedianWindowTest.calculateMedian(data, p, radius);
				//TestLog.debug("Position %d, Radius %d : %g vs %g\n", p, radius, median2, median);
				ExtraAssertions.assertEquals(median2, median, 1e-6, "Position %d, Radius %d", p, radius);
			}
		}
	}

	@Test
	public void canComputeMedianForSparseDataUsingDynamicLinkedList()
	{
		for (final double value : values)
		{
			final double[] data = MedianWindowTest.createSparseData(dataSize, value);
			for (final int radius : radii)
			{
				final double[] startData = Arrays.copyOf(data, 2 * radius + 1);
				final MedianWindowDLL mw = new MedianWindowDLL(startData);
				int p = 0;
				for (int i = 0; i < radius; i++, p++)
				{
					final double median = mw.getMedianOldest(i + 1 + radius);
					final double median2 = MedianWindowTest.calculateMedian(data, p, radius);
					//TestLog.debug("Position %d, Radius %d : %g vs %g\n", p, radius, median2, median);
					ExtraAssertions.assertEquals(median2, median, 1e-6, "Position %d, Radius %d", p, radius);
				}
				for (int j = startData.length; j < data.length; j++, p++)
				{
					final double median = mw.getMedian();
					mw.add(data[j]);
					final double median2 = MedianWindowTest.calculateMedian(data, p, radius);
					//TestLog.debug("Position %d, Radius %d : %g vs %g\n", p, radius, median2, median);
					ExtraAssertions.assertEquals(median2, median, 1e-6, "Position %d, Radius %d", p, radius);
				}
				for (int i = 2 * radius + 1; i-- > 0; p++)
				{
					final double median = mw.getMedianYoungest(i + 1);
					final double median2 = MedianWindowTest.calculateMedian(data, p, radius);
					//TestLog.debug("Position %d, Radius %d : %g vs %g\n", p, radius, median2, median);
					ExtraAssertions.assertEquals(median2, median, 1e-6, "Position %d, Radius %d", p, radius);
				}
			}
		}
	}

	@Test
	public void canComputeMedianForDuplicateDataUsingDynamicLinkedList()
	{
		final MedianWindowTest mwt = new MedianWindowTest();
		for (final double value : values)
		{
			final double[] data = mwt.createDuplicateData(dataSize, value);
			for (final int radius : radii)
			{
				final double[] startData = Arrays.copyOf(data, 2 * radius + 1);
				final MedianWindowDLL mw = new MedianWindowDLL(startData);
				int p = 0;
				for (int i = 0; i < radius; i++, p++)
				{
					final double median = mw.getMedianOldest(i + 1 + radius);
					final double median2 = MedianWindowTest.calculateMedian(data, p, radius);
					//TestLog.debug("Position %d, Radius %d : %g vs %g\n", p, radius, median2, median);
					ExtraAssertions.assertEquals(median2, median, 1e-6, "Position %d, Radius %d", p, radius);
				}
				for (int j = startData.length; j < data.length; j++, p++)
				{
					final double median = mw.getMedian();
					mw.add(data[j]);
					final double median2 = MedianWindowTest.calculateMedian(data, p, radius);
					//TestLog.debug("Position %d, Radius %d : %g vs %g\n", p, radius, median2, median);
					ExtraAssertions.assertEquals(median2, median, 1e-6, "Position %d, Radius %d", p, radius);
				}
				for (int i = 2 * radius + 1; i-- > 0; p++)
				{
					final double median = mw.getMedianYoungest(i + 1);
					final double median2 = MedianWindowTest.calculateMedian(data, p, radius);
					//TestLog.debug("Position %d, Radius %d : %g vs %g\n", p, radius, median2, median);
					ExtraAssertions.assertEquals(median2, median, 1e-6, "Position %d, Radius %d", p, radius);
				}
			}
		}
	}

	@SpeedTest
	public void isFasterThanMedianWindowUsingSortedCacheDataWhenIncrementIsSmall()
	{
		ExtraAssumptions.assumeSpeedTest(TestComplexity.LOW);
		for (final int radius : speedRadii)
			for (final int increment : speedIncrement)
			{
				if (increment > radius)
					continue;
				isFasterThanMedianWindowUsingSortedCacheDataWhenIncrementIsSmall(radius, increment);
			}
	}

	private void isFasterThanMedianWindowUsingSortedCacheDataWhenIncrementIsSmall(int radius, int increment)
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final int iterations = 20;
		final double[][] data = new double[iterations][];
		for (int i = 0; i < iterations; i++)
			data[i] = MedianWindowTest.createRandomData(rg, dataSize);

		final double[] m1 = new double[dataSize];
		// Initialise class
		final int finalPosition = dataSize - radius;
		MedianWindow mw = new MedianWindow(data[0], radius);
		mw.setPosition(radius);
		long t1;
		if (increment == 1)
		{
			int j = 0;
			do
			{
				m1[j++] = mw.getMedian();
				mw.increment();
			} while (mw.getPosition() < finalPosition);

			final long s1 = System.nanoTime();
			for (int iter = 0; iter < iterations; iter++)
			{
				mw = new MedianWindow(data[iter], radius);
				mw.setPosition(radius);
				do
				{
					mw.getMedian();
					mw.increment();
				} while (mw.getPosition() < finalPosition);
			}
			t1 = System.nanoTime() - s1;
		}
		else
		{
			int j = 0;
			do
			{
				m1[j++] = mw.getMedian();
				mw.increment(increment);
			} while (mw.getPosition() < finalPosition);

			final long s1 = System.nanoTime();
			for (int iter = 0; iter < iterations; iter++)
			{
				mw = new MedianWindow(data[iter], radius);
				mw.setPosition(radius);
				do
				{
					mw.getMedian();
					mw.increment(increment);
				} while (mw.getPosition() < finalPosition);
			}
			t1 = System.nanoTime() - s1;
		}

		final double[] m2 = new double[dataSize];
		double[] startData = Arrays.copyOf(data[0], 2 * radius + 1);
		MedianWindowDLL mw2 = new MedianWindowDLL(startData);
		long t2;
		if (increment == 1)
		{
			int k = 0;
			m2[k++] = mw2.getMedian();
			for (int j = startData.length; j < data[0].length; j++)
			{
				mw2.add(data[0][j]);
				m2[k++] = mw2.getMedian();
			}
			final long s2 = System.nanoTime();
			for (int iter = 0; iter < iterations; iter++)
			{
				startData = Arrays.copyOf(data[iter], 2 * radius + 1);
				mw2 = new MedianWindowDLL(startData);
				mw2.getMedian();
				for (int j = startData.length; j < data[iter].length; j++)
				{
					mw2.add(data[iter][j]);
					mw2.getMedian();
				}
			}
			t2 = System.nanoTime() - s2;
		}
		else
		{
			final int limit = data[0].length - increment;
			int k = 0;
			m2[k++] = mw2.getMedian();
			for (int j = startData.length; j < limit; j += increment)
			{
				for (int i = 0; i < increment; i++)
					mw2.add(data[0][j + i]);
				m2[k++] = mw2.getMedian();
			}
			final long s2 = System.nanoTime();
			for (int iter = 0; iter < iterations; iter++)
			{
				startData = Arrays.copyOf(data[iter], 2 * radius + 1);
				mw2 = new MedianWindowDLL(startData);
				mw2.getMedian();
				for (int j = startData.length; j < limit; j += increment)
				{
					for (int i = 0; i < increment; i++)
						mw2.add(data[iter][j + i]);
					mw2.getMedian();
				}
			}
			t2 = System.nanoTime() - s2;
		}

		ExtraAssertions.assertArrayEquals(m1, m2, 1e-6, "Radius %d, Increment %d", radius, increment);

		// Only test when the increment is small.
		// When the increment is large then the linked list is doing too many operations
		// verses the full array sort of the cache median window.
		if (increment <= 4)
			TestLog.logSpeedTestResult(t2 < t1, "Radius %d, Increment %d : Cached %d : DLL %d = %fx faster\n", radius,
					increment, t1, t2, (double) t1 / t2);
		else
			TestLog.info("Radius %d, Increment %d : Cached %d : DLL %d = %fx faster\n", radius, increment, t1, t2,
					(double) t1 / t2);
	}
}
