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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class DoubleRollingArrayTest {
  @Test
  void canAddRollingData() {
    final DoubleRollingArray array = new DoubleRollingArray(3);
    Assertions.assertEquals(3, array.getCapacity());
    assertArray(array, 0, 0, Double.NaN, new double[0]);
    array.add(1);
    assertArray(array, 1, 1, 1, new double[] {1});
    array.add(2);
    assertArray(array, 2, 3, 1.5, new double[] {1, 2});
    array.add(3);
    assertArray(array, 3, 6, 2, new double[] {1, 2, 3});
    array.add(4);
    assertArray(array, 3, 9, 3, new double[] {2, 3, 4});
    array.add(5);
    assertArray(array, 3, 12, 4, new double[] {3, 4, 5});
    array.clear();
    assertArray(array, 0, 0, Double.NaN, new double[0]);
    array.add(6);
    assertArray(array, 1, 6, 6, new double[] {6});
    array.add(7);
    assertArray(array, 2, 13, 6.5, new double[] {6, 7});
    array.add(8);
    assertArray(array, 3, 21, 7, new double[] {6, 7, 8});
    array.add(9);
    assertArray(array, 3, 24, 8, new double[] {7, 8, 9});
  }

  @Test
  void canAddRepeats() {
    final DoubleRollingArray array = new DoubleRollingArray(3);
    array.add(1, 2);
    assertArray(array, 2, 2, 1, new double[] {1, 1});
    array.add(2, 1);
    assertArray(array, 3, 4, 4.0 / 3, new double[] {1, 1, 2});
    array.add(3, 2);
    assertArray(array, 3, 8, 8.0 / 3, new double[] {2, 3, 3});
    array.add(4, 10);
    assertArray(array, 3, 12, 4, new double[] {4, 4, 4});
  }

  private static void assertArray(DoubleRollingArray array, int count, double sum, double average,
      double[] data) {
    Assertions.assertEquals(count, array.getCount());
    Assertions.assertEquals(sum, array.getSum());
    Assertions.assertEquals(average, array.getAverage());
    Assertions.assertEquals(array.getCount() == array.getCapacity(), array.isFull());
    Assertions.assertArrayEquals(data, array.toArray());
    double total = 0;
    for (final double value : data) {
      total += value;
    }
    Assertions.assertEquals(total, array.computeAndGetSum());
    Assertions.assertEquals(total / data.length, array.computeAndGetAverage());
  }
}
