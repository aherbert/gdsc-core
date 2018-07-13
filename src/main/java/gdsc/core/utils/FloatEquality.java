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

/**
 * Provides equality functions for floating point numbers
 * <p>
 * Adapted from http://www.cygnus-software.com/papers/comparingfloats/comparingfloats.htm
 */
public class FloatEquality
{
	/** The default relative error */
	public static final float RELATIVE_ERROR = 1e-2f;
	/** The default absolute error */
	public static final float ABSOLUTE_ERROR = 1e-10f;

	private float maxRelativeError;
	private float maxAbsoluteError;

	/**
	 * Instantiates a new float equality.
	 */
	public FloatEquality()
	{
		this(RELATIVE_ERROR, ABSOLUTE_ERROR);
	}

	/**
	 * Instantiates a new float equality.
	 *
	 * @param maxRelativeError
	 *            The relative error allowed between the numbers
	 * @param maxAbsoluteError
	 *            The absolute error allowed between the numbers. Should be a small number (e.g. 1e-10)
	 */
	public FloatEquality(float maxRelativeError, float maxAbsoluteError)
	{
		setMaxRelativeError(maxRelativeError);
		setMaxAbsoluteError(maxAbsoluteError);
	}

	/**
	 * Instantiates a new float equality.
	 *
	 * @param maxRelativeError
	 *            The relative error allowed between the numbers
	 * @param maxAbsoluteError
	 *            The absolute error allowed between the numbers. Should be a small number (e.g. 1e-10)
	 * @param significantDigits
	 *            the significant digits
	 * @deprecated The significant digits are ignored
	 */
	@Deprecated
	public FloatEquality(float maxRelativeError, float maxAbsoluteError, int significantDigits)
	{
		this(maxRelativeError, maxAbsoluteError);
	}

	/**
	 * Instantiates a new float equality.
	 *
	 * @param significantDigits
	 *            the significant digits
	 * @param maxAbsoluteError
	 *            The absolute error allowed between the numbers. Should be a small number (e.g. 1e-10)
	 * @see #getMaxRelativeError(int)
	 * @deprecated The significant digits are used to set the max relative error as 1e^-(n-1), e.g. 3sd => 1e-2
	 */
	@Deprecated
	public FloatEquality(int significantDigits, float maxAbsoluteError)
	{
		setSignificantDigits(significantDigits);
		setMaxAbsoluteError(maxAbsoluteError);
	}

	/**
	 * Compares two floats are within the configured errors.
	 *
	 * @param A
	 *            the first value
	 * @param B
	 *            the second value
	 * @return True if equal
	 */
	public boolean almostEqualRelativeOrAbsolute(float A, float B)
	{
		return almostEqualRelativeOrAbsolute(A, B, maxRelativeError, maxAbsoluteError);
	}

	/**
	 * Compares two floats are within the configured number of bits variation using int comparisons.
	 *
	 * @param A
	 *            the first value
	 * @param B
	 *            the second value
	 * @return True if equal
	 * @deprecated This method now calls {@link #almostEqualRelativeOrAbsolute(float, float)}
	 */
	@Deprecated
	public boolean almostEqualComplement(float A, float B)
	{
		return almostEqualRelativeOrAbsolute(A, B);
	}

	/**
	 * Compare complement.
	 *
	 * @param A
	 *            the first value
	 * @param B
	 *            the second value
	 * @return the int
	 * @deprecated This method now converts the relative error to significant digits and then to ULPs for complement
	 *             comparison
	 */
	@Deprecated
	public int compareComplement(float A, float B)
	{
		// Convert the relative error back to significant digits, then to ULPs
		final int maxUlps = getUlps((int) Math.round(1 - Math.log(maxRelativeError)));
		return compareComplement(A, B, maxUlps);
	}

	/**
	 * Compares two float arrays are within the configured errors.
	 *
	 * @param A
	 *            the first value
	 * @param B
	 *            the second value
	 * @return True if equal
	 */
	public boolean almostEqualRelativeOrAbsolute(float[] A, float[] B)
	{
		for (int i = 0; i < A.length; i++)
			if (!almostEqualRelativeOrAbsolute(A[i], B[i], maxRelativeError, maxAbsoluteError))
				return false;
		return true;
	}

	/**
	 * This method now calls {@link #almostEqualRelativeOrAbsolute(float[], float[])}
	 *
	 * @param A
	 *            the first value
	 * @param B
	 *            the second value
	 * @return true, if successful
	 * @deprecated This method now calls {@link #almostEqualRelativeOrAbsolute(float[], float[])}
	 */
	@Deprecated
	public boolean almostEqualComplement(float[] A, float[] B)
	{
		return almostEqualRelativeOrAbsolute(A, B);
	}

