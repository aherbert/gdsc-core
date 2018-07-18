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
package gdsc.core.math.interpolation;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import gdsc.core.utils.Sort;
import gdsc.test.BaseTimingTask;
import gdsc.test.LogLevel;
import gdsc.test.TestComplexity;
import gdsc.test.TestLog;
import gdsc.test.TestSettings;
import gdsc.test.TimingService;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;

/**
 * This class is used to in-line the computation for the CustomTricubicInterpolatingFunction
 */
@SuppressWarnings({ "javadoc" })
public class CustomTricubicInterpolatingFunctionTest
{
	static String inlineComputeCoefficients()
	{
		final StringBuilder sb = new StringBuilder();

		final int sz = 64;

		sb.append(String.format("final double[] a = new double[%d];\n", sz));

		for (int i = 0; i < sz; i++)
		{
			sb.append(String.format("a[%d]=", i));

			final double[] row = CustomTricubicInterpolatingFunction.AINV[i];
			for (int j = 0; j < sz; j++)
			{
				final double d = row[j];
				if (d != 0)
				{
					if (d > 0)
						sb.append('+');
					final int di = (int) Math.floor(d);
					if (di == d)
						sb.append(String.format("%d*beta[%d]", di, j));
					else
						sb.append(String.format("%f*beta[%d]", d, j));
				}
			}
			sb.append(String.format(";\n", i));
		}
		sb.append("return a;\n");

		return finialise(sb);
	}

	static String inlineComputeCoefficientsCollectTerms()
	{
		final StringBuilder sb = new StringBuilder();

		final int sz = 64;

		// Require integer coefficients
		int max = 0;
		for (int i = 0; i < sz; i++)
		{
			final double[] row = CustomTricubicInterpolatingFunction.AINV[i];
			for (int j = 0; j < sz; j++)
			{
				final double d = row[j];
				if (d != 0)
				{
					final int di = (int) Math.floor(d);
					if (di != d)
						return null;
					if (max < Math.abs(di))
						max = Math.abs(di);
				}
			}
		}

		final TIntObjectHashMap<TIntArrayList> map = new TIntObjectHashMap<>(max + 1);

		sb.append(String.format("final double[] a = new double[%d];\n", sz));

		for (int i = 0; i < sz; i++)
		{
			map.clear();
			final double[] row = CustomTricubicInterpolatingFunction.AINV[i];
			for (int j = 0; j < sz; j++)
			{
				final double d = row[j];
				if (d != 0)
				{
					final int di = (int) Math.floor(d);
					final int key = Math.abs(di);
					// Check if contains either positive or negative key
					TIntArrayList value = map.get(key);
					if (value == null)
					{
						value = new TIntArrayList();
						map.put(key, value);
					}
					// Store the index and the sign.
					// We use 1-based index so we can store -0
					value.add(((di < 0) ? -1 : 1) * (j + 1));
				}
			}

			sb.append(String.format("a[%d]=", i));

			// Collect terms
			map.forEachEntry(new TIntObjectProcedure<TIntArrayList>()
			{
				@Override
				public boolean execute(int key, TIntArrayList value)
				{
					final int[] js = value.toArray(); // Signed j
					final int[] j = js.clone(); // Unsigned j
					for (int i = 0; i < j.length; i++)
						j[i] = Math.abs(j[i]);

					Sort.sortArrays(js, j, true);

					// Check if starting with negative
					char add = '+';
					char sub = '-';

					if (js[0] < 0)
					{
						// Subtract the set
						sb.append('-');
						if (key > 1)
							sb.append(key).append('*');
						// Swap signs
						add = sub;
						sub = '+';
					}
					else
					{
						// Some positive so add the set
						sb.append('+');
						if (key > 1)
							sb.append(key).append('*');
					}

					if (js.length != 1)
						sb.append('(');
					for (int i = 0; i < js.length; i++)
					{
						if (i != 0)
							if (js[i] < 0)
								sb.append(sub);
							else
								sb.append(add);
						// Convert 1-based index back to 0-based
						sb.append("beta[").append(Math.abs(js[i]) - 1).append(']');
					}
					if (js.length != 1)
						sb.append(')');
					return true;
				}
			});

			sb.append(String.format(";\n", i));
		}
		sb.append("return a;\n");

		return finialise(sb);
	}

