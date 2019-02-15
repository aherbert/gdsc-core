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

package uk.ac.sussex.gdsc.core.utils;

/**
 * Provides equality functions for floating point numbers.
 *
 * @see <A
 *      href="https://randomascii.wordpress.com/2012/02/25/comparing-floating-point-numbers-2012-edition/">Comparing
 *      Floating Point Numbers, 2012 Edition</a>
 */
public class FloatEquality {

  /**
   * Contains the relative error for ascending numbers of significant digits using base 10.
   *
   * <p>1e-0, 1e-1, 1e-2, 1e-3, etc.
   */
  private static final float[] RELATIVE_ERROR_TABLE;

  static {
    final int precision = Math.abs((int) Math.floor(Math.log10(Float.MIN_VALUE))) + 1;
    RELATIVE_ERROR_TABLE = new float[precision];
    for (int p = 0; p < precision; p++) {
      RELATIVE_ERROR_TABLE[p] = Float.parseFloat("1e-" + p);
    }
  }

  /** The default relative error. */
  public static final float RELATIVE_ERROR = 1e-2f;
  /** The default absolute error. */
  public static final float ABSOLUTE_ERROR = 1e-10f;

  private float maxRelativeError;
  private float maxAbsoluteError;

  /**
   * Instantiates a new float equality.
   */
  public FloatEquality() {
    this(RELATIVE_ERROR, ABSOLUTE_ERROR);
  }

  /**
   * Instantiates a new float equality.
   *
   * @param maxRelativeError The relative error allowed between the numbers
   * @param maxAbsoluteError The absolute error allowed between the numbers. Should be a small
   *        number (e.g. 1e-10)
   */
  public FloatEquality(float maxRelativeError, float maxAbsoluteError) {
    setMaxRelativeError(maxRelativeError);
    setMaxAbsoluteError(maxAbsoluteError);
  }

  /**
   * Compares two floats are within the configured errors.
   *
   * @param v1 the first value
   * @param v2 the second value
   * @return True if equal
   */
  public boolean almostEqualRelativeOrAbsolute(float v1, float v2) {
    return almostEqualRelativeOrAbsolute(v1, v2, maxRelativeError, maxAbsoluteError);
  }

