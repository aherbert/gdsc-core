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

package uk.ac.sussex.gdsc.core.threshold;

import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.threshold.AutoThreshold.Method;
import uk.ac.sussex.gdsc.core.utils.rng.RandomUtils;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

@SuppressWarnings({"javadoc"})
class FloatHistogramTest {
  @Test
  void canCreateHistogram() {
    final float[] value = {1.1f, 2, 5, 8, 9};
    final int[] histogram = {0, 6, 7, 8, 0};
    final FloatHistogram h = new FloatHistogram(value, histogram);
    Assertions.assertEquals(1, h.minBin);
    Assertions.assertEquals(3, h.maxBin);
    for (int i = 0; i < histogram.length; i++) {
      Assertions.assertEquals(value[i], h.getValue(i));
    }
  }

  @Test
  void canCopy() {
    final float[] value = {1.1f, 2, 5, 8, 9};
    final int[] histogram = {0, 6, 7, 8, 0};
    final FloatHistogram h = new FloatHistogram(value, histogram);
    final FloatHistogram h2 = h.copy();
    Assertions.assertArrayEquals(h.histogramCounts, h2.histogramCounts);
    Assertions.assertArrayEquals(h.value, h2.value);
    Assertions.assertEquals(h.minBin, h2.minBin);
    Assertions.assertEquals(h.maxBin, h2.maxBin);
  }

  @Test
  void canBuildFloatHistogramWithNoData() {
    assertBuildFloatHistogram(new float[1], new int[1]);
    assertBuildFloatHistogram(new float[1], new int[1], null);
  }

  @Test
  void canBuildFloatHistogram() {
    assertBuildFloatHistogram(new float[] {3, 5, 6}, new int[] {1, 1, 1}, 3, 5, 6);
    assertBuildFloatHistogram(new float[] {3, 5, 6}, new int[] {1, 2, 1}, 3, 5, 5, 6);
    assertBuildFloatHistogram(new float[] {3, 5, 6}, new int[] {2, 1, 1}, 3, 3, 5, 6);
    assertBuildFloatHistogram(new float[] {3, 5, 6}, new int[] {1, 1, 2}, 3, 5, 6, 6);
    assertBuildFloatHistogram(new float[] {3, 5, 6}, new int[] {3, 1, 2}, 3, 3, 3, 5, 6, 6);
  }

  /**
   * Assert building a float histogram.
   *
   * @param values the values
   * @param histogram the histogram
   * @param data the data (must be sorted)
   */
  private static void assertBuildFloatHistogram(float[] values, int[] histogram, float... data) {
    assertBuildFloatHistogram(values, histogram, false, false, data);
    assertBuildFloatHistogram(values, histogram, true, false, data);
    assertBuildFloatHistogram(values, histogram, false, true, data);
    assertBuildFloatHistogram(values, histogram, true, true, data);
  }

  /**
   * Assert building a float histogram.
   *
   * @param values the values
   * @param histogram the histogram
   * @param doSort the do sort flag
   * @param inPlace the in place flag
   * @param data the data (must be sorted)
   */
  private static void assertBuildFloatHistogram(float[] values, int[] histogram, boolean doSort,
      boolean inPlace, float... data) {
    float[] input;
    float[] originalInput;
    if (data != null) {
      input = data.clone();
      if (doSort) {
        RandomUtils.shuffle(input, RngUtils.create(456L));
      }
      originalInput = input.clone();
    } else {
      input = originalInput = data;
    }
    FloatHistogram h;
    if (inPlace) {
      h = FloatHistogram.buildHistogram(input, doSort, inPlace);
    } else {
      h = FloatHistogram.buildHistogram(input, doSort);
    }
    Assertions.assertArrayEquals(histogram, h.histogramCounts,
        () -> String.format("doSort=%b, inPlace=%b, %s => %s, %s", doSort, inPlace,
            Arrays.toString(data), Arrays.toString(h.value), Arrays.toString(h.histogramCounts)));
    Assertions.assertArrayEquals(values, h.value);
    if (!inPlace) {
      Assertions.assertArrayEquals(originalInput, input);
    }
  }

  @Test
  void canCompactEmpty() {
    final float[] value = {13};
    final int[] histogram = {0};
    final FloatHistogram h = new FloatHistogram(value, histogram);
    final Histogram h2 = h.compact(42);
    Assertions.assertSame(h, h2);
  }

  @Test
  void canCompactToHistogram() {
    final float[] value = {2, 3, 4};
    final int[] histogram = {1, 2, 1};
    final FloatHistogram h = new FloatHistogram(value, histogram);
    final Histogram h2 = h.compact(6);
    // This is zero padded out to the specified size.
    Assertions.assertArrayEquals(new int[] {0, 0, 1, 2, 1, 0}, h2.histogramCounts);
    for (int i = 0; i < 6; i++) {
      Assertions.assertEquals(i, h2.getValue(i));
    }
  }

