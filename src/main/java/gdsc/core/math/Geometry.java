package gdsc.core.math;

/*----------------------------------------------------------------------------- 
 * GDSC Plugins for ImageJ
 * 
 * Copyright (C) 2011 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

/**
 * Contains methods for standard geometry computations
 * 
 * @author Alex Herbert
 */
public class Geometry
{
	/**
	 * Gets the area of the triangle from its vertices.
	 *
	 * @param xA
	 *            the first vertex x
	 * @param yA
	 *            the first vertex y
	 * @param xB
	 *            the second vertex x
	 * @param yB
	 *            the second vertex y
	 * @param xC
	 *            the third vertex x
	 * @param yC
	 *            the third vertex y
	 * @return the area
	 */
	public static double getArea(double xA, double yA, double xB, double yB, double xC, double yC)
	{
		return Math.abs((xA - xC) * (yB - yA) - (xA - xB) * (yC - yA)) / 2;
	}

	/**
	 * Gets the area of the triangle from its vertices assuming the third vertex is 0,0.
	 *
	 * @param xA
	 *            the first vertex x
	 * @param yA
	 *            the first vertex y
	 * @param xB
	 *            the second vertex x
	 * @param yB
	 *            the second vertex y
	 * @return the area
	 */
	public static double getArea(double xA, double yA, double xB, double yB)
	{
		return Math.abs(xA * yB - yA * xB) / 2;
	}

	/**
	 * Gets the area of a polygon using the Shoelace formula (https://en.wikipedia.org/wiki/Shoelace_formula)
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @return the area
	 * @throws IllegalArgumentException
	 *             If the arrays are not the same length
	 */
	public static double getArea(float[] x, float[] y) throws IllegalArgumentException
	{
		if (x.length < 3)
			return 0;
		if (x.length != y.length)
			throw new IllegalArgumentException("Input arrays must be the same length");
		// Start with the closing values
		int i = x.length - 1;
		double sum1 = x[i] * y[0];
		double sum2 = x[0] * y[i];
		while (i-- > 0)
		{
			sum1 += x[i] * y[i + 1];
			sum2 += x[i + 1] * y[i];
		}
		return (sum1 - sum2) / 2;
	}

	/**
	 * Gets the area of a polygon using the Shoelace formula (https://en.wikipedia.org/wiki/Shoelace_formula)
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @return the area
	 * @throws IllegalArgumentException
	 *             If the arrays are not the same length
	 */
	public static double getArea(double[] x, double[] y) throws IllegalArgumentException
	{
		if (x.length < 3)
			return 0;
		if (x.length != y.length)
			throw new IllegalArgumentException("Input arrays must be the same length");
		// Start with the closing values
		int i = x.length - 1;
		double sum1 = x[i] * y[0];
		double sum2 = x[0] * y[i];
		while (i-- > 0)
		{
			sum1 += x[i] * y[i + 1];
			sum2 += x[i + 1] * y[i];
		}
		return (sum1 - sum2) / 2;
	}
}
