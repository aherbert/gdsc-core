package gdsc.core.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import gdsc.core.TestSettings;
import gdsc.core.test.BaseTimingTask;
import gdsc.core.test.TimingService;

@SuppressWarnings("deprecation")
public class EqualityTest
{
	int MAX_ITER = 2000000;

	@Test
	public void doubleRelativeErrorIsCorrectUntilULPSIsSmall()
	{
		int precision = new BigDecimal(Double.toString(Double.MAX_VALUE)).precision();
		//System.out.printf("Double max precision = %d\n", precision);
		for (int sig = 1; sig <= precision; sig++)
		{
			BigDecimal error = new BigDecimal("1e-" + sig);
			double e = error.doubleValue();
			double tolerance = e * 0.01;
			BigDecimal one_m_error = BigDecimal.ONE.subtract(error);
			BigDecimal one_one_m_error = BigDecimal.ONE.divide(one_m_error, sig * 10, RoundingMode.HALF_UP);

			//System.out.printf("Error = %s  %s  %s\n", error, one_m_error, one_one_m_error);
			int same = 0, total = 0;
			for (int leadingDigit = 1; leadingDigit <= 9; leadingDigit++)
			{
				for (int trailingDigit = 1; trailingDigit <= 9; trailingDigit++)
				{
					BigDecimal A = BigDecimal.valueOf(trailingDigit);
					A = A.scaleByPowerOfTen(-(sig - 1));
					BigDecimal toAdd = BigDecimal.valueOf(leadingDigit);
					A = A.add(toAdd);

					// Get number with a set relative error
					BigDecimal BLow = A.multiply(one_m_error);
					BigDecimal BHigh = A.multiply(one_one_m_error);

					//bd1 = bd1.round(new MathContext(sig, RoundingMode.HALF_DOWN));
					double d = A.doubleValue();
					double d1 = BLow.doubleValue();
					double d2 = BHigh.doubleValue();
					long ulps1 = Double.doubleToLongBits(d) - Double.doubleToLongBits(d1);
					long ulps2 = Double.doubleToLongBits(d2) - Double.doubleToLongBits(d);
					double rel1 = DoubleEquality.relativeError(d, d1);
					double rel2 = DoubleEquality.relativeError(d, d2);
					//System.out.printf("%d  %s < %s < %s = %d  %d  %g  %g\n", sig, BLow, A, BHigh, ulps1, ulps2, rel1,	rel2);
					if (ulps1 > 100)
					{
						Assert.assertEquals(e, rel1, tolerance);
						Assert.assertEquals(e, rel2, tolerance);
					}
					if (ulps1 == ulps2)
						same++;
					total++;
				}
			}
			Assert.assertTrue(same < total);
		}
	}

	@Test
	public void floatRelativeErrorIsCorrectUntilULPSIsSmall()
	{
		int precision = new BigDecimal(Float.toString(Float.MAX_VALUE)).precision();
		//System.out.printf("Float max precision = %d\n", precision);
		for (int sig = 1; sig <= precision; sig++)
		{
			BigDecimal error = new BigDecimal("1e-" + sig);
			double e = error.doubleValue();
			double tolerance = e * 0.01;
			BigDecimal one_m_error = BigDecimal.ONE.subtract(error);
			BigDecimal one_one_m_error = BigDecimal.ONE.divide(one_m_error, sig * 10, RoundingMode.HALF_UP);

			//System.out.printf("Error = %s  %s  %s\n", error, one_m_error, one_one_m_error);
			int same = 0, total = 0;
			for (int leadingDigit = 1; leadingDigit <= 9; leadingDigit++)
			{
				for (int trailingDigit = 1; trailingDigit <= 9; trailingDigit++)
				{
					BigDecimal A = BigDecimal.valueOf(trailingDigit);
					A = A.scaleByPowerOfTen(-(sig - 1));
					BigDecimal toAdd = BigDecimal.valueOf(leadingDigit);
					A = A.add(toAdd);

					// Get number with a set relative error
					BigDecimal BLow = A.multiply(one_m_error);
					BigDecimal BHigh = A.multiply(one_one_m_error);

					//bd1 = bd1.round(new MathContext(sig, RoundingMode.HALF_DOWN));
					float d = A.floatValue();
					float d1 = BLow.floatValue();
					float d2 = BHigh.floatValue();
					int ulps1 = Float.floatToIntBits(d) - Float.floatToIntBits(d1);
					int ulps2 = Float.floatToIntBits(d2) - Float.floatToIntBits(d);
					float rel1 = FloatEquality.relativeError(d, d1);
					float rel2 = FloatEquality.relativeError(d, d2);
					//System.out.printf("%d  %s < %s < %s = %d  %d  %g  %g\n", sig, BLow, A, BHigh, ulps1, ulps2, rel1,	rel2);
					if (ulps1 > 100)
					{
						Assert.assertEquals(e, rel1, tolerance);
						Assert.assertEquals(e, rel2, tolerance);
					}
					if (ulps1 == ulps2)
						same++;
					total++;
				}
			}
			Assert.assertTrue(same < total);
		}
	}

