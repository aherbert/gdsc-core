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
import org.apache.commons.math3.util.MathArrays;
import org.junit.Assert;
import org.junit.Test;

import gdsc.test.BaseTimingTask;
import gdsc.test.TestSettings;
import gdsc.test.TimingService;
import gdsc.test.TestSettings.LogLevel;
import gdsc.test.TestSettings.TestComplexity;
import gnu.trove.set.hash.TIntHashSet;

@SuppressWarnings({ "javadoc" })
public class SimpleArrayUtilsTest
{
	TIntHashSet set = new TIntHashSet();

	@Test
	public void canFlatten()
	{
		final RandomGenerator r = TestSettings.getRandomGenerator();
		testFlatten(new int[0]);
		testFlatten(new int[10]);
		for (int i = 0; i < 10; i++)
		{
			testFlatten(next(r, 1, 10));
			testFlatten(next(r, 10, 10));
			testFlatten(next(r, 100, 10));
		}
	}

	private void testFlatten(int[] s1)
	{
		set.clear();
		set.addAll(s1);
		final int[] e = set.toArray();
		Arrays.sort(e);

		final int[] o = SimpleArrayUtils.flatten(s1);
		//TestSettings.debug("%s =? %s\n", Arrays.toString(e), Arrays.toString(o));
		Assert.assertArrayEquals(e, o);
	}

	@Test
	public void canSortMerge()
	{
		final RandomGenerator r = TestSettings.getRandomGenerator();
		testSortMerge(new int[0], new int[10]);
		testSortMerge(new int[10], new int[10]);
		testSortMerge(new int[10], next(r, 10, 10));
		for (int i = 0; i < 10; i++)
		{
			testSortMerge(next(r, 1, 10), next(r, 1, 10));
			testSortMerge(next(r, 10, 10), next(r, 1, 10));
			testSortMerge(next(r, 100, 10), next(r, 1, 10));
			testSortMerge(next(r, 1, 10), next(r, 10, 10));
			testSortMerge(next(r, 10, 10), next(r, 10, 10));
			testSortMerge(next(r, 100, 10), next(r, 10, 10));
			testSortMerge(next(r, 1, 10), next(r, 100, 10));
			testSortMerge(next(r, 10, 10), next(r, 100, 10));
			testSortMerge(next(r, 100, 10), next(r, 100, 10));
		}
	}

	@SuppressWarnings("deprecation")
	private void testSortMerge(int[] s1, int[] s2)
	{
		set.clear();
		set.addAll(s1);
		set.addAll(s2);
		final int[] e = set.toArray();
		Arrays.sort(e);

		final int[] o = SimpleArrayUtils.sortMerge(s1, s2);
		//TestSettings.debug("%s =? %s\n", Arrays.toString(e), Arrays.toString(o));
		Assert.assertArrayEquals(e, o);
	}

	private static int[] next(RandomGenerator r, int size, int max)
	{
		final int[] a = new int[size];
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

		@Override
		public int getSize()
		{
			return data.length;
		}

		@Override
		public Object getData(int i)
		{
			return new int[][] { data[i][0].clone(), data[i][1].clone() };
		}
	}

	@Test
	public void testMergeOnIndexData()
	{
		TestSettings.assume(LogLevel.INFO, TestComplexity.MEDIUM);
		final RandomGenerator r = TestSettings.getRandomGenerator();

		final double[] f = new double[] { 0.1, 0.5, 0.75 };
		for (final int size : new int[] { 100, 1000, 10000 })
			for (int i = 0; i < f.length; i++)
				for (int j = i; j < f.length; j++)
					testMergeOnIndexData(r, 100, size, (int) (size * f[i]), (int) (size * f[j]));
	}

	private void testMergeOnIndexData(RandomGenerator r, int length, final int size, final int n1, final int n2)
	{
		final int[][][] data = new int[length][2][];
		final int[] s1 = SimpleArrayUtils.newArray(size, 0, 1);
		for (int i = 0; i < length; i++)
		{
			Random.shuffle(s1, r);
			data[i][0] = Arrays.copyOf(s1, n1);
			Random.shuffle(s1, r);
			data[i][1] = Arrays.copyOf(s1, n2);
		}
		final String msg = String.format("[%d] %d vs %d", size, n1, n2);

		final TimingService ts = new TimingService();
		ts.execute(new MyTimingTask("SortMerge" + msg, data)
		{
			@Override
			@SuppressWarnings("deprecation")
			public Object run(Object data)
			{
				final int[][] d = (int[][]) data;
				return SimpleArrayUtils.sortMerge(d[0], d[1]);
			}
		});
		ts.execute(new MyTimingTask("merge+sort" + msg, data)
		{
			@Override
			public Object run(Object data)
			{
				final int[][] d = (int[][]) data;
				final int[] a = SimpleArrayUtils.merge(d[0], d[1]);
				Arrays.sort(a);
				return a;
			}
		});
		ts.execute(new MyTimingTask("merge+sort unique" + msg, data)
		{
			@Override
			public Object run(Object data)
			{
				final int[][] d = (int[][]) data;
				final int[] a = SimpleArrayUtils.merge(d[0], d[1], true);
				Arrays.sort(a);
				return a;
			}
		});

		ts.repeat(ts.getSize());
		ts.report();
	}

