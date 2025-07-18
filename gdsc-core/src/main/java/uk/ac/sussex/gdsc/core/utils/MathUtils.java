/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2025 Alex Herbert
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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Contains Math utilities.
 */
public final class MathUtils {
  /**
   * The limit on x above which the erf(x) returns 1.
   */
  private static final double ERF_LIMIT = 6.183574750897915;

  /**
   * 1 / log(2).
   */
  private static final double ONE_BY_LOG2 = 1.0 / Math.log(2.0);

  /**
   * No public construction.
   */
  private MathUtils() {}

  /**
   * Compute the min and max of the data.
   *
   * @param data the data
   * @return [min, max]
   */
  public static double[] limits(double... data) {
    if (data == null || data.length == 0) {
      return noDoubleLimits();
    }
    return limits(null, data);
  }

  /**
   * Compute the min and max of the data.
   *
   * @param data the data
   * @return [min, max]
   */
  public static float[] limits(float... data) {
    if (data == null || data.length == 0) {
      return noFloatLimits();
    }
    return limits(null, data);
  }

  /**
   * Compute the min and max of the data.
   *
   * @param data the data
   * @return [min, max]
   */
  public static int[] limits(int... data) {
    if (data == null || data.length == 0) {
      return noIntegerLimits();
    }
    return limits(null, data);
  }

  /**
   * Compute the min and max of the data.
   *
   * @param data the data
   * @return [min, max]
   */
  public static long[] limits(long... data) {
    if (data == null || data.length == 0) {
      return noLongLimits();
    }
    return limits(null, data);
  }

  /**
   * Compute the min and max of the data.
   *
   * @param data the data
   * @return [min, max]
   */
  public static short[] limits(short... data) {
    if (data == null || data.length == 0) {
      return noShortLimits();
    }
    return limits(null, data);
  }

  /**
   * Compute the min and max of the data.
   *
   * @param limits The current [min, max]
   * @param data the data
   * @return [min, max]
   */
  public static double[] limits(double[] limits, double... data) {
    if (data == null || data.length == 0) {
      return (limits == null || limits.length < 2) ? noDoubleLimits() : limits;
    }
    final double[] result =
        (limits == null || limits.length < 2) ? new double[] {data[0], data[0]} : limits;
    double min = result[0];
    double max = result[1];
    if (min > max) {
      final double tmp = min;
      min = max;
      max = tmp;
    }
    for (final double d : data) {
      if (min > d) {
        min = d;
      } else if (max < d) {
        max = d;
      }
    }
    result[0] = min;
    result[1] = max;
    return result;
  }

  /**
   * Compute the min and max of the data.
   *
   * @param limits The current [min, max]
   * @param data the data
   * @return [min, max]
   */
  public static float[] limits(float[] limits, float... data) {
    if (data == null || data.length == 0) {
      return (limits == null || limits.length < 2) ? noFloatLimits() : limits;
    }
    final float[] result =
        (limits == null || limits.length < 2) ? new float[] {data[0], data[0]} : limits;
    float min = result[0];
    float max = result[1];
    if (min > max) {
      final float tmp = min;
      min = max;
      max = tmp;
    }
    for (final float d : data) {
      if (min > d) {
        min = d;
      } else if (max < d) {
        max = d;
      }
    }
    result[0] = min;
    result[1] = max;
    return result;
  }

  /**
   * Compute the min and max of the data.
   *
   * @param limits The current [min, max]
   * @param data the data
   * @return [min, max]
   */
  public static int[] limits(int[] limits, int... data) {
    if (data == null || data.length == 0) {
      return (limits == null || limits.length < 2) ? noIntegerLimits() : limits;
    }
    final int[] result =
        (limits == null || limits.length < 2) ? new int[] {data[0], data[0]} : limits;
    int min = result[0];
    int max = result[1];
    if (min > max) {
      final int tmp = min;
      min = max;
      max = tmp;
    }
    for (final int d : data) {
      if (min > d) {
        min = d;
      } else if (max < d) {
        max = d;
      }
    }
    result[0] = min;
    result[1] = max;
    return result;
  }

  /**
   * Compute the min and max of the data.
   *
   * @param limits The current [min, max]
   * @param data the data
   * @return [min, max]
   */
  public static long[] limits(long[] limits, long... data) {
    if (data == null || data.length == 0) {
      return (limits == null || limits.length < 2) ? noLongLimits() : limits;
    }
    final long[] result =
        (limits == null || limits.length < 2) ? new long[] {data[0], data[0]} : limits;
    long min = result[0];
    long max = result[1];
    if (min > max) {
      final long tmp = min;
      min = max;
      max = tmp;
    }
    for (final long d : data) {
      if (min > d) {
        min = d;
      } else if (max < d) {
        max = d;
      }
    }
    result[0] = min;
    result[1] = max;
    return result;
  }

  /**
   * Compute the min and max of the data.
   *
   * @param limits The current [min, max]
   * @param data the data
   * @return [min, max]
   */
  public static short[] limits(short[] limits, short... data) {
    if (data == null || data.length == 0) {
      return (limits == null || limits.length < 2) ? noShortLimits() : limits;
    }
    final short[] result =
        (limits == null || limits.length < 2) ? new short[] {data[0], data[0]} : limits;
    short min = result[0];
    short max = result[1];
    if (min > max) {
      final short tmp = min;
      min = max;
      max = tmp;
    }
    for (final short d : data) {
      if (min > d) {
        min = d;
      } else if (max < d) {
        max = d;
      }
    }
    result[0] = min;
    result[1] = max;
    return result;
  }

  /**
   * Get the max.
   *
   * @param data the data
   * @return the max
   */
  public static double max(double... data) {
    if (data == null || data.length == 0) {
      return Double.NaN;
    }
    return maxDefault(Double.NEGATIVE_INFINITY, data);
  }

  /**
   * Get the max.
   *
   * @param data the data
   * @return the max
   */
  public static float max(float... data) {
    if (data == null || data.length == 0) {
      return Float.NaN;
    }
    return maxDefault(Float.NEGATIVE_INFINITY, data);
  }

