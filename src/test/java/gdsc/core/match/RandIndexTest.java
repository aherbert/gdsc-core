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
package gdsc.core.match;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.MathArrays;
import org.junit.Assert;
import org.junit.Test;

import gdsc.test.TestSettings;

@SuppressWarnings({ "javadoc" })
public class RandIndexTest
{
	@Test
	public void canComputeSimpleRandIndexWithNoData()
	{
		for (int size : new int[] { 0, 1, 2 })
		{
			double e = (size == 0) ? 0 : 1;
			int[] clusters = new int[size];
			double r = RandIndex.simpleRandIndex(clusters, clusters);
			Assert.assertEquals(e, r, 0);
		}
	}

	@Test
	public void canComputeRandIndexWithNoData()
	{
		for (int size : new int[] { 0, 1, 2 })
		{
			double e = (size == 0) ? 0 : 1;
			int[] clusters = new int[size];
			double r = RandIndex.randIndex(clusters, clusters);
			Assert.assertEquals(e, r, 0);
		}
	}

	@Test
	public void canComputeRandIndex2WithNoData()
	{
		for (int size : new int[] { 0, 1, 2 })
		{
			double e = (size == 0) ? 0 : 1;
			int[] clusters = new int[size];
			double r = RandIndex.randIndex(clusters, 1, clusters, 1);
			Assert.assertEquals(e, r, 0);
		}
	}

	@Test
	public void canComputeAdjustedRandIndexWithNoData()
	{
		for (int size : new int[] { 0, 1, 2 })
		{
			double e = (size == 0) ? 0 : 1;
			int[] clusters = new int[size];
			double r = RandIndex.adjustedRandIndex(clusters, 1, clusters, 1);
			Assert.assertEquals(e, r, 0);
		}
	}

	@Test
	public void canComputeAdjustedRandIndexWhenNoRandomness()
	{
		// Q. should this be zero?
		double e = 1;

		int[] clusters = new int[2];
		clusters[1] = 1;
		double r = RandIndex.adjustedRandIndex(clusters, 2, clusters, 2);
		Assert.assertEquals(e, r, 0);
	}

	// The example data and answer are from:
	// http://stats.stackexchange.com/questions/89030/rand-index-calculation

