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
 * Adapted from http://www.cygnus-software.com/papers/comparingdoubles/comparingdoubles.htm
 */
public class DoubleEquality
{
	/** The default relative error */
	public static final double RELATIVE_ERROR = 1e-2;
	/** The default absolute error */
	public static final double ABSOLUTE_ERROR = 1e-10;

	private double maxRelativeError;
	private double maxAbsoluteError;

	/**
	 * Default constructor
	 */
	public DoubleEquality()
	{
		this(RELATIVE_ERROR, ABSOLUTE_ERROR);
	}

	/**
	 * Override constructor
	 * 
	 * @param maxRelativeError
	 * @param maxAbsoluteError
	 */
	public DoubleEquality(double maxRelativeError, double maxAbsoluteError)
	{
		setMaxRelativeError(maxRelativeError);
		setMaxAbsoluteError(maxAbsoluteError);
	}

	/**
	 * @deprecated The significant digits are ignored
	 */
	@Deprecated
	public DoubleEquality(double maxRelativeError, double maxAbsoluteError, long significantDigits)
	{
		this(maxRelativeError, maxAbsoluteError);
	}

	/**
	 * Override constructor
	 * 
	 * @param significantDigits
	 * @param maxAbsoluteError
	 * @deprecated The significant digits are used to set the max relative error as 1e^-(n-1), e.g. 3sd => 1e-2
	 * @see #getMaxRelativeError(int)
	 */
	@Deprecated
	public DoubleEquality(long significantDigits, double maxAbsoluteError)
	{
		setSignificantDigits(significantDigits);
		setMaxAbsoluteError(maxAbsoluteError);
	}

	/**
	 * Compares two doubles are within the configured errors.
	 * 
	 * @param A
	 * @param B
	 * @return True if equal
	 */
	public boolean almostEqualRelativeOrAbsolute(double A, double B)
	{
		return almostEqualRelativeOrAbsolute(A, B, maxRelativeError, maxAbsoluteError);
	}

	/**
	 * Compares two doubles are within the configured number of bits variation using long comparisons.
	 * 
	 * @param A
	 * @param B
	 * @return True if equal
	 * @deprecated This method now calls {@link #almostEqualRelativeOrAbsolute(double, double)}
	 */
	@Deprecated
	public boolean almostEqualComplement(double A, double B)
	{
		return almostEqualRelativeOrAbsolute(A, B);
	}

	/**
	 * @deprecated This method now converts the relative error to significant digits and then to ULPs for complement
	 *             comparison
	 */
	@Deprecated
	public long compareComplement(double A, double B)
	{
		// Convert the relative error back to significant digits, then to ULPs
		long maxUlps = getUlps(Math.round(1 - Math.log(maxRelativeError)));
		return compareComplement(A, B, maxUlps);
	}

	/**
	 * Compares two double arrays are within the configured errors.
	 * 
	 * @param A
	 * @param B
	 * @return True if equal
	 */
	public boolean almostEqualRelativeOrAbsolute(double[] A, double[] B)
	{
		for (int i = 0; i < A.length; i++)
			if (!almostEqualRelativeOrAbsolute(A[i], B[i], maxRelativeError, maxAbsoluteError))
				return false;
		return true;
	}

	/**
	 * @deprecated This method now calls {@link #almostEqualRelativeOrAbsolute(double[], double[])}
	 */
	@Deprecated
	public boolean almostEqualComplement(double[] A, double[] B)
	{
		return almostEqualRelativeOrAbsolute(A, B);
	}

	/**
	 * Compares two doubles are within the specified errors.
	 * 
	 * @param A
	 * @param B
	 * @param maxRelativeError
	 *            The relative error allowed between the numbers
	 * @param maxAbsoluteError
	 *            The absolute error allowed between the numbers. Should be a small number (e.g. 1e-10)
	 * @return True if equal
	 */
	public static boolean almostEqualRelativeOrAbsolute(double A, double B, double maxRelativeError,
			double maxAbsoluteError)
	{
		// Check the two numbers are within an absolute distance.
		final double difference = Math.abs(A - B);
		if (difference <= maxAbsoluteError)
			return true;
		// Ignore NaNs. This is OK since if either number is a NaN the difference 
		// will be NaN and we end up returning false 
		final double size = max(Math.abs(A), Math.abs(B));
		if (difference <= size * maxRelativeError)
			return true;
		return false;
	}

	private static double max(double a, double b)
	{
		return (a >= b) ? a : b;
	}

	/**
	 * Compute the relative error between two doubles.
	 * 
	 * @param A
	 * @param B
	 * @return The relative error
	 */
	public static double relativeError(double A, double B)
	{
		final double diff = A - B;
		if (diff == 0)
			return 0;
		if (Math.abs(B) > Math.abs(A))
			return Math.abs(diff / B);
		else
			return Math.abs(diff / A);
	}

	/**
	 * Compares two doubles are within the specified number of bits variation using long comparisons.
	 * 
	 * @param A
	 * @param B
	 * @param maxUlps
	 *            How many representable doubles we are willing to accept between A and B
	 * @param maxAbsoluteError
	 *            The absolute error allowed between the numbers. Should be a small number (e.g. 1e-10)
	 * @return True if equal
	 */
	public static boolean almostEqualComplement(double A, double B, long maxUlps, double maxAbsoluteError)
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
	 * Compares two doubles within the specified number of bits variation using long comparisons.
	 * 
	 * @param A
	 * @param B
	 * @param maxUlps
	 *            How many representable doubles we are willing to accept between A and B
	 * @return -1, 0 or 1
	 */
	public static int compareComplement(double A, double B, long maxUlps)
	{
		long c = signedComplement(A, B);
		if (c < -maxUlps)
			return -1;
		if (c > maxUlps)
			return 1;
		return 0;
	}