	@Test
	public void doubleCanComputeRelativeErrorFromSignificantDigits()
	{
		BigDecimal number = BigDecimal.valueOf(2).divide(BigDecimal.valueOf(9), 20, RoundingMode.HALF_UP);
		BigDecimal margin = BigDecimal.valueOf(0.05);
		// This does not work over the full range of siginifcant digits
		for (int s = 2; s <= DoubleEquality.getMaxSignificantDigits() - 2; s++)
		{
			BigDecimal A = number.round(new MathContext(s));
			BigDecimal error = new BigDecimal("1e-" + (s - 1));
			double e = error.doubleValue();
			BigDecimal one_m_error = BigDecimal.ONE.subtract(error);

			BigDecimal BLow = A.multiply(one_m_error);
			// Add margin for error
			BLow = BLow.add(A.subtract(BLow).multiply(margin));
			BigDecimal BLower = BLow.round(new MathContext(s, RoundingMode.DOWN));

			double a = A.doubleValue();
			double b = BLow.doubleValue();
			double max = DoubleEquality.getMaxRelativeError(s);
			Assert.assertEquals(e, max, e * 0.01);
			//double rel = DoubleEquality.relativeError(a, b);
			//System.out.printf("[%d] %s -> %s : %g  %g\n", s, A, BLow, max, rel);
			DoubleEquality eq = new DoubleEquality(s, 0);
			Assert.assertTrue(eq.almostEqualRelativeOrAbsolute(a, b));
			Assert.assertFalse(eq.almostEqualRelativeOrAbsolute(a, BLower.doubleValue()));
		}
	}

	@Test
	public void floatCanComputeRelativeErrorFromSignificantDigits()
	{
		BigDecimal number = BigDecimal.valueOf(2).divide(BigDecimal.valueOf(9), 20, RoundingMode.HALF_UP);
		BigDecimal margin = BigDecimal.valueOf(0.05);
		// This does not work over the full range of siginifcant digits
		for (int s = 2; s <= FloatEquality.getMaxSignificantDigits() - 2; s++)
		{
			BigDecimal A = number.round(new MathContext(s));
			BigDecimal error = new BigDecimal("1e-" + (s - 1));
			float e = error.floatValue();
			BigDecimal one_m_error = BigDecimal.ONE.subtract(error);

			BigDecimal BLow = A.multiply(one_m_error);
			// Add margin for error
			BLow = BLow.add(A.subtract(BLow).multiply(margin));
			BigDecimal BLower = BLow.round(new MathContext(s, RoundingMode.DOWN));

			float a = A.floatValue();
			float b = BLow.floatValue();
			float max = FloatEquality.getMaxRelativeError(s);
			Assert.assertEquals(e, max, e * 0.01);
			//float rel = FloatEquality.relativeError(a, b);
			//System.out.printf("[%d] %s -> %s : %g  %g\n", s, A, BLow, max, rel);
			FloatEquality eq = new FloatEquality(s, 0);
			Assert.assertTrue(eq.almostEqualRelativeOrAbsolute(a, b));
			Assert.assertFalse(eq.almostEqualRelativeOrAbsolute(a, BLower.floatValue()));
		}
	}

