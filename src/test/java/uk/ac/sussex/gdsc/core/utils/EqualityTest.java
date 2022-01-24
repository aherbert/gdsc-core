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
 * Copyright (C) 2011 - 2022 Alex Herbert
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

package uk.ac.sussex.gdsc.core.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Logger;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.junit5.SpeedTag;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.BaseTimingTask;
import uk.ac.sussex.gdsc.test.utils.TestComplexity;
import uk.ac.sussex.gdsc.test.utils.TestLogUtils;
import uk.ac.sussex.gdsc.test.utils.TestSettings;
import uk.ac.sussex.gdsc.test.utils.TimingService;
import uk.ac.sussex.gdsc.test.utils.functions.FunctionUtils;

@SuppressWarnings({"javadoc"})
class EqualityTest {
  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(EqualityTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  static final int MAX_ITER = 2000000;

  @Test
  void doubleCanSetAbsoluteError() {
    final DoubleEquality eq = new DoubleEquality(0, 0);
    Assertions.assertEquals(0, eq.getMaxRelativeError());
    Assertions.assertEquals(0, eq.getMaxAbsoluteError());
    Assertions.assertFalse(eq.almostEqualRelativeOrAbsolute(1, 1.001));
    eq.setMaxAbsoluteError(0.01);
    Assertions.assertEquals(0, eq.getMaxRelativeError());
    Assertions.assertEquals(0.01, eq.getMaxAbsoluteError());
    Assertions.assertTrue(eq.almostEqualRelativeOrAbsolute(1, 1.001));
  }

  @Test
  void doubleCanSetRelativeError() {
    final DoubleEquality eq = new DoubleEquality(0, 0);
    Assertions.assertEquals(0, eq.getMaxRelativeError());
    Assertions.assertEquals(0, eq.getMaxAbsoluteError());
    Assertions.assertFalse(eq.almostEqualRelativeOrAbsolute(1, 1.001));
    eq.setMaxRelativeError(0.01);
    Assertions.assertEquals(0.01, eq.getMaxRelativeError());
    Assertions.assertEquals(0, eq.getMaxAbsoluteError());
    Assertions.assertTrue(eq.almostEqualRelativeOrAbsolute(1, 1.001));
  }

  @Test
  void floatCanSetAbsoluteError() {
    final FloatEquality eq = new FloatEquality(0, 0);
    Assertions.assertEquals(0, eq.getMaxRelativeError());
    Assertions.assertEquals(0, eq.getMaxAbsoluteError());
    Assertions.assertFalse(eq.almostEqualRelativeOrAbsolute(1f, 1.001f));
    eq.setMaxAbsoluteError(0.01f);
    Assertions.assertEquals(0f, eq.getMaxRelativeError());
    Assertions.assertEquals(0.01f, eq.getMaxAbsoluteError());
    Assertions.assertTrue(eq.almostEqualRelativeOrAbsolute(1f, 1.001f));
  }

  @Test
  void floatCanSetRelativeError() {
    final FloatEquality eq = new FloatEquality(0, 0);
    Assertions.assertEquals(0, eq.getMaxRelativeError());
    Assertions.assertEquals(0, eq.getMaxAbsoluteError());
    Assertions.assertFalse(eq.almostEqualRelativeOrAbsolute(1f, 1.001f));
    eq.setMaxRelativeError(0.01f);
    Assertions.assertEquals(0.01f, eq.getMaxRelativeError());
    Assertions.assertEquals(0f, eq.getMaxAbsoluteError());
    Assertions.assertTrue(eq.almostEqualRelativeOrAbsolute(1f, 1.001f));
  }

  @Test
  void doubleCanComputeAlmostEqualComplement() {
    final double d1 = 1;
    final double d2 = Math.nextUp(d1);
    final double d3 = Math.nextUp(d2);
    Assertions.assertTrue(DoubleEquality.almostEqualComplement(d1, d1, 0L, 0));
    Assertions.assertFalse(DoubleEquality.almostEqualComplement(d1, d2, 0L, 0));
    Assertions.assertFalse(DoubleEquality.almostEqualComplement(d2, d1, 0L, 0));
    Assertions.assertTrue(DoubleEquality.almostEqualComplement(d1, d2, 1L, 0));
    Assertions.assertTrue(DoubleEquality.almostEqualComplement(d2, d1, 1L, 0));
    Assertions.assertFalse(DoubleEquality.almostEqualComplement(d1, d3, 1L, 0));
    Assertions.assertFalse(DoubleEquality.almostEqualComplement(d3, d1, 1L, 0));
    Assertions.assertTrue(DoubleEquality.almostEqualComplement(d1, d3, 2L, 0));
    Assertions.assertTrue(DoubleEquality.almostEqualComplement(d3, d1, 2L, 0));
    Assertions.assertFalse(DoubleEquality.almostEqualComplement(d1, d3, 0L, 1e-16));
    Assertions.assertFalse(DoubleEquality.almostEqualComplement(d3, d1, 0L, 1e-16));
    Assertions.assertTrue(DoubleEquality.almostEqualComplement(d1, d3, 0L, 1e-15));
    Assertions.assertTrue(DoubleEquality.almostEqualComplement(d3, d1, 0L, 1e-15));
  }

  @Test
  void floatCanComputeAlmostEqualComplement() {
    final float d1 = 1;
    final float d2 = Math.nextUp(d1);
    final float d3 = Math.nextUp(d2);
    Assertions.assertTrue(FloatEquality.almostEqualComplement(d1, d1, 0, 0));
    Assertions.assertFalse(FloatEquality.almostEqualComplement(d1, d2, 0, 0));
    Assertions.assertFalse(FloatEquality.almostEqualComplement(d2, d1, 0, 0));
    Assertions.assertTrue(FloatEquality.almostEqualComplement(d1, d2, 1, 0));
    Assertions.assertTrue(FloatEquality.almostEqualComplement(d2, d1, 1, 0));
    Assertions.assertFalse(FloatEquality.almostEqualComplement(d1, d3, 1, 0));
    Assertions.assertFalse(FloatEquality.almostEqualComplement(d3, d1, 1, 0));
    Assertions.assertTrue(FloatEquality.almostEqualComplement(d1, d3, 2, 0));
    Assertions.assertTrue(FloatEquality.almostEqualComplement(d3, d1, 2, 0));
    Assertions.assertFalse(FloatEquality.almostEqualComplement(d1, d3, 0, 1e-7f));
    Assertions.assertFalse(FloatEquality.almostEqualComplement(d3, d1, 0, 1e-7f));
    Assertions.assertTrue(FloatEquality.almostEqualComplement(d1, d3, 0, 1e-6f));
    Assertions.assertTrue(FloatEquality.almostEqualComplement(d3, d1, 0, 1e-6f));
  }

  @Test
  void doubleCanCompareComplement() {
    final double d1 = 1;
    final double d2 = Math.nextUp(d1);
    final double d3 = Math.nextUp(d2);
    Assertions.assertEquals(-1, DoubleEquality.compareComplement(d1, d2, 0));
    Assertions.assertEquals(1, DoubleEquality.compareComplement(d2, d1, 0));
    Assertions.assertEquals(0, DoubleEquality.compareComplement(d1, d2, 1));
    Assertions.assertEquals(-1, DoubleEquality.compareComplement(d1, d3, 1));
    Assertions.assertEquals(1, DoubleEquality.compareComplement(d3, d1, 1));
    Assertions.assertEquals(0, DoubleEquality.compareComplement(d1, d3, 2));
  }

  @Test
  void floatCanCompareComplement() {
    final float d1 = 1;
    final float d2 = Math.nextUp(d1);
    final float d3 = Math.nextUp(d2);
    Assertions.assertEquals(-1, FloatEquality.compareComplement(d1, d2, 0));
    Assertions.assertEquals(1, FloatEquality.compareComplement(d2, d1, 0));
    Assertions.assertEquals(0, FloatEquality.compareComplement(d1, d2, 1));
    Assertions.assertEquals(-1, FloatEquality.compareComplement(d1, d3, 1));
    Assertions.assertEquals(1, FloatEquality.compareComplement(d3, d1, 1));
    Assertions.assertEquals(0, FloatEquality.compareComplement(d1, d3, 2));
  }

  @Test
  void doubleRelativeErrorIsCorrectUntilUlpsIsSmall() {
    final int precision = new BigDecimal(Double.toString(Double.MAX_VALUE)).precision();
    // TestLog.debug(logger,"Double max precision = %d", precision);
    for (int sig = 1; sig <= precision; sig++) {
      final BigDecimal error = new BigDecimal("1e-" + sig);
      final double e = error.doubleValue();
      final double tolerance = e * 0.01;
      final BigDecimal one_m_error = BigDecimal.ONE.subtract(error);
      final BigDecimal one_one_m_error =
          BigDecimal.ONE.divide(one_m_error, sig * 10, RoundingMode.HALF_UP);

      // TestLog.debug(logger,"Error = %s %s %s", error, one_m_error, one_one_m_error);
      int same = 0;
      int total = 0;
      for (int leadingDigit = 1; leadingDigit <= 9; leadingDigit++) {
        for (int trailingDigit = 1; trailingDigit <= 9; trailingDigit++) {
          BigDecimal v1 = BigDecimal.valueOf(trailingDigit);
          v1 = v1.scaleByPowerOfTen(-(sig - 1));
          final BigDecimal toAdd = BigDecimal.valueOf(leadingDigit);
          v1 = v1.add(toAdd);

          // Get number with a set relative error
          final BigDecimal v2low = v1.multiply(one_m_error);
          final BigDecimal v2high = v1.multiply(one_one_m_error);

          // bd1 = bd1.round(new MathContext(sig, RoundingMode.HALF_DOWN));
          final double d = v1.doubleValue();
          final double d1 = v2low.doubleValue();
          final double d2 = v2high.doubleValue();
          final long ulps1 = Double.doubleToLongBits(d) - Double.doubleToLongBits(d1);
          final long ulps2 = Double.doubleToLongBits(d2) - Double.doubleToLongBits(d);
          final double rel1 = DoubleEquality.relativeError(d, d1);
          final double rel2 = DoubleEquality.relativeError(d, d2);
          // TestLog.debug(logger,"%d %s < %s < %s = %d %d %g %g", sig, v2low, v1, v2high, ulps1,
          // ulps2, rel1, rel2);
          if (ulps1 > 100) {
            Assertions.assertEquals(e, rel1, tolerance);
            Assertions.assertEquals(e, rel2, tolerance);
          }
          if (ulps1 == ulps2) {
            same++;
          }
          total++;
        }
      }
      Assertions.assertTrue(same < total);
    }
  }

  @Test
  void floatRelativeErrorIsCorrectUntilUlpsIsSmall() {
    final int precision = new BigDecimal(Float.toString(Float.MAX_VALUE)).precision();
    // TestLog.debug(logger,"Float max precision = %d", precision);
    for (int sig = 1; sig <= precision; sig++) {
      final BigDecimal error = new BigDecimal("1e-" + sig);
      final double e = error.doubleValue();
      final double tolerance = e * 0.01;
      final BigDecimal one_m_error = BigDecimal.ONE.subtract(error);
      final BigDecimal one_one_m_error =
          BigDecimal.ONE.divide(one_m_error, sig * 10, RoundingMode.HALF_UP);

      // TestLog.debug(logger,"Error = %s %s %s", error, one_m_error, one_one_m_error);
      int same = 0;
      int total = 0;
      for (int leadingDigit = 1; leadingDigit <= 9; leadingDigit++) {
        for (int trailingDigit = 1; trailingDigit <= 9; trailingDigit++) {
          BigDecimal v1 = BigDecimal.valueOf(trailingDigit);
          v1 = v1.scaleByPowerOfTen(-(sig - 1));
          final BigDecimal toAdd = BigDecimal.valueOf(leadingDigit);
          v1 = v1.add(toAdd);

          // Get number with a set relative error
          final BigDecimal v2low = v1.multiply(one_m_error);
          final BigDecimal v2high = v1.multiply(one_one_m_error);

          // bd1 = bd1.round(new MathContext(sig, RoundingMode.HALF_DOWN));
          final float d = v1.floatValue();
          final float d1 = v2low.floatValue();
          final float d2 = v2high.floatValue();
          final int ulps1 = Float.floatToIntBits(d) - Float.floatToIntBits(d1);
          final int ulps2 = Float.floatToIntBits(d2) - Float.floatToIntBits(d);
          final float rel1 = FloatEquality.relativeError(d, d1);
          final float rel2 = FloatEquality.relativeError(d, d2);
          // TestLog.debug(logger,"%d %s < %s < %s = %d %d %g %g", sig, v2low, v1, v2high, ulps1,
          // ulps2, rel1, rel2);
          if (ulps1 > 100) {
            Assertions.assertEquals(e, rel1, tolerance);
            Assertions.assertEquals(e, rel2, tolerance);
          }
          if (ulps1 == ulps2) {
            same++;
          }
          total++;
        }
      }
      Assertions.assertTrue(same < total);
    }
  }

  @Test
  void doubleCanComputeRelativeEpsilonFromSignificantBits() {
    Assertions.assertEquals(0, DoubleEquality.getRelativeEpsilon(0));
    Assertions.assertEquals(0, DoubleEquality.getRelativeEpsilon(53));
    for (int s = 1; s <= 52; s++) {
      final double e = Math.pow(2, -s);
      Assertions.assertEquals(e, DoubleEquality.getRelativeEpsilon(s));
    }
  }

  @Test
  void floatCanComputeRelativeEpsilonFromSignificantBits() {
    Assertions.assertEquals(0, FloatEquality.getRelativeEpsilon(0));
    Assertions.assertEquals(0, FloatEquality.getRelativeEpsilon(24));
    for (int s = 1; s < 23; s++) {
      final double e = Math.pow(2, -s);
      Assertions.assertEquals(e, FloatEquality.getRelativeEpsilon(s));
    }
  }

  @Test
  void doubleCanComputeEquality() {
    final double maxRelativeError = 1e-3f;
    final double maxAbsoluteError = 1e-16f;
    final DoubleEquality equality = new DoubleEquality(maxRelativeError, maxAbsoluteError);

    for (int i = 0; i < 100; i++) {
      final double f = i / 10000.0;
      final double f2 = f * (1.00f + maxRelativeError - 1e-3f);
      final double f3 = f * (1.0f + 2.0f * maxRelativeError);
      Assertions.assertTrue(equality.almostEqualRelativeOrAbsolute(f, f),
          () -> String.format("not equal %f", f));
      Assertions.assertTrue(equality.almostEqualRelativeOrAbsolute(f, f2),
          () -> String.format("not equal %f", f));
      if (i > 0) {
        Assertions.assertFalse(equality.almostEqualRelativeOrAbsolute(f, f3),
            () -> String.format("equal %f", f));
      }
    }
  }

  @Test
  void floatCanComputeEquality() {
    final float maxRelativeError = 1e-3f;
    final float maxAbsoluteError = 1e-16f;
    final FloatEquality equality = new FloatEquality(maxRelativeError, maxAbsoluteError);

    for (int i = 0; i < 100; i++) {
      final float f = (float) (i / 10000.0);
      final float f2 = f * (1.00f + maxRelativeError - 1e-3f);
      final float f3 = f * (1.0f + 2.0f * maxRelativeError);
      Assertions.assertTrue(equality.almostEqualRelativeOrAbsolute(f, f),
          () -> String.format("not equal %f", f));
      Assertions.assertTrue(equality.almostEqualRelativeOrAbsolute(f, f2),
          () -> String.format("not equal %f", f));
      if (i > 0) {
        Assertions.assertFalse(equality.almostEqualRelativeOrAbsolute(f, f3),
            () -> String.format("equal %f", f));
      }
    }
  }

  @Test
  void canComputeComplement() {
    // computeComplement(100f);
    // computeComplement(10f);
    // computeComplement(1f);
    // computeComplement(1e-1f);
    // computeComplement(1e-2f);
    // computeComplement(1e-3f);
    // computeComplement(1e-4f);
    // computeComplement(1e-5f);
    // computeComplement(1e-6f);
    // computeComplement(1e-7f);
    // computeComplement(1e-8f);
    // computeComplement(1e-9f);
    // computeComplement(1e-10f);
    // computeComplement(1e-11f);
    // computeComplement(1e-12f);
    // computeComplement(1e-13f);
    // computeComplement(1e-14f);
    // computeComplement(1e-15f);
    // computeComplement(1e-16f);
    // computeComplement(1e-26f);
    // computeComplement(1e-36f);

    // Simple tests
    Assertions.assertEquals(1, DoubleEquality.complement(0, Double.MIN_VALUE));
    Assertions.assertEquals(1, DoubleEquality.complement(0, -Double.MIN_VALUE));
    Assertions.assertEquals(2, DoubleEquality.complement(-Double.MIN_VALUE, Double.MIN_VALUE));
    Assertions.assertEquals(2,
        DoubleEquality.signedComplement(Double.MIN_VALUE, -Double.MIN_VALUE));
    Assertions.assertEquals(-2,
        DoubleEquality.signedComplement(-Double.MIN_VALUE, Double.MIN_VALUE));
    Assertions.assertEquals(Long.MAX_VALUE,
        DoubleEquality.signedComplement(Double.MAX_VALUE, -Double.MAX_VALUE));
    Assertions.assertEquals(Long.MIN_VALUE,
        DoubleEquality.signedComplement(-Double.MAX_VALUE, Double.MAX_VALUE));

    Assertions.assertEquals(1, FloatEquality.complement(0, Float.MIN_VALUE));
    Assertions.assertEquals(1, FloatEquality.complement(0, -Float.MIN_VALUE));
    Assertions.assertEquals(2, FloatEquality.complement(-Float.MIN_VALUE, Float.MIN_VALUE));
    Assertions.assertEquals(2, FloatEquality.signedComplement(Float.MIN_VALUE, -Float.MIN_VALUE));
    Assertions.assertEquals(-2, FloatEquality.signedComplement(-Float.MIN_VALUE, Float.MIN_VALUE));
    Assertions.assertEquals(Integer.MAX_VALUE,
        FloatEquality.signedComplement(Float.MAX_VALUE, -Float.MAX_VALUE));
    Assertions.assertEquals(Integer.MIN_VALUE,
        FloatEquality.signedComplement(-Float.MAX_VALUE, Float.MAX_VALUE));

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

  private static void test(double lower, double upper) {
    if (lower > upper) {
      final double tmp = lower;
      lower = upper;
      upper = tmp;
    }
    final long h = DoubleEquality.complement(0, upper);
    final long l = DoubleEquality.complement(0, lower);
    long expected = (lower > 0) ? h - l : h + l;
    if (expected < 0) {
      expected = Long.MAX_VALUE;
    } else {
      final long c = DoubleEquality.signedComplement(lower, upper);
      Assertions.assertTrue(c < 0);
      Assertions.assertEquals(expected, -c);
      Assertions.assertEquals(expected, DoubleEquality.signedComplement(upper, lower));
    }
    // log("%g - %g = %d", upper, lower, d);
    Assertions.assertEquals(expected, DoubleEquality.complement(lower, upper));
  }

  private static void test(float lower, float upper) {
    if (lower > upper) {
      final float tmp = lower;
      lower = upper;
      upper = tmp;
    }
    final int h = FloatEquality.complement(0, upper);
    final int l = FloatEquality.complement(0, lower);
    int expected = (lower > 0) ? h - l : h + l;
    if (expected < 0) {
      expected = Integer.MAX_VALUE;
    }
    // log("%g - %g = %d", upper, lower, d);
    Assertions.assertEquals(expected, FloatEquality.complement(lower, upper));
  }

  /**
   * Used to check what the int difference between float actually is.
   *
   * @param value the value
   */
  @SuppressWarnings("unused")
  private static void computeComplement(float value) {
    final float f3 = value + value * 1e-2f;
    final float f4 = value - value * 1e-2f;
    logger.info(FunctionUtils.getSupplier("%g -> %g = %d : %d (%g : %g)", value, f3,
        FloatEquality.complement(f3, value), DoubleEquality.complement(f3, value),
        FloatEquality.relativeError(value, f3), DoubleEquality.relativeError(value, f3)));
    logger.info(FunctionUtils.getSupplier("%g -> %g = %d : %d (%g : %g)", value, f4,
        FloatEquality.complement(f4, value), DoubleEquality.complement(f4, value),
        FloatEquality.relativeError(value, f4), DoubleEquality.relativeError(value, f4)));
  }

  @SpeedTag
  @SeededTest
  void floatRelativeIsSameSpeedAsDoubleRelative(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final float maxRelativeError = 1e-2f;
    final float maxAbsoluteError = 1e-16f;
    final FloatEquality equality = new FloatEquality(maxRelativeError, maxAbsoluteError);
    final DoubleEquality equality2 = new DoubleEquality(maxRelativeError, maxAbsoluteError);

    // Create data
    final UniformRandomProvider rand = RngUtils.create(seed.getSeed());
    final float[] data1 = new float[MAX_ITER];
    final float[] data2 = new float[data1.length];
    final double[] data3 = new double[data1.length];
    final double[] data4 = new double[data1.length];

    for (int i = 0; i < data1.length; i++) {
      final float f = rand.nextFloat();
      data1[i] = f * ((rand.nextFloat() > 0.5) ? 1.001f : 1.1f);
      data2[i] = f * ((rand.nextFloat() > 0.5) ? 1.001f : 1.1f);
      data3[i] = data1[i];
      data4[i] = data2[i];
    }

    final TimingService ts = new TimingService(20);
    ts.execute(new BaseTimingTask("FloatEquality") {
      @Override
      public int getSize() {
        return 1;
      }

      @Override
      public Object getData(int index) {
        return null;
      }

      @Override
      public Object run(Object data) {
        relative(equality, data1, data2);
        return null;
      }
    });
    ts.execute(new BaseTimingTask("DoubleEquality") {
      @Override
      public int getSize() {
        return 1;
      }

      @Override
      public Object getData(int index) {
        return null;
      }

      @Override
      public Object run(Object data) {
        relative(equality2, data3, data4);
        return null;
      }
    });
    ts.repeat();
    logger.info(ts.getReport());

    final double error = DoubleEquality.relativeError(ts.get(-1).getMean(), ts.get(-2).getMean());
    logger.log(TestLogUtils.getResultRecord(error < 0.2,
        "Float and Double relative equality not the same speed: Error=%f", error));
  }

  private static void relative(FloatEquality equality, float[] data, float[] data2) {
    for (int i = 0; i < data.length; i++) {
      equality.almostEqualRelativeOrAbsolute(data[i], data2[i]);
    }
  }

  private static void relative(DoubleEquality equality, double[] data, double[] data2) {
    for (int i = 0; i < data.length; i++) {
      equality.almostEqualRelativeOrAbsolute(data[i], data2[i]);
    }
  }
}
