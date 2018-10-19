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

package uk.ac.sussex.gdsc.core.data.detection;

import gnu.trove.list.array.TIntArrayList;

import java.awt.geom.Rectangle2D;

/**
 * Class to compute collision detections between a point and a set of rectangles.
 */
public class SimpleDetectionGrid implements DetectionGrid {
  private final Rectangle2D[] rectangles;

  /**
   * Set to true to include the outer right and lower boundary edge of the rectangle.
   *
   * <p>This contrasts with {@link Rectangle2D#contains(double, double)} as a point on the right or
   * lower boundary will not be within the rectangle since due to the definition of "insideness".
   */
  private boolean includeOuterEdge = false;

  /**
   * Instantiates a new simple detection grid.
   *
   * @param rectangles the rectangles
   */
  SimpleDetectionGrid(Rectangle2D[] rectangles) {
    if (rectangles == null) {
      throw new IllegalArgumentException("Rectangle2Ds must not be null");
    }
    this.rectangles = rectangles;
  }

  /**
   * Wrap the data to create a simple detection grid.
   *
   * @param rectangles the rectangles
   * @return the simple detection grid
   */
  public static SimpleDetectionGrid wrap(Rectangle2D[] rectangles) {
    return new SimpleDetectionGrid(rectangles);
  }

  @Override
  public int size() {
    return rectangles.length;
  }

  @Override
  public int[] find(double x, double y) {
    final TIntArrayList list = new TIntArrayList();
    if (includeOuterEdge) {
      for (int i = 0; i < rectangles.length; i++) {
        // Because we want to know if the point is less than or equal to
        // the max XY. The default contains method of the rectangle
        // does less than.
        if (contains(rectangles[i], x, y)) {
          list.add(i);
        }
      }
    } else {
      for (int i = 0; i < rectangles.length; i++) {
        // Note that a point on the right or lower boundary will not be
        // within the rectangle since it respects the definition of "insideness"
        if (rectangles[i].contains(x, y)) {
          list.add(i);
        }
      }
    }
    return list.toArray();
  }

  /**
   * Check if the rectangle contains the point (x,y).
   *
   * @param rectangle the rectangle
   * @param x the x
   * @param y the y
   * @return true, if successful
   */
  private static boolean contains(Rectangle2D rectangle, double x, double y) {
    final double x0 = rectangle.getX();
    final double y0 = rectangle.getY();
    return (x >= x0 && y >= y0 && x <= rectangle.getMaxX() && y <= rectangle.getMaxY());
  }

  /**
   * Checks whether to include the outer right and lower boundary edge of the rectangle.
   *
   * <p>This contrasts with {@link Rectangle2D#contains(double, double)} as a point on the right or
   * lower boundary will not be within the rectangle since due to the definition of "insideness".
   *
   * @return true, if is include outer edge
   */
  public boolean isIncludeOuterEdge() {
    return includeOuterEdge;
  }

  /**
   * Set to true to include the outer right and lower boundary edge of the rectangle.
   *
   * <p>This contrasts with {@link Rectangle2D#contains(double, double)} as a point on the right or
   * lower boundary will not be within the rectangle since due to the definition of "insideness".
   *
   * @param includeOuterEdge the new include outer edge
   */
  public void setIncludeOuterEdge(boolean includeOuterEdge) {
    this.includeOuterEdge = includeOuterEdge;
  }
}
