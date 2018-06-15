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
import org.junit.Assert;
import org.junit.Test;

import gdsc.test.BaseTimingTask;
import gdsc.test.TestSettings;
import gdsc.test.TimingService;

public class PartialSortTest
{
	RandomGenerator r = TestSettings.getRandomGenerator();

	private abstract class MyTimingTask extends BaseTimingTask
	{
		double[][] data;

		public MyTimingTask(String name, double[][] data)
		{
			super(name);
			this.data = data;
		}

		@Override
		public int getSize()
		{
			return data.length;
		}

		@Override
		public Object getData(int i)
		{
			return data[i].clone();
		}
	}

	int[] testN = new int[] { 2, 3, 5, 10, 20, 30, 40, 50 };
	int[] testM = new int[] { 50, 100, 500 };

	@Test
	public void bottomNofMIsCorrect()
	{
		for (int n : testN)
			for (int m : testM)
			{
				bottomCompute(100, n, m);
			}
	}

	private static double[] bottom(int n, double[] d)
	{
		bottomSort(d);
		return Arrays.copyOf(d, n);
	}

	private static void bottomSort(double[] d)
	{
		Arrays.sort(d);
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

	public void bottomCompute(int length, final int n, final int m)
	{
		double[][] data = createData(length, m);
		String msg = String.format(" %d of %d", n, m);

		final MyTimingTask expected = new MyTimingTask("Sort" + msg, data)
		{
			@Override
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
			@Override
			public Object run(Object data) { return PartialSort.bottom((double[]) data, n); }
			@Override
			public void check(int i, Object result)
			{
				double[] e = (double[])expected.run(expected.getData(i));
				double[] o = (double[])result;
				Assert.assertArrayEquals(e, o, 0);
			}
		});
		ts.execute(new MyTimingTask("bottomHead" + msg, data)
		{
			@Override
			public Object run(Object data) { return PartialSort.bottom(PartialSort.OPTION_HEAD_FIRST, (double[]) data, n); }
			@Override
			public void check(int i, Object result)
			{
				double[] e = (double[])expected.run(expected.getData(i));
				double[] o = (double[])result;
				Assert.assertEquals(e[n-1], o[0], 0);
			}
		});
		ts.execute(new MyTimingTask("bottom" + msg, data)
		{
			@Override
			public Object run(Object data) { return PartialSort.bottom(0, (double[]) data, n); }
			@Override
			public void check(int i, Object result)
			{
				double[] e = (double[])expected.run(expected.getData(i));
				double[] o = (double[])result;
				bottomSort(o);
				Assert.assertArrayEquals(e, o, 0);
			}
		});
		final PartialSort.DoubleSelector ps = new PartialSort.DoubleSelector(n);
		ts.execute(new MyTimingTask("DoubleSelector" + msg, data)
		{
			@Override
			public Object run(Object data) { return ps.bottom(0, (double[]) data); }
			@Override
			public void check(int i, Object result)
			{
				double[] e = (double[])expected.run(expected.getData(i));
				double[] o = (double[])result;
				bottomSort(o);
				Assert.assertArrayEquals(e, o, 0);
			}
		});
		
		final PartialSort.DoubleHeap heap = new PartialSort.DoubleHeap(n);
		ts.execute(new MyTimingTask("DoubleHeap" + msg, data)
		{
			@Override
			public Object run(Object data) { return heap.bottom(0, (double[]) data); }
			@Override
			public void check(int i, Object result)
			{
				double[] e = (double[])expected.run(expected.getData(i));
				double[] o = (double[])result;
				bottomSort(o);
				Assert.assertArrayEquals(e, o, 0);
			}
		});
		ts.execute(new MyTimingTask("select" + msg, data)
		{
			@Override
			public Object run(Object data) { 
				double[] arr = (double[]) data;
				PartialSort.select(n-1, arr.length, arr);
				return Arrays.copyOf(arr, n);
			}
			@Override
			public void check(int i, Object result)
			{
				double[] e = (double[])expected.run(expected.getData(i));
				double[] o = (double[])result;
				bottomSort(o);
				Assert.assertArrayEquals(e, o, 0);
			}
		});

		//@formatter:on

		// Sometimes this fails
		//		if ((double) n / m > 0.5)
		//			Assert.assertTrue(String.format("%f vs %f" + msg, ts.get(0).getMean(), ts.get(1).getMean()),
		//					ts.get(0).getMean() > ts.get(1).getMean() * 0.5);

		ts.check();

		ts.report();
	}

