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

package uk.ac.sussex.gdsc.core.ij.roi;

import uk.ac.sussex.gdsc.core.data.procedures.FValueProcedure;
import uk.ac.sussex.gdsc.core.data.procedures.IValueProcedure;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.awt.Rectangle;

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
   * For each pixel inside the ROI execute the procedure. If the ROI is null then all pixels will be
   * sampled.
   *
   * @param roi the roi
   * @param ip the image processor
   * @param procedure the procedure
   */
  public static void forEach(Roi roi, ImageProcessor ip, FValueProcedure procedure) {
    if (roi == null) {
      final int size = ip.getPixelCount();
      for (int i = 0; i < size; i++) {
        procedure.execute(ip.getf(i));
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
          procedure.execute(ip.getf(i));
        }
      }
    } else {
      int maskIndex = 0;
      for (int y = 0; y < rheight; y++) {
        for (int x = 0, i = (y + yOffset) * maxx + xOffset; x < rwidth; x++, i++, maskIndex++) {
          if (mask.get(maskIndex) != 0) {
            procedure.execute(ip.getf(i));
          }
        }
      }
    }
  }

  /**
   * For each pixel inside the ROI execute the procedure. If the ROI is null then all pixels will be
   * sampled.
   *
   * @param roi the roi
   * @param stack the stack
   * @param procedure the procedure
   */
  public static void forEach(Roi roi, ImageStack stack, FValueProcedure procedure) {
    if (roi == null) {
      for (int slice = 1; slice <= stack.getSize(); slice++) {
        final ImageProcessor ip = stack.getProcessor(slice);
        final int size = ip.getPixelCount();
        for (int i = 0; i < size; i++) {
          procedure.execute(ip.getf(i));
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
            procedure.execute(ip.getf(i));
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
              procedure.execute(ip.getf(i));
            }
          }
        }
      }
    }
  }

  /**
   * For each pixel inside the ROI execute the procedure. If the ROI is null then all pixels will be
   * sampled.
   *
   * @param roi the roi
   * @param ip the image processor
   * @param procedure the procedure
   */
  public static void forEach(Roi roi, ImageProcessor ip, IValueProcedure procedure) {
    if (roi == null) {
      final int size = ip.getPixelCount();
      for (int i = 0; i < size; i++) {
        procedure.execute(ip.get(i));
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
          procedure.execute(ip.get(i));
        }
      }
    } else {
      int maskIndex = 0;
      for (int y = 0; y < rheight; y++) {
        for (int x = 0, i = (y + yOffset) * maxx + xOffset; x < rwidth; x++, i++, maskIndex++) {
          if (mask.get(maskIndex) != 0) {
            procedure.execute(ip.get(i));
          }
        }
      }
    }
  }

  /**
   * For each pixel inside the ROI execute the procedure. If the ROI is null then all pixels will be
   * sampled.
   *
   * @param roi the roi
   * @param stack the stack
   * @param procedure the procedure
   */
  public static void forEach(Roi roi, ImageStack stack, IValueProcedure procedure) {
    if (roi == null) {
      final int size = stack.getHeight() * stack.getWidth();
      for (int slice = 1; slice <= stack.getSize(); slice++) {
        final ImageProcessor ip = stack.getProcessor(slice);
        for (int i = 0; i < size; i++) {
          procedure.execute(ip.get(i));
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
            procedure.execute(ip.get(i));
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
              procedure.execute(ip.get(i));
            }
          }
        }
      }
    }
  }
}
