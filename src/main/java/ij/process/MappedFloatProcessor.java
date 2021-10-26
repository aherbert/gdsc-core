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
 * Copyright (C) 2011 - 2021 Alex Herbert
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

package ij.process;

import java.awt.image.ColorModel;

/**
 * Extends the ImageJ {@link FloatProcessor} class to map the min-max range to 1-255 in the 8-bit
 * image. The min is set to the first value above zero. All values below min are mapped to 0 in the
 * LUT.
 *
 * <p>Optionally +0.0f can be set as the min value mapped to 1. In this case -0.0f is still mapped
 * to 0. This allows for example display of the results of a probability calculation where 0 is a
 * valid display value. -0.0f can be used when no probability exists.
 *
 * <p>Note: This is not a native ImageJ class but must exist in the {@code ij.process} package to
 * override package private methods.
 *
 * @see FloatProcessor
 */
public class MappedFloatProcessor extends FloatProcessor {
  private static final int NEGATIVE_ZERO = Float.floatToRawIntBits(-0.0f);
  private static final int MAX_BYTE_VALUE = 255;

  private boolean mapZero;

  /**
   * If set to true positive zero is mapped to 1 in the LUT. The default maps the first value above
   * zero to 1 in the LUT.
   *
   * @return true, if is map zero
   */
  public boolean isMapZero() {
    return mapZero;
  }

  /**
   * Set to true to map positive zero to 1 in the LUT. The default maps the first value above zero
   * to 1 in the LUT.
   *
   * @param mapZero the new map zero value
   */
  public void setMapZero(boolean mapZero) {
    this.mapZero = mapZero;
  }

  /**
   * Creates a new MappedFloatProcessor using the specified pixel array.
   *
   * @param width the width
   * @param height the height
   * @param pixels the pixels
   */
  public MappedFloatProcessor(int width, int height, float[] pixels) {
    this(width, height, pixels, null);
  }

  /**
   * Creates a new MappedFloatProcessor using the specified pixel array and ColorModel.
   *
   * @param width the width
   * @param height the height
   * @param pixels the pixels
   * @param cm the colour model
   */
  public MappedFloatProcessor(int width, int height, float[] pixels, ColorModel cm) {
    super(width, height, pixels, cm);
  }

  /**
   * Creates a blank MappedFloatProcessor using the default grayscale LUT that displays zero as
   * black. Call invertLut() to display zero as white.
   *
   * @param width the width
   * @param height the height
   */
  public MappedFloatProcessor(int width, int height) {
    super(width, height, new float[width * height], null);
  }

  /**
   * Creates a MappedFloatProcessor from an int array using the default grayscale LUT.
   *
   * @param width the width
   * @param height the height
   * @param pixels the pixels
   */
  public MappedFloatProcessor(int width, int height, int[] pixels) {
    super(width, height, pixels);
  }

  /**
   * Creates a MappedFloatProcessor from a double array using the default grayscale LUT.
   *
   * @param width the width
   * @param height the height
   * @param pixels the pixels
   */
  public MappedFloatProcessor(int width, int height, double[] pixels) {
    super(width, height, pixels);
  }

  /**
   * Creates a MappedFloatProcessor from a 2D float array using the default LUT.
   *
   * @param array the array
   */
  public MappedFloatProcessor(float[][] array) {
    super(array);
  }

  /**
   * Creates a MappedFloatProcessor from a 2D int array.
   *
   * @param array the array
   */
  public MappedFloatProcessor(int[][] array) {
    super(array);
  }

  // scale from float to 8-bits
  @Override
  protected byte[] create8BitImage() {
    // In previous versions this method was protected. In ImageJ 1.52i it has changed to be
    // package private. This class has been moved into the ij.process package to support
    // the functionality.

    // Map all non zero values to the range 1-255.
    //
    // Optionally map +zero to the range 1-255 as well.
    //
    // Must find the minimum value above zero. This will be mapped to 1. Or special case is mapping
    // +0f to 1 but -0f to 0.

    final float[] pixels = (float[]) getPixels();
    final int size = pixels.length;
    create8bitPixels(size);

    // Get the default min/max and ensure above zero.
    float minAbove0 = Math.max(0, (float) getMin());
    final float maxAbove0 = Math.max(0, (float) getMax());

    // Get minimum above zero
    if (minAbove0 == 0 && maxAbove0 > 0 && !isMapZero()) {
      minAbove0 = maxAbove0;
      for (int i = 0; i < size; i++) {
        if (pixels[i] > 0 && minAbove0 > pixels[i]) {
          minAbove0 = pixels[i];
        }
      }
    }

    final float scale = 254f / (maxAbove0 - minAbove0);

    if (isMapZero() && minAbove0 == 0) {
      mapIncludingZero(pixels, size, scale);
    } else {
      mapAboveZero(pixels, size, minAbove0, scale);
    }
    return pixels8;
  }

  /**
   * Creates the 8 bit pixels.
   *
   * @param size the size
   */
  private void create8bitPixels(final int size) {
    if (pixels8 == null) {
      pixels8 = new byte[size];
    }
  }

  /**
   * Map all {@code value >= +0} to the range 1-255. {@code -0} and below maps to 0.
   *
   * @param pixels the pixels
   * @param size the size
   * @param scale the scale
   */
  private void mapIncludingZero(final float[] pixels, final int size, final float scale) {
    // We map equal or below -0 to 0.
    // Special case of mapping +0 to 1.
    for (int i = 0; i < size; i++) {
      if (pixels[i] < 0) {
        // Below zero maps to zero
        pixels8[i] = (byte) 0;
        continue;
      }

      // Special case where we must check for -0 or +0
      if (pixels[i] == 0 && Float.floatToRawIntBits(pixels[i]) == NEGATIVE_ZERO) {
        pixels8[i] = (byte) 0;
      } else {
        // +0 or above maps to 1-255
        pixels8[i] = (byte) Math.min(MAX_BYTE_VALUE, 1 + (int) ((pixels[i] * scale) + 0.5f));
      }
    }
  }


  /**
   * Map all {@code value > 0} to the range 1-255. {@code 0} and below maps to 0.
   *
   * @param pixels the pixels
   * @param size the size
   * @param minAbove0 the min above 0
   * @param scale the scale
   */
  private void mapAboveZero(final float[] pixels, final int size, float minAbove0,
      final float scale) {
    for (int i = 0; i < size; i++) {
      // This also checks == 0 as the minAboveZero may be zero.
      if (pixels[i] < minAbove0 || pixels[i] == 0) {
        // Below min (or zero) maps to zero
        pixels8[i] = (byte) 0;
      } else {
        // Map all non zero values to the range 1-255.
        pixels8[i] =
            (byte) Math.min(MAX_BYTE_VALUE, 1 + Math.round((pixels[i] - minAbove0) * scale));
      }
    }
  }
}
