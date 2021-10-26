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

package uk.ac.sussex.gdsc.core.utils;

import java.awt.Rectangle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class ImageExtractorTest {
  @Test
  void canCrop() {
    // @formatter:off
    final float[] data = {
        0,  1,  2,  3,  4,
        5,  6,  7,  8,  9,
       10, 11, 12, 13, 14,
       15, 16, 17, 18, 19
    };
    // @formatter:on
    final int width = 5;
    final int height = 6;
    final ImageExtractor ie = ImageExtractor.wrap(data, width, height);
    Assertions.assertArrayEquals(new float[] {17}, ie.crop(new Rectangle(2, 3, 1, 1)));
    Assertions.assertArrayEquals(new float[] {12, 13, 14}, ie.crop(new Rectangle(2, 2, 3, 1)));
    Assertions.assertArrayEquals(new float[] {12, 13, 14, 17, 18, 19},
        ie.crop(new Rectangle(2, 2, 3, 2)));
    final float[] region = ie.crop(new Rectangle(2, 2, 3, 1), (float[]) null);
    Assertions.assertArrayEquals(new float[] {12, 13, 14}, region);
    final float[] region2 = ie.crop(new Rectangle(2, 3, 1, 1), region);
    Assertions.assertSame(region, region2);
    Assertions.assertArrayEquals(new float[] {17, 13, 14}, region2);
    final float[] region3 = ie.crop(new Rectangle(2, 2, 3, 2), region2);
    Assertions.assertNotSame(region2, region3);
    Assertions.assertArrayEquals(new float[] {12, 13, 14, 17, 18, 19}, region3);
  }

  @Test
  void canCropToDouble() {
    // @formatter:off
    final float[] data = {
        0,  1,  2,  3,  4,
        5,  6,  7,  8,  9,
       10, 11, 12, 13, 14,
       15, 16, 17, 18, 19
    };
    // @formatter:on
    final int width = 5;
    final int height = 6;
    final ImageExtractor ie = ImageExtractor.wrap(data, width, height);
    Assertions.assertArrayEquals(new double[] {17}, ie.cropToDouble(new Rectangle(2, 3, 1, 1)));
    Assertions.assertArrayEquals(new double[] {12, 13, 14},
        ie.cropToDouble(new Rectangle(2, 2, 3, 1)));
    Assertions.assertArrayEquals(new double[] {12, 13, 14, 17, 18, 19},
        ie.cropToDouble(new Rectangle(2, 2, 3, 2)));
    final double[] region = ie.crop(new Rectangle(2, 2, 3, 1), (double[]) null);
    Assertions.assertArrayEquals(new double[] {12, 13, 14}, region);
    final double[] region2 = ie.crop(new Rectangle(2, 3, 1, 1), region);
    Assertions.assertSame(region, region2);
    Assertions.assertArrayEquals(new double[] {17, 13, 14}, region2);
    final double[] region3 = ie.crop(new Rectangle(2, 2, 3, 2), region2);
    Assertions.assertNotSame(region2, region3);
    Assertions.assertArrayEquals(new double[] {12, 13, 14, 17, 18, 19}, region3);
  }

  @Test
  void canGetBoxRegionBounds() {
    final int width = 5;
    final int height = 6;
    assertGetBoxRegionBounds(width, height, 0, 0, 40);
    assertGetBoxRegionBounds(width, height, 2, 3, 40);
    assertGetBoxRegionBounds(width, height, 2, 3, 0);
    assertGetBoxRegionBounds(width, height, 2, 3, 1);
    assertGetBoxRegionBounds(width, height, 2, 3, 2);
    assertGetBoxRegionBounds(width, height, 2, 3, 3);
    assertGetBoxRegionBounds(width, height, 2, 3, 4);
    assertGetBoxRegionBounds(width, height, -2, 3, 0, 0, 0, 0, 0);
    assertGetBoxRegionBounds(width, height, -2, 3, 1, 0, 0, 0, 0);
    assertGetBoxRegionBounds(width, height, -2, 3, 2);
    assertGetBoxRegionBounds(width, height, -2, 3, 3);
    assertGetBoxRegionBounds(width, height, -2, 3, 4);
    assertGetBoxRegionBounds(width, height, 2, -3, 0, 0, 0, 0, 0);
    assertGetBoxRegionBounds(width, height, 2, -3, 1, 0, 0, 0, 0);
    assertGetBoxRegionBounds(width, height, 2, -3, 2, 0, 0, 0, 0);
    assertGetBoxRegionBounds(width, height, 2, -3, 3);
    assertGetBoxRegionBounds(width, height, 2, -3, 4);
    assertGetBoxRegionBounds(width, height, -2, -3, 0, 0, 0, 0, 0);
    assertGetBoxRegionBounds(width, height, -2, -3, 1, 0, 0, 0, 0);
    assertGetBoxRegionBounds(width, height, -2, -3, 2, 0, 0, 0, 0);
    assertGetBoxRegionBounds(width, height, -2, -3, 3);
    assertGetBoxRegionBounds(width, height, -2, -3, 4);
    // Extremes
    assertGetBoxRegionBounds(width, height, 0, 0, -1, 0, 0, 1, 1);
    assertGetBoxRegionBounds(width, height, 1, 1, -1, 1, 1, 1, 1);
    assertGetBoxRegionBounds(width, height, 0, 0, Integer.MAX_VALUE, 0, 0, width, height);
    assertGetBoxRegionBounds(width, height, 0, 0, Integer.MIN_VALUE, 0, 0, 1, 1);
    assertGetBoxRegionBounds(width, height, Integer.MAX_VALUE, Integer.MAX_VALUE, 5, 0, 0, 0, 0);
    assertGetBoxRegionBounds(width, height, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE,
        0, 0, width, height);
    assertGetBoxRegionBounds(width, height, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE,
        0, 0, 0, 0);
    assertGetBoxRegionBounds(width, height, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE,
        0, 0, 0, 0);
    assertGetBoxRegionBounds(width, height, -10, -15, Integer.MAX_VALUE, 0, 0, width, height);
  }

  private static void assertGetBoxRegionBounds(int width, int height, int x, int y, int n) {
    final ImageExtractor ie = ImageExtractor.wrap(null, width, height);
    final Rectangle observed = ie.getBoxRegionBounds(x, y, n);
    final int ox = x - n;
    final int oy = y - n;
    final int size = 2 * n + 1;
    final Rectangle expected =
        new Rectangle(width, height).intersection(new Rectangle(ox, oy, size, size));
    // Handle bad width/height
    expected.height = Math.max(0, expected.height);
    expected.width = Math.max(0, expected.width);
    Assertions.assertEquals(expected, observed,
        () -> String.format("w=%d,h=%d : %d,%d n=%d", width, height, x, y, n));
  }

  private static void assertGetBoxRegionBounds(int width, int height, int x, int y, int n, int ox,
      int oy, int w, int h) {
    final ImageExtractor ie = ImageExtractor.wrap(null, width, height);
    final Rectangle observed = ie.getBoxRegionBounds(x, y, n);
    final Rectangle expected = new Rectangle(ox, oy, w, h);
    Assertions.assertEquals(expected, observed,
        () -> String.format("w=%d,h=%d : %d,%d n=%d", width, height, x, y, n));
  }
}
