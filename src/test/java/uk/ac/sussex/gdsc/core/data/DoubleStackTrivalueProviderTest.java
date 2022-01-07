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

import ij.process.FloatProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;

@SuppressWarnings({"javadoc"})
class DoubleStackTrivalueProviderTest {
  @Test
  void testConstructorThrows() {
    final int maxx = 5;
    final int maxy = 4;
    Assertions.assertThrows(DataException.class,
        () -> new DoubleStackTrivalueProvider(new double[0][0], maxx, maxy));
    Assertions.assertThrows(DataException.class,
        () -> new DoubleStackTrivalueProvider(new double[][] {new double[maxx * maxy + 1]}, maxx,
            maxy));
  }

  @SuppressWarnings("null")
  @Test
  void canProvideData() {
    final int maxx = 5;
    final int maxy = 4;
    final int maxz = 3;
    final int size = maxx * maxy;
    final double[][] data = new double[maxz][];
    for (int z = 0; z < maxz; z++) {
      data[z] = SimpleArrayUtils.newArray(size, z, 1.0);
    }

    final DoubleStackTrivalueProvider f = new DoubleStackTrivalueProvider(data, maxx, maxy);
    Assertions.assertEquals(maxx, f.getLengthX());
    Assertions.assertEquals(maxy, f.getLengthY());
    Assertions.assertEquals(maxz, f.getLengthZ());

    final double[][][] values = new double[3][3][3];

    final int[] test = {-1, 0, 1};

    // Test with FloatProcessor as that is the likely source of the stack of data
    for (int z = 0; z < maxz; z++) {
      final FloatProcessor fp = new FloatProcessor(maxx, maxy, data[z]);
      FloatProcessor fpp = null;
      FloatProcessor fpn = null;
      if (z > 0 && z < maxz - 1) {
        fpp = new FloatProcessor(maxx, maxy, data[z - 1]);
        fpn = new FloatProcessor(maxx, maxy, data[z + 1]);
      }

      for (int y = 0; y < maxy; y++) {
        for (int x = 0; x < maxx; x++) {
          Assertions.assertEquals(fp.getPixelValue(x, y), f.get(x, y, z));

          if (x > 0 && x < maxx - 1 && y > 0 && y < maxy - 1 && fpp != null) {
            f.get(x, y, z, values);

            for (final int i : test) {
              for (final int j : test) {
                Assertions.assertEquals(fpp.getPixelValue(x + i, y + j), values[i + 1][j + 1][0]);
                Assertions.assertEquals(fp.getPixelValue(x + i, y + j), values[i + 1][j + 1][1]);
                Assertions.assertEquals(fpn.getPixelValue(x + i, y + j), values[i + 1][j + 1][2]);
              }
            }
          }
        }
      }
    }
  }

  @Test
  void canConvertToArray() {
    final int maxx = 5;
    final int maxy = 4;
    final int maxz = 3;
    final int size = maxx * maxy;
    final double[][] data = new double[maxz][];
    for (int z = 0; z < maxz; z++) {
      data[z] = SimpleArrayUtils.newArray(size, z, (z + 1) * 2.0);
    }
    final DoubleStackTrivalueProvider f = new DoubleStackTrivalueProvider(data, maxx, maxy);
    final double[][][] e = new double[maxx][maxy][maxz];
    for (int x = 0; x < maxx; x++) {
      for (int y = 0; y < maxy; y++) {
        for (int z = 0; z < maxz; z++) {
          e[x][y][z] = f.get(x, y, z);
        }
      }
    }
    final double[][][] o = f.toArray();
    for (int x = 0; x < maxx; x++) {
      for (int y = 0; y < maxy; y++) {
        for (int z = 0; z < maxz; z++) {
          Assertions.assertEquals(e[x][y][z], o[x][y][z]);
        }
      }
    }
  }
}