  /**
   * Get the max.
   *
   * @param data the data
   * @return the max
   */
  public static int max(int... data) {
    return maxDefault(Integer.MIN_VALUE, data);
  }

  /**
   * Get the max.
   *
   * @param data the data
   * @return the max
   */
  public static long max(long... data) {
    return maxDefault(Long.MIN_VALUE, data);
  }

  /**
   * Get the max.
   *
   * @param data the data
   * @return the max
   */
  public static short max(short... data) {
    return maxDefault(Short.MIN_VALUE, data);
  }

  /**
   * Get the result using a default value.
   *
   * @param value the value
   * @param data the data
   * @return the result
   */
  public static double maxDefault(double value, double... data) {
    if (data == null || data.length == 0) {
      return value;
    }
    double result = value;
    for (final double d : data) {
      if (result < d) {
        result = d;
      }
    }
    return result;
  }

  /**
   * Get the result using a default value.
   *
   * @param value the value
   * @param data the data
   * @return the result
   */
  public static float maxDefault(float value, float... data) {
    if (data == null || data.length == 0) {
      return value;
    }
    float result = value;
    for (final float d : data) {
      if (result < d) {
        result = d;
      }
    }
    return result;
  }

  /**
   * Get the result using a default value.
   *
   * @param value the value
   * @param data the data
   * @return the result
   */
  public static int maxDefault(int value, int... data) {
    if (data == null || data.length == 0) {
      return value;
    }
    int result = value;
    for (final int d : data) {
      if (result < d) {
        result = d;
      }
    }
    return result;
  }

  /**
   * Get the result using a default value.
   *
   * @param value the value
   * @param data the data
   * @return the result
   */
  public static long maxDefault(long value, long... data) {
    if (data == null || data.length == 0) {
      return value;
    }
    long result = value;
    for (final long d : data) {
      if (result < d) {
        result = d;
      }
    }
    return result;
  }

  /**
   * Get the result using a default value.
   *
   * @param value the value
   * @param data the data
   * @return the result
   */
  public static short maxDefault(short value, short... data) {
    if (data == null || data.length == 0) {
      return value;
    }
    short result = value;
    for (final short d : data) {
      if (result < d) {
        result = d;
      }
    }
    return result;
  }

  /**
   * Get the min.
   *
   * @param data the data
   * @return the min
   */
  public static double min(double... data) {
    if (data == null || data.length == 0) {
      return Double.NaN;
    }
    return minDefault(Double.POSITIVE_INFINITY, data);
  }

  /**
   * Get the min.
   *
   * @param data the data
   * @return the min
   */
  public static float min(float... data) {
    if (data == null || data.length == 0) {
      return Float.NaN;
    }
    return minDefault(Float.POSITIVE_INFINITY, data);
  }

  /**
   * Get the min.
   *
   * @param data the data
   * @return the min
   */
  public static int min(int... data) {
    return minDefault(Integer.MAX_VALUE, data);
  }

  /**
   * Get the min.
   *
   * @param data the data
   * @return the min
   */
  public static long min(long... data) {
    return minDefault(Long.MAX_VALUE, data);
  }

  /**
   * Get the min.
   *
   * @param data the data
   * @return the min
   */
  public static short min(short... data) {
    return minDefault(Short.MAX_VALUE, data);
  }

  /**
   * Get the result using a default value.
   *
   * @param value the value
   * @param data the data
   * @return the result
   */
  public static double minDefault(double value, double... data) {
    if (data == null || data.length == 0) {
      return value;
    }
    double result = value;
    for (final double d : data) {
      if (result > d) {
        result = d;
      }
    }
    return result;
  }

  /**
   * Get the result using a default value.
   *
   * @param value the value
   * @param data the data
   * @return the result
   */
  public static float minDefault(float value, float... data) {
    if (data == null || data.length == 0) {
      return value;
    }
    float result = value;
    for (final float d : data) {
      if (result > d) {
        result = d;
      }
    }
    return result;
  }

  /**
   * Get the result using a default value.
   *
   * @param value the value
   * @param data the data
   * @return the result
   */
  public static int minDefault(int value, int... data) {
    if (data == null || data.length == 0) {
      return value;
    }
    int result = value;
    for (final int d : data) {
      if (result > d) {
        result = d;
      }
    }
    return result;
  }

  /**
   * Get the result using a default value.
   *
   * @param value the value
   * @param data the data
   * @return the result
   */
  public static long minDefault(long value, long... data) {
    if (data == null || data.length == 0) {
      return value;
    }
    long result = value;
    for (final long d : data) {
      if (result > d) {
        result = d;
      }
    }
    return result;
  }

  /**
   * Get the result using a default value.
   *
   * @param value the value
   * @param data the data
   * @return the result
   */
  public static short minDefault(short value, short... data) {
    if (data == null || data.length == 0) {
      return value;
    }
    short result = value;
    for (final short d : data) {
      if (result > d) {
        result = d;
      }
    }
    return result;
  }

  private static double[] noDoubleLimits() {
    return new double[] {Double.NaN, Double.NaN};
  }

  private static float[] noFloatLimits() {
    return new float[] {Float.NaN, Float.NaN};
  }

  private static int[] noIntegerLimits() {
    return new int[] {0, 0};
  }

  private static long[] noLongLimits() {
    return new long[] {0, 0};
  }

  private static short[] noShortLimits() {
    return new short[] {0, 0};
  }

