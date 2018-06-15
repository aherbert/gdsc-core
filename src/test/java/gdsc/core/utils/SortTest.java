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

import org.junit.Assume;
import org.junit.Test;

import gdsc.test.BaseTimingTask;
import gdsc.test.TimingService;

public class SortTest
{
	private abstract class FloatConversionTimingTask extends BaseTimingTask
	{
		final int n;

		public FloatConversionTimingTask(String name, int n)
		{
			super(name + " n=" + n);
			this.n = n;
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
		public Object run(Object data)
		{
			for (int i = n; i-- > 0;)
			{
				if (convertBack(convert(i)) != i)
					throw new RuntimeException();
			}
			return null;
		}

		abstract float convert(int i);

		abstract int convertBack(float f);
	}

	private class FloatCastConversion extends FloatConversionTimingTask
	{
		public FloatCastConversion(int n)
		{
			super("float cast", n);
		}

		@Override
		float convert(int i)
		{
			return i;
		}

		@Override
		int convertBack(float f)
		{
			return (int) f;
		}
	}

	private class FloatBitConversion extends FloatConversionTimingTask
	{
		public FloatBitConversion(int n)
		{
			super("float bit", n);
		}

		@Override
		float convert(int i)
		{
			return Float.intBitsToFloat(i);
		}

		@Override
		int convertBack(float f)
		{
			return Float.floatToRawIntBits(f);
		}
	}

	@Test
	public void canTestFloatBitConversionSpeed()
	{
		Assume.assumeTrue(false);

		// Q. Is it faster to use:
		// int i;
		// float f = i;
		// i = (int) f;
		// OR
		// float f = Float.intBitsToFloat(i)
		// i = Float.floatToRawIntBits(i);

		// Note that is the number of indices is above the max value that can be 
		// stored in a float mantissa then the casting case is invalid.

		int[] n = new int[] { 100, 10000, 1000000 };
		int maxn = n[n.length - 1];

		for (int i = 0; i < n.length; i++)
		{
			TimingService ts = new TimingService(10 * maxn / n[i]);
			ts.execute(new FloatCastConversion(n[i]));
			ts.execute(new FloatBitConversion(n[i]));

			int size = ts.getSize();
			ts.repeat(size);
			ts.report(size);
		}
	}

	private abstract class DoubleConversionTimingTask extends BaseTimingTask
	{
		final int n;

		public DoubleConversionTimingTask(String name, int n)
		{
			super(name + " n=" + n);
			this.n = n;
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
		public Object run(Object data)
		{
			for (int i = n; i-- > 0;)
			{
				if (convertBack(convert(i)) != i)
					throw new RuntimeException();
			}
			return null;
		}

		abstract double convert(int i);

		abstract int convertBack(double f);
	}

	private class DoubleCastConversion extends DoubleConversionTimingTask
	{
		public DoubleCastConversion(int n)
		{
			super("double cast", n);
		}

		@Override
		double convert(int i)
		{
			return i;
		}

		@Override
		int convertBack(double f)
		{
			return (int) f;
		}
	}

	private class DoubleBitConversion extends DoubleConversionTimingTask
	{
		public DoubleBitConversion(int n)
		{
			super("double bit", n);
		}

		@Override
		double convert(int i)
		{
			return Double.longBitsToDouble(i);
		}

		@Override
		int convertBack(double f)
		{
			return (int) Double.doubleToRawLongBits(f);
		}
	}

	@Test
	public void canTestDoubleBitConversionSpeed()
	{
		Assume.assumeTrue(false);

		// Q. Is it faster to use:
		// int i;
		// double f = i;
		// i = (int) f;
		// OR
		// double f = Double.longBitsToDouble(i)
		// i = (int) Double.doubleToRawLongBits(i);

		// Note that is the number of indices is above the max value that can be 
		// stored in a float mantissa then the casting case is invalid.

		// 1 << 30 takes too long to run
		int[] n = new int[] { 100, 10000, 1000000, 1 << 25 };
		int maxn = n[n.length - 1];

		for (int i = 0; i < n.length; i++)
		{
			TimingService ts = new TimingService(maxn / n[i]);
			ts.execute(new DoubleCastConversion(n[i]));
			ts.execute(new DoubleBitConversion(n[i]));

			int size = ts.getSize();
			ts.repeat(size);
			ts.report(size);
		}
	}
}
