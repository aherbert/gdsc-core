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

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.awt.Rectangle;
import java.net.URL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.ij.AlignImagesFft.SubPixelMethod;
import uk.ac.sussex.gdsc.core.utils.ImageWindow.WindowMethod;

@SuppressWarnings({"javadoc"})
class AlignImagesFftTest {

  static ImagePlus testImp;

  /**
   * Open the test image. It will be a float 32-bit image with power of 2 dimensions.
   *
   * @return the image
   */
  @SuppressWarnings("null")
  static ImagePlus openTestImp() {
    ImagePlus imp = testImp;
    if (imp == null) {
      try {
        final URL url =
            AlignImagesFftTest.class.getResource("/uk/ac/sussex/gdsc/core/ij/spots.tif");
        imp = new ImagePlus(url.getPath());
        // Convert to float
        imp.setProcessor(imp.getProcessor().toFloat(0, null));
        testImp = imp;
      } catch (Exception ex) {
        Assumptions.assumeFalse(true, "Failed to load test image");
      }
    }
    Assumptions.assumeTrue(imp != null);
    return imp.duplicate();
  }

  @Test
  void testSubPixelMethodName() {
    for (final SubPixelMethod m : SubPixelMethod.values()) {
      Assertions.assertNotEquals(m.name(), m.toString());
      Assertions.assertEquals(m.getName(), m.toString());
    }
  }

  @Test
  void testCreateHalfMaxBounds() {
    Assertions.assertEquals(new Rectangle(-5, -8, 10, 16),
        AlignImagesFft.createHalfMaxBounds(10, 16, 4, 6));
    Assertions.assertEquals(new Rectangle(-5, -8, 10, 16),
        AlignImagesFft.createHalfMaxBounds(11, 17, 4, 6));
    Assertions.assertEquals(new Rectangle(-5, -8, 10, 16),
        AlignImagesFft.createHalfMaxBounds(4, 6, 10, 16));
    Assertions.assertEquals(new Rectangle(-5, -8, 10, 16),
        AlignImagesFft.createHalfMaxBounds(4, 6, 11, 17));
  }

  @Test
  void testCreateBounds() {
    Assertions.assertEquals(new Rectangle(-1, -3, 3, 7), AlignImagesFft.createBounds(-1, 2, -3, 4));
  }

  @Test
  void testTranslateFloatProcessor() {
    // @formatter:off
    final FloatProcessor ip = (FloatProcessor) PixelUtils.wrap(5, 5, new float[] {
      0, 1, 1, 1, 0,
      0, 1, 2, 1, 0,
      0, 1, 2, 1, 0,
      0, 1, 1, 1, 0,
      0, 0, 0, 0, 0,
    });
    FloatProcessor ip2;
    ImageProcessor expected;
    // No interpolation
    ip2 = (FloatProcessor) AlignImagesFft.translate(ImageProcessor.BICUBIC, ip, 1, 2, false);
    Assertions.assertNotSame(ip, ip2);
    expected = ip.duplicate();
    expected.setInterpolationMethod(ImageProcessor.NONE);
    expected.translate(1, 2);
    Assertions.assertArrayEquals((float[]) expected.getPixels(), (float[]) ip2.getPixels());
    // Requires interpolation
    ip2 = (FloatProcessor) AlignImagesFft.translate(ImageProcessor.BICUBIC, ip, 1.5, 2, false);
    expected = ip.duplicate();
    expected.setInterpolationMethod(ImageProcessor.BICUBIC);
    expected.translate(1.5, 2);
    Assertions.assertArrayEquals((float[]) expected.getPixels(), (float[]) ip2.getPixels());
    ip2 = (FloatProcessor) AlignImagesFft.translate(ImageProcessor.BICUBIC, ip, 2, 1.5, false);
    expected = ip.duplicate();
    expected.setInterpolationMethod(ImageProcessor.BICUBIC);
    expected.translate(2, 1.5);
    Assertions.assertArrayEquals((float[]) expected.getPixels(), (float[]) ip2.getPixels());
    // Clipped
    ip2 = (FloatProcessor) AlignImagesFft.translate(ImageProcessor.BICUBIC, ip, 2, 1.5, true);
    final float[] pixels = (float[]) expected.getPixels();
    for (int i=0; i<pixels.length; i++) {
      if (pixels[i] > 2) {
        pixels[i] = 2;
      }
    }
    Assertions.assertArrayEquals((float[]) expected.getPixels(), (float[]) ip2.getPixels());
    // Requires no clipping
    ip2 = (FloatProcessor) AlignImagesFft.translate(ImageProcessor.BILINEAR, ip, 1.5, 2, false);
    expected = ip.duplicate();
    expected.setInterpolationMethod(ImageProcessor.BILINEAR);
    expected.translate(1.5, 2);
    Assertions.assertArrayEquals((float[]) expected.getPixels(), (float[]) ip2.getPixels());
  }

