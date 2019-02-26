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
import org.apache.commons.rng.sampling.distribution.ContinuousUniformSampler;
import org.apache.commons.rng.sampling.distribution.DiscreteUniformSampler;

/**
 * A utility class for random numbers.
 */
public final class RngUtils {

  /** No construction. */
  private RngUtils() {}

  /**
   * Compute a uniformly distributed value between {@code min} and {@code max}.
   *
   * <pre>
   * {@code
   * min + rng.nextDouble() * (max - min);
   * }
   * </pre>
   *
   * <p>Note that if max is below min then the signs are reversed and the result is a valid number
   * within the specified range.
   *
   * <p>The result is undefined if the range is not finite.
   *
   * @param rng the source of randomness
   * @param min the minimum of the range
   * @param max the maximum of the range
   * @return the value
   */
  public static double nextDouble(UniformRandomProvider rng, double min, double max) {
    return new ContinuousUniformSampler(rng, min, max).sample();
  }

  /**
   * Compute a uniformly distributed value between {@code min} (inclusive) and {@code max}
   * (exclusive).
   * 
   * @param rng the source of randomness
   * @param min the minimum of the range (inclusive)
   * @param max the maximum of the range (exclusive)
   * @return the value
   * @throws IllegalArgumentException if {@code min > max}.
   */
  public static int nextInt(UniformRandomProvider rng, int min, int max) {
    return new DiscreteUniformSampler(rng, min, max - 1).sample();
  }
}
