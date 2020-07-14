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
import org.apache.commons.rng.sampling.SharedStateSampler;

/**
 * Class for generating a random point within a unit circle.
 *
 * @since 2.0
 * @see <a href="https://mathworld.wolfram.com/DiskPointPicking.html">Disk point picking</a>
 */
public class UnitCircleSampler implements SharedStateSampler<UnitCircleSampler> {

  private static final double TWO_PI = 2 * Math.PI;

  /** The generator of uniformly distributed random numbers. */
  private final UniformRandomProvider rng;

  /**
   * Creates a new instance.
   *
   * @param rng Generator of uniformly distributed random numbers.
   */
  private UnitCircleSampler(UniformRandomProvider rng) {
    this.rng = rng;
  }

  /**
   * Create a new instance.
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @return the sampler
   */
  public static UnitCircleSampler of(UniformRandomProvider rng) {
    return new UnitCircleSampler(rng);
  }

  /**
   * Sample from the unit circle.
   *
   * @return the sample
   */
  public double[] sample() {
    // Generate a random point within a circle uniformly.
    final double t = TWO_PI * rng.nextDouble();
    final double r = Math.sqrt(rng.nextDouble());
    final double x = r * Math.cos(t);
    final double y = r * Math.sin(t);
    return new double[] {x, y};
  }

  @Override
  public UnitCircleSampler withUniformRandomProvider(UniformRandomProvider rng) {
    return of(rng);
  }
}
