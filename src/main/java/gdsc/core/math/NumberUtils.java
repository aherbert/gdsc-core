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
package gdsc.core.math;

/**
 * Contains methods for number computations
 *
 * @author Alex Herbert
 */
public class NumberUtils
{
	/**
	 * Gets the unsigned exponent. This is in the range 0-255.
	 * <p>
	 * Note that the max value is a special case indicating either: NaN; positive; or negative infinity.
	 *
	 * @param x
	 *            the x
	 * @return the signed exponent
	 */
	public static int getUnsignedExponent(float x)
	{
		final int bits = Float.floatToRawIntBits(x);

		// Note the documentation from Float.intBitsToFloat(int):
		// int s = ((bits >> 31) == 0) ? 1 : -1;
		// int e = ((bits >> 23) & 0xff);
		// int m = (e == 0) ?
		//                 (bits & 0x7fffff) << 1 :
		//                 (bits & 0x7fffff) | 0x800000;
		// Then the floating-point result equals the value of the mathematical
		// expression s x m x 2^(e-150):
		// e-127 is the unbiased exponent. 23 is the mantissa precision
		// = s x m x 2^(e-127-23)

		// Get the unbiased exponent
		return ((bits >> 23) & 0xff);
	}

	/**
	 * Gets the unsigned exponent. This is in the range 0-2047.
	 * <p>
	 * Note that the max value is a special case indicating either: NaN; positive; or negative infinity.
	 *
	 * @param x
	 *            the x
	 * @return the signed exponent
	 */
	public static int getUnsignedExponent(double x)
	{
		final long bits = Double.doubleToRawLongBits(x);

		// Note the documentation from Double.longBitsToDouble(int):
		// int s = ((bits >> 63) == 0) ? 1 : -1;
		// int e = (int)((bits >>> 52) & 0x7ffL);
		// long m = (e == 0) ?
		//                 (bits & 0xfffffffffffffL) << 1 :
		//                 (bits & 0xfffffffffffffL) | 0x10000000000000L;
		// Then the floating-point result equals the value of the mathematical
		// expression s x m x 2^(e-1075):
		// e-1023 is the unbiased exponent. 52 is the mantissa precision
		// = s x m x 2^(e-1023-52)

		// Get the unbiased exponent
		return ((int) ((bits >>> 52) & 0x7ffL));
	}

	/**
	 * Gets the signed exponent. This is in the range -127 to 128.
	 * <p>
	 * Note that the max value is a special case indicating either: NaN; positive; or negative infinity.
	 *
	 * @param x
	 *            the x
	 * @return the signed exponent
	 */
	public static int getSignedExponent(float x)
	{
		return getUnsignedExponent(x) - 127;
	}

	/**
	 * Gets the signed exponent. This is in the range -1023 to 1024.
	 * <p>
	 * Note that the max value is a special case indicating either: NaN; positive; or negative infinity.
	 *
	 * @param x
	 *            the x
	 * @return the signed exponent
	 */
	public static int getSignedExponent(double x)
	{
		return getUnsignedExponent(x) - 1023;
	}

	/**
	 * Gets the mantissa. This is a 23 bit integer. A leading 1 should be added to create a 24-bit integer if the
	 * unbiased exponent is not 0.
	 *
	 * @param x
	 *            the x
	 * @param raw
	 *            Set to true to get the raw mantissa, otherwise add a leading 1 if applicable.
	 * @return the mantissa
	 */
	public static int getMantissa(float x, boolean raw)
	{
		final int bits = Float.floatToRawIntBits(x);

		// Note the documentation from Float.intBitsToFloat(int):
		// int s = ((bits >> 31) == 0) ? 1 : -1;
		// int e = ((bits >> 23) & 0xff);
		// int m = (e == 0) ?
		//                 (bits & 0x7fffff) << 1 :
		//                 (bits & 0x7fffff) | 0x800000;
		// Then the floating-point result equals the value of the mathematical
		// expression s x m x 2^(e-150):
		// e-127 is the unbiased exponent. 23 is the mantissa precision
		// = s x m x 2^(e-127-23)

		// raw mantissa
		final int m = (bits & 0x7fffff);
		if (raw)
			return m;

		final int e = (bits >> 23) & 0xff;

		return (e == 0) ? m : (m | 0x00800000);
	}

	/**
	 * Gets the mantissa. This is a 52 bit integer. A leading 1 should be added to create a 24-bit integer if the
	 * unbiased exponent is not 0.
	 *
	 * @param x
	 *            the x
	 * @param raw
	 *            Set to true to get the raw mantissa, otherwise add a leading 1 if applicable.
	 * @return the mantissa
	 */
	public static long getMantissa(double x, boolean raw)
	{
		final long bits = Double.doubleToRawLongBits(x);

		// Note the documentation from Double.longBitsToDouble(int):
		// int s = ((bits >> 63) == 0) ? 1 : -1;
		// int e = (int)((bits >>> 52) & 0x7ffL);
		// long m = (e == 0) ?
		//                 (bits & 0xfffffffffffffL) << 1 :
		//                 (bits & 0xfffffffffffffL) | 0x10000000000000L;
		// Then the floating-point result equals the value of the mathematical
		// expression s x m x 2^(e-1075):
		// e-1023 is the unbiased exponent. 52 is the mantissa precision
		// = s x m x 2^(e-1023-52)

		// raw mantissa
		final long m = (bits & 0xfffffffffffffL);
		if (raw)
			return m;

		// Get the biased exponent
		final int e = (int) ((bits >>> 52) & 0x7ffL);

		return (e == 0) ? m : (m | 0x10000000000000L);
	}

	/**
	 * Gets the sign. This returns 0 for NaN.
	 *
	 * @param x
	 *            the x
	 * @return the sign
	 */
	public static int getSign(float x)
	{
		if (x < 0f)
			return -1;
		if (x > 0f)
			return 1;
		return 0;
	}

	/**
	 * Gets the sign. This returns 0 for NaN.
	 *
	 * @param x
	 *            the x
	 * @return the sign
	 */
	public static int getSign(double x)
	{
		if (x < 0d)
			return -1;
		if (x > 0d)
			return 1;
		return 0;
	}

	/**
	 * Checks if this is a sub normal number. This will have an unbiased exponent of 0.
	 *
	 * @param x
	 *            the x
	 * @return true, if is sub normal
	 */
	public static boolean isSubNormal(float x)
	{
		return getUnsignedExponent(x) == 0;
	}

	/**
	 * Checks if this is a sub normal number. This will have an unbiased exponent of 0.
	 *
	 * @param x
	 *            the x
	 * @return true, if is sub normal
	 */
	public static boolean isSubNormal(double x)
	{
		return getUnsignedExponent(x) == 0;
	}
}
