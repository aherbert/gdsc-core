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

package uk.ac.sussex.gdsc.core.ij.process;

import ij.process.FloatProcessor;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import uk.ac.sussex.gdsc.core.data.VisibleForTesting;

/**
 * Extends the ImageJ {@link FloatProcessor} class to map the min-max range to 1-255 in the 8-bit
 * image. The min is set to the first value above zero. All values below min are mapped to 0 in the
 * LUT.
 *
 * <p>Optionally +0.0f can be set as the min value mapped to 1. In this case -0.0f is still mapped
 * to 0. This allows for example display of the results of a probability calculation where 0 is a
 * valid display value. -0.0f can be used when no probability exists.
 *
 * <p>Note: This is not a native ImageJ class and can only override public or protected
 * functionality. Any package-private or private method to render the image will have default ImageJ
 * behaviour. This class is specialised for the display of float pixels in the ImageJ GUI and
 * display of composite images is not supported.
 *
 * @see FloatProcessor
 */
abstract class AbstractMappedFloatProcessor extends FloatProcessor {
  /**
   * Creates a new MappedFloatProcessor using the specified pixel array.
   *
   * @param width the width
   * @param height the height
   * @param pixels the pixels
   */
  AbstractMappedFloatProcessor(int width, int height, float[] pixels) {
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
  AbstractMappedFloatProcessor(int width, int height, float[] pixels, ColorModel cm) {
    super(width, height, pixels, cm);
  }

  /**
   * Creates a blank MappedFloatProcessor using the default grayscale LUT that displays zero as
   * black. Call invertLut() to display zero as white.
   *
   * @param width the width
   * @param height the height
   */
  AbstractMappedFloatProcessor(int width, int height) {
    super(width, height, new float[width * height], null);
  }

  /**
   * Creates a MappedFloatProcessor from an int array using the default grayscale LUT.
   *
   * @param width the width
   * @param height the height
   * @param pixels the pixels
   */
  AbstractMappedFloatProcessor(int width, int height, int[] pixels) {
    super(width, height, pixels);
  }

  /**
   * Creates a MappedFloatProcessor from a double array using the default grayscale LUT.
   *
   * @param width the width
   * @param height the height
   * @param pixels the pixels
   */
  AbstractMappedFloatProcessor(int width, int height, double[] pixels) {
    super(width, height, pixels);
  }

  /**
   * Creates a MappedFloatProcessor from a 2D float array using the default LUT.
   *
   * @param array the array
   */
  AbstractMappedFloatProcessor(float[][] array) {
    super(array);
  }

  /**
   * Creates a MappedFloatProcessor from a 2D int array.
   *
   * @param array the array
   */
  AbstractMappedFloatProcessor(int[][] array) {
    super(array);
  }

  // When an image is rendered in ImageJ it uses:
  // ImagePlus.getImage() or updateImage() which calls
  // FloatProcessor: public Image createImage()
  // This uses a private method create8BitImage(boolean).
  // Override at the createImage level.
  // Note: There are some cases where create8BitImage can be called from
  // other methods, e.g. the package-private level create8BitImage() method.
  // In these cases the 8-bit image will use the default mapping.

  /**
   * {@inheritDoc}
   *
   * <p>This method has been copied from ij.process.FloatProcessor (version 1.53f). The
   * create8BitImage implementation has been changed.
   */
  @Override
  public Image createImage() {
    final boolean thresholding = minThreshold != NO_THRESHOLD && lutUpdateMode < NO_LUT_UPDATE;
    final float[] pixels = (float[]) getPixels();

    // ***
    // Changed from ImageJ to always call this method.
    // The FloatProcessor does this if (firstTime || !lutAnimation)
    // There is no way to set the lutAnimation flag to true as
    // the setLutAnimation method sets the flag to false.
    // Updated to call an abstract method to generate the pixels.
    // ***
    create8BitImage(thresholding && lutUpdateMode == RED_LUT);
    // *** Changed: Ensure a colour model ***
    getColorModel();
    // Support the thresholding tool in ImageJ
    if (thresholding) {
      final int size = width * height;
      double value;
      if (lutUpdateMode == BLACK_AND_WHITE_LUT) {
        for (int i = 0; i < size; i++) {
          value = pixels[i];
          if (value >= minThreshold && value <= maxThreshold) {
            pixels8[i] = (byte) 255;
          } else {
            pixels8[i] = (byte) 0;
          }
        }
      } else {
        // threshold red
        for (int i = 0; i < size; i++) {
          value = pixels[i];
          if (value >= minThreshold && value <= maxThreshold) {
            pixels8[i] = (byte) 255;
          }
        }
      }
    }
    return createBufferedImageCopy();
  }

  /**
   * Gets the 8-bit image pixels.
   *
   * @return the pixels
   */
  @VisibleForTesting
  byte[] getPixels8() {
    return pixels8;
  }

  /**
   * Creates the buffered image.
   *
   * <p>This method has been copied from ij.process.FloatProcessor (version 1.53f).
   *
   * @return the image
   */
  private Image createBufferedImageCopy() {
    if (raster == null) {
      final SampleModel sm = getIndexSampleModel();
      final DataBuffer db = new DataBufferByte(pixels8, width * height, 0);
      raster = Raster.createWritableRaster(sm, db, null);
    }
    if (image == null || cm != cm2) {
      // *** Changed: cm cannot be null as it is created in createImage ***
      image = new BufferedImage(cm, raster, false, null);
      cm2 = cm;
    }
    lutAnimation = false;
    return image;
  }

  /**
   * An updated version of the method for rendering the 8-bit image.
   *
   * @param thresholding true if thresholding is enabled. This requires using the range 0-254 (i.e.
   *        255 is reserved)
   * @return the 8-bit image
   */
  abstract byte[] create8BitImage(boolean thresholding);

  /**
   * Creates the 8 bit pixels.
   *
   * @param size the size
   */
  void create8bitPixels(final int size) {
    if (pixels8 == null) {
      pixels8 = new byte[size];
    }
  }
}
