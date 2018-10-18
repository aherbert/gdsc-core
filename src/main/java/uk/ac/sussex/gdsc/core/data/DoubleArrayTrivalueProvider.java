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
 * Copyright (C) 2011 - 2018 Alex Herbert
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

import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;

import java.util.Arrays;

/**
 * Provide data on 3-axes from an array of doubles.
 */
public class DoubleArrayTrivalueProvider implements TrivalueProvider {
  private final int maxx;
  private final int maxy;
  private final int maxz;
  private final double[][][] val;

  /**
   * Instantiates a new double array trivalue provider.
   *
   * @param val the val
   * @throws DataException If the array is missing data
   */
  public DoubleArrayTrivalueProvider(double[][][] val) throws DataException {
    if (val.length == 0) {
      throw new DataException("No X data");
    }
    if (val[0].length == 0) {
      throw new DataException("No Y data");
    }
    if (val[0][0].length == 0) {
      throw new DataException("No Z data");
    }
    this.val = val;
    maxx = val.length;
    maxy = val[0].length;
    maxz = val[0][0].length;
    for (int x = 0; x < maxx; x++) {
      if (maxy != val[x].length) {
        throw new DataException("Y data must be the same length");
      }
      for (int j = 0; j < maxy; j++) {
        if (maxz != val[x][j].length) {
          throw new DataException("Z data must be the same length");
        }
      }
    }
  }

  @Override
  public int getLengthX() {
    return maxx;
  }

  @Override
  public int getLengthY() {
    return maxy;
  }

  @Override
  public int getLengthZ() {
    return maxz;
  }

  @Override
  public double get(int x, int y, int z) {
    return val[x][y][z];
  }

  @Override
  public void get(int x, int y, int z, double[][][] values) {
    final int nX = x + 1;
    final int pX = x - 1;
    final int nY = y + 1;
    final int pY = y - 1;
    final int nZ = z + 1;
    final int pZ = z - 1;

    values[0][0][0] = val[pX][pY][pZ];
    values[0][0][1] = val[pX][pY][z];
    values[0][0][2] = val[pX][pY][nZ];
    values[0][1][0] = val[pX][y][pZ];
    values[0][1][1] = val[pX][y][z];
    values[0][1][2] = val[pX][y][nZ];
    values[0][2][0] = val[pX][nY][pZ];
    values[0][2][1] = val[pX][nY][z];
    values[0][2][2] = val[pX][nY][nZ];
    values[1][0][0] = val[x][pY][pZ];
    values[1][0][1] = val[x][pY][z];
    values[1][0][2] = val[x][pY][nZ];
    values[1][1][0] = val[x][y][pZ];
    values[1][1][1] = val[x][y][z];
    values[1][1][2] = val[x][y][nZ];
    values[1][2][0] = val[x][nY][pZ];
    values[1][2][1] = val[x][nY][z];
    values[1][2][2] = val[x][nY][nZ];
    values[2][0][0] = val[nX][pY][pZ];
    values[2][0][1] = val[nX][pY][z];
    values[2][0][2] = val[nX][pY][nZ];
    values[2][1][0] = val[nX][y][pZ];
    values[2][1][1] = val[nX][y][z];
    values[2][1][2] = val[nX][y][nZ];
    values[2][2][0] = val[nX][nY][pZ];
    values[2][2][1] = val[nX][nY][z];
    values[2][2][2] = val[nX][nY][nZ];
  }

  @Override
  public double[][][] toArray() {
    return Arrays.stream(val).map(
        // Function to clone each double[][] element of double[][][]
        SimpleArrayUtils::deepCopy).toArray(double[][][]::new);
  }
}