	@Test
	public void doubleCanComputeEquality()
	{
		double maxRelativeError = 1e-3f;
		double maxAbsoluteError = 1e-16f;
		DoubleEquality equality = new DoubleEquality(maxRelativeError, maxAbsoluteError);
		DoubleEquality equality2 = new DoubleEquality(4, maxAbsoluteError);

		for (int i = 0; i < 100; i++)
		{
			double f = (double) (i / 10000.0);
			double f2 = f * (1.00f + maxRelativeError - 1e-3f);
			double f3 = f * (1.0f + 2.0f * maxRelativeError);
			Assert.assertTrue("not equal " + f, equality.almostEqualRelativeOrAbsolute(f, f));
			Assert.assertTrue("not equal " + f, equality.almostEqualRelativeOrAbsolute(f, f2));
			if (i > 0)
				Assert.assertFalse("equal " + f, equality.almostEqualRelativeOrAbsolute(f, f3));

			Assert.assertTrue("not equal " + f, equality2.almostEqualRelativeOrAbsolute(f, f));
			Assert.assertTrue("not equal " + f, equality2.almostEqualRelativeOrAbsolute(f, f2));
			if (i > 0)
				Assert.assertFalse("equal " + f, equality2.almostEqualRelativeOrAbsolute(f, f3));
		}
	}

	@Test
	public void floatCanComputeEquality()
	{
		float maxRelativeError = 1e-3f;
		float maxAbsoluteError = 1e-16f;
		FloatEquality equality = new FloatEquality(maxRelativeError, maxAbsoluteError);
		FloatEquality equality2 = new FloatEquality(4, maxAbsoluteError);

		for (int i = 0; i < 100; i++)
		{
			float f = (float) (i / 10000.0);
			float f2 = f * (1.00f + maxRelativeError - 1e-3f);
			float f3 = f * (1.0f + 2.0f * maxRelativeError);
			Assert.assertTrue("not equal " + f, equality.almostEqualRelativeOrAbsolute(f, f));
			Assert.assertTrue("not equal " + f, equality.almostEqualRelativeOrAbsolute(f, f2));
			if (i > 0)
				Assert.assertFalse("equal " + f, equality.almostEqualRelativeOrAbsolute(f, f3));

			Assert.assertTrue("not equal " + f, equality2.almostEqualRelativeOrAbsolute(f, f));
			Assert.assertTrue("not equal " + f, equality2.almostEqualRelativeOrAbsolute(f, f2));
			if (i > 0)
				Assert.assertFalse("equal " + f, equality2.almostEqualRelativeOrAbsolute(f, f3));
		}
	}

	@Test
	public void canComputeComplement()
	{
		//computeComplement(100f);
		//computeComplement(10f);
		//computeComplement(1f);
		//computeComplement(1e-1f);
		//computeComplement(1e-2f);
		//computeComplement(1e-3f);
		//computeComplement(1e-4f);
		//computeComplement(1e-5f);
		//computeComplement(1e-6f);
		//computeComplement(1e-7f);
		//computeComplement(1e-8f);
		//computeComplement(1e-9f);
		//computeComplement(1e-10f);
		//computeComplement(1e-11f);
		//computeComplement(1e-12f);
		//computeComplement(1e-13f);
		//computeComplement(1e-14f);
		//computeComplement(1e-15f);
		//computeComplement(1e-16f);
		//computeComplement(1e-26f);
		//computeComplement(1e-36f);

		// Simple tests
		Assert.assertEquals(1, DoubleEquality.complement(0, Double.MIN_VALUE));
		Assert.assertEquals(1, DoubleEquality.complement(0, -Double.MIN_VALUE));
		Assert.assertEquals(2, DoubleEquality.complement(-Double.MIN_VALUE, Double.MIN_VALUE));

		Assert.assertEquals(1, FloatEquality.complement(0, Float.MIN_VALUE));
		Assert.assertEquals(1, FloatEquality.complement(0, -Float.MIN_VALUE));
		Assert.assertEquals(2, FloatEquality.complement(-Float.MIN_VALUE, Float.MIN_VALUE));

		// Check the complement is correct around a change of sign
		test(-Double.MAX_VALUE, Double.MAX_VALUE);
		test(-1e10, 1e40);
		test(-1e2, 1e2);
		test(-10, 10);
		test(-1, 1);
		test(-1e-1, 1e-1);
		test(-1e-2, 1e-2);
		test(1e-2, 1e-4);
		test(1e-2, 2e-2);
		test(1.0001, 1.0002);

		test(-Float.MAX_VALUE, Float.MAX_VALUE);
		test(-1e10f, 1e20f);
		test(-1e2f, 1e2f);
		test(-10f, 10f);
		test(-1f, 1f);
		test(-1e-1f, 1e-1f);
		test(-1e-2f, 1e-2f);
		test(1e-2f, 1e-4f);
		test(1e-2f, 2e-2f);
		test(1.0001f, 1.0002f);
	}

