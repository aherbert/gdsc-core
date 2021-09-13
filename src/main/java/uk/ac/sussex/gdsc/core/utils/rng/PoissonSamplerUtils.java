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

package uk.ac.sussex.gdsc.core.utils.rng;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.PoissonSamplerCache;
import org.apache.commons.rng.sampling.distribution.SharedStateDiscreteSampler;

/**
 * Class for computing Poisson samples handling the edge case where the mean is zero. In that case
 * the sample return is zero.
 */
public final class PoissonSamplerUtils {
  /**
   * Cache to use for Poisson samplers.
   *
   * <p>This is thread safe.
   */
  private static final PoissonSamplerCache poissonCache = new PoissonSamplerCache(0, 1000);

  /** A sampler to return zero for the sample() method. */
  private static final SharedStateDiscreteSampler ZERO = new SharedStateDiscreteSampler() {
    @Override
    public int sample() {
      return 0;
    }

    @Override
    public SharedStateDiscreteSampler withUniformRandomProvider(UniformRandomProvider rng) {
      return this;
    }
  };

  /** No construction. */
  private PoissonSamplerUtils() {}

  /**
   * Creates a new sampler from the Poisson distribution.
   *
   * <p>If the mean is zero this will return a sampler that always returns 0.
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @param mean Mean of the Poisson distribution.
   * @return the Poisson sampler
   * @throws IllegalArgumentException if {@code mean < 0}.
   */
  public static SharedStateDiscreteSampler createPoissonSampler(UniformRandomProvider rng,
      double mean) {
    if (mean == 0) {
      return ZERO;
    }
    return poissonCache.createSharedStateSampler(rng, mean);
  }

  /**
   * Creates a new Poisson sample.
   *
   * <p>If the mean is zero this will return 0.
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @param mean Mean of the Poisson distribution.
   * @return the Poisson sample
   * @throws IllegalArgumentException if {@code mean < 0}.
   */
  public static int nextPoissonSample(UniformRandomProvider rng, double mean) {
    if (mean == 0) {
      return 0;
    }
    return poissonCache.createSharedStateSampler(rng, mean).sample();
  }
}
