package gdsc.core.utils;

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Assert;
import org.junit.Test;

import gdsc.core.test.BaseTimingTask;
import gdsc.core.test.TimingService;

public class PartialSortTest
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
	public void botttomNofMIsCorrect()
	{
		for (int n : new int[] { 1, 3, 5, 10 })
			for (int m : new int[] { 10, 20, 50 })
			{
				computeBottom(100, n, m);
			}
	}

	private static double[] bottom(int n, double[] d)
	{
		Arrays.sort(d);
		return Arrays.copyOf(d, n);
	}

	@Test
	public void bottomCanHandleNullData()
	{
		double[] o = PartialSort.bottom((double[]) null, 5);
		Assert.assertEquals(0, o.length);
	}

	@Test
	public void bottomCanHandleEmptyData()
	{
		double[] o = PartialSort.bottom(new double[0], 5);
		Assert.assertEquals(0, o.length);
	}

	@Test
	public void bottomCanHandleIncompleteData()
	{
		double[] d = { 1, 3, 2 };
		double[] e = { 1, 2, 3 };
		double[] o = PartialSort.bottom(d, 5);
		Assert.assertArrayEquals(e, o, 0);
	}

	@Test
	public void bottomCanHandleNaNData()
	{
		double[] d = { 1, 2, Double.NaN, 3 };
		double[] e = { 1, 2, 3 };
		double[] o = PartialSort.bottom(d, 5);
		Assert.assertArrayEquals(e, o, 0);
	}

	public void computeBottom(int length, final int n, final int m)
	{
		double[][] data = createData(length, m);
		String msg = String.format(" %d of %d", n, m);

		final MyTimingTask expected = new MyTimingTask("Sort" + msg, data)
		{
			public Object run(Object data)
			{
				return bottom(n, (double[]) data);
			}
		};

		//@formatter:off
		TimingService ts = new TimingService();
		ts.execute(expected);
		ts.execute(new MyTimingTask("bottomSort" + msg, data)
		{
			public Object run(Object data) { return PartialSort.bottom((double[]) data, n); }
			public void check(int i, Object result)
			{
				double[] e = (double[])expected.run(expected.getData(i));
				double[] o = (double[])result;
				Assert.assertArrayEquals(e, o, 0);
			}
		});
		ts.execute(new MyTimingTask("bottomHead" + msg, data)
		{
			public Object run(Object data) { return PartialSort.bottom(PartialSort.OPTION_HEAD_FIRST, (double[]) data, n); }
			public void check(int i, Object result)
			{
				double[] e = (double[])expected.run(expected.getData(i));
				double[] o = (double[])result;
				Assert.assertEquals(e[n-1], o[0], 0);
			}
		});
		ts.execute(new MyTimingTask("bottom" + msg, data)
		{
			public Object run(Object data) { return PartialSort.bottom(0, (double[]) data, n); }
			public void check(int i, Object result)
			{
				double[] e = (double[])expected.run(expected.getData(i));
				double[] o = (double[])result;
				Arrays.sort(o);
				Assert.assertArrayEquals(e, o, 0);
			}
		});
		final PartialSort.DoubleSelector ps = new PartialSort.DoubleSelector(n);
		ts.execute(new MyTimingTask("bottomInstance" + msg, data)
		{
			public Object run(Object data) { return ps.bottom(0, (double[]) data); }
			public void check(int i, Object result)
			{
				double[] e = (double[])expected.run(expected.getData(i));
				double[] o = (double[])result;
				Arrays.sort(o);
				Assert.assertArrayEquals(e, o, 0);
			}
		});

		//@formatter:on

		// Sometimes this fails
		if ((double) n / m > 0.5)
			Assert.assertTrue(String.format("%f vs %f" + msg, ts.get(0).getMean(), ts.get(1).getMean()),
					ts.get(0).getMean() > ts.get(1).getMean() * 0.5);
		ts.check();

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
