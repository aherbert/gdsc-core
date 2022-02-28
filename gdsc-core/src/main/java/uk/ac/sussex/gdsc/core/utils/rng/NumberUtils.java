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

package uk.ac.sussex.gdsc.core.utils.rng;

/**
 * Number utilities.
 */
public final class NumberUtils {
  /**
   * A mask to convert an {@code int} to an unsigned integer stored as a {@code long}.
   */
  private static final long INT_TO_UNSIGNED_BYTE_MASK = 0xffffffffL;
  /** The exception message prefix when a number is not strictly positive. */
  private static final String NOT_STRICTLY_POSITIVE = "Must be strictly positive: ";

  /**
   * No public constructor.
   */
  private NumberUtils() {}

  /**
   * Generates an {@code int} value between 0 (inclusive) and the specified value (exclusive) using
   * a bit source of randomness.
   *
   * <p>This is equivalent to {@code floor(n * u)} where floor rounds down to the nearest integer
   * and {@code u} is a uniform random deviate in the range {@code [0,1)}. The equivalent unsigned
   * integer arithmetic is:
   *
   * <pre>
   * {@code (int) (n * (v & 0xffffffffL) / 2^32)}
   * </pre>
   *
   * <p>Notes:
   *
   * <ul>
   *
   * <li>The sampling is biased unless {@code n} is a power of 2 as values will be represented with
   * a probability of either {@code floor(2^32 / n) * 2^-32} or {@code ceil(2^32 / n) * 2^-32}. This
   * is roughly {@code 1 / n} but with round-off error. As {@code n} increases the effect of
   * round-off error is greater (but the number of possible samples is larger so masking any bias
   * that may be observed).
   *
   * <li>The algorithm uses the most significant bits of the source of randomness to construct the
   * output.
   *
   * </ul>
   *
   * @param value Value to use as a source of randomness.
   * @param n Bound on the random number to be returned. Must be positive.
   * @return a random {@code int} value between 0 (inclusive) and {@code n} (exclusive).
   * @throws IllegalArgumentException if {@code n} is negative.
   */
  public static int makeIntInRange(int value, int n) {
    if (n <= 0) {
      throw new IllegalArgumentException(NOT_STRICTLY_POSITIVE + n);
    }
    // This computes the rounded fraction n * v / 2^32.
    // If v is an unsigned integer in the range 0 to 2^32 -1 then v / 2^32
    // is a uniform deviate in the range [0,1).
    // This is possible using unsigned integer arithmetic with long.
    // A division by 2^32 is a bit shift of 32.
    return (int) ((n * (value & INT_TO_UNSIGNED_BYTE_MASK)) >>> 32);
  }

