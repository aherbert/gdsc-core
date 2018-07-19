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
package uk.ac.sussex.gdsc.core.match;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.MathArrays;
import org.junit.Assert;
import org.junit.Test;

import uk.ac.sussex.gdsc.test.TestLog;
import uk.ac.sussex.gdsc.test.TestSettings;
import uk.ac.sussex.gdsc.test.junit4.TestAssume;

@SuppressWarnings({ "javadoc" })
public class RandIndexTest
{
	@Test
	public void canComputeSimpleRandIndexWithNoData()
	{
		for (final int size : new int[] { 0, 1, 2 })
		{
			final double e = (size == 0) ? 0 : 1;
			final int[] clusters = new int[size];
			final double r = RandIndex.simpleRandIndex(clusters, clusters);
			Assert.assertEquals(e, r, 0);
		}
	}

	@Test
	public void canComputeRandIndexWithNoData()
	{
		for (final int size : new int[] { 0, 1, 2 })
		{
			final double e = (size == 0) ? 0 : 1;
			final int[] clusters = new int[size];
			final double r = RandIndex.randIndex(clusters, clusters);
			Assert.assertEquals(e, r, 0);
		}
	}

	@Test
	public void canComputeRandIndex2WithNoData()
	{
		for (final int size : new int[] { 0, 1, 2 })
		{
			final double e = (size == 0) ? 0 : 1;
			final int[] clusters = new int[size];
			final double r = RandIndex.randIndex(clusters, 1, clusters, 1);
			Assert.assertEquals(e, r, 0);
		}
	}

	@Test
	public void canComputeAdjustedRandIndexWithNoData()
	{
		for (final int size : new int[] { 0, 1, 2 })
		{
			final double e = (size == 0) ? 0 : 1;
			final int[] clusters = new int[size];
			final double r = RandIndex.adjustedRandIndex(clusters, 1, clusters, 1);
			Assert.assertEquals(e, r, 0);
		}
	}

	@Test
	public void canComputeAdjustedRandIndexWhenNoRandomness()
	{
		// Q. should this be zero?
		final double e = 1;

		final int[] clusters = new int[2];
		clusters[1] = 1;
		final double r = RandIndex.adjustedRandIndex(clusters, 2, clusters, 2);
		Assert.assertEquals(e, r, 0);
	}

	// The example data and answer are from:
	// http://stats.stackexchange.com/questions/89030/rand-index-calculation

