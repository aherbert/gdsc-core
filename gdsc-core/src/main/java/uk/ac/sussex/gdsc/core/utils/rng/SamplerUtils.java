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
 * Copyright (C) 2011 - 2023 Alex Herbert
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

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.AhrensDieterMarsagliaTsangGammaSampler;
import org.apache.commons.rng.sampling.distribution.ContinuousSampler;
import org.apache.commons.rng.sampling.distribution.DiscreteSampler;
import org.apache.commons.rng.sampling.distribution.GaussianSampler;
import org.apache.commons.rng.sampling.distribution.GeometricSampler;
import org.apache.commons.rng.sampling.distribution.InverseTransformDiscreteSampler;
import org.apache.commons.rng.sampling.distribution.NormalizedGaussianSampler;
import org.apache.commons.rng.sampling.distribution.SharedStateContinuousSampler;
import org.apache.commons.rng.sampling.distribution.SharedStateDiscreteSampler;
import org.apache.commons.rng.sampling.distribution.ZigguratSampler;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

/**
 * Sampler utilities.
 */
public final class SamplerUtils {

  /** No construction. */
  private SamplerUtils() {}

  /**
   * Creates samples from the sampler.
   *
   * @param size the size
   * @param sampler the sampler
   * @return the samples
   */
  public static int[] createSamples(int size, DiscreteSampler sampler) {
    return IntStream.generate(sampler::sample).limit(size).toArray();
  }

  /**
   * Creates samples from the sampler.
   *
   * @param size the size
   * @param sampler the sampler
   * @return the samples
   */
  public static double[] createSamples(int size, ContinuousSampler sampler) {
    return DoubleStream.generate(sampler::sample).limit(size).toArray();
  }

  /**
   * Creates a new {@link SharedStateContinuousSampler} for the Gaussian distribution.
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @param mean Mean of the Gaussian distribution.
   * @param standardDeviation Standard deviation of the Gaussian distribution.
   * @return the sampler
   */
  public static SharedStateContinuousSampler createGaussianSampler(UniformRandomProvider rng,
      double mean, double standardDeviation) {
    return GaussianSampler.of(createNormalizedGaussianSampler(rng), mean, standardDeviation);
  }

  /**
   * Creates a new {@link NormalizedGaussianSampler}.
   *
   * @param <S> the type of sampler
   * @param rng Generator of uniformly distributed random numbers.
   * @return the sampler
   */
  public static <S extends NormalizedGaussianSampler & SharedStateContinuousSampler> S
      createNormalizedGaussianSampler(UniformRandomProvider rng) {
    @SuppressWarnings("unchecked")
    final S s = (S) ZigguratSampler.NormalizedGaussian.of(rng);
    return s;
  }

  /**
   * Creates a new {@link SharedStateContinuousSampler} for the gamma distribution.
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @param shape the shape
   * @param scale the scale
   * @return the sampler
   */
  public static SharedStateContinuousSampler createGammaSampler(UniformRandomProvider rng,
      double shape, double scale) {
    return AhrensDieterMarsagliaTsangGammaSampler.of(rng, shape, scale);
  }

  /**
   * Creates a new {@link SharedStateDiscreteSampler} for the binomial distribution.
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @param trials Number of trials.
   * @param probabilityOfSuccess Probability of success.
   * @return the sampler
   * @throws NotPositiveException if {@code trials < 0}.
   * @throws OutOfRangeException if {@code p < 0} or {@code p > 1}.
   */
  public static SharedStateDiscreteSampler createBinomialSampler(UniformRandomProvider rng,
      int trials, double probabilityOfSuccess) {
    final BinomialDiscreteInverseCumulativeProbabilityFunction fun =
        new BinomialDiscreteInverseCumulativeProbabilityFunction(trials, probabilityOfSuccess);
    return InverseTransformDiscreteSampler.of(rng, fun);
  }

  /**
   * Creates a new {@link SharedStateDiscreteSampler} for the geometric distribution.
   *
   * @param rng Generator of uniformly distributed random numbers
   * @param probabilityOfSuccess the probability of success
   * @return the geometric sampler
   * @throws IllegalArgumentException if {@code mean} is not positive
   */
  public static SharedStateDiscreteSampler createGeometricSampler(UniformRandomProvider rng,
      double probabilityOfSuccess) {
    return GeometricSampler.of(rng, probabilityOfSuccess);
  }

  /**
   * Creates a new {@link SharedStateDiscreteSampler} for the geometric distribution.
   *
   * @param rng Generator of uniformly distributed random numbers
   * @param mean the mean
   * @return the geometric sampler
   * @throws IllegalArgumentException if {@code mean} is not positive
   */
  public static SharedStateDiscreteSampler createGeometricSamplerFromMean(UniformRandomProvider rng,
      double mean) {
    ValidationUtils.checkPositive(mean, "Mean");
    final double probabilityOfSuccess = 1 / (1 + mean);
    return createGeometricSampler(rng, probabilityOfSuccess);
  }

  /**
   * Creates a new {@link SharedStateContinuousSampler} for the exponential distribution with a mean
   * of 1.
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @return the sampler
   */
  public static SharedStateContinuousSampler createExponentialSampler(UniformRandomProvider rng) {
    return ZigguratSampler.Exponential.of(rng);
  }

  /**
   * Creates a new {@link SharedStateContinuousSampler} for the exponential distribution.
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @param mean Mean of the exponential distribution.
   * @return the sampler
   */
  public static SharedStateContinuousSampler createExponentialSampler(UniformRandomProvider rng,
      double mean) {
    return ZigguratSampler.Exponential.of(rng, mean);
  }
}