	@Test
	public void canComputeSimpleRandIndex()
	{
		int[] clusters = { 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2 };
		int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0 };
		double r = RandIndex.simpleRandIndex(clusters, classes);
		Assert.assertEquals(0.67647058823529416, r, 1e-10);
	}

	@Test
	public void canComputeRandIndex()
	{
		int[] clusters = { 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2 };
		int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0 };
		double r = RandIndex.randIndex(clusters, classes);
		Assert.assertEquals(0.67647058823529416, r, 1e-10);
	}

	@Test
	public void canComputeRandIndexWithArbitraryClusterNumbers()
	{
		int[] clusters = { 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2 };
		int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0 };
		double r = RandIndex.simpleRandIndex(clusters, classes);

		//@formatter:off
		int[][] maps = new int[][] {
			{ 0,1,2 },
			{ 0,2,1 },
			{ 1,0,2 },
			{ 1,2,0 },
			{ 2,0,1 },
			{ 2,1,0 },
		};
		//@formatter:on
		RandIndex ri = new RandIndex();
		for (int[] map : maps)
		{
			int[] c2 = new int[classes.length];
			for (int i = 0; i < c2.length; i++)
				c2[i] = map[classes[i]];
			Assert.assertEquals(r, ri.getRandIndex(clusters, 3, c2, 3), 0);
		}
	}

	@Test
	public void canComputeRandIndex2()
	{
		int[] clusters = { 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2 };
		int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0 };
		double r = RandIndex.randIndex(clusters, 3, classes, 3);
		Assert.assertEquals(0.67647058823529416, r, 1e-10);
	}

	@Test
	public void canComputeSimpleRandIndexWithNegativeData()
	{
		int[] clusters = { 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, 2, 2, 2, 2, 2 };
		int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, -2, 1, 0, -2, -2, -2, 0 };
		double r = RandIndex.simpleRandIndex(clusters, classes);
		Assert.assertEquals(0.67647058823529416, r, 1e-10);
	}

	@Test
	public void canComputeRandIndexWithNegativeData()
	{
		int[] clusters = { 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, 2, 2, 2, 2, 2 };
		int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, -2, 1, 0, -2, -2, -2, 0 };
		double r = RandIndex.randIndex(clusters, classes);
		Assert.assertEquals(0.67647058823529416, r, 1e-10);
	}

	@Test
	public void canComputeRandIndex2WithNegativeData()
	{
		int[] clusters = { 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, 2, 2, 2, 2, 2 };
		int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, -2, 1, 0, -2, -2, -2, 0 };
		double r = RandIndex.randIndex(clusters, 3, classes, 3);
		Assert.assertEquals(0.67647058823529416, r, 1e-10);
	}

	@Test
	public void canComputeRandIndexWhenInvalidNClusters()
	{
		int[] clusters = { 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2 };
		int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0 };
		double r = RandIndex.randIndex(clusters, 2, classes, 3);
		Assert.assertEquals(0.67647058823529416, r, 1e-10);
	}

	@Test
	public void canComputeAdjustedRandIndexWhenInvalidNClusters()
	{
		int[] clusters = { 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2 };
		int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0 };
		double e = RandIndex.adjustedRandIndex(clusters, 3, classes, 3);
		double o = RandIndex.adjustedRandIndex(clusters, 2, classes, 3);
		Assert.assertEquals(e, o, 0);
	}

	@Test
	public void canComputeSimpleRandIndexWithSparseData()
	{
		int[] clusters = { 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6 };
		int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 9, 1, 0, 9, 9, 9, 0 };
		double r = RandIndex.simpleRandIndex(clusters, classes);
		Assert.assertEquals(0.67647058823529416, r, 1e-10);
	}

	@Test
	public void canComputeRandIndexWithSparseData()
	{
		int[] clusters = { 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6 };
		int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 9, 1, 0, 9, 9, 9, 0 };
		double r = RandIndex.randIndex(clusters, classes);
		Assert.assertEquals(0.67647058823529416, r, 1e-10);
	}

	@Test
	public void canComputeRandIndex2WithSparseData()
	{
		int[] clusters = { 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6 };
		int[] classes = { 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 9, 1, 0, 9, 9, 9, 0 };
		double r = RandIndex.randIndex(clusters, 7, classes, 10);
		Assert.assertEquals(0.67647058823529416, r, 1e-10);
	}

	@Test(expected = IllegalStateException.class)
	public void getRandIndexThrowsWhenNotComputed()
	{
		RandIndex ri = new RandIndex();
		ri.getRandIndex();
	}

	@Test(expected = IllegalStateException.class)
	public void getAdjustedRandIndexThrowsWhenNotComputed()
	{
		RandIndex ri = new RandIndex();
		ri.getAdjustedRandIndex();
	}

	@Test
	public void canComputeRandIndexWithSimpleData()
	{
		RandomGenerator rg = TestSettings.getRandomGenerator();
		int size = 100;
		for (int n1 : new int[] { 1, 2, 3, 4, 5 })
			for (int n2 : new int[] { 1, 2, 3, 4, 5 })
				canComputeRandIndexWithData(rg, size, n1, n2);
	}

	@Test
	public void canComputeRandIndexWithBigData()
	{
		TestSettings.assumeLowComplexity();
		RandomGenerator rg = TestSettings.getRandomGenerator();
		int size = 10000;
		for (int i : new int[] { 3, 5, 10 })
		{
			int n1 = size / i;
			int n2 = size / i;
			canComputeRandIndexWithData(rg, size, n1, n2);
		}
		for (int i : new int[] { 3, 5, 10 })
		{
			int n1 = size / i;
			int n2 = i;
			canComputeRandIndexWithData(rg, size, n1, n2);
		}
		for (int i : new int[] { 3, 5, 10 })
		{
			int n1 = i;
			int n2 = i;
			canComputeRandIndexWithData(rg, size, n1, n2);
		}
	}

	private void canComputeRandIndexWithData(RandomGenerator rg, int size, int n1, int n2)
	{
		int n = size;
		int[] c1 = new int[size];
		int[] c2 = new int[size];
		while (size-- > 0)
		{
			c1[size] = size % n1;
			c2[size] = size % n2;
		}
		MathArrays.shuffle(c1, rg);

		long t1 = System.nanoTime();
		double e = RandIndex.simpleRandIndex(c1, c2);
		long t2 = System.nanoTime();
		double o1 = RandIndex.randIndex(c1, c2);
		long t3 = System.nanoTime();
		double o2 = RandIndex.randIndex(c1, n1, c2, n2);
		long t4 = System.nanoTime();

		long simple = t2 - t1;
		long table1 = t3 - t2;
		long table2 = t4 - t3;

		TestSettings.info("[%d,%d,%d] simple=%d (%f), table1=%d (%f), %f\n", n, n1, n2, simple, e, table1, o1,
				simple / (double) table1);
		TestSettings.info("[%d,%d,%d] simple=%d (%f), table2=%d (%f), %f\n", n, n1, n2, simple, e, table2, o2,
				simple / (double) table2);

		Assert.assertEquals(e, o1, e * 1e-10);
		Assert.assertEquals(o2, o1, 0);
	}

	@Test
	public void adjustedRandIndexIsZeroForRandomData()
	{
		RandomGenerator rg = TestSettings.getRandomGenerator();
		int size = 100;
		for (int n1 : new int[] { 2, 5, 10 })
			for (int n2 : new int[] { 2, 5 })
				adjustedRandIndexIsZeroForRandomData(rg, size, n1, n2, 10);
	}

	private void adjustedRandIndexIsZeroForRandomData(RandomGenerator rg, int size, int n1, int n2, int loops)
	{
		int n = size;
		int[] c1 = new int[size];
		int[] c2 = new int[size];
		while (size-- > 0)
		{
			c1[size] = size % n1;
			c2[size] = size % n2;
		}
		RandIndex ri = new RandIndex();

		double sum = 0;
		for (int i = loops; i-- > 0;)
		{
			MathArrays.shuffle(c1, rg);
			sum += ri.getAdjustedRandIndex(c1, n1, c2, n2);
		}

		sum /= loops;
		TestSettings.info("[%d,%d,%d,%d] %f\n", n, n1, n2, loops, sum);

		double delta = 0.1;
		Assert.assertTrue(sum < delta && sum > -delta);
	}

	@Test
	public void canComputeAdjustedRandIndexWithSimpleData()
	{
		int size = 100;
		for (int n1 : new int[] { 1, 2, 3, 4, 5 })
			for (int n2 : new int[] { 1, 2, 3, 4, 5 })
				canComputeAdjustedRandIndexWithData(size, n1, n2);
	}

	// Speed test on large data
	@Test
	public void canComputeAdjustedRandIndexWithBigData()
	{
		int size = 10000;
		for (int i : new int[] { 3, 5, 10 })
		{
			int n1 = size / i;
			int n2 = size / i;
			canComputeAdjustedRandIndexWithData(size, n1, n2);
		}
		for (int i : new int[] { 3, 5, 10 })
		{
			int n1 = size / i;
			int n2 = i;
			canComputeAdjustedRandIndexWithData(size, n1, n2);
		}
		for (int i : new int[] { 3, 5, 10 })
		{
			int n1 = i;
			int n2 = i;
			canComputeAdjustedRandIndexWithData(size, n1, n2);
		}
	}

	private void canComputeAdjustedRandIndexWithData(int size, int n1, int n2)
	{
		int n = size;
		int[] c1 = new int[size];
		int[] c2 = new int[size];
		while (size-- > 0)
		{
			c1[size] = size % n1;
			c2[size] = size % n2;
		}
		RandomGenerator rand = TestSettings.getRandomGenerator();
		MathArrays.shuffle(c1, rand);

		RandIndex ri = new RandIndex();

		long t1 = System.nanoTime();
		double o1 = ri.getAdjustedRandIndex(c1, c2);
		long t2 = System.nanoTime();
		double o2 = ri.getAdjustedRandIndex(c1, n1, c2, n2);
		long t3 = System.nanoTime();

		double r = ri.getRandIndex();

		long table1 = t2 - t1;
		long table2 = t3 - t2;

		TestSettings.info("[%d,%d,%d] table1=%d (%f [%f]), table2=%d (%f), %f\n", n, n1, n2, table1, o1, r, table2, o2,
				table1 / (double) table2);

		Assert.assertEquals(o2, o1, 0);
	}
}
