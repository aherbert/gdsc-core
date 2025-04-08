/*-
 * #%L
 * Genome Damage and Stability Centre Core ImageJ Package
 *
 * Contains core utilities for image analysis in ImageJ and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2025 Alex Herbert
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

package uk.ac.sussex.gdsc.core.ij.gui;

import ij.gui.PolygonRoi;

/**
 * Custom PolygonRoi which has float coordinates with respect to the upper left corners of the
 * pixels. The default PolygonRoi has float coordinates with respect to the pixel centre (i.e. a 0.5
 * pixel offset is applied to the coordinate when drawing on screen) if the type is a line or a
 * point.
 */
public class OffsetPolygonRoi extends PolygonRoi {
  private static final long serialVersionUID = 1L;

  /**
   * Create an instance.
   *
   * @param x the x coordinates
   * @param y the y coordinates
   * @param type the type
   */
  public OffsetPolygonRoi(float[] x, float[] y, int type) {
    super(x, y, type);
  }

  /**
   * Create an instance.
   *
   * @param x the x coordinates
   * @param y the y coordinates
   * @param points the number of points
   * @param type the type
   */
  public OffsetPolygonRoi(float[] x, float[] y, int points, int type) {
    super(x, y, points, type);
  }

  @Override
  protected boolean useLineSubpixelConvention() {
    // This may return true for ij.gui.PolygonRoi depending on the type. Here we require that
    // coordinates are with respect to the upper left corner and not the pixel centre.
    return false;
  }
}