	@Test
	public void canComputeSimpleRandIndex()
	{
		final int[] clusters = { 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2 };
		final int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0 };
		final double r = RandIndex.simpleRandIndex(clusters, classes);
		Assert.assertEquals(0.67647058823529416, r, 1e-10);
	}

	@Test
	public void canComputeRandIndex()
	{
		final int[] clusters = { 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2 };
		final int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0 };
		final double r = RandIndex.randIndex(clusters, classes);
		Assert.assertEquals(0.67647058823529416, r, 1e-10);
	}

	@Test
	public void canComputeRandIndexWithArbitraryClusterNumbers()
	{
		final int[] clusters = { 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2 };
		final int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0 };
		final double r = RandIndex.simpleRandIndex(clusters, classes);

		//@formatter:off
		final int[][] maps = new int[][] {
			{ 0,1,2 },
			{ 0,2,1 },
			{ 1,0,2 },
			{ 1,2,0 },
			{ 2,0,1 },
			{ 2,1,0 },
		};
		//@formatter:on
		final RandIndex ri = new RandIndex();
		for (final int[] map : maps)
		{
			final int[] c2 = new int[classes.length];
			for (int i = 0; i < c2.length; i++)
				c2[i] = map[classes[i]];
			Assert.assertEquals(r, ri.getRandIndex(clusters, 3, c2, 3), 0);
		}
	}

	@Test
	public void canComputeRandIndex2()
	{
		final int[] clusters = { 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2 };
		final int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0 };
		final double r = RandIndex.randIndex(clusters, 3, classes, 3);
		Assert.assertEquals(0.67647058823529416, r, 1e-10);
	}

	@Test
	public void canComputeSimpleRandIndexWithNegativeData()
	{
		final int[] clusters = { 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, 2, 2, 2, 2, 2 };
		final int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, -2, 1, 0, -2, -2, -2, 0 };
		final double r = RandIndex.simpleRandIndex(clusters, classes);
		Assert.assertEquals(0.67647058823529416, r, 1e-10);
	}

	@Test
	public void canComputeRandIndexWithNegativeData()
	{
		final int[] clusters = { 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, 2, 2, 2, 2, 2 };
		final int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, -2, 1, 0, -2, -2, -2, 0 };
		final double r = RandIndex.randIndex(clusters, classes);
		Assert.assertEquals(0.67647058823529416, r, 1e-10);
	}

	@Test
	public void canComputeRandIndex2WithNegativeData()
	{
		final int[] clusters = { 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, 2, 2, 2, 2, 2 };
		final int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, -2, 1, 0, -2, -2, -2, 0 };
		final double r = RandIndex.randIndex(clusters, 3, classes, 3);
		Assert.assertEquals(0.67647058823529416, r, 1e-10);
	}

	@Test
	public void canComputeRandIndexWhenInvalidNClusters()
	{
		final int[] clusters = { 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2 };
		final int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0 };
		final double r = RandIndex.randIndex(clusters, 2, classes, 3);
		Assert.assertEquals(0.67647058823529416, r, 1e-10);
	}

	@Test
	public void canComputeAdjustedRandIndexWhenInvalidNClusters()
	{
		final int[] clusters = { 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2 };
		final int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0 };
		final double e = RandIndex.adjustedRandIndex(clusters, 3, classes, 3);
		final double o = RandIndex.adjustedRandIndex(clusters, 2, classes, 3);
		Assert.assertEquals(e, o, 0);
	}

	@Test
	public void canComputeSimpleRandIndexWithSparseData()
	{
		final int[] clusters = { 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6 };
		final int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 9, 1, 0, 9, 9, 9, 0 };
		final double r = RandIndex.simpleRandIndex(clusters, classes);
		Assert.assertEquals(0.67647058823529416, r, 1e-10);
	}

	@Test
	public void canComputeRandIndexWithSparseData()
	{
		final int[] clusters = { 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6 };
		final int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 9, 1, 0, 9, 9, 9, 0 };
		final double r = RandIndex.randIndex(clusters, classes);
		Assert.assertEquals(0.67647058823529416, r, 1e-10);
	}

	@Test
	public void canComputeRandIndex2WithSparseData()
	{
		final int[] clusters = { 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6 };
		final int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 9, 1, 0, 9, 9, 9, 0 };
		final double r = RandIndex.randIndex(clusters, 7, classes, 10);
		Assert.assertEquals(0.67647058823529416, r, 1e-10);
	}

	@Test(expected = IllegalStateException.class)
	public void getRandIndexThrowsWhenNotComputed()
	{
		final RandIndex ri = new RandIndex();
		ri.getRandIndex();
	}

	@Test(expected = IllegalStateException.class)
	public void getAdjustedRandIndexThrowsWhenNotComputed()
	{
		final RandIndex ri = new RandIndex();
		ri.getAdjustedRandIndex();
	}

	@Test
	public void canComputeRandIndexWithSimpleData()
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final int size = 100;
		for (final int n1 : new int[] { 1, 2, 3, 4, 5 })
			for (final int n2 : new int[] { 1, 2, 3, 4, 5 })
				canComputeRandIndexWithData(rg, size, n1, n2);
	}

	@Test
	public void canComputeRandIndexWithBigData()
	{
		TestAssume.assumeLowComplexity();
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final int size = 10000;
		for (final int i : new int[] { 3, 5, 10 })
		{
			final int n1 = size / i;
			final int n2 = size / i;
			canComputeRandIndexWithData(rg, size, n1, n2);
		}
		for (final int i : new int[] { 3, 5, 10 })
		{
			final int n1 = size / i;
			final int n2 = i;
			canComputeRandIndexWithData(rg, size, n1, n2);
		}
		for (final int i : new int[] { 3, 5, 10 })
		{
			final int n1 = i;
			final int n2 = i;
			canComputeRandIndexWithData(rg, size, n1, n2);
		}
	}

	private static void canComputeRandIndexWithData(RandomGenerator rg, int size, int n1, int n2)
	{
		final int n = size;
		final int[] c1 = new int[size];
		final int[] c2 = new int[size];
		while (size-- > 0)
		{
			c1[size] = size % n1;
			c2[size] = size % n2;
		}
		MathArrays.shuffle(c1, rg);

		final long t1 = System.nanoTime();
		final double e = RandIndex.simpleRandIndex(c1, c2);
		final long t2 = System.nanoTime();
		final double o1 = RandIndex.randIndex(c1, c2);
		final long t3 = System.nanoTime();
		final double o2 = RandIndex.randIndex(c1, n1, c2, n2);
		final long t4 = System.nanoTime();

		final long simple = t2 - t1;
		final long table1 = t3 - t2;
		final long table2 = t4 - t3;

		TestLog.info("[%d,%d,%d] simple=%d (%f), table1=%d (%f), %f\n", n, n1, n2, simple, e, table1, o1,
				simple / (double) table1);
		TestLog.info("[%d,%d,%d] simple=%d (%f), table2=%d (%f), %f\n", n, n1, n2, simple, e, table2, o2,
				simple / (double) table2);

		Assert.assertEquals(e, o1, e * 1e-10);
		Assert.assertEquals(o2, o1, 0);
	}

	@Test
	public void adjustedRandIndexIsZeroForRandomData()
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final int size = 100;
		for (final int n1 : new int[] { 2, 5, 10 })
			for (final int n2 : new int[] { 2, 5 })
				adjustedRandIndexIsZeroForRandomData(rg, size, n1, n2, 10);
	}

	private static void adjustedRandIndexIsZeroForRandomData(RandomGenerator rg, int size, int n1, int n2, int loops)
	{
		final int n = size;
		final int[] c1 = new int[size];
		final int[] c2 = new int[size];
		while (size-- > 0)
		{
			c1[size] = size % n1;
			c2[size] = size % n2;
		}
		final RandIndex ri = new RandIndex();

		double sum = 0;
		for (int i = loops; i-- > 0;)
		{
			MathArrays.shuffle(c1, rg);
			sum += ri.getAdjustedRandIndex(c1, n1, c2, n2);
		}

		sum /= loops;
		TestLog.info("[%d,%d,%d,%d] %f\n", n, n1, n2, loops, sum);

		final double delta = 0.1;
		Assert.assertTrue(sum < delta && sum > -delta);
	}

	@Test
	public void canComputeAdjustedRandIndexWithSimpleData()
	{
		final int size = 100;
		for (final int n1 : new int[] { 1, 2, 3, 4, 5 })
			for (final int n2 : new int[] { 1, 2, 3, 4, 5 })
				canComputeAdjustedRandIndexWithData(size, n1, n2);
	}

	// Speed test on large data
	@Test
	public void canComputeAdjustedRandIndexWithBigData()
	{
		final int size = 10000;
		for (final int i : new int[] { 3, 5, 10 })
		{
			final int n1 = size / i;
			final int n2 = size / i;
			canComputeAdjustedRandIndexWithData(size, n1, n2);
		}
		for (final int i : new int[] { 3, 5, 10 })
		{
			final int n1 = size / i;
			final int n2 = i;
			canComputeAdjustedRandIndexWithData(size, n1, n2);
		}
		for (final int i : new int[] { 3, 5, 10 })
		{
			final int n1 = i;
			final int n2 = i;
			canComputeAdjustedRandIndexWithData(size, n1, n2);
		}
	}

	private static void canComputeAdjustedRandIndexWithData(int size, int n1, int n2)
	{
		final int n = size;
		final int[] c1 = new int[size];
		final int[] c2 = new int[size];
		while (size-- > 0)
		{
			c1[size] = size % n1;
			c2[size] = size % n2;
		}
		final RandomGenerator rand = TestSettings.getRandomGenerator();
		MathArrays.shuffle(c1, rand);

		final RandIndex ri = new RandIndex();

		final long t1 = System.nanoTime();
		final double o1 = ri.getAdjustedRandIndex(c1, c2);
		final long t2 = System.nanoTime();
		final double o2 = ri.getAdjustedRandIndex(c1, n1, c2, n2);
		final long t3 = System.nanoTime();

		final double r = ri.getRandIndex();

		final long table1 = t2 - t1;
		final long table2 = t3 - t2;

		TestLog.info("[%d,%d,%d] table1=%d (%f [%f]), table2=%d (%f), %f\n", n, n1, n2, table1, o1, r, table2, o2,
				table1 / (double) table2);

		Assert.assertEquals(o2, o1, 0);
	}
}
