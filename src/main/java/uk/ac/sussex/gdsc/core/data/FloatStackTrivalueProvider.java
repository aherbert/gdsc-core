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

package uk.ac.sussex.gdsc.core.data;

/**
 * Provide data on 3-axes from a stack of XY float data.
 */
public class FloatStackTrivalueProvider implements TrivalueProvider {
  /** The length (max) of the x-dimension. */
  private final int maxx;
  /** The length (max) of the y-dimension. */
  private final int maxy;
  /** The data. */
  private final float[][] data;

  /**
   * Creates a new instance.
   *
   * <p>The input array in wrapped; that is, modifications to the array will cause the provided data
   * to be modified.
   *
   * @param data the stack of values. Each array is packed in yx order.
   * @param maxx the length in the x-dimension
   * @param maxy the length in the y-dimension
   * @throws DataException If any dimension is length zero or if there is a dimension mismatch
   */
  public FloatStackTrivalueProvider(float[][] data, int maxx, int maxy) {
    if (data.length == 0) {
      throw new DataException("No data");
    }
    final int size = maxx * maxy;
    for (final float[] xyData : data) {
      if (size != xyData.length) {
        throw new DataException("XY data must be length " + size);
      }
    }
    this.maxx = maxx;
    this.maxy = maxy;
    // Documented to wrap the reference directly
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
    return data.length;
  }

  /**
   * Gets the index of the point in the XY data.
   *
   * @param x the x
   * @param y the y
   * @return the index
   */
  public int getIndex(int x, int y) {
    return y * maxx + x;
  }

  @Override
  public double get(int x, int y, int z) {
    return data[z][getIndex(x, y)];
  }

  @Override
  public void get(int x, int y, int z, double[][][] values) {
    final int cXcY = getIndex(x, y);
    final int pXcY = cXcY - 1;
    final int nXcY = cXcY + 1;
    final int cXpY = cXcY - maxx;
    final int pXpY = cXpY - 1;
    final int nXpY = cXpY + 1;
    final int cXnY = cXcY + maxx;
    final int pXnY = cXnY - 1;
    final int nXnY = cXnY + 1;
    final int pZ = z - 1;
    final int cZ = z;
    final int nZ = z + 1;

    values[0][0][0] = data[pZ][pXpY];
    values[0][0][1] = data[cZ][pXpY];
    values[0][0][2] = data[nZ][pXpY];
    values[0][1][0] = data[pZ][pXcY];
    values[0][1][1] = data[cZ][pXcY];
    values[0][1][2] = data[nZ][pXcY];
    values[0][2][0] = data[pZ][pXnY];
    values[0][2][1] = data[cZ][pXnY];
    values[0][2][2] = data[nZ][pXnY];
    values[1][0][0] = data[pZ][cXpY];
    values[1][0][1] = data[cZ][cXpY];
    values[1][0][2] = data[nZ][cXpY];
    values[1][1][0] = data[pZ][cXcY];
    values[1][1][1] = data[cZ][cXcY];
    values[1][1][2] = data[nZ][cXcY];
    values[1][2][0] = data[pZ][cXnY];
    values[1][2][1] = data[cZ][cXnY];
    values[1][2][2] = data[nZ][cXnY];
    values[2][0][0] = data[pZ][nXpY];
    values[2][0][1] = data[cZ][nXpY];
    values[2][0][2] = data[nZ][nXpY];
    values[2][1][0] = data[pZ][nXcY];
    values[2][1][1] = data[cZ][nXcY];
    values[2][1][2] = data[nZ][nXcY];
    values[2][2][0] = data[pZ][nXnY];
    values[2][2][1] = data[cZ][nXnY];
    values[2][2][2] = data[nZ][nXnY];
  }

  @Override
  public double[][][] toArray() {
    final double[][][] xyz = new double[maxx][maxy][getLengthZ()];
    for (int z = 0; z < data.length; z++) {
      final float[] data2D = data[z];
      int index = 0;
      for (int y = 0; y < maxy; y++) {
        for (int x = 0; x < maxx; x++, index++) {
          xyz[x][y][z] = data2D[index];
        }
      }
    }
    return xyz;
  }
}