  /**
   * Generates an {@code long} value between 0 (inclusive) and the specified value (exclusive) using
   * a bit source of randomness.
   *
   * <p>This is equivalent to {@code floor(n * u)} where floor rounds down to the nearest long
   * integer and {@code u} is a uniform random deviate in the range {@code [0,1)}. The equivalent
   * {@link java.math.BigInteger BigInteger} arithmetic is:</p>
   *
   * <pre>
   * <code>
   * // Construct big-endian byte representation from the long
   * byte[] bytes = new byte[8];
   * for(int i = 0; i &lt; 8; i++) {
   *   bytes[7 - i] = (byte)((v &gt;&gt;&gt; (i * 8)) &amp; 0xff);
   * }
   * BigInteger unsignedValue = new BigInteger(1, bytes);
   * return BigInteger.valueOf(n).multiply(unsignedValue).shiftRight(64).longValue();
   * </code>
   * </pre>
   *
   * <p>Notes:
   *
   * <ul>
   *
   * <li>The sampling is biased unless {@code n} is a power of 2 as values will be represented with
   * a probability of either {@code floor(2^64 / n) * 2^-64} or {@code ceil(2^64 / n) * 2^-64}. This
   * is roughly {@code 1 / n} but with round-off error. As {@code n} increases the effect of
   * round-off error is greater (but the number of possible samples is larger so masking any bias
   * that may be observed).
   *
   * <li>The algorithm does not use {@link java.math.BigInteger BigInteger} and is optimised for
   * 128-bit arithmetic.
   *
   * <li>The algorithm uses the most significant bits of the source of randomness to construct the
   * output.
   *
   * </ul>
   *
   * @param value Value to use as a source of randomness.
   * @param n Bound on the random number to be returned. Must be positive.
   * @return a random {@code long} value between 0 (inclusive) and {@code n} (exclusive).
   * @throws IllegalArgumentException if {@code n} is negative.
   */
  public static long makeLongInRange(long value, long n) {
    if (n <= 0) {
      throw new IllegalArgumentException(NOT_STRICTLY_POSITIVE + n);
    }

    // This computes the rounded fraction n * v / 2^64.
    // If v is an unsigned integer in the range 0 to 2^64 -1 then v / 2^64
    // is a uniform deviate in the range [0,1).
    // This computation is possible using the 2s-complement integer arithmetic in Java
    // which is unsigned.
    //
    // Note: This adapts the multiply and carry idea in BigInteger arithmetic.
    // This is based on the following observation about the upper and lower bits of an
    // unsigned big-endian integer:
    // @formatter:off
    //   ab * xy
    // =  b *  y
    // +  b * x0
    // + a0 *  y
    // + a0 * x0
    // = b * y
    // + b * x * 2^32
    // + a * y * 2^32
    // + a * x * 2^64
    // @formatter:on
    //
    // A division by 2^64 is a bit shift of 64. So we must compute the equivalent of the
    // 128-bit results of multiplying two unsigned 64-bit numbers and return only the upper
    // 64-bits.
    final long a = n >>> 32;
    final long b = n & INT_TO_UNSIGNED_BYTE_MASK;
    final long x = value >>> 32;
    if (a == 0) {
      // Fast computation with long.
      // Use the upper bits from the source of randomness so the result is the same
      // as the full computation.
      // Here (b * y) would be discarded when dividing by 2^32.
      return (b * x) >>> 32;
    }
    final long y = value & INT_TO_UNSIGNED_BYTE_MASK;
    if (b == 0) {
      // Fast computation with long.
      // Note: This will catch power of 2 edge cases with large n but ensure the most
      // significant bits are used rather than returning: v & (n - 1)
      // Cannot overflow at the maximum values.
      return ((a * y) >>> 32) + (a * x);
    }

    // Note:
    // This algorithm thus computes the small numbers multiplied together and then sums
    // the carry on to the result for the next power 2^32.
    // This is a diagram of the bit cascade (using a 4 byte representation):
    // @formatter:off
    //             byby byby
    // +      bxbx bxbx 0000
    // +      ayay ayay 0000
    // + axax axax 0000 0000
    // @formatter:on
    //
    // The carry is a composed intermediate which will never overflow:
    //
    //             byby byby
    // +           bxbx 0000
    // +      ayay ayay 0000
    //
    // +      bxbx 0000 0000
    // + axax axax 0000 0000

    final long by = b * y;
    final long bx = b * x;
    final long ay = a * y;
    final long ax = a * x;

    // @formatter:off
    final long carry = (by >>> 32) +
                       (bx & INT_TO_UNSIGNED_BYTE_MASK) +
                        ay;
    // @formatter:on

    return (bx >>> 32) + (carry >>> 32) + ax;
  }

