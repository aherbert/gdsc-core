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

import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.AhrensDieterExponentialSampler;
import org.apache.commons.rng.sampling.distribution.DiscreteSampler;

/**
 * Sampling from a <a href="https://en.wikipedia.org/wiki/Geometric_distribution">geometric
 * distribution</a>.
 *
 * <p>This distribution samples the number of failures before the first success taking values in the
 * set {0, 1, 2, ...}.
 *
 * @see <a
 *      href="https://en.wikipedia.org/wiki/Geometric_distribution#Related_distributions">geometric
 *      distribution - related distributions</a>
 *
 * @since 2.0
 */
public class GeometricSampler implements DiscreteSampler {
  /** The probability of success. */
  private final double probabilityOfSuccess;
  /** The sampler for the geometric distribution. */
  private final AhrensDieterExponentialSampler exponentialSampler;

  /**
   * Instantiates a new geometric distribution sampler. The samples will be provided in the set
   * {@code k=[0, 1, 2, ...]} where {@code k} indicates the number of failures before the first
   * success.
   *
   * @param rng Generator of uniformly distributed random numbers
   * @param probabilityOfSuccess The probability of success
   * @throws IllegalArgumentException if {@code probabilityOfSuccess} is not in the range [0 <
   *         probabilityOfSuccess <= 1]
   */
  public GeometricSampler(UniformRandomProvider rng, double probabilityOfSuccess) {
    ValidationUtils.checkArgument(probabilityOfSuccess > 0 && probabilityOfSuccess <= 1,
        "Probability of success must be in the range [0 < p <= 1]: %f" + probabilityOfSuccess);
    this.probabilityOfSuccess = probabilityOfSuccess;
    // Use a related exponential distribution:
    // λ = −ln(1 − probabilityOfSuccess)
    // exponential mean = 1 / λ
    final double exponentialMean = 1.0 / (-Math.log(1.0 - probabilityOfSuccess));
    exponentialSampler = new AhrensDieterExponentialSampler(rng, exponentialMean);
  }

  /**
   * Gets the mean.
   *
   * <p>This is equal to {@code (1 - p) / p} where {@code p} is the probability of a successful
   * trial.
   *
   * @param probabilityOfSuccess the probability of success
   * @return the mean
   */
  public static double getMean(double probabilityOfSuccess) {
    return (1 - probabilityOfSuccess) / probabilityOfSuccess;
  }

  /**
   * Gets the probability of success.
   *
   * <p>Computed as {@code 1 / (1 + mean)}.
   *
   * @param mean the mean
   * @return the probability of success
   */
  public static double getProbabilityOfSuccess(double mean) {
    return 1 / (1 + mean);
  }

  /**
   * Gets the probability of a successful trial.
   *
   * @return the probability of success
   */
  public double getProbabilityOfSuccess() {
    return probabilityOfSuccess;
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
