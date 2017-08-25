package gdsc.core.utils;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Assert;
import org.junit.Test;

import gdsc.core.TestSettings;

public class StoredDataStatisticsTest extends StatisticsTest
{
	static StoredDataStatistics stats;
	static int n = 10000;
	static int loops = 100;

	static
	{
		stats = new StoredDataStatistics(n);
		RandomGenerator rand = new Well19937c();
		for (int i = 0; i < n; i++)
			stats.add(rand.nextDouble());
	}

	@Test
	public void getValuesEqualsIterator()
	{
		double[] values = stats.getValues();
		int i = 0;
		for (double d : stats)
		{
			Assert.assertEquals(d, values[i++], 0);
		}
	}

	@SuppressWarnings("unused")
	//@Test
	public void forLoopIsSlowerThanValuesIterator()
	{
		// This fails. Perhaps change the test to use the TimingService for repeat testing.
		
		long start1 = System.nanoTime();
		for (int i = 0; i < loops; i++)
		{
			double total = 0;
			double[] values = stats.getValues();
			for (int j = 0; j < values.length; j++)
			{
				total += values[j];
			}
		}
		start1 = System.nanoTime() - start1;

		long start2 = System.nanoTime();
		for (int i = 0; i < loops; i++)
		{
			double total = 0; 
			for (double d : stats.getValues())
			{
				total += d;
			}
		}
		start2 = System.nanoTime() - start2;

		log("getValues = %d : values for loop = %d : %fx\n", start1, start2, (1.0 * start2) / start1);
		if (TestSettings.ASSERT_SPEED_TESTS)
			Assert.assertTrue(start1 < start2);
	}

	@SuppressWarnings("unused")
	@Test
	public void iteratorIsSlowerUsingdouble()
	{
		long start1 = System.nanoTime();
		for (int i = 0; i < loops; i++)
		{
			for (double d : stats.getValues())
			{
			}
		}
		start1 = System.nanoTime() - start1;

		long start2 = System.nanoTime();
		for (int i = 0; i < loops; i++)
		{
			for (double d : stats)
			{
			}
		}
		start2 = System.nanoTime() - start2;

		log("getValues = %d : iterator<double> = %d : %fx\n", start1, start2, (1.0 * start2) / start1);
		if (TestSettings.ASSERT_SPEED_TESTS)
			Assert.assertTrue(start1 < start2);
	}

	@SuppressWarnings("unused")
	@Test
	public void iteratorIsSlowerUsingDouble()
	{
		long start1 = System.nanoTime();
		for (int i = 0; i < loops; i++)
		{
			for (double d : stats.getValues())
			{
			}
		}
		start1 = System.nanoTime() - start1;

		long start2 = System.nanoTime();
		for (int i = 0; i < loops; i++)
		{
			for (Double d : stats)
			{
			}
		}
		start2 = System.nanoTime() - start2;

		log("getValues = %d : iterator<Double> = %d : %fx\n", start1, start2, (1.0 * start2) / start1);
		if (TestSettings.ASSERT_SPEED_TESTS)
			Assert.assertTrue(start1 < start2);
	}

	void log(String format, Object... args)
	{
		System.out.printf(format, args);
	}
}
