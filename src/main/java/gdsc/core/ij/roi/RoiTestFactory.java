package gdsc.core.ij.roi;

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
 * Class for creating an RoiTest from an ROI.
 */
public class RoiTestFactory
{

	/**
	 * Creates the RoiTest.
	 *
	 * @param roi
	 *            the roi (must be an area ROI)
	 * @return the roi test (or null)
	 */
	public static RoiTest create(Roi roi)
	{
		// Support different ROIs.
		if (roi == null || !roi.isArea() || !(roi.getFloatWidth() > 0 && roi.getFloatHeight() > 0))
			return null;

		if (roi.getType() == Roi.RECTANGLE || roi.getType() == Roi.OVAL)
		{
			return new BasicRoiTest(roi);
		}
		else if (roi.getType() == Roi.COMPOSITE)
		{
			return new CompositeRoiTest(roi);
		}
		else if (roi.getType() == Roi.POLYGON || roi.getType() == Roi.FREEROI || roi.getType() == Roi.TRACED_ROI)
		{
			return new PolygonRoiTest(roi);
		}
		
		return null;
	}
}
