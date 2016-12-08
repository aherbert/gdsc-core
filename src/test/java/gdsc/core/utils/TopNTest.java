package gdsc.core.utils;

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Assert;
import org.junit.Test;

import gdsc.core.test.BaseTimingTask;
import gdsc.core.test.TimingService;

public class TopNTest
{
	RandomGenerator r = new Well19937c(30051977);

	private abstract class MyTimingTask extends BaseTimingTask
	{
		double[][] data;

		public MyTimingTask(String name, double[][] data)
		{
			super(name);
			this.data = data;
		}

		public int getSize()
		{
			return data.length;
		}

		public Object getData(int i)
		{
			return data[i].clone();
		}
	}

	@Test
	public void topNofMIsCorrect()
	{
		for (int n : new int[] { 1, 3, 5, 10 })
			for (int m : new int[] { 10, 20, 50 })
			{
				checkTopNofMIsCorrect(n, m);
			}
	}

	private void checkTopNofMIsCorrect(int n, int m)
	{
		TopN topN = new TopN(n);
		int length = 10;
		double[][] data = createData(length, m);
		for (int i = 0; i < length; i++)
		{
			double[] e = sort(n, data[i].clone());
			double[] o = topN.pick(data[i].clone());
			Arrays.sort(o);
			Assert.assertArrayEquals(o, e, 0);
			o = topN.safePick(data[i], true);
			Assert.assertArrayEquals(o, e, 0);
		}
	}

	private double[] sort(int n, double[] d)
	{
		Arrays.sort(d);
		return Arrays.copyOf(d, n);
	}

	@Test
	public void safePickCanHandleNullData()
	{
		TopN topN = new TopN(5);
		double[] o = topN.safePick(null, false);
		Assert.assertEquals(0, o.length);
	}

	@Test
	public void safePickCanHandleEmptyData()
	{
		TopN topN = new TopN(5);
		double[] o = topN.safePick(new double[0], false);
		Assert.assertEquals(0, o.length);
	}
	
	@Test
	public void safePickCanHandleIncompleteData()
	{
		TopN topN = new TopN(5);
		double[] e = { 1,2,3 };
		double[] o = topN.safePick(e, false);
		Assert.assertArrayEquals(o, e, 0);
	}
	
	@Test
	public void top1of5IsFaster()
	{
		computeSortSpeed(100, 1, 5);
	}

	@Test
	public void top5of10IsFaster()
	{
		computeSortSpeed(100, 5, 10);
	}

	@Test
	public void top5of20IsFaster()
	{
		computeSortSpeed(100, 5, 20);
	}

	@Test
	public void top10of20IsFaster()
	{
		computeSortSpeed(100, 10, 20);
	}

	@Test
	public void top10of50IsFaster()
	{
		computeSortSpeed(100, 10, 50);
	}

	@Test
	public void top10of100IsFaster()
	{
		computeSortSpeed(100, 10, 100);
	}

	public void computeSortSpeed(int length, final int n, final int m)
	{
		double[][] data = createData(length, m);
		String msg = String.format(" %d of %d", n, m);

		//@formatter:off
		TimingService ts = new TimingService();
		ts.execute(new MyTimingTask("Sort" + msg, data)
		{
			public Object run(Object data) { Arrays.sort((double[]) data); return data; }
		});
		ts.execute(new MyTimingTask("TopN" + msg, data)
		{
			final TopN topN = new TopN(n);
			public Object run(Object data) { return topN.pick((double[]) data, m); }
		});

		//@formatter:on

		Assert.assertTrue(ts.get(0).getMin() > ts.get(1).getMin());

		ts.report();
	}

	private double[][] createData(int size, int m)
	{
		double[][] data = new double[size][];
		for (int i = 0; i < size; i++)
		{
			double[] d = new double[m];
			for (int j = 0; j < m; j++)
				d[j] = r.nextDouble() * 4 * Math.PI;
			data[i] = d;
		}
		return data;
	}
}