  /**
   * Multiply the two values as if unsigned 64-bit longs to produce a 128-bit unsigned result.
   *
   * @param value1 the first value
   * @param value2 the second value
   * @param result the 128-bit result stored as two 64-bit values
   */
  static void multiply(long value1, long value2, long[] result) {
    // Note: This adapts the multiply and carry idea in BigInteger arithmetic.
    // This is based on the following observation about the upper and lower bits of an
    // unsigned big-endian integer:
    // @formatter:off
    //   ab * xy
    // =  b *  y
    // +  b * x0
    // + a0 *  y
    // + a0 * x0
    // = b * y
    // + b * x * 2^32
    // + a * y * 2^32
    // + a * x * 2^64
    // @formatter:on
    final long a = value1 >>> 32;
    final long b = value1 & INT_TO_UNSIGNED_BYTE_MASK;
    final long x = value2 >>> 32;
    final long y = value2 & INT_TO_UNSIGNED_BYTE_MASK;

    // Note:
    // This algorithm computes the small numbers multiplied together and then sums
    // the carry on to the result for the next power 2^32.
    // This is a diagram of the bit cascade (using a 4 byte representation):
    // @formatter:off
    //             byby byby
    // +      bxbx bxbx 0000
    // +      ayay ayay 0000
    // + axax axax 0000 0000
    //
    // The products are added in parts to create a carry.
    // The result of two unsigned 32-bit integers multiplied together plus two full 32-bit
    // integers cannot overflow 64 bits:
    // > long x = (1L << 32) - 1
    // > Long.toBinaryString(x * x)
    // ==> "1111111111111111111111111111111000000000000000000000000000000001"
    // > Long.toBinaryString(x * x + x + x)
    // ==> "1111111111111111111111111111111111111111111111111111111111111111"
    // The carry is a composed intermediate which will never overflow:
    //
    //             byby byby
    // +           bxbx 0000
    // +      ayay ayay 0000
    //
    // +      bxbx 0000 0000
    // + axax axax 0000 0000
    // @formatter:on

    final long by = b * y;
    final long bx = b * x;
    final long ay = a * y;
    final long ax = a * x;

    // @formatter:off
    final long carry = (by >>> 32) +
                       (bx & INT_TO_UNSIGNED_BYTE_MASK) +
                        ay;
    // @formatter:on

    result[0] = (carry << 32) | (by & INT_TO_UNSIGNED_BYTE_MASK);
    result[1] = (bx >>> 32) + (carry >>> 32) + ax;
  }

  /**
   * Compute the accumulated multiplier and addition for a Linear Congruential Generator (LCG). The
   * base generator advance step is:
   *
   * <pre>
   * x = m * x + c
   * </pre>
   *
   * <p>A number of consecutive steps can be computed in a single multiply and add operation. This
   * method computes the accumulated multiplier and addition for the given number of steps. The
   * steps may be positive or negative for forward or backward.
   *
   * <p>Uses the algorithm from:
   *
   * <blockquote>Brown, F.B. (1994) Random number generation with arbitrary strides, Transactions of
   * the American Nuclear Society 71.</blockquote>
   *
   * @param multiplier the multiplier
   * @param constant the constant
   * @param advanceSteps the number of advance steps
   * @return the accumulated multiplier and addition
   * @see <A href="https://www.osti.gov/biblio/89100-random-number-generation-arbitrary-strides">
   *      Brown, F.B. (1994) Random number generation with arbitrary strides, Transactions of the
   *      American Nuclear Society 71</a>
   */
  public static long[] computeLcgAdvance(final long multiplier, final long constant,
      final long advanceSteps) {
    // Current multiplier and constant
    long currM = multiplier;
    long currC = constant;
    // Accumulated multiplier and constant
    long accM = 1;
    long accC = 0;

    for (long steps = advanceSteps; steps != 0; steps >>>= 1) {
      if ((steps & 1) != 0) {
        // If the bit is set then the current multiplier and constant will be used
        accM *= currM;
        accC = accC * currM + currC;
      }
      // Update the multiplier and constant for the next power of 2
      currC = (currM + 1) * currC;
      currM *= currM;
    }
    return new long[] {accM, accC};
  }

  /**
   * Compute the accumulated multiplier and addition for a Linear Congruential Generator (LCG). The
   * base generator advance step is:
   *
   * <pre>
   * x = m * x + c
   * </pre>
   *
   * <p>A number of consecutive steps can be computed in a single multiply and add operation. This
   * method computes the accumulated multiplier and addition for the given number of steps expressed
   * as a power of 2. Provides support to advance for 2<sup>k</sup> for {@code k in [0, 63)}. Any
   * power {@code >= 64} is ignored as this would wrap the generator to the same point. Negative
   * powers are ignored but do not throw an exception.
   *
   * <p>Based on the algorithm from:
   *
   * <blockquote>Brown, F.B. (1994) Random number generation with arbitrary strides, Transactions of
   * the American Nuclear Society 71.</blockquote>
   *
   * @param multiplier the multiplier
   * @param constant the constant
   * @param advancePowerOf2 the number of advance steps as a power of 2 (range [0, 63])
   * @return the accumulated multiplier and addition
   * @see <A href="https://www.osti.gov/biblio/89100-random-number-generation-arbitrary-strides">
   *      Brown, F.B. (1994) Random number generation with arbitrary strides, Transactions of the
   *      American Nuclear Society 71</a>
   */
  public static long[] computeLcgAdvancePow2(final long multiplier, final long constant,
      final int advancePowerOf2) {
    // If any bits above the first 6 are set then this would wrap the generator to the same point
    // as multiples of period (2^64). It also identifies negative powers to ignore.
    if ((advancePowerOf2 & ~0x3f) != 0) {
      return new long[] {1, 0};
    }

    // Current multiplier and constant
    long currM = multiplier;
    long currC = constant;

    for (int i = advancePowerOf2; i != 0; i--) {
      // Update the multiplier and constant for the next power of 2
      currC = (currM + 1) * currC;
      currM *= currM;
    }
    return new long[] {currM, currC};
  }