  @Test
  void testTranslateColorProcessor() {
    // @formatter:off
    final ColorProcessor ip = (ColorProcessor) PixelUtils.wrap(5, 5, new int[] {
      0, 1, 1, 1, 0,
      0, 1, 2, 1, 0,
      0, 1, 2, 1, 0,
      0, 1, 1, 1, 0,
      0, 0, 0, 0, 0,
    });
    ColorProcessor ip2;
    ImageProcessor expected;
    // Requires interpolation
    ip2 = (ColorProcessor) AlignImagesFft.translate(ImageProcessor.BICUBIC, ip, 1.5, 2, false);
    expected = ip.duplicate();
    expected.setInterpolationMethod(ImageProcessor.BICUBIC);
    expected.translate(1.5, 2);
    Assertions.assertArrayEquals((int[]) expected.getPixels(), (int[]) ip2.getPixels());
    // Clipping has not effect
    ip2 = (ColorProcessor) AlignImagesFft.translate(ImageProcessor.BICUBIC, ip, 1.5, 2, true);
    expected = ip.duplicate();
    expected.setInterpolationMethod(ImageProcessor.BICUBIC);
    expected.translate(1.5, 2);
    Assertions.assertArrayEquals((int[]) expected.getPixels(), (int[]) ip2.getPixels());
  }

  @Test
  void testPerformCubicFit() {
    // @formatter:off
    final FloatProcessor fp = (FloatProcessor) PixelUtils.wrap(5, 5, new float[] {
      0, 1, 1, 1, 0,
      0, 1, 2, 1, 0,
      0, 1, 2, 1, 0,
      0, 1, 1, 1, 0,
      0, 0, 0, 0, 0,
    });
    // @formatter:on
    double[] centre;
    centre = AlignImagesFft.performCubicFit(fp, 2, 2);
    Assertions.assertEquals(2, centre[0], 0.01);
    Assertions.assertEquals(1.5, centre[1], 0.01);
    centre = AlignImagesFft.performCubicFit(fp, 3, 2);
    Assertions.assertEquals(2, centre[0], 0.01);
    Assertions.assertEquals(1.5, centre[1], 0.01);
    centre = AlignImagesFft.performCubicFit(fp, 2, 1);
    Assertions.assertEquals(2, centre[0], 0.01);
    Assertions.assertEquals(1.5, centre[1], 0.01);
  }

  @Test
  void testProperties() {
    final AlignImagesFft align = new AlignImagesFft();
    for (final boolean value : new boolean[] {true, false}) {
      align.setDoTranslation(value);
      Assertions.assertEquals(value, align.isDoTranslation());
    }
    // This should be allowed
    align.setProgress(null);
  }

  @Test
  void testTransformTargetWithNull() {
    final AlignImagesFft align = new AlignImagesFft();
    FloatProcessor ip = new FloatProcessor(2, 2);
    ip.setf(0, 44);
    Assertions.assertNull(align.transformTarget(null, WindowMethod.TUKEY));
    Assertions.assertNull(align.transformTarget(ip, WindowMethod.TUKEY));
    align.initialiseReference(ip, WindowMethod.TUKEY, false);
    Assertions.assertNull(align.transformTarget(null, WindowMethod.TUKEY));
    Assertions.assertNotNull(align.transformTarget(ip, WindowMethod.TUKEY));
  }

