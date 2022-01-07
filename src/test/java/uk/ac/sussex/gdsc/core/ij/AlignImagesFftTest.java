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

package uk.ac.sussex.gdsc.core.ij;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.EnumSet;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.ij.AlignImagesFft.SubPixelMethod;
import uk.ac.sussex.gdsc.core.ij.process.Fht;
import uk.ac.sussex.gdsc.core.utils.ImageWindow.WindowMethod;
import uk.ac.sussex.gdsc.core.utils.LocalList;
import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.core.utils.Statistics;

@SuppressWarnings({"javadoc"})
class AlignImagesFftTest {

  static ImagePlus testImp;

  /**
   * Open the test image. It will be a float 32-bit image with power of 2 dimensions.
   *
   * <p>Requires a big image to negate the effect of the edge window, i.e. the image should be at
   * least 4-fold larger than the alignment shift.
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
      } catch (final Exception ex) {
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
    final FloatProcessor ip = new FloatProcessor(2, 2);
    ip.setf(0, 44);
    Assertions.assertNull(align.transformTarget(null, WindowMethod.NONE));
    Assertions.assertNull(align.transformTarget(ip, WindowMethod.NONE));
    align.initialiseReference(ip, WindowMethod.NONE, false);
    Assertions.assertNull(align.transformTarget(null, WindowMethod.NONE));
    Assertions.assertNotNull(align.transformTarget(ip, WindowMethod.NONE));
  }

  @Test
  void testIsValdid() {
    final ImageProcessor refIp = new FloatProcessor(4, 4);
    final ImagePlus targetImp = new ImagePlus(null, refIp);
    Assertions.assertFalse(AlignImagesFft.isValid(null, null));
    Assertions.assertFalse(AlignImagesFft.isValid(refIp, null));
    Assertions.assertFalse(AlignImagesFft.isValid(refIp, targetImp));
    refIp.set(0, 42);
    Assertions.assertTrue(AlignImagesFft.isValid(refIp, targetImp));
  }

  @Test
  void testAlignWithNull() {
    final AlignImagesFft align = new AlignImagesFft();
    final FloatProcessor ip = new FloatProcessor(2, 2);
    ip.setf(0, 44);
    final WindowMethod windowMethod = WindowMethod.NONE;
    final Rectangle bounds = AlignImagesFft.createBounds(-10, 10, -10, 10);
    final SubPixelMethod subPixelMethod = SubPixelMethod.CUBIC;
    final int interpolationMethod = ImageProcessor.BICUBIC;
    final boolean normalised = false;
    final boolean clipOutput = false;

    final ImagePlus targetImp = new ImagePlus(null, ip);
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
    final FloatProcessor ip = new FloatProcessor(2, 2);
    ip.setf(0, 44);
    final WindowMethod windowMethod = WindowMethod.NONE;
    final Rectangle bounds = AlignImagesFft.createBounds(-10, 10, -10, 10);
    final SubPixelMethod subPixelMethod = SubPixelMethod.CUBIC;
    final int interpolationMethod = ImageProcessor.BICUBIC;
    final boolean normalised = false;
    final boolean clipOutput = false;
    align.initialiseReference(ip, windowMethod, normalised);

    final FloatProcessor ip1 = new FloatProcessor(3, 2);
    final FloatProcessor ip2 = new FloatProcessor(2, 3);

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

    final Rectangle bounds = null;
    final SubPixelMethod subPixelMethod = SubPixelMethod.CUBIC;
    final int interpolationMethod = ImageProcessor.BICUBIC;
    final boolean normalised = false;
    final boolean clipOutput = false;

    // No alignment to empty image
    align1.align(imp1, imp2, windowMethod, bounds, subPixelMethod, interpolationMethod, normalised,
        null, null, null, clipOutput);
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
    final ImagePlus result1 = align1.align(imp1, imp2, windowMethod, bounds, subPixelMethod,
        interpolationMethod, normalised, null, null, null, clipOutput);
    final ImagePlus result2 =
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
      final ImagePlus result3 = align2.align(new ImagePlus(null, stack2.getProcessor(i + 1)),
          windowMethod, bounds, subPixelMethod, interpolationMethod, clipOutput);
      Assertions.assertEquals(shift[0], align2.getLastXOffset());
      Assertions.assertEquals(shift[1], align2.getLastYOffset());

      Assertions.assertArrayEquals((float[]) result1.getProcessor().getPixels(),
          (float[]) result3.getProcessor().getPixels());
    }
  }

  @Test
  void testAlignWithNormalisationAndNoInterpolation() {
    final ImagePlus imp = openTestImp();

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
    final boolean clipOutput = false;

    // Set-up non-empty processor
    imp1.setPosition(2);
    align2.initialiseReference(imp1, windowMethod, normalised);
    final ImagePlus result1 = align1.align(imp1, imp2, windowMethod, bounds, subPixelMethod,
        interpolationMethod, normalised, null, null, null, clipOutput);
    final ImagePlus result2 =
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
      final ImagePlus result3 = align2.align(new ImagePlus(null, stack2.getProcessor(i + 1)),
          windowMethod, bounds, subPixelMethod, interpolationMethod, clipOutput);
      Assertions.assertEquals(shift[0], align2.getLastXOffset());
      Assertions.assertEquals(shift[1], align2.getLastYOffset());

      Assertions.assertArrayEquals((float[]) result1.getProcessor().getPixels(),
          (float[]) result3.getProcessor().getPixels());
    }
  }

  @Test
  void testAlignWithConsumers() {
    final ImagePlus imp = openTestImp();

    final FloatProcessor ip = imp.getProcessor().toFloat(0, null);
    final int width = ip.getWidth();
    final int height = ip.getHeight();

    final ImageStack stack1 = new ImageStack(width, height);
    stack1.addSlice(ip.duplicate());

    ImageStack stack2 = new ImageStack(width, height);
    stack2.addSlice(ip.duplicate());
    ip.setInterpolationMethod(ImageProcessor.NONE);
    ip.translate(-1, -2);
    stack2.addSlice(ip.duplicate());
    ip.translate(-2, -3);
    stack2.addSlice(ip.duplicate());
    stack2.addSlice(new FloatProcessor(width, height));

    // Works if the image is a power of 2 size.
    stack2 = stack2.crop(width / 4, 0, 0, width / 2, height, stack2.size());

    final ImagePlus imp1 = new ImagePlus(null, stack1);
    final ImagePlus imp2 = new ImagePlus(null, stack2);

    final AlignImagesFft align1 = new AlignImagesFft();
    final AlignImagesFft align2 = new AlignImagesFft();

    final WindowMethod windowMethod = WindowMethod.HANNING;

    final Rectangle bounds = AlignImagesFft.createBounds(-10, 10, -10, 10);
    final SubPixelMethod subPixelMethod = SubPixelMethod.NONE;
    final int interpolationMethod = ImageProcessor.NONE;
    final boolean normalised = true;
    final boolean clipOutput = false;

    final LocalList<ImagePlus> l1 = new LocalList<>(1);
    final LocalList<ImagePlus> l2 = new LocalList<>(1);
    final LocalList<ImagePlus> l3 = new LocalList<>(1);

    final Consumer<ImagePlus> correlationImageAction = l1::add;
    final Consumer<ImagePlus> normalisedReferenceAction = l2::add;
    final Consumer<ImagePlus> normalisedTargetAction = l3::add;

    // Set-up non-empty processor
    align2.initialiseReference(imp1, windowMethod, normalised);
    final ImagePlus result1a = align1.align(imp1, imp2, windowMethod, bounds, subPixelMethod,
        interpolationMethod, normalised, correlationImageAction, null, null, clipOutput);

    Assertions.assertEquals(1, l1.size());
    // Image should be normalised to [-1, 1].
    ImageStack stack = l1.get(0).getImageStack();
    Assertions.assertEquals(4, stack.getSize());
    for (int s = 1; s <= stack.getSize(); s++) {
      final float[] pixels = (float[]) stack.getPixels(s);
      final float[] limits = MathUtils.limits(pixels);
      Assertions.assertTrue(limits[0] > -1.1);
      Assertions.assertTrue(limits[1] < 1.1);
      if (s == stack.getSize()) {
        // Last image should have all values 0
        Assertions.assertEquals(0, limits[0]);
        Assertions.assertEquals(0, limits[1]);
      }
    }

    final ImagePlus result1b = align1.align(imp1, imp2, windowMethod, bounds, subPixelMethod,
        interpolationMethod, normalised, null, normalisedReferenceAction, null, clipOutput);
    Assertions.assertEquals(1, l2.size());
    // Image should be normalised to have mean=0
    stack = l2.get(0).getImageStack();
    Assertions.assertEquals(1, stack.getSize());
    Assertions.assertEquals(0, MathUtils.sum((float[]) stack.getPixels(1)), 1e-3);

    final ImagePlus result1c = align1.align(imp1, imp2, windowMethod, bounds, subPixelMethod,
        interpolationMethod, normalised, null, null, normalisedTargetAction, clipOutput);
    Assertions.assertEquals(1, l3.size());
    // Image should be normalised to have mean=0 and sum of squares 1 (or zero for final frame).
    stack = l3.get(0).getImageStack();
    Assertions.assertEquals(4, stack.getSize());
    for (int s = 1; s < stack.getSize(); s++) {
      final float[] pixels = (float[]) stack.getPixels(s);
      final Statistics stats = Statistics.create(pixels);
      Assertions.assertEquals(0, stats.getMean(), 1e-3);
      Assertions.assertEquals(s == stack.size() ? 0 : 1, stats.getSumOfSquares(), 1e-3);
    }

    // Align each slice
    final double[][] expected = {{0, 0}, {1, 2}, {3, 5}, {0, 0}};
    for (int i = 0; i < expected.length; i++) {
      // Check aligned stacks
      result1a.setPosition(i + 1);
      result1b.setPosition(i + 1);
      result1c.setPosition(i + 1);

      // Align image
      final ImagePlus result3 = align2.align(new ImagePlus(null, stack2.getProcessor(i + 1)),
          windowMethod, bounds, subPixelMethod, interpolationMethod, clipOutput);
      Assertions.assertEquals(expected[i][0], align2.getLastXOffset(), "X shift");
      Assertions.assertEquals(expected[i][1], align2.getLastYOffset(), "Y shift");

      final float[] pixels = (float[]) result3.getProcessor().getPixels();
      Assertions.assertArrayEquals(pixels, (float[]) result1a.getProcessor().getPixels());
      Assertions.assertArrayEquals(pixels, (float[]) result1b.getProcessor().getPixels());
      Assertions.assertArrayEquals(pixels, (float[]) result1c.getProcessor().getPixels());
    }
  }

  @Test
  void testAlignWithInterrupted() {
    final ImagePlus imp = openTestImp();

    final FloatProcessor ip = imp.getProcessor().toFloat(0, null);
    final int width = ip.getWidth();
    final int height = ip.getHeight();

    final ImageStack stack1 = new ImageStack(width, height);
    stack1.addSlice(ip.duplicate());

    final ImageStack stack2 = new ImageStack(width, height);
    stack2.addSlice(ip.duplicate());
    ip.setInterpolationMethod(ImageProcessor.NONE);
    ip.translate(-1, -2);
    stack2.addSlice(ip.duplicate());

    final ImagePlus imp1 = new ImagePlus(null, stack1);
    final ImagePlus imp2 = new ImagePlus(null, stack2);

    final AlignImagesFft align1 = new AlignImagesFft();
    final AlignImagesFft align2 = new AlignImagesFft();

    final WindowMethod windowMethod = WindowMethod.HANNING;

    final Rectangle bounds = AlignImagesFft.createBounds(-10, 10, -10, 10);
    final SubPixelMethod subPixelMethod = SubPixelMethod.NONE;
    final int interpolationMethod = ImageProcessor.NONE;
    final boolean normalised = true;
    final boolean clipOutput = false;

    align2.initialiseReference(imp1, windowMethod, normalised);

    try {
      IJ.setKeyDown(KeyEvent.VK_ESCAPE);
      Assertions.assertNull(align1.align(imp1, imp2, windowMethod, bounds, subPixelMethod,
          interpolationMethod, normalised, null, null, null, clipOutput));
      Assertions.assertNull(align2.align(imp2, windowMethod, bounds, subPixelMethod,
          interpolationMethod, clipOutput));
    } finally {
      IJ.resetEscape();
    }
  }

  @Test
  void testAlignAndTranslate() {
    final ImagePlus imp = openTestImp();

    FloatProcessor ip = imp.getProcessor().toFloat(0, null);
    final int width = ip.getWidth();
    // Ensure padding by cropping. This ensure coverage is hit for padding.
    ip.setRoi(0, 0, width, ip.getHeight() - 1);
    ip = (FloatProcessor) ip.crop();
    final int height = ip.getHeight();

    final ImageStack stack1 = new ImageStack(width, height);
    stack1.addSlice(ip.duplicate());

    final ImageStack stack2 = new ImageStack(width, height);
    ip.setInterpolationMethod(ImageProcessor.NONE);
    ip.translate(-1, -2);
    stack2.addSlice(ip.duplicate());

    final ImagePlus imp1 = new ImagePlus(null, stack1);
    final ImagePlus imp2 = new ImagePlus(null, stack2);

    final AlignImagesFft align1 = new AlignImagesFft();

    final WindowMethod windowMethod = WindowMethod.HANNING;
    final Rectangle bounds = AlignImagesFft.createBounds(-10, 10, -10, 10);
    final SubPixelMethod subPixelMethod = SubPixelMethod.NONE;
    final int interpolationMethod = ImageProcessor.NONE;
    final boolean normalised = true;
    final boolean clipOutput = false;

    align1.setDoTranslation(true);
    final ImagePlus result1 = align1.align(imp1, imp2, windowMethod, bounds, subPixelMethod,
        interpolationMethod, normalised, null, null, null, clipOutput);
    Assertions.assertEquals(1, align1.getLastXOffset(), "X shift");
    Assertions.assertEquals(2, align1.getLastYOffset(), "Y shift");
    // Check centre pixels
    final ImageProcessor ip2 = result1.getProcessor();
    for (int y = height / 2 - 1; y <= height / 2 + 1; y++) {
      for (int x = width / 2 - 1; x <= width / 2 + 1; x++) {
        Assertions.assertEquals(ip.get(x - 1, y - 2), ip2.get(x, y));
      }
    }

    align1.setDoTranslation(false);
    final ImagePlus result2 = align1.align(imp1, imp2, windowMethod, bounds, subPixelMethod,
        interpolationMethod, normalised, null, null, null, clipOutput);
    Assertions.assertEquals(1, align1.getLastXOffset(), "X shift");
    Assertions.assertEquals(2, align1.getLastYOffset(), "Y shift");
    Assertions.assertArrayEquals((float[]) imp2.getProcessor().getPixels(),
        (float[]) result2.getProcessor().getPixels());
  }

  @Test
  void testAlignTargetFht() {
    final ImagePlus imp = openTestImp();

    final FloatProcessor ip = imp.getProcessor().toFloat(0, null);

    final AlignImagesFft align1 = new AlignImagesFft();

    final WindowMethod windowMethod = WindowMethod.HANNING;
    final Rectangle bounds = AlignImagesFft.createBounds(-10, 10, -10, 10);
    final SubPixelMethod subPixelMethod = SubPixelMethod.NONE;
    final boolean normalised = true;

    align1.initialiseReference(ip, windowMethod, normalised);

    ip.setInterpolationMethod(ImageProcessor.NONE);
    ip.translate(-1, -2);

    final double[] result1 = align1.align(ip.duplicate(), windowMethod, bounds, subPixelMethod);
    Assertions.assertEquals(1, result1[0], "X shift");
    Assertions.assertEquals(2, result1[1], "Y shift");

    // Align an FHT
    final Fht fht = align1.transformTarget(ip, windowMethod);
    final double[] result2 = align1.align(fht, windowMethod, bounds, subPixelMethod);
    Assertions.assertEquals(1, result2[0], "X shift");
    Assertions.assertEquals(2, result2[1], "Y shift");

    // Bad FHT (wrong size)
    ip.setRoi(0, 0, ip.getWidth() / 2, ip.getHeight() / 2);
    final Fht badFht1 = new Fht(ip.crop());
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> align1.align(badFht1, windowMethod, bounds, subPixelMethod));
  }

  @Test
  void testAlignToSelf() {
    final ImagePlus imp = openTestImp();

    final FloatProcessor ip = imp.getProcessor().toFloat(0, null);
    final int width = ip.getWidth();
    final int height = ip.getHeight();

    final ImageStack stack1 = new ImageStack(width, height);
    stack1.addSlice(ip.duplicate());
    ip.setInterpolationMethod(ImageProcessor.NONE);
    ip.translate(-1, -2);
    stack1.addSlice(ip.duplicate());

    final ImagePlus imp1 = new ImagePlus(null, stack1);

    final AlignImagesFft align1 = new AlignImagesFft();

    final WindowMethod windowMethod = WindowMethod.HANNING;
    final Rectangle bounds = AlignImagesFft.createBounds(-10, 10, -10, 10);
    final SubPixelMethod subPixelMethod = SubPixelMethod.NONE;
    final int interpolationMethod = ImageProcessor.NONE;
    final boolean normalised = true;
    final boolean clipOutput = false;

    final ImagePlus result1 = align1.align(imp1, null, windowMethod, bounds, subPixelMethod,
        interpolationMethod, normalised, null, null, null, clipOutput);
    Assertions.assertEquals(1, align1.getLastXOffset(), "X shift");
    Assertions.assertEquals(2, align1.getLastYOffset(), "Y shift");
    // Check centre pixels
    final ImageProcessor ip1 = result1.getProcessor();
    Assertions.assertArrayEquals((float[]) imp1.getProcessor().getPixels(),
        (float[]) ip1.getPixels());
    result1.setPosition(2);
    final ImageProcessor ip2 = result1.getProcessor();
    for (int y = height / 2 - 1; y <= height / 2 + 1; y++) {
      for (int x = width / 2 - 1; x <= width / 2 + 1; x++) {
        Assertions.assertEquals(ip.get(x - 1, y - 2), ip2.get(x, y));
      }
    }
  }

  @Test
  void testApplyWindowSeperable() {
    assertApplyWindow(AlignImagesFft::applyWindowSeparable);
  }

  @Test
  void testApplyWindow() {
    assertApplyWindow(AlignImagesFft::applyWindow);
  }

  private static void
      assertApplyWindow(BiFunction<ImageProcessor, WindowMethod, FloatProcessor> fun) {
    final ImagePlus imp = openTestImp();
    final ImageProcessor ip = imp.getProcessor();
    final EnumSet<WindowMethod> set = EnumSet.allOf(WindowMethod.class);
    final int[] widths = {1, 2, 3, ip.getWidth()};
    final int[] heights = {1, 2, 3, ip.getHeight()};
    for (final WindowMethod windowMethod : set) {
      for (final int w : widths) {
        for (final int h : heights) {
          ip.setRoi(0, 0, w, h);
          final FloatProcessor fp = fun.apply(ip.crop(), windowMethod);
          final Statistics stats = Statistics.create((float[]) fp.getPixels());
          Assertions.assertEquals(0, stats.getMean(), 1e-6,
              () -> String.format("%s : (%d x %d)", windowMethod, w, h));
        }
      }
    }
  }

  @Test
  void testNormaliseWithZeroImage() {
    final FloatProcessor fp = new FloatProcessor(4, 4);
    final FloatProcessor fp2 = AlignImagesFft.normaliseImage(fp);
    Assertions.assertArrayEquals(new float[16], (float[]) fp2.getPixels());
  }

  @Test
  void testAlignWithPartialZeroImage() {
    // Create a special image which already has a mean of zero thus no shift will be applied and
    // large sections can be zero.
    final FloatProcessor ip = new FloatProcessor(64, 64);
    ip.setf(32, 32, 4);
    ip.setf(32, 31, -1);
    ip.setf(32, 33, -1);
    ip.setf(31, 32, -1);
    ip.setf(33, 32, -1);
    // Align with a small image that may overlap entirely zero sections
    final FloatProcessor ip2 = new FloatProcessor(16, 16);
    ip2.setf(8, 8, 4);
    ip2.setf(8, 7, -1);
    ip2.setf(8, 9, -1);
    ip2.setf(7, 8, -1);
    ip2.setf(9, 8, -1);

    final ImagePlus imp1 = new ImagePlus(null, ip);
    final ImagePlus imp2 = new ImagePlus(null, ip2);
    final AlignImagesFft align1 = new AlignImagesFft();
    final WindowMethod windowMethod = WindowMethod.NONE;
    final Rectangle bounds = AlignImagesFft.createBounds(-10, 10, -10, 10);
    final SubPixelMethod subPixelMethod = SubPixelMethod.NONE;
    final int interpolationMethod = ImageProcessor.NONE;
    final boolean normalised = true;
    final boolean clipOutput = false;

    final LocalList<ImagePlus> l1 = new LocalList<>(1);
    final Consumer<ImagePlus> correlationImageAction = l1::add;

    align1.align(imp1, imp2, windowMethod, bounds, subPixelMethod, interpolationMethod, normalised,
        correlationImageAction, null, null, clipOutput);

    // The correlation should ignore normalisation when the sum of squares and mean are zero
    final ImageProcessor cp = l1.get(0).getProcessor();
    Assertions.assertEquals(1, cp.getf(32, 32), 1e-6);
    Assertions.assertEquals(0, cp.getf(24, 26), 1e-6);
    Assertions.assertEquals(0, cp.getf(4, 6), 1e-6);
  }
}