  /**
   * Compute the advanced state of a Linear Congruential Generator (LCG). The base generator advance
   * step is:
   *
   * <pre>
   * x = m * x + c
   * </pre>
   *
   * <p>A number of consecutive steps can be computed in a single multiply and add operation. This
   * method computes the accumulated multiplier and addition for the given number of steps. The
   * steps may be positive or negative for forward or backward.
   *
   * <p>Based on the algorithm from:
   *
   * <blockquote>Brown, F.B. (1994) Random number generation with arbitrary strides, Transactions of
   * the American Nuclear Society 71.</blockquote>
   *
   * @param state the state
   * @param multiplier the multiplier
   * @param constant the constant
   * @param advanceSteps the number of advance steps
   * @return the new state
   * @see <A href="https://www.osti.gov/biblio/89100-random-number-generation-arbitrary-strides">
   *      Brown, F.B. (1994) Random number generation with arbitrary strides, Transactions of the
   *      American Nuclear Society 71</a>
   */
  public static long lcgAdvance(final long state, final long multiplier, final long constant,
      final long advanceSteps) {
    // Current multiplier and constant
    long currM = multiplier;
    long currC = constant;
    // Accumulated multiplier and constant
    long accM = 1;
    long accC = 0;

    for (long steps = advanceSteps; steps != 0; steps >>>= 1) {
      if ((steps & 1) != 0) {
        // If the bit is set then the current multiplier and constant will be used
        accM *= currM;
        accC = accC * currM + currC;
      }
      // Update the multiplier and constant for the next power of 2
      currC = (currM + 1) * currC;
      currM *= currM;
    }
    return state * accM + accC;
  }

  /**
   * Compute the advanced state of a Linear Congruential Generator (LCG). The base generator advance
   * step is:
   *
   * <pre>
   * x = m * x + c
   * </pre>
   *
   * <p>A number of consecutive steps can be computed in a single multiply and add operation. This
   * method computes the accumulated multiplier and addition for the given number of steps expressed
   * as a power of 2. Provides support to advance for 2<sup>k</sup> for {@code k in [0, 63)}. Any
   * power {@code >= 64} is ignored as this would wrap the generator to the same point. Negative
   * powers are ignored but do not throw an exception.
   *
   * <p>Based on the algorithm from:
   *
   * <blockquote>Brown, F.B. (1994) Random number generation with arbitrary strides, Transactions of
   * the American Nuclear Society 71.</blockquote>
   *
   * @param state the state
   * @param multiplier the multiplier
   * @param constant the constant
   * @param advancePowerOf2 the number of advance steps as a power of 2 (range [0, 63])
   * @return the new state
   * @see <A href="https://www.osti.gov/biblio/89100-random-number-generation-arbitrary-strides">
   *      Brown, F.B. (1994) Random number generation with arbitrary strides, Transactions of the
   *      American Nuclear Society 71</a>
   */
  public static long lcgAdvancePow2(final long state, final long multiplier, final long constant,
      final int advancePowerOf2) {
    // If any bits above the first 6 are set then this would wrap the generator to the same point
    // as multiples of period (2^64). It also identifies negative powers to ignore.
    if ((advancePowerOf2 & ~0x3f) != 0) {
      return state;
    }

    // Current multiplier and constant
    long currM = multiplier;
    long currC = constant;

    for (int i = advancePowerOf2; i != 0; i--) {
      // Update the multiplier and constant for the next power of 2
      currC = (currM + 1) * currC;
      currM *= currM;
    }
    return state * currM + currC;
  }

