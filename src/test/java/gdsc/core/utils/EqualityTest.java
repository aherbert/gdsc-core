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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Test;

import gdsc.test.BaseTimingTask;
import gdsc.test.TestSettings;
import gdsc.test.TestSettings.LogLevel;
import gdsc.test.TimingService;

@SuppressWarnings({ "deprecation", "javadoc" })
public class EqualityTest
{
	int MAX_ITER = 2000000;

	@Test
	public void doubleRelativeErrorIsCorrectUntilULPSIsSmall()
	{
		final int precision = new BigDecimal(Double.toString(Double.MAX_VALUE)).precision();
		//TestSettings.debug("Double max precision = %d\n", precision);
		for (int sig = 1; sig <= precision; sig++)
		{
			final BigDecimal error = new BigDecimal("1e-" + sig);
			final double e = error.doubleValue();
			final double tolerance = e * 0.01;
			final BigDecimal one_m_error = BigDecimal.ONE.subtract(error);
			final BigDecimal one_one_m_error = BigDecimal.ONE.divide(one_m_error, sig * 10, RoundingMode.HALF_UP);

			//TestSettings.debug("Error = %s  %s  %s\n", error, one_m_error, one_one_m_error);
			int same = 0, total = 0;
			for (int leadingDigit = 1; leadingDigit <= 9; leadingDigit++)
				for (int trailingDigit = 1; trailingDigit <= 9; trailingDigit++)
				{
					BigDecimal A = BigDecimal.valueOf(trailingDigit);
					A = A.scaleByPowerOfTen(-(sig - 1));
					final BigDecimal toAdd = BigDecimal.valueOf(leadingDigit);
					A = A.add(toAdd);

					// Get number with a set relative error
					final BigDecimal BLow = A.multiply(one_m_error);
					final BigDecimal BHigh = A.multiply(one_one_m_error);

					//bd1 = bd1.round(new MathContext(sig, RoundingMode.HALF_DOWN));
					final double d = A.doubleValue();
					final double d1 = BLow.doubleValue();
					final double d2 = BHigh.doubleValue();
					final long ulps1 = Double.doubleToLongBits(d) - Double.doubleToLongBits(d1);
					final long ulps2 = Double.doubleToLongBits(d2) - Double.doubleToLongBits(d);
					final double rel1 = DoubleEquality.relativeError(d, d1);
					final double rel2 = DoubleEquality.relativeError(d, d2);
					//TestSettings.debug("%d  %s < %s < %s = %d  %d  %g  %g\n", sig, BLow, A, BHigh, ulps1, ulps2, rel1,	rel2);
					if (ulps1 > 100)
					{
						Assert.assertEquals(e, rel1, tolerance);
						Assert.assertEquals(e, rel2, tolerance);
					}
					if (ulps1 == ulps2)
						same++;
					total++;
				}
			Assert.assertTrue(same < total);
		}
	}

	@Test
	public void floatRelativeErrorIsCorrectUntilULPSIsSmall()
	{
		final int precision = new BigDecimal(Float.toString(Float.MAX_VALUE)).precision();
		//TestSettings.debug("Float max precision = %d\n", precision);
		for (int sig = 1; sig <= precision; sig++)
		{
			final BigDecimal error = new BigDecimal("1e-" + sig);
			final double e = error.doubleValue();
			final double tolerance = e * 0.01;
			final BigDecimal one_m_error = BigDecimal.ONE.subtract(error);
			final BigDecimal one_one_m_error = BigDecimal.ONE.divide(one_m_error, sig * 10, RoundingMode.HALF_UP);

			//TestSettings.debug("Error = %s  %s  %s\n", error, one_m_error, one_one_m_error);
			int same = 0, total = 0;
			for (int leadingDigit = 1; leadingDigit <= 9; leadingDigit++)
				for (int trailingDigit = 1; trailingDigit <= 9; trailingDigit++)
				{
					BigDecimal A = BigDecimal.valueOf(trailingDigit);
					A = A.scaleByPowerOfTen(-(sig - 1));
					final BigDecimal toAdd = BigDecimal.valueOf(leadingDigit);
					A = A.add(toAdd);

					// Get number with a set relative error
					final BigDecimal BLow = A.multiply(one_m_error);
					final BigDecimal BHigh = A.multiply(one_one_m_error);

					//bd1 = bd1.round(new MathContext(sig, RoundingMode.HALF_DOWN));
					final float d = A.floatValue();
					final float d1 = BLow.floatValue();
					final float d2 = BHigh.floatValue();
					final int ulps1 = Float.floatToIntBits(d) - Float.floatToIntBits(d1);
					final int ulps2 = Float.floatToIntBits(d2) - Float.floatToIntBits(d);
					final float rel1 = FloatEquality.relativeError(d, d1);
					final float rel2 = FloatEquality.relativeError(d, d2);
					//TestSettings.debug("%d  %s < %s < %s = %d  %d  %g  %g\n", sig, BLow, A, BHigh, ulps1, ulps2, rel1,	rel2);
					if (ulps1 > 100)
					{
						Assert.assertEquals(e, rel1, tolerance);
						Assert.assertEquals(e, rel2, tolerance);
					}
					if (ulps1 == ulps2)
						same++;
					total++;
				}
			Assert.assertTrue(same < total);
		}
	}

