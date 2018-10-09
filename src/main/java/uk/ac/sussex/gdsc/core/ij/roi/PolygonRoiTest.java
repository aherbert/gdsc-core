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

import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;

import java.awt.geom.Rectangle2D;

import ij.gui.Roi;
import ij.process.FloatPolygon;

/**
 * Class for testing if coordinates are within a polygon/free/traced ROI.
 */
public class PolygonRoiTest extends RoiTest {
  private final Rectangle2D.Double bounds;
  private final double[] xpoints, ypoints;

  /**
   * Instantiates a new polygon roi test.
   *
   * @param roi the roi
   */
  public PolygonRoiTest(Roi roi) {
    if (roi.getType() == Roi.POLYGON || roi.getType() == Roi.FREEROI
        || roi.getType() == Roi.TRACED_ROI) {
      bounds = roi.getFloatBounds();
      final FloatPolygon poly = roi.getFloatPolygon();
      xpoints = SimpleArrayUtils.toDouble(poly.xpoints);
      ypoints = SimpleArrayUtils.toDouble(poly.ypoints);
    } else {
      throw new IllegalArgumentException("Require polygon/free/traced ROI");
    }
  }

  @Override
  public boolean contains(double x, double y) {
    return bounds.contains(x, y) && polygonContains(x, y);
  }

  /**
   * Returns 'true' if the point (x,y) is inside this polygon. This is a Java version of the
   * remarkably small C program by W. Randolph Franklin at
   * http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html#The%20C%20Code
   *
   * @param x the x
   * @param y the y
   * @return true, if successful
   */
  public boolean polygonContains(double x, double y) {
    boolean inside = false;
    for (int i = xpoints.length, j = 0; i-- > 0; j = i) {
      if (((ypoints[i] > y) != (ypoints[j] > y))
          && (x < (xpoints[j] - xpoints[i]) * (y - ypoints[i]) / (ypoints[j] - ypoints[i])
              + xpoints[i])) {
        inside = !inside;
      }
    }
    return inside;
  }
}
