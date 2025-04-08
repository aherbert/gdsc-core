/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
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

package uk.ac.sussex.gdsc.core.clustering;

import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;

/**
 * Store 2D coordinates shifted to the origin for efficient grid processing.
 */
public class CoordinateStore {

  /** The xcoords. */
  protected final float[] xcoord;

  /** The ycoords. */
  protected final float[] ycoord;

  /** The origin for the x coordinate. Add this to x to get the original coordinates. */
  public final float originx;
  /** The origin for the y coordinate. Add this to y to get the original coordinates. */
  public final float originy;

  /** The min X coord. */
  public final float minXCoord;
  /** The min Y coord. */
  public final float minYCoord;
  /** The max X coord. */
  public final float maxXCoord;
  /** The max Y coord. */
  public final float maxYCoord;

  /**
   * The volume containing the coordinates. This may be larger than the product of the lengths
   * defined by the maximum minus the minimum in each dimension.
   */
  public final double area;

  /**
   * Create a new instance.
   *
   * <p>Input arrays are modified. The area may be provided or left as zero. The stored area will be
   * the larger of the input area and the product of the lengths defined by the maximum minus the
   * minimum in each dimension.
   *
   * @param xcoord the x coordinates (stored by reference)
   * @param ycoord the y coordinates (stored by reference)
   * @param area the volume of the coordinates (width by height)
   * @throws IllegalArgumentException if results are null or empty
   */
  public CoordinateStore(float[] xcoord, float[] ycoord, double area) {
    if (xcoord == null || ycoord == null || xcoord.length == 0 || xcoord.length != ycoord.length) {
      throw new IllegalArgumentException("Results are null or empty or mismatched in length");
    }

    this.xcoord = xcoord;
    this.ycoord = ycoord;

    // Get min bounds
    final float minX = MathUtils.min(xcoord);
    final float minY = MathUtils.min(ycoord);

    // Round down and shift to origin (so all coords are >=0 for efficient grid allocation)
    originx = (float) Math.floor(minX);
    originy = (float) Math.floor(minY);

    SimpleArrayUtils.add(xcoord, -originx);
    SimpleArrayUtils.add(ycoord, -originy);

    // Store the limits
    this.minXCoord = minX - originx;
    this.minYCoord = minY - originy;
    this.maxXCoord = MathUtils.max(xcoord);
    this.maxYCoord = MathUtils.max(ycoord);

    // Store the area of the input results
    this.area = Math.max(area, (maxXCoord - minXCoord) * (maxYCoord - minYCoord));
  }

  /**
   * Instantiates a new coordinate store.
   *
   * @param source the source
   * @param deepCopy Set to true to copy the coordinate arrays
   */
  protected CoordinateStore(CoordinateStore source, boolean deepCopy) {
    this.xcoord = (deepCopy) ? source.xcoord.clone() : source.xcoord;
    this.ycoord = (deepCopy) ? source.ycoord.clone() : source.ycoord;
    this.originx = source.originx;
    this.originy = source.originy;
    this.minXCoord = source.minXCoord;
    this.minYCoord = source.minYCoord;
    this.maxXCoord = source.maxXCoord;
    this.maxYCoord = source.maxYCoord;
    this.area = source.area;
  }

  /**
   * Gets the number of points in the data store.
   *
   * @return the size
   */
  public int getSize() {
    return xcoord.length;
  }

  /**
   * Gets the minimum X.
   *
   * @return the minimum X
   */
  public float getMinimumX() {
    return minXCoord;
  }

  /**
   * Gets the maximum X.
   *
   * @return the maximum X
   */
  public float getMaximumX() {
    return maxXCoord;
  }

  /**
   * Gets the minimum Y.
   *
   * @return the minimum Y
   */
  public float getMinimumY() {
    return minYCoord;
  }

  /**
   * Gets the maximum Y.
   *
   * @return the maximum Y
   */
  public float getMaximumY() {
    return maxYCoord;
  }

  /**
   * Gets the data in float format.
   *
   * @return the data
   */
  public float[][] getData() {
    return new float[][] {xcoord.clone(), ycoord.clone()};
  }

  /**
   * Gets the data in double format.
   *
   * @return the data
   */
  public double[][] getDoubleData() {
    final double[] x = new double[xcoord.length];
    final double[] y = new double[xcoord.length];
    for (int i = x.length; i-- > 0;) {
      x[i] = xcoord[i];
      y[i] = ycoord[i];
    }
    return new double[][] {x, y};
  }

  /**
   * Create a copy.
   *
   * @param deepCopy Set to true to copy the coordinate arrays
   * @return the copy
   */
  public CoordinateStore copy(boolean deepCopy) {
    return new CoordinateStore(this, deepCopy);
  }
}