	/**
	 * @param maxRelativeError
	 *            the maxRelativeError to set
	 */
	public void setMaxRelativeError(double maxRelativeError)
	{
		this.maxRelativeError = maxRelativeError;
	}

	/**
	 * @return the maxRelativeError
	 */
	public double getMaxRelativeError()
	{
		return maxRelativeError;
	}

	/**
	 * @param maxAbsoluteError
	 *            the maxAbsoluteError to set
	 */
	public void setMaxAbsoluteError(double maxAbsoluteError)
	{
		this.maxAbsoluteError = maxAbsoluteError;
	}

	/**
	 * @return the maxAbsoluteError
	 */
	public double getMaxAbsoluteError()
	{
		return maxAbsoluteError;
	}

	/**
	 * @deprecated ULP comparison is no longer supported
	 */
	@Deprecated
	public void setMaxUlps(long maxUlps)
	{
	}

	/**
	 * @deprecated ULP comparison is no longer supported
	 */
	@Deprecated
	public long getMaxUlps()
	{
		return 0;
	}

	// The following methods are different between the FloatEquality and DoubleEquality class

	private static double[] RELATIVE_ERROR_TABLE;
	static
	{
		int precision = new BigDecimal(Double.toString(Double.MAX_VALUE)).precision();
		RELATIVE_ERROR_TABLE = new double[precision];
		for (int p = 0; p < precision; p++)
		{
			RELATIVE_ERROR_TABLE[p] = Double.parseDouble("1e-" + p);
		}
	}

	/**
	 * Get the maximum relative error in terms of the number of decimal significant digits that will be compared between
	 * two real values, e.g. the relative error to use for equality testing at approximately n significant digits.
	 * <p>
	 * Note that the relative error term is just 1e^-(n-1). This method is to provide backward support for equality
	 * testing when the significant digits term was used to generate an approximate ULP (Unit of Least Precision) value
	 * for direct float comparisons using the complement.
	 * <p>
	 * If significant digits is below 1 or above the precision of the double datatype then zero is returned.
	 *
	 * @param significantDigits
	 *            The number of significant digits for comparisons
	 * @return the max relative error
	 */
	public static double getMaxRelativeError(int significantDigits)
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
	public void setSignificantDigits(long significantDigits)
	{
		setMaxRelativeError(getMaxRelativeError((int) significantDigits));
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
	public static long getUlps(long significantDigits)
	{
		long value1 = (long) Math.pow(10.0, significantDigits - 1);
		long value2 = value1 + 1;
		long ulps = Double.doubleToRawLongBits(value2) - Double.doubleToRawLongBits(value1);
		return (ulps < 0) ? 0 : ulps;
	}

	/**
	 * Compute the number of bits variation using long comparisons.
	 * <p>
	 * If the number is too large to fit in a long then Long.MAX_VALUE is returned.
	 * 
	 * @param A
	 * @param B
	 * 
	 * @return How many representable doubles we are between A and B
	 */
	public static long complement(double A, double B)
	{
		long aInt = Double.doubleToRawLongBits(A);
		long bInt = Double.doubleToRawLongBits(B);
		if (((aInt ^ bInt) & 0x8000000000000000L) == 0l)
			// Same sign
			return Math.abs(aInt - bInt);
		if (aInt < 0)
		{
			// Make aInt lexicographically ordered as a twos-complement long
			aInt = 0x8000000000000000L - aInt;
			return difference(bInt, aInt);
		}
		else
		{
			// Make bInt lexicographically ordered as a twos-complement long
			bInt = 0x8000000000000000L - bInt;
			return difference(aInt, bInt);
		}
	}

	private static long difference(long high, long low)
	{
		long d = high - low;
		// Check for over-flow
		return (d < 0) ? Long.MAX_VALUE : d;
	}

	/**
	 * Compute the number of bits variation using long comparisons.
	 * <p>
	 * If the number is too large to fit in a long then Long.MIN_VALUE/MAX_VALUE is returned depending on the sign.
	 * 
	 * @param A
	 * @param B
	 * 
	 * @return How many representable doubles we are between A and B
	 */
	public static long signedComplement(double A, double B)
	{
		long aInt = Double.doubleToRawLongBits(A);
		long bInt = Double.doubleToRawLongBits(B);
		if (((aInt ^ bInt) & 0x8000000000000000L) == 0l)
			// Same sign - no overflow
			return aInt - bInt;
		if (aInt < 0)
		{
			// Make aInt lexicographically ordered as a twos-complement long
			aInt = 0x8000000000000000L - aInt;
			long d = aInt - bInt;
			// Check for over-flow. We know a is negative and b positive
			return (d > 0) ? Long.MIN_VALUE : d;
		}
		else
		{
			// Make bInt lexicographically ordered as a twos-complement long
			bInt = 0x8000000000000000L - bInt;
			long d = aInt - bInt;
			// Check for over-flow. We know a is positive and b negative
			return (d < 0) ? Long.MAX_VALUE : d;
		}
	}
}
