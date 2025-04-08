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

package uk.ac.sussex.gdsc.core.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class DoubleArrayBivalueProviderTest {
  @Test
  void testConstructorThrows() {
    final int maxx = 5;
    final int maxy = 4;
    Assertions.assertThrows(DataException.class,
        () -> new DoubleArrayBivalueProvider(new double[0][0]));
    Assertions.assertThrows(DataException.class,
        () -> new DoubleArrayBivalueProvider(new double[maxx][0]));
    Assertions.assertThrows(DataException.class, () -> new DoubleArrayBivalueProvider(
        new double[][] {new double[maxy], new double[maxy + 1]}));
  }

  @Test
  void canProvideData() {
    final int maxx = 5;
    final int maxy = 4;
    final double[][] data = new double[maxx][maxy];
    for (int x = 0, i = 0; x < maxx; x++) {
      for (int y = 0; y < maxy; y++) {
        data[x][y] = i++;
      }
    }

    final DoubleArrayBivalueProvider f = new DoubleArrayBivalueProvider(data);
    Assertions.assertEquals(maxx, f.getLengthX());
    Assertions.assertEquals(maxy, f.getLengthY());
    Assertions.assertSame(data, f.toArray());

    final double[][] values = new double[3][3];

    final int[] test = {-1, 0, 1};

    for (int x = 0; x < maxx; x++) {
      for (int y = 0; y < maxy; y++) {
        Assertions.assertEquals(data[x][y], f.get(x, y));

        if (x > 0 && x < maxx - 1 && y > 0 && y < maxy - 1) {
          f.get(x, y, values);

          for (final int i : test) {
            for (final int j : test) {
              Assertions.assertEquals(data[x + i][y + j], values[i + 1][j + 1]);
            }
          }
        }
      }
    }
  }
}
