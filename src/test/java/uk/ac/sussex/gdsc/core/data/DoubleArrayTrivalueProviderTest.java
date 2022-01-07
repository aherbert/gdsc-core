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

package uk.ac.sussex.gdsc.core.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class DoubleArrayTrivalueProviderTest {
  @Test
  void testConstructorThrows() {
    final int maxx = 5;
    final int maxy = 4;
    final int maxz = 3;
    Assertions.assertThrows(DataException.class,
        () -> new DoubleArrayTrivalueProvider(new double[0][0][0]));
    Assertions.assertThrows(DataException.class,
        () -> new DoubleArrayTrivalueProvider(new double[maxx][0][0]));
    Assertions.assertThrows(DataException.class,
        () -> new DoubleArrayTrivalueProvider(new double[maxx][maxy][0]));
    Assertions.assertThrows(DataException.class, () -> new DoubleArrayTrivalueProvider(
        new double[][][] {new double[maxy][maxz], new double[maxy + 1][maxz]}));
    Assertions.assertThrows(DataException.class, () -> new DoubleArrayTrivalueProvider(
        new double[][][] {new double[maxy][maxz], new double[maxy][maxz + 1]}));
  }

  @Test
  void canProvideData() {
    final int maxx = 5;
    final int maxy = 4;
    final int maxz = 3;
    final double[][][] data = new double[maxx][maxy][maxz];
    for (int x = 0, i = 0; x < maxx; x++) {
      for (int y = 0; y < maxy; y++) {
        for (int z = 0; z < maxz; z++) {
          data[x][y][z] = i++;
        }
      }
    }

    final DoubleArrayTrivalueProvider f = new DoubleArrayTrivalueProvider(data);
    Assertions.assertEquals(maxx, f.getLengthX());
    Assertions.assertEquals(maxy, f.getLengthY());
    Assertions.assertEquals(maxz, f.getLengthZ());
    Assertions.assertSame(data, f.toArray());

    final double[][][] values = new double[3][3][3];

    final int[] test = {-1, 0, 1};

    for (int x = 0; x < maxx; x++) {
      for (int y = 0; y < maxy; y++) {
        for (int z = 0; z < maxz; z++) {
          Assertions.assertEquals(data[x][y][z], f.get(x, y, z));

          if (x > 0 && x < maxx - 1 && y > 0 && y < maxy - 1 && z > 0 && z < maxz - 1) {
            f.get(x, y, z, values);

            for (final int i : test) {
              for (final int j : test) {
                for (final int k : test) {
                  Assertions.assertEquals(data[x + i][y + j][z + k], values[i + 1][j + 1][k + 1]);
                }
              }
            }
          }
        }
      }
    }
  }
}
