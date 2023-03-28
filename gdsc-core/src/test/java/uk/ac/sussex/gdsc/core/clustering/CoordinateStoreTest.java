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

package uk.ac.sussex.gdsc.core.clustering;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class CoordinateStoreTest {
  @Test
  void testConstructorThrows() {
    final float[] xcoord = {1, 2};
    final float[] ycoord = {3, 4};
    final double area = 45;
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> new CoordinateStore(null, ycoord, area));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> new CoordinateStore(xcoord, null, area));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> new CoordinateStore(new float[1], ycoord, area));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> new CoordinateStore(new float[0], new float[0], area));
  }

  @Test
  void testCoordinateStore() {
    final float[] xcoord = {1.1f, 2.5f};
    final float[] ycoord = {3.2f, 8.5f};
    final double area = 45;
    Assertions.assertEquals(area, new CoordinateStore(xcoord.clone(), ycoord.clone(), area).area);
    final CoordinateStore store = new CoordinateStore(xcoord.clone(), ycoord.clone(), 0);
    Assertions.assertEquals(1.4f * 5.3f, store.area);
    Assertions.assertEquals(2, store.getSize());
    Assertions.assertEquals(xcoord[0] - 1, store.getMinimumX());
    Assertions.assertEquals(xcoord[1] - 1, store.getMaximumX());
    Assertions.assertEquals(ycoord[0] - 3, store.getMinimumY());
    Assertions.assertEquals(ycoord[1] - 3, store.getMaximumY());
    Assertions.assertArrayEquals(
        new float[][] {{xcoord[0] - 1, xcoord[1] - 1}, {ycoord[0] - 3, ycoord[1] - 3}},
        store.getData());
    Assertions.assertArrayEquals(
        new double[][] {{xcoord[0] - 1, xcoord[1] - 1}, {ycoord[0] - 3, ycoord[1] - 3}},
        store.getDoubleData());

    for (final boolean value : new boolean[] {true, false}) {
      final CoordinateStore store2 = store.copy(value);
      Assertions.assertEquals(1.4f * 5.3f, store2.area);
      Assertions.assertEquals(2, store2.getSize());
      Assertions.assertEquals(xcoord[0] - 1, store2.getMinimumX());
      Assertions.assertEquals(xcoord[1] - 1, store2.getMaximumX());
      Assertions.assertEquals(ycoord[0] - 3, store2.getMinimumY());
      Assertions.assertEquals(ycoord[1] - 3, store2.getMaximumY());
      Assertions.assertArrayEquals(
          new float[][] {{xcoord[0] - 1, xcoord[1] - 1}, {ycoord[0] - 3, ycoord[1] - 3}},
          store2.getData());
      Assertions.assertArrayEquals(
          new double[][] {{xcoord[0] - 1, xcoord[1] - 1}, {ycoord[0] - 3, ycoord[1] - 3}},
          store2.getDoubleData());
    }
  }
}
