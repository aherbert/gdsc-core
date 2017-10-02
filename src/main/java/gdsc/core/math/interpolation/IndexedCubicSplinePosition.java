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
public class IndexedCubicSplinePosition extends CubicSplinePosition
{
	/** The index of the spline node */
	final int index;

	/** The scale used to compress the original value to the range 0-1. */
	final double scale;

	/**
	 * Instantiates a new indexed cubic spline position. Only used when x is known to be in the range 0-1 and the index
	 * is positive..
	 *
	 * @param index
	 *            the index
	 * @param x
	 *            the x
	 * @param scale
	 *            the scale used to compress the original value to the range 0-1
	 * @param dummy
	 *            the dummy flag
	 */
	IndexedCubicSplinePosition(int index, double x, double scale, boolean dummy)
	{
		super(x, dummy);
		this.index = index;
		this.scale = scale;
	}

	/**
	 * Instantiates a new spline position.
	 *
	 * @param index
	 *            the index
	 * @param x
	 *            the distance along the spline to the next node (range 0 to 1)
	 * @param scale
	 *            the scale used to compress the original value to the range 0-1
	 * @throws IllegalArgumentException
	 *             If the index is negative
	 * @throws OutOfRangeException
	 *             If x is not in the range 0 to 1
	 */
	public IndexedCubicSplinePosition(int index, double x, double scale)
			throws IllegalArgumentException, OutOfRangeException
	{
		super(x);
		// If the user creates a spline position then we should check it is valid
		if (index < 0)
			throw new IllegalArgumentException("Index must be positive");
		this.index = index;
		this.scale = scale;
	}

	@Override
	public double scale(double x)
	{
		return x * scale;
	}

	@Override
	public double scaleGradient(double df_dx)
	{
		return df_dx / scale;
	}
	
	@Override
	public double scaleGradient2(double d2f_dx2)
	{
		return d2f_dx2 / scale / scale;
	}
}