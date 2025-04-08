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

import ij.gui.Line;

/**
 * Custom Line ROI which has float coordinates with respect to the upper left corners of the
 * pixels. The default Line has float coordinates with respect to the pixel centre (i.e. a 0.5
 * pixel offset is applied to the coordinate when drawing on screen) if the type is a line or a
 * point.
 */
public class OffsetLineRoi extends Line {
  private static final long serialVersionUID = 1L;

  /**
   * Create an instance.
   *
   * @param ox1 start x
   * @param oy1 start y
   * @param ox2 end x
   * @param oy2 end y
   */
  public OffsetLineRoi(double ox1, double oy1, double ox2, double oy2) {
    super(ox1, oy1, ox2, oy2);
  }

  @Override
  protected boolean useLineSubpixelConvention() {
    // This returns true for ij.gui.Line. Here we require that coordinates are with respect
    // to the upper left corner and not the pixel centre.
    return false;
  }
}