	@Test
	public void topNofMIsCorrect()
	{
		for (int n : testN)
			for (int m : testM)
			{
				topCompute(100, n, m);
			}
	}

	private static double[] top(int n, double[] d)
	{
		topSort(d);
		return Arrays.copyOf(d, n);
	}

	private static void topSort(double[] d)
	{
		Arrays.sort(d);
		SimpleArrayUtils.reverse(d);
	}

	@Test
	public void topCanHandleNullData()
	{
		double[] o = PartialSort.top((double[]) null, 5);
		Assert.assertEquals(0, o.length);
	}

	@Test
	public void topCanHandleEmptyData()
	{
		double[] o = PartialSort.top(new double[0], 5);
		Assert.assertEquals(0, o.length);
	}

	@Test
	public void topCanHandleIncompleteData()
	{
		double[] d = { 1, 3, 2 };
		double[] e = { 3, 2, 1 };
		double[] o = PartialSort.top(d, 5);
		Assert.assertArrayEquals(e, o, 0);
	}

	@Test
	public void topCanHandleNaNData()
	{
		double[] d = { 1, 2, Double.NaN, 3 };
		double[] e = { 3, 2, 1 };
		double[] o = PartialSort.top(d, 5);
		Assert.assertArrayEquals(e, o, 0);
	}

	public void topCompute(int length, final int n, final int m)
	{
		double[][] data = createData(length, m);
		String msg = String.format(" %d of %d", n, m);

		final MyTimingTask expected = new MyTimingTask("Sort" + msg, data)
		{
			@Override
			public Object run(Object data)
			{
				return top(n, (double[]) data);
			}
		};

		//@formatter:off
		TimingService ts = new TimingService();
		ts.execute(expected);
		ts.execute(new MyTimingTask("topSort" + msg, data)
		{
			@Override
			public Object run(Object data) { return PartialSort.top((double[]) data, n); }
			@Override
			public void check(int i, Object result)
			{
				double[] e = (double[])expected.run(expected.getData(i));
				double[] o = (double[])result;
				Assert.assertArrayEquals(e, o, 0);
			}
		});
		ts.execute(new MyTimingTask("topHead" + msg, data)
		{
			@Override
			public Object run(Object data) { return PartialSort.top(PartialSort.OPTION_HEAD_FIRST, (double[]) data, n); }
			@Override
			public void check(int i, Object result)
			{
				double[] e = (double[])expected.run(expected.getData(i));
				double[] o = (double[])result;
				Assert.assertEquals(e[n-1], o[0], 0);
			}
		});
		ts.execute(new MyTimingTask("top" + msg, data)
		{
			@Override
			public Object run(Object data) { return PartialSort.top(0, (double[]) data, n); }
			@Override
			public void check(int i, Object result)
			{
				double[] e = (double[])expected.run(expected.getData(i));
				double[] o = (double[])result;
				topSort(o);
				Assert.assertArrayEquals(e, o, 0);
			}
		});
		final PartialSort.DoubleSelector ps = new PartialSort.DoubleSelector(n);
		ts.execute(new MyTimingTask("DoubleSelector" + msg, data)
		{
			@Override
			public Object run(Object data) { return ps.top(0, (double[]) data); }
			@Override
			public void check(int i, Object result)
			{
				double[] e = (double[])expected.run(expected.getData(i));
				double[] o = (double[])result;
				topSort(o);
				Assert.assertArrayEquals(e, o, 0);
			}
		});
		
		final PartialSort.DoubleHeap heap = new PartialSort.DoubleHeap(n);
		ts.execute(new MyTimingTask("DoubleHeap" + msg, data)
		{
			@Override
			public Object run(Object data) { return heap.top(0, (double[]) data); }
			@Override
			public void check(int i, Object result)
			{
				double[] e = (double[])expected.run(expected.getData(i));
				double[] o = (double[])result;
				topSort(o);
				Assert.assertArrayEquals(e, o, 0);
			}
		});

		//@formatter:on

		//		// Sometimes this fails
		//		if ((double) n / m > 0.5)
		//			Assert.assertTrue(String.format("%f vs %f" + msg, ts.get(0).getMean(), ts.get(1).getMean()),
		//					ts.get(0).getMean() > ts.get(1).getMean() * 0.5);

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
