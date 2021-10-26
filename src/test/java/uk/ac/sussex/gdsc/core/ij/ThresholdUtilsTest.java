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

package uk.ac.sussex.gdsc.core.ij;

import ij.ImageStack;
import ij.process.ImageProcessor;
import java.awt.Rectangle;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.threshold.AutoThreshold;
import uk.ac.sussex.gdsc.core.threshold.AutoThreshold.Method;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.functions.IndexSupplier;

@SuppressWarnings({"javadoc"})
class ThresholdUtilsTest {
  @Test
  void canCreateHistogram8Bit() {
    final int width = 3;
    final int height = 4;
    final ImageStack stack = new ImageStack(width, height, 2);
    stack.setPixels(new byte[] {0, 0, 3, 3, 0, 0, 2, 0, 0, 0, 0, 0}, 1);
    stack.setPixels(new byte[] {0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1}, 2);
    final int[] h = new int[256];
    h[0] = 17;
    h[1] = 4;
    h[2] = 1;
    h[3] = 2;
    Assertions.assertArrayEquals(h, ThresholdUtils.getHistogram(stack));
  }

  @Test
  void canCreateHistogram16Bit() {
    final int width = 3;
    final int height = 4;
    final ImageStack stack = new ImageStack(width, height, 2);
    stack.setPixels(new short[] {0, 0, 3, 3, 0, 0, 2, 0, 0, 0, 0, 0}, 1);
    stack.setPixels(new short[] {0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1}, 2);
    final int[] h = new int[1 << 16];
    h[0] = 17;
    h[1] = 4;
    h[2] = 1;
    h[3] = 2;
    Assertions.assertArrayEquals(h, ThresholdUtils.getHistogram(stack));
  }

  @Test
  void testCreateHistogramThrows() {
    final int width = 3;
    final int height = 4;
    final ImageStack stack = new ImageStack(width, height, 2);
    stack.setPixels(new float[] {0, 0, 3, 3, 0, 0, 2, 0, 0, 0, 0, 0}, 1);
    stack.setPixels(new float[] {0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1}, 2);
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> ThresholdUtils.getHistogram(stack));
  }

  @Test
  void canCreateMask() {
    final UniformRandomProvider rng = RngUtils.create(1234L);
    final int width = 30;
    final int height = 40;
    final ImageStack stack = new ImageStack(width, height, 2);
    stack.setPixels(random8Bit(rng, width, height), 1);
    stack.setPixels(random8Bit(rng, width, height), 2);
    // Assume getHistogram works
    final Method method = Method.OTSU;
    final int threshold = AutoThreshold.getThreshold(method, ThresholdUtils.getHistogram(stack));
    final ImageStack mask = ThresholdUtils.createMask(stack, method.toString());
    Assertions.assertEquals(stack.getWidth(), mask.getWidth());
    Assertions.assertEquals(stack.getHeight(), mask.getHeight());
    Assertions.assertEquals(stack.getSize(), mask.getSize());
    final IndexSupplier msg = new IndexSupplier(2);
    for (int i = 1; i <= mask.getSize(); i++) {
      msg.set(0, i);
      final ImageProcessor ip = stack.getProcessor(i);
      final ImageProcessor maskIp = mask.getProcessor(i);
      final int count = ip.getPixelCount();
      for (int j = 0; j < count; j++) {
        Assertions.assertEquals(ip.get(j) > threshold ? 255 : 0, maskIp.get(j), msg.set(1, j));
      }
    }
  }

  @Test
  void canCreateMaskWithRectangleRoi() {
    final UniformRandomProvider rng = RngUtils.create(1234L);
    final int width = 30;
    final int height = 40;
    final ImageStack stack = new ImageStack(width, height, 2);
    stack.setPixels(random8Bit(rng, width, height), 1);
    stack.setPixels(random8Bit(rng, width, height), 2);
    final Rectangle roi = new Rectangle(10, 12, 13, 14);
    stack.setRoi(roi);
    // Assume getHistogram works
    final Method method = Method.OTSU;
    final int threshold = AutoThreshold.getThreshold(method, ThresholdUtils.getHistogram(stack));
    final ImageStack mask = ThresholdUtils.createMask(stack, method.toString());
    Assertions.assertEquals(stack.getWidth(), mask.getWidth());
    Assertions.assertEquals(stack.getHeight(), mask.getHeight());
    Assertions.assertEquals(stack.getSize(), mask.getSize());
    final IndexSupplier msg = new IndexSupplier(2);
    for (int i = 1; i <= mask.getSize(); i++) {
      msg.set(0, i);
      final ImageProcessor ip = stack.getProcessor(i);
      final ImageProcessor maskIp = mask.getProcessor(i);
      int j = 0;
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++, j++) {
          Assertions.assertEquals(roi.contains(x, y) && ip.get(j) > threshold ? 255 : 0,
              maskIp.get(j), msg.set(1, j));
        }
      }
    }
  }

  private static byte[] random8Bit(UniformRandomProvider rng, int width, int height) {
    final byte[] data = new byte[width * height];
    for (int i = 0; i < data.length; i++) {
      data[i] = (byte) rng.nextInt();
    }
    return data;
  }
}
