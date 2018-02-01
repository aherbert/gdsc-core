package gdsc.core.utils;

import org.junit.Assume;
import org.junit.Test;

import gdsc.core.test.BaseTimingTask;
import gdsc.core.test.TimingService;

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

		public int getSize()
		{
			return 1;
		}

		public Object getData(int i)
		{
			return null;
		}

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

		float convert(int i)
		{
			return i;
		}

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

		float convert(int i)
		{
			return Float.intBitsToFloat(i);
		}

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
		int maxn = n[n.length-1];

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

		public int getSize()
		{
			return 1;
		}

		public Object getData(int i)
		{
			return null;
		}

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

		double convert(int i)
		{
			return i;
		}

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

		double convert(int i)
		{
			return Double.longBitsToDouble(i);
		}

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
		int maxn = n[n.length-1];

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
