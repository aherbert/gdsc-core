/*-
 * #%L
 * Genome Damage and Stability Centre ImageJ Core Package
 * 
 * Contains code used by:
 * 
 * GDSC ImageJ Plugins - Microscopy image analysis
 * 
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2018 Alex Herbert
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package gdsc.core.ij.roi;

import ij.gui.Roi;

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
