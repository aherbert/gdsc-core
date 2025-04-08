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
 * Copyright (C) 2011 - 2025 Alex Herbert
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class DoubleDataTest {
  @Test
  void canWrapDoubleArray() {
    Assertions.assertThrows(NullPointerException.class, () -> DoubleData.wrap(null));
    final double[] data = {0, 1, 2, 3, 4};
    final DoubleData dd = DoubleData.wrap(data);
    Assertions.assertEquals(data.length, dd.size());
    Assertions.assertSame(data, dd.values());
    final int[] count = {0};
    dd.forEach(d -> Assertions.assertEquals(data[count[0]++], d));
    Assertions.assertEquals(data.length, count[0]);
  }
}
