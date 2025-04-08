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

import ij.process.ImageProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class InfinityMappedFloatProcessorTest {
  @Test
  void testConstructors() {
    final int width = 3;
    final int height = 4;
    InfinityMappedFloatProcessor fp;
    fp = new InfinityMappedFloatProcessor(width, height);
    Assertions.assertFalse(fp.isMapPositiveInfinity());
    Assertions.assertEquals(width, fp.getWidth());
    Assertions.assertEquals(height, fp.getHeight());

    fp = new InfinityMappedFloatProcessor(width, height, new float[width * height]);
    Assertions.assertFalse(fp.isMapPositiveInfinity());
    Assertions.assertEquals(width, fp.getWidth());
    Assertions.assertEquals(height, fp.getHeight());

    // Check the color model is passed through
    java.awt.image.ColorModel cm = fp.getColorModel();

    fp = new InfinityMappedFloatProcessor(width, height, new float[width * height], cm);
    Assertions.assertFalse(fp.isMapPositiveInfinity());
    Assertions.assertEquals(width, fp.getWidth());
    Assertions.assertEquals(height, fp.getHeight());
    Assertions.assertSame(cm, fp.getColorModel());

    fp = new InfinityMappedFloatProcessor(width, height, new int[width * height]);
    Assertions.assertFalse(fp.isMapPositiveInfinity());
    Assertions.assertEquals(width, fp.getWidth());
    Assertions.assertEquals(height, fp.getHeight());

    fp = new InfinityMappedFloatProcessor(width, height, new double[width * height]);
    Assertions.assertFalse(fp.isMapPositiveInfinity());
    Assertions.assertEquals(width, fp.getWidth());
    Assertions.assertEquals(height, fp.getHeight());

    fp = new InfinityMappedFloatProcessor(new float[][] {{0, 1, 2}, {5, 6, 7}});
    Assertions.assertFalse(fp.isMapPositiveInfinity());
    Assertions.assertEquals(2, fp.getWidth());
    Assertions.assertEquals(3, fp.getHeight());

    fp = new InfinityMappedFloatProcessor(new int[][] {{0, 1, 2}, {5, 6, 7}});
    Assertions.assertFalse(fp.isMapPositiveInfinity());
    Assertions.assertEquals(2, fp.getWidth());
    Assertions.assertEquals(3, fp.getHeight());
  }

  @Test
  void testPixelCache() {
    final InfinityMappedFloatProcessor fp = new InfinityMappedFloatProcessor(10, 4);
    final java.awt.Image pixels = fp.createImage();
    Assertions.assertSame(pixels, fp.createImage());
  }

  @Test
  void testCreate8BitImage() {
    final float inf = Float.POSITIVE_INFINITY;
    assertCreate8BitImage(new int[] {1, 1, 1, 1}, 1, 4, new float[] {0, 0, 0, 0}, false);
    assertCreate8BitImage(new int[] {1, 86, 170, 255}, 1, 4, new float[] {1, 2, 3, 4}, false);
    assertCreate8BitImage(new int[] {1, 86, 170, 255}, 1, 4, new float[] {1, 2, 3, 4}, true);
    assertCreate8BitImage(new int[] {1, 86, 170, 255}, 1, 4, new float[] {0, 1, 2, 3}, false);
    assertCreate8BitImage(new int[] {1, 86, 170, 255}, 1, 4, new float[] {0, 1, 2, 3}, true);
    assertCreate8BitImage(new int[] {0, 1, 128, 255}, 1, 4, new float[] {-inf, 1, 2, 3}, false);
    assertCreate8BitImage(new int[] {0, 1, 170, 255}, 1, 4, new float[] {-inf, 0, 2, 3}, false);
    assertCreate8BitImage(new int[] {255, 1, 170, 255}, 1, 4, new float[] {inf, 0, 2, 3}, false);
    assertCreate8BitImage(new int[] {0, 1, 170, 255}, 1, 4, new float[] {inf, 0, 2, 3}, true);
  }

  private static void assertCreate8BitImage(int[] expected, int width, int height, float[] pixels,
      boolean mapInfinity) {
    final InfinityMappedFloatProcessor fp = new InfinityMappedFloatProcessor(width, height, pixels);
    fp.setMapPositiveInfinity(mapInfinity);
    assert8BitImage(fp, expected);
  }

  private static void assert8BitImage(final InfinityMappedFloatProcessor fp, final int[] expected) {
    // Convert to byte
    final byte[] bytes = new byte[expected.length];
    for (int i = 0; i < expected.length; i++) {
      bytes[i] = (byte) expected[i];
    }
    fp.createImage();
    Assertions.assertArrayEquals(bytes, fp.getPixels8());
  }

  @Test
  void testThresholdingModes() {
    final InfinityMappedFloatProcessor fp =
        new InfinityMappedFloatProcessor(1, 4, new float[] {1, 2, 3, 4});
    assert8BitImage(fp, new int[] {1, 86, 170, 255});
    fp.setThreshold(0, 100, ImageProcessor.NO_LUT_UPDATE);
    assert8BitImage(fp, new int[] {1, 86, 170, 255});
    fp.setThreshold(10, 100, ImageProcessor.BLACK_AND_WHITE_LUT);
    assert8BitImage(fp, new int[] {0, 0, 0, 0});
    fp.setThreshold(3, 100, ImageProcessor.BLACK_AND_WHITE_LUT);
    assert8BitImage(fp, new int[] {0, 0, 255, 255});
    fp.setThreshold(2, 3, ImageProcessor.BLACK_AND_WHITE_LUT);
    assert8BitImage(fp, new int[] {0, 255, 255, 0});
    fp.setThreshold(2, 3, ImageProcessor.RED_LUT);
    assert8BitImage(fp, new int[] {1, 255, 255, 254});
    fp.setThreshold(3, 100, ImageProcessor.RED_LUT);
    assert8BitImage(fp, new int[] {1, 85, 255, 255});
  }
}
