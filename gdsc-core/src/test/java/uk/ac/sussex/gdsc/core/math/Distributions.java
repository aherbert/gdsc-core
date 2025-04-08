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

package uk.ac.sussex.gdsc.core.math;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.ZigguratSampler;
import org.apache.commons.statistics.distribution.ContinuousDistribution;

/**
 * Contains methods for probability distributions.
 */
public final class Distributions {
  /**
   * Implement the exponential distribution parameterized by the rate (1 / mean).
   */
  private static class RateExponentialDistribution implements ContinuousDistribution {
    /** Support lower bound. */
    private static final double SUPPORT_LO = 0;
    /** Support upper bound. */
    private static final double SUPPORT_HI = Double.POSITIVE_INFINITY;
    /** ln(2). */
    private static final double LN_2 = 0.6931471805599453094172;

    /** The rate of this distribution. */
    private final double rate;
    /** The logarithm of the rate, stored to reduce computing time. */
    private final double logRate;

    /**
     * Create an instance.
     *
     * @param rate Rate of this distribution.
     */
    private RateExponentialDistribution(double rate) {
      this.rate = rate;
      logRate = Math.log(rate);
    }

    @Override
    public double density(double x) {
      if (x < SUPPORT_LO) {
        return 0;
      }
      return Math.exp(-x * rate) * rate;
    }

    @Override
    public double logDensity(double x) {
      if (x < SUPPORT_LO) {
        return Double.NEGATIVE_INFINITY;
      }
      return logRate - x * rate;
    }

    @Override
    public double cumulativeProbability(double x) {
      if (x <= SUPPORT_LO) {
        return 0;
      }
      return -Math.expm1(-x * rate);
    }

    @Override
    public double survivalProbability(double x) {
      if (x <= SUPPORT_LO) {
        return 1;
      }
      return Math.exp(-x * rate);
    }

    @Override
    public double probability(double x0, double x1) {
      if (x0 > x1) {
        throw new IllegalArgumentException(
            String.format("Lower bound %s > upper bound %s", x0, x1));
      }
      // Use the survival probability when in the upper domain:
      final double median = LN_2 / rate;
      if (x0 >= median) {
        return survivalProbability(x0) - survivalProbability(x1);
      }
      return cumulativeProbability(x1) - cumulativeProbability(x0);
    }

    @Override
    public double inverseCumulativeProbability(double p) {
      checkProbability(p);
      if (p == 1) {
        return Double.POSITIVE_INFINITY;
      }
      // Subtract from zero to prevent returning -0.0 for p=-0.0
      return 0 - Math.log1p(-p) / rate;
    }

    @Override
    public double inverseSurvivalProbability(double p) {
      checkProbability(p);
      if (p == 0) {
        return Double.POSITIVE_INFINITY;
      }
      // Subtract from zero to prevent returning -0.0 for p=1
      return 0 - Math.log(p) / rate;
    }

    @Override
    public double getMean() {
      return 1 / rate;
    }

    @Override
    public double getVariance() {
      return 1 / (rate * rate);
    }

    @Override
    public double getSupportLowerBound() {
      return SUPPORT_LO;
    }

    @Override
    public double getSupportUpperBound() {
      return SUPPORT_HI;
    }

    @Override
    public Sampler createSampler(UniformRandomProvider rng) {
      // Exponential distribution sampler.
      // Handle the edge case where the mean is infinite.
      final double mean = getMean();
      if (Double.isInfinite(mean)) {
        final ZigguratSampler.Exponential sampler = ZigguratSampler.Exponential.of(rng);
        return () -> sampler.sample() / rate;
      }
      return ZigguratSampler.Exponential.of(rng, mean)::sample;
    }

    /**
     * Check the probability {@code p} is in the interval {@code [0, 1]}.
     *
     * @param p Probability
     * @throws IllegalArgumentException if {@code p < 0} or {@code p > 1}
     */
    private static void checkProbability(double p) {
      if (p >= 0 && p <= 1) {
        return;
      }
      // Out-of-range or NaN
      throw new IllegalArgumentException("Invalid probability: " + p);
    }
  }

  /** No public construction. */
  private Distributions() {}

  /**
   * Return a new exponential distribution.
   *
   * <p>The probability density function of X is:
   *
   * <p>f(x; lambda) = lambda e^{-x * lambda}
   *
   * <p>This implementation uses the rate parameter {@code lambda} which is the inverse scale of the
   * distribution. A common alternative parameterization uses the scale parameter {@code mu} which
   * is the mean of the distribution. The distribution can be be created using
   * {@code lambda = 1 / mu}. For a parameterisation using the mean see
   * {@link org.apache.commons.statistics.distribution.ExponentialDistribution}.
   *
   * <p>Note this implementation is within a few ULP of a parameterisation using the mean. Only the
   * log density may be very different; this occurs as the x value approaches the mean.
   *
   * @param lambda the rate parameter
   * @return the continuous distribution
   * @throws IllegalArgumentException if {@code rate <= 0}.
   * @see <a href="https://en.wikipedia.org/wiki/Exponential_distribution">Exponential distribution
   *      (Wikipedia)</a>
   */
  public static ContinuousDistribution exponential(double lambda) {
    if (lambda <= 0) {
      throw new IllegalArgumentException("Invalid rate: " + lambda);
    }
    return new RateExponentialDistribution(lambda);
  }
}
