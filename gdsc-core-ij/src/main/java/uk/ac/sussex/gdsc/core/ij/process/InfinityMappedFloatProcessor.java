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

package uk.ac.sussex.gdsc.core.ij.process;

import ij.process.FloatProcessor;
import java.awt.image.ColorModel;

/**
 * Extends the ImageJ {@link FloatProcessor} class to map the min-max range to 1-255 in the 8-bit
 * image. Negative infinity is mapped to 0 in the LUT.
 *
 * <p>This allows display of a range of float data using the special marker -Infinity to ignore
 * pixels from display (assuming the LUT has black for 0). -Infinity is ignored by ImageJ for most
 * FloatProcessor functionality (histograms, min/max value, etc). This is not the case for NaN which
 * breaks ImageJ data display.
 *
 * <p>Note: This is not a native ImageJ class and can only override public or protected
 * functionality. Any package-private or private method to render the image will have default ImageJ
 * behaviour. This class is specialised for the display of float pixels in the ImageJ GUI and
 * display of composite images is not supported.
 *
 * @see FloatProcessor
 */
public class InfinityMappedFloatProcessor extends AbstractMappedFloatProcessor {
  private boolean mapPositiveInfinity;

  /**
   * Checks if positive infinity is mapped to zero.
   *
   * @return true, if positive infinity is mapped to zero
   */
  public boolean isMapPositiveInfinity() {
    return mapPositiveInfinity;
  }

  /**
   * Set to true to map positive infinity to zero.
   *
   * @param mapPositiveInfinity the new map positive infinity flag
   */
  public void setMapPositiveInfinity(boolean mapPositiveInfinity) {
    this.mapPositiveInfinity = mapPositiveInfinity;
  }

  /**
   * Creates a new MappedFloatProcessor using the specified pixel array.
   *
   * @param width the width
   * @param height the height
   * @param pixels the pixels
   */
  public InfinityMappedFloatProcessor(int width, int height, float[] pixels) {
    super(width, height, pixels);
  }

  /**
   * Creates a new MappedFloatProcessor using the specified pixel array and ColorModel.
   *
   * @param width the width
   * @param height the height
   * @param pixels the pixels
   * @param cm the colour model
   */
  public InfinityMappedFloatProcessor(int width, int height, float[] pixels, ColorModel cm) {
    super(width, height, pixels, cm);
  }

  /**
   * Creates a blank MappedFloatProcessor using the default grayscale LUT that displays zero as
   * black. Call invertLut() to display zero as white.
   *
   * @param width the width
   * @param height the height
   */
  public InfinityMappedFloatProcessor(int width, int height) {
    super(width, height);
  }

  /**
   * Creates a MappedFloatProcessor from an int array using the default grayscale LUT.
   *
   * @param width the width
   * @param height the height
   * @param pixels the pixels
   */
  public InfinityMappedFloatProcessor(int width, int height, int[] pixels) {
    super(width, height, pixels);
  }

  /**
   * Creates a MappedFloatProcessor from a double array using the default grayscale LUT.
   *
   * @param width the width
   * @param height the height
   * @param pixels the pixels
   */
  public InfinityMappedFloatProcessor(int width, int height, double[] pixels) {
    super(width, height, pixels);
  }

  /**
   * Creates a MappedFloatProcessor from a 2D float array using the default LUT.
   *
   * @param array the array
   */
  public InfinityMappedFloatProcessor(float[][] array) {
    super(array);
  }

  /**
   * Creates a MappedFloatProcessor from a 2D int array.
   *
   * @param array the array
   */
  public InfinityMappedFloatProcessor(int[][] array) {
    super(array);
  }

  @Override
  byte[] create8BitImage(boolean thresholding) {
    final int max = thresholding ? 254 : 255;
    // Map all values to the range 1-max. Negative infinity maps to zero.
    final int size = width * height;
    if (pixels8 == null) {
      pixels8 = new byte[size];
    }
    final float[] pixels = (float[]) getPixels();
    float value;
    int ivalue;

    // Default min/max
    final float min2 = (float) getMin();
    final float max2 = (float) getMax();

    final float scale = (max - 1f) / (max2 - min2);

    if (mapPositiveInfinity) {
      for (int i = 0; i < size; i++) {
        if (Float.isInfinite(pixels[i])) {
          // Infinity maps to zero.
          pixels8[i] = (byte) 0;
        } else {
          // Map all values to the range 1-255.
          value = pixels[i] - min2;
          ivalue = 1 + (int) ((value * scale) + 0.5f);
          if (ivalue >= max) {
            pixels8[i] = (byte) max;
          } else {
            pixels8[i] = (byte) ivalue;
          }
        }
      }
    } else {
      for (int i = 0; i < size; i++) {
        if (pixels[i] < min2) {
          // Below min maps to zero. This is -Infinity.
          pixels8[i] = (byte) 0;
        } else {
          // Map all values to the range 1-255.
          value = pixels[i] - min2;
          // Positive infinity should map to 255. So we do not add 1 before
          // comparing to max byte value as the cast of infinity will be the max
          // int value and adding 1 will rollover to negative.
          ivalue = (int) ((value * scale) + 0.5f);
          if (ivalue >= max) {
            pixels8[i] = (byte) max;
          } else {
            pixels8[i] = (byte) (1 + ivalue);
          }
        }
      }
    }
    return pixels8;
  }
}
