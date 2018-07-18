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

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Test;

import gdsc.test.TestLog;
import gdsc.test.TestSettings;

@SuppressWarnings({ "javadoc" })
public class StoredDataStatisticsTest extends StatisticsTest
{
	static StoredDataStatistics stats;
	static int n = 10000;
	static int loops = 100;

	static
	{
		stats = new StoredDataStatistics(n);
		final RandomGenerator rand = TestSettings.getRandomGenerator();
		for (int i = 0; i < n; i++)
			stats.add(rand.nextDouble());
	}

	@Test
	public void getValuesEqualsIterator()
	{
		final double[] values = stats.getValues();
		int i = 0;
		for (final double d : stats)
			Assert.assertEquals(d, values[i++], 0);
	}

	@SuppressWarnings("unused")
	//@Test
	public void forLoopIsSlowerThanValuesIterator()
	{
		// This fails. Perhaps change the test to use the TimingService for repeat testing.
		TestSettings.assumeSpeedTest();

		long start1 = System.nanoTime();
		for (int i = 0; i < loops; i++)
		{
			double total = 0;
			final double[] values = stats.getValues();
			for (int j = 0; j < values.length; j++)
				total += values[j];
		}
		start1 = System.nanoTime() - start1;

		long start2 = System.nanoTime();
		for (int i = 0; i < loops; i++)
		{
			double total = 0;
			for (final double d : stats.getValues())
				total += d;
		}
		start2 = System.nanoTime() - start2;

		TestLog.logSpeedTestResult(start1 < start2, "getValues = %d : values for loop = %d : %fx\n", start1,
				start2, (1.0 * start2) / start1);
	}

	@SuppressWarnings("unused")
	@Test
	public void iteratorIsSlowerUsingdouble()
	{
		TestSettings.assumeSpeedTest();
		long start1 = System.nanoTime();
		for (int i = 0; i < loops; i++)
			for (final double d : stats.getValues())
			{
				// Do nothing
			}
		start1 = System.nanoTime() - start1;

		long start2 = System.nanoTime();
		for (int i = 0; i < loops; i++)
			for (final double d : stats)
			{
				// Do nothing
			}
		start2 = System.nanoTime() - start2;

		TestLog.logSpeedTestResult(start1 < start2, "getValues = %d : iterator<double> = %d : %fx\n", start1,
				start2, (1.0 * start2) / start1);
	}

	@SuppressWarnings("unused")
	@Test
	public void iteratorIsSlowerUsingDouble()
	{
		TestSettings.assumeSpeedTest();
		long start1 = System.nanoTime();
		for (int i = 0; i < loops; i++)
			for (final double d : stats.getValues())
			{
				// Do nothing
			}
		start1 = System.nanoTime() - start1;

		long start2 = System.nanoTime();
		for (int i = 0; i < loops; i++)
			for (final Double d : stats)
			{
				// Do nothing
			}
		start2 = System.nanoTime() - start2;

		TestLog.logSpeedTestResult(start1 < start2, "getValues = %d : iterator<Double> = %d : %fx\n", start1,
				start2, (1.0 * start2) / start1);
	}

	@Test
	public void canConstructWithData()
	{
		// This requires that the constructor correctly initialises the storage
		@SuppressWarnings("unused")
		StoredDataStatistics s;
		s = new StoredDataStatistics(new double[] { 1, 2, 3 });
		s = new StoredDataStatistics(new float[] { 1, 2, 3 });
		s = new StoredDataStatistics(new int[] { 1, 2, 3 });
	}
}
