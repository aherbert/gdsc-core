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
 * Copyright (C) 2011 - 2020 Alex Herbert
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

package uk.ac.sussex.gdsc.core.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link NumberUtils}.
 */
@SuppressWarnings({"javadoc"})
public class NumberUtilsTest {

  @Test
  public void canGetUnsignedExponentFloat() {
    final float value = Math.nextDown(1f);
    for (float f : new float[] {0, 1, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
        1.23f, value, Math.scalb(value, -45), Math.scalb(value, 67)}) {
      Assertions.assertEquals(Math.getExponent(f), NumberUtils.getUnsignedExponent(f) - 127);
    }
  }

  @Test
  public void canGetUnsignedExponentDouble() {
    final double value = Math.nextDown(1.0);
    for (double f : new double[] {0, 1, Double.NaN, Double.POSITIVE_INFINITY,
        Double.NEGATIVE_INFINITY, 1.23, value, Math.scalb(value, -45), Math.scalb(value, 67)}) {
      Assertions.assertEquals(Math.getExponent(f), NumberUtils.getUnsignedExponent(f) - 1023);
    }
  }

  @Test
  public void canGetSignedExponentFloat() {
    final float value = Math.nextDown(1f);
    for (float f : new float[] {0, 1, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
        1.23f, value, Math.scalb(value, -45), Math.scalb(value, 67)}) {
      Assertions.assertEquals(Math.getExponent(f), NumberUtils.getSignedExponent(f));
    }
  }

  @Test
  public void canGetSignedExponentDouble() {
    final double value = Math.nextDown(1.0);
    for (double f : new double[] {0, 1, Double.NaN, Double.POSITIVE_INFINITY,
        Double.NEGATIVE_INFINITY, 1.23, value, Math.scalb(value, -45), Math.scalb(value, 67)}) {
      Assertions.assertEquals(Math.getExponent(f), NumberUtils.getSignedExponent(f));
    }
  }

  @Test
  public void canGetMantissaFloat() {
    Assertions.assertEquals(0, NumberUtils.getMantissa(0f, true));
    Assertions.assertEquals(0, NumberUtils.getMantissa(0f, false));
    final int bits = -1 << 23;
    final int extra = 1 << 23;
    Assertions.assertEquals(42, NumberUtils.getMantissa(Float.intBitsToFloat(bits | 42), true));
    Assertions.assertEquals(extra | 42,
        NumberUtils.getMantissa(Float.intBitsToFloat(bits | 42), false));
    Assertions.assertEquals(0, NumberUtils.getMantissa(Float.MIN_NORMAL, true));
    Assertions.assertEquals(extra, NumberUtils.getMantissa(Float.MIN_NORMAL, false));
    Assertions.assertEquals(1, NumberUtils.getMantissa(Float.MIN_VALUE, true));
    Assertions.assertEquals(2, NumberUtils.getMantissa(Float.MIN_VALUE, false));
  }

  @Test
  public void canGetMantissaDouble() {
    Assertions.assertEquals(0, NumberUtils.getMantissa(0f, true));
    Assertions.assertEquals(0, NumberUtils.getMantissa(0f, false));
    final long bits = -1L << 52;
    final long extra = 1L << 52;
    Assertions.assertEquals(42, NumberUtils.getMantissa(Double.longBitsToDouble(bits | 42), true));
    Assertions.assertEquals(extra | 42,
        NumberUtils.getMantissa(Double.longBitsToDouble(bits | 42), false));
    Assertions.assertEquals(0, NumberUtils.getMantissa(Double.MIN_NORMAL, true));
    Assertions.assertEquals(extra, NumberUtils.getMantissa(Double.MIN_NORMAL, false));
    Assertions.assertEquals(1, NumberUtils.getMantissa(Double.MIN_VALUE, true));
    Assertions.assertEquals(2, NumberUtils.getMantissa(Double.MIN_VALUE, false));
  }

  @Test
  public void canGetSignFloat() {
    Assertions.assertEquals(0, NumberUtils.getSign(Float.NaN));
    for (float f : new float[] {-8.96f, -1, -0f, 0, 1, 2.34f, Float.POSITIVE_INFINITY,
        Float.NEGATIVE_INFINITY}) {
      Assertions.assertEquals((int) Math.signum(f), NumberUtils.getSign(f));
    }
  }

  @Test
  public void canGetSignDouble() {
    Assertions.assertEquals(0, NumberUtils.getSign(Double.NaN));
    for (double f : new double[] {-8.96, -1, -0.0, 0, 1, 2.34, Double.POSITIVE_INFINITY,
        Double.NEGATIVE_INFINITY}) {
      Assertions.assertEquals((int) Math.signum(f), NumberUtils.getSign(f));
    }
  }

