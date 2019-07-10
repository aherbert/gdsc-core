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

import org.apache.commons.rng.sampling.distribution.ContinuousSampler;
import org.apache.commons.rng.sampling.distribution.DiscreteSampler;

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
}
