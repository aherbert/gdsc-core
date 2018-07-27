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

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import uk.ac.sussex.gdsc.test.DataCache;
import uk.ac.sussex.gdsc.test.DataProvider;
import uk.ac.sussex.gdsc.test.TestLog;
import uk.ac.sussex.gdsc.test.TestSettings;
import uk.ac.sussex.gdsc.test.junit5.ExtraAssumptions;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.junit5.SpeedTag;

@SuppressWarnings({ "javadoc" })
public class StoredDataStatisticsTest extends StatisticsTest implements DataProvider<RandomSeed, StoredDataStatistics>
{
	static int n = 10000;
	static int loops = 100;
	private static DataCache<RandomSeed, StoredDataStatistics> data = new DataCache<>();

	@Override
	public StoredDataStatistics getData(RandomSeed seed)
	{
		UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());
		StoredDataStatistics stats = new StoredDataStatistics(n);
		for (int i = 0; i < n; i++)
			stats.add(r.nextDouble());
		return stats;
	}

	@SeededTest
	public void getValuesEqualsIterator(RandomSeed seed)
	{
		StoredDataStatistics stats = data.getData(seed, this);

		final double[] values = stats.getValues();
		int i = 0;
		for (final double d : stats)
			Assertions.assertEquals(d, values[i++]);
	}

	@SuppressWarnings("unused")
	//@SpeedTag
	@SeededTest
	public void forLoopIsSlowerThanValuesIterator(RandomSeed seed)
	{
		// This fails. Perhaps change the test to use the TimingService for repeat testing.
		ExtraAssumptions.assumeSpeedTest();

		StoredDataStatistics stats = data.getData(seed, this);

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

		TestLog.logSpeedTestResult(start1 < start2, "getValues = %d : values for loop = %d : %fx\n", start1, start2,
				(1.0 * start2) / start1);
	}

	@SuppressWarnings("unused")
	@SpeedTag
	@SeededTest
	public void iteratorIsSlowerUsingdouble(RandomSeed seed)
	{
		StoredDataStatistics stats = data.getData(seed, this);

		ExtraAssumptions.assumeSpeedTest();
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

		TestLog.logSpeedTestResult(start1 < start2, "getValues = %d : iterator<double> = %d : %fx\n", start1, start2,
				(1.0 * start2) / start1);
	}

	@SuppressWarnings("unused")
	@SpeedTag
	@SeededTest
	public void iteratorIsSlowerUsingDouble(RandomSeed seed)
	{
		StoredDataStatistics stats = data.getData(seed, this);

		ExtraAssumptions.assumeSpeedTest();
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

		TestLog.logSpeedTestResult(start1 < start2, "getValues = %d : iterator<Double> = %d : %fx\n", start1, start2,
				(1.0 * start2) / start1);
	}

	@Test
	public void canConstructWithData()
	{
		// This requires that the constructor correctly initialises the storage
		StoredDataStatistics s;
		s = new StoredDataStatistics(new double[] { 1, 2, 3 });
		s.add(1d);
		s = new StoredDataStatistics(new float[] { 1, 2, 3 });
		s.add(1f);
		s = new StoredDataStatistics(new int[] { 1, 2, 3 });
		s.add(1);
	}
}