	@Test
	public void testMergeOnRedundantData()
	{
		TestSettings.assume(LogLevel.INFO, TestComplexity.MEDIUM);
		final RandomGenerator r = TestSettings.getRandomGenerator();

		final int[] n = new int[] { 2, 4, 8 };
		final int[] size = new int[] { 100, 1000, 10000 };

		for (int i = 0; i < n.length; i++)
			for (int j = i; j < n.length; j++)
				for (int ii = 0; ii < size.length; ii++)
					for (int jj = ii; jj < size.length; jj++)
						testMergeOnRedundantData(r, 50, size[ii], n[i], size[jj], n[j]);
	}

	public void testMergeOnRedundantData(RandomGenerator r, int length, final int n1, final int r1, final int n2,
			final int r2)
	{
		final int[][][] data = new int[length][2][];
		final int[] s1 = new int[n1];
		for (int i = 0; i < n1; i++)
			s1[i] = i % r1;
		final int[] s2 = new int[n2];
		for (int i = 0; i < n2; i++)
			s2[i] = i % r2;
		for (int i = 0; i < length; i++)
		{
			MathArrays.shuffle(s1, r);
			data[i][0] = s1.clone();
			MathArrays.shuffle(s2, r);
			data[i][1] = s2.clone();
		}
		final String msg = String.format("%d%%%d vs %d%%%d", n1, r1, n2, r2);

		final TimingService ts = new TimingService();
		ts.execute(new MyTimingTask("SortMerge" + msg, data)
		{
			@Override
			@SuppressWarnings("deprecation")
			public Object run(Object data)
			{
				final int[][] d = (int[][]) data;
				return SimpleArrayUtils.sortMerge(d[0], d[1]);
			}
		});
		ts.execute(new MyTimingTask("merge+sort" + msg, data)
		{
			@Override
			public Object run(Object data)
			{
				final int[][] d = (int[][]) data;
				final int[] a = SimpleArrayUtils.merge(d[0], d[1]);
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

	private static void testGetRanges(int[] in, int[] e)
	{
		final int[] o = SimpleArrayUtils.getRanges(in);
		//TestSettings.debug("%s =? %s\n", Arrays.toString(e), Arrays.toString(o));
		Assert.assertArrayEquals(e, o);
	}

	@Test
	public void canToString()
	{
		Assert.assertEquals("null", SimpleArrayUtils.toString(null));

		Assert.assertEquals("[0.5, 1.0]", SimpleArrayUtils.toString(new float[] { 0.5f, 1f }));
		Assert.assertEquals("[0.5, 1.0]", SimpleArrayUtils.toString(new double[] { 0.5, 1 }));

		Assert.assertEquals("[c, a]", SimpleArrayUtils.toString(new char[] { 'c', 'a' }));

		Assert.assertEquals("[true, false]", SimpleArrayUtils.toString(new boolean[] { true, false }));

		Assert.assertEquals("[2, 1]", SimpleArrayUtils.toString(new byte[] { 2, 1 }));
		Assert.assertEquals("[2, 1]", SimpleArrayUtils.toString(new short[] { 2, 1 }));
		Assert.assertEquals("[2, 1]", SimpleArrayUtils.toString(new int[] { 2, 1 }));
		Assert.assertEquals("[2, 1]", SimpleArrayUtils.toString(new long[] { 2, 1 }));

		// Check objects
		Assert.assertEquals("[2, 1]", SimpleArrayUtils.toString(new Object[] { 2, 1 }));
		Assert.assertEquals("[foo, bar]", SimpleArrayUtils.toString(new Object[] { "foo", "bar" }));
		Assert.assertEquals("[foo, 1]", SimpleArrayUtils.toString(new Object[] { "foo", 1 }));

		// Check recursion
		Assert.assertEquals("[[2, 1], [3, 4]]", SimpleArrayUtils.toString(new int[][] { { 2, 1 }, { 3, 4 } }));
	}
}
