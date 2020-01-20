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
 * Copyright (C) 2011 - 2019 Alex Herbert
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

import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

import java.util.Arrays;

/**
 * Contains methods for number computations.
 */
public final class NumberUtils {

  /** The list of primes below 200 excluding 2. */
  private static final int[] primes = {3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59,
      61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157,
      163, 167, 173, 179, 181, 191, 193, 197, 199};
  /** The final prime before 200. */
  private static final int FINAL_PRIME = primes[primes.length - 1];
  /** The next prime after 200. */
  private static final int NEXT_PRIME = 211;

  /** No public construction. */
  private NumberUtils() {}

  /**
   * Gets the unsigned exponent. This is in the range 0-255.
   *
   * <p>Note that the max value is a special case indicating either: NaN; positive; or negative
   * infinity.
   *
   * @param x the x
   * @return the signed exponent
   */
  public static int getUnsignedExponent(float x) {
    final int bits = Float.floatToRawIntBits(x);

    // Note the documentation from Float.intBitsToFloat(int):
    // int s = ((bits >> 31) == 0) ? 1 : -1
    // int e = ((bits >> 23) & 0xff)
    // int m = (e == 0) ?
    // (bits & 0x7fffff) << 1 :
    // (bits & 0x7fffff) | 0x800000
    // Then the floating-point result equals the value of the mathematical
    // expression s x m x 2^(e-150):
    // e-127 is the unbiased exponent. 23 is the mantissa precision
    // = s x m x 2^(e-127-23)

    // Get the unbiased exponent
    return ((bits >> 23) & 0xff);
  }

  /**
   * Gets the unsigned exponent. This is in the range 0-2047.
   *
   * <p>Note that the max value is a special case indicating either: NaN; positive; or negative
   * infinity.
   *
   * @param x the x
   * @return the signed exponent
   */
  public static int getUnsignedExponent(double x) {
    final long bits = Double.doubleToRawLongBits(x);

    // Note the documentation from Double.longBitsToDouble(int):
    // int s = ((bits >> 63) == 0) ? 1 : -1
    // int e = (int)((bits >>> 52) & 0x7ffL)
    // long m = (e == 0) ?
    // (bits & 0xfffffffffffffL) << 1 :
    // (bits & 0xfffffffffffffL) | 0x10000000000000L
    // Then the floating-point result equals the value of the mathematical
    // expression s x m x 2^(e-1075):
    // e-1023 is the unbiased exponent. 52 is the mantissa precision
    // = s x m x 2^(e-1023-52)

    // Get the unbiased exponent
    return ((int) ((bits >>> 52) & 0x7ffL));
  }

  /**
   * Gets the signed exponent. This is in the range -127 to 128.
   *
   * <p>Note that the max value is a special case indicating either: NaN; positive; or negative
   * infinity.
   *
   * @param x the x
   * @return the signed exponent
   */
  public static int getSignedExponent(float x) {
    return getUnsignedExponent(x) - 127;
  }

  /**
   * Gets the signed exponent. This is in the range -1023 to 1024.
   *
   * <p>Note that the max value is a special case indicating either: NaN; positive; or negative
   * infinity.
   *
   * @param x the x
   * @return the signed exponent
   */
  public static int getSignedExponent(double x) {
    return getUnsignedExponent(x) - 1023;
  }

  /**
   * Gets the mantissa. This is a 23 bit integer. A leading 1 should be added to create a 24-bit
   * integer if the unbiased exponent is not 0.
   *
   * @param x the x
   * @param raw Set to true to get the raw mantissa, otherwise add a leading 1 if applicable.
   * @return the mantissa
   */
  public static int getMantissa(float x, boolean raw) {
    final int bits = Float.floatToRawIntBits(x);

    // Note the documentation from Float.intBitsToFloat(int):
    // int s = ((bits >> 31) == 0) ? 1 : -1
    // int e = ((bits >> 23) & 0xff)
    // int m = (e == 0) ?
    // (bits & 0x7fffff) << 1 :
    // (bits & 0x7fffff) | 0x800000
    // Then the floating-point result equals the value of the mathematical
    // expression s x m x 2^(e-150):
    // e-127 is the unbiased exponent. 23 is the mantissa precision
    // = s x m x 2^(e-127-23)

    // raw mantissa
    final int m = (bits & 0x7fffff);
    if (raw) {
      return m;
    }

    final int e = (bits >> 23) & 0xff;

    return (e == 0) ? m : (m | 0x00800000);
  }

