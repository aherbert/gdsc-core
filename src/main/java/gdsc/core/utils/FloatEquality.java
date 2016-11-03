package gdsc.core.utils;

import org.apache.commons.math3.util.FastMath;

/*----------------------------------------------------------------------------- 
 * GDSC SMLM Software
 * 
 * Copyright (C) 2013 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

/**
 * Provides equality functions for floating point numbers
 * <p>
 * Adapted from http://www.cygnus-software.com/papers/comparingfloats/comparingfloats.htm
 */
public class FloatEquality
{
	private static final float RELATIVE_ERROR = 1e-2f;
	private static final float ABSOLUTE_ERROR = 1e-10f;
	private static final int SIGNIFICANT_DIGITS = 3;
	
	private float maxRelativeError;
	private float maxAbsoluteError;
	private int maxUlps;

	/**
	 * Default constructor
	 */
	public FloatEquality()
	{
		init(RELATIVE_ERROR, ABSOLUTE_ERROR, SIGNIFICANT_DIGITS);
	}

	/**
	 * Override constructor
	 * 
	 * @param maxRelativeError
	 * @param maxAbsoluteError
	 */
	public FloatEquality(float maxRelativeError, float maxAbsoluteError)
	{
		init(maxRelativeError, maxAbsoluteError, SIGNIFICANT_DIGITS);
	}

	/**
	 * Override constructor
	 * 
	 * @param maxRelativeError
	 * @param maxAbsoluteError
	 * @param significantDigits
	 */
	public FloatEquality(float maxRelativeError, float maxAbsoluteError, int significantDigits)
	{
		init(maxRelativeError, maxAbsoluteError, significantDigits);
	}

	/**
	 * Override constructor
	 * 
	 * @param significantDigits
	 * @param maxAbsoluteError
	 */
	public FloatEquality(int significantDigits, float maxAbsoluteError)
	{
		init(RELATIVE_ERROR, maxAbsoluteError, significantDigits);
	}
	
	private void init(float maxRelativeError, float maxAbsoluteError, int significantDigits)
	{
		this.maxRelativeError = maxRelativeError;
		this.maxAbsoluteError = maxAbsoluteError;
		setSignificantDigits(significantDigits);
	}

	/**
	 * Compares two floats are within the configured errors.
	 * 
	 * @param A
	 * @param B
	 * @return True if equal
	 */
	public boolean almostEqualRelativeOrAbsolute(float A, float B)
	{
		return almostEqualRelativeOrAbsolute(A, B, maxRelativeError, maxAbsoluteError);
	}

	/**
	 * Compares two floats are within the configured number of bits variation using integer comparisons.
	 * 
	 * @param A
	 * @param B
	 * @return True if equal
	 */
	public boolean almostEqualComplement(float A, float B)
	{
		return almostEqualComplement(A, B, maxUlps, maxAbsoluteError);
	}

	/**
	 * Compares two floats within the configured number of bits variation using integer comparisons.
	 * 
	 * @param A
	 * @param B
	 * @return -1, 0 or 1
	 */
	public int compareComplement(float A, float B)
	{
		return compareComplement(A, B, maxUlps);
	}

	/**
	 * Compares two float arrays are within the configured errors.
	 * 
	 * @param A
	 * @param B
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
	 * Compares two float arrays are within the configured number of bits variation using integer comparisons.
	 * 
	 * @param A
	 * @param B
	 * @return True if equal
	 */
	public boolean almostEqualComplement(float[] A, float[] B)
	{
		for (int i = 0; i < A.length; i++)
			if (!almostEqualComplement(A[i], B[i], maxUlps, maxAbsoluteError))
				return false;
		return true;
	}
	
	/**
	 * Compares two floats are within the specified errors.
	 * 
	 * @param A
	 * @param B
	 * @param maxRelativeError
	 *            The relative error allowed between the numbers
	 * @param maxAbsoluteError
	 *            The absolute error allowed between the numbers. Should be a small number (e.g. 1e-10)
	 * @return True if equal
	 */
	public static boolean almostEqualRelativeOrAbsolute(float A, float B, float maxRelativeError, float maxAbsoluteError)
	{
		// Check the two numbers are within an absolute distance.
		final float difference = Math.abs(A - B);
		if (difference <= maxAbsoluteError)
			return true;
		final float size = FastMath.max(Math.abs(A), Math.abs(B));
		if (difference <= size * maxRelativeError)
			return true;
		return false;
	}

