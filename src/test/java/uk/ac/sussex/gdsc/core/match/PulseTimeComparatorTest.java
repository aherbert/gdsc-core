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

package uk.ac.sussex.gdsc.core.match;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link PulseTimeComparator}.
 */
@SuppressWarnings({"javadoc"})
public class PulseTimeComparatorTest {
  @Test
  public void canCompare() {
    // Compare using start
    assertOverlap(0, 1, 1, 1, 1);
    assertOverlap(-1, 1, 1, 2, 2);
    assertOverlap(1, 2, 2, 1, 1);
    // Equal start should compare using end, lowest first
    assertOverlap(1, 1, 2, 1, 1);
    assertOverlap(-1, 1, 1, 1, 2);
  }

  private static void assertOverlap(int result, int start1, int end1, int start2, int end2) {
    final Pulse data1 = new Pulse(0f, 0f, start1, end1);
    final Pulse data2 = new Pulse(0f, 0f, start2, end2);
    Assertions.assertEquals(result, PulseTimeComparator.getInstance().compare(data1, data2));
  }
}
