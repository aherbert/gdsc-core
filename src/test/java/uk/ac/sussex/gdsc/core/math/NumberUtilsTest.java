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
  public void testIsPrime() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> NumberUtils.isPrime(-1));
    Assertions.assertFalse(NumberUtils.isPrime(1), "1 should not be prime");
    Assertions.assertTrue(NumberUtils.isPrime(2), "2 should be prime");
    // Small primes
    for (int prime : new int[] {3, 5, 7, 11, 13, 17}) {
      Assertions.assertTrue(NumberUtils.isPrime(prime), () -> prime + " is prime");
      Assertions.assertFalse(NumberUtils.isPrime(prime + 1), () -> (prime + 1) + " is not prime");
    }
    // Bigger primes. Must not be prime 2 above the value
    for (int prime : new int[] {883, 1777, 3313}) {
      Assertions.assertTrue(NumberUtils.isPrime(prime), () -> prime + " is prime");
      Assertions.assertFalse(NumberUtils.isPrime(prime + 2), () -> (prime + 2) + " is not prime");
    }
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
