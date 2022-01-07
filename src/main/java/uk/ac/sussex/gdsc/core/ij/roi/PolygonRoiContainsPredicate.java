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
 * Copyright (C) 2011 - 2022 Alex Herbert
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

import ij.gui.Roi;
import ij.process.FloatPolygon;
import java.awt.geom.Rectangle2D;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;

/**
 * Class for testing if coordinates are within a polygon/free/traced ROI.
 */
public class PolygonRoiContainsPredicate implements CoordinatePredicate {
  private final Rectangle2D.Double bounds;
  private final double[] xpoints;
  private final double[] ypoints;

  /**
   * Creates a new instance.
   *
   * @param roi the roi
   */
  public PolygonRoiContainsPredicate(Roi roi) {
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
  public boolean test(double x, double y) {
    return bounds.contains(x, y) && polygonContains(x, y);
  }

  /**
   * Returns 'true' if the point (x,y) is inside this polygon.
   *
   * <p>Note: Boundary points may not be inside the polygon. The definition of inside requires that:
   *
   * <ul>
   *
   * <li>it lies completely inside the boundary or;
   *
   * <li>it lies exactly on the boundary and the space immediately adjacent to the point in the
   * increasing X direction is entirely inside the boundary.
   *
   * <li>it lies exactly on a horizontal boundary segment and the space immediately adjacent to the
   * point in the increasing Y direction is inside the boundary.
   *
   * </ul>
   *
   * @param x the x
   * @param y the y
   * @return true, if successful
   */
  public boolean polygonContains(double x, double y) {
    // This is a Java version of the winding number algorithm wn_PnPoly:
    // http://geomalgorithms.com/a03-_inclusion.html
    int wn = 0;
    // All edges of polygon, each edge is from i to j
    for (int j = xpoints.length, i = 0; j-- > 0; i = j) {
      if (ypoints[i] <= y) {
        // start y <= y
        if (ypoints[j] > y) {
          // an upward crossing
          if (isLeft(xpoints[i], ypoints[i], xpoints[j], ypoints[j], x, y) > 0) {
            // P left of edge
            // have a valid up intersect
            ++wn;
          }
        }
      } else {
        // start y > y (no test needed)
        if (ypoints[j] <= y) {
          // a downward crossing
          if (isLeft(xpoints[i], ypoints[i], xpoints[j], ypoints[j], x, y) < 0) {
            // P right of edge
            // have a valid down intersect
            --wn;
          }
        }
      }
    }
    return wn != 0;
  }

  /**
   * Tests if a point is Left|On|Right of an infinite line.
   *
   * @param x1 the line start x
   * @param y1 the line start y
   * @param x2 the line end x
   * @param y2 the line end y
   * @param x the point x
   * @param y the point y
   * @return >0 for point left of the line through the start to end, =0 for on the line, otherwise
   *         <0
   */
  private static double isLeft(double x1, double y1, double x2, double y2, double x, double y) {
    return ((x2 - x1) * (y - y1) - (x - x1) * (y2 - y1));
  }
}
