package gdsc.core.match;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.util.MathArrays;
import org.junit.Assert;
import org.junit.Test;

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

	@Test(expected=IllegalStateException.class)
	public void getRandIndexThrowsWhenNotComputed()
	{
		RandIndex ri = new RandIndex();
		ri.getRandIndex();
	}
	
	@Test(expected=IllegalStateException.class)
	public void getAdjustedRandIndexThrowsWhenNotComputed()
	{
		RandIndex ri = new RandIndex();
		ri.getAdjustedRandIndex();
	}
	
	@Test
	public void canComputeRandIndexWithSimpleData()
	{
		int size = 100;
		for (int n1 : new int[] { 1, 2, 3, 4, 5 })
			for (int n2 : new int[] { 1, 2, 3, 4, 5 })
				canComputeRandIndexWithData(size, n1, n2);
	}

	// Speed test on large data
	@Test
	public void canComputeRandIndexWithBigData()
	{
		int size = 10000;
		for (int i : new int[] { 3, 5, 10 })
		{
			int n1 = size / i;
			int n2 = size / i;
			canComputeRandIndexWithData(size, n1, n2);
		}
		for (int i : new int[] { 3, 5, 10 })
		{
			int n1 = size / i;
			int n2 = i;
			canComputeRandIndexWithData(size, n1, n2);
		}
		for (int i : new int[] { 3, 5, 10 })
		{
			int n1 = i;
			int n2 = i;
			canComputeRandIndexWithData(size, n1, n2);
		}
	}

	private void canComputeRandIndexWithData(int size, int n1, int n2)
	{
		int n = size;
		int[] c1 = new int[size];
		int[] c2 = new int[size];
		while (size-- > 0)
		{
			c1[size] = size % n1;
			c2[size] = size % n2;
		}
		RandomGenerator rand = new Well19937c(30051977);
		MathArrays.shuffle(c1, rand);

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

		System.out.printf("[%d,%d,%d] simple=%d (%f), table1=%d (%f), %f\n", n, n1, n2, simple, e, table1, o1,
				simple / (double) table1);
		System.out.printf("[%d,%d,%d] simple=%d (%f), table2=%d (%f), %f\n", n, n1, n2, simple, e, table2, o2,
				simple / (double) table2);

		Assert.assertEquals(e, o1, e * 1e-10);
		Assert.assertEquals(o2, o1, 0);
	}

	@Test
	public void adjustedRandIndexIsZeroForRandomData()
	{
		int size = 100;
		for (int n1 : new int[] { 2, 5, 10 })
			for (int n2 : new int[] { 2, 5 })
				adjustedRandIndexIsZeroForRandomData(size, n1, n2, 10);
	}

	private void adjustedRandIndexIsZeroForRandomData(int size, int n1, int n2, int loops)
	{
		int n = size;
		int[] c1 = new int[size];
		int[] c2 = new int[size];
		while (size-- > 0)
		{
			c1[size] = size % n1;
			c2[size] = size % n2;
		}
		RandomGenerator rand = new Well19937c(30051977);
		RandIndex ri = new RandIndex();

		double sum = 0;
		for (int i = loops; i-- > 0;)
		{
			MathArrays.shuffle(c1, rand);
			sum += ri.getAdjustedRandIndex(c1, n1, c2, n2);
		}

		sum /= loops;
		System.out.printf("[%d,%d,%d,%d] %f\n", n, n1, n2, loops, sum);
		
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
		RandomGenerator rand = new Well19937c(30051977);
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

		System.out.printf("[%d,%d,%d] table1=%d (%f [%f]), table2=%d (%f), %f\n", n, n1, n2, table1, o1, r, table2, o2,
				table1 / (double) table2);

		Assert.assertEquals(o2, o1, 0);
	}
}
