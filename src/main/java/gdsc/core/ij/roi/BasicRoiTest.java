package gdsc.core.ij.roi;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import ij.gui.Roi;

/*----------------------------------------------------------------------------- 
 * GDSC Plugins for ImageJ
 * 
 * Copyright (C) 2018 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

/**
 * Class for testing if coordinates are within a rectangle/oval ROI.
 */
public class BasicRoiTest extends RoiTest
{
	final Shape shape;

	public BasicRoiTest(Roi roi)
	{
		if (roi.getType() == Roi.RECTANGLE)
		{
			// Account for corners
			if (roi.getCornerDiameter() != 0)
			{
				shape = new RoundRectangle2D.Double(roi.getXBase(), roi.getYBase(), roi.getFloatWidth(),
						roi.getFloatHeight(), roi.getCornerDiameter(), roi.getCornerDiameter());
			}
			else
			{
				shape = roi.getFloatBounds();
			}
		}
		else if (roi.getType() == Roi.OVAL)
		{
			shape = new Ellipse2D.Double(roi.getXBase(), roi.getYBase(), roi.getFloatWidth(), roi.getFloatHeight());
		}
		else
		{
			throw new IllegalArgumentException("Require rectangle or oval ROI");
		}
	}

	@Override
	public boolean contains(double x, double y)
	{
		return shape.contains(x, y);
	}
}