  @Test
  void testAlignWithNull() {
    final AlignImagesFft align = new AlignImagesFft();
    FloatProcessor ip = new FloatProcessor(2, 2);
    ip.setf(0, 44);
    final WindowMethod windowMethod = WindowMethod.TUKEY;
    final Rectangle bounds = AlignImagesFft.createBounds(-10, 10, -10, 10);
    final SubPixelMethod subPixelMethod = SubPixelMethod.CUBIC;
    final int interpolationMethod = ImageProcessor.BICUBIC;
    final boolean normalised = false;
    final boolean clipOutput = false;

    ImagePlus targetImp = new ImagePlus(null, ip);
    Assertions.assertNull(align.align((ImagePlus) null, windowMethod, bounds, subPixelMethod,
        interpolationMethod, clipOutput));
    Assertions.assertNull(align.align(targetImp, windowMethod, bounds, subPixelMethod,
        interpolationMethod, clipOutput));
    Assertions.assertNull(align.align((ImageProcessor) null, windowMethod, bounds, subPixelMethod));
    Assertions.assertNull(align.align(ip, windowMethod, bounds, subPixelMethod));

    align.initialiseReference(ip, windowMethod, normalised);
    Assertions.assertNull(align.align((ImagePlus) null, windowMethod, bounds, subPixelMethod,
        interpolationMethod, clipOutput));
    Assertions.assertNull(align.align((ImageProcessor) null, windowMethod, bounds, subPixelMethod));
  }

  @Test
  void testAlignWithTargetLargerThanReference() {
    final AlignImagesFft align = new AlignImagesFft();
    FloatProcessor ip = new FloatProcessor(2, 2);
    ip.setf(0, 44);
    final WindowMethod windowMethod = WindowMethod.TUKEY;
    final Rectangle bounds = AlignImagesFft.createBounds(-10, 10, -10, 10);
    final SubPixelMethod subPixelMethod = SubPixelMethod.CUBIC;
    final int interpolationMethod = ImageProcessor.BICUBIC;
    final boolean normalised = false;
    final boolean clipOutput = false;
    align.initialiseReference(ip, windowMethod, normalised);

    FloatProcessor ip1 = new FloatProcessor(3, 2);
    FloatProcessor ip2 = new FloatProcessor(2, 3);

    Assertions.assertNull(align.align(new ImagePlus(null, ip1), windowMethod, bounds,
        subPixelMethod, interpolationMethod, clipOutput));
    Assertions.assertNull(align.align(new ImagePlus(null, ip2), windowMethod, bounds,
        subPixelMethod, interpolationMethod, clipOutput));

    Assertions.assertNull(align.align(ip1, windowMethod, bounds, subPixelMethod));
    Assertions.assertNull(align.align(ip2, windowMethod, bounds, subPixelMethod));
  }

