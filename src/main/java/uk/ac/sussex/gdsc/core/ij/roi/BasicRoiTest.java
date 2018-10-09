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
package uk.ac.sussex.gdsc.core.ij.roi;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import ij.gui.Roi;

/**
 * Class for testing if coordinates are within a rectangle/oval ROI.
 */
public class BasicRoiTest extends RoiTest {
  private final Shape shape;

  /**
   * Instantiates a new basic roi test.
   *
   * @param roi the roi
   */
  public BasicRoiTest(Roi roi) {
    if (roi.getType() == Roi.RECTANGLE) {
      // Account for corners
      if (roi.getCornerDiameter() != 0) {
        shape = new RoundRectangle2D.Double(roi.getXBase(), roi.getYBase(), roi.getFloatWidth(),
            roi.getFloatHeight(), roi.getCornerDiameter(), roi.getCornerDiameter());
      } else {
        shape = roi.getFloatBounds();
      }
    } else if (roi.getType() == Roi.OVAL) {
      shape = new Ellipse2D.Double(roi.getXBase(), roi.getYBase(), roi.getFloatWidth(),
          roi.getFloatHeight());
    } else {
      throw new IllegalArgumentException("Require rectangle or oval ROI");
    }
  }

  @Override
  public boolean contains(double x, double y) {
    return shape.contains(x, y);
  }
}