  /**
   * Compares two float arrays are within the configured errors.
   *
   * @param v1 the first value
   * @param v2 the second value
   * @return True if equal
   */
  public boolean almostEqualRelativeOrAbsolute(float[] v1, float[] v2) {
    for (int i = 0; i < v1.length; i++) {
      if (!almostEqualRelativeOrAbsolute(v1[i], v2[i], maxRelativeError, maxAbsoluteError)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Compares two floats are within the specified errors.
   *
   * @param v1 the first value
   * @param v2 the second value
   * @param maxRelativeError The relative error allowed between the numbers
   * @param maxAbsoluteError The absolute error allowed between the numbers. Should be a small
   *        number (e.g. 1e-10)
   * @return True if equal
   */
  public static boolean almostEqualRelativeOrAbsolute(float v1, float v2, float maxRelativeError,
      float maxAbsoluteError) {
    // Check the two numbers are within an absolute distance.
    final float difference = Math.abs(v1 - v2);
    if (difference <= maxAbsoluteError) {
      return true;
    }
    // Ignore NaNs. This is OK since if either number is a NaN the difference
    // will be NaN and we end up returning false
    final float size = max(Math.abs(v1), Math.abs(v2));
    return (difference <= size * maxRelativeError);
  }

  /**
   * Get the max.
   *
   * @param v1 the first value
   * @param v2 the second value
   * @return the max
   */
  private static float max(float v1, float v2) {
    return (v1 >= v2) ? v1 : v2;
  }

  /**
   * Compute the relative error between two floats.
   *
   * @param v1 the first value
   * @param v2 the second value
   * @return The relative error
   */
  public static float relativeError(float v1, float v2) {
    final float diff = v1 - v2;
    if (diff == 0) {
      return 0;
    }
    if (Math.abs(v2) > Math.abs(v1)) {
      return Math.abs(diff / v2);
    }
    return Math.abs(diff / v1);
  }

  /**
   * Compute the maximum relative error between two float arrays.
   *
   * @param v1 the first value
   * @param v2 the second value
   * @return The relative error
   */
  public static float relativeError(float[] v1, float[] v2) {
    float max = 0;
    for (int i = 0; i < v1.length; i++) {
      max = Math.max(max, relativeError(v1[i], v2[i]));
    }
    return max;
  }

  /**
   * Compares two floats are within the specified number of bits variation using int comparisons.
   *
   * @param v1 the first value
   * @param v2 the second value
   * @param maxUlps How many representable floats we are willing to accept between v1 and v2
   * @param maxAbsoluteError The absolute error allowed between the numbers. Should be a small
   *        number (e.g. 1e-10)
   * @return True if equal
   */
  public static boolean almostEqualComplement(float v1, float v2, int maxUlps,
      float maxAbsoluteError) {
    // Make sure maxUlps is non-negative and small enough that the
    // default NAN won't compare as equal to anything.
    // assert (maxUlps > 0 && maxUlps < (1 << 23)

    if (Math.abs(v1 - v2) < maxAbsoluteError) {
      return true;
    }
    return (complement(v1, v2) <= maxUlps);
  }

  /**
   * Compares two floats within the specified number of bits variation using int comparisons.
   *
   * @param v1 the first value
   * @param v2 the second value
   * @param maxUlps How many representable floats we are willing to accept between v1 and v2
   * @return -1, 0 or 1
   */
  public static int compareComplement(float v1, float v2, int maxUlps) {
    final int c = signedComplement(v1, v2);
    if (c < -maxUlps) {
      return -1;
    }
    if (c > maxUlps) {
      return 1;
    }
    return 0;
  }

  /**
   * Gets the supported max significant digits.
   *
   * @return the max significant digits
   * @see #getRelativeErrorTerm(int)
   */
  public static int getMaxSignificantDigits() {
    return RELATIVE_ERROR_TABLE.length;
  }

  /**
   * Get the relative error in terms of the number of decimal significant digits that will be
   * compared between two real values, e.g. the relative error to use for equality testing at
   * approximately n significant digits.
   *
   * <p>Note that the relative error term is just 1e^-(n-1). This method is to provide backward
   * support for equality testing when the significant digits term was used to generate an
   * approximate ULP (Unit of Least Precision) value for direct float comparisons using the
   * complement.
   *
   * <p>If significant digits is below 1 or above the precision of the float datatype then zero is
   * returned.
   *
   * @param significantDigits The number of significant digits for comparisons
   * @return the max relative error
   * @see #getMaxSignificantDigits()
   */
  public static float getRelativeErrorTerm(int significantDigits) {
    if (significantDigits < 1 || significantDigits > RELATIVE_ERROR_TABLE.length) {
      return 0;
    }
    return RELATIVE_ERROR_TABLE[significantDigits - 1];
  }

  /**
   * Sets the max relative error.
   *
   * @param maxRelativeError the maxRelativeError to set
   */
  public void setMaxRelativeError(float maxRelativeError) {
    this.maxRelativeError = maxRelativeError;
  }

  /**
   * Gets the max relative error.
   *
   * @return the maxRelativeError
   */
  public float getMaxRelativeError() {
    return maxRelativeError;
  }

  /**
   * Sets the max absolute error.
   *
   * @param maxAbsoluteError the maxAbsoluteError to set
   */
  public void setMaxAbsoluteError(float maxAbsoluteError) {
    this.maxAbsoluteError = maxAbsoluteError;
  }

  /**
   * Gets the max absolute error.
   *
   * @return the maxAbsoluteError
   */
  public float getMaxAbsoluteError() {
    return maxAbsoluteError;
  }

  /**
   * Compute the number of representable doubles until a difference in significant digits. This is
   * only approximate since the ULP depend on the doubles being compared.
   *
   * <p>The number of doubles are computed between Math.power(10, sig-1) and 1 + Math.power(10,
   * sig-1)
   *
   * @param significantDigits The significant digits
   * @return The number of representable doubles (Units in the Last Place)
   */
  public static int getUlps(int significantDigits) {
    final int value1 = (int) Math.pow(10.0, significantDigits - 1.0);
    final int value2 = value1 + 1;
    final int ulps = Float.floatToRawIntBits(value2) - Float.floatToRawIntBits(value1);
    return (ulps < 0) ? 0 : ulps;
  }

  /**
   * Compute the number of bits variation using integer comparisons.
   *
   * <p>If the number is too large to fit in a int then Integer.MAX_VALUE is returned.
   *
   * @param v1 the first value
   * @param v2 the second value
   * @return How many representable floats are between v1 and v2
   */
  public static int complement(float v1, float v2) {
    int bits1 = Float.floatToRawIntBits(v1);
    int bits2 = Float.floatToRawIntBits(v2);
    if (((bits1 ^ bits2) & 0x80000000) == 0) {
      // Same sign
      return Math.abs(bits1 - bits2);
    }
    if (bits1 < 0) {
      // Make bits1 lexicographically ordered as a twos-complement int
      bits1 = 0x80000000 - bits1;
      return difference(bits2, bits1);
    }
    // Make bits2 lexicographically ordered as a twos-complement int
    bits2 = 0x80000000 - bits2;
    return difference(bits1, bits2);
  }

  private static int difference(int high, int low) {
    final int d = high - low;
    // Check for over-flow
    return (d < 0) ? Integer.MAX_VALUE : d;
  }

  /**
   * Compute the number of bits variation using int comparisons.
   *
   * <p>If the number is too large to fit in a int then Integer.MIN_VALUE/MAX_VALUE is returned
   * depending on the sign.
   *
   * @param v1 the first value
   * @param v2 the second value
   * @return How many representable floats we are between v1 and v2
   */
  public static int signedComplement(float v1, float v2) {
    int bits1 = Float.floatToRawIntBits(v1);
    int bits2 = Float.floatToRawIntBits(v2);
    if (((bits1 ^ bits2) & 0x80000000) == 0) {
      // Same sign - no overflow
      return bits1 - bits2;
    }
    if (bits1 < 0) {
      // Make bits1 lexicographically ordered as a twos-complement int
      bits1 = 0x80000000 - bits1;
      final int d = bits1 - bits2;
      // Check for over-flow. We know a is negative and b positive
      return (d > 0) ? Integer.MIN_VALUE : d;
    }
    // Make bits2 lexicographically ordered as a twos-complement int
    bits2 = 0x80000000 - bits2;
    final int d = bits1 - bits2;
    // Check for over-flow. We know a is positive and b negative
    return (d < 0) ? Integer.MAX_VALUE : d;
  }
}
