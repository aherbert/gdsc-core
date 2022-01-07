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

package uk.ac.sussex.gdsc.core.math;

import org.apache.commons.lang3.ArrayUtils;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

/**
 * Calculate the mean and variance of 2D arrayed data using a rolling algorithm.
 *
 * <p>Note: For a single moment use {@link SumOfSquaredDeviations}.
 */
public final class RollingArrayMoment implements ArrayMoment {
  private long size;

  /** First moment of values that have been added. */
  private double[] m1;

  /** Second moment of values that have been added. */
  private double[] m2;

  /**
   * Instantiates a new array moment with data.
   */
  public RollingArrayMoment() {
    // Do nothing
  }

  @Override
  public void add(double[] data) {
    if (size == 0) {
      size = 1;
      // Initialise the first moment to the input value
      m1 = data.clone();
      // Initialise sum-of-squared differences to zero
      m2 = new double[data.length];
    } else {
      final double nMinus1 = size;
      size++;
      final double n0 = size;
      for (int i = 0; i < data.length; i++) {
        final double dev = data[i] - m1[i];
        final double nDev = dev / n0;
        m1[i] += nDev;
        m2[i] += nMinus1 * dev * nDev;
      }
    }
  }

  @Override
  public void add(float[] data) {
    if (size == 0) {
      size = 1;
      // Initialise the first moment to the input value
      m1 = new double[data.length];
      for (int i = 0; i < data.length; i++) {
        m1[i] = data[i];
      }
      // Initialise sum-of-squared differences to zero
      m2 = new double[data.length];
    } else {
      final double nMinus1 = size;
      size++;
      final double n0 = size;
      for (int i = 0; i < data.length; i++) {
        final double dev = data[i] - m1[i];
        final double nDev = dev / n0;
        m1[i] += nDev;
        m2[i] += nMinus1 * dev * nDev;
      }
    }
  }

  @Override
  public void add(int[] data) {
    if (size == 0) {
      size = 1;
      // Initialise the first moment to the input value
      m1 = new double[data.length];
      for (int i = 0; i < data.length; i++) {
        m1[i] = data[i];
      }
      // Initialise sum-of-squared differences to zero
      m2 = new double[data.length];
    } else {
      final double nMinus1 = size;
      size++;
      final double n0 = size;
      for (int i = 0; i < data.length; i++) {
        final double dev = data[i] - m1[i];
        final double nDev = dev / n0;
        m1[i] += nDev;
        m2[i] += nMinus1 * dev * nDev;
      }
    }
  }

  @Override
  public void add(short[] data) {
    if (size == 0) {
      size = 1;
      // Initialise the first moment to the input value
      m1 = new double[data.length];
      for (int i = 0; i < data.length; i++) {
        m1[i] = data[i];
      }
      // Initialise sum-of-squared differences to zero
      m2 = new double[data.length];
    } else {
      final double nMinus1 = size;
      size++;
      final double n0 = size;
      for (int i = 0; i < data.length; i++) {
        final double dev = data[i] - m1[i];
        final double nDev = dev / n0;
        m1[i] += nDev;
        m2[i] += nMinus1 * dev * nDev;
      }
    }
  }

  @Override
  public void add(byte[] data) {
    if (size == 0) {
      size = 1;
      // Initialise the first moment to the input value
      m1 = new double[data.length];
      for (int i = 0; i < data.length; i++) {
        m1[i] = data[i];
      }
      // Initialise sum-of-squared differences to zero
      m2 = new double[data.length];
    } else {
      final double nMinus1 = size;
      size++;
      final double n0 = size;
      for (int i = 0; i < data.length; i++) {
        final double dev = data[i] - m1[i];
        final double nDev = dev / n0;
        m1[i] += nDev;
        m2[i] += nMinus1 * dev * nDev;
      }
    }
  }