  /**
   * Gets the mantissa. This is a 52 bit integer. A leading 1 should be added to create a 24-bit
   * integer if the unbiased exponent is not 0.
   *
   * @param x the x
   * @param raw Set to true to get the raw mantissa, otherwise add a leading 1 if applicable.
   * @return the mantissa
   */
  public static long getMantissa(double x, boolean raw) {
    final long bits = Double.doubleToRawLongBits(x);

    // Note the documentation from Double.longBitsToDouble(int):
    // int s = ((bits >> 63) == 0) ? 1 : -1
    // int e = (int)((bits >>> 52) & 0x7ffL)
    // long m = (e == 0) ?
    // (bits & 0xfffffffffffffL) << 1 :
    // (bits & 0xfffffffffffffL) | 0x10000000000000L
    // Then the floating-point result equals the value of the mathematical
    // expression s x m x 2^(e-1075):
    // e-1023 is the unbiased exponent. 52 is the mantissa precision
    // = s x m x 2^(e-1023-52)

    // raw mantissa
    final long m = (bits & 0xfffffffffffffL);
    if (raw) {
      return m;
    }

    // Get the biased exponent
    final int e = (int) ((bits >>> 52) & 0x7ffL);

    return (e == 0) ? m : (m | 0x10000000000000L);
  }

  /**
   * Gets the sign. This returns 0 for NaN.
   *
   * @param x the x
   * @return the sign
   */
  public static int getSign(float x) {
    if (x < 0) {
      return -1;
    }
    if (x > 0) {
      return 1;
    }
    return 0;
  }

  /**
   * Gets the sign. This returns 0 for NaN.
   *
   * @param x the x
   * @return the sign
   */
  public static int getSign(double x) {
    if (x < 0) {
      return -1;
    }
    if (x > 0) {
      return 1;
    }
    return 0;
  }

  /**
   * Checks if this is a sub normal number. This will have an unbiased exponent of 0.
   *
   * @param x the x
   * @return true, if is sub normal
   */
  public static boolean isSubNormal(float x) {
    return getUnsignedExponent(x) == 0;
  }

  /**
   * Checks if this is a sub normal number. This will have an unbiased exponent of 0.
   *
   * @param x the x
   * @return true, if is sub normal
   */
  public static boolean isSubNormal(double x) {
    return getUnsignedExponent(x) == 0;
  }

  /**
   * Checks the number is prime.
   *
   * @param n the number
   * @return true if prime
   * @see <a href="https://en.wikipedia.org/wiki/Primality_test#Pseudocode">Primality test</a>
   * @throws IllegalArgumentException if n is not strictly positive
   */
  public static boolean isPrime(long n) {
    ValidationUtils.checkStrictlyPositive(n);
    // Special case for 2 which is prime, but 1 is not
    if (n <= 3) {
      return n > 1;
    }
    // Any even number is not prime
    if ((n & 1L) == 0) {
      return false;
    }
    // Test against known primes
    if (n <= FINAL_PRIME) {
      return Arrays.binarySearch(primes, (int) n) >= 0;
    }
    // n must be above the tabulated set of primes so test for divisors
    for (final long prime : primes) {
      if (n % prime == 0) {
        return false;
      }
    }
    // Simple algorithm to test all 6k+/-1 divisors up to sqrt(n)
    long prime = NEXT_PRIME;
    while (prime * prime <= n) {
      if (n % prime == 0 || n % (prime + 2) == 0) {
        return false;
      }
      prime += 6;
    }
    return true;
  }

  /**
   * Compute the units of least precision (ulps) between the two numbers. For convenience the result
   * is clipped to {@value Long#MAX_VALUE} so that large differences are positive.
   *
   * <p>The magnitude of the result is meaningless if one but not both numbers are infinite or nan;
   * or both are infinite or nan but not the same. In this case the method returns
   * {@value Long#MAX_VALUE}. If the numbers are the same inf/nan the result is 0.
   *
   * @param a first number
   * @param b second number
   * @return the ulps
   */
  public static long ulps(double a, double b) {
    // No requirement to collapse all NaNs to a single value so use raw bits
    final long x = Double.doubleToRawLongBits(a);
    final long y = Double.doubleToRawLongBits(b);
    if (x != y) {
      // inf/nan detection. Exponent will have all bits set.
      if ((x & 0x7ff0000000000000L) == 0x7ff0000000000000L
          || (y & 0x7ff0000000000000L) == 0x7ff0000000000000L) {
        // Handle nan/nan here
        return Double.isNaN(a) && Double.isNaN(b) ? 0 : Long.MAX_VALUE;
      }
      if ((x ^ y) < 0L) {
        // Opposite signs. Measure the combined distance to zero.
        // If positive the distance to zero is the number: x
        // If negative the distance to zero is: (x - 0x8000000000000000L)
        // +1 to measure -0.0 to 0.0.
        // x - (y - 0x8000000000000000L) + 1 (when x > y)
        // Due to roll-over we can combine this by measuring the distance
        // of the negative value above the biggest positive number:
        // x + (y - Long.MAX_VALUE)
        final long z = x + y - Long.MAX_VALUE;
        // For convenience do not return negative for large ulps
        return z < 0 ? Long.MAX_VALUE : z;
      }
      return x < y ? y - x : x - y;
    }
    return 0;
  }
}
