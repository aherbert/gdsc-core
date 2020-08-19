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
public class HistogramTest {
  @Test
  public void canCreateHistogram() {
    final int[] histogram = {0, 6, 7, 8, 0};
    final Histogram h = new Histogram(histogram);
    Assertions.assertEquals(1, h.minBin);
    Assertions.assertEquals(3, h.maxBin);
    for (int i = 0; i < histogram.length; i++) {
      Assertions.assertEquals(i, h.getValue(i));
    }
  }

  @Test
  public void canCopy() {
    final int[] histogram = {0, 6, 7, 8, 0};
    final Histogram h = new Histogram(histogram);
    final Histogram h2 = h.copy();
    Assertions.assertArrayEquals(h.histogramCounts, h2.histogramCounts);
    Assertions.assertEquals(h.minBin, h2.minBin);
    Assertions.assertEquals(h.maxBin, h2.maxBin);
  }

  @Test
  public void canBuildHistogramWithNoData() {
    assertBuildHistogram(new int[1], new int[1]);
    assertBuildHistogram(new int[1], new int[1], null);
  }

  @Test
  public void canBuildHistogram() {
    assertBuildHistogram(new int[] {3, 4, 5, 6}, new int[] {1, 0, 1, 1}, 3, 5, 6);
    assertBuildHistogram(new int[] {3, 4, 5, 6}, new int[] {1, 0, 2, 1}, 3, 5, 5, 6);
    assertBuildHistogram(new int[] {3, 4, 5, 6}, new int[] {2, 0, 1, 1}, 3, 3, 5, 6);
    assertBuildHistogram(new int[] {3, 4, 5, 6}, new int[] {1, 0, 1, 2}, 3, 5, 6, 6);
    assertBuildHistogram(new int[] {3, 4, 5, 6}, new int[] {3, 0, 1, 2}, 3, 3, 3, 5, 6, 6);
    assertBuildHistogram(new int[] {0, 1, 2, 3}, new int[] {3, 0, 1, 2}, 0, 0, 0, 2, 3, 3);
  }

  /**
   * Assert building a float histogram.
   *
   * @param values the values
   * @param histogram the histogram
   * @param data the data (must be sorted)
   */
  private static void assertBuildHistogram(int[] values, int[] histogram, int... data) {
    final int[] input = data == null ? null : data.clone();
    for (int i = 0; i < 3; i++) {
      final Histogram h = Histogram.buildHistogram(input);
      Assertions.assertArrayEquals(histogram, h.histogramCounts, () -> String.format("%s => %s",
          Arrays.toString(data), Arrays.toString(h.histogramCounts)));
      for (int j = 0; j < values.length; j++) {
        Assertions.assertEquals(values[j], h.getValue(j));
      }
      if (data == null) {
        break;
      }
      RandomUtils.shuffle(input, RngUtils.create(456L));
    }
  }

  @Test
  public void canCompact() {
    final int[] histogram = {0, 6, 7, 8, 0};
    final Histogram h = new Histogram(histogram);
    Assertions.assertSame(h, h.compact(3));
  }

  @Test
  public void testGetThreshold() {
    final int[] histogram = {4, 1, 5, 1, 0, 1, 0, 1, 1};
    for (int i = 0; i < 3; i++) {
      final Histogram h = new Histogram(histogram);
      for (final Method method : new Method[] {Method.OTSU, Method.MEAN}) {
        Assertions.assertEquals(AutoThreshold.getThreshold(method, histogram),
            h.getThreshold(method));
      }
      histogram[i] = 0;
    }
  }
}
