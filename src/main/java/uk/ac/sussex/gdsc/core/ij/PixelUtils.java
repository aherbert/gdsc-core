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
 * Copyright (C) 2011 - 2020 Alex Herbert
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
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

/**
 * Contains helper functions for ImageJ pixels.
 */
public final class PixelUtils {

  /**
   * No public construction.
   */
  private PixelUtils() {}

  /**
   * Copy the pixels.
   *
   * <p>Supports: {@code byte[]}; {@code short[]}; {@code float[]}; {@code int[]}.
   *
   * @param pixels the pixels
   * @return the object
   * @throws IllegalArgumentException If the input is not an ImageJ pixels array
   */
  public static Object copyPixels(Object pixels) {
    if (pixels instanceof byte[]) {
      return ((byte[]) pixels).clone();
    }
    if (pixels instanceof short[]) {
      return ((short[]) pixels).clone();
    }
    if (pixels instanceof float[]) {
      return ((float[]) pixels).clone();
    }
    if (pixels instanceof int[]) {
      return ((int[]) pixels).clone();
    }
    throw new IllegalArgumentException("Unsupported pixels type");
  }

  /**
   * Copies the pixels in {@code stack2} to ({@code xloc,yloc}) in {@code stack1} using one of the
   * transfer modes defined in the {@link Blitter} interface.
   *
   * <p>Note the {@link ImageProcessor#copyBits(ImageProcessor, int, int, int)} method uses
   * zero-indexed x and y locations but an image stack uses 1-based index for the first slice.
   *
   * <p>If the stack ranges do not overlap then nothing is done.
   *
   * @param stack1 the stack 1
   * @param stack2 the stack 2
   * @param xloc the x location
   * @param yloc the y location
   * @param zloc the z location (1-indexed)
   * @param mode the mode
   */
  public static void copyBits(ImageStack stack1, ImageStack stack2, int xloc, int yloc, int zloc,
      int mode) {
    // Compute the intersection of the slice ranges:
    int s1;
    int s2;
    if (zloc < 1) {
      //@formatter:off
      // 1:          |--------------|
      //             s1
      // 2: |--------------|
      //             s2
      //@formatter:on
      s1 = 1;
      s2 = 2 - zloc;
    } else {
      //@formatter:off
      // 1: |--------------|
      //             s1
      // 2:          |--------------|
      //             s2
      //@formatter:on
      s1 = zloc;
      s2 = 1;
    }

    while (s1 <= stack1.getSize() && s2 < stack2.getSize()) {
      stack1.getProcessor(s2).copyBits(stack2.getProcessor(s2), xloc, yloc, mode);
      s1++;
      s2++;
    }
  }

  /**
   * Copies the pixels in {@code stack2} to (xloc, yloc) in {@code stack1} using one of the transfer
   * modes defined in the {@link Blitter} interface.
   *
   * <p>A full copy is done using an x,y origin of 0,0 and a slice origin of 1.
   *
   * @param stack1 the stack 1
   * @param stack2 the stack 2
   * @param mode the mode
   */
  public static void copyBits(ImageStack stack1, ImageStack stack2, int mode) {
    copyBits(stack1, stack2, 0, 0, 1, mode);
  }

  /**
   * Wrap the pixels with an ImageProcessor.
   *
   * <p>Supports: {@code byte[]}; {@code short[]}; {@code float[]}; {@code int[]}.
   *
   * @param width the width
   * @param height the height
   * @param pixels the pixels
   * @return the ImageProcessor
   * @throws IllegalArgumentException If the input is not an ImageJ pixels array
   */
  public static ImageProcessor wrap(int width, int height, Object pixels) {
    if (pixels instanceof byte[]) {
      return new ByteProcessor(width, height, (byte[]) pixels);
    }
    if (pixels instanceof short[]) {
      return new ShortProcessor(width, height, (short[]) pixels, null);
    }
    if (pixels instanceof float[]) {
      return new FloatProcessor(width, height, (float[]) pixels);
    }
    if (pixels instanceof int[]) {
      return new ColorProcessor(width, height, (int[]) pixels);
    }
    throw new IllegalArgumentException("Unsupported pixels type");
  }
}