  @Test
  void testAlign() {
    final ImagePlus imp = openTestImp();

    // Requires a big image to negate the effect of the edge window
    // Create an image that can be aligned
    FloatProcessor ip = imp.getProcessor().toFloat(0, null);
    // Crop to odd size
    ip.setRoi(0, 0, ip.getWidth() - 1, ip.getHeight() - 3);
    ip = (FloatProcessor) ip.crop();
    final int width = ip.getWidth();
    final int height = ip.getHeight();

    final ImageStack stack1 = new ImageStack(width, height);
    stack1.addSlice(new FloatProcessor(width, height));
    stack1.addSlice(ip.duplicate());
    stack1.addSlice(new FloatProcessor(width, height));

    final ImageStack stack2 = new ImageStack(width, height);
    stack2.addSlice(ip.duplicate());
    ip.setInterpolationMethod(ImageProcessor.BICUBIC);
    ip.translate(-1.5, -2.5);
    stack2.addSlice(ip.duplicate());
    ip.translate(-1, -2);
    stack2.addSlice(ip.duplicate());
    stack2.addSlice(new FloatProcessor(width, height));

    final ImagePlus imp1 = new ImagePlus(null, stack1);
    final ImagePlus imp2 = new ImagePlus(null, stack2);

    final AlignImagesFft align1 = new AlignImagesFft();
    final AlignImagesFft align2 = new AlignImagesFft();

    final WindowMethod windowMethod = WindowMethod.TUKEY;

    final Rectangle bounds = AlignImagesFft.createBounds(-10, 10, -10, 10);
    final SubPixelMethod subPixelMethod = SubPixelMethod.CUBIC;
    final int interpolationMethod = ImageProcessor.BICUBIC;
    final boolean normalised = false;
    final boolean showCorrelationImage = false;
    final boolean showNormalisedImage = false;
    final boolean clipOutput = false;

    // No alignment to empty image
    align1.align(imp1, imp2, windowMethod, bounds, subPixelMethod, interpolationMethod, normalised,
        showCorrelationImage, showNormalisedImage, clipOutput);
    Assertions.assertEquals(0, align1.getLastXOffset());
    Assertions.assertEquals(0, align1.getLastYOffset());

    // No alignment without pre-initialised reference
    align2.align(imp2, windowMethod, bounds, subPixelMethod, interpolationMethod, clipOutput);
    Assertions.assertEquals(0, align2.getLastXOffset());
    Assertions.assertEquals(0, align2.getLastYOffset());
    double[] shift;
    shift = align2.align(null, windowMethod, bounds, subPixelMethod);
    Assertions.assertNull(shift);
    Assertions.assertEquals(0, align2.getLastXOffset());
    Assertions.assertEquals(0, align2.getLastYOffset());
    shift = align2.align(imp2.getProcessor(), windowMethod, bounds, subPixelMethod);
    Assertions.assertNull(shift);
    Assertions.assertEquals(0, align2.getLastXOffset());
    Assertions.assertEquals(0, align2.getLastYOffset());

    // Set-up non-empty processor
    imp1.setPosition(2);
    align2.initialiseReference(imp1, windowMethod, normalised);
    // Clear
    align2.initialiseReference((ImagePlus) null, windowMethod, normalised);
    shift = align2.align(imp2.getProcessor(), windowMethod, bounds, subPixelMethod);
    Assertions.assertNull(shift);
    Assertions.assertEquals(0, align2.getLastXOffset());
    Assertions.assertEquals(0, align2.getLastYOffset());
    align2.initialiseReference((ImageProcessor) null, windowMethod, normalised);
    shift = align2.align(imp2.getProcessor(), windowMethod, bounds, subPixelMethod);
    Assertions.assertNull(shift);
    Assertions.assertEquals(0, align2.getLastXOffset());
    Assertions.assertEquals(0, align2.getLastYOffset());
    align2.initialiseReference(stack1.getProcessor(1), windowMethod, normalised);
    shift = align2.align(imp2.getProcessor(), windowMethod, bounds, subPixelMethod);
    Assertions.assertNull(shift);
    Assertions.assertEquals(0, align2.getLastXOffset());
    Assertions.assertEquals(0, align2.getLastYOffset());
    align2.initialiseReference(imp1, windowMethod, normalised);

    // Align
    ImagePlus result1 = align1.align(imp1, imp2, windowMethod, bounds, subPixelMethod,
        interpolationMethod, normalised, showCorrelationImage, showNormalisedImage, clipOutput);
    ImagePlus result2 =
        align2.align(imp2, windowMethod, bounds, subPixelMethod, interpolationMethod, clipOutput);
    // Align each slice
    final double[][] expected = {{0, 0}, {1.5, 2.5}, {2.5, 4.5}, {0, 0}};
    final double error = 0.1;
    for (int i = 0; i < expected.length; i++) {
      // Align single processor
      shift = align2.align(stack2.getProcessor(i + 1), windowMethod, bounds, subPixelMethod);
      Assertions.assertEquals(expected[i][0], align2.getLastXOffset(), expected[i][0] * error,
          "X shift");
      Assertions.assertEquals(expected[i][1], align2.getLastYOffset(), expected[i][1] * error,
          "Y shift");
      Assertions.assertEquals(shift[0], align2.getLastXOffset());
      Assertions.assertEquals(shift[1], align2.getLastYOffset());

      // Check aligned stacks
      result1.setPosition(i + 1);
      result2.setPosition(i + 1);
      Assertions.assertArrayEquals((float[]) result1.getProcessor().getPixels(),
          (float[]) result2.getProcessor().getPixels());

      // Align image
      ImagePlus result3 = align2.align(new ImagePlus(null, stack2.getProcessor(i + 1)),
          windowMethod, bounds, subPixelMethod, interpolationMethod, clipOutput);

      Assertions.assertArrayEquals((float[]) result1.getProcessor().getPixels(),
          (float[]) result3.getProcessor().getPixels());
    }
  }

