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
 * Copyright (C) 2011 - 2019 Alex Herbert
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

import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.AhrensDieterMarsagliaTsangGammaSampler;
import org.apache.commons.rng.sampling.distribution.ContinuousSampler;
import org.apache.commons.rng.sampling.distribution.DiscreteSampler;
import org.apache.commons.rng.sampling.distribution.GaussianSampler;
import org.apache.commons.rng.sampling.distribution.InverseTransformDiscreteSampler;
import org.apache.commons.rng.sampling.distribution.NormalizedGaussianSampler;
import org.apache.commons.rng.sampling.distribution.ZigguratNormalizedGaussianSampler;

/**
 * Sampler utilities.
 *
 * @since 2.0
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
    final int[] samples = new int[size];
    for (int i = 0; i < size; i++) {
      samples[i] = sampler.sample();
    }
    return samples;
  }

  /**
   * Creates samples from the sampler.
   *
   * @param size the size
   * @param sampler the sampler
   * @return the samples
   */
  public static double[] createSamples(int size, ContinuousSampler sampler) {
    final double[] samples = new double[size];
    for (int i = 0; i < size; i++) {
      samples[i] = sampler.sample();
    }
    return samples;
  }

  /**
   * Creates a new {@link GaussianSampler}.
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @param mean Mean of the Gaussian distribution.
   * @param standardDeviation Standard deviation of the Gaussian distribution.
   * @return the sampler
   */
  public static GaussianSampler createGaussianSampler(UniformRandomProvider rng, double mean,
      double standardDeviation) {
    return new GaussianSampler(createNormalizedGaussianSampler(rng), mean, standardDeviation);
  }

  /**
   * Creates a new {@link NormalizedGaussianSampler}.
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @return the sampler
   */
  public static NormalizedGaussianSampler
      createNormalizedGaussianSampler(UniformRandomProvider rng) {
    return new ZigguratNormalizedGaussianSampler(rng);
  }

  /**
   * Creates a new {@link ContinuousSampler} for the Gamma distribution.
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @param shape the shape
   * @param scale the scale
   * @return the sampler
   */
  public static ContinuousSampler createGammaSampler(UniformRandomProvider rng, double shape,
      double scale) {
    // TODO: Swap these parameters when updating to v1.3.
    // Commons RNG v1.2 incorrectly interprets alpha as the scale and beta as the shape.
    // v1.3 fixes this to have alpha as the shape and beta the scale.
    return new AhrensDieterMarsagliaTsangGammaSampler(rng, scale, shape);
  }

  /**
   * Creates a new {@link DiscreteSampler} for the Binomial distribution.
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @param trials Number of trials.
   * @param probabilityOfSuccess Probability of success.
   * @return the sampler
   * @throws NotPositiveException if {@code trials < 0}.
   * @throws OutOfRangeException if {@code p < 0} or {@code p > 1}.
   */
  public static DiscreteSampler createBinomialSampler(UniformRandomProvider rng, int trials,
      double probabilityOfSuccess) {
    final BinomialDiscreteInverseCumulativeProbabilityFunction fun =
        new BinomialDiscreteInverseCumulativeProbabilityFunction(trials, probabilityOfSuccess);
    return new InverseTransformDiscreteSampler(rng, fun);
  }
}
