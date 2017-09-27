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
	/** The index of the spline node */
	final int index;

	/** The power of the value (0, 1, 2, 3) */
	double[] p = new double[4];

	CubicSplinePosition(int index, double x, boolean dummy)
	{
		this.index = index;
		createPowers(x);
	}

	private void createPowers(double x)
	{
		final double x2 = x * x;
		p[0] = 1;
		p[1] = x;
		p[2] = x2;
		p[3] = x2 * x;
	}

	/**
	 * Instantiates a new spline position.
	 *
	 * @param index
	 *            the index
	 * @param x
	 *            the distance along the spline to the next node (range 0 to 1)
	 * @throws IllegalArgumentException
	 *             If the index is negative
	 * @throws OutOfRangeException
	 *             If x is not in the range 0 to 1
	 */
	public CubicSplinePosition(int index, double x) throws IllegalArgumentException, OutOfRangeException
	{
		// If the user creates a spline position then we should check it is valid
		if (index < 0)
			throw new IllegalArgumentException("Index must be positive");
		if (x < 0 || x > 1)
			throw new OutOfRangeException(x, 0, 1);
		this.index = index;
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
		return p[n];
	}

	/**
	 * Gets x.
	 *
	 * @return x
	 */
	public double getX()
	{
		return p[1];
	}

	/**
	 * Gets x^2.
	 *
	 * @return x^2
	 */
	public double getX2()
	{
		return p[2];
	}

	/**
	 * Gets x^3.
	 *
	 * @return x^3
	 */
	public double getX3()
	{
		return p[3];
	}
}