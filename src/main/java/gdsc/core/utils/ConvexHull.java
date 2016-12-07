package gdsc.core.utils;

/*----------------------------------------------------------------------------- 
 * GDSC ImageJ Software
 * 
 * Copyright (C) 2016 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

import java.util.Arrays;

/**
 * Contains a set of paired coordinates representing the convex hull of a set of points.
 */
public class ConvexHull
{
	/** The x coordinates. */
	final float[] x;

	/** The y coordinates. */
	final float[] y;

	/**
	 * Instantiates a new convex hull.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 */
	public ConvexHull(float[] x, float[] y)
	{
		this.x = x;
		this.y = y;
	}

	/**
	 * Create a new convex hull from the given coordinates.
	 * <p>
	 * Uses the gift wrap algorithm to find the convex hull.
	 * <p>
	 * Taken from ij.gui.PolygonRoi and adapted for float coordinates.
	 * 
	 * @throws NullPointerException
	 *             if the inputs are null
	 * @throws ArrayIndexOutOfBoundsException
	 *             if the yCoordinates are smaller than the xCoordinates
	 */
	public static ConvexHull create(float[] xCoordinates, float[] yCoordinates)
	{
		int n = xCoordinates.length;
		float xbase = xCoordinates[0];
		float ybase = yCoordinates[0];
		for (int i = 1; i < n; i++)
		{
			if (xbase > xCoordinates[i])
				xbase = xCoordinates[i];
			if (ybase > yCoordinates[i])
				ybase = yCoordinates[i];
		}
		float[] xx = new float[n];
		float[] yy = new float[n];
		int n2 = 0;
		float smallestY = ybase;
		float x, y;
		float smallestX = Float.MAX_VALUE;
		int p1 = 0;
		for (int i = 0; i < n; i++)
		{
			x = xCoordinates[i];
			y = yCoordinates[i];
			if (y == smallestY && x < smallestX)
			{
				smallestX = x;
				p1 = i;
			}
		}
		int pstart = p1;
		float x1, y1, x2, y2, x3, y3;
		int p2, p3;
		float determinate;
		int count = 0;
		do
		{
			x1 = xCoordinates[p1];
			y1 = yCoordinates[p1];
			p2 = p1 + 1;
			if (p2 == n)
				p2 = 0;
			x2 = xCoordinates[p2];
			y2 = yCoordinates[p2];
			p3 = p2 + 1;
			if (p3 == n)
				p3 = 0;
			do
			{
				x3 = xCoordinates[p3];
				y3 = yCoordinates[p3];
				determinate = x1 * (y2 - y3) - y1 * (x2 - x3) + (y3 * x2 - y2 * x3);
				if (determinate > 0)
				{
					x2 = x3;
					y2 = y3;
					p2 = p3;
				}
				p3 += 1;
				if (p3 == n)
					p3 = 0;
			} while (p3 != p1);
			if (n2 < n)
			{
				xx[n2] = xbase + x1;
				yy[n2] = ybase + y1;
				n2++;
			}
			else
			{
				count++;
				if (count > 10)
					return null;
			}
			p1 = p2;
		} while (p1 != pstart);
		xx = Arrays.copyOf(xx, n2);
		yy = Arrays.copyOf(yy, n2);
		return new ConvexHull(xx, yy);
	}
}