	/**
	 * Compute the relative error between two floats.
	 * 
	 * @param A
	 * @param B
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
	 * Compares two floats are within the specified number of bits variation using integer comparisons.
	 * 
	 * @param A
	 * @param B
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
	 * Compares two floats within the specified number of bits variation using integer comparisons.
	 * 
	 * @param A
	 * @param B
	 * @param maxUlps
	 *            How many representable floats we are willing to accept between A and B
	 * @return -1, 0 or 1
	 */
	public static int compareComplement(float A, float B, int maxUlps)
	{
		int c = signedComplement(A, B);
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
	public void setMaxRelativeError(float maxRelativeError)
	{
		this.maxRelativeError = maxRelativeError;
	}

	/**
	 * @return the maxRelativeError
	 */
	public float getMaxRelativeError()
	{
		return maxRelativeError;
	}

	/**
	 * @param maxAbsoluteError
	 *            the maxAbsoluteError to set
	 */
	public void setMaxAbsoluteError(float maxAbsoluteError)
	{
		this.maxAbsoluteError = maxAbsoluteError;
	}

	/**
	 * @return the maxAbsoluteError
	 */
	public float getMaxAbsoluteError()
	{
		return maxAbsoluteError;
	}

	/**
	 * @param maxUlps
	 *            the maximum error in terms of Units in the Last Place
	 */
	public void setMaxUlps(int maxUlps)
	{
		this.maxUlps = maxUlps;
	}

	/**
	 * @return the maximum error in terms of Units in the Last Place
	 */
	public int getMaxUlps()
	{
		return maxUlps;
	}

	/**
	 * Set the maximum error in terms of Units in the Last Place using the number of decimal significant digits
	 * 
	 * @param significantDigits The number of significant digits for comparisons
	 */
	public void setSignificantDigits(int significantDigits)
	{
		this.maxUlps = getUlps(significantDigits);
	}

	// The following methods are different between the FloatEquality and DoubleEquality class

	/**
	 * Compute the number of representable floats until a difference in significant digits
	 * <p>
	 * The number of floats are computed between Math.power(10, sig) and 1 + Math.power(10, sig)
	 * 
	 * @param significantDigits
	 *            The significant digits
	 * @return The number of representable floats (Units in the Last Place)
	 */
	public static int getUlps(int significantDigits)
	{
		int value1 = (int)Math.pow(10.0, significantDigits-1);
		int value2 = value1 + 1;
		int ulps = Float.floatToRawIntBits((float)value2) - Float.floatToRawIntBits((float)value1);
		return (ulps < 0) ? 0 : ulps;
	}
	
	/**
	 * Compute the number of bits variation using integer comparisons.
	 * 
	 * @param A
	 * @param B
	 * 
	 * @return How many representable floats we are between A and B
	 */
	public static int complement(float A, float B)
	{
		int aInt = Float.floatToRawIntBits(A);
		// Make aInt lexicographically ordered as a twos-complement int
		if (aInt < 0)
			aInt = 0x80000000 - aInt;
		// Make bInt lexicographically ordered as a twos-complement int
		int bInt = Float.floatToRawIntBits(B);
		if (bInt < 0)
			bInt = 0x80000000 - bInt;
		return Math.abs(aInt - bInt);
	}
	
	/**
	 * Compute the number of bits variation using integer comparisons.
	 * 
	 * @param A
	 * @param B
	 * 
	 * @return How many representable floats we are between A and B
	 */
	public static int signedComplement(float A, float B)
	{
		int aInt = Float.floatToRawIntBits(A);
		// Make aInt lexicographically ordered as a twos-complement int
		if (aInt < 0)
			aInt = 0x80000000 - aInt;
		// Make bInt lexicographically ordered as a twos-complement int
		int bInt = Float.floatToRawIntBits(B);
		if (bInt < 0)
			bInt = 0x80000000 - bInt;
		return aInt - bInt;
	}
}
