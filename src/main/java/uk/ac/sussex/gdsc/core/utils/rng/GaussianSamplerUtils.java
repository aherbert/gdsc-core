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

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.GaussianSampler;
import org.apache.commons.rng.sampling.distribution.NormalizedGaussianSampler;
import org.apache.commons.rng.sampling.distribution.ZigguratNormalizedGaussianSampler;

/**
 * A factory for creating GaussianSampler objects.
 */
public final class GaussianSamplerUtils {

  /** No construction. */
  private GaussianSamplerUtils() {}

  /**
   * Creates a new GaussianSampler.
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @param mean Mean of the Gaussian distribution.
   * @param standardDeviation Standard deviation of the Gaussian distribution.
   * @return the Gaussian sampler
   */
  public static GaussianSampler createGaussianSampler(UniformRandomProvider rng, double mean,
      double standardDeviation) {
    return new GaussianSampler(createNormalizedGaussianSampler(rng), mean, standardDeviation);
  }

  /**
   * Creates a new NormalizedGaussianSampler.
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @return the normalized Gaussian sampler
   */
  public static NormalizedGaussianSampler
      createNormalizedGaussianSampler(UniformRandomProvider rng) {
    return new ZigguratNormalizedGaussianSampler(rng);
  }
}
