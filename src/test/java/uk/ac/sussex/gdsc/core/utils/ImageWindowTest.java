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

package uk.ac.sussex.gdsc.core.utils;

import java.util.Map.Entry;
import java.util.TreeMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.ImageWindow.Cosine;
import uk.ac.sussex.gdsc.core.utils.ImageWindow.Hanning;
import uk.ac.sussex.gdsc.core.utils.ImageWindow.NoWindowFunction;
import uk.ac.sussex.gdsc.core.utils.ImageWindow.Tukey;
import uk.ac.sussex.gdsc.core.utils.ImageWindow.WindowFunction;
import uk.ac.sussex.gdsc.core.utils.ImageWindow.WindowMethod;

/**
 * Tests for {@link ImageWindow}. The tests ensure the image window falls off from 1 to zero as the
 * distance from the centre increases, and that the weights are symmetric. No assertions are made on
 * actual image window weights.
 */
@SuppressWarnings({"javadoc"})
class ImageWindowTest {

  @Test
  void testNoWindowFunction() {
    for (double value : new double[] {0, 0.5, 1, -1, Double.NaN, 100}) {
      Assertions.assertEquals(1, NoWindowFunction.INSTANCE.weight(value));
    }
  }

  @Test
  void testApplySeparableEdgeCases() {
    final ImageWindow window = new ImageWindow();
    final int maxx = 4;
    final int maxy = 6;
    Assertions.assertThrows(NullPointerException.class,
        () -> window.applySeparable(null, maxx, maxy, WindowMethod.COSINE));
    final float[] image = new float[maxx * maxy];
    Assertions.assertSame(image, window.applySeparable(image, maxx, maxy, WindowMethod.NONE));
  }

