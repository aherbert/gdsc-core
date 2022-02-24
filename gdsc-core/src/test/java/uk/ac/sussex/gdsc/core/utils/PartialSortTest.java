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

package uk.ac.sussex.gdsc.core.utils;

import java.util.Arrays;
import java.util.function.Supplier;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;
import uk.ac.sussex.gdsc.test.utils.functions.ObjectArrayFormatSupplier;

@SuppressWarnings({"javadoc"})
class PartialSortTest {

  int[] testN = new int[] {2, 3, 5, 10, 30, 50};
  int[] testM = new int[] {50, 100};

  // XXX copy double from here

  @SeededTest
  void bottomNofMIsCorrectDouble(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.get());
    for (final int size : testN) {
      for (final int total : testM) {
        bottomComputeDouble(rng, 100, size, total);
      }
    }
  }

  static double[] bottomDouble(int size, double[] values) {
    bottomSortDouble(values);
    return Arrays.copyOf(values, size);
  }

  static void bottomSortDouble(double[] values) {
    Arrays.sort(values);
  }

  @Test
  void bottomCanHandleNullDataDouble() {
    Assertions.assertEquals(0, PartialSort.bottom((double[]) null, 5).length);
    Assertions.assertEquals(0, PartialSort.bottom(0, (double[]) null, 5).length);
    Assertions.assertEquals(0, PartialSort.bottom(0, (double[]) null, 5, 3).length);
  }

  @Test
  void bottomCanHandleEmptyDataDouble() {
    final double[] o = PartialSort.bottom(ArrayUtils.EMPTY_DOUBLE_ARRAY, 5);
    Assertions.assertEquals(0, o.length);
  }

  @Test
  void bottomCanHandleIncompleteDataDouble() {
    final double[] d = {1, 3, 2};
    final double[] e = {1, 2, 3};
    final double[] o = PartialSort.bottom(d, 5);
    Assertions.assertArrayEquals(e, o);
  }

  @Test
  void bottomCanHandleNaNDataDouble() {
    Assertions.assertArrayEquals(new double[0],
        PartialSort.bottom(new double[] {Double.NaN, Double.NaN}, 1));
    final double[] d = {1, 2, Double.NaN, 3};
    final double[] e = {1, 2, 3};
    final double[] o = PartialSort.bottom(d, 5);
    Assertions.assertArrayEquals(e, o);
  }

  private static void bottomComputeDouble(UniformRandomProvider rng, int length, final int size,
      final int total) {
    final ObjectArrayFormatSupplier msg = new ObjectArrayFormatSupplier("%s %d of %d", 3);
    msg.set(1, size);
    msg.set(2, total);
    final double[] d = new double[total];
    final PartialSort.DoubleSelector ps = new PartialSort.DoubleSelector(size);
    final PartialSort.DoubleHeap heap = new PartialSort.DoubleHeap(size);
    for (int i = 0; i < length; i++) {
      for (int j = 0; j < total; j++) {
        d[j] = rng.nextDouble() * 4 * Math.PI;
      }
      // Reference
      final double[] expected = bottomDouble(size, d.clone());
      // Test methods
      assertBottomSortEqual(expected, PartialSort.bottom(d.clone(), size), false, true,
          msg.set(0, "bottomSort"));
      assertBottomSortEqual(expected,
          PartialSort.bottom(PartialSort.OPTION_HEAD_FIRST, d.clone(), size), true, false,
          msg.set(0, "bottomHead"));
      assertBottomSortEqual(expected, PartialSort.bottom(0, d.clone(), size), false, false,
          msg.set(0, "bottom"));
      assertBottomSortEqual(expected, ps.bottom(0, d.clone()), false, false,
          msg.set(0, "DoubleSelector"));
      assertBottomSortEqual(expected, heap.bottom(0, d.clone()), false, false,
          msg.set(0, "DoubleMinHeap"));
    }
  }

  private static void assertBottomSortEqual(double[] expected, double[] actual, boolean head,
      boolean sorted, Supplier<String> msg) {
    if (head) {
      Assertions.assertEquals(expected[expected.length - 1], actual[0], msg);
    }
    if (!sorted) {
      bottomSortDouble(actual);
    }
    Assertions.assertArrayEquals(expected, actual, msg);
  }

  @SeededTest
  void topNofMIsCorrectDouble(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.get());
    for (final int size : testN) {
      for (final int total : testM) {
        topComputeDouble(rng, 100, size, total);
      }
    }
  }

  static double[] topDouble(int size, double[] values) {
    topSortDouble(values);
    return Arrays.copyOf(values, size);
  }

  static void topSortDouble(double[] values) {
    Arrays.sort(values);
    SimpleArrayUtils.reverse(values);
  }

  @Test
  void topCanHandleNullDataDouble() {
    Assertions.assertEquals(0, PartialSort.top((double[]) null, 5).length);
    Assertions.assertEquals(0, PartialSort.top(0, (double[]) null, 5).length);
    Assertions.assertEquals(0, PartialSort.top(0, (double[]) null, 5, 3).length);
  }

  @Test
  void topCanHandleEmptyDataDouble() {
    final double[] o = PartialSort.top(ArrayUtils.EMPTY_DOUBLE_ARRAY, 5);
    Assertions.assertEquals(0, o.length);
  }

  @Test
  void topCanHandleIncompleteDataDouble() {
    final double[] d = {1, 3, 2};
    final double[] e = {3, 2, 1};
    final double[] o = PartialSort.top(d, 5);
    Assertions.assertArrayEquals(e, o);
  }

  @Test
  void topCanHandleNaNDataDouble() {
    Assertions.assertArrayEquals(new double[0],
        PartialSort.top(new double[] {Double.NaN, Double.NaN}, 1));
    final double[] d = {1, 2, Double.NaN, 3};
    final double[] e = {3, 2, 1};
    final double[] o = PartialSort.top(d, 5);
    Assertions.assertArrayEquals(e, o);
  }

  private static void topComputeDouble(UniformRandomProvider rng, int length, final int size,
      final int total) {
    final ObjectArrayFormatSupplier msg = new ObjectArrayFormatSupplier("%s %d of %d", 3);
    msg.set(1, size);
    msg.set(2, total);
    final double[] d = new double[total];
    final PartialSort.DoubleSelector ps = new PartialSort.DoubleSelector(size);
    final PartialSort.DoubleHeap heap = new PartialSort.DoubleHeap(size);
    for (int i = 0; i < length; i++) {
      for (int j = 0; j < total; j++) {
        d[j] = rng.nextDouble() * 4 * Math.PI;
      }
      // Reference
      final double[] expected = topDouble(size, d.clone());
      // Test methods
      assertTopSortEqual(expected, PartialSort.top(d.clone(), size), false, true,
          msg.set(0, "topSort"));
      assertTopSortEqual(expected, PartialSort.top(PartialSort.OPTION_HEAD_FIRST, d.clone(), size),
          true, false, msg.set(0, "topHead"));
      assertTopSortEqual(expected, PartialSort.top(0, d.clone(), size), false, false,
          msg.set(0, "top"));
      assertTopSortEqual(expected, ps.top(0, d.clone()), false, false,
          msg.set(0, "DoubleSelector"));
      assertTopSortEqual(expected, heap.top(0, d.clone()), false, false,
          msg.set(0, "DoubleMinHeap"));
    }
  }

  private static void assertTopSortEqual(double[] expected, double[] actual, boolean head,
      boolean sorted, Supplier<String> msg) {
    if (head) {
      Assertions.assertEquals(expected[expected.length - 1], actual[0], msg);
    }
    if (!sorted) {
      topSortDouble(actual);
    }
    Assertions.assertArrayEquals(expected, actual, msg);
  }

  // XXX copy to here

  @SeededTest
  void bottomNofMIsCorrectFloat(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.get());
    for (final int size : testN) {
      for (final int total : testM) {
        bottomComputeFloat(rng, 100, size, total);
      }
    }
  }

  static float[] bottomFloat(int size, float[] values) {
    bottomSortFloat(values);
    return Arrays.copyOf(values, size);
  }

  static void bottomSortFloat(float[] values) {
    Arrays.sort(values);
  }

  @Test
  void bottomCanHandleNullDataFloat() {
    Assertions.assertEquals(0, PartialSort.bottom((float[]) null, 5).length);
    Assertions.assertEquals(0, PartialSort.bottom(0, (float[]) null, 5).length);
    Assertions.assertEquals(0, PartialSort.bottom(0, (float[]) null, 5, 3).length);
  }

  @Test
  void bottomCanHandleEmptyDataFloat() {
    final float[] o = PartialSort.bottom(ArrayUtils.EMPTY_FLOAT_ARRAY, 5);
    Assertions.assertEquals(0, o.length);
  }

  @Test
  void bottomCanHandleIncompleteDataFloat() {
    final float[] d = {1, 3, 2};
    final float[] e = {1, 2, 3};
    final float[] o = PartialSort.bottom(d, 5);
    Assertions.assertArrayEquals(e, o);
  }

  @Test
  void bottomCanHandleNaNDataFloat() {
    Assertions.assertArrayEquals(new float[0],
        PartialSort.bottom(new float[] {Float.NaN, Float.NaN}, 1));
    final float[] d = {1, 2, Float.NaN, 3};
    final float[] e = {1, 2, 3};
    final float[] o = PartialSort.bottom(d, 5);
    Assertions.assertArrayEquals(e, o);
  }

  private static void bottomComputeFloat(UniformRandomProvider rng, int length, final int size,
      final int total) {
    final ObjectArrayFormatSupplier msg = new ObjectArrayFormatSupplier("%s %d of %d", 3);
    msg.set(1, size);
    msg.set(2, total);
    final float[] d = new float[total];
    final PartialSort.FloatSelector ps = new PartialSort.FloatSelector(size);
    final PartialSort.FloatHeap heap = new PartialSort.FloatHeap(size);
    for (int i = 0; i < length; i++) {
      for (int j = 0; j < total; j++) {
        d[j] = (float) (rng.nextFloat() * 4 * Math.PI);
      }
      // Reference
      final float[] expected = bottomFloat(size, d.clone());
      // Test methods
      assertBottomSortEqual(expected, PartialSort.bottom(d.clone(), size), false, true,
          msg.set(0, "bottomSort"));
      assertBottomSortEqual(expected,
          PartialSort.bottom(PartialSort.OPTION_HEAD_FIRST, d.clone(), size), true, false,
          msg.set(0, "bottomHead"));
      assertBottomSortEqual(expected, PartialSort.bottom(0, d.clone(), size), false, false,
          msg.set(0, "bottom"));
      assertBottomSortEqual(expected, ps.bottom(0, d.clone()), false, false,
          msg.set(0, "FloatSelector"));
      assertBottomSortEqual(expected, heap.bottom(0, d.clone()), false, false,
          msg.set(0, "FloatMinHeap"));
    }
  }

  private static void assertBottomSortEqual(float[] expected, float[] actual, boolean head,
      boolean sorted, Supplier<String> msg) {
    if (head) {
      Assertions.assertEquals(expected[expected.length - 1], actual[0], msg);
    }
    if (!sorted) {
      bottomSortFloat(actual);
    }
    Assertions.assertArrayEquals(expected, actual, msg);
  }

  @SeededTest
  void topNofMIsCorrectFloat(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.get());
    for (final int size : testN) {
      for (final int total : testM) {
        topComputeFloat(rng, 100, size, total);
      }
    }
  }

  static float[] topFloat(int size, float[] values) {
    topSortFloat(values);
    return Arrays.copyOf(values, size);
  }

  static void topSortFloat(float[] values) {
    Arrays.sort(values);
    SimpleArrayUtils.reverse(values);
  }

  @Test
  void topCanHandleNullDataFloat() {
    Assertions.assertEquals(0, PartialSort.top((float[]) null, 5).length);
    Assertions.assertEquals(0, PartialSort.top(0, (float[]) null, 5).length);
    Assertions.assertEquals(0, PartialSort.top(0, (float[]) null, 5, 3).length);
  }

  @Test
  void topCanHandleEmptyDataFloat() {
    final float[] o = PartialSort.top(ArrayUtils.EMPTY_FLOAT_ARRAY, 5);
    Assertions.assertEquals(0, o.length);
  }

  @Test
  void topCanHandleIncompleteDataFloat() {
    final float[] d = {1, 3, 2};
    final float[] e = {3, 2, 1};
    final float[] o = PartialSort.top(d, 5);
    Assertions.assertArrayEquals(e, o);
  }

  @Test
  void topCanHandleNaNDataFloat() {
    Assertions.assertArrayEquals(new float[0],
        PartialSort.top(new float[] {Float.NaN, Float.NaN}, 1));
    final float[] d = {1, 2, Float.NaN, 3};
    final float[] e = {3, 2, 1};
    final float[] o = PartialSort.top(d, 5);
    Assertions.assertArrayEquals(e, o);
  }

  private static void topComputeFloat(UniformRandomProvider rng, int length, final int size,
      final int total) {
    final ObjectArrayFormatSupplier msg = new ObjectArrayFormatSupplier("%s %d of %d", 3);
    msg.set(1, size);
    msg.set(2, total);
    final float[] d = new float[total];
    final PartialSort.FloatSelector ps = new PartialSort.FloatSelector(size);
    final PartialSort.FloatHeap heap = new PartialSort.FloatHeap(size);
    for (int i = 0; i < length; i++) {
      for (int j = 0; j < total; j++) {
        d[j] = (float) (rng.nextFloat() * 4 * Math.PI);
      }
      // Reference
      final float[] expected = topFloat(size, d.clone());
      // Test methods
      assertTopSortEqual(expected, PartialSort.top(d.clone(), size), false, true,
          msg.set(0, "topSort"));
      assertTopSortEqual(expected, PartialSort.top(PartialSort.OPTION_HEAD_FIRST, d.clone(), size),
          true, false, msg.set(0, "topHead"));
      assertTopSortEqual(expected, PartialSort.top(0, d.clone(), size), false, false,
          msg.set(0, "top"));
      assertTopSortEqual(expected, ps.top(0, d.clone()), false, false, msg.set(0, "FloatSelector"));
      assertTopSortEqual(expected, heap.top(0, d.clone()), false, false,
          msg.set(0, "FloatMinHeap"));
    }
  }

  private static void assertTopSortEqual(float[] expected, float[] actual, boolean head,
      boolean sorted, Supplier<String> msg) {
    if (head) {
      Assertions.assertEquals(expected[expected.length - 1], actual[0], msg);
    }
    if (!sorted) {
      topSortFloat(actual);
    }
    Assertions.assertArrayEquals(expected, actual, msg);
  }
}
