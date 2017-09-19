package gdsc.core.data.detection;

import java.awt.geom.Rectangle2D;

import gnu.trove.list.array.TIntArrayList;

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
 * Class to compute collision detections between a point and a set of rectangles
 */
public class SimpleDetectionGrid implements DetectionGrid
{
	private Rectangle2D[] rectangles;

	public SimpleDetectionGrid(Rectangle2D[] rectangles)
	{
		if (rectangles == null)
			throw new IllegalArgumentException("Rectangle2Ds must not be null");
		this.rectangles = rectangles;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.detection.DetectionGrid#size()
	 */
	public int size()
	{
		return rectangles.length;
	}

	public int[] find(double x, double y)
	{
		TIntArrayList list = new TIntArrayList();
		for (int i = 0; i < rectangles.length; i++)
		{
			// Because we want to know if the point is less than or equal to
			// the max XY. The default contains method of the rectangle
			// does less than.
			//if (rectangles[i].contains(x, y))
			if (contains(rectangles[i], x, y))
				list.add(i);
		}
		return list.toArray();
	}

	private boolean contains(Rectangle2D r, double x, double y)
	{
		double x0 = r.getX();
		double y0 = r.getY();
		return (x >= x0 && y >= y0 && x <= r.getMaxX() && y <= r.getMaxY());
	}

}
