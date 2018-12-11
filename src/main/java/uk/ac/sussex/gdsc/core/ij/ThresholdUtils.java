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

package uk.ac.sussex.gdsc.core.ij;

import uk.ac.sussex.gdsc.core.threshold.AutoThreshold;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

import ij.ImageStack;
import ij.process.ImageProcessor;

import java.awt.Rectangle;

/**
 * Contains helper functions for ImageJ pixels.
 */
public final class ThresholdUtils {

  /**
   * No public construction.
   */
  private ThresholdUtils() {}

  /**
   * Creates a mask using the specified thresholding method.
   *
   * <p>The mask stack must be an 8 or 16 bit image.
   *
   * @param imageStack the image stack
   * @param method the method
   * @return the mask stack
   * @throws IllegalArgumentException If the bit depth is not 8/16 bit
   */
  public static ImageStack createMask(ImageStack imageStack, String method) {

    final int[] data = getHistogram(imageStack);

    final int threshold = AutoThreshold.getThreshold(method, data);

    final int width = imageStack.getWidth();
    final ImageStack maskStack = new ImageStack(width, imageStack.getHeight());
    final int size = imageStack.getWidth() * imageStack.getHeight();

    // Support ROIs
    final ImageProcessor ip = imageStack.getProcessor(1);
    final ImageProcessor mask = ip.getMask();
    final Rectangle rectangle = ip.getRoi();
    final int rx = rectangle.x;
    final int ry = rectangle.y;
    final int rw = rectangle.width;
    final int rh = rectangle.height;

    if (mask != null) {
      final byte[] mpixels = (byte[]) mask.getPixels();
      for (int s = 1; s <= imageStack.getSize(); s++) {
        final byte[] bp = new byte[size];
        for (int y = ry, my = 0; y < (ry + rh); y++, my++) {
          int index = y * width + rx;
          int mi = my * rw;
          for (int x = rx; x < (rx + rw); x++) {
            if (mpixels[mi] != 0 && ip.get(index) > threshold) {
              bp[index] = (byte) 255;
            }
            index++;
            mi++;
          }
        }
        maskStack.addSlice(null, bp);
      }
    } else {
      for (int s = 1; s <= imageStack.getSize(); s++) {
        final byte[] bp = new byte[size];
        for (int y = ry; y < (ry + rh); y++) {
          int index = y * width + rx;
          for (int x = rx; x < (rx + rw); x++) {
            if (ip.get(index) > threshold) {
              bp[index] = (byte) 255;
            }
            index++;
          }
        }
        maskStack.addSlice(null, bp);
      }
    }

    return maskStack;
  }

  /**
   * Gets a stack histogram.
   *
   * <p>The mask stack must be an 8 or 16 bit image.
   *
   * @param imageStack the image stack
   * @return the histogram
   * @throws IllegalArgumentException If the bit depth is not 8/16 bit
   */
  public static int[] getHistogram(ImageStack imageStack) {
    final ImageProcessor ip = imageStack.getProcessor(1);
    final int bitDepth = ip.getBitDepth();
    ValidationUtils.checkArgument(bitDepth == 8 || bitDepth == 16,
        "Require an 8/16 bit image: bit depth=%d", bitDepth);

    // Create an aggregate histogram
    final int[] data = ip.getHistogram();
    for (int s = 2; s <= imageStack.getSize(); s++) {
      final int[] temp = imageStack.getProcessor(s).getHistogram();
      for (int i = 0; i < data.length; i++) {
        data[i] += temp[i];
      }
    }

    return data;
  }
}
