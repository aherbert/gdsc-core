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
 * Provide data on 1-axis from an array of doubles.
 */
public class DoubleArrayValueProvider implements ValueProvider {

  /** The data. */
  private final double[] data;

  /**
   * Creates a new instance.
   *
   * <p>The input array in wrapped; that is, modifications to the array will cause the provided data
   * to be modified.
   *
   * @param data the data
   * @throws DataException If the array is length zero
   */
  public DoubleArrayValueProvider(double[] data) {
    if (data.length == 0) {
      throw new DataException("No data");
    }
    // Documented to wrap the reference directly
    this.data = data;
  }

  @Override
  public int getLength() {
    return data.length;
  }

  @Override
  public double get(int x) {
    return data[x];
  }

  @Override
  public void get(int x, double[] values) {
    values[0] = data[x - 1];
    values[1] = data[x];
    values[2] = data[x + 1];
  }

  @Override
  public double[] toArray() {
    // Documented to wrap the reference directly
    return data;
  }
}
