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

import org.apache.commons.math3.distribution.FDistribution;

/**
 * Contains utilities for working with regression data.
 */
public final class RegressionUtils {
  /**
   * No public construction.
   */
  private RegressionUtils() {}

  /**
   * Gets the F statistic for two nested regression models. That is, model 1 has p1 parameters, and
   * model 2 has p2 parameters, where {@code p1 < p2}, and for any choice of parameters in model 1,
   * the same regression curve can be achieved by some choice of the parameters of model 2.
   *
   * <pre>
   * F = ((rss1 - rss2) / (p2 - p1)) / (rss2 / (n - p2))
   * </pre>
   *
   * <p>Under the null hypothesis that model 2 does not provide a significantly better fit than
   * model 1, F will have an F distribution, with (p2−p1, n−p2) degrees of freedom.
   *
   * <p>It is expected that the residual sum of squares for model 2 is lower or equal to model 1 and
   * {@code n > p2 > p1}.
   *
   * @param residualSumSquares1 The sum of squared residuals from model 1 (rss1)
   * @param numberOfParameters1 the number of parameters in model 1 (p1)
   * @param residualSumSquares2 The sum of squared residuals from model 2 (rss2)
   * @param numberOfParameters2 the number of parameters in model 2 (p2)
   * @param numberOfPoints the number of points (n)
   * @return the F statistic
   * @throws IllegalArgumentException if @{@code rss2 > rss1} or {@code p1 >= p2} or {@code p2 >= n}
   * @see <a href="https://en.wikipedia.org/wiki/F-test#Regression_problems">F-Test</a>
   */
  public static double residualsFStatistic(double residualSumSquares1, int numberOfParameters1,
      double residualSumSquares2, int numberOfParameters2, int numberOfPoints) {
    ValidationUtils.checkArgument(numberOfParameters1 < numberOfParameters2,
        "p1 (%d) should be < p2 (%d)", numberOfParameters1, numberOfParameters2);
    ValidationUtils.checkArgument(numberOfParameters2 < numberOfPoints,
        "p2 (%d) should be < n (%d)", numberOfParameters1, numberOfPoints);
    ValidationUtils.checkArgument(residualSumSquares2 <= residualSumSquares1,
        "rrs2 (%f) should be <= rss1 (%f)", residualSumSquares2, residualSumSquares1);
    final double num =
        (residualSumSquares1 - residualSumSquares2) / (numberOfParameters2 - numberOfParameters1);
    final double denom = residualSumSquares2 / (numberOfPoints - numberOfParameters2);
    return MathUtils.div0(num, denom);
  }

  /**
   * Returns the <i>observed significance level</i>, or <a
   * href="http://www.cas.lancs.ac.uk/glossary_v1.1/hyptest.html#pvalue">p-value</a>, associated
   * with a <a href="https://en.wikipedia.org/wiki/F-test#Regression_problems">F-Test for goodness
   * of fit</a> comparing two nested regression models. That is, model 1 has p1 parameters, and
   * model 2 has p2 parameters, where {@code p1 < p2}, and for any choice of parameters in model 1,
   * the same regression curve can be achieved by some choice of the parameters of model 2.
   *
   * <pre>
   * F = ((rss1 - rss2) / (p2 - p1)) / (rss2 / (n - p2))
   * </pre>
   *
   * <p>Under the null hypothesis that model 2 does not provide a significantly better fit than
   * model 1, F will have an F distribution, with (p2−p1, n−p2) degrees of freedom.
   *
   * <p>The number returned is the smallest significance level at which one can reject the null
   * hypothesis that model 2 does not provide a significantly better fit than model 1; a smaller
   * value is more significant.
   *
   * <p>The probability returned is the tail probability beyond
   * {@link #residualsFStatistic(double, int, double, int, int)} in the F distribution with the
   * specified degrees of freedom.
   *
   * <p>It is expected that the residual sum of squares for model 2 is lower than model 1 and
   * {@code n > p2 > p1}.
   *
   * @param residualSumSquares1 The sum of squared residuals from model 1 (rss1)
   * @param numberOfParameters1 the number of parameters in model 1 (p1)
   * @param residualSumSquares2 The sum of squared residuals from model 2 (rss2)
   * @param numberOfParameters2 the number of parameters in model 2 (p2)
   * @param numberOfPoints the number of points (n)
   * @return p-value
   * @throws IllegalArgumentException if @{@code rss2 > rss1} or {@code p1 >= p2} or {@code p2 >= n}
   * @see <a href="https://en.wikipedia.org/wiki/F-test#Regression_problems">F-Test</a>
   */
  public static double residualsFTest(double residualSumSquares1, int numberOfParameters1,
      double residualSumSquares2, int numberOfParameters2, int numberOfPoints) {
    final double f = residualsFStatistic(residualSumSquares1, numberOfParameters1,
        residualSumSquares2, numberOfParameters2, numberOfPoints);
    // Edge case when rss2 is zero.
    if (f == Double.POSITIVE_INFINITY) {
      return 0;
    }
    // pass a null rng to avoid unneeded overhead as we will not sample from this distribution
    final FDistribution distribution = new FDistribution(null,
        numberOfParameters2 - numberOfParameters1, numberOfPoints - numberOfParameters2);
    return 1.0 - distribution.cumulativeProbability(f);
  }

  /**
   * Performs a F-Test for two nested regression models evaluating the null hypothesis that model 2
   * does not provide a significantly better fit than model 1, with significance level
   * {@code alpha}. Returns true iff the null hypothesis can be rejected with
   * {@code 100 * (1 - alpha)} percent confidence.
   *
   * <p>Returns true iff {@link #residualsFTest(double, int, double, int, int) } {@code < alpha}</p>
   *
   * <p>It is expected that the residual sum of squares for model 2 is lower than model 1 and
   * {@code n > p2 > p1}.
   *
   * @param residualSumSquares1 The sum of squared residuals from model 1 (rss1)
   * @param numberOfParameters1 the number of parameters in model 1 (p1)
   * @param residualSumSquares2 The sum of squared residuals from model 2 (rss2)
   * @param numberOfParameters2 the number of parameters in model 2 (p2)
   * @param numberOfPoints the number of points (n)
   * @param alpha significance level of the test
   * @return true iff null hypothesis can be rejected with confidence 1 - alpha; that is model 2 is
   *         significantly better
   * @throws IllegalArgumentException if @{@code rss2 > rss1} or {@code p1 >= p2} or {@code p2 >= n}
   *         or {@code alpha} is not strictly greater than zero and less than or equal to 0.5
   * @see <a href="https://en.wikipedia.org/wiki/F-test#Regression_problems">F-Test</a>
   */
  public static boolean residualsFTest(double residualSumSquares1, int numberOfParameters1,
      double residualSumSquares2, int numberOfParameters2, int numberOfPoints, double alpha) {
    if (alpha <= 0 || alpha > 0.5) {
      throw new IllegalArgumentException("Alpha must be in the interval (0, 0.5]: " + alpha);
    }
    return residualsFTest(residualSumSquares1, numberOfParameters1, residualSumSquares2,
        numberOfParameters2, numberOfPoints) < alpha;
  }
}
