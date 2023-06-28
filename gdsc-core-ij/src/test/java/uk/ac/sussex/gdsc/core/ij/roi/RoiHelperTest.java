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
 * Copyright (C) 2011 - 2023 Alex Herbert
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

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.awt.Rectangle;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings({"javadoc"})
class RoiHelperTest {

  @Test
  void testGetMask() {
    final int size = 8;
    final ImagePlus imp = new ImagePlus(null, new ByteProcessor(size, size));
    // No ROI
    Assertions.assertNull(RoiHelper.getMask(imp));
    // No area ROI
    imp.setRoi(new Line(1, 2, 3, 4));
    Assertions.assertNull(RoiHelper.getMask(imp));
    // Full size rectangle
    final Roi roi = new Roi(0, 0, size, size);
    imp.setRoi(roi);
    Assertions.assertNull(RoiHelper.getMask(imp));
    // Rounded edge
    roi.setRoundRectArcSize(1);
    Assertions.assertNotNull(RoiHelper.getMask(imp));
    // Part size rectangle
    for (final Roi r : new Roi[] {new Roi(1, 2, 3, 4), new Roi(0, 2, size, 4),
        new Roi(1, 0, 3, size)}) {
      imp.setRoi(r);
      final Rectangle bounds = r.getBounds();
      final ByteProcessor bp = RoiHelper.getMask(imp);
      // Just check the sum is correct
      int sum = 0;
      for (int i = bp.getPixelCount(); i-- > 0;) {
        sum += bp.get(i);
      }
      Assertions.assertEquals(255 * bounds.width * bounds.height, sum);
      for (int x = bounds.x; x < bounds.x + bounds.width; x++) {
        for (int y = bounds.y; y < bounds.y + bounds.height; y++) {
          Assertions.assertEquals(255, bp.get(x, y));
        }
      }
    }
    // Polygon
    imp.setRoi(new PolygonRoi(new float[] {0, size, size}, new float[] {0, 0, size}, Roi.POLYGON));
    Assertions.assertNotNull(RoiHelper.getMask(imp));
  }

  @Test
  void testForEachFloat() {
    final int size = 8;
    final byte[] pixels = new byte[size * size];
    for (int i = 0; i < pixels.length; i++) {
      pixels[i] = (byte) i;
    }
    final ByteProcessor ip = new ByteProcessor(size, size, pixels);
    // No ROI
    final IntOpenHashSet set = new IntOpenHashSet(pixels.length);
    RoiHelper.forEach(null, ip, (float value) -> {
      Assertions.assertTrue(set.add((int) value));
    });
    // ROI outside image
    RoiHelper.forEach(new Roi(size, size, 1, 2), ip, (float value) -> {
      Assertions.fail();
    });
    // No width
    RoiHelper.forEach(new Roi(-1, 0, 1, 2), ip, (float value) -> {
      Assertions.fail();
    });
    // No height
    RoiHelper.forEach(new Roi(0, -2, 1, 2), ip, (float value) -> {
      Assertions.fail();
    });
    // Square ROI
    final Roi roi = new Roi(3, 4, 2, 1);
    RoiHelper.forEach(roi, ip, (float value) -> {
      final int index = (int) value;
      final int x = index % size;
      final int y = index / size;
      Assertions.assertTrue(roi.contains(x, y));
    });
    // Masked ROI
    final OvalRoi oval = new OvalRoi(1, 2, 3, 4);
    RoiHelper.forEach(oval, ip, (float value) -> {
      final int index = (int) value;
      final int x = index % size;
      final int y = index / size;
      Assertions.assertTrue(oval.contains(x, y));
    });
  }

