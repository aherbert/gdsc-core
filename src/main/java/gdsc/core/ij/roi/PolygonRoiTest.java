package gdsc.core.ij.roi;

import java.awt.geom.Rectangle2D;

import gdsc.core.utils.SimpleArrayUtils;
import ij.gui.Roi;
import ij.process.FloatPolygon;

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
 * Class for testing if coordinates are within a polygon/free/traced ROI.
 */
public class PolygonRoiTest extends RoiTest
{
	final Rectangle2D.Double bounds;
	final double[] xpoints, ypoints;

	public PolygonRoiTest(Roi roi)
	{
		if (roi.getType() == Roi.POLYGON || roi.getType() == Roi.FREEROI || roi.getType() == Roi.TRACED_ROI)
		{
			bounds = roi.getFloatBounds();
			FloatPolygon poly = roi.getFloatPolygon();
			xpoints = SimpleArrayUtils.toDouble(poly.xpoints);
			ypoints = SimpleArrayUtils.toDouble(poly.ypoints);
		}
		else
		{
			throw new IllegalArgumentException("Require polygon/free/traced ROI");
		}
	}

	@Override
	public boolean contains(double x, double y)
	{
		return bounds.contains(x, y) && polygonContains(x, y);
	}

	/**
	 * Returns 'true' if the point (x,y) is inside this polygon. This is a Java
	 * version of the remarkably small C program by W. Randolph Franklin at
	 * http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html#The%20C%20Code
	 */
	public boolean polygonContains(double x, double y)
	{
		boolean inside = false;
		for (int i = xpoints.length, j = 0; i-- > 0; j = i)
		{
			if (((ypoints[i] > y) != (ypoints[j] > y)) &&
					(x < (xpoints[j] - xpoints[i]) * (y - ypoints[i]) / (ypoints[j] - ypoints[i]) + xpoints[i]))
				inside = !inside;
		}
		return inside;
	}
}