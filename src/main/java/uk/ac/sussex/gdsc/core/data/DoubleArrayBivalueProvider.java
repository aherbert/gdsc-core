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

import java.util.Arrays;

/**
 * Provide data on 2-axes from an array of doubles.
 */
public class DoubleArrayBivalueProvider implements BivalueProvider {
  private final int maxx;
  private final int maxy;
  private final double[][] val;

  /**
   * Instantiates a new double array trivalue provider.
   *
   * @param val the val
   * @throws DataException If the array is missing data
   */
  public DoubleArrayBivalueProvider(double[][] val) throws DataException {
    if (val.length == 0) {
      throw new DataException("No X data");
    }
    if (val[0].length == 0) {
      throw new DataException("No Y data");
    }
    this.val = val;
    maxx = val.length;
    maxy = val[0].length;
    for (int x = 0; x < maxx; x++) {
      if (maxy != val[x].length) {
        throw new DataException("Y data must be the same length");
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
  public double get(int x, int y) {
    return val[x][y];
  }

  @Override
  public void get(int x, int y, double[][] values) {
    final int nX = x + 1;
    final int pX = x - 1;
    final int nY = y + 1;
    final int pY = y - 1;

    values[0][0] = val[pX][pY];
    values[0][1] = val[pX][y];
    values[0][2] = val[pX][nY];
    values[1][0] = val[x][pY];
    values[1][1] = val[x][y];
    values[1][2] = val[x][nY];
    values[2][0] = val[nX][pY];
    values[2][1] = val[nX][y];
    values[2][2] = val[nX][nY];
  }

  @Override
  public double[][] toArray() {
    return Arrays.stream(val).map(double[]::clone).toArray(double[][]::new);
  }
}