  /**
   * Calculate a cumulative histogram of the input values. The data is sorted and the first value in
   * the returned values array will be the lowest value. NaN are ignored.
   *
   * <p>If the values are {@code null} this returns two zero length arrays.
   *
   * @param values the values
   * @param normalise Normalise so the total is 1
   * @return Histogram values and cumulative total
   */
  public static double[][] cumulativeHistogram(double[] values, boolean normalise) {
    if (values == null || values.length == 0) {
      return new double[2][0];
    }

    double[] data = values.clone();
    Arrays.sort(data);

    // Arrays.sort() put the NaN values higher than all others.
    // If this is the first value then stop.
    if (Double.isNaN(data[0])) {
      return new double[2][0];
    }

    double[] sum = new double[data.length];
    double lastValue = data[0];
    int position = 0;
    int count = 0;
    for (final double value : data) {
      // Arrays.sort() put the NaN values higher than all others so this should occur at the end
      if (Double.isNaN(value)) {
        break;
      }

      // When a new value is reached, store the cumulative total for the previous value
      if (lastValue != value) {
        data[position] = lastValue;
        sum[position] = count;
        lastValue = value;
        position++;
      }
      count++;
    }

    // Record the final value
    data[position] = lastValue;
    sum[position] = count;
    position++;

    // Truncate if necessary
    if (position < data.length) {
      data = Arrays.copyOf(data, position);
      sum = Arrays.copyOf(sum, position);
    }

    // Normalise. Count is always positive as zero length arrays, or all NaN are fast exit.
    if (normalise) {
      for (int i = 0; i < sum.length; i++) {
        sum[i] /= count;
      }
    }

    return new double[][] {data, sum};
  }

  /**
   * Calculate a weighted cumulative histogram of the input values. The data is sorted and the first
   * value in the returned values array will be the lowest value. NaN are ignored.
   *
   * <p>If the weights are {@code null} this returns the result of
   * {@link #cumulativeHistogram(double[], boolean)}.
   *
   * @param values the values
   * @param weights the weights
   * @param normalise Normalise so the total is 1
   * @return Histogram values and cumulative total
   * @throws IllegalArgumentException if the non-null values and weights are different lengths; if
   *         any weight is below zero; or if the sum of weights is not finite and strictly positive.
   */
  public static double[][] cumulativeHistogram(double[] values, double[] weights,
      boolean normalise) {
    if (weights == null) {
      return cumulativeHistogram(values, normalise);
    }
    if (values == null || values.length == 0) {
      return new double[2][0];
    }
    if (values.length != weights.length) {
      throw new IllegalArgumentException(String
          .format("values and weights length mismatch: %d != %d", values.length, weights.length));
    }
    // Sort by value
    double total = 0;
    double[][] weightedValues = new double[values.length][2];
    for (int i = 0; i < values.length; i++) {
      if (weights[i] < 0) {
        throw new IllegalArgumentException("invalid weight: " + weights[i]);
      }
      weightedValues[i][0] = values[i];
      weightedValues[i][1] = weights[i];
      total += weights[i];
    }
    if (!Double.isFinite(total)) {
      throw new IllegalArgumentException("invalid total weight: " + total);
    }

    Arrays.sort(weightedValues, Comparator.comparingDouble(k -> k[0]));

    // Arrays.sort() put the NaN values higher than all others.
    // If this is the first value then stop.
    if (Double.isNaN(weightedValues[0][0])) {
      return new double[2][0];
    }

    double[] data = new double[values.length];
    double[] sum = new double[values.length];
    double lastValue = weightedValues[0][0];
    int position = 0;
    total = 0;
    for (int i = 0; i < data.length; i++) {
      final double value = weightedValues[i][0];
      // Arrays.sort() put the NaN values higher than all others so this should occur at the end
      if (Double.isNaN(value)) {
        break;
      }
      final double weight = weightedValues[i][1];

      // When a new value is reached, store the cumulative total for the previous value
      if (lastValue != value) {
        data[position] = lastValue;
        sum[position] = total;
        lastValue = value;
        position++;
      }
      total += weight;
    }

    if (total <= 0) {
      throw new IllegalArgumentException("invalid total weight: " + total);
    }

    // Record the final value
    data[position] = lastValue;
    sum[position] = total;
    position++;

    // Truncate if necessary
    if (position < data.length) {
      data = Arrays.copyOf(data, position);
      sum = Arrays.copyOf(sum, position);
    }

    // Normalise. Total is always positive as zero length arrays, or all NaN are fast exit.
    if (normalise) {
      for (int i = 0; i < sum.length; i++) {
        sum[i] /= total;
      }
    }

    return new double[][] {data, sum};
  }

  /**
   * Gets the log likelihood for a least squares estimate. This assumes that the residuals are
   * distributed according to independent identical normal distributions (with zero mean).
   *
   * <pre>
   * ln(L) = - n ln(2pi) / 2 - n ln(rss/n) / 2 - n / 2
   * </pre>
   *
   * <p>For example this assumption is approximately the case for weighted least squares fitting of
   * data.
   *
   * @param sumOfSquaredResiduals the sum of squared residuals from the weighted least squares fit
   * @param numberOfPoints The number of data points
   * @return the log likelihood
   * @see <a
   *      href="https://en.wikipedia.org/wiki/Akaike_information_criterion#Comparison_with_least_squares">Wikipedia:
   *      AIC comparison with least squares</a>
   */
  public static double getLogLikelihood(double sumOfSquaredResiduals, int numberOfPoints) {
    // logLikelihood = 0.5 * (-numberOfPoints * log(2 * PI) - numberOfPoints *
    // log(sumOfSquaredResiduals/numberOfPoints) - numberOfPoints)
    // logLikelihood = 0.5 * (-numberOfPoints * (log(2 * PI) +
    // log(sumOfSquaredResiduals/numberOfPoints) + 1))
    // log(2 * PI) = 1.837877066
    return 0.5 * (-numberOfPoints
        * (1.837877066 + Math.log(sumOfSquaredResiduals / numberOfPoints) + 1.0));
  }

  /**
   * Get the Akaike Information Criterion (AICc).
   *
   * <pre>
   * AIC = 2k - 2 ln(L)
   * </pre>
   *
   * @param logLikelihood the log-likelihood of the fit (from Maximum likelihood estimation; ln(L))
   * @param numberOfParameters The number of fitted parameters (k)
   * @return The Akaike Information Criterion
   * @see <a href="http://en.wikipedia.org/wiki/Akaike_information_criterion">Wikipedia: Akaike
   *      information criterion</a>
   */
  public static double getAkaikeInformationCriterion(double logLikelihood, int numberOfParameters) {
    // aic = 2.0 * numberOfParameters - 2.0 * logLikelihood
    return 2.0 * (numberOfParameters - logLikelihood);
  }