	private void test(double lower, double upper)
	{
		if (lower > upper)
		{
			double tmp = lower;
			lower = upper;
			upper = tmp;
		}
		long h = DoubleEquality.complement(0, upper);
		long l = DoubleEquality.complement(0, lower);
		long d = (lower > 0) ? h - l : h + l;
		if (d < 0)
			d = Long.MAX_VALUE;
		//log("%g - %g = %d\n", upper, lower, d);
		Assert.assertEquals(d, DoubleEquality.complement(lower, upper));
	}

	private void test(float lower, float upper)
	{
		if (lower > upper)
		{
			float tmp = lower;
			lower = upper;
			upper = tmp;
		}
		int h = FloatEquality.complement(0, upper);
		int l = FloatEquality.complement(0, lower);
		int d = (lower > 0) ? h - l : h + l;
		if (d < 0)
			d = Integer.MAX_VALUE;
		//log("%g - %g = %d\n", upper, lower, d);
		Assert.assertEquals(d, FloatEquality.complement(lower, upper));
	}

	/**
	 * Used to check what the int difference between float actually is
	 * 
	 * @param f
	 * @param f2
	 */
	@SuppressWarnings("unused")
	private void computeComplement(float f)
	{
		float f3 = f + f * 1e-2f;
		float f4 = f - f * 1e-2f;
		System.out.printf("%g -> %g = %d : %d (%g : %g)\n", f, f3, FloatEquality.complement(f3, f),
				DoubleEquality.complement(f3, f), FloatEquality.relativeError(f, f3),
				DoubleEquality.relativeError(f, f3));
		System.out.printf("%g -> %g = %d : %d (%g : %g)\n", f, f4, FloatEquality.complement(f4, f),
				DoubleEquality.complement(f4, f), FloatEquality.relativeError(f, f4),
				DoubleEquality.relativeError(f, f4));
	}

	@Test
	public void floatRelativeIsSameSpeedAsDoubleRelative()
	{
		org.junit.Assume.assumeTrue(TestSettings.RUN_SPEED_TESTS);

		float maxRelativeError = 1e-2f;
		float maxAbsoluteError = 1e-16f;
		final FloatEquality equality = new FloatEquality(maxRelativeError, maxAbsoluteError);
		final DoubleEquality equality2 = new DoubleEquality(maxRelativeError, maxAbsoluteError);

		// Create data
		Random rand = new Random(30051977);
		final float[] data1 = new float[MAX_ITER];
		final float[] data2 = new float[data1.length];
		final double[] data3 = new double[data1.length];
		final double[] data4 = new double[data1.length];

		for (int i = 0; i < data1.length; i++)
		{
			float f = rand.nextFloat();
			data1[i] = f * ((rand.nextFloat() > 0.5) ? 1.001f : 1.1f);
			data2[i] = f * ((rand.nextFloat() > 0.5) ? 1.001f : 1.1f);
			data3[i] = data1[i];
			data4[i] = data2[i];
		}

		TimingService ts = new TimingService(20);
		ts.execute(new BaseTimingTask("FloatEquality")
		{
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
				relative(equality, data1, data2);
				return null;
			}
		});
		ts.execute(new BaseTimingTask("DoubleEquality")
		{
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
				relative(equality2, data3, data4);
				return null;
			}
		});
		ts.repeat();
		ts.report();

		if (TestSettings.ASSERT_SPEED_TESTS)
			Assert.assertTrue(DoubleEquality.relativeError(ts.get(3).getMean(), ts.get(2).getMean()) < 0.2);
	}

	private void relative(FloatEquality equality, float[] data, float[] data2)
	{
		for (int i = 0; i < data.length; i++)
			equality.almostEqualRelativeOrAbsolute(data[i], data2[i]);
	}

	private void relative(DoubleEquality equality, double[] data, double[] data2)
	{
		for (int i = 0; i < data.length; i++)
			equality.almostEqualRelativeOrAbsolute(data[i], data2[i]);
	}

	void log(String format, Object... args)
	{
		System.out.printf(format, args);
	}
}