  @Test
  public void canTestIsSubnormalFloat() {
    Assertions.assertFalse(NumberUtils.isSubNormal(Float.NaN));
    Assertions.assertFalse(NumberUtils.isSubNormal(Float.POSITIVE_INFINITY));
    Assertions.assertFalse(NumberUtils.isSubNormal(Float.NEGATIVE_INFINITY));
    Assertions.assertFalse(NumberUtils.isSubNormal(Float.MAX_VALUE));
    Assertions.assertFalse(NumberUtils.isSubNormal(1f));
    Assertions.assertFalse(NumberUtils.isSubNormal(Float.MIN_NORMAL));
    Assertions.assertTrue(NumberUtils.isSubNormal(Math.nextDown(Float.MIN_NORMAL)));
    Assertions.assertTrue(NumberUtils.isSubNormal(Float.MIN_VALUE));
    Assertions.assertTrue(NumberUtils.isSubNormal(0f));
  }

  @Test
  public void canTestIsSubnormalDouble() {
    Assertions.assertFalse(NumberUtils.isSubNormal(Double.NaN));
    Assertions.assertFalse(NumberUtils.isSubNormal(Double.POSITIVE_INFINITY));
    Assertions.assertFalse(NumberUtils.isSubNormal(Double.NEGATIVE_INFINITY));
    Assertions.assertFalse(NumberUtils.isSubNormal(Double.MAX_VALUE));
    Assertions.assertFalse(NumberUtils.isSubNormal(1.0));
    Assertions.assertFalse(NumberUtils.isSubNormal(Double.MIN_NORMAL));
    Assertions.assertTrue(NumberUtils.isSubNormal(Math.nextDown(Double.MIN_NORMAL)));
    Assertions.assertTrue(NumberUtils.isSubNormal(Double.MIN_VALUE));
    Assertions.assertTrue(NumberUtils.isSubNormal(0.0));
  }

  @Test
  public void testIsPrime() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> NumberUtils.isPrime(-1));
    Assertions.assertFalse(NumberUtils.isPrime(1), "1 should not be prime");
    Assertions.assertTrue(NumberUtils.isPrime(2), "2 should be prime");
    // Small primes
    for (int prime : new int[] {3, 5, 7, 11, 13, 17}) {
      Assertions.assertTrue(NumberUtils.isPrime(prime), () -> prime + " is prime");
      Assertions.assertFalse(NumberUtils.isPrime(prime + 1), () -> (prime + 1) + " is not prime");
    }
    Assertions.assertFalse(NumberUtils.isPrime(15));
    // Bigger primes. Must not be prime 2 above the value.
    // Require a value above 211^2 = 44521
    for (int prime : new int[] {883, 1777, 3313, 46147}) {
      Assertions.assertTrue(NumberUtils.isPrime(prime), () -> prime + " is prime");
      Assertions.assertFalse(NumberUtils.isPrime(prime + 2), () -> (prime + 2) + " is not prime");
    }
    Assertions.assertFalse(NumberUtils.isPrime(211 * 211));
    Assertions.assertFalse(NumberUtils.isPrime(211 * 213));
  }

  @Test
  public void testUlps() {
    Assertions.assertEquals(0, NumberUtils.ulps(4.56, 4.56));
    Assertions.assertEquals(1, NumberUtils.ulps(4.56, Math.nextUp(4.56)));
    Assertions.assertEquals(0, NumberUtils.ulps(0.0, 0.0));
    Assertions.assertEquals(1, NumberUtils.ulps(-0.0, 0.0));
    Assertions.assertEquals(1, NumberUtils.ulps(0.0, -0.0));
    Assertions.assertEquals(2, NumberUtils.ulps(0.0, -Double.MIN_VALUE));
    Assertions.assertEquals(3, NumberUtils.ulps(Double.MIN_VALUE, -Double.MIN_VALUE));
    Assertions.assertEquals(Long.MAX_VALUE,
        NumberUtils.ulps(Double.MAX_VALUE, Double.POSITIVE_INFINITY));
    Assertions.assertEquals(Long.MAX_VALUE,
        NumberUtils.ulps(Double.POSITIVE_INFINITY, Double.MAX_VALUE));
    Assertions.assertEquals(Long.MAX_VALUE, NumberUtils.ulps(Double.MAX_VALUE, -Double.MAX_VALUE));
    Assertions.assertEquals(Long.MAX_VALUE, NumberUtils.ulps(Double.NaN, Double.POSITIVE_INFINITY));
    Assertions.assertEquals(Long.MAX_VALUE, NumberUtils.ulps(Double.NEGATIVE_INFINITY, Double.NaN));
    Assertions.assertEquals(Long.MAX_VALUE,
        NumberUtils.ulps(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
    Assertions.assertEquals(0,
        NumberUtils.ulps(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
    Assertions.assertEquals(0, NumberUtils.ulps(Double.NaN, Double.NaN));
    // Different NaNs
    final double a = Double.longBitsToDouble(0x7ff0000000000001L);
    final double b = Double.longBitsToDouble(0x7ff0000000000002L);
    Assertions.assertEquals(0, NumberUtils.ulps(a, b));
    // Test exponents are not merged to the maximum
    Assertions.assertEquals(
        Double.doubleToRawLongBits(Double.MAX_VALUE)
            - Double.doubleToRawLongBits(Double.MIN_NORMAL),
        NumberUtils.ulps(Double.MAX_VALUE, Double.MIN_NORMAL));
  }
}