  @Test
  void testApplyWindowSeparableEdgeCases() {
    final int maxx = 4;
    final int maxy = 6;
    final float[] image = new float[maxx * maxy];
    final double[] wx = new double[maxx];
    final double[] wy = new double[maxy];
    final double[] w = new double[1];
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> ImageWindow.applyWindowSeparable(image, maxx, maxy, null, wy));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> ImageWindow.applyWindowSeparable(image, maxx, maxy, w, wy));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> ImageWindow.applyWindowSeparable(image, maxx, maxy, wx, null));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> ImageWindow.applyWindowSeparable(image, maxx, maxy, wx, w));
  }

  @Test
  void testApplyWindowSeparableInPlaceEdgeCases() {
    final int maxx = 4;
    final int maxy = 6;
    final float[] image = new float[maxx * maxy];
    final double[] wx = new double[maxx];
    final double[] wy = new double[maxy];
    final double[] w = new double[1];
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> ImageWindow.applyWindowSeparableInPlace(image, maxx, maxy, null, wy));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> ImageWindow.applyWindowSeparableInPlace(image, maxx, maxy, w, wy));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> ImageWindow.applyWindowSeparableInPlace(image, maxx, maxy, wx, null));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> ImageWindow.applyWindowSeparableInPlace(image, maxx, maxy, wx, w));
  }

  @Test
  void testApplyWindowSeparableHanning() {
    assertApplyWindowSeparable(WindowMethod.HANNING);
  }

  @Test
  void testApplyWindowSeparableCosine() {
    assertApplyWindowSeparable(WindowMethod.COSINE);
  }

  @Test
  void testApplyWindowSeparableTukey() {
    assertApplyWindowSeparable(WindowMethod.TUKEY);
  }

  @Test
  void testApplyWindowSeparableNone() {
    assertApplyWindowSeparable(WindowMethod.NONE);
  }

  private static void assertApplyWindowSeparable(WindowMethod windowMethod) {
    final ImageWindow window = new ImageWindow();
    for (final int maxx : new int[] {4, 7}) {
      for (final int maxy : new int[] {6, 5}) {
        final float[] image = SimpleArrayUtils.newFloatArray(maxx * maxy, 1);
        final double[] wx = ImageWindow.createWindow(windowMethod, maxx);
        final double[] wy = ImageWindow.createWindow(windowMethod, maxy);
        final float[] result = window.applySeparable(image, maxx, maxy, windowMethod);
        Assertions.assertEquals(windowMethod == WindowMethod.NONE, result == image);
        final float[] result2 = window.applySeparable(image, maxx, maxy, windowMethod);
        Assertions.assertEquals(windowMethod == WindowMethod.NONE, result2 == image);
        Assertions.assertEquals(windowMethod == WindowMethod.NONE, result2 == result);
        Assertions.assertArrayEquals(result, result2);

        final float[] result3 = ImageWindow.applyWindowSeparable(image, maxx, maxy, windowMethod);
        Assertions.assertEquals(windowMethod == WindowMethod.NONE, result3 == image);
        Assertions.assertArrayEquals(result, result3);
        final float[] result4 = ImageWindow.applyWindowSeparable(image, maxx, maxy, wx, wy);
        Assertions.assertNotSame(image, result4);
        Assertions.assertArrayEquals(result, result4);

        ImageWindow.applyWindowSeparableInPlace(image, maxx, maxy, wx, wy);
        Assertions.assertArrayEquals(result, image);
        for (int y = 0, i = 0; y < maxy; y++) {
          for (int x = 0; x < maxx; x++, i++) {
            Assertions.assertEquals(image[i], (float) (wx[x] * wy[y]));
          }
        }
      }
    }
  }

  @Test
  void testApplyWindowEdgeCases() {
    final int maxx = 4;
    final int maxy = 6;
    final float[] image = new float[maxx * maxy];
    Assertions.assertSame(image, ImageWindow.applyWindow(image, maxx, maxy, WindowMethod.NONE));
  }

  @Test
  void testApplyWindowHanning() {
    assertApplyWindow(WindowMethod.HANNING);
  }

  @Test
  void testApplyWindowCosine() {
    assertApplyWindow(WindowMethod.COSINE);
  }

  @Test
  void testApplyWindowTukey() {
    assertApplyWindow(WindowMethod.TUKEY);
  }

  private static void assertApplyWindow(WindowMethod windowMethod) {
    for (final int maxx : new int[] {4, 7}) {
      for (final int maxy : new int[] {6, 5}) {
        final float[] image = SimpleArrayUtils.newFloatArray(maxx * maxy, 1);

        final float[] result = ImageWindow.applyWindow(image, maxx, maxy, windowMethod);

        final double cx = maxx * 0.5;
        final double cy = maxy * 0.5;
        final TreeMap<Double, Double> map = new TreeMap<>();
        for (int y = 0, i = 0; y < maxy; y++) {
          for (int x = 0; x < maxx; x++, i++) {
            final double weight = result[i];
            Assertions.assertTrue(weight >= 0.0);
            Assertions.assertTrue(weight <= 1.0);
            final double distance = MathUtils.distance2(x, y, cx, cy);
            final Double old = map.putIfAbsent(distance, weight);
            if (old != null) {
              Assertions.assertEquals(old, weight);
            }
          }
        }
        Entry<Double, Double> last = map.firstEntry();
        // Close to one in the centre
        Assertions.assertEquals(1.0, last.getValue(), 0.08);
        // As distance from the centre increases then the weight should decrease
        Entry<Double, Double> next = map.higherEntry(last.getKey());
        while (next != null) {
          Assertions.assertTrue(last.getValue() >= next.getValue());
          last = next;
          next = map.higherEntry(last.getKey());
        }
        // Close to zero at the corner
        Assertions.assertEquals(0.0, last.getValue(), 0.05);
      }
    }
  }

  @Test
  void testWindowMethodGetName() {
    for (final WindowMethod method : WindowMethod.values()) {
      Assertions.assertNotEquals(method.name(), method.toString());
      Assertions.assertEquals(method.getName(), method.toString());
    }
  }

  @Test
  void testHanningWindowFunction() {
    assertWindowFunction(Hanning.INSTANCE);
  }

  @Test
  void testCosineWindowFunction() {
    assertWindowFunction(Cosine.INSTANCE);
  }

  @Test
  void testTukeyWindowFunction() {
    assertWindowFunction(Tukey.INSTANCE);
    assertWindowFunction(new Tukey(0.0), false);
    assertWindowFunction(new Tukey(0.1));
    assertWindowFunction(new Tukey(0.2));
  }

  private static void assertWindowFunction(WindowFunction wf) {
    assertWindowFunction(wf, true);
  }

  private static void assertWindowFunction(WindowFunction wf, boolean zeroAtEdge) {
    double last = wf.weight(0);
    if (zeroAtEdge) {
      Assertions.assertEquals(0.0, last, 0.02);
    }
    Assertions.assertEquals(1.0, wf.weight(0.5), 0.02);
    for (int i = 1; i <= 50; i++) {
      final double distance = i / 100.0;
      final double weight = wf.weight(distance);
      Assertions.assertTrue(weight >= last);
      Assertions.assertTrue(weight >= 0.0);
      Assertions.assertTrue(weight <= 1.0);
      Assertions.assertEquals(weight, wf.weight(1 - distance), 1e-10);
      last = weight;
    }
  }

  @Test
  void testCreateWindowHanning() {
    assertCreateWindow(WindowMethod.HANNING);
  }

  @Test
  void testCreateWindowCosine() {
    assertCreateWindow(WindowMethod.COSINE);
  }

  @Test
  void testCreateWindowTukey() {
    assertCreateWindow(WindowMethod.TUKEY);
  }

  @Test
  void testCreateWindowNone() {
    assertCreateWindow(WindowMethod.NONE, false);
  }

  private static void assertCreateWindow(WindowMethod windowMethod) {
    assertCreateWindow(windowMethod, true);
  }

  private static void assertCreateWindow(WindowMethod windowMethod, boolean zeroAtEdge) {
    for (final int size : new int[] {20, 41}) {
      final double[] window = ImageWindow.createWindow(windowMethod, size);
      Assertions.assertEquals(size, window.length);
      if (zeroAtEdge) {
        Assertions.assertEquals(0.0, window[0], 0.02);
      }
      Assertions.assertEquals(1.0, window[size / 2], 0.02);
      double last = window[0];
      for (int i = 1; i < size / 2; i++) {
        final double weight = window[i];
        Assertions.assertTrue(weight >= 0.0);
        Assertions.assertTrue(weight <= 1.0);
        Assertions.assertTrue(weight >= last);
        Assertions.assertEquals(weight, window[size - i - 1]);
        last = weight;
      }
    }
    // Test small window functions have non-zero sum
    for (final int size : new int[] {1, 2, 3}) {
      final double[] window = ImageWindow.createWindow(windowMethod, size);
      Assertions.assertTrue(MathUtils.sum(window) > 0, () -> "sum zero for size: " + size);
    }
  }

  @Test
  void testTukeyWithBadAlpha() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> ImageWindow.tukey(100, -0.1));
    Assertions.assertThrows(IllegalArgumentException.class, () -> ImageWindow.tukey(100, 1.1));
  }

  @Test
  void testTukeyDefaultAlpha() {
    for (final int size : new int[] {20, 50}) {
      Assertions.assertArrayEquals(ImageWindow.tukey(size, 0.5), ImageWindow.tukey(size));
    }
  }

  @Test
  void testTukeyEdge() {
    Assertions.assertArrayEquals(new double[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        ImageWindow.tukeyEdge(10, 0));
    final double[] window = ImageWindow.tukeyEdge(10, 3);
    for (int i = 0; i < 3; i++) {
      Assertions.assertTrue(window[i] < 1);
      Assertions.assertTrue(window[10 - i - 1] < 1);
    }
    for (int i = 3; i < 7; i++) {
      Assertions.assertEquals(1, window[i]);
    }
  }

  @Test
  void testTukeyAlpha() {
    Assertions.assertEquals(0, ImageWindow.tukeyAlpha(0, 10));
    Assertions.assertEquals(0, ImageWindow.tukeyAlpha(100, 0));
    Assertions.assertEquals(20.0 / 99, ImageWindow.tukeyAlpha(100, 10));
    Assertions.assertEquals(20.0 / 49, ImageWindow.tukeyAlpha(50, 10));
    Assertions.assertEquals(1.0, ImageWindow.tukeyAlpha(50, 50));
    Assertions.assertEquals(1.0, ImageWindow.tukeyAlpha(50, 25));
  }
}