  @Test
  void testForEachImageStackFloat() {
    final int size = 8;
    final byte[] pixels = new byte[size * size];
    for (int i = 0; i < pixels.length; i++) {
      pixels[i] = (byte) i;
    }
    final ImageStack stack = new ImageStack(size, size);
    stack.addSlice(null, pixels);
    // No ROI
    final IntOpenHashSet set = new IntOpenHashSet(pixels.length);
    RoiHelper.forEach(null, stack, (float value) -> {
      Assertions.assertTrue(set.add((int) value));
    });
    // ROI outside image
    RoiHelper.forEach(new Roi(size, size, 1, 2), stack, (float value) -> {
      Assertions.fail();
    });
    // No width
    RoiHelper.forEach(new Roi(-1, 0, 1, 2), stack, (float value) -> {
      Assertions.fail();
    });
    // No height
    RoiHelper.forEach(new Roi(0, -2, 1, 2), stack, (float value) -> {
      Assertions.fail();
    });
    // Square ROI
    final Roi roi = new Roi(3, 4, 2, 1);
    RoiHelper.forEach(roi, stack, (float value) -> {
      final int index = (int) value;
      final int x = index % size;
      final int y = index / size;
      Assertions.assertTrue(roi.contains(x, y));
    });
    // Masked ROI
    final OvalRoi oval = new OvalRoi(1, 2, 3, 4);
    RoiHelper.forEach(oval, stack, (float value) -> {
      final int index = (int) value;
      final int x = index % size;
      final int y = index / size;
      Assertions.assertTrue(oval.contains(x, y));
    });
  }

  @Test
  void testForEachInt() {
    final int size = 8;
    final byte[] pixels = new byte[size * size];
    for (int i = 0; i < pixels.length; i++) {
      pixels[i] = (byte) i;
    }
    final ByteProcessor ip = new ByteProcessor(size, size, pixels);
    // No ROI
    final IntOpenHashSet set = new IntOpenHashSet(pixels.length);
    RoiHelper.forEach(null, ip, (int value) -> {
      Assertions.assertTrue(set.add(value));
    });
    // ROI outside image
    RoiHelper.forEach(new Roi(size, size, 1, 2), ip, (int value) -> {
      Assertions.fail();
    });
    // No width
    RoiHelper.forEach(new Roi(-1, 0, 1, 2), ip, (int value) -> {
      Assertions.fail();
    });
    // No height
    RoiHelper.forEach(new Roi(0, -2, 1, 2), ip, (int value) -> {
      Assertions.fail();
    });
    // Square ROI
    final Roi roi = new Roi(3, 4, 2, 1);
    RoiHelper.forEach(roi, ip, (int index) -> {
      final int x = index % size;
      final int y = index / size;
      Assertions.assertTrue(roi.contains(x, y));
    });
    // Masked ROI
    final OvalRoi oval = new OvalRoi(1, 2, 3, 4);
    RoiHelper.forEach(oval, ip, (int index) -> {
      final int x = index % size;
      final int y = index / size;
      Assertions.assertTrue(oval.contains(x, y));
    });
  }

  @Test
  void testForEachImageStackInt() {
    final int size = 8;
    final byte[] pixels = new byte[size * size];
    for (int i = 0; i < pixels.length; i++) {
      pixels[i] = (byte) i;
    }
    final ImageStack stack = new ImageStack(size, size);
    stack.addSlice(null, pixels);
    // No ROI
    final IntOpenHashSet set = new IntOpenHashSet(pixels.length);
    RoiHelper.forEach(null, stack, (int value) -> {
      Assertions.assertTrue(set.add(value));
    });
    // ROI outside image
    RoiHelper.forEach(new Roi(size, size, 1, 2), stack, (int value) -> {
      Assertions.fail();
    });
    // No width
    RoiHelper.forEach(new Roi(-1, 0, 1, 2), stack, (int value) -> {
      Assertions.fail();
    });
    // No height
    RoiHelper.forEach(new Roi(0, -2, 1, 2), stack, (int value) -> {
      Assertions.fail();
    });
    // Square ROI
    final Roi roi = new Roi(3, 4, 2, 1);
    RoiHelper.forEach(roi, stack, (int index) -> {
      final int x = index % size;
      final int y = index / size;
      Assertions.assertTrue(roi.contains(x, y));
    });
    // Masked ROI
    final OvalRoi oval = new OvalRoi(1, 2, 3, 4);
    RoiHelper.forEach(oval, stack, (int index) -> {
      final int x = index % size;
      final int y = index / size;
      Assertions.assertTrue(oval.contains(x, y));
    });
  }

