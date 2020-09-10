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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class PixelUtilsTest {
  @Test
  void testCopyPixels() {
    final byte[] bytes = {1, 2, 3};
    final short[] shorts = {4, 5, 6};
    final float[] floats = {7, 8, 9};
    final int[] ints = {10, 11, 12};
    Assertions.assertNotSame(bytes, PixelUtils.copyPixels(bytes));
    Assertions.assertArrayEquals(bytes, (byte[]) PixelUtils.copyPixels(bytes));
    Assertions.assertNotSame(shorts, PixelUtils.copyPixels(shorts));
    Assertions.assertArrayEquals(shorts, (short[]) PixelUtils.copyPixels(shorts));
    Assertions.assertNotSame(floats, PixelUtils.copyPixels(floats));
    Assertions.assertArrayEquals(floats, (float[]) PixelUtils.copyPixels(floats));
    Assertions.assertNotSame(ints, PixelUtils.copyPixels(ints));
    Assertions.assertArrayEquals(ints, (int[]) PixelUtils.copyPixels(ints));
    Assertions.assertThrows(IllegalArgumentException.class, () -> PixelUtils.copyPixels(null));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> PixelUtils.copyPixels(new Object()));
  }

  @Test
  void testCopyBits() {
    final int width1 = 3;
    final int height1 = 2;
    final ImageStack stack1 = new ImageStack(width1, height1);
    stack1.addSlice(PixelUtils.wrap(width1, height1, new byte[] {0, 1, 2, 3, 4, 5}));
    stack1.addSlice(PixelUtils.wrap(width1, height1, new byte[] {6, 7, 8, 9, 10, 11}));
    final int width2 = 2;
    final int height2 = 1;
    final ImageStack stack2 = new ImageStack(width2, height2);
    stack2.addSlice(PixelUtils.wrap(width2, height2, new byte[] {1, 2}));

    PixelUtils.copyBits(stack1, stack2, Blitter.ADD);
    Assertions.assertArrayEquals(new byte[] {1, 3, 2, 3, 4, 5}, (byte[]) stack1.getPixels(1));
    Assertions.assertArrayEquals(new byte[] {6, 7, 8, 9, 10, 11}, (byte[]) stack1.getPixels(2));

    stack2.addSlice(PixelUtils.wrap(width2, height2, new byte[] {3, 4}));

    PixelUtils.copyBits(stack1, stack2, 0, 0, 0, Blitter.COPY);
    Assertions.assertArrayEquals(new byte[] {3, 4, 2, 3, 4, 5}, (byte[]) stack1.getPixels(1));
    Assertions.assertArrayEquals(new byte[] {6, 7, 8, 9, 10, 11}, (byte[]) stack1.getPixels(2));

    PixelUtils.copyBits(stack1, stack2, 2, 1, 2, Blitter.COPY);
    Assertions.assertArrayEquals(new byte[] {3, 4, 2, 3, 4, 5}, (byte[]) stack1.getPixels(1));
    Assertions.assertArrayEquals(new byte[] {6, 7, 8, 9, 10, 1}, (byte[]) stack1.getPixels(2));

    PixelUtils.copyBits(stack1, stack2, 1, 0, 1, Blitter.COPY);
    Assertions.assertArrayEquals(new byte[] {3, 1, 2, 3, 4, 5}, (byte[]) stack1.getPixels(1));
    Assertions.assertArrayEquals(new byte[] {6, 3, 4, 9, 10, 1}, (byte[]) stack1.getPixels(2));
  }

  @Test
  void testWrap() {
    final byte[] bytes = {1, 2, 3, 4, 5, 6};
    final short[] shorts = {4, 5, 6, 7, 8, 9};
    final float[] floats = {7, 8, 9, 10, 11, 12};
    final int[] ints = {10, 11, 12, 13, 14, 15};
    final int width = 2;
    final int height = 3;
    ImageProcessor ip;
    ip = PixelUtils.wrap(width, height, bytes);
    Assertions.assertTrue(ip instanceof ByteProcessor);
    Assertions.assertSame(bytes, ip.getPixels());
    Assertions.assertEquals(width, ip.getWidth());
    Assertions.assertEquals(height, ip.getHeight());

    ip = PixelUtils.wrap(width, height, shorts);
    Assertions.assertTrue(ip instanceof ShortProcessor);
    Assertions.assertSame(shorts, ip.getPixels());
    Assertions.assertEquals(width, ip.getWidth());
    Assertions.assertEquals(height, ip.getHeight());

    ip = PixelUtils.wrap(width, height, floats);
    Assertions.assertTrue(ip instanceof FloatProcessor);
    Assertions.assertSame(floats, ip.getPixels());
    Assertions.assertEquals(width, ip.getWidth());
    Assertions.assertEquals(height, ip.getHeight());

    ip = PixelUtils.wrap(width, height, ints);
    Assertions.assertTrue(ip instanceof ColorProcessor);
    Assertions.assertSame(ints, ip.getPixels());
    Assertions.assertEquals(width, ip.getWidth());
    Assertions.assertEquals(height, ip.getHeight());

    Assertions.assertThrows(IllegalArgumentException.class,
        () -> PixelUtils.wrap(width, height, null));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> PixelUtils.wrap(width, height, new Object()));
  }
}
