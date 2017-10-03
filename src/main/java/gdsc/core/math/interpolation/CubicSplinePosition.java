package gdsc.core.math.interpolation;

import org.apache.commons.math3.exception.OutOfRangeException;

/*----------------------------------------------------------------------------- 
 * GDSC Software
 * 
 * Copyright (C) 2017 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

/**
 * Contains the cubic spline position for a value within the interpolation range. Used to pre-compute values to evaluate
 * the spline value.
 */
public class CubicSplinePosition
{
	/** The power of the value (1, 2, 3) */
	final double[] p = new double[3];

	/**
	 * Instantiates a new cubic spline position. Only used when x is known to be in the range 0-1.
	 *
	 * @param x
	 *            the x
	 * @param dummy
	 *            the dummy flag
	 */
	CubicSplinePosition(double x, boolean dummy)
	{
		createPowers(x);
	}

	private void createPowers(double x)
	{
		final double x2 = x * x;
		p[0] = x;
		p[1] = x2;
		p[2] = x2 * x;
	}

	/**
	 * Instantiates a new spline position.
	 *
	 * @param x
	 *            the distance along the spline to the next node (range 0 to 1)
	 * @throws IllegalArgumentException
	 *             If the index is negative
	 * @throws OutOfRangeException
	 *             If x is not in the range 0 to 1
	 */
	public CubicSplinePosition(double x) throws IllegalArgumentException, OutOfRangeException
	{
		// Use negation to catch NaN
		if (!(x >= 0 && x <= 1))
			throw new OutOfRangeException(x, 0, 1);
		createPowers(x);
	}

	/**
	 * Gets the power of the value (x^n).
	 *
	 * @param n
	 *            the power
	 * @return the power of the value
	 */
	public double getPower(int n)
	{
		return (n == 0) ? 1.0 : p[n - 1];
	}

	/**
	 * Gets x.
	 *
	 * @return x
	 */
	public double getX()
	{
		return p[0];
	}

	/**
	 * Gets x^2.
	 *
	 * @return x^2
	 */
	public double getX2()
	{
		return p[1];
	}

	/**
	 * Gets x^3.
	 *
	 * @return x^3
	 */
	public double getX3()
	{
		return p[2];
	}

	/**
	 * Scale the x value back to the original scale. This can be used if a function axis was compressed to the interval
	 * 0-1 for cubic interpolation to decompress back to the original scale.
	 *
	 * @param x
	 *            the x
	 * @return the scaled x value
	 */
	public double scale(double x)
	{
		return x;
	}

	/**
	 * Scale the first-order gradient back to the original scale. This can be used if a function axis was compressed to
	 * the interval 0-1 for cubic interpolation to appropriately scale the gradients (partial derivatives).
	 *
	 * @param x
	 *            the x
	 * @return the scaled x value
	 */
	public double scaleGradient(double df_dx)
	{
		return df_dx;
	}

	/**
	 * Scale the second-order gradient back to the original scale. This can be used if a function axis was compressed to
	 * the interval 0-1 for cubic interpolation to appropriately scale the gradients (partial derivatives).
	 *
	 * @param x
	 *            the x
	 * @return the scaled x value
	 */
	public double scaleGradient2(double d2f_dx2)
	{
		return d2f_dx2;
	}
}