  /**
   * Get the corrected Akaike Information Criterion (AICc).
   *
   * <pre>
   * AICc = 2k - 2 ln(L) + (2k^2 + 2k) / (n - k - 1)
   * </pre>
   *
   * <p>The correction is assuming that the model is univariate, is linear in its parameters, and
   * has normally-distributed residuals.
   *
   * @param logLikelihood the log-likelihood of the fit (from Maximum likelihood estimation; ln(L))
   * @param numberOfPoints The number of data points (n)
   * @param numberOfParameters The number of fitted parameters (k)
   * @return The corrected Akaike Information Criterion
   * @see <a
   *      href="https://en.wikipedia.org/wiki/Akaike_information_criterion#Modification_for_small_sample_size">
   *      Wikipedia: Corrected Akaike information criterion (AICc)</a>
   */
  public static double getAkaikeInformationCriterion(double logLikelihood, int numberOfPoints,
      int numberOfParameters) {
    // Note: The true bias corrected AIC is derived from the 2nd, 3rd and 4th derivatives of the
    // negative log-likelihood function. This is complex and so is not implemented.
    // See:
    // http://www.math.sci.hiroshima-u.ac.jp/stat/TR/TR11/TR11-06.pdf
    // http://www.sciencedirect.com/science/article/pii/S0024379512000821#

    // This paper explains that the AIC or BIC are much better than the Adjusted coefficient of
    // determination for model selection:
    // http://www.ncbi.nlm.nih.gov/pmc/articles/PMC2892436/

    // aic = 2.0 * numberOfParameters - 2.0 * logLikelihood

    // The Bias Corrected Akaike Information Criterion (AICc)
    // http://en.wikipedia.org/wiki/Akaike_information_criterion#AICc
    // Assumes a univariate linear model.
    // aic = aic + (2.0 * numberOfParameters * (numberOfParameters + 1)) / (numberOfPoints -
    // numberOfParameters - 1)

    // Optimised
    return 2.0 * (numberOfParameters - logLikelihood)
        + (2.0 * numberOfParameters * (numberOfParameters + 1))
            / (numberOfPoints - numberOfParameters - 1);
  }

  /**
   * Get the Bayesian Information Criterion (BIC).
   *
   * <pre>
   * BIC = k ln(n) - 2 ln(L)
   * </pre>
   *
   * @param logLikelihood the log-likelihood of the fit (from Maximum likelihood estimation; ln(L))
   * @param numberOfPoints The number of data points (n)
   * @param numberOfParameters The number of fitted parameters (k)
   * @return The Bayesian Information Criterion
   * @see <a href="http://en.wikipedia.org/wiki/Bayesian_information_criterion">Wikipedia: Bayesian
   *      information criterion</a>
   */
  public static double getBayesianInformationCriterion(double logLikelihood, int numberOfPoints,
      int numberOfParameters) {
    // Bayesian Information Criterion (BIC), which gives a higher penalty on the number of
    // parameters
    // http://en.wikipedia.org/wiki/Bayesian_information_criterion
    return numberOfParameters * Math.log(numberOfPoints) - 2.0 * logLikelihood;
  }

  /**
   * Gets the adjusted coefficient of determination.
   *
   * <pre>
   * Adjusted r^2 = 1 - rss/tss * (n-1) / (n-k-1)
   * </pre>
   *
   * @param residualSumSquares The sum of squared residuals from the model (rss)
   * @param totalSumSquares the sum of the squared differences from the mean of the dependent
   *        variable (total sum of squares; tss)
   * @param numberOfPoints The number of data points (n)
   * @param numberOfParameters The number of fitted parameters (k)
   * @return The adjusted coefficient of determination
   * @see <a
   *      href="https://en.wikipedia.org/wiki/Coefficient_of_determination#Adjusted_R2">Wikipedia:
   *      Adjusted r^2</a>
   */
  public static double getAdjustedCoefficientOfDetermination(double residualSumSquares,
      double totalSumSquares, int numberOfPoints, int numberOfParameters) {
    return 1 - (residualSumSquares / totalSumSquares)
        * ((double) (numberOfPoints - 1) / (numberOfPoints - numberOfParameters - 1));
  }

  /**
   * Gets the total sum of squares.
   *
   * <pre>
   * tss = sum(x ^ 2) - sum(x) ^ 2 / n
   * </pre>
   *
   * @param values the values
   * @return the total sum of squares
   */
  public static double getTotalSumOfSquares(double[] values) {
    double sx = 0;
    double ssx = 0;
    for (int i = values.length; i-- > 0;) {
      sx += values[i];
      ssx += values[i] * values[i];
    }
    return ssx - (sx * sx) / (values.length);
  }

  /**
   * Get the sum.
   *
   * @param data the data
   * @return the sum
   */
  public static double sum(double... data) {
    if (data == null) {
      return 0;
    }
    double sum = 0;
    for (final double d : data) {
      sum += d;
    }
    return sum;
  }

  /**
   * Get the sum.
   *
   * @param data the data
   * @return the sum
   */
  public static double sum(float... data) {
    if (data == null) {
      return 0;
    }
    double sum = 0;
    for (final float d : data) {
      sum += d;
    }
    return sum;
  }

  /**
   * Get the sum.
   *
   * @param data the data
   * @return the sum
   */
  public static long sum(long... data) {
    if (data == null) {
      return 0;
    }
    long sum = 0;
    for (final long d : data) {
      sum += d;
    }
    return sum;
  }

  /**
   * Get the sum.
   *
   * @param data the data
   * @return the sum
   */
  public static long sum(int... data) {
    if (data == null) {
      return 0;
    }
    long sum = 0;
    for (final int d : data) {
      sum += d;
    }
    return sum;
  }

  /**
   * Round the double to the specified significant digits. Non-finite values are unchanged.
   *
   * <p>This method is intended to round the decimal String representation of the input
   * {@code double}. To perform rounding of the exact double value use
   * {@link java.math.BigDecimal#BigDecimal(double) new BigDecimal(value)} to construct a BigDecimal
   * for rounding.
   *
   * @param value The double
   * @param significantDigits The number of significant digits
   * @return A string containing the rounded double
   */
  public static String rounded(double value, int significantDigits) {
    if (!Double.isFinite(value)) {
      return Double.toString(value);
    }
    // Do not use the BigDecimal.toString. It leaves trailing zeros when the
    // value is not exactly representable as a double. Since this is formatting
    // doubles it makes more sense to convert back to the closest representable double.
    return Double.toString(roundToBigDecimal(value, significantDigits).doubleValue());
  }