	@Test
	public void doubleCanComputeRelativeErrorFromSignificantDigits()
	{
		final BigDecimal number = BigDecimal.valueOf(2).divide(BigDecimal.valueOf(9), 20, RoundingMode.HALF_UP);
		final BigDecimal margin = BigDecimal.valueOf(0.05);
		// This does not work over the full range of siginifcant digits
		for (int s = 2; s <= DoubleEquality.getMaxSignificantDigits() - 2; s++)
		{
			final BigDecimal A = number.round(new MathContext(s));
			final BigDecimal error = new BigDecimal("1e-" + (s - 1));
			final double e = error.doubleValue();
			final BigDecimal one_m_error = BigDecimal.ONE.subtract(error);

			BigDecimal BLow = A.multiply(one_m_error);
			// Add margin for error
			BLow = BLow.add(A.subtract(BLow).multiply(margin));
			final BigDecimal BLower = BLow.round(new MathContext(s, RoundingMode.DOWN));

			final double a = A.doubleValue();
			final double b = BLow.doubleValue();
			final double max = DoubleEquality.getMaxRelativeError(s);
			Assert.assertEquals(e, max, e * 0.01);
			//double rel = DoubleEquality.relativeError(a, b);
			//TestSettings.debug("[%d] %s -> %s : %g  %g\n", s, A, BLow, max, rel);
			final DoubleEquality eq = new DoubleEquality(s, 0);
			Assert.assertTrue(eq.almostEqualRelativeOrAbsolute(a, b));
			Assert.assertFalse(eq.almostEqualRelativeOrAbsolute(a, BLower.doubleValue()));
		}
	}

	@Test
	public void floatCanComputeRelativeErrorFromSignificantDigits()
	{
		final BigDecimal number = BigDecimal.valueOf(2).divide(BigDecimal.valueOf(9), 20, RoundingMode.HALF_UP);
		final BigDecimal margin = BigDecimal.valueOf(0.05);
		// This does not work over the full range of siginifcant digits
		for (int s = 2; s <= FloatEquality.getMaxSignificantDigits() - 2; s++)
		{
			final BigDecimal A = number.round(new MathContext(s));
			final BigDecimal error = new BigDecimal("1e-" + (s - 1));
			final float e = error.floatValue();
			final BigDecimal one_m_error = BigDecimal.ONE.subtract(error);

			BigDecimal BLow = A.multiply(one_m_error);
			// Add margin for error
			BLow = BLow.add(A.subtract(BLow).multiply(margin));
			final BigDecimal BLower = BLow.round(new MathContext(s, RoundingMode.DOWN));

			final float a = A.floatValue();
			final float b = BLow.floatValue();
			final float max = FloatEquality.getMaxRelativeError(s);
			Assert.assertEquals(e, max, e * 0.01);
			//float rel = FloatEquality.relativeError(a, b);
			//TestSettings.debug("[%d] %s -> %s : %g  %g\n", s, A, BLow, max, rel);
			final FloatEquality eq = new FloatEquality(s, 0);
			Assert.assertTrue(eq.almostEqualRelativeOrAbsolute(a, b));
			Assert.assertFalse(eq.almostEqualRelativeOrAbsolute(a, BLower.floatValue()));
		}
	}

