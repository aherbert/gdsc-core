package gdsc.core.utils;

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.util.MathArrays;
import org.junit.Assert;
import org.junit.Test;

import gdsc.core.test.BaseTimingTask;
import gdsc.core.test.TimingService;
import gnu.trove.set.hash.TIntHashSet;

public class SimpleArrayUtilsTest
{
	RandomGenerator r = new Well19937c(30051977);
	TIntHashSet set = new TIntHashSet();

	@Test
	public void canFlatten()
	{
		testFlatten(new int[0]);
		testFlatten(new int[10]);
		for (int i = 0; i < 10; i++)
		{
			testFlatten(next(1, 10));
			testFlatten(next(10, 10));
			testFlatten(next(100, 10));
		}
	}

	private void testFlatten(int[] s1)
	{
		set.clear();
		set.addAll(s1);
		int[] e = set.toArray();
		Arrays.sort(e);

		int[] o = SimpleArrayUtils.flatten(s1);
		//System.out.printf("%s =? %s\n", Arrays.toString(e), Arrays.toString(o));
		Assert.assertArrayEquals(e, o);
	}

	@Test
	public void canSortMerge()
	{
		testSortMerge(new int[0], new int[10]);
		testSortMerge(new int[10], new int[10]);
		testSortMerge(new int[10], next(10, 10));
		for (int i = 0; i < 10; i++)
		{
			testSortMerge(next(1, 10), next(1, 10));
			testSortMerge(next(10, 10), next(1, 10));
			testSortMerge(next(100, 10), next(1, 10));
			testSortMerge(next(1, 10), next(10, 10));
			testSortMerge(next(10, 10), next(10, 10));
			testSortMerge(next(100, 10), next(10, 10));
			testSortMerge(next(1, 10), next(100, 10));
			testSortMerge(next(10, 10), next(100, 10));
			testSortMerge(next(100, 10), next(100, 10));
		}
	}

	@SuppressWarnings("deprecation")
	private void testSortMerge(int[] s1, int[] s2)
	{
		set.clear();
		set.addAll(s1);
		set.addAll(s2);
		int[] e = set.toArray();
		Arrays.sort(e);

		int[] o = SimpleArrayUtils.sortMerge(s1, s2);
		//System.out.printf("%s =? %s\n", Arrays.toString(e), Arrays.toString(o));
		Assert.assertArrayEquals(e, o);
	}

	private int[] next(int size, int max)
	{
		int[] a = new int[size];
		for (int i = 0; i < size; i++)
			a[i] = r.nextInt(max);
		return a;
	}

	private abstract class MyTimingTask extends BaseTimingTask
	{
		int[][][] data;