	private static String finialise(final StringBuilder sb)
	{
		String result = sb.toString();
		result = result.replaceAll("\\+1\\*", "+");
		result = result.replaceAll("-1\\*", "-");
		result = result.replaceAll("=\\+", "=");
		result = result.replaceAll("=\\-", "=-");
		return result;
	}

	private final LogLevel level = LogLevel.DEBUG;

	@Test
	public void canConstructInlineComputeCoefficients()
	{
		Assume.assumeTrue(TestSettings.allow(level));
		TestLog.log(level, inlineComputeCoefficients());
	}

	@Test
	public void canConstructInlineComputeCoefficientsCollectTerms()
	{
		Assume.assumeTrue(TestSettings.allow(level));
		TestLog.log(level, inlineComputeCoefficientsCollectTerms());
	}

	private abstract class MyTimingTask extends BaseTimingTask
	{
		double[][] a;

		public MyTimingTask(String name, double[][] a)
		{
			super(name);
			this.a = a;
		}

		@Override
		public int getSize()
		{
			return 1;
		}

		@Override
		public Object getData(int i)
		{
			return null;
		}

		@Override
		public void check(int i, Object result)
		{
			final double[][] b = (double[][]) result;
			for (int j = 0; j < a.length; j++)
				for (int k = 0; k < a[j].length; k++)
					Assert.assertEquals(getName(), a[j][k], b[j][k], Math.abs(a[j][k]) * 1e-6);
		}
	}

	@Test
	public void inlineComputeCoefficientsIsFaster()
	{
		TestSettings.assume(TestComplexity.MEDIUM);

		final RandomGenerator r = TestSettings.getRandomGenerator();

		final int N = 3000;
		final double[][] tables = new double[N][];
		final double[][] a = new double[N][];
		for (int i = 0; i < tables.length; i++)
		{
			final double[] table = new double[64];
			for (int j = 0; j < 64; j++)
				table[j] = r.nextDouble();
			tables[i] = table;
			a[i] = CustomTricubicInterpolatingFunction.computeCoefficients(table);
		}

		final TimingService ts = new TimingService();

		ts.execute(new MyTimingTask("Standard", a)
		{
			@Override
			public Object run(Object data)
			{
				final double[][] a = new double[N][];
				for (int i = 0; i < N; i++)
					a[i] = CustomTricubicInterpolatingFunction.computeCoefficients(tables[i]);
				return a;
			}
		});
		ts.execute(new MyTimingTask("Inline", a)
		{
			@Override
			public Object run(Object data)
			{
				final double[][] a = new double[N][];
				for (int i = 0; i < N; i++)
					a[i] = CustomTricubicInterpolatingFunction.computeCoefficientsInline(tables[i]);
				return a;
			}
		});
		ts.execute(new MyTimingTask("InlineCollectTerms", a)
		{
			@Override
			public Object run(Object data)
			{
				final double[][] a = new double[N][];
				for (int i = 0; i < N; i++)
					a[i] = CustomTricubicInterpolatingFunction.computeCoefficientsInlineCollectTerms(tables[i]);
				return a;
			}
		});

		final int n = ts.getSize();
		ts.check();
		ts.repeat();
		if (TestSettings.allow(LogLevel.INFO))
			//ts.report();
			ts.report(n);

		Assert.assertTrue(String.format("%f vs %f", ts.get(-1).getMean(), ts.get(-n).getMean()),
				ts.get(-1).getMean() < ts.get(-n).getMean());
	}
}
