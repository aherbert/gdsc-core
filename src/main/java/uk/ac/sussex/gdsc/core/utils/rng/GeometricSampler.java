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

package uk.ac.sussex.gdsc.core.utils.rng;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.AhrensDieterExponentialSampler;
import org.apache.commons.rng.sampling.distribution.DiscreteSampler;

/**
 * Sampling from a <a href="https://en.wikipedia.org/wiki/Geometric_distribution">geometric
 * distribution</a>.
 *
 * <p>This distribution samples the number of failures before the first success.
 *
 * <p>Computes the sample using an exponential distribution. If X is an exponentially distributed
 * random variable with parameter λ, then Y = floor(X) is a geometrically distributed random
 * variable with parameter p = 1 − e<sup>−λ</sup> (thus λ = −ln(1 − p)) and taking values in the set
 * {0, 1, 2, ...}.
 *
 * @see <a
 *      href="https://en.wikipedia.org/wiki/Geometric_distribution#Related_distributions">geometric
 *      distribution - related distributions</a>
 *
 * @since 2.0
 */
public class GeometricSampler implements DiscreteSampler {
  /** The mean of this distribution. */
  private final double mean;
  /** The sampler for the geometric distribution. */
  private final AhrensDieterExponentialSampler exponentialSampler;

  /**
   * Instantiates a new geometric distribution sampler. The samples will be provided in the set
   * {@code k=[0, 1, 2, ...]} where {@code k} indicates the number of failures before the first
   * success.
   *
   * <p>The mean is equal to {@code (1 - p) / p} where {@code p} is the probability of a successful
   * trial.
   *
   * <p>Note: The mean must be strictly positive.
   *
   * @param rng Generator of uniformly distributed random numbers
   * @param mean The mean of this distribution
   * @throws IllegalArgumentException if the mean is unsupported (e.g. negative, zero or infinite)
   */
  public GeometricSampler(UniformRandomProvider rng, double mean) {
    if (mean <= 0) {
      throw new IllegalArgumentException("Mean must be strictly positive: " + mean);
    }
    this.mean = mean;
    // Use a related exponential distribution:
    // λ = −ln(1 − p)
    // exponential mean = 1 / λ
    final double exponentialMean = 1.0 / (-Math.log(1.0 - getP(mean)));
    if (!Double.isFinite(exponentialMean)) {
      // Note on validation:
      // If the mean is large then p will approach 0 and the log function will approach 0.
      // This will result in the exponential mean approaching infinity so check this does not
      // occur.
      throw new IllegalArgumentException("Unsupported mean: " + mean);
    }
    exponentialSampler = new AhrensDieterExponentialSampler(rng, exponentialMean);
  }

  /**
   * Gets the mean.
   *
   * <p>This is equal to {@code (1 - p) / p} where {@code p} is the probability of a successful
   * trial.
   *
   * @return the mean
   */
  public double getMean() {
    return mean;
  }

  /**
   * Gets the probability of a successful trial.
   *
   * @return the success probability
   */
  public double getP() {
    return getP(mean);
  }

  /**
   * Gets the success probability. Computed as {@code 1 / (1 + mean)}.
   *
   * @param mean the mean
   * @return the success probability
   */
  private static double getP(double mean) {
    // Geometric distribution:
    // mean = (1 - p) / p
    return 1 / (1 + mean);
  }

  /**
   * Create a sample from a geometric distribution.
   *
   * <p>The sample will take the values in the set {0, 1, 2, ...}, equivalent to the number of
   * failures before the first success.
   */
  @Override
  public int sample() {
    // Return the floor of the exponential sample
    return (int) Math.floor(exponentialSampler.sample());
  }
}
