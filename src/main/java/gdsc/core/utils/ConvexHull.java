package gdsc.core.utils;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

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
 * <p>
 * Functionality of this has been taken from ij.process.FloatPolygon.
 */
public class ConvexHull
{
	/** The x coordinates. */
	public final float[] x;

	/** The y coordinates. */
	public final float[] y;

	/**
	 * Instantiates a new convex hull.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 */
	private ConvexHull(float[] x, float[] y)
	{
		this.x = x;
		this.y = y;
	}

	public int size()
	{
		return x.length;
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
		return create(0, 0, xCoordinates, yCoordinates, xCoordinates.length);
	}

	/**
	 * Create a new convex hull from the given coordinates.
	 * <p>
	 * Uses the gift wrap algorithm to find the convex hull.
	 * <p>
	 * Taken from ij.gui.PolygonRoi and adapted for float coordinates.
	 *
	 * @param xCoordinates
	 *            the x coordinates
	 * @param yCoordinates
	 *            the y coordinates
	 * @param n
	 *            the number of coordinates
	 * @return the convex hull
	 * @throws NullPointerException
	 *             if the inputs are null
	 * @throws ArrayIndexOutOfBoundsException
	 *             if the yCoordinates are smaller than the xCoordinates
	 */
	public static ConvexHull create(float[] xCoordinates, float[] yCoordinates, int n)
	{
		return create(0, 0, xCoordinates, yCoordinates, n);
	}

	/**
	 * Create a new convex hull from the given coordinates.
	 * <p>
	 * Uses the gift wrap algorithm to find the convex hull.
	 * <p>
	 * Taken from ij.gui.PolygonRoi and adapted for float coordinates.
	 *
	 * @param xbase
	 *            the x base coordinate (origin)
	 * @param ybase
	 *            the y base coordinate (origin)
	 * @param xCoordinates
	 *            the x coordinates
	 * @param yCoordinates
	 *            the y coordinates
	 * @param n
	 *            the number of coordinates
	 * @return the convex hull
	 * @throws NullPointerException
	 *             if the inputs are null
	 * @throws ArrayIndexOutOfBoundsException
	 *             if the yCoordinates are smaller than the xCoordinates
	 */
	public static ConvexHull create(float xbase, float ybase, float[] xCoordinates, float[] yCoordinates, int n)
	{
		float[] xx = new float[n];
		float[] yy = new float[n];
		int n2 = 0;
		float smallestY = yCoordinates[0];
		for (int i = 1; i < n; i++)
		{
			if (smallestY > yCoordinates[i])
				smallestY = yCoordinates[i];
		}
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

	// Below is functionality taken from ij.process.FloatPolygon
	private Rectangle bounds;
	private float minX, minY, maxX, maxY;

	/**
	 * Returns 'true' if the point (x,y) is inside this polygon. This is a Java
	 * version of the remarkably small C program by W. Randolph Franklin at
	 * http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html#The%20C%20Code
	 */
	public boolean contains(float x, float y)
	{
		int npoints = size();
		float[] xpoints = this.x;
		float[] ypoints = this.y;
		boolean inside = false;
		for (int i = 0, j = npoints - 1; i < npoints; j = i++)
		{
			if (((ypoints[i] > y) != (ypoints[j] > y)) &&
					(x < (xpoints[j] - xpoints[i]) * (y - ypoints[i]) / (ypoints[j] - ypoints[i]) + xpoints[i]))
				inside = !inside;
		}
		return inside;
	}

	public Rectangle getBounds()
	{
		int npoints = size();
		float[] xpoints = this.x;
		float[] ypoints = this.y;
		if (npoints == 0)
			return new Rectangle();
		if (bounds == null)
			calculateBounds(xpoints, ypoints, npoints);
		return bounds.getBounds();
	}

	public Rectangle2D.Double getFloatBounds()
	{
		int npoints = size();
		float[] xpoints = this.x;
		float[] ypoints = this.y;
		if (npoints == 0)
			return new Rectangle2D.Double();
		if (bounds == null)
			calculateBounds(xpoints, ypoints, npoints);
		return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
	}

	void calculateBounds(float[] xpoints, float[] ypoints, int npoints)
	{
		minX = Float.MAX_VALUE;
		minY = Float.MAX_VALUE;
		maxX = Float.MIN_VALUE;
		maxY = Float.MIN_VALUE;
		for (int i = 0; i < npoints; i++)
		{
			float x = xpoints[i];
			minX = Math.min(minX, x);
			maxX = Math.max(maxX, x);
			float y = ypoints[i];
			minY = Math.min(minY, y);
			maxY = Math.max(maxY, y);
		}
		int iMinX = (int) Math.floor(minX);
		int iMinY = (int) Math.floor(minY);
		bounds = new Rectangle(iMinX, iMinY, (int) (maxX - iMinX + 0.5), (int) (maxY - iMinY + 0.5));
	}

	public double getLength(boolean isLine)
	{
		int npoints = size();
		float[] xpoints = this.x;
		float[] ypoints = this.y;
		double dx, dy;
		double length = 0.0;
		for (int i = 0; i < (npoints - 1); i++)
		{
			dx = xpoints[i + 1] - xpoints[i];
			dy = ypoints[i + 1] - ypoints[i];
			length += Math.sqrt(dx * dx + dy * dy);
		}
		if (!isLine)
		{
			dx = xpoints[0] - xpoints[npoints - 1];
			dy = ypoints[0] - ypoints[npoints - 1];
			length += Math.sqrt(dx * dx + dy * dy);
		}
		return length;
	}
}