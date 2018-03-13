package gdsc.core.ij.roi;

import java.awt.Rectangle;
import java.awt.Shape;

import ij.gui.Roi;
import ij.gui.ShapeRoi;

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
 * Class for testing if coordinates are within a composite ROI.
 */
public class CompositeRoiTest extends RoiTest
{
	final Shape shape;
	final int ox, oy;

	public CompositeRoiTest(Roi roi)
	{
		if (roi.getType() == Roi.COMPOSITE)
		{
			// The composite shape is offset by the origin
			final Rectangle bounds = roi.getBounds();
			shape = ((ShapeRoi) roi).getShape();
			ox = bounds.x;
			oy = bounds.y;
		}
		else
		{
			throw new IllegalArgumentException("Require composite ROI");
		}
	}

	@Override
	public boolean contains(double x, double y)
	{
		return shape.contains(x - ox, y - oy);
	}
}