  /**
   * Creates a {@code double} from two {@code int} values.
   *
   * @param highBits high order bits.
   * @param lowBit low order bits.
   * @return a {@code double} value in the interval {@code [0, 1]}.
   */
  public static double makeDouble(int highBits, int lowBit) {
    // Require the least significant 53-bits from a long.
    // Join the most significant 26 from v with 27 from w.
    final long high = ((long) (highBits >>> 6)) << 27;
    final int low = lowBit >>> 5;
    return (high | low) * 0x1.0p-53d;
  }

  /**
   * Creates a signed double in the range {@code [-1, 1)}. The magnitude is sampled evenly from the
   * 2<sup>54</sup> dyadic rationals in the range.
   *
   * <p>Note: This method will not return samples for both -0.0 and 0.0.
   *
   * @param bits the bits
   * @return the double
   */
  public static double makeSignedDouble(long bits) {
    // Use the upper 54 bits on the assumption they are more random.
    // The sign bit is maintained by the signed shift.
    // The next 53 bits generates a magnitude in the range [0, 2^53) or [-2^53, 0).
    return (bits >> 10) * 0x1.0p-53d;
  }

  /**
   * Creates a normalised double in the range {@code [1, 2)}. The magnitude is sampled evenly from
   * the 2<sup>52</sup> dyadic rationals in the range.
   *
   * @param bits the bits
   * @return the double
   */
  public static double makeNormalDouble(long bits) {
    // Combine an unbiased exponent of 0 with the 52 bit mantissa
    return Double.longBitsToDouble((1023L << 52) | (bits >>> 12));
  }

  /**
   * Compute the multiplicative inverse of an integer. Given {@code x} compute {@code y} such that
   * {@code x * y = y * x = 1}.
   *
   * @param value the value (must be odd)
   * @return the multiplicative inverse
   * @see <a
   *      href="https://lemire.me/blog/2017/09/18/computing-the-inverse-of-odd-integers/">Computing
   *      the inverse of odd integers</a>
   * @throws IllegalArgumentException If {@code x} is not odd
   */
  public static long computeInverse(long value) {
    if ((value & 0x1L) == 0) {
      throw new IllegalArgumentException("value must be odd: " + value);
    }
    // Initial estimate with 5-bits of accuracy
    long inverse = (3 * value) ^ 2L;
    inverse = f64(value, inverse);
    inverse = f64(value, inverse);
    inverse = f64(value, inverse);
    inverse = f64(value, inverse);
    return inverse;
  }

  /**
   * Compute the multiplicative inverse of an integer. Given {@code x} compute {@code y} such that
   * {@code x * y = y * x = 1}.
   *
   * @param value the value (must be odd)
   * @return the multiplicative inverse
   * @see <a
   *      href="https://lemire.me/blog/2017/09/18/computing-the-inverse-of-odd-integers/">Computing
   *      the inverse of odd integers</a>
   * @throws IllegalArgumentException If {@code x} is not odd
   */
  public static int computeInverse(int value) {
    if ((value & 0x1) == 0) {
      throw new IllegalArgumentException("value must be odd: " + value);
    }
    // Initial estimate with 5-bits of accuracy
    int inverse = (3 * value) ^ 2;
    inverse = f32(value, inverse);
    inverse = f32(value, inverse);
    inverse = f32(value, inverse);
    return inverse;
  }

  /**
   * Implementation of Newton's method as one would apply it to finding the zero of g(y) = 1/y - x.
   *
   * @param x the x
   * @param y the y
   * @return the new estimate for y
   */
  private static long f64(long x, long y) {
    return y * (2 - y * x);
  }

  /**
   * Implementation of Newton's method as one would apply it to finding the zero of g(y) = 1/y - x.
   *
   * @param x the x
   * @param y the y
   * @return the new estimate for y
   */
  private static int f32(int x, int y) {
    return y * (2 - y * x);
  }
}
