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

package uk.ac.sussex.gdsc.core.utils;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

/**
 * A test class to compute a correlation.
 */
class SimpleCorrelator {

  /** The x data. */
  private final DoubleArrayList x = new DoubleArrayList();

  /** The y data. */
  private final DoubleArrayList y = new DoubleArrayList();

  /**
   * Adds the values.
   *
   * @param v1 value 1
   * @param v2 value 2
   */
  void add(int v1, int v2) {
    x.add(v1);
    y.add(v2);
  }

  /**
   * Gets the x.
   *
   * @return the x
   */
  int[] getX() {
    return x.doubleStream().mapToInt(v -> (int) v).toArray();
  }

  /**
   * Gets the y.
   *
   * @return the y
   */
  int[] getY() {
    return y.doubleStream().mapToInt(v -> (int) v).toArray();
  }

  /**
   * Gets the sum X.
   *
   * @return the sum X
   */
  long getSumX() {
    return (long) x.doubleStream().sum();
  }

  /**
   * Gets the sum Y.
   *
   * @return the sum Y
   */
  long getSumY() {
    return (long) y.doubleStream().sum();
  }

  /**
   * Gets the number of data points.
   *
   * @return the n
   */
  int getN() {
    return x.size();
  }

  /**
   * Gets the sum of squared X.
   *
   * @return the sum squared X
   */
  long getSumXX() {
    return sumSquare(x);
  }

  /**
   * Gets the sum of squared Y.
   *
   * @return the sum squared Y
   */
  long getSumYY() {
    return sumSquare(y);
  }

  private static long sumSquare(DoubleArrayList data) {
    long sum = 0;
    final double[] e = data.elements();
    for (int i = 0; i < data.size(); i++) {
      final long v = (long) e[i];
      sum += v * v;
    }
    return sum;
  }

  /**
   * Gets the sum of {@code X*Y}.
   *
   * @return the sum XY
   */
  long getSumXY() {
    long sum = 0;
    final double[] ex = x.elements();
    final double[] ey = y.elements();
    for (int i = 0; i < x.size(); i++) {
      final long v1 = (long) ex[i];
      final long v2 = (long) ey[i];
      sum += v1 * v2;
    }
    return sum;
  }

  /**
   * Gets the correlation.
   *
   * @return the correlation
   */
  double getCorrelation() {
    if (x.size() < 2) {
      // Q. Should this return NaN or 0 for size 1? Currently return 0.
      return x.isEmpty() ? Double.NaN : 0.0;
    }
    final PearsonsCorrelation c = new PearsonsCorrelation();
    return c.correlation(x.toDoubleArray(), y.toDoubleArray());
  }
}
