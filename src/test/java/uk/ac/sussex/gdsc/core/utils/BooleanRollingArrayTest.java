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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class BooleanRollingArrayTest {
  @Test
  void canAddRollingData() {
    final BooleanRollingArray array = new BooleanRollingArray(3);
    Assertions.assertEquals(3, array.getCapacity());
    assertArray(array, 0, 0, new boolean[0]);
    array.add(true);
    assertArray(array, 1, 1, new boolean[] {true});
    array.add(false);
    assertArray(array, 2, 1, new boolean[] {true, false});
    array.add(false);
    assertArray(array, 3, 1, new boolean[] {true, false, false});
    array.add(true);
    assertArray(array, 3, 1, new boolean[] {false, false, true});
    array.add(false);
    assertArray(array, 3, 1, new boolean[] {false, true, false});
    array.clear();
    assertArray(array, 0, 0, new boolean[0]);
    array.add(false);
    assertArray(array, 1, 0, new boolean[] {false});
    array.add(true);
    assertArray(array, 2, 1, new boolean[] {false, true});
    array.add(true);
    assertArray(array, 3, 2, new boolean[] {false, true, true});
    array.add(false);
    assertArray(array, 3, 2, new boolean[] {true, true, false});
  }

  @Test
  void canAddRepeats() {
    final BooleanRollingArray array = new BooleanRollingArray(3);
    array.add(true, 2);
    assertArray(array, 2, 2, new boolean[] {true, true});
    array.add(false, 1);
    assertArray(array, 3, 2, new boolean[] {true, true, false});
    array.add(true, 2);
    assertArray(array, 3, 2, new boolean[] {false, true, true});
    array.add(false, 10);
    assertArray(array, 3, 0, new boolean[] {false, false, false});
  }

  private static void assertArray(BooleanRollingArray array, int count, int trueCount,
      boolean[] data) {
    Assertions.assertEquals(count, array.getCount());
    Assertions.assertEquals(trueCount, array.getTrueCount());
    Assertions.assertEquals(count - trueCount, array.getFalseCount());
    Assertions.assertEquals(array.getCount() == array.getCapacity(), array.isFull());
    Assertions.assertArrayEquals(data, array.toArray());
  }
}
