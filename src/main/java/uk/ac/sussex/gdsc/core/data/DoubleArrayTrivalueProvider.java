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

/**
 * Provide data on 3-axes from an array of doubles.
 */
public class DoubleArrayTrivalueProvider implements TrivalueProvider {
  /** The length (max) of the x-dimension. */
  private final int maxx;
  /** The length (max) of the y-dimension. */
  private final int maxy;
  /** The length (max) of the z-dimension. */
  private final int maxz;
  /** The data. */
  private final double[][][] data;

  /**
   * Creates a new instance.
   *
   * <p>The input array in wrapped; that is, modifications to the array will cause the provided data
   * to be modified.
   *
   * @param data the data
   * @throws DataException If any dimension is length zero or if there is a dimension mismatch
   */
  public DoubleArrayTrivalueProvider(double[][][] data) {
    if (data.length == 0) {
      throw new DataException("No X data");
    }
    if (data[0].length == 0) {
      throw new DataException("No Y data");
    }
    if (data[0][0].length == 0) {
      throw new DataException("No Z data");
    }
    maxx = data.length;
    maxy = data[0].length;
    maxz = data[0][0].length;
    for (int x = 0; x < maxx; x++) {
      if (maxy != data[x].length) {
        throw new DataException("Y data must be the same length");
      }
      for (int y = 0; y < maxy; y++) {
        if (maxz != data[x][y].length) {
          throw new DataException("Z data must be the same length");
        }
      }
    }
    this.data = data;
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
    return data[x][y][z];
  }

  @Override
  public void get(int x, int y, int z, double[][][] values) {
    final int nX = x + 1;
    final int pX = x - 1;
    final int nY = y + 1;
    final int pY = y - 1;
    final int nZ = z + 1;
    final int pZ = z - 1;

    values[0][0][0] = data[pX][pY][pZ];
    values[0][0][1] = data[pX][pY][z];
    values[0][0][2] = data[pX][pY][nZ];
    values[0][1][0] = data[pX][y][pZ];
    values[0][1][1] = data[pX][y][z];
    values[0][1][2] = data[pX][y][nZ];
    values[0][2][0] = data[pX][nY][pZ];
    values[0][2][1] = data[pX][nY][z];
    values[0][2][2] = data[pX][nY][nZ];
    values[1][0][0] = data[x][pY][pZ];
    values[1][0][1] = data[x][pY][z];
    values[1][0][2] = data[x][pY][nZ];
    values[1][1][0] = data[x][y][pZ];
    values[1][1][1] = data[x][y][z];
    values[1][1][2] = data[x][y][nZ];
    values[1][2][0] = data[x][nY][pZ];
    values[1][2][1] = data[x][nY][z];
    values[1][2][2] = data[x][nY][nZ];
    values[2][0][0] = data[nX][pY][pZ];
    values[2][0][1] = data[nX][pY][z];
    values[2][0][2] = data[nX][pY][nZ];
    values[2][1][0] = data[nX][y][pZ];
    values[2][1][1] = data[nX][y][z];
    values[2][1][2] = data[nX][y][nZ];
    values[2][2][0] = data[nX][nY][pZ];
    values[2][2][1] = data[nX][nY][z];
    values[2][2][2] = data[nX][nY][nZ];
  }

  @Override
  public double[][][] toArray() {
    return data;
  }
}