  @Test
  void testLargestBoundingRoiThrows() {
    final ByteProcessor ip = new ByteProcessor(4, 3);
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> RoiHelper.largestBoundingRoi(ip, -1, 0));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> RoiHelper.largestBoundingRoi(ip, ip.getWidth(), 0));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> RoiHelper.largestBoundingRoi(ip, 0, -1));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> RoiHelper.largestBoundingRoi(ip, 0, ip.getHeight()));
  }

  @ParameterizedTest
  @MethodSource
  void testLargestBoundingRoi(ImageProcessor ip, int x, int y, Roi expected) {
    Assertions.assertEquals(expected, RoiHelper.largestBoundingRoi(ip, x, y));
  }

  static Stream<Arguments> testLargestBoundingRoi() {
    final Stream.Builder<Arguments> builder = Stream.builder();
    // Full size
    final ByteProcessor ip = new ByteProcessor(4, 3);
    final Roi roi = new Roi(0, 0, ip.getWidth(), ip.getHeight());
    builder.add(Arguments.of(ip, 0, 0, roi));
    builder.add(Arguments.of(ip, 1, 1, roi));
    builder.add(Arguments.of(ip, 3, 2, roi));
    // With spots
    // @formatter:off
    final ByteProcessor ip2 = createByteProcessor("01010",
                                                  "01000",
                                                  "10000",
                                                  "00001",
                                                  "01000");
    // @formatter:on
    builder.add(Arguments.of(ip2, 0, 1, new Roi(0, 0, 1, 2)));
    builder.add(Arguments.of(ip2, 2, 4, new Roi(2, 1, 2, 4)));
    builder.add(Arguments.of(ip2, 1, 3, new Roi(1, 2, 3, 2)));
    builder.add(Arguments.of(ip2, 4, 0, new Roi(4, 0, 1, 3)));
    builder.add(Arguments.of(ip2, 1, 0, new Roi(1, 0, 1, 2)));
    builder.add(Arguments.of(ip2, 2, 0, new Roi(2, 0, 1, 5)));
    builder.add(Arguments.of(ip2, 2, 2, new Roi(2, 1, 2, 4)));
    return builder.build();
  }

  private static ByteProcessor createByteProcessor(String... bits) {
    final int n = bits[0].length();
    final ByteProcessor bp = new ByteProcessor(n, n);
    int index = 0;
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        // Assume values are digits [0, 9]
        bp.set(index++, bits[i].charAt(j) - '0');
      }
    }
    return bp;
  }

  @ParameterizedTest
  @MethodSource
  void testConvertToMask(ImageProcessor ip, int x, int y) {
    final int w = ip.getWidth();
    final int h = ip.getHeight();
    final ByteProcessor bp = new ByteProcessor(w, h);
    final double v = ip.getValue(x, y);
    int index = 0;
    for (int j = 0; j < h; j++) {
      for (int i = 0; i < w; i++) {
        bp.set(index++, ip.getValue(i, j) == v ? 0 : -1);
      }
    }
    Assertions.assertArrayEquals((byte[]) bp.getPixels(), RoiHelper.convertToMask(ip, x, y));
  }

  static Stream<Arguments> testConvertToMask() {
    final Stream.Builder<Arguments> builder = Stream.builder();
    final ShortProcessor ip = new ShortProcessor(4, 3);
    ip.set(1, 2, Short.MAX_VALUE);
    ip.set(2, 1, Short.MAX_VALUE);
    builder.add(Arguments.of(ip, 0, 0));
    builder.add(Arguments.of(ip, 2, 1));
    final FloatProcessor ip2 = new FloatProcessor(4, 3);
    ip2.setf(1, 2, Float.MAX_VALUE);
    ip2.setf(2, 1, Float.MAX_VALUE);
    builder.add(Arguments.of(ip2, 0, 0));
    builder.add(Arguments.of(ip2, 2, 1));
    return builder.build();
  }
}