	/**
	 * Compares two floats are within the specified errors.
	 *
	 * @param A
	 *            the first value
	 * @param B
	 *            the second value
	 * @param maxRelativeError
	 *            The relative error allowed between the numbers
	 * @param maxAbsoluteError
	 *            The absolute error allowed between the numbers. Should be a small number (e.g. 1e-10)
	 * @return True if equal
	 */
	public static boolean almostEqualRelativeOrAbsolute(float A, float B, float maxRelativeError,
			float maxAbsoluteError)
	{
		// Check the two numbers are within an absolute distance.
		final float difference = Math.abs(A - B);
		if (difference <= maxAbsoluteError)
			return true;
		// Ignore NaNs. This is OK since if either number is a NaN the difference
		// will be NaN and we end up returning false
		final float size = max(Math.abs(A), Math.abs(B));
		if (difference <= size * maxRelativeError)
			return true;
		return false;
	}

	/**
	 * Get the max.
	 *
	 * @param a
	 *            the first value
	 * @param b
	 *            the second value
	 * @return the max
	 */
	private static float max(float a, float b)
	{
		return (a >= b) ? a : b;
	}

	/**
	 * Compute the relative error between two floats.
	 *
	 * @param A
	 *            the first value
	 * @param B
	 *            the second value
	 * @return The relative error
	 */
	public static float relativeError(float A, float B)
	{
		final float diff = A - B;
		if (diff == 0)
			return 0;
		if (Math.abs(B) > Math.abs(A))
			return Math.abs(diff / B);
		else
			return Math.abs(diff / A);
	}

	/**
	 * Compute the maximum relative error between two float arrays.
	 *
	 * @param A
	 *            the first value
	 * @param B
	 *            the second value
	 * @return The relative error
	 */
	public static float relativeError(float[] A, float[] B)
	{
		float max = 0;
		for (int i = 0; i < A.length; i++)
			max = Math.max(max, relativeError(A[i], B[i]));
		return max;
	}

	/**
	 * Compares two floats are within the specified number of bits variation using int comparisons.
	 *
	 * @param A
	 *            the first value
	 * @param B
	 *            the second value
	 * @param maxUlps
	 *            How many representable floats we are willing to accept between A and B
	 * @param maxAbsoluteError
	 *            The absolute error allowed between the numbers. Should be a small number (e.g. 1e-10)
	 * @return True if equal
	 */
	public static boolean almostEqualComplement(float A, float B, int maxUlps, float maxAbsoluteError)
	{
		// Make sure maxUlps is non-negative and small enough that the
		// default NAN won't compare as equal to anything.
		//assert (maxUlps > 0 && maxUlps < 4 * 1024 * 1024);

		if (Math.abs(A - B) < maxAbsoluteError)
			return true;
		if (complement(A, B) <= maxUlps)
			return true;
		return false;
	}

	/**
	 * Compares two floats within the specified number of bits variation using int comparisons.
	 *
	 * @param A
	 *            the first value
	 * @param B
	 *            the second value
	 * @param maxUlps
	 *            How many representable floats we are willing to accept between A and B
	 * @return -1, 0 or 1
	 */
	public static int compareComplement(float A, float B, int maxUlps)
	{
		final int c = signedComplement(A, B);
		if (c < -maxUlps)
			return -1;
		if (c > maxUlps)
			return 1;
		return 0;
	}

	/**
	 * Sets the max relative error.
	 *
	 * @param maxRelativeError
	 *            the maxRelativeError to set
	 */
	public void setMaxRelativeError(float maxRelativeError)
	{
		this.maxRelativeError = maxRelativeError;
	}

	/**
	 * Gets the max relative error.
	 *
	 * @return the maxRelativeError
	 */
	public float getMaxRelativeError()
	{
		return maxRelativeError;
	}

	/**
	 * Sets the max absolute error.
	 *
	 * @param maxAbsoluteError
	 *            the maxAbsoluteError to set
	 */
	public void setMaxAbsoluteError(float maxAbsoluteError)
	{
		this.maxAbsoluteError = maxAbsoluteError;
	}

	/**
	 * Gets the max absolute error.
	 *
	 * @return the maxAbsoluteError
	 */
	public float getMaxAbsoluteError()
	{
		return maxAbsoluteError;
	}

	/**
	 * Ignored. ULP comparison is no longer supported.
	 *
	 * @param maxUlps
	 *            the new max ulps
	 * @deprecated ULP comparison is no longer supported
	 */
	@Deprecated
	public void setMaxUlps(int maxUlps)
	{ // Ignore
	}

	/**
	 * Ignored. ULP comparison is no longer supported.
	 *
	 * @return 0
	 * @deprecated ULP comparison is no longer supported
	 */
	@Deprecated
	public int getMaxUlps()
	{
		return 0;
	}

	// The following methods are different between the FloatEquality and FloatEquality class

