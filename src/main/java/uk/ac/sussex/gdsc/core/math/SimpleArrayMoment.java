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

package uk.ac.sussex.gdsc.core.math;

import org.apache.commons.lang3.ArrayUtils;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

/**
 * Simple class to calculate the mean and variance of arrayed data using a fast summation algorithm
 * that tracks the sum of input values and the sum of squared input values. This may not be suitable
 * for a large series of data where the mean is far from zero due to floating point round-off error.
 */
public final class SimpleArrayMoment implements ArrayMoment {
  private long size;

  /** The sum of values that have been added. */
  private double[] sum;

  /** The sum of squared values that have been added. */
  private double[] sumSq;

  /**
   * Instantiates a new array moment with data.
   */
  public SimpleArrayMoment() {
    // Do nothing
  }

  /**
   * Instantiates a new array moment with data.
   *
   * @param data the data
   */
  public SimpleArrayMoment(double[] data) {
    add(data);
  }

  /**
   * Instantiates a new array moment with data.
   *
   * @param data the data
   */
  public SimpleArrayMoment(float[] data) {
    add(data);
  }

  /**
   * Instantiates a new array moment with data.
   *
   * @param data the data
   */
  public SimpleArrayMoment(int[] data) {
    add(data);
  }

  @Override
  public void add(double data) {
    if (size == 0) {
      // Initialise the array lengths
      sum = new double[1];
      sumSq = new double[1];
    }
    size++;
    sum[0] += data;
    sumSq[0] += data * data;
  }

  @Override
  public void add(double[] data) {
    if (size == 0) {
      // Initialise the array lengths
      sum = new double[data.length];
      sumSq = new double[data.length];
    }
    size++;
    for (int i = 0; i < data.length; i++) {
      sum[i] += data[i];
      sumSq[i] += data[i] * data[i];
    }
  }

  @Override
  public void add(float[] data) {
    if (size == 0) {
      // Initialise the array lengths
      sum = new double[data.length];
      sumSq = new double[data.length];
    }
    size++;
    for (int i = 0; i < data.length; i++) {
      sum[i] += data[i];
      sumSq[i] += (double) data[i] * data[i];
    }
  }

  @Override
  public void add(int[] data) {
    if (size == 0) {
      // Initialise the array lengths
      sum = new double[data.length];
      sumSq = new double[data.length];
    }
    size++;
    for (int i = 0; i < data.length; i++) {
      sum[i] += data[i];
      sumSq[i] += (double) data[i] * data[i];
    }
  }

  @Override
  public void add(short[] data) {
    if (size == 0) {
      // Initialise the array lengths
      sum = new double[data.length];
      sumSq = new double[data.length];
    }
    size++;
    for (int i = 0; i < data.length; i++) {
      sum[i] += data[i];
      sumSq[i] += (double) data[i] * data[i];
    }
  }

  @Override
  public void add(byte[] data) {
    if (size == 0) {
      // Initialise the array lengths
      sum = new double[data.length];
      sumSq = new double[data.length];
    }
    size++;
    for (int i = 0; i < data.length; i++) {
      sum[i] += data[i];
      sumSq[i] += (double) data[i] * data[i];
    }
  }

  /**
   * Adds the data in the array moment.
   *
   * @param arrayMoment the array moment
   */
  public void add(SimpleArrayMoment arrayMoment) {
    if (arrayMoment.size == 0) {
      return;
    }

    final long nb = arrayMoment.size;
    final double[] sb = arrayMoment.sum;
    final double[] ssb = arrayMoment.sumSq;

    if (size == 0) {
      // Copy
      size = nb;
      sum = sb.clone();
      sumSq = ssb.clone();
      return;
    }

    if (sb.length != sum.length) {
      throw new IllegalArgumentException(
          "Different number of moments: " + sb.length + " != " + sum.length);
    }

    size += nb;
    for (int i = 0; i < sum.length; i++) {
      sum[i] += sb[i];
      sumSq[i] += ssb[i];
    }
  }

  @Override
  public void add(ArrayMoment arrayMoment) {
    ValidationUtils.checkArgument(arrayMoment instanceof SimpleArrayMoment,
        "Not compatible array moment %s", arrayMoment);
    add((SimpleArrayMoment) arrayMoment);
  }

  @Override
  public void addUnsigned(short[] data) {
    if (size == 0) {
      // Initialise the array lengths
      sum = new double[data.length];
      sumSq = new double[data.length];
    }
    size++;
    for (int i = 0; i < data.length; i++) {
      final double v = data[i] & 0xffff;
      sum[i] += v;
      sumSq[i] += v * v;
    }
  }

  @Override
  public void addUnsigned(byte[] data) {
    if (size == 0) {
      // Initialise the array lengths
      sum = new double[data.length];
      sumSq = new double[data.length];
    }
    size++;
    for (int i = 0; i < data.length; i++) {
      final double v = data[i] & 0xff;
      sum[i] += v;
      sumSq[i] += v * v;
    }
  }

  @Override
  public double[] getFirstMoment() {
    if (size == 0) {
      return ArrayUtils.EMPTY_DOUBLE_ARRAY;
    }
    final double[] m1 = sum.clone();
    final double n = this.size;
    for (int i = 0; i < sum.length; i++) {
      m1[i] /= n;
    }
    return m1;
  }

  @Override
  public double[] getSecondMoment() {
    if (size == 0) {
      return ArrayUtils.EMPTY_DOUBLE_ARRAY;
    }
    final double[] m2 = new double[sum.length];
    final double n = this.size;
    for (int i = 0; i < sum.length; i++) {
      m2[i] = sumSq[i] - (sum[i] * sum[i]) / n;
    }
    return m2;
  }

  @Override
  public long getN() {
    return size;
  }

  @Override
  public double[] getVariance() {
    return getVariance(true);
  }

  @Override
  public double[] getVariance(boolean isBiasCorrected) {
    if (size == 0) {
      return ArrayUtils.EMPTY_DOUBLE_ARRAY;
    }
    if (size == 1) {
      return new double[sum.length];
    }
    final double[] v = getSecondMoment();
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
  public double[] getStandardDeviation() {
    return getStandardDeviation(true);
  }

  @Override
  public double[] getStandardDeviation(boolean isBiasCorrected) {
    if (size == 0) {
      return ArrayUtils.EMPTY_DOUBLE_ARRAY;
    }
    if (size == 1) {
      return new double[sum.length];
    }
    final double[] v = getSecondMoment();
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
  public SimpleArrayMoment newInstance() {
    return new SimpleArrayMoment();
  }
}
