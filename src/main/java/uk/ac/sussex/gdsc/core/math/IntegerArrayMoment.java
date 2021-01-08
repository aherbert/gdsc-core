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

import java.math.BigInteger;
import org.apache.commons.lang3.ArrayUtils;
import uk.ac.sussex.gdsc.core.data.IntegerType;
import uk.ac.sussex.gdsc.core.data.NotImplementedException;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

/**
 * Calculate the mean and variance of 2D arrayed integer data using a fast summation algorithm that
 * tracks the sum of input values and the sum of squared input values. This may not be suitable for
 * a large series of data where the mean is far from zero due to floating point round-off error.
 *
 * <p>Only supports integer data types (byte, short, integer). A NotImplementedException is thrown
 * for floating point data.
 *
 * <p>This class is only suitable if the user is assured that overflow of Long.MAX_VALUE will not
 * occur. The sum accumulates the input value squared. A check can be made for potential overflow if
 * the maximum value and number of inputs is known (see {@link #isValid(IntegerType, int)}).
 */
public final class IntegerArrayMoment implements ArrayMoment {

  /** The max value of a long as a BigInteger. */
  private static final BigInteger BIG_MAX_VALUE = BigInteger.valueOf(Long.MAX_VALUE);

  /** The size. */
  private long size;

  /** The sum of values that have been added. */
  private long[] sum;

  /** The sum of squared values that have been added. */
  private long[] sumSq;

  /**
   * Instantiates a new array moment with data.
   */
  public IntegerArrayMoment() {
    // Do nothing
  }

  @Override
  public void add(double[] data) {
    throw new NotImplementedException();
  }

  @Override
  public void add(float[] data) {
    throw new NotImplementedException();
  }

  @Override
  public void add(int[] data) {
    if (size == 0) {
      // Initialise the array lengths
      sum = new long[data.length];
      sumSq = new long[data.length];
    }
    size++;
    for (int i = 0; i < data.length; i++) {
      sum[i] += data[i];
      sumSq[i] += (long) data[i] * data[i];
    }
  }

  @Override
  public void add(short[] data) {
    if (size == 0) {
      // Initialise the array lengths
      sum = new long[data.length];
      sumSq = new long[data.length];
    }
    size++;
    for (int i = 0; i < data.length; i++) {
      sum[i] += data[i];
      sumSq[i] += (long) data[i] * data[i];
    }
  }

  @Override
  public void add(byte[] data) {
    if (size == 0) {
      // Initialise the array lengths
      sum = new long[data.length];
      sumSq = new long[data.length];
    }
    size++;
    for (int i = 0; i < data.length; i++) {
      sum[i] += data[i];
      sumSq[i] += (long) data[i] * data[i];
    }
  }

  /**
   * Adds the data in the array moment.
   *
   * @param arrayMoment the array moment
   * @throws ArithmeticException if overflow occurs
   */
  public void add(IntegerArrayMoment arrayMoment) {
    if (arrayMoment.size == 0) {
      return;
    }

    final long nb = arrayMoment.size;
    final long[] sb = arrayMoment.sum;
    final long[] ssb = arrayMoment.sumSq;

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
      sum[i] = Math.addExact(sum[i], sb[i]);
      sumSq[i] = Math.addExact(sumSq[i], ssb[i]);
    }
  }

  @Override
  public void add(ArrayMoment arrayMoment) {
    ValidationUtils.checkArgument(arrayMoment instanceof IntegerArrayMoment,
        "Not compatible array moment %s", arrayMoment);
    add((IntegerArrayMoment) arrayMoment);
  }

  @Override
  public void addUnsigned(short[] data) {
    if (size == 0) {
      // Initialise the array lengths
      sum = new long[data.length];
      sumSq = new long[data.length];
    }
    size++;
    for (int i = 0; i < data.length; i++) {
      final long v = data[i] & 0xffff;
      sum[i] += v;
      sumSq[i] += v * v;
    }
  }

  @Override
  public void addUnsigned(byte[] data) {
    if (size == 0) {
      // Initialise the array lengths
      sum = new long[data.length];
      sumSq = new long[data.length];
    }
    size++;
    for (int i = 0; i < data.length; i++) {
      final long v = data[i] & 0xff;
      sum[i] += v;
      sumSq[i] += v * v;
    }
  }

  @Override
  public double[] getMean() {
    if (size == 0) {
      return ArrayUtils.EMPTY_DOUBLE_ARRAY;
    }
    final double[] m1 = new double[sum.length];
    final double n = this.size;
    for (int i = 0; i < sum.length; i++) {
      m1[i] = sum[i] / n;
    }
    return m1;
  }

  @Override
  public double[] getSumOfSquares() {
    if (size == 0) {
      return ArrayUtils.EMPTY_DOUBLE_ARRAY;
    }
    final double[] m2 = new double[sum.length];
    for (int i = 0; i < sum.length; i++) {
      m2[i] = sumSq[i] - ((double) sum[i] * sum[i]) / size;
    }
    return m2;
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
      return new double[sum.length];
    }
    final double[] v = getSumOfSquares();
    final double n1 = (isBiasCorrected) ? size - 1 : size;
    for (int i = 0; i < v.length; i++) {
      v[i] = positive(v[i] / n1);
    }
    return v;
  }

  /**
   * Positive.
   *
   * @param value the value
   * @return the double
   */
  private static double positive(final double value) {
    return (value > 0) ? value : 0;
  }

  @Override
  public double[] getStandardDeviation(boolean isBiasCorrected) {
    if (size == 0) {
      return ArrayUtils.EMPTY_DOUBLE_ARRAY;
    }
    if (size == 1) {
      return new double[sum.length];
    }
    final double[] v = getSumOfSquares();
    final double n1 = (isBiasCorrected) ? size - 1 : size;
    for (int i = 0; i < v.length; i++) {
      v[i] = positiveSqrt(v[i] / n1);
    }
    return v;
  }

  /**
   * Positive sqrt.
   *
   * @param value the value
   * @return the double
   */
  private static double positiveSqrt(final double value) {
    return (value > 0) ? Math.sqrt(value) : 0;
  }

  @Override
  public IntegerArrayMoment newInstance() {
    return new IntegerArrayMoment();
  }

  /**
   * Checks if it is valid to use this class for the expected data size.
   *
   * <p>This class is only suitable if the user is assured that overflow of Long.MAX_VALUE will not
   * occur. The sum accumulates the input value squared. A check can be made for potential overflow
   * if the maximum value and number of inputs is known.
   *
   * <p>This uses {@link BigInteger} to compute the limit.
   *
   * @param integerType the integer type
   * @param size the number of inputs
   * @return true, if is valid
   */
  public static boolean isValid(IntegerType integerType, int size) {
    // Get the largest magnitude.
    final BigInteger mag =
        BigInteger.valueOf((integerType.isSigned()) ? integerType.getMin() : integerType.getMax());

    // Get the total
    final BigInteger total = mag.multiply(mag).multiply(BigInteger.valueOf(size));
    return (total.compareTo(BIG_MAX_VALUE) <= 0);
  }
}