	private static float[] RELATIVE_ERROR_TABLE;
	static
	{
		final int precision = new BigDecimal(Float.toString(Float.MAX_VALUE)).precision();
		RELATIVE_ERROR_TABLE = new float[precision];
		for (int p = 0; p < precision; p++)
			RELATIVE_ERROR_TABLE[p] = Float.parseFloat("1e-" + p);
	}

	/**
	 * Get the maximum relative error in terms of the number of decimal significant digits that will be compared between
	 * two real values, e.g. the relative error to use for equality testing at approximately n significant digits.
	 * <p>
	 * Note that the relative error term is just 1e^-(n-1). This method is to provide backward support for equality
	 * testing when the significant digits term was used to generate an approximate ULP (Unit of Least Precision) value
	 * for direct float comparisons using the complement.
	 * <p>
	 * If significant digits is below 1 or above the precision of the float datatype then zero is returned.
	 *
	 * @param significantDigits
	 *            The number of significant digits for comparisons
	 * @return the max relative error
	 */
	public static float getMaxRelativeError(int significantDigits)
	{
		if (significantDigits < 1 || significantDigits > RELATIVE_ERROR_TABLE.length)
			return 0;
		return RELATIVE_ERROR_TABLE[significantDigits - 1];
	}

	/**
	 * Gets the supported max significant digits.
	 *
	 * @return the max significant digits
	 */
	public static int getMaxSignificantDigits()
	{
		return RELATIVE_ERROR_TABLE.length;
	}

	/**
	 * Set the maximum relative error in terms of the number of decimal significant digits
	 *
	 * @param significantDigits
	 *            The number of significant digits for comparisons
	 * @deprecated The significant digits are used to set the max relative error as 1e^-(n-1), e.g. 3sd => 1e-2
	 */
	@Deprecated
	public void setSignificantDigits(int significantDigits)
	{
		setMaxRelativeError(getMaxRelativeError(significantDigits));
	}

	/**
	 * Compute the number of representable doubles until a difference in significant digits. This is only approximate
	 * since the ULP depend on the doubles being compared.
	 * <p>
	 * The number of doubles are computed between Math.power(10, sig-1) and 1 + Math.power(10, sig-1)
	 *
	 * @param significantDigits
	 *            The significant digits
	 * @return The number of representable doubles (Units in the Last Place)
	 */
	public static int getUlps(int significantDigits)
	{
		final int value1 = (int) Math.pow(10.0, significantDigits - 1);
		final int value2 = value1 + 1;
		final int ulps = Float.floatToRawIntBits(value2) - Float.floatToRawIntBits(value1);
		return (ulps < 0) ? 0 : ulps;
	}

	/**
	 * Compute the number of bits variation using integer comparisons.
	 * <p>
	 * If the number is too large to fit in a int then Integer.MAX_VALUE is returned.
	 *
	 * @param A
	 *            the first value
	 * @param B
	 *            the second value
	 * @return How many representable floats we are between A and B
	 */
	public static int complement(float A, float B)
	{
		int aInt = Float.floatToRawIntBits(A);
		int bInt = Float.floatToRawIntBits(B);
		if (((aInt ^ bInt) & 0x80000000) == 0)
			// Same sign
			return Math.abs(aInt - bInt);
		if (aInt < 0)
		{
			// Make aInt lexicographically ordered as a twos-complement int
			aInt = 0x80000000 - aInt;
			return difference(bInt, aInt);
		}
		else
		{
			// Make bInt lexicographically ordered as a twos-complement int
			bInt = 0x80000000 - bInt;
			return difference(aInt, bInt);
		}
	}

	private static int difference(int high, int low)
	{
		final int d = high - low;
		// Check for over-flow
		return (d < 0) ? Integer.MAX_VALUE : d;
	}

	/**
	 * Compute the number of bits variation using int comparisons.
	 * <p>
	 * If the number is too large to fit in a int then Integer.MIN_VALUE/MAX_VALUE is returned depending on the sign.
	 *
	 * @param A
	 *            the first value
	 * @param B
	 *            the second value
	 * @return How many representable floats we are between A and B
	 */
	public static int signedComplement(float A, float B)
	{
		int aInt = Float.floatToRawIntBits(A);
		int bInt = Float.floatToRawIntBits(B);
		if (((aInt ^ bInt) & 0x80000000) == 0)
			// Same sign - no overflow
			return aInt - bInt;
		if (aInt < 0)
		{
			// Make aInt lexicographically ordered as a twos-complement int
			aInt = 0x80000000 - aInt;
			final int d = aInt - bInt;
			// Check for over-flow. We know a is negative and b positive
			return (d > 0) ? Integer.MIN_VALUE : d;
		}
		else
		{
			// Make bInt lexicographically ordered as a twos-complement int
			bInt = 0x80000000 - bInt;
			final int d = aInt - bInt;
			// Check for over-flow. We know a is positive and b negative
			return (d < 0) ? Integer.MAX_VALUE : d;
		}
	}

}
