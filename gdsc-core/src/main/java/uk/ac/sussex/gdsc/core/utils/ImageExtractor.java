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
 * Copyright (C) 2011 - 2023 Alex Herbert
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

package uk.ac.sussex.gdsc.core.utils;

import java.awt.Rectangle;

/**
 * Contains methods for extracting parts of an image.
 */
public class ImageExtractor {
  private final float[] data;

  /** The width of the image. */
  public final int width;

  /** The height of the image. */
  public final int height;

  /**
   * Constructor.
   *
   * @param data The image data
   * @param width The image width
   * @param height The image height
   */
  ImageExtractor(float[] data, int width, int height) {
    this.data = data;
    this.width = width;
    this.height = height;
  }

  /**
   * Create a new image extractor wrapping the provided data.
   *
   * @param data the data
   * @param width the width
   * @param height the height
   * @return the image extractor
   */
  public static ImageExtractor wrap(float[] data, int width, int height) {
    return new ImageExtractor(data, width, height);
  }

  /**
   * Extract a region from the image.
   *
   * @param regionBounds The region to extract
   * @return The image region
   */
  public float[] crop(Rectangle regionBounds) {
    return crop(regionBounds, (float[]) null);
  }

  /**
   * Extract a region from the image. The region buffer will be allocated if smaller than the region
   * total length (width * height), otherwise it will be reused.
   *
   * @param regionBounds The region to extract
   * @param region A reusable buffer for the region
   * @return The image region
   */
  public float[] crop(Rectangle regionBounds, float[] region) {
    final float[] buffer = allocate(region, regionBounds.width * regionBounds.height);

    int offset1 = 0;
    for (int ys = regionBounds.y; ys < regionBounds.y + regionBounds.height; ys++) {
      int offset2 = ys * width + regionBounds.x;
      for (int xs = 0; xs < regionBounds.width; xs++) {
        buffer[offset1++] = data[offset2++];
      }
    }

    return buffer;
  }

  /**
   * Extract a region from the image. The region buffer will be allocated if smaller than the region
   * total length (width * height), otherwise it will be reused.
   *
   * @param regionBounds The region to extract
   * @param region A reusable buffer for the region
   * @return The image region
   */
  public double[] crop(Rectangle regionBounds, double[] region) {
    final double[] buffer = allocate(region, regionBounds.width * regionBounds.height);

    int offset1 = 0;
    for (int ys = regionBounds.y; ys < regionBounds.y + regionBounds.height; ys++) {
      int offset2 = ys * width + regionBounds.x;
      for (int xs = 0; xs < regionBounds.width; xs++) {
        buffer[offset1++] = data[offset2++];
      }
    }

    return buffer;
  }

  /**
   * Extract a region from the image.
   *
   * @param regionBounds The region to extract
   * @return The image region
   */
  public double[] cropToDouble(Rectangle regionBounds) {
    return crop(regionBounds, (double[]) null);
  }

  private static float[] allocate(float[] buffer, int size) {
    if (buffer == null || buffer.length < size) {
      return new float[size];
    }
    return buffer;
  }

  private static double[] allocate(double[] buffer, int size) {
    if (buffer == null || buffer.length < size) {
      return new double[size];
    }
    return buffer;
  }

  /**
   * Calculate a square region of size 2n+1 around the given coordinates. Respects the image
   * boundaries and so may return a non-square region.
   *
   * <p>A value of {@code n<0} is computed using {@code n=0}.
   *
   * <p>If there is no intersection this will return an empty rectangle.
   *
   * @param x the x
   * @param y the y
   * @param n the n
   * @return The region
   */
  public Rectangle getBoxRegionBounds(int x, int y, int n) {
    if (n < 0) {
      return intersection(x, y, x + 1L, y + 1L);
    }
    final long n1 = n + 1L;
    return intersection((long) x - n, (long) y - n, x + n1, y + n1);
  }

  /**
   * Compute the intersection with the given rectangle.
   *
   * <p>This has been adapted from {@link Rectangle#intersection(Rectangle)} with the assumption
   * that the origin x and y are zero. In contrast to the Rectangle method this will return an empty
   * rectangle when there is no intersection; the Rectangle method returns a partially computed
   * rectangle with at least one of width or height set to negative or zero.
   *
   * @param rx1 the rectangle x origin
   * @param ry1 the rectangle y origin
   * @param rx2 the rectangle x limit (exclusive)
   * @param ry2 the rectangle y limit (exclusive)
   * @return the rectangle
   */
  private Rectangle intersection(long rx1, long ry1, long rx2, long ry2) {
    // Intersect with this upper-left (0,0) and lower-right (width,height)
    final long tx1 = Math.max(0L, rx1);
    final long ty1 = Math.max(0L, ry1);
    final long tx2 = Math.min(this.width, rx2);
    final long ty2 = Math.min(this.height, ry2);

    // tx1,tx2 is always positive integer
    // (since rx1=x-n with n bounded to a positive integer, same for ry1).
    // tx2,ty2 is always below the maximum positive integer.
    // width = tx2 - tx1
    // height = ty2 - ty1
    // If the upper bounds are below the lower bounds there is no intersection.
    // Otherwise the width,height must be integer.
    if (tx2 <= tx1 || ty2 <= ty1) {
      // No intersection
      return new Rectangle(0, 0, 0, 0);
    }
    return new Rectangle((int) tx1, (int) ty1, (int) (tx2 - tx1), (int) (ty2 - ty1));
  }
}