  /**
   * Round the double to 4 significant digits. Non-finite values are unchanged.
   *
   * @param value The double
   * @return A string containing the rounded double
   * @see #rounded(double, int)
   */
  public static String rounded(double value) {
    return rounded(value, 4);
  }

  /**
   * Round the double to the specified significant digits. Non-finite values are unchanged.
   *
   * <p>This method is intended to round the decimal String representation of the input
   * {@code double}. To perform rounding of the exact double value use
   * {@link java.math.BigDecimal#BigDecimal(double) new BigDecimal(value)} to construct a BigDecimal
   * for rounding.
   *
   * @param value The double
   * @param significantDigits The number of significant digits
   * @return The rounded double
   */
  public static double round(double value, int significantDigits) {
    if (!Double.isFinite(value)) {
      return value;
    }
    return roundToBigDecimal(value, significantDigits).doubleValue();
  }

  /**
   * Round the double to 4 significant digits. Non-finite values are unchanged.
   *
   * @param value The double
   * @return The rounded double
   * @see #round(double, int)
   */
  public static double round(double value) {
    return round(value, 4);
  }

  /**
   * Round to the nearest factor. Non-finite values are unchanged.
   *
   * @param value the value
   * @param factor the factor
   * @return the rounded value
   */
  public static double round(double value, double factor) {
    if (!Double.isFinite(value)) {
      return value;
    }
    return Math.round(value / factor) * factor;
  }

  /**
   * Round the double to the specified significant digits. Non-finite values are unsupported.
   *
   * <p>This method is intended to round the decimal String representation of the input
   * {@code double}. To perform rounding of the exact double value use
   * {@link java.math.BigDecimal#BigDecimal(double) new BigDecimal(value)} to construct a BigDecimal
   * for rounding.
   *
   * @param value The double
   * @param significantDigits The number of significant digits
   * @return The rounded value
   * @throws NumberFormatException If the value is non-finite
   */
  public static BigDecimal roundToBigDecimal(double value, int significantDigits) {
    return BigDecimal.valueOf(value).round(new MathContext(significantDigits));
  }

  /**
   * Round the double to the specified decimal places.
   *
   * <p>This method is intended to round the decimal String representation of the input
   * {@code double}. To perform rounding of the exact double value use
   * {@link java.math.BigDecimal#BigDecimal(double) new BigDecimal(value)} to construct a BigDecimal
   * for rounding.
   *
   * @param value The double
   * @param decimalPlaces the decimal places (can be negative)
   * @return The rounded double
   */
  public static double roundUsingDecimalPlaces(double value, int decimalPlaces) {
    if (Double.isInfinite(value) || Double.isNaN(value)) {
      return value;
    }
    final BigDecimal bd = BigDecimal.valueOf(value);
    if (decimalPlaces > bd.scale()) {
      return value;
    }
    return bd.setScale(decimalPlaces, RoundingMode.HALF_UP).doubleValue();
  }

  /**
   * Round the double to the specified decimal places.
   *
   * <p>This method is intended to round the decimal String representation of the input
   * {@code double}. To perform rounding of the exact double value use
   * {@link java.math.BigDecimal#BigDecimal(double) new BigDecimal(value)} to construct a BigDecimal
   * for rounding.
   *
   * @param value The double
   * @param decimalPlaces the decimal places
   * @return The rounded value
   * @throws NumberFormatException If the value is non-finite
   */
  public static BigDecimal roundUsingDecimalPlacesToBigDecimal(double value, int decimalPlaces) {
    final BigDecimal bd = BigDecimal.valueOf(value);
    if (decimalPlaces > bd.scale()) {
      return bd;
    }
    return bd.setScale(decimalPlaces, RoundingMode.HALF_UP);
  }

  /**
   * Round down to the nearest factor.
   *
   * @param value the value
   * @param factor the factor
   * @return the rounded value
   */
  public static double floor(double value, double factor) {
    if (!Double.isFinite(value)) {
      return value;
    }
    return Math.floor(value / factor) * factor;
  }

  /**
   * Round up to the nearest factor.
   *
   * @param value the value
   * @param factor the factor
   * @return the rounded value
   */
  public static double ceil(double value, double factor) {
    if (!Double.isFinite(value)) {
      return value;
    }
    return Math.ceil(value / factor) * factor;
  }

  /**
   * Interpolate between the two points. Create a straight line and then look up the y-value for x.
   * x may be outside the bounds.
   *
   * @param x1 the x value for point 1
   * @param y1 the y value for point 1
   * @param x2 the x value for point 2
   * @param y2 the y value for point 2
   * @param x the x
   * @return the y value
   */
  public static double interpolateY(double x1, double y1, double x2, double y2, double x) {
    // y = mx + c
    final double m = (y2 - y1) / (x2 - x1);
    // c = y - mx
    final double c = y1 - m * x1;
    return m * x + c;
  }

  /**
   * Interpolate between the two points. Create a straight line and then look up the x-value for y.
   * y may be outside the bounds.
   *
   * @param x1 the x value for point 1
   * @param y1 the y value for point 1
   * @param x2 the x value for point 2
   * @param y2 the y value for point 2
   * @param y the y
   * @return the x value
   */
  public static double interpolateX(double x1, double y1, double x2, double y2, double y) {
    // y = mx + c
    final double m = (y2 - y1) / (x2 - x1);
    // c = y - mx
    final double c = y1 - m * x1;
    // x = (y-c) / m
    return (y - c) / m;
  }

  /**
   * Compute the Euclidian distance between two 2D points.
   *
   * @param x1 the x value for point 1
   * @param y1 the y value for point 1
   * @param x2 the x value for point 2
   * @param y2 the y value for point 2
   * @return the distance
   */
  public static double distance(double x1, double y1, double x2, double y2) {
    return Math.sqrt(distance2(x1, y1, x2, y2));
  }

