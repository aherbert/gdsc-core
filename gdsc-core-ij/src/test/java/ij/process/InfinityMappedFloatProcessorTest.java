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

package ij.process;

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
    final byte[] pixels = fp.create8BitImage();
    Assertions.assertSame(pixels, fp.create8BitImage());
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
    // Convert to byte
    final byte[] bytes = new byte[expected.length];
    for (int i = 0; i < expected.length; i++) {
      bytes[i] = (byte) expected[i];
    }
    Assertions.assertArrayEquals(bytes, fp.create8BitImage());
  }
}