		public MyTimingTask(String name, int[][][] data)
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
			return new int[][] { data[i][0].clone(), data[i][1].clone() };
		}
	}

	@Test
	public void testOnIndexData()
	{
		double[] f = new double[] { 0.1, 0.5, 0.75 };
		for (int size : new int[] { 100, 1000, 10000 })
			for (int i = 0; i < f.length; i++)
				for (int j = i; j < f.length; j++)
				{
					testOnIndexData(100, size, (int) (size * f[i]), (int) (size * f[j]));
				}
	}

	public void testOnIndexData(int length, final int size, final int n1, final int n2)
	{
		int[][][] data = new int[length][2][];
		int[] s1 = SimpleArrayUtils.newArray(size, 0, 1);
		for (int i = 0; i < length; i++)
		{
			MathArrays.shuffle(s1, r);
			data[i][0] = Arrays.copyOf(s1, n1);
			MathArrays.shuffle(s1, r);
			data[i][1] = Arrays.copyOf(s1, n2);
		}
		String msg = String.format("[%d] %d vs %d", size, n1, n2);

		TimingService ts = new TimingService();
		ts.execute(new MyTimingTask("SortMerge" + msg, data)
		{
			@SuppressWarnings("deprecation")
			public Object run(Object data)
			{
				int[][] d = (int[][]) data;
				return SimpleArrayUtils.sortMerge(d[0], d[1]);
			}
		});
		ts.execute(new MyTimingTask("merge+sort" + msg, data)
		{
			public Object run(Object data)
			{
				int[][] d = (int[][]) data;
				int[] a = SimpleArrayUtils.merge(d[0], d[1]);
				Arrays.sort(a);
				return a;
			}
		});
		ts.execute(new MyTimingTask("merge+sort unique" + msg, data)
		{
			public Object run(Object data)
			{
				int[][] d = (int[][]) data;
				int[] a = SimpleArrayUtils.merge(d[0], d[1], true);
				Arrays.sort(a);
				return a;
			}
		});

		ts.repeat(ts.getSize());
		ts.report();
	}

	@Test
	public void testOnRedundantData()
	{
		int[] n = new int[] { 2, 4, 8 };
		int[] size = new int[] { 100, 1000, 10000 };

		for (int i = 0; i < n.length; i++)
			for (int j = i; j < n.length; j++)
				for (int ii = 0; ii < size.length; ii++)
					for (int jj = ii; jj < size.length; jj++)
						testOnRedundantData(50, size[ii], n[i], size[jj], n[j]);
	}

	public void testOnRedundantData(int length, final int n1, final int r1, final int n2, final int r2)
	{
		int[][][] data = new int[length][2][];
		int[] s1 = new int[n1];
		for (int i = 0; i < n1; i++)
			s1[i] = i % r1;
		int[] s2 = new int[n2];
		for (int i = 0; i < n2; i++)
			s2[i] = i % r2;
		for (int i = 0; i < length; i++)
		{
			MathArrays.shuffle(s1, r);
			data[i][0] = s1.clone();
			MathArrays.shuffle(s2, r);
			data[i][1] = s2.clone();
		}
		String msg = String.format("%d%%%d vs %d%%%d", n1, r1, n2, r2);

		TimingService ts = new TimingService();
		ts.execute(new MyTimingTask("SortMerge" + msg, data)
		{
			@SuppressWarnings("deprecation")
			public Object run(Object data)
			{
				int[][] d = (int[][]) data;
				return SimpleArrayUtils.sortMerge(d[0], d[1]);
			}
		});
		ts.execute(new MyTimingTask("merge+sort" + msg, data)
		{
			public Object run(Object data)
			{
				int[][] d = (int[][]) data;
				int[] a = SimpleArrayUtils.merge(d[0], d[1]);
				Arrays.sort(a);
				return a;
			}
		});

		ts.repeat(ts.getSize());
		ts.report();
	}

	@Test
	public void canGetRanges()
	{
		testGetRanges(null, new int[0]);
		testGetRanges(new int[0], new int[0]);
		testGetRanges(new int[] { 0 }, new int[] { 0, 0 });
		testGetRanges(new int[] { 1 }, new int[] { 1, 1 });
		testGetRanges(new int[] { 0, 1 }, new int[] { 0, 1 });
		testGetRanges(new int[] { 0, 1, 2, 3 }, new int[] { 0, 3 });
		testGetRanges(new int[] { 0, 1, 3, 4, 5, 7 }, new int[] { 0, 1, 3, 5, 7, 7 });
		testGetRanges(new int[] { 0, 3, 5, 7 }, new int[] { 0, 0, 3, 3, 5, 5, 7, 7 });
		testGetRanges(new int[] { -1, 0, 1 }, new int[] { -1, 1 });
		testGetRanges(new int[] { -2, -1, 1 }, new int[] { -2, -1, 1, 1 });

		// With duplicates
		testGetRanges(new int[] { 0 }, new int[] { 0, 0 });
		testGetRanges(new int[] { 1 }, new int[] { 1, 1 });
		testGetRanges(new int[] { 0, 1 }, new int[] { 0, 1 });
		testGetRanges(new int[] { 0, 1, 2, 3 }, new int[] { 0, 3 });
		testGetRanges(new int[] { 0, 1, 3, 4, 5, 7 }, new int[] { 0, 1, 3, 5, 7, 7 });
		testGetRanges(new int[] { 0, 3, 5, 7 }, new int[] { 0, 0, 3, 3, 5, 5, 7, 7 });
		testGetRanges(new int[] { -1, 0, 1 }, new int[] { -1, 1 });
		testGetRanges(new int[] { -2, -1, 1 }, new int[] { -2, -1, 1, 1 });
	}

	@Test
	public void canGetRangesWithDuplicates()
	{
		testGetRanges(new int[] { 0, 0, 0 }, new int[] { 0, 0 });
		testGetRanges(new int[] { 1, 1 }, new int[] { 1, 1 });
		testGetRanges(new int[] { 0, 1, 1 }, new int[] { 0, 1 });
		testGetRanges(new int[] { 0, 1, 2, 2, 2, 3, 3 }, new int[] { 0, 3 });
		testGetRanges(new int[] { 0, 1, 1, 3, 3, 4, 5, 7, 7 }, new int[] { 0, 1, 3, 5, 7, 7 });
		testGetRanges(new int[] { 0, 3, 5, 5, 5, 7 }, new int[] { 0, 0, 3, 3, 5, 5, 7, 7 });
		testGetRanges(new int[] { -1, 0, 0, 0, 1, 1 }, new int[] { -1, 1 });
		testGetRanges(new int[] { -2, -2, -1, 1 }, new int[] { -2, -1, 1, 1 });
	}

	private void testGetRanges(int[] in, int[] e)
	{
		int[] o = SimpleArrayUtils.getRanges(in);
		//System.out.printf("%s =? %s\n", Arrays.toString(e), Arrays.toString(o));
		Assert.assertArrayEquals(e, o);
	}
}
