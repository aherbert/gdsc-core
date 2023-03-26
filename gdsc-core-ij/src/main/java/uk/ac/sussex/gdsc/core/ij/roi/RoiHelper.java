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

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.awt.Rectangle;
import java.util.function.IntConsumer;

import uk.ac.sussex.gdsc.core.data.VisibleForTesting;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;
import uk.ac.sussex.gdsc.core.utils.function.FloatConsumer;

/**
 * Class for working with image ROIs.
 */
public final class RoiHelper {

  /** No construction. */
  private RoiHelper() {}

  /**
   * Build a byte mask of all pixels in an ROI. If no area ROI is present then the mask will be
   * null.
   *
   * @param imp The input image
   * @return a byte mask (255 inside the ROI, else 0)
   */
  public static ByteProcessor getMask(ImagePlus imp) {
    final Roi roi = imp.getRoi();
    if (roi == null || !roi.isArea()) {
      return null;
    }

    final int maxx = imp.getWidth();
    final int maxy = imp.getHeight();

    // Check if this is a standard rectangle ROI that covers the entire image
    if (roi.getType() == Roi.RECTANGLE && roi.getRoundRectArcSize() == 0) {
      final Rectangle roiBounds = roi.getBounds();
      if (roiBounds.width == maxx && roiBounds.height == maxy) {
        return null;
      }
    }

    final ByteProcessor bp = new ByteProcessor(maxx, maxy);
    bp.setColor(255);
    bp.fill(roi);
    return bp;
  }

  /**
   * For each pixel inside the ROI accept the procedure. If the ROI is null then all pixels will be
   * sampled.
   *
   * @param roi the roi
   * @param ip the image processor
   * @param procedure the procedure
   */
  public static void forEach(Roi roi, ImageProcessor ip, FloatConsumer procedure) {
    if (roi == null) {
      final int size = ip.getPixelCount();
      for (int i = 0; i < size; i++) {
        procedure.accept(ip.getf(i));
      }
      return;
    }

    // Ensure the roi bounds fit inside the processor
    final int maxx = ip.getWidth();
    final int maxy = ip.getHeight();
    final Rectangle roiBounds = roi.getBounds().intersection(new Rectangle(maxx, maxy));
    final int rwidth = roiBounds.width;
    final int rheight = roiBounds.height;
    if (rwidth == 0 || rheight == 0) {
      return;
    }
    final int xOffset = roiBounds.x;
    final int yOffset = roiBounds.y;

    final ImageProcessor mask = roi.getMask();
    if (mask == null) {
      for (int y = 0; y < rheight; y++) {
        for (int x = 0, i = (y + yOffset) * maxx + xOffset; x < rwidth; x++, i++) {
          procedure.accept(ip.getf(i));
        }
      }
    } else {
      int maskIndex = 0;
      for (int y = 0; y < rheight; y++) {
        for (int x = 0, i = (y + yOffset) * maxx + xOffset; x < rwidth; x++, i++, maskIndex++) {
          if (mask.get(maskIndex) != 0) {
            procedure.accept(ip.getf(i));
          }
        }
      }
    }
  }

  /**
   * For each pixel inside the ROI accept the procedure. If the ROI is null then all pixels will be
   * sampled.
   *
   * @param roi the roi
   * @param stack the stack
   * @param procedure the procedure
   */
  public static void forEach(Roi roi, ImageStack stack, FloatConsumer procedure) {
    if (roi == null) {
      for (int slice = 1; slice <= stack.getSize(); slice++) {
        final ImageProcessor ip = stack.getProcessor(slice);
        final int size = ip.getPixelCount();
        for (int i = 0; i < size; i++) {
          procedure.accept(ip.getf(i));
        }
      }
      return;
    }

    // Ensure the roi bounds fit inside the processor
    final int maxx = stack.getWidth();
    final int maxy = stack.getHeight();
    final Rectangle roiBounds = roi.getBounds().intersection(new Rectangle(maxx, maxy));
    final int rwidth = roiBounds.width;
    final int rheight = roiBounds.height;
    if (rwidth == 0 || rheight == 0) {
      return;
    }
    final int xOffset = roiBounds.x;
    final int yOffset = roiBounds.y;

    final ImageProcessor mask = roi.getMask();
    if (mask == null) {
      for (int slice = 1; slice <= stack.getSize(); slice++) {
        final ImageProcessor ip = stack.getProcessor(slice);
        for (int y = 0; y < rheight; y++) {
          for (int x = 0, i = (y + yOffset) * maxx + xOffset; x < rwidth; x++, i++) {
            procedure.accept(ip.getf(i));
          }
        }
      }
    } else {
      for (int slice = 1; slice <= stack.getSize(); slice++) {
        final ImageProcessor ip = stack.getProcessor(slice);
        int maskIndex = 0;
        for (int y = 0; y < rheight; y++) {
          for (int x = 0, i = (y + yOffset) * maxx + xOffset; x < rwidth; x++, i++, maskIndex++) {
            if (mask.get(maskIndex) != 0) {
              procedure.accept(ip.getf(i));
            }
          }
        }
      }
    }
  }