  @Test
  void testAlignWithNormalisationAndNoInterpolation() {
    final ImagePlus imp = openTestImp();

    // Requires a big image to negate the effect of the edge window
    // Create an image that can be aligned
    final FloatProcessor ip = imp.getProcessor().toFloat(0, null);
    final int width = ip.getWidth();
    final int height = ip.getHeight();

    final ImageStack stack1 = new ImageStack(width, height);
    stack1.addSlice(new FloatProcessor(width, height));
    stack1.addSlice(ip.duplicate());
    stack1.addSlice(new FloatProcessor(width, height));

    ImageStack stack2 = new ImageStack(width, height);
    stack2.addSlice(ip.duplicate());
    ip.setInterpolationMethod(ImageProcessor.NONE);
    ip.translate(-1, -2);
    stack2.addSlice(ip.duplicate());
    ip.translate(-2, -3);
    stack2.addSlice(ip.duplicate());
    stack2.addSlice(new FloatProcessor(width, height));

    // For normalised interpolation the target should be smaller than the reference.
    // Works if the image is a power of 2 size.
    stack2 = stack2.crop(width / 4, height / 4, 0, width / 2, height / 2, stack2.size());

    final ImagePlus imp1 = new ImagePlus(null, stack1);
    final ImagePlus imp2 = new ImagePlus(null, stack2);

    final AlignImagesFft align1 = new AlignImagesFft();
    final AlignImagesFft align2 = new AlignImagesFft();

    final WindowMethod windowMethod = WindowMethod.NONE;

    final Rectangle bounds = AlignImagesFft.createBounds(-10, 10, -10, 10);
    final SubPixelMethod subPixelMethod = SubPixelMethod.NONE;
    final int interpolationMethod = ImageProcessor.NONE;
    final boolean normalised = true;
    final boolean showCorrelationImage = false;
    final boolean showNormalisedImage = false;
    final boolean clipOutput = false;

    // Set-up non-empty processor
    imp1.setPosition(2);
    align2.initialiseReference(imp1, windowMethod, normalised);
    ImagePlus result1 = align1.align(imp1, imp2, windowMethod, bounds, subPixelMethod,
        interpolationMethod, normalised, showCorrelationImage, showNormalisedImage, clipOutput);
    ImagePlus result2 =
        align2.align(imp2, windowMethod, bounds, subPixelMethod, interpolationMethod, clipOutput);
    // Align each slice
    final double[][] expected = {{0, 0}, {1, 2}, {3, 5}, {0, 0}};
    for (int i = 0; i < expected.length; i++) {
      // Align single processor
      final double[] shift =
          align2.align(stack2.getProcessor(i + 1), windowMethod, bounds, subPixelMethod);
      Assertions.assertEquals(expected[i][0], align2.getLastXOffset(), "X shift");
      Assertions.assertEquals(expected[i][1], align2.getLastYOffset(), "Y shift");
      Assertions.assertEquals(shift[0], align2.getLastXOffset());
      Assertions.assertEquals(shift[1], align2.getLastYOffset());

      // Check aligned stacks
      result1.setPosition(i + 1);
      result2.setPosition(i + 1);
      Assertions.assertArrayEquals((float[]) result1.getProcessor().getPixels(),
          (float[]) result2.getProcessor().getPixels());

      // Align image
      ImagePlus result3 = align2.align(new ImagePlus(null, stack2.getProcessor(i + 1)),
          windowMethod, bounds, subPixelMethod, interpolationMethod, clipOutput);

      Assertions.assertArrayEquals((float[]) result1.getProcessor().getPixels(),
          (float[]) result3.getProcessor().getPixels());
    }
  }
}
