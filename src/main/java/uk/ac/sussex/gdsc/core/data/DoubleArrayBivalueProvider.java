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
 * Copyright (C) 2011 - 2021 Alex Herbert
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
 * Provide data on 2-axes from an array of doubles.
 */
public class DoubleArrayBivalueProvider implements BivalueProvider {

  /** The length (max) of the x-dimension. */
  private final int maxx;
  /** The length (max) of the y-dimension. */
  private final int maxy;
  /** The data. */
  private final double[][] data;

  /**
   * Creates a new instance.
   *
   * <p>The input array in wrapped; that is, modifications to the array will cause the provided data
   * to be modified.
   *
   * @param data the data
   * @throws DataException If any dimension is length zero or if there is a dimension mismatch
   */
  public DoubleArrayBivalueProvider(double[][] data) {
    if (data.length == 0) {
      throw new DataException("No X data");
    }
    if (data[0].length == 0) {
      throw new DataException("No Y data");
    }
    maxx = data.length;
    maxy = data[0].length;
    for (int x = 0; x < maxx; x++) {
      if (maxy != data[x].length) {
        throw new DataException("Y data must be the same length");
      }
    }
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
  public double get(int x, int y) {
    return data[x][y];
  }

  @Override
  public void get(int x, int y, double[][] values) {
    final int nX = x + 1;
    final int pX = x - 1;
    final int nY = y + 1;
    final int pY = y - 1;

    values[0][0] = data[pX][pY];
    values[0][1] = data[pX][y];
    values[0][2] = data[pX][nY];
    values[1][0] = data[x][pY];
    values[1][1] = data[x][y];
    values[1][2] = data[x][nY];
    values[2][0] = data[nX][pY];
    values[2][1] = data[nX][y];
    values[2][2] = data[nX][nY];
  }

  @Override
  public double[][] toArray() {
    // Documented to wrap the reference directly
    return data;
  }
}
