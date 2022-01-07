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

package uk.ac.sussex.gdsc.core.ij.gui;

import ij.gui.PointRoi;

/**
 * Custom PointRoi which has float coordinates with respect to the upper left corners of the pixels.
 * The default PointRoi has float coordinates with respect to the pixel centre (i.e. a 0.5 pixel
 * offset is applied to the coordinate when drawing on screen).
 */
public class OffsetPointRoi extends PointRoi {
  private static final long serialVersionUID = 1L;

  /**
   * Create an instance.
   *
   * @param x the x coordinates
   * @param y the y coordinates
   */
  public OffsetPointRoi(float[] x, float[] y) {
    super(x, y);
  }

  /**
   * Create an instance.
   *
   * @param x the x coordinates
   * @param y the y coordinates
   * @param points the number of points
   */
  public OffsetPointRoi(float[] x, float[] y, int points) {
    super(x, y, points);
  }

  /**
   * Create an instance.
   *
   * @param x the x coordinate
   * @param y the y coordinate
   */
  public OffsetPointRoi(double x, double y) {
    super(x, y);
  }

  @Override
  protected boolean useLineSubpixelConvention() {
    // This returns true for ij.gui.PointRoi. Here we require that coordinates are with respect
    // to the upper left corner and not the pixel centre.
    return false;
  }
}
