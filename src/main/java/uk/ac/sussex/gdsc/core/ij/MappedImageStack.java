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
 * Copyright (C) 2011 - 2019 Alex Herbert
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

import ij.ImageStack;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.MappedFloatProcessor;
import java.awt.image.ColorModel;

/**
 * Extends the ImageJ {@link ImageStack} class to support a mapped float processor for float data.
 *
 * @see MappedFloatProcessor
 */
public class MappedImageStack extends ImageStack {
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

  /** Default constructor. */
  public MappedImageStack() {
    // Do nothing
  }

  /**
   * Creates a new, empty image stack.
   *
   * @param width the width
   * @param height the height
   */
  public MappedImageStack(int width, int height) {
    this(width, height, null);
  }

  /**
   * Creates a new, empty image stack with a capacity of 'size'. All 'size' slices and labels of
   * this image stack are initially null.
   *
   * @param width the width
   * @param height the height
   * @param size the size
   */
  public MappedImageStack(int width, int height, int size) {
    super(width, height, size);
  }

  /**
   * Creates a new, empty image stack using the specified color model.
   *
   * @param width the width
   * @param height the height
   * @param cm the colour model
   */
  public MappedImageStack(int width, int height, ColorModel cm) {
    super(width, height, cm);
  }

  @Override
  public ImageProcessor getProcessor(int n) {
    ImageProcessor ip = super.getProcessor(n);
    if (ip instanceof FloatProcessor) {
      final MappedFloatProcessor fp = new MappedFloatProcessor(getWidth(), getHeight(),
          (float[]) ip.getPixels(), ip.getColorModel());
      fp.setMapZero(mapZero);
      fp.setMinAndMax(ip.getMin(), ip.getMax());
      if (ip.getCalibrationTable() != null) {
        fp.setCalibrationTable(ip.getCalibrationTable());
      }
      ip = fp;
    }
    return ip;
  }
}
