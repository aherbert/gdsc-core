/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
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

package uk.ac.sussex.gdsc.core.threshold;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class IntHistogramTest {
  @Test
  void canCreateHistogram() {
    final int offset = 3;
    final int[] histogram = {0, 6, 7, 8, 0};
    final IntHistogram h = new IntHistogram(histogram, offset);
    Assertions.assertEquals(1, h.minBin);
    Assertions.assertEquals(3, h.maxBin);
    for (int i = 0; i < histogram.length; i++) {
      Assertions.assertEquals(i + offset, h.getValue(i));
    }
  }

  @Test
  void canCopy() {
    final int offset = 3;
    final int[] histogram = {0, 6, 7, 8, 0};
    final IntHistogram h = new IntHistogram(histogram, offset);
    final IntHistogram h2 = h.copy();
    Assertions.assertArrayEquals(h.histogramCounts, h2.histogramCounts);
    Assertions.assertEquals(h.minBin, h2.minBin);
    Assertions.assertEquals(h.maxBin, h2.maxBin);
  }
}
