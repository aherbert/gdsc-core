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
import org.apache.commons.rng.sampling.distribution.SharedStateContinuousSampler;

/**
 * Sampling from the interval {@code [-1, 1)}.
 *
 * <p>Samples evenly from the 2<sup>54</sup> dyadic rationals in the range. Sampling uses 64-bits
 * per sample and branch conditions are avoided for efficient sampling. This sampler can be used as
 * an alternative to:
 *
 * <pre>
 * // Samples from 2^54 dyadic rationals with a branch condition
 * x = rng.nextDouble() - (rng.nextBoolean() ? 1.0 : 0);
 *
 * // Samples from 2^54 dyadic rationals with extra random bit required
 * x = rng.nextDouble() - rng.nextInt(1);
 *
 * // Samples from 2^53 dyadic rationals with no branch condition
 * x = 2 * rng.nextDouble() - 1.0;
 * </pre>
 *
 * @since 2.0
 */
public class SignedDoubleSampler implements SharedStateContinuousSampler {
  /** Underlying source of randomness. */
  private final UniformRandomProvider rng;

  /**
   * Class to sample from the range {@code [-1, 1)}.
   *
   * @param rng Generator of uniformly distributed random numbers.
   */
  public SignedDoubleSampler(UniformRandomProvider rng) {
    this.rng = rng;
  }

  @Override
  public double sample() {
    return NumberUtils.makeSignedDouble(rng.nextLong());
  }

  @Override
  public SharedStateContinuousSampler withUniformRandomProvider(UniformRandomProvider rng) {
    return new SignedDoubleSampler(rng);
  }
}