  /**
   * Compute the Euclidian distance between two 3D points.
   *
   * @param x1 the x value for point 1
   * @param y1 the y value for point 1
   * @param z1 the z value for point 1
   * @param x2 the x value for point 2
   * @param y2 the y value for point 2
   * @param z2 the z value for point 2
   * @return the distance
   */
  public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
    return Math.sqrt(distance2(x1, y1, z1, x2, y2, z2));
  }

  /**
   * Compute the Euclidian distance between two 2D points.
   *
   * @param x1 the x value for point 1
   * @param y1 the y value for point 1
   * @param x2 the x value for point 2
   * @param y2 the y value for point 2
   * @return the distance
   */
  public static float distance(float x1, float y1, float x2, float y2) {
    return (float) Math.sqrt(distance2(x1, y1, x2, y2));
  }

  /**
   * Compute the squared Euclidian distance between two 2D points.
   *
   * @param x1 the x value for point 1
   * @param y1 the y value for point 1
   * @param x2 the x value for point 2
   * @param y2 the y value for point 2
   * @return the squared distance
   */
  public static double distance2(double x1, double y1, double x2, double y2) {
    final double dx = x1 - x2;
    final double dy = y1 - y2;
    return (dx * dx + dy * dy);
  }

  /**
   * Compute the squared Euclidian distance between two 3D points.
   *
   * @param x1 the x value for point 1
   * @param y1 the y value for point 1
   * @param z1 the z value for point 1
   * @param x2 the x value for point 2
   * @param y2 the y value for point 2
   * @param z2 the z value for point 2
   * @return the squared distance
   */
  public static double distance2(double x1, double y1, double z1, double x2, double y2, double z2) {
    final double dx = x1 - x2;
    final double dy = y1 - y2;
    final double dz = z1 - z2;
    return (dx * dx + dy * dy + dz * dz);
  }

  /**
   * Compute the squared Euclidian distance between two 2D points.
   *
   * @param x1 the x value for point 1
   * @param y1 the y value for point 1
   * @param x2 the x value for point 2
   * @param y2 the y value for point 2
   * @return the squared distance
   */
  public static float distance2(float x1, float y1, float x2, float y2) {
    final float dx = x1 - x2;
    final float dy = y1 - y2;
    return (dx * dx + dy * dy);
  }

  /**
   * Return value clipped to within the given bounds.
   *
   * @param lower the lower limit (inclusive)
   * @param upper the upper limit (inclusive)
   * @param value the value
   * @return the clipped value
   */
  public static double clip(double lower, double upper, double value) {
    if (value < lower) {
      return lower;
    }
    if (value > upper) {
      return upper;
    }
    return value;
  }

  /**
   * Return value clipped to within the given bounds.
   *
   * @param lower the lower limit (inclusive)
   * @param upper the upper limit (inclusive)
   * @param value the value
   * @return the clipped value
   */
  public static float clip(float lower, float upper, float value) {
    if (value < lower) {
      return lower;
    }
    if (value > upper) {
      return upper;
    }
    return value;
  }

  /**
   * Return value clipped to within the given bounds.
   *
   * @param lower the lower limit (inclusive)
   * @param upper the upper limit (inclusive)
   * @param value the value
   * @return the clipped value
   */
  public static int clip(int lower, int upper, int value) {
    if (value < lower) {
      return lower;
    }
    if (value > upper) {
      return upper;
    }
    return value;
  }

  /**
   * Get the argument to the power 2.
   *
   * @param value the value
   * @return value^2
   */
  public static double pow2(double value) {
    return value * value;
  }

  /**
   * Get the argument to the power 2. No check is made for overflow.
   *
   * @param value the value
   * @return value^2
   */
  public static int pow2(int value) {
    return value * value;
  }

  /**
   * Get the argument to the power 3.
   *
   * @param value the value
   * @return value^3
   */
  public static double pow3(double value) {
    return value * value * value;
  }

  /**
   * Get the argument to the power 3. No check is made for overflow.
   *
   * @param value the value
   * @return value^3
   */
  public static int pow3(int value) {
    return value * value * value;
  }

  /**
   * Get the argument to the power 4.
   *
   * @param value the value
   * @return value^4
   */
  public static double pow4(double value) {
    return pow2(pow2(value));
  }

  /**
   * Get the argument to the power 4. No check is made for overflow.
   *
   * @param value the value
   * @return value^4
   */
  public static int pow4(int value) {
    return pow2(pow2(value));
  }

  /**
   * Checks if the strictly positive number is a power of 2. Note a value of 1 will return true (as
   * this is 2^0).
   *
   * <p>Warning: The method should be used to test positive non-zero sizes are a power of 2. This
   * method is not valid for zero or negative integers. A value of zero returns true which is
   * incorrect. Negations of powers of 2 return false (e.g. -2, -4, etc) with the exception of
   * {@link Integer#MIN_VALUE} which returns true.
   *
   * <p>All values can be handled using:
   *
   * <pre>
   * {@code int value = ...;
   * boolean isPow2 = value > 0 && MathUtils.isPow2(value);
   * }
   * </pre>
   *
   * <p>Adapted from the JTransforms library class org.jtransforms.utils.CommonUtils.
   *
   * @param value the value
   * @return true, if is a power of 2
   */
  public static boolean isPow2(int value) {
    return ((value & (value - 1)) == 0);
  }

  /**
   * Returns the closest power-of-two number greater than or equal to value.
   *
   * <p>Warning: This will return {@link Integer#MIN_VALUE} for any value above {@code 1 << 30}.
   * This is the next power of 2 as an unsigned integer.
   *
   * <p>See <a href="https://graphics.stanford.edu/~seander/bithacks.html#RoundUpPowerOf2">Bit
   * Hacks: Rounding up to a power of 2</a>
   *
   * @param value the value
   * @return the closest power-of-two number greater than or equal to value
   * @throws IllegalArgumentException if the value is not strictly positive
   */
  public static int nextPow2(int value) {
    if (value < 1) {
      throw new IllegalArgumentException("value must be greater or equal 1");
    }
    int result = value - 1;
    result |= (result >>> 1);
    result |= (result >>> 2);
    result |= (result >>> 4);
    result |= (result >>> 8);
    return (result | (result >>> 16)) + 1;
  }

  /**
   * Return the log<sub>2</sub> of value rounded down to a power of 2. This is done by scanning for
   * the most significant bit of the value.
   *
   * <p>If value is negative or zero this will return Integer.MIN_VALUE (as negative infinity).
   *
   * @param value the value (must be positive)
   * @return floor(log<sub>2</sub>(x))
   */
  public static int log2(int value) {
    if (value <= 0) {
      return Integer.MIN_VALUE;
    }
    return 31 - Integer.numberOfLeadingZeros(value);
  }

  /**
   * Return the log<sub>2</sub> of value. Special cases:
   *
   * <ul>
   *
   * <li>If the argument is NaN or less than zero, then the result is NaN.
   *
   * <li>If the argument is positive infinity, then the result is positive infinity.
   *
   * <li>If the argument is positive zero or negative zero, then the result is negative infinity.
   *
   * </ul>
   *
   * @param value the value (must be positive)
   * @return log<sub>2</sub>(x)
   * @see Math#log(double)
   */
  public static double log2(double value) {
    return Math.log(value) * ONE_BY_LOG2;
  }

  /**
   * Check if value1 is zero and return zero else divide value1 by value2.
   *
   * @param value1 the first value
   * @param value2 the second value
   * @return value1/value2
   */
  public static double div0(double value1, double value2) {
    return (value1 == 0) ? 0 : value1 / value2;
  }

  /**
   * Check if value1 is zero and return zero else divide value1 by value2.
   *
   * @param value1 the first value
   * @param value2 the second value
   * @return value1/value2
   */
  public static float div0(float value1, float value2) {
    return (value1 == 0) ? 0 : value1 / value2;
  }

  /**
   * Check if value1 is zero and return zero else divide value1 by value2.
   *
   * @param value1 the first value
   * @param value2 the second value
   * @return value1/value2
   */
  public static double div0(int value1, int value2) {
    return (value1 == 0) ? 0 : (double) value1 / value2;
  }

  /**
   * Check if value1 is zero and return zero else divide value1 by value2.
   *
   * @param value1 the first value
   * @param value2 the second value
   * @return value1/value2
   */
  public static double div0(long value1, long value2) {
    return (value1 == 0) ? 0 : (double) value1 / value2;
  }

  /**
   * Checks if the value is a mathematical integer.
   *
   * <p>Note this may require a {@code long} integer type to store the value.
   *
   * @param value the value
   * @return true if the value is a mathematical integer
   */
  public static boolean isMathematicalInteger(double value) {
    return Double.isFinite(value) && Math.rint(value) == value;
  }

  /**
   * Checks if the value can be represented as an {@code integer}.
   *
   * @param value the value
   * @return true if the value is an integer
   */
  public static boolean isInteger(double value) {
    return ((int) (value) == value);
  }

  /**
   * Checks if the value can be represented as an {@code integer}.
   *
   * @param value the value
   * @return true if the value is an integer
   */
  public static boolean isInteger(float value) {
    return ((int) (value) == value);
  }

  /**
   * Returns the error function.
   *
   * <p>erf(x) = 2/&radic;&pi; <sub>0</sub>&int;<sup>x</sup> e<sup>-t*t</sup>dt </p>
   *
   * <p>This implementation computes erf(x) using the approximation by Abramowitz and Stegun. The
   * maximum absolute error is about 3e-7 for all x. </p>
   *
   * <p>The value returned is always between -1 and 1 (inclusive). If {@code abs(x) > 40}, then
   * {@code erf(x)} is indistinguishable from either 1 or -1 as a double, so the appropriate extreme
   * value is returned. </p>
   *
   * @param x the value.
   * @return the error function erf(x)
   */
  public static double erf(double x) {
    if (x < 0) {
      // Negate the symmetric result
      return -computeErf(-x);
    }
    return computeErf(x);
  }

  /**
   * Returns the error function of a positive value.
   *
   * <p>erf(x) = 2/&radic;&pi; <sub>0</sub>&int;<sup>x</sup> e<sup>-t*t</sup>dt </p>
   *
   * <p>This implementation computes erf(x) using the approximation by Abramowitz and Stegun. The
   * maximum absolute error is about 3e-7 for all x. </p>
   *
   * <p>The value returned is always between -1 and 1 (inclusive). If {@code abs(x) > 40}, then
   * {@code erf(x)} is indistinguishable from either 1 or -1 as a double, so the appropriate extreme
   * value is returned. </p>
   *
   * @param x the value.
   * @return the error function erf(x)
   */
  private static double computeErf(double x) {
    if (x > ERF_LIMIT) {
      // At the limit
      return 1;
    }

    final double x2 = x * x;
    final double x3 = x2 * x;
    return 1 - 1 / power16(1.0 + 0.0705230784 * x + 0.0422820123 * x2 + 0.0092705272 * x3
        + 0.0001520143 * x2 * x2 + 0.0002765672 * x2 * x3 + 0.0000430638 * x3 * x3);
  }

  private static double power16(double value) {
    double result = value * value; // power2
    result = result * result; // power4
    result = result * result; // power8
    return result * result;
  }

  /**
   * Compute the average of two indices using integer arithmetic.
   *
   * <p>This uses unsigned right shift to ensure the result is positive and avoid overflow.
   *
   * <pre>
   * {@code
   * (index1 + index2) >>> 1
   * }
   * </pre>
   *
   * <p>Note: Does not check that the input indices are positive.
   *
   * @param index1 the first index
   * @param index2 the second index
   * @return the average index
   */
  public static int averageIndex(int index1, int index2) {
    return (index1 + index2) >>> 1;
  }

  /**
   * Returns {@code log(1 + x) - x}. This function is accurate when {@code x -> 0}.
   *
   * <p>This function uses a Taylor series expansion when x is small ({@code |x| < 0.01}):
   *
   * <pre>
   * ln(1 + x) - x = -x^2/2 + x^3/3 - x^4/4 + ...
   * </pre>
   *
   * <p>or around 0 ({@code -0.791 <= x <= 1}):
   *
   * <pre>
   * ln(1 + x) = ln(a) + 2 [z + z^3/3 + z^5/5 + z^7/7 + ... ]
   *
   * z = x / (2a + x)
   * </pre>
   *
   * <p>For a = 1:
   *
   * <pre>
   * ln(x + 1) - x = -x + 2 [z + z^3/3 + z^5/5 + z^7/7 + ... ]
   *               = z * (-x + 2z [ 1/3 + z^2/5 + z^4/7 + ... ])
   * </pre>
   *
   * <p>The code is based on the {@code log1pmx} documentation for the <a
   * href="https://rdrr.io/rforge/DPQ/man/log1pmx.html">R DPQ package</a> with addition of the
   * direct Taylor series for tiny x.
   *
   * <p>See Abramowitz, M. and Stegun, I. A. (1972) Handbook of Mathematical Functions. New York:
   * Dover. Formulas 4.1.24 and 4.2.29, p.68. <a
   * href="https://en.wikipedia.org/wiki/Abramowitz_and_Stegun">Wikipedia: Abramowitz_and_Stegun</a>
   * provides links to the full text which is in public domain.
   *
   * @param x Value x
   * @return {@code log(1 + x) - x}
   */
  public static double log1pmx(double x) {
    if (x < -1) {
      return Double.NaN;
    }
    if (x == -1) {
      return Double.NEGATIVE_INFINITY;
    }
    // Use the threshold documented in the R implementation
    if (x < -0.79149064 || x > 1) {
      return Math.log1p(x) - x;
    }
    final double a = Math.abs(x);

    // Addition to the R version for small x.
    // Use a direct Taylor series:
    // ln(1 + x) = x - x^2/2 + x^3/3 - x^4/4 + ...
    // Reverse the summation (small to large) for a marginal increase in precision.
    // To stop the Taylor series the next term must be less than 1 ulp from the answer.
    // x^n/n < |log(1+x)-x| * eps
    // eps = machine epsilon = 2^-53
    // x^n < |log(1+x)-x| * eps
    // n < (log(|log(1+x)-x|) + log(eps)) / log(x)
    // In practice this is a conservative limit.

    // +/-0.015625: log1pmx = -0.00012081346403474586 : -0.00012335696813916864
    // n = 10.9974
    if (a < 0x1.0p-6) {
      final double x2 = x * x;
      final double x4 = x2 * x2;

      // +/-2.44140625E-4: log1pmx = -2.9797472637290841e-08 : -2.9807173914456693e-08
      // n = 6.49
      if (a < 0x1.0p-12) {

        // +/-9.5367431640625e-07: log1pmx = -4.547470617660916e-13 : -4.5474764000725028e-13
        // n = 4.69
        // @formatter:off
        if (a < 0x1.0p-20) {

          if (a < 0x1.0p-53) {
            // Below machine epsilon. Addition of x^3/3 is not possible.
            return -x2 / 2;
          }

          // n=5
          return x * x4 / 5 -
                     x4 / 4 +
                 x * x2 / 3 -
                     x2 / 2;
        }

        // n=7
        return x * x2 * x4 / 7 -
                   x2 * x4 / 6 +
                    x * x4 / 5 -
                        x4 / 4 +
                    x * x2 / 3 -
                        x2 / 2;
      }

      // n=11
      final double x8 = x4 * x4;
      return x * x2 * x8 / 11 -
                 x2 * x8 / 10 +
                  x * x8 /  9 -
                      x8 /  8 +
             x * x2 * x4 /  7 -
                 x2 * x4 /  6 +
                  x * x4 /  5 -
                      x4 /  4 +
                  x * x2 /  3 -
                      x2 /  2;
      // @formatter:on
    }

    // The use of the following series is faster converging:
    // ln(x + 1) - x = -x + 2 [z + z^3/3 + z^5/5 + z^7/7 + ... ]
    // z = x / (2 + x)
    // Test show this is more accurate when |x| > 1e-4 than the direct Taylor series.
    // The direct series can be modified to sum multiple terms together for an increase in
    // precision. The direct series takes approximately 3x longer to converge and cannot
    // be optimised with a threshold as high as 0.01.

    final double t = x / (2 + x);
    final double y = t * t;

    // Continued fraction
    // sum(k=0,...,Inf; y^k/(i+k*d)) = 1/3 + y/5 + y^2/7 + y^3/9 + ... )

    double sum = 1.0 / 3;
    double numerator = 1;
    int denominator = 3;
    for (;;) {
      numerator *= y;
      denominator += 2;
      final double sum2 = sum + numerator / denominator;
      // Since x <= 1 the additional terms will reduce in magnitude.
      // Iterate until convergence. Expected iterations:
      // x iterations
      // -0.79 38
      // -0.5 15
      // -0.1 5
      // 0.1 5
      // 0.5 10
      // 1.0 15
      if (sum2 == sum) {
        break;
      }
      sum = sum2;
    }
    return t * (2 * y * sum - x);
  }

  /**
   * Returns {@code pow(x, y) - 1}. This function is accurate when {@code x -> 1} or {@code y} is
   * small.
   *
   * @param x the x
   * @param y the y
   * @return {@code pow(x, y) - 1}
   */
  public static double powm1(double x, double y) {
    if (x > 0) {
      // Check for small y or x close to 1.
      // Require term < 0.5
      // => log(x) * y < 0.5
      // Assume log(x) ~ (x - 1) [true when x is close to 1]
      // => |(x-1) * y| < 0.5
      if ((Math.abs(y) < 0.25 || Math.abs((x - 1) * y) < 0.5)) {
        // Use expm1 when the term is close to 0.0 or negative (and result < 1)
        final double term = Math.log(x) * y;
        if (term < 0.5) {
          return Math.expm1(term);
        }
      }
      // Fall through to default
    } else if (x < 0
        // x is negative.
        // pow(x, y) only allowed if y is an integer.
        // if y is even then we can invert non-zero finite x.
        && Math.rint(y * 0.5) == y * 0.5) {
      return powm1(-x, y);
    }
    return Math.pow(x, y) - 1;
  }
}
