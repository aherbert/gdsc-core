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
 * Copyright (C) 2011 - 2020 Alex Herbert
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class OffsetPointRoiTest {
  @Test
  void testConstructor() {
    // Simple test to verify the line sub-pixel convention.
    // A better test would be to compare to the standard PointRoi when on an image canvas
    // that the screenXD/offScreenXD/etc methods do not add a 0.5 offset.
    Assertions.assertFalse(new OffsetPointRoi(1.0, 2.0).useLineSubpixelConvention());
    Assertions.assertFalse(
        new OffsetPointRoi(new float[] {0.5f}, new float[] {1.5f}).useLineSubpixelConvention());
    Assertions.assertFalse(
        new OffsetPointRoi(new float[] {0.5f}, new float[] {1.5f}, 1).useLineSubpixelConvention());
  }
}