	@Test
	public void doubleCanComputeEquality()
	{
		final double maxRelativeError = 1e-3f;
		final double maxAbsoluteError = 1e-16f;
		final DoubleEquality equality = new DoubleEquality(maxRelativeError, maxAbsoluteError);
		final DoubleEquality equality2 = new DoubleEquality(4, maxAbsoluteError);

		for (int i = 0; i < 100; i++)
		{
			final double f = i / 10000.0;
			final double f2 = f * (1.00f + maxRelativeError - 1e-3f);
			final double f3 = f * (1.0f + 2.0f * maxRelativeError);
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
		final float maxRelativeError = 1e-3f;
		final float maxAbsoluteError = 1e-16f;
		final FloatEquality equality = new FloatEquality(maxRelativeError, maxAbsoluteError);
		final FloatEquality equality2 = new FloatEquality(4, maxAbsoluteError);

		for (int i = 0; i < 100; i++)
		{
			final float f = (float) (i / 10000.0);
			final float f2 = f * (1.00f + maxRelativeError - 1e-3f);
			final float f3 = f * (1.0f + 2.0f * maxRelativeError);
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
		Assert.assertEquals(2, DoubleEquality.signedComplement(Double.MIN_VALUE, -Double.MIN_VALUE));
		Assert.assertEquals(-2, DoubleEquality.signedComplement(-Double.MIN_VALUE, Double.MIN_VALUE));
		Assert.assertEquals(Long.MAX_VALUE, DoubleEquality.signedComplement(Double.MAX_VALUE, -Double.MAX_VALUE));
		Assert.assertEquals(Long.MIN_VALUE, DoubleEquality.signedComplement(-Double.MAX_VALUE, Double.MAX_VALUE));

		Assert.assertEquals(1, FloatEquality.complement(0, Float.MIN_VALUE));
		Assert.assertEquals(1, FloatEquality.complement(0, -Float.MIN_VALUE));
		Assert.assertEquals(2, FloatEquality.complement(-Float.MIN_VALUE, Float.MIN_VALUE));
		Assert.assertEquals(2, FloatEquality.signedComplement(Float.MIN_VALUE, -Float.MIN_VALUE));
		Assert.assertEquals(-2, FloatEquality.signedComplement(-Float.MIN_VALUE, Float.MIN_VALUE));
		Assert.assertEquals(Integer.MAX_VALUE, FloatEquality.signedComplement(Float.MAX_VALUE, -Float.MAX_VALUE));
		Assert.assertEquals(Integer.MIN_VALUE, FloatEquality.signedComplement(-Float.MAX_VALUE, Float.MAX_VALUE));

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

	private static void test(double lower, double upper)
	{
		if (lower > upper)
		{
			final double tmp = lower;
			lower = upper;
			upper = tmp;
		}
		final long h = DoubleEquality.complement(0, upper);
		final long l = DoubleEquality.complement(0, lower);
		long d = (lower > 0) ? h - l : h + l;
		if (d < 0)
			d = Long.MAX_VALUE;
		else
		{
			final long c = DoubleEquality.signedComplement(lower, upper);
			Assert.assertTrue(c < 0);
			Assert.assertEquals(d, -c);
			Assert.assertEquals(d, DoubleEquality.signedComplement(upper, lower));
		}
		//log("%g - %g = %d\n", upper, lower, d);
		Assert.assertEquals(d, DoubleEquality.complement(lower, upper));
	}

	private static void test(float lower, float upper)
	{
		if (lower > upper)
		{
			final float tmp = lower;
			lower = upper;
			upper = tmp;
		}
		final int h = FloatEquality.complement(0, upper);
		final int l = FloatEquality.complement(0, lower);
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
	private static void computeComplement(float f)
	{
		final float f3 = f + f * 1e-2f;
		final float f4 = f - f * 1e-2f;
		TestSettings.info("%g -> %g = %d : %d (%g : %g)\n", f, f3, FloatEquality.complement(f3, f),
				DoubleEquality.complement(f3, f), FloatEquality.relativeError(f, f3),
				DoubleEquality.relativeError(f, f3));
		TestSettings.info("%g -> %g = %d : %d (%g : %g)\n", f, f4, FloatEquality.complement(f4, f),
				DoubleEquality.complement(f4, f), FloatEquality.relativeError(f, f4),
				DoubleEquality.relativeError(f, f4));
	}

	@Test
	public void floatRelativeIsSameSpeedAsDoubleRelative()
	{
		TestSettings.assumeSpeedTest();

		final float maxRelativeError = 1e-2f;
		final float maxAbsoluteError = 1e-16f;
		final FloatEquality equality = new FloatEquality(maxRelativeError, maxAbsoluteError);
		final DoubleEquality equality2 = new DoubleEquality(maxRelativeError, maxAbsoluteError);

		// Create data
		final RandomGenerator rand = TestSettings.getRandomGenerator();
		final float[] data1 = new float[MAX_ITER];
		final float[] data2 = new float[data1.length];
		final double[] data3 = new double[data1.length];
		final double[] data4 = new double[data1.length];

		for (int i = 0; i < data1.length; i++)
		{
			final float f = rand.nextFloat();
			data1[i] = f * ((rand.nextFloat() > 0.5) ? 1.001f : 1.1f);
			data2[i] = f * ((rand.nextFloat() > 0.5) ? 1.001f : 1.1f);
			data3[i] = data1[i];
			data4[i] = data2[i];
		}

		final TimingService ts = new TimingService(20);
		ts.execute(new BaseTimingTask("FloatEquality")
		{
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
				relative(equality, data1, data2);
				return null;
			}
		});
		ts.execute(new BaseTimingTask("DoubleEquality")
		{
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
				relative(equality2, data3, data4);
				return null;
			}
		});
		ts.repeat();
		if (TestSettings.allow(LogLevel.INFO))
			ts.report();

		final double error = DoubleEquality.relativeError(ts.get(-1).getMean(), ts.get(-2).getMean());
		TestSettings.logSpeedTestResult(error < 0.2,
				"Float and Double relative equality not the same speed: Error=" + error);
	}

	private static void relative(FloatEquality equality, float[] data, float[] data2)
	{
		for (int i = 0; i < data.length; i++)
			equality.almostEqualRelativeOrAbsolute(data[i], data2[i]);
	}

	private static void relative(DoubleEquality equality, double[] data, double[] data2)
	{
		for (int i = 0; i < data.length; i++)
			equality.almostEqualRelativeOrAbsolute(data[i], data2[i]);
	}

	void log(String format, Object... args)
	{
		TestSettings.info(format, args);
	}
}