  /**
   * For each pixel inside the ROI accept the procedure. If the ROI is null then all pixels will be
   * sampled.
   *
   * @param roi the roi
   * @param ip the image processor
   * @param procedure the procedure
   */
  public static void forEach(Roi roi, ImageProcessor ip, IntConsumer procedure) {
    if (roi == null) {
      final int size = ip.getPixelCount();
      for (int i = 0; i < size; i++) {
        procedure.accept(ip.get(i));
      }
      return;
    }

    // Ensure the roi bounds fit inside the processor
    final int maxx = ip.getWidth();
    final int maxy = ip.getHeight();
    final Rectangle roiBounds = roi.getBounds().intersection(new Rectangle(maxx, maxy));
    final int rwidth = roiBounds.width;
    final int rheight = roiBounds.height;
    if (rwidth == 0 || rheight == 0) {
      return;
    }
    final int xOffset = roiBounds.x;
    final int yOffset = roiBounds.y;

    final ImageProcessor mask = roi.getMask();
    if (mask == null) {
      for (int y = 0; y < rheight; y++) {
        for (int x = 0, i = (y + yOffset) * maxx + xOffset; x < rwidth; x++, i++) {
          procedure.accept(ip.get(i));
        }
      }
    } else {
      int maskIndex = 0;
      for (int y = 0; y < rheight; y++) {
        for (int x = 0, i = (y + yOffset) * maxx + xOffset; x < rwidth; x++, i++, maskIndex++) {
          if (mask.get(maskIndex) != 0) {
            procedure.accept(ip.get(i));
          }
        }
      }
    }
  }

  /**
   * For each pixel inside the ROI accept the procedure. If the ROI is null then all pixels will be
   * sampled.
   *
   * @param roi the roi
   * @param stack the stack
   * @param procedure the procedure
   */
  public static void forEach(Roi roi, ImageStack stack, IntConsumer procedure) {
    if (roi == null) {
      final int size = stack.getHeight() * stack.getWidth();
      for (int slice = 1; slice <= stack.getSize(); slice++) {
        final ImageProcessor ip = stack.getProcessor(slice);
        for (int i = 0; i < size; i++) {
          procedure.accept(ip.get(i));
        }
      }
      return;
    }

    // Ensure the roi bounds fit inside the processor
    final int maxx = stack.getWidth();
    final int maxy = stack.getHeight();
    final Rectangle roiBounds = roi.getBounds().intersection(new Rectangle(maxx, maxy));
    final int rwidth = roiBounds.width;
    final int rheight = roiBounds.height;
    if (rwidth == 0 || rheight == 0) {
      return;
    }
    final int xOffset = roiBounds.x;
    final int yOffset = roiBounds.y;

    final ImageProcessor mask = roi.getMask();
    if (mask == null) {
      for (int slice = 1; slice <= stack.getSize(); slice++) {
        final ImageProcessor ip = stack.getProcessor(slice);
        for (int y = 0; y < rheight; y++) {
          for (int x = 0, i = (y + yOffset) * maxx + xOffset; x < rwidth; x++, i++) {
            procedure.accept(ip.get(i));
          }
        }
      }
    } else {
      for (int slice = 1; slice <= stack.getSize(); slice++) {
        final ImageProcessor ip = stack.getProcessor(slice);
        int maskIndex = 0;
        for (int y = 0; y < rheight; y++) {
          for (int x = 0, i = (y + yOffset) * maxx + xOffset; x < rwidth; x++, i++, maskIndex++) {
            if (mask.get(maskIndex) != 0) {
              procedure.accept(ip.get(i));
            }
          }
        }
      }
    }
  }