  @Test
  void canCompactToIntHistogram() {
    final float[] value = {-4, -3, -2};
    final int[] histogram = {1, 2, 1};
    final FloatHistogram h = new FloatHistogram(value, histogram);
    final Histogram h2 = h.compact(6);
    Assertions.assertTrue(h2 instanceof IntHistogram);
    // This is zero padded out to the specified size.
    Assertions.assertArrayEquals(new int[] {1, 2, 1, 0, 0, 0}, h2.histogramCounts);
    for (int i = 0; i < 6; i++) {
      Assertions.assertEquals(i - 4, h2.getValue(i));
    }
  }

  @Test
  void canCompactToIntHistogram2() {
    final float[] value = {2, 3, 4};
    final int[] histogram = {1, 2, 1};
    final FloatHistogram h = new FloatHistogram(value, histogram);
    final Histogram h2 = h.compact(4);
    Assertions.assertTrue(h2 instanceof IntHistogram);
    // This is zero padded out to the specified size.
    Assertions.assertArrayEquals(new int[] {1, 2, 1, 0}, h2.histogramCounts);
    for (int i = 0; i < 4; i++) {
      Assertions.assertEquals(i + 2, h2.getValue(i));
    }
  }

  @Test
  void canCompactToFloatHistogram() {
    final float[] value = {2, 3, 7};
    final int[] histogram = {1, 2, 1};
    final FloatHistogram h = new FloatHistogram(value, histogram);
    final Histogram h2 = h.compact(3);
    Assertions.assertTrue(h2 instanceof FloatHistogram);
    final FloatHistogram h3 = (FloatHistogram) h2;
    // This is zero padded out to the specified size.
    Assertions.assertArrayEquals(new int[] {3, 0, 1}, h3.histogramCounts);
    Assertions.assertArrayEquals(new float[] {2, 4.5f, 7}, h3.value);
  }

  @Test
  void canCompactToFloatHistogram2() {
    final float[] value = {2, 3.5f, 4};
    final int[] histogram = {1, 2, 1};
    final FloatHistogram h = new FloatHistogram(value, histogram);
    final Histogram h2 = h.compact(3);
    Assertions.assertTrue(h2 instanceof FloatHistogram);
    final FloatHistogram h3 = (FloatHistogram) h2;
    // This is zero padded out to the specified size.
    Assertions.assertArrayEquals(new int[] {1, 0, 3}, h3.histogramCounts);
    Assertions.assertArrayEquals(new float[] {2, 3, 4}, h3.value);
  }

  @Test
  void canCompactToFloatHistogram3() {
    final float[] value = {2.5f, 3, 10.5f};
    final int[] histogram = {1, 2, 1};
    final FloatHistogram h = new FloatHistogram(value, histogram);
    final Histogram h2 = h.compact(3);
    Assertions.assertTrue(h2 instanceof FloatHistogram);
    final FloatHistogram h3 = (FloatHistogram) h2;
    // This is zero padded out to the specified size.
    Assertions.assertArrayEquals(new int[] {3, 0, 1}, h3.histogramCounts);
    Assertions.assertArrayEquals(new float[] {2.5f, 6.5f, 10.5f}, h3.value);
  }

  @Test
  void canCompactToFloatHistogram4() {
    final float[] value = {2, 3, 10.5f};
    final int[] histogram = {1, 2, 1};
    final FloatHistogram h = new FloatHistogram(value, histogram);
    final Histogram h2 = h.compact(3);
    Assertions.assertTrue(h2 instanceof FloatHistogram);
    final FloatHistogram h3 = (FloatHistogram) h2;
    // This is zero padded out to the specified size.
    Assertions.assertArrayEquals(new int[] {3, 0, 1}, h3.histogramCounts);
    Assertions.assertArrayEquals(new float[] {2, 2 + (10.5f - 2) / 2, 10.5f}, h3.value);
  }

  @Test
  void testCompactThrows() {
    final float[] value = {2.5f, 3, 10.5f};
    final int[] histogram = {1, 2, 1};
    final FloatHistogram h = new FloatHistogram(value, histogram);
    Assertions.assertThrows(IllegalArgumentException.class, () -> h.compact(0));
    Assertions.assertThrows(IllegalArgumentException.class, () -> h.compact(1));
  }

  @Test
  void testGetThresholdWithNoHistogramRange() {
    final float[] value = {2, 3, 10};
    final int[] histogram = {0, 1, 0};
    final FloatHistogram h = new FloatHistogram(value, histogram);
    Assertions.assertEquals(Float.NEGATIVE_INFINITY, h.getThreshold(Method.OTSU));
  }

  @Test
  void testGetThreshold() {
    final float[] value = {2, 3, 4, 5, 6, 7, 8, 9, 10};
    final int[] histogram = {0, 1, 5, 1, 0, 1, 0, 1, 1};
    final FloatHistogram h = new FloatHistogram(value, histogram);
    for (final Method method : new Method[] {Method.OTSU, Method.MEAN}) {
      Assertions.assertEquals(value[0] + AutoThreshold.getThreshold(method, histogram),
          h.getThreshold(method));
    }
  }
}