  /**
   * Adds the data in the array moment.
   *
   * @param arrayMoment the array moment
   */
  public void add(RollingArrayMoment arrayMoment) {
    if (arrayMoment.size == 0) {
      return;
    }

    final long nb = arrayMoment.size;
    final double[] m1b = arrayMoment.m1;
    final double[] m2b = arrayMoment.m2;

    if (size == 0) {
      // Copy
      this.size = nb;
      m1 = m1b.clone();
      m2 = m2b.clone();
      return;
    }

    if (m1b.length != m1.length) {
      throw new IllegalArgumentException("Different number of moments");
    }

    // Adapted from
    // org.apache.commons.math3.stat.regression.SimpleRegression.append(SimpleRegression)
    final double f1 = nb / (double) (nb + size);
    final double f2 = size * nb / (double) (nb + size);
    for (int i = 0; i < m1.length; i++) {
      final double dev = m1b[i] - m1[i];
      m1[i] += dev * f1;
      m2[i] += m2b[i] + dev * dev * f2;
    }
    size += nb;
  }

  @Override
  public void add(ArrayMoment arrayMoment) {
    ValidationUtils.checkArgument(arrayMoment instanceof RollingArrayMoment,
        "Not compatible array moment %s", arrayMoment);
    add((RollingArrayMoment) arrayMoment);
  }

  @Override
  public void addUnsigned(short[] data) {
    if (size == 0) {
      size = 1;
      // Initialise the first moment to the input value
      m1 = new double[data.length];
      for (int i = 0; i < data.length; i++) {
        m1[i] = data[i] & 0xffff;
      }
      // Initialise sum-of-squared differences to zero
      m2 = new double[data.length];
    } else {
      final double nMinus1 = size;
      size++;
      final double n0 = size;
      for (int i = 0; i < data.length; i++) {
        final double dev = (data[i] & 0xffff) - m1[i];
        final double nDev = dev / n0;
        m1[i] += nDev;
        m2[i] += nMinus1 * dev * nDev;
      }
    }
  }

  @Override
  public void addUnsigned(byte[] data) {
    if (size == 0) {
      size = 1;
      // Initialise the first moment to the input value
      m1 = new double[data.length];
      for (int i = 0; i < data.length; i++) {
        m1[i] = data[i] & 0xff;
      }
      // Initialise sum-of-squared differences to zero
      m2 = new double[data.length];
    } else {
      final double nMinus1 = size;
      size++;
      final double n0 = size;
      for (int i = 0; i < data.length; i++) {
        final double dev = (data[i] & 0xff) - m1[i];
        final double nDev = dev / n0;
        m1[i] += nDev;
        m2[i] += nMinus1 * dev * nDev;
      }
    }
  }

  @Override
  public double[] getMean() {
    return (m1 == null) ? ArrayUtils.EMPTY_DOUBLE_ARRAY : m1.clone();
  }

  @Override
  public double[] getSumOfSquares() {
    return (m2 == null) ? ArrayUtils.EMPTY_DOUBLE_ARRAY : m2.clone();
  }

  @Override
  public long getN() {
    return size;
  }

  @Override
  public double[] getVariance(boolean isBiasCorrected) {
    if (size == 0) {
      return ArrayUtils.EMPTY_DOUBLE_ARRAY;
    }
    if (size == 1) {
      return new double[m2.length];
    }
    final double[] v = m2.clone();
    final double n1 = (isBiasCorrected) ? size - 1 : size;
    for (int i = 0; i < v.length; i++) {
      v[i] = positive(v[i] / n1);
    }
    return v;
  }

  private static double positive(final double value) {
    return (value > 0) ? value : 0;
  }

  @Override
  public double[] getStandardDeviation(boolean isBiasCorrected) {
    if (size == 0) {
      return ArrayUtils.EMPTY_DOUBLE_ARRAY;
    }
    if (size == 1) {
      return new double[m2.length];
    }
    final double[] v = m2.clone();
    final double n1 = (isBiasCorrected) ? size - 1 : size;
    for (int i = 0; i < v.length; i++) {
      v[i] = positiveSqrt(v[i] / n1);
    }
    return v;
  }

  private static double positiveSqrt(final double value) {
    return (value > 0) ? Math.sqrt(value) : 0;
  }

  @Override
  public RollingArrayMoment newInstance() {
    return new RollingArrayMoment();
  }
}