  /**
   * Return the largest bounding ROI that contains pixels with the same value as the target pixel.
   * The ROI shape is limited to a rectangle.
   *
   * @param ip the image processor
   * @param x the x position
   * @param y the y position
   * @return the largest bounding ROI
   * @throws IllegalArgumentException if the target pixel is not within the image
   */
  public static Roi largestBoundingRoi(ImageProcessor ip, int x, int y) {
    final int w = ip.getWidth();
    final int h = ip.getHeight();
    ValidationUtils.checkPositive(x, "x");
    ValidationUtils.checkPositive(y, "y");
    ValidationUtils.checkArgument(x < w, "x >= width");
    ValidationUtils.checkArgument(y < h, "y >= height");

    final byte[] mask = convertToMask(ip, x, y);

    // Find the vertical strip containing (x,y)
    int miny = y;
    while (miny > 0 && mask[(miny - 1) * w + x] == 0) {
      miny--;
    }
    int maxy = y;
    while (maxy + 1 < h && mask[(maxy + 1) * w + x] == 0) {
      maxy++;
    }

    // Starting at the horizontal strip containing (x,y), find the strips with lower y.
    // Strips are limited by the previous strip range [min, max].
    final int[][] lower = new int[y - miny + 1][2];
    int min = 0;
    int max = w - 1;
    for (int yy = y; yy >= miny; yy--) {
      min = minX(mask, w, x, yy, min);
      max = maxX(mask, w, x, yy, max);
      lower[yy - miny][0] = min;
      lower[yy - miny][1] = max;
    }
    // Starting at the horizontal strip above (x,y), find the strips with higher y.
    // Strips are limited by the previous strip range [min, max].
    final int[][] upper = new int[maxy - y][2];
    min = lower[y - miny][0];
    max = lower[y - miny][1];
    for (int yy = y + 1; yy <= maxy; yy++) {
      min = minX(mask, w, x, yy, min);
      max = maxX(mask, w, x, yy, max);
      upper[yy - y - 1][0] = min;
      upper[yy - y - 1][1] = max;
    }

    // Lower strips are ordered from miny to y, with a successively larger range for x.
    // For each lower strip, create rectangles within each successive upper strip and find largest.
    final Rectangle best = new Rectangle(0, 0);
    int size = 0;

    for (int i = 0; i < lower.length; i++) {
      final int[] l = lower[i];
      min = l[0];
      max = l[1];
      int height = lower.length - i;
      if (size < height * (max - min + 1)) {
        size = height * (max - min + 1);
        best.x = min;
        best.y = i + miny;
        best.width = max - min + 1;
        best.height = height;
      }
      for (final int[] u : upper) {
        min = Math.max(min, u[0]);
        max = Math.min(max, u[1]);
        height++;
        if (size < height * (max - min + 1)) {
          size = height * (max - min + 1);
          best.x = min;
          best.y = i + miny;
          best.width = max - min + 1;
          best.height = height;
        }
      }
    }

    return new Roi(best);
  }

  /**
   * Convert the processor to a mask where all values matching the target pixel are zero.
   *
   * @param ip the image processor
   * @param x the x position
   * @param y the y position
   * @return the mask
   */
  @VisibleForTesting
  static byte[] convertToMask(ImageProcessor ip, int x, int y) {
    // Convert to byte[]
    byte[] mask;
    if (ip instanceof ByteProcessor) {
      mask = ((byte[]) ip.getPixels()).clone();
      final byte v = mask[y * ip.getWidth() + x];
      for (int i = 0; i < mask.length; i++) {
        mask[i] = (byte) (mask[i] == v ? 0 : -1);
      }
    } else if (ip.getBitDepth() < 32) {
      mask = new byte[ip.getWidth() * ip.getHeight()];
      final int v = ip.get(x, y);
      for (int i = 0; i < mask.length; i++) {
        mask[i] = (byte) (ip.get(i) == v ? 0 : -1);
      }
    } else {
      mask = new byte[ip.getWidth() * ip.getHeight()];
      final float v = ip.getf(x, y);
      for (int i = 0; i < mask.length; i++) {
        mask[i] = (byte) (ip.getf(i) == v ? 0 : -1);
      }
    }
    return mask;
  }

  /**
   * Find the minimum x from the target index where the value is zero. Stops at the specified limit.
   *
   * @param mask the mask
   * @param w the width
   * @param x the x position
   * @param y the y position
   * @param minx the minimum allowed x
   * @return the minimum x
   */
  private static int minX(byte[] mask, int w, int x, int y, int minx) {
    int i = y * w + x;
    final int j = y * w + minx;
    while (i - 1 >= j && mask[i - 1] == 0) {
      i--;
    }
    // i >= j
    return i - j + minx;
  }

  /**
   * Find the maximum x from the target index where the value is zero. Stops at the specified limit.
   *
   * @param mask the mask
   * @param w the width
   * @param x the x position
   * @param y the y position
   * @param maxx the maximum allowed x
   * @return the maximum x
   */
  private static int maxX(byte[] mask, int w, int x, int y, int maxx) {
    int i = y * w + x;
    final int j = y * w + maxx;
    while (i + 1 <= j && mask[i + 1] == 0) {
      i++;
    }
    // i <= j
    return i - j + maxx;
  }
}
