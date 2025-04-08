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

package uk.ac.sussex.gdsc.core.ij.roi;

import ij.gui.Roi;

/**
 * Class for creating a {@link CoordinatePredicate} from an ROI.
 */
public final class CoordinatePredicateUtils {

  /** No public construction. */
  private CoordinatePredicateUtils() {}

  /**
   * Creates a {@link CoordinatePredicate} for testing if coordinates are within the ROI.
   *
   * @param roi the roi (must be an area ROI)
   * @return the predicate (or null if not supported)
   */
  public static CoordinatePredicate createContainsPredicate(Roi roi) {
    // Support different ROIs.
    if (isNotArea(roi)) {
      return null;
    }

    if (roi.getType() == Roi.RECTANGLE || roi.getType() == Roi.OVAL) {
      return new BasicRoiContainsPredicate(roi);
    } else if (roi.getType() == Roi.COMPOSITE) {
      return new CompositeRoiContainsPredicate(roi);
    } else if (roi.getType() == Roi.POLYGON || roi.getType() == Roi.FREEROI
        || roi.getType() == Roi.TRACED_ROI) {
      return new PolygonRoiContainsPredicate(roi);
    }

    // All values supported by Roi.isArea have been covered.
    // We should not get here.
    return null;
  }

  /**
   * Checks if is not an area ROI.
   *
   * @param roi the roi
   * @return true, if is not an area
   */
  private static boolean isNotArea(Roi roi) {
    return roi == null || !roi.isArea() || !(roi.getFloatWidth() * roi.getFloatHeight() != 0.0);
  }
}
