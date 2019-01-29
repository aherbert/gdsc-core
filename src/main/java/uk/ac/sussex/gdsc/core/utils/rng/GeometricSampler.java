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
import org.apache.commons.rng.sampling.distribution.DiscreteInverseCumulativeProbabilityFunction;
import org.apache.commons.rng.sampling.distribution.InverseTransformDiscreteSampler;

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
public class GeometricSampler extends InverseTransformDiscreteSampler {
  /** The probability of success. */
  private final double probabilityOfSuccess;

  /**
   * Define the inverse cumulative probability function for the Geometric distribution when the
   * probability of success is 1.
   */
  private static class Geometric1DiscreteInverseCumulativeProbabilityFunction
      implements DiscreteInverseCumulativeProbabilityFunction {

    /** An instance. */
    static final Geometric1DiscreteInverseCumulativeProbabilityFunction INSTANCE =
        new Geometric1DiscreteInverseCumulativeProbabilityFunction();

    @Override
    public int inverseCumulativeProbability(double cumulativeProbability) {
      // When p=1 then the sample is always 0
      return 0;
    }

    @Override
    public String toString() {
      return "Geometric";
    }
  }

  /**
   * Define the inverse cumulative probability function for the Geometric distribution.
   *
   * <p>Adapted from org.apache.commons.math3.distribution.GeometricDistribution.
   */
  public static class GeometricDiscreteInverseCumulativeProbabilityFunction
      implements DiscreteInverseCumulativeProbabilityFunction {

    /**
     * {@code log(1 - p)} where p is the probability of success.
     */
    private final double log1mProbabilityOfSuccess;

    /**
     * Instantiates a new geometric discrete inverse cumulative probability function.
     *
     * @param probabilityOfSuccess the probability of success
     */
    public GeometricDiscreteInverseCumulativeProbabilityFunction(double probabilityOfSuccess) {
      checkProbabilityOfSuccess(probabilityOfSuccess);
      log1mProbabilityOfSuccess = Math.log1p(-probabilityOfSuccess);
    }

    @Override
    public int inverseCumulativeProbability(double cumulativeProbability) {
      // This is the equivalent of floor(log(u)/ln(1-p))
      // where:
      // u = cumulative probability
      // p = probability of success
      // See: https://en.wikipedia.org/wiki/Geometric_distribution#Related_distributions
      // ---
      // Note: if cumulativeProbability == 0 then log1p(-0) is zero and the result
      // after the range check is 0.
      // Note: if cumulativeProbability == 1 then log1p(-1) is negative infinity, the result of
      // the divide is positive infinity and the result after the range check is Integer.MAX_VALUE.
      return Math.max(0,
          (int) Math.ceil(Math.log1p(-cumulativeProbability) / log1mProbabilityOfSuccess - 1));
    }

    @Override
    public String toString() {
      return "Geometric";
    }
  }

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
    super(rng, createFunction(probabilityOfSuccess));
    this.probabilityOfSuccess = probabilityOfSuccess;
  }

  /**
   * Creates the function.
   *
   * @param probabilityOfSuccess the probability of success
   * @return the discrete inverse cumulative probability function
   */
  private static DiscreteInverseCumulativeProbabilityFunction
      createFunction(double probabilityOfSuccess) {
    if (probabilityOfSuccess == 1) {
      return Geometric1DiscreteInverseCumulativeProbabilityFunction.INSTANCE;
    }
    return new GeometricDiscreteInverseCumulativeProbabilityFunction(probabilityOfSuccess);
  }

  /**
   * Factory method to create a sampler from the mean of the distribution.
   *
   * @param rng Generator of uniformly distributed random numbers
   * @param mean the mean
   * @return the geometric sampler
   * @throws IllegalArgumentException if {@code mean} is not positive
   */
  public static GeometricSampler createFromMean(UniformRandomProvider rng, double mean) {
    return new GeometricSampler(rng, getProbabilityOfSuccess(mean));
  }

  /**
   * Gets the mean of the Geometric distribution from the probability of success.
   *
   * <p>This is equal to {@code (1 - p) / p} where {@code p} is the probability of a successful
   * trial.
   *
   * @param probabilityOfSuccess the probability of success
   * @return the mean
   * @throws IllegalArgumentException if {@code probabilityOfSuccess} is not in the range [0 <
   *         probabilityOfSuccess <= 1]
   */
  public static double getMean(double probabilityOfSuccess) {
    checkProbabilityOfSuccess(probabilityOfSuccess);
    return (1 - probabilityOfSuccess) / probabilityOfSuccess;
  }

  /**
   * Gets the probability of success of the Geometric distribution from the mean.
   *
   * <p>Computed as {@code 1 / (1 + mean)}.
   *
   * @param mean the mean
   * @return the probability of success
   * @throws IllegalArgumentException if {@code mean} is not positive
   */
  public static double getProbabilityOfSuccess(double mean) {
    ValidationUtils.checkPositive(mean, "Mean");
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

  private static void checkProbabilityOfSuccess(double probabilityOfSuccess) {
    ValidationUtils.checkArgument(probabilityOfSuccess > 0 && probabilityOfSuccess <= 1,
        "Probability of success must be in the range [0 < p <= 1]: %f" + probabilityOfSuccess);
  }
}
